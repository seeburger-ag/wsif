# WSIF – Performance & Thread-Safety Improvement Plan

Scope: usage of `org.apache.wsif` by **SeeBPEngine** (bpengine) and resulting improvements
in this WSIF fork.

- bpengine compiles against `org.apache.ws:wsif:0.20040603` (scope `provided`).
- This fork is `org.apache.ws:wsif:0.20260921`.

## 1. How bpengine uses WSIF

- **Provider registration:** `EngineConfiguration` static initializer calls
  `WSIFPluggableProviders.setAutoLoadProviders(true)`. It then calls
  `overrideDefaultProvider(...)` for JMS (`SeeWSIFJMSProvider`), Java (`SeeWSIFJavaProvider`)
  and EJB (`SeeWSIFEJBProvider`), and finally `WSIFServiceImpl.addExtensionRegistry(...)`.
- **Factory:** `ServiceInvoker` holds a static `WSIFServiceFactory.newInstance()`. Service
  caching is **off** (the default).
- **Per invoke activity:** `ServiceInvoker.invoke` → `createOperation`. `createOperation`
  switches the TCCL to the bpengine bundle class loader, then:
  1. `getPort(def, portType)` → `wsifServiceFactory.getService(def, service, portType).getPort()`
  2. `port.createOperation(op)`
  3. Several `operation.getContext()` / `setContext()` round trips.
  4. `executeRequestResponseOperation` / `executeInputOnlyOperation`.
- **WSDL `Definition`:** cached and shared across threads via bpengine `DefinitionsCache`.
  WSIF only reads it.

## 2. Per-invocation cost (everything is rebuilt per call)

| # | Location | Work done per call |
|---|---|---|
| 1 | `WSIFServiceImpl.init()` | `checkWSDL` walks all messages, portTypes and bindings of the definition.<br>`checkPortTypeInformation` walks all operations.<br>`isProviderAvailable` runs per port: a new `ArrayList`, plus `Trc.event` string concatenation evaluated even when tracing is off. |
| 2 | `WSIFMapperFactory.newMapper()`<br>`WSIFMappingConventionFactory.newMappingConvention()` | Each call takes a global `synchronized` lock and does `Class.forName` + reflective `newInstance`. This happens twice per `getService`. |
| 3 | `WSIFServiceImpl.getPort()` | `setupTypeMappings` (synchronized), then `createDynamicWSIFPort`. That runs **`checkWSDL` a second time**, calls `getProvider`, and clones the context. |
| 4 | Java provider | A new `WSIFPort_Java` per call. Its reflection caches (`Method[]`, constructors, resolved classes) are discarded after one use.<br>`SeeWSIFJavaPort` additionally calls `getDeclaredField` + `setAccessible` per call. |
| 5 | JMS provider | A new `SeeWSIFPort` per call, so its `operationInstances` cache never gets a hit.<br>The operation is constructed twice (`new` + `copy()`). Each constructor does 3 `WSIFProperties` lookups and possibly a `Class.forName`. |
| 6 | `SeeWSIFJMSDestination` | A new `JMSManager` per call. |
| 7 | `ServiceInvoker` | 2–3 context clone round trips per call. |
| 8 | `WSIFProperties` (**confirmed: not found at runtime in bpengine; fixed – see §5**) | `wsif.properties` is a root resource of the WSIF bundle. It is loaded through the TCCL, which bpengine sets to its own bundle class loader, so the file is not visible.<br>Because the failure was not cached, every `getProperty` call did three things: a bundle classpath search, `Properties.load(null)` throwing a swallowed NPE, and (for the timeout getters) `Long.parseLong(null)` throwing a swallowed NFE.<br>Each `SeeWSIFJMSOperation` constructor makes 3 lookups and runs twice per call. That adds up to **about 6 resource lookups and 10 thrown exceptions per JMS invocation**. When the file *was* found, the stream was never closed. |

## 3. Thread-safety findings

| Severity | Location | Issue |
|---|---|---|
| High | `WSIFPluggableProviders` | `defaultNSProviders` (`HashMap`), `providersFromSPIFiles` and `autoLoadProviders` are static and accessed without synchronization from all invocation threads.<br>The lazy `new HashMap()` and `put` on a cache miss can corrupt the map or lose the `overrideDefaultProvider` entries.<br>The cache-hit path also misses its `Trc.exit`. |
| Medium | `WSIFMapperFactory`<br>`WSIFMappingConventionFactory` | Correct, but a global lock on every service creation. |
| Medium | `WSIFServiceFactoryImpl` | `useCache` and `cache` are not volatile. Irrelevant while caching is off; must be fixed before enabling it. |
| High (with caching ON) | `WSIFServiceCache` | *Found in Phase 1; the original analysis wrongly listed it as synchronized.* `get`/`put` override `Hashtable` **without** `synchronized`, and `get` relinks the LRU list. Concurrent use corrupts the list: `NullPointerException` in the eviction stress test.<br>Single-threaded bugs as well:<br>- re-putting a key leaves a stale node, and the next eviction drops the fresh entry;<br>- a node moved to the top keeps a stale `nextObject` link;<br>- `setCacheSize(< 10)` yields a cushion of 0, so the cache grows without bound. |
| Medium (with caching ON) | `WSIFServiceFactoryImpl` cache miss | *Found in Phase 1.* The instance returned to the caller was also the cached one. The caller mutates it (`getPort` type mappings, `mapType`, `setContext`) while other threads copy it. |
| Medium | `WSIFServiceFactoryImpl.genCacheKey` | The key is based on `documentBaseURI`. Different process versions with the same base URI would collide and could get a stale service. |
| Medium | `WSIFServiceImpl` copy constructor | Shares `mapper` and `mapCon` between cached copies. Verify they are stateless before enabling caching. |
| Low | `WSIFProperties.properties`, `Trc.ON` | Non-volatile lazily-set statics. The race is benign but formally incorrect. |
| Low | `SeeWSIFJMSOperation` (bpengine) | `synchronized (cs)` holds a global lock around the JMS send in the async path with a handler. bpengine never passes a handler, so this path is currently unused. |
| Bug | `SeeWSIFJMSOperation` constructor (bpengine) | `clazz.isAssignableFrom(WSIFTransactionControl.class)` is inverted, so `transControl` is never set from `wsif.transactioncontrol.impl`. |

What is already fine:

- `WSIFServiceImpl`, ports and operations are effectively confined to one thread, because they
  are created per call.
- `WSIFPort_Java` in this fork is already safe to share (volatile caches, `ConcurrentHashMap`).
- ~~`WSIFServiceCache` is synchronized.~~ Wrong; see the table above. Fixed in Phase 1.2.

### Constraints – why ports must not simply be cached

- **Java ports:** each `WSIFPort_Java` owns its own service object instance. Sharing a port
  would share that instance across invocations and threads, which changes the behaviour of
  stateful services.
- **JMS ports:** `SeeWSIFJMSDestination` decides XA vs. non-XA from the transaction state at
  construction time. A shared port would freeze that decision.

So: cache **immutable metadata and service templates**, and keep creating a fresh port per call.

## 4. Plan

### Phase 0 – Baseline (mandatory: this fork has no tests)

1. ✅ Benchmark harness: `test/java/org/apache/wsif/bench/WSIFBaselineBenchmarkTest`.
   - JMH is not in the offline repository, so this is a time-based JUnit harness.
   - It uses generated Java-binding WSDLs, and emulates the `ServiceInvoker` call pattern
     plus the bpengine TCCL that hides `wsif.properties`.
   - Results: see §6.
2. ✅ Concurrency stress tests: `test/java/org/apache/wsif/bench/WSIFConcurrencyStressTest`.
   Results: see §6.
3. ⏳ Take a JFR profile of bpengine under load (still open; must be done in bpengine).
4. ✅ ~~Verify #8 above: is `wsif.properties` visible through the bpengine TCCL?~~
   Confirmed: it is not visible.

How to run (PowerShell; quote the `-D` arguments):

```powershell
mvn -o test '-Dtest=WSIFBaselineBenchmarkTest' '-Dwsif.bench=true' '-Dwsif.bench.label=<name>'
#   optional: '-Dwsif.bench.warmup=1000' '-Dwsif.bench.measure=2000' '-Dwsif.bench.threads=1,2,4,8,16'
#   report:   target/bench/wsif-baseline-<name>.md
mvn -o test '-Dtest=WSIFConcurrencyStressTest' '-Dwsif.stress=true'
#   optional: '-Dwsif.stress.threads=8' '-Dwsif.stress.rounds=2000' '-Dwsif.stress.iterations=2000'
```

Both suites are skipped by default, so `mvn -o clean package` is unaffected.

**JMS is not covered.** A JMS round trip needs a broker, and bpengine's JMS port
(`SeeWSIFPort`) lives in bpengine. JMS costs (`SeeWSIFJMSOperation`, `JMSManager`) must be
measured in bpengine, with JFR (item 3).

### Phase 1 – Thread-safety in WSIF (low risk, no behaviour change) – ✅ done, see §7

1. ✅ `WSIFPluggableProviders`:
   - `defaultNSProviders` is a final `ConcurrentHashMap`.
   - The SPI provider list is immutable behind a `volatile` field.
   - `autoLoadProviders` is volatile and the writing methods are synchronized.
   - The missing `Trc.exit` calls were added.
   - `isProviderAvailable(ns1, ns2)` is memoized and invalidated on override and auto-load changes.
2. ✅ `WSIFServiceFactoryImpl`: a single `volatile` cache reference. Extended in Phase 1:
   - `WSIFServiceCache` made thread-safe and its LRU bugs fixed;
   - the cache holds a private, pre-initialised template.
3. ✅ `WSIFProperties` – see §5.
4. ✅ `Trc.ON` is volatile, and string-building trace calls on hot paths are guarded with `if (Trc.ON)`.

### Phase 2 – Per-call cost and scalability in WSIF (re-evaluated, see §8) – ✅ done, see §9

The original Phase 2 list was re-evaluated against JFR profiles (§8). The new order and scope:

| # | Item | Evidence (§8) | Replaces |
|---|---|---|---|
| 2.1 | **Lazy mapper / mapping convention** in `WSIFServiceImpl` | 93% of contended lock time; never used by bpengine | old 2.2 |
| 2.2 | **Skip type mapping when there is nothing to map** | 11% CPU, **30% of allocation** | – (new) |
| 2.3 | **Validate a `Definition` / `PortType` once** (memoized), drop the 2nd `checkWSDL` in `getPort` | **37% CPU**, wsdl4j `Vector` contention | old 2.1 |
| 2.4 | **Identity-keyed service cache** for the `Definition` variants, safe against stale and leaked definitions | caching ON: −45% CPU, −50% allocation; fixes the stale-definition defect | old 2.3 |
| 2.5 | **Cheaper context propagation** (drop the redundant 2nd clone, no copy of an empty service context) | 4% CPU, 5–8% allocation | – (new) |
| 2.6 | **Share the Java binding type map per `Binding`** plus `protected getFieldTypeMaps()` | `WSIFPort_Java.<init>` 3% CPU, 8% allocation; also paid by bpengine's `SeeWSIFJavaPort` | old 2.5 (+ part of old 2.4) |
| – | ~~Shared reflection metadata for `WSIFOperation_Java`~~ | 15% CPU in the benchmark, **0% in bpengine**: `SeeWSIFJavaOperation` does not extend `WSIFOperation_Java` and does no reflection | old 2.4, **deferred** |
| – | Lock-free `WSIFMapperFactory` / `WSIFMappingConventionFactory` (public API) | irrelevant once 2.1 removes the calls from the hot path | old 2.2, optional |

Implementation details are in §8.3. Every item gets a regression test that runs in the
default build, and the benchmark and stress suites are rerun after each step.

### Phase 3 – bpengine

1. Avoid rebuilding services per call. Either:
   - (a) enable `WSIF_FEATURE_SERVICE_CACHING` after Phase 2.3, or
   - (b) keep a `ConcurrentHashMap<QName, WSIFServiceImpl>` template per `DefinitionsCache`
     and port type, and copy it per call. This is tied to the definition lifecycle, so
     redeployments are handled automatically.
2. Keep calling `service.getPort()` per call, to preserve the Java service-object and XA semantics.
3. `SeeWSIFJavaPort`: replace `getFieldTypeMapsFromParent()` (reflection) with the new getter.
4. `SeeWSIFPort` / `SeeWSIFJMSOperation`:
   - Build each operation once instead of `new` + `copy()`.
   - Read the sync/async timeouts and the transaction-control class once (static lazy holder).
   - Fix the inverted `isAssignableFrom` check.
5. `ServiceInvoker`: collect all context entries (engine group, context ID, serializers,
   process info, priority) and do one `getContext` / `setContext`.
6. Compile against this fork (`0.20260921`) and align the OSGi import range, so the new API
   can be used.
7. Later: check whether `JMSManager` can share a connection factory instead of being created
   per call.

### Suggested order (gain vs. risk)

1. Phase 0
2. Phase 1.1 and 1.3
3. Phase 3.1, 3.4 and 3.5
4. Phase 2.1, 2.2 and 2.4
5. The remaining items

### Risks

- WSIF test coverage is still thin: the Phase 1 unit tests, plus the opt-in benchmark and
  stress suites from Phase 0. Add a regression test with every change.
- A stale cache after redeployment is possible unless the cache is keyed by `Definition`
  identity or tied to `DefinitionsCache`.
- The Java service-object and XA semantics must be preserved.

## 5. Implemented changes

### `WSIFProperties` (Phase 1.3)

File: `src/org/apache/wsif/util/WSIFProperties.java`

- The lookup result is cached **per context class loader**, including "not found" (a
  `NOT_FOUND` sentinel).
  - The cache is a `WeakHashMap` guarded by itself, so class loaders of redeployed bundles
    can be garbage collected.
  - A `volatile` single-entry fast path for the most recently used loader makes the common
    case lock-free.
- Semantics are unchanged:
  - The file is still looked up via the TCCL.
  - A loader that cannot see the file still gets `null` for every property.
  - So the packaged sample `wsif.servicefactory=customfactory...` and the timeout settings
    still do **not** become active in bpengine.
- A missing resource no longer calls `Properties.load(null)`, so there is no swallowed NPE.
- The input stream is now closed.
- A null TCCL now yields "not found" instead of a `NullPointerException`.
- `getSyncTimeout()` / `getAsyncTimeout()` check for `null` before parsing, so there is no
  `NumberFormatException` when the property is absent. An invalid value still falls back to 0.
- Thread-safe: the previously racy non-volatile static field is gone.

Effect in bpengine:

- Per JMS invocation, about 6 bundle resource lookups and about 10 thrown exceptions are gone.
- After the first call, each lookup is a volatile read plus a reference compare.

Follow-up in bpengine (Phase 3.4) is still worthwhile: read timeouts and the transaction
control class once instead of per `SeeWSIFJMSOperation` construction.

## 6. Phase 0 baseline results (2026-10-19)

Environment: Windows 11, 20 CPUs, HotSpot 21.0.2, default heap. Benchmarks used 1 s warmup
and 2 s measurement, in a single JVM.

Full reports:

- `doc/bench/wsif-baseline-pre-fix-8773079.md` – before the `WSIFProperties` fix
- `doc/bench/wsif-baseline-head-8620b6b.md` – with the fix

**Measurement noise:** unchanged code paths differ by 10–20% between the two runs, and by
up to 40% at 16 threads. Only differences well above that are meaningful. Compare future
changes with ≥ 3 runs per variant.

### 6.1 Performance

| Measurement | pre-fix (8773079) | HEAD (8620b6b) | Notes |
|---|---:|---:|---|
| `WSIFProperties.getSyncTimeout()`, file hidden (bpengine case) | 1.15 µs, 1,480 B | 0.012 µs, 0 B | **~100× faster.** In bpengine the gain is larger, because the OSGi bundle resource lookup is more expensive than the test's `URLClassLoader`. |
| `getService`, small WSDL (5 ops, 10 msgs) | 1.2–1.6 µs | | 2.2 KB/op |
| `getService`, large WSDL (20 ops, 51 portTypes, 540 msgs) | 2.9–3.5 µs | | Grows with total WSDL size (whole-definition `checkWSDL`). |
| `getService + getPort`, large WSDL | 6.6–6.9 µs | | 8.0 KB/op. `getPort` costs as much as `getService` (second `checkWSDL`, new port, type maps). |
| Full sync invoke (`ServiceInvoker` pattern), large WSDL | 8.8–9.2 µs | | **11.8 KB allocated per invocation** for a trivial echo. |
| Same, with factory service caching ON | 4.9–5.1 µs | | **~45% faster, ~50% less allocation** – upper bound for Phase 2.3 / 3.1. |
| `WSIFMapperFactory.newMapper()` (plus `newMappingConvention()`, both once per `getService`) | 0.3–0.6 µs | | 288 B/op (+152 B) |

Scalability of the full sync invoke on 20 CPUs (total ops/s):

| Threads | 1 | 2 | 4 | 8 | 16 |
|---|---:|---:|---:|---:|---:|
| pre-fix | 102k | 219k | 335k | 295k | 284k |
| HEAD | 110k | 197k | 295k | 281k | 168k |

- Throughput **stops scaling at about 4 threads** and drops beyond that.
- `newMapper()` alone loses throughput from 1 to 2 threads (3.1M → 1.0M ops/s pre-fix) and
  stays flat after that. That is the effect of its global `synchronized` lock.
- The other likely contributors are allocation pressure (about 12 KB per call) and the
  synchronized `Hashtable`s. Confirm with JFR (Phase 0.3).

**Interpretation:**

- In absolute terms, WSIF costs about 10 µs of CPU per invocation in the Java path. That is
  small compared to a JMS round trip (milliseconds).
- The value of Phases 1–3 therefore lies in:
  - **scalability under concurrency** (global lock, allocation rate),
  - removing the **JMS-path per-call costs** that only exist in bpengine (`SeeWSIFJMSOperation`,
    `JMSManager`, formerly `WSIFProperties`),
  - the **correctness** issues below.

### 6.2 Thread safety and correctness (`WSIFConcurrencyStressTest`, 8 threads)

| Test | Result | Finding |
|---|---|---|
| `serviceCacheDoesNotReturnStaleDefinition` | ❌ **FAILS deterministically** | With service caching ON, a new `Definition` with the same `documentBaseURI` (a redeployed process) gets the service built from the **old** definition. This blocks enabling caching until Phase 2.3 is done. |
| `coldConcurrentProviderLookupIsConsistent` | ✅ passes, but | In **2000 of 2000** rounds, concurrent cold lookups ran SPI discovery several times in parallel and handed out **different provider instances** (unsynchronized lazy init). No nulls or exceptions were observed. |
| `overrideDefaultProviderIsNotLostUnderConcurrentLookup` | ✅ passes (0 of 2000 rounds) | The lost-update race is real by the Java Memory Model, but its window is too small to hit on x86 in practice. A 20000-round × 16-thread run exceeded 5 minutes and was aborted. Fix it in Phase 1.1 anyway; it costs nothing. |
| `concurrentInvocationWithSharedFactory` (caching OFF) | ✅ 16,000 invocations, 0 errors | bpengine's current pattern is functionally safe. |
| `concurrentInvocationWithServiceCaching` (caching ON) | ✅ 16,000 invocations, 0 errors | The copy-constructor path is safe under concurrency; only the cache key is wrong. |

### 6.3 Consequences for the plan

1. Phase 2.3 (identity-based cache key) is a **hard prerequisite** for any service caching
   (Phase 3.1 option a). Option b (template per `DefinitionsCache` in bpengine) avoids the
   problem by construction.
2. Phase 3.1 is the largest single win: about 45% CPU and 50% allocation per invocation.
3. Removing the `WSIFMapperFactory` / `WSIFMappingConventionFactory` global locks (Phase 2.2)
   matters for scalability beyond 4 threads. Re-measure with ≥ 3 runs after the change.
4. Still open in Phase 0: a JFR profile of bpengine under load, to quantify the JMS path and to
   confirm the scalability bottleneck.

## 7. Phase 1 – implementation and results (2026-10-20)

### 7.1 Changes

| File | Change |
|---|---|
| `util/WSIFPluggableProviders` | `ConcurrentHashMap` registry with `putIfAbsent`, so concurrent lookups converge on one provider and an override is never overwritten.<br>SPI discovery runs once per registry generation (double-checked locking on a volatile immutable list).<br>Memoized `isProviderAvailable`: the memo map is *replaced* on every change, so a racing computation cannot store a stale result in the live memo.<br>Missing `Trc.exit` calls added; trace strings guarded; `null` namespace handled (CHM does not accept null keys). |
| `base/WSIFServiceFactoryImpl` | `volatile WSIFServiceCache cache` (`null` = off) replaces `useCache` + `cache`. Every `getService` reads it once.<br>`setFeature` / `setFeatures` are synchronized.<br>On a cache miss a **private template** is cached, with type mappings set up once (`cacheTemplate`). The caller keeps its own instance. |
| `base/WSIFServiceCache` | `get` / `put` synchronized (on the `Hashtable` monitor).<br>Re-put of an existing key unlinks the old node.<br>A moved node gets `nextObject = null`.<br>Cushion is at least 1; eviction is null-safe; trace strings guarded. |
| `logging/Trc` | `ON` is `volatile`. |
| `providers/java/WSIFOperation_Java` | The per-operation constructor trace concatenation is guarded by `if (Trc.ON)`. |

Behaviour is unchanged for bpengine, which runs with caching off. With caching ON:

- the cached template is no longer mutated by callers;
- a caller's own `mapType` no longer leaks into later services from the cache.

### 7.2 Tests

New **unit tests that run in every build** (`mvn -o clean package`: 10 run, 7 opt-in skipped):

- `test/java/org/apache/wsif/base/WSIFServiceCacheTest` (5 tests): LRU eviction, re-put,
  small size bound, get/put-on-miss under eviction (checked after every operation), and
  concurrent get/put. **All 5 fail on the pre-Phase-1 code.**
- `test/java/org/apache/wsif/util/WSIFPluggableProvidersTest` (5 tests): SPI discovery, memo
  invalidation on override and removal, override wins, auto-load reset, null namespace.

New stress test `concurrentInvocationWithEvictingServiceCache`: 12 definitions with cache
size 4 and 8 threads. On the pre-Phase-1 code it fails with an
`NPE … "oldObject.prevObject" is null` (corrupted LRU list).

Stress suite results (8 threads):

| Test | Phase 0 | Phase 1 |
|---|---|---|
| cold provider lookup – rounds with duplicate SPI discovery | 2000 / 2000 | **0 / 2000** |
| override lost | 0 / 2000 | 0 / 2000 (now impossible by construction) |
| concurrent invocation, caching OFF / ON | ✅ / ✅ | ✅ / ✅ |
| concurrent invocation, evicting cache | ❌ NPE (pre-Phase-1 code) | ✅ |
| stale definition from cache | ❌ | ❌ still expected – Phase 2.3 |

### 7.3 Performance (`doc/bench/wsif-baseline-phase1.md` vs. HEAD baseline)

| Measurement | Phase 0 (HEAD 8620b6b) | Phase 1 | Change |
|---|---:|---:|---|
| `isProviderAvailable(java)` | 0.17 µs, 1,208 B | 0.07 µs, 120 B | memoized; −90% allocation |
| `getService` (both WSDL sizes) | 2,176 B/op | 1,136 B/op | **−48% allocation** |
| full sync invoke, caching OFF | 11,760 B/op | 10,672 B/op | −9% allocation; time within noise |
| full sync invoke, caching ON, large WSDL | 5.1 µs, 5,808 B | 4.5 µs, 5,688 B | within noise; private template costs nothing per hit |
| scalability 1 / 2 / 4 / 8 / 16 threads (k ops/s) | 110 / 197 / 295 / 281 / 168 | 103 / 204 / 308 / 228 / 183 | unchanged within noise |

Notes:

- A first Phase 1 run showed 2-thread throughput at 74k ops/s. An immediate re-run gave 204k,
  so it was machine noise; the re-run is the stored report.
- Throughput still drops beyond 4–8 threads. Phase 1 did not target that. It is the goal of
  Phase 2.2 (global mapper locks) and of Phase 3.1 (fewer allocations).

## 8. Phase 2 re-evaluation (2026-10-20)

### 8.1 Method

- **CPU and allocation:** JFR (`settings=profile`) recording of the benchmark, single thread.
  The samples were filtered to the bpengine-style invocation (`BenchSupport.invokeLikeBpEngine`)
  and aggregated by *inclusive* presence of each method on the stack.
- **Lock contention:** a separate JFR recording with 8 threads and
  `jdk.JavaMonitorEnter#threshold=0ms`, filtered the same way.
- **bpengine relevance:** each finding was checked against the bpengine sources:
  - which WSIF classes bpengine subclasses (`SeeWSIFJavaPort`, `SeeWSIFJavaOperation`, `SeeWSIFPort`);
  - which WSIF features bpengine uses: none (no `mapType` / `mapPackage`, no auto type mapping,
    no custom mapper, no service caching).

### 8.2 Findings

CPU, single thread (share of samples in the invocation, inclusive):

| Method | Share | Relevant for bpengine |
|---|---:|---|
| `WSIFServiceImpl.checkWSDL` (2× per invocation: `init` and `createDynamicWSIFPort`) | **37%** | yes – grows with total WSDL size |
| `WSIFOperation_Java.<init>` (`getMethods`, `getMethodArgumentClasses`, …) | 15% | **no** – `SeeWSIFJavaOperation` does not extend it |
| `WSIFServiceImpl.setupTypeMappings` → `WSIFDefaultMapper.getMappings` → `MappingHelper.populateWithStandardXMLJavaMappings` | **11%** | yes |
| `WSIFUtils.getBindingOperation` | 5% | partly |
| `WSIFServiceImpl.copyInitializedService` (caching ON only) | 5% | after Phase 3.1 |
| `WSIFDefaultOperation.getContext` + `WSIFDefaultMessage.clone` | 4% | yes |
| `WSIFPort_Java.<init>` (`buildTypeMap`) | 3% | yes (`SeeWSIFJavaPort` extends it) |
| `WSIFMapperFactory.newMapper` | 1% | yes, but see contention below |

Allocation (share of sampled allocation weight in the invocation, inclusive):

| Method | Share |
|---|---:|
| `setupTypeMappings` → standard XML→Java mapping table | **30%** |
| `WSIFOperation_Java.<init>` (not bpengine) | 22% |
| `WSIFDefaultMessage.setObjectPart` (payload, legitimate) | 16% |
| `WSIFPort_Java.<init>` | 8% |
| `genCacheKey` (caching ON) | 6% |
| context clones | 5% |

Lock contention, 8 threads (blocked time within the invocation):

| Monitor @ first WSIF frame | Share of blocked time |
|---|---:|
| `StringBuffer` @ `WSIFMapperFactory.newMapper` | **75%** |
| `StringBuffer` @ `WSIFMappingConventionFactory.newMappingConvention` | **18%** |
| wsdl4j `Vector` @ `WSIFOperation_Java.getOperation`, `WSIFServiceImpl.checkPortTypeInformation`, `init`, `getBindingOperation`, … | ~7% |

Root causes:

1. **The mapper is pure overhead for bpengine.**
   - Every `getService` creates a `WSIFDefaultMapper` (which creates its own
     `WSIFDefaultMappingConvention` in a field initialiser) plus another mapping convention.
     Both factories serialise on a global `StringBuffer` monitor and call `Class.forName` +
     `newInstance`.
   - The mapper is only consulted by `setupTypeMappings`, `mapType` and `mapPackage`. With
     auto type mapping off (the default and bpengine's setting) `schemaTypes` is empty. So
     `getMappings(new SchemaType[0])` builds the complete standard XML→Java table for three XSD
     namespaces and returns an empty result. That table is 30% of all allocation.
2. **Validation repeats on every call.**
   - `checkWSDL` walks every message, portType and binding of the whole definition, in `init`
     and again in `createDynamicWSIFPort`.
   - `checkPortTypeInformation` walks all operations of the port type.
   - The `Definition` objects are long-lived and shared (bpengine `DefinitionsCache`); the
     wsdl4j `Vector`s iterated here are also a source of contention.
3. **`WSIFDefaultMapper` is mutable, not stateless** (`typeMappings` via `overrideTypeMapping`,
   settable convention). The `WSIFServiceImpl` copy constructor shares it between cached copies,
   so a `mapType` on one copy would leak into the template and all later copies.
4. **wsdl4j model objects** (`DefinitionImpl`, `ServiceImpl`, `PortImpl`, `BindingImpl`,
   `PortTypeImpl`, `OperationImpl`, `MessageImpl`) do not override `equals` / `hashCode`, and
   Service/Port/Binding/PortType hold no reference to their `Definition`. So weak identity
   caches keyed on them are possible, provided the cached values do not reference the key.

### 8.3 Proposed implementation

**2.1 Lazy mapper and mapping convention** (`WSIFServiceImpl`)

- `init()` only records the configured class names (`WSIF_FEATURE_MAPPER_CLASS`,
  `WSIF_FEATURE_MAPPINGCONVENTION_CLASS`).
- A private `ensureMapper()` creates mapper and convention on first real use:
  `mapType` (both overloads), `mapPackage`, and `setupTypeMappings` when there are schema types.
  It runs under the existing `schemaTypes` lock.
- **Fail-fast is kept:** if a feature *explicitly* names a mapper or convention class, it is
  still created eagerly in `init()`, so a bad class name fails `getService` as before. The
  default classes cannot fail.
- The copy constructor no longer shares the mutable mapper. Copies start with `mapper == null`
  and the recorded class names, and create their own on first use.
  - Equivalent for every existing path: cache templates are taken before the caller can call
    `mapType`.
  - This fixes root cause 3.
- Effect: removes ~93% of contended lock time and 3 allocations + 2 `Class.forName` per `getService`.

**2.2 Skip type mapping when there is nothing to map** (`setupTypeMappings`)

- If the mapper was never created (no `mapType` / `mapPackage` call), auto type mapping is off,
  so `schemaTypes` is empty. Then set `typeMapInitialised = true` and return.
- This is exactly equivalent to today's result: `getMappings` on an empty array with the
  internally created default mapper returns an empty map.
- If the mapper exists (overrides, custom setup), the original code path runs unchanged.
- Effect: −11% CPU, −30% allocation per invocation.

**2.3 Validate once**

- Remove `checkWSDLForWSIF(def)` from `createDynamicWSIFPort`. `def` is the one validated in
  `init()`, or the copy of an already validated template.
- Memoize successful validations in a small static helper (`ValidatedWsdl`):
  - `checkWSDL` keyed per `Definition`, `checkPortTypeInformation` keyed per `PortType`;
  - a `WeakHashMap` (identity semantics, see finding 4) guarded by itself, plus a volatile
    last-hit fast path (same pattern as `WSIFProperties`);
  - **failures are not memoized**, so an invalid definition throws the same exception on every call.
- Assumption to document: a `Definition` is not structurally modified after its first
  successful use with WSIF. bpengine treats parsed definitions as immutable. An escape hatch
  (system property `wsif.validation.memoize=false`) costs nothing.
- Effect: up to −37% CPU (grows with WSDL size); removes most wsdl4j `Vector` iteration on
  shared definitions.

**2.4 Identity-keyed service cache** (`WSIFServiceFactoryImpl`, for the `Definition` variants)

- Cache structure: `WeakHashMap<Definition, Map<String, WSIFServiceImpl>>`.
  - The inner key is service QName + portType QName (or the given NS/name strings).
  - It is guarded by a lock, with lookups on the inner map outside the lock (`ConcurrentHashMap`).
- **The cached template must not reference its `Definition`.** Otherwise the weak key is never
  cleared (classic `WeakHashMap` value→key leak). So the template is stored with `def == null`,
  and the copy constructor takes the `Definition` from the caller:
  `new WSIFServiceImpl(template, def)`. Service and PortType hold no back-reference (finding 4).
- No LRU and no size bound: entries disappear with their definition (process undeployed → GC).
  - The existing LRU `WSIFServiceCache` and the `WSIF_FEATURE_SERVICE_CACHE_SIZE` feature stay
    for the URL-based `getService(String wsdlLoc, …)` variants.
  - URL-keyed caching keeps returning the service parsed from that URL at first use. That is
    the existing behaviour of those variants and stays unchanged.
- Fixes `serviceCacheDoesNotReturnStaleDefinition`.
- New tests:
  - a GC test: a `WeakReference` to a dropped `Definition` is cleared despite a cached template;
  - concurrent copy tests.
- **Consequence for Phase 3.1:** bpengine can use option (a). One line,
  `wsifServiceFactory.setFeature(WSIF_FEATURE_SERVICE_CACHING, Boolean.TRUE)`, instead of its own
  template management. Measured effect of caching ON: −45% CPU and −50% allocation per invocation.

**2.5 Cheaper context propagation**

- `WSIFDefaultOperation.getContext()`: `getWSIFPort().getContext()` already returns a private
  copy, so the additional `.clone()` is dropped.
- `WSIFServiceImpl.createDynamicWSIFPort`: if the service has no context, do not create and set
  an empty one. `WSIFDefaultPort.getContext()` returns a fresh empty message in that case
  anyway. The behaviour is identical for all ports that inherit `WSIFDefaultPort`'s context
  handling, which includes bpengine's `SeeWSIFPort`.
- Effect: −1 to 2 message allocations + clones per invocation (~4% CPU).

**2.6 Shared Java binding type map** (`WSIFPort_Java`)

- `buildTypeMap()` depends only on the `Binding`. Its result (QName → class-name `String` or
  `List`) is cached immutably in a `WeakHashMap<Binding, Map<QName, Object>>`. The value holds
  no reference to the binding.
- Add `protected Map<QName, Object> getFieldTypeMaps()`, so bpengine's `SeeWSIFJavaPort` can drop
  its reflective `getFieldTypeMapsFromParent()` (`getDeclaredField` + `setAccessible` per call).
- Class resolution (`resolvedTypeMaps`) stays per port, because it depends on the TCCL.

**Deferred: shared reflection metadata for `WSIFOperation_Java`** (old 2.4)

- It is 15% CPU in the benchmark but 0% in bpengine.
- A correct implementation needs a cache keyed by (port model, context class loader), whose
  values reference `Class` objects of that loader. That is a class-loader leak hazard in OSGi
  unless keyed very carefully.
- Not worth the risk for this project; revisit only for other WSIF users.

### 8.4 Expected effect and order

For the bpengine path with caching off, items 2.1 + 2.2 + 2.3 + 2.5 together are expected to give:

- **about −50% CPU per `getService` + `getPort`**;
- **about −40% allocation**;
- **about −95% lock contention**, which is the cause of the scaling stop at about 4 threads.

With 2.4 and caching enabled in bpengine (Phase 3.1a), `getService` becomes a cheap copy on top
of that.

Implementation order:

1. 2.1 + 2.2 together – they share code; the lazy mapper is what makes the 2.2 short-circuit
   exactly equivalent.
2. 2.3.
3. 2.5.
4. 2.4 – the largest design change; it unlocks the one-line Phase 3.1.
5. 2.6.

Measure after each step with ≥ 3 benchmark runs (noise, §6).

## 9. Phase 2 – implementation and results (2026-10-20)

Confirmed precondition: bpengine never modifies a parsed WSDL definition after its first
use with WSIF (required by 2.3 and 2.6).

### 9.1 Changes

| Item | File(s) | Change |
|---|---|---|
| 2.1 Lazy mapper | `base/WSIFServiceImpl` | `init()` records the mapper / mapping-convention class names; `ensureMappers()` creates both on first use (`mapType`, `mapPackage`, `setupTypeMappings` with schema types).<br>Classes named by a feature are still created eagerly, so a bad class name still fails `getService`.<br>The copy constructor no longer shares the mutable mapper (copies create their own). |
| 2.2 Skip empty type mapping | `base/WSIFServiceImpl.setupTypeMappings` | If no mapper exists and there are no schema types, the type map is marked initialised without building the standard mapping table. Exactly equivalent to the previous result. |
| 2.3 Validate once | new `base/ValidatedWsdl`, `base/WSIFServiceImpl` | `checkWSDLForWSIF` (per `Definition`) and `checkPortTypeInformation` (per `PortType`) remember successful validations in a weak identity cache with a two-slot lock-free fast path.<br>Failures are never remembered.<br>The redundant `checkWSDL` in `createDynamicWSIFPort` is removed.<br>Switch: `-Dwsif.validation.memoize=false`. |
| 2.4 Identity-keyed service cache | new `base/DefinitionServiceCache`, `base/WSIFServiceFactoryImpl`, `base/WSIFServiceImpl` | The four `getService(Definition, …)` variants use a `WeakHashMap<Definition, ConcurrentHashMap<key, template>>`, with the fast path published atomically.<br>Templates are stored with the definition detached (`detachDefinition()`) and copied with `new WSIFServiceImpl(template, def)`, so cached definitions stay collectable.<br>Not cached when an authenticating proxy re-reads the WSDL.<br>The URL variants keep the LRU `WSIFServiceCache`; the unused `Definition`-based `genCacheKey` methods are removed. |
| 2.5 Context copies | `base/WSIFDefaultOperation` | The redundant second clone of the port context is dropped for ports whose `getContext()` is WSIF's own copying implementation (`WSIFDefaultPort`, `ModelWSIFPort`; checked once per class via `ClassValue`). Other ports are still cloned.<br>*Not done:* skipping `setContext` on new ports when the service has no context. A foreign port might not cope with a `null` context, and the saving is a single small allocation. |
| 2.6 Shared Java type map | `providers/java/WSIFPort_Java` | `buildTypeMap()` results are cached per `Binding` (`WeakHashMap`; values do not reference the binding).<br>New `protected getFieldTypeMaps()` for subclasses such as bpengine's `SeeWSIFJavaPort`. |

Behaviour changes, all deliberate and documented in the code:

- A mapper class configured *globally* (system property or `wsif.properties`) but not loadable
  now fails only when a mapping is actually needed, not on every `getService`.
- Subclasses reading the `protected` fields `mapper` / `mapCon` directly see `null` until first
  use. There are no such subclasses in WSIF or bpengine.
- With service caching on, a new `Definition` object always gets its own service. This fixes
  the stale-definition defect; it is no longer keyed on `documentBaseURI`.

### 9.2 Tests

New unit tests (in every build; `mvn -o clean package`: 29 run, 7 opt-in skipped):

| Test | Covers |
|---|---|
| `base/WSIFServiceImplTest` (6) | no mapper unless needed; `mapType` / `mapPackage` create it; copies do not share it; explicit feature eager; bad feature class still fails `getService` |
| `base/ValidatedWsdlTest` (3) | validation remembered; invalid WSDL fails on every call and is never remembered; validated definition is still garbage-collectable |
| `base/WSIFDefaultOperationContextTest` (3) | operation context is still a private copy; WSIF's own ports recognised; overriding ports still cloned |
| `base/DefinitionServiceCacheTest` (4) | cached services are copies bound to the caller's definition; same base URI not mixed up; all four `Definition` variants; cached definition is garbage-collectable (verified to fail when the template keeps its definition) |
| `providers/java/WSIFPort_JavaTypeMapTest` (3) | ports of one binding share the type map; different bindings do not; shared map is immutable |

Stress suite (8 threads): **all 6 tests pass**, including `serviceCacheDoesNotReturnStaleDefinition`,
which had failed since Phase 0.

### 9.3 Performance (`doc/bench/wsif-phase2-ab.md`)

Measured as an interleaved A/B comparison: 3 runs of the committed Phase 1 code against 3 runs
of Phase 2, median per row.

- The machine was 2.5–3× slower than during the Phase 0/1 measurements, so only the A/B
  columns are comparable with each other.
- Control rows (unchanged code) agree within ±13%.

| Measurement (median) | Phase 1 | Phase 2 | Change |
|---|---:|---:|---:|
| `getService`, large WSDL | 10.9 µs | 0.83 µs | **−92%** |
| `getService + getPort`, large WSDL | 24.8 µs, 6,976 B | 1.6 µs, 1,200 B | **−94% time, −83% allocation** |
| `getService + getPort`, small WSDL | 11.6 µs | 2.4 µs | −79% |
| full sync invoke, large WSDL | 39.2 µs, 10,672 B | 12.5 µs, 4,864 B | **−68% time, −54% allocation** |
| full sync invoke, small WSDL | 21.7 µs | 11.1 µs | −49% |
| full sync invoke, caching ON, large WSDL | 21.0 µs | 12.6 µs | −40% |
| throughput 1 / 2 / 4 / 8 / 16 threads (k ops/s) | 27 / 53 / 91 / 105 / 114 | 76 / 146 / 224 / 224 / 198 | **2.0–2.8×** |

Observations:

- The expected −50% CPU (§8.4) is exceeded on the WSIF-owned part: `getService + getPort` is
  down by about 80–94% depending on WSDL size.
- The remaining per-invocation cost in the benchmark is mostly the stock Java provider's
  `WSIFOperation_Java` reflection, which bpengine does not use (`SeeWSIFJavaOperation`). So
  in bpengine the relative gain is expected to be larger.
- With caching off, the full invoke is now as fast as with caching on. Service caching
  (Phase 3.1) therefore brings little additional CPU benefit. It remains a safe option
  (identity-keyed, leak-free), but it is no longer a priority.
- Throughput still flattens beyond 4 threads in the benchmark. The remaining contention is no
  longer in WSIF's own code; profile bpengine under load (Phase 0.3) to find the next limit.



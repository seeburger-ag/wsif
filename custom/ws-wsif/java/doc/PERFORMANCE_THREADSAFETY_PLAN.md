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

### Phase 2 – Per-call cost in WSIF

1. `WSIFServiceImpl.createDynamicWSIFPort`: skip the second `checkWSDL`, since `init` already
   validated the definition. Alternatively, memoize the validation per `Definition` instance.
2. Mapper and mapping-convention factories: cache the resolved `Class` per
   (class loader, class name), using a volatile field or `ClassValue`. Take the lock only on
   first resolution.
3. Make service caching usable:
   - Key the cache by `Definition` identity (weak keys) instead of `documentBaseURI`.
   - Verify that `WSIFDefaultMapper` is stateless, or copy it in the copy constructor.
4. Java provider: move the reflection metadata (`Method[]`, constructors, resolved types) to a
   shared cache keyed by service class (`ClassValue`). New ports reuse the metadata, and each
   port still gets its own service object.
5. Add `protected Map getFieldTypeMaps()` to `WSIFPort_Java`, so subclasses no longer need reflection.

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

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
| Medium | `WSIFServiceFactoryImpl.genCacheKey` | The key is based on `documentBaseURI`. Different process versions with the same base URI would collide and could get a stale service. |
| Medium | `WSIFServiceImpl` copy constructor | Shares `mapper` and `mapCon` between cached copies. Verify they are stateless before enabling caching. |
| Low | `WSIFProperties.properties`, `Trc.ON` | Non-volatile lazily-set statics. The race is benign but formally incorrect. |
| Low | `SeeWSIFJMSOperation` (bpengine) | `synchronized (cs)` holds a global lock around the JMS send in the async path with a handler. bpengine never passes a handler, so this path is currently unused. |
| Bug | `SeeWSIFJMSOperation` constructor (bpengine) | `clazz.isAssignableFrom(WSIFTransactionControl.class)` is inverted, so `transControl` is never set from `wsif.transactioncontrol.impl`. |

What is already fine:

- `WSIFServiceImpl`, ports and operations are effectively confined to one thread, because they
  are created per call.
- `WSIFPort_Java` in this fork is already safe to share (volatile caches, `ConcurrentHashMap`).
- `WSIFServiceCache` is synchronized.

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

### Phase 1 – Thread-safety in WSIF (low risk, no behaviour change)

1. `WSIFPluggableProviders`:
   - Make `defaultNSProviders` a final `ConcurrentHashMap`.
   - Hold the SPI provider list as an immutable list behind a `volatile` field.
   - Make `autoLoadProviders` volatile and the writing methods synchronized.
   - Add the missing `Trc.exit` on the cache-hit path.
   - Memoize `isProviderAvailable(ns1, ns2)`; invalidate it on override and on auto-load changes.
2. `WSIFServiceFactoryImpl`: replace `useCache` and `cache` with a single `volatile` cache reference.
3. ✅ `WSIFProperties` – **done**, see §5.
4. `Trc`: make `ON` volatile, and put `if (Trc.ON)` around string-building trace calls on hot paths.

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

- There is no test coverage in WSIF, so Phase 0 is the only safety net.
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


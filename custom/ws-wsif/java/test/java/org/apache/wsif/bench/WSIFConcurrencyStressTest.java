/*
 * Copyright 2026 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.wsif.bench;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.bench.BenchSupport.Target;
import org.apache.wsif.providers.WSIFDynamicTypeMap;
import org.apache.wsif.spi.WSIFProvider;
import org.apache.wsif.util.WSIFPluggableProviders;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Phase 0 concurrency stress tests (doc/PERFORMANCE_THREADSAFETY_PLAN.md).
 * <p>
 * The tests assert the <em>correct</em> behaviour. Against the unmodified code base
 * some of them are expected to fail - that is the baseline evidence for the
 * thread-safety findings. They are opt-in and skipped by default:
 * <pre>
 * mvn -o test -Dtest=WSIFConcurrencyStressTest -Dwsif.stress=true
 * </pre>
 * Optional: {@code wsif.stress.threads} (default 8), {@code wsif.stress.rounds}
 * (default 2000), {@code wsif.stress.iterations} (default 2000).
 */
public class WSIFConcurrencyStressTest {

    private static final String MARKER_NS = "http://wsif.apache.org/bench/marker/";

    private int threads;
    private int rounds;
    private int iterations;

    @Before
    public void setUp() {
        assumeTrue("set -Dwsif.stress=true to run stress tests", BenchSupport.flag("wsif.stress"));
        threads = (int) BenchSupport.longProp("wsif.stress.threads", 8);
        rounds = (int) BenchSupport.longProp("wsif.stress.rounds", 2000);
        iterations = (int) BenchSupport.longProp("wsif.stress.iterations", 2000);
    }

    @After
    public void tearDown() {
        resetProviderRegistry();
    }

    /** Clears WSIFPluggableProviders' static state (both caches are set to null). */
    private static void resetProviderRegistry() {
        WSIFPluggableProviders.setAutoLoadProviders(false);
        WSIFPluggableProviders.setAutoLoadProviders(true);
    }

    /**
     * Finding: WSIFPluggableProviders lazily creates its static HashMap without
     * synchronization. A provider registered via overrideDefaultProvider (what
     * bpengine's EngineConfiguration does) can be lost when another thread performs
     * the first getProvider() at the same time.
     */
    @Test
    public void overrideDefaultProviderIsNotLostUnderConcurrentLookup() throws Exception {
        final WSIFProvider marker = new MarkerProvider();
        int lost = 0;
        final AtomicInteger errors = new AtomicInteger();
        final AtomicReference<Throwable> firstError = new AtomicReference<>();

        for (int r = 0; r < rounds; r++) {
            resetProviderRegistry();
            final CyclicBarrier barrier = new CyclicBarrier(threads);
            List<Thread> ts = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                final boolean registrar = i == 0;
                Thread t = new Thread(() -> {
                    try {
                        barrier.await();
                        if (registrar) {
                            WSIFPluggableProviders.overrideDefaultProvider(MARKER_NS, marker);
                        } else {
                            WSIFPluggableProviders.getProvider(BenchSupport.JAVA_NS);
                        }
                    } catch (Throwable e) {
                        errors.incrementAndGet();
                        firstError.compareAndSet(null, e);
                    }
                });
                ts.add(t);
                t.start();
            }
            for (Thread t : ts) {
                t.join();
            }
            if (WSIFPluggableProviders.getProvider(MARKER_NS) != marker) {
                lost++;
            }
        }
        System.out.println("[stress] overrideDefaultProvider lost in " + lost + " of " + rounds
            + " rounds, errors=" + errors.get());
        if (firstError.get() != null) {
            firstError.get().printStackTrace();
        }
        assertEquals("unexpected exceptions", 0, errors.get());
        assertEquals("overridden provider lost (rounds)", 0, lost);
    }

    /**
     * Finding: cold, concurrent provider lookups race on the unsynchronized static
     * caches. Every lookup must still return a provider without throwing. The
     * number of distinct provider instances handed out is reported (duplicate
     * SPI discovery), but not asserted.
     */
    @Test
    public void coldConcurrentProviderLookupIsConsistent() throws Exception {
        final AtomicInteger nulls = new AtomicInteger();
        final AtomicInteger errors = new AtomicInteger();
        final AtomicReference<Throwable> firstError = new AtomicReference<>();
        int roundsWithDuplicates = 0;

        for (int r = 0; r < rounds; r++) {
            resetProviderRegistry();
            final CyclicBarrier barrier = new CyclicBarrier(threads);
            final Set<Object> seen = Collections.synchronizedSet(
                Collections.newSetFromMap(new IdentityHashMap<>()));
            List<Thread> ts = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                Thread t = new Thread(() -> {
                    try {
                        barrier.await();
                        WSIFProvider p = WSIFPluggableProviders.getProvider(BenchSupport.JAVA_NS);
                        if (p == null) {
                            nulls.incrementAndGet();
                        } else {
                            seen.add(p);
                        }
                    } catch (Throwable e) {
                        errors.incrementAndGet();
                        firstError.compareAndSet(null, e);
                    }
                });
                ts.add(t);
                t.start();
            }
            for (Thread t : ts) {
                t.join();
            }
            if (seen.size() > 1) {
                roundsWithDuplicates++;
            }
        }
        System.out.println("[stress] cold provider lookup: nulls=" + nulls.get() + ", errors=" + errors.get()
            + ", rounds with >1 distinct provider instance=" + roundsWithDuplicates + " of " + rounds);
        if (firstError.get() != null) {
            firstError.get().printStackTrace();
        }
        assertEquals("unexpected exceptions", 0, errors.get());
        assertEquals("getProvider returned null", 0, nulls.get());
    }

    /**
     * bpengine pattern: one static factory shared by all threads, caching off.
     * All invocations must succeed and return the right result.
     */
    @Test
    public void concurrentInvocationWithSharedFactory() throws Exception {
        runConcurrentInvocations(WSIFServiceFactory.newInstance(), "caching OFF");
    }

    /**
     * Same with factory service caching ON (Phase 2 candidate): cached services are
     * copied per call, so concurrent use must remain correct.
     */
    @Test
    public void concurrentInvocationWithServiceCaching() throws Exception {
        WSIFServiceFactory f = WSIFServiceFactory.newInstance();
        f.setFeature(WSIFConstants.WSIF_FEATURE_SERVICE_CACHING, Boolean.TRUE);
        runConcurrentInvocations(f, "caching ON");
    }

    private void runConcurrentInvocations(final WSIFServiceFactory factory, String label) throws Exception {
        final Target t = BenchSupport.target(20, 50);
        final AtomicInteger wrong = new AtomicInteger();
        final AtomicInteger errors = new AtomicInteger();
        final AtomicReference<Throwable> firstError = new AtomicReference<>();
        final CyclicBarrier barrier = new CyclicBarrier(threads);
        List<Thread> ts = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            final int id = i;
            Thread th = new Thread(() -> {
                try {
                    barrier.await();
                    for (int k = 0; k < iterations; k++) {
                        String payload = "t" + id + "-" + k;
                        Object r = BenchSupport.invokeLikeBpEngine(factory, t, "op" + (k % t.operations), payload);
                        if (!payload.equals(r)) {
                            wrong.incrementAndGet();
                        }
                    }
                } catch (Throwable e) {
                    errors.incrementAndGet();
                    firstError.compareAndSet(null, e);
                }
            });
            ts.add(th);
            th.start();
        }
        for (Thread th : ts) {
            th.join();
        }
        System.out.println("[stress] concurrent invocation (" + label + "): threads=" + threads
            + ", iterations=" + iterations + ", wrong=" + wrong.get() + ", errors=" + errors.get());
        if (firstError.get() != null) {
            firstError.get().printStackTrace();
        }
        assertEquals("unexpected exceptions", 0, errors.get());
        assertEquals("wrong results", 0, wrong.get());
    }

    /**
     * Factory service cache under eviction pressure: more distinct definitions than
     * cache entries, so get/put/shrink interleave constantly. Before Phase 1.2 the
     * LRU bookkeeping of WSIFServiceCache was unsynchronized.
     */
    @Test
    public void concurrentInvocationWithEvictingServiceCache() throws Exception {
        final WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
        factory.setFeature(WSIFConstants.WSIF_FEATURE_SERVICE_CACHE_SIZE, Integer.valueOf(4));
        factory.setFeature(WSIFConstants.WSIF_FEATURE_SERVICE_CACHING, Boolean.TRUE);
        final Target[] targets = new Target[12];
        for (int i = 0; i < targets.length; i++) {
            String wsdl = BenchSupport.wsdl(3, 0, BenchEchoService.class.getName());
            targets[i] = new Target(BenchSupport.readDefinition("urn:wsif:bench:evict:" + i, wsdl), 3);
        }
        final AtomicInteger wrong = new AtomicInteger();
        final AtomicInteger errors = new AtomicInteger();
        final AtomicReference<Throwable> firstError = new AtomicReference<>();
        final CyclicBarrier barrier = new CyclicBarrier(threads);
        List<Thread> ts = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            final int id = i;
            Thread th = new Thread(() -> {
                try {
                    barrier.await();
                    for (int k = 0; k < iterations; k++) {
                        Target t = targets[(id + k) % targets.length];
                        String payload = "t" + id + "-" + k;
                        Object r = BenchSupport.invokeLikeBpEngine(factory, t, "op" + (k % 3), payload);
                        WSIFService s = factory.getService(t.def, t.service, t.portType);
                        if (!payload.equals(r) || s.getDefinition() != t.def) {
                            wrong.incrementAndGet();
                        }
                    }
                } catch (Throwable e) {
                    errors.incrementAndGet();
                    firstError.compareAndSet(null, e);
                }
            });
            ts.add(th);
            th.start();
        }
        for (Thread th : ts) {
            th.join();
        }
        System.out.println("[stress] concurrent invocation (evicting cache, 12 defs / size 4): threads="
            + threads + ", iterations=" + iterations + ", wrong=" + wrong.get() + ", errors=" + errors.get());
        if (firstError.get() != null) {
            firstError.get().printStackTrace();
        }
        assertEquals("unexpected exceptions", 0, errors.get());
        assertEquals("wrong results", 0, wrong.get());
    }

    /**
     * Finding: the factory service cache keys on the WSDL documentBaseURI. When a
     * definition is replaced (e.g. a redeployed bpengine process) but keeps its base
     * URI, the cache returns a service built from the OLD definition.
     */
    @Test
    public void serviceCacheDoesNotReturnStaleDefinition() throws Exception {
        WSIFServiceFactory f = WSIFServiceFactory.newInstance();
        f.setFeature(WSIFConstants.WSIF_FEATURE_SERVICE_CACHING, Boolean.TRUE);

        String base = "urn:wsif:bench:redeploy";
        Definition v1 = BenchSupport.readDefinition(base, BenchSupport.wsdl(3, 0, BenchEchoService.class.getName()));
        Definition v2 = BenchSupport.readDefinition(base, BenchSupport.wsdl(4, 0, BenchEchoService.class.getName()));
        Target t1 = new Target(v1, 3);
        Target t2 = new Target(v2, 4);

        WSIFService s1 = f.getService(t1.def, t1.service, t1.portType);
        assertSame(v1, s1.getDefinition());
        WSIFService s2 = f.getService(t2.def, t2.service, t2.portType);
        assertSame("service for redeployed definition was served from the cache of the old one",
            v2, s2.getDefinition());
        assertTrue(s2.getPort().createOperation("op3") != null);
    }

    /** Provider that only exists to be registered via overrideDefaultProvider. */
    private static final class MarkerProvider implements WSIFProvider {
        public WSIFPort createDynamicWSIFPort(Definition def, Service service, Port port,
            WSIFDynamicTypeMap typeMap) throws WSIFException {
            return null;
        }

        public String[] getBindingNamespaceURIs() {
            return new String[] {MARKER_NS};
        }

        public String[] getAddressNamespaceURIs() {
            return new String[] {MARKER_NS};
        }
    }
}


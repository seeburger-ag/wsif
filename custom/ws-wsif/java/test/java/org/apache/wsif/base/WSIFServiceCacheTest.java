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

package org.apache.wsif.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

/**
 * Regression tests for the LRU bookkeeping and thread-safety of
 * {@link WSIFServiceCache} (Phase 1.2 of doc/PERFORMANCE_THREADSAFETY_PLAN.md).
 */
public class WSIFServiceCacheTest {

    @Test
    public void evictsLeastRecentlyUsedEntry() {
        WSIFServiceCache cache = new WSIFServiceCache(10); // cushion = 1
        for (int i = 0; i < 10; i++) {
            cache.put("k" + i, "v" + i);
        }
        assertEquals("v0", cache.get("k0")); // k0 becomes most recently used
        cache.put("k10", "v10");             // evicts the oldest: k1

        assertNull(cache.get("k1"));
        assertEquals("v0", cache.get("k0"));
        assertEquals("v10", cache.get("k10"));
        assertEquals(10, cache.size());
        assertListConsistent(cache);
    }

    @Test
    public void rePutOfExistingKeyKeepsCacheConsistent() {
        WSIFServiceCache cache = new WSIFServiceCache(3); // cushion = 1
        cache.put("a", "a1");
        cache.put("b", "b1");
        assertEquals("a1", cache.put("a", "a2"));
        cache.put("a", "a3");
        cache.put("c", "c1");
        cache.put("d", "d1"); // full -> evicts the least recently used: b

        assertNull(cache.get("b"));
        assertEquals("a3", cache.get("a"));
        assertEquals("c1", cache.get("c"));
        assertEquals("d1", cache.get("d"));
        assertEquals(3, cache.size());
        assertListConsistent(cache);
    }

    @Test
    public void smallCacheSizeStaysBounded() {
        WSIFServiceCache cache = new WSIFServiceCache(100);
        cache.setCacheSize(5); // 10% of 5 used to be 0 -> the cache grew without limit
        for (int i = 0; i < 50; i++) {
            cache.put("k" + i, "v" + i);
        }
        assertTrue("size " + cache.size(), cache.size() <= 5);
        assertListConsistent(cache);
    }

    @Test
    public void getThenPutOnMissUnderEvictionKeepsCacheConsistent() {
        // The factory pattern: get, and put on a miss. 12 keys, room for 4.
        WSIFServiceCache cache = new WSIFServiceCache(4); // cushion = 1
        java.util.Random rnd = new java.util.Random(42);
        StringBuilder trace = new StringBuilder();
        for (int i = 0; i < 20_000; i++) {
            String key = "k" + rnd.nextInt(12);
            Object v = cache.get(key);
            trace.append("get ").append(key).append(v == null ? " miss; " : " hit; ");
            if (v == null) {
                cache.put(key, key);
                trace.append("put ").append(key).append("; ");
            }
            try {
                assertListConsistent(cache);
            } catch (AssertionError e) {
                int from = Math.max(0, trace.length() - 400);
                throw new AssertionError("after op " + i + ": ..." + trace.substring(from), e);
            }
        }
    }

    @Test
    public void concurrentGetAndPutKeepCacheConsistent() throws Exception {
        final WSIFServiceCache cache = new WSIFServiceCache(16);
        final int threads = 8;
        final CyclicBarrier barrier = new CyclicBarrier(threads);
        final AtomicReference<Throwable> failure = new AtomicReference<>();
        List<Thread> ts = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            Thread th = new Thread(() -> {
                try {
                    barrier.await();
                    ThreadLocalRandom rnd = ThreadLocalRandom.current();
                    for (int i = 0; i < 50_000; i++) {
                        String key = "k" + rnd.nextInt(64);
                        if (rnd.nextBoolean()) {
                            cache.put(key, key);
                        } else {
                            Object v = cache.get(key);
                            if (v != null && !key.equals(v)) {
                                throw new AssertionError("wrong value " + v + " for " + key);
                            }
                        }
                    }
                } catch (Throwable e) {
                    failure.compareAndSet(null, e);
                }
            });
            ts.add(th);
            th.start();
        }
        for (Thread th : ts) {
            th.join();
        }
        if (failure.get() != null) {
            throw new AssertionError("concurrent access failed", failure.get());
        }
        assertTrue("size " + cache.size(), cache.size() <= 16);
        assertListConsistent(cache);
    }

    /** The LRU list must contain exactly the entries of the table, linked both ways. */
    private static void assertListConsistent(WSIFServiceCache cache) {
        synchronized (cache) {
            int n = 0;
            WSIFServiceCache.LRUWrapper prev = null;
            for (WSIFServiceCache.LRUWrapper w = cache.oldestObject; w != null; w = w.nextObject) {
                assertSame("broken back link", prev, w.prevObject);
                assertTrue("list entry not in table: " + w.key, cache.containsKey(w.key));
                prev = w;
                n++;
                assertTrue("cycle in LRU list", n <= cache.size());
            }
            assertSame("newestObject is not the list tail", prev, cache.newestObject);
            assertEquals("list length vs. table size", cache.size(), n);
            assertEquals("size counter", cache.size(), cache.currentSize);
        }
    }
}



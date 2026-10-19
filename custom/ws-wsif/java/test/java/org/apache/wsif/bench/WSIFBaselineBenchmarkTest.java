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
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.bench.BenchSupport.Op;
import org.apache.wsif.bench.BenchSupport.Result;
import org.apache.wsif.bench.BenchSupport.Target;
import org.apache.wsif.mapping.WSIFMapperFactory;
import org.apache.wsif.mapping.WSIFMappingConventionFactory;
import org.apache.wsif.util.WSIFPluggableProviders;
import org.apache.wsif.util.WSIFProperties;
import org.junit.Test;

/**
 * Phase 0 baseline benchmark (doc/PERFORMANCE_THREADSAFETY_PLAN.md).
 * <p>
 * Opt-in, skipped by default:
 * <pre>
 * mvn -o test -Dtest=WSIFBaselineBenchmarkTest -Dwsif.bench=true -Dwsif.bench.label=baseline
 * </pre>
 * Optional properties: {@code wsif.bench.warmup} / {@code wsif.bench.measure}
 * (milliseconds per benchmark, default 1000 / 2000) and {@code wsif.bench.threads}
 * (comma separated, default {@code 1,2,4,8,16}).
 * <p>
 * The results are printed and written to {@code target/bench/wsif-baseline-<label>.md}.
 */
public class WSIFBaselineBenchmarkTest {

    @Test
    public void baseline() throws Exception {
        assumeTrue("set -Dwsif.bench=true to run benchmarks", BenchSupport.flag("wsif.bench"));

        final long warmup = BenchSupport.longProp("wsif.bench.warmup", 1000);
        final long measure = BenchSupport.longProp("wsif.bench.measure", 2000);
        final int[] threadCounts = BenchSupport.intsProp("wsif.bench.threads", "1,2,4,8,16");

        // Like ServiceInvoker: one static factory, caching off (default).
        final WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
        // For comparison only (not used by bpengine today): factory with service caching.
        final WSIFServiceFactory cachingFactory = WSIFServiceFactory.newInstance();
        cachingFactory.setFeature(WSIFConstants.WSIF_FEATURE_SERVICE_CACHING, Boolean.TRUE);

        final Target small = BenchSupport.target(5, 0);
        final Target large = BenchSupport.target(20, 50);

        // Sanity check before measuring anything.
        assertEquals("hello", BenchSupport.invokeLikeBpEngine(factory, small, "op0", "hello"));
        assertEquals("hello", BenchSupport.invokeLikeBpEngine(factory, large, "op7", "hello"));
        assertEquals("hello", BenchSupport.invokeLikeBpEngine(cachingFactory, large, "op7", "hello"));

        List<Result> results = new ArrayList<>();

        // ---- hot spots (single thread) ------------------------------------------
        String g = "Hot spots (1 thread, bpengine-like TCCL hiding wsif.properties)";
        results.add(withTccl(g, "WSIFPluggableProviders.getProvider(java)", 1, warmup, measure,
            () -> WSIFPluggableProviders.getProvider(BenchSupport.JAVA_NS)));
        results.add(withTccl(g, "WSIFPluggableProviders.isProviderAvailable(java)", 1, warmup, measure,
            () -> WSIFPluggableProviders.isProviderAvailable(BenchSupport.JAVA_NS, BenchSupport.JAVA_NS)));
        results.add(withTccl(g, "WSIFProperties.getSyncTimeout() [file hidden]", 1, warmup, measure,
            () -> WSIFProperties.getSyncTimeout()));
        results.add(BenchSupport.measure(g, "WSIFProperties.getSyncTimeout() [file visible]", 1, warmup, measure,
            () -> WSIFProperties.getSyncTimeout()));
        results.add(withTccl(g, "WSIFMapperFactory.newMapper()", 1, warmup, measure,
            () -> WSIFMapperFactory.newMapper()));
        results.add(withTccl(g, "WSIFMappingConventionFactory.newMappingConvention()", 1, warmup, measure,
            () -> WSIFMappingConventionFactory.newMappingConvention()));

        // ---- invocation path (single thread) ------------------------------------
        for (Target t : new Target[] {small, large}) {
            String desc = t.operations + " ops, " + t.def.getPortTypes().size() + " portTypes, "
                + t.def.getMessages().size() + " messages";
            String gi = "Invocation path (1 thread) - WSDL: " + desc;
            results.add(withTccl(gi, "getService", 1, warmup, measure,
                () -> factory.getService(t.def, t.service, t.portType)));
            results.add(withTccl(gi, "getService + getPort", 1, warmup, measure,
                () -> factory.getService(t.def, t.service, t.portType).getPort()));
            results.add(BenchSupport.measure(gi, "createOperation (ServiceInvoker.createOperation)", 1, warmup,
                measure, () -> BenchSupport.createOperationLikeBpEngine(factory, t, "op1")));
            results.add(BenchSupport.measure(gi, "full sync invoke (ServiceInvoker.invoke)", 1, warmup, measure,
                () -> BenchSupport.invokeLikeBpEngine(factory, t, "op1", "x")));
            results.add(BenchSupport.measure(gi, "full sync invoke, factory service caching ON", 1, warmup,
                measure, () -> BenchSupport.invokeLikeBpEngine(cachingFactory, t, "op1", "x")));
        }

        // ---- scalability --------------------------------------------------------
        String gs = "Scalability - full sync invoke, WSDL: " + large.operations + " ops / "
            + large.def.getPortTypes().size() + " portTypes";
        for (int n : threadCounts) {
            results.add(BenchSupport.measure(gs, "full sync invoke", n, warmup, measure,
                () -> BenchSupport.invokeLikeBpEngine(factory, large, "op3", "x")));
        }
        String gm = "Scalability - WSIFMapperFactory.newMapper() (global lock)";
        for (int n : threadCounts) {
            results.add(withTccl(gm, "newMapper", n, warmup, measure, () -> WSIFMapperFactory.newMapper()));
        }

        String report = BenchSupport.markdown("WSIF Phase 0 baseline benchmark", results);
        File f = BenchSupport.writeReport("wsif-baseline", report);
        System.out.println(report);
        System.out.println("Report written to " + f.getAbsolutePath());
    }

    /** Measures {@code op} with the bpengine-like TCCL installed on every worker thread. */
    private static Result withTccl(String group, String name, int threads, long warmup, long measure, Op op)
        throws Exception {
        return BenchSupport.measure(group, name, threads, warmup, measure, () -> {
            Thread th = Thread.currentThread();
            ClassLoader old = th.getContextClassLoader();
            th.setContextClassLoader(BenchSupport.BPENGINE_TCCL);
            try {
                return op.run();
            } finally {
                th.setContextClassLoader(old);
            }
        });
    }
}


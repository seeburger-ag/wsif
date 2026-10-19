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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.util.WSIFUtils;

/**
 * Shared infrastructure for the Phase 0 baseline benchmarks and stress tests
 * (see doc/PERFORMANCE_THREADSAFETY_PLAN.md).
 * <ul>
 * <li>Generates Java-binding WSDLs of configurable size.</li>
 * <li>Emulates the SeeBPEngine invocation pattern (ServiceInvoker).</li>
 * <li>Provides a minimal time-based measurement harness (JMH is not available
 *     in the offline repository).</li>
 * </ul>
 */
public final class BenchSupport {

    public static final String TNS = "urn:wsif:bench";
    public static final String JAVA_NS = "http://schemas.xmlsoap.org/wsdl/java/";
    public static final QName SERVICE_QNAME = new QName(TNS, "BenchService");
    public static final QName PORTTYPE_QNAME = new QName(TNS, "BenchPortType");

    /** Sink to prevent dead-code elimination. */
    public static volatile int sink;

    private BenchSupport() {
    }

    // ------------------------------------------------------------------
    // Configuration
    // ------------------------------------------------------------------

    public static boolean flag(String name) {
        return Boolean.getBoolean(name);
    }

    public static long longProp(String name, long def) {
        String v = System.getProperty(name);
        return v == null || v.isEmpty() ? def : Long.parseLong(v.trim());
    }

    public static int[] intsProp(String name, String def) {
        String v = System.getProperty(name, def);
        String[] parts = v.split(",");
        int[] r = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            r[i] = Integer.parseInt(parts[i].trim());
        }
        return r;
    }

    // ------------------------------------------------------------------
    // WSDL generation
    // ------------------------------------------------------------------

    /**
     * Builds a WSDL with a Java-bound port type of {@code operations} operations
     * (all mapped to {@link BenchEchoService#echo(String)}) plus
     * {@code extraPortTypes} unbound port types with 5 operations each. The extra
     * port types simulate the large merged definitions bpengine passes to WSIF and
     * make WSIF's whole-definition validation cost visible.
     */
    public static String wsdl(int operations, int extraPortTypes, String serviceClass) {
        StringBuilder b = new StringBuilder(4096);
        b.append("<?xml version='1.0' encoding='UTF-8'?>\n")
            .append("<definitions name='Bench' targetNamespace='").append(TNS).append("'\n")
            .append("  xmlns:tns='").append(TNS).append("'\n")
            .append("  xmlns:xsd='http://www.w3.org/2001/XMLSchema'\n")
            .append("  xmlns:format='http://schemas.xmlsoap.org/wsdl/formatbinding/'\n")
            .append("  xmlns:java='").append(JAVA_NS).append("'\n")
            .append("  xmlns='http://schemas.xmlsoap.org/wsdl/'>\n");

        for (int i = 0; i < operations; i++) {
            b.append(" <message name='req").append(i).append("'><part name='in' type='xsd:string'/></message>\n");
            b.append(" <message name='resp").append(i).append("'><part name='out' type='xsd:string'/></message>\n");
        }
        for (int p = 0; p < extraPortTypes; p++) {
            for (int i = 0; i < 5; i++) {
                b.append(" <message name='x").append(p).append("req").append(i)
                    .append("'><part name='in' type='xsd:string'/></message>\n");
                b.append(" <message name='x").append(p).append("resp").append(i)
                    .append("'><part name='out' type='xsd:string'/></message>\n");
            }
        }

        b.append(" <portType name='").append(PORTTYPE_QNAME.getLocalPart()).append("'>\n");
        for (int i = 0; i < operations; i++) {
            b.append("  <operation name='op").append(i).append("' parameterOrder='in'>")
                .append("<input message='tns:req").append(i).append("'/>")
                .append("<output message='tns:resp").append(i).append("'/></operation>\n");
        }
        b.append(" </portType>\n");
        for (int p = 0; p < extraPortTypes; p++) {
            b.append(" <portType name='Extra").append(p).append("PortType'>\n");
            for (int i = 0; i < 5; i++) {
                b.append("  <operation name='op").append(i).append("' parameterOrder='in'>")
                    .append("<input message='tns:x").append(p).append("req").append(i).append("'/>")
                    .append("<output message='tns:x").append(p).append("resp").append(i).append("'/></operation>\n");
            }
            b.append(" </portType>\n");
        }

        b.append(" <binding name='BenchJavaBinding' type='tns:").append(PORTTYPE_QNAME.getLocalPart()).append("'>\n")
            .append("  <java:binding/>\n")
            .append("  <format:typeMapping encoding='Java' style='Java'>\n")
            .append("   <format:typeMap typeName='xsd:string' formatType='java.lang.String'/>\n")
            .append("  </format:typeMapping>\n");
        for (int i = 0; i < operations; i++) {
            b.append("  <operation name='op").append(i).append("'>")
                .append("<java:operation methodName='echo' methodType='instance'/>")
                .append("<input/><output/></operation>\n");
        }
        b.append(" </binding>\n");

        b.append(" <service name='").append(SERVICE_QNAME.getLocalPart()).append("'>\n")
            .append("  <port name='BenchJavaPort' binding='tns:BenchJavaBinding'>")
            .append("<java:address className='").append(serviceClass).append("'/></port>\n")
            .append(" </service>\n")
            .append("</definitions>\n");
        return b.toString();
    }

    public static Definition readDefinition(String documentBaseURI, String wsdl) throws Exception {
        return WSIFUtils.readWSDL(documentBaseURI, new StringReader(wsdl));
    }

    /** Parsed definition plus the selected WSDL service and port type. */
    public static final class Target {
        public final Definition def;
        public final Service service;
        public final PortType portType;
        public final int operations;

        public Target(Definition def, int operations) {
            this.def = def;
            this.service = def.getService(SERVICE_QNAME);
            this.portType = def.getPortType(PORTTYPE_QNAME);
            this.operations = operations;
            if (service == null || portType == null) {
                throw new IllegalStateException("Benchmark WSDL incomplete");
            }
        }
    }

    public static Target target(int operations, int extraPortTypes) throws Exception {
        String wsdl = wsdl(operations, extraPortTypes, BenchEchoService.class.getName());
        return new Target(readDefinition("urn:wsif:bench:" + operations + ":" + extraPortTypes, wsdl), operations);
    }

    // ------------------------------------------------------------------
    // bpengine emulation
    // ------------------------------------------------------------------

    /**
     * Class loader that delegates everything to its parent but hides
     * {@code wsif.properties}. It mimics the bpengine bundle class loader, which
     * bpengine installs as the thread context class loader before calling WSIF
     * and which cannot see the root resource {@code wsif.properties} of the WSIF
     * bundle (confirmed at runtime).
     */
    public static final class HidingClassLoader extends ClassLoader {
        static {
            registerAsParallelCapable();
        }

        public HidingClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        public URL getResource(String name) {
            return hidden(name) ? null : super.getResource(name);
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            return hidden(name) ? Collections.emptyEnumeration() : super.getResources(name);
        }

        private static boolean hidden(String name) {
            return "wsif.properties".equals(name) || "/wsif.properties".equals(name);
        }
    }

    public static final ClassLoader BPENGINE_TCCL = new HidingClassLoader(BenchSupport.class.getClassLoader());

    /**
     * Steps of ServiceInvoker.createOperation(): service, port and operation creation
     * plus the context round trip. Runs with the bpengine-like TCCL.
     */
    public static WSIFOperation createOperationLikeBpEngine(WSIFServiceFactory factory, Target t, String opName)
        throws Exception {
        Thread th = Thread.currentThread();
        ClassLoader old = th.getContextClassLoader();
        th.setContextClassLoader(BPENGINE_TCCL);
        try {
            WSIFService service = factory.getService(t.def, t.service, t.portType);
            WSIFPort port = service.getPort();
            WSIFOperation op = port.createOperation(opName);
            WSIFMessage ctx = op.getContext();
            ctx.setObjectPart("processName", "bench");
            ctx.setObjectPart("processId", "4711");
            op.setContext(ctx);
            return op;
        } finally {
            th.setContextClassLoader(old);
        }
    }

    /**
     * Full ServiceInvoker.invoke() sync request/response path: createOperation,
     * second context round trip, message creation and execution.
     *
     * @return the output part value (expected to equal {@code payload})
     */
    public static Object invokeLikeBpEngine(WSIFServiceFactory factory, Target t, String opName, String payload)
        throws Exception {
        WSIFOperation op = createOperationLikeBpEngine(factory, t, opName);
        Thread th = Thread.currentThread();
        ClassLoader old = th.getContextClassLoader();
        th.setContextClassLoader(BPENGINE_TCCL);
        try {
            WSIFMessage in = op.createInputMessage();
            WSIFMessage out = op.createOutputMessage();
            WSIFMessage fault = op.createFaultMessage();
            WSIFMessage ctx = op.getContext();
            ctx.setObjectPart("engineGroup", "ANY");
            op.setContext(ctx);
            in.setObjectPart("in", payload);
            if (!op.executeRequestResponseOperation(in, out, fault)) {
                throw new IllegalStateException("Operation " + opName + " returned a fault");
            }
            return out.getObjectPart("out");
        } finally {
            th.setContextClassLoader(old);
        }
    }

    // ------------------------------------------------------------------
    // Measurement harness
    // ------------------------------------------------------------------

    @FunctionalInterface
    public interface Op {
        Object run() throws Exception;
    }

    /** One measurement result. */
    public static final class Result {
        public final String group;
        public final String name;
        public final int threads;
        public final long ops;
        public final long nanos;
        public final long allocatedBytes;

        Result(String group, String name, int threads, long ops, long nanos, long allocatedBytes) {
            this.group = group;
            this.name = name;
            this.threads = threads;
            this.ops = ops;
            this.nanos = nanos;
            this.allocatedBytes = allocatedBytes;
        }

        /** Average latency per operation as seen by one thread. */
        public double usPerOp() {
            return ops == 0 ? Double.NaN : (nanos / 1000.0) * threads / ops;
        }

        public double opsPerSec() {
            return ops == 0 ? 0 : ops * 1e9 / nanos;
        }

        public double bytesPerOp() {
            return allocatedBytes < 0 || ops == 0 ? Double.NaN : (double) allocatedBytes / ops;
        }
    }

    private static final com.sun.management.ThreadMXBean THREADS =
        (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();

    private static long allocated() {
        try {
            return THREADS.getCurrentThreadAllocatedBytes();
        } catch (UnsupportedOperationException e) {
            return -1;
        }
    }

    /**
     * Runs {@code op} on {@code threads} threads: first for {@code warmupMs}, then
     * measures for {@code measureMs}. Operations run in batches so that the clock
     * is not read on every (possibly sub-microsecond) call.
     */
    public static Result measure(String group, String name, int threads, long warmupMs, long measureMs, Op op)
        throws Exception {
        final int batch = 16;
        final CyclicBarrier start = new CyclicBarrier(threads + 1);
        final CyclicBarrier measureStart = new CyclicBarrier(threads + 1);
        final CyclicBarrier end = new CyclicBarrier(threads + 1);
        final long[] opsPerThread = new long[threads];
        final long[] bytesPerThread = new long[threads];
        final AtomicReference<Throwable> failure = new AtomicReference<>();
        final long[] window = new long[2];

        List<Thread> workers = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            final int idx = i;
            Thread w = new Thread(() -> {
                try {
                    start.await();
                    long warmEnd = System.nanoTime() + warmupMs * 1_000_000L;
                    int acc = 0;
                    while (System.nanoTime() < warmEnd) {
                        for (int k = 0; k < batch; k++) {
                            Object r = op.run();
                            acc += r == null ? 0 : 1;
                        }
                    }
                    measureStart.await();
                    long stop = window[1];
                    long a0 = allocated();
                    long n = 0;
                    while (System.nanoTime() < stop) {
                        for (int k = 0; k < batch; k++) {
                            Object r = op.run();
                            acc += r == null ? 0 : 1;
                        }
                        n += batch;
                    }
                    long a1 = allocated();
                    opsPerThread[idx] = n;
                    bytesPerThread[idx] = (a0 < 0 || a1 < 0) ? -1 : a1 - a0;
                    sink += acc;
                } catch (Throwable t) {
                    failure.compareAndSet(null, t);
                } finally {
                    try {
                        end.await();
                    } catch (Exception ignored) {
                        // ignore
                    }
                }
            }, "wsif-bench-" + i);
            w.setDaemon(true);
            workers.add(w);
            w.start();
        }

        start.await();
        Thread.sleep(warmupMs);
        window[0] = System.nanoTime();
        window[1] = window[0] + measureMs * 1_000_000L;
        measureStart.await();
        end.await();
        long elapsed = Math.max(1, System.nanoTime() - window[0]);

        if (failure.get() != null) {
            throw new IllegalStateException("Benchmark '" + name + "' failed", failure.get());
        }
        long ops = 0;
        long bytes = 0;
        for (int i = 0; i < threads; i++) {
            ops += opsPerThread[i];
            bytes = (bytes < 0 || bytesPerThread[i] < 0) ? -1 : bytes + bytesPerThread[i];
        }
        return new Result(group, name, threads, ops, elapsed, bytes);
    }

    // ------------------------------------------------------------------
    // Reporting
    // ------------------------------------------------------------------

    public static String markdown(String title, List<Result> results) {
        StringBuilder b = new StringBuilder();
        b.append("# ").append(title).append("\n\n");
        b.append("- Date: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append('\n');
        b.append("- Label: ").append(System.getProperty("wsif.bench.label", "run")).append('\n');
        b.append("- JVM: ").append(System.getProperty("java.vm.name")).append(' ')
            .append(System.getProperty("java.runtime.version")).append('\n');
        b.append("- OS: ").append(System.getProperty("os.name")).append(", CPUs: ")
            .append(Runtime.getRuntime().availableProcessors()).append('\n');
        b.append("- Max heap: ").append(Runtime.getRuntime().maxMemory() / (1024 * 1024)).append(" MB\n\n");

        String group = null;
        for (Result r : results) {
            if (!r.group.equals(group)) {
                group = r.group;
                b.append("\n## ").append(group).append("\n\n");
                b.append("| Benchmark | Threads | us/op (per thread) | ops/s (total) | bytes/op |\n");
                b.append("|---|---:|---:|---:|---:|\n");
            }
            b.append("| ").append(r.name)
                .append(" | ").append(r.threads)
                .append(" | ").append(String.format(Locale.ROOT, "%.3f", r.usPerOp()))
                .append(" | ").append(String.format(Locale.ROOT, "%,.0f", r.opsPerSec()))
                .append(" | ").append(Double.isNaN(r.bytesPerOp()) ? "n/a"
                    : String.format(Locale.ROOT, "%,.0f", r.bytesPerOp()))
                .append(" |\n");
        }
        return b.toString();
    }

    public static File writeReport(String fileStem, String content) throws IOException {
        File dir = new File("target/bench");
        dir.mkdirs();
        String label = System.getProperty("wsif.bench.label", "run").replaceAll("[^A-Za-z0-9._-]", "_");
        File f = new File(dir, fileStem + "-" + label + ".md");
        Files.write(f.toPath(), content.getBytes(StandardCharsets.UTF_8));
        return f;
    }
}





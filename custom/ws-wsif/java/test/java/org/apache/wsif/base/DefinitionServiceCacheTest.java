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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.lang.ref.WeakReference;

import javax.wsdl.Definition;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.bench.BenchEchoService;
import org.apache.wsif.bench.BenchSupport;
import org.apache.wsif.bench.BenchSupport.Target;
import org.junit.Test;

/**
 * Regression tests for Phase 2.4 of doc/PERFORMANCE_THREADSAFETY_PLAN.md: the
 * service cache for the Definition variants is keyed by Definition identity.
 */
public class DefinitionServiceCacheTest {

    private static WSIFServiceFactory cachingFactory() {
        WSIFServiceFactory f = WSIFServiceFactory.newInstance();
        f.setFeature(WSIFConstants.WSIF_FEATURE_SERVICE_CACHING, Boolean.TRUE);
        return f;
    }

    @Test
    public void cachedServicesAreCopiesBoundToTheirDefinition() throws Exception {
        WSIFServiceFactory f = cachingFactory();
        Target t = BenchSupport.target(3, 0);
        WSIFService s1 = f.getService(t.def, t.service, t.portType);
        WSIFService s2 = f.getService(t.def, t.service, t.portType);
        WSIFService s3 = f.getService(t.def, t.service, t.portType);
        assertNotSame(s1, s2);
        assertNotSame(s2, s3);
        assertSame(t.def, s1.getDefinition());
        assertSame(t.def, s2.getDefinition());
        assertSame(t.def, s3.getDefinition());
        assertNotNull(s2.getPort().createOperation("op2"));
        assertEquals("y", BenchSupport.invokeLikeBpEngine(f, t, "op2", "y"));
    }

    @Test
    public void definitionsWithTheSameBaseUriAreNotMixedUp() throws Exception {
        WSIFServiceFactory f = cachingFactory();
        String base = "urn:wsif:test:redeploy";
        Definition v1 = BenchSupport.readDefinition(base, BenchSupport.wsdl(3, 0, BenchEchoService.class.getName()));
        Definition v2 = BenchSupport.readDefinition(base, BenchSupport.wsdl(4, 0, BenchEchoService.class.getName()));
        Target t1 = new Target(v1, 3);
        Target t2 = new Target(v2, 4);
        for (int i = 0; i < 3; i++) {
            assertSame(v1, f.getService(t1.def, t1.service, t1.portType).getDefinition());
            WSIFService s2 = f.getService(t2.def, t2.service, t2.portType);
            assertSame(v2, s2.getDefinition());
            assertNotNull("operation only present in the new definition",
                s2.getPort().createOperation("op3"));
        }
    }

    @Test
    public void allDefinitionVariantsAreCachedCorrectly() throws Exception {
        WSIFServiceFactory f = cachingFactory();
        Target t = BenchSupport.target(3, 0);
        for (int i = 0; i < 2; i++) {
            assertSame(t.def, f.getService(t.def).getDefinition());
            assertSame(t.def, f.getService(t.def, t.service).getDefinition());
            assertSame(t.def, f.getService(t.def, t.service, t.portType).getDefinition());
            assertSame(t.def, f.getService(t.def,
                BenchSupport.TNS, BenchSupport.SERVICE_QNAME.getLocalPart(),
                BenchSupport.TNS, BenchSupport.PORTTYPE_QNAME.getLocalPart()).getDefinition());
        }
    }

    @Test
    public void cachedDefinitionCanBeGarbageCollected() throws Exception {
        WSIFServiceFactory f = cachingFactory();
        WeakReference<Definition> ref = useAndForget(f);
        for (int i = 0; i < 50 && ref.get() != null; i++) {
            System.gc();
            byte[][] pressure = new byte[64][];
            for (int j = 0; j < pressure.length; j++) {
                pressure[j] = new byte[64 * 1024];
            }
            Thread.sleep(10);
        }
        assertNull("the service cache keeps a Definition strongly reachable", ref.get());
        // the factory itself is still alive and usable
        Target t = BenchSupport.target(2, 0);
        assertSame(t.def, f.getService(t.def, t.service, t.portType).getDefinition());
    }

    private static WeakReference<Definition> useAndForget(WSIFServiceFactory f) throws Exception {
        Target t = BenchSupport.target(2, 0);
        f.getService(t.def, t.service, t.portType).getPort();
        f.getService(t.def, t.service, t.portType).getPort();
        return new WeakReference<>(t.def);
    }
}


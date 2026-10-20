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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.xml.namespace.QName;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.bench.BenchSupport;
import org.apache.wsif.bench.BenchSupport.Target;
import org.junit.Test;

/**
 * Regression tests for Phase 2 of doc/PERFORMANCE_THREADSAFETY_PLAN.md in
 * {@link WSIFServiceImpl}: 2.1 lazy mapper, 2.2 skipped empty type mapping.
 */
public class WSIFServiceImplTest {

    private static final QName XML_TYPE = new QName("urn:test", "MyType");

    private static WSIFServiceImpl service(WSIFServiceFactory f, Target t) throws Exception {
        return (WSIFServiceImpl) f.getService(t.def, t.service, t.portType);
    }

    @Test
    public void noMapperIsCreatedWhenNothingIsMapped() throws Exception {
        Target t = BenchSupport.target(3, 0);
        WSIFServiceImpl s = service(WSIFServiceFactory.newInstance(), t);
        assertNull("mapper created eagerly", s.mapper);
        assertNull("mapping convention created eagerly", s.mapCon);

        assertNotNull(s.getPort().createOperation("op1"));
        assertNull("getPort created a mapper although there is nothing to map", s.mapper);
        // the invocation still works
        assertEquals("x", BenchSupport.invokeLikeBpEngine(WSIFServiceFactory.newInstance(), t, "op1", "x"));
    }

    @Test
    public void mapTypeCreatesMapperAndRecordsOverride() throws Exception {
        WSIFServiceImpl s = service(WSIFServiceFactory.newInstance(), BenchSupport.target(3, 0));
        s.mapType(XML_TYPE, String.class);
        assertNotNull(s.mapper);
        assertNotNull(s.mapCon);
        assertNotNull(s.getPort());
    }

    @Test
    public void mapPackageCreatesMappingConvention() throws Exception {
        WSIFServiceImpl s = service(WSIFServiceFactory.newInstance(), BenchSupport.target(3, 0));
        s.mapPackage("urn:test", "org.example");
        assertNotNull(s.mapCon);
        assertNotNull(s.getPort());
    }

    @Test
    public void copiesDoNotShareTheMutableMapper() throws Exception {
        WSIFServiceImpl s = service(WSIFServiceFactory.newInstance(), BenchSupport.target(3, 0));
        s.mapType(XML_TYPE, String.class);
        WSIFServiceImpl copy = new WSIFServiceImpl(s);
        assertNull("copy shares the source's mapper", copy.mapper);
        copy.mapType(XML_TYPE, Integer.class);
        assertNotNull(copy.mapper);
        assertNotSame(s.mapper, copy.mapper);
    }

    @Test
    public void explicitlyConfiguredMapperIsCreatedEagerly() throws Exception {
        WSIFServiceFactory f = WSIFServiceFactory.newInstance();
        f.setFeature(WSIFConstants.WSIF_FEATURE_MAPPER_CLASS,
            "org.apache.wsif.mapping.WSIFDefaultMapper");
        WSIFServiceImpl s = service(f, BenchSupport.target(3, 0));
        assertNotNull(s.mapper);
        assertNotNull(s.mapCon);
        assertNotNull(s.getPort());
    }

    @Test
    public void badlyConfiguredMapperStillFailsGetService() throws Exception {
        WSIFServiceFactory f = WSIFServiceFactory.newInstance();
        f.setFeature(WSIFConstants.WSIF_FEATURE_MAPPER_CLASS, "org.example.DoesNotExist");
        try {
            service(f, BenchSupport.target(3, 0));
            fail("expected a WSIFException for an unknown mapper class");
        } catch (WSIFException expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("mapper"));
        }
    }
}


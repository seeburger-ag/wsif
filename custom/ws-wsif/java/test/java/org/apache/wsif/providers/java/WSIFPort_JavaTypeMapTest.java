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

package org.apache.wsif.providers.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.bench.BenchSupport;
import org.apache.wsif.bench.BenchSupport.Target;
import org.junit.Test;

/**
 * Regression tests for Phase 2.6 of doc/PERFORMANCE_THREADSAFETY_PLAN.md: the Java
 * binding type map is built once per Binding and exposed to subclasses.
 */
public class WSIFPort_JavaTypeMapTest {

    private static final QName XSD_STRING =
        new QName("http://www.w3.org/2001/XMLSchema", "string");

    private static WSIFPort_Java port(Target t) throws Exception {
        return (WSIFPort_Java) WSIFServiceFactory.newInstance()
            .getService(t.def, t.service, t.portType).getPort();
    }

    @Test
    public void portsOfTheSameBindingShareTheTypeMap() throws Exception {
        Target t = BenchSupport.target(3, 0);
        WSIFPort_Java p1 = port(t);
        WSIFPort_Java p2 = port(t);
        assertNotSame(p1, p2);
        assertSame(p1.getFieldTypeMaps(), p2.getFieldTypeMaps());
        assertEquals("java.lang.String", p1.getFieldTypeMaps().get(XSD_STRING));
    }

    @Test
    public void differentBindingsHaveTheirOwnTypeMap() throws Exception {
        WSIFPort_Java p1 = port(BenchSupport.target(3, 0));
        WSIFPort_Java p2 = port(BenchSupport.target(3, 0));
        assertNotSame(p1.getFieldTypeMaps(), p2.getFieldTypeMaps());
        assertEquals(p1.getFieldTypeMaps(), p2.getFieldTypeMaps());
    }

    @Test
    public void sharedTypeMapIsImmutable() throws Exception {
        Map<QName, Object> typeMaps = port(BenchSupport.target(3, 0)).getFieldTypeMaps();
        try {
            typeMaps.put(new QName("urn:x", "y"), "java.lang.Object");
            fail("the shared type map must not be modifiable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }
}

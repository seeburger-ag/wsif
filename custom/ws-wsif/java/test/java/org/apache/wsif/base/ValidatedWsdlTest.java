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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.lang.ref.WeakReference;

import javax.wsdl.Definition;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.bench.BenchEchoService;
import org.apache.wsif.bench.BenchSupport;
import org.apache.wsif.bench.BenchSupport.Target;
import org.junit.Test;

/**
 * Regression tests for Phase 2.3 of doc/PERFORMANCE_THREADSAFETY_PLAN.md:
 * WSDL validation runs once per Definition / PortType.
 */
public class ValidatedWsdlTest {

    @Test
    public void validDefinitionAndPortTypeAreRemembered() throws Exception {
        assumeTrue(ValidatedWsdl.ENABLED);
        Target t = BenchSupport.target(3, 0);
        assertFalse(ValidatedWsdl.isValidated(t.def));
        WSIFServiceFactory.newInstance().getService(t.def, t.service, t.portType).getPort();
        assertTrue(ValidatedWsdl.isValidated(t.def));
        assertTrue(ValidatedWsdl.isValidated(t.portType));
    }

    @Test
    public void invalidDefinitionFailsOnEveryUse() throws Exception {
        String wsdl = BenchSupport.wsdl(3, 0, BenchEchoService.class.getName())
            .replace("<binding name='BenchJavaBinding' type='tns:BenchPortType'>",
                     "<binding name='BenchJavaBinding' type='tns:UndefinedPortType'>");
        Definition def = BenchSupport.readDefinition("urn:wsif:test:invalid", wsdl);
        Target t = new Target(def, 3);
        WSIFServiceFactory f = WSIFServiceFactory.newInstance();
        for (int i = 0; i < 3; i++) {
            try {
                f.getService(t.def, t.service, t.portType);
                fail("expected an invalid WSDL to be rejected (attempt " + (i + 1) + ")");
            } catch (WSIFException expected) {
                // expected every time - failures are never memoized
            }
        }
        assertFalse(ValidatedWsdl.isValidated(def));
    }

    @Test
    public void validatedDefinitionCanBeGarbageCollected() throws Exception {
        assumeTrue(ValidatedWsdl.ENABLED);
        WeakReference<Definition> ref = validateAndForget();
        for (int i = 0; i < 50 && ref.get() != null; i++) {
            System.gc();
            byte[][] pressure = new byte[64][];
            for (int j = 0; j < pressure.length; j++) {
                pressure[j] = new byte[64 * 1024];
            }
            Thread.sleep(10);
        }
        assertNull("validated Definition is still strongly reachable", ref.get());
    }

    private static WeakReference<Definition> validateAndForget() throws Exception {
        Target t = BenchSupport.target(2, 0);
        WSIFServiceFactory.newInstance().getService(t.def, t.service, t.portType).getPort();
        assertTrue(ValidatedWsdl.isValidated(t.def));
        return new WeakReference<>(t.def);
    }
}


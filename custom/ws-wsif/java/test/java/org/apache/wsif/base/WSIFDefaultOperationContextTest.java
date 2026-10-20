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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;

import static org.junit.Assert.assertTrue;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.bench.BenchSupport;
import org.apache.wsif.bench.BenchSupport.Target;
import org.apache.wsif.providers.ModelWSIFPort;
import org.apache.wsif.providers.java.WSIFPort_Java;
import org.junit.Test;

/**
 * Regression tests for Phase 2.5 of doc/PERFORMANCE_THREADSAFETY_PLAN.md: an
 * operation's context is still a private copy after dropping the redundant clone.
 */
public class WSIFDefaultOperationContextTest {

    @Test
    public void operationContextIsAPrivateCopy() throws Exception {
        Target t = BenchSupport.target(3, 0);
        WSIFPort port = WSIFServiceFactory.newInstance()
            .getService(t.def, t.service, t.portType).getPort();
        WSIFMessage portCtx = port.getContext();
        portCtx.setObjectPart("fromPort", "p");
        port.setContext(portCtx);

        WSIFOperation op = port.createOperation("op1");
        WSIFMessage c1 = op.getContext();
        WSIFMessage c2 = op.getContext();
        assertNotSame(c1, c2);
        assertEquals("p", c1.getObjectPart("fromPort"));

        c1.setObjectPart("onlyInC1", "x");
        assertFalse(hasPart(port.getContext(), "onlyInC1"));
        assertFalse(hasPart(op.getContext(), "onlyInC1"));
        assertTrue(hasPart(c1, "onlyInC1"));
    }

    private static boolean hasPart(WSIFMessage m, String name) {
        for (java.util.Iterator<?> it = m.getPartNames(); it.hasNext();) {
            if (name.equals(it.next())) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void wsifOwnPortsAreKnownToReturnCopies() {
        assertTrue(WSIFDefaultOperation.portContextIsCopy(WSIFPort_Java.class));
        assertTrue(WSIFDefaultOperation.portContextIsCopy(ModelWSIFPort.class));
    }

    @Test
    public void portsOverridingGetContextAreStillCloned() {
        assertFalse(WSIFDefaultOperation.portContextIsCopy(OverridingPort.class));
    }

    /** A port with its own getContext(), which might return its internal instance. */
    abstract static class OverridingPort extends WSIFDefaultPort {
        @Override
        public WSIFMessage getContext() throws WSIFException {
            return context;
        }
    }
}


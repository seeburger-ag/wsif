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

package org.apache.wsif.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.providers.WSIFDynamicTypeMap;
import org.apache.wsif.providers.java.WSIFDynamicProvider_Java;
import org.apache.wsif.spi.WSIFProvider;
import org.junit.After;
import org.junit.Test;

/**
 * Regression tests for the thread-safe provider registry
 * (Phase 1.1 of doc/PERFORMANCE_THREADSAFETY_PLAN.md).
 */
public class WSIFPluggableProvidersTest {

    private static final String JAVA_NS = "http://schemas.xmlsoap.org/wsdl/java/";
    private static final String MEMO_NS = "http://wsif.apache.org/test/memo/";

    @After
    public void resetRegistry() {
        WSIFPluggableProviders.overrideDefaultProvider(MEMO_NS, null);
        WSIFPluggableProviders.setAutoLoadProviders(false);
        WSIFPluggableProviders.setAutoLoadProviders(true);
    }

    @Test
    public void discoversProvidersFromServiceFile() {
        WSIFProvider p = WSIFPluggableProviders.getProvider(JAVA_NS);
        assertNotNull(p);
        assertTrue(p instanceof WSIFDynamicProvider_Java);
        // the chosen provider is remembered, also without the trailing '/'
        assertSame(p, WSIFPluggableProviders.getProvider(JAVA_NS));
        assertSame(p, WSIFPluggableProviders.getProvider("http://schemas.xmlsoap.org/wsdl/java"));
        assertTrue(WSIFPluggableProviders.isProviderAvailable(JAVA_NS));
    }

    @Test
    public void availabilityMemoFollowsOverrides() {
        assertFalse(WSIFPluggableProviders.isProviderAvailable(MEMO_NS));
        assertNull(WSIFPluggableProviders.getProvider(MEMO_NS));

        WSIFProvider marker = new MarkerProvider();
        WSIFPluggableProviders.overrideDefaultProvider(MEMO_NS, marker);
        assertTrue("memo not invalidated by override",
            WSIFPluggableProviders.isProviderAvailable(MEMO_NS));
        assertSame(marker, WSIFPluggableProviders.getProvider(MEMO_NS));

        WSIFPluggableProviders.overrideDefaultProvider(MEMO_NS, null);
        assertFalse("memo not invalidated by removal",
            WSIFPluggableProviders.isProviderAvailable(MEMO_NS));
        assertNull(WSIFPluggableProviders.getProvider(MEMO_NS));
    }

    @Test
    public void overrideWinsOverDiscoveredProvider() {
        WSIFProvider discovered = WSIFPluggableProviders.getProvider(JAVA_NS);
        WSIFProvider marker = new MarkerProvider();
        WSIFPluggableProviders.overrideDefaultProvider(JAVA_NS, marker);
        assertSame(marker, WSIFPluggableProviders.getProvider(JAVA_NS));
        assertNotNull(discovered);
    }

    @Test
    public void disablingAutoLoadClearsRegistry() {
        assertNotNull(WSIFPluggableProviders.getProvider(JAVA_NS));
        WSIFPluggableProviders.setAutoLoadProviders(false);
        assertFalse(WSIFPluggableProviders.isAutoLoadProviders());
        assertNull(WSIFPluggableProviders.getProvider(JAVA_NS));
        assertFalse(WSIFPluggableProviders.isProviderAvailable(JAVA_NS));
        WSIFPluggableProviders.setAutoLoadProviders(true);
        assertNotNull(WSIFPluggableProviders.getProvider(JAVA_NS));
    }

    @Test
    public void nullNamespaceIsNotAvailable() {
        assertFalse(WSIFPluggableProviders.isProviderAvailable(null));
        assertFalse(WSIFPluggableProviders.isProviderAvailable(null, null));
    }

    private static final class MarkerProvider implements WSIFProvider {
        public WSIFPort createDynamicWSIFPort(Definition def, Service service, Port port,
            WSIFDynamicTypeMap typeMap) throws WSIFException {
            return null;
        }

        public String[] getBindingNamespaceURIs() {
            return new String[] {MEMO_NS};
        }

        public String[] getAddressNamespaceURIs() {
            return new String[] {MEMO_NS};
        }
    }
}


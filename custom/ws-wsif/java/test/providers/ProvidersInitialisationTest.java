/*
 * Copyright 2002-2004 The Apache Software Foundation.
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2001, 2002, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package providers;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.util.WSIFUtils;

import util.TestUtilities;

public class ProvidersInitialisationTest extends TestCase {

    String wsdlLocation =
        TestUtilities.getWsdlPath("java\\test\\stockquote\\wsifservice")
            + "Stockquote.wsdl";

    public ProvidersInitialisationTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(ProvidersInitialisationTest.class);
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
    }

    public void testRegistered() {
        try {
            // Step 1: identify the service
            Definition def = WSIFUtils.readWSDL(null, wsdlLocation);
            Service service = WSIFUtils.selectService(def, null, null);
            PortType portType = WSIFUtils.selectPortType(def, null, null);
            // Step 2: identify the operation (choose an
            // appropriate service port)
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService wsifService = factory.getService(def, service, portType);
            // Get default port
            String portName = "JavaPort";
            WSIFPort port = wsifService.getPort(portName);
            // WSIFPort was created correctly, so Java binding was registered
            assertTrue("WSIFPort was created correctly", true);
        } catch (Exception e) {
            assertTrue("Couldn't create JavaPort so bindings not registered false", false);
        }
    }
}

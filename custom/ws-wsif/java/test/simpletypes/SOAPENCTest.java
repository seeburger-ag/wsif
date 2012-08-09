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

package simpletypes;

import java.math.BigDecimal;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.providers.soap.apacheaxis.WSIFDynamicProvider_ApacheAxis;
import org.apache.wsif.util.WSIFPluggableProviders;
import util.TestUtilities;

/**
 * Junit test to test out using SOAP-ENC simple types
 * @author Owen Burroughs
 */
public class SOAPENCTest extends TestCase {
    String wsdlLocation =
        TestUtilities.getWsdlPath("java\\test\\simpletypes") + "SOAPENCTest.wsdl";
    static String server = TestUtilities.getSoapServer().toUpperCase();

    public SOAPENCTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
//        TestUtilities.startListeners();
        junit.textui.TestRunner.run(suite());
//        TestUtilities.stopListeners();
    }

    public static Test suite() {
        return new TestSuite(SOAPENCTest.class);
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
    }

    public void testAxis() {
        doit(server+"Port", "axis");
    }
    public void testSoap() {
        doit(server+"Port", "soap");
    }

    private void doit(String portName, String protocol) {
    	TestUtilities.setProviderForProtocol( protocol );

        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory.getService(wsdlLocation, null, // serviceNS
                null, // serviceName
                "http://wsifservice.simpletypes/", // portTypeNS
                "SimplePT"); // portTypeName

            WSIFPort port = service.getPort(portName);
			
			int i = 10;
			byte b = (byte) i;

            invokeOperation(port, "getString", "abcd");
            invokeOperation(port, "getBoolean", new Boolean(true));
            invokeOperation(port, "getFloat", new Float(4321));
            invokeOperation(port, "getDouble", new Double(4321));
            invokeOperation(port, "getDecimal", new BigDecimal(4321));
            invokeOperation(port, "getInteger", new Integer(4321));
            invokeOperation(port, "getShort", new Short("4321"));
            invokeOperation(port, "getByte", new Byte(b));
            invokeOperation(port, "getBase64", new byte[] {b});
        } catch (Exception e) {
            System.err.println(
                "SimpleTest(" + portName + ") caught exception " + e);
            e.printStackTrace();
            assertTrue(false);
        } finally {
        	TestUtilities.resetDefaultProviders();
        }
    }

    private void invokeOperation(WSIFPort port, String name, Object partVal)
        throws Exception {
        WSIFOperation operation = port.createOperation(name);

        WSIFMessage inputMessage = operation.createInputMessage();
        WSIFMessage outputMessage = operation.createOutputMessage();
        WSIFMessage faultMessage = operation.createFaultMessage();
        inputMessage.setObjectPart("dummy", partVal);

        boolean b =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        if (b) {
            System.out.println(
                name + " returned: "
                    + outputMessage.getObjectPart("return"));
        } else {
            System.out.println(name + " failed!");
        }
    }
}

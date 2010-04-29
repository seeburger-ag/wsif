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

package headers;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.util.WSIFPluggableProviders;
import org.apache.wsif.util.WSIFUtils;
import util.TestUtilities;

/**
 * Junit test to test out the Asynchronous requests.
 *
 * Tests the get/setContext methods using the stockquote sample
 * the context allows seting HTTP and SOAP headers, HTTP headers
 * are currently only uid/pswd for basic authentication.
 * Currently all this does is set them so you can see thme in 
 * the packets using TCPMON.
 * You should see this at the top of the out going packet:
 *    Authorization: Basic cGV0cmE6d2FzaGVyZQ==
 */
public class HeadersTest extends TestCase {

    String wsdlLocation =
        TestUtilities.getWsdlPath("java\\test\\stockquote\\wsifservice") + "Stockquote.wsdl";
    static String server = TestUtilities.getSoapServer().toUpperCase();

    public HeadersTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(HeadersTest.class);
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
    }

    public void testAxis() {
        doit(server+"Port", "axis");
    }
    public void testSoap() {
    	doit(server+"Port"   ,"soap");
    }
    //public void testJava          () { doit("JavaPort"   ,"java"); }
    //public void testSoapJms       () { if (TestUtilities.areWeTesting("jms")) doit("SOAPJMSPort","soap"); }
    //public void testAxisJms       () { if (TestUtilities.areWeTesting("jms")) doit("SOAPJMSPort","axis"); }

    private void doit(String portName, String protocol) {

        try {
            invokeMethod(
                wsdlLocation,
                "getQuote",
                null,
                null,
                portName,
                protocol,
                new String[] { "" },
                0);
        } catch (Exception e) {
            System.err.println("AsyncTest(" + portName + ") caught exception " + e);
            e.printStackTrace();
            assertTrue(false);
        } finally {
        }

    }

    public static void invokeMethod(
        String wsdlLocation,
        String operationName,
        String inputName,
        String outputName,
        String portName,
        String protocol,
        String[] args,
        int argShift)
        throws Exception {

        String serviceNS = null;
        String serviceName = null;
        String portTypeNS = null;
        String portTypeName = null;

        TestUtilities.setProviderForProtocol( protocol );

        System.out.println("Reading WSDL document from '" + wsdlLocation + "'");
        Definition def = WSIFUtils.readWSDL(null, wsdlLocation);

        Service service = WSIFUtils.selectService(def, serviceNS, serviceName);
        PortType portType = WSIFUtils.selectPortType(def, portTypeNS, portTypeName);

        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
        WSIFService dpf = factory.getService(def, service, portType);
        WSIFPort port = (portName == null) ? dpf.getPort() : dpf.getPort(portName);

        System.out.println("HeadersTest executing getQuote for \"\"" + operationName);
        WSIFOperation operation =
            port.createOperation("getQuote", inputName, outputName);
        WSIFMessage input = operation.createInputMessage();
        WSIFMessage output = operation.createOutputMessage();
        WSIFMessage fault = operation.createFaultMessage();
        input.setObjectPart("symbol", "");

        // set a basic authentication header
        WSIFMessage headers = operation.getContext();
        headers.setObjectPart(WSIFConstants.CONTEXT_HTTP_USER, "petra");
        headers.setObjectPart(WSIFConstants.CONTEXT_HTTP_PSWD, "washere");
        operation.setContext(headers);

        boolean ok = operation.executeRequestResponseOperation(input, output, fault);
        System.out.println("operation returned " + ok);

        float q = ((Float) output.getObjectPart("quote")).floatValue();

        if (q == -1.0F) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }

        // do it agian without context so you can see the difference 
        System.out.println(
            "HeadersTest no headers executing getQuote for \"\"" + operationName);
        operation = port.createOperation("getQuote", inputName, outputName);
        input = operation.createInputMessage();
        output = operation.createOutputMessage();
        fault = operation.createFaultMessage();
        input.setObjectPart("symbol", "");
        ok = operation.executeRequestResponseOperation(input, output, fault);
        q = ((Float) output.getObjectPart("quote")).floatValue();
        if (q == -1.0F) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }

       	TestUtilities.resetDefaultProviders();

    }

}
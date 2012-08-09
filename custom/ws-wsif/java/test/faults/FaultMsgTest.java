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

package faults;

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
import org.apache.wsif.util.WSIFUtils;
import util.TestUtilities;

/**
 * Junit test to test out Faults.
 * Tests faults using the stockquote sample
 */
public class FaultMsgTest extends TestCase {
    String wsdlLocation =
        TestUtilities.getWsdlPath("java\\test\\faults") + "Stockquote.wsdl";
    static String server = TestUtilities.getSoapServer().toUpperCase();

    public FaultMsgTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
	   TestUtilities.startListeners(TestUtilities.STOCKQUOTE_LISTENER);	
	   junit.textui.TestRunner.run (suite());
	   TestUtilities.stopListeners();	
    }

    public static Test suite() {
        return new TestSuite(FaultMsgTest.class);
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
    }

    //public void testJava          () { doit("JavaPort"   ,"java"); }
    public void testAxis() {
        doit(server+"Port", "axis");
    }
    public void testSoap() {
        doit(server+"Port", "soap");
    }
    public void testSoapJms() {
        if (TestUtilities.areWeTesting("jms"))
            doit("SOAPJMSPort", "soap");
    }
    public void testAxisJms() {
        if (TestUtilities.areWeTesting("jms"))
            doit("SOAPJMSPort", "axis");
    }

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

        try {
            System.out.println("Reading WSDL document from '" + wsdlLocation + "'");
            Definition def = WSIFUtils.readWSDL(null, wsdlLocation);

            Service service = WSIFUtils.selectService(def, serviceNS, serviceName);
            PortType portType = WSIFUtils.selectPortType(def, portTypeNS, portTypeName);

            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService dpf = factory.getService(def, service, portType);
            WSIFPort port = (portName == null) ? dpf.getPort() : dpf.getPort(portName);

            // 1st a call that should work
            WSIFOperation op1 = port.createOperation("getQuote", inputName, outputName);
            WSIFMessage input1 = op1.createInputMessage();
            WSIFMessage output1 = op1.createOutputMessage();
            WSIFMessage fault1 = op1.createFaultMessage();
            input1.setObjectPart("symbol", "");
            boolean ok = op1.executeRequestResponseOperation(input1, output1, fault1);

            assertEquals("ok getQuote response", true, ok);
            float q1 = ((Float) output1.getObjectPart("quote")).floatValue();
            assertEquals("getQuote value", -1.0F, q1, 0F);

            // now a call that is defined in WSDL but not on server
            op1 = port.createOperation("XXXgetQuote", inputName, outputName);
            input1 = op1.createInputMessage();
            output1 = op1.createOutputMessage();
            fault1 = op1.createFaultMessage();
            input1.setObjectPart("symbol", "");
            ok = op1.executeRequestResponseOperation(input1, output1, fault1);

            assertEquals("ok getQuote response", false, ok);
            String name = fault1.getName();
            Object fobject = fault1.getObjectPart(WSIFConstants.SOAP_FAULT_OBJECT);

            assertEquals("fault message name", WSIFConstants.SOAP_FAULT_MSG_NAME, name);
            if ("axis".equals(protocol)) {
                assertTrue("fault obj type", fobject instanceof org.apache.axis.AxisFault);
                //System.out.println( "AxisFault=" + fobject );
            } else {
                assertTrue("fault obj type", fobject instanceof org.apache.soap.Fault);
                //System.out.println( "SOAPFault=" + fobject );
            }

            // now a call where the remote service returns a fault;
            //  doesn't seem any diff?

        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        } finally {
        	TestUtilities.resetDefaultProviders();
        }

    }
}

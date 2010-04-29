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

package interop;

import interop.wsifserviceWrapped.ArrayOfSimpleDocument;
import interop.wsifserviceWrapped.ChildDocument;
import interop.wsifserviceWrapped.ComplexDocumentResponse;
import interop.wsifserviceWrapped.ComplexDocument_ElemType;
import interop.wsifserviceWrapped.ComplexDocument_Type;
import interop.wsifserviceWrapped.Doc_TestPortType;
import interop.wsifserviceWrapped.SimpleDocument_ElemType;
import interop.wsifserviceWrapped.SimpleDocument_Type;
import interop.wsifserviceWrapped.SingleTagResponse;
import interop.wsifserviceWrapped.SingleTag_ElemType;
import interop.wsifserviceWrapped.SingleTag_Type;
import interop.wsifserviceWrapped.SimpleDocumentResponse;
import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Element;
import util.TestUtilities;

/**
 * Junit test to test out the AXIS provider docstyle support using wrapped parts
 * 
 * This test is part of the SOAP Builders interoperability testing
 * effort described at http://www.whitemesa.net/. The WSDL is 
 * available online at http://www.whitemesa.net/wsdl/interopdoc.wsdl
 * 
 * @author Ant Elder <ant.elder@uk.ibm.com>
 */
public class InteropDocWrappedTest extends TestCase {
	
	String wsdlLocation =
		TestUtilities.getWsdlPath("java\\test\\interop\\wsifservice")
            + "interopdoc.wsdl";
//            + "interopdocLocal.wsdl";
//	          "http://www.whitemesa.net/wsdl/interopdoc.wsdl";

	public InteropDocWrappedTest(String name) {
		super(name);
	}

    public static void main(String[] args) {
//        TestUtilities.startListeners(
//            TestUtilities.ADDRESSBOOK_LISTENER
//                | TestUtilities.ASYNC_LISTENER
//                | TestUtilities.NATIVEJMS_LISTENER);
        junit.textui.TestRunner.run(suite());
//        TestUtilities.stopListeners();
    }

	public static Test suite() {
		return new TestSuite(InteropDocWrappedTest.class);
	}

	public void setUp() {
		TestUtilities.setUpExtensionsAndProviders();
	}

	public void testSingleTagDII() {
		doitSingleTagDII("interopDocPort", "axis");
	}
	public void testSimpleDocumentDII() {
		doitSimpleDocumentDII("interopDocPort", "axis");
	}
	public void testComplexDocumentDII() {
		doitComplexDocumentDII("interopDocPort", "axis");
	}
	public void testSingleTagStub() {
		doitSingleTagStub("interopDocPort", "axis");
	}
	public void testSimpleDocumentStub() {
		doitSimpleDocumentStub("interopDocPort", "axis");
	}
	public void testComplexDocumentStub() {
		doitComplexDocumentStub("interopDocPort", "axis");
	}

	private void doitSingleTagDII(String portName, String protocol) {
		if (portName.toUpperCase().indexOf("JMS") != -1
			&& !TestUtilities.areWeTesting("jms"))
			return;

		TestUtilities.setProviderForProtocol(protocol);

		try {
			WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
			WSIFService service =
				factory.getService(
					wsdlLocation,
					null,
					null,
					"http://soapinterop.org/",
					"Doc_TestPortType");

            service.mapType(
               new javax.xml.namespace.QName(
                   "http://soapinterop.org/", 
                   ">SingleTag"),
               SingleTag_ElemType.class );
            service.mapType(
               new javax.xml.namespace.QName(
                   "http://soapinterop.org/", 
                   "SingleTag"),
               SingleTag_Type.class );
            service.mapType(
               new javax.xml.namespace.QName(
                   "http://soapinterop.org/", 
                   "SingleTagResponse"),
               SingleTagResponse.class );

			WSIFPort port = service.getPort(portName);

			WSIFOperation operation = port.createOperation("SingleTag");

			WSIFMessage inMsg = operation.createInputMessage();
			WSIFMessage outMsg = operation.createOutputMessage();
			WSIFMessage faultMsg = operation.createFaultMessage();

            SingleTag_ElemType stet = new SingleTag_ElemType();
            stet.setSingleTag(new SingleTag_Type());
            
			inMsg.setObjectPart("parameters", stet);

			boolean ok =
				operation.executeRequestResponseOperation(
					inMsg,
					outMsg,
					faultMsg);

			assertTrue("operation returned false!!", ok);
 
            Object o = null;
            try {
			    o = outMsg.getObjectPart("outputDoc");
			    assertTrue("response is null!!!", o != null);
            } catch (WSIFException e) {
            	assertTrue("response part 'outputDoc' not found in output message!!!", false);
            }
			assertTrue(
			    "response part has wrong type: " + o.getClass(), 
			    SingleTagResponse.class.isAssignableFrom(o.getClass()));

            SingleTagResponse response = (SingleTagResponse) o;
			assertNotNull("response singleTag is null!!!", response.getSingleTag());

		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(
				"InteropDocTest.doitSingleTagDII("
					+ portName
					+ ") caught exception "
					+ ex.getLocalizedMessage(),
				false);
		}
	}

	private void doitSimpleDocumentDII(String portName, String protocol) {
		if (portName.toUpperCase().indexOf("JMS") != -1
			&& !TestUtilities.areWeTesting("jms"))
			return;

		TestUtilities.setProviderForProtocol(protocol);

		try {
			WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
			WSIFService service =
				factory.getService(
					wsdlLocation,
					null,
					null,
					"http://soapinterop.org/",
					"Doc_TestPortType");

            service.mapPackage("http://soapinterop.org/", "interop.wsifserviceWrapped");
            service.mapType(
               new javax.xml.namespace.QName(
                   "http://soapinterop.org/", 
                   ">SimpleDocument"),
               SimpleDocument_ElemType.class );
            service.mapType(
               new javax.xml.namespace.QName(
                   "http://soapinterop.org/", 
                   "SimpleDocument"),
               SimpleDocument_Type.class );
            service.mapType(
               new javax.xml.namespace.QName(
                   "http://soapinterop.org/", 
                   "SimpleDocumentResponse"),
               SimpleDocumentResponse.class );

			WSIFPort port = service.getPort(portName);

			WSIFOperation operation = port.createOperation("SimpleDocument");

			WSIFMessage inMsg = operation.createInputMessage();
			WSIFMessage outMsg = operation.createOutputMessage();
			WSIFMessage faultMsg = operation.createFaultMessage();

            SimpleDocument_ElemType sdtet = new SimpleDocument_ElemType();
            SimpleDocument_Type sdt = new SimpleDocument_Type();
            sdt.setValue("petra");
            sdtet.setSimpleDocument(sdt);

			inMsg.setObjectPart("parameters", sdtet);

			boolean ok =
				operation.executeRequestResponseOperation(
					inMsg,
					outMsg,
					faultMsg);

			assertTrue("operation returned false!!", ok);
 
            Object o = null;
            try {
			    o = outMsg.getObjectPart("outputDoc");
			    assertTrue("response is null!!!", o != null);
            } catch (WSIFException e) {
            	assertTrue("response part 'outputDoc' not found in output message!!!", false);
            }
			assertTrue(
			    "response part has wrong type: " + o.getClass(), 
			    SimpleDocumentResponse.class.isAssignableFrom(o.getClass()));

            SimpleDocumentResponse response = (SimpleDocumentResponse) o;
            SimpleDocument_Type sdtResponse = response.getSimpleDocument();
			assertNotNull("response SimpleDocument is null!!!", sdtResponse);

			assertTrue(
			    "document value wrong: " + sdtResponse.getValue(), 
			    sdt.getValue().equals(sdtResponse.getValue()));

		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(
				"InteropDocTest.doitSimpleDocumentDII("
					+ portName
					+ ") caught exception "
					+ ex.getLocalizedMessage(),
				false);
		}
	}

	private void doitComplexDocumentDII(String portName, String protocol) {
		if (portName.toUpperCase().indexOf("JMS") != -1
			&& !TestUtilities.areWeTesting("jms"))
			return;

		TestUtilities.setProviderForProtocol(protocol);

		try {
			WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
			WSIFService service =
				factory.getService(
					wsdlLocation,
					null,
					null,
					"http://soapinterop.org/",
					"Doc_TestPortType");

            service.mapPackage("http://soapinterop.org/", "interop.wsifserviceWrapped");

            service.mapType(
               new javax.xml.namespace.QName(
                   "http://soapinterop.org/", 
                   ">ComplexDocument"),
               ComplexDocument_ElemType.class );
            service.mapType(
               new javax.xml.namespace.QName(
                   "http://soapinterop.org/", 
                   "ComplexDocument"),
               ComplexDocument_Type.class );
            service.mapType(
               new javax.xml.namespace.QName(
                   "http://soapinterop.org/", 
                   "ArrayOfSimpleDocument"),
               ArrayOfSimpleDocument.class );
            service.mapType(
               new javax.xml.namespace.QName(
                   "http://soapinterop.org/", 
                   "SimpleDocument"),
               SimpleDocument_Type.class );
            service.mapType(
               new javax.xml.namespace.QName(
                   "http://soapinterop.org/", 
                   "ChildDocument"),
               ChildDocument.class );
            service.mapType(
               new javax.xml.namespace.QName(
                   "http://soapinterop.org/", 
                   "ComplexDocumentResponse"),
               ComplexDocumentResponse.class );

			WSIFPort port = service.getPort(portName);

			WSIFOperation operation = port.createOperation("ComplexDocument");

			WSIFMessage inMsg = operation.createInputMessage();
			WSIFMessage outMsg = operation.createOutputMessage();
			WSIFMessage faultMsg = operation.createFaultMessage();

            ComplexDocument_ElemType cdtet = new ComplexDocument_ElemType();
            ComplexDocument_Type cdt = makeComplexDocument();
            checkComplexDocument(cdt);
            cdtet.setComplexDocument(cdt);

			inMsg.setObjectPart("parameters", cdtet);

			boolean ok =
				operation.executeRequestResponseOperation(
					inMsg,
					outMsg,
					faultMsg);

			assertTrue("operation returned false!!", ok);
 
            Object o = null;
            try {
			    o = outMsg.getObjectPart("outputDoc");
			    assertTrue("response is null!!!", o != null);
            } catch (WSIFException e) {
            	assertTrue("response part 'outputDoc' not found in output message!!!", false);
            }
			assertTrue(
			    "response part has wrong type: " + o.getClass(), 
			    ComplexDocumentResponse.class.isAssignableFrom(o.getClass()));

            ComplexDocumentResponse response = (ComplexDocumentResponse) o;
            ComplexDocument_Type cdtResponse = response.getComplexDocument();
			assertNotNull("response ComplexDocument is null!!!", cdtResponse);
          
			checkComplexDocument(cdtResponse);

		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(
				"InteropDocTest.doitComplexDocumentDII("
					+ portName
					+ ") caught exception "
					+ ex.getLocalizedMessage(),
				false);
		}
	}
	private void doitSingleTagStub(String portName, String protocol) {
		if (portName.toUpperCase().indexOf("JMS") != -1
			&& !TestUtilities.areWeTesting("jms"))
			return;

		TestUtilities.setProviderForProtocol(protocol);

		try {
			WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
			WSIFService service =
				factory.getService(
					wsdlLocation,
					null,
					null,
					"http://soapinterop.org/",
					"Doc_TestPortType");

            service.mapPackage("http://soapinterop.org/", "interop.wsifserviceWrapped");

            Doc_TestPortType stub = (Doc_TestPortType) service.getStub(portName, Doc_TestPortType.class);

            SingleTag_ElemType stet = new SingleTag_ElemType();
            stet.setSingleTag(new SingleTag_Type());

            SingleTagResponse response = stub.singleTag(stet);

			assertNotNull("response is null!!!", response);
			assertNotNull("response singleTag is null!!!", response.getSingleTag());

		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(
				"InteropDocTest.doitSingleTagDII("
					+ portName
					+ ") caught exception "
					+ ex.getLocalizedMessage(),
				false);
		}
	}

	private void doitSimpleDocumentStub(String portName, String protocol) {
		if (portName.toUpperCase().indexOf("JMS") != -1
			&& !TestUtilities.areWeTesting("jms"))
			return;

		TestUtilities.setProviderForProtocol(protocol);

		try {
			WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
			WSIFService service =
				factory.getService(
					wsdlLocation,
					null,
					null,
					"http://soapinterop.org/",
					"Doc_TestPortType");

            service.mapPackage("http://soapinterop.org/", "interop.wsifserviceWrapped");

            Doc_TestPortType stub = (Doc_TestPortType) service.getStub(portName, Doc_TestPortType.class);

            SimpleDocument_ElemType sdtet = new SimpleDocument_ElemType();
            SimpleDocument_Type sdt = new SimpleDocument_Type();
            sdt.setValue("petra");
            sdtet.setSimpleDocument(sdt);

            SimpleDocumentResponse response = stub.simpleDocument(sdtet);

			assertNotNull("response is null!!!", response);

            SimpleDocument_Type sdtResponse = response.getSimpleDocument();
			assertNotNull("response simpleDocument is null!!!", sdtResponse);

			assertTrue(
			    "simpleDocument value wrong: " + sdtResponse.getValue(), 
			    sdt.getValue().equals(sdtResponse.getValue()));

		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(
				"InteropDocTest.doitSimpleDocumentDII("
					+ portName
					+ ") caught exception "
					+ ex.getLocalizedMessage(),
				false);
		}
	}

	private void doitComplexDocumentStub(String portName, String protocol) {
		if (portName.toUpperCase().indexOf("JMS") != -1
			&& !TestUtilities.areWeTesting("jms"))
			return;

		TestUtilities.setProviderForProtocol(protocol);

		try {
			WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
			WSIFService service =
				factory.getService(
					wsdlLocation,
					null,
					null,
					"http://soapinterop.org/",
					"Doc_TestPortType");

            service.mapPackage("http://soapinterop.org/", "interop.wsifserviceWrapped");

            Doc_TestPortType stub = (Doc_TestPortType) service.getStub(portName, Doc_TestPortType.class);

            ComplexDocument_ElemType cdtet = new ComplexDocument_ElemType();
            ComplexDocument_Type cdt = makeComplexDocument();
            checkComplexDocument(cdt);
            cdtet.setComplexDocument(cdt);
                        
            ComplexDocumentResponse response = stub.complexDocument(cdtet);
			assertNotNull("response is null!!!", response);

            ComplexDocument_Type cdtResponse = response.getComplexDocument();
			assertNotNull("response complexDocument is null!!!", cdtResponse);

			checkComplexDocument(cdtResponse);

		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(
				"InteropDocTest.doitComplexDocumentDII("
					+ portName
					+ ") caught exception "
					+ ex.getLocalizedMessage(),
				false);
		}
	}

    private ComplexDocument_Type makeComplexDocument() {
    	
        ComplexDocument_Type cdt = new ComplexDocument_Type();

        ArrayOfSimpleDocument asd = new ArrayOfSimpleDocument();
        SimpleDocument_Type[] adt = new SimpleDocument_Type[2];
        adt[0] = new SimpleDocument_Type("petra");
        adt[1] = new SimpleDocument_Type("ant");
        asd.setSimpleDocument(adt);
        cdt.setSimpleDoc(asd);
        
        ChildDocument cd = new ChildDocument();
        asd = new ArrayOfSimpleDocument();
        adt = new SimpleDocument_Type[1];
        adt[0] = new SimpleDocument_Type("sue");
        asd.setSimpleDocument(adt);
        cd.setChildSimpleDoc(asd);
        cdt.setChild(cd);
        
        cdt.setAnAttribute("together");    

    	return cdt;
    }

    private void checkComplexDocument(ComplexDocument_Type cdt) {
    	
        ArrayOfSimpleDocument asd = cdt.getSimpleDoc();
        assertTrue("asd is null!!!", asd != null);
        SimpleDocument_Type[] adt = asd.getSimpleDocument();

        boolean b = "petra".equals(adt[0]);

        assertTrue("adt is null!!!", adt != null);
        assertTrue("adt length not 2, is: " + adt.length, adt.length == 2);
        assertTrue("child adt[0] not 'petra', is: " + adt[0], "petra".equals(adt[0].getValue()));
        assertTrue("child adt[1] not 'ant', is: " + adt[0], "ant".equals(adt[1].getValue()));
        
        ChildDocument cd = cdt.getChild();
        asd = cd.getChildSimpleDoc();
        assertTrue("child asd is null!!!", asd != null);
        adt = asd.getSimpleDocument();
        assertTrue("child adt is null!!!", adt != null);
        assertTrue("child adt length not 1, is: " + adt.length, adt.length == 1);
        assertTrue("child adt[0] not 'sue', is: " + adt[0], "sue".equals(adt[0].getValue()));
        
        String s = cdt.getAnAttribute();  
        assertTrue("attribute not 'together' is: " + s, "together".equals(s));  

    }

}

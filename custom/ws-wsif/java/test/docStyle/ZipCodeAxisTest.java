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

package docStyle;

import java.io.StringReader;
import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import docStyle.wsifservice.ZipCodeResolverSoap;
import docStyle.zipCodeNW.ShortZipCode;
import docStyle.zipCodeNW.ShortZipCodeResponse;

import util.TestUtilities;

/**
 * Junit test to test out the AXIS provider docstyle support
 */
public class ZipCodeAxisTest extends TestCase {
	String wsdlLocation =
		TestUtilities.getWsdlPath("java\\test\\docStyle\\wsifservice")
            + "zipCodeResolver.wsdl";
//			  + "zipCode.wsdl";
//            + "zipCodeLocal.wsdl";
//	          "http://webservices.eraserver.net/zipcoderesolver/zipcoderesolver.asmx?WSDL";

	public ZipCodeAxisTest(String name) {
		super(name);
	}

    public static void main(String[] args) {
        TestUtilities.startListeners(
            TestUtilities.ADDRESSBOOK_LISTENER
                | TestUtilities.ASYNC_LISTENER
                | TestUtilities.NATIVEJMS_LISTENER);
        junit.textui.TestRunner.run(suite());
        TestUtilities.stopListeners();
    }

	public static Test suite() {
		return new TestSuite(ZipCodeAxisTest.class);
	}

	public void setUp() {
		TestUtilities.setUpExtensionsAndProviders();
	}

	public void testDynamicAxis() {
		doitDyn("ZipCodeResolverSoap", "axis");
	}
	public void testDynamicAxisWrapped() {
		doitDynWrapped("ZipCodeResolverSoap", "axis");
	}
	public void testStubsAxis() {
		doitStub("ZipCodeResolverSoap", "axis");
	}
	public void testStubsAxisWrapped() {
		doitStubWrapped("ZipCodeResolverSoap", "axis");
	}
	public void testMessagingAxis() {
		doitMessaging("ZipCodeResolverSoap", "axis");
	}
	public void testDyn() {
		doitStub("ZipCodeResolverSoapJMS", "axis");
	}

	private void doitDyn(String portName, String protocol) {
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
					"http://webservices.eraserver.net/",
					"ZipCodeResolverSoap");

			WSIFPort port = service.getPort(portName);

			WSIFOperation operation = port.createOperation("ShortZipCode");

			WSIFMessage inMsg = operation.createInputMessage();
			WSIFMessage outMsg = operation.createOutputMessage();
			WSIFMessage faultMsg = operation.createFaultMessage();
			inMsg.setObjectPart("accessCode", "9999");
			inMsg.setObjectPart("address", "607 Trinity");
			inMsg.setObjectPart("city", "Austin");
			inMsg.setObjectPart("state", "TX");

			boolean ok =
				operation.executeRequestResponseOperation(
					inMsg,
					outMsg,
					faultMsg);

			assertTrue("operation returned false!!", ok);
			String s = (String) outMsg.getObjectPart("ShortZipCodeResult");
			assertTrue("wrong zipcode: " + s + "!!", "78701".equals(s));

		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(
				"ZipCodeAxisTest("
					+ portName
					+ ") caught exception "
					+ ex.getLocalizedMessage(),
				false);
		}
	}

	private void doitDynWrapped(String portName, String protocol) {
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
					"http://webservices.eraserver.net/",
					"ZipCodeResolverSoap");

            service.mapType(
               new javax.xml.namespace.QName(
                   "http://webservices.eraserver.net/", 
                   "ShortZipCode"),
               ShortZipCode.class );
            service.mapType(
               new javax.xml.namespace.QName(
                   "http://webservices.eraserver.net/", 
                   "ShortZipCodeResponse"),
               ShortZipCodeResponse.class );

			WSIFPort port = service.getPort(portName);

			WSIFOperation operation = port.createOperation("ShortZipCode");

			WSIFMessage inMsg = operation.createInputMessage();
			WSIFMessage outMsg = operation.createOutputMessage();
			WSIFMessage faultMsg = operation.createFaultMessage();

            ShortZipCode zc = new ShortZipCode();
            zc.setAccessCode("9999");
            zc.setAddress("607 Trinity");
            zc.setCity("Austin");
            zc.setState("TX");

			inMsg.setObjectPart("parameters", zc);

			boolean ok =
				operation.executeRequestResponseOperation(
					inMsg,
					outMsg,
					faultMsg);

			assertTrue("operation returned false!!", ok);

            ShortZipCodeResponse zcResp =
			   (ShortZipCodeResponse) outMsg.getObjectPart("parameters");

            String s = zcResp.getShortZipCodeResult();
			assertTrue("wrong zipcode: " + s + "!!", "78701".equals(s));

		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(
				"ZipCodeAxisTest("
					+ portName
					+ ") caught exception "
					+ ex.getLocalizedMessage(),
				false);
		}
	}

	private void doitStub(String portName, String protocol) {
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
					"http://webservices.eraserver.net/",
					"ZipCodeResolverSoap");

            ZipCodeResolverSoap stub = (ZipCodeResolverSoap) service.getStub(portName, ZipCodeResolverSoap.class);

            String zipcode = stub.ShortZipCode( "9999", "607 Trinity", "Austin", "TX" );
			assertTrue("wrong zipcode: " + zipcode + "!!", "78701".equals(zipcode));

		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(
				"ZipCodeAxisTest("
					+ portName
					+ ") caught exception "
					+ ex.getLocalizedMessage(),
				false);
		}
	}

	private void doitStubWrapped(String portName, String protocol) {
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
					"http://webservices.eraserver.net/",
					"ZipCodeResolverSoap");

            //TODO: its a bug that these mapTypes are needed with wrapped operation stubs 
            service.mapType(
               new javax.xml.namespace.QName(
                   "http://webservices.eraserver.net/", 
                   "ShortZipCode"),
               ShortZipCode.class );
            service.mapType(
               new javax.xml.namespace.QName(
                   "http://webservices.eraserver.net/", 
                   "ShortZipCodeResponse"),
               ShortZipCodeResponse.class );

            ShortZipCode zc = new ShortZipCode();
            zc.setAccessCode("9999");
            zc.setAddress("607 Trinity");
            zc.setCity("Austin");
            zc.setState("TX");

            docStyle.zipCodeNW.ZipCodeResolverSoap stub = (docStyle.zipCodeNW.ZipCodeResolverSoap) service.getStub(portName, docStyle.zipCodeNW.ZipCodeResolverSoap.class);

            ShortZipCodeResponse zcResp = stub.shortZipCode(zc);

            String s = zcResp.getShortZipCodeResult();
			assertTrue("wrong zipcode: " + s + "!!", "78701".equals(s));

		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(
				"ZipCodeAxisTest("
					+ portName
					+ ") caught exception "
					+ ex.getLocalizedMessage(),
				false);
		}
	}

	private void doitMessaging(String portName, String protocol) {
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
					"http://webservices.eraserver.net/",
					"ZipCodeResolverSoap");

			WSIFPort port = service.getPort(portName);
			WSIFOperation operation = port.createOperation("ShortZipCode");
			WSIFMessage inMsg = operation.createInputMessage();
			WSIFMessage outMsg = operation.createOutputMessage();
			WSIFMessage faultMsg = operation.createFaultMessage();

			String inputDocument =
				"<ShortZipCode xmlns=\"http://webservices.eraserver.net/\">"
					+ "<accessCode>9999</accessCode>"
					+ "<address>607 Trinity</address>"
					+ "<city>Austin</city>"
					+ "<state>TX</state>"
					+ "</ShortZipCode>";

			DOMParser parser = new DOMParser();
			String xmlString = "<?xml version=\"1.0\"?>\n" + inputDocument;
			parser.parse(new InputSource(new StringReader(xmlString)));
			Element element = parser.getDocument().getDocumentElement();
			//printElement(element);

			inMsg.setObjectPart("parameters", element);

			boolean ok =
				operation.executeRequestResponseOperation(
					inMsg,
					outMsg,
					faultMsg);
			assertTrue("operation returned false!!", ok);

			Element responseElement =
				(Element) outMsg.getObjectPart("parameters");
			assertTrue("return element is null!!", responseElement != null);
			//			printElement(responseElement);

			NodeList nl = responseElement.getChildNodes();
			Node n = nl.item(0); // "ShortZipCodeResponse"
			nl = n.getChildNodes();
			n = nl.item(0); // "ShortZipCodeResult"
			String s = n.getNodeValue();
			if (!"78701".equals(s)) {
				printElement(responseElement);
			}
			assertTrue("wrong zipcode: " + s + "!!", "78701".equals(s));

		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(
				"ZipCodeAxisTest("
					+ portName
					+ ") caught exception "
					+ ex.getLocalizedMessage(),
				false);
		}
	}

	private void printElement(Element e) throws Exception {
		OutputFormat of = new OutputFormat(e.getOwnerDocument(), "UTF-8", true);
		XMLSerializer xmls = new XMLSerializer(of);
		StringWriter sw = new StringWriter();
		xmls.setOutputCharStream(sw);
		xmls.serialize(e);
		System.err.println("element=" + sw.toString());
	}
}

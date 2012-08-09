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

import http.net.pointwsp.www.ws.finance.ArrayOfCurrency;
import http.net.pointwsp.www.ws.finance.Currency;
import http.net.pointwsp.www.ws.finance.Currencyrates;
import java.io.StringReader;
import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import util.TestUtilities;

import docStyle.wsifservice.PwspNoCentrbankCurRatesSoap;

/**
 * Junit test to test out the AXIS provider docstyle support.
 * Uses the norwegian central bank daily rates service
 * (this has no input parts and returns complex types)
 */
public class NWBankTest extends TestCase {
	String wsdlLocation =
		TestUtilities.getWsdlPath("java\\test\\docStyle\\wsifservice")
			+ "nwBank.wsdl";
//            + "nwBankLocal.wsdl";

	public NWBankTest(String name) {
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
		return new TestSuite(NWBankTest.class);
	}

	public void setUp() {
		TestUtilities.setUpExtensionsAndProviders();
	}

	public void testDynamicAxis() {
		doitDyn("pwspNoCentrbankCurRatesSoap", "axis");
	}

	public void testStubsAxis() {
    	doitStub("pwspNoCentrbankCurRatesSoap", "axis"); 
	}

	public void testMessagingAxis() {
		doitMessaging("pwspNoCentrbankCurRatesSoap", "axis");
	}

	public void testDynJMS() {
		doitDyn("pwspNoCentrbankCurRatesSoapJMS", "axis");
	}

	public void testStubsAxisJMS() {
    	doitStub("pwspNoCentrbankCurRatesSoapJMS", "axis"); 
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
					"http/www.pointwsp.net/ws/finance",
					"pwspNoCentrbankCurRatesSoap");

            service.mapType(
               new javax.xml.namespace.QName("http/www.pointwsp.net/ws/finance", "currencyrates"),
               Currencyrates.class );

            service.mapType(
               new javax.xml.namespace.QName("http/www.pointwsp.net/ws/finance", "ArrayOfCurrency"),
               ArrayOfCurrency.class);

            service.mapType(
               new javax.xml.namespace.QName("http/www.pointwsp.net/ws/finance", "currency"),
               Currency.class);

            // force to use a 'wrapped' type operation
            WSIFMessage context = service.getContext();
            context.setObjectPart(WSIFConstants.CONTEXT_OPERATION_STYLE, WSIFConstants.CONTEXT_OPERATION_STYLE_WRAPPED);
            service.setContext(context);

			WSIFPort port = service.getPort(portName);

			WSIFOperation operation = port.createOperation("GetRatesXML");

			WSIFMessage inMsg = operation.createInputMessage();
			WSIFMessage outMsg = operation.createOutputMessage();
			WSIFMessage faultMsg = operation.createFaultMessage();
			
			boolean ok =
				operation.executeRequestResponseOperation(
					inMsg,
					outMsg,
					faultMsg);

			assertTrue("operation returned false!!", ok);
			Object o =  outMsg.getObjectPart("GetRatesXMLResult");
			assertTrue("unexpected result: " + o, o instanceof Currencyrates);
          
//            Currencyrates cr = (Currencyrates) o;
//            System.out.println("source=" + cr.getSource());

		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(
				"AddressBookTest("
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
					"http/www.pointwsp.net/ws/finance",
					"pwspNoCentrbankCurRatesSoap");

			// Need to call mapPackage since the namespace in the wsdl is an invalid URI!!!
			service.mapPackage("http/www.pointwsp.net/ws/finance", "http.net.pointwsp.www.ws.finance");
			
            // force to use a 'wrapped' type operation
            WSIFMessage context = service.getContext();
            context.setObjectPart(WSIFConstants.CONTEXT_OPERATION_STYLE, WSIFConstants.CONTEXT_OPERATION_STYLE_WRAPPED);
            service.setContext(context);

            PwspNoCentrbankCurRatesSoap stub = (PwspNoCentrbankCurRatesSoap) service.getStub(portName, PwspNoCentrbankCurRatesSoap.class);

            Object o = stub.GetRatesXML();
			assertTrue("unexpected result: " + o, o instanceof Currencyrates);
//            Currencyrates cr = (Currencyrates) o;
//            System.out.println("source=" + cr.getSource());

		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(
				"AddressBookTest("
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
					"http/www.pointwsp.net/ws/finance",
					"pwspNoCentrbankCurRatesSoap");

            service.mapType(
               new javax.xml.namespace.QName("http/www.pointwsp.net/ws/finance", "currencyrates"),
               Currencyrates.class );

            service.mapType(
               new javax.xml.namespace.QName("http/www.pointwsp.net/ws/finance", "ArrayOfCurrency"),
               ArrayOfCurrency.class);

            service.mapType(
               new javax.xml.namespace.QName("http/www.pointwsp.net/ws/finance", "currency"),
               Currency.class);

			WSIFPort port = service.getPort(portName);

			WSIFOperation operation = port.createOperation("GetRatesXML");

			WSIFMessage inMsg = operation.createInputMessage();
			WSIFMessage outMsg = operation.createOutputMessage();
			WSIFMessage faultMsg = operation.createFaultMessage();
			
			String inputDocument =
				"<GetRatesXML xmlns=\"http/www.pointwsp.net/ws/finance\"/>";

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
			//printElement(responseElement);
			assertTrue("return element is null!!", responseElement != null);


		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(
				"AddressBookTest("
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

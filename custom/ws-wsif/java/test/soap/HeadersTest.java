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

package soap;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.util.WSIFPluggableProviders;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import util.TestUtilities;

/**
 * Junit test to test out SOAP headers.
 * 
 * TODO:
 * This doesn't check the headers at all. Al it does
 * is allow you to see them being sent and received in 
 * TCPMON. Need to sort out a proper automated test.
 *   
 * @author Ant Elder <ant.elder@uk.ibm.com>
 */
public class HeadersTest extends TestCase {

    static final String WSDL_LOCATION =
        TestUtilities.getWsdlPath("java\\test\\soap\\wsifservice")
            + "FakeStockQuote.wsdl";

    static final String FAKE_SOAP_MSG_FILENAME1 =
        TestUtilities.getWsdlPath("java\\test\\soap")
            + "FakeSQRespHeaderOK.txt";

    public HeadersTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestUtilities.startListeners();
        junit.textui.TestRunner.run(suite());
        TestUtilities.stopListeners();
    }

    public static Test suite() {
        return new TestSuite(HeadersTest.class);
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
    }

    public void tearDown() {
        WSIFPluggableProviders.overrideDefaultProvider(
            "http://schemas.xmlsoap.org/wsdl/soap/",
            null);
    }

    public void testAXIS1() {
       doitStockquote( "SOAPJMSPort", "axis", FAKE_SOAP_MSG_FILENAME1, null );
    }

    public void testSOAP1() {
        doitStockquote("SOAPJMSPort", "soap", FAKE_SOAP_MSG_FILENAME1, null);
    }

    /**
     * Query Stockquote sample
     */
    public void doitStockquote(
        String portName,
        String protocol,
        String fakeFile,
        String error) {
        float value;
        WSIFPort port;
        WSIFOperation operation;
        WSIFMessage input, output, fault, context;

        if (portName.toUpperCase().indexOf("JMS") != -1
            && !TestUtilities.areWeTesting("jms")) {
            return;
        }

        TestUtilities.setProviderForProtocol(protocol);

        System.out.println("\n=== StockQuote");
        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory.getService(
                    WSDL_LOCATION, 
                    null, // serviceNS
                    null, // serviceName
                    "http://wsifservice.stockquote/", // portTypeNS
                    "StockquotePT"); // portTypeName

            port = service.getPort(portName);

            // Executing executeRequestResponseAsync(input)
            operation = port.createOperation("getQuote");

            context = operation.getContext();
            context.setObjectPart(
                WSIFConstants.CONTEXT_JMS_PREFIX + "WSIF_FAKE",
                "\"" + fakeFile + "\"");

            ArrayList al = makeSOAPHeaders();
            context.setObjectPart(
                WSIFConstants.CONTEXT_REQUEST_SOAP_HEADERS,
                al);

            input = operation.createInputMessage();
            input.setName("GetQuoteInput");
            input.setObjectPart("symbol", "");
            output = operation.createOutputMessage();

            //output = doAsyncOpNoHandler( operation, input, context );
            doSyncOp(operation, input, output, context);

            context = operation.getContext();
            al =
                (ArrayList) context.getObjectPart(
                    WSIFConstants.CONTEXT_RESPONSE_SOAP_HEADERS);
            for (Iterator i = al.iterator(); i.hasNext();) {
                Element e = (Element) i.next();
                printElement(e);
            }

            Object o;

            o = output.getObjectPart("quote");
            if (o != null) {
                value = ((Float) o).floatValue();
                assertTrue(
                    "doAsyncOpNoHandler stockquote value incorrect!",
                    value == -1.0F);
            } else if (error.equals("<null>")) {
                assertNull("return value not null!!", o);
            } else {
                assertTrue(
                    "return value null!!",
                    error.equals("return=null") || error.equals("all=null"));
            }

            System.out.println("out message contains:");
            String partName;
            for (Iterator i = output.getPartNames(); i.hasNext();) {
                partName = (String) i.next();
                o = output.getObjectPart(partName);
                System.out.println("part=" + partName + " value=" + o);
            }

            o = output.getObjectPart("p1");
            if (o != null) {
                assertTrue("error on part p1!!", o instanceof String);
            } else if (error.equals("<null>")) {
                assertNull("return value not null!!", o);
            } else {
                assertTrue(
                    "p1 is null!!",
                    error.equals("p1=null")
                        || error.equals("parms=null")
                        || error.equals("all=null"));
            }
            o = output.getObjectPart("p2");
            if (o != null) {
                assertTrue("error on part p2!!", o instanceof Float);
            } else if (error.equals("<null>")) {
                assertNull("return value not null!!", o);
            } else {
                assertTrue(
                    "p2 is null!!",
                    error.equals("p2=null")
                        || error.equals("parms=null")
                        || error.equals("all=null"));
            }
            o = output.getObjectPart("p3");
            if (o != null) {
                assertTrue("error on part p3!!", o instanceof String);
            } else if (error.equals("<null>")) {
                assertNull("return value not null!!", o);
            } else {
                assertTrue(
                    "p3 is null!!",
                    error.equals("p3=null")
                        || error.equals("parms=null")
                        || error.equals("all=null"));
            }

        } catch (Exception ex) {
            if (error == null || !error.equals(ex.getMessage())) {
                ex.printStackTrace();
                assertTrue(
                    "exception during stockquote test: " + ex.getMessage(),
                    false);
            }
        }
    }

    private void doSyncOp(
        WSIFOperation op,
        WSIFMessage input,
        WSIFMessage output,
        WSIFMessage context)
        throws WSIFException {
        //try {
        op.setContext(context);
        WSIFMessage fault = op.createFaultMessage();
        boolean ok = op.executeRequestResponseOperation(input, output, fault);
        assertTrue("executeRequestResponseOperation returned false!", ok);
    }

    private ArrayList makeSOAPHeaders() throws WSIFException {
        ArrayList headers = new ArrayList();

        String inputDoc =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<soapenv:Header>"
                + "   <m:Trans xmlns:m=\"http://www.w3schools.com/transaction/\""
                + "   soapenv:mustUnderstand=\"1\">head1</m:Trans>"
                + "</soapenv:Header>"
                + "</soapenv:Envelope>";

        DOMParser parser = new DOMParser();

        try {
            parser.parse(new InputSource(new StringReader(inputDoc)));
        } catch (Exception e) {
            throw new WSIFException("header exception", e);
        }
        Element element = parser.getDocument().getDocumentElement();
        element = (Element) element.getFirstChild();
        NodeList l = element.getChildNodes();
        element = (Element) l.item(1);
        printElement(element);

        headers.add(element);

        return headers;
    }

    private void printElement(Element e) throws WSIFException {
        OutputFormat of = new OutputFormat(e.getOwnerDocument(), "UTF-8", true);
        XMLSerializer xmls = new XMLSerializer(of);
        StringWriter sw = new StringWriter();
        xmls.setOutputCharStream(sw);
        try {
            xmls.serialize(e);
        } catch (IOException ex) {
            throw new WSIFException("printElement exception", ex);
        }
        System.err.println("element=" + sw.toString());
    }

}
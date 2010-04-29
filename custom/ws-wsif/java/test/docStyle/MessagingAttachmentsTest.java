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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
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
import util.TestUtilities;

/**
 * Junit test to test out the AXIS provider docstyle support
 */
public class MessagingAttachmentsTest extends TestCase {
	String wsdlLocation =
		TestUtilities.getWsdlPath("java\\test\\docStyle\\wsifservice")
            + "mime.wsdl";
    static String server = TestUtilities.getSoapServer().toUpperCase();
    
    private final static String IMAGE_LOCATION = 
        TestUtilities.getWsdlPath("java\\test\\mime")
        + "axis.jpg";

	public MessagingAttachmentsTest(String name) {
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
		return new TestSuite(MessagingAttachmentsTest.class);
	}

	public void setUp() {
		TestUtilities.setUpExtensionsAndProviders();
	}

	public void testMsgAxisRcvAtt() {
		doitMsgRcvAtt(server+"Port", "axis");
	}
	public void testMsgAxisSendAtt() {
		doitMsgSendAtt(server+"Port", "axis");
	}

	private void doitMsgRcvAtt(String portName, String protocol) {
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
                    "http://mime/",
                    "Mime");

			WSIFPort port = service.getPort(portName);
			WSIFOperation operation = port.createOperation("stringToDataHandler");
			WSIFMessage inMsg = operation.createInputMessage();
			WSIFMessage outMsg = operation.createOutputMessage();
			WSIFMessage faultMsg = operation.createFaultMessage();

            String content = "The owl and the pussy cat went to sea in a beautiful pea-green boat,";

            String inputDoc = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                " <soapenv:Body>" +
                "    <ns1:stringToDataHandler soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:ns1=\"http://mime/\">" +
                "       <ns1:buff xsi:type=\"xsd:string\">" + 
                content + 
                "</ns1:buff>" +
                "    </ns1:stringToDataHandler>" +
                " </soapenv:Body>" +
                "</soapenv:Envelope>";            

			DOMParser parser = new DOMParser();

			parser.parse(new InputSource(new StringReader(inputDoc)));
			Element element = parser.getDocument().getDocumentElement();
			NodeList l = element.getChildNodes();
			Element e1 = (Element)l.item(1);
			l = e1.getChildNodes();
			element = (Element)l.item(1);
			Node n = e1.getFirstChild();
			n = n.getNextSibling();
            element = (Element) n;
//			printElement(element);
            			
			inMsg.setObjectPart("buff", element);

			boolean ok =
				operation.executeRequestResponseOperation(
					inMsg,
					outMsg,
					faultMsg);
			assertTrue("operation returned false!!", ok);
			
			Object o = outMsg.getObjectPart("return");
            assertTrue(
                "return part not a DataHandler!!",
                (o instanceof DataHandler));
                
            DataHandler dh = (DataHandler) o;
            assertTrue("response Datahandler content wrong!!", compareFiles(dh, content));

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

	private void doitMsgSendAtt(String portName, String protocol) {
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
                    "http://mime/",
                    "Mime");

			WSIFPort port = service.getPort(portName);
			WSIFOperation operation = port.createOperation("bounceImage4");
			WSIFMessage inMsg = operation.createInputMessage();
			WSIFMessage outMsg = operation.createOutputMessage();
			WSIFMessage faultMsg = operation.createFaultMessage();

            String inputDoc = 
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                  "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                  " <soapenv:Body>" +
                  "  <ns1:bounceImage4 soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:ns1=\"http://mime/\">" +
                  "   <ns1:shouldBounce xsi:type=\"xsd:boolean\">true</ns1:shouldBounce>" +
                  "   <ns1:file href=\"cid:blablabla\"/>" +
                  "  </ns1:bounceImage4>" +
                  " </soapenv:Body>" +
                  "</soapenv:Envelope>";

			DOMParser parser = new DOMParser();

			parser.parse(new InputSource(new StringReader(inputDoc)));
			Element element = parser.getDocument().getDocumentElement();
			NodeList l = element.getChildNodes();
			Element e1 = (Element)l.item(1);
			l = e1.getChildNodes();
			element = (Element)l.item(1);
			Node n = e1.getFirstChild();
			n = n.getNextSibling();
            element = (Element) n;
//			printElement(element);
            			
			inMsg.setObjectPart("shouldBounce", element);
			
            DataHandler dh1 = new DataHandler(new FileDataSource(IMAGE_LOCATION));
			inMsg.setObjectPart("file", dh1);

			boolean ok =
				operation.executeRequestResponseOperation(
					inMsg,
					outMsg,
					faultMsg);
			assertTrue("operation returned false!!", ok);
			
			Object o = outMsg.getObjectPart("return");
            assertTrue(
                "return part not a DataHandler!!",
                (o instanceof DataHandler));
                
            DataHandler dhResponse = (DataHandler) o;
            assertTrue("response Datahandler content wrong!!", compareFiles(dhResponse, dh1));

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

    private boolean compareFiles(DataHandler dh1, DataHandler dh2)
        throws FileNotFoundException, IOException {
        InputStream is1 = dh1.getInputStream();
        InputStream is2 = dh2.getInputStream();
        boolean success = false;
        try {
            success = compareFiles(is1, is2);
        } finally {
            if (null != is1)
                is1.close();
            if (null != is2)
                is2.close();
        }
        return success;
    }

    private boolean compareFiles(DataHandler dh, String buff)
        throws FileNotFoundException, IOException {
        InputStream is = dh.getInputStream();
        boolean success = false;
        try {
            success = compareFiles(is, buff);
        } finally {
            if (null != is)
                is.close();
        }
        return success;
    }

    private boolean compareFiles(String one, String buff)
        throws FileNotFoundException, IOException {
        BufferedInputStream oneStream = null;
        File f1 = new File(one);

        boolean success = false;
        try {
            oneStream =
                new BufferedInputStream(
                    new FileInputStream(one),
                    buff.length());
            success = compareFiles(oneStream, buff);
        } finally {
            if (null != oneStream)
                oneStream.close();
        }
        return success;
    }

    private boolean compareFiles(InputStream is, String buff)
        throws FileNotFoundException, IOException {
        return compareFiles(is, new ByteArrayInputStream(buff.getBytes()));
    }
    
    private boolean compareFiles(InputStream is1, InputStream is2) 
        throws FileNotFoundException, IOException {

        int avail1 = is1.available();
        int avail2 = is2.available();
        if (avail1 != avail2) return false;
        if (avail1==0) return true;

        byte[] buff1 = new byte[avail1];
        byte[] buff2 = new byte[avail2];

        Arrays.fill(buff1, (byte) 0);
        Arrays.fill(buff2, (byte) 0);

        int bread1 = -1;
        int bread2 = -1;
        bread1 = is1.read(buff1, 0, avail1);
        bread2 = is2.read(buff2, 0, avail2);
        String s1 = new String(buff1);
        String s2 = new String(buff2);
        if (!s1.equals(s2))
            return false;
        return true;
    }
    
    private String concat(String one, String two)
        throws FileNotFoundException, IOException {
        InputStream is1 = null;
        InputStream is2 = null;
        try {
            is1 = (new DataHandler(new FileDataSource(one))).getInputStream();
            is2 = (new DataHandler(new FileDataSource(two))).getInputStream();
            int avail1 = is1.available();
            int avail2 = is2.available();

            byte[] buff1 = new byte[avail1];
            byte[] buff2 = new byte[avail2];

            Arrays.fill(buff1, (byte) 0);
            Arrays.fill(buff2, (byte) 0);

            int bread1 = -1;
            int bread2 = -1;
            bread1 = is1.read(buff1, 0, avail1);
            bread2 = is2.read(buff2, 0, avail2);
            String s1 = new String(buff1);
            String s2 = new String(buff2);
            return s1 + s2;

        } finally {
            if (null != is1)
                is1.close();
            if (null != is2)
                is2.close();
        }
    }

}

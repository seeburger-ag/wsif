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

package mime;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.internet.MimeMultipart;
import javax.swing.ImageIcon;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.axis.attachments.PlainTextDataSource;
import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.attachments.WSIFAttachmentPart;
import org.apache.wsif.util.WSIFPluggableProviders;
import org.apache.wsif.util.WSIFProperties;
import util.TestUtilities;

/**
 * Junit test for Mime attachments
 * 
 * A list of remaining workitems ...
 * Images across MIME/JMS
 * Honouring MIME headers for referenced attachments
 * Multiple output referenced attachments
 * Inout referenced attachments
 * Support for Strings, Images, Source, MimeMultipart across all the providers
 * Are referenced attachments optional? (Mentioned in the WSDL but not passed as in or out)
 * Passing and returning null attachments
 * Stub support for Strings, Images, etc
 * Arrays or Vectors of referenced attachments
 * Caching of DataHandlers, Strings, etc in WSIFAttachmentPart
 * Do all the providers correctly equate WSIFAttachmentPart/DataHandler/Strings, etc
 * Make WSIFAttachmentPart Serializable (needed for EJB and NativeJms)
 * Attachments for NativeJms
 * Referenced attachments doc-style
 * Better local testing of doc-style attachments
 * What should we do with mime types specified in the WSDL?
 * Attachments for the EJB provider
 * Nested mime:multipartRelated
 * mime:mimeXml
 * MIME/JMS/Async
 * Test passing class files as an attachment
 * Sending or receiving objects that extend WSIFAttachmentPart/DataHandler/String/etc
 * Sending or receiving objects that contain WSIFAttachmentPart/DataHandler/String/etc
 * 
 * @author Mark Whitlock
 */
public class MimeTest extends TestCase {
    private final static String wsdlPath =
        TestUtilities.getWsdlPath("java\\test\\mime");
    private final static String imageLocation = wsdlPath + "axis.jpg";
    private final static String flatfileLocation = wsdlPath + "test.txt";
    private final static String flatfileLocation2 = wsdlPath + "test2.txt";
    private String server = TestUtilities.getSoapServer().toUpperCase();

    private final static String SEND_DH = "SEND-DH";
    private final static String RECEIVE_DH = "RECEIVE-DH";
    private final static String SEND_PLAINTEXT = "SEND-PLAINTEXT";
    private final static String RECEIVE_PLAINTEXT = "RECEIVE-PLAINTEXT";
    private final static String BOUNCE_IMAGE = "BOUNCE-IMAGE";
    private final static String BOUNCE_IMAGE2 = "BOUNCE-IMAGE2";
    private final static String BOUNCE_IMAGE3 = "BOUNCE-IMAGE3";
    private final static String BOUNCE_IMAGE4_DEFAULT = "BOUNCE-IMAGE4-DEFAULT";
    private final static String BOUNCE_IMAGE4_FALSE = "BOUNCE-IMAGE4-FALSE";
    private final static String BOUNCE_IMAGE4_NULL = "BOUNCE-IMAGE4-NULL";
    private final static String SEND_SOURCE = "SEND-SOURCE";
    private final static String RECEIVE_SOURCE = "RECEIVE-SOURCE";
    private final static String SEND_MIMEMULTIPART = "SEND-MIMEMULTIPART";
    private final static String RECEIVE_MIMEMULTIPART = "RECEIVE-MIMEMULTIPART";
    private final static String OR_MULTIPARTS1 = "OR-MULTIPARTS1";
    private final static String OR_MULTIPARTS2 = "OR-MULTIPARTS2";
    private final static String AND_MULTIPARTS = "AND-MULTIPARTS";
    private final static String MULTI_OUT_PARTS = "MULTI-OUT-PARTS";
    private final static String MULTI_INOUT_PARTS = "MULTI-INOUT-PARTS";
    private final static String NO_CONTENT = "NO-CONTENT";
    private final static String TYPE_STAR = "TYPE-STAR";
    private final static String SOAP_BODY_PARTS1 = "SOAP_BODY_PARTS1";
    private final static String SOAP_BODY_PARTS2 = "SOAP_BODY_PARTS2";
    private final static String SOAP_BODY_PARTS3 = "SOAP_BODY_PARTS3";
    private final static String SOAP_BODY_PARTS4 = "SOAP_BODY_PARTS4";
    private final static String ARRAY_OF_BINARY = "ARRAY-OF-BINARY";
    private final static String OPTIONAL_SOAP_BODY = "OPTIONAL_SOAP_BODY";
    private final static String MIX_MIME_PARTS = "MIX_MIME_PARTS";
    private final static String SEND_UNREF_AP = "SEND_UNREF_AP";
    private final static String SEND_UNREF_AP_LOTS = "SEND_UNREF_AP_LOTS";
    private final static String INPUTONLY_UNREF = "INPUTONLY_UNREF";
    private final static String INPUTONLY_REF = "INPUTONLY_REF";
    private final static String SEND_AP = "SEND_AP";
    private final static String RECEIVE_AP = "RECEIVE_AP";
    private final static String MAP_TYPE = "MAP-TYPE";
    private final static String BAD_NO_PART = "BAD-NO-PART";
    private final static String BAD_PART = "BAD-PART";
    private final static String BAD_NESTED = "BAD-NESTED";
    private final static String BAD_MIX_SOAP_MIME = "BAD-MIX-SOAP-MIME";
    private final static String BAD_MULTIPLE_SOAP_BODIES =
        "BAD-MULTIPLE-SOAP-BODIES";
    private final static String BAD_SOAP_BODY_TYPE = "BAD-SOAP-BODY-TYPE";

    /** 
     * Milliseconds to sleep while the user looks at the displayed images.
     */
    private int SLEEPY_TIME = 0;

    private final static String[] rhyme =
        {
            "The owl and the pussy cat went to sea in a beautiful pea-green boat,",
            "They took some honey and plenty of money wrapped up in a five pound note" };

    private final boolean SYNC = true;
    private final boolean ASYNC = false;

    public MimeTest(String name) {
        super(name);

        try {
            SLEEPY_TIME =
                Integer.parseInt(
                    TestUtilities.getWsifProperty("wsif.displaytime"));
        } finally {
            if (SLEEPY_TIME <= 0)
                SLEEPY_TIME = 2000;
        }
    }

    public static void main(String[] args) {
        TestUtilities.startListeners(TestUtilities.MIME_LISTENER);
        junit.textui.TestRunner.run(suite());
        TestUtilities.stopListeners();
    }

    public static Test suite() {
        return new TestSuite(MimeTest.class);
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
    }

    /* ***************************************************/
    /*                AXIS/HTTP tests                    */
    /* ***************************************************/

    public void testSendHandlerHttp() {
        doit(server + "Port", SEND_DH, "Mime.wsdl");
    }

    public void testReceiveHandlerHttp() {
        doit(server + "Port", RECEIVE_DH, "Mime.wsdl");
    }

    /*
     * Attachments that are Strings, Images, are not supported at present.
     * Attachments can only be DataHandlers.
     * 
     * public void testSendPlainTextHttp() {
     *     doit(server+"Port", SEND_PLAINTEXT, "Mime.wsdl");
     * }
     *
     * public void testReceivePlainTextHttp() {
     *    doit(server+"Port", RECEIVE_PLAINTEXT, "Mime.wsdl");
     * }
     * 
     * public void testBounceImageHttp() {
     *     doit(server+"Port", BOUNCE_IMAGE, "Mime.wsdl");
     * }
     */
    public void testBounceImage2Http() {
        doit(server + "Port", BOUNCE_IMAGE2, "Mime.wsdl");
    }

    public void testBounceImage3Http() {
        doit(server + "Port", BOUNCE_IMAGE3, "Mime.wsdl");
    }

    public void testBounceImage4DefaultHttp() {
        doit(server + "Port", BOUNCE_IMAGE4_DEFAULT, "Mime.wsdl");
    }

    public void testBounceImage4FalseHttp() {
        doit(server + "Port", BOUNCE_IMAGE4_FALSE, "Mime.wsdl");
    }

    public void testBounceImage4NullHttp() {
        doit(server + "Port", BOUNCE_IMAGE4_NULL, "Mime.wsdl");
    }

    /*
     * public void testSendSourceHttp() {
     *     doit(server+"Port", SEND_SOURCE, "Mime.wsdl");
     * }
     *
     * public void testReceiveSourceHttp() {
     *     doit(server+"Port", RECEIVE_SOURCE, "Mime.wsdl");
     * }
     *
     * public void testSendMimeMultipartHttp() {
     *     doit(server+"Port", SEND_MIMEMULTIPART, "Mime.wsdl");
     * }
     *
     * public void testReceiveMimeMultipartHttp() {
     *     doit(server+"Port", RECEIVE_MIMEMULTIPART, "Mime.wsdl");
     * }
     */

    public void testOrMultiParts1Http() {
        doit(server + "Port", OR_MULTIPARTS1, "Mime.wsdl");
    }

    public void testOrMultiParts2Http() {
        doit(server + "Port", OR_MULTIPARTS2, "Mime.wsdl");
    }

    public void testAndMultiPartsHttp() {
        doit(server + "Port", AND_MULTIPARTS, "Mime.wsdl");
    }

    /*
     *  public void testMultiOutPartsHttp() {
     *     doit(server+"Port", MULTI_OUT_PARTS, "Mime.wsdl");
     * }
     *
     * public void testMultiInoutPartsHttp() {
     *     doit(server+"Port", MULTI_INOUT_PARTS, "Mime.wsdl");
     * }
     */

    public void testNoContentHttp() {
        doit(server + "Port", NO_CONTENT, "Mime.wsdl");
    }

    public void testTypeStarHttp() {
        doit(server + "Port", TYPE_STAR, "Mime.wsdl");
    }

    public void testSoapBodyParts1Http() {
        doit(server + "Port", SOAP_BODY_PARTS1, "Mime.wsdl");
    }

    public void testSoapBodyParts2Http() {
        doit(server + "Port", SOAP_BODY_PARTS2, "Mime.wsdl");
    }

    public void testSoapBodyParts3Http() {
        doit(server + "Port", SOAP_BODY_PARTS3, "Mime.wsdl");
    }
    
    public void testSoapBodyParts4Http() {
        doit(server + "Port", SOAP_BODY_PARTS4, "Mime.wsdl");
    }

    public void testArrayOfBinaryHttp() {
        doit(server + "Port", ARRAY_OF_BINARY, "Mime.wsdl");
    }

    public void testMapTypeHttp() {
        doit(server + "Port", MAP_TYPE, "Mime.wsdl");
    }

      public void testOptionalSoapBodyHttp() {
          doit(server + "Port", OPTIONAL_SOAP_BODY, "Mime.wsdl");
      }

    public void testMixMimePartsHttp() {
        doit(server + "Port", MIX_MIME_PARTS, "Mime.wsdl");
    }

    public void testSendUnrefAttachmentPartHttp() {
        doit(server + "Port", SEND_UNREF_AP, "Mime.wsdl");
    }

    public void testSendUnrefAttachmentPartLotsHttp() {
        doit(server + "Port", SEND_UNREF_AP_LOTS, "Mime.wsdl");
    }

    public void testSendAttachmentPartHttp() {
        doit(server + "Port", SEND_AP, "Mime.wsdl");
    }

    //    public void testReceiveAttachmentPartHttp() {
    //        doit(server + "Port", RECEIVE_AP, "Mime.wsdl");
    //    }

    public void testInputOnlyUnrefHttp() {
        doit(server + "Port", INPUTONLY_UNREF, "Mime.wsdl");
    }

    public void testInputOnlyRefHttp() {
        doit(server + "Port", INPUTONLY_REF, "Mime.wsdl");
    }

    /* ***************************************************/
    /*                AXIS/JMS tests                     */
    /* ***************************************************/

    public void testSendHandlerJms() {
        doit("SOAPJMSPort", SEND_DH, "Mime.wsdl");
    }

    public void testReceiveHandlerJms() {
        doit("SOAPJMSPort", RECEIVE_DH, "Mime.wsdl");
    }

    //    public void testBounceImage2Jms() {
    //        doit("SOAPJMSPort", BOUNCE_IMAGE2, "Mime.wsdl");
    //    }
    //
    //    public void testBounceImage3Jms() {
    //        doit("SOAPJMSPort", BOUNCE_IMAGE3, "Mime.wsdl");
    //    }
    //
    //    public void testBounceImage4DefaultJms() {
    //        doit("SOAPJMSPort", BOUNCE_IMAGE4_DEFAULT, "Mime.wsdl");
    //    }
    
    public void testBounceImage4FalseJms() {
        doit("SOAPJMSPort", BOUNCE_IMAGE4_FALSE, "Mime.wsdl");
    }
    
    public void testBounceImage4NullJms() {
        doit("SOAPJMSPort", BOUNCE_IMAGE4_NULL, "Mime.wsdl");
    }

    public void testOrMultiParts1Jms() {
        doit("SOAPJMSPort", OR_MULTIPARTS1, "Mime.wsdl");
    }

    public void testOrMultiParts2Jms() {
        doit("SOAPJMSPort", OR_MULTIPARTS2, "Mime.wsdl");
    }

    public void testAndMultiPartsJms() {
        doit("SOAPJMSPort", AND_MULTIPARTS, "Mime.wsdl");
    }

    public void testNoContentJms() {
        doit("SOAPJMSPort", NO_CONTENT, "Mime.wsdl");
    }

    public void testTypeStarJms() {
        doit("SOAPJMSPort", TYPE_STAR, "Mime.wsdl");
    }

    public void testSoapBodyParts1Jms() {
        doit("SOAPJMSPort", SOAP_BODY_PARTS1, "Mime.wsdl");
    }

    public void testSoapBodyParts2Jms() {
        doit("SOAPJMSPort", SOAP_BODY_PARTS2, "Mime.wsdl");
    }

    public void testSoapBodyParts3Jms() {
        doit("SOAPJMSPort", SOAP_BODY_PARTS3, "Mime.wsdl");
    }

    public void testSoapBodyParts4Jms() {
        doit("SOAPJMSPort", SOAP_BODY_PARTS4, "Mime.wsdl");
    }

    public void testArrayOfBinaryJms() {
        doit("SOAPJMSPort", ARRAY_OF_BINARY, "Mime.wsdl");
    }

    public void testMapTypeJms() {
        doit("SOAPJMSPort", MAP_TYPE, "Mime.wsdl");
    }

    public void testOptionalSoapBodyJms() {
        doit("SOAPJMSPort", OPTIONAL_SOAP_BODY, "Mime.wsdl");
    }

    public void testMixMimePartsJms() {
        doit("SOAPJMSPort", MIX_MIME_PARTS, "Mime.wsdl");
    }

    public void testSendUnrefAttachmentPartJms() {
        doit("SOAPJMSPort", SEND_UNREF_AP, "Mime.wsdl");
    }

    public void testSendUnrefAttachmentPartLotsJms() {
        doit("SOAPJMSPort", SEND_UNREF_AP_LOTS, "Mime.wsdl");
    }

    public void testSendAttachmentPartJms() {
        doit("SOAPJMSPort", SEND_AP, "Mime.wsdl");
    }

    /*
     * These next two JMS tests don't work because they rely on 
     * instance methods being supported over JMS.
     * 
     * public void testInputOnlyUnrefJms() {
     *     doit("SOAPJMSPort", INPUTONLY_UNREF, "Mime.wsdl");
     * }
     *  
     * public void testInputOnlyRefJms() {
     *     doit("SOAPJMSPort", INPUTONLY_REF, "Mime.wsdl");
     * }
     */

    /* ***************************************************/
    /*                Java tests                         */
    /* ***************************************************/

    public void testSendHandlerJava() {
        doit("JavaPort", SEND_DH, "Mime.wsdl");
    }

    public void testReceiveHandlerJava() {
        doit("JavaPort", RECEIVE_DH, "Mime.wsdl");
    }

    public void testSendUnrefAttachmentPartJava() {
        doit("JavaPort", SEND_UNREF_AP, "Mime.wsdl");
    }

    public void testSendUnrefAttachmentPartLotsJava() {
        doit("JavaPort", SEND_UNREF_AP_LOTS, "Mime.wsdl");
    }

    public void testSendAttachmentPartJava() {
        doit("JavaPort", SEND_AP, "Mime.wsdl");
    }

    public void testInputOnlyUnrefJava() {
        doit("JavaPort", INPUTONLY_UNREF, "Mime.wsdl");
    }

    /* ***************************************************/
    /*                ERROR tests                        */
    /* ***************************************************/

    public void testBadNoPartHttp() {
        doit(server + "Port", BAD_NO_PART, "MimeBadNoPart.wsdl");
    }

    public void testBadPartHttp() {
        doit(server + "Port", BAD_PART, "MimeBadPart.wsdl");
    }

    public void testBadNestedHttp() {
        doit(server + "Port", BAD_NESTED, "MimeBadNested.wsdl");
    }

    public void testBadMixSoapMimeHttp() {
        doit(server + "Port", BAD_MIX_SOAP_MIME, "MimeBadMixSoapMime.wsdl");
    }

    public void testBadMultipleSoapBodiesHttp() {
        doit(
            server + "Port",
            BAD_MULTIPLE_SOAP_BODIES,
            "MimeBadMultipleSoapBodies.wsdl");
    }

    public void testBadSoapBodyTypeHttp() {
        doit(server + "Port", BAD_SOAP_BODY_TYPE, "MimeBadSoapBodyType.wsdl");
    }

    /**
     * doit should probably do the mapTypes() but unfortunately plain text
     * means that Strings are serialized with the JAFDataHandlerSerializer
     * and I can't find a way to differentiate between strings that should
     * be treated as a mime part and those that should not. Basically if I
     * mapTypes(plaintext,String) all Strings get mapped to a DataHandler,
     * and soap on the server looks for a web service that takes a 
     * DataHandler, not a String. So be careful where mapTypes(plaintext,String)
     * is done.
     */
    private void doit(String portName, String cmd, String wsdl) {
        if (!TestUtilities.areWeTesting("mime"))
            return;
        if (portName.toUpperCase().indexOf("JMS") != -1
            && !TestUtilities.areWeTesting("jms"))
            return;

        // The unreferenced attachment tests over Axis needs axis on the 
        // server and needs wsif.unreferencedattachments=on in wsif.properties.
        if (cmd.indexOf("UNREF") != -1) {
            if (!WSIFProperties.areUnreferencedAttachmentsSupported())
                return;

            // Doing soap/http or soap/jms needs axis on the server
            if (portName.indexOf("SOAP") != -1
                && !"axis".equalsIgnoreCase(server))
                return;
        }

        TestUtilities.setProviderForProtocol("axis");

        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory.getService(
                    wsdlPath + wsdl,
                    null,
                    null,
                    "http://mime/",
                    "Mime");

            // Many of the error tests are badly behaved and would
            // throw an exception here.
            Mime stub = null;
            if (!cmd.startsWith("BAD") && (cmd.indexOf("UNREF") == -1))
                stub = (Mime) service.getStub(portName, Mime.class);

            if (cmd.equals(SEND_DH))
                send_dh(service, stub);
            else if (cmd.equals(RECEIVE_DH))
                receive_dh(service, stub);
            else if (cmd.equals(SEND_PLAINTEXT))
                send_plaintext(service, stub);
            else if (cmd.equals(RECEIVE_PLAINTEXT))
                receive_plaintext(service, stub);
            else if (cmd.equals(BOUNCE_IMAGE))
                bounce_image(service, stub);
            else if (cmd.equals(BOUNCE_IMAGE2))
                bounce_image2(service, stub, portName);
            else if (cmd.equals(BOUNCE_IMAGE3))
                bounce_image3(service, stub, portName);
            else if (cmd.equals(BOUNCE_IMAGE4_DEFAULT))
                bounce_image4_default(service, stub, portName);
            else if (cmd.equals(BOUNCE_IMAGE4_FALSE))
                bounce_image4_false(service, stub, portName);
            else if (cmd.equals(BOUNCE_IMAGE4_NULL))
                bounce_image4_null(service, stub, portName);
            else if (cmd.equals(SEND_SOURCE))
                send_source(service, stub);
            else if (cmd.equals(RECEIVE_SOURCE))
                receive_source(service, stub);
            else if (cmd.equals(SEND_MIMEMULTIPART))
                send_mimemultipart(service, stub);
            else if (cmd.equals(RECEIVE_MIMEMULTIPART))
                receive_mimemultipart(service, stub);
            else if (cmd.equals(OR_MULTIPARTS1))
                or_multiparts1(service, stub);
            else if (cmd.equals(OR_MULTIPARTS2))
                or_multiparts2(service, stub);
            else if (cmd.equals(AND_MULTIPARTS))
                and_multiparts(service, stub);
            else if (cmd.equals(MULTI_OUT_PARTS))
                multi_out_parts(service, stub);
            else if (cmd.equals(MULTI_INOUT_PARTS))
                multi_inout_parts(service, stub);
            else if (cmd.equals(NO_CONTENT))
                no_content(service, stub);
            else if (cmd.equals(TYPE_STAR))
                type_star(service, stub);
            else if (cmd.equals(SOAP_BODY_PARTS1))
                soap_body_parts1(portName, service, stub);
            else if (cmd.equals(SOAP_BODY_PARTS2))
                soap_body_parts2(portName, service, stub);
            else if (cmd.equals(SOAP_BODY_PARTS3))
                soap_body_parts3(portName, service, stub);
            else if (cmd.equals(SOAP_BODY_PARTS4))
                soap_body_parts4(portName, service, stub);
            else if (cmd.equals(ARRAY_OF_BINARY))
                array_of_binary(service, stub);
            else if (cmd.equals(MAP_TYPE))
                map_type(service, stub);
            else if (cmd.equals(OPTIONAL_SOAP_BODY))
                optional_soap_body(service, stub);
            else if (cmd.equals(MIX_MIME_PARTS))
                mix_mime_parts(portName, service, stub);
            else if (cmd.equals(SEND_UNREF_AP))
                send_unref_ap(portName, service);
            else if (cmd.equals(SEND_UNREF_AP_LOTS))
                send_unref_ap_lots(portName, service);
            else if (cmd.equals(SEND_AP))
                send_ap(portName, service);
            else if (cmd.equals(RECEIVE_AP))
                receive_ap(service, stub);
            else if (cmd.equals(INPUTONLY_REF))
                inputonly_ref(portName, service);
            else if (cmd.equals(INPUTONLY_UNREF))
                inputonly_unref(portName, service);
            else if (cmd.equals(BAD_NO_PART))
                bad_no_part(portName, service);
            else if (cmd.equals(BAD_PART))
                bad_part(portName, service);
            else if (cmd.equals(BAD_NESTED))
                bad_nested(portName, service);
            else if (cmd.equals(BAD_MIX_SOAP_MIME))
                bad_mix_soap_mime(portName, service);
            else if (cmd.equals(BAD_MULTIPLE_SOAP_BODIES))
                bad_multiple_soap_bodies(portName, service);
            else if (cmd.equals(BAD_SOAP_BODY_TYPE))
                bad_soap_body_type(portName, service);
            else
                assertTrue(false);

        } catch (Exception e) {
            System.err.println(
                "MimeTest(" + portName + ") caught exception " + e);
            e.printStackTrace();
            assertTrue(false);
        } finally {
            WSIFPluggableProviders.overrideDefaultProvider(
                "http://schemas.xmlsoap.org/wsdl/soap/",
                null);

            WSIFFrame.close();
        }

    }

    private void send_dh(WSIFService service, Mime stub) throws Exception {
        DataHandler dh = new DataHandler(new FileDataSource(flatfileLocation));
        String buff = stub.dataHandlerToString(dh);
        assertTrue(compareFiles(flatfileLocation, buff));
    }

    private void receive_dh(WSIFService service, Mime stub) throws Exception {
        DataHandler dh = stub.stringToDataHandler(rhyme[0]);
        assertTrue(compareFiles(dh, rhyme[0]));
    }

    private void send_plaintext(WSIFService service, Mime stub)
        throws Exception {
        service.mapType(new QName("http://mime/", "plaintext"), String.class);
        String buff = stub.plainTextToString(rhyme[0]);
        assertTrue(rhyme[0].equals(buff));
    }

    private void receive_plaintext(WSIFService service, Mime stub)
        throws Exception {
        service.mapType(new QName("http://mime/", "plaintext"), String.class);
        String buff = stub.stringToPlainText(rhyme[1]);
        assertTrue(rhyme[1].equals(buff));
    }

    private void bounce_image(WSIFService service, Mime stub)
        throws Exception {
        service.mapType(new QName("http://mime/", "image"), Image.class);

        // This blocks until the image is loaded.
        Image im1 = new ImageIcon(imageLocation).getImage();
        WSIFFrame.display(im1, "Original image");

        // Could use Image image2 = Toolkit.getDefaultToolkit().getImage("image.gif");
        // which loads the image in the background.

        Image im2 = stub.bounceImage(im1);
        WSIFFrame.display(im2, "Bounced image");

        System.out.println("Sleeping");
        Thread.sleep(SLEEPY_TIME);
        System.out.println("Woken up");
    }

    /**
     * This test does not do any mapTypes() to test that WSIF will 
     * automatically register mime parts as a DataHandler. 
     */
    private void bounce_image2(WSIFService service, Mime stub, String portName)
        throws Exception {
        DataHandler dh1 = new DataHandler(new FileDataSource(imageLocation));

// Debug code ....
//        InputStream is = dh1.getInputStream();
//        byte[] bBuff = new byte[is.available()];
//        is.read(bBuff);
//
//        byte least = 0;
//        for (int i = 0; i < bBuff.length; i++) {
//            if (bBuff[i] < least)
//                least = bBuff[i];
//        }
//
//        System.out.println("least=" + least);

        WSIFPort port = service.getPort(portName);
        WSIFOperation op = port.createOperation("bounceImage2");
        WSIFMessage in = op.createInputMessage();
        WSIFMessage out = op.createOutputMessage();
        WSIFMessage fault = op.createFaultMessage();
        in.setObjectPart("file", dh1);

        boolean success = op.executeRequestResponseOperation(in, out, fault);
        assertTrue(success);

        DataHandler dh2 = (DataHandler) (out.getObjectPart("file2"));

        //        This commented out code displays the image, so proving that the 
        //        bounced image is correct.
        //
        //        InputStream is = dh2.getInputStream();
        //        byte[] bBuff = new byte[is.available()];
        //        is.read(bBuff);
        //        Image im = new ImageIcon(bBuff).getImage();
        //        WSIFFrame.display(im,"Image");

        assertTrue(compareFiles(dh1, dh2));
    }

    private void bounce_image3(WSIFService service, Mime stub, String portName)
        throws Exception {
        DataHandler dh1 = new DataHandler(new FileDataSource(imageLocation));
        DataHandler dh2 = stub.bounceImage2(dh1);
        assertTrue(compareFiles(dh1, dh2));
    }

    private void bounce_image4_default(
        WSIFService service,
        Mime stub,
        String portName)
        throws Exception {
        DataHandler dh1 = new DataHandler(new FileDataSource(imageLocation));
        DataHandler dh2 = stub.bounceImage4(true, dh1);
        assertTrue(compareFiles(dh1, dh2));
    }

    private void bounce_image4_false(
        WSIFService service,
        Mime stub,
        String portName)
        throws Exception {
        DataHandler dh1 = new DataHandler(new FileDataSource(imageLocation));
        DataHandler dh2 = stub.bounceImage4(false, dh1);
        assertTrue(dh2 == null);
    }

    /**
     * This test needs an axis server.
     */
    private void bounce_image4_null(
        WSIFService service,
        Mime stub,
        String portName)
        throws Exception {
        if (!"axis".equalsIgnoreCase(server))
                return;

        DataHandler dh2 = stub.bounceImage4(true, null);
        assertTrue(dh2 == null);
    }

    private void send_source(WSIFService service, Mime stub) throws Exception {
        service.mapType(new QName("http://mime/", "source"), Source.class);
        //        String buff = stub.plainTextToString(rhyme[0]);
        //        assertTrue(rhyme[0].equals(buff));
        assertTrue(false);
    }

    private void receive_source(WSIFService service, Mime stub)
        throws Exception {
        service.mapType(new QName("http://mime/", "source"), Source.class);
        //        String buff = stub.stringToPlainText(rhyme[1]);
        //        assertTrue(rhyme[1].equals(buff));
        assertTrue(false);
    }

    private void send_mimemultipart(WSIFService service, Mime stub)
        throws Exception {
        service.mapType(
            new QName("http://mime/", "mimemultipart"),
            MimeMultipart.class);
        //        String buff = stub.plainTextToString(rhyme[0]);
        //        assertTrue(rhyme[0].equals(buff));
        assertTrue(false);
    }

    private void receive_mimemultipart(WSIFService service, Mime stub)
        throws Exception {
        service.mapType(
            new QName("http://mime/", "mimemultipart"),
            MimeMultipart.class);
        //        String buff = stub.stringToPlainText(rhyme[1]);
        //        assertTrue(rhyme[1].equals(buff));
        assertTrue(false);
    }

    private void or_multiparts1(WSIFService service, Mime stub)
        throws Exception {
        DataHandler dh = new DataHandler(new FileDataSource(flatfileLocation));
        String buff = stub.orMultiMimeParts(dh);
        assertTrue("text/plain".equals(buff));
    }

    private void or_multiparts2(WSIFService service, Mime stub)
        throws Exception {
        DataHandler dh = new DataHandler(new FileDataSource(imageLocation));
        String buff = stub.orMultiMimeParts(dh);
        assertTrue("image/jpeg".equals(buff));
    }

    private void and_multiparts(WSIFService service, Mime stub)
        throws Exception {
        DataHandler dh1 = new DataHandler(new FileDataSource(flatfileLocation));
        DataHandler dh2 =
            new DataHandler(new FileDataSource(flatfileLocation2));
        String buff = stub.andMultiMimeParts(dh1, dh2);
        assertTrue(concat(flatfileLocation, flatfileLocation2).equals(buff));
    }

    private void multi_out_parts(WSIFService service, Mime stub)
        throws Exception {
        assertTrue(false);
    }

    private void multi_inout_parts(WSIFService service, Mime stub)
        throws Exception {
        assertTrue(false);
    }

    private void no_content(WSIFService service, Mime stub) throws Exception {
        assertTrue(rhyme[0].equals(stub.noContent(rhyme[0])));
    }

    private void type_star(WSIFService service, Mime stub) throws Exception {
        DataHandler dh = new DataHandler(new FileDataSource(flatfileLocation));
        String buff = stub.dataHandlerToString(dh);
        assertTrue(compareFiles(flatfileLocation, buff));
    }

    /**
     * These next tests have to be run stubless because they test the parts="..."
     * in the soap:body and the WSIFClientProxy doesn't look in the binding
     * and so would not be able to find this signature in the portType.
     */
    private void soap_body_parts1(
        String portName,
        WSIFService service,
        Mime stub)
        throws Exception {
        DataHandler dh = new DataHandler(new FileDataSource(flatfileLocation));
        WSIFPort port = service.getPort(portName);
        WSIFOperation op = port.createOperation("soapBodyParts1");
        WSIFMessage in = op.createInputMessage();
        WSIFMessage out = op.createOutputMessage();
        WSIFMessage fault = op.createFaultMessage();
        in.setBooleanPart("shouldBounce", true);
        in.setObjectPart("file", dh);

        boolean success = op.executeRequestResponseOperation(in, out, fault);
        assertTrue(success);

        String s = (String) (out.getObjectPart("buff"));
        assertTrue("1".equals(s));
    }

    private void soap_body_parts2(
        String portName,
        WSIFService service,
        Mime stub)
        throws Exception {
        DataHandler dh = new DataHandler(new FileDataSource(flatfileLocation));
        WSIFPort port = service.getPort(portName);
        WSIFOperation op = port.createOperation("soapBodyParts2");
        WSIFMessage in = op.createInputMessage();
        WSIFMessage out = op.createOutputMessage();
        WSIFMessage fault = op.createFaultMessage();
        in.setObjectPart("file", dh);

        boolean success = op.executeRequestResponseOperation(in, out, fault);
        assertTrue(success);

        String s = (String) (out.getObjectPart("buff"));
        assertTrue("2".equals(s));
    }

    private void soap_body_parts3(
        String portName,
        WSIFService service,
        Mime stub)
        throws Exception {
        WSIFPort port = service.getPort(portName);
        WSIFOperation op = port.createOperation("soapBodyParts3");
        WSIFMessage in = op.createInputMessage();
        WSIFMessage out = op.createOutputMessage();
        WSIFMessage fault = op.createFaultMessage();
        in.setBooleanPart("shouldBounce", true);

        boolean success = op.executeRequestResponseOperation(in, out, fault);
        assertTrue(success);

        String s = (String) (out.getObjectPart("buff"));
        assertTrue("3".equals(s));
    }

    private void soap_body_parts4(
        String portName,
        WSIFService service,
        Mime stub)
        throws Exception {
        WSIFPort port = service.getPort(portName);
        WSIFOperation op = port.createOperation("soapBodyParts4");
        WSIFMessage in = op.createInputMessage();
        WSIFMessage out = op.createOutputMessage();
        WSIFMessage fault = op.createFaultMessage();

        boolean success = op.executeRequestResponseOperation(in, out, fault);
        assertTrue(success);

        String s = (String) (out.getObjectPart("buff"));
        assertTrue("4".equals(s));
    }

    private void array_of_binary(WSIFService service, Mime stub)
        throws Exception {
        DataHandler dh = new DataHandler(new FileDataSource(flatfileLocation));
        String buff = stub.arrayOfBinary(dh);
        assertTrue(compareFiles(flatfileLocation, buff));
    }

    private void map_type(WSIFService service, Mime stub) throws Exception {
        service.mapType(
            new QName("http://mime/", "datahandler"),
            DataHandler.class);

        DataHandler dh = new DataHandler(new FileDataSource(flatfileLocation));
        String buff = stub.dataHandlerToString(dh);
        assertTrue(compareFiles(flatfileLocation, buff));
    }

    private void optional_soap_body(WSIFService service, Mime stub)
        throws Exception {
        DataHandler dh1 = new DataHandler(new FileDataSource(flatfileLocation));
        DataHandler dh2 = stub.optionalSoapBody(dh1);
        assertTrue(compareFiles(dh1, dh2));
    }

    private void mix_mime_parts(
        String portName,
        WSIFService service,
        Mime stub)
        throws Exception {

        DataHandler dh1 = new DataHandler(new FileDataSource(flatfileLocation));
        DataHandler dh2 =
            new DataHandler(new FileDataSource(flatfileLocation2));
        String position1 = "Position One";
        String position2 = "Position Two";
        String position3 = "Position Three";

        WSIFPort port = service.getPort(portName);
        WSIFOperation op = port.createOperation("mixMimeParts");
        WSIFMessage in = op.createInputMessage();
        WSIFMessage out = op.createOutputMessage();
        WSIFMessage fault = op.createFaultMessage();
        in.setObjectPart("position1", position1);
        in.setObjectPart("file1", dh1);
        in.setObjectPart("position2", position2);
        in.setObjectPart("file2", dh2);
        in.setObjectPart("position3", position3);

        boolean success = op.executeRequestResponseOperation(in, out, fault);
        assertTrue(success);

        String s = (String) (out.getObjectPart("buff"));
        String expected =
            position1 + readFile(dh1) + position2 + readFile(dh2) + position3;
        assertTrue(expected.equals(s));
    }

    private void send_unref_ap(String portName, WSIFService service)
        throws Exception {
        DataHandler dh = new DataHandler(new FileDataSource(flatfileLocation));
        WSIFAttachmentPart ap = new WSIFAttachmentPart(dh);
        ap.setProperty("Content-Id", "1234");
        ap.setProperty("Content-Location", "peculiar");
        ap.setProperty("AnotherMimeHeader", "something else");
        List apList = new ArrayList(Arrays.asList(new Object[] { ap }));

        WSIFPort port = service.getPort(portName);
        WSIFOperation op = port.createOperation("unref");
        WSIFMessage in = op.createInputMessage();
        WSIFMessage out = op.createOutputMessage();
        WSIFMessage fault = op.createFaultMessage();
        WSIFMessage context = op.getContext();
        context.setObjectPart(
            WSIFConstants.CONTEXT_REQUEST_UNREFERENCED_ATTACHMENT_PARTS,
            apList);
        op.setContext(context);

        boolean success = op.executeRequestResponseOperation(in, out, fault);
        assertTrue(success);

        String buff = (String) (out.getObjectPart("buff"));
        StringTokenizer tok = new StringTokenizer(buff, ":");
        for (int i = 0; i < 3; i++) {
            String next = (String)tok.nextElement();
            assertTrue(
                next.equals("Content-Location=peculiar")
                    || next.equals("Content-Id=1234")
                    || next.equals("AnotherMimeHeader=something else"));
        }
        
        String next = (String)tok.nextElement();
        assertTrue(compareFiles(flatfileLocation, next));
    }
    
    private void send_unref_ap_lots(String portName, WSIFService service)
        throws Exception {

        final int lots = 150;
        List apList = new ArrayList();
        for (int i = 0; i < lots; i++) {
            PlainTextDataSource ptds =
                new PlainTextDataSource("Attachment" + i, "value%" + i);
            DataHandler dh = new DataHandler(ptds);

            WSIFAttachmentPart ap = new WSIFAttachmentPart(dh);
            ap.setProperty("Index", "" + i);
            apList.add(ap);
        }
        
        WSIFPort port = service.getPort(portName);
        WSIFOperation op = port.createOperation("unref");
        WSIFMessage in = op.createInputMessage();
        WSIFMessage out = op.createOutputMessage();
        WSIFMessage fault = op.createFaultMessage();
        WSIFMessage context = op.getContext();
        context.setObjectPart(
            WSIFConstants.CONTEXT_REQUEST_UNREFERENCED_ATTACHMENT_PARTS,
            apList);
        op.setContext(context);

        boolean success = op.executeRequestResponseOperation(in, out, fault);
        assertTrue(success);

        String buff = (String) (out.getObjectPart("buff"));
        StringTokenizer tok = new StringTokenizer(buff, ":");
        for (int i = 0; i < lots; i++) {
            String next = (String) tok.nextElement();
            assertTrue(next.equals("Index=" + i));
            next = (String) tok.nextElement();
            assertTrue(next.equals("value%" + i));
        }
    }
    
    private void send_ap(String portName, WSIFService service) throws Exception {
        WSIFAttachmentPart ap =
            new WSIFAttachmentPart(
                new DataHandler(new FileDataSource(flatfileLocation)));

        WSIFPort port = service.getPort(portName);
        WSIFOperation op = port.createOperation("dataHandlerToString");
        WSIFMessage in = op.createInputMessage();
        WSIFMessage out = op.createOutputMessage();
        WSIFMessage fault = op.createFaultMessage();
        in.setObjectPart("file", ap);

        boolean success = op.executeRequestResponseOperation(in, out, fault);
        assertTrue(success);

        String buff = (String) (out.getObjectPart("buff"));
        assertTrue(compareFiles(flatfileLocation, buff));
    }

    private void receive_ap(WSIFService service, Mime stub) throws Exception {
        assertTrue(false);
        // WSIFAttachmentPart ap = stub.stringToAttachmentPart(rhyme[0]);
        // assertTrue(compareFiles(ap, rhyme[0]));
    }

    /**
     * Tests unreferenced attachments on input-only operations. Also tests
     * multiple output unreferenced attachments mixed with an output
     * referenced attachment.
     */
    private void inputonly_unref(String portName, WSIFService service)
        throws Exception {
        WSIFPort port = service.getPort(portName);
        DataHandler dh1 = new DataHandler(new FileDataSource(flatfileLocation));
        DataHandler dh2 =
            new DataHandler(new FileDataSource(flatfileLocation2));
        putUnref(port, new WSIFAttachmentPart(dh1));
        putUnref(port, new WSIFAttachmentPart(dh2));
        ArrayList outRefAttachments = new ArrayList();
        List l = getUnref(port,outRefAttachments);
        // assertTrue("l.size() expected 2 actual " + l.size(), 2 == l.size());
        assertTrue(
            compareFiles(
                ((WSIFAttachmentPart) l.get(0)).getDataHandler(),
                dh1));
        assertTrue(
            compareFiles(
                ((WSIFAttachmentPart) l.get(1)).getDataHandler(),
                dh2));

        DataHandler refDh = (DataHandler) outRefAttachments.get(0);
        InputStream isRefDh = refDh.getInputStream();
        byte[] bBuff = new byte[isRefDh.available()];
        isRefDh.read(bBuff);
        String strRefDh = new String(bBuff);
        assertTrue("Spurious referenced DataHandler".equals(strRefDh));
    }
    
    /**
     * Tests referenced attachments on input-only operations
     */
    private void inputonly_ref(String portName, WSIFService service)
        throws Exception {
        DataHandler dh = new DataHandler(new FileDataSource(flatfileLocation));
        WSIFPort port = service.getPort(portName);
        putRef(port,dh);
        DataHandler dh2 = getRef(port);
        assertTrue(compareFiles(dh, dh2));
    }
        
    private void putUnref(WSIFPort port, WSIFAttachmentPart ap) throws Exception {
        List apList = new ArrayList(Arrays.asList(new Object[] { ap }));

        WSIFOperation op = port.createOperation("putUnrefDhInputOnly");
        WSIFMessage in = op.createInputMessage();
        
        WSIFMessage context = op.getContext();
        context.setObjectPart(
            WSIFConstants.CONTEXT_REQUEST_UNREFERENCED_ATTACHMENT_PARTS,
            apList);
        op.setContext(context);

        op.executeInputOnlyOperation(in);
    }
    
    private List getUnref(WSIFPort port, List outRefAttachments) throws Exception {
        WSIFOperation op = port.createOperation("getUnrefDh");
        WSIFMessage in = op.createInputMessage();
        WSIFMessage out = op.createOutputMessage();
        WSIFMessage fault = op.createFaultMessage();

        boolean success = op.executeRequestResponseOperation(in, out, fault);
        assertTrue(success);

        WSIFMessage context = op.getContext();
        ArrayList al =
            new ArrayList(
                (List) context.getObjectPart(
                    WSIFConstants.CONTEXT_RESPONSE_UNREFERENCED_ATTACHMENT_PARTS));

        outRefAttachments.add(out.getObjectPart("file"));
        return al;
    }
    
    private void putRef(WSIFPort port, DataHandler dh) throws Exception {
        WSIFAttachmentPart ap = new WSIFAttachmentPart(dh);
        WSIFOperation op = port.createOperation("putRefDhInputOnly");
        WSIFMessage in = op.createInputMessage();
        in.setObjectPart("file", ap);
        op.executeInputOnlyOperation(in);
    }
    
    private DataHandler getRef(WSIFPort port) throws Exception {
        WSIFOperation op = port.createOperation("getRefDh");
        WSIFMessage in = op.createInputMessage();
        WSIFMessage out = op.createOutputMessage();
        WSIFMessage fault = op.createFaultMessage();

        boolean success = op.executeRequestResponseOperation(in, out, fault);
        assertTrue(success);

        Object o = out.getObjectPart("file");
        DataHandler dh = null;
        if (o instanceof DataHandler) {
            dh = (DataHandler)o;
        } else if (o instanceof WSIFAttachmentPart) {
            dh = ((WSIFAttachmentPart) o).getDataHandler();
        }
        return dh;
    }
    
    /* *********************** ERRORS ********************************** */

    private void bad_no_part(String portName, WSIFService service)
        throws Exception {
        DataHandler dh = new DataHandler(new FileDataSource(flatfileLocation));
        boolean exceptionCaught = false;
        try {
            Mime stub = (Mime) service.getStub(portName, Mime.class);
            String buff = stub.badNoPart(dh);
        } catch (WSIFException we) {
            exceptionCaught = true;
            System.out.println("Expected exception=" + we);
            we.printStackTrace();
        }
        assertTrue(exceptionCaught);
    }

    private void bad_part(String portName, WSIFService service)
        throws Exception {
        DataHandler dh = new DataHandler(new FileDataSource(flatfileLocation));
        boolean exceptionCaught = false;
        try {
            Mime stub = (Mime) service.getStub(portName, Mime.class);
            String buff = stub.badPart(dh);
        } catch (WSIFException we) {
            exceptionCaught = true;
            System.out.println("Expected exception=" + we);
            we.printStackTrace();
        }
        assertTrue(exceptionCaught);
    }

    private void bad_nested(String portName, WSIFService service)
        throws Exception {
        DataHandler dh = new DataHandler(new FileDataSource(flatfileLocation));
        boolean exceptionCaught = false;
        try {
            Mime stub = (Mime) service.getStub(portName, Mime.class);
            stub.badNested(dh);
        } catch (WSIFException we) {
            exceptionCaught = true;
            System.out.println("Expected exception=" + we);
            we.printStackTrace();
        }
        assertTrue(exceptionCaught);
    }

    private void bad_mix_soap_mime(String portName, WSIFService service)
        throws Exception {
        DataHandler dh = new DataHandler(new FileDataSource(flatfileLocation));
        boolean exceptionCaught = false;
        try {
            Mime stub = (Mime) service.getStub(portName, Mime.class);
            String buff = stub.badMixSoapMime(dh);
        } catch (WSIFException we) {
            exceptionCaught = true;
            System.out.println("Expected exception=" + we);
            we.printStackTrace();
        }
        assertTrue(exceptionCaught);
    }

    private void bad_multiple_soap_bodies(String portName, WSIFService service)
        throws Exception {
        DataHandler dh = new DataHandler(new FileDataSource(flatfileLocation));
        boolean exceptionCaught = false;
        try {
            Mime stub = (Mime) service.getStub(portName, Mime.class);
            String buff = stub.badMultipleSoapBodies(dh);
        } catch (WSIFException we) {
            exceptionCaught = true;
            System.out.println("Expected exception=" + we);
            we.printStackTrace();
        }
        assertTrue(exceptionCaught);
    }

    private void bad_soap_body_type(String portName, WSIFService service)
        throws Exception {
        DataHandler dh = new DataHandler(new FileDataSource(flatfileLocation));
        boolean exceptionCaught = false;
        try {
            Mime stub = (Mime) service.getStub(portName, Mime.class);
            String buff = stub.badSoapBodyType(dh);
        } catch (WSIFException we) {
            exceptionCaught = true;
            System.out.println("Expected exception=" + we);
            we.printStackTrace();
        }
        assertTrue(exceptionCaught);
    }

    /* ******************* UTILITIES ************************************* */

    private boolean compareFiles(DataHandler dh1, DataHandler dh2)
        throws FileNotFoundException, IOException {
        assertTrue(dh1 != null && dh2 != null);
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
        assertTrue(dh != null && buff != null);
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
        assertTrue(one != null && buff != null);
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
        assertTrue(is != null && buff != null);
        return compareFiles(is, new ByteArrayInputStream(buff.getBytes()));
    }

    private boolean compareFiles(InputStream is1, InputStream is2)
        throws FileNotFoundException, IOException {
        assertTrue(is1 != null && is2 != null);

        int avail1 = is1.available();
        int avail2 = is2.available();
        if (avail1 != avail2)
            return false;
        if (avail1 == 0)
            return true;

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
        assertTrue(one != null && two != null);
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

    private String readFile(DataHandler dh) {
        assertTrue(dh != null);
        try {
            InputStream is = dh.getInputStream();
            byte[] bBuff = new byte[is.available()];
            is.read(bBuff);
            String sBuff = new String(bBuff);
            return sBuff;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return "";
        }
    }
}

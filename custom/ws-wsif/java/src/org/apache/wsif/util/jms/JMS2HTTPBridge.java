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

package org.apache.wsif.util.jms;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.TextMessage;

import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.MessageContext;
import org.apache.axis.components.net.BooleanHolder;
import org.apache.axis.components.net.DefaultSocketFactory;
import org.apache.axis.components.net.SocketFactory;
import org.apache.axis.encoding.Base64;
import org.apache.axis.transport.http.ChunkedInputStream;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.utils.Messages;
import org.apache.soap.transport.TransportMessage;
import org.apache.wsif.WSIFException;
import org.apache.wsif.logging.Trc;

/**
 * This class implements a JMS to HTTP bridge. That is it takes SOAP
 * messages off of a JMS queue and posts them using HTTP. The SOAP message
 * in the HTTP response is put on a JMS reply queue. This class contains a
 * main method which takes as parameters all the JMS and HTTP information
 * needed. This bridge can be cold or warm started. Cold starting wipes
 * messages off queues on startup, whereas warm starting does not.
 * 
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class JMS2HTTPBridge {
    private String httpURL = null;
    private JMS2HTTPBridgeDestination destination = null;

    private static final ArrayList outGoingHeaders =
        new ArrayList(
            Arrays.asList(new String[] { "SOAPAction", "WSIFContentType" }));

    private static final ArrayList interestingProperties =
        new ArrayList(
            Arrays.asList(new String[] { "JMSPriority", "JMSDeliveryMode" }));

    private HashMap strProp = new HashMap();
    private boolean maintainSession = false;
    private boolean http10 = true;
    private Message original = null;
    private Integer statusCode = null;
    private String statusMessage = null;
    private boolean verbose = false;
    private boolean veryVerbose = false;
    
    // Sync timeout of 10000 milliseconds is the default.
    private int syncTimeout = 10000;

    private WSIFJMSListener list = new WSIFJMSListener() {
        public void onException(JMSException arg1) {
            Trc.entry(this, arg1);
            System.err.println("Caught an exception!");
            arg1.printStackTrace();
            Trc.exit();
        }

        public void onMessage(Message message) {
            Trc.entry(this, message);
            receiveMessage(message);
            Trc.exit();
        }
    };

    public JMS2HTTPBridge(
        String initialContextFactory,
        String jndiUrl,
        String queueConnectionFactory,
        String readQueue,
        String httpURL,
        String startType,
        int syncTimeout,
        boolean verbose)
        throws Exception {
        Trc.entry(
            this,
            initialContextFactory,
            jndiUrl,
            queueConnectionFactory,
            readQueue,
            httpURL,
            startType,
            new Integer(syncTimeout),
            new Boolean(verbose));

        if (verbose)
            System.out.println(
                "Starting the JMS2HTTPBridge with"
                    + "\n"
                    + "Initial Context Factory = "
                    + initialContextFactory
                    + "\n"
                    + "JNDI URL = "
                    + jndiUrl
                    + "\n"
                    + "Queue Connection Factory  = "
                    + queueConnectionFactory
                    + "\n"
                    + "JNDI Read Queue = "
                    + readQueue
                    + "\n"
                    + "HTTP URL = "
                    + httpURL
                    + "\n"
                    + "Start Type = "
                    + startType
                    + "\n"
                    + "Synchronous Timeout = "
                    + syncTimeout
                    + "\n"
                    + "Verbose = "
                    + verbose);

        destination =
            new JMS2HTTPBridgeDestination(
                new WSIFJMSFinderForJndi(
                    null,
                    initialContextFactory,
                    jndiUrl,
                    WSIFJMSFinder.STYLE_QUEUE,
                    queueConnectionFactory,
                    readQueue,
                    null),
                null,
                WSIFJMSConstants.WAIT_FOREVER,
                startType,
                verbose);

        this.httpURL = httpURL;
        this.syncTimeout = syncTimeout;
        this.verbose = verbose;
        Trc.exit();
    }

    public static void main(String[] args) throws Exception {
        Trc.entry(null, args);

        String usage =
            "Usage: java "
                + JMS2HTTPBridge.class.getName()
                + " [-cold|-warm] "
                + "-icf <initialContextFactory> "
                + "-jndi <jndiUrl> "
                + "-s <sampleName> "
                + "-qcf <queueConnectionFactory> "
                + "-q <readQueue> "
                + "-http <httpURL> "
                + "-t <syncTimeout> "
                + "-v";

        String startType = JMS2HTTPBridgeDestination.WARMSTART;
        String initialContextFactory =
            "com.sun.jndi.fscontext.RefFSContextFactory";
        String jndiUrl = "file:///JNDI-Directory";
        String sampleName = null;
        String queueConnectionFactory = "WSIFSampleQCF";
        String readQueue = null;
        String httpURL = "http://localhost:8080/soap/servlet/rpcrouter";
        // Sync timeout of 10000 milliseconds is the default.
        int syncTimeout = 10000;
        boolean verbose = false;

        for (int idx = 0; idx < args.length; idx++) {
            if (!args[idx].startsWith("-"))
                throw new Exception("Bad parameter\n" + usage);

            if (args[idx].equals("-cold")) {
                startType = JMS2HTTPBridgeDestination.COLDSTART;
            } else if (args[idx].equals("-warm")) {
                startType = JMS2HTTPBridgeDestination.WARMSTART;
            } else if (args[idx].equals("-icf")) {
                idx++;
                initialContextFactory = args[idx];
            } else if (args[idx].equals("-jndi")) {
                idx++;
                jndiUrl = args[idx];
            } else if (args[idx].equals("-s")) {
                idx++;
                sampleName = args[idx];
            } else if (args[idx].equals("-qcf")) {
                idx++;
                queueConnectionFactory = args[idx];
            } else if (args[idx].equals("-q")) {
                idx++;
                readQueue = args[idx];
            } else if (args[idx].equals("-http")) {
                idx++;
                httpURL = args[idx];
            } else if (args[idx].equals("-t")) {
                idx++;
                syncTimeout = new Integer(args[idx]).intValue();
            } else if (args[idx].equals("-v")) {
                verbose = true;
            } else
                throw new Exception("Bad parameter\n" + usage);
        }

        if (readQueue == null && sampleName != null)
            readQueue = "SoapJms" + sampleName + "Queue";

        if (startType == null
            || initialContextFactory == null
            || jndiUrl == null
            || queueConnectionFactory == null
            || readQueue == null
            || httpURL == null)
            throw new Exception("Missing parameter\n" + usage);

        JMS2HTTPBridge j2h =
            new JMS2HTTPBridge(
                initialContextFactory,
                jndiUrl,
                queueConnectionFactory,
                readQueue,
                httpURL,
                startType,
                syncTimeout,
                verbose);

        j2h.listen();
        Trc.exit();
    }

    public void listen() throws WSIFException {
        Trc.entry(this);
        destination.listen(list);
        Trc.exit();
    }

    void receiveMessage(Message msg) {
        Trc.entry(this, msg);
        String payload = null;
        original = msg;

        try {
            if (verbose)
                System.out.println("JMS2HTTPBridge Caught a message!");

            if (msg instanceof TextMessage) {
                String body = ((TextMessage) msg).getText();
                if (body != null) {
                    if (verbose)
                        System.out.println(
                            "JMS2HTTPBridge Message contained '" + body + "'");

                    String url = getServiceURL(msg);
                    URL tmpUrl = new URL(url);
                    String host = tmpUrl.getHost();
                    int port = tmpUrl.getPort();

                    statusCode = null;
                    statusMessage = null;
                    BooleanHolder useFullURL = new BooleanHolder(false);
                    StringBuffer otherHeaders = new StringBuffer();
                    
                    // The SocketFactoryFactory.getFactory method has changed 
                    // signature between Axis 1.0 and Axis 1.1 so avoid its use
                    // by using DefaultSocketFactory instead. This prevents the
                    // bridge from using secure sockets.
                    // SocketFactory factory =
                    //     SocketFactoryFactory.getFactory(new Hashtable());
                    SocketFactory factory =
                        new DefaultSocketFactory(new Hashtable());

                    Socket sock =
                        factory.create(host, port, otherHeaders, useFullURL);

                    String ct = "text/xml; charset=utf-8";
                    if (msg.propertyExists("WSIFContentType")) {
                        try {
                            ct =
                                (String) msg.getStringProperty(
                                    "WSIFContentType");
                        } catch (JMSException je) {
                        	// Safe to ignore this exception as ct has a good default
                            Trc.ignoredException(je);
                        }
                    }

                    String action = " ";
                    if (msg.propertyExists("SOAPAction")) {
                        try {
                            action =
                                (String) msg.getStringProperty("SOAPAction");
                        } catch (JMSException je) {
                        	// Safe to ignore this exception as action has a good default
                            Trc.ignoredException(je);
                        }
                    }

                    InputStream inp =
                        writeToSocket(
                            sock,
                            body,
                            url,
                            otherHeaders,
                            host,
                            port,
                            useFullURL,
                            ct,
                            body.length(),
                            action);

                    // Read the response back from the server
                    readFromSocket(sock, inp, null);
                }
            } else {
                System.err.println(
                    "JMS2HTTPBridge ignoring message because it was not a TextMessage");
                Trc.exit();
                return;
            }
        } catch (Exception e) {
            Trc.exception(e);
            e.printStackTrace();

            String errText =
                "<?xml version='1.0' encoding='UTF-8'?>\n"
                    + "<SOAP-ENV:Envelope "
                    + "xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                    + "xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" "
                    + "xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\">\n"
                    + "<SOAP-ENV:Body>\n"
                    + "<SOAP-ENV:Fault>\n"
                    + "<faultcode>SOAP-ENV:Server</faultcode>\n"
                    + "<faultstring>"
                    + "JMS2HTTPBridge caught "
                    + e
                    + "</faultstring>\n"
                    + "<faultactor>org.apache.wsif.util.jms.JMS2HTTPBridge</faultactor>\n"
                    + "</SOAP-ENV:Fault>\n\n"
                    + "</SOAP-ENV:Body>\n"
                    + "</SOAP-ENV:Envelope>";

            try {
                destination.setReplyToQueue((Queue) original.getJMSReplyTo());
                destination.send(errText, original.getJMSMessageID());
            } catch (Exception e2) {
                // Deep trouble
                Trc.exception(e2);
                System.err.println(
                    "JMS2HTTPBridge unable to send exception information "
                        + "back to the WSIF client. Exception "
                        + e2);
            }
        }

        Trc.exit();
        return;
    }



    /**
     * Send the soap request message to the server
     *
     * @param sock socket
     * @param msgContext message context
     * @param tmpURL url to connect to
     * @param otherHeaders other headers if any
     * @param host host name
     * @param port port
     * @param useFullURL flag to indicate if the whole url needs to be sent
     *
     * @throws IOException
     */
    private InputStream writeToSocket(
        Socket sock,
        String body,
        String url,
        StringBuffer otherHeaders,
        String host,
        int port,
        BooleanHolder useFullURL,
        String ct,
        long cl,
        String action)
        throws Exception {

        Trc.entry(
            this,
            sock,
            body,
            url,
            otherHeaders,
            host,
            new Integer(port),
            useFullURL,
            ct,
            new Long(cl),
            action);

        String userID = null;
        String passwd = null;
        String reqEnv = null;
        //In case it is necessary to read before the full respose.
        InputStream inp = null;

        //        userID = msgContext.getUsername();
        //        passwd = msgContext.getPassword();

        // Get SOAPAction, default to ""
        //        String action = msgContext.useSOAPAction()
        //                ? msgContext.getSOAPActionURI()
        //                : "";

        if (action == null) {
            action = "";
        }

        if (userID != null) {
            StringBuffer tmpBuf = new StringBuffer();

            tmpBuf.append(userID).append(":").append(
                (passwd == null) ? "" : passwd);
            otherHeaders
                .append(HTTPConstants.HEADER_AUTHORIZATION)
                .append(": Basic ")
                .append(Base64.encode(tmpBuf.toString().getBytes()))
                .append("\r\n");
        }

        // don't forget the cookies!
        // mmm... cookies
        if (maintainSession) {
            String cookie = (String) strProp.get(HTTPConstants.HEADER_COOKIE);
            String cookie2 = (String) strProp.get(HTTPConstants.HEADER_COOKIE2);

            if (cookie != null) {
                otherHeaders
                    .append(HTTPConstants.HEADER_COOKIE)
                    .append(": ")
                    .append(cookie)
                    .append("\r\n");
            }
            if (cookie2 != null) {
                otherHeaders
                    .append(HTTPConstants.HEADER_COOKIE2)
                    .append(": ")
                    .append(cookie2)
                    .append("\r\n");
            }
        }
        StringBuffer header = new StringBuffer();

        // byte[] request = reqEnv.getBytes();
        header.append(HTTPConstants.HEADER_POST).append(" ").append(
            new URL(url).getFile());

        //        Message reqMessage = msgContext.getRequestMessage();

        //True if this is to use HTTP 1.0 / false HTTP 1.1
        boolean http10 = true;
        boolean httpChunkStream = false; //Use HTTP chunking or not.
        //Under HTTP 1.1 if false you *MAY* need to wait for a 100 rc,
        //  if true the server MUST reply with 100 continue.
        boolean httpContinueExpected = false;
        String httpConnection = null;

        String httpver =
            (String) strProp.get(MessageContext.HTTP_TRANSPORT_VERSION);
        if (null == httpver)
            httpver = HTTPConstants.HEADER_PROTOCOL_V10;
        httpver = httpver.trim();
        if (httpver.equals(HTTPConstants.HEADER_PROTOCOL_V11)) {
            http10 = false;
        }

        //process user defined headers for information.
        Hashtable userHeaderTable = null; 
        // (Hashtable) msgContext.getProperty(HTTPConstants.REQUEST_HEADERS);

        if (userHeaderTable != null) {
            if (null == otherHeaders)
                otherHeaders = new StringBuffer(1024);

            for (java.util.Iterator e = userHeaderTable.entrySet().iterator();
                e.hasNext();
                ) {

                java.util.Map.Entry me = (java.util.Map.Entry) e.next();
                Object keyObj = me.getKey();
                if (null == keyObj)
                    continue;
                String key = keyObj.toString().trim();

                if (key
                    .equalsIgnoreCase(HTTPConstants.HEADER_TRANSFER_ENCODING)) {
                    if (!http10) {
                        String val = me.getValue().toString();
                        if (null != val
                            && val.trim().equalsIgnoreCase(
                                HTTPConstants
                                    .HEADER_TRANSFER_ENCODING_CHUNKED))
                            httpChunkStream = true;
                    }
                } else if (key.equalsIgnoreCase(HTTPConstants.HEADER_HOST)) {
                    //ignore
                } else if (
                    key.equalsIgnoreCase(HTTPConstants.HEADER_CONTENT_TYPE)) {
                    //ignore
                } else if (
                    key.equalsIgnoreCase(HTTPConstants.HEADER_SOAP_ACTION)) {
                    //ignore
                } else if (
                    key.equalsIgnoreCase(
                        HTTPConstants.HEADER_CONTENT_LENGTH)) {
                    //ignore
                } else if (key.equalsIgnoreCase(HTTPConstants.HEADER_COOKIE)) {
                    //ignore
                } else if (
                    key.equalsIgnoreCase(HTTPConstants.HEADER_COOKIE2)) {
                    //ignore
                } else if (
                    key.equalsIgnoreCase(HTTPConstants.HEADER_AUTHORIZATION)) {
                    //ignore
                } else if (
                    key.equalsIgnoreCase(
                        HTTPConstants.HEADER_PROXY_AUTHORIZATION)) {
                    //ignore
                } else if (
                    key.equalsIgnoreCase(HTTPConstants.HEADER_CONNECTION)) {
                    if (!http10) {
                        String val = me.getValue().toString();
                        if (val
                            .trim()
                            .equalsIgnoreCase(
                                HTTPConstants.HEADER_CONNECTION_CLOSE))
                            httpConnection =
                                HTTPConstants.HEADER_CONNECTION_CLOSE;
                    }
                    //HTTP 1.0 will always close.
                    //HTTP 1.1 will use persistent. //no need to specify
                } else {
                    if (!http10
                        && key.equalsIgnoreCase(HTTPConstants.HEADER_EXPECT)) {
                        String val = me.getValue().toString();
                        if (null != val
                            && val.trim().equalsIgnoreCase(
                                HTTPConstants.HEADER_EXPECT_100_Continue))
                            httpContinueExpected = true;
                    }

                    otherHeaders.append(key).append(": ").append(
                        me.getValue()).append(
                        "\r\n");
                }
            }
        }

        if (!http10)
            httpConnection = HTTPConstants.HEADER_CONNECTION_CLOSE;
        //Force close for now.

        header.append(" ");
        header
            .append(
                http10
                    ? HTTPConstants.HEADER_PROTOCOL_10
                    : HTTPConstants.HEADER_PROTOCOL_11)
            .append("\r\n")
            .append(HTTPConstants.HEADER_CONTENT_TYPE)
            .append(": ")
            .append(ct)
            .append("\r\n")
            .append(
                HTTPConstants.HEADER_ACCEPT)
        //Limit to the types that are meaningful to us.
        .append(": ")
        .append(HTTPConstants.HEADER_ACCEPT_APPL_SOAP)
        .append(", ")
        .append(HTTPConstants.HEADER_ACCEPT_APPLICATION_DIME)
        .append(", ")
        .append(HTTPConstants.HEADER_ACCEPT_MULTIPART_RELATED)
        .append(", ")
        .append(HTTPConstants.HEADER_ACCEPT_TEXT_ALL)
        .append("\r\n")
        .append(HTTPConstants.HEADER_USER_AGENT) //Tell who we are.
        .append(": ").append("WSIF").append("\r\n").append(
            HTTPConstants.HEADER_HOST) //used for virtual connections
        .append(": ")
        .append(host)
        .append((port == -1) ? ("") : (":" + port))
        .append("\r\n")
        .append(
            HTTPConstants.HEADER_CACHE_CONTROL)
        //Stop caching proxies from caching SOAP reqeuest.
        .append(": ")
        .append(HTTPConstants.HEADER_CACHE_CONTROL_NOCACHE)
        .append("\r\n")
        .append(HTTPConstants.HEADER_PRAGMA)
        .append(": ")
        .append(HTTPConstants.HEADER_CACHE_CONTROL_NOCACHE)
        .append("\r\n")
        .append(HTTPConstants.HEADER_SOAP_ACTION) //The SOAP action.
        .append(": \"").append(action).append("\"\r\n");

        if (!httpChunkStream) {
            //Content length MUST be sent on HTTP 1.0 requests.
            header
                .append(HTTPConstants.HEADER_CONTENT_LENGTH)
                .append(": ")
                .append(cl)
                .append("\r\n");
        } else {
            //Do http chunking.
            header
                .append(HTTPConstants.HEADER_TRANSFER_ENCODING)
                .append(": ")
                .append(HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED)
                .append("\r\n");
        }

        if (null != httpConnection) {
            header.append(HTTPConstants.HEADER_CONNECTION);
            header.append(": ");
            header.append(httpConnection);
            header.append("\r\n");
        }

        if (null != otherHeaders)
            header.append(otherHeaders); //Add other headers to the end.


        header.append("\r\n"); //The empty line to start the BODY.

        OutputStream out = sock.getOutputStream();

        if (httpChunkStream) {
            throw new WSIFException("Chunking not supported by the JMS2HTTPBridge");
            //            out.write(header.toString()
            //                    .getBytes(HTTPConstants.HEADER_DEFAULT_CHAR_ENCODING));
            //            if(httpContinueExpected ){ //We need to get a reply from the server as to whether
            //                                      // it wants us send anything more.
            //                out.flush();
            //                Hashtable cheaders= new Hashtable ();
            //                inp=readFromSocket(sock, msgContext, null, cheaders);
            //                int returnCode= -1;
            //                Integer Irc= (Integer)msgContext.getProperty(HTTPConstants.MC_HTTP_STATUS_CODE);
            //                if(null != Irc) returnCode= Irc.intValue();
            //                if(100 == returnCode){  // got 100 we may continue.
            //                    //Need todo a little msgContext house keeping....
            //                    msgContext.removeProperty(HTTPConstants.MC_HTTP_STATUS_CODE);
            //                    msgContext.removeProperty(HTTPConstants.MC_HTTP_STATUS_MESSAGE);
            //                }
            //                else{ //If no 100 Continue then we must not send anything!
            //                    String statusMessage= (String)
            //                        msgContext.getProperty(HTTPConstants.MC_HTTP_STATUS_MESSAGE);
            //
            //                    AxisFault fault = new AxisFault("HTTP", "(" + returnCode+ ")" + statusMessage, null, null);
            //
            //                    fault.setFaultDetailString(Messages.getMessage("return01",
            //                            "" + returnCode, ""));
            //                    throw fault;
            //               }
            //
            //
            //            }
            //            ChunkedOutputStream chunkedOutputStream = new ChunkedOutputStream(out);
            //            out = new BufferedOutputStream(chunkedOutputStream, 8 * 1024);
            //            try {
            //                reqMessage.writeTo(out);
            //            } catch (SOAPException e) {
            //                log.error(Messages.getMessage("exception00"), e);
            //            }
            //            out.flush();
            //            chunkedOutputStream.eos();
        } else {
            //No chunking...
            if (httpContinueExpected) { 
            	//We need to get a reply from the server as to whether
                // it wants us send anything more.
                throw new WSIFException("Continue expected is not supported by the JMS2HTTPBridge");
                //                out.flush();
                //                Hashtable cheaders= new Hashtable ();
                //                inp=readFromSocket(sock, msgContext, null, cheaders);
                //                int returnCode= -1;
                //                Integer Irc=  (Integer) msgContext.getProperty(HTTPConstants.MC_HTTP_STATUS_CODE);
                //                if(null != Irc) returnCode= Irc.intValue();
                //                if(100 == returnCode){  // got 100 we may continue.
                //                    //Need todo a little msgContext house keeping....
                //                    msgContext.setProperty(HTTPConstants.MC_HTTP_STATUS_CODE,
                //                            null);
                //                    msgContext.setProperty(HTTPConstants.MC_HTTP_STATUS_MESSAGE,
                //                            null);
                //                }
                //                else{ //If no 100 Continue then we must not send anything!
                //                    String statusMessage= (String)
                //                        msgContext.getProperty(HTTPConstants.MC_HTTP_STATUS_MESSAGE);
                //
                //                    AxisFault fault = new AxisFault("HTTP", "(" + returnCode+ ")" + statusMessage, null, null);
                //
                //                    fault.setFaultDetailString(Messages.getMessage("return01",
                //                            "" + returnCode, ""));
                //                    throw fault;
                //               }
            }

            out = new BufferedOutputStream(out, 8 * 1024);
            ByteArrayOutputStream out1 = new ByteArrayOutputStream();
            out1.write(
                header.toString().getBytes(
                    HTTPConstants.HEADER_DEFAULT_CHAR_ENCODING));
            out1.write(body.getBytes());

            if (verbose)
                System.out.println(
                    "JMS2HTTPBridge writeToSocket " + out1.toString());
            out.write(out1.toByteArray());

            // Flush ONLY once.
            out.flush();
        }
        Trc.exit(inp);
        return inp;
    }

    /**
     * Reads the SOAP response back from the server
     *
     * @param sock socket
     * @param msgContext message context
     *
     * @throws IOException
     */
    private InputStream readFromSocket(
        Socket sock,
        InputStream inp,
        Hashtable headers)
        throws Exception {
        Trc.entry(this, sock, inp, headers);

        Message outMsg = null;
        byte b;
        int len = 0;
        int colonIndex = -1;
        boolean headersOnly = false;
        if (null != headers) {
            headersOnly = true;
        } else {
            headers = new Hashtable();
        }
        String name, value;
        statusMessage = "";
        int returnCode = 0;
        if (null == inp)
            inp = new BufferedInputStream(sock.getInputStream());

        // Should help performance. Temporary fix only till its all stream oriented.
        // Need to add logic for getting the version # and the return code
        // but that's for tomorrow!

        /* Logic to read HTTP response headers */
        boolean readTooMuch = false;

        b = 0;
        StringBuffer toStdout = new StringBuffer();
        for (ByteArrayOutputStream buf = new ByteArrayOutputStream(4097);;) {
            if (!readTooMuch) {
                b = (byte) inp.read();
                if (veryVerbose)
                    System.out.println("read1 " + b + " " + (char) b);
                toStdout.append((char) b);
            }
            if (b == -1) {
                break;
            }
            readTooMuch = false;
            if ((b != '\r') && (b != '\n')) {
                if ((b == ':') && (colonIndex == -1)) {
                    colonIndex = len;
                }
                len++;
                buf.write(b);
            } else if (b == '\r') {
                continue;
            } else { // b== '\n'
                if (len == 0) {
                    break;
                }
                b = (byte) inp.read();
                if (veryVerbose)
                    System.out.println("read2 " + b + " " + (char) b);
                toStdout.append((char) b);
                readTooMuch = true;

                // A space or tab at the begining of a line means the header continues.
                if ((b == ' ') || (b == '\t')) {
                    continue;
                }
                buf.close();
                byte[] hdata = buf.toByteArray();
                buf.reset();
                if (colonIndex != -1) {
                    name =
                        new String(
                            hdata,
                            0,
                            colonIndex,
                            HTTPConstants.HEADER_DEFAULT_CHAR_ENCODING);
                    value =
                        new String(
                            hdata,
                            colonIndex + 1,
                            len - 1 - colonIndex,
                            HTTPConstants.HEADER_DEFAULT_CHAR_ENCODING);
                    colonIndex = -1;
                } else {

                    name =
                        new String(
                            hdata,
                            0,
                            len,
                            HTTPConstants.HEADER_DEFAULT_CHAR_ENCODING);
                    value = "";
                }
                Trc.event("name=", name, " value=", value);
                if (verbose)
                    System.out.println("JMS2HTTPBridge name=" + name + " value=" + value);

                if (statusCode == null) {

                    // Reader status code
                    int start = name.indexOf(' ') + 1;
                    String tmp = name.substring(start).trim();
                    int end = tmp.indexOf(' ');

                    if (end != -1) {
                        tmp = tmp.substring(0, end);
                    }
                    returnCode = Integer.parseInt(tmp);
                    statusCode = new Integer(returnCode);
                    //                    msgContext.setProperty(HTTPConstants.MC_HTTP_STATUS_CODE,
                    //                            new Integer(returnCode));
                    statusMessage = name.substring(start + end + 1);
                    //                    msgContext.setProperty(HTTPConstants.MC_HTTP_STATUS_MESSAGE,
                    //                            statusMessage);
                    if (veryVerbose)
                        System.out.println(
                            "returncode/statuscode "
                                + returnCode
                                + "/"
                                + statusCode);
                } else {
                    headers.put(name.toLowerCase(), value);
                }
                len = 0;
            }
        }

        if (verbose)
            System.out.println("JMS2HTTPBridge readFromSocket " + toStdout);
        if (headersOnly) {
            Trc.exit(inp);
            return inp;
        }

        /* All HTTP headers have been read. */
        String contentType =
            (String) headers.get(
                HTTPConstants.HEADER_CONTENT_TYPE.toLowerCase());

        contentType = (null == contentType) ? null : contentType.trim();
        if ((returnCode > 199) && (returnCode < 300)) {
            // SOAP return is OK - so fall through
        } else if (
            (contentType != null)
                && !contentType.startsWith("text/html")
                && ((returnCode > 499) && (returnCode < 600))) {
            // SOAP Fault should be in here - so fall through
        } else {
            // Unknown return code - so wrap up the content into a
            // SOAP Fault.
            ByteArrayOutputStream buf = new ByteArrayOutputStream(4097);

            while (-1 != (b = (byte) inp.read())) {
                buf.write(b);
            }
            AxisFault fault =
                new AxisFault(
                    "HTTP",
                    "(" + returnCode + ")" + statusMessage,
                    null,
                    null);

            fault.setFaultDetailString(
                Messages.getMessage(
                    "return01",
                    "" + returnCode,
                    buf.toString()));
            throw fault;
        }
        if (b != -1) { // more data than just headers.
            String contentLocation =
                (String) headers.get(
                    HTTPConstants.HEADER_CONTENT_LOCATION.toLowerCase());

            contentLocation =
                (null == contentLocation) ? null : contentLocation.trim();

            String contentLength =
                (String) headers.get(
                    HTTPConstants.HEADER_CONTENT_LENGTH.toLowerCase());

            contentLength =
                (null == contentLength) ? null : contentLength.trim();

            String transferEncoding =
                (String) headers.get(
                    HTTPConstants.HEADER_TRANSFER_ENCODING.toLowerCase());
            if (null != transferEncoding
                && transferEncoding.trim().equals(
                    HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED)) {
                inp = new ChunkedInputStream(inp);
            }

            sendJmsReply(inp, contentType, contentLocation, contentLength);

            //            outMsg = new Message( new SocketInputStream(inp, sock), false, contentType,
            //                    contentLocation);
            //            outMsg.setMessageType(Message.RESPONSE);
            //            msgContext.setResponseMessage(outMsg);
            //            if (log.isDebugEnabled()) {
            //                if (null == contentLength) {
            //                    log.debug("\n"
            //                            + Messages.getMessage("no00", "Content-Length"));
            //                }
            //                log.debug("\n" + Messages.getMessage("xmlRecd00"));
            //                log.debug("-----------------------------------------------");
            //                log.debug((String) outMsg.getSOAPPartAsString());
            //            }
        }

        // if we are maintaining session state,
        // handle cookies (if any)
        if (maintainSession) {
            handleCookie(
                HTTPConstants.HEADER_COOKIE,
                HTTPConstants.HEADER_SET_COOKIE,
                headers);
            handleCookie(
                HTTPConstants.HEADER_COOKIE2,
                HTTPConstants.HEADER_SET_COOKIE2,
                headers);
        }
        Trc.exit(inp);
        return inp;
    }


    private void sendJmsReply(
        InputStream inp,
        String contentType,
        String contentLocation,
        String contentLength)
        throws Exception {
        Trc.entry(this, inp, contentType, contentLocation);

        // Zero means unknown length. Will just read from inp once.
        int cl = 0;
        if (contentLength != null)
            try {
                cl = new Integer(contentLength).intValue();
            } catch (Exception e) {
                Trc.ignoredException(e);
            }

        byte[] buff = null;
        if (cl != 0)
            buff = new byte[cl];

        int bytesRead = 0;
        int cnt = 0;
        // do {} while() ensures we read once if contentLength is unknown
        do {
            int avail = inp.available();
            while ((avail == 0)
                && ((syncTimeout == 0) || (cnt * 100 < syncTimeout))) {

                if (verbose)
                    System.out.println("wait " + cnt);
                cnt++;
                Thread.sleep(100);
                avail = inp.available();
            }

            if (syncTimeout != 0 && cnt * 100 >= syncTimeout) {
                String errText =
                    "JMS2HTTPBridge timeout before reading complete "
                        + "message from socket. Bytes read="
                        + bytesRead;
                System.err.println(errText);
                throw new WSIFException(errText);
            }

            if (veryVerbose)
                System.out.println(
                    "JMS2HTTPBridge sendJmsReply avail="
                        + avail
                        + " bytesRead="
                        + bytesRead);

            if (cl == 0)
                buff = new byte[avail];
            inp.read(buff, bytesRead, avail);
            bytesRead += avail;
        }
        while (bytesRead < cl);
        String sBuff = new String(buff);

        if (verbose)
            System.out.println(
                "JMS2HTTPBridge sendJmsReply len="
                    + sBuff.length()
                    + " buff="
                    + sBuff);

        // Put the properties from the received message onto the message we are 
        // about to send. Filter out everything but those properties that we 
        // know about and are interested in, since there'll be lots of stuff in
        // props that aren't really properties at all.
        WSIFJMSProperties props = new WSIFJMSProperties(WSIFJMSProperties.OUT);
        props.getPropertiesFromMessage(original);
        HashMap kept = new HashMap();
        Iterator it = interestingProperties.iterator();
        while (true) {
            try {
                if (!it.hasNext())
                    break;
                String prop = (String) it.next();
                if (props.containsKey(prop))
                    kept.put(prop, props.get(prop));
            } catch (Exception e) {
            	Trc.ignoredException(e);
                e.printStackTrace();
            }
        }

        if (contentType != null)
            kept.put("WSIFContentType", contentType);
        if (contentLocation != null)
            kept.put("WSIFContentLocation", contentLocation);

        destination.setProperties(kept);
        destination.setReplyToQueue((Queue) original.getJMSReplyTo());
        destination.send(sBuff, original.getJMSMessageID());

        Trc.exit();
    }
    
    /**
     * little helper function for cookies
     *
     * @param cookieName
     * @param setCookieName
     * @param headers
     * @param msgContext
     */
    public void handleCookie(
        String cookieName,
        String setCookieName,
        Hashtable headers) {
        Trc.entry(this, cookieName, setCookieName, headers);

        if (headers.containsKey(setCookieName.toLowerCase())) {
            String cookie = (String) headers.get(setCookieName.toLowerCase());
            cookie = cookie.trim();

            // chop after first ; a la Apache SOAP (see HTTPUtils.java there)
            int index = cookie.indexOf(';');

            if (index != -1) {
                cookie = cookie.substring(0, index);
            }
            strProp.put(cookieName, cookie);
        }
        Trc.exit();
    }

    private void setOutGoingHeaders(Message m, TransportMessage tmsg) {
        Trc.entry(this, m, tmsg);
        for (Iterator i = outGoingHeaders.iterator(); i.hasNext();) {
            try {
                String name = (String) i.next();
                if (m.propertyExists(name)) {
                    String value = m.getStringProperty(name);
                    if (value != null && value.length() > 0)
                        if ("WSIFContentType".equals(name))
                            tmsg.setContentType(value);
                        else
                            tmsg.setHeader(name, "\"" + value + "\"");
                }
            } catch (JMSException e) {
                Trc.ignoredException(e);
                e.printStackTrace();
            }
        }
        Trc.exit();
    }

    private String getServiceURL(Message m) {
        Trc.entry(this, m);
        String url = null;
        try {
            if (m.propertyExists("WSIFServiceURL")) {
                url = m.getStringProperty("WSIFServiceURL");
                if (verbose)
                    System.out.println("JMS2HTTPBridge WSIFServiceURL=" + url);
            }
        } catch (Exception e) {
            Trc.ignoredException(e);
            // Quietly ignore this as we default to httpURL
        }

        if (url == null)
            url = httpURL;
        Trc.exit(url);
        return url;
    }

}
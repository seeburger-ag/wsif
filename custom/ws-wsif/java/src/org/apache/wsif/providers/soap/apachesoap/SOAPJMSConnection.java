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

package org.apache.wsif.providers.soap.apachesoap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Hashtable;
import java.util.HashMap;

import javax.mail.MessagingException;
import org.apache.soap.Envelope;
import org.apache.soap.SOAPException;
import org.apache.soap.encoding.SOAPMappingRegistry;
import org.apache.soap.rpc.SOAPContext;
import org.apache.soap.transport.SOAPTransport;
import org.apache.wsif.WSIFCorrelationId;
import org.apache.wsif.WSIFCorrelationService;
import org.apache.wsif.WSIFException;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.util.WSIFCorrelationServiceLocator;
import org.apache.wsif.util.WSIFProperties;
import org.apache.wsif.util.jms.WSIFJMSCorrelationId;
import org.apache.wsif.util.jms.WSIFJMSDestination;
import org.apache.wsif.util.jms.WSIFJMSFinder;
import org.apache.wsif.wsdl.extensions.jms.JMSAddress;

/**
 * This class is a SOAPTransport that supports JMS.
 *
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class SOAPJMSConnection implements SOAPTransport {
    private BufferedReader responseReader = null;
    private SOAPContext responseSOAPContext = null;
    private WSIFJMSDestination destination = null;

    // folowing are for async operation
    private boolean asyncOperation = false;
    private WSIFOperation_ApacheSOAP wsifOperation = null;
    private long syncTimeout;
    private long asyncTimeout;

    private static final String DUMMY_RESPONSE =
        "<?xml version='1.0' encoding='UTF-8'?>\n<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\">\n<SOAP-ENV:Body>\n<ns1:addEntryResponse xmlns:ns1=\"http://wsifservice.addressbook/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n</ns1:addEntryResponse>\n\n</SOAP-ENV:Body>\n</SOAP-ENV:Envelope>";

    public SOAPJMSConnection(JMSAddress ja, String portName) throws WSIFException {
        Trc.entry(this, ja, portName);
        destination =
            new WSIFJMSDestination(
                WSIFJMSFinder.newFinder(ja,portName),
                ja.getJmsProvDestName(),
                WSIFProperties.getSyncTimeout());
        Trc.exit(ja);
    }

    public Hashtable getHeaders() {
        Trc.entry(this);
        Trc.exit(null);
        return null;
    }

    public SOAPContext getResponseSOAPContext() {
        Trc.entry(this);
        Trc.exit(responseSOAPContext);
        return responseSOAPContext;
    }

    public long getSyncTimeout() {
        Trc.entry(this);
        Trc.exit(new Long(syncTimeout));
        return syncTimeout;
    }

    public long getAsyncTimeout() {
        Trc.entry(this);
        Trc.exit(new Long(asyncTimeout));
        return asyncTimeout;
    }

    public BufferedReader receive() {
        Trc.entry(this);
        Trc.exit();
        return responseReader;
    }

    /**
     * This send method is really doing a send followed by a receive. The
     * receive method just returns the BufferedReader that is set up by
     * this method.
     */
    public void send(
        URL sendTo,
        String action,
        Hashtable headers,
        Envelope env,
        SOAPMappingRegistry smr,
        SOAPContext ctx)
        throws SOAPException {
        Trc.entry(this, sendTo, action, headers, env, smr, ctx);

        try {
            if (isAsyncOperation()) {
                performAsyncSend(sendTo, action, headers, env, smr, ctx);
            } else {
                StringWriter payloadSW = new StringWriter();
                env.marshall(payloadSW, smr, ctx);
                String id = destination.send(payloadSW.toString(), null);
                String response = destination.receiveString(id, syncTimeout);
                responseSOAPContext = new SOAPContext();
                responseSOAPContext.setRootPart(response, "text/xml");
                responseReader = new BufferedReader(new StringReader(response));
            }
        } catch (IOException ioe) {
        	Trc.exception(ioe);
            // Not sure what the faultCode should be - this may be wrong
            throw new SOAPException("WSIF SOAPJMSConnection ", ioe.toString());
        } catch (MessagingException me) {
        	Trc.exception(me);
            // Not sure what the faultCode should be - this may be wrong
            throw new SOAPException("WSIF SOAPJMSConnection ", me.toString());
        }
        Trc.exit();
    }

    /**
     * Send the request asynchronously.
     * After sending the request this associates the JMS message correlation
     * ID with the WSIFOperation in the correlation service, and stores the
     * the correlation ID in the message context so the WSIFOperation can
     * pass it back to the executeRequestResponseAsync caller.  
     */
    private void performAsyncSend(
        URL sendTo,
        String action,
        Hashtable headers,
        Envelope env,
        SOAPMappingRegistry smr,
        SOAPContext ctx)
        throws IOException, SOAPException, MessagingException {
        String msgID;
        StringWriter payloadSW = new StringWriter();
        env.marshall(payloadSW, smr, ctx);
        WSIFOperation_ApacheSOAP wsifOp = (WSIFOperation_ApacheSOAP) getWsifOperation();

        WSIFCorrelationId cid;

        // only save op in the correlation service if there's a response handler
        if ( wsifOp.getResponseHandler() == null ) {
            msgID = destination.send(payloadSW.toString(), null);
            cid = new WSIFJMSCorrelationId( msgID );
        } else {
           WSIFCorrelationService correlator =
              WSIFCorrelationServiceLocator.getCorrelationService();
           synchronized( correlator ) {   
              msgID = destination.send(payloadSW.toString(), null);
              cid = new WSIFJMSCorrelationId( msgID );
              if ( correlator != null ) {
                 correlator.put( 
                    cid,
                    (Serializable)getWsifOperation(),
                    asyncTimeout );
              }
           }
        }
           

        wsifOp.setAsyncRequestID(new WSIFJMSCorrelationId(msgID));

        // SOAP doesn't like a null response so give it a dummy null
        responseSOAPContext = new SOAPContext();
        responseSOAPContext.setRootPart(DUMMY_RESPONSE, "text/xml");
        responseReader = new BufferedReader(new StringReader(DUMMY_RESPONSE));

    }

    public void setAsyncOperation(boolean b) {
        Trc.entry(this, b);
        asyncOperation = b;
        Trc.exit();
    }

    public boolean isAsyncOperation() {
        Trc.entry(this);
        Trc.exit(asyncOperation);
        return asyncOperation;
    }

    public WSIFOperation_ApacheSOAP getWsifOperation() {
        Trc.entry(this);
        Trc.exit(wsifOperation);
        return wsifOperation;
    }

    public void setWsifOperation(WSIFOperation_ApacheSOAP op) {
        Trc.entry(this, op);
        wsifOperation = op;
        Trc.exit();
    }

    public void setJmsProperty(String name, Object value) throws WSIFException {
        Trc.entry(this, name, value);
        destination.setProperty(name, value);
        Trc.exit();
    }

    public void setJmsProperties(HashMap hm) { 
        Trc.entry(this,hm);
        destination.setProperties(hm);  
        Trc.exit();
    }

    public HashMap getJmsProperties() throws WSIFException { 
  	   Trc.entry(this);
  	   HashMap hm = destination.getProperties();  
  	   Trc.exit(hm);
  	   return hm;  
    }

    void close() throws WSIFException {
        Trc.entry(this);
        destination.close();
        Trc.exit();
    }

    public void setSyncTimeout(long timeout) {
        Trc.entry(this, new Long(timeout));
        syncTimeout = timeout;
        Trc.exit();
    }

    public void setAsyncTimeout(long timeout) {
        Trc.entry(this, new Long(timeout));
        asyncTimeout = timeout;
        Trc.exit();
    }
}
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

package org.apache.wsif.providers.soap.apacheaxis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.soap.SOAPException;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.soap.SOAPConstants;
import org.apache.wsif.WSIFCorrelationId;
import org.apache.wsif.WSIFCorrelationService;
import org.apache.wsif.WSIFException;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.util.WSIFCorrelationServiceLocator;
import org.apache.wsif.util.WSIFProperties;
import org.apache.wsif.util.jms.WSIFJMSCorrelationId;
import org.apache.wsif.util.jms.WSIFJMSDestination;

/**
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class WSIFJmsSender extends BasicHandler {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final long SYNC_TIMEOUT = WSIFProperties.getSyncTimeout();
    private static final long ASYNC_TIMEOUT = WSIFProperties.getAsyncTimeout();
    private static final String DUMMY_RESPONSE =
        "<?xml version='1.0' encoding='UTF-8'?>\n<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\">\n<SOAP-ENV:Body>\n<ns1:addEntryResponse xmlns:ns1=\"http://wsifservice.addressbook/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n</ns1:addEntryResponse>\n\n</SOAP-ENV:Body>\n</SOAP-ENV:Envelope>";

    public void invoke(MessageContext messageContext) throws AxisFault {
        Trc.entry(this, messageContext);
        try {
            // Determine if this is an async operation
            boolean asyncMode =
                messageContext.isPropertyTrue(WSIFJmsTransport.ASYNCOPERATION);
            WSIFJMSDestination dest =
                (WSIFJMSDestination) messageContext.getProperty(
                    WSIFJmsTransport.DESTINATION);

            Long transportSyncTimeoutValue =
                (Long) messageContext.getProperty(
                    WSIFJmsTransport.SYNC_TIMEOUT);
            long syncTimeout =
                transportSyncTimeoutValue == null
                    ? SYNC_TIMEOUT
                    : transportSyncTimeoutValue.longValue();

            Message message = messageContext.getRequestMessage();

            // The next line has the desired side effect of setting 
            // up MIME attachements correctly.
            SOAPConstants sc = messageContext.getSOAPConstants();
            String ct = message.getContentType(sc);
            dest.setProperty("WSIFContentType", ct);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            message.writeTo(baos);
            String contents = baos.toString();

            if (asyncMode) {
                performAsyncSend(messageContext, dest, contents);
            } else {
                String id = dest.send(contents, null);
                String response = dest.receiveString(id, syncTimeout);
                // System.out.println("WSIFJmsSender response("+response.length()+") "+response);
                ct = (String) dest.getProperty("WSIFContentType");
                String cl = (String) dest.getProperty("WSIFContentLocation");

                Message responseMessage;
                if (ct != null || cl != null)
                    responseMessage =
                        new Message(
                            new ByteArrayInputStream(response.getBytes()),
                            false,
                            ct,
                            cl);
                else
                    responseMessage = new Message(response);
                messageContext.setResponseMessage(responseMessage);
            }
        } catch (IOException ioe) {
            Trc.exception(ioe);
            throw new AxisFault(ioe.toString());
        } catch (SOAPException se) {
            Trc.exception(se);
            throw new AxisFault(se.toString());
        }
        Trc.exit();
    }

    public void undo(MessageContext messageContext) {
        Trc.entry(this, messageContext);
        Trc.exit();
    }

    /**
     * Send the request asynchronously.
     * If there is a WISFResponseHandler associated with the operation then
     * the the WSIFOperation is stored in the correlation service with the JMS 
     * messgage ID of the request. A listener is then set to listen for the 
     * response to the request and when the response arrives the listener 
     * looks up the responses ID in the correlation service and forwards 
     * the response to the associated WSIFOperation.
     */
    private void performAsyncSend(
        MessageContext messageContext,
        WSIFJMSDestination dest,
        String data)
        throws WSIFException {
        String msgID;

        WSIFOperation_ApacheAxis wsifOp =
            (WSIFOperation_ApacheAxis) messageContext.getProperty(
                WSIFJmsTransport.WSIFOPERATION);

        WSIFCorrelationId cid;

        // only save op in the correlation service if there's a response handler
        if (wsifOp.getResponseHandler() == null) {
            msgID = dest.send(data);
            cid = new WSIFJMSCorrelationId(msgID);
        } else {
            Long transportAsyncTimeoutValue =
                (Long) messageContext.getProperty(
                    WSIFJmsTransport.ASYNC_TIMEOUT);
            long asyncTimeout =
                transportAsyncTimeoutValue == null
                    ? ASYNC_TIMEOUT
                    : transportAsyncTimeoutValue.longValue();
            WSIFCorrelationService correlator =
                WSIFCorrelationServiceLocator.getCorrelationService();
            synchronized (correlator) {
                msgID = dest.send(data);
                cid = new WSIFJMSCorrelationId(msgID);
                if (correlator != null) {
                    correlator.put(cid, (Serializable) wsifOp, asyncTimeout);
                }
            }
        }

        // Save msg ID in the WSIFop for this calling client
        wsifOp.setAsyncRequestID(new WSIFJMSCorrelationId(msgID));

        // Axis doesn't like a null response so give it something
        Message responseMessage = new Message(DUMMY_RESPONSE);
        messageContext.setResponseMessage(responseMessage);
    }
}
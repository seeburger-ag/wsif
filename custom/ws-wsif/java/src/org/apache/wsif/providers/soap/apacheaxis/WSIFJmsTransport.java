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

import org.apache.axis.AxisEngine;
import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.client.Transport;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.util.jms.WSIFJMSDestination;

/**
 * @author Mark Whitlock <whitlock@apache.org>
 * @author Ant Elder <ant.elder@uk.ibm.com>
 */
public class WSIFJmsTransport extends Transport {
    private WSIFJMSDestination destination = null;
    private String asyncOperation = "false";
    private WSIFOperation wsifOperation = null;
    private Long syncTimeout = null;
    private Long asyncTimeout = null;

    public static final String DESTINATION = "destination";
    public static final String ASYNCOPERATION = "asyncOperation";
    public static final String WSIFOPERATION = "wsifOperation";
    public static final String SYNC_TIMEOUT = "syncTimeout";
    public static final String ASYNC_TIMEOUT = "asyncTimeout";

    public WSIFJmsTransport(WSIFJMSDestination destination) throws WSIFException {
    	if (destination == null) {
    		throw new WSIFException("destination is null");
    	}
    	this.destination = destination;
    }

    public void setDestination(WSIFJMSDestination destination) {
        Trc.entry(this, destination);
        this.destination = destination;
        Trc.exit();
    }

    public void setAsyncOperation(String asyncOperation) {
        Trc.entry(this, asyncOperation);
        this.asyncOperation = asyncOperation;
        Trc.exit();
    }

    public void setWsifOperation(WSIFOperation wsifOperation) {
        Trc.entry(this, wsifOperation);
        this.wsifOperation = wsifOperation;
        Trc.exit();
    }

    public void setSyncTimeout(Long syncTimeout) {
        Trc.entry(this, syncTimeout);
        this.syncTimeout = syncTimeout;
        Trc.exit();
    }

    public void setAsyncTimeout(Long asyncTimeout) {
        Trc.entry(this, asyncTimeout);
        this.asyncTimeout = asyncTimeout;
        Trc.exit();
    }

    public WSIFJMSDestination getDestination() {
        Trc.entry(this);
        Trc.exit(this.destination);
        return this.destination;
    }

    public String getAsyncOperation() {
        Trc.entry(this);
        Trc.exit(this.asyncOperation);
        return this.asyncOperation;
    }

    public WSIFOperation getWsifOperation() {
        Trc.entry(this);
        Trc.exit(this.wsifOperation);
        return this.wsifOperation;
    }

    public Long getSyncTimeout() {
        Trc.entry(this);
        Trc.exit(this.syncTimeout);
        return this.syncTimeout;
    }

    public Long getAsyncTimeout() {
        Trc.entry(this);
        Trc.exit(this.asyncTimeout);
        return this.asyncTimeout;
    }

    public void setupMessageContextImpl(
        MessageContext context,
        Call call,
        AxisEngine engine)
        throws AxisFault {
        Trc.entry(this, context, call, engine);
        context.setTransportName("jms");
        if (destination != null)
            context.setProperty(DESTINATION, destination);
        context.setProperty(ASYNCOPERATION, new Boolean(asyncOperation));
        if (wsifOperation != null)
            context.setProperty(WSIFOPERATION, wsifOperation);
        if (syncTimeout != null)
            context.setProperty(SYNC_TIMEOUT, syncTimeout);
        if (asyncTimeout != null)
            context.setProperty(ASYNC_TIMEOUT, asyncTimeout);
        Trc.exit();
    }

    public WSIFJmsTransport copy() throws WSIFException {
        Trc.entry(this);
        WSIFJmsTransport t = new WSIFJmsTransport(destination);
        t.setAsyncOperation(asyncOperation);
        t.setWsifOperation(wsifOperation);
        t.setSyncTimeout(syncTimeout);
        t.setAsyncTimeout(asyncTimeout);
        if (Trc.ON)
            Trc.exit(t.deep());
        return t;
    }

    public void close() throws WSIFException {
        Trc.entry(this);
    	if (destination == null) {
    		throw new WSIFException("already closed");
    	}
   		destination.close();
   		destination = null;
        Trc.exit();
    }
    
    public String deep() {
        String buff = "";
        try {
            buff = new String(super.toString() + ":\n");

            buff += "destination:" + destination;
            buff += "asyncOperation:" + asyncOperation;
            buff += "wsifOperation:" + wsifOperation;
            buff += "syncTimeout:" + syncTimeout;
            buff += "asyncTimeout:" + asyncTimeout;
        } catch (Exception e) {
            Trc.exceptionInTrace(e);
        }
        return buff;
    }
}
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

import java.util.ArrayList;
import java.util.Arrays;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import org.apache.wsif.WSIFException;
import org.apache.wsif.logging.Trc;

/**
 * Server destination for the JMS2HTTPBridge
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class JMS2HTTPBridgeDestination extends WSIFJMSDestination {
    public static final String COLDSTART = "cold";
    public static final String WARMSTART = "warm";

    private boolean verbose;
    private static final ArrayList allStarts =
        new ArrayList(Arrays.asList(new Object[] { COLDSTART, WARMSTART }));

    /**
     * Public constructor.
     * @param finder used to find JMS objects.
     * @param altdestName is an alterative JMS provider destination name
     * @param timeout is the maximum time to wait on a synchronous receive
     * @param startType is WARMSTART or COLDSTART. Cold means wipe the read queue on startup.
     */
    public JMS2HTTPBridgeDestination(
        WSIFJMSFinder finder,
        String altDestName,
        long timeout,
        String startType,
        boolean verbose)
        throws WSIFException {
        super(finder, altDestName, timeout);
        Trc.entry(
            this,
            finder,
            altDestName,
            new Long(timeout),
            startType,
            new Boolean(verbose));

        this.verbose = verbose;
        
        // Swap the queues because we're a server not a client.  	
        readQ = writeQ;
        writeQ = null;

        if (!allStarts.contains(startType))
            throw new WSIFException("StartType must either be warm or cold");

        if (COLDSTART.equals(startType)) {
            if (verbose)
                System.out.println("Wiping messages off the read queue");
            Message msg = null;
            try {
                QueueReceiver rec = session.createReceiver(readQ);
                for (;;) {
                    msg = rec.receive(100);
                    if (msg != null) {
                        if (verbose)
                            System.out.println("Removing an input message");
                    } else
                        break;
                }
            } catch (Exception ignored) {
            	Trc.exception(ignored);
            }
        }
        Trc.exit();
    }

    /**
     * Create a listener thread to listen for messages. This waits forever until it gets 
     * an InterruptedException. This listens on the read queue.
     * @param listener is the JMS message and exception callback interface implementation
     */
    public void listen(WSIFJMSListener listener) throws WSIFException {
        Trc.entry(this, listener);
        listen(listener, readQ);
        Trc.exit();
    }

    /**
     * Create a listener thread to listen for messages. This waits forever until it gets 
     * an InterruptedException. 
     * @param listener is the JMS message and exception callback interface implementation
     * @param queue to listen on
     */
    public void listen(WSIFJMSListener listener, Queue queue)
        throws WSIFException {
        Trc.entry(this, listener, queue);
        areWeClosed();

        try {
            QueueReceiver qr = session.createReceiver(queue);
            qr.setMessageListener(listener);
            connection.setExceptionListener(listener);

            connection.start();

            for (int i = 1; !Thread.interrupted(); i++) {
                Thread.yield();
                Thread.sleep(5000);
                if (verbose)
                    System.out.println("Waiting... " + i);
            }
        } catch (JMSException je) {
        	Trc.exception(je);
            throw WSIFJMSConstants.ToWsifException(je);
        } catch (InterruptedException ignored) {
        	Trc.exception(ignored);
            System.out.println("Exitting");
        }
        Trc.exit();
    }

    // Stop anyone overwriting our readQ
    public void setReplyToQueue() throws WSIFException {
        Trc.entry(this);
        Trc.exit();
    }

    public void setReplyToQueue(String replyTo) throws WSIFException {
        Trc.entry(this, replyTo);
        Trc.exit();
    }

    /**
     * Set the replyTo queue. Special bridge version.
     * @param replyTo queue.
     */
    public void setReplyToQueue(Queue replyTo) throws WSIFException {
        Trc.entry(this, replyTo);
        areWeClosed();

        if (writeQ == null)
            writeQ = replyTo;
        else if (!writeQ.equals(replyTo)) {

            if (sender != null) {
                try {
                    sender.close();
                } catch (Exception e) {
                	Trc.exception(e);
                }
                sender = null;
            }
            writeQ = replyTo;
        }
        Trc.exit();
    }
}
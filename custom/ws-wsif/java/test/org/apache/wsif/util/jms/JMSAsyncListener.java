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

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueReceiver;

import org.apache.wsif.WSIFCorrelationId;
import org.apache.wsif.WSIFCorrelationService;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.util.WSIFCorrelationServiceLocator;
import util.TestUtilities;
/**
 * A simple JMS listener which can be used to test async operations 
 * @author ant elder <antelder@apache.org>
 */
public class JMSAsyncListener extends JMS2HTTPBridgeDestination {

    static final String startType = JMS2HTTPBridgeDestination.COLDSTART;
    static final boolean VERBOSE = TestUtilities.isJmsVerbose();

    private Thread listenerThread;
    
    private WSIFJMSListener list = new WSIFJMSListener() {
        public void onException(JMSException arg1) {
            Trc.entry(this, arg1);
            arg1.printStackTrace();
            Trc.exit();
        }

        public void onMessage(Message message) {
            Trc.entry(this, message);
            processResponse(message);
            Trc.exit();
        }
    };

    public JMSAsyncListener(String msgQ) throws WSIFException {
        super(
            new WSIFJMSFinderForJndi(
                null,
                TestUtilities.getWsifProperty(
                    "wsif.jms2httpbridge.initialcontextfactory"),
                TestUtilities.getWsifProperty(
                    "wsif.jms2httpbridge.jndiproviderurl"),
                WSIFJMSFinder.STYLE_QUEUE,
                TestUtilities.getWsifProperty(
                    "wsif.jms2httpbridge.jndiconnectionfactoryname"),
                msgQ,
                null),
            null,
            WSIFJMSConstants.WAIT_FOREVER,
            startType,
            VERBOSE);
                
        listenerThread = new Thread() {
        	public void run() {
        		try {
   		           listen( list );
        		} catch (WSIFException ex) {
        		   ex.printStackTrace();
        		}
        	}
        };
        listenerThread.start();
    }
    /**
     * Create a listener thread to listen for messages. This waits forever 
     * until it gets an InterruptedException. 
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
                if (VERBOSE)
                    System.out.println("JMSAsyncListener waiting... " + i);
            }
        } catch (JMSException je) {
            throw WSIFJMSConstants.ToWsifException(je);
        } catch (InterruptedException ignored) {
            if ( VERBOSE ) System.out.println("JMSAsyncListener Exitting");
        }
        Trc.exit();
    }
    
    private void processResponse(Message msg) {
        Serializable so;
        try {
            if (VERBOSE)
                System.out.println(
                    "WSIFJmsAsyncListener.processResponse called");

            WSIFCorrelationService cs =
                WSIFCorrelationServiceLocator.getCorrelationService();
            WSIFCorrelationId cid =
                new WSIFJMSCorrelationId(msg.getJMSCorrelationID());
            synchronized (cs) {
                so = cs.get(cid);
            }
            if (so != null && so instanceof WSIFOperation) {
                cs.remove(cid);
                ((WSIFOperation) so).fireAsyncResponse(msg);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void stop() {
        listenerThread.interrupt();
    }

}
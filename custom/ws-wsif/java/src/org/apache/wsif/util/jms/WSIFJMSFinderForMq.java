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

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import org.apache.wsif.WSIFException;

/**
 * Finds JMS objects by going directly to MQSeries.
 * @author Mark Whitlock <whitlock@apache.org>
 */
class WSIFJMSFinderForMq extends WSIFJMSFinder {
    
    private QueueConnectionFactory factory;
    private Destination initialDestination;
    private String style;

    WSIFJMSFinderForMq(String jmsVendorURL, String implSpecURL)
        throws WSIFException {
        throw new WSIFException("not yet implemented");

        /*    try {
              factory = new MQQueueConnectionFactory();
              factory.setQueueManager("");
        
              connection = factory.createQueueConnection();
              session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        
              readQ  = session.createQueue(readQName );
              writeQ = session.createQueue(writeQName);
              connection.start();
        
              if (startType==COLDSTART) {
                System.out.println("Wiping messages off the queues");
                Message msg=null;
                try {
        	      if (receiver==null) receiver=session.createReceiver(readQ);
                  for (;;) {
                    msg=receiver.receive(100);
                    if (msg!=null) System.out.println("Removing an input message");
                    else break;
                  }
                } catch (Exception e) {}
        
                try {
        	      QueueReceiver tmpRec = session.createReceiver(writeQ);
                  for (;;) {
                    msg=tmpRec.receive(100);
                    if (msg!=null) System.out.println("Removing an output message");
                    else break;
                  }
                } catch (Exception e) {}
              }
              
            } catch( JMSException je ) {
              System.out.println("caught JMSException: " + je);
              Exception le = je.getLinkedException();
              if (le != null) System.out.println("linked exception: "+le);
        	
            } catch( Exception e ) {
              System.out.println("Caught exception: " + e );
            } */

    }

    public QueueConnectionFactory getFactory() {
        return factory;
    }
    
    public Destination getInitialDestination() {
        return initialDestination;
    }
    
    String getStyle() {
        return style;
    }

    Queue findQueue(String name) throws WSIFException {
        return null;
    }
}
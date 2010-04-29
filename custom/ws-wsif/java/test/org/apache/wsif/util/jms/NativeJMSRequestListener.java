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

import inout.wsiftypes.InoutImpl;
import inout.wsiftypes.Mutablestring;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.TextMessage;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFException;
import org.apache.wsif.logging.Trc;
import stockquote.wsiftypes.StockQuote;
import util.TestUtilities;

import addressbook.wsiftypes.Address;
import addressbook.wsiftypes.AddressBook;

/**
 * Used to simulate the remote service for native JMS requests.
 */
public class NativeJMSRequestListener extends JMS2HTTPBridgeDestination {
    int counter = 0;
    static final String startType = JMS2HTTPBridgeDestination.COLDSTART;
    static final boolean VERBOSE = TestUtilities.isJmsVerbose();

    private Thread listenerThread;

    static AddressBook ab = new AddressBook();
    static StockQuote sq = new StockQuote();
    static InoutImpl inout = new InoutImpl();

    static final Object LOCK = new Lock();
    static class Lock {}
    
    private WSIFJMSListener list = new WSIFJMSListener() {
        public void onException(JMSException arg1) {
            Trc.entry(this, arg1);
            arg1.printStackTrace();
            Trc.exit();
        }

        public void onMessage(Message message) {
        	try {
            Trc.entry(this, message);
  	        synchronized ( getLock() ) {
               processResponse(message);
  	        }
            Trc.exit();
        	} catch (Exception ex) {
        		ex.printStackTrace();
        	}
        }
    };


    public NativeJMSRequestListener(String msgQ) throws WSIFException {
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
                if (VERBOSE) System.out.println("JMSAsyncListener waiting... " + i);
            }
        } catch (JMSException je) {
        	je.printStackTrace();
            throw new WSIFException( je.getMessage() );
        } catch (InterruptedException ignored) {
            if (VERBOSE) System.out.println("JMSAsyncListener Exitting");
        }
        Trc.exit();
    }
    
    public void stop() {
        listenerThread.interrupt();
    }

    private void processResponse(Message msg) {
        if (VERBOSE) {
            try {
                System.out.println("NativeJMSRequestListener got msg:" + msg);
                if (msg instanceof TextMessage) {
                    TextMessage tm = (TextMessage) msg;
                    String tmt = tm.getText();
                    System.out.println(
                        "TextMessage " + (tmt == null ? "null" : tmt));
                } else if (msg instanceof ObjectMessage) {
                    ObjectMessage om = (ObjectMessage) msg;
                    Object omo = om.getObject();
                    System.out.println(
                        "ObjectMessage "
                            + (omo == null ? "null" : omo.toString()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            String operationName = null;
            String input = null;
            String output = null;
            String fake = null;
            try {
                operationName =
                    msg.getStringProperty(
                        WSIFConstants.JMS_PROP_OPERATION_NAME);
                input =
                    msg.getStringProperty(WSIFConstants.JMS_PROP_INPUT_NAME);
                output =
                    msg.getStringProperty(WSIFConstants.JMS_PROP_OUTPUT_NAME);
                fake = msg.getStringProperty("WSIF_FAKE");
            } catch (javax.jms.JMSException e) {
            }

            Object reply = null;
            Object dummyReply = "input only, so no reply";
            if ( fake != null ) {
                sendReply( msg, doFakeOp( fake ) );
            } else if ( "getQuote".equals( operationName ) ) {
            	reply = new Float( sqGetQuote( (ObjectMessage)msg ) );
                sendReply( msg, reply );
            } else if ( "AddEntryFirstAndLastNamesRequest".equals( input ) ) {
            	abAddEntryFL( (ObjectMessage) msg );
                sendReply( msg, dummyReply ); //TODO jms test needs this???
            } else if ( "addEntry".equals( operationName ) ) {
            	abAddEntry( (ObjectMessage) msg );
                sendReply( msg, dummyReply ); //TODO jms test needs this???
            } else if ( "addEntryWholeName".equals( operationName ) ) {
            	abAddEntry( (ObjectMessage) msg );
                sendReply( msg, dummyReply ); //TODO jms test needs this???
            } else if ( "addEntryUserProp".equals( operationName ) ) {
            	abAddEntry( (ObjectMessage) msg );
                sendReply( msg, dummyReply ); //TODO jms test needs this???
            } else if ( "addEntryJmsProp".equals( operationName ) ) {
            	abAddEntry( (ObjectMessage) msg );
                sendReply( msg, dummyReply ); //TODO jms test needs this???
            } else if ( "addEntryFirstAndLastNames".equals( operationName ) ) {
            	abAddEntryFL( (ObjectMessage) msg );
                sendReply( msg, dummyReply ); //TODO jms test needs this???
            } else if ( "GetAddressFromNameMSRequest".equals( input ) ) {
            	reply = inoutGetAddressFromName( (ObjectMessage) msg );
                sendReply( msg, reply );
            } else if ( "getAddressFromName".equals( operationName ) ) {
            	reply = abGetAddressFromName( (ObjectMessage) msg );
                sendReply( msg, reply );
            } else if ( "getAddressFromName".equals( operationName ) ) {
            	reply = abGetAddressFromName( (ObjectMessage) msg );
                sendReply( msg, reply );
            } else if ( "getAddressFromNameMS".equals( operationName ) ) {
            	reply = inoutGetAddressFromName( (ObjectMessage) msg );
                sendReply( msg, reply );
            } else if ( "addNumbers".equals( operationName ) ) {
            	reply = inoutAddNumbers( (ObjectMessage) msg );
                sendReply( msg, reply );
            } else if ( "getDate".equals( operationName ) ) {
            	reply = inoutGetDate( (ObjectMessage) msg );
                sendReply( msg, reply );
            } else if ( "whoamiString".equals( operationName ) ) {
            	reply = inoutWhoami( (ObjectMessage) msg );
                sendReply( msg, reply );
            } else if ( "whoamiFloat".equals( operationName ) ) {
            	reply = inoutWhoami( (ObjectMessage) msg );
                sendReply( msg, reply );
            } else if ( "whoamiInt".equals( operationName ) ) {
            	reply = inoutWhoami( (ObjectMessage) msg );
                sendReply( msg, reply );
            } else if ( "whoamiAddress".equals( operationName ) ) {
            	reply = inoutWhoami( (ObjectMessage) msg );
                sendReply( msg, reply );
            } else if ( "inoutArgs".equals( operationName ) ) {
            	reply = inoutArgs( (ObjectMessage) msg );
                sendReply( msg, reply );
            } else if ( "whoami".equals( operationName ) ) {
            	reply = inoutWhoami( (ObjectMessage) msg );
                sendReply( msg, reply );
            } else if (operationName.startsWith("throw")) {
                throwFault(operationName, (ObjectMessage) msg);
            } else {
                System.err.println("unknown operation: " + operationName);
            }
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }

    private void abAddEntry(ObjectMessage msg) throws JMSException { 
       HashMap hm = (HashMap) msg.getObject();
       String name = (String)hm.get( "name" );  	
       Address addr = (Address)hm.get( "address" );  	
       ab.addEntry( name, addr );
       inout.addEntry( name, addr );
    }

    private void abAddEntryFL(ObjectMessage msg) throws JMSException { 
       HashMap hm = (HashMap) msg.getObject();
       String first = (String)hm.get( "firstName" );  	
       String last = (String)hm.get( "lastName" );  	
       Address addr = (Address)hm.get( "address" );  	
       ab.addEntry( first, last, addr );
       inout.addEntry( first, last, addr );
    }

    private Address abGetAddressFromName(ObjectMessage msg) throws JMSException { 
       String name = (String)msg.getObject();  	
       return ab.getAddressFromName( name );
    }
    
    private float sqGetQuote(ObjectMessage msg) throws Exception { 
       String name = (String) msg.getObject();  	
       return sq.getQuote( name );
    }
    
    private Address inoutGetAddressFromName(ObjectMessage msg) throws JMSException { 
       Mutablestring name = (Mutablestring)msg.getObject();  	
       return inout.getAddressFromName( name );
    }
    
    private Date inoutGetDate(ObjectMessage msg) throws JMSException { 
       return inout.getDate();
    }
    
    private String inoutWhoami(ObjectMessage msg) throws JMSException { 
       Object o = null;
       o = msg.getObject();
       if ( o instanceof String ) {
          return inout.whoami( (String) o );
       }
       o = msg.getObject();
       if ( o instanceof Address ) {
          return inout.whoami( (Address) o );
       }
       o = msg.getObject();
       if ( o instanceof Integer ) {
          return inout.whoami( ((Integer)o).intValue() );
       }
       o = msg.getObject();
       if ( o instanceof Float ) {
          return inout.whoami( ((Float)o).floatValue() );
       } 
       return "errror - unknown obj";
    }
    
    private Integer inoutAddNumbers(ObjectMessage msg) throws JMSException { 
       int[] nums = (int[])msg.getObject();
       return new Integer( inout.addNumbers( nums ) );
    }
    
    private HashMap inoutArgs(ObjectMessage msg) throws JMSException { 
       HashMap hm = (HashMap) msg.getObject();
       Mutablestring ms1 = (Mutablestring)hm.get( "ms1" );  	
       Mutablestring ms2 = (Mutablestring)hm.get( "ms2" );  	
//       String s = inout.inoutArgs( ms1, ms2 ); TODO ???what happened?
       HashMap hmr = new HashMap();
//       hmr.put( "reply", s );
       hmr.put( "ms2", ms2 );
       return hmr;
    }
    
    private void throwFault(String operationName, ObjectMessage msg)
        throws Exception {

        Queue replyTo = (Queue) (msg.getJMSReplyTo());
        if (replyTo == null)
            return;
            
        ObjectMessage faultMsg = (ObjectMessage) session.createObjectMessage();
        int choice = ((Integer) msg.getObject()).intValue();
        System.out.println("NativeJMSRequestListener throwSimple choice="+choice);
        switch (choice) {
        	case 0 :
        	    break;
            case 1 :
                faultMsg.setStringProperty("faultIndicator", "simple");
                faultMsg.setObject((Serializable) "A Simple Fault");
                break;
            case 2 :
            case 3 :
                faultMsg.setStringProperty(
                    "faultIndicator",
                    choice == 2 ? "ints" : "twoints");
                HashMap hm = new HashMap();
                hm.put("faultInt1", new Integer(1));
                hm.put("faultInt2", new Integer(2));
                hm.put("faultInt3", new Integer(3));
                faultMsg.setObject((Serializable) hm);
                break;
            case 4 :
                faultMsg.setIntProperty("faultIndicator", 43);
                faultMsg.setObject((Serializable) "A Simple Fault");
                break;
            case 5 :
                faultMsg.setStringProperty("faultIndicator", "not a fault");
                faultMsg.setObject((Serializable) "Not a Fault");
                break;
            case 6 :
                faultMsg.setByteProperty("faultIndicator", (byte)-1);
                faultMsg.setObject((Serializable) "A Fault Indicator");
                break;
            case 7 :
                faultMsg.setByteProperty("faultIndicator", (byte)-2);
                faultMsg.setObject((Serializable) "Another Property Fault");
                faultMsg.setStringProperty("anotherProperty", "Another JMS Property");
                break;
            case 8 :
                faultMsg.setByteProperty("faultIndicator", (byte)-3);
                break;
            case 9 :
                faultMsg.setByteProperty("faultIndicator", (byte)-4);
                faultMsg.setStringProperty("anotherProperty", "Another JMS Property");
                break;
            case 10 :
                faultMsg.setByteProperty("faultIndicator", (byte)-5);
                break;
            default :
                throw new RuntimeException("throwSimple: Bad choice");
        }

        setReplyToQueue(replyTo);
        String o = msg.getJMSMessageID();
        try {
            send(faultMsg, o, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private String doFakeOp(String fake) {
        fake = fake.substring( 1 ); // remove leading quote
		fake = fake.substring( 0, fake.length() - 1 ); // remove trailing quote
        return getFakeReply( fake );
    }
    
    private String getFakeReply(String name) {
        String s;
        StringBuffer sb = new StringBuffer();
        try {
           BufferedReader in = new BufferedReader(
              new FileReader( name ) );
           while (( s = in.readLine() ) != null) {
              sb.append( s );
           }
           in.close();
        } catch (IOException ex) {
        	ex.printStackTrace();
    	}
        return sb.toString();
    }
    
    private void sendReply(Message msg, Object response) throws Exception {
        Queue replyTo = (Queue) (msg.getJMSReplyTo());
        if (replyTo == null)
            return;

        Message replyMsg = session.createObjectMessage();
        ((ObjectMessage) replyMsg).setObject((Serializable) response);

        setEchoProperties( msg, replyMsg );

        setReplyToQueue(replyTo);
        String o = msg.getJMSMessageID();
        try {
            send(replyMsg, o, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void sendReply(Message msg, String response) throws Exception {
        Queue replyTo = (Queue) (msg.getJMSReplyTo());
        if (replyTo == null)
            return;

        Message replyMsg = session.createTextMessage();
        ((TextMessage) replyMsg).setText( response );

        setEchoProperties( msg, replyMsg );
        
        setReplyToQueue(replyTo);
        String o = msg.getJMSMessageID();
        try {
           send(replyMsg, o, false);
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }

    private void setEchoProperties(Message inMsg, Message outMsg) {
      try {
    	for (Enumeration en = inMsg.getPropertyNames(); en.hasMoreElements(); ) {
    		String propName = (String)en.nextElement();
    		if ( propName.startsWith( "Echo" ) ) {
    			String key = propName;
     		    Object propValue = inMsg.getObjectProperty( propName );
    			outMsg.setObjectProperty( key, propValue );
    		}
    	}
      } catch (JMSException ex) {
      	ex.printStackTrace();
      }
    }
    
    /**
     * this is needed as the native JMS inputonly method really is inputonly
     * so doesn't wait for a response. Therefore the next request, such as an
     * addressbook lookup may run before the previous add has completed.
     */
    public final Object getLock() {
        return LOCK;
    }

}
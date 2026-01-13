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
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.namespace.QName;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFException;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.wsdl.extensions.jms.JMSAddress;
import org.apache.wsif.wsdl.extensions.jms.JMSConstants;
import org.apache.wsif.wsdl.extensions.jms.JMSPropertyValue;

/**
 * A WSIFJMSDestination is a pair of queues, one that read from and
 * the other that is written to. This class provides various methods
 * for different flavours of reading and writing messages to those
 * queues. This class hides the JMS interface.
 *
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class WSIFJMSDestination {
    protected WSIFJMSFinder finder;
    protected QueueConnection connection = null;
    protected QueueSession session = null;
    protected Queue readQ = null;
    protected Queue writeQ = null;
    protected QueueSender sender = null;

    protected boolean asyncMode = false;
    protected Queue syncTempQueue = null;

    protected WSIFJMSProperties inProps;
    protected WSIFJMSProperties outProps;
    protected Message lastMessage = null;
    protected long timeout;
    protected String replyToName = null;

    /**
     * Public constructor.
     * @param finder used to find JMS objects.
     */
    public WSIFJMSDestination(WSIFJMSFinder finder) throws WSIFException {
        this(finder, WSIFJMSConstants.WAIT_FOREVER);
        Trc.entry(this, finder);
        Trc.exit();
    }

    /**
     * Public constructor.
     * @param finder used to find JMS objects.
     * @param timeout is the maximum time to wait on a synchronous receive
     */
    public WSIFJMSDestination(WSIFJMSFinder finder, long timeout)
        throws WSIFException {
        this(finder, null, timeout);
        Trc.entry(this, finder, Long.valueOf(timeout));
        Trc.exit();
    }

    /**
     * Public constructor.
     * @param finder used to find JMS objects.
     * @param altdestName is an alterative JMS provider destination name
     * @param timeout is the maximum time to wait on a synchronous receive
     */
    public WSIFJMSDestination(
        WSIFJMSFinder finder,
        String altDestName,
        long timeout)
        throws WSIFException {
        Trc.entry(this, finder, altDestName, Long.valueOf(timeout));

        inProps = new WSIFJMSProperties(WSIFJMSProperties.IN);
        outProps = new WSIFJMSProperties(WSIFJMSProperties.OUT);
        this.timeout = timeout;
        this.finder = finder;

        try {
            connection = finder.getFactory().createQueueConnection();
            session =
                connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination initDest = finder.getInitialDestination();
            if (initDest != null && altDestName != null)
                throw new WSIFException("Both jndiDestinationName and jmsproviderDestinationName cannot be specified");
            if (initDest == null && altDestName == null)
                throw new WSIFException("Either jndiDestinationName or jmsproviderDestinationName must be specified");

            if (altDestName != null)
                initDest = session.createQueue(altDestName);

            writeQ = (Queue) initDest;
            readQ = null;

            connection.start();

        } catch (JMSException je) {
            Trc.exception(je);
            throw WSIFJMSConstants.ToWsifException(je);
        }
        if (Trc.ON)
            Trc.exit(deep());
    }

    /**
     * Close all objects.
     */
    public void close() throws WSIFException {
        Trc.entry(this);
        try {
            QueueSender sndr = sender;
            QueueSession sssn = session;
            QueueConnection cnnctn = connection;

            sender = null; // Ensure these are nulled (flagging the close()),
            session = null; // even if a close() throws a JMSException
            connection = null;

            if (sndr != null)
                sndr.close();
            if (sssn != null)
                sssn.close();
            if (cnnctn != null)
                cnnctn.close();
        } catch (JMSException je) {
            Trc.exception(je);
            throw WSIFJMSConstants.ToWsifException(je);
        }
        Trc.exit();
    }

    /**
     * close the destination at finalize.
     */
    public void finalize() throws WSIFException {
        Trc.entry(this);
        close();
        Trc.exit();
    }

    /**
     * Send a message to the write queue
     * @param data is the message
     * @return the id of the message that was sent.
     */
    public String send(String data) throws WSIFException {
        Trc.entry(this, data);
        String s = send(data, null);
        Trc.exit(s);
        return s;
    }

    /**
     * Send a message to the write queue
     * @param data is the message
     * @param id is the correlation id to set on the message
     * @return the id of the message that was sent.
     */
    public String send(String data, String id) throws WSIFException {
        Trc.entry(this, data, id);
        areWeClosed();
        try {
            TextMessage msg = session.createTextMessage();
            msg.setText(data);
            String s = send(msg, id, true);
            Trc.exit(s);
            return s;
        } catch (JMSException je) {
            Trc.exception(je);
            throw WSIFJMSConstants.ToWsifException(je);
        }
    }

    /**
     * Send a message to the write queue
     * @param data is the message
     * @return the id of the message that was sent.
     */
    public String send(Serializable data) throws WSIFException {
        Trc.entry(this, data);
        String s = send(data, null);
        Trc.exit(s);
        return s;
    }

    /**
     * Send a message to the write queue
     * @param data is the message
     * @param id is the correlation id to set on the message
     * @return the id of the message that was sent.
     */
    public String send(Serializable data, String id) throws WSIFException {
        Trc.entry(this, data, id);
        areWeClosed();

        try {
            ObjectMessage msg = session.createObjectMessage();
            msg.setObject(data);
            String s = send(msg, id, true);
            Trc.exit(s);
            return s;
        } catch (JMSException je) {
            Trc.exception(je);
            throw WSIFJMSConstants.ToWsifException(je);
        }
    }

    /**
     * Sends a message to the write queue.
     * @param message
     * @param id Correlation id
     * @param setReplyTo If true JMSReplyTo is always set. If false JMSReplyTo 
     *                    is only set if the ReplyTo was explicitly set as a 
     *                    property.
     */
    public String send(Message msg, String id, boolean setReplyTo)
        throws WSIFException {

        Trc.entry(this, msg, id);
        areWeClosed();

        String msgId = null;
        boolean propsSet = true;

        try {
            if (sender == null) {
                sender = session.createSender(writeQ);
				sender.setTimeToLive(timeout);
            }

            // Process replyTo queues separately since they are not 
            // ordinary JMS properties.
            if (inProps.containsKey(WSIFJMSConstants.REPLY_TO)) {
                String rto = (String) inProps.get(WSIFJMSConstants.REPLY_TO);
                setReplyToQueue(rto);
                inProps.remove(WSIFJMSConstants.REPLY_TO);
                msg.setJMSReplyTo(readQ);
            } else if (setReplyTo) {
                setReplyToQueue();
                msg.setJMSReplyTo(readQ);
            }

            if (id != null)
                msg.setJMSCorrelationID(id);
            inProps.set(sender, msg);

            sender.send(msg);
            msgId = msg.getJMSMessageID();

        } catch (JMSException je) {
            Trc.exception(je);
            throw WSIFJMSConstants.ToWsifException(je);
        } finally {
            // If properties were set, trash the sender so 
            // we get the default props next time.
            if (propsSet) {
            	try {
		    if (sender!=null)
			sender.close();
                } catch (JMSException e) {
                	throw new WSIFException(
                	    "JMSException closing sender: " 
                	    + e.getLinkedException(),
                	    e);
                }
                sender = null;
            }
            inProps.clear();
        }

        Trc.exit(msgId);
        return msgId;
    }

    /**
     * Blocking receive for the wsif.syncrequest.timeout
     * @return the received message
     */
    public String receive() throws WSIFException {
        Trc.entry(this);
        String s = receiveString(null);
        Trc.exit(s);
        return s;
    }

    /**
     * Blocking receive for the wsif.syncrequest.timeout
     * @return the received message
     */
    public String receiveString(String id) throws WSIFException {
        Trc.entry(this, id);
        String s = receiveString( id, timeout );
        Trc.exit(s);
        return s;
    }

    /**
     * Blocking receive waits for the specified timeout
     * @return the received message
     */
    public String receiveString(String id, long timeout) throws WSIFException {
        Trc.entry(this, id);
        Message msg = receive(id, timeout);
        String s = null;
        try {
            if (msg instanceof TextMessage message)
                s = message.getText();
            else
                throw new WSIFException(
                    "Reply message was not a TextMessage:msg="
                        + (msg == null ? "null" : msg.toString()));
        } catch (JMSException e) {
            Trc.exception(e);
            throw WSIFJMSConstants.ToWsifException(e);
        }
        Trc.exit(s);
        return s;
    }

    /**
     * Blocking receive waits for a message for the wsif.syncrequest.timeout
     * @param id is the correlation id that the received message must have
     * @return the received message
     */
    public Message receive(String id) throws WSIFException {
        Trc.entry(this, id);
        Message msg = receive(id, timeout);
        Trc.exit(msg);
        return msg;
    }

    /**
     * Blocking receive waits for a message for the specified timeout
     * @param id is the correlation id that the received message must have
     * @param timeout how long in milliseconds to wait
     * @return the received message
     */
    public Message receive(String id, long timeout) throws WSIFException {
        Trc.entry(this, id);
        areWeClosed();
        QueueReceiver rec = null;
        Message msg = null;

        try {
            if (id != null)
                rec =
                    session.createReceiver(
                        readQ,
                        WSIFJMSConstants.JMS_CORRELATION_ID + "='" + id + "'");
            else
                rec = session.createReceiver(readQ);

            msg = rec.receive(timeout);
            setLastMessage(msg);

            if (msg == null)
                throw new WSIFException(
                    "Receive timed out on JMS queue "
                        + readQ.getQueueName()
                        + ", timeout "
                        + timeout);
        } catch (JMSException e) {
            Trc.exception(e);
            throw WSIFJMSConstants.ToWsifException(e);
        } finally {
            try {
                if (rec != null)
                    rec.close();
            } catch (Exception ignored) {
                Trc.ignoredException(ignored);
            }
        }

        Trc.exit(msg);
        return msg;
    }

    /**
     * Set the replyTo queue to a temporary queue. 
     */
    public void setReplyToQueue() throws WSIFException {
        Trc.entry(this);
        areWeClosed();

        Queue tmp;
        try {
            if (syncTempQueue == null)
                syncTempQueue = session.createTemporaryQueue();
        } catch (JMSException je) {
            Trc.exception(je);
            throw WSIFJMSConstants.ToWsifException(je);
        }

        // So we don't overwrite readQ if there was an error.
        readQ = syncTempQueue;
        replyToName = null;
        Trc.exit();
    }

    /**
     * Set the replyTo queue.
     * @param replyTo queue name.
     */
    public void setReplyToQueue(String replyTo) throws WSIFException {
        Trc.entry(this, replyTo);
        areWeClosed();

        if (replyTo == null || replyTo.length() == 0) {
            setReplyToQueue();
            Trc.exit();
            return;
        }

        // If we're already using this queue, then reuse it.
        if (replyTo.equals(replyToName)) {
            Trc.exit();
            return;
        }

        readQ = finder.findQueue(replyTo);

        replyToName = replyTo;
        Trc.exit();
    }

    /**
     * Sets if this destination is to be used for asynchronous requests.
     * If this destination is to be used for asynchronous requests then a
     * WSIFJMSAsyncListener will be created to listen for the async responses.  
     * 
     * @param b   true if this destination is to be used for asynchronous requests,
     *            otherwise false.
     */
    public void setAsyncMode(boolean b) throws WSIFException {
        Trc.entry(this, b);
        areWeClosed();
        if (asyncMode != b) {
            asyncMode = b;
        }
        Trc.exit();
    }

    /**
     * Sets a JMS property to a value. This property value will be only be used for
     * the next message that is sent, then the property will be reset.
     */
    public void setProperty(String name, Object value) throws WSIFException {
        Trc.entry(this, name, value);
        if (name != null && value != null)
            inProps.put(name, value);
        Trc.exit();
    }

    /**
     * Sets a HashMap of JMS property value pairs. The property values will be only 
     * be used for the next message that is sent, then all the properties will be reset.
     */
    public void setProperties(HashMap propMap) {
        Trc.entry(this, propMap);
        if (propMap != null && !propMap.isEmpty())
            inProps.putAll(propMap);
        Trc.exit();
    }

    /**
     * Gets a JMS property from the previous message that was received.
     */
    public Object getProperty(String name) throws WSIFException {
        Trc.entry(this, name);
        if (lastMessage == null) {
            Trc.exit(null);
            return null;
        }

        if (outProps.isEmpty())
            outProps.getPropertiesFromMessage(lastMessage);

        Object prop = null;
        if (name != null)
            prop = outProps.get(name);

        Trc.exit(prop);
        return prop;
    }

    /**
     * Gets all the JMS properties from the previous message that was received.
     */
    public HashMap getProperties() throws WSIFException {
        Trc.entry(this);
        if (lastMessage == null) {
            Trc.exit(null);
            return null;
        }

        if (outProps.isEmpty())
            outProps.getPropertiesFromMessage(lastMessage);
        if (!outProps.isEmpty()) {
            Trc.exit(outProps);
            return outProps;
        }
        Trc.exit(null);
        return null;
    }

    protected void areWeClosed() throws WSIFException {
        if (session == null)
            throw new WSIFException("Cannot use a closed destination");
    }

    public static Message createMessage(Session session, int msgType)
        throws WSIFException {
        Trc.entry(null, session, Integer.valueOf(msgType));
        Message jmsMsg = null;

        try {
            if (msgType
                == org
                    .apache
                    .wsif
                    .wsdl
                    .extensions
                    .jms
                    .JMSConstants
                    .MESSAGE_TYPE_OBJECTMESSAGE)
                jmsMsg = session.createObjectMessage();
            else if (
                msgType
                    == org
                        .apache
                        .wsif
                        .wsdl
                        .extensions
                        .jms
                        .JMSConstants
                        .MESSAGE_TYPE_TEXTMESSAGE)
                jmsMsg = session.createTextMessage();
            else
                throw new WSIFException("Unable to support message type");
        } catch (JMSException je) {
            Trc.exception(je);
            throw WSIFJMSConstants.ToWsifException(je);
        }
        Trc.exit(jmsMsg);
        return jmsMsg;
    }

    public Message createMessage(int msgType) throws WSIFException {
        Trc.entry(this, msgType);
        Message m = createMessage(session, msgType);
        Trc.exit(m);
        return m;
    }

    /**
     * The last message is the most recent message that was received by this 
     * WSIFJMSDestination. The getProperty(s) methods return the properties 
     * that are on the lastMessage. The works fine for sync, but for async
     * user code will have received the message. So the provider must inform
     * the WSIFJMSDestination about the lastMessage explicitly so it can 
     * inquire correctly about any jms properties on it.
     */
    public void setLastMessage(Message msg) {
        Trc.entry(this, msg);
        lastMessage = msg;
        Trc.exit();
    }
    
    public String deep() {
        String buff = "";
        try {
            buff = new String(this.toString() + "\n");
            buff += "finder: " + finder;
            buff += " connection: " + connection;
            buff += " session: " + session;
            buff += " readQ: " + readQ;
            buff += " writeQ: " + writeQ;
            buff += " sender: " + sender;
            buff += " asyncMode: " + asyncMode;
            buff += " syncTempQueue: " + syncTempQueue;
            buff += " inProps: " + inProps;
            buff += " outProps: " + outProps;
            buff += " lastMessage: " + lastMessage;
            buff += " timeout: " + timeout;
            buff += " replyToName: " + replyToName;
        } catch (Exception e) {
            Trc.exceptionInTrace(e);
        }
        return buff;
    }
    
    /**
     * Create a JMSAddress extensability element from a URL string
     * The URL string would normally be from the WSDL soap:address
     * location attribute.
     * 
     * The format of the URL is:
     * jms:/[queue|topic]?<property>=<value>|<property>=<value>|...
     * 
     * Valid properties are:
     * 
     *    destination
     * 		The JNDI name of the destination queue or topic.
     *    connectionFactory 	
     *      The JNDI name of the connection factory.
     *    targetService
     * 		The name of the deployed service to which the request will be dispatched.
     * 
     * JNDI-related Properties (optional)
     * 
     *    initialContextFactory
     *  	The name of the initial context factory to use 
     *      (this is mapped to the java.naming.factory.initial property)
     *    jndiProviderURL
     *  	The JNDI provider URL 
     *      (this is mapped to the java.naming.provider.url property)
     * 
     * JMS-related Properties (optional)
     * 
     *    deliveryMode
     * 		An indication as to whether the request message should be persistent or not. 
     *      The valid values are DeliveryMode.NON_PERSISTENT (default) and 
     *      DeliveryMode.PERSISTENT.
     *    timeToLive
     * 		The lifetime (in milliseconds) of the request message. 
     *      A value of 0 indicates an infinite lifetime.
     *    priority
     * 		The JMS priority associated with the request message 
     *      Valid values are 0 to 9. The default value is 4
     *    userid
     * 		The userid to be used to gain access to the connection factory.
     *    password
     * 		The password to be used to gain access to the connection factory.
     * 
     * @param url   the URL string
     * @return JMSAddress   the new JMSAddress created from the URL 
     */
    public static JMSAddress getJMSAddressFromURL(String url)
        throws WSIFException {

        JMSAddress jmsAddress = new JMSAddress();

        // validate protocol
        if (!url.startsWith(JMSConstants.JMS_URL_PROTOCOL)) {
            throw new WSIFException(
                "protocol must be '"
                    + JMSConstants.JMS_URL_PROTOCOL
                    + "', url: "
                    + url);
        }

        // validate its a query
        int queryStart = url.indexOf(JMSConstants.JMS_URL_QUERY_CHAR);
        if (queryStart < 1) {
            throw new WSIFException("URL must be a query, url: " + url);
        }

        // validate destination type
        String destinationStyle =
            url.substring(
                JMSConstants.JMS_URL_PROTOCOL.length(),
                queryStart);
        if (!JMSConstants.JMS_URL_QUEUE.equals(destinationStyle)
            && !JMSConstants.JMS_URL_TOPIC.equals(destinationStyle)) {
            throw new WSIFException(
                "style must be "
                + JMSConstants.JMS_URL_QUEUE
                + " or "
                + JMSConstants.JMS_URL_TOPIC
                + ". found: " 
                + destinationStyle);
        }
        jmsAddress.setDestStyle(destinationStyle.substring(1));

        // validate query string
        if (queryStart >= url.length()) {
            throw new WSIFException("empty query string: " + url);
        }
        String queryString = url.substring(queryStart + 1);

        //TODO: this seems a bit nasty
        String seperator = JMSConstants.JMS_URL_QUERY_SEPERATOR1;
        if (queryString.indexOf(seperator.charAt(0)) < 0) {
            seperator = JMSConstants.JMS_URL_QUERY_SEPERATOR2;
        }
        	
        // parse and validate the query
        StringTokenizer st = new StringTokenizer(queryString, seperator);
        while (st.hasMoreTokens()) {
            String property = st.nextToken();
            int delim = property.indexOf('=');
            if (delim < 1 || delim >= property.length()) {
                throw new WSIFException(
                    "property must be '=' seperated name value pair: "
                        + property);
            }

            String propertyName = property.substring(0, delim);
            String propertyValue = property.substring(delim + 1);

            if (propertyName.equals(JMSConstants.JMS_URL_DESTINATION)) {
                jmsAddress.setJndiDestName(propertyValue);

            } else if (propertyName.equals(JMSConstants.JMS_URL_CONNECTION_FACTORY)) {
                jmsAddress.setJndiConnFactName(propertyValue);

            } else if (propertyName.equals(JMSConstants.JMS_URL_INITIAL_CONTEXT_FACTORY)) {
                jmsAddress.setInitCxtFact(propertyValue);

            } else if (propertyName.equals(JMSConstants.JMS_URL_PROVIDER_URL)) {
                jmsAddress.setJndiProvURL(propertyValue);

            } else if (propertyName.equals(JMSConstants.JMS_URL_DELIVERY_MODE)) {
                JMSPropertyValue jpv = new JMSPropertyValue();
                jpv.setName(WSIFJMSProperties.DELIVERYMODE);
                jpv.setValue(propertyValue);
                jpv.setType(
                    new QName(WSIFConstants.NS_URI_2001_SCHEMA_XSD, "int"));
                jmsAddress.addJMSPropertyValue(jpv);

            } else if (propertyName.equals(JMSConstants.JMS_URL_TIME_TO_LIVE)) {
                JMSPropertyValue jpv = new JMSPropertyValue();
                jpv.setName(WSIFJMSProperties.TIMETOLIVE);
                jpv.setValue(propertyValue);
                jpv.setType(
                    new QName(WSIFConstants.NS_URI_2001_SCHEMA_XSD, "long"));
                jmsAddress.addJMSPropertyValue(jpv);

            } else if (propertyName.equals(JMSConstants.JMS_URL_PRIORITY)) {
                JMSPropertyValue jpv = new JMSPropertyValue();
                jpv.setName(WSIFJMSProperties.PRIORITY);
                jpv.setValue(propertyValue);
                jpv.setType(
                    new QName(WSIFConstants.NS_URI_2001_SCHEMA_XSD, "int"));
                jmsAddress.addJMSPropertyValue(jpv);

            } else if (propertyName.equals(JMSConstants.JMS_URL_USERID)) {
                JMSPropertyValue jpv = new JMSPropertyValue();
                jpv.setName(WSIFJMSProperties.QCF_USERID);
                jpv.setValue(propertyValue);
                jpv.setType(
                    new QName(WSIFConstants.NS_URI_2001_SCHEMA_XSD, "string"));
                jmsAddress.addJMSPropertyValue(jpv);

            } else if (propertyName.equals(JMSConstants.JMS_URL_PASSWORD)) {
                JMSPropertyValue jpv = new JMSPropertyValue();
                jpv.setName(WSIFJMSProperties.QCF_PASSWORD);
                jpv.setValue(propertyValue);
                jpv.setType(
                    new QName(WSIFConstants.NS_URI_2001_SCHEMA_XSD, "string"));
                jmsAddress.addJMSPropertyValue(jpv);

            } else {
//TODO: should this be allowed?            	
//                throw new WSIFException(
//                    "invalid property in JMS URL: " + property);
                JMSPropertyValue jpv = new JMSPropertyValue();
                jpv.setName(propertyName);
                jpv.setValue(propertyValue);
                jpv.setType(
                    new QName(WSIFConstants.NS_URI_2001_SCHEMA_XSD, "string"));
                jmsAddress.addJMSPropertyValue(jpv);
            }
        }

        return jmsAddress;
    }
    
}

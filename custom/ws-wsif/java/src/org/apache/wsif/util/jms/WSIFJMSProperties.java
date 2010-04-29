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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Enumeration;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import org.apache.wsif.WSIFException;
import org.apache.wsif.logging.Trc;

/**
 * WSIFJMSProperties is a HashMap of jms properties. The WSIFJMSProperties can either be IN 
 * or OUT. If IN, the properties can only set on a QueueSender. If OUT, the properties 
 * can only be got from a message. Reflection is used to set and get the properties in JMS. 
 * Using reflection avoids having a table of properties that would have to updated for 
 * different JMS implementations and different versions of JMS. 
 * 
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class WSIFJMSProperties extends HashMap {
	private static final long serialVersionUID = 1L;
    public static final String IN = "in";
    public static final String OUT = "out";
    private static final ArrayList allDirections =
        new ArrayList(Arrays.asList(new Object[] { IN, OUT }));

    public static final String CORRELATIONID = "JMSCorrelationID";
    public static final String DELIVERYMODE  = "JMSDeliveryMode";
    public static final String DESTINATION   = "JMSDestination";
    public static final String EXPIRATION    = "JMSExpiration";
    public static final String MESSAGEID     = "JMSMessageID";
    public static final String PRIORITY      = "JMSPriority";
    public static final String REDELIVERED   = "JMSRedelivered";
    public static final String REPLYTO       = "JMSReplyTo";
    public static final String TIMESTAMP     = "JMSTimestamp";
    public static final String TIMETOLIVE    = "JMSTimeToLive";
    public static final String TYPE          = "JMSType";
    public static final String QCF_USERID    = "JMSUserid";
    public static final String QCF_PASSWORD  = "JMSPassword";
  
    private static final ArrayList predefinedProps = 
        new ArrayList(Arrays.asList(new Object[]{
   	    CORRELATIONID,DELIVERYMODE,DESTINATION,EXPIRATION,PRIORITY,
 	    REDELIVERED,REPLYTO,TIMESTAMP,TIMETOLIVE, TYPE}));

    private String direction;

    /**
     * Constructor for WSIFJMSProperties
     */
    public WSIFJMSProperties(String direction) throws WSIFException {
        super();
        Trc.entry(this, direction);

        if (!allDirections.contains(direction))
            throw new WSIFException("Invalid direction " + direction);
        this.direction = direction;
        Trc.exit(deep());
    }

    /**
     * Constructor for WSIFJMSProperties from another HashMap.
     */
    public WSIFJMSProperties(WSIFJMSProperties props) {
        super(props);
        Trc.entry(this, props);
        direction = props.direction;
        Trc.exit(deep());
    }

    /**
     * Set all the properties that have been loaded into this HashMap on a QueueSender.
     * @return whether any properties were set on this QueueSender
     */
    public boolean set(MessageProducer producer, Message message)
        throws WSIFException {
        Trc.entry(this, producer, message);
        if (producer != null && direction != IN)
            throw new WSIFException("Only input properties can be set on a MessageProducer");
    
        if (isEmpty()) {
            Trc.exit(false);
            return false;
        }
    
        for (Iterator it = keySet().iterator(); it.hasNext();)
            try {
                String prop = (String) (it.next());
                Object value = get(prop);
                Class type = value.getClass();
    
                if (predefinedProps.contains(prop))
                    try {
                        if (prop.equals(CORRELATIONID)) {
                            message.setJMSCorrelationID((String) value);
                        } else if (prop.equals(DELIVERYMODE)) {
                            message.setJMSDeliveryMode(
                                ((Integer) value).intValue());
                            if (producer != null) {
                                producer.setDeliveryMode(((Integer) value).intValue());
                            }
                        } else if (prop.equals(DESTINATION)) {
                            message.setJMSDestination((Destination) value);
                        } else if (prop.equals(EXPIRATION)) {
                            message.setJMSExpiration(((Long) value).longValue());
                        } else if (prop.equals(PRIORITY)) {
                            message.setJMSPriority(((Integer) value).intValue());
                            if (producer != null) {
                                producer.setPriority(((Integer) value).intValue());
                            }
                        } else if (prop.equals(REDELIVERED)) {
                            message.setJMSRedelivered(
                                ((Boolean) value).booleanValue());
                        } else if (prop.equals(REPLYTO)) {
                            message.setJMSReplyTo((Destination) value);
                        } else if (prop.equals(TIMESTAMP)) {
                            message.setJMSTimestamp(((Long) value).longValue());
                        } else if (prop.equals(TIMETOLIVE)) {
                            if (producer != null) {
                                producer.setTimeToLive(((Long) value).longValue());
                            }
                        } else if(prop.equals(TYPE)) {
                            message.setJMSType((String)value);
                        }
                    } catch (ClassCastException ce) {
			        	Trc.exception(ce);
                        throw new WSIFException(
                            "Unexpected type "
                                + type
                                + " for JMS property "
                                + prop
                                + ".");
                    }
    
                // User defined properties
                else {
                    if (type.equals(String.class))
                        message.setStringProperty(prop, value.toString());
                    else if (type.equals(Integer.class))
                        message.setIntProperty(prop, ((Integer) value).intValue());
                    else if (type.equals(Boolean.class))
                        message.setBooleanProperty(
                            prop,
                            ((Boolean) value).booleanValue());
                    else if (type.equals(Byte.class))
                        message.setByteProperty(prop, ((Byte) value).byteValue());
                    else if (type.equals(Double.class))
                        message.setDoubleProperty(
                            prop,
                            ((Double) value).doubleValue());
                    else if (type.equals(Float.class))
                        message.setFloatProperty(
                            prop,
                            ((Float) value).floatValue());
                    else if (type.equals(Long.class))
                        message.setLongProperty(prop, ((Long) value).longValue());
                    else if (type.equals(Short.class))
                        message.setShortProperty(
                            prop,
                            ((Short) value).shortValue());
                    else
                        message.setObjectProperty(prop, value);
                }
            } catch (JMSException je) {
	        	Trc.exception(je);
                throw WSIFJMSConstants.ToWsifException(je);
            }
    
        Trc.exit(true);
        return true;
    }

    /**
     * Get all the properties from a Message and load them into this HashMap.
     */
    public void getPropertiesFromMessage(Message message) throws WSIFException {
        Trc.entry(this, message);
        if (direction != OUT)
            throw new WSIFException("Only output properties can be got from a message");
    
        clear();
        try {
            put(CORRELATIONID, message.getJMSCorrelationID());
            put(DELIVERYMODE, new Integer(message.getJMSDeliveryMode()));
            put(DESTINATION, message.getJMSDestination());
            put(EXPIRATION, new Long(message.getJMSExpiration()));
            put(MESSAGEID, message.getJMSMessageID());
            put(PRIORITY, new Integer(message.getJMSPriority()));
            put(REDELIVERED, new Boolean(message.getJMSRedelivered()));
            put(REPLYTO, message.getJMSReplyTo());
            put(TIMESTAMP, new Long(message.getJMSTimestamp()));
            put(TYPE, message.getJMSType());
    
            Enumeration enum_ = message.getPropertyNames();
            while (enum_.hasMoreElements()) {
                String name = (String) enum_.nextElement();
                put(name, message.getObjectProperty(name));
            }
        } catch (JMSException je) {
        	Trc.exception(je);
            throw WSIFJMSConstants.ToWsifException(je);
        }
    
        if (Trc.ON)
            Trc.exit(deep());
    }

    public void clear() {
        Trc.entry(this);
        super.clear();
        Trc.exit();
    }

    public Object get(Object o1) {
        Trc.entry(this, o1);
        Object o2 = super.get(o1);
        Trc.exit(o2);
        return o2;
    }

    public Object put(Object o1, Object o2) {
        Trc.entry(this, o1, o2);
        Object o3 = super.put(o1, o2);
        Trc.exit(o3);
        return o3;
    }

    public void putAll(HashMap hm) {
        Trc.entry(this, hm);
        super.putAll(hm);
        Trc.exit();
    }

    public String toString() {
        return "WSIFJMSProperties(" + size() + "," + hashCode() + ")";
    }

    public String deep() {
        String buff = "";
        try {
            buff = new String(super.toString() + "\n");
            buff += "direction:"
                + (direction.equals(IN) ? "in" : direction.equals(OUT) ? "out" : "unknown");

        } catch (Exception e) {
            Trc.exceptionInTrace(e);
        }
        return buff;
    }
}
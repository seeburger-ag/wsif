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

package org.apache.wsif.providers.jms;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.xml.namespace.QName;

import org.apache.wsif.format.jms.JMSFormatHandler;
import org.apache.wsif.logging.Trc;

public class PrimitiveTypeFormatHandler implements JMSFormatHandler {
    @Serial
    private static final long serialVersionUID = 1L;

    protected static DateFormat GREGORIAN_CALENDAR_DATE_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd");
    protected static DateFormat STANDARD_DATE_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    protected static DateFormat PRECISE_DATE_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'SSS'Z'");

    QName name;
    Object primitiveType;
    Class type;
    
    /**
     * Constructor for CustomerInfoFormatHandler
     */
    public PrimitiveTypeFormatHandler(Class type) {
        super();
        this.type = type;
    }

    /**
     * @see JMSFormatHandler#getElement(String)
     */
    public Object getElement(String arg0) {
        return null;
    }

    /**
     * @see JMSFormatHandler#setElement(String, Object)
     */
    public void setElement(String arg0, Object arg1) {
    }

    /**
     * @see JMSFormatHandler#getElement(String, int)
     */
    public Object getElement(String arg0, int arg1) {
        return null;
    }

    /**
     * @see JMSFormatHandler#setElement(String, int, Object)
     */
    public void setElement(String arg0, int arg1, Object arg2) {
    }

    /**
     * @see JMSFormatHandler#getObjectPart()
     */
    public Object getObjectPart() {
        return primitiveType;
    }

    /**
     * @see JMSFormatHandler#getObjectPart(Class)
     */
    public Object getObjectPart(Class arg0) {
        return null;
    }

    public char getCharPart() {
        return 0;
    }

    public byte getBytePart() {
        return 0;
    }

    public short getShortPart() {
        return 0;
    }

    public int getIntPart() {
        return 0;
    }

    public long getLongPart() {
        return 0;
    }

    public float getFloatPart() {
        return 0;
    }

    public double getDoublePart() {
        return 0;
    }

	public QName getPartQName() {
		return name;
	}

    public void setObjectPart(Object arg0) {
        primitiveType = arg0;
    }

    public void setCharPart(char arg0) {
    }

    public void setBytePart(byte arg0) {
    }

    public void setShortPart(short arg0) {
    }

    public void setIntPart(int arg0) {
    }

    public void setLongPart(long arg0) {
    }

    public void setFloatPart(float arg0) {
    }

    public void setDoublePart(double arg0) {
    }

	public void setPartQName(QName name) {
		this.name = name;
	}

    /**
     * @see JMSFormatHandler#write(Message)
     */
    public void write(Message message) {
        Trc.entry(this, message);
        try {
            if (message instanceof javax.jms.TextMessage textMessage)
                write(textMessage);
            else if (message instanceof javax.jms.ObjectMessage objectMessage)
                write(objectMessage);
        } catch (Exception e) {
        	Trc.exception(e);
        }
        Trc.exit();
    }

    /**
     * @see JMSFormatHandler#read(Message)
     */
    public void read(Message message) {
        Trc.entry(this, message);
        try {
            if (message instanceof javax.jms.TextMessage textMessage)
                read(textMessage);
            else if (message instanceof javax.jms.ObjectMessage objectMessage)
                read(objectMessage);
        } catch (Exception e) {
        	Trc.exception(e);
        }
        Trc.exit();

    }

    private void write(javax.jms.TextMessage message) throws JMSException {
        String value = null;

        if (primitiveType == null)
            value = "";
        else if (primitiveType instanceof java.util.GregorianCalendar calendar)
            value =
                GREGORIAN_CALENDAR_DATE_FORMAT.format(
                    calendar.getTime());
        else if (primitiveType instanceof java.util.Date date)
            value = STANDARD_DATE_FORMAT.format(date);
        else if (primitiveType instanceof byte[] bytes)
            value = new String(bytes);
        else {
            // Convert the primitiveType to a String
            value = primitiveType.toString();
        }

        if (value != null)
            message.setText(value);
    }

    private void write(javax.jms.ObjectMessage message) throws JMSException {
        if (primitiveType != null && primitiveType instanceof java.io.Serializable serializable)
            message.setObject(serializable);
    }

    private void read(javax.jms.TextMessage message) throws JMSException {
        String value = message.getText();

        Class cls = type;
        try {
            if (cls == String.class) {
                primitiveType = value;
            }
            // For byte array class
            else if (cls == byte[].class) {
                primitiveType = value.getBytes();
            }
            // For Gregorian Calendar
            else if (java.util.GregorianCalendar.class.isAssignableFrom(cls)) {
                java.util.GregorianCalendar result = new java.util.GregorianCalendar();
                result.setTime(GREGORIAN_CALENDAR_DATE_FORMAT.parse(value));
                primitiveType = result;
            }
            // For Date
            else if (java.util.Date.class.isAssignableFrom(cls)) {
                java.util.Date result = new java.util.Date();
                try {
                    result = STANDARD_DATE_FORMAT.parse(value);
                } catch (java.text.ParseException standardException) {
                	Trc.exception(standardException);
                    result = PRECISE_DATE_FORMAT.parse(value);
                }
                primitiveType = result;
            }
            // For all the rest
            else {
                java.lang.reflect.Constructor constructor =
                    cls.getConstructor(new Class[] { String.class });
                primitiveType = constructor.newInstance(new Object[] { value });
            }
        } catch (Exception e) {
        	Trc.exception(e);
            throw new JMSException("Unable to parse message");
        }

    }

    private void read(javax.jms.ObjectMessage message) throws JMSException {
        primitiveType = message.getObject();
        // Check to see if the contents is of the expected class type
        if (!primitiveType.getClass().isAssignableFrom(type))
            throw new JMSException("Unable to parse message");
    }
}
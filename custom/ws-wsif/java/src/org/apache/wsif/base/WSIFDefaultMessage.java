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

package org.apache.wsif.base;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.wsdl.Message;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.logging.Trc;

/**
 * A DefaultWSIFMessage is a default implementation of
 * WSIFMessage holding a collection of WSIFParts corresponding
 * to the parts of a message as defined in WSDL.
 *
 * @author Paul Fremantle
 * @author Alekander Slominski
 * @author Matthew J. Duftler
 * @author Sanjiva Weerawarana
 * @author Nirmal Mukhi
 * @author Owen Burroughs <owenb@apache.org>
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>

 */
public class WSIFDefaultMessage implements WSIFMessage {
	
	private static final long serialVersionUID = 1L;
	
    protected Map parts;
    protected String name;
    protected String style;
    protected Message msgDefinition;

    public WSIFDefaultMessage() {
        Trc.entry(this);
        Trc.exit();
    }

    /**
     * Set the name of this message.
     */
    public String getName() {
        Trc.entry(this);
        Trc.exit(name);
        return name;
    }

    /**
     * Get the name of this message.
     */
    public void setName(String name) {
        Trc.entry(this, name);
        this.name = name;
        Trc.exit();
    }

    /**
     * Return the representation style for all parts in this message
     */
    public String getRepresentationStyle() {
        Trc.entry(this);
        Trc.exit(style);
        return style;
    }

    /**
     * Set the representation style for all parts in this message
     */
    public void setRepresentationStyle(String rStyle) {
        Trc.entry(this, rStyle);
        style = rStyle;
        Trc.exit();
    }

    /**
     * Return an iterator of the parts in the message.
     * Supercedes void getParts(Map)
     */
    public Iterator getParts() {
        Trc.entry(this);
        if (parts == null) {
            parts = new HashMap();
        }
        Iterator it = parts.values().iterator();
        Trc.exit(it);
        return it;
    }

    public void setParts(Map sourceParts) {
        Trc.entry(this, sourceParts);
        if (parts == null) {
            parts = new HashMap();
        } else {
            parts.clear();
        }

        for (Iterator names = sourceParts.keySet().iterator(); names.hasNext();) {
            String name = (String) names.next();
            Object part = sourceParts.get(name);
            parts.put(name, part);
        }
        Trc.exit();
    }

    public Message getMessageDefinition() {
        Trc.entry(this);
        Trc.exit(msgDefinition);
        return msgDefinition;
    }
        
    public void setMessageDefinition(Message msgDef) {
        Trc.entry(this, msgDefinition);
        msgDefinition = msgDef;
        Trc.exit();
    }

    public void setObjectPart(String name, Object part) throws WSIFException {
        Trc.entry(this, name, part);
        if (parts == null) {
            parts = new HashMap();
        }
        parts.put(name, part);
        Trc.exit();
    }

    public Object getObjectPart(String name) throws WSIFException {
        Trc.entry(this, name);
        if (parts == null) {
            handleNoPartsException(name, "Object");
            Trc.exit(null);
            return null;
        }
        if (parts.get(name) == null) {
            if (!parts.keySet().contains(name)) {
                handlePartNotFoundException(name);
            }
            Trc.exit(null);
            return null;
        }
        Object o = parts.get(name);
        Trc.exit(o);
        return o;
    }

    public Object getObjectPart(String name, Class sourceClass)
        throws WSIFException {
        Trc.entry(this, name, sourceClass);
        if (parts == null) {
            handleNoPartsException(name, "Object");
            Trc.exit(null);
            return null;
        }
        Object part = parts.get(name);
        if (part == null) {
            if (!parts.keySet().contains(name)) {
                handlePartNotFoundException(name);
            }
            Trc.exit(null);
            return null;
        } else {
            if (part.getClass().getName().equals(sourceClass.getName())) {
                Trc.exit(part);
                return part;
            } else {
                handleSourcedPartNotFoundException(name, sourceClass);
                Trc.exit(null);
                return null;
            }
        }
    }

    public byte getBytePart(String name) throws WSIFException {
        Trc.entry(this, name);
        if (parts == null)
            handleNoPartsException(name, "byte");
        try {
            byte b = ((Byte) parts.get(name)).byteValue();
            Trc.exit(b);
            return b;
        } catch (NullPointerException ne) {
        	Trc.exception(ne);
            handlePartNotFoundException(name);
            Trc.exit(0);
            return 0;
        } catch (ClassCastException ce) {
        	Trc.exception(ce);
            handlePartCastException(name, parts.get(name).getClass().getName(), "Byte");
            Trc.exit(0);
            return 0;
        }
    }

    public void setBytePart(String name, byte part) {
        Trc.entry(this, name, new Byte(part));
        if (parts == null) {
            parts = new HashMap();
        }
        parts.put(name, new Byte(part));
        Trc.exit();
    }

    public char getCharPart(String name) throws WSIFException {
        Trc.entry(this, name);
        if (parts == null)
            handleNoPartsException(name, "char");
        try {
            char c = ((Character) parts.get(name)).charValue();
            Trc.exit(c);
            return c;
        } catch (NullPointerException ne) {
            Trc.exception(ne);
            handlePartNotFoundException(name);
            Trc.exit(0);
            return 0;
        } catch (ClassCastException ce) {
        	// If the part is a String of length 1 convert it to 
        	// a char and return the char.
            try {
                String s = (String) parts.get(name);
                if (s != null && s.length() == 1) {
                    char c = s.charAt(0);
                    Trc.exit(c);
                    return c;
                } else {
                	// throw the original exception
                	throw ce;
                }
            } catch (ClassCastException ce2) {
            	// trace the original exception
                Trc.exception(ce);
                handlePartCastException(
                    name,
                    parts.get(name).getClass().getName(),
                    "Character");
                Trc.exit(0);
                return 0;
            }            
        }
    }

    public void setCharPart(String name, char part) {
        Trc.entry(this, name, new Character(part));
        if (parts == null) {
            parts = new HashMap();
        }
        parts.put(name, new Character(part));
        Trc.exit();
    }

    public int getIntPart(String name) throws WSIFException {
        Trc.entry(this, name);
        if (parts == null)
            handleNoPartsException(name, "int");
        try {
            int i = ((Integer) parts.get(name)).intValue();
            Trc.exit(i);
            return i;
        } catch (NullPointerException ne) {
        	Trc.exception(ne);
            handlePartNotFoundException(name);
            Trc.exit(0);
            return 0;
        } catch (ClassCastException ce) {
        	Trc.exception(ce);
            handlePartCastException(name, parts.get(name).getClass().getName(), "Integer");
            Trc.exit(0);
            return 0;
        }
    }

    public void setIntPart(String name, int part) {
        Trc.entry(this, name, new Integer(part));
        if (parts == null) {
            parts = new HashMap();
        }
        parts.put(name, new Integer(part));
        Trc.exit();
    }

    public long getLongPart(String name) throws WSIFException {
        Trc.entry(this, name);
        if (parts == null)
            handleNoPartsException(name, "long");
        try {
            long l = ((Long) parts.get(name)).longValue();
            Trc.exit(new Long(l));
            return l;
        } catch (NullPointerException ne) {
        	Trc.exception(ne);
            handlePartNotFoundException(name);
            Trc.exit(0);
            return 0;
        } catch (ClassCastException ce) {
        	Trc.exception(ce);
            handlePartCastException(name, parts.get(name).getClass().getName(), "Long");
            Trc.exit(0);
            return 0;
        }
    }

    public void setLongPart(String name, long part) {
        Trc.entry(this, name, new Long(part));
        if (parts == null) {
            parts = new HashMap();
        }
        parts.put(name, new Long(part));
        Trc.exit();
    }

    public short getShortPart(String name) throws WSIFException {
        Trc.entry(this, name);
        if (parts == null)
            handleNoPartsException(name, "short");
        try {
            short s = ((Short) parts.get(name)).shortValue();
            Trc.exit(s);
            return s;
        } catch (NullPointerException ne) {
        	Trc.exception(ne);
            handlePartNotFoundException(name);
            Trc.exit(0);
            return 0;
        } catch (ClassCastException ce) {
        	Trc.exception(ce);
            handlePartCastException(name, parts.get(name).getClass().getName(), "Short");
            Trc.exit(0);
            return 0;
        }
    }

    public void setShortPart(String name, short part) {
        Trc.entry(this, name, new Short(part));
        if (parts == null) {
            parts = new HashMap();
        }
        parts.put(name, new Short(part));
        Trc.exit();
    }

    public float getFloatPart(String name) throws WSIFException {
        Trc.entry(this, name);
        if (parts == null)
            handleNoPartsException(name, "float");
        try {
            float f = ((Float) parts.get(name)).floatValue();
            Trc.exit(new Float(f));
            return f;
        } catch (NullPointerException ne) {
        	Trc.exception(ne);
            handlePartNotFoundException(name);
            Trc.exit(0);
            return 0;
        } catch (ClassCastException ce) {
        	Trc.exception(ce);
            handlePartCastException(name, parts.get(name).getClass().getName(), "Float");
            Trc.exit(0);
            return 0;
        }
    }

    public void setFloatPart(String name, float part) {
        Trc.entry(this, name, new Float(part));
        if (parts == null) {
            parts = new HashMap();
        }
        parts.put(name, new Float(part));
        Trc.exit();
    }

    public double getDoublePart(String name) throws WSIFException {
        Trc.entry(this, name);
        if (parts == null)
            handleNoPartsException(name, "double");
        try {
            double d = ((Double) parts.get(name)).doubleValue();
            Trc.exit(new Double(d));
            return d;
        } catch (NullPointerException ne) {
        	Trc.exception(ne);
            handlePartNotFoundException(name);
            Trc.exit(0);
            return 0;
        } catch (ClassCastException ce) {
        	Trc.exception(ce);
            handlePartCastException(name, parts.get(name).getClass().getName(), "Double");
            Trc.exit(0);
            return 0;
        }
    }

    public void setDoublePart(String name, double part) {
        Trc.entry(this, name, new Double(part));
        if (parts == null) {
            parts = new HashMap();
        }
        parts.put(name, new Double(part));
        Trc.exit();
    }

    public boolean getBooleanPart(String name) throws WSIFException {
        Trc.entry(this, name);
        if (parts == null)
            handleNoPartsException(name, "boolean");
        try {
            boolean b = ((Boolean) parts.get(name)).booleanValue();
            Trc.exit(b);
            return b;
        } catch (NullPointerException ne) {
        	Trc.exception(ne);
            handlePartNotFoundException(name);
            Trc.exit(false);
            return false;
        } catch (ClassCastException ce) {
        	Trc.exception(ce);
            handlePartCastException(name, parts.get(name).getClass().getName(), "Boolean");
            Trc.exit(false);
            return false;
        }
    }

    public void setBooleanPart(String name, boolean part) {
        Trc.entry(this, name, new Boolean(part));
        if (parts == null) {
            parts = new HashMap();
        }
        parts.put(name, new Boolean(part));
        Trc.exit();
    }

    public Iterator getPartNames() {
        Trc.entry(this);
        if (parts == null) {
            parts = new HashMap();
        }
        Iterator it = parts.keySet().iterator();
        Trc.exit(it);
        return it;
    }

    public Object clone() throws CloneNotSupportedException {
        Trc.entry(this);
        WSIFDefaultMessage dm = new WSIFDefaultMessage();
        dm.setName(this.name);
        dm.setRepresentationStyle(this.style);
        dm.setMessageDefinition(this.msgDefinition);

        if (parts != null) {
        	// Clone the parts:
            Iterator it = getPartNames();
            while (it.hasNext()) {
            	// For each part:
            	// 1. if it's null, null out the clone
            	// 2. if it's an objectified primitive type, use the same object
            	// 3. if it's cloneable, create a clone for it
            	// 4. if it's serializable, write it to an objectstream, read
            	//    it back into a new object for the clone 
                String pn = (String) it.next();
                Object po = parts.get(pn);
                
                // 1. if the part is null use null in the clone
                if (po == null) {
                    try {
                        dm.setObjectPart(pn, null);
                    } catch (Exception e) {
                        Trc.exception(e);
                        throw new CloneNotSupportedException(
                            "Exception thrown whilst cloning part "
                                + pn
                                + ". Message is "
                                + e.getMessage());
                    }
                    continue;
                }

                // 2. if the part is an objectified primitive type, use the
                //    same object ... as these are all immutable.
                Object simpleTypeObj = null;
                if ((po instanceof String)
                    || (po instanceof Integer)
                    || (po instanceof Float)
                    || (po instanceof Byte)
                    || (po instanceof Long)
                    || (po instanceof Short)
                    || (po instanceof Double)
                    || (po instanceof Boolean)) {
                    simpleTypeObj = po;
                }

                try {
                    if (simpleTypeObj != null) {
                        dm.setObjectPart(pn, simpleTypeObj);
                        continue;
                    }
                } catch (Exception e) {
                    Trc.exception(e);
                    throw new CloneNotSupportedException(
                        "Exception thrown whilst cloning part "
                            + pn
                            + ". Message is "
                            + e.getMessage());
                }

                // 3. if the part is Cloneable call its clone method
                if (po instanceof Cloneable) {
                    Class cls = po.getClass();
                    try {
                        Method clone = cls.getMethod("clone", null);
                        Object poc = clone.invoke(po, null);
                        dm.setObjectPart(pn, poc);
                    } catch (InvocationTargetException e) {
                        Trc.exception(e);
                        throw new CloneNotSupportedException(
                            "Exception thrown whilst cloning part "
                                + pn
                                + ". Message is "
                                + e.getTargetException().getMessage());
                    } catch (Exception e) {
                        Trc.exception(e);
                        throw new CloneNotSupportedException(
                            "Exception thrown whilst cloning part "
                                + pn
                                + ". Message is "
                                + e.getMessage());
                    }
                    continue;
                }
                
                // 4. all else has failed so the only way to deep copy the object
                //    is to serialize it to an object stream and read it back into
                //    another instance
                if (po instanceof Serializable) {
                    try {
                        ByteArrayOutputStream b = new ByteArrayOutputStream();
                        ObjectOutputStream out = new ObjectOutputStream(b);
                        out.writeObject(po);
                        out.flush();
                        out.close();
                        byte[] data = b.toByteArray();
                        WSIFObjectInputStream in =
                            new WSIFObjectInputStream(
                                new ByteArrayInputStream(data));
                        Object poc = in.readObject();
                        in.close();
                        dm.setObjectPart(pn, poc);
                    } catch (Exception e) {
                        Trc.exception(e);
                        throw new CloneNotSupportedException(
                            "Exception thrown whilst cloning part "
                                + pn
                                + " by serialization. Message is "
                                + e.getMessage());
                    }
                } else {
                    throw new CloneNotSupportedException(
                        "Part " + pn + " cannot be cloned");
                }
                // Not reached. Don't put code here
            }
        }
        if (Trc.ON)
            Trc.exit(dm.deep());
        return dm;
    }

    private void handleNoPartsException(String part, String type)
        throws WSIFException {
        throw new WSIFException(
            "Cannot get " + type + " part '" + part + "'. No parts are set on the message");
    }

    private void handlePartNotFoundException(String part) throws WSIFException {
        throw new WSIFException(
            "Cannot get part '" + part + "'. Part was not found in message");
    }

    private void handleSourcedPartNotFoundException(String part, Class sclass)
        throws WSIFException {
        throw new WSIFException(
            "Cannot get part. Part '"
                + part
                + "' with source '"
                + sclass
                + "' was not found in message");
    }

    private void handlePartCastException(String part, String act, String exp)
        throws WSIFException {
        throw new WSIFException(
            "Cannot get part '"
                + part
                + "'. Cannot convert "
                + "from "
                + act
                + " to "
                + exp);
    }

    public String toString() {
        return deep();
    }
    
    public String deep() {
        String buff = "";
        try {
            buff = new String(super.toString());
            buff += " name:" + name;
            if (parts==null)
                buff += " parts:null";
            else 
                buff += Trc.brief(Trc.LOG_LEVEL_INFO," parts",parts.values());
        } catch (Exception e) {
            Trc.exceptionInTrace(e);
        }
        return buff;
    }
}

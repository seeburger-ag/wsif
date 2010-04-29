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

package org.apache.wsif;

import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Message;

/**
 * A WSIFMessage is a an interface representing a WSDL Message.
 * <p>
 * In WSDL, a Message describes the abstract type of the input 
 * or output to an operation. This is the corresponding WSIF 
 * class which represents in memory the actual input or output
 * of an operation. 
 * <p>
 * A WSIFMessage is a container for a set of named parts. The 
 * WSIFMessage interface separates the actual representation of 
 * the data from the abstract type defined by WSDL. 
 * <p>
 * WSIFMessage implementations are free to represent the 
 * actual part data in any way that is convenient to them. 
 * This could be a simple HashMap as in the WSIFDefaultMessage 
 * implementation, or it could be something more complex, such 
 * as a stream or tree representation.
 * <p>
 * In addition to the containing parts, a WSIFMessage also has 
 * a message name. This can be used to distinguish between messages.
 * <p>
 * WSIFMessages are cloneable and serializable. If the parts set are 
 * not cloneable, the implementation should try to clone them using 
 * serialization. If the parts are not serializable either, then a 
 * CloneNotSupportedException will be thrown if cloning is attempted.
 * <p>
 * A WSIFMessage should be not created by directly instantiating
 * a WSIFMessage, but should be created by calling one of the
 * {@link WSIFOperation#createInputMessage()}, {@link WSIFOperation#createOutputMessage()},
 * or {@link WSIFOperation#createFaultMessage()} methods.
 * <p>
 * An instance of a WSIFMessage should only be used for the purpose 
 * it was created for, for example, a WSIFMessage created by the 
 * {@link WSIFOperation#createInputMessage(String)} should not be used as an 
 * output message. A WSIFMessage should only be used once, it should
 * not be reused in any subsequent WSIFOperation requests.
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
public interface WSIFMessage extends java.io.Serializable, Cloneable {
    /**
     * Get the name of this message.
     */
    public String getName();

    /**
     * Set the name of this message.
     */
    public void setName(String name);

    /**
     * Return list of part names.
     * <p><b>NOTE:</b> part names are unordered.
     */
    public Iterator getPartNames();

    /** 
     * Create an iterator of the parts in this message.
     * Supercedes void getParts(Map).
     */
    public Iterator getParts();

    /**
     * This message parts will be replaced by sourceParts.
     * @parameter sourceParts must be Map that has as key Strings with
     *   part names and values must be instances of WSIFPart.
     */
    public void setParts(Map sourceParts);

    /**
     * Get the underlying WSDL model for this message.
     * @return javax.wsdl.Message
     */
    public Message getMessageDefinition();
    
    /**
     * Set the underlying WSDL model for this message.
     * @param msgDefinition
     */
    public void setMessageDefinition(Message msgDef);

    public String getRepresentationStyle();
    public void setRepresentationStyle(String rStyle);
    
    public Object getObjectPart(String name) throws WSIFException;
    public Object getObjectPart(String name, Class sourceClass)
        throws WSIFException;
    public void setObjectPart(String name, Object part) throws WSIFException;

    public char getCharPart(String name) throws WSIFException;
    public byte getBytePart(String name) throws WSIFException;
    public short getShortPart(String name) throws WSIFException;
    public int getIntPart(String name) throws WSIFException;
    public long getLongPart(String name) throws WSIFException;
    public float getFloatPart(String name) throws WSIFException;
    public double getDoublePart(String name) throws WSIFException;
    public boolean getBooleanPart(String name) throws WSIFException;

    public void setCharPart(String name, char charPart);
    public void setBytePart(String name, byte bytePart);
    public void setShortPart(String name, short shortPart);
    public void setIntPart(String name, int intPart);
    public void setLongPart(String name, long longPart);
    public void setFloatPart(String name, float floatPart);
    public void setDoublePart(String name, double doublePart);
    public void setBooleanPart(String name, boolean booleanPart);

    public Object clone() throws CloneNotSupportedException;
}

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

package stockquote.wsifservice;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Message;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;

public class GetQuoteOutputMessage implements WSIFMessage {
    private String partNames[] = { "quote" };
    protected float _quote;
    private String representationStyle;

    public GetQuoteOutputMessage() {

    }
    public String getName() {
        return "GetQuoteOutput";
    }
    public void setName(String s) {
    }
    public java.util.Iterator getPartNames() {
        System.err.println(this.getClass().getName() + "getPartNames");
        List resp = new java.util.LinkedList();
        for (int i = 0; i < partNames.length; i++) {
            resp.add(partNames[i]);
        }
        return resp.iterator();
    }
    public float getPt_Quote() {
        return _quote;
    }
    public void setPt_Quote(float value) {
        this._quote = value;
    }
    public boolean getBooleanPart(String partName) {
        throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public void setBooleanPart(String partName, boolean value) {
        throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public char getCharPart(String partName) {
        throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public void setCharPart(String partName, char value) {
        throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public String getStringPart(String partName) {
        throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public void setStringPart(String partName, String value) {
        throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public byte getBytePart(String partName) {
        throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public void setBytePart(String partName, byte value) {
        throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public short getShortPart(String partName) {
        throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public void setShortPart(String partName, short value) {
        throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public int getIntPart(String partName) {
        throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public void setIntPart(String partName, int value) {
        throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public long getLongPart(String partName) {
        throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public void setLongPart(String partName, long value) {
        throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public float getFloatPart(String partName) {
        if (partName.equals("quote")) {
            return _quote;
        } else
            throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public void setFloatPart(String partName, float value) {
        if (partName.equals("quote")) {
            this._quote = value;
        } else
            throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public double getDoublePart(String partName) {
        throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public void setDoublePart(String partName, double value) {
        throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public Object getObjectPart(String partName) {
        if (partName.equals("quote")) {
            return new Float(_quote);
        } else
            throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public Object getObjectPart(String partName, Class sourceClass) {
        if (partName.equals("quote")) {
            return new Float(_quote);
        } else
            throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public void setObjectPart(String partName, Object value) {
        if (partName.equals("quote")) {
            this._quote = ((Float) value).floatValue();
        } else
            throw new IllegalArgumentException("Part " + partName + " not found");
    }
    public Iterator getParts() {
        Map resp = new java.util.HashMap();
        resp.put("quote", new Float(_quote));
        return resp.values().iterator();
    }
    public void getParts(Map resp) {
        resp.put("quote", new Float(_quote));
    }
    public void setParts(Map source) {
        _quote = ((Float) source.get("quote")).floatValue();
    }
    private GetQuoteOutputMessage(GetQuoteOutputMessage msg) throws WSIFException {
        this.partNames = msg.partNames;
        this.representationStyle = msg.representationStyle;
        this._quote = msg._quote;
    }
    public String getRepresentationStyle() {
        return representationStyle;
    }
    public void setRepresentationStyle(String style) {
        representationStyle = style;
    }
    public Object clone() throws CloneNotSupportedException {
        try {
            return new GetQuoteOutputMessage(this);
        } catch (WSIFException ex) {
            throw new CloneNotSupportedException(ex.getMessage());
        }
    }    

    public Message getMessageDefinition() { return null; }
    
    public void setMessageDefinition(Message msgDef) {}
}

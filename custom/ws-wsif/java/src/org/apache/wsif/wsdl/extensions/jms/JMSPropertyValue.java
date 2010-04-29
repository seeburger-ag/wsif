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

package org.apache.wsif.wsdl.extensions.jms;

import java.io.Serializable;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

/**
 * WSDL Jms Binding extension.
 * This class holds one property. It does not validate it.
 * 
 * @author Ant Elder <antelder@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class JMSPropertyValue implements ExtensibilityElement, Serializable {

	private static final long serialVersionUID = 1L;

    protected QName fieldElementType = JMSConstants.Q_ELEM_JMS_PROPERTY_VALUE;
    // Uses the wrapper type so we can tell if it was set or not.
    
    protected Boolean fieldRequired = null;
    protected String fieldName;
    protected QName fieldType;
    protected String fieldValue;

    /**
     * accessors
     */
    public String getName() {
        return fieldName;
    }

    public QName getType() {
        return fieldType;
    }

    public String getValue() {
        return fieldValue;
    }

    /**
     * mutators
     */
    public void setName(String rhs) {
        fieldName = rhs;
    }

    public void setType(QName rhs) {
        fieldType = rhs;
    }

    public void setValue(String rhs) {
        fieldValue = rhs;
    }

    /**
     * @see ExtensibilityElement#setElementType(QName)
     */
    public void setElementType(QName elementType) {
        fieldElementType = elementType;
    }

    /**
     * @see ExtensibilityElement#getElementType()
     */
    public QName getElementType() {
        return fieldElementType;
    }

    /**
     * @see ExtensibilityElement#setRequired(Boolean)
     */
    public void setRequired(Boolean required) {
        fieldRequired = required;
    }

    /**
     * @see ExtensibilityElement#getRequired()
     */
    public Boolean getRequired() {
        return fieldRequired;
    }

    public String toString() {
        StringBuffer strBuf = new StringBuffer(super.toString());

        strBuf.append("\nJavaAddress (" + fieldElementType + "):");
        strBuf.append("\nrequired=" + fieldRequired);

        strBuf.append("\nname=" + fieldName);
        strBuf.append("\ntype=" + fieldType);
        strBuf.append("\nvalue=" + fieldValue);

        return strBuf.toString();
    }
}
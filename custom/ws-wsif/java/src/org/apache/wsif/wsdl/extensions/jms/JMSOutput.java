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
import java.util.List;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

/**
 * WSDL Jms service-port extension
 * 
 * @author <a href="mailto:ake@de.ibm.com">Hermann Akermann</a>
 * @author Ant Elder <antelder@apache.org>
 */
public class JMSOutput implements ExtensibilityElement, Serializable {
	
	private static final long serialVersionUID = 1L;

    protected QName fieldElementType = JMSConstants.Q_ELEM_JMS_OUTPUT;
    // Uses the wrapper type so we can tell if it was set or not.
    
    protected Boolean fieldRequired = null;
    protected String fieldSchema;
    protected List fieldParts;

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

        strBuf.append("\nschema=" + fieldSchema);
        strBuf.append("\nparts=" + fieldParts);
        return strBuf.toString();
    }
    
    /**
     * Gets the Schema
     * @return Returns a String
     */
    public String getSchema() {
        return fieldSchema;
    }
    
    /**
     * Sets the Schema
     * @param fieldSchema The schema to set
     */
    public void setSchema(String schema) {
        this.fieldSchema = schema;
    }

    /**
     * Gets the parts
     * @return Returns a List
     */
    public List getParts() {
        return fieldParts;
    }
    
    /**
     * Sets the parts
     * @param parts The parts to set
     */
    public void setParts(List parts) {
        this.fieldParts = parts;
    }
}
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

package org.apache.wsif.wsdl.extensions.ejb;

import java.util.List;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

import com.ibm.wsdl.util.StringUtils;

/** 
 * @author Gerhard Pfau <gpfau@de.ibm.com>
 * @author Ant Elder <antelder@apache.org>
 * @author Owen Burroughs <owenb@pache.org>

 */
public class EJBOperation
    implements ExtensibilityElement, java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    protected QName fieldElementType = EJBBindingConstants.Q_ELEM_EJB_OPERATION;
    // Uses the wrapper type so we can tell if it was set or not.
    protected Boolean fieldRequired = null;
    protected java.lang.String fieldMethodName;
    protected java.lang.String fieldEjbInterface;
    protected List fieldParameterOrder;
    protected String fieldReturnPart;

    public java.lang.String getEjbInterface() {
        return fieldEjbInterface;
    }
    
    /**
     * Get the type of this extensibility element.
     *
     * @return the extensibility element's type
     */
    public QName getElementType() {
        return fieldElementType;
    }
    
    public java.lang.String getMethodName() {
        return fieldMethodName;
    }
    
    /**
     * Get whether or not the semantics of this extension
     * are required. Relates to the wsdl:required attribute.
     */
    public Boolean getRequired() {
        return fieldRequired;
    }
    
    public void setEjbInterface(java.lang.String ejbInterface) {
        fieldEjbInterface = ejbInterface;
    }
    
    /**
     * Set the type of this extensibility element.
     *
     * @param elementType the type
     */
    public void setElementType(QName elementType) {
        fieldElementType = elementType;
    }
    
    public void setMethodName(java.lang.String newMethodName) {
        fieldMethodName = newMethodName;
    }
    
    /**
     * Set whether or not the semantics of this extension
     * are required. Relates to the wsdl:required attribute.
     */
    public void setRequired(Boolean required) {
        fieldRequired = required;
    }
    
    public String toString() {
        StringBuffer strBuf = new StringBuffer(super.toString());

        strBuf.append("\nEJBOperation (" + fieldElementType + "):");
        strBuf.append("\nrequired=" + fieldRequired);
        strBuf.append("\nmethodName=" + fieldMethodName);
        strBuf.append("\nejbInterface=" + fieldEjbInterface);
        strBuf.append("\nparameterOrder=" + fieldParameterOrder);
        strBuf.append("\nreturnPart=" + fieldReturnPart);

        return strBuf.toString();
    }
    
    /**
     * Gets the fieldReturnPart
     * @return Returns a String
     */
    public String getReturnPart() {
        return fieldReturnPart;
    }
    
    /**
     * Sets the fieldReturnPart
     * @param fieldReturnPart The fieldReturnPart to set
     */
    public void setReturnPart(String fieldReturnPart) {
        this.fieldReturnPart = fieldReturnPart;
    }

    /**
     * Gets the fieldParameterOrder
     * @return Returns a List
     */
    public List getParameterOrder() {
        return fieldParameterOrder;
    }

    public void setParameterOrder(String newParameterOrderStr) {
        if (newParameterOrderStr != null) {
            fieldParameterOrder = StringUtils.parseNMTokens(newParameterOrderStr);
        }
    }
}
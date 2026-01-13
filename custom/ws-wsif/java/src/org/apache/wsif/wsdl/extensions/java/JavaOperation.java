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

package org.apache.wsif.wsdl.extensions.java;

import java.io.Serial;
import java.util.List;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

import com.ibm.wsdl.util.StringUtils;

/**
 * @author Gerhard Pfau <gpfau@de.ibm.com>
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 */
public class JavaOperation
    implements ExtensibilityElement, java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
        
    protected QName fieldElementType = JavaBindingConstants.Q_ELEM_JAVA_OPERATION;
    // Uses the wrapper type so we can tell if it was set or not.
    
    protected Boolean fieldRequired = null;
    protected String fieldMethodName;
    protected String fieldMethodType;
    protected List fieldParameterOrder;
    protected String fieldReturnPart;

    /**
     * Get the type of this extensibility element.
     *
     * @return the extensibility element's type
     */
    public QName getElementType() {
        return fieldElementType;
    }
    
    public String getMethodName() {
        return fieldMethodName;
    }
    
    public String getMethodType() {
        return fieldMethodType;
    }
    
    public List getParameterOrder() {
        return fieldParameterOrder;
    }
    
    public String getReturnPart() {
        return fieldReturnPart;
    }

    /**
     * Get whether or not the semantics of this extension
     * are required. Relates to the wsdl:required attribute.
     */
    public Boolean getRequired() {
        return fieldRequired;
    }
    
    /**
     * Set the type of this extensibility element.
     *
     * @param elementType the type
     */
    public void setElementType(QName elementType) {
        fieldElementType = elementType;
    }
    
    public void setMethodName(String newMethodName) {
        fieldMethodName = newMethodName;
    }
    
    public void setMethodType(String newMethodType) {
        fieldMethodType = newMethodType;
    }
    
    public void setParameterOrder(String newParameterOrderStr) {
        if (newParameterOrderStr != null) {
            fieldParameterOrder = StringUtils.parseNMTokens(newParameterOrderStr);
        }
    }
    
    public void setReturnPart(String newReturnPart) {
        fieldReturnPart = newReturnPart;
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

        strBuf.append("\nJavaOperation (" + fieldElementType + "):");
        strBuf.append("\nrequired=" + fieldRequired);

        strBuf.append("\nmethodName=" + fieldMethodName);
        strBuf.append("\nmethodType=" + fieldMethodType);
        strBuf.append("\nparameterOrder=" + fieldParameterOrder);
        strBuf.append("\nreturnPart=" + fieldReturnPart);

        return strBuf.toString();
    }
}

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

package org.apache.wsif.wsdl.extensions.format;

import java.io.Serial;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

/**
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 */
public class TypeMap implements java.io.Serializable, ExtensibilityElement {

    @Serial
    private static final long serialVersionUID = 1L;
    
    protected QName fieldElementType =
        FormatBindingConstants.Q_ELEM_FORMAT_BINDING_MAP;
        
    private QName fieldTypeName;
    private QName fieldElementName;
    private String fieldFormatType;
    
    /**
     * ConnectorAddress constructor comment.
     */
    public TypeMap() {
        super();
    }
    
    /**
      * Get the type of this extensibility element.
      *
      * @return the extensibility element's type
      */
    public QName getElementType() {
        return this.fieldElementType;
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (6/19/2001 9:27:15 PM)
     * @return java.lang.String
     */
    public String getFormatType() {
        return fieldFormatType;
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (6/19/2001 1:30:38 PM)
     * @return java.lang.String
     */
    public QName getTypeName() {
        return fieldTypeName;
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (7/20/2001 11:07:00 AM)
     * @return java.lang.String
     */
    public QName getElementName() {
        return fieldElementName;
    }
    
    /**
      * Get whether or not the semantics of this extension
      * are required. Relates to the wsdl:required attribute.
      */
    public Boolean getRequired() {
        return null;
    }
    
    /**
      * Set the type of this extensibility element.
      *
      * @param elementType the type
      */
    public void setElementType(QName aElementType) {

        fieldElementType = aElementType;
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (6/19/2001 9:27:15 PM)
     * @param newFormatType java.lang.String
     */
    public void setFormatType(java.lang.String newFormatType) {
        fieldFormatType = newFormatType;
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (6/19/2001 1:30:38 PM)
     * @param newPart java.lang.String
     */
    public void setTypeName(QName newTypeName) {
        fieldTypeName = newTypeName;
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (7/20/2001 11:07:00 AM)
     * @param newPosition java.lang.String
     */
    public void setElementName(QName newElementName) {
        fieldElementName = newElementName;
    }
    
    /**
      * Set whether or not the semantics of this extension
      * are required. Relates to the wsdl:required attribute.
      */
    public void setRequired(Boolean required) {
    }
}

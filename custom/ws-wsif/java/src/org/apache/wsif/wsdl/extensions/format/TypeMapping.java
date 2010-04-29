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

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

/**
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 */
public class TypeMapping
    implements java.io.Serializable, ExtensibilityElement {

	private static final long serialVersionUID = 1L;

    protected QName fieldElementType = FormatBindingConstants.Q_ELEM_FORMAT_BINDING;
    private String fieldEncoding;
    private String fieldStyle;
    protected java.util.List fieldTypeMaps = new java.util.Vector();

    /**
     * 
     */
    public TypeMapping() {
        super();
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (6/19/2001 2:59:59 PM)
     * @param newPartTypes java.util.List
     */
    public void addMap(TypeMap typeMap) {
        fieldTypeMaps.add(typeMap);
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
     * Creation date: (6/19/2001 1:23:29 PM)
     * @return java.lang.String
     */
    public java.lang.String getStyle() {
        return fieldStyle;
    }

    public java.lang.String getEncoding() {
        return fieldEncoding;
    }

    /**
     * Insert the method's description here.
     * Creation date: (6/19/2001 2:59:59 PM)
     * @return java.util.List
     */
    public java.util.List getMaps() {
        return fieldTypeMaps;
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

        this.fieldElementType = aElementType;
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (6/19/2001 1:23:29 PM)
     * @param newEncodingStyle java.lang.String
     */
    public void setStyle(java.lang.String newStyle) {
        fieldStyle = newStyle;
    }

    public void setEncoding(java.lang.String newEncoding) {
        fieldEncoding = newEncoding;
    }
    
    /**
      * Set whether or not the semantics of this extension
      * are required. Relates to the wsdl:required attribute.
      */
    public void setRequired(Boolean required) {
    }
}
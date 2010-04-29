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

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

/**
 * @author Gerhard Pfau <gpfau@de.ibm.com>
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 */
public class JavaAddress
    implements ExtensibilityElement, java.io.Serializable {

	private static final long serialVersionUID = 1L;
        
    protected QName fieldElementType = JavaBindingConstants.Q_ELEM_JAVA_ADDRESS;
    // Uses the wrapper type so we can tell if it was set or not.
    
    protected Boolean fieldRequired = null;
    protected java.lang.String fieldClassName;
    protected java.lang.String fieldClassPath;
    protected java.lang.String fieldClassLoader;

    public java.lang.String getClassLoader() {
        return fieldClassLoader;
    }

    public java.lang.String getClassName() {
        return fieldClassName;
    }

    public java.lang.String getClassPath() {
        return fieldClassPath;
    }

    /**
     * Get the type of this extensibility element.
     *
     * @return the extensibility element's type
     */
    public QName getElementType() {
        return fieldElementType;
    }

    /**
     * Get whether or not the semantics of this extension
     * are required. Relates to the wsdl:required attribute.
     */
    public Boolean getRequired() {
        return fieldRequired;
    }

    public void setClassLoader(java.lang.String newClassLoader) {
        fieldClassLoader = newClassLoader;
    }

    public void setClassName(java.lang.String newClassName) {
        fieldClassName = newClassName;
    }

    public void setClassPath(java.lang.String newClassPath) {
        fieldClassPath = newClassPath;
    }

    /**
     * Set the type of this extensibility element.
     *
     * @param elementType the type
     */
    public void setElementType(QName elementType) {
        fieldElementType = elementType;
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

        strBuf.append("\nJavaAddress (" + fieldElementType + "):");
        strBuf.append("\nrequired=" + fieldRequired);

        strBuf.append("\nclassName=" + fieldClassName);
        strBuf.append("\nclassPath=" + fieldClassPath);
        strBuf.append("\nclassLoader=" + fieldClassLoader);

        return strBuf.toString();
    }
}
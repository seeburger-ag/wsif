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

import java.io.Serializable;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

/** 
 * @author Gerhard Pfau <gpfau@de.ibm.com>
 * @author Ant Elder <antelder@apache.org>
 * @author Owen Burroughs <owenb@pache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 */
public class EJBAddress implements ExtensibilityElement, Serializable {

	private static final long serialVersionUID = 1L;
    
    protected QName fieldElementType = EJBBindingConstants.Q_ELEM_EJB_ADDRESS;
    // Uses the wrapper type so we can tell if it was set or not. 
    
    protected Boolean fieldRequired = null;

    protected java.lang.String fieldClassName;
    protected java.lang.String fieldArchive;
    protected java.lang.String fieldClassLoader;
    protected java.lang.String fieldJndiName;
    protected java.lang.String fieldInitialContextFactory;
    protected java.lang.String fieldJndiProviderURL;

    public java.lang.String getClassLoader() {
        return fieldClassLoader;
    }

    public java.lang.String getClassName() {
        return fieldClassName;
    }

    public java.lang.String getArchive() {
        return fieldArchive;
    }

    public java.lang.String getInitialContextFactory() {
        return fieldInitialContextFactory;
    }

    public java.lang.String getJndiProviderURL() {
        return fieldJndiProviderURL;
    }

    /** 
     * Get the type of this extensibility element. 
     * 
     * @return the extensibility element's type 
     */
    public QName getElementType() {
        return fieldElementType;
    }

    public java.lang.String getJndiName() {
        return fieldJndiName;
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

    public void setArchive(java.lang.String newArchive) {
        fieldArchive = newArchive;
    }

    public void setInitialContextFactory(java.lang.String newICF) {
        fieldInitialContextFactory = newICF;
    }

    public void setJndiProviderURL(java.lang.String newProvider) {
        fieldJndiProviderURL = newProvider;
    }

    /** 
     * Set the type of this extensibility element. 
     * 
     * @param elementType the type 
     */
    public void setElementType(QName elementType) {
        fieldElementType = elementType;
    }

    public void setJndiName(java.lang.String newJndiName) {
        fieldJndiName = newJndiName;
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

        strBuf.append("\nEJBAddress (" + fieldElementType + "):");
        strBuf.append("\nrequired=" + fieldRequired);
        strBuf.append("\nclassName=" + fieldClassName);
        strBuf.append("\narchive=" + fieldArchive);
        strBuf.append("\nclassLoader=" + fieldClassLoader);
        strBuf.append("\njndiName=" + fieldJndiName);
        strBuf.append("\njndiProviderURL=" + fieldJndiProviderURL);
        strBuf.append("\ninitialContextFactory=" + fieldInitialContextFactory);

        return strBuf.toString();
    }
}
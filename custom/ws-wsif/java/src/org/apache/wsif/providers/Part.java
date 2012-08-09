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

package org.apache.wsif.providers;

import java.io.Serializable;

import javax.xml.namespace.QName;

import org.apache.wsif.logging.Trc;

/**
 * WSIF representation of a WSDL Part
 *  
 * @author <a href="mailto:ant.elder@uk.ibm.com">Ant Elder</a>
 */
public class Part implements Serializable {

    protected javax.wsdl.Part wsdlPart;

    protected QName type;
    protected Class javaClass;
    protected Object value;

    /**
     * Construct a new Part
     */
    protected Part(javax.wsdl.Part wsdlPart) {
        Trc.entry(this);
        if (wsdlPart == null) {
            throw new IllegalArgumentException("wsdlPart is null");
        }

        this.wsdlPart = wsdlPart;

        Trc.exit();
    }

    /**
     * Returns the name of the Part.
     * @return String
     */
    public String getName() {
        return wsdlPart.getName();
    }

    /**
     * Returns the type of the Part.
     * @return QName
     */
    public QName getType() {
        if (type == null) {
            type = ProviderUtils.getPartType(wsdlPart);
        }
        return type;
    }

    /**
     * Returns the javaClass the Part type represents.
     * @return Class
     */
    public Class getJavaClass() {
        return javaClass;
    }

    /**
     * Sets the javaClass the Part type represents.
     * @param javaClass The javaClass to set
     */
    public void setJavaClass(Class javaClass) {
        this.javaClass = javaClass;
    }

    /**
     * Returns the wsdlPart object for the part.
     * @return javax.wsdl.Part
     */
    public javax.wsdl.Part getWsdlPart() {
        return wsdlPart;
    }

    /**
     * Returns the value.
     * @return Object
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value.
     * @param value The value to set
     */
    public void setValue(Object value) {
        this.value = value;
    }

}

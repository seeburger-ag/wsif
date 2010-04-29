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

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.ExtensionSerializer;
import javax.xml.namespace.QName;

import org.apache.wsif.logging.Trc;
import org.w3c.dom.Element;

import com.ibm.wsdl.Constants;
import com.ibm.wsdl.util.xml.DOMUtils;

/** 
 * WSDL Jms extension
 * 
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class JMSFaultPropertySerializer
    implements ExtensionSerializer, ExtensionDeserializer, Serializable {

	private static final long serialVersionUID = 1L;

    /**
     * @see ExtensionSerializer#marshall(Class, QName, ExtensibilityElement, 
     * PrintWriter, Definition, ExtensionRegistry)
     */
    public void marshall(
        Class parentType,
        QName elementType,
        javax.wsdl.extensions.ExtensibilityElement extension,
        java.io.PrintWriter pw,
        javax.wsdl.Definition def,
        javax.wsdl.extensions.ExtensionRegistry extReg)
        throws javax.wsdl.WSDLException {
        Trc.entry(parentType, elementType, extension, pw, def, extReg);

        if (extension == null) {
            Trc.exit();
            return;
        }

        JMSFaultProperty jmsFaultProperty = (JMSFaultProperty) extension;
        String tagName =
            DOMUtils.getQualifiedValue(
                JMSConstants.NS_URI_JMS,
                "faultProperty",
                def);
        pw.print("      <" + tagName);

        /**
         * handle attributes 
         */
        if (jmsFaultProperty.getName() != null) {
            DOMUtils.printAttribute(
                JMSConstants.ATTR_NAME,
                jmsFaultProperty.getName(),
                pw);
        }

        if (jmsFaultProperty.getType() != null) {
            DOMUtils.printQualifiedAttribute(
                JMSConstants.ATTR_TYPE,
                jmsFaultProperty.getType(),
                def,
                pw);
        }

        if (jmsFaultProperty.getValue() != null) {
            DOMUtils.printAttribute(
                JMSConstants.ATTR_VALUE,
                jmsFaultProperty.getValue(),
                pw);
        }

        if (jmsFaultProperty.getPart() != null) {
            DOMUtils.printAttribute(
                JMSConstants.ATTR_PART,
                jmsFaultProperty.getPart(),
                pw);
        }

        Boolean required = extension.getRequired();
        if (required != null) {
            DOMUtils.printQualifiedAttribute(
                Constants.Q_ATTR_REQUIRED,
                required.toString(),
                def,
                pw);
        }

        pw.println("/>");

        Trc.exit();
    }

    /**
     * Registers the serializer.
     */
    public void registerSerializer(ExtensionRegistry registry) {
        Trc.entry(this, registry);

        // faultProperty
        registry.registerSerializer(
            JMSFaultIndicator.class,
            JMSConstants.Q_ELEM_JMS_FAULT_PROPERTY,
            this);
        registry.registerDeserializer(
            JMSFaultIndicator.class,
            JMSConstants.Q_ELEM_JMS_FAULT_PROPERTY,
            this);
        registry.mapExtensionTypes(
            JMSFaultIndicator.class,
            JMSConstants.Q_ELEM_JMS_FAULT_PROPERTY,
            JMSFaultProperty.class);

        Trc.exit();
    }

    /**
     * @see ExtensionDeserializer#unmarshall(Class, QName, Element, Definition, 
     * ExtensionRegistry)
     */
    public ExtensibilityElement unmarshall(
        Class parentType,
        QName elementType,
        Element el,
        Definition def,
        ExtensionRegistry extReg)
        throws WSDLException {
        Trc.entry(this, parentType, elementType, el, def, extReg);

        JMSFaultProperty jmsFaultProperty =
            (JMSFaultProperty) extReg.createExtension(parentType, elementType);

        String name = DOMUtils.getAttribute(el, JMSConstants.ATTR_NAME);
        if (name != null) {
            jmsFaultProperty.setName(name);
        }

        QName type =
            DOMUtils.getQualifiedAttributeValue(
                el,
                JMSConstants.ATTR_TYPE,
                JMSConstants.ELEM_FAULT_PROPERTY,
                false,
                def);
        if (type != null) {
            jmsFaultProperty.setType(type);
        }

        String value = DOMUtils.getAttribute(el, JMSConstants.ATTR_VALUE);
        if (value != null) {
            jmsFaultProperty.setValue(value);
        }

        String part = DOMUtils.getAttribute(el, JMSConstants.ATTR_PART);
        if (part != null) {
            jmsFaultProperty.setPart(part);
        }

        Trc.exit(jmsFaultProperty);
        return jmsFaultProperty;
    }
}
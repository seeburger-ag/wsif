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

import java.io.Serial;
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
import com.ibm.wsdl.util.xml.QNameUtils;

/** 
 * WSDL Jms extension
 * 
 * @author <a href="mailto:ake@de.ibm.com">Hermann Akermann</a>
 * @author Ant Elder <antelder@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class JMSAddressSerializer
    implements ExtensionSerializer, ExtensionDeserializer, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    JMSPropertyValueSerializer jmsPropertyValueSerializer;

    private JMSPropertyValueSerializer getJMSPropertyValueSerializer() {
        if (jmsPropertyValueSerializer == null)
            jmsPropertyValueSerializer = new JMSPropertyValueSerializer();
        return jmsPropertyValueSerializer;
    }

    /**
     * @see ExtensionSerializer#marshall(Class, QName, ExtensibilityElement, PrintWriter, Definition, ExtensionRegistry)
     */
    public void marshall(
        Class parentType,
        QName elementType,
        javax.wsdl.extensions.ExtensibilityElement extension,
        java.io.PrintWriter pw,
        javax.wsdl.Definition def,
        javax.wsdl.extensions.ExtensionRegistry extReg)
        throws javax.wsdl.WSDLException {
        Trc.entry(this, parentType, elementType, extension, pw, def, extReg);

        JMSAddress jmsAddress = (JMSAddress) extension;
        String tagName =
            DOMUtils.getQualifiedValue(JMSConstants.NS_URI_JMS, "address", def);

        if (jmsAddress != null) {
            pw.print("      <" + tagName);

            /**
             * handle attributes
             */
            if (jmsAddress.getJmsVendorURI() != null) {
                DOMUtils.printAttribute(
                    JMSConstants.ELEM_JMS_JMS_VENDOR_URI,
                    jmsAddress.getJmsVendorURI(),
                    pw);
            }

            if (jmsAddress.getInitCxtFact() != null) {
                DOMUtils.printAttribute(
                    JMSConstants.ELEM_JMS_INIT_CXT_FACT,
                    jmsAddress.getInitCxtFact(),
                    pw);
            }

            if (jmsAddress.getJndiProvURL() != null) {
                DOMUtils.printAttribute(
                    JMSConstants.ELEM_JMS_JNDI_PROV_URL,
                    jmsAddress.getJndiProvURL(),
                    pw);
            }

            if (jmsAddress.getDestStyle() != null) {
                DOMUtils.printAttribute(
                    JMSConstants.ELEM_JMS_DEST_STYLE,
                    jmsAddress.getDestStyle(),
                    pw);
            }

            if (jmsAddress.getJndiConnFactName() != null) {
                DOMUtils.printAttribute(
                    JMSConstants.ELEM_JMS_JNDI_CONN_FACT_NAME,
                    jmsAddress.getJndiConnFactName(),
                    pw);
            }

            if (jmsAddress.getJndiDestName() != null) {
                DOMUtils.printAttribute(
                    JMSConstants.ELEM_JMS_JNDI_DEST_NAME,
                    jmsAddress.getJndiDestName(),
                    pw);
            }

            if (jmsAddress.getJmsProvDestName() != null) {
                DOMUtils.printAttribute(
                    JMSConstants.ELEM_JMS_JMS_PROV_DEST_NAME,
                    jmsAddress.getJmsProvDestName(),
                    pw);
            }

            if (jmsAddress.getJmsImplSpecURI() != null) {
                DOMUtils.printAttribute(
                    JMSConstants.ELEM_JMS_JMS_IMPL_SPEC_URI,
                    jmsAddress.getJmsImplSpecURI(),
                    pw);
            }

            pw.println(">");

            // Handle JMSPropertyValues
            java.util.Iterator i = jmsAddress.getJMSPropertyValues().iterator();

            while (i.hasNext()) {
                JMSPropertyValue ee = (JMSPropertyValue) i.next();
                getJMSPropertyValueSerializer().marshall(
                    JMSAddress.class,
                    JMSConstants.Q_ELEM_JMS_PROPERTY_VALUE,
                    ee,
                    pw,
                    def,
                    extReg);
            }

            Boolean required = extension.getRequired();
            if (required != null) {
                DOMUtils.printQualifiedAttribute(
                    Constants.Q_ATTR_REQUIRED,
                    required.toString(),
                    def,
                    pw);
            }

            pw.println("</" + tagName + ">");
        }
        Trc.exit();
    }

    /**
     * Registers the serializer.
     */
    public void registerSerializer(ExtensionRegistry registry) {
        Trc.entry(this, registry);

        registry.registerSerializer(
            javax.wsdl.Port.class,
            JMSConstants.Q_ELEM_JMS_ADDRESS,
            this);
        registry.registerDeserializer(
            javax.wsdl.Port.class,
            JMSConstants.Q_ELEM_JMS_ADDRESS,
            this);
        registry.mapExtensionTypes(
            javax.wsdl.Port.class,
            JMSConstants.Q_ELEM_JMS_ADDRESS,
            JMSAddress.class);
        Trc.exit();
    }

    /**
     * @see ExtensionDeserializer#unmarshall(Class, QName, Element, Definition, ExtensionRegistry)
     */
    public ExtensibilityElement unmarshall(
        Class parentType,
        QName elementType,
        Element el,
        Definition def,
        ExtensionRegistry extReg)
        throws WSDLException {
        Trc.entry(this, parentType, elementType, el, def, extReg);

        JMSAddress jmsAddress =
            (JMSAddress) extReg.createExtension(parentType, elementType);

        /**
        * handle address attributes
        */
        String jmsVendorURI =
            DOMUtils.getAttribute(el, JMSConstants.ELEM_JMS_JMS_VENDOR_URI);
        if (jmsVendorURI != null) {
            jmsAddress.setJmsVendorURI(jmsVendorURI);
        }

        String jmsImplSpecURI =
            DOMUtils.getAttribute(el, JMSConstants.ELEM_JMS_JMS_IMPL_SPEC_URI);
        if (jmsImplSpecURI != null) {
            jmsAddress.setJmsImplSpecURI(jmsImplSpecURI);
        }

        String initCxtFact =
            DOMUtils.getAttribute(el, JMSConstants.ELEM_JMS_INIT_CXT_FACT);
        if (initCxtFact != null) {
            jmsAddress.setInitCxtFact(initCxtFact);
        }

        String jndiProvURL =
            DOMUtils.getAttribute(el, JMSConstants.ELEM_JMS_JNDI_PROV_URL);
        if (jndiProvURL != null) {
            jmsAddress.setJndiProvURL(jndiProvURL);
        }

        String destStyle =
            DOMUtils.getAttribute(el, JMSConstants.ELEM_JMS_DEST_STYLE);
        if (destStyle != null) {
            jmsAddress.setDestStyle(destStyle);
        }

        String jndiConnFactName =
            DOMUtils.getAttribute(
                el,
                JMSConstants.ELEM_JMS_JNDI_CONN_FACT_NAME);
        if (jndiConnFactName != null) {
            jmsAddress.setJndiConnFactName(jndiConnFactName);
        }

        String jndiDestName =
            DOMUtils.getAttribute(el, JMSConstants.ELEM_JMS_JNDI_DEST_NAME);
        if (jndiDestName != null) {
            jmsAddress.setJndiDestName(jndiDestName);
        }

        String jmsProvDestName =
            DOMUtils.getAttribute(el, JMSConstants.ELEM_JMS_JMS_PROV_DEST_NAME);
        if (jmsProvDestName != null) {
            jmsAddress.setJmsProvDestName(jmsProvDestName);
        }

        // Handle JMSPropertyValues
        Element tempEl = DOMUtils.getFirstChildElement(el);

        while (tempEl != null) {
            if (QNameUtils
                .matches(JMSConstants.Q_ELEM_JMS_PROPERTY_VALUE, tempEl)) {
                jmsAddress.addJMSPropertyValue(
                    (JMSPropertyValue) getJMSPropertyValueSerializer()
                        .unmarshall(
                        JMSAddress.class,
                        JMSConstants.Q_ELEM_JMS_PROPERTY_VALUE,
                        tempEl,
                        def,
                        extReg));
            }

            tempEl = DOMUtils.getNextSiblingElement(tempEl);
        }

        Trc.exit(jmsAddress);
        return jmsAddress;
    }
}

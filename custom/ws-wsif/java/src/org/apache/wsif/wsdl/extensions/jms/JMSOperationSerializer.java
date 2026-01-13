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
 * @author <a href="mailto:ake@de.ibm.com">Hermann Akermann</a>
 * @author Ant Elder <antelder@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class JMSOperationSerializer
    implements ExtensionSerializer, ExtensionDeserializer, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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

        if (extension == null) {
            Trc.exit();
            return;
        }

        JMSOperation jmsOperation = (JMSOperation) extension;
        String tagName =
            DOMUtils.getQualifiedValue(JMSConstants.NS_URI_JMS, "operation", def);
        pw.print("      <" + tagName);

        /**
         * handle attributes
         */
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

        registry.registerSerializer(
            javax.wsdl.BindingOperation.class,
            JMSConstants.Q_ELEM_JMS_OPERATION,
            this);
        registry.registerDeserializer(
            javax.wsdl.BindingOperation.class,
            JMSConstants.Q_ELEM_JMS_OPERATION,
            this);
        registry.mapExtensionTypes(
            javax.wsdl.BindingOperation.class,
            JMSConstants.Q_ELEM_JMS_OPERATION,
            JMSOperation.class);
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

        JMSOperation jmsOperation =
            (JMSOperation) extReg.createExtension(parentType, elementType);

        Trc.exit(jmsOperation);
        return jmsOperation;
    }
}
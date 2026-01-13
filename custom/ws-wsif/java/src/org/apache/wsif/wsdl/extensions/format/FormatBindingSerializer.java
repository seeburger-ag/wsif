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
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.wsif.logging.Trc;
import org.w3c.dom.Element;

import com.ibm.wsdl.Constants;
import com.ibm.wsdl.util.xml.DOMUtils;
import com.ibm.wsdl.util.xml.QNameUtils;

/**
 * @author Owen Burroughs <owenb@apache.org>
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class FormatBindingSerializer
    implements
        javax.wsdl.extensions.ExtensionDeserializer,
        javax.wsdl.extensions.ExtensionSerializer,
        Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * ConnectorBindingSerializer constructor comment.
     */
    public FormatBindingSerializer() {
        super();
        Trc.entry(this);
        Trc.exit();
    }

    /**
      * 
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

        if (extension instanceof TypeMapping typeMapping) {
            pw.print("         <format:typeMapping");

            String style = typeMapping.getStyle();
            if (style != null)
                DOMUtils.printAttribute("style", style, pw);
            String encoding = typeMapping.getEncoding();
            if (encoding != null)
                DOMUtils.printAttribute("encoding", encoding, pw);
            pw.println(">");

            List maps = typeMapping.getMaps();
            Iterator iterator = maps.iterator();
            while (iterator.hasNext()) {
                TypeMap typeMap = (TypeMap) iterator.next();
                pw.print("            <format:typeMap");

                // Need definition to resolve namespace to prefix to do it properly
                QName elementName = typeMap.getElementName();
                if (elementName != null) {
                    String prefix =
                        def.getPrefix(elementName.getNamespaceURI());
                    DOMUtils.printAttribute(
                        "elementName",
                        prefix + ":" + elementName.getLocalPart(),
                        pw);
                }
                QName typeName = typeMap.getTypeName();
                if (typeName != null) {
                    String prefix = def.getPrefix(typeName.getNamespaceURI());
                    DOMUtils.printAttribute(
                        "typeName",
                        prefix + ":" + typeName.getLocalPart(),
                        pw);
                }
                String formatType = typeMap.getFormatType();
                if (formatType != null)
                    DOMUtils.printAttribute("formatType", formatType, pw);
                pw.println("/>");
                Boolean required = extension.getRequired();

                if (required != null) {
                    DOMUtils.printQualifiedAttribute(
                        Constants.Q_ATTR_REQUIRED,
                        required.toString(),
                        def,
                        pw);
                }

            }
            pw.println("         </format:typeMapping>");

        }
        Trc.exit();
    }

    /**
     * Registers the serializer.
     */
    public void registerSerializer(ExtensionRegistry registry) {
        Trc.entry(this, registry);

        // Binding
        registry.registerSerializer(
            javax.wsdl.Binding.class,
            FormatBindingConstants.Q_ELEM_FORMAT_BINDING,
            this);

        registry.registerDeserializer(
            javax.wsdl.Binding.class,
            FormatBindingConstants.Q_ELEM_FORMAT_BINDING,
            this);
        Trc.exit();
    }

    /**
     * unmarshall method comment.
     */
    public javax.wsdl.extensions.ExtensibilityElement unmarshall(
        Class parentPart,
        javax.xml.namespace.QName elementPart,
        org.w3c.dom.Element el,
        javax.wsdl.Definition def,
        javax.wsdl.extensions.ExtensionRegistry extReg)
        throws javax.wsdl.WSDLException {
        Trc.entry(this, parentPart, elementPart, el, def, extReg);

        javax.wsdl.extensions.ExtensibilityElement returnValue = null;

        if (FormatBindingConstants.Q_ELEM_FORMAT_BINDING.equals(elementPart)) {
            TypeMapping typeMapping = new TypeMapping();

            String style = DOMUtils.getAttribute(el, "style");
            String encoding = DOMUtils.getAttribute(el, "encoding");
            String requiredStr =
                DOMUtils.getAttributeNS(
                    el,
                    Constants.NS_URI_WSDL,
                    Constants.ATTR_REQUIRED);

            if (style != null) {
                typeMapping.setStyle(style);
            }
            if (encoding != null) {
                typeMapping.setEncoding(encoding);
            }

            Element tempEl = DOMUtils.getFirstChildElement(el);
            while (tempEl != null) {
                if (QNameUtils
                    .matches(
                        FormatBindingConstants.Q_ELEM_FORMAT_BINDING_MAP,
                        tempEl)) {
                    TypeMap typeMap = new TypeMap();

                    QName qElementName =
                        DOMUtils.getQualifiedAttributeValue(
                            tempEl,
                            "elementName",
                            "typeMap",
                            false,
                            def);
                    QName qTypeName =
                        DOMUtils.getQualifiedAttributeValue(
                            tempEl,
                            "typeName",
                            "typeMap",
                            false,
                            def);

                    String formatType =
                        DOMUtils.getAttribute(tempEl, "formatType");
                    if (qElementName != null) {
                        typeMap.setElementName(qElementName);
                    }
                    if (qTypeName != null) {
                        typeMap.setTypeName(qTypeName);
                    }
                    if (formatType != null) {
                        typeMap.setFormatType(formatType);
                    }
                    typeMapping.addMap(typeMap);
                }
                tempEl = DOMUtils.getNextSiblingElement(tempEl);
            }
            Trc.exit(typeMapping);
            return typeMapping;
        }
        Trc.exit(returnValue);
        return returnValue;
    }
}

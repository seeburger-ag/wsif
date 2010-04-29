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

import java.io.Serializable;

import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.ExtensionSerializer;
import javax.xml.namespace.QName;

import org.apache.wsif.logging.Trc;

import com.ibm.wsdl.Constants;
import com.ibm.wsdl.util.StringUtils;
import com.ibm.wsdl.util.xml.DOMUtils;

/**
 * @author Gerhard Pfau <gpfau@de.ibm.com>
 * @author Owen Burroughs <owenb@apache.org>
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class JavaBindingSerializer
    implements ExtensionSerializer, ExtensionDeserializer, Serializable {

	private static final long serialVersionUID = 1L;
        
    public void marshall(
        Class parentType,
        QName elementType,
        javax.wsdl.extensions.ExtensibilityElement extension,
        java.io.PrintWriter pw,
        javax.wsdl.Definition def,
        javax.wsdl.extensions.ExtensionRegistry extReg)
        throws javax.wsdl.WSDLException {
        Trc.entry(this, parentType, elementType, extension, pw, def, extReg);

        // CHANGE HERE: Adjust with unmarshall() !!!

        if (extension == null) {
            Trc.exit();
            return;
        }

        if (extension instanceof JavaBinding) {
            JavaBinding javaBinding = (JavaBinding) extension;
            pw.print("      <java:binding");

            Boolean required = extension.getRequired();
            if (required != null) {
                DOMUtils.printQualifiedAttribute(
                    Constants.Q_ATTR_REQUIRED,
                    required.toString(),
                    def,
                    pw);
            }

            pw.println("/>");
        } else if (extension instanceof JavaOperation) {
            JavaOperation javaOperation = (JavaOperation) extension;
            pw.print("      <java:operation");

            if (javaOperation.getMethodName() != null) {
                DOMUtils.printAttribute("methodName", javaOperation.getMethodName(), pw);
            }

            if (javaOperation.getMethodType() != null) {
                DOMUtils.printAttribute("methodType", javaOperation.getMethodType(), pw);
            }

            if (javaOperation.getParameterOrder() != null) {
                DOMUtils.printAttribute(
                    "parameterOrder",
                    StringUtils.getNMTokens(javaOperation.getParameterOrder()),
                    pw);
            }

            if (javaOperation.getReturnPart() != null) {
                DOMUtils.printAttribute("returnPart", javaOperation.getReturnPart(), pw);
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
        } else if (extension instanceof JavaAddress) {
            JavaAddress javaAddress = (JavaAddress) extension;
            pw.print("      <java:address");

            if (javaAddress.getClassName() != null) {
                DOMUtils.printAttribute("className", javaAddress.getClassName(), pw);
            }

            if (javaAddress.getClassPath() != null) {
                DOMUtils.printAttribute("classPath", javaAddress.getClassPath(), pw);
            }

            if (javaAddress.getClassLoader() != null) {
                DOMUtils.printAttribute("classLoader", javaAddress.getClassLoader(), pw);
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
        }
        Trc.exit();
    }

    /**
     * Registers the serializer.
     */
    public void registerSerializer(ExtensionRegistry registry) {
        Trc.entry(this, registry);
        // binding	
        registry.registerSerializer(
            javax.wsdl.Binding.class,
            JavaBindingConstants.Q_ELEM_JAVA_BINDING,
            this);
        registry.registerDeserializer(
            javax.wsdl.Binding.class,
            JavaBindingConstants.Q_ELEM_JAVA_BINDING,
            this);
        registry.mapExtensionTypes(
            javax.wsdl.Binding.class,
            JavaBindingConstants.Q_ELEM_JAVA_BINDING,
            JavaBinding.class);

        // operation
        registry.registerSerializer(
            javax.wsdl.BindingOperation.class,
            JavaBindingConstants.Q_ELEM_JAVA_OPERATION,
            this);
        registry.registerDeserializer(
            javax.wsdl.BindingOperation.class,
            JavaBindingConstants.Q_ELEM_JAVA_OPERATION,
            this);
        registry.mapExtensionTypes(
            javax.wsdl.BindingOperation.class,
            JavaBindingConstants.Q_ELEM_JAVA_OPERATION,
            JavaOperation.class);
        // address
        registry.registerSerializer(
            javax.wsdl.Port.class,
            JavaBindingConstants.Q_ELEM_JAVA_ADDRESS,
            this);
        registry.registerDeserializer(
            javax.wsdl.Port.class,
            JavaBindingConstants.Q_ELEM_JAVA_ADDRESS,
            this);
        registry.mapExtensionTypes(
            javax.wsdl.Port.class,
            JavaBindingConstants.Q_ELEM_JAVA_ADDRESS,
            JavaAddress.class);
        Trc.exit();
    }

    public javax.wsdl.extensions.ExtensibilityElement unmarshall(
        Class parentType,
        javax.xml.namespace.QName elementType,
        org.w3c.dom.Element el,
        javax.wsdl.Definition def,
        javax.wsdl.extensions.ExtensionRegistry extReg)
        throws javax.wsdl.WSDLException {
        Trc.entry(this, parentType, elementType, el, def, extReg);

        // CHANGE HERE: Use only one temp string ...

        javax.wsdl.extensions.ExtensibilityElement returnValue = null;

        if (JavaBindingConstants.Q_ELEM_JAVA_BINDING.equals(elementType)) {
            JavaBinding javaBinding = new JavaBinding();
            Trc.exit(javaBinding);
            return javaBinding;
        } else if (JavaBindingConstants.Q_ELEM_JAVA_OPERATION.equals(elementType)) {
            JavaOperation javaOperation = new JavaOperation();

            String methodName = DOMUtils.getAttribute(el, "methodName");
            //String requiredStr = DOMUtils.getAttributeNS(el, Constants.NS_URI_WSDL, Constants.ATTR_REQUIRED);
            if (methodName != null) {
                javaOperation.setMethodName(methodName);
            }

            String methodType = DOMUtils.getAttribute(el, "methodType");
            if (methodType != null) {
                javaOperation.setMethodType(methodType);
            }

            String parameterOrder = DOMUtils.getAttribute(el, "parameterOrder");
            if (parameterOrder != null) {
                javaOperation.setParameterOrder(parameterOrder);
            }

            String returnPart = DOMUtils.getAttribute(el, "returnPart");
            if (returnPart != null) {
                javaOperation.setReturnPart(returnPart);
            }
            Trc.exit(javaOperation);
            return javaOperation;
        } else if (JavaBindingConstants.Q_ELEM_JAVA_ADDRESS.equals(elementType)) {
            JavaAddress javaAddress = new JavaAddress();

            String className = DOMUtils.getAttribute(el, "className");
            if (className != null) {
                javaAddress.setClassName(className);
            }

            String classPath = DOMUtils.getAttribute(el, "classPath");
            if (classPath != null) {
                javaAddress.setClassPath(classPath);
            }

            String classLoader = DOMUtils.getAttribute(el, "classLoader");
            if (classLoader != null) {
                javaAddress.setClassLoader(classLoader);
            }
            Trc.exit(javaAddress);
            return javaAddress;
        }
        Trc.exit(returnValue);
        return returnValue;
    }
}
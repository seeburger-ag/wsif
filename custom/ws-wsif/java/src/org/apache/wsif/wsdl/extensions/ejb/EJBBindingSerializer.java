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
 * @author Ant Elder <antelder@apache.org>
 * @author Owen Burroughs <owenb@pache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class EJBBindingSerializer
        implements ExtensionSerializer, ExtensionDeserializer, Serializable {

    @Serial
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
        if (extension == null) {
            Trc.exit();
            return;
        }

        if (extension instanceof EJBBinding ejbBinding) {
            pw.print("      <ejb:binding");

            Boolean required = extension.getRequired();
            if (required != null) {
                DOMUtils.printQualifiedAttribute(
                    Constants.Q_ATTR_REQUIRED,
                    required.toString(),
                    def,
                    pw);
            }

            pw.println("/>");
        } else if (extension instanceof EJBOperation ejbOperation) {
            pw.print("      <ejb:operation");

            if (ejbOperation.getMethodName() != null) {
                DOMUtils.printAttribute("methodName", ejbOperation.getMethodName(), pw);
            }

            if (ejbOperation.getEjbInterface() != null) {
                DOMUtils.printAttribute("interface", ejbOperation.getEjbInterface(), pw);
            }

            if (ejbOperation.getParameterOrder() != null) {
                DOMUtils.printAttribute(
                    "parameterOrder",
                    StringUtils.getNMTokens(ejbOperation.getParameterOrder()),
                    pw);
            }

            if (ejbOperation.getReturnPart() != null) {
                DOMUtils.printAttribute("returnPart", ejbOperation.getReturnPart(), pw);
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
        } else if (extension instanceof EJBAddress ejbAddress) {
            pw.print("      <ejb:address");

            if (ejbAddress.getClassName() != null) {
                DOMUtils.printAttribute("className", ejbAddress.getClassName(), pw);
            }

            if (ejbAddress.getArchive() != null) {
                DOMUtils.printAttribute("archive", ejbAddress.getArchive(), pw);
            }

            if (ejbAddress.getClassLoader() != null) {
                DOMUtils.printAttribute("classLoader", ejbAddress.getClassLoader(), pw);
            }

            if (ejbAddress.getJndiName() != null) {
                DOMUtils.printAttribute("jndiName", ejbAddress.getJndiName(), pw);
            }

            if (ejbAddress.getJndiProviderURL() != null) {
                DOMUtils.printAttribute("jndiProviderURL", ejbAddress.getJndiProviderURL(), pw);
            }

            if (ejbAddress.getInitialContextFactory() != null) {
                DOMUtils.printAttribute(
                    "initialContextFactory",
                    ejbAddress.getInitialContextFactory(),
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
            EJBBindingConstants.Q_ELEM_EJB_BINDING,
            this);
        registry.registerDeserializer(
            javax.wsdl.Binding.class,
            EJBBindingConstants.Q_ELEM_EJB_BINDING,
            this);
        registry.mapExtensionTypes(
            javax.wsdl.Binding.class,
            EJBBindingConstants.Q_ELEM_EJB_BINDING,
            EJBBinding.class);

        // operation 
        registry.registerSerializer(
            javax.wsdl.BindingOperation.class,
            EJBBindingConstants.Q_ELEM_EJB_OPERATION,
            this);
        registry.registerDeserializer(
            javax.wsdl.BindingOperation.class,
            EJBBindingConstants.Q_ELEM_EJB_OPERATION,
            this);
        registry.mapExtensionTypes(
            javax.wsdl.BindingOperation.class,
            EJBBindingConstants.Q_ELEM_EJB_OPERATION,
            EJBOperation.class);

        // address 
        registry.registerSerializer(
            javax.wsdl.Port.class,
            EJBBindingConstants.Q_ELEM_EJB_ADDRESS,
            this);
        registry.registerDeserializer(
            javax.wsdl.Port.class,
            EJBBindingConstants.Q_ELEM_EJB_ADDRESS,
            this);
        registry.mapExtensionTypes(
            javax.wsdl.Port.class,
            EJBBindingConstants.Q_ELEM_EJB_ADDRESS,
            EJBAddress.class);
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

        javax.wsdl.extensions.ExtensibilityElement returnValue = null;

        if (EJBBindingConstants.Q_ELEM_EJB_BINDING.equals(elementType)) {
            EJBBinding ejbBinding = new EJBBinding();
            Trc.exit(ejbBinding);
            return ejbBinding;
        } else if (EJBBindingConstants.Q_ELEM_EJB_OPERATION.equals(elementType)) {
            EJBOperation ejbOperation = new EJBOperation();

            String methodName = DOMUtils.getAttribute(el, "methodName");

            if (methodName != null) {
                ejbOperation.setMethodName(methodName);
            }

            String ejbInterface = DOMUtils.getAttribute(el, "interface");
            if (ejbInterface != null) {
                ejbOperation.setEjbInterface(ejbInterface);
            }

            String parameterOrder = DOMUtils.getAttribute(el, "parameterOrder");
            if (parameterOrder != null) {
                ejbOperation.setParameterOrder(parameterOrder);
            }

            String returnPart = DOMUtils.getAttribute(el, "returnPart");
            if (returnPart != null) {
                ejbOperation.setReturnPart(returnPart);
            }
            Trc.exit(ejbOperation);
            return ejbOperation;
        } else if (EJBBindingConstants.Q_ELEM_EJB_ADDRESS.equals(elementType)) {
            EJBAddress ejbAddress = new EJBAddress();

            String className = DOMUtils.getAttribute(el, "className");
            if (className != null) {
                ejbAddress.setClassName(className);
            }

            String archive = DOMUtils.getAttribute(el, "archive");
            if (archive != null) {
                ejbAddress.setArchive(archive);
            }

            String classLoader = DOMUtils.getAttribute(el, "classLoader");
            if (classLoader != null) {
                ejbAddress.setClassLoader(classLoader);
            }

            String jndiName = DOMUtils.getAttribute(el, "jndiName");
            if (jndiName != null) {
                ejbAddress.setJndiName(jndiName);
            }

            String jndiProviderURL = DOMUtils.getAttribute(el, "jndiProviderURL");
            if (jndiProviderURL != null) {
                ejbAddress.setJndiProviderURL(jndiProviderURL);
            }

            String icf = DOMUtils.getAttribute(el, "initialContextFactory");
            if (icf != null) {
                ejbAddress.setInitialContextFactory(icf);
            }
            Trc.exit(ejbAddress);
            return ejbAddress;
        }
        Trc.exit(returnValue);
        return returnValue;
    }
}
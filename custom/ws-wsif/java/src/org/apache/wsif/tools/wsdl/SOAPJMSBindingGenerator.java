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

package org.apache.wsif.tools.wsdl;

import java.util.Iterator;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.xml.namespace.QName;

import org.apache.wsif.providers.ProviderUtils;
import org.apache.wsif.util.WSIFUtils;
import org.apache.wsif.wsdl.extensions.jms.JMSConstants;

import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import com.ibm.wsdl.extensions.soap.SOAPBindingImpl;

/**
 * Utility to add a WSIF SOAP/JMS binding to a WSDL4J definition
 * 
 * @author <a href="mailto:ant.elder@uk.ibm.com">Ant Elder</a>
 */
public class SOAPJMSBindingGenerator extends ModelBindingGenerator {

    // name of this BindingGenerators binding type
    protected static String soapjmsBindingTypeName = "soapjms";

    // implemeted Binding class
    protected Class implementedBinding = SOAPBinding.class;

    /**
     * Construct a new binding generator
     */
    public SOAPJMSBindingGenerator(String name) {
        super(name);
    }

    /**
     * Construct a new binding generator
     */
    public SOAPJMSBindingGenerator() {
        super(soapjmsBindingTypeName);
    }

    /**
     * Returns the implemetedBinding.
     * @return Class
     */
    public Class getImplementedBinding() {
        return implementedBinding;
    }

    /**
     * Only add a JMS binding for a SOAP binding,
     * and only if the SOAP binding doesn't already
     * have a JMS transport.
     */
    protected boolean isNewBindingRequired(Port port) {
        boolean wanted = false;
        Binding b = port.getBinding();
        List extEls = b.getExtensibilityElements();
        for (Iterator i = extEls.iterator(); !wanted && i.hasNext();) {
            Object o = i.next();
            if (o instanceof SOAPBinding sb) {
                if (JMSConstants.NS_URI_SOAPJMS.equals(sb.getTransportURI())) {
                    wanted = false;
                } else {
                    wanted = true;
                }
            }
        }
        return wanted;
    }

    /**
     * Add the JMS binding namespaces
     */
    protected void doAddNamespaces(Definition def) {
        def.addNamespace("jms", JMSConstants.NS_URI_SOAPJMS);
    }

    /**
     * Make the new JMS binding
     * Overrides makeBining in ModelBindingGenerator as 
     * the new SOAPJMS binding is the same as the existing
     * SOAP binding with only the transport URL changed.
     */
    protected Binding createBinding(Definition def, Port port) {
        Binding oldBinding = port.getBinding();
        Binding newBinding = def.createBinding();

        newBinding.setDocumentationElement(
            oldBinding.getDocumentationElement());
        newBinding.setPortType(oldBinding.getPortType());

        QName oldBindingName = oldBinding.getQName();
        QName newBindingName =
            new QName(
                oldBindingName.getNamespaceURI(),
                oldBindingName.getLocalPart()
                    + ProviderUtils.capitalizeFirst(getBindingTypeName()));
        newBinding.setQName(newBindingName);

        List operations = oldBinding.getBindingOperations();
        for (Iterator i = operations.iterator(); i.hasNext();) {
            newBinding.addBindingOperation((BindingOperation) i.next());
        }

        List ees = oldBinding.getExtensibilityElements();
        for (Iterator i = ees.iterator(); i.hasNext();) {
            ExtensibilityElement ee = (ExtensibilityElement) i.next();
            if (ee instanceof SOAPBinding binding) {
                ee = createSOAPJMSBinding(binding);
            }
            newBinding.addExtensibilityElement(ee);
        }

        newBinding.setUndefined(false);
        return newBinding;
    }

    /**
     * Create a new SOAP/JMS binding from a SOAPBinding
     */
    protected SOAPBinding createSOAPJMSBinding(SOAPBinding oldBinding) {
        SOAPBinding jmsSoapBinding = new SOAPBindingImpl();
        jmsSoapBinding.setElementType(oldBinding.getElementType());
        jmsSoapBinding.setRequired(oldBinding.getRequired());
        jmsSoapBinding.setStyle(oldBinding.getStyle());
        jmsSoapBinding.setTransportURI(JMSConstants.NS_URI_SOAPJMS);
        return jmsSoapBinding;
    }

    /**
     * Add the JMS location URL to the port
     */
    protected void doCreateServicePort(Port p) {
        SOAPAddress sa = new SOAPAddressImpl();
        sa.setLocationURI(
            "jms:/queue?destination=yourQName|connectionFactory=yourQCF|initialContextFactory=com.sun.jndi.fscontext.RefFSContextFactory|jndiProviderURL=file:///JNDI-Directory");
        p.addExtensibilityElement(sa);
    }

}

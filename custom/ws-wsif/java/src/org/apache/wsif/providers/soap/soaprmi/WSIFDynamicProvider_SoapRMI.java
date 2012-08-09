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

package org.apache.wsif.providers.soap.soaprmi;

import java.util.Iterator;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPBinding;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.base.WSIFServiceImpl;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.providers.WSIFDynamicTypeMap;
import org.apache.wsif.spi.WSIFProvider;

/**
 * SoapRMI provider of dynamic WSDL invocations.
 *
 * <P>Limitations of this SoapRMI dynamic port provider
 *     (relative to WSDL 1.1 SOAP binding):<UL>
 * <LI>only rpc style is supported (not document)
 * <LI>only HTTP transport is supported
 * <LI>only soap:body use 'encoded' is supported (not literal)
 * <LI>soap:header is not allowed
 * <LI>soap:fault is ignored
 * <LI>only first encodingStyle is used from input soap:body
 *  (when the space separated list of encoding styles is provided)
 * <LI>output soap:body namespaceURI and encodingStyles are ignored
 * <LI>first part from output soap:body is used as return value
 * <LI>fault processing is not yet implemented - SOAP faults
 *    as provided by Apache SOAP exceptions are always wrapped into
 *    WSIFException.
 * </UL>
 *
 * @author Aleksander Slominski
 */
public class WSIFDynamicProvider_SoapRMI implements WSIFProvider {

    private static final String[] supportedBindingNamespaceURIs =
        { "http://schemas.xmlsoap.org/wsdl/soap/xxx" };

    private static final String[] supportedAddressNamespaceURIs =
        { "http://schemas.xmlsoap.org/wsdl/soap/xxx" };

    public WSIFDynamicProvider_SoapRMI() {
        WSIFServiceImpl.addExtensionRegistry(
            new org.apache.wsif.wsdl.extensions.jms.JMSExtensionRegistry());
    }

    /**
     * Check if WSDL port has SOAP binding and if successful try
     * to create SOAP port instance.
     */
    public WSIFPort createDynamicWSIFPort(
        Definition def,
        Service service,
        Port port,
        WSIFDynamicTypeMap typeMap)
        throws WSIFException {

        // check that Port binding has SOAP binding extensibility element
        Binding binding = port.getBinding();
        List exs = binding.getExtensibilityElements();
        for (Iterator i = exs.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof SOAPBinding) {
                // if so try to create SOAP dynamic port instance
                return new WSIFPort_SoapRMI(def, service, port, typeMap);
            }
        }

        // otherwise return null (so other providers can be checked)
        return null;
    }

    /**
     * Returns the WSDL namespace URIs of any bindings this provider supports.
     * @return an array of all binding namespaces supported by this provider
     */
    public String[] getBindingNamespaceURIs() {
        Trc.entry(this);
        Trc.exit(supportedBindingNamespaceURIs);
        return supportedBindingNamespaceURIs;
    }

    /**
     * Returns the WSDL namespace URIs of any port addresses this provider supports.
     * @return an array of all address namespaces supported by this provider
     */
    public String[] getAddressNamespaceURIs() {
        Trc.entry(this);
        Trc.exit(supportedAddressNamespaceURIs);
        return supportedAddressNamespaceURIs;
    }
}
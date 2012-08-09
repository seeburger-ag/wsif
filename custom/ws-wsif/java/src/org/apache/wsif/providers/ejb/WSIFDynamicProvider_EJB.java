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

package org.apache.wsif.providers.ejb;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.providers.WSIFDynamicTypeMap;
import org.apache.wsif.spi.WSIFProvider;
import org.apache.wsif.wsdl.extensions.ejb.EJBBinding;

/**
 * EJB specific provider of dynamic WSDL invocations.
 *
 * @author <a href="mailto:gpfau@de.ibm.com">Gerhard Pfau</a>
 * Based on DynamicWSIFProvider_ApacheSOAP by Aleksander Slominski
 * @author Owen Burroughs <owenb@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 */
public class WSIFDynamicProvider_EJB implements WSIFProvider {

    private static final String ejb = "http://schemas.xmlsoap.org/wsdl/ejb/";
    private static String[] bindings = new String[0];
    private static String[] addresses = new String[0];
    private static boolean setUpBindings = false;
    private static boolean setUpAddresses = false;    

    public WSIFDynamicProvider_EJB() {
        Trc.entry(this);
        if (!setUpBindings) {
            setUpBindingNamespaceURIs();
        }
        if (!setUpAddresses) {
            setUpAddressNamespaceURIs();
        }        
        Trc.exit();
    }

    /**
     * Check if WSDL port has EJB binding and if successful try
     * to create EJB port instance.
     */
    public WSIFPort createDynamicWSIFPort(
        Definition def,
        Service service,
        Port port,
        WSIFDynamicTypeMap typeMap)
        throws WSIFException {
        Trc.entry(this, def, service, port, typeMap);

        // check that Port binding has EJB binding extensibility element
        Binding binding = port.getBinding();
        List exs = binding.getExtensibilityElements();
        for (Iterator i = exs.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof EJBBinding) {
                // if so try to create EJB dynamic port instance
                WSIFPort wp = new WSIFPort_EJB(def, port, typeMap);
                Trc.exit(wp);
                return wp;
            }
        }

        // otherwise return null (so other providers can be checked)
        Trc.exit();
        return null;
    }

    /**
     * Returns the WSDL namespace URIs of any bindings this provider supports.
     * @return an array of all binding namespaces supported by this provider
     */
    public String[] getBindingNamespaceURIs() {
        Trc.entry(this);
        Trc.exit(bindings);
        return bindings;
    }

    /**
     * Returns the WSDL namespace URIs of any port addresses this provider supports.
     * @return an array of all address namespaces supported by this provider
     */
    public String[] getAddressNamespaceURIs() {
        Trc.entry(this);
        Trc.exit(addresses);
        return addresses;
    }

    private void setUpBindingNamespaceURIs() {
        // check if the ejb classes are available, if not then we cannot
        // support ejb bindings!        
        Class cls =
            (Class) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    return Class.forName(
                        "javax.ejb.EJBHome",
                        true,
                        Thread.currentThread().getContextClassLoader());
                } catch (Throwable ignored) {
                  	Trc.ignoredException(ignored);
                }
                return null;
            }
        });
        if (cls != null) {
            bindings = new String[] { ejb };
        }
        setUpBindings = true;
    }

    private void setUpAddressNamespaceURIs() {
        // check if the ejb classes are available, if not then we cannot
        // support ejb addresses!        
        Class cls =
            (Class) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    return Class.forName(
                        "javax.ejb.EJBHome",
                        true,
                        Thread.currentThread().getContextClassLoader());
                } catch (Throwable ignored) {
                  	Trc.ignoredException(ignored);
                }
                return null;
            }
        });
                
        if (cls != null) {
            addresses = new String[] { ejb };
        }
        setUpAddresses = true;
    }
}
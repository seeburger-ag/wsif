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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionRegistry;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.base.WSIFServiceImpl;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.spi.WSIFProvider;
import org.apache.wsif.tools.wsdl.BindingGenerator;

/**
 * Model super class for a WSIFProvider
 * 
 * Models are provided for all the classes required to 
 * be implemented when writing a WSIF provider: 
 * 			WSIFProvider, WSIFPort, and WSIFOperation. 
 * The models are intended to simplify the work in the
 * implementing subclasses, and insure that all providers
 * work in standard way. Things like hunting around in 
 * the WSDL for ExtensabilityElements and verifying the 
 * types of request and response objects against the WSDL
 * should be done in the model code. Subclasses should only 
 * need to provide code directly related to accessing the
 * particular service type they implement.
 *  
 * @author <a href="mailto:ant.elder@uk.ibm.com">Ant Elder</a>
 */
public abstract class ModelWSIFProvider implements WSIFProvider {

    /* the namespaces of the binding the provider supports */
    protected String[] supportedBindings;

    /* the namespaces of the addresses the provider supports */
    protected String[] supportedAddresses;
    // TODO: why is that required??? Its a transport thing so not
    // really applicable at this level. Lets try to get rid of it 

    /**
     * Construct a new WSIFProvider
     * 
     * This first checks if all the classes required by the provider
     * are available. If so it registers any extension registeries 
     * required by the provider and initialises the binding and address
     * namespaces supported by the provider. If required classes are not
     * available then the provider disables itself by setting the supported
     * binding and address namespaces to empty arrays. 
     */
    protected ModelWSIFProvider() throws WSIFException {
        Trc.entry(this);
        if (isActive()) {
            enableProvider();
        } else {
            disableProvider();
        }
        Trc.exit();
    }

    /**
     * Returns the WSDL namespace URIs of any bindings this provider supports.
     * @return String[] of binding namespaces supported by this provider
     */
    public String[] getBindingNamespaceURIs() {
        Trc.entry(this);
        String[] bindings = supportedBindings;
        Trc.exit(bindings);
        return bindings;
    }

    /**
     * Returns the WSDL namespace URIs of any port addresses this provider supports.
     * @return String[] of address namespaces supported by this provider
     */
    public String[] getAddressNamespaceURIs() {
        Trc.entry(this);
        String[] addresses = supportedAddresses;
        Trc.exit(addresses);
        return addresses;
    }

    /**
     * @see WSIFProvider#createDynamicWSIFPort(Definition, Service, Port, WSIFDynamicTypeMap)
     */
    public WSIFPort createDynamicWSIFPort(
        Definition def,
        Service service,
        Port port,
        WSIFDynamicTypeMap typeMap)
        throws WSIFException {
        Trc.entry(this, def, service, port, typeMap);

        Class bindingClass = getImplementedBindingClass();
        if (!(ExtensibilityElement.class.isAssignableFrom(bindingClass))) {
            throw new WSIFException("getImplementedBindingClass must return a subclass of ExtensibilityElement");
        }

        ModelWSIFPort wsifPort = null;
        Binding binding = port.getBinding();

        List l = binding.getExtensibilityElements();
        for (Iterator i = l.iterator(); wsifPort == null && i.hasNext();) {
            Object o = i.next();
            if (bindingClass.isAssignableFrom(o.getClass())) {
                ExtensibilityElement ee = (ExtensibilityElement) o;
                wsifPort = makeWSIFPort(def, service, port, typeMap, ee);
            }
        }

        if (wsifPort != null) {
            initializeWSIFPort(port, wsifPort);
        }

        Trc.exit(wsifPort);
        return wsifPort;
    }

    protected void initializeWSIFPort(Port port, ModelWSIFPort wsifPort)
        throws WSIFException {
        Class addressClass = wsifPort.getImplementedAddressClass();
        ExtensibilityElement addrEE = null;
        List portEEs = port.getExtensibilityElements();
        for (Iterator i = portEEs.iterator(); addrEE == null && i.hasNext();) {
            ExtensibilityElement ee = (ExtensibilityElement) i.next();
            if (addressClass.isAssignableFrom(ee.getClass())) {
                addrEE = ee;
            }
        }
        if (addrEE == null) {
            throw new WSIFException(
                "port '"
                    + port
                    + "' missing address ExtensibilityElement: "
                    + addressClass.getName());
        }
        wsifPort.validateAddress(addrEE);
        wsifPort.doInitialize();
    }

    /**
     * Factory method to create a WSIFPort.
     * By default this calls the short form of makeWSIFPort
     * which takes only a Definition, Port and WSIFDynamicType.
     * Subclass may override this implementation if required. 
     */
    protected ModelWSIFPort makeWSIFPort(
        Definition def,
        Service service,
        Port port,
        WSIFDynamicTypeMap typeMap,
        ExtensibilityElement binding)
        throws WSIFException {
        return makeWSIFPort(def, port, typeMap);
    }

    /**
     * Checks if the provider is active.
     * A provider is active when all the classes it requires
     * are available in the classpath. If they are not, then
     * the provider should disable it self.
     * 
     * @return boolean   true if all the classes are available,
     *                    otherwise false
     */
    protected boolean isActive() {
        String[] requiredClasses = getRequiredClasses();
        return isRequiredClassesAvailable(requiredClasses);
    }

    /**
     * Register any extension registeries and initialises
     * the supported binding and address namespaces.
     */
    protected void enableProvider() throws WSIFException {
        ExtensionRegistry[] extensionRegistries = getExtensionRegistries();
        for (int i = 0; i < extensionRegistries.length; i++) {
            WSIFServiceImpl.addExtensionRegistry(extensionRegistries[i]);
        }
        supportedBindings = getSupportedBindingNamespaces();
        supportedAddresses = getSupportedAddressNamespaces();
        Trc.event(null, "provider " + getProviderName() + " enabled");
    }

    /**
     * Disable the provider by setting the supported
     * namepsaces to empty arrays.
     */
    protected void disableProvider() {
        String[] none = new String[0];
        supportedBindings = none;
        supportedAddresses = none;
        Trc.event(null, "provider " + getProviderName() + " disabled");
    }

    /**
     * Returns the binding namespaces the provider implements
     * TODO: perhaps better for subclasses to hard code this?
     */
    protected String[] getSupportedBindingNamespaces() throws WSIFException {
        ExtensibilityElement binding;
        try {
            binding =
                (ExtensibilityElement) getImplementedBindingClass()
                    .newInstance();
        } catch (InstantiationException e) {
            throw new WSIFException(
                "InstantiationException creating binding: " + e);
        } catch (IllegalAccessException e) {
            throw new WSIFException(
                "IllegalAccessException creating binding: " + e);
        }
        return new String[] { binding.getElementType().getNamespaceURI()};
    }

    /**
     * Returns the address namespaces the provider implements
     * Defaults to the same as the supported binding namespaces.
     */
    protected String[] getSupportedAddressNamespaces() throws WSIFException {
        return getSupportedBindingNamespaces();
    }

    /**
     * Checks if classes are available in the classpath.
     * 
     * @param classes   the names of the classes to check are avaliable
     */
    protected static boolean isRequiredClassesAvailable(final String[] classes) {
        boolean classesAvailable = true;
        if (classes != null) {
            for (int i = 0; classesAvailable && i < classes.length; i++) {
                final String className = classes[i];
                Class c =
                    (Class) AccessController
                            .doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        try {
                            return Class.forName(
                                className,
                                true,
                                Thread.currentThread().getContextClassLoader());
                        } catch (Throwable ignored) {
                            Trc.ignoredException(ignored);
                        }
                        return null;
                    }
                });
                if (c == null) {
                    classesAvailable = false;
                }
            }
        }
        Trc.event(null, "required classes available=" + classesAvailable);
        return classesAvailable;
    }

    /* The following are all the methods subclasses must implement */

    /**
     * Returns the name of the provider
     */
    abstract protected String getProviderName();

    /**
     * Returns an array of class names the provider requires available
     */
    abstract protected String[] getRequiredClasses();

    /**
     * Returns an array of the ExtensionRegistery's to be registered for the provider
     */
    abstract protected ExtensionRegistry[] getExtensionRegistries();

    /**
     * Returns the WSDL4J Binding ExtensabilityElement implemented by the provider 
     */
    abstract protected Class getImplementedBindingClass();

    /**
     * Returns a tooling BindingGenerator for the provider
     */
    abstract public BindingGenerator[] getBindingGenerators();

    /**
     * Factory method for providers to make a WSIFPort
     */
    abstract protected ModelWSIFPort makeWSIFPort(
        Definition def,
        Port port,
        WSIFDynamicTypeMap typeMap)
        throws WSIFException;

}

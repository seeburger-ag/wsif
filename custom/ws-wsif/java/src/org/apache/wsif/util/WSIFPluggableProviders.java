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
package org.apache.wsif.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFException;
import org.apache.wsif.logging.MessageLogger;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.spi.WSIFProvider;

/**
 * Utility methods for pluggable provider support.
 * <p>
 * A WSIF provider is a class that implements the 
 * org.apache.wsif.spi.Provider interface. A Provider is
 * the logic that supports any particular WSDL binding extension.
 * <p>
 * WSIF providers are packaged in JAR files, and use the J2SE 1.3 
 * JAR file extensions to support service providers. 
 * A WSIF Provider JAR will contain the following file:
 * <p>
 * META-INF/services/com.ibm.wsif.spi.WSIFProvider
 * <p>
 * This file will contain a list of the class names of the Provider 
 * classes in the JAR (which must implement com.ibm.wsif.spi.WSIFProvider).
 * When the first request for a provider is made all the providers
 * defined in the META-INF/services files will be instantiated. This can
 * be prevented from happening by using the <code>setAutoLoadProviders<\code>
 * method. When this is done providers must be manually defined by using the
 * <code>overrideDefaultProvider<\code> method.
 * <p>
 * It is possible to have multiple providers supporting the same binding
 * namespace. When this occurs the provider used is chosen in the following 
 * order:
 * 1 - the provider explicitly set for the namespace with the 
 *     <code>overrideDefaultProvider<\code> method.
 * 2 - the provider defined in the WSIF properties file as being the
 *     default provider for the binding namespace
 * 3 - the provider defined first in the META-INF/services file in the
 *     jar file found first in the classpath.
 *
 * @author Ant Elder <antelder@apache.org>
 */
public class WSIFPluggableProviders {

    // defines if providers will be loaded automatically
    private static boolean autoLoadProviders = true;

    // all the providers found in all the SPI files from
    // all the jar files in the classpath, in the order
    // they were found with the 1st in the ArrayList
    // being higher in the classpath.
    private static ArrayList providersFromSPIFiles;

    // a mapping of providers chosen to be supporting
    // a binding namespace. The mapping key is the 
    // namespace URI, the value is the provider
    private static HashMap defaultNSProviders;

    private static final String PLUGABLE_PROVIDER_FILENAME =
        "META-INF/services/org.apache.wsif.spi.WSIFProvider";

    /**
     * Gets a WSIFProvider for a particular bindng namespace URI.
     * @param namespaceURI  the URI of the binding namespace 
     *                       that the WSIFProvider must support 
     * @return    a WSIFProvider supporting the requested binding
     *             namespace, or null if no providers are available. 
     */
    public static WSIFProvider getProvider(String namespaceURI) {
        Trc.entry(null, namespaceURI);
        WSIFProvider provider;

        // the defaultNSProviders Hashtable URIs end with a '/'
        if (!namespaceURI.endsWith("/")) {
            namespaceURI += "/";
        }

        if (defaultNSProviders == null) {
            defaultNSProviders = new HashMap();
        } else {
            provider = (WSIFProvider) defaultNSProviders.get(namespaceURI);
            if (provider != null) {
                return provider;
            }
        }
        ArrayList providers = getSupportingProviders(namespaceURI, true);
        if (providers.size() == 0) {
            return null;
        }
        if (providers.size() == 1) {
            provider = (WSIFProvider) providers.getFirst();
        } else {
            provider = chooseProvider(providers, namespaceURI);
        }
        defaultNSProviders.put(namespaceURI, provider);
        Trc.exit(provider);
        return provider;
    }

    /**
     * Change the WSIFProvider used for a particular binding namespace.
     * Calling this with a null provider removes the previously chosen 
     * provider for the binding namespace causing the next request for
     * a provider for the namespace to use the default search order.
     * 
     * @param providerNamespaceURI    the binding namespace to be overriden
     * @param provider   the WSIFProvider to be used for the binding namespace
     */
    public static void overrideDefaultProvider(
        String providerNamespaceURI,
        WSIFProvider provider) {
        Trc.entry(null, providerNamespaceURI, provider);

        if (defaultNSProviders == null) {
            defaultNSProviders = new HashMap();
        }

        // the defaultNSProviders HashMap URIs end with a '/'
        if (!providerNamespaceURI.endsWith("/")) {
            providerNamespaceURI += "/";
        }

        if (provider == null) {
            WSIFProvider p =
                (WSIFProvider) defaultNSProviders.get(providerNamespaceURI);
            if (defaultNSProviders != null) {
                defaultNSProviders.remove(providerNamespaceURI);
            }
        } else {
            defaultNSProviders.put(providerNamespaceURI, provider);
            issueChosenProviderMsg(providerNamespaceURI, provider);
        }

        Trc.exit();
    }

    /**
     * Tests if a provider is available for the given namespace.
     * @param ns1   the WSDL binding namespace URI
     * @return   true if a provider is available for the given
     *            binding namespace, otherwise false. 
     */
    public static boolean isProviderAvailable(String ns1) {
        return isProviderAvailable(ns1, ns1);
    }

    /**
     * Tests if a provider is available for the given namespaces.
     * @param ns1   the WSDL binding namespace URI
     * @param ns2   the WSDL port addresses namespace URI
     * @return   true if a provider is available for the given
     *            namespaces, otherwise false. 
     */
    public static boolean isProviderAvailable(String ns1, String ns2) {
        boolean supported = false;
        ArrayList ps = getSupportingProviders(ns1, false);
        if (ps.size() > 0) {
            if (ns2 == null || ns2.length() < 1 || ns2.equals(ns1)) {
                supported = true;
            } else {
                String[] supportedNS;
                for (int i = 0; i < ps.size() && !supported; i++) {
                 supportedNS = ((WSIFProvider)ps.get(i)).getAddressNamespaceURIs();
           	     for (int j=0; j<supportedNS.length && !supported; j++) {
                        if (ns2.equals(supportedNS[j])) {
                            supported = true;
                        }
                    }
                }
            }
        }
        return supported;
    }

    /**
     * This sets if the WSIFProviders will be automatically loaded.
     * If this is set to false any providers required msut be explicitly 
     * defined by using the <code>overrideDefaultProvider<\code> method.
     * Changing the state of the auto loading of providers clears any providers
     * that have already been loaded or choosen as a default provider.  
     * @param b   true means all the WSIFProviders will be loaded automatically,
     *            false means all WSIFProviders must be manually set with the
     *            setDynamicWSIFProvider method
     */
    public static void setAutoLoadProviders(boolean b) {
        Trc.entry(null, b);
        if (autoLoadProviders != b) {
            providersFromSPIFiles = null;
            defaultNSProviders = null;
            autoLoadProviders = b;
        }
        Trc.exit();
    }

    /**
     * Tests if providers are set to be automatically loaded.
     * @return true if providers will be loaded automatically,
     *          otherwise false.
     */
    public static boolean isAutoLoadProviders() {
        Trc.entry(null);
        Trc.exit(autoLoadProviders);
        return autoLoadProviders;
    }

    /**
     * Gets all the available WSIFProvider that support a particular 
     * namespace URI.
     * @param namesapceURI   the namespace the WSIFProvider must support.
     * @param issueMessage Flag to inicate whether or not to log when multiple
     * providers are found for the same namespace. 
     * @return    an array of WSIFProvider. The array will have a length
     * of zero if no WSIFProvider are available for the requested namespace.
     */
    private static ArrayList getSupportingProviders(
        String namespaceURI,
        boolean issueMessage) {

        Trc.entry(null, namespaceURI, Boolean.valueOf(issueMessage));
        if (providersFromSPIFiles == null) {
            providersFromSPIFiles = getAllDynamicWSIFProviders();
        }
        ArrayList supportingProviders = new ArrayList();
        String[] uris;
        WSIFProvider p;
        for (Iterator i = providersFromSPIFiles.iterator(); i.hasNext();) {
            p = (WSIFProvider) i.next();
            uris = p.getBindingNamespaceURIs();
            for (int j = 0; j < uris.length; j++) {
                if (namespaceURI != null && namespaceURI.equals(uris[j])) {
                    Trc.event(
                        null,
                        "Adding provider " + p + " for namespace " + uris[j]);
                    supportingProviders.add(p);
                }
            }
        }
        if (defaultNSProviders != null) {
            if (defaultNSProviders.get(namespaceURI) != null) {
                Trc.event(
                    null,
                    "Adding default provider "
                        + defaultNSProviders.get(namespaceURI)
                        + " for namespace "
                        + namespaceURI);
                supportingProviders.add(defaultNSProviders.get(namespaceURI));
            }
        }

        if (supportingProviders.size() > 1 && issueMessage) {
            issueMultipleProvidersMsg(namespaceURI, supportingProviders);
        }

        Trc.exit(supportingProviders);
        return supportingProviders;
    }

    /**
     * Gets all the available WSIFProviders. 
     * WSIFProviders are located using the J2SE 1.3 JAR file extensions 
     * to support service providers.
     * @return    an array of WSIFProvider.
     */
    private static ArrayList getAllDynamicWSIFProviders() {
        Trc.entry(null);
        if (!autoLoadProviders) {
            return new ArrayList();
        }

        ArrayList al =
            (ArrayList) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return findPlugableProviders();
            }
        });
        Trc.exit(al);
        return al;
    }

    private static ArrayList findPlugableProviders() {
        Object o;
        ArrayList classNames = new ArrayList();
        ArrayList providers = new ArrayList();

        // find all the class names mentioned in all the META-INF files
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try {
            for (Enumeration e = loader.getResources(PLUGABLE_PROVIDER_FILENAME); e.hasMoreElements();) {
                readMETAINFClassNames((URL) e.nextElement(), classNames);
            }
        } catch (Exception ex) {
            Trc.exception(ex);
            MessageLogger.log("WSIF.0003W", ex.getMessage());
            return providers;
        }

        // instantiate a provider for each of the named classes
        for (Iterator i = classNames.iterator(); i.hasNext();) {
            try {
                o = Class.forName((String) i.next(), true, loader).newInstance();
                if (o instanceof org.apache.wsif.spi.WSIFProvider p) {
                    if (p.getBindingNamespaceURIs().length > 0) {
                        Trc.event(null, "Registering provider: " + p);
                        providers.add(p);
                    } else {
                        WSIFException ex =
                            new WSIFException(
                                "Disabled WSIFProvider found:"
                                    + p.getClass().getName());
                        Trc.ignoredException(ex);
                    }
                } else {
                    MessageLogger.log(
                        "WSIF.0003W",
                        "The provider class specified,"
                            + ((o == null) ? null : o.getClass().getName())
                            + ", does not implement org.apache.wsif.spi.WSIFProvider");
                }
            } catch (ClassNotFoundException ex) {
                Trc.exception(ex);
                MessageLogger.log("WSIF.0003W", ex.getMessage());
            } catch (Throwable ex) {
                Trc.exception(ex);
                MessageLogger.log("WSIF.0003W", ex.getMessage());
            }
        }

        return providers;
    }

    private static void readMETAINFClassNames(URL u, ArrayList classNames) {
        Trc.entry(null, u);
        Trc.event(
            null,
            "Reading provider class names from URL: " + u.toString());
        BufferedReader in = null;
        String inputLine;
        int i;
        try {
            in = new BufferedReader(new InputStreamReader(u.openStream()));
            while ((inputLine = in.readLine()) != null) {
                i = inputLine.indexOf('#');
                if (i >= 0) {
                    inputLine = inputLine.substring(0, i);
                }
                inputLine = inputLine.trim();
                if (inputLine.length() > 0) {
                    Trc.event(null, "Found provider class name: " + inputLine);
                    if (!classNames.contains(inputLine)) {
                    	classNames.add(inputLine);
                    }                    
                }
            }
        } catch (IOException ex) {
            Trc.exception(ex);
            MessageLogger.log("WSIF.0003W", ex.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Trc.exception(ex);
                    MessageLogger.log("WSIF.0003W", ex.getMessage());
                }
            }
        }
        Trc.exit();
    }

    /**
     * Chooses a particular WSIFProvider. If the passed array of providers
     * contains more than one element then a choice is made based on a WSIF 
     * properties file default setting.     
     * @param providers   an array of WSIFProvider 
     * @return    a WSIFProvider. Returns null if the input array is null or
     *            has a length of zero, the first element if the array contains
     *            only one element, or an element based on the property file setting.
     */
    private static WSIFProvider chooseProvider(
        ArrayList providers,
        String uri) {
        if (providers == null || providers.size() < 1) {
            return null;
        } else if (providers.size() == 1) {
            return (WSIFProvider) providers.getFirst();
        }

        int i = providers.size() - 1;
        while (i > 0
            && !isDefaultProvider((WSIFProvider) providers.get(i), uri)) {
            i--;
        }
        WSIFProvider p = (WSIFProvider) providers.get(i);
        issueChosenProviderMsg(uri, p);

        return p;
    }

    /**
     * Tests if a class name is defined in the WSIF properties file as being the
     * default WSIF provider for the namespace URI.
     * @param className  the class name to test
     * @param uri the namespace URI
     * @return    true if className is defined as the default WSIFprovider, 
     *            otherwise false.
     */
    private static boolean isDefaultProvider(
        WSIFProvider provider,
        String uri) {
        String className = provider.getClass().getName();
        String defaultURI;
        try {
            String key = WSIFConstants.WSIF_PROP_PROVIDER_PFX1 + className;
            int n = Integer.parseInt(WSIFProperties.getProperty(key));
            for (int i = 1; i <= n; i++) {
                key =
                    WSIFConstants.WSIF_PROP_PROVIDER_PFX2 + i + "." + className;
                defaultURI = WSIFProperties.getProperty(key);
                if (uri != null && uri.equals(defaultURI)) {
                    return true;
                }
            }
        } catch (NumberFormatException e) { // ignore any error
            Trc.ignoredException(e);
        } // ignore any error
        return false;
    }

    /**
     * Record the fact that multiple providers 
     * exist with support for the same namespaceURI.
     * @param uri   the namespaceURI with multiple WSIFProviders 
     * @param providers  an array of the providers supporting the namespaceURI 
     */
    private static void issueMultipleProvidersMsg(
        String uri,
        ArrayList providers) {
        String providerNames = providers.getFirst().getClass().getName();
        for (int i = 1; i < providers.size(); i++) {
            providerNames += ", " + providers.get(i).getClass().getName();
        }
        Trc.event(null, "- Multiple WSIFProvider found supporting the same namespace URI \'"
            + uri + "\'. Found (\'" + providerNames + "\')");
    }

    /**
     * Record which provider has 
     * been chosen to support a namespaceURI when multiple providers are available.
     * @param uri   the namespaceURI with multiple WSIFProviders 
     * @param providers  an array of the providers supporting the namespaceURI 
     */
    private static void issueChosenProviderMsg(
        String uri,
        WSIFProvider provider) {
        	
        String provName = (provider == null) ? "null" : provider.getClass().getName();
        Trc.event(null, "- Using WSIFProvider \'" + provName + "\' for namespaceURI \'" + 
            uri + "\'");
    }

}

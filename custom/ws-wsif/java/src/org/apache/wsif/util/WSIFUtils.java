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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFException;
import org.apache.wsif.base.WSIFServiceImpl;
import org.apache.wsif.format.WSIFFormatHandler;
import org.apache.wsif.logging.MessageLogger;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.mapping.MappingHelper;
import org.apache.wsif.providers.ProviderUtils;
import org.apache.wsif.wsdl.AuthenticatingProxyWSDLLocatorImpl;
import org.apache.wsif.wsdl.ClosableLocator;
import org.apache.wsif.wsdl.WSIFWSDLLocatorImpl;
import org.apache.wsif.wsdl.extensions.format.TypeMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.ibm.wsdl.Constants;

/**
 * This class provides utilities for WSIF runtime and generated stubs.
 *
 * @author Alekander Slominski
 * @author Matthew J. Duftler
 * @author Sanjiva Weerawarana
 * @author Owen Burroughs <owenb@apache.org>
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 * @author Piotr Przybylski
 */
public class WSIFUtils {
    private static Class initContextClass;
    private static final String SLASH = "/";
    private static final String DOT = ".";
    private static final String FORMAT_HANDLER = "FormatHandler";
    private static final String ELEMENT_FORMAT_HANDLER = "ElementFormatHandler";
    private static final String PHYSICALREP = "physicalrep/";
    private static final String FORMATBINDING = "formatbinding/";
    private static final String XMLSEPARATORS =
        "\u002D\u002E\u003A\u00B7\u0387\u06DD\u06DE\u30FB";
    private static final String XMLSEPARATORS_NODOT =
        "\u002D\u003A\u00B7\u0387\u06DD\u06DE";
    private static final String UNDERSCORE = "_";
    private static final String WWW = "www";
    private static final String lookupPrefix = "java:comp/env/";
    private static final String emptyString = "";
    
    private static Boolean providersInitialized = new Boolean(false);
    private static boolean simpleTypesMapCreated = false;
    private static HashMap simpleTypesMap = new HashMap();
	private static HashMap keywordMap = null;
    
    /**
     * This checks whether JNDI classes are available at runtime.    
     */
    public static boolean isJNDIAvailable() {
        Trc.entry(null);
        initContextClass =
            (Class) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                try {
                    return Class.forName(
                        "javax.naming.InitialContext",
                        true,
                        Thread.currentThread().getContextClassLoader());
                } catch (Throwable ignored) {
                    Trc.ignoredException(ignored);
                }
                return null;
            }
        });

        boolean b = true;
        if (initContextClass == null)
            b = false;
        Trc.exit(b);
        return b;
    }

    public static Service selectService(
        Definition def,
        String serviceNS,
        String serviceName)
        throws WSIFException {
        Trc.entry(null, def, serviceNS, serviceName);
        Map services = getAllItems(def, "Service");
        QName serviceQName =
            ((serviceNS != null && serviceName != null)
                ? new QName(serviceNS, serviceName)
                : null);
        Service service =
            (Service) getNamedItem(services, serviceQName, "Service");

        Trc.exit(service);
        return service;
    }

    public static PortType selectPortType(
        Definition def,
        String portTypeNS,
        String portTypeName)
        throws WSIFException {
        Trc.entry(null, def, portTypeNS, portTypeName);
        Map portTypes = getAllItems(def, "PortType");
        QName portTypeQName =
            ((portTypeNS != null && portTypeName != null)
                ? new QName(portTypeNS, portTypeName)
                : null);
        PortType portType =
            (PortType) getNamedItem(portTypes, portTypeQName, "PortType");

        Trc.exit(portType);
        return portType;
    }

    public static void addDefinedItems(
        Map fromItems,
        String itemType,
        Map toItems) {
        Trc.entry(null, fromItems, itemType, toItems);

        if (fromItems != null) {
            Iterator entryIterator = fromItems.entrySet().iterator();

            if (itemType.equals("Message")) {
                while (entryIterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) entryIterator.next();
                    Message message = (Message) entry.getValue();

                    if (!message.isUndefined()) {
                        toItems.put(entry.getKey(), message);
                    }
                }
            } else if (itemType.equals("Operation")) {
                while (entryIterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) entryIterator.next();
                    Operation operation = (Operation) entry.getValue();

                    if (!operation.isUndefined()) {
                        toItems.put(entry.getKey(), operation);
                    }
                }
            } else if (itemType.equals("PortType")) {
                while (entryIterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) entryIterator.next();
                    PortType portType = (PortType) entry.getValue();

                    if (!portType.isUndefined()) {
                        toItems.put(entry.getKey(), portType);
                    }
                }
            } else if (itemType.equals("Binding")) {
                while (entryIterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) entryIterator.next();
                    Binding binding = (Binding) entry.getValue();

                    if (!binding.isUndefined()) {
                        toItems.put(entry.getKey(), binding);
                    }
                }
            } else if (itemType.equals("Service")) {
                while (entryIterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) entryIterator.next();
                    Service service = (Service) entry.getValue();

                    toItems.put(entry.getKey(), service);
                }
            }
        }
        Trc.exit();
    }

    private static void getAllItems(
        Definition def,
        String itemType,
        Map toItems) {
        Trc.entry(null, def, itemType, toItems);
        Map items = null;

        if (itemType.equals("PortType")) {
            items = def.getPortTypes();
        } else if (itemType.equals("Service")) {
            items = def.getServices();
        } else {
            throw new IllegalArgumentException(
                "Don't know how to find all " + itemType + "s.");
        }

        addDefinedItems(items, itemType, toItems);

        Map imports = def.getImports();

        if (imports != null) {
            Iterator valueIterator = imports.values().iterator();

            while (valueIterator.hasNext()) {
                List importList = (List) valueIterator.next();

                if (importList != null) {
                    Iterator importIterator = importList.iterator();

                    while (importIterator.hasNext()) {
                        Import tempImport = (Import) importIterator.next();

                        if (tempImport != null) {
                            Definition importedDef = tempImport.getDefinition();

                            if (importedDef != null) {
                                getAllItems(importedDef, itemType, toItems);
                            }
                        }
                    }
                }
            }
        }
        Trc.exit();
    }

    public static Map getAllItems(Definition def, String itemType) {
        Trc.entry(null, def, itemType);
        Map ret = new HashMap();

        getAllItems(def, itemType, ret);

        Trc.exit(ret);
        return ret;
    }

    public static Object getNamedItem(Map items, QName qname, String itemType)
        throws WSIFException {
        Trc.entry(null, items, qname, itemType);
        if (qname != null) {
            Object item = items.get(qname);

            if (item != null) {
                Trc.exit(item);
                return item;
            } else {
                throw new WSIFException(
                    itemType
                        + " '"
                        + qname
                        + "' not found. Choices are: "
                        + getCommaListFromQNameMap(items));
            }
        } else {
            int size = items.size();

            if (size == 1) {
                Iterator valueIterator = items.values().iterator();

                Object o = valueIterator.next();
                Trc.exit(o);
                return o;
            } else if (size == 0) {
                throw new WSIFException(
                    "WSDL document contains no " + itemType + "s.");
            } else {
                throw new WSIFException(
                    "Please specify a "
                        + itemType
                        + ". Choices are: "
                        + getCommaListFromQNameMap(items));
            }
        }
    }

    private static String getCommaListFromQNameMap(Map qnameMap) {
        StringBuffer strBuf = new StringBuffer("{");
        Set keySet = qnameMap.keySet();
        Iterator keyIterator = keySet.iterator();
        int index = 0;

        while (keyIterator.hasNext()) {
            QName key = (QName) keyIterator.next();

            strBuf.append((index > 0 ? ", " : "") + key);
            index++;
        }

        strBuf.append("}");

        return strBuf.toString();
    }

    /**
     * Read WSDL - it is different from a WSDLReader readWSDL method in that it
     * specifies the use of the WSIF WSDLFactory implementation which registers
     * the extensiblity elements used by the WSIF providers.
     * @param contextURL The context in which to resolve the wsdlLoc, if the wsdlLoc is 
     * relative. Can be null, in which case it will be ignored.
     * @param wsdlLoc a URI (can be a filename or URL) pointing to a WSDL XML definition.
     * @return Defintion object representing the definition in the wsdl
     * @throws WSDLException Exception thrown if wsdl cannot be read
     */
    public static Definition readWSDL(String contextURL, String wsdlLoc)
        throws WSDLException {
        Trc.entry(null, contextURL, wsdlLoc);

        initializeProviders();

        WSDLFactory factory = WSDLFactory.newInstance(
            WSIFConstants.WSIF_WSDLFACTORY);
        WSDLReader wsdlReader = factory.newWSDLReader();
        wsdlReader.setFeature(Constants.FEATURE_VERBOSE, false);
        try {
            Definition def = wsdlReader.readWSDL(contextURL, wsdlLoc);
            Trc.exitExpandWsdl(def);
            return def;
        } catch (WSDLException e) {
            Trc.exception(e);
            MessageLogger.log("WSIF.0002E", wsdlLoc);
            throw e;
        }
    }

    /**
     * Read WSDL through an authenticating proxy. It is different from a WSDLReader 
     * readWSDL method in that it
     * specifies the use of the WSIF WSDLFactory implementation which registers
     * the extensiblity elements used by the WSIF providers.
     * @param wsdlLoc a URI (must be an http or ftp URL) pointing to a WSDL file
     * @param pa A username and password for the proxy, encapsulated as a 
     * java.net.PasswordAuthentication object
     * @return Defintion object representing the definition in the wsdl
     * @throws WSDLException Exception thrown if wsdl cannot be read
     */
    public static Definition readWSDLThroughAuthProxy(String wsdlLoc, PasswordAuthentication pa)
        throws WSDLException {
        Trc.entry(null, wsdlLoc, pa);

        initializeProviders();

        WSDLFactory factory = WSDLFactory.newInstance(
            WSIFConstants.WSIF_WSDLFACTORY);
        WSDLReader wsdlReader = factory.newWSDLReader();
        wsdlReader.setFeature(Constants.FEATURE_VERBOSE, false);
        WSDLLocator lo = null;
        try {
            lo = new AuthenticatingProxyWSDLLocatorImpl(wsdlLoc, pa);
            Definition def = wsdlReader.readWSDL(lo);
            Trc.exitExpandWsdl(def);
            return def;
        } catch (WSDLException e) {
            Trc.exception(e);
            MessageLogger.log("WSIF.0002E", wsdlLoc);
            throw e;
        } finally {
        	try {
        		if (lo != null && lo instanceof ClosableLocator) {
        		    ((ClosableLocator) lo).close();
        		}
        	} catch (IOException ioe) {
        		//ignore
        		Trc.ignoredException(ioe);
        	}
        }
    }

    /**
     * Read WSDL - it is different from a WSDLReader readWSDL method in that it
     * specifies the use of the WSIF WSDLFactory implementation which registers
     * the extensiblity elements used by the WSIF providers.
     * @param loc A WSDLLocator to use in locating the wsdl file and its imports
     * @throws WSDLException Exception thrown if wsdl cannot be read
     */
    public static Definition readWSDL(WSDLLocator loc)
        throws WSDLException {
        Trc.entry(null, loc);
        
        if (loc == null) {
        	throw new WSDLException(WSDLException.CONFIGURATION_ERROR,
        	   "Cannot use null WSDLLocator for reading wsdl");
        }
        
        initializeProviders();

        WSDLFactory factory = WSDLFactory.newInstance(
            WSIFConstants.WSIF_WSDLFACTORY);
        WSDLReader wsdlReader = factory.newWSDLReader();
        wsdlReader.setFeature(Constants.FEATURE_VERBOSE, false);
        try {
            Definition def = wsdlReader.readWSDL(loc);
            Trc.exitExpandWsdl(def);
            return def;
        } catch (WSDLException e) {
            Trc.exception(e);
            MessageLogger.log("WSIF.0002E", loc.getBaseURI());
            throw e;
        }
    }

    /**
     * Read WSDL - it is different from a WSDLReader readWSDL method in that it
     * specifies the use of the WSIF WSDLFactory implementation which registers
     * the extensiblity elements used by the WSIF providers.
     * @param documentBase A URL for the document base URI for the wsdl
     * @param reader A Reader "pointing at" the wsdl file
     * @param cl A ClassLoader used to resolve relative imports when files are in
     * in the classpath
     * @return Defintion object representing the definition in the wsdl
     * @throws WSDLException Exception thrown if wsdl cannot be read
     */
    public static Definition readWSDL(
        URL documentBase,
        Reader reader,
        ClassLoader cl)
        throws WSDLException {
        String base = (documentBase == null) ? null : documentBase.toString();
        return readWSDL(base, reader, cl);
    }

    /**
     * Read WSDL - it is different from a WSDLReader readWSDL method in that it
     * specifies the use of the WSIF WSDLFactory implementation which registers
     * the extensiblity elements used by the WSIF providers.
     * @param documentBase The document base URI for the wsdl
     * @param reader A Reader "pointing at" the wsdl file
     * @param cl A ClassLoader used to resolve relative imports when files are in
     * in the classpath
     * @return Defintion object representing the definition in the wsdl
     * @throws WSDLException Exception thrown if wsdl cannot be read
     */
    public static Definition readWSDL(
        String documentBase,
        Reader reader,
        ClassLoader cl)
        throws WSDLException {
        Trc.entry(null, documentBase, reader, cl);

        initializeProviders();

        WSDLFactory factory = WSDLFactory.newInstance(
            WSIFConstants.WSIF_WSDLFACTORY);
        WSDLReader wsdlReader = factory.newWSDLReader();
        wsdlReader.setFeature(Constants.FEATURE_VERBOSE, false);
        WSIFWSDLLocatorImpl lo = null;
        try {
            lo = new WSIFWSDLLocatorImpl(documentBase, reader, cl);
            Definition def = wsdlReader.readWSDL(lo);
            Trc.exitExpandWsdl(def);
            return def;
        } catch (WSDLException e) {
            Trc.exception(e);
            MessageLogger.log("WSIF.0002E", documentBase);
            throw e;
        } finally {
        	try {
        		if (lo != null) lo.close();
        	} catch (IOException ioe) {
        		//ignore
        		Trc.ignoredException(ioe);
        	}
        }
    }

    /**
     * Read WSDL - it is different from a WSDLReader readWSDL method in that it
     * specifies the use of the WSIF WSDLFactory implementation which registers
     * the extensiblity elements used by the WSIF providers.
     * @param contextURL The context in which to resolve the wsdlLoc, if the wsdlLoc is 
     * relative. Can be null, in which case it will be ignored.
     * @param wsdlLoc a URI (can be a filename or URL) pointing to a WSDL XML definition.
     * @param cl A ClassLoader used to resolve relative imports when files are in
     * in the classpath
     * @return Defintion object representing the definition in the wsdl
     * @throws WSDLException Exception thrown if wsdl cannot be read
     */
    public static Definition readWSDL(
        URL contextURL,
        String wsdlLoc,
        ClassLoader cl)
        throws WSDLException {
        Trc.entry(null, contextURL, wsdlLoc, cl);

        initializeProviders();

        WSDLFactory factory = WSDLFactory.newInstance(
            WSIFConstants.WSIF_WSDLFACTORY);
        WSDLReader wsdlReader = factory.newWSDLReader();
        wsdlReader.setFeature(Constants.FEATURE_VERBOSE, false);
        WSIFWSDLLocatorImpl lo = null;

        try {
            String url = (contextURL == null) ? null : contextURL.toString();
            lo = new WSIFWSDLLocatorImpl(url, wsdlLoc, cl);
            Definition def = wsdlReader.readWSDL(lo);
            Trc.exitExpandWsdl(def);
            return def;
        } catch (WSDLException e) {
            Trc.exception(e);
            MessageLogger.log("WSIF.0002E", wsdlLoc);
            throw e;
        } finally {
        	try {
        		if (lo != null) lo.close();
        	} catch (IOException ioe) {
        		//ignore
        		Trc.ignoredException(ioe);
        	}
        }
    }

    /**
     * Read WSDL - it is different from a WSDLReader readWSDL method in that it
     * specifies the use of the WSIF WSDLFactory implementation which registers
     * the extensiblity elements used by the WSIF providers.
     * @param documentBaseURI the document base URI of the WSDL definition
     * described by the element. Will be set as the documentBaseURI
     * of the returned Definition. Can be null, in which case it
     * will be ignored.
     * @param reader A Reader "pointing at" the wsdl file
     * @return Defintion object representing the definition in the wsdl
     * @throws WSDLException Exception thrown if wsdl cannot be read
     */
    public static Definition readWSDL(String documentBaseURI, Reader reader)
        throws WSDLException {
        Trc.entry(null, documentBaseURI, reader);

        initializeProviders();

        WSDLFactory factory = WSDLFactory.newInstance(
            WSIFConstants.WSIF_WSDLFACTORY);
        WSDLReader wsdlReader = factory.newWSDLReader();
        wsdlReader.setFeature(Constants.FEATURE_VERBOSE, false);
        Definition def =
            wsdlReader.readWSDL(documentBaseURI, new InputSource(reader));
        Trc.exitExpandWsdl(def);
        return def;
    }

    /**
     * Read WSDL - it is different from a WSDLReader readWSDL method in that it
     * specifies the use of the WSIF WSDLFactory implementation which registers
     * the extensiblity elements used by the WSIF providers.
     * @param documentBaseURI the document base URI of the WSDL definition
     * described by the element. Will be set as the documentBaseURI
     * of the returned Definition. Can be null, in which case it
     * will be ignored.
     * @param wsdlDocument The base wsdl document
     * @return Defintion object representing the definition in the wsdl
     * @throws WSDLException Exception thrown if wsdl cannot be read
     */
    public static Definition readWSDL(String documentBaseURI, Document wsdlDocument)
        throws WSDLException {
        Trc.entry(null, documentBaseURI, wsdlDocument);

        initializeProviders();

        WSDLFactory factory = WSDLFactory.newInstance(
            WSIFConstants.WSIF_WSDLFACTORY);
        WSDLReader wsdlReader = factory.newWSDLReader();
        wsdlReader.setFeature(Constants.FEATURE_VERBOSE, false);
        Definition def = wsdlReader.readWSDL(documentBaseURI, wsdlDocument);

        Trc.exitExpandWsdl(def);
        return def;
    }

    /**
     * Read WSDL - it is different from a WSDLReader readWSDL method in that it
     * specifies the use of the WSIF WSDLFactory implementation which registers
     * the extensiblity elements used by the WSIF providers.
     * @param documentBaseURI the document base URI of the WSDL definition
     * described by the element. Will be set as the documentBaseURI
     * of the returned Definition. Can be null, in which case it
     * will be ignored.
     * @param definitionsElement the &lt;wsdl:definitions&gt; element
     * @return Defintion object representing the definition in the wsdl
     * @throws WSDLException Exception thrown if wsdl cannot be read
     */
    public static Definition readWSDL(
        String documentBaseURI,
        Element wsdlServicesElement)
        throws WSDLException {
        Trc.entry(null, documentBaseURI, wsdlServicesElement);

        initializeProviders();

        WSDLFactory factory = WSDLFactory.newInstance(
            WSIFConstants.WSIF_WSDLFACTORY);
        WSDLReader wsdlReader = factory.newWSDLReader();
        wsdlReader.setFeature(Constants.FEATURE_VERBOSE, false);
        Definition def = wsdlReader.readWSDL(documentBaseURI, wsdlServicesElement);

        Trc.exitExpandWsdl(def);
        return def;
    }

    /**
     * Write WSDL. This method will use the WSIF WSDLFactory implementation to
     * create a WSDLWriter.
     * @param wsdlDef the WSDL definition to be written.
     * @param sink the Writer to write the xml to.
     * @throws WSDLException Exception thrown if wsdl cannot be written
     */
    public static void writeWSDL(Definition def, Writer sink)
        throws WSDLException {
        Trc.entry(null, def, sink);

        WSDLFactory factory = WSDLFactory.newInstance(
            WSIFConstants.WSIF_WSDLFACTORY);
        WSDLWriter wsdlWriter = factory.newWSDLWriter();
        wsdlWriter.writeWSDL(def, sink);

        Trc.exit();
    }

    public static Definition getDefinitionFromLocation(
        String contextURL,
        String location)
        throws WSIFException {
        Trc.entry(null, contextURL, location);

        if (location == null) {
            throw new WSIFException("WSDL location must not be null.");
        }

        Definition def = null;
        try {
            def = WSIFUtils.readWSDL(contextURL, location);
        } catch (WSDLException e) {
            Trc.exception(e);
            throw new WSIFException("Problem reading WSDL document.", e);
        }
        Trc.exitExpandWsdl(def);
        return def;
    }

    public static Definition getDefinitionFromContent(
        String contextURL,
        String content)
        throws WSIFException {
        Trc.entry(null, contextURL, content);
        if (content == null) {
            throw new WSIFException("WSDL content must not be null.");
        }

        Definition def = null;
        try {
            def = WSIFUtils.readWSDL(contextURL, new StringReader(content));
        } catch (WSDLException e) {
            Trc.exception(e);
            throw new WSIFException("Problem reading WSDL document.", e);
        }
        Trc.exitExpandWsdl(def);
        return def;
    }

    /**
     * Initialize the WSIF providers. Each provider initializes its WSDL
     * extension registries. This has no effect if AutoLoad providers has
     * been turned off on WSIFServiceImpl ... in that case it is the
     * responsibility of the application to initialize providers.
     */
    public static void initializeProviders() {
        synchronized (providersInitialized) {
            if (!providersInitialized.booleanValue()) {
                WSIFPluggableProviders.getProvider("/");
                providersInitialized = new Boolean(true);
            }
        }
    }

    /**
     * Create a map of all schema simple types and there Java equivalents.
     */
    public static void createSimpleTypesMap() {
        synchronized (simpleTypesMap) {
            if (!simpleTypesMapCreated) {
                MappingHelper.populateWithStandardXMLJavaMappings(
                    simpleTypesMap,
                    WSIFConstants.NS_URI_1999_SCHEMA_XSD,
                    true);
                MappingHelper.populateWithStandardXMLJavaMappings(
                    simpleTypesMap,
                    WSIFConstants.NS_URI_2000_SCHEMA_XSD,
                    false);
                MappingHelper.populateWithStandardXMLJavaMappings(
                    simpleTypesMap,
                    WSIFConstants.NS_URI_2001_SCHEMA_XSD,
                    false);
                simpleTypesMapCreated = true;
            }
        }
    }

    /**
     * Get a map of all schema simple types and there Java equivalents.
     * @return The map of simple types
     */
    public static Map getSimpleTypesMap() {
        if (!simpleTypesMapCreated) {
            createSimpleTypesMap();
        }
        return simpleTypesMap;
    }

    // the following code copied from JCAUtils
    public static WSIFFormatHandler getFormatHandler(
        Part part,
        Definition definition,
        javax.wsdl.Binding binding)
        throws
            java.lang.InstantiationException,
            java.lang.IllegalAccessException,
            java.lang.ClassNotFoundException {
        Trc.entry(null, part, definition, binding);
        WSIFFormatHandler formatHandler = null;
        javax.xml.namespace.QName partTypeQName = part.getTypeName();
        if (partTypeQName == null)
            partTypeQName = part.getElementName();
        if (partTypeQName == null)
            throw new ClassNotFoundException(part.getName());

        String typePackageName =
            getPackageNameFromNamespaceURI(partTypeQName.getNamespaceURI());
        String formatHandlerName = typePackageName;

        String bindingShortName =
            getPackageNameFromXMLName(
                definition.getPrefix(getBindingNamespace(binding)));
        if (bindingShortName != null)
            formatHandlerName = formatHandlerName + DOT + bindingShortName;

        if (getFormatStylePackage(binding) != null)
            formatHandlerName =
                formatHandlerName + DOT + getFormatStylePackage(binding);

        String formatHandlerShortName =
            formatHandlerName
                + DOT
                + getJavaClassNameFromXMLName(partTypeQName.getLocalPart());

        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            formatHandler =
                (WSIFFormatHandler) cl
                    .loadClass(formatHandlerShortName + FORMAT_HANDLER)
                    .newInstance();
        } catch (ClassNotFoundException exn1) {
            Trc.ignoredException(exn1);
            try {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                formatHandler =
                    (WSIFFormatHandler) cl
                        .loadClass(
                            formatHandlerShortName + ELEMENT_FORMAT_HANDLER)
                        .newInstance();
            } catch (ClassNotFoundException exn2) {
                Trc.ignoredException(exn2);
                try {
                    formatHandler =
                        (WSIFFormatHandler) Class
                            .forName(formatHandlerShortName + FORMAT_HANDLER)
                            .newInstance();
                } catch (ClassNotFoundException exn3) {
                    Trc.ignoredException(exn3);
                    //try {
                    formatHandler =
                        (WSIFFormatHandler) Class
                            .forName(
                                formatHandlerShortName
                                    + ELEMENT_FORMAT_HANDLER)
                            .newInstance();
                    //}
                    //catch (ClassNotFoundException exn4) {
                    //	throw new ClassNotFoundException(JCAResource.get("IWAA0101E", formatHandlerName));
                    //}
                }
            }
        }
        //catch (Throwable exn3) {
        //throw new ClassNotFoundException(JCAResource.get("IWAA0102E", formatHandlerName, exn3.getLocalizedMessage()));
        //}
        if (formatHandler == null)
            //throw new ClassNotFoundException(JCAResource.get("IWAA0101E", formatHandlerName));
            throw new ClassNotFoundException(formatHandlerName);
        else {
            Trc.exit(formatHandler);
            return formatHandler;
        }
    }

    public static String getPackageNameFromNamespaceURI(String namespaceURI) {
        Trc.entry(null, namespaceURI);
        // Get the segments in the namespace URI
        List segments = getNamespaceURISegments(namespaceURI);

        StringBuffer packageNameBuffer = new StringBuffer();
        for (int i = 0; i < segments.size(); i++) {
            String name;

            // The first segment is the host name
            if (i == 0) {

                // Turn segment into a valid package segment name
                name = getPackageNameFromXMLName((String) segments.get(i));

                // Reverse its components
                StringTokenizer tokenizer = new StringTokenizer(name, ".");
                List host = new ArrayList();
                for (; tokenizer.hasMoreTokens();) {
                    String nextT = tokenizer.nextToken();
                    host.add(0, nextT);
                }
                StringBuffer buffer = new StringBuffer();
                for (Iterator hi = host.iterator(); hi.hasNext();) {
                    if (buffer.length() != 0)
                        buffer.append('.');
                    String nextSegment = (String) hi.next();
                    if (!Character
                        .isJavaIdentifierStart(nextSegment.toCharArray()[0]))
                        nextSegment = UNDERSCORE + nextSegment;
                    if (isJavaKeyword(nextSegment))
                        nextSegment = UNDERSCORE + nextSegment;
                    buffer.append(nextSegment);
                }
                name = buffer.toString();
            } else {

                // Turn segment into a valid java name
                name = getJavaNameFromXMLName((String) segments.get(i));

            }

            // Concatenate segments, separated by '.'
            if (name.length() == 0)
                continue;
            if (packageNameBuffer.length() != 0)
                packageNameBuffer.append('.');
            packageNameBuffer.append(name);
        }
        Trc.exit(packageNameBuffer.toString());
        return packageNameBuffer.toString();
    }

    public static String getJavaNameFromXMLName(
        String xmlName,
        String delims) {
        Trc.entry(null, xmlName, delims);
        StringTokenizer tokenizer = new StringTokenizer(xmlName, delims);
        StringBuffer buffer = new StringBuffer();
        while (tokenizer.hasMoreTokens()) {
            buffer.append(tokenizer.nextToken());
        }
        String result = buffer.toString();
        if (!Character.isJavaIdentifierStart(result.toCharArray()[0]))
            result = UNDERSCORE + result;
        if (isJavaKeyword(result))
            result = UNDERSCORE + result;
        Trc.exit(result);
        return result;
    }

    public static String getJavaNameFromXMLName(String xmlName) {
        Trc.entry(null, xmlName);
        String s = getJavaNameFromXMLName(xmlName, XMLSEPARATORS);
        Trc.exit(s);
        return s;
    }

    public static String getPackageNameFromXMLName(String xmlName) {
        Trc.entry(null, xmlName);

        // Tokenize, don't consider '.' as a delimiter here
        String name = getJavaNameFromXMLName(xmlName, XMLSEPARATORS_NODOT);

        // Tokenize using delimiter '.' and add the tokens separated by '.'
        // This is to ensure that we have no heading/trailing/dup '.' in the string
        StringTokenizer tokenizer = new StringTokenizer(name, DOT);
        StringBuffer buffer = new StringBuffer();
        for (; tokenizer.hasMoreTokens();) {
            if (buffer.length() != 0)
                buffer.append('.');
            // -->				
            String nextSegment = (String) tokenizer.nextToken();
            if (!Character.isJavaIdentifierStart(nextSegment.toCharArray()[0]))
                nextSegment = UNDERSCORE + nextSegment;
            if (isJavaKeyword(nextSegment))
                nextSegment = UNDERSCORE + nextSegment;
            buffer.append(nextSegment);

            //			buffer.append(tokenizer.nextToken());
            // <--				
        }
        Trc.exit(buffer.toString());
        return buffer.toString();
    }

    private static List getNamespaceURISegments(String namespaceURI) {
        Trc.entry(null, namespaceURI);

        // Tokenize
        List segments = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(namespaceURI, ":/");
        while (tokenizer.hasMoreTokens()) {
            segments.add(tokenizer.nextToken());
        }

        // Remove protocol
        if (!segments.isEmpty()) {
            try {
                URL url = new URL(namespaceURI);
                if (segments.get(0).equals(url.getProtocol()))
                    segments.remove(0);
            } catch (MalformedURLException exn) {
                Trc.ignoredException(exn);
            }
        }
        Trc.exit(segments);
        return segments;
    }

    private static String getBindingNamespace(Binding bindingModel) {
        Trc.entry(null, bindingModel);
        Iterator iterator = bindingModel.getExtensibilityElements().iterator();
        String returnNamespace = null;
        while (iterator.hasNext()) {
            ExtensibilityElement ee = (ExtensibilityElement) iterator.next();
            if (returnNamespace == null) {
                String namespace = ee.getElementType().getNamespaceURI();
                if (!namespace.endsWith(PHYSICALREP)
                    && !namespace.endsWith(FORMATBINDING)) {
                    returnNamespace = namespace;
                }
            }
        }
        Trc.exit(returnNamespace);
        return returnNamespace;
    }

    public static String getFormatStylePackage(Binding bindingModel) {
        Trc.entry(null, bindingModel);

        Iterator iterator = bindingModel.getExtensibilityElements().iterator();
        String formatPackageName = null;
        while (iterator.hasNext()) {
            ExtensibilityElement ee = (ExtensibilityElement) iterator.next();
            if (ee instanceof TypeMapping) {
                TypeMapping typeMapping = (TypeMapping) ee;
                formatPackageName = typeMapping.getEncoding();
                if (typeMapping.getStyle() != null)
                    formatPackageName += typeMapping.getStyle();
            }
            if (formatPackageName != null)
                break;
        }
        String s = null;
        if (formatPackageName != null) {
            //			return getJavaNameFromXMLName(formatPackageName);
            s = getPackageNameFromXMLName(formatPackageName);
        } else {
            s = formatPackageName;
        }
        Trc.exit(s);
        return s;
    }

    public static String getFormatHandlerName(
        Part part,
        Definition definition,
        Binding binding)
        throws
            java.lang.InstantiationException,
            java.lang.IllegalAccessException,
            java.lang.ClassNotFoundException {
        Trc.entry(null, part, definition, binding);

        javax.xml.namespace.QName partTypeQName = part.getTypeName();
        if (partTypeQName == null)
            partTypeQName = part.getElementName();
        if (partTypeQName == null)
            throw new ClassNotFoundException(part.getName());

        String typePackageName =
            getPackageNameFromNamespaceURI(partTypeQName.getNamespaceURI());
        String formatHandlerName = typePackageName;

        String bindingShortName =
            getPackageNameFromXMLName(
                definition.getPrefix(getBindingNamespace(binding)));
        if (bindingShortName != null)
            formatHandlerName = formatHandlerName + DOT + bindingShortName;

        if (getFormatStylePackage(binding) != null)
            formatHandlerName =
                formatHandlerName + DOT + getFormatStylePackage(binding);

        formatHandlerName =
            formatHandlerName
                + DOT
                + getJavaClassNameFromXMLName(partTypeQName.getLocalPart())
                + FORMAT_HANDLER;

        Trc.exit(formatHandlerName);
        return formatHandlerName;
    }

    public static String getJavaClassNameFromXMLName(String xmlName) {
        Trc.entry(null, xmlName);
        String s = getJavaClassNameFromXMLName(xmlName, XMLSEPARATORS);
        Trc.exit(s);
        return s;
    }

    public static String getJavaClassNameFromXMLName(
        String xmlName,
        String delims) {
        Trc.entry(null, xmlName, delims);
        StringTokenizer tokenizer = new StringTokenizer(xmlName, delims);
        StringBuffer buffer = new StringBuffer();
        while (tokenizer.hasMoreTokens()) {
            String nextSegment = (String) tokenizer.nextToken();
            if (nextSegment.length() > 0) {
                nextSegment =
                    Character.toUpperCase((nextSegment.toCharArray())[0])
                        + nextSegment.substring(1);
            }
            buffer.append(nextSegment);
        }
        String result = buffer.toString();
        if (!Character.isJavaIdentifierStart(result.toCharArray()[0]))
            result = UNDERSCORE + result;
        Trc.exit(result);
        if (isJavaKeyword(result))
            return UNDERSCORE + result;
        else
            return result;
    }

    public static String getXSDNamespaceFromPackageName(String packageName) {
    
        String result = "";
        StringTokenizer tokenizer = new java.util.StringTokenizer(packageName, ".");
        while (tokenizer.hasMoreTokens()) {
            String nextT = tokenizer.nextToken();
            result = removeUnderscores(nextT) + "." + result;
        }
        if (result.endsWith(".")) {
            return "http://" + result.substring(0, result.length() - 1) + "/";
        }
        return "http://" + result + "/";
    }
    
    /**
     * Remove any underscore (_) characters from a string
     */
    private static String removeUnderscores(String s) {
        String result = "";
        StringTokenizer tokenizer = new StringTokenizer(s, UNDERSCORE);
        while (tokenizer.hasMoreTokens()) {
            String nextT = tokenizer.nextToken();
            result += nextT;
        }
        return result;
    }

   /**
    * Get a binding operation for a portType operation.
    * 
    * @param binding the WSLD binding the operation will choosen from
    * @param portTypeOp the portType operation the binding operation 
    *         must match
    * @return the BindingOperation  
    */
   public static BindingOperation getBindingOperation(
      Binding binding,
      Operation portTypeOp) throws WSIFException {

      Trc.entry(null, binding, portTypeOp);
      BindingOperation bop;
      if ( portTypeOp == null ) {
      	bop = null;
      } else {
      	bop = getBindingOperation( 
           binding, 
           portTypeOp.getName(),
           portTypeOp.getInput()==null ? null : portTypeOp.getInput().getName(),
           portTypeOp.getOutput()==null ? null : portTypeOp.getOutput().getName() );
      }
      Trc.exit(bop);
      return bop;
   }

   /**
    * Get a binding operation for a portType operation.
    * 
    * @param binding the WSLD binding the operation will choosen from
    * @param opName the portType operation name of the wanted operation
    * @param inName the portType operation input name
    * @param outName the portType operation outpur name
    * @return the BindingOperation  
    */
   public static BindingOperation getBindingOperation(
      Binding binding,
      String opName,
      String inName,
      String outName) throws WSIFException {

      Trc.entry(null, binding, opName, inName, outName);        	
      BindingOperation op = null;
      if (binding != null && opName != null) {
         ArrayList matchingOps = new ArrayList();
         List bops = binding.getBindingOperations();
         if (bops != null) {
            for (Iterator i = bops.iterator(); i.hasNext();) {
               BindingOperation bop = (BindingOperation) i.next();
               if ( opName.equalsIgnoreCase(bop.getName()) ) {
                  matchingOps.add(bop);
               }
            }
            if (matchingOps.size() == 1) {
               op = (BindingOperation) matchingOps.get(0);
            } else if (matchingOps.size() > 1) {
               op = chooseBindingOperation(matchingOps, inName, outName);
            }
         }
      }
      Trc.exit(op);
      return op;      	
   }

   private static BindingOperation chooseBindingOperation(
      ArrayList bindingOps,
      String inName,
      String outName) throws WSIFException {
      	
      BindingOperation choosenOp = null;
      for (Iterator i = bindingOps.iterator(); i.hasNext(); ) {
         BindingOperation bop = (BindingOperation) i.next();
         String binName = (bop.getBindingInput() == null) ? 
            null : 
            bop.getBindingInput().getName();
         String boutName = (bop.getBindingOutput() == null) ?
            null : 
            bop.getBindingOutput().getName();
         if ((inName == null) ? binName == null : inName.equalsIgnoreCase(binName)) {
         	boolean outNamesMatch = true;
         	if (outName == null || outName.length() < 1) {
				outNamesMatch = (boutName == null || boutName.length() < 1);
         	} else {
         		outNamesMatch= outName.equalsIgnoreCase(boutName);
         	}
            if (outNamesMatch) {
               if ( choosenOp == null ) {
                  choosenOp = bop;
               } else {
                  throw new WSIFException( 
                     "duplicate operation in binding: " +
                     bop.getName() +
                     ":" + inName +
                     ":" + outName );
               }
            }
         }
      }
      return choosenOp;
   }

    private static boolean isJavaKeyword(String identifier) {
        if (keywordMap == null) {
            Object value = new Object();
            keywordMap = new HashMap();
            keywordMap.put("abstract", value);
            keywordMap.put("default", value);
            keywordMap.put("if", value);
            keywordMap.put("private", value);
            keywordMap.put("this", value);
            keywordMap.put("boolean", value);
            keywordMap.put("do", value);
            keywordMap.put("implements", value);
            keywordMap.put("protected", value);
            keywordMap.put("throw", value);
            keywordMap.put("break", value);
            keywordMap.put("double", value);
            keywordMap.put("import", value);
            keywordMap.put("public", value);
            keywordMap.put("throws", value);
            keywordMap.put("byte", value);
            keywordMap.put("else", value);
            keywordMap.put("instanceof", value);
            keywordMap.put("return", value);
            keywordMap.put("transient", value);
            keywordMap.put("case", value);
            keywordMap.put("extends", value);
            keywordMap.put("int", value);
            keywordMap.put("short", value);
            keywordMap.put("try", value);
            keywordMap.put("catch", value);
            keywordMap.put("final", value);
            keywordMap.put("interface", value);
            keywordMap.put("static", value);
            keywordMap.put("void", value);
            keywordMap.put("char", value);
            keywordMap.put("finally", value);
            keywordMap.put("long", value);
            keywordMap.put("strictfp", value);
            keywordMap.put("volatile", value);
            keywordMap.put("class", value);
            keywordMap.put("float", value);
            keywordMap.put("native", value);
            keywordMap.put("super", value);
            keywordMap.put("while", value);
            keywordMap.put("const", value);
            keywordMap.put("for", value);
            keywordMap.put("new", value);
            keywordMap.put("switch", value);
            keywordMap.put("continue", value);
            keywordMap.put("goto", value);
            keywordMap.put("package", value);
            keywordMap.put("synchronized", value);
            keywordMap.put("null", value);
            keywordMap.put("true", value);
            keywordMap.put("false", value);
            keywordMap.put("assert", value);
        }
        return keywordMap.containsKey(identifier);

        //	abstract    default    if            private      this
        //	boolean     do         implements    protected    throw
        //	break       double     import        public       throws
        //	byte        else       instanceof    return       transient
        //	case        extends    int           short        try
        //	catch       final      interface     static       void
        //	char        finally    long          strictfp     volatile
        //	class       float      native        super        while
        //	const       for        new           switch
        //	continue    goto       package       synchronized
        //  null        true       false         assert
    }

    /**
     * Compares two strings taking acount of a wildcard.
     * The first string is compared to the second string taking 
     * account of a wildcard character in the first string. For
     * example, wildcardCompare( "*.ibm.com", "hursley.ibm.com", '*')
     * would return true.
     */
    public static boolean wildcardCompare(String s1, String s2, char wild) {
        if (s1 == null) {
            return false;
        }
        String w = wild + "";
        return cmp(new StringTokenizer(s1, w, true), s2, w);
    }
    
    private static boolean cmp(StringTokenizer st, String s, String wild) {
    	if ( s == null || s.equals( "" ) ) {
    		return !st.hasMoreTokens();
    	}
    	if ( st.hasMoreTokens() ) {
           String s2 = st.nextToken();
           if ( wild.equals( s2 ) ) {
           	  if ( !st.hasMoreTokens() ) { 
           	     return true;   // a trailing wildcard matches anything
           	  }
           	  s2 = st.nextToken();
           	  if ( s.equals( s2 ) ) { 
           	  	 return false;   //  wildcard must be at least 1 character
           	  }
           }
           int i = s.indexOf( s2 );
           if ( i < 0 ) {
              return false;  // prefix not in s
           }
           i += s2.length();
           if ( i < s.length() ) {
              return cmp( st, s.substring( i ), wild );
           } else {
              return cmp( st, "", wild );
           }
    	}
    	return false; // no more tokens but still some s
    }
    
	/**
	 * @deprecated use org.apache.wsif.providers.ProviderUtils.isWrappedDocLiteral
	 */
	public static boolean isWrappedDocLiteral(List parts, String name) {
		return !(ProviderUtils.getWrapperPart(parts, name) == null);
	}

	/**
	 * @deprecated use org.apache.wsif.providers.ProviderUtils.getWrapperPart
	 */
	public static Part getWrappedDocLiteralPart(List parts, String operationName) {
        return ProviderUtils.getWrapperPart(parts, operationName);
	}

	/**
	 * @deprecated use org.apache.wsif.providers.ProviderUtils.unWrapPart
	 */
	public static List unWrapPart(Part p, Definition def) throws WSIFException {
		return ProviderUtils.unWrapPart(p, def);
	}

	/**
	 * Gets the WSIF WSDL Extensions Registry
	 * This calls initializeProviders to ensure all providers
	 * have registered any custom WSDL extensions.
	 * @return ExtensionRegistry   the WSIF ExtensionRegistry 
	 */
	public static ExtensionRegistry getExtensionRegistry() {
        Trc.entry(null);        	
        initializeProviders();		
        ExtensionRegistry er = WSIFServiceImpl.getCompositeExtensionRegistry();
        Trc.exit(er);
        return er; 
	}

}
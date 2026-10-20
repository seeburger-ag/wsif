/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
 * Business Machines, Inc., http://www.ibm.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.wsif.base;

import java.util.Hashtable;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.xml.WSDLLocator;
import javax.xml.namespace.QName;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.wsdl.WSIFWSDLLocatorImpl;

/**
 * Factory class used to create instances of WSIFService
 * 
 * @author Mark Whitlock
 * @author Owen Burroughs <owenb@apache.org>
 */
public class WSIFServiceFactoryImpl extends WSIFServiceFactory {

    /**
     * The service cache, or null when service caching is off. A factory is
     * typically shared by many threads (bpengine keeps one in a static field):
     * the reference is volatile and every getService reads it exactly once, so a
     * concurrent feature change can never be observed half-applied. Written only
     * under the monitor of this (setFeature / setFeatures).
     */
    private volatile WSIFServiceCache cache = null;
    private Map features = new Hashtable();

    /**
     * Create a WSIFService from WSDL document URL.
     * <br> If serviceName or serviceNS is null,
     *   then WSDL document must have exactly one service in it.
     * <br> If portTypeName or portTypeNS is null,
     *   then WSDL document must have exactly one portType in it
     *   and all ports of the selected service must
     *    implement the same portType.
     * @param wsdlLoc The URL for the wsdl's location
     * @param serviceNS The namespace of the service
     * @param serviceName The name of the service
     * @param portTypeNS The namespace of the port type
     * @param portTypeName The name of the port type
     * @return The service
     * @exception A WSIFException if an error occurs when creating the service
     */
    public WSIFService getService(
        String wsdlLoc,
        String serviceNS,
        String serviceName,
        String portTypeNS,
        String portTypeName)
        throws WSIFException {
        Trc.entry(
            this,
            wsdlLoc,
            serviceNS,
            serviceName,
            portTypeNS,
            portTypeName);

        String key = "";
        WSIFServiceCache c = cache;
        if (c != null) {
            key =
                genCacheKey(
                    wsdlLoc,
                    serviceNS,
                    serviceName,
                    portTypeNS,
                    portTypeName);
            WSIFServiceImpl cachedWSI = (WSIFServiceImpl) c.get(key);
            if (cachedWSI != null) {
                WSIFServiceImpl wsi = new WSIFServiceImpl(cachedWSI);
                Trc.exit(wsi);
                return wsi;
            }
        }

        WSIFServiceImpl wsi =
            new WSIFServiceImpl(
                wsdlLoc,
                serviceNS,
                serviceName,
                portTypeNS,
                portTypeName,
                getFeatures());

        if (c != null && !key.equals("")) {
            c.put(key, cacheTemplate(wsi));
        }

        Trc.exit(wsi);
        return wsi;
    }

    /**
     * Create a WSIF service instance from WSDL document URL
     * using a ClassLoader to find local resources.
     * <br> If serviceName or serviceNS is null,
     *   then WSDL document must have exactly one service in it.
     * <br> If portTypeName or portTypeNS is null,
     *   then WSDL document must have exactly one portType in it
     *   and all ports of the selected service must
     *    implement the same portType.
     * @param wsdlLoc The URL for the wsdl's location
     * @param cl A ClassLoader to use in locating the wsdl
     * @param serviceNS The namespace of the service
     * @param serviceName The name of the service
     * @param portTypeNS The namespace of the port type
     * @param portTypeName The name of the port type
     * @return The service
     * @exception A WSIFException if an error occurs when creating the service
     */
    public WSIFService getService(
        String wsdlLoc,
        ClassLoader cl,
        String serviceNS,
        String serviceName,
        String portTypeNS,
        String portTypeName)
        throws WSIFException {
        Trc.entry(
            this,
            wsdlLoc,
            cl,
            serviceNS,
            serviceName,
            portTypeNS,
            portTypeName);

        String key = "";
        WSIFServiceCache c = cache;
        if (c != null) {
            key =
                genCacheKey(
                    wsdlLoc,
                    serviceNS,
                    serviceName,
                    portTypeNS,
                    portTypeName);
            WSIFServiceImpl cachedWSI = (WSIFServiceImpl) c.get(key);
            if (cachedWSI != null) {
                WSIFServiceImpl wsi = new WSIFServiceImpl(cachedWSI);
                Trc.exit(wsi);
                return wsi;
            }
        }

        WSIFServiceImpl wsi =
            new WSIFServiceImpl(
                wsdlLoc,
                cl,
                serviceNS,
                serviceName,
                portTypeNS,
                portTypeName,
                getFeatures());

        if (c != null && !key.equals("")) {
            c.put(key, cacheTemplate(wsi));
        }

        Trc.exit(wsi);
        return wsi;
    }

    /**
     * Returns a new WSIFService.
     * @param def The Definition object representing the wsdl
     * @return The service
     * @exception A WSIFException if an error occurs when creating the service
     */
    public WSIFService getService(Definition def) throws WSIFException {
        Trc.entryExpandWsdl(this, new Object[] { def });
        String key = "";
        WSIFServiceCache c = cache;
        if (c != null) {
            key = genCacheKey(def, null, null);
            WSIFServiceImpl cachedWSI = (WSIFServiceImpl) c.get(key);
            if (cachedWSI != null) {
                WSIFServiceImpl wsi = new WSIFServiceImpl(cachedWSI);
                Trc.exit(wsi);
                return wsi;
            }
        }

        WSIFServiceImpl wsi = new WSIFServiceImpl(def, getFeatures());

        if (c != null && !key.equals("")) {
            c.put(key, cacheTemplate(wsi));
        }

        Trc.exit(wsi);
        return wsi;
    }

    /**
     * Returns a new WSIFService.
     * @param def The Definition object representing the wsdl
     * @param service The Service object representing the service to use
     * @return The service
     * @exception A WSIFException if an error occurs when creating the service
     */
    public WSIFService getService(Definition def, Service service)
        throws WSIFException {
        Trc.entryExpandWsdl(this, new Object[] { def, service });
        String key = "";
        WSIFServiceCache c = cache;
        if (c != null) {
            key = genCacheKey(def, service, null);
            WSIFServiceImpl cachedWSI = (WSIFServiceImpl) c.get(key);
            if (cachedWSI != null) {
                WSIFServiceImpl wsi = new WSIFServiceImpl(cachedWSI);
                Trc.exit(wsi);
                return wsi;
            }
        }

        WSIFServiceImpl wsi = new WSIFServiceImpl(def, service, getFeatures());

        if (c != null && !key.equals("")) {
            c.put(key, cacheTemplate(wsi));
        }
        Trc.exit(wsi);
        return wsi;
    }

    /**
     * Returns a new WSIFService.
     * @param def The Definition object representing the wsdl
     * @param service The Service object representing the service to use
     * @param portType The PortType object representing the port type to use
     * @return The service
     * @exception A WSIFException if an error occurs when creating the service
     */
    public WSIFService getService(
        Definition def,
        Service service,
        PortType portType)
        throws WSIFException {
        Trc.entryExpandWsdl(this, new Object[]{def, service, portType});
        String key = "";
        WSIFServiceCache c = cache;
        if (c != null) {
            key = genCacheKey(def, service, portType);
            WSIFServiceImpl cachedWSI = (WSIFServiceImpl) c.get(key);
            if (cachedWSI != null) {
                WSIFServiceImpl wsi = new WSIFServiceImpl(cachedWSI);
                Trc.exit(wsi);
                return wsi;
            }
        }

        WSIFServiceImpl wsi = new WSIFServiceImpl(def, service, portType, getFeatures());

        if (c != null && !key.equals("")) {
            c.put(key, cacheTemplate(wsi));
        }

        Trc.exit(wsi);
        return wsi;
    }

    /**
     * Returns a new WSIFService.
     * @param def The Definition object representing the wsdl
     * @param serviceNS The namespace of the service
     * @param serviceName The name of the service
     * @param portTypeNS The namespace of the port type
     * @param portTypeName The name of the port type
     * @return The service
     * @exception A WSIFException if an error occurs when creating the service
     */
    public WSIFService getService(
        Definition def,
        String serviceNS,
        String serviceName,
        String portTypeNS,
        String portTypeName)
        throws WSIFException {
        Trc.entryExpandWsdl(
            this,
            new Object[] {
                def,
                serviceNS,
                serviceName,
                portTypeNS,
                portTypeName });

        String key = "";
        WSIFServiceCache c = cache;
        if (c != null) {
            key =
                genCacheKey(
                    def,
                    serviceNS,
                    serviceName,
                    portTypeNS,
                    portTypeName);
            WSIFServiceImpl cachedWSI = (WSIFServiceImpl) c.get(key);
            if (cachedWSI != null) {
                WSIFServiceImpl wsi = new WSIFServiceImpl(cachedWSI);
                Trc.exit(wsi);
                return wsi;
            }
        }

        WSIFServiceImpl wsi =
            new WSIFServiceImpl(
                def,
                serviceNS,
                serviceName,
                portTypeNS,
                portTypeName,
                getFeatures());

        if (c != null && !key.equals("")) {
            c.put(key, cacheTemplate(wsi));
        }

        Trc.exit(wsi);
        return wsi;
    }

    /**
     * Set caching of servies on/off. The default is off. If caching is on then
     * a call to getService will first check if a service matching the parameters
     * specified has already been created and if so a reference to that instance
     * of WSIFService is returned.
     * @param on Flag to indicate whether or not caching of services should be used
     * @deprecated Use <code>setFeature(WSIFConstants.WSIF_FEATURE_SERVICE_CACHING, new Boolean(true))</code>
     * or <code>setFeature(WSIFConstants.WSIF_FEATURE_SERVICE_CACHING, new Boolean(false))</code> instead
     */
    public void cachingOn(boolean on) {
    	Trc.entry(this,on);
        setFeature(WSIFConstants.WSIF_FEATURE_SERVICE_CACHING, Boolean.valueOf(on));
        Trc.exit();
    }
   
    /**
     * @see org.apache.wsif.WSIFServiceFactory#setFeature(String, Object)
     */
    public synchronized void setFeature(String name, Object value) {
        Trc.entry(this, name, value);
        if (WSIFConstants.WSIF_FEATURE_SERVICE_CACHING.equals(name)) {
            applyCaching(value);
        } else if (WSIFConstants.WSIF_FEATURE_SERVICE_CACHE_SIZE.equals(name)) {
            applyCacheSize(value);
        }
        features.put(name, value);
        Trc.exit();
    }

    /**
     * @see org.apache.wsif.WSIFServiceFactory#setFeatures(Map)
     */
    public synchronized void setFeatures(Map map) {
        Trc.entry(this, map);
        features.clear();
        features.putAll(map);
        if (map.containsKey(WSIFConstants.WSIF_FEATURE_SERVICE_CACHING)) {
            applyCaching(map.get(WSIFConstants.WSIF_FEATURE_SERVICE_CACHING));
        }
        if (map.containsKey(WSIFConstants.WSIF_FEATURE_SERVICE_CACHE_SIZE)) {
            applyCacheSize(map.get(WSIFConstants.WSIF_FEATURE_SERVICE_CACHE_SIZE));
        }
        Trc.exit();
    }

    /**
     * Switches service caching on or off. Caller must hold the monitor of this.
     * A non-Boolean value is ignored, as before.
     */
    private void applyCaching(Object value) {
        if (value instanceof Boolean boolean1) {
            if (boolean1.booleanValue()) {
                if (cache == null) {
                    int size = 100;
                    Object tempInt = getFeature(WSIFConstants.WSIF_FEATURE_SERVICE_CACHE_SIZE);
                    if (tempInt instanceof Integer integer) {
                        size = integer.intValue();
                    }
                    cache = new WSIFServiceCache(size);
                }
            } else {
                cache = null;
            }
        }
    }

    /**
     * Resizes the cache if caching is on. Caller must hold the monitor of this.
     */
    private void applyCacheSize(Object value) {
        WSIFServiceCache c = cache;
        if (value instanceof Integer integer && c != null) {
            c.setCacheSize(integer.intValue());
        }
    }
    
    /**
     * @see org.apache.wsif.WSIFServiceFactory#getFeature(String)
     */    
    public Object getFeature(String name) {
    	return features.get(name);
    }
    
    /**
     * @see org.apache.wsif.WSIFServiceFactory#getFeatures()
     */    
    public Map getFeatures() {
    	return (Map) ((Hashtable)features).clone();
    }

    /**
     * Builds the instance stored in the service cache.
     * <p>
     * The cache must hold a private template rather than the service returned to
     * the caller: the caller mutates its instance (getPort sets up type mappings,
     * mapType, setContext) while other threads copy the cached one. The template's
     * type mappings are set up here, once, before it is published, so that every
     * copy inherits them instead of recomputing them on its first getPort.
     */
    private static WSIFServiceImpl cacheTemplate(WSIFServiceImpl wsi)
        throws WSIFException {
        WSIFServiceImpl template = new WSIFServiceImpl(wsi);
        try {
            template.setupTypeMappings();
        } catch (WSIFException e) {
            // Leave the template uninitialised; each copy then reports the
            // problem from its own getPort, exactly as without caching.
            Trc.ignoredException(e);
        }
        return template;
    }

    private String genCacheKey(
        Definition def,
        Service service,
        PortType portType) {
        Trc.entry(this, def, service, portType);

        String db =
            (def != null && def.getDocumentBaseURI() != null)
                ? def.getDocumentBaseURI()
                : "null";
        QName serviceName = (service != null) ? service.getQName() : null;
        String sn = (serviceName != null) ? serviceName.toString() : "null";
        QName portTypeName = (portType != null) ? portType.getQName() : null;
        String ptn = (portTypeName != null) ? portTypeName.toString() : "null";
        StringBuffer key = new StringBuffer();
        key.append("D=");
        key.append(db);
        key.append("S=");
        key.append(sn);
        key.append("P=");
        key.append(ptn);

        String ret = key.toString();
        // If no distinguishable information is available then don't add to cache
        if (ret.equals("D=nullS=nullP=null")) 
            ret = "";
        Trc.exit(ret);
        return ret;
    }

    private String genCacheKey(
        String wsdlLoc,
        String serviceNS,
        String serviceName,
        String portTypeNS,
        String portTypeName) {
        Trc.entry(
            this,
            wsdlLoc,
            serviceNS,
            serviceName,
            portTypeNS,
            portTypeName);

        StringBuffer key = new StringBuffer();
        if (wsdlLoc == null)
            wsdlLoc = "";
        if (serviceNS == null)
            serviceNS = "";
        if (serviceName == null)
            serviceName = "";
        if (portTypeNS == null)
            portTypeNS = "";
        if (portTypeName == null)
            portTypeName = "";
        key.append("W=");
        key.append(wsdlLoc);
        key.append("SN=");
        key.append(serviceNS);
        key.append("SS=");
        key.append(serviceName);
        key.append("PN=");
        key.append(portTypeNS);
        key.append("PS=");
        key.append(portTypeName);

        String ret = key.toString();
        // If no distinguishable information is available then don't add to cache
        if (ret.equals("W=SN=SS=PN=PS="))
            ret = "";
        Trc.exit(ret);
        return ret;
    }

    private String genCacheKey(
        Definition def,
        String serviceNS,
        String serviceName,
        String portTypeNS,
        String portTypeName) {
        Trc.entry(this, def, serviceNS, serviceName, portTypeNS, portTypeName);

        StringBuffer key = new StringBuffer();
        String db =
            (def != null && def.getDocumentBaseURI() != null)
                ? def.getDocumentBaseURI()
                : "null";
        if (serviceNS == null)
            serviceNS = "";
        if (serviceName == null)
            serviceName = "";
        if (portTypeNS == null)
            portTypeNS = "";
        if (portTypeName == null)
            portTypeName = "";
        key.append("D=");
        key.append(db);
        key.append("SN=");
        key.append(serviceNS);
        key.append("SS=");
        key.append(serviceName);
        key.append("PN=");
        key.append(portTypeNS);
        key.append("PS=");
        key.append(portTypeName);

        String ret = key.toString();
        // If no distinguishable information is available then don't add to cache
        if (ret.equals("D=nullSN=SS=PN=PS="))
            ret = "";
        Trc.exit(ret);
        return ret;
    }
}

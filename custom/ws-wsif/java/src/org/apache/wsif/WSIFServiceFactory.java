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

package org.apache.wsif;

import java.util.Map;
import javax.wsdl.Definition;
import javax.wsdl.PortType;
import javax.wsdl.Service;

import org.apache.wsif.base.WSIFServiceFactoryImpl;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.util.WSIFUtils;

/**
 * Abstract factory class to create instances of WSIFService. Call newInstance
 * to get a instance of the factory.
 * 
 * @author Mark Whitlock
 * @author Owen Burroughs
 */
public abstract class WSIFServiceFactory {

    /** 
     * Creates a new instance of an implementation the abstract
     * WSIFServiceFactory class.
     */
    public static WSIFServiceFactory newInstance() {
        Trc.entry(null);

        WSIFServiceFactoryImpl wsf = new WSIFServiceFactoryImpl();

        // Create the simple types map for use by other WSIF classes
        WSIFUtils.createSimpleTypesMap();

        Trc.exit(wsf);
        return wsf;
    }

    /**
     * Create a WSIFService from WSDL document URL.
     * <br> If serviceName or serviceNS is null,
     *   then WSDL document must have exactly one service in it.
     * <br> If portTypeName or portTypeNS is null,
     *   then WSDL document must have exactly one portType in it
     *   and all ports of the selected service must
     *    implement the same portType.
     */
    public abstract WSIFService getService(
        String wsdlLoc,
        String serviceNS,
        String serviceName,
        String portTypeNS,
        String portTypeName)
        throws WSIFException;

    /**
     * Create a WSIF service instance from WSDL document URL
     * using a ClassLoader to find local resources.
     * <br> If serviceName or serviceNS is null,
     *   then WSDL document must have exactly one service in it.
     * <br> If portTypeName or portTypeNS is null,
     *   then WSDL document must have exactly one portType in it
     *   and all ports of the selected service must
     *    implement the same portType.
     */
    public abstract WSIFService getService(
        String wsdlLoc,
        ClassLoader cl,
        String serviceNS,
        String serviceName,
        String portTypeNS,
        String portTypeName)
        throws WSIFException;

    /**
     * Returns a new WSIFService.
     */
    public abstract WSIFService getService(Definition def)
        throws WSIFException;

    /**
     * Returns a new WSIFService.
     */
    public abstract WSIFService getService(Definition def, Service service)
        throws WSIFException;

    /**
     * Returns a new WSIFService.
     */
    public abstract WSIFService getService(
        Definition def,
        Service service,
        PortType portType)
        throws WSIFException;

    /**
     * Returns a new WSIFService.
     */
    public abstract WSIFService getService(
        Definition def,
        String serviceNS,
        String serviceName,
        String portTypeNS,
        String portTypeName)
        throws WSIFException;
     
    /**
     * Set caching on services on/off. Off is the default
     * @param on Flag to indicate whether or not caching of services should be used
     * @deprecated Use <code>setFeature(WSIFConstants.WSIF_FEATURE_SERVICE_CACHING, new Boolean(true))</code>
     * or <code>setFeature(WSIFConstants.WSIF_FEATURE_SERVICE_CACHING, new Boolean(false))</code> instead
     */    
    public void cachingOn(boolean on) {
    }       
  
    /**
     * Set a feature on the WSIFServiceFactory. The names of supported features are stored as constants
     * in the {@link WSIFConstants} class. The names of these constants have a convention of starting 
     * <code>WSIF_FEATURE_</code>. 
     * For more information about individual features, see the field details for the feature constants.
     * <br><br><b>Note:</b> features should be set before calls to the getService methods.<br>
     * @param name The name of the feature to set
     * @param value The value of the feature
     */
    public abstract void setFeature(String name, Object value);

    /**
     * Set features on the WSIFServiceFactory. Calling this method will replace the currently set features
     * with those configured in the Map passed in.
     * The names of supported features are stored as constants
     * in the {@link WSIFConstants} class. The names of these constants have a convention of starting 
     * <code>WSIF_FEATURE_</code>. 
     * For more information about individual features, see the field details for the feature constants.
     * <br><br><b>Note:</b> features should be set before calls to the getService methods.<br>
     * @param map A Map containing all the features to set on the factory
     */    
    public abstract void setFeatures(Map map);

    /**
     * Get the map of features currently being used by the factory.
     * @return The map of features
     */    
    public abstract Map getFeatures();

    /**
     * Get the value for a feature currently being used by the factory.
     * @return The feature value
     */    
    public abstract Object getFeature(String name);                     
}

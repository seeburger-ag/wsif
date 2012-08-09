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

package org.apache.wsif;

import java.util.Iterator;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;

/**
 * A WSIFService is a factory via which WSIFPorts
 * are retrieved. This follows the J2EE design pattern of accessing
 * resources (WSIFPorts, in this case) via a factory which
 * is retrieved from the context in which the application is running.
 * When WSIF is hosted in an app server, the container can manage
 * service invocation details by providing a factory implementation
 * that follows the app servers wishes and guidelines.
 *
 * The factory is assumed to be for a specific portType; i.e.,
 * the factory knows how to factor WSIFPorts for a given portType.
 * As such the getPort() methods do not take portType arguments.
 *
 * @author Paul Fremantle
 * @author Michael Beisiegel
 * @author Sanjiva Weerawarana
 * @author Aleksander Slominski
 */
public interface WSIFService {
    /**
     * Returns an appropriate WSIFPort for the portType that this factory
     * supports. If the service had multiple ports, which one is returned 
     * depends on the specific factory - the factory implementation may 
     * use whatever heuristic it feels like to select an "appropriate" one.
     *
     * @return the new WSIFPort
     * @exception WSIFException if a suitable port cannot be located.
     */
    public WSIFPort getPort() throws WSIFException;

    /**
     * Returns a WSIFPort for the indicated port. 
     * 
     * @param portName name of the port (local part of the name). 
     * @return the new WSIFPort
     * @exception WSIFException if the named port is not known or available
     */
    public WSIFPort getPort(String portName) throws WSIFException;

    /**
     * Get the dynamic proxy that will implement an interface for a port
     * 
     * @param portName the name of the port
     * @param iface the interface that the stub will implement
     * @return a stub (a dynamic proxy)
     * @exception WSIFException if something goes wrong
     */
    public Object getStub(String portName, Class iface) throws WSIFException;

    /**
     * Get the dynamic proxy that will implement an interface for a port
     * This method will attempt to use the preferred port if set otherwise
     * it will use the first available port.
     * 
     * @param portName the name of the port
     * @return a stub (a dynamic proxy)
     * @exception WSIFException if something goes wrong
     */
    public Object getStub(Class iface) throws WSIFException;

    /**
     * Inform WSIF that a particular XML type (referred to in the WSDL) 
     * actually maps to a particular Java class. Use this method when there
     * is no schema definition for this type, or when the mapping is 
     * sufficiently complicated that WSIF does not understand the schema
     * definition. Calling this method overrides whatever schema is present
     * in the WSDL for this type.
     * @param xmlType the fully qualified XML type name
     * @param javaType the java class that this type maps to
     * @exception WSIFException if something goes wrong
     */
    public void mapType(QName xmlType, Class javaType) throws WSIFException;

    /**
     * Add an association between a namespace URI and and a Java package.
     * This enables WSIF to map schema definitions to real java classes.
     * @param namespace The namespace URI
     * @param packageName The full package name
     * @exception WSIFException if something goes wrong
     */
    public void mapPackage(String namespace, String packageName)
        throws WSIFException;

    /**
     * Set the preferred port
     * @param portName The name of the port to use
     * @exception WSIFException if something goes wrong
     */
    public void setPreferredPort(String portName) throws WSIFException;

    /**
     * Get the names of the available ports
     * @return Iterator for list of available port names.
     * @exception WSIFException if something goes wrong
     */
    public Iterator getAvailablePortNames() throws WSIFException;

    /**
     * Get the Definition object representing the wsdl document
     * @return The Definition object
     */
    public Definition getDefinition();
    
    /**
     * Gets the context information for this WSIFService.
     * @return context
     */
    public WSIFMessage getContext() throws WSIFException ;

    /**
     * Sets the context information for this WSIFService.
     * @param WSIFMessage the new context information
     */
    public void setContext(WSIFMessage context);

}

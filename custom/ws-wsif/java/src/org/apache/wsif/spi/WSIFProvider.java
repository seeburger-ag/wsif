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

package org.apache.wsif.spi;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.providers.WSIFDynamicTypeMap;

/**
 * A WSIFProvider is reponsible for translating  WSDL port model
 * into a dynamic WSIF port .
 *
 * <b>NOTE:</b> providers MUST be stateless
 *   it MUST be safe to call provider methods in multiple threads.
 *
 * @author Alekander Slominski
 * @author Owen Burroughs <owenb@apache.org>
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>

 */
public interface WSIFProvider {

    /**
     * For the given WSDL definition, service and port
     * try to provide dynamic port,
     * or return null if this provider can not do it.
     * It is required to pass definition and service in addition to port
     *   as in current WSDL4J it is not posssible to retrieve service to
     *   which port belongs and definition in which it was defined.
     */
    public WSIFPort createDynamicWSIFPort(
        Definition def,
        Service service,
        Port port,
        WSIFDynamicTypeMap typeMap)
        throws WSIFException;

    /**
     * Returns the WSDL namespace URIs of any bindings this provider supports.
     * The assumtion is made that the provider supports all combintations of
     * binding and address namespaces returned by this and the 
     * getAddressNamespaceURIs method.
     * @return an array of all binding namespaces supported by this provider
     */
    public String[] getBindingNamespaceURIs();

    /**
     * Returns the WSDL namespace URIs of any port addresses this provider supports.
     * The assumtion is made that the provider supports all combintations of
     * binding and address namespaces returned by this and the 
     * getBindingNamespaceURIs method.
     * @return an array of all address namespaces supported by this provider
     */
    public String[] getAddressNamespaceURIs();
}
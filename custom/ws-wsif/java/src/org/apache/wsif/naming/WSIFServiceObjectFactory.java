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

package org.apache.wsif.naming;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.logging.Trc;

/**
 * This is an object factory which creates:<br>
 * <ul>
 * <li>instances of WSIFService from a javax.naming.Reference object 
 * representing the service.</li>
 * <li>instances of a service stub object from a javax.naming.Reference object 
 * representing the service .</li>
 * </ul>
 * 
 * @author Owen Burroughs <owenb@apache.org>
 */
public class WSIFServiceObjectFactory implements ObjectFactory {

    // Required no argument constructor
    public WSIFServiceObjectFactory() {
        Trc.entry(this);
        Trc.exit();
    }

    /**
     * Instantiates and returns a WSIFService based on information
     * from the Reference object.
     * @param obj The possibly null object containing location or 
     * reference information that can be used in creating an object.
     * @param name The name of this object relative to ctx, or null 
     * if no name is specified
     * @param context The context relative to which the name parameter 
     * is specified, or null if name is relative to the default initial context.
     * @param env The possibly null environment that is used in creating the object.
     * @return A WSIFService or null if this factory cannot create the type of object
     * required given the information available.
     */
    public Object getObjectInstance(
        Object obj,
        Name name,
        Context context,
        Hashtable env)
        throws Exception {
        Trc.entry(this, obj, name, context, env);

        // Check that obj is a Reference object, if not we can't use it.	
        if (obj instanceof Reference && obj != null) {
            Reference ref = (Reference) obj;
            if (ref.getClassName().equals(WSIFServiceRef.class.getName())) {
                String wsdlLoc = resolveString(ref.get("wsdlLoc"));
                String serviceNS = resolveString(ref.get("serviceNS"));
                String serviceName = resolveString(ref.get("serviceName"));
                String portTypeNS = resolveString(ref.get("portTypeNS"));
                String portTypeName = resolveString(ref.get("portTypeName"));

                if (wsdlLoc != null) {
                    WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
                    WSIFService service =
                        factory.getService(wsdlLoc, serviceNS, serviceName, portTypeNS, portTypeName);
                    Trc.exit(service);
                    return service;
                }
            } else if (ref.getClassName().equals(WSIFServiceStubRef.class.getName())) {
                String wsdlLoc = resolveString(ref.get("wsdlLoc"));
                String serviceNS = resolveString(ref.get("serviceNS"));
                String serviceName = resolveString(ref.get("serviceName"));
                String portTypeNS = resolveString(ref.get("portTypeNS"));
                String portTypeName = resolveString(ref.get("portTypeName"));
                String preferredPort = resolveString(ref.get("preferredPort"));
                String className = resolveString(ref.get("className"));

                if (wsdlLoc != null) {
                    WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
                    WSIFService service =
                        factory.getService(wsdlLoc, serviceNS, serviceName, portTypeNS, portTypeName);
                    Class iface =
                        Class.forName(className, true, Thread.currentThread().getContextClassLoader());
                    Object stub = service.getStub(preferredPort, iface);
                    Trc.exit(stub);
                    return stub;
                }
            }
        }
        // Cannot create a WSIFService or stub from the information available 
        // so return null.
        Trc.exit();
        return null;
    }

    private String resolveString(RefAddr a) {
        String e = "";
        String s = (String) a.getContent();
        return (e.equals(s)) ? null : s;
    }
}
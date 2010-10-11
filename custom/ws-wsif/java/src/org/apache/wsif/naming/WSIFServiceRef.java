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

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;
import org.apache.wsif.logging.Trc;

/**
 * This is a lightweight object which provides a reference for a WSIFService. 
 * When passed to Context.bind(), the getReference() method is invoked and the 
 * resulting Reference object is stored in the directory by JNDI.
 *  
 * @author Owen Burroughs <owenb@apache.org>
 */
public class WSIFServiceRef implements Referenceable {

    String wsdlLoc;
    String serviceNS;
    String serviceName;
    String portTypeNS;
    String portTypeName;

    /**
     * Constructor that takes all necessary information needed to create a 
     * WSIFService.
     * @param wsdl The location of the wsdl file
     * @param sNS The namespace for the service as specified in the wsdl
     * @param sName The name of the service required, as specified in the wsdl
     * @param ptNS The namespace of the port type required, as specified in the wsdl
     * @param ptName The name of the port type required, as specified in the wsdl
     */
    public WSIFServiceRef(
            String wsdl,
            String sNS,
            String sName,
            String ptNS,
            String ptName) {
        Trc.entry(this, wsdl, sNS, sName, ptNS, ptName);

        wsdlLoc = wsdl;
        serviceNS = sNS;
        serviceName = sName;
        portTypeNS = ptNS;
        portTypeName = ptName;
        if (Trc.ON)
            Trc.exit(deep());
    }

    /**
     * Method to create and return a Reference object for the service.
     * @return A Reference object containing the information required to create a 
     * WSIFService and return it, when a lookup is performed on the service using
     * JNDI.
     */
    public Reference getReference() throws NamingException {
        Trc.entry(this);

        Reference ref =
            new Reference(
                WSIFServiceRef.class.getName(),
                WSIFServiceObjectFactory.class.getName(),
                null);
        ref.add(new StringRefAddr("wsdlLoc", wsdlLoc));
        ref.add(new StringRefAddr("serviceNS", serviceNS));
        ref.add(new StringRefAddr("serviceName", serviceName));
        ref.add(new StringRefAddr("portTypeNS", portTypeNS));
        ref.add(new StringRefAddr("portTypeName", portTypeName));

        Trc.exit(ref);
        return ref;
    }

    public String deep() {
        String buff = "";
        try {
            buff = new String(this.toString() + "\n");
            buff += "wsdlLoc: " + wsdlLoc;
            buff += " serviceNS: " + serviceNS;
            buff += " serviceName: " + serviceName;
            buff += " portTypeNS: " + portTypeNS;
            buff += " portTypeName: " + portTypeName;
        } catch (Exception e) {
            Trc.exceptionInTrace(e);
        }
        return buff;
    }
}
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

package jndi;

import java.util.StringTokenizer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import org.apache.wsif.naming.WSIFServiceRef;
import org.apache.wsif.naming.WSIFServiceStubRef;

/**
 * A JNDI helper class for binding and unbinding
 * services and stubs
 * 
 * @author Owen Burroughs <owenb@apache.org>
 **/

public class WSIFJndiHelper {
    private static Context ctx;

	/**
	 * Given the elements of a WSIFServiceRef, create and bind the
	 * service using JNDI under the specified name.
     * @param wsdl The location of the wsdl file
     * @param sNS The namespace for the service as specified in the wsdl
     * @param sName The name of the service required, as specified in the wsdl
     * @param ptNS The namespace of the port type required, as specified in the wsdl
     * @param ptName The name of the port type required, as specified in the wsdl
     * @param jndiName The full JNDI name under which the service will be bound
     * @exception A NamingException thrown if an error occured when working with JNDI
	 */
    public static void bindService(
	        String wsdl,
	        String sNS,
	        String sName,
	        String ptNS,
	        String ptName,
	        String jndiName)
	        throws NamingException {
        recursiveBind(jndiName, new WSIFServiceRef(wsdl, sNS, sName, ptNS, ptName));
    }

	/**
	 * Given a WSIFServiceRef, create and bind the
	 * service using JNDI under the specified name.
     * @param ref A WSIFServiceRef object reference for the service
     * @param jndiName The full JNDI name under which the service will be bound
     * @exception A NamingException thrown if an error occured when working with JNDI
	 */
    public static void bindService(WSIFServiceRef ref, String jndiName)
	        throws NamingException {
        recursiveBind(jndiName, ref);
    }

	/**
	 * Given the elements of a WSIFServiceStubRef, create and bind the
	 * stub using JNDI under the specified name.
     * @param wsdl The location of the wsdl file
     * @param sNS The namespace for the service as specified in the wsdl
     * @param sName The name of the service required, as specified in the wsdl
     * @param ptNS The namespace of the port type required, as specified in the wsdl
     * @param ptName The name of the port type required, as specified in the wsdl
     * @param portName The name of the preferred port to use
     * @param cls The fully qualified name of the interface class for the stub 
     * @param jndiName The full JNDI name under which the service will be bound
     * @exception A NamingException thrown if an error occured when working with JNDI
	 */
    public static void bindStub(
	        String wsdl,
	        String sNS,
	        String sName,
	        String ptNS,
	        String ptName,
	        String portName,
	        String cls,
	        String jndiName)
	        throws NamingException {
        recursiveBind(
            jndiName,
            new WSIFServiceStubRef(wsdl, sNS, sName, ptNS, ptName, portName, cls));
    }

	/**
	 * Given a WSIFServiceStubRef, create and bind the
	 * stub using JNDI under the specified name.
     * @param ref A WSIFServiceRef object reference for the service
     * @param jndiName The full JNDI name under which the service will be bound
     * @exception A NamingException thrown if an error occured when working with JNDI
	 */
    public static void bindStub(WSIFServiceStubRef ref, String jndiName)
	        throws NamingException {
        recursiveBind(jndiName, ref);
    }

	/**
	 * A helper method to unbind a service or stub using JNDI. Any subcontexts left
	 * empty by the unbinding will be destoyed.
     * @param name The full JNDI name of service or stub to be unbound.
     * @exception A NamingException thrown if an error occured when working with JNDI
	 */
    public static void unbindServiceOrStub(String name) throws NamingException {
        recursiveUnbind(name);
    }

	/**
	 * A helper method to recursively bind an object using JNDI. Any subcontexts
	 * required that do not currently exist will be created.
     * @param name The full JNDI name under which the object will be bound.
     * @param obj The object to bind
     * @exception A NamingException thrown if an error occured when working with JNDI
	 */
    public static void recursiveBind(String name, Object obj)
	        throws NamingException {
        String[] tokens = parseString(name);
        Context startingContext = getInitialContext();
        for (int i = 0; i < tokens.length - 1; i++) {
            try {
                startingContext.createSubcontext(tokens[i]);
            } catch (NameAlreadyBoundException nab) {
            }
        }
        startingContext.bind(name, obj);
    }

	/**
	 * A helper method to unbind an object using JNDI. Any subcontexts left
	 * empty by the unbinding will be destoyed.
     * @param name The full JNDI name of object to be unbound.
     * @exception A NamingException thrown if an error occured when working with JNDI
	 */
    public static void recursiveUnbind(String name) throws NamingException {
        String[] tokens = parseString(name);
        Context startingContext = getInitialContext();
        startingContext.unbind(name);

        for (int i = tokens.length - 2; i >= 0; i--) {
            startingContext.destroySubcontext(tokens[i]);
        }
    }

    private static String[] parseString(String s) {
        StringTokenizer st = new StringTokenizer(s, "/");
        int i = st.countTokens();
        int j = 0;
        String[] tokens = new String[i];
        while (st.hasMoreTokens()) {
            if (j > 0) {
                tokens[j] = tokens[j - 1] + "/" + st.nextToken();
            } else {
                tokens[j] = st.nextToken();
            }
            j++;
        }
        return tokens;
    }

    /**
     * Gets the current InitialContext
     * @return Returns a Context
     */
    public static Context getInitialContext() throws NamingException {
        if (ctx == null)
            ctx = new InitialContext();
        return ctx;
    }

    /**
     * Sets the InitialContext to be used
     * @param ctx The InitialContext
     */
    public static void setInitialContext(Context c) {
        ctx = c;
    }
}
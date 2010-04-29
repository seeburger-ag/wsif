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


package org.apache.wsif.providers.jca;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.naming.*;
import javax.resource.cci.ConnectionFactory;
import javax.wsdl.*;
import javax.wsdl.Binding;
import javax.wsdl.extensions.ExtensibilityElement;

import org.apache.wsif.util.WSIFUtils;
import org.apache.wsif.wsdl.extensions.format.TypeMapping;
import org.apache.wsif.format.WSIFFormatHandler;


/**
 * A utility class with methods simplifying the JNDI lookup of a ConnectionFactory.
 * 
 * @author Piotr Przybylski <piotrp@ca.ibm.com>
 */


public class WSIFUtils_JCA extends WSIFUtils{

	private static final long serialVersionUID = 1L;
	private static final String DOT = ".";
	private static final String lookupPrefix = "java:comp/env/";
	private static final String emptyString = "";

	/**
	 * Returns the default lookup name created from the Service namespace,
	 * Service name and Port name. The default lookup name is used if there is no explicit lookup
	 * name value specified in the Resource Adapter address extensibility element. 
	 * 
	 * @param service
	 * @param port
	 * @return String
	 */
	public static String getJNDILookupName(Service service, Port port) {
		
		String jndiName = getPackageNameFromNamespaceURI(service.getQName().getNamespaceURI());
		if(!jndiName.endsWith(DOT))
			jndiName = jndiName + DOT;
		jndiName = jndiName.replace('.', '/');
		jndiName = jndiName + WSIFUtils_JCA.getJavaNameFromXMLName(service.getQName().getLocalPart());
		jndiName = jndiName + WSIFUtils_JCA.getJavaNameFromXMLName(port.getName());
		return jndiName;
		
	}



	/**
	 * Returns a Connection Factory from the JNDI context. The order of search 
	 * is to use the component's local naming context (java:comp/env/) first, then the global naming context.
	 * If the lookup fails or the lookup result does not match <code>res_type</code>, null is returned.
	 * 
	 * @param res_ref_name
	 * @param res_type
	 * @return ConnectionFactory
	 */
	public static ConnectionFactory lookupConnectionFactory(String res_ref_name, String res_type){
		
		ConnectionFactory connectionFactory = null;
		Context ctx = null;
		try {
			// Create the initial context
			ctx = new InitialContext();
			if (ctx == null) {
				return null;
			}
			connectionFactory = (ConnectionFactory) ctx.lookup(lookupPrefix + res_ref_name);

			if (connectionFactory == null) {
				throw new NamingException();
			}

			// Verify that the retrieved factory is of type "res_type"
			try {
				if (!(Class.forName(res_type, false, Thread.currentThread().getContextClassLoader()).isInstance(connectionFactory))) {
					return null;
				}
			}
			catch (ClassNotFoundException exn5) {
				return null;
			}

			ctx.close();
		}
		catch (NamingException exn2) {
			try {
				if (ctx == null) {
					return null;
				}
				// Try lookup without lookup prefix (java:comp/env/)
				connectionFactory = (ConnectionFactory) ctx.lookup(res_ref_name);

				if (connectionFactory == null) {
					return null;
				}
				// Verify that the retrieved factory is of type "res_type"
				try {
					if (!(Class.forName(res_type, false, Thread.currentThread().getContextClassLoader()).isInstance(connectionFactory))) {
						return null;
					}
				}
				catch (ClassNotFoundException exn5) {
					return null;
				}
				ctx.close();
			}
			catch (NamingException exn3) {
				return null;
			}
		}
		return connectionFactory;	
	}

}

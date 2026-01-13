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

package org.apache.wsif.providers.soap.apacheaxis;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPBinding;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.base.WSIFServiceImpl;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.providers.WSIFDynamicTypeMap;
import org.apache.wsif.spi.WSIFProvider;

/**
 * @author Owen Burroughs <owenb@apache.org>
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class WSIFDynamicProvider_ApacheAxis implements WSIFProvider {

    private static final boolean axisAvailable = isAXISAvailable();
	private static final boolean jmsAvailable = isJMSAvailable();
	private static final String[] bindings = setUpBindingNamespaceURIs();
	private static final String[] addresses = setUpAddressNamespaceURIs();

	/**
	 * Construct a new AXIS provider 
	 */
	public WSIFDynamicProvider_ApacheAxis() {
		Trc.entry(this);
		if (axisAvailable && jmsAvailable) {
			WSIFServiceImpl.addExtensionRegistry(
				new org.apache.wsif.wsdl.extensions.jms.JMSExtensionRegistry());
		}
		Trc.exit();
	}

	/**
	 * Create a new AXIS WSIFPort
	 * @see WSIFProvider.createDynamicWSIFPort
	 */
	public WSIFPort createDynamicWSIFPort(
		Definition definition,
		Service service,
		Port port,
		WSIFDynamicTypeMap wsifdynamictypemap)
		throws WSIFException {
		Trc.entry(this, definition, service, port, wsifdynamictypemap);

		Binding binding = port.getBinding();
		List list = binding.getExtensibilityElements();

		WSIFPort wp = null;
		for (Iterator i = list.iterator();(i.hasNext() && wp == null);) {
			Object o = i.next();
			if (o instanceof SOAPBinding pBinding) {
				wp = new WSIFPort_ApacheAxis(
						definition,
						port,
						pBinding,
						wsifdynamictypemap);
			}
		}

		Trc.exit(wp);
		return wp;
	}

	/**
	 * Returns the WSDL namespace URIs of any bindings this provider supports.
	 * @return an array of all binding namespaces supported by this provider
	 */
	public String[] getBindingNamespaceURIs() {
		Trc.entry(this);
		Trc.exit(bindings);
		return bindings;
	}

	/**
	 * Returns the WSDL namespace URIs of any port addresses this provider supports.
	 * @return an array of all address namespaces supported by this provider
	 */
	public String[] getAddressNamespaceURIs() {
		Trc.entry(this);
		Trc.exit(addresses);
		return addresses;
	}

	/**
	 * Sets up the binding namespace URIs this provider supports.
	 */
	private static String[] setUpBindingNamespaceURIs() {
		String[] bindings;
		if (axisAvailable) {
			bindings =
				new String[] { WSIFAXISConstants.SOAP_BINDING_NAMESPACE };
		} else {
			bindings = new String[0];
		}
		Trc.event("available binding namespace URIs: ", bindings);
		return bindings;
	}

	/**
	 * Sets up the address namespace URIs this provider supports.
	 */
	private static String[] setUpAddressNamespaceURIs() {
		ArrayList l = new ArrayList();
		if (isAXISAvailable()) {
			l.add(WSIFAXISConstants.SOAP_BINDING_NAMESPACE);
		}
		if (jmsAvailable) {
			l.add(WSIFAXISConstants.JMS_NS_URI);
		}
		String[] addresses = new String[l.size()];
		for (int i = 0; i < l.size(); i++) {
			addresses[i] = (String) l.get(i);
		}
		Trc.event("available address namespace URIs: ", addresses);
		return addresses;
	}

	/**
	 * Checks if the axis.jar is available in the Java CLASSPATH
	 */
	private static boolean isAXISAvailable() {
		return isClassAvailable(WSIFAXISConstants.CLASS_IN_AXIS_JAR);
	}

	/**
	 * Checks if the JMS API jar is available in the Java CLASSPATH
	 */
	private static boolean isJMSAvailable() {
		return isClassAvailable(WSIFAXISConstants.CLASS_IN_JMS_JAR);
	}

	/**
	 * Checks if a class is available in the Java CLASSPATH
	 */
	private static boolean isClassAvailable(final String className) {
		Class c =
			(Class) AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				try {
					return Class.forName(
						className,
						true,
						Thread.currentThread().getContextClassLoader());
				} catch (Throwable ignored) {
					Trc.ignoredException(ignored);
				}
				return null;
			}
		});
		return c != null;
	}

}
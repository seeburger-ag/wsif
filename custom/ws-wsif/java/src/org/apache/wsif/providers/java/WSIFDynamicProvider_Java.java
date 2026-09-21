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

package org.apache.wsif.providers.java;

import java.util.Iterator;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.providers.WSIFDynamicTypeMap;
import org.apache.wsif.spi.WSIFProvider;
import org.apache.wsif.wsdl.extensions.java.JavaBinding;

/**
 * Java specific provider of dynamic WSDL invocations.
 * <p>
 * The WSIF Java Provider allows WSIF to invoke Java classes and JavaBeans.
 * This is designed to allow customers in a 'thin-client' environment such as
 * a JVM or Tomcat test runtime to define 'shortcuts' to local Java code. 
 * <p>
 * The Java binding exploits the format binding for type mapping. 
 * The format binding allows WSDL to define the mapping between XML Schema 
 * types and Java types.
 * <p>
 * The Java provider requires the targeted Java classes to be in the 
 * classpath of the client. The Java method is invoked synchronously, 
 * in-process, in-thread, with the current thread and ORB contexts.
 * The Java provider is not transactional.
 * <p>
 * The binding extends WSDL with the following extensibility elements:
 * <p>
 * <pre>
 * &lt;definitions .... &gt; 
 * &lt;!-- Java binding --&gt;
 *    &lt;binding ... &gt;
 *        &lt;java:binding/&gt; 
 *        &lt;format:typeMapping style="uri" encoding="..."/&gt;? 
 *            &lt;format:typeMap name="qname" formatType="nmtoken"/&gt;* 
 *        &lt;/format:typeMapping&gt; 
 *        &lt;operation&gt;* 
 *            &lt;java:operation 
 *                method="nmtoken" 
 *                parameterOrder="nmtoken"? 
 *                methodType="instance|static|constructor"?
 *                returnPart="nmtoken"? /&gt;? 
 *            &lt;input name="nmtoken"? /&gt;? 
 *            &lt;output name="nmtoken"? /&gt;? 
 *            &lt;fault name="nmtoken"? /&gt;? 
 *        &lt;/operation&gt; 
 *    &lt;/binding&gt; 
 *     &lt;service ... &gt; 
 *        &lt;port&gt;* 
 *             &lt;java:address 
 *                 class="nmtoken" 
 *                 archive="uri"? 
 *                 classloader="nmtoken"? /&gt; 
 *        &lt;/port&gt; 
 *    &lt;/service&gt; 
 * &lt;/definitions&gt; 
 * </pre>
 * <p>
 * @author <a href="mailto:gpfau@de.ibm.com">Gerhard Pfau</a>
 * @author <a href="mailto:owenb@apache.org">Owen Burroughs</a> 
 * @author <a href="mailto:antelder@apache.org">Ant Elder</a> 
 * @author <a href="mailto:hughesj@apache.org">Jeremy Hughes</a> 
 * @author <a href="mailto:whitlock@apache.org">Mark Whitlock</a> 
 */
public class WSIFDynamicProvider_Java implements WSIFProvider {
    private static final String[] supportedBindingNamespaceURIs =
        { "http://schemas.xmlsoap.org/wsdl/java/" };

    private static final String[] supportedAddressNamespaceURIs =
        { "http://schemas.xmlsoap.org/wsdl/java/" };

    public WSIFDynamicProvider_Java() {
        Trc.entry(this);
        Trc.exit();
    }

    /**
     * Check if WSDL port has Java binding and if successful try
     * to create Java port instance.
     */
    public WSIFPort createDynamicWSIFPort(
        Definition def,
        Service service,
        Port port,
        WSIFDynamicTypeMap typeMap)
        throws WSIFException {
        Trc.entry(this, def, service, port, typeMap);

        // check that Port binding has Java binding extensibility element
        Binding binding = port.getBinding();
        List exs = binding.getExtensibilityElements();
        for (Iterator i = exs.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof JavaBinding) {
                // if so try to create Java dynamic port instance
                WSIFPort wp = new WSIFPort_Java(def, port, typeMap);
                Trc.exit(wp);
                return wp;
            }
        }

        // otherwise return null (so other providers can be checked)
        Trc.exit();
        return null;
    }

    /**
     * Returns the WSDL namespace URIs of any bindings this provider supports.
     * <p>
     * A defensive copy is returned: <code>static final</code> only freezes the array
     * reference, not its contents, so handing out the shared instance would let any
     * caller corrupt the provider lookup for every thread in the JVM.
     *
     * @return an array of all binding namespaces supported by this provider
     */
    public String[] getBindingNamespaceURIs() {
        Trc.entry(this);
        String[] uris = supportedBindingNamespaceURIs.clone();
        Trc.exit(uris);
        return uris;
    }

    /**
     * Returns the WSDL namespace URIs of any port addresses this provider supports.
     * <p>
     * A defensive copy is returned, for the same reason as
     * {@link #getBindingNamespaceURIs()}.
     *
     * @return an array of all address namespaces supported by this provider
     */
    public String[] getAddressNamespaceURIs() {
        Trc.entry(this);
        String[] uris = supportedAddressNamespaceURIs.clone();
        Trc.exit(uris);
        return uris;
    }
}

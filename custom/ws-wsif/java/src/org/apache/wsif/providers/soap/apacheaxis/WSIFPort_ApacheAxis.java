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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;

import org.apache.axis.client.Call;
import org.apache.axis.client.Transport;
import org.apache.axis.transport.http.HTTPTransport;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.base.WSIFDefaultPort;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.providers.WSIFDynamicTypeMap;
import org.apache.wsif.util.WSIFProperties;
import org.apache.wsif.util.WSIFUtils;
import org.apache.wsif.util.jms.WSIFJMSDestination;
import org.apache.wsif.util.jms.WSIFJMSFinder;
import org.apache.wsif.wsdl.extensions.jms.JMSAddress;
import org.apache.wsif.wsdl.extensions.jms.JMSConstants;

/**
 * This class implements a WSDL SOAP binding using Apache AXIS.
 * See section 3 of the WSDL 1.1 specification for details 
 * of the WSDL SOAP binding. WSIF extends this standard SOAP
 * binding with the WSIF extensions for SOAP over JMS.
 * 
 * @author Mark Whitlock <whitlock@apache.org>
 * @author Ant Elder <ant.elder@uk.ibm.com>
 */
public class WSIFPort_ApacheAxis extends WSIFDefaultPort {

    @Serial
    private static final long serialVersionUID = 2L;

	protected Definition definition;
	protected Port port;
	protected SOAPBinding soapBinding;
	protected SOAPAddress soapAddress;
	protected JMSAddress jmsAddress;

	protected String bindingStyle;
	protected URL endPointURL;
	protected List jmsAddressPropVals;

	protected WSIFDynamicTypeMap wsifdynamictypemap;
	protected Map cachedWSIFOperations;

	transient protected Transport transport;
	transient protected Call call;

	private static final String HTTP_TRANSPORT_URI =
		WSIFAXISConstants.HTTP_TRANSPORT_URI;
	private static final String JMS_TRANSPORT_URI =
		WSIFAXISConstants.JMS_TRANSPORT_URI;

	/**
	 * Construct a new WSIFPort
	 */
	public WSIFPort_ApacheAxis(
		Definition definition,
		Port port,
		SOAPBinding soapBinding,
		WSIFDynamicTypeMap wsifdynamictypemap)
		throws WSIFException {
		Trc.entry(this, definition, port, soapBinding, wsifdynamictypemap);

		this.definition = definition;
		this.port = port;
		this.soapBinding = soapBinding;
		this.wsifdynamictypemap = wsifdynamictypemap;

		parseSoapBinding();
		parseServiceAddress();

		if (Trc.ON)
			Trc.exit(deep());
	}

	/**
	 * Gets the soap:binding WSDL element and validates its attributes
	 * The soap:binding WSDL element has the form:
	 *    <soap:binding style="rpc|document" transport="uri">
	 */
	private void parseSoapBinding() throws WSIFException {
		this.bindingStyle = soapBinding.getStyle();
		if (bindingStyle == null || bindingStyle.length() < 1) {
			bindingStyle = WSIFAXISConstants.STYLE_DOCUMENT;
		} else if (!WSIFAXISConstants.VALID_STYLES.contains(bindingStyle)) {
			throw new WSIFException(
				"unsupported style '"
					+ bindingStyle
					+ "' for binding:"
					+ soapBinding);
		}

		String transportURI = soapBinding.getTransportURI();
		if (!WSIFAXISConstants.VALID_TRANSPORTS.contains(transportURI)) {
			throw new WSIFException(
				"unsupported transport '"
					+ transportURI
					+ "' for binding: "
					+ soapBinding);
		}
	}

	/**
	 * Gets the soap:address or jms:address WSDL element from the service port
	 * and validates it against the binding transport. 
     *    <port .... >
     *        <soap:address location="uri"/>? 
	 *        <jms:address destinationStyle="topic|queue"
     *                     jmsVendorURI="uri"?
     *                     initialContextFactory="uri"?
     *                     jndiProviderURL="uri"?
     *                     jndiConnectionFactoryName="nmtoken"
     *                     jndiDestinationName="nmtoken"
     *                     <jms:propertyValue 
     *                        name="nmtoken" value="nmtoken" type="qname">*
     *        </jms:address>?
     *        <jms:address jmsVendorURI="uri"
     *                     jmsImplementationSpecificURI="uri"
     *                     <jms:propertyValue 
     *                        name="nmtoken" value="nmtoken" type="qname">*
     *        </jms:address>?
     *    </port>
	 */
	private void parseServiceAddress() throws WSIFException {
		this.soapAddress =
			(SOAPAddress) getExtElem(port,
				SOAPAddress.class,
				port.getExtensibilityElements());
		this.jmsAddress =
			(JMSAddress) getExtElem(port,
				JMSAddress.class,
				port.getExtensibilityElements());

		if (soapAddress != null && jmsAddress != null)
			throw new WSIFException(
				"Both soap:address and jms:address cannot be specified for port "
					+ port);

		if (soapAddress == null && jmsAddress == null)
			throw new WSIFException(
				"Either soap:address or jms:address must be specified for port "
					+ port);

		if (isTransportHTTP() && soapAddress == null) {
			throw new WSIFException(
				"binding transport "
					+ HTTP_TRANSPORT_URI
					+ " requires soap:address for port "
					+ port);
		}

		if (soapAddress != null) {
			String s = soapAddress.getLocationURI();
			if (s == null || s.length() < 1) {
				throw new WSIFException(
					"soap:address with location URI is required for " + port);
			}
			
			if (s.startsWith(JMSConstants.JMS_URL_PROTOCOL)) {
				this.jmsAddress = WSIFJMSDestination.getJMSAddressFromURL(s);
				this.soapAddress = null;
			} else {
			    try {
				    this.endPointURL = URI.create(s).toURL();
			    } catch (MalformedURLException e) {
				    Trc.exception(e);
				    throw new WSIFException(
					    "exception setting SOAP address to "
						    + s
						    + ": "
						    + e.getLocalizedMessage(),
					    e);
			    }
			}
		} 
		
		if (isTransportJMS() && jmsAddress == null) {
			throw new WSIFException(
				"binding transport "
					+ JMS_TRANSPORT_URI
					+ " requires jms:address for port "
					+ port);
		}

		if (jmsAddress != null) {
			this.jmsAddressPropVals = jmsAddress.getJMSPropertyValues();
		}

	}

	/**
	 * Creates a WSIFOperation for the given operation name
	 * @return WSIFOperation   the WSIFOperation
	 * @see org.apache.wsif.WSIFPort#createOperation(String) 
	 * @throws WSIFException  if there is an exception creating the WSIFOperation
	 */
	public WSIFOperation createOperation(String operationName)
		throws WSIFException {
		Trc.entry(this, operationName);
		WSIFOperation wo = createOperation(operationName, null, null);
		Trc.exit(wo);
		return wo;
	}

	/**
	 * Creates a WSIFOperation for the given operation name
	 * @return WSIFOperation   the WSIFOperation_ApacheAxis
	 * @see org.apache.wsif.WSIFPort#createOperation(String, String, String) 
	 * @throws WSIFException  if there is an exception creating the WSIFOperation
	 */
	public WSIFOperation createOperation(
		String operationName,
		String inputName,
		String outputName)
		throws WSIFException {
		Trc.entry(this, operationName, inputName, outputName);

		WSIFOperation_ApacheAxis op =
			getDynamicWSIFOperation(operationName, inputName, outputName);
		if (op == null) {
			throw new WSIFException(
				"Could not create operation: "
					+ operationName
					+ ":"
					+ inputName
					+ ":"
					+ outputName);
		}
		Trc.exit(op);
		return op;
	}

	/**
	 * Closes the port. 
	 * All methods are invalid after calling this method.
	 * @throws WSIFException  if there is an exception closing the Transport
	 */
	public void close() throws WSIFException {
		Trc.entry(this);
		if (transport != null && transport instanceof WSIFJmsTransport jmsTransport) {
			jmsTransport.close();
		}
		Trc.exit();
	}

	/**
	 * @deprecated replaced by getTransport 
	 */
	public Transport getAxisTransport() throws WSIFException {
		Trc.entry(this);
		Transport t = getTransport();
		Trc.exit(t);
		return t;
	}

	/**
	 * Gets the QName of the WSDL Binding
	 * @return QName   the name of the Binding
	 */
	public QName getBindingName() {
		Trc.entry(this);
		Binding binding = port.getBinding();
		QName bindingQN = binding.getQName();
		Trc.exit(bindingQN);
		return bindingQN;
	}

	/**
	 * Returns the style attribute of this Binding.
	 * @return String   the style attribute
	 */
	public String getBindingStyle() {
		Trc.entry(this);
		Trc.exit(bindingStyle);
		return bindingStyle;
	}

	/**
	 * Returns the namespace of the WSDL portType used by this Binding.
	 * @return String   the portType namespace
	 */
	public String getPortTypeNamespace() {
		Trc.entry(this);
		String portTypeNamespace =  
    		port.getBinding().getPortType().getQName().getNamespaceURI();
   		Trc.exit(portTypeNamespace);
		return portTypeNamespace;
	}

	/**
	 * Gets the AXIS Call object being used by this WSIFPort 
	 * @return Call   the AXIS Call object
	 * @throws WSIFException  if there is an exception creating the AXIS Call
	 */
	public Call getCall() throws WSIFException {
		Trc.entry(this);
		if (call == null) {
			call = makeNewAXISCall();
		}
		Trc.exit(call);
		return call;
	}

    /**
     * Creates a new AXIS Call object
     */
    private Call makeNewAXISCall() throws WSIFException {
    	Call c = null;
        java.net.URL url = getEndPoint();
		try {
		    if (url != null) {
				c = new Call(url);
			    Transport axistransport = getTransport();
				if (axistransport != null) {
					axistransport.setUrl(url.toString());
				}
			} else {
				c = new Call(new org.apache.axis.client.Service());
			}
    		c.setMaintainSession(true);
		} catch (JAXRPCException e) {
			Trc.exception(e);
			throw new WSIFException(
				"exception creating call object: "
					+ e.getLocalizedMessage(),
				e);
		}
		return c;
    }

	/**
	 * Returns the Definition object for the WSDL file 
	 * @return Definition   the WSDL4J Definition object
	 */
	public Definition getDefinition() {
		Trc.entry(this);
		Trc.exit(definition);
		return definition;
	}

	/**
	 * @deprecated WSIF clients should use the createOperation methods
	 */
	public WSIFOperation_ApacheAxis getDynamicWSIFOperation(
		String opName,
		String inputName,
		String outputName)
		throws WSIFException {
		Trc.entry(this, opName, inputName, outputName);

		WSIFOperation_ApacheAxis cachedOp = null;
		
        if (cachedWSIFOperations == null) {
           cachedWSIFOperations = new HashMap();
        } else {
  		   cachedOp = (WSIFOperation_ApacheAxis) cachedWSIFOperations.get(
		      getKey(opName, inputName, outputName));
	    }

		WSIFOperation_ApacheAxis wsifOperation;
		if (cachedOp == null) {
            BindingOperation bop =
               WSIFUtils.getBindingOperation( 
                  port.getBinding(), opName, inputName, outputName );
			if (bop == null) {
				throw new WSIFException(
				   "no operation found named " + 
				   opName + ", input:" + inputName + ", output:" + outputName );
			}
			cachedOp =
				new WSIFOperation_ApacheAxis(
					this,
                    bop.getOperation(),
					wsifdynamictypemap);
			cachedWSIFOperations.put(
				getKey(opName, inputName, outputName),
				cachedOp);
		    wsifOperation = cachedOp;
		} else {
		    wsifOperation = cachedOp.copy();
		}

		Trc.exit(wsifOperation);
		return wsifOperation;
	}

	/**
	 * Returns the URL of the location attribute of the soap:address 
	 * @return URL   the URL of the service location        
	 */
	public URL getEndPoint() {
		Trc.entry(this);
		Trc.exit(endPointURL);
		return endPointURL;
	}

    //TODO these getExtElem methods should be moved to ProviderUtils?
	/**
	 * Wrapper to enable WSIFOperation to use the same WSIFDefaultPort method
	 * @see WSIFDefaultPort#getExtElem(Object, Class, List)
	 */
	public Object getExtElem(Object ctx, Class extType, List extElems)
		throws WSIFException {
		Trc.entry(this, ctx, extType, extElems);
		Object o = super.getExtElem(ctx, extType, extElems);
		Trc.exit(o);
		return o;
	}

	/**
	 * Wrapper to enable WSIFOperation to use the same WSIFDefaultPort method
	 * @see WSIFDefaultPort#getExtElems(Object, Class, List)
	 */
	public List getExtElems(Object ctx, Class extType, List extElems)
		throws WSIFException {
		Trc.entry(this, ctx, extType, extElems);
		List l = super.getExtElems(ctx, extType, extElems);
		Trc.exit(l);
		return l;
	}

	/**
	 * Returns any JMS propertyValue elements in the jms:address element
	 * @return List   a List of the jms:propertyValue elements 
	 */
	public List getJmsAddressPropVals() {
		Trc.entry(this);
		Trc.exit(jmsAddressPropVals);
		return jmsAddressPropVals;
	}

	/**
	 * Returns the WSDL Port object this WSIFPort represents
	 * @return Port   the WSDL4J Port object
	 */
	public Port getPort() {
		Trc.entry(this);
		Trc.exit(port);
		return port;
	}

	/**
	 * Gets the soap:operation WSDL element from a BindingOperation
	 * The WSDL binding operation element has the form:
     *   <binding .... >
     *        ...
     *        <operation .... >
     *           <soap:operation soapAction="uri"? style="rpc|document"?>?
     *           ...
     *        </operation>
     *    </binding>
	 * @return SOAPOperation   the soap:operation element
	 * @throws WSIFException  if there is no soap:operation element in the binding operation element.
	 */
	public SOAPOperation getSOAPOperation(BindingOperation bindingOp)
		throws WSIFException {
		Trc.entry(this, bindingOp);
		SOAPOperation soapOperation =
			(SOAPOperation) getExtElem(bindingOp,
				javax.wsdl.extensions.soap.SOAPOperation.class,
				bindingOp.getExtensibilityElements());
		if (soapOperation == null)
			throw new WSIFException(
				"no soap:operation found in binding for: " + bindingOp);
		Trc.exit(soapOperation);
		return soapOperation;
	}

	/**
	 * Gets the AXIS Transport object being used by this WSIFPort 
	 * @return Transport   the AXIS Transport object
	 * @throws WSIFException  if there is an exception creating the Transport
	 */
	public Transport getTransport() throws WSIFException {
		Trc.entry(this);
		if (transport == null) {
			String s = soapBinding.getTransportURI();
			if (HTTP_TRANSPORT_URI.equals(s)) {
				transport = new HTTPTransport();
			} else if (JMS_TRANSPORT_URI.equals(s)) {
				WSIFJMSDestination jmsDestination =
					new WSIFJMSDestination(
						WSIFJMSFinder.newFinder(jmsAddress, port.getName()),
						jmsAddress.getJmsProvDestName(),
						WSIFProperties.getSyncTimeout());
				transport = new WSIFJmsTransport(jmsDestination);
			}
		}
		Trc.exit(transport);
		return transport;
	}

	/**
	 * Tests if an HTTP transport is being used
	 * @return boolean   true if an HTTP transport is being used,
	 *                    otherwise false
	 */
	public boolean isTransportHTTP() {
		Trc.entry(this);
		String transportURI = soapBinding.getTransportURI();
		boolean httpTransport = HTTP_TRANSPORT_URI.equals(transportURI);
		Trc.exit(httpTransport);
		return httpTransport;
	}

	/**
	 * Tests if a JMS transport is being used
	 * @return boolean   true if a JMS transport is being used,
	 *                    otherwise false
	 */
	public boolean isTransportJMS() {
		Trc.entry(this);
		String transportURI = soapBinding.getTransportURI();
		boolean jmsTransport = JMS_TRANSPORT_URI.equals(transportURI);
		Trc.exit(jmsTransport);
		return jmsTransport;
	}

	/**
	 * @deprecated should anyone be calling this? 
	 */
	public void setDefinition(Definition definition1) {
		Trc.entry(this, definition1);
		definition = definition1;
		Trc.exit();
	}

	/**
	 * @deprecated should anyone be calling this? 
	 */
	public void setDynamicWSIFOperation(
		String s,
		String s1,
		String s2,
		WSIFOperation_ApacheAxis wsifoperation_apacheaxis) {
		Trc.entry(this, s, s1, s2, wsifoperation_apacheaxis);
        if (cachedWSIFOperations == null) {
           cachedWSIFOperations = new HashMap();
        }
		cachedWSIFOperations.put(getKey(s, s1, s2), wsifoperation_apacheaxis);
		Trc.exit();
	}

	/**
	 * @deprecated should anyone be calling this? 
	 */
	public void setEndPoint(URL url1) {
		Trc.entry(this, url1);
		endPointURL = url1;
		Trc.exit();
	}

	/**
	 * @deprecated should anyone be calling this? 
	 */
	public void setPort(Port port1) {
		Trc.entry(this, port1);
		port = port1;
		Trc.exit();
	}

	/**
	 * Tests if this port supports asynchronous calls to operations.
	 * @return true if the port is using a JMS transport, otherwise false
	 */
	public boolean supportsAsync() {
		Trc.entry(this);
		if (isTransportJMS()) {
			Trc.exit(true);
			return true;
		} else {
			Trc.exit(false);
			return false;
		}
	}

	public String deep() {
		StringBuffer buff = new StringBuffer();
		try {
			buff.append(new String(super.toString()));
			buff.append(":\n");
			buff.append(" port: ").append(port);
			buff.append(" definition: ").append(definition);
			buff.append(" soapbinding: ").append(soapBinding);
			buff.append(" bindingStyle: ").append(bindingStyle);
			buff.append(" soapAddress: ").append(soapAddress);
			buff.append(" jmsAddress: ").append(jmsAddress);
			buff.append(" service url: ").append(endPointURL);
			buff.append(" jmsAddressPropVals: ").append(jmsAddressPropVals);
			buff.append(" dynamicTypeMap: ").append(wsifdynamictypemap);
			buff.append(" transport: ").append(transport);
			buff.append(" call: ").append(call);
			buff.append("operationInstances: ").append(cachedWSIFOperations);
		} catch (Exception e) {
			Trc.exceptionInTrace(e);
		}
		return buff.toString();
	}

}
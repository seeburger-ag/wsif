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

package com.myeis.wsif.providers.j2c.myeis;

import javax.resource.*;
import javax.resource.cci.*;
import javax.wsdl.*;
import javax.wsdl.Binding;
import javax.wsdl.extensions.*;

import java.util.Iterator;
import java.util.List;

import com.myeis.j2c.*;
import com.myeis.wsdl.extensions.j2c.myeis.*;
import org.apache.wsif.*;
import org.apache.wsif.base.*;
import org.apache.wsif.providers.jca.*;
import org.apache.wsif.providers.jca.WSIFUtils_JCA;
import org.apache.wsif.providers.*;

public class WSIFProvider_MyEIS implements org.apache.wsif.spi.WSIFProvider, org.apache.wsif.providers.jca.WSIFProviderJCAExtensions {

	private final String[] namespaces = {MyEISBindingConstants.NS_URI};

    /**
     * WSIFDynamicProvider_MyEIS default constructor
     */
    public WSIFProvider_MyEIS() {
    	super();
    	WSIFServiceImpl.addExtensionRegistry(new MyEISExtensionRegistry());
    }
    
	/**
	 * @see WSIFDynamicProvider#createDynamicWSIFPort(Definition, Service, Port, WSIFDynamicTypeMap)
	 */
	public WSIFPort createDynamicWSIFPort(Definition definition, Service service, Port port, WSIFDynamicTypeMap typeMap) throws WSIFException {
		
		WSIFPort_JCA jcaPort = null;
		Connection connection = null;
		ConnectionFactory connectionFactory = null;

		Binding binding = port.getBinding();
		List eElements = binding.getExtensibilityElements();
		Iterator iterator = eElements.iterator();
		while (iterator.hasNext()) {
			Object o = iterator.next();
			if (o instanceof MyEISBinding) {
				/*
				 * To create the connection factory, the default behaviour is to assume that the J2EE Connector is running
				 * in a managed mode (i.e. deployed on an Application Server with a JNDI name). Hence the first attempt is done
				 * by retrieving the connection factory using a JNDI lookup name.
				 */
				String res_ref_name = WSIFUtils_JCA.getJNDILookupName(service, port);
				connectionFactory = WSIFUtils_JCA.lookupConnectionFactory(res_ref_name, "javax.resource.cci.ConnectionFactory");
				
				try {
					ExtensibilityElement portExtension = (ExtensibilityElement) port.getExtensibilityElements().get(0);

					if (portExtension == null) {
						return jcaPort;
					}
					MyEISAddress address = (MyEISAddress) portExtension;
					if (connectionFactory == null) {
						/*
						 * The JNDI lookup must have failed, so we fall back to a non-managed mode. (i.e. we manually create
						 * the managed connection factory and a connection.
						 */
						MyEISManagedConnectionFactory managedConnectionFactory = new MyEISManagedConnectionFactory();
						managedConnectionFactory.setRepositoryLocation(address.getRepositoryLocation());
						connectionFactory = (ConnectionFactory) managedConnectionFactory.createConnectionFactory();
					}
					connection = connectionFactory.getConnection();
				}
				catch(Throwable exn){
					return jcaPort;
				}
				jcaPort = new WSIFPort_JCA(definition, service, port, connection, this, typeMap);
				return jcaPort;
				
			}
		}
		return jcaPort;
	}


	public WSIFMessage createInputMessage(Definition definition, Binding binding, String operationName, String inputName, String outputName){return null;}
	public WSIFMessage createOutputMessage(Definition definition, Binding binding, String operationName, String inputName, String outputName){return null;}
	public WSIFMessage createFaultMessage(Definition definition, Binding binding, String operationName, String inputName, String outputName){return null;}

	public String[] getBindingNamespaceURIs(){
		return namespaces;
	}	

	public String[] getAddressNamespaceURIs(){
		return namespaces;
	}
	public void updateOutputMessage(WSIFMessage output, Binding aBinding, String aOperationName, String aInputName, String aOutputName, InteractionSpec aInteractionSpec) {
		// Not supported in this sample
	}
	public void updateInteractionSpec(WSIFMessage input, Binding aBinding, String aOperationName, String aInputName, String aOutputName, InteractionSpec aInteractionSpec) {
		// Not supported in this sample
	}
	
	
	/**
	 * @see org.apache.wsif.providers.jca.WSIFProviderJCAExtensions#createConnection(WSIFMessage, Definition, Service, Port, WSIFDynamicTypeMap, Binding, String, String, String)
	 */
	public Connection createConnection(
		WSIFMessage input,
		Definition definition,
		Service service,
		Port port,
		WSIFDynamicTypeMap typeMap,
		Binding aBinding,
		String aOperationName,
		String aInputName,
		String aOutputName)
		throws WSIFException {
		return null;
	}

	/**
	 * @see org.apache.wsif.providers.jca.WSIFProviderJCAExtensions#createOperation(Definition, Service, Port, String, String, String, WSIFDynamicTypeMap, WSIFPort_JCA, Connection)
	 */
	public WSIFOperation createOperation(
		Definition aDefinition,
		Service aService,
		Port aPort,
		String aOperationName,
		String aInputName,
		String aOutputName,
		WSIFDynamicTypeMap typeMap,
		WSIFPort_JCA jcaPort,
		Connection aConnection)
		throws WSIFException {
		WSIFOperation_JCA operation = null;

		try {
			BindingOperation bindingOperationModel = aPort.getBinding().getBindingOperation(aOperationName, aInputName, aOutputName);

			ExtensibilityElement bindingOperationModelExtension = (ExtensibilityElement) bindingOperationModel.getExtensibilityElements().get(0);
			if (bindingOperationModelExtension == null) {
				throw new WSIFException("missing bindingOperation extension");
			}
			if (!(bindingOperationModelExtension instanceof MyEISOperation)) {
				throw new WSIFException("invalid extensibility element");
			}

			MyEISOperation operationModelExtension = (MyEISOperation) bindingOperationModelExtension;

			MyEISInteractionSpec interactionSpec = new MyEISInteractionSpec();
			interactionSpec.setFunctionName(operationModelExtension.getFunctionName());

			operation = new WSIFOperation_JCA(aDefinition, aService, aPort, aOperationName, aInputName, aOutputName, typeMap, jcaPort, this, aConnection, interactionSpec);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return operation;
	}

}
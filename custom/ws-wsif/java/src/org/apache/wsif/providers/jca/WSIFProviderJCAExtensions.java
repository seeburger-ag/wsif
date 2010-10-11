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

import javax.wsdl.*;
import javax.resource.cci.*;
import org.apache.wsif.*;
import org.apache.wsif.providers.WSIFDynamicTypeMap;

/**
 * This interface contains methods implemented by each Resource Adapter and used by the Connector 
 * Architecture provider to delegate Connector specific operations, for example creation of the
 * WSIFOperation to the Resource Adapter.
 * 
 * @author Michael Beisiegel
 * @author Piotr Przybylski <piotrp@ca.ibm.com>
 * @author John Green
 */
public interface WSIFProviderJCAExtensions {
	

	/**
	 * The provider for a resource adapter creates a WSIFOperation based on the specified WSDL
	 * operation.  The binding operation extensibility element allows the resource adapter to
	 * populate its InteractionSpec to be used in the operation.
	 * 
	 * @param definition
	 * @param aService
	 * @param aPort
	 * @param operationName
	 * @param inputName
	 * @param outputName
	 * @param typeMap
	 * @param jcaPort
	 * @param connection
	 * @return WSIFOperation
	 * @throws WSIFException
	 */
	public WSIFOperation createOperation(Definition definition, Service aService, Port aPort, String operationName, String inputName, String outputName, WSIFDynamicTypeMap typeMap, WSIFPort_JCA jcaPort, Connection connection) throws WSIFException;
	/**
	 * This method creates input message. It only needs to be implemented by Resource Adapter which
	 * uses custom format of the input and output records (i.e. does not use javax.resource.cci.Streamable
	 * interface).
	 * 
	 * @param definition
	 * @param binding
	 * @param operationName
	 * @param inputName
	 * @param outputName
	 * @return WSIFMessage
	 */
	public WSIFMessage createInputMessage(Definition definition, Binding binding, String operationName, String inputName, String outputName);
	/**
	 * This method creates output message. It only needs to be implemented by Resource Adapter which
	 * uses custom format of the input and output records (i.e. does not use javax.resource.cci.Streamable
	 * interface).
	 * 
	 * @param definition
	 * @param binding
	 * @param operationName
	 * @param inputName
	 * @param outputName
	 * @return WSIFMessage
	 */
	public WSIFMessage createOutputMessage(Definition definition, Binding binding, String operationName, String inputName, String outputName);
	/**
	 * This method creates a FaultMessage.
	 * 
	 * @param definition
	 * @param binding
	 * @param operationName
	 * @param inputName
	 * @param outputName
	 * @return WSIFMessage
	 */
	public WSIFMessage createFaultMessage(Definition definition, Binding binding, String operationName, String inputName, String outputName);
	/**
	 * Updates the interactionSpec from input message values. The method is called from
	 * within the operation execute method, before invocation of Interaction.execute() method
	 * of the resource adapter.
	 * 
	 * @param input
	 * @param aBinding
	 * @param aOperationName
	 * @param aInputName
	 * @param aOutputName
	 * @param aInteractionSpec
	 * @throws WSIFException
	 */
	public void updateInteractionSpec(WSIFMessage input, Binding aBinding, String aOperationName, String aInputName, String aOutputName, InteractionSpec aInteractionSpec) throws WSIFException;
	/**
	 * Updates the output message using output InteractionSpec values.  This method is called
	 * from within the WSIFOperation execute method, after processing the Interaction.execute() method
	 * of the resource adapter..	 
	 * 
	 * @param output Output message to populate
	 * @param aBinding Bonding
	 * @param aOperationName Operation name
	 * @param aInputName Inpput name
	 * @param aOutputName Output name
	 * @param aInteractionSpec InteractionSpec after the execute() method invocation 
	 * @throws WSIFException
	 */
	public void updateOutputMessage(WSIFMessage output, Binding aBinding, String aOperationName, String aInputName, String aOutputName, InteractionSpec aInteractionSpec) throws WSIFException;
	/**
	 * Creates a javax.resource.cci.Connection.  This should be used when a resource adapter supports
	 * passing ConnectionSpec values as part of the input message.  WSIFOperation_JCA will only call
	 * this method during the execute method if the WSIFPort_JCA does not contain a connection.
	 * 
	 * @param input
	 * @param definition
	 * @param service
	 * @param port
	 * @param typeMap
	 * @param aBinding
	 * @param aOperationName
	 * @param aInputName
	 * @param aOutputName
	 * @return Connection
	 * @throws WSIFException
	 */
	public Connection createConnection(WSIFMessage input, Definition definition, Service service, Port port, org.apache.wsif.providers.WSIFDynamicTypeMap typeMap, Binding aBinding, String aOperationName, String aInputName, String aOutputName) throws WSIFException;
}

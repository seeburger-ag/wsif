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
package org.apache.wsif.providers.jca.toolplugin;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;

/**
 * This class is a default implementation proxy for the Operations of the Import Service. Tooling environments can extend this class and customize
 * its behaviour. It is provided as a default implementation of how to invoke the various operations that are defined in the Import Service archictecture.
 * 
 *  @author Hesham Fahmy <hfahmy@ca.ibm.com>
 */
public abstract class Import {

	private static final long serialVersionUID = 1L;
	/*The WSDL Definition that is providing the implementation of the Import Service*/
	private Definition serviceDefintion = null;
	/*The WSDL Service that is providing the implementation of the Import Service*/
	private Service importService = null;
	/*The WSDL PortType that is providing the implementation of the Import Service*/
	private PortType importPortType = null;


	/*Constants that represent standard values of any Import Service implementation*/
	public static final String IMPORT_SERVICE_BASE_NAMESPACE = "http://importservice.jca.providers.wsif.apache.org/";
	public static final String IMPORT_SERVICE_BASE_PORTTYPE_NAME = "Import";
	
	public static final String GET_PORTTYPES_OPERATION = "getPortTypes";
	public static final String GET_DEFINITION_OPERATION = "getDefinition";
	public static final String GET_RAW_EIS_METADATA_OPERATION = "getRawEISMetaData";

	public static final String PORT_TYPE_SELECTION_PART = "portTypeSelection";
	public static final String RESULT_PART = "result";
	public static final String QUERY_STRING_PART = "queryString";

	/**
	 * Default Constructor for <code>Import</code>.
	 */
	public Import() {
		super();
	}

	/**
	 * Constructor for <code>Import</code>. This constructor allows a WSDL definition, that defines a concrete implementation of
	 * the Import Service (i.e. by a particular EIS), to be passed in. In addition to the WSDL Defintion, the constructor needs the name of
	 * the WSDL Service, contained in the Definition, that provides the implementation of the Import Service.
	 * @param <code>aServiceDefinition</code> The WSDL Definition
	 * @param <code>aServiceName</code> The name of the Service
	 * @throws <code>WSIFException</code>
	 */
	public Import(Definition aServiceDefinition, String aServiceName) throws WSIFException {
		super();
		try {
			serviceDefintion = aServiceDefinition;
			importPortType = serviceDefintion.getPortType(new QName(IMPORT_SERVICE_BASE_NAMESPACE, IMPORT_SERVICE_BASE_PORTTYPE_NAME));
			importService = serviceDefintion.getService(new QName(serviceDefintion.getTargetNamespace(), aServiceName));
		} catch (Exception e) {
			if (e instanceof WSIFException)
				throw (WSIFException) e;
			else
				throw new WSIFException(e.getMessage(), e);
		}
	}


	/**
	 * This is the proxy method to invoke the <code>getPortTypes</code> Operation of the Import Service.
	 * @param <code>queryString</code> - The query string to be used
	 * @return <code>PortTypeArray</code>
	 * @throws <code>WSIFException</code>
	 */
	public PortTypeArray getPortTypes(String queryString) throws WSIFException {
		try {
			WSIFService portFactory = WSIFServiceFactory.newInstance().getService(serviceDefintion, importService, importPortType);

			WSIFPort port = portFactory.getPort();

			WSIFOperation operation = port.createOperation(GET_PORTTYPES_OPERATION);
			WSIFMessage inputMessage = operation.createInputMessage();
			WSIFMessage outputMessage = operation.createOutputMessage();
			inputMessage.setObjectPart(QUERY_STRING_PART, queryString);

			operation.executeRequestResponseOperation(inputMessage, outputMessage, null);

			PortTypeArray portTypeArray = (PortTypeArray) outputMessage.getObjectPart(RESULT_PART);

			port.close();
			return portTypeArray;
		} catch (Exception e) {
			if (e instanceof WSIFException)
				throw (WSIFException) e;
			else
				throw new WSIFException(e.getMessage(), e);
		}
	}

	/**
	 * This is the proxy method to invoke the <code>getDefinition</code> Operation of the Import Service.
	 * @param <code>portTypeSelection<code>  - the <code>PortTypeSelection</code> to be used.
	 * @return <code>ImportDefinition</code>
	 * @throws <code>WSIFException<code>
	 */
	public ImportDefinition getDefinition(PortTypeSelection portTypeSelection) throws WSIFException {
		try {
			WSIFService portFactory = WSIFServiceFactory.newInstance().getService(serviceDefintion, importService, importPortType);
			WSIFPort port = portFactory.getPort();

			WSIFOperation operation = port.createOperation(GET_DEFINITION_OPERATION);
			WSIFMessage inputMessage = operation.createInputMessage();
			WSIFMessage outputMessage = operation.createOutputMessage();

			inputMessage.setObjectPart(PORT_TYPE_SELECTION_PART, portTypeSelection);

			operation.executeRequestResponseOperation(inputMessage, outputMessage, null);

			ImportDefinition importDefinition = (ImportDefinition) outputMessage.getObjectPart(RESULT_PART);
			port.close();

			return importDefinition;
		} catch (Exception e) {
			if (e instanceof WSIFException)
				throw (WSIFException) e;
			else
				throw new WSIFException(e.getMessage(), e);
		}

	}

	/**
	 * This is the proxy method to invoke the <code>getRawEISMetaData</code> Operation of the Import Service
	 * @param <code>queryString</code> - The query string to be used
	 * @return <code>byte[]</code>
	 * @throws <code>WSIFException</code>
	 */
	public byte[] getRawEISMetaData(String queryString) throws WSIFException {
		try {
			WSIFService portFactory = WSIFServiceFactory.newInstance().getService(serviceDefintion, importService, importPortType);

			WSIFPort port = portFactory.getPort();

			// getPortTypes
			WSIFOperation operation = port.createOperation(GET_RAW_EIS_METADATA_OPERATION);
			WSIFMessage inputMessage = operation.createInputMessage();
			WSIFMessage outputMessage = operation.createOutputMessage();
			inputMessage.setObjectPart(QUERY_STRING_PART, queryString);

			operation.executeRequestResponseOperation(inputMessage, outputMessage, null);

			byte[] byteArray = (byte[]) outputMessage.getObjectPart(RESULT_PART);

			port.close();
			return byteArray;
		} catch (Exception e) {
			if (e instanceof WSIFException)
				throw (WSIFException) e;
			else
				throw new WSIFException(e.getMessage(), e);

		}
	}

	/**
	 * Returns the WSDL PortType for the Import Service
	 * @return <code>PortType</code>
	 */
	public PortType getImportPortType() {
		return importPortType;
	}

	/**
	 * Returns the WSDL Service for the Import Service
	 * @return <code>Service</code>
	 */
	public Service getImportService() {
		return importService;
	}

	/**
	 * Returns the WSDL Definition for the Import Service
	 * @return <code>Definition</code>
	 */
	public Definition getServiceDefintion() {
		return serviceDefintion;
	}

	/**
	 * Sets the WSDL PortType for the Import Service
	 * @param <code>importPortType</code> The PortType to set
	 */
	public void setImportPortType(PortType importPortType) {
		this.importPortType = importPortType;
	}

	/**
	 * Sets the the WSDL Service for the Import Service
	 * @param <code>importService</code> The Service to set
	 */
	public void setImportService(Service importService) {
		this.importService = importService;
	}

	/**
	 * Sets the WSDL Definition for the Import Service
	 * @param <code>serviceDefintion</code> The Defintion to set
	 */
	public void setServiceDefintion(Definition serviceDefintion) {
		this.serviceDefintion = serviceDefintion;
	}

}
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

import java.io.Serial;
import org.apache.wsif.logging.*;
import org.apache.wsif.providers.*;
import org.apache.wsif.*;
import org.apache.wsif.base.*;
import org.apache.wsif.util.*;

import javax.resource.*;
import javax.resource.cci.*;
import javax.wsdl.extensions.*;
import javax.wsdl.*;

import java.net.URL;
import java.util.*;

/**
 * The WSIFPort_JCA class is a connector architecture specific implementation of WSIFPort.
 * This port has a handle to resource adapter connection, and is used to create WSIFOperation.
 * 
 * @author Michael Beisiegel
 * @author Piotr Przybylski <piotrp@ca.ibm.com>
 */
public class WSIFPort_JCA extends WSIFDefaultPort {

    @Serial
    private static final long serialVersionUID = 1L;
	private Connection fieldConnection;
	private Port fieldPort;
	private Definition fieldDefinition;
	private Service fieldService;
	private WSIFProviderJCAExtensions fieldFactory;
	private final static String crlf = System.getProperty("line.separator");
	protected Map operationInstances = new HashMap();
	private org.apache.wsif.providers.WSIFDynamicTypeMap fieldTypeMap;
	
	
	/**
	 * Constructor for WSIFPort_JCA.
	 * @param aDefinition
	 * @param aService
	 * @param aPort
	 * @param aConnection
	 * @param aFactory
	 * @param typeMap
	 */
	public WSIFPort_JCA(Definition aDefinition, Service aService, Port aPort, Connection aConnection, WSIFProviderJCAExtensions aFactory, org.apache.wsif.providers.WSIFDynamicTypeMap typeMap) {

		super();
		this.fieldDefinition = aDefinition;
		this.fieldService = aService;
		this.fieldPort = aPort;
		this.fieldConnection = aConnection;
		this.fieldFactory = aFactory;
		this.fieldTypeMap = typeMap;		
	}

	private void addOperation(String name, String inputName, String outputName, WSIFOperation value) {
		operationInstances.put(getKey(name, inputName, outputName), value);
	}

	/**
	 * This method creates WSIFOperation_JCA using the configured, Resource Adapter specific
	 * factory (i.e. the implementor of <code>WSIFProviderJCAExtensions</code>).
	 * @see org.apache.wsif.WSIFPort#createOperation(String, String, String)
	 */
	public WSIFOperation createOperation(String aOperationName, String aInputName, String aOutputName) throws WSIFException {
		
		Trc.entry(this);
		WSIFOperation wsifOperation = getOperation(aOperationName, aInputName, aOutputName);
		if (wsifOperation == null) {
			wsifOperation = fieldFactory.createOperation(fieldDefinition, fieldService, fieldPort, aOperationName, 
														 aInputName, aOutputName, fieldTypeMap, this, fieldConnection);
			addOperation(aOperationName, aInputName, aOutputName, wsifOperation);
		}
		Trc.exit(wsifOperation);
		return wsifOperation;
	}

	/**
	 * Creates a WSIFOperation_JCA using the configured, Resource Adapter specific
	 * factory (i.e. the implementor of <code>WSIFProviderJCAExtensions</code>).
	 * @see org.apache.wsif.WSIFPort#createOperation(String)
	 */
	public WSIFOperation createOperation(String aOperationName) throws WSIFException {

		Trc.entry(this, aOperationName);

		WSIFOperation wsifOperation = getOperation(aOperationName, null, null);
		if (wsifOperation == null) {
			wsifOperation = fieldFactory.createOperation(fieldDefinition, fieldService, fieldPort, aOperationName, 
														 null, null, fieldTypeMap, this, fieldConnection);

			addOperation(aOperationName, null, null, wsifOperation);
		}
		Trc.exit(wsifOperation);
		return wsifOperation;
	}

	/**
	 * Closes the javax.resource.cci.Connection associated with this port. 
	 * 
	 * @see org.apache.wsif.WSIFPort#close()
	 */
	public void close() {
		try {
			if(this.fieldConnection != null)
				this.fieldConnection.close();
		}
		catch (ResourceException exn) {
		}
	}

	private WSIFOperation_JCA getOperation(String name, String inputName, String outputName) {

		return (WSIFOperation_JCA) operationInstances.get(getKey(name, inputName, outputName));

	}

	public String toString() {

		StringBuffer buffer = new StringBuffer();
		buffer.append(crlf + "[WSIFPort_JCA" + crlf);
		try {
			if (fieldConnection != null)
				buffer.append("\tConnection: " + fieldConnection.toString() + crlf);
			else
				buffer.append("\tConnection: null" + crlf);

			if (fieldPort != null)
				buffer.append("\tPort:       " + fieldPort.toString() + crlf);
			else
				buffer.append("\tPort:       null" + crlf);

			if (fieldDefinition != null)
				buffer.append("\tDefinition: " + fieldDefinition.toString() + crlf);
			else
				buffer.append("\tDefinition  null" + crlf);

			if (fieldService != null)
				buffer.append("\tService:    " + fieldService.toString() + crlf);
			else
				buffer.append("\tService:    null" + crlf);

			if (fieldFactory != null)
				buffer.append("\tFactory:    " + fieldFactory.toString() + crlf);
			else
				buffer.append("\tFactory:    null" + crlf);
			buffer.append("]" + crlf);
		}
		catch (Throwable exn) {
		}
		return buffer.toString();
	}
	/**
	 * Sets the javax.resource.cci.Connection associated with this port.
	 * @param connection The connection to set
	 */
	void setConnection(Connection connection) {
		fieldConnection = connection;
	}

}

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

import javax.resource.*;
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
import javax.resource.cci.*;
import java.io.Serializable;
import java.util.*;

/**
 * The WSIFOperation_JCA class is an implementation of the WSIFOperation interface, 
 * which is used to execute interactions with the EIS.
 * 
 * @author Michael Beisiegel
 * @author Piotr Przybylski <piotrp@ca.ibm.com>
 * @author John Green
 */
public class WSIFOperation_JCA implements WSIFOperation {

    @Serial
    private static final long serialVersionUID = 1L;
	protected Connection fieldConnection;
	protected InteractionSpec fieldInteractionSpec;
	protected Definition fieldDefinition;
	protected Binding fieldBinding;
	protected String fieldOperationName;
	protected String fieldInputName;
	protected String fieldOutputName;
	protected Operation fieldOperation;
	protected WSIFProviderJCAExtensions fieldFactory = null;
	private final static String crlf = System.getProperty("line.separator");
	private org.apache.wsif.providers.WSIFDynamicTypeMap fieldTypeMap;
	private Port fieldPort;
	private Service fieldService;
	private WSIFPort_JCA fieldJcaPort;
	
	/**
	 * The WSIFOperation_JCA constructor. 
	 * @param aDefinition
	 * @param aBinding
	 * @param aOperationName
	 * @param aInputName
	 * @param aOutputName
	 * @param aConnection
	 * @param aInteractionSpec
	 * @param aFactory
	 * @param typeMap
	 * @param aPort
	 * @param aService
	 * @param jcaPort
	 */
	public WSIFOperation_JCA(
		Definition aDefinition,
		Service aService,
		Port aPort,
		String aOperationName,
		String aInputName,
		String aOutputName,
		org.apache.wsif.providers.WSIFDynamicTypeMap typeMap,
		WSIFPort_JCA jcaPort,
		WSIFProviderJCAExtensions aFactory,
		Connection aConnection,
		InteractionSpec aInteractionSpec) {

		super();
		this.fieldDefinition = aDefinition;
		this.fieldInteractionSpec = aInteractionSpec;
		this.fieldConnection = aConnection;
		this.fieldFactory = aFactory;
		this.fieldBinding = aPort.getBinding();
		this.fieldOperationName = aOperationName;
		this.fieldInputName = aInputName;
		this.fieldOutputName = aOutputName;
		this.fieldTypeMap = typeMap;
		this.fieldPort = aPort;
		this.fieldService = aService;
		this.fieldJcaPort = jcaPort;
	}

	/**
	 * Invokes the request/response operation. This method 
	 * <ul>
	 * <li>Updates the InteractionSpec using data from the input message.
	 * <li>If a Connection is not currently available creates one, where a
	 * ConnectionSpec can be created using data from the input message and then 
	 * used when creating the Connection.
	 * <li>Uses the Connection to create a javax.resource.cci.Interaction.
	 * <li>Invokes the Interaction execute method.
	 * <li>Closes the interaction.
	 * <li>Updates the output message with InteractionSpec properties.
	 * </ul>
	 */
	public boolean executeRequestResponseOperation(WSIFMessage input, WSIFMessage output, WSIFMessage fault) throws WSIFException {

		Trc.entry(this, input, output, fault);
        if (input.getParts() == null || !input.getParts().hasNext())
			input = null;
		try {
			fieldFactory.updateInteractionSpec(input, fieldBinding, fieldOperationName, fieldInputName, fieldOutputName, fieldInteractionSpec);
			if (this.fieldConnection == null){
				this.fieldConnection = this.fieldFactory.createConnection(input, this.fieldDefinition, this.fieldService, this.fieldPort, this.fieldTypeMap, this.fieldBinding, this.fieldOperationName, this.fieldInputName, this.fieldOutputName);
				fieldJcaPort.setConnection(fieldConnection);
			}
			Interaction interaction = this.fieldConnection.createInteraction();
			interaction.execute(this.fieldInteractionSpec, (javax.resource.cci.Record) input, (javax.resource.cci.Record) output);
			interaction.close();
			if (output instanceof WSIFMessage_JCA cA) {
				cA.setInteractionSpec(this.fieldInteractionSpec);
			}
			fieldFactory.updateOutputMessage(output, fieldBinding, fieldOperationName, fieldInputName, fieldOutputName, fieldInteractionSpec);
		}
		catch (ResourceException exn1) {
			WSIFException newExn = new WSIFException(WSIFResource_JCA.get("WSIF1000E"));
			newExn.setTargetException(exn1);
			Trc.exception(exn1);
			throw newExn;
		}
		catch (Throwable exn3) {
			WSIFException newExn = new WSIFException(WSIFResource_JCA.get("WSIF1008E", exn3.getLocalizedMessage()));
			newExn.setTargetException(exn3);
			Trc.exception(newExn);
			throw newExn;
		}
		Trc.exit();
		return true;
	}

	/**
	 * Invokes input only operation.  This method 
	 * <ul>
	 * <li>Updates the InteractionSpec using data from the input message.
	 * <li>If a Connection is not currently available creates one, where a
	 * ConnectionSpec can be created using data from the input message and then 
	 * used when creating the Connection.
	 * <li>Uses the Connection to create a javax.resource.cci.Interaction.
	 * <li>Invokes the Interaction execute method.
	 * <li>Closes the interaction.
	 * </ul>
	 */
	public void executeInputOnlyOperation(WSIFMessage input) throws WSIFException {

		Trc.entry(this, input);
        if (input.getParts() == null || !input.getParts().hasNext())
			input = null;
		try {
			fieldFactory.updateInteractionSpec(input, fieldBinding, fieldOperationName, fieldInputName, fieldOutputName, fieldInteractionSpec);
			if (fieldConnection == null){
				fieldConnection = this.fieldFactory.createConnection(input, this.fieldDefinition, this.fieldService, this.fieldPort, this.fieldTypeMap, this.fieldBinding, this.fieldOperationName, this.fieldInputName, this.fieldOutputName);
				fieldJcaPort.setConnection(fieldConnection);
			}
			Interaction interaction = fieldConnection.createInteraction();
			interaction.execute(fieldInteractionSpec, (javax.resource.cci.Record) input);
			interaction.close();
		}
		catch (ResourceException exn1) {
			WSIFException newExn = new WSIFException(WSIFResource_JCA.get("WSIF1000E"));
			Trc.exception(exn1);
			newExn.setTargetException(exn1);
			throw newExn;
		}
		catch (Throwable exn3) {
			WSIFException newExn = new WSIFException(WSIFResource_JCA.get("WSIF1008E", exn3.getLocalizedMessage()));
			newExn.setTargetException(exn3);
			Trc.exception(newExn);
			throw newExn;
		}
		Trc.exit();
	}

	/**
	 * This method creates the fault message. It first attempts to use Resource Adapter specific class
	 * to create the message. If this fails (i.e. the Resource Adapter does not require specialized messages),
	 * the method creates and returns <code>WSIFMessage_JCAStreamable</code> message. 
	 */
	
	public WSIFMessage createFaultMessage() {

		Trc.entry(this);
		WSIFMessage message = this.fieldFactory.createFaultMessage(this.fieldDefinition, this.fieldBinding, this.fieldOperationName, this.fieldInputName, this.fieldOutputName);
		if (message != null)
			return message;
		return new WSIFMessage_JCAStreamable(this.fieldDefinition, this.fieldBinding, this.fieldOperationName, this.fieldInputName, this.fieldOutputName, WSIFMessage_JCA.FAULT_MESSAGE);
	}

	/**
	 * This method creates the fault message with specific name. It first attempts to use Resource Adapter specific class
	 * to create message. If this fails (i.e. the Resource Adapter does not require specialized messages),
	 * the method creates and returns <code>WSIFMessage_JCAStreamable</code> message. 
	 */
	public WSIFMessage createFaultMessage(String name) {

		Trc.entry(this, name);

		WSIFMessage message = this.fieldFactory.createFaultMessage(this.fieldDefinition, this.fieldBinding, this.fieldOperationName, this.fieldInputName, this.fieldOutputName);
		if (message != null) {
			message.setName(name);
			return message;
		}
		message = new WSIFMessage_JCAStreamable(this.fieldDefinition, this.fieldBinding, this.fieldOperationName, this.fieldInputName, this.fieldOutputName, WSIFMessage_JCA.FAULT_MESSAGE);
		message.setName(name);
		return message;
	}

	/**
	 * This method creates the input message. It first attempts to use Resource Adapter specific class
	 * to create message. If this fails (i.e. the Resource Adapter does not require specialized messages),
	 * the method creates and returns <code>WSIFMessage_JCAStreamable</code> message. 
	 */
	public WSIFMessage createInputMessage() {

		Trc.entry(this);
		WSIFMessage message = this.fieldFactory.createInputMessage(this.fieldDefinition, this.fieldBinding, this.fieldOperationName, this.fieldInputName, this.fieldOutputName);
		if (message != null)
			return message;
		return new WSIFMessage_JCAStreamable(this.fieldDefinition, this.fieldBinding, this.fieldOperationName, this.fieldInputName, this.fieldOutputName, WSIFMessage_JCA.INPUT_MESSAGE);
	}

	/**
	 * This method creates the input message with specific name. It first attempts to use Resource Adapter specific class
	 * to create message. If this fails (i.e. the Resource Adapter does not require specialized messages),
	 * the method creates and returns <code>WSIFMessage_JCAStreamable</code> message. 
	 */
	public WSIFMessage createInputMessage(String name) {

		Trc.entry(this, name);
		WSIFMessage message = this.fieldFactory.createInputMessage(this.fieldDefinition, this.fieldBinding, this.fieldOperationName, this.fieldInputName, this.fieldOutputName);
		if (message != null) {
			message.setName(name);
			return message;
		}
		message = new WSIFMessage_JCAStreamable(this.fieldDefinition, this.fieldBinding, this.fieldOperationName, this.fieldInputName, this.fieldOutputName, WSIFMessage_JCA.INPUT_MESSAGE);
		message.setName(name);
		return message;
	}

	/**
	 * This method creates the output message. It first attempts to use Resource Adapter specific class
	 * to create message. If this fails (i.e. the Resource Adapter does not require specialized messages),
	 * the method creates and returns <code>WSIFMessage_JCAStreamable</code> message. 
	 */
	public WSIFMessage createOutputMessage() {

		Trc.entry(this);
		WSIFMessage message = this.fieldFactory.createOutputMessage(this.fieldDefinition, this.fieldBinding, this.fieldOperationName, this.fieldInputName, this.fieldOutputName);
		if (message != null)
			return message;
		return new WSIFMessage_JCAStreamable(this.fieldDefinition, this.fieldBinding, this.fieldOperationName, this.fieldInputName, this.fieldOutputName, WSIFMessage_JCA.OUTPUT_MESSAGE);
	}

	/**
	 * This method creates the output message with specific name. It first attempts to use Resource Adapter specific class
	 * to create message. If this fails (i.e. the Resource Adapter does not require specialized messages),
	 * the method creates and returns <code>WSIFMessage_JCAStreamable</code> message. 
	 */
	public WSIFMessage createOutputMessage(String name) {

		Trc.entry(this, name);
		WSIFMessage message = this.fieldFactory.createOutputMessage(this.fieldDefinition, this.fieldBinding, this.fieldOperationName, this.fieldInputName, this.fieldOutputName);
		if (message != null) {
			message.setName(name);
			return message;
		}
		message = new WSIFMessage_JCAStreamable(this.fieldDefinition, this.fieldBinding, this.fieldOperationName, this.fieldInputName, this.fieldOutputName, WSIFMessage_JCA.OUTPUT_MESSAGE);
		message.setName(name);
		return message;
	}

	/**
	 * Returns the interactionSpec.
	 * @return Returns a InteractionSpec
	 */
	public InteractionSpec getInteractionSpec() {
		return fieldInteractionSpec;
	}

	/**
	 * Sets the interactionSpec.
	 * @param interactionSpec The interactionSpec to set
	 */
	public void setInteractionSpec(InteractionSpec interactionSpec) {
		fieldInteractionSpec = interactionSpec;
	}

	public String toString() {

		StringBuffer buffer = new StringBuffer();
		buffer.append(crlf + "[JCAOperation" + crlf);
		try {
			if (fieldConnection != null)
				buffer.append("\tConnection: " + fieldConnection.toString() + crlf);
			else
				buffer.append("\tConnection: null" + crlf);

			if (fieldInteractionSpec != null)
				buffer.append("\tInteractionSpec:       " + fieldInteractionSpec.toString() + crlf);
			else
				buffer.append("\tInteractionSpec:       null" + crlf);

			if (fieldBinding != null)
				buffer.append("\tBinding:    " + fieldBinding.toString() + crlf);
			else
				buffer.append("\tBinding:    null" + crlf);

			if (fieldOperation != null)
				buffer.append("\tOperation:    " + fieldOperation.toString() + crlf);
			else
				buffer.append("\tOperation:    null" + crlf);

			if (fieldFactory != null)
				buffer.append("\tFactory:    " + fieldFactory.toString() + crlf);
			else
				buffer.append("\tFactory:    null" + crlf);

			if (fieldOperationName != null)
				buffer.append("\tOperationName:    " + fieldOperationName + crlf);
			else
				buffer.append("\tOperationName:    null" + crlf);

			if (fieldInputName != null)
				buffer.append("\tInputName:    " + fieldInputName + crlf);
			else
				buffer.append("\tInputName:    null" + crlf);

			if (fieldOutputName != null)
				buffer.append("\tOutputName:    " + fieldOutputName + crlf);
			else
				buffer.append("\tOutputName:    null" + crlf);

			buffer.append("]" + crlf);
		}
		catch (Throwable exn) {
		}
		return buffer.toString();
	}

	/**
	 * Method not supported.
	 */
	public WSIFCorrelationId executeRequestResponseAsync(WSIFMessage input, WSIFResponseHandler handler) throws WSIFException {
		return null;
	}

	/**
	 * Method not supported.
	 */
	public WSIFCorrelationId executeRequestResponseAsync(WSIFMessage input) throws WSIFException {
		return null;
	}

	/**
	 * Method not supported.
	 */
	public void fireAsyncResponse(Object response) throws WSIFException {
		
	}
	/**
	 * Method not supported.
	 */
	public boolean processAsyncResponse(Object response, WSIFMessage output, WSIFMessage fault) throws WSIFException {
		return false;
	}

	/**
	 * Method not supported.
	 */
	public void setContext(WSIFMessage context) {

	}

	/**
	 * Method not supported.
	 */
	public WSIFMessage getContext() {

		return null;
	}

}
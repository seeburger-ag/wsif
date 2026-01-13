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

/**
 * This class is the top level superclass for all JCA WSIF messages.  It is extended by the connector specific messages, 
 * and implements the javax.resource.cci.Record interface.
 * 
 * @author Michael Beisiegel
 * @author Piotr Przybylski <piotrp@ca.ibm.com>
 */

public class WSIFMessage_JCA extends org.apache.wsif.base.WSIFDefaultMessage implements javax.resource.cci.Record {

    @Serial
    private static final long serialVersionUID = 1L;
	public static final int INPUT_MESSAGE = 1;
	public static final int OUTPUT_MESSAGE = 2;
	public static final int FAULT_MESSAGE = 3;
	
	protected String fieldRecordName = null;
	protected String fieldRecordShortDescription = null;

	protected Definition fieldDefinition = null;
	protected Binding fieldBinding = null;
	protected String fieldOperationName = null;
	protected String fieldInputName = null; 
	protected String fieldOutputName = null;
	protected int fieldMessageType = 0;
	protected javax.resource.cci.InteractionSpec fieldInteractionSpec = null;	
		
	/**
	 * WSIFMessage_JCA Constructor
	 * @param aDefinition A Definition containing the operation to execute.
	 * @param aBinding A service binding
	 * @param aOperationName The name of the operation
	 * @param aInputName Input name
	 * @param aOutputName Output name
	 * @param aMessageType Determines what kind of message is created, input, output or fault. 
	 */
	public WSIFMessage_JCA(Definition aDefinition, Binding aBinding, String aOperationName, String aInputName, String aOutputName, int aMessageType) {
		super();
		this.fieldDefinition = aDefinition;
		this.fieldBinding = aBinding;
		this.fieldOperationName = aOperationName;
		this.fieldInputName = aInputName;
		this.fieldOutputName = aOutputName;
		this.fieldMessageType = aMessageType;
	}

	/**
	 * Gets the recordName.
	 * @return Returns a String
	 */
	public String getRecordName() {
		return fieldRecordName;
	}

	/*
	 * @see Record#setRecordName(String)
	 */
	public void setRecordName(String name) {
		this.fieldRecordName = name;
	}

	/*
	 * @see Record#setRecordShortDescription(String)
	 */
	public void setRecordShortDescription(String desription) {
		this.fieldRecordShortDescription = desription;
	}

	/**
	 * Gets the recordShortDescription.
	 * @return Returns a String
	 */
	public String getRecordShortDescription() {
		return fieldRecordShortDescription;
	}

	/**
	 * Method not supported.
	 */
	public Object clone()throws CloneNotSupportedException{
		throw new CloneNotSupportedException();
	}
	/**
	 * Sets the interactionSpec.
	 * @param interactionSpec The interactionSpec to set
	 */
	public void setInteractionSpec(javax.resource.cci.InteractionSpec interactionSpec) {
		fieldInteractionSpec = interactionSpec;
	}

}
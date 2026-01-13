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
import org.apache.wsif.*;
import org.apache.wsif.format.*;
import org.apache.wsif.providers.jca.WSIFUtils_JCA;
import org.apache.wsif.logging.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import javax.wsdl.*;
import javax.wsdl.extensions.ExtensibilityElement;

/**
 * The class WSIFMessage_JCAStreamable is a specialized version of the WSIFMessage_JCA to support Resource Adapters
 * using javax.resource.cci.Streamable. 
 * 
 * @author Michael Beisiegel
 * @author Piotr Przybylski <piotrp@ca.ibm.com>
 * @author John Green
 */
 
public class WSIFMessage_JCAStreamable extends org.apache.wsif.providers.jca.WSIFMessage_JCA implements javax.resource.cci.Streamable {

    @Serial
    private static final long serialVersionUID = 1L;
	private Message fieldMessageModel = null;
	private java.util.HashMap fieldPartNameFormatHandlerMapping = new java.util.HashMap();

	/**
	 * @see org.apache.wsif.providers.jca.WSIFMessage_JCA#WSIFMessage_JCA(Definition, Binding, String, String, String, int)
	 */
	public WSIFMessage_JCAStreamable(Definition aDefinition, Binding aBinding, String aOperationName, String aInputName, String aOutputName, int aMessageType) {
		super(aDefinition, aBinding, aOperationName, aInputName, aOutputName, aMessageType);
		Operation operation = aBinding.getPortType().getOperation(aOperationName, aInputName, aOutputName);
		switch(aMessageType){
			case WSIFMessage_JCA.INPUT_MESSAGE:
				if(operation.getInput() != null) {
					this.fieldMessageModel = operation.getInput().getMessage();
					setMessageDefinition(this.fieldMessageModel);
				}
				break;
			case WSIFMessage_JCA.OUTPUT_MESSAGE:
				if(operation.getOutput() != null) {
					this.fieldMessageModel = operation.getOutput().getMessage();
					setMessageDefinition(this.fieldMessageModel);
				}
				break;
			case WSIFMessage_JCA.FAULT_MESSAGE:
				break;
			default:
				// Assume input
				this.fieldMessageModel = operation.getInput().getMessage();
				setMessageDefinition(this.fieldMessageModel);
				break;
		}
	}


	/**
	 * The method to read input stream and create message parts. For each part in the message
	 * (as defined in WSDL), with exception of parts representing ConnectionSpec and InteractionSpec properties, 
	 * the format handler is created and its read() method is passed the inputStream. The parts
	 * creation is delayed until they are needed (i.e. when the client invokes <code>getObjectPart</code>).
	 * In this method only the part's format handler is created and stored. 
	 *  
	 * @see javax.resource.cci.Streamable#read(InputStream)
	 */
	public void read(java.io.InputStream inputStream) throws java.io.IOException {

		try {
			Trc.entry(this);
			if(fieldMessageModel == null)
				return;
			HashMap partsToNotProcess = new HashMap();
			BindingOperation bindingOperation = fieldBinding.getBindingOperation(fieldOperationName, fieldInputName, fieldOutputName);
			BindingOutput bindingOutput = bindingOperation.getBindingOutput();
			if (bindingOutput != null) {
				List list = bindingOutput.getExtensibilityElements();
				Iterator inputIterator = list.iterator();
				while (inputIterator.hasNext()) {
					ExtensibilityElement ele = (ExtensibilityElement)inputIterator.next();
					if (ele instanceof WSIFBindingOperation_JCAProperty prop) {
						String partName = prop.getPartName();
						partsToNotProcess.put(partName,partName);
					}
				}
			}
			Iterator iterator = this.fieldMessageModel.getOrderedParts(null).iterator();
			while (iterator.hasNext()) {
				Part part = (Part) iterator.next();
				String partName = part.getName();
				
				if (partsToNotProcess.get(partName) != null) continue;

				WSIFFormatHandler_JCA formatHandler = null;
				if (this.fieldPartNameFormatHandlerMapping.containsKey(partName))
					formatHandler =
						(WSIFFormatHandler_JCA) this.fieldPartNameFormatHandlerMapping.get(partName);
				else {
					formatHandler = (WSIFFormatHandler_JCA) WSIFUtils_JCA.getFormatHandler(part, this.fieldDefinition, this.fieldBinding);
					this.fieldPartNameFormatHandlerMapping.put(partName, formatHandler);
				}
				formatHandler.read(inputStream);
			}
			Trc.exit();
		}
		catch (Exception exn1) {
			Trc.exception(exn1);
			throw new java.io.IOException(WSIFResource_JCA.get("WSIF1004E", exn1.getLocalizedMessage()));
		}
	}
	
	
	/**
	 * Writes the contents of the message parts into the OutputStream. For each part in the message
	 * (as defined in WSDL), except parts representing interactionSpec properties, the format handler
	 * is created, part is set on the format handler and its <code>write</code> method is invoked. 
	 * The format handlers are stored in the table.
	 * 
	 * @see javax.resource.cci.Streamable#write(OutputStream)
	 */
	public void write(java.io.OutputStream outputStream) throws java.io.IOException {

		try {
			Trc.entry(this);

			HashMap partsToNotProcess = new HashMap();
			BindingOperation bindingOperation = fieldBinding.getBindingOperation(fieldOperationName, fieldInputName, fieldOutputName);
			BindingInput bindingInput = bindingOperation.getBindingInput();
			if (bindingInput != null) {
				List list = bindingInput.getExtensibilityElements();
				Iterator inputIterator = list.iterator();
				while (inputIterator.hasNext()) {
					ExtensibilityElement ele = (ExtensibilityElement) inputIterator.next();
					if (ele instanceof WSIFBindingOperation_JCAProperty prop) {
						String partName = prop.getPartName();
						partsToNotProcess.put(partName, partName);
					}
				}
			}

			Iterator iterator = this.getPartNames();
			while (iterator.hasNext()) {
				String partName = (String) iterator.next();
				if (partsToNotProcess.get(partName) != null)
					continue;
				Object oPart = this.parts.get(partName);
				WSIFFormatHandler_JCA formatHandler = null;
				if (oPart instanceof WSIFFormatPart jcaPart) {
					if (jcaPart._getFormatHandler() != null) {
						formatHandler = (WSIFFormatHandler_JCA) jcaPart._getFormatHandler();
						this.fieldPartNameFormatHandlerMapping.put(partName, formatHandler);
					}
				}
				if (formatHandler == null) {
					if (this.fieldPartNameFormatHandlerMapping.containsKey(partName))
						formatHandler = (WSIFFormatHandler_JCA) this.fieldPartNameFormatHandlerMapping.get(partName);
					else {
						if (fieldMessageModel == null)
							return;
						Part part = (Part) this.fieldMessageModel.getPart(partName);
						formatHandler = (WSIFFormatHandler_JCA) WSIFUtils_JCA.getFormatHandler(part, this.fieldDefinition, this.fieldBinding);
						this.fieldPartNameFormatHandlerMapping.put(partName, formatHandler);
					}
					formatHandler.setObjectPart(oPart);
				}
				formatHandler.write(outputStream);
			}
			Trc.exit();
		}
		catch (Exception exn1) {
			Trc.exception(exn1);
			throw new java.io.IOException(WSIFResource_JCA.get("WSIF1005E", exn1.getLocalizedMessage()));

		}

	}

	/**
	 * Returns object part with the given name. If the part had already been created, 
	 * returns it. If there is a format handler for this part, the object part is obtained from 
	 * it and returned, otherwise it creates the format handler and returns 
	 * the object parts form it.
	 * 
	 * @see org.apache.wsif.WSIFMessage#getObjectPart(String)
	 */
	public Object getObjectPart(String partName) {

		Trc.entry(this, partName);

		if(this.parts != null){
			Object existingPart = this.parts.get(partName);
			if (existingPart != null)
				return existingPart;
		}
		try {
			WSIFFormatHandler_JCA formatHandler =
				(WSIFFormatHandler_JCA) this.fieldPartNameFormatHandlerMapping.get(partName);
			if (formatHandler != null) {
				if(this.fieldInteractionSpec != null)
					formatHandler.setInteractionSpec(this.fieldInteractionSpec);
				Object retPart = formatHandler.getObjectPart();
				this.setObjectPart(partName, retPart);
				Trc.exit(retPart);
				return retPart;
			}
			else {
				if(fieldMessageModel == null)
					return null;
				Part part = (Part) this.fieldMessageModel.getPart(partName);
				if (part == null) return null;
				formatHandler = (WSIFFormatHandler_JCA)WSIFUtils_JCA.getFormatHandler(part, this.fieldDefinition, this.fieldBinding);
				if(this.fieldInteractionSpec != null)
					formatHandler.setInteractionSpec(this.fieldInteractionSpec);
				Object retPart = formatHandler.getObjectPart();
				this.setObjectPart(partName, retPart);
				Trc.exit(retPart);
				return retPart;
			}

		}
		catch (Exception exn) {
			Trc.exception(exn);
			throw new RuntimeException(WSIFResource_JCA.get("WSIF1007E", exn.getLocalizedMessage()));
		}
	}

	/**
	 * Returns object part with the given name and requested representation. If the part had already 
	 * been created, it is returned. If there is a format handler for this part, it gets the object part from the
	 * format handler and returns it, otherwise the format handler is created and its 
	 * object part is returned.
	 * 
	 * @see org.apache.wsif.WSIFMessage#getObjectPart(String)
	 */
	public Object getObjectPart(String partName, Class sourceClass) {

		Trc.entry(this, partName, sourceClass);
			
		try {
			if (this.parts != null) {
				Object existingPart = this.parts.get(partName);
				if (existingPart != null) {
					if (sourceClass.isAssignableFrom(existingPart.getClass())){
						Trc.exit(existingPart);
						return existingPart;
					}
				}
			}
		}
		catch (Exception exn) {
			Trc.exception(exn);
		}

		try {
			WSIFFormatHandler_JCA formatHandler = (WSIFFormatHandler_JCA) this.fieldPartNameFormatHandlerMapping.get(partName);
			if (formatHandler != null) {
				if(this.fieldInteractionSpec != null)
					formatHandler.setInteractionSpec(this.fieldInteractionSpec);
				Object retSource = formatHandler.getObjectPart(sourceClass);
				this.setObjectPart(partName, retSource);
				Trc.exit(retSource);
				return retSource;
			}
			else {
				if(fieldMessageModel == null)
					return null;
				Part part = (Part) this.fieldMessageModel.getPart(partName);
				if (part == null) return null;
				formatHandler = (WSIFFormatHandler_JCA)WSIFUtils_JCA.getFormatHandler(part, this.fieldDefinition, this.fieldBinding);
				if(this.fieldInteractionSpec != null)
					formatHandler.setInteractionSpec(this.fieldInteractionSpec);
				Object retSource = formatHandler.getObjectPart(sourceClass);
				this.setObjectPart(partName, retSource);
				Trc.exit(retSource);
				return retSource;
			}
		}
		catch (Exception exn) {
			Trc.exception(exn);
			throw new RuntimeException(WSIFResource_JCA.get("WSIF1007E", exn.getLocalizedMessage()));
		}
	}

	/**
	 * @see org.apache.wsif.WSIFMessage#getPartNames()
	 */
    public Iterator getPartNames() {
		
		try{	
		    if(this.fieldMessageModel == null)
		    	return null;
	    	return this.fieldMessageModel.getParts().keySet().iterator();
		}
		catch(Throwable exn){
			return null;
		}
    }

	/**
	 * @see org.apache.wsif.WSIFMessage#getParts()
	 */
    public Iterator getParts() {
    	
		try{
			Iterator partNames = this.getPartNames();
			while(partNames.hasNext()){
				String nextName = (String)partNames.next();
				this.getObjectPart(nextName);
			}
			return(this.parts.values().iterator());
		}
		catch(Throwable exn){
			return null;
		}
    }


}

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

package com.myeis.wsdl.extensions.j2c.myeis;

import java.io.*;
import java.util.*;
import javax.wsdl.*;
import javax.xml.namespace.*;
import javax.wsdl.extensions.*;
import com.ibm.wsdl.*;
import com.ibm.wsdl.util.xml.*;

/**
 * Insert the type's description here.
 * Creation date: (5/21/2001 8:48:12 PM)
 * @author: Administrator
 */
public class MyEISBindingSerializer
	implements
		javax.wsdl.extensions.ExtensionDeserializer,
		javax.wsdl.extensions.ExtensionSerializer,
		java.io.Serializable {
	/**
	 * ConnectorBindingSerializer constructor comment.
	 */
	public MyEISBindingSerializer() {
		super();
	}

	/**
	  * 
	  */
	public void marshall(
		Class parentType,
		javax.xml.namespace.QName extensionType,
		javax.wsdl.extensions.ExtensibilityElement extension,
		java.io.PrintWriter pw,
		javax.wsdl.Definition def,
		javax.wsdl.extensions.ExtensionRegistry extReg)
		throws javax.wsdl.WSDLException {

		if (extension == null)
			return;

		//Get the EIS namespace prefix from this definition	
		String TPrefix = null;
		Map nameSpaces = def.getNamespaces();
		Iterator i = nameSpaces.keySet().iterator();

		while (i.hasNext()) {
			String prefix = (String) i.next();
			String namespaceURI = (String) nameSpaces.get(prefix);
			if (namespaceURI.equals(MyEISBindingConstants.NS_URI)) {
				TPrefix = prefix;
				break;
			}
		}

		//Check if the namespace prefix was found. if not thrwo an exception
		if (TPrefix == null)
			throw new WSDLException(
				"001",
				"The EIS binding namespace was not found in the definition");

		if (extension instanceof MyEISBinding) {
			MyEISBinding binding = (MyEISBinding) extension;
			pw.print("      <" + TPrefix + ":binding");

			Boolean required = extension.getRequired();
			if (required != null) {
				DOMUtils.printQualifiedAttribute(
					Constants.Q_ATTR_REQUIRED,
					required.toString(),
					def,
					pw);
			}

			pw.println("/>");
		} else
			if (extension instanceof MyEISOperation) {
				MyEISOperation operation = (MyEISOperation) extension;
				pw.print("      <" + TPrefix + ":operation");
				
				if (operation.getFunctionName() != null) {
					DOMUtils.printAttribute("functionName", operation.getFunctionName(), pw);
				}

				Boolean required = extension.getRequired();
				if (required != null) {
					DOMUtils.printQualifiedAttribute(
						Constants.Q_ATTR_REQUIRED,
						required.toString(),
						def,
						pw);
				}

				pw.println("/>");
			} else
				if (extension instanceof MyEISAddress) {
					MyEISAddress address = (MyEISAddress) extension;
					pw.print("      <" + TPrefix + ":address");

					if (address.getRepositoryLocation() != null) {
						DOMUtils.printAttribute("repositoryLocation", address.getRepositoryLocation(), pw);
					}

					Boolean required = extension.getRequired();
					if (required != null) {
						DOMUtils.printQualifiedAttribute(
							Constants.Q_ATTR_REQUIRED,
							required.toString(),
							def,
							pw);
					}
					pw.println("/>");
				}
	}
	/**
	 * unmarshall method comment.
	 */
	public javax.wsdl.extensions.ExtensibilityElement unmarshall(
		Class parentType,
		javax.xml.namespace.QName elementType,
		org.w3c.dom.Element el,
		javax.wsdl.Definition def,
		javax.wsdl.extensions.ExtensionRegistry extReg)
		throws javax.wsdl.WSDLException {

		javax.wsdl.extensions.ExtensibilityElement returnValue = null;

		if (MyEISBindingConstants.Q_ELEM_BINDING.equals(elementType)) {
			MyEISBinding binding = new MyEISBinding();

			return binding;
		} else
			if (MyEISBindingConstants.Q_ELEM_OPERATION.equals(elementType)) {
				MyEISOperation operation = new MyEISOperation();
						
				String functionName = DOMUtils.getAttribute(el, "functionName");
				if (functionName != null) {
					operation.setFunctionName(functionName);
				}
				return operation;
			} else
				if (MyEISBindingConstants.Q_ELEM_ADDRESS.equals(elementType)) {
					MyEISAddress address = new MyEISAddress();

					String repositoryLocation = DOMUtils.getAttribute(el, "repositoryLocation");
					if (repositoryLocation != null) {
						address.setRepositoryLocation(repositoryLocation);
					}
					return address;
				}
					
		return returnValue;
	}
}
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

import javax.wsdl.*;

/**
 * The <code>PortTypeSelection</code> class is the representation of a selected PortType
 * that is passed to the <code>getDefinition</code> operation of the ImportService. Tool environments
 * are responsible for constructing instances of this object and passing them to the Import service.
 *  @author Hesham Fahmy <hfahmy@ca.ibm.com>
 *  @author Piotr Przybylski <piotrp@ca.ibm.com>
 */
public class PortTypeSelection implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private javax.xml.namespace.QName fieldPortTypeQName = null;
	private OperationSelection[] fieldOperationSelection = null;

	/**
	 * Constructs a <code>PortTypeSelection</code> for a given WSDL PortType and selected WSDL Operations
	 * @param <code>portTypeQName</code> The QName of the WSDL PortType selected
	 * @param <code>operationSelection</code> The set of WSDL operations that are selected for the given WSDL PortType.
	 */
	public PortTypeSelection(javax.xml.namespace.QName portTypeQName, OperationSelection[] operationSelection) {
		
		this.fieldPortTypeQName = portTypeQName;
		this.fieldOperationSelection = operationSelection;
	}

	/**
	 * Returns the QName of the selected WSDL PortType.
	 * @return QName
	 */
	public javax.xml.namespace.QName getPortTypeQName() {
		return this.fieldPortTypeQName;
	}
	
	/**
	 * Sets the QName of the selected WSDL PortType.
	 * @param <code>portTypeQName</code> The QName to set
	 */
	public void setPortTypeQName(javax.xml.namespace.QName portTypeQName) {
		this.fieldPortTypeQName = portTypeQName;
	}


	/**
	 * Returns the selected WSDL Operations for the PortType. The selected operations are represented by an array of <code>OperationSelection</code>
	 * objects.
	 * @return Returns a OperationSelection[]
	 */
	public OperationSelection[] getOperationSelection() {
		return fieldOperationSelection;
	}

	/**
	 * Sets the selected WSDL Operations for the PortType.
	 * @param <code>operationSelection</code> The operationSelection to set.
	 */
	public void setOperationSelection(OperationSelection[] operationSelection) {
		fieldOperationSelection = operationSelection;
	}

}
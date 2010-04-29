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
 * The <code>OperationSelection</code> class is the representation of a selected Operation
 * that is passed to the <code>getDefinition</code> operation of the ImportService. Tool environments
 * are responsible for constructing instances of this object and passing them to the Import service.
 *  @author Hesham Fahmy <hfahmy@ca.ibm.com>
 *  @author Piotr Przybylski <piotrp@ca.ibm.com>
 */
public class OperationSelection implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private javax.xml.namespace.QName fieldPortTypeQName = null;
	private String fieldOperationName = null;
	private String fieldInputName = null;
	private String fieldOutputName = null;

	public OperationSelection(String operationName, String inputName, String outputName) {
		
		this.fieldOperationName = operationName;
		this.fieldInputName = inputName;
		this.fieldOutputName = outputName;
	
	}
	public String getOperationName() {
		return this.fieldOperationName;
	}
	public String getInputName() {
		return this.fieldInputName;
	}
	public String getOutputName() {
		return this.fieldOutputName;
	}
	
	public void setOperationName(String operationName) {
		this.fieldOperationName = operationName;
	}
	public void setInputName(String inputName) {
		this.fieldInputName = inputName;
	}
	public void setOutputName(String outputName) {
		this.fieldOutputName = outputName;
	}			


}
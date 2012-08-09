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
 * This class represents the type returned from the getDefinition operation.
 * The <code>ImportDefinition</code> class contains the WSDL definition of the imported service, along with
 * all necessary XSD types and additional binary resources that are required.
 * 
 *  @author Hesham Fahmy <hfahmy@ca.ibm.com>
 *  @author Piotr Przybylski <piotrp@ca.ibm.com>
 */
public class ImportDefinition implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	private Definition fieldDefinition = null;
	private ImportXSD[] fieldImportXSDs = null;
	private ImportResource[] fieldImportResources = null;
	
	/**
	 * Returns the WSDL Definition representing the Imported Service.
	 * @return a WSDL Definition
	 */
	public Definition getDefinition() {
		return fieldDefinition;
	}
	/**
	 * Sets WSDL representing the Imported Service.
	 * @param <code>definition</code> - The WSDL Definition
	 */
	public void setDefinition(Definition definition) {
		this.fieldDefinition = definition;
	}
	/**
	 * Returns the XSD types that may be required for the Imported Service.  
	 * 
	 * @return Returns an array of ImportXSD types
	 */
	public ImportXSD[] getImportXSDs() {
		return fieldImportXSDs;
	}
	/**
	 * Sets the XSD types that may be required for the Imported Service
	 * @param <code>importXSDs</code> The array of ImportXSD types
	 */
	public void setImportXSDs(ImportXSD[] importXSDs) {
		this.fieldImportXSDs = importXSDs;
	}
	/**
	 * Returns the binary resources that may be required for the Imported Service
	 * @return Returns an array of ImportResource types
	 */
	public ImportResource[] getImportResources() {
		return fieldImportResources;
	}

	/**
	 * Sets the binary resources that may be required for the Imported Service
	 * @param <code>importResources</code>  The array of ImportResource types
	 */
	public void setImportResources(ImportResource[] importResources) {
		this.fieldImportResources = importResources;
	}
}


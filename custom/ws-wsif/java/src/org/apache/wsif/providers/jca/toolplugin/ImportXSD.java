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
/**
 * This class represents an XSD definition that may be required for an Imported Service. The Resource Adapter
 * does not need to implement this class if it returns XSD types inlined within Definition.
 *  @author Hesham Fahmy <hfahmy@ca.ibm.com>
 *  @author Piotr Przybylski <piotrp@ca.ibm.com>
 */

import java.io.Serial;

public class ImportXSD implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
	private String fieldNamespace = null;
	private String fieldLocation = null;
	private String fieldSource = null;

	/**
	 * Returns the namespace that is associated with the XSD definition
	 * @return Returns a String representation of the namespace
	 */
	public String getNamespace() {
		return fieldNamespace;
	}
	/**
	 * Sets the namespace that is associated with the XSD definition
	 * @param <code>namespace</code> The namespace to set
	 */
	public void setNamespace(String namespace) {
		this.fieldNamespace = namespace;
	}
	/**
	 * Returns a String representation of the relative path where the XSD should be saved.
	 * The path is relative to the location of the WSDL document that represents the imported service.
	 * @return Returns a String
	 */
	public String getLocation() {
		return fieldLocation;
	}
	/**
	 * Sets the relative path where the XSD should be saved.
	 * @param <code>location</code> The location to set
	 */
	public void setLocation(String location) {
		this.fieldLocation = location;
	}
	/**
	 * Gets the contents of the XSD definiton
	 * @return Returns a String representation of the contents of the XSD
	 */
	public String getSource() {
		return fieldSource;
	}
	/**
	 * Sets the contents of the XSD definiton.
	 * @param <code>source</code> The XSD source to set
	 */
	public void setSource(String source) {
		this.fieldSource = source;
	}
}


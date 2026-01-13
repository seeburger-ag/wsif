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
 * This class represents a binary resource that may required for an Imported Service.
 *  @author Hesham Fahmy <hfahmy@ca.ibm.com>
 */
public class ImportResource implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
	private String fieldLocation = null;
	private byte[] fieldContents = null;

	/**
	 * Returns a String representation of the relative path where the resource should be saved.
	 * The path is relative to the location of the WSDL document that represents the imported service.
	 * @return Returns a String
	 */
	public String getLocation() {
		return fieldLocation;
	}
	/**
	 * Sets the relative path where the resource should be saved.
	 * @param <code>location</code> The location to set
	 */
	public void setLocation(String location) {
		this.fieldLocation = location;
	}
	/**
	 * Returns the binary contents of the resource as a byte array.
	 * @return byte[] - the binary contents of the resource
	 */
	public byte[] getContents() {
		return fieldContents;
	}

	/**
	 * Sets the binary contents of the resource.
	 * @param <code>contents</code> The byte array to set.
	 */
	public void setContents(byte[] contents) {
		this.fieldContents = contents;
	}

}


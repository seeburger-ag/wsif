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

package com.myeis.services.internal;

public class CustomerDataObject implements java.io.Serializable {
	
	private String fieldNumber = null;
	private String fieldFirstName = null;
	private String fieldLastName = null;

	/**
	 * Gets the fieldNumber
	 * @return Returns a String
	 */
	public String getNumber() {
		return fieldNumber;
	}
	/**
	 * Sets the fieldNumber
	 * @param number The fieldNumber to set
	 */
	public void setNumber(String number) {
		this.fieldNumber = number;
	}
	/**
	 * Gets the fieldFirstName
	 * @return Returns a String
	 */
	public String getFirstName() {
		return fieldFirstName;
	}
	/**
	 * Sets the fieldFirstName
	 * @param firstName The fieldFirstName to set
	 */
	public void setFirstName(String firstName) {
		this.fieldFirstName = firstName;
	}
	/**
	 * Gets the fieldLastName
	 * @return Returns a String
	 */
	public String getLastName() {
		return fieldLastName;
	}
	/**
	 * Sets the fieldLastName
	 * @param lastName The fieldLastName to set
	 */
	public void setLastName(String lastName) {
		this.fieldLastName = lastName;
	}

}


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

public class AddressDataObject implements java.io.Serializable {

	private String fieldCustomerNumber = null;
	private String fieldStreet = null;
	private String fieldCity = null;
	private String fieldState = null;
	private String fieldZipCode = null;

	/**
	 * Gets the fieldCustomerNumber
	 * @return Returns a String
	 */
	public String getCustomerNumber() {
		return fieldCustomerNumber;
	}
	/**
	 * Sets the fieldCustomerNumber
	 * @param fieldCustomerNumber The fieldCustomerNumber to set
	 */
	public void setCustomerNumber(String fieldCustomerNumber) {
		this.fieldCustomerNumber = fieldCustomerNumber;
	}
	/**
	 * Gets the fieldStreet
	 * @return Returns a String
	 */
	public String getStreet() {
		return fieldStreet;
	}
	/**
	 * Sets the fieldStreet
	 * @param fieldStreet The fieldStreet to set
	 */
	public void setStreet(String fieldStreet) {
		this.fieldStreet = fieldStreet;
	}
	/**
	 * Gets the fieldCity
	 * @return Returns a String
	 */
	public String getCity() {
		return fieldCity;
	}
	/**
	 * Sets the fieldCity
	 * @param fieldCity The fieldCity to set
	 */
	public void setCity(String fieldCity) {
		this.fieldCity = fieldCity;
	}
	/**
	 * Gets the fieldState
	 * @return Returns a String
	 */
	public String getState() {
		return fieldState;
	}
	/**
	 * Sets the fieldState
	 * @param fieldState The fieldState to set
	 */
	public void setState(String fieldState) {
		this.fieldState = fieldState;
	}
	/**
	 * Gets the fieldZipCode
	 * @return Returns a String
	 */
	public String getZipCode() {
		return fieldZipCode;
	}
	/**
	 * Sets the fieldZipCode
	 * @param fieldZipCode The fieldZipCode to set
	 */
	public void setZipCode(String fieldZipCode) {
		this.fieldZipCode = fieldZipCode;
	}
}


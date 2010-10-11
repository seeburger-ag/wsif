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
public class PurchaseOrderDataObject implements java.io.Serializable {

	private String fieldOrderNumber;
	private String fieldPartNumber;
	private String fieldDescription;
	private int fieldQuantity;
	private double fieldUnitPrice;
	
	/**
	 * Gets the fieldOrderNumber
	 * @return Returns a String
	 */
	public String getOrderNumber() {
		return fieldOrderNumber;
	}
	/**
	 * Sets the fieldOrderNumber
	 * @param fieldOrderNumber The fieldOrderNumber to set
	 */
	public void setOrderNumber(String fieldOrderNumber) {
		this.fieldOrderNumber = fieldOrderNumber;
	}

	/**
	 * Gets the fieldPartNumber
	 * @return Returns a String
	 */
	public String getPartNumber() {
		return fieldPartNumber;
	}
	/**
	 * Sets the fieldPartNumber
	 * @param fieldPartNumber The fieldPartNumber to set
	 */
	public void setPartNumber(String fieldPartNumber) {
		this.fieldPartNumber = fieldPartNumber;
	}

	/**
	 * Gets the fieldDescription
	 * @return Returns a String
	 */
	public String getDescription() {
		return fieldDescription;
	}
	/**
	 * Sets the fieldDescription
	 * @param fieldDescription The fieldDescription to set
	 */
	public void setDescription(String fieldDescription) {
		this.fieldDescription = fieldDescription;
	}

	/**
	 * Gets the fieldQuantity
	 * @return Returns a int
	 */
	public int getQuantity() {
		return fieldQuantity;
	}
	/**
	 * Sets the fieldQuantity
	 * @param fieldQuantity The fieldQuantity to set
	 */
	public void setQuantity(int fieldQuantity) {
		this.fieldQuantity = fieldQuantity;
	}

	/**
	 * Gets the fieldUnitPrice
	 * @return Returns a double
	 */
	public double getUnitPrice() {
		return fieldUnitPrice;
	}
	/**
	 * Sets the fieldUnitPrice
	 * @param fieldUnitPrice The fieldUnitPrice to set
	 */
	public void setUnitPrice(double fieldUnitPrice) {
		this.fieldUnitPrice = fieldUnitPrice;
	}

}


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

import javax.wsdl.*;
import javax.xml.namespace.*;

/**
 * Insert the type's description here.
 * Creation date: (6/23/2001 8:34:57 PM)
 * @author: Administrator
 */
public class MyEISAddress
	implements java.io.Serializable, javax.wsdl.extensions.ExtensibilityElement {

	private javax.xml.namespace.QName fieldElementType = MyEISBindingConstants.Q_ELEM_ADDRESS;
	private Boolean required = null;

	private String fieldRepositoryLocation = "local";

	/**
	 * EISAddress constructor comment.
	 */
	public MyEISAddress() {
		super();
	}
	/**
	  * Get the type of this extensibility element. 
	  *
	  * @return the extensibility element's type
	  */
	public javax.xml.namespace.QName getElementType() {
		return fieldElementType;
	}
	/**
	  * Get whether or not the semantics of this extension
	  * are required. Relates to the wsdl:required attribute.
	  */
	public Boolean getRequired() {
		return required;
	}	
	/**
	 * Insert the method's description here.
	 * Creation date: (7/25/2001 3:17:56 AM)
	 * @return java.lang.String
	 */
	public java.lang.String getRepositoryLocation() {
		return fieldRepositoryLocation;
	}
	/**
	  * Set the type of this extensibility element.
	  *
	  * @param elementType the type
	  */
	public void setElementType(javax.xml.namespace.QName elementType) {
		this.fieldElementType = elementType;
	}
	/**
	  * Set whether or not the semantics of this extension
	  * are required. Relates to the wsdl:required attribute.
	  */
	public void setRequired(Boolean required) {
		this.required = required;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (7/25/2001 3:17:56 AM)
	 * @param newLanguage java.lang.String
	 */
	public void setRepositoryLocation(java.lang.String repositoryLocation) {
		fieldRepositoryLocation = repositoryLocation;
	}
}
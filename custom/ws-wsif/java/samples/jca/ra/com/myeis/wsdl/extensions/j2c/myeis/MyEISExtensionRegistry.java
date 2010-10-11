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

/**
 * Insert the type's description here.
 * Creation date: (12/09/2001 1:07:45 PM)
 * @author: Administrator
 */
public class MyEISExtensionRegistry
	extends javax.wsdl.extensions.ExtensionRegistry {
	/**
	 * Insert the method's description here.
	 * Creation date: (12/09/2001 1:08:17 PM)
	 */
	public MyEISExtensionRegistry() {
		MyEISBindingSerializer ser = new MyEISBindingSerializer();
		// binding	
		this.registerSerializer(javax.wsdl.Binding.class, MyEISBindingConstants.Q_ELEM_BINDING, ser);
		this.registerDeserializer(
			javax.wsdl.Binding.class,
			MyEISBindingConstants.Q_ELEM_BINDING,
			ser);

		// operation
		this.registerSerializer(
			javax.wsdl.BindingOperation.class,
			MyEISBindingConstants.Q_ELEM_OPERATION,
			ser);
		this.registerDeserializer(
			javax.wsdl.BindingOperation.class,
			MyEISBindingConstants.Q_ELEM_OPERATION,
			ser);

		// address
		this.registerSerializer(javax.wsdl.Port.class, MyEISBindingConstants.Q_ELEM_ADDRESS, ser);
		this.registerDeserializer(
			javax.wsdl.Port.class,
			MyEISBindingConstants.Q_ELEM_ADDRESS,
			ser);
	}

}
/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
 * Business Machines, Inc., http://www.ibm.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.wsif.mapping;

import javax.xml.namespace.QName;

/**
 * A WSIFMappingConvention defines a convention for converting an xml name
 * to a Java class name. Different techniques can be applied to complexTypes
 * simpleTypes and elements
 * 
 * @author Owen Burroughs <owenb@uk.ibm.com>
 */
public interface WSIFMappingConvention {
	
	/**
	 * Get a class name for a complexType
	 * @param qn The name of the complexType
	 * @return The Java class name
	 */
	public String getClassNameForComplexType(QName qn);

	/**
	 * Get a class name for a simpleType
	 * @param qn The name of the simpleType
	 * @return The Java class name
	 */	
	public String getClassNameForSimpleType(QName qn);

	/**
	 * Get a class name for a global element
	 * @param qn The name of the element
	 * @return The Java class name
	 */	
	public String getClassNameForElementType(QName qn);

	/**
	 * Override the mapping from a namespace to a package name for a specific 
	 * namespace
	 * @param namespace The namespace to map
	 * @param packageName The package name to map the namespace to
	 */
	public void overridePackageMapping(String namespace, String packageName);		
}

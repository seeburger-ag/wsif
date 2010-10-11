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

import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.wsif.schema.SchemaType;

/**
 * A WSIFMapper defines a way of producing a list of QName
 * to Java class name mappings. It defines the logic for determining
 * what type of mapping is required for a particular 
 * {@link org.apache.wsif.schema.SchemaType} The mapping is then produced
 * by an associated {@link org.apache.wsif.mapping.WSIFMappingConvention}
 * 
 * @author Owen Burroughs <owenb@uk.ibm.com>
 */
public interface WSIFMapper {

	/**
	 * Set a flag to indicate whether or not the list of mappings returned
	 * by the getMappings method includes the standard JAX-RPC mappings for
	 * simple types.
	 * @param b The flag
	 */
	public void setIncludeStandardMappings(boolean b);

	/**
	 * Get the flag indicating whether or not the list of mappings returned
	 * by the getMappings method includes the standard JAX-RPC mappings for
	 * simple types.
	 * @return The flag
	 */
	public boolean getIncludeStandardMappings();

	/**
	 * Set the {@link WSIFMappingConvention} used by the WSIFMapper
	 * @param con The {@link WSIFMappingConvention} to use
	 */
    public void setMappingConvention(WSIFMappingConvention con);

	/**
	 * Get the {@link WSIFMappingConvention} used by the WSIFMapper
	 * @return The {@link WSIFMappingConvention}
	 */
	public WSIFMappingConvention getMappingConvention();	
	
	/**
	 * Get a map of xml name to Java class name (QName to String) mappings
	 * from a array of {@link SchemaType} objects
	 * @param types An array of {@link SchemaType} objects to create the
	 * mappings for
	 * @return A map of xml name to Java class name mappings
	 */
    public Map getMappings(SchemaType[] types);
    
    /**
     * Override a generated mapping for a particular xml name with this information
     * @param xmlName The xml name of the mapping
     * @param className The class name for the mapping
     */
    public void overrideTypeMapping(QName xmlName, String className);		
}

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
 
 
package org.apache.wsif.providers.jca;

/**
 * An interface which is used to expose InteractionSpec or ConnectionSpec properties 
 * as data at runtime by using parts in a message.
 * @author John Green
 */
public interface WSIFBindingOperation_JCAProperty {
	/**
	 * Returns the name of the part which contains the InteractionSpec or ConnectionSpec property value.
	 */
	public String getPartName();

	/**
	 * Returns the name of the InteractionSpec or ConnectionSpec property which is being stored.
	 */	
	public String getPropertyName();
	
	/**
	 * Sets the name of the part which contains the InteractionSpec or ConnectionSpec property value.
	 */
	public void setPartName(String partName);
	
	/**
	 * Sets the name of the InteractionSpec or ConnectionSpec property which is being stored. 
	 */
	public void setPropertyName(String propertyName);

}


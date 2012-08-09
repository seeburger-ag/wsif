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

package com.myeis.j2c;

import java.io.PrintWriter;
import java.util.Set;

import javax.resource.*;
import javax.resource.spi.*;
import javax.security.auth.*;

public class MyEISManagedConnectionFactory implements ManagedConnectionFactory {

	private String fieldRepositoryLocation = null;

	/**
	 * @see ManagedConnectionFactory#createConnectionFactory(ConnectionManager)
	 */
	public Object createConnectionFactory(ConnectionManager connectionManager)
		throws ResourceException {
			
			
		return new MyEISConnectionFactory(connectionManager, this);
	}

	/**
	 * @see ManagedConnectionFactory#createConnectionFactory()
	 */
	public Object createConnectionFactory() throws ResourceException {
		
		return new MyEISConnectionFactory(null, this);
	}

	/**
	 * @see ManagedConnectionFactory#createManagedConnection(Subject, ConnectionRequestInfo)
	 */
	public ManagedConnection createManagedConnection(
		Subject subject,
		ConnectionRequestInfo connectionRequestInfo)
		throws ResourceException {
			
		return new MyEISManagedConnection(subject, connectionRequestInfo, this);
	}

	/**
	 * @see ManagedConnectionFactory#matchManagedConnections(Set, Subject, ConnectionRequestInfo)
	 */
	public ManagedConnection matchManagedConnections(
		Set arg0,
		Subject arg1,
		ConnectionRequestInfo arg2)
		throws ResourceException {
		return null;
	}

	/**
	 * @see ManagedConnectionFactory#getLogWriter()
	 */
	public PrintWriter getLogWriter() throws ResourceException {
		return null;
	}
	
	/**
	 * @see ManagedConnectionFactory#setLogWriter(PrintWriter)
	 */
	public void setLogWriter(PrintWriter arg0) throws ResourceException {
	}

	/**
	 * Gets the fieldRepositoryLocation
	 * @return Returns a String
	 */
	public String getRepositoryLocation() {
		return fieldRepositoryLocation;
	}
	/**
	 * Sets the fieldRepositoryLocation
	 * @param fieldRepositoryLocation The fieldRepositoryLocation to set
	 */
	public void setRepositoryLocation(String fieldRepositoryLocation) {
		this.fieldRepositoryLocation = fieldRepositoryLocation;
	}

}


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

import javax.resource.*;
import javax.resource.cci.*;
import javax.resource.spi.*;

public class MyEISConnection implements Connection {

	private MyEISManagedConnection fieldManagedConnection = null;

	/**
	 * Constructor
	 */
	public MyEISConnection(ManagedConnection managedConnection) {
		
		this.fieldManagedConnection = (MyEISManagedConnection)managedConnection;
	}

	/**
	 * @see Connection#createInteraction()
	 */
	public Interaction createInteraction() throws ResourceException {
		
		return new MyEISInteraction(this);
	}

	/**
	 * @see Connection#getLocalTransaction()
	 */
	public javax.resource.cci.LocalTransaction getLocalTransaction() throws ResourceException {
		return null;
	}

	/**
	 * @see Connection#setAutoCommit(boolean)
	 */
	public void setAutoCommit(boolean arg0) throws ResourceException {
	}

	/**
	 * @see Connection#getAutoCommit()
	 */
	public boolean getAutoCommit() throws ResourceException {
		return false;
	}

	/**
	 * @see Connection#getMetaData()
	 */
	public ConnectionMetaData getMetaData() throws ResourceException {
		return null;
	}

	/**
	 * @see Connection#getResultSetInfo()
	 */
	public ResultSetInfo getResultSetInfo() throws ResourceException {
		return null;
	}

	/**
	 * @see Connection#close()
	 */
	public void close() throws ResourceException {
		
		this.fieldManagedConnection.close();
		this.fieldManagedConnection = null;
	}

	/**
	 * call
	 */
	public boolean call(InteractionSpec interactionSpec, Record inputRecord, Record outputRecord)
		throws ResourceException {
			
		return this.fieldManagedConnection.call(interactionSpec, inputRecord, outputRecord);
	}

}


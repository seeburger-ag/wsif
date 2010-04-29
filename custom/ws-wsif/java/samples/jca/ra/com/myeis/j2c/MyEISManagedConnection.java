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

import java.io.*;
import javax.resource.*;
import javax.resource.cci.*;
import javax.resource.spi.*;
import javax.security.auth.*;
import javax.transaction.xa.XAResource;
import org.apache.wsif.providers.jca.toolplugin.*;
import org.apache.wsif.providers.jca.WSIFFormatHandler_JCA;
import com.myeis.repository.*;
import com.myeis.*;
import org.apache.wsif.WSIFException;
import org.apache.wsif.providers.jca.*;

public class MyEISManagedConnection implements ManagedConnection {

	private Subject fieldSubject = null;
	private ConnectionRequestInfo fieldConnectionRequestInfo = null;
	private MyEISManagedConnectionFactory fieldManagedConnectionFactory = null;

	/**
	 * Constructor
	 */
	public MyEISManagedConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo, ManagedConnectionFactory managedConnectionFactory) {

		this.fieldSubject = subject;
		this.fieldConnectionRequestInfo = connectionRequestInfo;
		this.fieldManagedConnectionFactory = (MyEISManagedConnectionFactory) managedConnectionFactory;
	}

	/**
	 * @see ManagedConnection#getConnection(Subject, ConnectionRequestInfo)
	 */
	public Object getConnection(Subject arg0, ConnectionRequestInfo arg1) throws ResourceException {

		return new MyEISConnection(this);
	}

	/**
	 * @see ManagedConnection#destroy()
	 */
	public void destroy() throws ResourceException {
	}

	/**
	 * @see ManagedConnection#cleanup()
	 */
	public void cleanup() throws ResourceException {
	}

	/**
	 * @see ManagedConnection#associateConnection(Object)
	 */
	public void associateConnection(Object arg0) throws ResourceException {
	}

	/**
	 * @see ManagedConnection#addConnectionEventListener(ConnectionEventListener)
	 */
	public void addConnectionEventListener(ConnectionEventListener arg0) {
	}

	/**
	 * @see ManagedConnection#removeConnectionEventListener(ConnectionEventListener)
	 */
	public void removeConnectionEventListener(ConnectionEventListener arg0) {
	}

	/**
	 * @see ManagedConnection#getXAResource()
	 */
	public XAResource getXAResource() throws ResourceException {
		return null;
	}

	/**
	 * @see ManagedConnection#getLocalTransaction()
	 */
	public javax.resource.spi.LocalTransaction getLocalTransaction() throws ResourceException {
		return null;
	}

	/**
	 * @see ManagedConnection#getMetaData()
	 */
	public ManagedConnectionMetaData getMetaData() throws ResourceException {
		return null;
	}

	/**
	 * @see ManagedConnection#setLogWriter(PrintWriter)
	 */
	public void setLogWriter(PrintWriter arg0) throws ResourceException {
	}

	/**
	 * @see ManagedConnection#getLogWriter()
	 */
	public PrintWriter getLogWriter() throws ResourceException {
		return null;
	}

	/**
	 * close()
	 */
	public void close() throws ResourceException {

	}

	/**
	 * call
	 */
	public boolean call(InteractionSpec interactionSpec, Record inputRecord, Record outputRecord) throws ResourceException {
		/*
		 * Depending on the capabilities of the real EIS backend, the implementation of this method will differ. If the backend system
		 * supports the metadata browsing capabilities (as part of its normal business functions) then this method can simply delegate
		 * all the calls to the backend. If the EIS does not have this capability, then the resource adapter needs to implement the logic
		 * to run the getDefinition and getPortTypes operations.
		 * 
		 * In the myEIS sample, the backend implements the import service as business functions. Hence all calls can be passed directly to 
		 * the backend. As an illustration of how the resource adapter can implement the functions, we introduced the 'local' setting on the 'RepositoryLocation'
		 * property. If the property is set to 'local' then the resource adapter implements the logic.
		 */
        try {
        	/*
        	 * If the repository is 'local' then perform the import service logic here.
        	 */
			if ((this.fieldManagedConnectionFactory.getRepositoryLocation().equals("local")) && (((MyEISInteractionSpec) interactionSpec).getFunctionName().equals("IMPORT_PORTTYPES"))) {
				// local import
				String queryString = (String) ((WSIFMessage_JCAStreamable) inputRecord).getObjectPart("queryString");
				PortTypeArray portTypeArray = (new Repository()).getPortTypes(queryString);
				((WSIFMessage_JCAStreamable) outputRecord).setObjectPart("result", portTypeArray);
	
				return true;
			}
			if ((this.fieldManagedConnectionFactory.getRepositoryLocation().equals("local")) && (((MyEISInteractionSpec) interactionSpec).getFunctionName().equals("IMPORT_DEFINITION"))) {
				// local import
				PortTypeSelection selection = (PortTypeSelection) ((WSIFMessage_JCAStreamable) inputRecord).getObjectPart("portTypeSelection");
				ImportDefinition importDefinition = (new Repository()).getDefinition(selection);
				((WSIFMessage_JCAStreamable) outputRecord).setObjectPart("result", importDefinition);
	
				return true;
			}
        } catch (WSIFException e) {
			e.printStackTrace();
			throw new ResourceException(e.getMessage());
        }

		// invocation of remote import and all other business services
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
			objectOutputStream.writeObject(((MyEISInteractionSpec) interactionSpec).getFunctionName());
			objectOutputStream.flush();

			((WSIFMessage_JCAStreamable) inputRecord).write(outputStream);

			// call my EIS
			MyEIS myEIS = new MyEIS();
			byte[] outBytes = myEIS.doIt(outputStream.toByteArray());

			ByteArrayInputStream inputStream = new ByteArrayInputStream(outBytes);
			((WSIFMessage_JCAStreamable) outputRecord).read(inputStream);
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new ResourceException(e.getMessage());
		}

		return true;
	}
}
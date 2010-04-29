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

package org.apache.wsif;

import java.io.Serializable;

/**
 * A WSIFPort represents the handle by which the operations
 * from the <portType> of the <port> of this WSIFPort can be
 * executed. This is an interface which must implemented by
 * specific implementations for the ports. That is, the actual
 * logic is dependent on the binding associated with this port.
 * An interface is used to enable dynamic implementation generation
 * using JDK1.3 dynamic proxy stuff.
 *
 * @author Paul Fremantle
 * @author Alekander Slominski
 * @author Matthew J. Duftler
 * @author Sanjiva Weerawarana
 * @author Nirmal Mukhi
 */
public interface WSIFPort extends Serializable {
	/**
	 * Create a new WSIFOperation. There must be exactly one
	 * operation in this port's portType with this name. For 
	 * overloaded operations see {@link #createOperation(String,String,String)}.
	 * 
	 * @param operationName the name of an operation in this port's portType
	 * @return the new WSIFOperation
	 * @exception WSIFException if something goes wrong
	 */
    public WSIFOperation createOperation(String operationName)
        throws WSIFException;

	/**
	 * Create a new WSIFOperation. There must be an
	 * operation in this port's portType with this operation name, 
	 * input message name and output message name. The input message name
	 * distinguishes overloaded operations.
	 * 
	 * @param operationName the name of an operation in this port's portType
	 * @param inputName the input message name
	 * @param outputName the output message name
	 * @return the new WSIFOperation
	 * @exception WSIFException if something goes wrong
	 */
    public WSIFOperation createOperation(
        String operationName,
        String inputName,
        String outputName)
        throws WSIFException;

    /**
     * Close this port; indicates that the user is done using it. This
     * is only essential for WSIFPorts that are being used in a stateful
     * or resource-shared manner. Responsible stubs will call this if
     * feasible at the right time.
     * @exception WSIFException if something goes wrong
     */
    public void close() throws WSIFException;

    /**
     * Tests if this port supports synchronous calls to operations.
     * @return <code>true</code> this port support synchronous calls
     *      <br><code>false</code> this port does not support synchronous calls
     */
    public boolean supportsSync();

    /**
     * Tests if this port supports asynchronous calls to operations.
     * @return <code>true</code> this port support asynchronous calls
     *      <br><code>false</code> this port does not support asynchronous calls
     */
    public boolean supportsAsync();

    /**
     * Gets the context information for this WSIFPort.
     * @return context
     */
    public WSIFMessage getContext() throws WSIFException ;

    /**
     * Sets the context information for this WSIFPort.
     * @param WSIFMessage the new context information
     */
    public void setContext(WSIFMessage context);

}

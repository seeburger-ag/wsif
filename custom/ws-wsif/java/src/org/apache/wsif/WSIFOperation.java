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
 * A WSIFOperation is a handle on a particular operation of a portType
 * that can be used to invoke web service methods. This interface is 
 * implemented by each provider. A WSIFOperation can be created using
 * {@link WSIFPort#createOperation(String)}.
 * 
 * @author Owen Burroughs <owenb@apache.org>
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */
public interface WSIFOperation extends Serializable {

    /**
     * Execute a request-response operation. The signature allows for
     * input, output and fault messages. WSDL in fact allows one to
     * describe the set of possible faults an operation may result
     * in, however, only one fault can occur at any one time.
     *
     * @param op name of operation to execute
     * @param input input message to send to the operation
     * @param output an empty message which will be filled in if
     *        the operation invocation succeeds. If it does not
     *        succeed, the contents of this message are undefined.
     *        (This is a return value of this method.)
     * @param fault an empty message which will be filled in if
     *        the operation invocation fails. If it succeeds, the
     *        contents of this message are undefined. (This is a
     *        return value of this method.)
     *
     * @return true or false indicating whether a fault message was
     *         generated or not. The truth value indicates whether
     *         the output or fault message has useful information.
     *
     * @exception WSIFException if something goes wrong.
     */
    public boolean executeRequestResponseOperation(
        WSIFMessage input,
        WSIFMessage output,
        WSIFMessage fault)
        throws WSIFException;

    /**
     * Execute an asynchronous request
     * @param input   input message to send to the operation
     *
     * @return the correlation ID or the request. The correlation ID
     *         is used to associate the request with the WSIFOperation.
     *
     * @exception WSIFException if something goes wrong.
     */
    public WSIFCorrelationId executeRequestResponseAsync(WSIFMessage input)
        throws WSIFException;

    /**
     * Execute an asynchronous request
     * @param input   input message to send to the operation
     * @param handler   the response handler that will be notified 
     *        when the asynchronous response becomes available.
     *
     * @return the correlation ID or the request. The correlation ID
     *         is used to associate the request with the WSIFOperation.
     *
     * @exception WSIFException if something goes wrong.
     */
    public WSIFCorrelationId executeRequestResponseAsync(
        WSIFMessage input,
        WSIFResponseHandler handler)
        throws WSIFException;

    /**
     * fireAsyncResponse is called when a response has been received
     * for a previous executeRequestResponseAsync call.
     * @param response   an Object representing the response
     * @exception WSIFException if something goes wrong
     */
    public void fireAsyncResponse(Object response) throws WSIFException;

    /**
     * Processes the response to an asynchronous request. 
     * This is called for when the asynchronous operation was
     * initiated without a WSIFResponseHandler, that is, by calling
     * the executeRequestResponseAsync(WSIFMessage input) method.
     * 
     * @param response   an Object representing the response.
     * @param output an empty message which will be filled in if
     *        the operation invocation succeeds. If it does not
     *        succeed, the contents of this message are undefined.
     *        (This is a return value of this method.)
     * @param fault an empty message which will be filled in if
     *        the operation invocation fails. If it succeeds, the
     *        contents of this message are undefined. (This is a
     *        return value of this method.)
     * 
     * @return true or false indicating whether a fault message was
     *         generated or not. The truth value indicates whether
     *         the output or fault message has useful information.
     *
     * @exception WSIFException if something goes wrong
     *
     */
    public boolean processAsyncResponse(
        Object response,
        WSIFMessage output,
        WSIFMessage fault)
        throws WSIFException;

    /**
     * Execute an input-only operation.
     *
     * @param input input message to send to the operation
     * @exception WSIFException if something goes wrong.
     */
    public void executeInputOnlyOperation(WSIFMessage input) throws WSIFException;

    /**
     * Allows the application programmer or stub to pass context 
     * information to the binding. The Port implementation may use 
     * this context - for example to update a SOAP header. There is 
     * no definition of how a Port may utilize the context.
     * @param context context information
     */
    public void setContext(WSIFMessage context);

    /**
     * Gets the context information for this binding.
     * @return context
     */
    public WSIFMessage getContext() throws WSIFException;

    /**
     * Create an input message that will be sent via this port.
     * It is responsibility of caller to set message name.
     * @return a new message
     */
    public WSIFMessage createInputMessage();

    /**
     * Create an input message that will be sent via this port.
     * @param name for the new message
     * @return a new message
     */
    public WSIFMessage createInputMessage(String name);

    /**
     * Create an output message that will be received into via this port.
     * It is responsibility of caller to set message name.
     * @return a new message
     */
    public WSIFMessage createOutputMessage();

    /**
     * Create an output message that will be received into via this port.
     *
     * @param name for the new message
     * @return a new message
     */
    public WSIFMessage createOutputMessage(String name);

    /**
     * Create a fault message that may be received into via this port.
     * It is responsibility of caller to set message name.
     * @return a new message
     */
    public WSIFMessage createFaultMessage();

    /**
     * Create a fault message that may be received into via this port.
     *
     * @param name for the new message
     * @return a new message
     */
    public WSIFMessage createFaultMessage(String name);

}

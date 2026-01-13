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

import java.io.Serial;
import java.io.Serializable;

import javax.xml.namespace.QName;

/**
 * This class represents a service request coming into WSIF. It
 * contains the incoming WSIF message, context information (in the
 * form of a WSIF message) as well as other information (service ID,
 * port name, operation name etc.).
 *
 * @author Sanjiva Weerawarana <sanjiva@watson.ibm.com>
 * @author Paul Fremantle <pzf@uk.ibm.com>
 */
public class WSIFRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
	
    QName serviceID;
    String portName;
    String operationName;
    String inputName;
    String outputName;
    WSIFMessage incomingMessage;
    WSIFMessage contextMessage;

    /**
     * Constructor.
     */
    public WSIFRequest(QName serviceID) {
        this.serviceID = serviceID;
    }

    /**
     * Get the service ID.
     */
    public QName getServiceID() {
        return serviceID;
    }

    /**
     * Set the name of the port within the service that is to be used.
     * If not set the either the service must have only one port/portType
     * or someone must be able to figure out the port from the service ID.
     */
    public void setPortName(String portName) {
        this.portName = portName;
    }

    /**
     * Get the port name.
     */
    public String getPortName() {
        return portName;
    }

    /**
     * Set the operation name.
     */
    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    /**
     * Get the operation name.
     */
    public String getOperationName() {
        return operationName;
    }

    /**
     * Set the incoming message. 
     */
    public void setIncomingMessage(WSIFMessage incomingMessage) {
        this.incomingMessage = incomingMessage;
    }

    /**
     * Get the incoming message. 
     */
    public WSIFMessage getIncomingMessage() {
        return incomingMessage;
    }

    /**
     * Set the context message.
     */
    public void setContextMessage(WSIFMessage contextMessage) {
        this.contextMessage = contextMessage;
    }

    /**
     * Get the context message.
     */
    public WSIFMessage getContextMessage() {
        return contextMessage;
    }

    /**
     * Printable version.
     */
    public String toString() {
        return "[WSIFRequest:\n"
            + "\t serviceID = '"
            + serviceID
            + "'\n"
            + "\t operationName = '"
            + operationName
            + "'\n"
            + "\t incomingMessage = '"
            + incomingMessage
            + "'\n"
            + "\t contextMessage = '"
            + contextMessage
            + "']";
    }
    /**
     * Gets the outputName
     * @return Returns a String
     */
    public String getOutputName() {
        return outputName;
    }
    /**
     * Sets the outputName
     * @param outputName The outputName to set
     */
    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    /**
     * Gets the inputName
     * @return Returns a String
     */
    public String getInputName() {
        return inputName;
    }
    /**
     * Sets the inputName
     * @param inputName The inputName to set
     */
    public void setInputName(String inputName) {
        this.inputName = inputName;
    }

}

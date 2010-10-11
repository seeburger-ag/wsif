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

import javax.xml.namespace.QName;

/**
 * This class represents a service response coming out of WSIF. It
 * contains the outgoing message or fault message, context information
 * as well as other information.
 *
 * @author Sanjiva Weerawarana <sanjiva@watson.ibm.com>
 * @author Paul Fremantle <pzf@uk.ibm.com>
 */
public class WSIFResponse implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
    QName serviceID;
    String operationName;
    boolean isFault = false;
    WSIFMessage outgoingMessage;
    WSIFMessage faultMessage;
    WSIFMessage contextMessage;

    String portName;
    String inputName;
    String outputName;
    /**
     * Constructor.
     */
    public WSIFResponse(QName serviceID) {
        this.serviceID = serviceID;
    }

    /**
     * Get the service ID.
     */
    public QName getServiceID() {
        return serviceID;
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
     * Indicate whether this response contains a a fault message or an 
     * ok response message. Defaults to ok (i.e., the value of the flag
     * is false).
     */
    public void setIsFault(boolean isFault) {
        this.isFault = isFault;
    }

    /**
     * Get the value of the isFault flag. True if response contains a
     * fault and false otherwise.
     */
    public boolean getIsFault() {
        return isFault;
    }

    /**
     * Set the outgoing message. The outgoing message or the fault 
     * message must be set for any given response.
     */
    public void setOutgoingMessage(WSIFMessage outgoingMessage) {
        this.outgoingMessage = outgoingMessage;
    }

    /**
     * Get the outgoing message. 
     */
    public WSIFMessage getOutgoingMessage() {
        return outgoingMessage;
    }

    /**
     * Set the fault message. The outgoing message or the fault 
     * message must be set for any given response.
     */
    public void setFaultMessage(WSIFMessage faultMessage) {
        this.faultMessage = faultMessage;
    }

    /**
     * Get the fault message. 
     */
    public WSIFMessage getFaultMessage() {
        return faultMessage;
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
        return "[WSIFResponse:\n"
            + "\t serviceID = '"
            + serviceID
            + "'\n"
            + "\t operationName = '"
            + operationName
            + "'\n"
            + "\t isFault = '"
            + isFault
            + "'\n"
            + "\t outgoingMessage = '"
            + outgoingMessage
            + "'\n"
            + "\t faultMessage = '"
            + faultMessage
            + "'\n"
            + "\t contextMessage = '"
            + contextMessage
            + "']";
    }
    /**
     * Gets the portName
     * @return Returns a String
     */
    public String getPortName() {
        return portName;
    }
    /**
     * Sets the portName
     * @param portName The portName to set
     */
    public void setPortName(String portName) {
        this.portName = portName;
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

}

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

package org.apache.wsif.providers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.BindingOperation;
import javax.wsdl.Fault;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.extensions.ExtensibilityElement;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFCorrelationId;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFResponseHandler;
import org.apache.wsif.attachments.WSIFAttachmentPart;
import org.apache.wsif.base.WSIFDefaultMessage;
import org.apache.wsif.logging.Trc;

/**
 * ModelWSIFOperation
 * 
 * Models are provided for all the classes required to 
 * be implemented when writing a WSIF provider: 
 * 			WSIFProvider, WSIFPort, and WSIFOperation. 
 * The models are intended to simplify the work in the
 * implementing subclasses, and insure that all providers
 * work in standard way. Things like hunting around in 
 * the WSDL for ExtensabilityElements and verifying the 
 * types of request and response objects against the WSDL
 * should be done in the model code. Subclasses should only 
 * need to provide code directly related to accessing the
 * particular service type they implement.
 * 
 * For a subclass to use ModelWSIFOperation, as a minimum it
 * would implement the doInvokeRequestResponse method. 
 * 
 * If special processing is required for input only operations 
 * then subclasses would also implement doInvokeInputOnly.
 * 
 * If the binding being implemented uses a binding operation
 * extensibility element then subclasses should override the
 * getOperationExtensibilityClass, and validateOperationExtensibilityElement
 * methods.
 * 
 * Other methods may be overriden to customise the behaviour,
 * see the method javadoc for details. 
 * 
 * @author <a href="mailto:antelder@apache.org">Ant Elder</a>
 */
abstract public class ModelWSIFOperation implements WSIFOperation {

    protected ModelWSIFPort wsifPort;
    protected BindingOperation bindingOperation;
    protected Operation portTypeOperation;

    protected List inputParts;
    protected List outputParts;

    protected WSIFResponseHandler asyncResponseHandler;

    protected WSIFMessage context;

    protected boolean used;

    /**
     * Construct a ModelWSIFOperation
     */
    public ModelWSIFOperation(
        ModelWSIFPort wsifPort,
        BindingOperation bindingOperation)
        throws WSIFException {

        this.wsifPort = wsifPort;
        this.bindingOperation = bindingOperation;

    }

    /**
     * @see WSIFOperation#executeInputOnlyOperation(WSIFMessage)
     */
    public void executeInputOnlyOperation(WSIFMessage inMsg)
        throws WSIFException {
        Trc.entry(this, inMsg);

        markAsUsed();

        //TODO: does this need supportsSync() or suuportsAsync()?

        if (inMsg == null) {
            throw new IllegalArgumentException("input message is null");
        }

        invokeInputOnlyOperation(inMsg);

        Trc.exit();
    }

    /**
     * @see WSIFOperation#executeInputOnlyOperation(WSIFMessage,WSIFMessage,WSIFMessage)
     */
    public boolean executeRequestResponseOperation(
        WSIFMessage inMsg,
        WSIFMessage outMsg,
        WSIFMessage faultMsg)
        throws WSIFException {
        Trc.entry(this, inMsg, outMsg, faultMsg);

        markAsUsed();

        if (wsifPort.supportsSync() == false) {
            throw new WSIFException("synchronus operations not supported");
        }

        if (inMsg == null) {
            throw new IllegalArgumentException("input message is null");
        }
        if (outMsg == null) {
            throw new IllegalArgumentException("output message is null");
        }
        if (faultMsg == null) {
            throw new IllegalArgumentException("fault message is null");
        }

        boolean ok = invokeRequestResponseOperation(inMsg, outMsg, faultMsg);

        Trc.exit(ok);
        return ok;
    }

    /**
     * @see WSIFOperation#executeRequestResponseAsync(WSIFMessage)
     */
    public WSIFCorrelationId executeRequestResponseAsync(WSIFMessage inMsg)
        throws WSIFException {
        Trc.entry(this, inMsg);

        markAsUsed();

        if (wsifPort.supportsAsync() == false) {
            throw new WSIFException("asynchronous operations not supportted");
        }

        if (inMsg == null) {
            throw new IllegalArgumentException("input message is null");
        }

        WSIFCorrelationId cid = invokeRequestResponseAsync(inMsg, null);

        Trc.exit(cid);
        return cid;
    }

    /**
     * @see WSIFOperation#executeRequestResponseAsync(WSIFMessage, WSIFResponseHandler)
     */
    public WSIFCorrelationId executeRequestResponseAsync(
        WSIFMessage inMsg,
        WSIFResponseHandler handler)
        throws WSIFException {
        Trc.entry(this, inMsg, handler);

        markAsUsed();

        if (wsifPort.supportsAsync() == false) {
            throw new WSIFException("asynchronous operations not supportted");
        }

        if (inMsg == null) {
            throw new IllegalArgumentException("input message is null");
        }

        if (handler == null) {
            throw new IllegalArgumentException("WSIFResponseHandler is null");
        }

        WSIFCorrelationId cid = invokeRequestResponseAsync(inMsg, handler);

        Trc.exit(cid);
        return cid;
    }

    /**
     * @see WSIFOperation#fireAsyncResponse(Object)
     */
    public void fireAsyncResponse(Object response) throws WSIFException {
        Trc.entry(this, response);

        if (wsifPort.supportsAsync() == false) {
            throw new WSIFException("asynchronous operations not supportted");
        }

        if (asyncResponseHandler == null) {
            throw new WSIFException("asyncResponseHandler is null!");
        }

        Map results = doDeserialiseResponse(response);

        WSIFMessage outMsg = createOutputMessage();
        WSIFMessage faultMsg = createFaultMessage();

        buildResponseMessages(results, outMsg, faultMsg);

        asyncResponseHandler.executeAsyncResponse(outMsg, faultMsg);

        Trc.exit();
    }

    /**
     * @see WSIFOperation#processAsyncResponse(Object,WSIFMessage,WSIFMessage)
     */
    public boolean processAsyncResponse(
        Object response,
        WSIFMessage outMsg,
        WSIFMessage faultMsg)
        throws WSIFException {
        Trc.entry(this, response, outMsg, faultMsg);
        Trc.exit();
        //TODO:
        throw new WSIFException("not implemented yet");
    }

    /**
     * Invoke a request response operation
     * Subclasses should not normally override this, but
     * should implement doInvokeRequestResponse.
     */
    protected boolean invokeRequestResponseOperation(
        WSIFMessage inMsg,
        WSIFMessage outMsg,
        WSIFMessage faultMsg)
        throws WSIFException {

        boolean invokedOK = false;

        setPartValues(inputParts, inMsg);
        List inputArgs = getInputArguments();
        List responseValues = new ArrayList();

        setInvocationContext();

        invokedOK = doInvokeRequestResponse(inputArgs, responseValues);

        if (invokedOK == true) {
            if (outMsg != null) {
                setPartValues(outputParts, responseValues);
                setMessagePartValues(outMsg, outputParts);
            }
        } else {
            if (faultMsg != null) {
                setFaultObjects(faultMsg, responseValues);
            }
        }

        return invokedOK;
    }

    /**
     * Template method for RequestResponse operation implementation.
     * This is the method that actually invokes the service 
     * Subclasses must implement this if they support synchronous operation
     */
    protected boolean doInvokeRequestResponse(
        List inputArgs,
        List responseArgs)
        throws WSIFException {
        throw new WSIFException("doInvoke must be implemented by subclass!");
    }

    /**
     * Invoke an input only operation
     * Subclasses should not normally override this, but
     * should implement doInvokeInputOnly.
     */
    protected void invokeInputOnlyOperation(WSIFMessage inMsg)
        throws WSIFException {
        invokeRequestResponseOperation(inMsg, null, null);

        boolean invokedOK = false;

        setPartValues(inputParts, inMsg);
        List inputArgs = getInputArguments();
        List responseValues = new ArrayList();

        setInvocationContext();

        doInvokeInputOnly(inputArgs);

    }

    /**
     * Template method for inputOnly operation implementation.
     * This is the method that actually invokes the service 
     * By default this just calls doInvokeRequestResponse and
     * discards the response. 
     * Subclasses may override with their own implementation
     */
    protected void doInvokeInputOnly(List inputArgs) throws WSIFException {
        doInvokeRequestResponse(inputArgs, new ArrayList());
    }

    /**
     * Invoke an asynchronus request response operation
     * Subclasses should not normally override this, but
     * should implement doInvokeRequestResponseAsync.
     */
    protected WSIFCorrelationId invokeRequestResponseAsync(
        WSIFMessage inMsg,
        WSIFResponseHandler handler)
        throws WSIFException {

        setPartValues(inputParts, inMsg);
        List inputArgs = getInputArguments();

        setInvocationContext();

        WSIFCorrelationId cid = doInvokeRequestResponseAsync(inputArgs);

        return cid;
    }

    /**
     * Template method for asynchronous RequestResponse operation implementation.
     * This is the method that actually invokes the service 
     * Subclasses must implement this if they support asynchronous operation
     */
    protected WSIFCorrelationId doInvokeRequestResponseAsync(List inputArgs)
        throws WSIFException {
        throw new WSIFException("doInvokeAsync must be implemented by subclass!");
    }

    /**
     * Deserialises an asynchronus response object
     */
    protected Map doDeserialiseResponse(Object response) throws WSIFException {
        throw new WSIFException("doDeserialiseResponse must be implemented by subclass!");
    }

    /**
     * 
     */
    protected void buildResponseMessages(
        Map results,
        WSIFMessage outMsg,
        WSIFMessage faultMsg)
        throws WSIFException {
        throw new WSIFException("buildResponseMessages must be implemented by subclass!");
    }

    /**
     * Populates the inputParts List from a WSIFMessage 
     */
    protected void setPartValues(List parts, WSIFMessage msg)
        throws WSIFException {
        for (Iterator i = parts.iterator(); i.hasNext();) {
            Part part = (Part) i.next();
            Object partValue;
            try {
                partValue = msg.getObjectPart(part.getName());
                validatePartValue(part, partValue);
            } catch (WSIFException e) {
                partValue = doMissingPart(msg, part);
            }
            part.setValue(partValue);
        }
    }

    /**
     * Populates the outputPartValues map from a List of response objects 
     */
    protected void setPartValues(List parts, List responseValues)
        throws WSIFException {

        Iterator responses = responseValues.iterator();
        for (Iterator i = parts.iterator(); i.hasNext();) {
            Part part = (Part) i.next();

            Object partValue;
            if (responses.hasNext() == true) {
                partValue = responses.next();
                validatePartValue(part, partValue);
            } else {
                partValue = doMissingOutputValue(part);
            }
            part.setValue(partValue);
        }
    }

    /**
     * Populates a WSIFMessage with the part names and values from a Map  
     */
    protected void setMessagePartValues(WSIFMessage msg, List parts)
        throws WSIFException {
        for (Iterator i = parts.iterator(); i.hasNext();) {
            Part part = (Part) i.next();
            msg.setObjectPart(part.getName(), part.getValue());
        }
    }

    /**
     * Populate a WSIFMessage from a list of fault objects
     */
    protected void setFaultObjects(WSIFMessage msg, List responseObjects)
        throws WSIFException {

        //TODO: how to do faults properly
        int faultNumber = 0;
        for (Iterator i = responseObjects.iterator(); i.hasNext();) {
            Object faultValue = i.next();
            String faultName = "fault" + faultNumber++;
            msg.setObjectPart(faultName, faultValue);
        }
    }

    /**
     * Returns a List of the input value objects in inputPartOrder order 
     */
    protected List getInputArguments() throws WSIFException {
        List args = new ArrayList();
        for (Iterator i = inputParts.iterator(); i.hasNext();) {
            Part part = (Part) i.next();
            Object value = part.getValue();
            args.add(value);
        }
        return args;
    }

    /**
     * Handles a input WSIFMessage not containg a part in the WSDL message.
     * This default implementation defaults the part value to be null
     * Subclasses may overide with their own default  
     */
    protected Object doMissingPart(WSIFMessage msg, Part part) {
        Trc.event(
            this,
            "message "
                + msg.getName()
                + "has missing input part '"
                + part.getName()
                + "', defaulting to null");
        return null;
    }

    /**
     * Handles a output response not having a value for a part in the WSDL message.
     * This default implementation defaults the part value to be null
     * Subclasses may overide with their own default  
     */
    protected Object doMissingOutputValue(Part part) {
        Trc.event(this, "response value missing for part: " + part.getName());
        return null;
    }

    /**
     * Validates an object is compatable with the type of a WSDL part
     */
    protected void validatePartValue(Part part, Object value)
        throws WSIFException {
        if (value != null) {
            Class valueClass = value.getClass();
            if (part.getJavaClass().isAssignableFrom(valueClass)) {
                // type ok
                // TODO: what about int mapping to java.lang.Integer etc?                
                //            } else if (isPrimitiveWrapper(value, partClass)) {
                //            	// ?
            } else if (value instanceof WSIFAttachmentPart) {
                //TODO: what to do about WSIFAttachmentPart? 
                //      for now type ok
            } else if ("javax.activation.DataHandler".equals(valueClass)) {
                //TODO: what to do about DataHandler? 
                //      for now type ok
            } else {
                throw new WSIFException(
                    "WSIFMessage part '"
                        + part.getName()
                        + "' has invalid type, expecting "
                        + part.getJavaClass()
                        + " found "
                        + value.getClass());
            }
        }
    }

    /**
     * Initialises the WSIFOperation.
     * This is called after the WSIFOperation is instantiated or
     * when it is a cached WSIFOperation about to be reused.
     * 
     * Subclasses would not normally override this but 
     * should override the doInitialise method.
     */
    protected void initialise() throws WSIFException {
        used = false;
        if (inputParts == null) {
            prepare();
        }
        doInitialise();
    }

    /**
     * Initialises the WSIFOperation.
     * This will be called when the WSIFOperation is first created,
     * or when a cached WSIFOperation is about to be reused. 
     * The default implementation does nothing, subclasses may
     * override it if they need to do something before reuse.
     */
    protected void doInitialise() {
    }

    /**
     * This sets up the WSIFOperation from the WSDL including:
     * - do any unwrapping of parameters if required
     * - finding the names and types of the input/output
     *   parameters
     */
    protected void prepare() throws WSIFException {

        this.inputParts = new ArrayList();
        this.outputParts = new ArrayList();

        Operation op = getPortTypeOperation();

        // if this is a wrapped style operation, should the parts be unwrapped?
        boolean unwrap = ProviderUtils.isUnwrapable(op);
        if (unwrap == true) {
            // don't unwrap if context asks not to
            unwrap = !isWrappedInContext();
        }

        // initialise the input parts
        if (op.getInput() != null) {
            Message inMsg = op.getInput().getMessage();
            initialiseParts(inMsg, unwrap, inputParts);
        }

        // initialise the output parts
        if (op.getOutput() != null) {
            Message outMsg = op.getOutput().getMessage();
            initialiseParts(outMsg, unwrap, outputParts);
        }

        initializeOperationExtensibilityElement();

        // template method for subclasses
        doPrepare();
    }

    /**
     * Initializes the binding operation extensebility element.
     * This find the binding operation extensebility element the
     * WSIFOperation concrete subclass implements, and then passes
     * it to the validateOperationExtensibilityElement method.
     */
    protected void initializeOperationExtensibilityElement()
        throws WSIFException {

        Class c = getOperationExtensibilityClass();
        if (c != null) {
            if (!(ExtensibilityElement.class.isAssignableFrom(c))) {
                throw new WSIFException("getOperationExtensabilityClass must return a subclass of ExtensibilityElement");
            }
            ExtensibilityElement ee = null;
            List ees = bindingOperation.getExtensibilityElements();
            for (Iterator i = ees.iterator(); ee == null && i.hasNext();) {
                Object o = i.next();
                if (c.isAssignableFrom(o.getClass())) {
                    ee = (ExtensibilityElement) o;
                }
            }
            if (ee != null) {
                validateOperationExtensibilityElement(ee);
            }
        }
    }

    /**
     * Gets the binding operation extensebility element
     * This default implementation returns null. If subclasses
     * require a WSDL binding operation element they should 
     * override this method to return its WSDL4J implementation class.
     */
    protected Class getOperationExtensibilityClass() {
        return null;
    }

    /**
     * Validates the binding operation ExtensibilityElement
     * This default implementation does nothing. If subclasses
     * use a WSDL binding operation element they should override 
     * this method to validate the ExtensibilityElement attributes.
     */
    protected void validateOperationExtensibilityElement(ExtensibilityElement address)
        throws WSIFException {
    }

    /**
     * Prepares the WSIFOperation.
     * 
     * This is called the first time a WSIFOperation is initialised.
     * The difference between doPrepare and doInitialise is that
     * doPrepare is called only once, whereas doInitialise is called
     * every time a WSIFOperation is about to be reused.
     * 
     * TODO: whats this all about, should it be called from the constructor then?
     * 
     * The default implementation does nothing, subclasses may
     * override it if they need to do something before reuse.
     */
    protected void doPrepare() {
    }

    /**
     * Initializes part collections from a WSDL Message
     * The part collections incluse a List of the order of the 
     * parts in the WSDL message, and a Map with a key the part
     * in the WSDL message and a value the Java class for the
     * parts type.
     */
    protected void initialiseParts(Message msg, boolean unwrap, List parts)
        throws WSIFException {

        List msgParts = msg.getOrderedParts(null);
        if (msgParts != null && !msgParts.isEmpty()) {
            if (unwrap == true) {
                Part p = (Part) msgParts.get(0);
                List unwrappedParts =
                    ProviderUtils.unWrapPart(
                        p.getWsdlPart(),
                        wsifPort.getDefinition(),
                        getContext());
                msgParts.remove(p);
                msgParts.add(0, unwrappedParts);
            }
            for (Iterator i = msgParts.iterator(); i.hasNext();) {
                javax.wsdl.Part wsdlPart = (javax.wsdl.Part) i.next();
                Part part = new Part(wsdlPart);
                part.setJavaClass(wsifPort.getClassForPart(wsdlPart));
                parts.add(part);
            }
        }
    }

    /**
     * Tests if wrapped operations should use wrapped parts
     */
    protected boolean isWrappedInContext() {
        boolean wrappedInContext = false;

        String value = null;
        try {
            WSIFMessage context = getContext();
            value =
                (String) context.getObjectPart(
                    WSIFConstants.CONTEXT_OPERATION_STYLE);

        } catch (WSIFException e) {
            Trc.ignoredException(e);
        }

        if (WSIFConstants.CONTEXT_OPERATION_STYLE_WRAPPED.equals(value)) {
            wrappedInContext = true;
        }

        return wrappedInContext;
    }

    /**
     * Tests if wrapped operations should use unwrapped parts
     */
    protected boolean isUnwrappedInContext() {
        boolean unWrapInContext = false;

        String value = null;
        try {
            WSIFMessage context = getContext();
            value =
                (String) context.getObjectPart(
                    WSIFConstants.CONTEXT_OPERATION_STYLE);

        } catch (WSIFException e) {
            Trc.ignoredException(e);
        }

        if (WSIFConstants.CONTEXT_OPERATION_STYLE_UNWRAPPED.equals(value)) {
            unWrapInContext = true;
        }

        return unWrapInContext;
    }

    /**
     * @deprecated use getPortTypeOperation
     */
    protected Operation getOperation() throws WSIFException {
        Trc.entry(this);
        getPortTypeOperation();
        Trc.exit(portTypeOperation);
        return portTypeOperation;
    }

    /**
     * Get the WSDL portType operation this WSIFOperation implements.
     * 
     * @see WSIFDefaultOperation#getOperation()
     */
    public Operation getPortTypeOperation() {
        Trc.entry(this);
        if (portTypeOperation == null) {

            String inputName = null;
            if (bindingOperation.getBindingInput() != null) {
                inputName = bindingOperation.getBindingInput().getName();
            }

            String outputName = null;
            if (bindingOperation.getBindingOutput() != null) {
                outputName = bindingOperation.getBindingOutput().getName();
            }

            portTypeOperation =
                wsifPort.getPort().getBinding().getPortType().getOperation(
                    bindingOperation.getName(),
                    inputName,
                    outputName);
            if (portTypeOperation == null) {
                throw new RuntimeException(
                    "no WSDL portType operation found for binding opertion: "
                        + bindingOperation.getName()
                        + ":"
                        + inputName
                        + ":"
                        + outputName);
            }
        }
        Trc.exit(portTypeOperation);
        return portTypeOperation;
    }

    /**
     * @see WSIFOperation#createInputMessage()
     */
    public WSIFMessage createInputMessage() {
        Trc.entry(this);
        WSIFMessage msg = createInputMessage(null);
        Trc.exit(msg);
        return msg;
    }

    /**
     * @see WSIFOperation#createInputMessage(String)
     */
    public WSIFMessage createInputMessage(String name) {
        Trc.entry(this, name);
        WSIFMessage msg = new WSIFDefaultMessage();
        msg.setName(name);
        Input in = getPortTypeOperation().getInput();
        if (in != null) {
            msg.setMessageDefinition(in.getMessage());
            //TODO: should also add unwrapped parts?
        }
        Trc.exit(msg);
        return msg;
    }

    /**
     * @see WSIFOperation#createOutputMessage()
     */
    public WSIFMessage createOutputMessage() {
        Trc.entry(this);
        WSIFMessage msg = createOutputMessage(null);
        Trc.exit(msg);
        return msg;
    }

    /**
     * @see WSIFOperation#createOutputMessage(String)
     */
    public WSIFMessage createOutputMessage(String name) {
        Trc.entry(this, name);
        WSIFMessage msg = new WSIFDefaultMessage();
        msg.setName(name);
        Output out = getPortTypeOperation().getOutput();
        if (out != null) {
            msg.setMessageDefinition(out.getMessage());
            //TODO: should also add unwrapped parts?
        }
        Trc.exit(msg);
        return msg;
    }

    /**
     * @see WSIFOperation#createFaultMessage()
     */
    public WSIFMessage createFaultMessage() {
        Trc.entry(this);
        WSIFMessage msg = createFaultMessage(null);
        Trc.exit(msg);
        return msg;
    }

    /**
     * @see WSIFOperation#createFaultMessage(String)
     */
    public WSIFMessage createFaultMessage(String name) {
        Trc.entry(this, name);
        WSIFMessage msg = new WSIFDefaultMessage();
        msg.setName(name);
        Fault fault = getPortTypeOperation().getFault(name);
        if (fault != null) {
            msg.setMessageDefinition(fault.getMessage());
        }
        Trc.exit(msg);
        return msg;
    }

    /**
     * Returns the List of inputParts.
     * @return List   the List of inputParts
     */
    protected List getInputParts() {
        return inputParts;
    }

    /**
     */
    protected void setInputParts(List parts) throws WSIFException {
        this.inputParts = parts;
    }

    /**
     * Sets the order of the inputParts.
     * @param partNames   A List of String part names in required order
     */
    protected void setInputPartOrder(List partNames) throws WSIFException {
        this.inputParts = setPartOrder(partNames, inputParts);
    }

    /**
     * Gets the WSDL output message return part
     * By default the return part is the first part in the WSDL message
     */
    protected Part getReturnPart() {
        Part returnPart;
        if (outputParts.size() > 0) {
            returnPart = (Part) outputParts.get(0);
        } else {
            returnPart = null;
        }
        return returnPart;
    }

    /**
     * Sets the WSDL output message return part
     */
    protected void SetReturnPart(String returnPartName) throws WSIFException {
        Part p = getNamedPart(returnPartName, outputParts);
        if (p != null) {
            SetReturnPart(p);
        } else {
            throw new WSIFException("part does not exist: " + returnPartName);
        }
    }

    /**
     * Sets the WSDL output message return part
     */
    protected void SetReturnPart(Part returnPart) throws WSIFException {
        if (outputParts.contains(returnPart)) {
            outputParts.remove(returnPart);
            outputParts.add(0, returnPart);
        } else {
            throw new WSIFException("part does not exist: " + returnPart);
        }
    }

    /**
     * Returns the outputPartOrder.
     * @return ArrayList
     */
    protected List getOutputParts() {
        return outputParts;
    }

    /**
     * Sets the order of the outputParts.
     * @param partNames   A List of String part names in required order
     */
    protected void setOutputPartOrder(List partNames) throws WSIFException {
        this.outputParts = setPartOrder(partNames, outputParts);
    }

    /**
     * Get a ordered list of WSDL Parts
     * 
     * @param partNames   a List of String part names
     * @param parts   a Map with WSDL Parts as the key 
     * @return List   of WSDL Parts in the same order 
     *                 as the partNames list
     */
    protected List setPartOrder(List partNames, List parts)
        throws WSIFException {
        List oldPartOrder = parts;
        if (partNames != null) {
            List newPartOrder = new ArrayList();
            for (Iterator i = partNames.iterator(); i.hasNext();) {
                String partName = (String) i.next();
                Part p = getNamedPart(partName, parts);
                if (p != null) {
                    newPartOrder.add(p);
                    oldPartOrder.remove(p);
                } else {
                    throw new WSIFException(
                        "part does not exists: " + partName);
                }
            }
            for (Iterator i = oldPartOrder.iterator(); i.hasNext();) {
                newPartOrder.add(i.next());
            }
            parts = newPartOrder;
        }
        return parts;
    }

    /**
     * Find a named part from a list of WSDL Parts
     */
    protected Part getNamedPart(String partName, List parts) {
        Part part = null;
        for (Iterator i = parts.iterator(); part == null && i.hasNext();) {
            Part p = (Part) i.next();
            if (p.getName().equals(partName)) {
                part = p;
            }
        }
        return part;
    }

    /**
     * Find a named part from a map with WSDL Parts as the key
     */
    protected Part getNamedPart(String partName, Map parts) {
        Part part = null;
        for (Iterator i = parts.keySet().iterator();
            part == null && i.hasNext();
            ) {
            Part p = (Part) i.next();
            if (p.getName().equals(partName)) {
                part = p;
            }
        }
        return part;
    }

    /**
     * Gets the context information for this binding.
     */
    public WSIFMessage getContext() throws WSIFException {
        Trc.entry(this);
        WSIFMessage contextCopy;
        try {
            if (this.context == null) {
                this.context = (WSIFMessage) wsifPort.getContext().clone();
            }
            contextCopy = (WSIFMessage) context.clone();
        } catch (CloneNotSupportedException e) {
            throw new WSIFException(
                "CloneNotSupportedException cloning context",
                e);
        }
        Trc.exit(contextCopy);
        return contextCopy;
    }

    /**
     * Allows the application programmer or stub to pass context 
     * information to the binding. The Port implementation may use 
     * this context - for example to update a SOAP header. There is 
     * no definition of how a Port may utilize the context.
     */
    public void setContext(WSIFMessage context) {
        Trc.entry(this, context);
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        this.context = context;
        Trc.exit();
    }

    /**
     * Sets this WSIFOperations context message in the InvocationHelper 
     */
    protected void setInvocationContext() throws WSIFException {
        WSIFMessage ctxt = getContext();
        if (this.context == null) {
            getContext();
        }
        InvocationHelper.setMessageContext(context);
    }

    /**
     * Mark the WSIFOperation as having invoked an operation
     * A WSIFOperation may only be used once by a client, but
     * ModelWSIFPort may cache a WSIFOperation for reuse and
     * will reset the used flag if a WSIFOperation is reused.
     */
    protected void markAsUsed() throws WSIFException {
        if (used == true) {
            throw new WSIFException("WSIFOperations has already been executed");
        } else {
            used = true;
        }
    }

    /**
     * String representation of this WSIFOperation for WSIF Trc.
     */
    public String deep() {
        StringBuffer buff = new StringBuffer(super.toString());
        buff.append(":\n");
        buff.append("portTypeOperation:" + Trc.brief(portTypeOperation));
        buff.append(", bindingOperation:" + Trc.brief(bindingOperation));
        buff.append(", wsifPort:" + wsifPort);
        buff.append(", used:" + used);
        buff.append(", inputParts:" + inputParts);
        buff.append(", outputParts:" + outputParts);
        buff.append(", asyncResponseHandler:" + asyncResponseHandler);
        buff.append(", context:" + context);
        return buff.toString();
    }

}
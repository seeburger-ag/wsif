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

package org.apache.wsif.base;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.wsif.WSIFCorrelationId;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFResponseHandler;
import org.apache.wsif.compiler.util.TypeMapping;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.util.WSIFUtils;
import org.apache.wsif.wsdl.extensions.jms.JMSProperty;
import org.apache.wsif.wsdl.extensions.jms.JMSPropertyValue;

public abstract class WSIFDefaultOperation implements WSIFOperation {
	private static final long serialVersionUID = 1L;
    transient protected HashMap inJmsProps = new HashMap();
    transient protected HashMap outJmsProps = new HashMap();
    transient protected HashMap inJmsPropVals = new HashMap();
    protected WSIFMessage context;
    protected boolean closed = false;
   
    /**
     * @see WSIFOperation#executeRequestResponseOperation(WSIFMessage, WSIFMessage, WSIFMessage)
     */
    public abstract boolean executeRequestResponseOperation(
        WSIFMessage input,
        WSIFMessage output,
        WSIFMessage fault)
        throws WSIFException;

    /**
     * @see WSIFOperation#executeInputOnlyOperation(WSIFMessage)
     */
    public abstract void executeInputOnlyOperation(WSIFMessage input)
        throws WSIFException;

    /**
     * Default implementation of executeRequestResponseAsync.
     * By default async operation is not supported so this just
     * throws an exception.
     * @see WSIFOperation#executeRequestResponseAsync(WSIFMessage, WSIFResponseHandler)
     */
    public WSIFCorrelationId executeRequestResponseAsync(
        WSIFMessage input,
        WSIFResponseHandler handler)
        throws WSIFException {
        throw new WSIFException("asynchronous operations not supportted");
    }

    /**
     * Default implementation of executeRequestResponseAsync.
     * By default async operation is not supported so this just
     * throws an exception.
     * @see WSIFOperation#executeRequestResponseAsync(WSIFMessage)
     */
    public WSIFCorrelationId executeRequestResponseAsync(WSIFMessage input)
        throws WSIFException {
        throw new WSIFException("asynchronous operations not supportted");
    }

    /**
     * Default implemantation of fireAsyncResponse.
     * By default async operation is not supported so this just
     * throws an exception.
     * @see WSIFOperation#fireAsyncResponse(Object)
     * @param response   an Object representing the response
     */
    public void fireAsyncResponse(Object response) throws WSIFException {
        throw new WSIFException("asynchronous operations not supportted");
    }

    /**
     * Default implemantation of processAsyncResponse.
     * By default async operation is not supported so this just
     * throws an exception.
     * @see WSIFOperation#processAsyncResponse(Object,WSIFMessage,WSIFMessage)
     */
    public boolean processAsyncResponse(
        Object response,
        WSIFMessage output,
        WSIFMessage fault)
        throws WSIFException {
        throw new WSIFException("asynchronous operations not supportted");
    }

    /**
     * @see WSIFOperation#createInputMessage()
     */
    public WSIFMessage createInputMessage() {
        Trc.entry(this);
        WSIFMessage msg = new WSIFDefaultMessage();
        if (msg != null) {
            // Now find the javax.wsdl.Message & set it on the WSIFMessage
            try {
                msg.setMessageDefinition(
                    getOperation().getInput().getMessage());
            } catch (Exception e) {
                Trc.ignoredException(e);
            }
        }
        Trc.exit(msg);
        return msg;
    }

    /**
     * @see WSIFOperation#createInputMessage(String)
     */
    public WSIFMessage createInputMessage(String name) {
        Trc.entry(this, name);
        WSIFMessage msg = new WSIFDefaultMessage();
        if (msg != null) {
            msg.setName(name);
            // Now find the javax.wsdl.Message & set it on the WSIFMessage
            try {
                msg.setMessageDefinition(
                    getOperation().getInput().getMessage());
            } catch (Exception e) {
                Trc.ignoredException(e);
            }
        }
        Trc.exit(msg);
        return msg;
    }

    /**
     * @see WSIFOperation#createOutputMessage()
     */
    public WSIFMessage createOutputMessage() {
        Trc.entry(this);
        WSIFMessage msg = new WSIFDefaultMessage();
        if (msg != null) {
            // Now find the javax.wsdl.Message & set it on the WSIFMessage
            try {
                msg.setMessageDefinition(
                    getOperation().getOutput().getMessage());
            } catch (Exception e) {
                Trc.ignoredException(e);
            }
        }
        Trc.exit(msg);
        return msg;
    }

    /**
     * @see WSIFOperation#createOutputMessage(String)
     */
    public WSIFMessage createOutputMessage(String name) {
        Trc.entry(this, name);
        WSIFMessage msg = new WSIFDefaultMessage();
        if (msg != null) {
            msg.setName(name);
            // Now find the javax.wsdl.Message & set it on the WSIFMessage
            try {
                msg.setMessageDefinition(
                    getOperation().getOutput().getMessage());
            } catch (Exception e) {
                Trc.ignoredException(e);
            }
        }
        Trc.exit(msg);
        return msg;
    }

    /**
     * @see WSIFOperation#createFaultMessage()
     */
    public WSIFMessage createFaultMessage() {
        Trc.entry(this);
        WSIFMessage wm = new WSIFDefaultMessage();
        Trc.exit(wm);
        return wm;
    }

    /**
     * @see WSIFOperation#createFaultMessage(String)
     */
    public WSIFMessage createFaultMessage(String name) {
        Trc.entry(this, name);
        WSIFMessage msg = new WSIFDefaultMessage();
        if (msg != null) {
            msg.setName(name);
            // Now find the javax.wsdl.Message & set it on the WSIFMessage
            try {
                msg.setMessageDefinition(
                    getOperation().getFault(name).getMessage());
            } catch (Exception e) {
                Trc.ignoredException(e);
            }
        }
        Trc.exit(msg);
        return msg;
    }

    /**
     * Sets the input Jms properties for this operation
     */
    public void setInputJmsProperties(List list) throws WSIFException {
        Trc.entry(this, list);
        inJmsProps = makeSomeKindOfJmsMap(list);
        Trc.exit();
    }
    
    /**
     * Sets the output Jms properties for this operation
     */
    public void setOutputJmsProperties(List list) throws WSIFException {
        Trc.entry(this, list);
        outJmsProps = makeSomeKindOfJmsMap(list);
        Trc.exit();
    }
    
    public void setInputJmsProperties(HashMap hm) {
        Trc.entry(this, hm);
        inJmsProps = hm;
        Trc.exit();
    }
    
    public void setOutputJmsProperties(HashMap hm) {
        Trc.entry(this, hm);
        outJmsProps = hm;
        Trc.exit();
    }
    
    public HashMap getInputJmsProperties() {
        Trc.entry(this);
        Trc.exit(inJmsProps);
        return inJmsProps;
    }
    
    public HashMap getOutputJmsProperties() {
        Trc.entry(this);
        Trc.exit(outJmsProps);
        return outJmsProps;
    }
    
    public abstract WSIFPort getWSIFPort();
    
    /**
     * This method adds new property values to existing HashMap.
     * Where a property value exists in the existing HashMap and the new list, 
     * this method replaces the existing property value with the new one from the list.
     */
    public void addInputJmsPropertyValues(List list) throws WSIFException {
        Trc.entry(this, list);
        if (list != null && !list.isEmpty()) {
            HashMap newPvs = makeSomeKindOfJmsMap(list);
            newPvs.putAll(inJmsPropVals);
            inJmsPropVals = newPvs;
        }
        Trc.exit();
    }
    
    public void setInputJmsPropertyValues(HashMap hm) {
        Trc.entry(this, hm);
        inJmsPropVals = hm;
        Trc.exit();
    }
    
    public HashMap getInputJmsPropertyValues() {
        Trc.entry(this);
        Trc.exit(inJmsPropVals);
        return inJmsPropVals;
    }
    
    /**
     * Utility method that sets the jms properties for this operation
     */
    protected HashMap makeSomeKindOfJmsMap(List list) throws WSIFException {
        Trc.entry(this, list);
        Map simpleTypeReg = WSIFUtils.getSimpleTypesMap();
        HashMap props = new HashMap(list.size());
        for (Iterator it = list.iterator(); it.hasNext();) {
            Object ee = it.next();
            if (ee instanceof JMSProperty) {
                JMSProperty prop = (JMSProperty) ee;
                props.put(prop.getPart(), prop.getName());
            } else if (ee instanceof JMSPropertyValue) {
                JMSPropertyValue propVal = (JMSPropertyValue) ee;
    
                String name = propVal.getName();
                if (name == null || name.length() == 0)
                    throw new WSIFException("jms:propertyValue found without a name");
    
                QName type = propVal.getType();
                if (type == null)
                    throw new WSIFException(
                        "jms:propertyValue " + name + " did not have a type");
                if (type.getNamespaceURI() == null || type.getLocalPart() == null)
                    throw new WSIFException(
                        "jms:propertyValue " + name + " has a badly formed type");
    
                String value = propVal.getValue();
                if (value == null || value.length() == 0)
                    throw new WSIFException(
                        "jms:propertyValue " + name + " did not have a value");
    
                String cls = (String) (simpleTypeReg.get(type));
                if (cls == null)
                    throw new WSIFException(
                        "jms:propertyValue "
                            + name
                            + " had a type that was "
                            + "unknown or was not a simple type");
    
                Class javaClass = null;
                Exception classNotFound = null;
                try {
                    javaClass =
                        Class.forName(
                            cls,
                            true,
                            Thread.currentThread().getContextClassLoader());
                } catch (ClassNotFoundException cce) {
                    Trc.exception(cce);
                    classNotFound = cce;
                }

                Object obj = null;
                try {
                    if (String.class.equals(javaClass))
                        obj = value;
                    else if (
                        "int".equals(cls) || Integer.class.equals(javaClass))
                        obj = new Integer(value);
                    else if (
                        "boolean".equals(cls)
                            || Boolean.class.equals(javaClass))
                        obj = new Boolean(value);
                    else if (
                        "byte".equals(cls) || Byte.class.equals(javaClass))
                        obj = new Byte(value);
                    else if (
                        "double".equals(cls) || Double.class.equals(javaClass))
                        obj = new Double(value);
                    else if (
                        "float".equals(cls) || Float.class.equals(javaClass))
                        obj = new Float(value);
                    else if (
                        "long".equals(cls) || Long.class.equals(javaClass))
                        obj = new Long(value);
                    else if (
                        "short".equals(cls) || Short.class.equals(javaClass))
                        obj = new Short(value);
                    else if (classNotFound != null)
                        throw new WSIFException(
                            "Unexpected ClassNotFoundException when processing "
                                + "jms:propertyValue "
                                + name
                                + ". Could not convert the type to a java class. "
                                + classNotFound);
                    else
                        throw new WSIFException(
                            "jms:propertyValue "
                                + name
                                + " had an invalid type");
                } catch (NumberFormatException nfe) {
                    Trc.exception(nfe);
                    throw new WSIFException(
                        "jms:propertyValue "
                            + name
                            + " a value that could not "
                            + "be converted into the specified type. Caught NumberFormatException. "
                            + nfe);
                }
    
                props.put(name, obj);
            }
        }
        Trc.exit(props);
        return props;
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
     * Gets the context information for this binding.
     */
    public WSIFMessage getContext() throws WSIFException {
        Trc.entry(this);
    	WSIFMessage contextCopy;
	    try {
    	    if (this.context == null) {
    		    contextCopy = (WSIFMessage) getWSIFPort().getContext().clone();
    	    } else {
			    contextCopy = (WSIFMessage) this.context.clone();
    	    }
		} catch (CloneNotSupportedException e) {
		    throw new WSIFException(
		        "CloneNotSupportedException cloning context", e);
		}
        Trc.exit(contextCopy);
    	return contextCopy;
    }
    
    abstract protected Operation getOperation() throws Exception;
    
    protected void close() throws WSIFException {
    Trc.entry(this);
        if (closed)
            throw new WSIFException("Cannot reuse a WSIFOperation to invoke multiple operations");
        closed = true;
        Trc.exit();
    }
}

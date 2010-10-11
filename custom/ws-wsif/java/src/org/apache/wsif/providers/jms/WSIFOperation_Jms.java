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

package org.apache.wsif.providers.jms;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFCorrelationId;
import org.apache.wsif.WSIFCorrelationService;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFRequest;
import org.apache.wsif.WSIFResponse;
import org.apache.wsif.WSIFResponseHandler;
import org.apache.wsif.base.WSIFDefaultOperation;
import org.apache.wsif.logging.MessageLogger;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.providers.WSIFTransactionControl;
import org.apache.wsif.util.WSIFCorrelationServiceLocator;
import org.apache.wsif.util.WSIFProperties;
import org.apache.wsif.util.WSIFUtils;
import org.apache.wsif.util.jms.WSIFJMSCorrelationId;
import org.apache.wsif.util.jms.WSIFJMSDestination;
import org.apache.wsif.util.jms.WSIFJMSProperties;
import org.apache.wsif.wsdl.extensions.jms.JMSAddress;
import org.apache.wsif.wsdl.extensions.jms.JMSBinding;
import org.apache.wsif.wsdl.extensions.jms.JMSConstants;
import org.apache.wsif.wsdl.extensions.jms.JMSFaultIndicator;
import org.apache.wsif.wsdl.extensions.jms.JMSFaultProperty;
import org.apache.wsif.wsdl.extensions.jms.JMSInput;
import org.apache.wsif.wsdl.extensions.jms.JMSOutput;
import org.apache.wsif.wsdl.extensions.jms.JMSProperty;
import org.apache.wsif.wsdl.extensions.jms.JMSPropertyValue;

/**
 * WSIFOperation_Jms
 * 
 * @author <a href="mailto:ake@de.ibm.com">Hermann Akermann</a>
 * @author <a href="mailto:antelder@apache.org">Ant Elder</a>
 * @author <a href="mailto:seto@ca.ibm.com">Norman Seto</a>
 */
public class WSIFOperation_Jms
	extends WSIFDefaultOperation
	implements WSIFOperation {

	private static final long serialVersionUID = 1L;

	protected Port fieldBasePort;
	protected BindingOperation fieldBindingOperation;
	protected WSIFPort_Jms fieldJmsPort;

	protected Operation fieldOperation;

	private boolean inputOnlyOp = false;

	// input message 	
	protected String fieldInputMessageName;
	protected HashMap fieldInputJmsPropertyValues;
	protected HashMap fieldInputJmsProperties;
	protected JMSInput fieldInput;

	// output message
	protected String fieldOutputMessageName;
	protected HashMap fieldOutputProperties;
	protected JMSOutput fieldOutput;

	private WSIFResponseHandler handler;
	private boolean asyncOperation;
	private JMSFormatter formatter;

	private long syncTimeout;
	private long asyncTimeout;
	transient private WSIFTransactionControl transControl;

	/**
	 * ctor
	 */
	public WSIFOperation_Jms(
		Port basePort,
		BindingOperation bindingOperation,
		WSIFPort_Jms jmsPort)
		throws WSIFException {

		Trc.entry(this, basePort, bindingOperation, jmsPort);

		fieldBasePort = basePort;
		fieldBindingOperation = bindingOperation;
		fieldJmsPort = jmsPort;

		syncTimeout = WSIFProperties.getSyncTimeout();
		asyncTimeout = WSIFProperties.getAsyncTimeout();

        final String name =
            WSIFProperties.getProperty("wsif.transactioncontrol.impl");

        if (name != null) {
            Class clazz =  (Class) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    try {
                        return Class.forName(
                            name,
                            true,
                            Thread.currentThread().getContextClassLoader());
                    } catch (Throwable ignored) {
                        Trc.ignoredException(ignored);
                    }
                    return null;
                }
            });

            if (clazz != null) {
                try {
                    transControl = (WSIFTransactionControl) clazz.newInstance();
                } catch (Exception ignored) {
                    Trc.ignoredException(ignored);
                    transControl = null;
                }
            } else
                transControl = null;
        }

		if (Trc.ON)
			Trc.exit(deep());
	}

	/**
	* Create a new copy of this object. This is not a clone, since
	* it does not copy the referenced objects as well.
	*/
	public WSIFOperation_Jms copy() throws WSIFException {
		Trc.entry(this);
		WSIFOperation_Jms woj =
			new WSIFOperation_Jms(
				fieldBasePort,
				fieldBindingOperation,
				fieldJmsPort);
		if (Trc.ON)
			Trc.exit(woj.deep());
		return woj;
	}

	/**
	 * executeRequestResponseOperation(WSIFMessage, WSIFMessage, WSIFMessage)
	 * 
	 *		synchronous execution is NOT supported for JMS	
	 * 
	 */
	public boolean executeRequestResponseOperation(
		WSIFMessage input,
		WSIFMessage output,
		WSIFMessage fault)
		throws WSIFException {

		Trc.entry(this, input, output, fault);
		close();

		if (!fieldJmsPort.supportsSync())
			throw new WSIFException("synchronous operations not available");

		setAsyncOperation(false);

		boolean operationSucceeded = true;
		boolean txnSuspended = false;
		try {
			getOperation();

            if (transControl != null) {
                transControl.suspend();
                txnSuspended = true;
            }

			// send the jms message	  
			String correlId = sendJmsMessage(input);

			WSIFJMSDestination jmsDest = fieldJmsPort.getJmsDestination();
			javax.jms.Message response = jmsDest.receive(correlId, syncTimeout);
			operationSucceeded = receiveJmsMessage(response, output, fault);

		} catch (Exception ex) {
			Trc.exception(ex);

			// Log message
			MessageLogger.log(
				"WSIF.0005E",
				"Jms",
				fieldBindingOperation.getName());

			throw new WSIFException(
				this
					+ " : Could not invoke '"
					+ fieldBindingOperation.getName()
					+ "'",
				ex);
        } finally {
            if (txnSuspended)
                transControl.resume();
		}

		Trc.exit(operationSucceeded);
		return operationSucceeded;
	}

	/**
	 * executeInputOnlyOperation(WSIFMessage)
	 * WSDL transmission primitive Notification (fire&forget)
	 */
	public void executeInputOnlyOperation(WSIFMessage input)
		throws WSIFException {
		Trc.entry(this, input);
		inputOnlyOp = true;
		executeRequestResponseAsync(input, null);
		Trc.exit();
	}

	/**
	 * executeRequestResponseAsync(WSIFMessage)
	 * This is the simple async form where the client is expected 
	 * to handle the correlating of the response.
	 * @param input   input message to send to the operation
	 *
	 * @return the correlation ID or the request. The correlation ID
	 *         is used to associate the request with the WSIFOperation.
	 *
	 * @exception WSIFException if something goes wrong.
	 */
	public WSIFCorrelationId executeRequestResponseAsync(WSIFMessage input)
		throws WSIFException {
		return executeRequestResponseAsync(input, null);
	}

	/**
	 * executeRequestResponseAsync(WSIFMessage, WSIFResponseHandler)
	 * 
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
		throws WSIFException {

		Trc.entry(this, input, handler);
		close();

		if (!fieldJmsPort.supportsAsync())
			throw new WSIFException("asynchronous operations not available");

		setAsyncOperation(true);
		WSIFCorrelationId correlId = null;

		try {

			getOperation();
			this.handler = handler;

			if (inputOnlyOp) {
				sendJmsMessage(input);
			} else {
				if (handler == null) {
					correlId = new WSIFJMSCorrelationId(sendJmsMessage(input));
				} else {
					WSIFCorrelationService cs =
						WSIFCorrelationServiceLocator.getCorrelationService();
					synchronized (cs) {
						correlId =
							new WSIFJMSCorrelationId(sendJmsMessage(input));
						//register it to the correlation service
						cs.put(correlId, this, asyncTimeout);
					}
				}
			}
		} catch (Exception ex) {
			Trc.exception(ex);

			// Log message
			MessageLogger.log(
				"WSIF.0005E",
				"Jms",
				fieldBindingOperation.getName());

			throw new WSIFException(
				this
					+ " : Could not invoke '"
					+ fieldBindingOperation.getName()
					+ "'",
				ex);
		}

		Trc.exit(correlId);
		return correlId;
	}

	public boolean processAsyncResponse(
		Object responseObject,
		WSIFMessage output,
		WSIFMessage fault)
		throws WSIFException {

		Trc.entry(this, responseObject, output, fault);

		boolean ok = false;
		try {

			getOperation();

			// set output message name
			output.setName(fieldBindingOperation.getBindingOutput().getName());
			ok = receiveJmsMessage(responseObject, output, fault);

		} catch (Exception ex) {
			Trc.exception(ex);

			// Log message
			MessageLogger.log(
				"WSIF.0005E",
				"Jms",
				fieldBindingOperation.getName());

			throw new WSIFException(
				this
					+ " : Could not invoke '"
					+ fieldBindingOperation.getName()
					+ "'",
				ex);
		}

		Trc.exit(ok);
		return ok;
	}

	/**
	 * fireAsyncResponse is called when a response has been received
	 * for a previous executeRequestResponseAsync call.
	 * @param response   an Object representing the response
	 */

	public void fireAsyncResponse(Object response) throws WSIFException {
		Trc.entry(this, response);

		// use processAsyncResponse
		// **NS
		WSIFMessage output = createOutputMessage();
		WSIFMessage fault = createFaultMessage();

		receiveJmsMessage(response, output, fault);

		handler.executeAsyncResponse(output, fault);
		Trc.exit();
	}

	//
	// helper
	//

	/**
	 * send jms message
	 */
	private String sendJmsMessage(WSIFMessage input) throws WSIFException {

		String correlId = null;
		WSIFJMSDestination jmsDest = fieldJmsPort.getJmsDestination();

		setPropertyValues();

		/**
		 * set the parts onto the message
		 */
		// The input message parts can either be a jms property
		// or an input part. Input parts not defined in the
		// WSDL will be ignored. Input parts will only be used
		// if there is not a jms:input atribute, or the part 
		// name is in the jms:input list. Parts defined in the
		// WSDL but not in the input message defualt to null. 
		String partName;
		ArrayList wsdlInputParts = getWSDLInputPartNames();
		HashMap propertyParts = getPropertyParts();
		WSIFMessage message = createInputMessage();
		for (Iterator i = input.getPartNames(); i.hasNext();) {
			partName = (String) i.next();
			if (propertyParts.containsKey(partName)) {
				String name = (String) propertyParts.get(partName);
				Object value = input.getObjectPart(partName);
				if (!timeoutProperty(name, value)) {
					jmsDest.setProperty(name, value);
				}
			} else if (wsdlInputParts.contains(partName)) {
				wsdlInputParts.remove(partName);
				if (fieldInput == null) {
					message.setObjectPart(
						partName,
						input.getObjectPart(partName));
				} else if (fieldInput.getParts().contains(partName)) {
					message.setObjectPart(
						partName,
						input.getObjectPart(partName));
				}
			}
		}

		// default missing parts to null
		for (Iterator i = wsdlInputParts.iterator(); i.hasNext();) {
			message.setObjectPart((String) i.next(), null);
		}

		// properties from the context
		setDestinationContext();

		Service serviceModel = null;
		Definition def = fieldJmsPort.getDefinition();
		Map services = WSIFUtils.getAllItems(def, "Service");
		Port port = fieldJmsPort.getPortModel();
		for (Iterator i = services.values().iterator(); i.hasNext();) {
			Service s = (Service) i.next();
			if (s.getPorts().values().contains(port)) {
				serviceModel = s;
				break;
			}
		}
		if (serviceModel == null) {
			throw new WSIFException("cannot find service for port: " + port);
		}

		formatter = (JMSFormatter) fieldJmsPort.getFormatter();

		WSIFRequest request = new WSIFRequest(serviceModel.getQName());
		request.setIncomingMessage(message);
		request.setOperationName(fieldOperation.getName());
		request.setPortName(fieldJmsPort.getPortModel().getName());
		request.setInputName(fieldBindingOperation.getBindingInput().getName());

		javax.jms.Message jmsMessage =
			jmsDest.createMessage(getJMSMessageType());

		formatter.formatRequest(request, jmsMessage);

		// **NS No support for listener - add that in
		// The basis is off the handler - if handler is defined, we start up the listener
		if (isAsyncOperation() && handler != null)
			jmsDest.setAsyncMode(true);
		correlId = jmsDest.send(jmsMessage, null, !inputOnlyOp);

		return correlId;

	}

	/**
	 * receivesend jms message
	 */
	private boolean receiveJmsMessage(
		Object responseObject,
		WSIFMessage output,
		WSIFMessage fault)
		throws WSIFException {
		Trc.entry(this, responseObject, output, fault);

		if (!(responseObject instanceof javax.jms.Message))
			throw new WSIFException("Object is not of type javax.jms.Message");

        // The WSIFJMSDestination probably didn't receive this message, so 
        // tell it about the message so we can inquire about its JMS properties
        // later.
        fieldJmsPort.getJmsDestination().setLastMessage(
            (javax.jms.Message) responseObject);
        
		WSIFResponse resp =
			formatter.unformatResponse((javax.jms.Message) responseObject);

		if (resp.getIsFault()) {
			formatter.copyTo(resp.getOutgoingMessage(), fault);
			setFaultProperties(fault, formatter.getLastBindingFault());

			Trc.exit(false);
			return false;
		} else {

			// the output message contains all response parts
			// even if not defined in the WSDL. Any parts
			// defined in the WSDL but not in the response
			// default to null
			ArrayList wsdlOutputParts = getWSDLOutputPartNames();
			WSIFMessage m = resp.getOutgoingMessage();
			if (m != null) {
				String partName;
				for (Iterator i = m.getPartNames(); i.hasNext();) {
					partName = (String) i.next();
					output.setObjectPart(partName, m.getObjectPart(partName));
					wsdlOutputParts.remove(partName);
				}
			}

			for (Iterator i = wsdlOutputParts.iterator(); i.hasNext();) {
				output.setObjectPart((String) i.next(), null);
			}

			setOutProperties(output);
			Trc.exit(true);
			return true;
		}
	}

	/**
	 * This merges all properties from the various places into
	 * a single HashMap with a key of the part name, and value
	 * the property that part is for. 
	 */
	private HashMap getPropertyParts() {
		HashMap propertyParts = new HashMap();

		// the input message properties
		if (fieldInputJmsProperties != null) {
			for (Iterator i = fieldInputJmsProperties.keySet().iterator();
				i.hasNext();
				) {
				String propertyName = (String) i.next();
				propertyParts.put(
					propertyName,
					fieldInputJmsProperties.get(propertyName));
			}
		}

		return propertyParts;
	}

	private void setPropertyValues() throws WSIFException {
		String value;
		WSIFJMSDestination jmsDest = fieldJmsPort.getJmsDestination();
		// First set the default properties identifying the JMS message
		try {
			value = fieldOperation.getName();
			if (value != null && value.length() > 0) {
				jmsDest.setProperty(
					WSIFConstants.JMS_PROP_OPERATION_NAME,
					value);
			}
		} catch (WSIFException ex) {
			Trc.ignoredException(ex);
		}

		try {
			BindingInput bi = fieldBindingOperation.getBindingInput();
			value = (bi == null) ? null : bi.getName();
			if (value != null && value.length() > 0) {
				jmsDest.setProperty(WSIFConstants.JMS_PROP_INPUT_NAME, value);
			}
		} catch (WSIFException ex) {
			Trc.ignoredException(ex);
		}

		try {
			BindingOutput bo = fieldBindingOperation.getBindingOutput();
			value = (bo == null) ? null : bo.getName();
			if (value != null && value.length() > 0) {
				jmsDest.setProperty(WSIFConstants.JMS_PROP_OUTPUT_NAME, value);
			}
		} catch (WSIFException ex) {
			Trc.ignoredException(ex);
		}

		// propertyValues from jms:address
		try {
			JMSAddress ja = fieldJmsPort.getObjectReference();
			if (ja != null) {
				setJMSPropertyValues(ja.getJMSPropertyValues());
			}
		} catch (WSIFException ex) {
			Trc.ignoredException(ex);
		}

		// propertyValues from the input message
		if (fieldInputJmsPropertyValues != null) {
			try {
				setJmsPropertyValues(fieldInputJmsPropertyValues);
			} catch (WSIFException ex) {
				Trc.ignoredException(ex);
			}
		}
	}

	/**
	 * sets the output message properties from the received JMS message.
	 */
	private void setOutProperties(WSIFMessage output) throws WSIFException {

		if (output != null && fieldOutputProperties != null) {
			WSIFJMSDestination jmsDest = fieldJmsPort.getJmsDestination();
			for (Iterator i = fieldOutputProperties.keySet().iterator();
				i.hasNext();
				) {
				String partName = (String) i.next();
				String propertyName =
					(String) fieldOutputProperties.get(partName);
				try {
					Object propertyValue = jmsDest.getProperty(propertyName);
					output.setObjectPart(partName, propertyValue);
				} catch (WSIFException ex) {
					Trc.ignoredException(ex);
				}
			}
		}
	}

	private void setFaultProperties(
		WSIFMessage fault,
		BindingFault bindingFault) throws WSIFException{

		Trc.entry(this, fault, bindingFault);

     	WSIFJMSDestination jmsDest = fieldJmsPort.getJmsDestination();
		Iterator it = bindingFault.getExtensibilityElements().iterator();
		while (it.hasNext()) {
			Object ele = it.next();
			if (ele instanceof JMSFaultIndicator) {
				JMSFaultIndicator indic = (JMSFaultIndicator) ele;
				List fProps = indic.getJMSFaultProperties();
				Iterator itFProp = fProps.iterator();
				while (itFProp.hasNext()) {
					Object next = itFProp.next();
					if (next instanceof JMSFaultProperty) {
						JMSFaultProperty fProp = (JMSFaultProperty) next;
						String partName = fProp.getPart();
						String propName = fProp.getName();

						if (partName != null && partName.length() > 0)
							try {
								Object propValue =
									jmsDest.getProperty(propName);
								fault.setObjectPart(partName, propValue);
							} catch (WSIFException ex) {
								Trc.ignoredException(ex);
							}
					}
				}
			} else if (ele instanceof JMSProperty) {
				JMSProperty prop = (JMSProperty) ele;
				String partName = prop.getPart();
				String propName = prop.getName();

				try {
					Object propValue = jmsDest.getProperty(propName);
					fault.setObjectPart(partName, propValue);
				} catch (WSIFException ex) {
					Trc.ignoredException(ex);
				}
			}
		}

		Trc.exit();
	}

	/**
	 * set the specified jms property values
	 */
	private void setJMSPropertyValues(List propertyValues)
		throws WSIFException {
		if (propertyValues != null) {
			WSIFJMSDestination jmsDest = fieldJmsPort.getJmsDestination();
			for (Iterator i = propertyValues.iterator(); i.hasNext();) {
				JMSPropertyValue pv = (JMSPropertyValue) i.next();
				if (pv != null) {
					Object o = getObjectValue(pv.getType(), pv.getValue());
					if (!timeoutProperty(pv.getName(), o)) {
						jmsDest.setProperty(pv.getName(), o);
					}
				}
			}
		}
	}

	/**
	 * set the specified jms header values
	 */
	private void setJmsPropertyValues(HashMap attr) throws WSIFException {
		//FIXME
		// Need to separate out between header values and property values	
		Iterator iter = attr.keySet().iterator();
		while (iter.hasNext()) {
			String attName = (String) iter.next();

			JMSPropertyValue value = (JMSPropertyValue) attr.get(attName);

			if (value != null) {
				Object o = getObjectValue(value.getType(), value.getValue());
				if (!timeoutProperty(attName, o)) {
                    WSIFJMSDestination jmsDest = fieldJmsPort.getJmsDestination();
					jmsDest.setProperty(attName, o);
				}
			}
		}
	}

	private boolean timeoutProperty(String propertyName, Object value) {
		boolean isTimeoutProperty = false;
		if (WSIFConstants.WSIF_PROP_SYNC_TIMEOUT.equals(propertyName)) {
			isTimeoutProperty = true;
			try {
				syncTimeout = Long.parseLong(value.toString());
				Trc.event(this, "overridding syncTimeout to " + syncTimeout);
			} catch (NumberFormatException ex) {
				Trc.ignoredException(ex);
			}
		} else if (
			WSIFConstants.WSIF_PROP_ASYNC_TIMEOUT.equals(propertyName)) {
			isTimeoutProperty = true;
			try {
				asyncTimeout = Long.parseLong(value.toString());
				Trc.event(this, "overridding asyncTimeout to " + syncTimeout);
			} catch (NumberFormatException ex) {
				Trc.ignoredException(ex);
			}
		}
		return isTimeoutProperty;
	}

	/**
	 * get the specified operation (w/ input and output message)
	 */
	protected Operation getOperation() throws Exception {

		if (fieldOperation == null) {
			// <input> and <output> tags in binding operations are not mandatory
			// so deal with null BindingInputs or BindingOutputs
			if (fieldBindingOperation.getBindingInput() != null)
				fieldInputMessageName =
					fieldBindingOperation.getBindingInput().getName();

			if (fieldBindingOperation.getBindingOutput() != null)
				fieldOutputMessageName =
					fieldBindingOperation.getBindingOutput().getName();

			// Get operation
			fieldOperation =
				fieldBasePort.getBinding().getPortType().getOperation(
					fieldBindingOperation.getName(),
					fieldInputMessageName,
					fieldOutputMessageName);

			//
			// Jms extensions on Input
			//
			if (fieldBindingOperation.getBindingInput() != null) {
				Iterator inputIterator =
					fieldBindingOperation
						.getBindingInput()
						.getExtensibilityElements()
						.iterator();
				fieldInputJmsProperties = new HashMap();
				fieldInputJmsPropertyValues = new HashMap();
				while (inputIterator.hasNext()) {
					ExtensibilityElement ele =
						(ExtensibilityElement) inputIterator.next();
					if (ele instanceof JMSInput) {
						fieldInput = (JMSInput) ele;
					} else if (ele instanceof JMSProperty) {
						fieldInputJmsProperties.put(
							((JMSProperty) ele).getPart(),
							((JMSProperty) ele).getName());
					} else if (ele instanceof JMSPropertyValue) {
						fieldInputJmsPropertyValues.put(
							((JMSPropertyValue) ele).getName(),
							ele);
					}
				}
			}

			//
			// Jms extensions on Output
			//
			if (fieldBindingOperation.getBindingOutput() != null) {
				Iterator outputIterator =
					fieldBindingOperation
						.getBindingOutput()
						.getExtensibilityElements()
						.iterator();
				fieldOutputProperties = new HashMap();
				while (outputIterator.hasNext()) {
					ExtensibilityElement ele =
						(ExtensibilityElement) outputIterator.next();
					if (ele instanceof JMSOutput) {
						fieldOutput = (JMSOutput) ele;
					} else if (ele instanceof JMSProperty) {
						fieldOutputProperties.put(
							((JMSProperty) ele).getPart(),
							((JMSProperty) ele).getName());
					}
				}

			}
		}

		if (fieldOperation == null) {
			throw new WSIFException(
				"Unable to resolve Jms binding for operation '"
					+ fieldBindingOperation.getName()
					+ ":"
					+ fieldInputMessageName
					+ ":"
					+ fieldOutputMessageName
					+ "'");
		}

		return fieldOperation;
	}

	private Object getObjectValue(QName type, String value)
		throws WSIFException {

		Object primitiveType = null;

		if (JMSMessage.isSchemaNamespace(type.getNamespaceURI())
			&& JMSMessage.isXSDPrimitiveType(type.getLocalPart())) {
			Class cls =
				(Class) JMSMessage.PRIMITIVE_JAVA_MAPPING.get(
					type.getLocalPart().toLowerCase());

			if (cls == String.class) {
				primitiveType = value;
			}
			// For byte array class
			else if (cls == byte[].class) {
				primitiveType = value.getBytes();
			}
			// For Gregorian Calendar && Date
			else if (
				java.util.GregorianCalendar.class.isAssignableFrom(cls)
					|| java.util.Date.class.isAssignableFrom(cls)) {
				// DON"T DO ANYTHING since unable to know what the format is
			}
			// For all the rest
			else {
				try {
					java.lang.reflect.Constructor constructor =
						cls.getConstructor(new Class[] { String.class });
					primitiveType =
						constructor.newInstance(new Object[] { value });
				} catch (Exception e) {
					Trc.ignoredException(e);
				}
			}
		}

		if (primitiveType != null)
			return primitiveType;
		else
			throw new WSIFException(
				"Unable to create the java object for XSD Type '"
					+ type.toString()
					+ "' using value '"
					+ value
					+ "'");
	}

	//FIXME - Should I place somewhere else??
	public int getJMSMessageType() {
		Trc.entry(this);
		Iterator bindingIterator =
			fieldBasePort.getBinding().getExtensibilityElements().iterator();

		while (bindingIterator.hasNext()) {
			try {
				ExtensibilityElement ele =
					(ExtensibilityElement) bindingIterator.next();
				if (JMSConstants
					.Q_ELEM_JMS_BINDING
					.equals(ele.getElementType())) {

					int t = ((JMSBinding) ele).getJmsMessageType();
					Trc.exit(t);
					return t;
				}
			} catch (ClassCastException exn) {
				Trc.ignoredException(exn);
			}
		}
		Trc.exit(JMSConstants.MESSAGE_TYPE_NOTSET);
		return JMSConstants.MESSAGE_TYPE_NOTSET;
	}

	/**
	 * Sets if the currently executing request is an asynchronous request.
	 * 
	 * @param b   true if the current request is a asynchronous request,
	 *            otherwise false
	 */
	private void setAsyncOperation(boolean b) {
		asyncOperation = b;
	}

	/**
	 * Tests if the currently executing request is an asynchronous request.
	 * 
	 * @return   true if the current request is a asynchronous request,
	 *            otherwise false
	 */
	public boolean isAsyncOperation() {
		Trc.entry(this);
		Trc.exit(asyncOperation);
		return asyncOperation;
	}

	/**
	 * This sets up any context JMS property values in the Destination
	 */
	private void setDestinationContext() throws WSIFException{
		if (context == null) {
			return;
		}
		HashMap jmsProps = new HashMap();
		for (Iterator i = context.getPartNames(); i.hasNext();) {
			String partName = (String) i.next();
			try {
				Object value = context.getObjectPart(partName);
				if (!timeoutProperty(partName, value)) {
					if (partName
						.startsWith(WSIFConstants.CONTEXT_JMS_PREFIX)) {
						jmsProps.put(
							partName.substring(
								WSIFConstants.CONTEXT_JMS_PREFIX.length()),
							value);
					}
				}
			} catch (WSIFException ex) {
				Trc.ignoredException(ex);
			}
		}
		if (jmsProps.size() > 0) {
			WSIFJMSDestination jmsDest = fieldJmsPort.getJmsDestination();
			jmsDest.setProperties(jmsProps);
		}
	}

	private ArrayList getWSDLInputPartNames() {
		ArrayList wsdlInputParts = new ArrayList();
		Input wsdlInput = fieldOperation.getInput();
		if (wsdlInput != null) {
			Message inputMessage = wsdlInput.getMessage();
			String partName;
			for (Iterator i = inputMessage.getParts().keySet().iterator();
				i.hasNext();
				) {
				partName = (String) i.next();
				if (fieldInputJmsProperties == null
					|| !fieldInputJmsProperties.containsKey(partName)) {
					wsdlInputParts.add(partName);
				}
			}
		}
		return wsdlInputParts;
	}

	private ArrayList getWSDLOutputPartNames() {
		ArrayList wsdlOutputParts = new ArrayList();
		Output wsdlOutput = fieldOperation.getOutput();
		if (wsdlOutput != null) {
			Message outputMessage = wsdlOutput.getMessage();
			String partName;
			for (Iterator i = outputMessage.getParts().keySet().iterator();
				i.hasNext();
				) {
				partName = (String) i.next();
				if (fieldOutputProperties == null
					|| !fieldOutputProperties.containsKey(partName)) {
					wsdlOutputParts.add(partName);
				}
			}
		}
		return wsdlOutputParts;
	}

    public WSIFPort getWSIFPort() {
        Trc.entry(this);
        Trc.exit(fieldJmsPort);
        return fieldJmsPort;
    }

    /**
     * Sets JMS properties on a JMS message
     * This has the side effect of removing the JMS properties
     * from the srcMsg WSIFMessage.
     * @param targetMsg   the JMS message where properties will be set
     * @param srcMsg   WSIF message containing the JMS property values
     * @param formatter   JMSFormatter holding WSDL definitions defining
     *                     which JMS properties should be set
     */
    public static void setJMSMessageOutputHeaderProperties(
        javax.jms.Message targetMsg,
        WSIFMessage srcMsg,
        JMSFormatter formatter)
        throws WSIFException {

        Trc.entry(null, targetMsg, srcMsg, formatter);

        if (targetMsg == null) {
        	throw new IllegalArgumentException("targetMsg is null");
        }
        if (srcMsg == null) {
        	throw new IllegalArgumentException("srcMsg is null");
        }
        if (formatter == null) {
        	throw new IllegalArgumentException("formatter is null");
        }
        
        WSIFJMSProperties jmsProps = new WSIFJMSProperties(WSIFJMSProperties.OUT);

        Map newParts = new HashMap();
        Map properties = getJMSOutputProperties(formatter);
        for (Iterator i = srcMsg.getPartNames(); i.hasNext(); ) {
        	String partName = (String) i.next();
           	Object value = null;
           	try {
               	value = srcMsg.getObjectPart(partName);
       	    } catch (WSIFException e) {
       		    Trc.ignoredException(e);
       	    }
        	if (properties.containsKey(partName)) {
            	String jmsProperty = (String) properties.get(partName);
               	jmsProps.put(jmsProperty, value);
         	} else {
                newParts.put(partName, value);         		
        	}
        }

        jmsProps.set(null, targetMsg);        

        // replace srcMsg contents to only have non-jms property parts
		srcMsg.setParts(newParts);

        Trc.exit();
    }

    /**
     * Gets the output jms:property's defined in a WSDL JMS binding 
     */
    protected static Map getJMSOutputProperties(JMSFormatter f) throws WSIFException {
    	Map jmsProperties = new HashMap();

        BindingOperation bop = getFormatterOperation(f);
        BindingOutput bout = bop.getBindingOutput();
    	if (bout != null) {
    		List extEls = bout.getExtensibilityElements();
    		for (Iterator i = extEls.iterator(); i.hasNext(); ) {
    			ExtensibilityElement ele = (ExtensibilityElement) i.next();
				if (ele instanceof JMSProperty) {
    				jmsProperties.put(
    				    ((JMSProperty) ele).getPart(),
						((JMSProperty) ele).getName());
				}
			}
		}
    	
    	return jmsProperties;
    }

    /**
     * Gets the WSDL binding operation associated with a JMSFormatter
     */
    protected static BindingOperation getFormatterOperation(JMSFormatter f) throws WSIFException {

        Port port = f.getPort();
        if (port == null) {
        	throw new WSIFException("formatter contains a null Port");
        }

        BindingOperation bop =
            WSIFUtils.getBindingOperation(
                port.getBinding(),
                f.getOperationName(),
                f.getInputName(),
                f.getOutputName());
    	
        if (bop == null) {
        	throw new WSIFException("cannot find binding operation for formatter");
        }

    	return bop;
    }

	public String deep() {
		String buff = "";
		try {
			buff = new String(super.toString() + ":\n");
			buff += "basePort:" + Trc.brief(fieldBasePort);
			buff += " bindingOperation:" + Trc.brief(fieldBindingOperation);
			buff += " wsifPort_Jms:" + fieldJmsPort;
			buff += " Operation:" + Trc.brief(fieldOperation);
			buff += " InputOnlyOperation:" + inputOnlyOp;
			buff += " JmsMessageType:" + getJMSMessageType();
			buff += " inputMessageName:" + fieldInputMessageName;
			buff += " InputJmsPropertyValues:" + fieldInputJmsPropertyValues;
			buff += " InputProperty:" + fieldInputJmsProperties;
			buff += " Input:" + fieldInput;
			buff += " outputMessageName:" + fieldOutputMessageName;
			buff += " OutputProperties:" + fieldOutputProperties;
			buff += " Output:" + fieldOutput;
			buff += " Formater:" + formatter;
			buff += " handler:" + handler;
			buff += " asyncOperation:" + asyncOperation;
		} catch (Exception e) {
			Trc.exceptionInTrace(e);
		}
		return buff;
	}

}
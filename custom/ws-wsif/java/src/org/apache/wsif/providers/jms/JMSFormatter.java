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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingOperation;
import javax.wsdl.Fault;
import javax.wsdl.Operation;
import javax.wsdl.extensions.ExtensibilityElement;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFRequest;
import org.apache.wsif.WSIFResponse;
import org.apache.wsif.format.WSIFFormatter;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.util.WSIFUtils;
import org.apache.wsif.wsdl.extensions.jms.JMSFault;
import org.apache.wsif.wsdl.extensions.jms.JMSFaultIndicator;
import org.apache.wsif.wsdl.extensions.jms.JMSFaultProperty;
import org.apache.wsif.wsdl.extensions.jms.JMSInput;
import org.apache.wsif.wsdl.extensions.jms.JMSOutput;
import org.apache.wsif.wsdl.extensions.jms.JMSProperty;

/**
 * JMSFormatter
 * 
 * @author <a href="mailto:seto@ca.ibm.com">Norman Seto</a>
 * @author <a href="mailto:antelder@apache.org">Ant Elder</a>
 */
public class JMSFormatter implements WSIFFormatter, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private javax.wsdl.Definition fieldDefinition;
	private javax.wsdl.Port fieldPort;

    private String reqOpName;
    private String reqInputName;
    private String reqOutputName;
    private BindingFault lastBindingFault = null;

    /**
     * Constructor for JMSFormatter
     */
	public JMSFormatter(javax.wsdl.Definition def, javax.wsdl.Port port) {
        super();
		Trc.entry(this,def,port);
        fieldDefinition = def;
		fieldPort = port;
        Trc.exit();
    }

    /**
     * @see WSIFFormatter#formatRequest(WSIFRequest, OutputStream)
     */
    public void formatRequest(WSIFRequest req, OutputStream out) {
        Trc.entry(this, req);
        Trc.exit();
    }

    /**
     * @see WSIFFormatter#unformatRequest(InputStream)
     */
    public WSIFRequest unformatRequest(InputStream in) {
        Trc.entry(this);
        Trc.exit();
        return null;
    }

    /**
     * @see WSIFFormatter#formatResponse(WSIFResponse, OutputStream)
     */
    public void formatResponse(WSIFResponse resp, OutputStream out) {
        Trc.entry(this, resp);
        Trc.exit();
    }

    /**
     * @see WSIFFormatter#unformatResponse(InputStream)
     */
    public WSIFResponse unformatResponse(InputStream in) {
        Trc.entry(this);
        Trc.exit();
        return null;
    }

    public void formatRequest(WSIFRequest req, javax.jms.Message out)
        throws WSIFException {
        Trc.entry(this, req, out);

		Binding binding = fieldPort.getBinding();
        BindingOperation bop =
            binding.getBindingOperation(
                req.getOperationName(),
                req.getInputName(),
                req.getOutputName());
        javax.wsdl.Message inMessage = bop.getOperation().getInput().getMessage();

        org.apache.wsif.providers.jms.JMSMessage fhMsg =
            new org.apache.wsif.providers.jms.JMSMessage(
                fieldDefinition,
                binding,
                inMessage,
                getInputParts(bop));

        copyTo(req.getIncomingMessage(), fhMsg);
        fhMsg.write(out);

		setOperationName( req.getOperationName() );
		setInputName( req.getInputName() );
		setOutputName( req.getOutputName() );		
		
        Trc.exit();
    }

    public WSIFRequest unformatRequest(javax.jms.Message in)
        throws WSIFException {
        Trc.entry(this, in);
        // Need to construct the request.
        WSIFRequest req = createRequest(in);

		Binding binding = fieldPort.getBinding();
        BindingOperation bop =
            binding.getBindingOperation(
                req.getOperationName(),
                req.getInputName(),
                req.getOutputName());

        javax.wsdl.Message inMessage = bop.getOperation().getInput().getMessage();

        org.apache.wsif.providers.jms.JMSMessage fhMsg =
            new org.apache.wsif.providers.jms.JMSMessage(
                fieldDefinition,
                binding,
                inMessage,
                getInputParts(bop));

        fhMsg.read(in);

        req.setIncomingMessage(fhMsg);

        Trc.exit(req);
        return req;
    }

    protected WSIFRequest createRequest(javax.jms.Message msg)
        throws WSIFException {
        Trc.entry(this, msg);
        javax.xml.namespace.QName serviceName = null;
        String operationName = null;
        String input = null;
        String output = null;

		Binding fieldBinding = fieldPort.getBinding();
		if (fieldBinding == null)
			throw new WSIFException("Unable to locate Binding");		

		//Locate Service
        Map sm = WSIFUtils.getAllItems( fieldDefinition, "Service" );
		Iterator services = sm.values().iterator();        				
		while (services.hasNext() && serviceName ==null) {
			javax.wsdl.Service s = (javax.wsdl.Service)services.next();
					
			for (Iterator ports = s.getPorts().values().iterator(); ports.hasNext();) {
				javax.wsdl.Port p = (javax.wsdl.Port)ports.next();
				if (p.getBinding() != null && p.getBinding().getQName().equals(fieldBinding.getQName())) {
					// Binding found
					serviceName = s.getQName();
					break;
				}
			}
		}
		
		if (serviceName == null)
			throw new WSIFException("Unable to locate Service");


        // Find the operation, if the Port only contains 1 operation then use
		// that one, otherwise use the one defined in the JMS message properties.
		javax.wsdl.PortType portType = fieldBinding.getPortType();
		if (portType!= null) {
		   List operations = portType.getOperations();
		   if (operations != null && operations.size() == 1 ) {
			     javax.wsdl.Operation o = (javax.wsdl.Operation)operations.getFirst();
				 operationName = o.getName();
				 if (o.getInput() != null) {
				    input = o.getInput().getName();
				 }
				 if (o.getOutput() != null) {
				    output = o.getOutput().getName();
				 }
			}
		}

		if (operationName == null) {
		   try {
			  operationName = msg.getStringProperty( WSIFConstants.JMS_PROP_OPERATION_NAME );
			  input = msg.getStringProperty( WSIFConstants.JMS_PROP_INPUT_NAME );
			  output = msg.getStringProperty( WSIFConstants.JMS_PROP_OUTPUT_NAME );			
		   } catch (javax.jms.JMSException e) {
		   	  Trc.exception(e);
		   }
		}
 
		if (operationName == null)
		   throw new WSIFException( "Unable to determine Operation" );

				
		WSIFRequest request = new WSIFRequest( serviceName );
		request.setPortName( fieldPort.getName() );
        request.setOperationName( operationName );
        request.setInputName( input );
        request.setOutputName( output );
        Trc.exit( request );
        return request;
    }

    protected WSIFResponse createResponse(javax.jms.Message msg)
        throws WSIFException {
        Trc.entry(this, msg);
        javax.xml.namespace.QName serviceName = null;
        String operationName = null;
        String input = null;
        String output = null;

		Binding fieldBinding = fieldPort.getBinding();
		if (fieldBinding == null)
			throw new WSIFException("Unable to locate Binding");		

		//Locate Service
        Map sm = WSIFUtils.getAllItems( fieldDefinition, "Service" );
		Iterator services = sm.values().iterator();        				
		while (services.hasNext() && serviceName ==null) {
			javax.wsdl.Service s = (javax.wsdl.Service)services.next();
					
			for (Iterator ports = s.getPorts().values().iterator(); ports.hasNext();) {
				javax.wsdl.Port p = (javax.wsdl.Port)ports.next();
				if (p.getBinding() != null && p.getBinding().getQName().equals(fieldBinding.getQName())) {
					// Binding found
					serviceName = s.getQName();
					break;
				}
			}
		}
		
		if (serviceName == null)
			throw new WSIFException("Unable to locate Service");

		// Locate the Operation from the stored values
		operationName = getOperationName();
		input = getInputName();
		output = getOutputName();

		if (operationName == null || operationName.equals("")) {	
			// Nothing defined.  Resort to getting the info from the wsdl document
			javax.wsdl.PortType portType = fieldBinding.getPortType();
			if (portType!= null) {
				List operations = portType.getOperations();
				if (operations != null) {
					if (operations.size() == 1) {
						javax.wsdl.Operation o = (javax.wsdl.Operation)operations.getFirst();
						operationName = o.getName();
						if (o.getInput() != null)
							input = o.getInput().getName();
						if (o.getOutput() != null)
							output = o.getOutput().getName();
					}
					else
						throw new WSIFException("Port Type definition contains multiple operations.");
				}
			}
		}

		if (operationName == null)
			throw new WSIFException("Unable to locate Operation");
				
		WSIFResponse response = new WSIFResponse(serviceName);
		response.setPortName(fieldPort.getName());
        response.setOperationName(operationName);
        response.setInputName(input);
        response.setOutputName(output);
        Trc.exit(response);
        return response;
    }

    public void formatResponse(WSIFResponse resp, javax.jms.Message out)
        throws WSIFException {
        Trc.entry(this, resp, out);
		Binding binding = fieldPort.getBinding();
        BindingOperation bop =
            binding.getBindingOperation(
                resp.getOperationName(),
                resp.getInputName(),
                resp.getOutputName());
        javax.wsdl.Message outMessage = bop.getOperation().getOutput().getMessage();

        org.apache.wsif.providers.jms.JMSMessage fhMsg =
            new org.apache.wsif.providers.jms.JMSMessage(
                fieldDefinition,
                binding,
                outMessage,
                getOutputParts(bop));

        copyTo(resp.getOutgoingMessage(), fhMsg);
        fhMsg.write(out);

        setOperationName( resp.getOperationName() );
        setInputName( resp.getInputName() );
        setOutputName( resp.getOutputName() );       
        Trc.exit();
    }

    /**
     * @see WSIFFormatter#unformatResponse(InputStream)
     */
    public WSIFResponse unformatResponse(javax.jms.Message out)
        throws WSIFException {
        	
        Trc.entry(this, out);
        // Need to construct the response.
        WSIFResponse resp = createResponse(out);

		Binding binding = fieldPort.getBinding();
        BindingOperation bop =
            binding.getBindingOperation(
                resp.getOperationName(),
                resp.getInputName(),
                resp.getOutputName());
                
        if (unformatResponseFault(resp, out, binding, bop)) {
        	Trc.exit(resp);
            return resp;
        }
        
        Operation op = bop.getOperation();
        javax.wsdl.Output output =  op.getOutput();
        if ( output != null ) {
           javax.wsdl.Message outMessage = output.getMessage();

           org.apache.wsif.providers.jms.JMSMessage fhMsg =
              new org.apache.wsif.providers.jms.JMSMessage(
                 fieldDefinition,
                 binding,
                 outMessage,
                 getOutputParts(bop));

           fhMsg.read(out);

           resp.setOutgoingMessage(fhMsg);
        }

        Trc.exit(resp);
        return resp;
    }

    /**
     * If this message is a fault message, unformat it, else do nothing
     * @return true for a fault, false if it is not a fault.
     */
    private boolean unformatResponseFault(
        WSIFResponse resp,
        javax.jms.Message out,
        Binding binding,
        BindingOperation bop)
        throws WSIFException {

        Trc.entry(this, resp, out, binding, bop);

        // Iterate through the BindingOperation to find all the <jms:faultIndicators.
        Map bndFs = bop.getBindingFaults();
        if (bndFs != null && !bndFs.isEmpty()) {
            Iterator itBndFNames = bndFs.keySet().iterator();
            while (itBndFNames.hasNext()) {
                String bndFName = (String) itBndFNames.next();
                BindingFault bndF = (BindingFault) bndFs.get(bndFName);

                List bndFElems = bndF.getExtensibilityElements();
                if (bndFElems == null || bndFElems.isEmpty())
                    continue;

                Iterator itBndFElems = bndFElems.iterator();
                while (itBndFElems.hasNext()) {
                    Object bndFElem = itBndFElems.next();
                    
                    // Ignore anything that isn't a fault indicator, since
                    // those will be dealt with by unformatFaultMessage.
                    if (bndFElem instanceof JMSFaultIndicator indic) {

                        // Only the first fault indicator that matches is used.
                        // If others match, then this error is ignored.
                        if (matchesFaultIndicator(indic, out)) {
                            WSIFMessage msg =
                                unformatFaultMessage(
                                    out,
                                    binding,
                                    bop,
                                    bndFName,
                                    bndF);

                            resp.setOutgoingMessage(msg);
                            resp.setIsFault(true);
                            lastBindingFault = bndF;
                            Trc.exit(true);
                            return true;
                        }
                    }
                }
            }
        }
        Trc.exit(false);
        return false;
    }

    /**
     * Look to see whether this JMSFaultIndicator matches this output JMS message
     * by seeing whether the faultProperty exists on the message and has the 
     * same value.
     * 
     * It would be good to first see if there are any properties on the message 
     * that might indicate a fault so this method could return false quickly.
     * Unfortunately I assume all JMS messages will have a number of properties
     * set so this performance enhancement is not available.
     * 
     * @return true for a match, false for no match.
     */    
    private boolean matchesFaultIndicator(
        JMSFaultIndicator indic,
        javax.jms.Message out) {
        Trc.entry(this, indic, out);

        // Fault indicators must have type=property. Other types are ignored.
        String type = indic.getType();
        if (type == null || !"property".equals(type)) {
            Trc.exit(false);
            return false;
        }

        List fProps = indic.getJMSFaultProperties();
        if (fProps == null || fProps.isEmpty()) {
            Trc.exit(false);
            return false;
        }

        Iterator itFProps = fProps.iterator();
        while (itFProps.hasNext()) {
            JMSFaultProperty fProp = (JMSFaultProperty) itFProps.next();
            String propName = fProp.getName();
            if (propName == null
                || fProp.getType() == null
                || fProp.getValue() == null)
                continue;

            Object propValue = null;
            try {
                if (!out.propertyExists(propName))
                    continue;

                propValue = out.getObjectProperty(propName);
            } catch (JMSException je) {
                Trc.ignoredException(je);
                continue;
            }

            if (propValue != null
                && fProp.getValue().equals(propValue.toString())) {
                Trc.exit(true);
                return true;
            }
        }

        Trc.exit(false);
        return false;
    }

    private WSIFMessage unformatFaultMessage(
        javax.jms.Message out,
        Binding binding,
        BindingOperation bop,
        String bndFName,
        BindingFault bndF)
        throws WSIFException {
        Trc.entry(this, out, binding, bop, bndFName, bndF);

        Operation op = bop.getOperation();
        Fault fault = op.getFault(bndFName);
        if (fault == null)
            throw new WSIFException(
                "No fault "
                    + bndFName
                    + " found in operation "
                    + op.getName());

        JMSMessage fhMsg =
            new JMSMessage(
                fieldDefinition,
                binding,
                fault.getMessage(),
                getFaultParts(bndF));

        fhMsg.read(out);
        Trc.exit(fhMsg);
        return fhMsg;

    }
    
    void copyTo(
        WSIFMessage source,
        WSIFMessage target)
        throws WSIFException {
        if (source == null || target == null)
            return;
        for (Iterator i = source.getPartNames(); i.hasNext();) {
            String partName = i.next().toString();
            target.setObjectPart(partName, source.getObjectPart(partName));
        }
    }

    private List getInputParts(
        BindingOperation bindingOperation) {
        if (bindingOperation.getBindingInput() != null) {
            Iterator inputIterator =
                bindingOperation.getBindingInput().getExtensibilityElements().iterator();

            while (inputIterator.hasNext()) {
                ExtensibilityElement ele =
                    (ExtensibilityElement) inputIterator.next();
                if (ele instanceof JMSInput input) {
                    return input.getParts();
                }
            }
        }
        return null;
    }

    private List getOutputParts(
        BindingOperation bindingOperation) {
        if (bindingOperation.getBindingOutput() != null) {
            Iterator outputIterator =
                bindingOperation.getBindingOutput().getExtensibilityElements().iterator();

            while (outputIterator.hasNext()) {
                ExtensibilityElement ele =
                    (ExtensibilityElement) outputIterator.next();
                if (ele instanceof JMSOutput output) {
                    return output.getParts();
                }
            }
        }
        return null;
    }

    private List getFaultParts(BindingFault bindingFault) {
        Trc.entry(this, bindingFault);

        List list = null;
        if (bindingFault != null) {
            Iterator it = bindingFault.getExtensibilityElements().iterator();
            while (it.hasNext()) {
                Object ele = it.next();
                if (ele instanceof JMSFault fault) {
                    list = fault.getParts();
                    break;
                }
            }
        }
        Trc.exit(list);
        return list;
    }

    public String getOperationName() {
       return reqOpName;
    }

    private void setOperationName(String s) {
       reqOpName = s;
    }

    public String getInputName() {
       return reqInputName;
    }

    private void setInputName(String s) {
       reqInputName = s;
    }

    public String getOutputName() {
       return reqOutputName;
    }

    private void setOutputName(String s) {
       reqOutputName = s;
    }
    
    public BindingFault getLastBindingFault() {
        Trc.entry(this);
        Trc.exit(lastBindingFault);
        return lastBindingFault;
    }

    /**
     * Returns the WSDL Definition.
     * @return javax.wsdl.Definition
     */
    public javax.wsdl.Definition getDefinition() {
        return fieldDefinition;
    }

    /**
     * Returns the WSDL Port.
     * @return javax.wsdl.Port
     */
    public javax.wsdl.Port getPort() {
        return fieldPort;
    }

}

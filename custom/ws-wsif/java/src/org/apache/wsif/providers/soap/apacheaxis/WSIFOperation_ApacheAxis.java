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

package org.apache.wsif.providers.soap.apacheaxis;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.jms.TextMessage;
import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.extensions.mime.MIMEContent;
import javax.wsdl.extensions.mime.MIMEMimeXml;
import javax.wsdl.extensions.mime.MIMEMultipartRelated;
import javax.wsdl.extensions.mime.MIMEPart;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPFault;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPHeaderFault;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.soap.SOAPException;

import org.apache.axis.AxisEngine;
import org.apache.axis.AxisFault;
import org.apache.axis.AxisProperties;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.client.Transport;
import org.apache.axis.encoding.DeserializerFactory;
import org.apache.axis.encoding.SerializerFactory;
import org.apache.axis.encoding.SimpleType;
import org.apache.axis.encoding.TypeMapping;
import org.apache.axis.encoding.TypeMappingRegistry;
import org.apache.axis.encoding.ser.ArrayDeserializerFactory;
import org.apache.axis.encoding.ser.ArraySerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.encoding.ser.SimpleDeserializerFactory;
import org.apache.axis.encoding.ser.SimpleSerializerFactory;
import org.apache.axis.message.RPCElement;
import org.apache.axis.message.RPCParam;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPFaultElement;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFCorrelationId;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFResponseHandler;
import org.apache.wsif.attachments.WSIFAttachmentPart;
import org.apache.wsif.base.WSIFDefaultOperation;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.providers.ProviderUtils;
import org.apache.wsif.providers.WSIFDynamicTypeMap;
import org.apache.wsif.providers.WSIFDynamicTypeMapping;
import org.apache.wsif.util.TypeSerializerInfo;
import org.apache.wsif.util.WSIFProperties;
import org.apache.wsif.util.WSIFUtils;
import org.apache.wsif.util.jms.WSIFJMSDestination;
import org.apache.wsif.wsdl.extensions.jms.JMSProperty;
import org.apache.wsif.wsdl.extensions.jms.JMSPropertyValue;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ibm.wsdl.extensions.mime.MIMEConstants;

/**
 * The WSIFOperation class for invoking WSDL operations using Apache AXIS.
 *   
 * @author Mark Whitlock <whitlock@apache.org>
 * @author Ant Elder <ant.elder@uk.ibm.com>
 */
public class WSIFOperation_ApacheAxis extends WSIFDefaultOperation {

	private static final long serialVersionUID = 2L;

	transient protected WSIFPort_ApacheAxis wsifPort;
	
	transient protected Operation portTypeOperation;
	transient protected BindingOperation bindingOperation;
	transient protected SOAPOperation soapOperation;
	
	transient protected List inputSOAPParts;
	transient protected List inputUnwrappedSOAPParts;
	transient protected List inputMIMEParts;
	transient protected Part inputSOAPHeader;
	transient protected Part inputSOAPHeaderFault;

	protected List outputSOAPParts;
	protected List outputUnwrappedSOAPParts;
	protected List outputMIMEParts;
	protected Part outputSOAPHeader;
	protected Part outputSOAPHeaderFault;

	transient protected String inputEncodingStyle;
	transient protected String inputNamespace;
	transient protected String inputUse;

	transient protected String soapActionURI;

	transient protected HashMap responseMessageParameters;

	// for async operation
	transient protected boolean asyncOperation;
	transient protected WSIFCorrelationId asyncRequestID;

	// everything other than what is needed to process async response should be transient
	protected WSIFResponseHandler responseHandler;
	protected String outputEncodingStyle;
	protected WSIFDynamicTypeMap typeMap;
	protected String operationStyle;
	
    /**
     * Construct a new WSIFOperation
     */
	public WSIFOperation_ApacheAxis(
		WSIFPort_ApacheAxis wsifPort,
		Operation portTypeOperation,
		WSIFDynamicTypeMap typeMap)
		throws WSIFException {
		Trc.entry(this, wsifPort, portTypeOperation, typeMap);

        /* Note: if you change anything here make sure you consider
         * the impact to the constructor, copy and prepare methods
         * and to if any instance variables are transient 
         */

		this.wsifPort = wsifPort;
		this.portTypeOperation = portTypeOperation;
		this.typeMap = typeMap;

		this.bindingOperation = getBindingOperation(portTypeOperation);

		this.inputEncodingStyle = WSIFAXISConstants.DEFAULT_SOAP_ENCODING_URI;
		this.outputEncodingStyle = WSIFAXISConstants.DEFAULT_SOAP_ENCODING_URI;

		if (Trc.ON) {
			Trc.exit(deep());
		}
	}

	/**
	 * Create a new copy of this object. This is not a clone, since 
	 * it does not copy the referenced objects as well.
	 * The intention here is to copy anything that the prepare method
	 * has done so that WSIFOperation instances can be cached by the
	 * WSIFPort and safely reused with minimum overhead. 
	 * @deprecated why was this ever made public??? It should only
	 * ever be used by the WSIFPort!!!
	 */
	public WSIFOperation_ApacheAxis copy() throws WSIFException {
		Trc.entry(this);

        /* Note: if you change anything here make sure you consider
         * the impact to the constructor, copy and prepare methods
         * and to if any instance variables are transient 
         */

		WSIFOperation_ApacheAxis op =
			new WSIFOperation_ApacheAxis(wsifPort, portTypeOperation, typeMap);

        op.inputSOAPParts = inputSOAPParts;
        op.inputUnwrappedSOAPParts = inputUnwrappedSOAPParts;
        op.inputMIMEParts = inputMIMEParts;
	    op.inputSOAPHeader = inputSOAPHeader;
        op.inputSOAPHeaderFault = inputSOAPHeaderFault;

        op.outputSOAPParts = outputSOAPParts;
        op.outputUnwrappedSOAPParts = outputUnwrappedSOAPParts;
        op.outputMIMEParts = outputMIMEParts;
        op.outputSOAPHeader = outputSOAPHeader;
        op.outputSOAPHeaderFault = outputSOAPHeaderFault;

	    op.soapOperation = soapOperation;

		op.setSoapActionURI(getSoapActionURI());
		op.setInputNamespace(getInputNamespace());
		op.setInputEncodingStyle(getInputEncodingStyle());
		op.setInputUse(getInputUse());
		op.setOutputEncodingStyle(getOutputEncodingStyle());
		op.setAsyncOperation(isAsyncOperation());
		op.setResponseHandler(getResponseHandler());
		op.setInputJmsProperties(getInputJmsProperties());
		op.setOutputJmsProperties(getOutputJmsProperties());
		op.setInputJmsPropertyValues(getInputJmsPropertyValues());
		op.setOperationStyle(getOperationStyle());

		if (Trc.ON)
			Trc.exit(op.deep());
		return op;
	}

	/**
	 * Initialises instance variables relating to the WSDL soap:operation element
	 * The soap:operation WSDL element has the form: 
	 *    <soap:operation soapAction="uri"? style="rpc|document"?>?
	 */
	private void parseSoapOperation() throws WSIFException {

		this.soapOperation = wsifPort.getSOAPOperation(bindingOperation);
		this.soapActionURI = soapOperation.getSoapActionURI();
		this.operationStyle = soapOperation.getStyle();

		if (operationStyle == null || operationStyle.length() < 1) {
			operationStyle = wsifPort.getBindingStyle();
		} else if (!WSIFAXISConstants.VALID_STYLES.contains(operationStyle)) {
			throw new WSIFException(
				"unsupported style "
					+ operationStyle
					+ " for operation "
					+ portTypeOperation.getName());
		}
		if (operationStyle == null || operationStyle.length() < 1) {
			operationStyle = WSIFAXISConstants.STYLE_DOCUMENT;
		}
	}

	/**
	 * Initialises instance variables relating to the WSDL binding:input element
	 * The WSDL binding input has the form:
	 *      <input>
	 *          <soap:body parts="nmtokens"? use="literal|encoded"
	 *                     encodingStyle="uri-list"? namespace="uri"?>
	 *          <soap:header message="qname" part="nmtoken" use="literal|encoded"
	 *                       encodingStyle="uri-list"? namespace="uri"?>*
	 *              <soap:headerfault message="qname" 
	 *                                part="nmtoken" use="literal|encoded"
	 *                                encodingStyle="uri-list"? namespace="uri"?/>*
	 *          <soap:header>                                
	 *      </input>
	 */
	private void parseBindingInput() throws WSIFException {
		BindingInput bindinginput = bindingOperation.getBindingInput();
		List inExtElems = bindinginput.getExtensibilityElements();
		
		SOAPBody inSoapBody =
			(SOAPBody) wsifPort.getExtElem(
				bindinginput,
				javax.wsdl.extensions.soap.SOAPBody.class,
				inExtElems);
		if (inSoapBody != null) {
			this.inputSOAPParts = parseSoapBody(inSoapBody, true);
		}

		MIMEMultipartRelated inMimeMultipart =
			(MIMEMultipartRelated) wsifPort.getExtElem(
				bindinginput,
				MIMEMultipartRelated.class,
				inExtElems);

		if (inSoapBody != null && inMimeMultipart != null) {
			throw new WSIFException(
				"In a binding operation that contains a mime:multipartRelated, "
					+ "a soap:body was found that was not in a mime:part. "
					+ "OperationName="
					+ getName());
		}
		if (inSoapBody == null && inMimeMultipart == null) {
			throw new WSIFException(
				"binding operation input must contain either a soap:body " +
				"or a mime:multipartRelated element. "
					+ "OperationName="
					+ getName());
		}

		if (inMimeMultipart != null) {
			parseMimeMultipart(inMimeMultipart, true);
		}

		MIMEMimeXml inMimeMimeXml =
			(MIMEMimeXml) wsifPort.getExtElem(
				bindinginput,
				MIMEMimeXml.class,
				inExtElems);
		if (inMimeMimeXml != null)
			throw new WSIFException(
				"WSIF does not support mime:mimeXml. Operation="
					+ getName());

		parseSOAPHeaderElement(bindinginput);

		List inJmsProps =
			wsifPort.getExtElems(
				bindinginput,
				JMSProperty.class,
				bindinginput.getExtensibilityElements());
		if (inJmsProps != null && inJmsProps.size() > 0) {
			if (wsifPort.isTransportJMS())
				setInputJmsProperties(inJmsProps);
			else
				throw new WSIFException("jms:property found in non-jms binding");
		}

		List inJmsPropVals =
			wsifPort.getExtElems(
				bindinginput,
				JMSPropertyValue.class,
				bindinginput.getExtensibilityElements());
		if (inJmsPropVals != null && inJmsPropVals.size() > 0) {
			if (wsifPort.isTransportJMS())
				addInputJmsPropertyValues(inJmsPropVals);
			else
				throw new WSIFException("jms:propertyValue found in non-jms binding");
		}
	}

	/**
	 * Initialises instance variables relating to the WSDL binding:output element
	 * The WSDL binding output has the form:
	 *      <output>
	 *          <soap:body parts="nmtokens"? use="literal|encoded"
	 *                     encodingStyle="uri-list"? namespace="uri"?>
	 *          <soap:header message="qname" part="nmtoken" use="literal|encoded"
	 *                       encodingStyle="uri-list"? namespace="uri"?>*
	 *              <soap:headerfault message="qname" 
	 *                                part="nmtoken" use="literal|encoded"
	 *                                encodingStyle="uri-list"? namespace="uri"?/>*
	 *          <soap:header>                                
	 *      </output>
	 */
	private void parseBindingOutput() throws WSIFException {
		BindingOutput bindingoutput = bindingOperation.getBindingOutput();
		if (bindingoutput != null) {
			List outExtElems = bindingoutput.getExtensibilityElements();
			SOAPBody outSoapBody =
				(SOAPBody) wsifPort.getExtElem(
					bindingoutput,
					javax.wsdl.extensions.soap.SOAPBody.class,
					outExtElems);
			if (outSoapBody != null) {
			    this.outputSOAPParts = parseSoapBody(outSoapBody, false);
			}

			MIMEMultipartRelated outMimeMultipart =
				(MIMEMultipartRelated) wsifPort.getExtElem(
					bindingoutput,
					MIMEMultipartRelated.class,
					outExtElems);

			if (outSoapBody != null && outMimeMultipart != null) {
				throw new WSIFException(
					"In a binding operation that contains a mime:multipartRelated, "
						+ "a soap:body was found that was not in a mime:part. "
						+ "OperationName="
						+ getName());
			}
			if (outSoapBody == null && outMimeMultipart == null) {
				throw new WSIFException(
  				    "binding operation output must contain either a soap:body " +
				    "or a mime:multipartRelated element. " +
					"OperationName=" +
					getName());
			}

			if (outMimeMultipart != null) {
				parseMimeMultipart(outMimeMultipart, false);
			}

			MIMEMimeXml outMimeMimeXml =
				(MIMEMimeXml) wsifPort.getExtElem(
					bindingoutput,
					MIMEMimeXml.class,
					outExtElems);
			if (outMimeMimeXml != null) {
				throw new WSIFException(
					"WSIF does not support mime:mimeXml. Operation=" +
				    getName());
			}

			parseSOAPHeaderElement(bindingoutput);

			for (Iterator iterator1 =
				bindingOperation.getBindingFaults().values().iterator();
				iterator1.hasNext();
				) {
				BindingFault bindingfault = (BindingFault) iterator1.next();
				SOAPFault soapfault =
					(SOAPFault) wsifPort.getExtElem(
						bindingfault,
						javax.wsdl.extensions.soap.SOAPFault.class,
						bindingfault.getExtensibilityElements());
			}

			List outJmsProps =
				wsifPort.getExtElems(
					bindingoutput,
					JMSProperty.class,
					outExtElems);
			if (outJmsProps != null && outJmsProps.size() > 0) {
				if (wsifPort.isTransportJMS()) {
					setOutputJmsProperties(outJmsProps);
				} else {
					throw new WSIFException("jms:properties found in non-jms binding");
				}
			}
		}
	}

	/**
	 * Parses any SOAP header elements in the binding input or output.
	 * The WSDL soap:header element has the form:
	 *    <soap:header message="qname" part="nmtoken" use="literal|encoded"
	 *                 encodingStyle="uri-list"? namespace="uri"?>*
	 *       <soap:headerfault message="qname" part="nmtoken" use="literal|encoded"
	 *                         encodingStyle="uri-list"? namespace="uri"?/>*
	 *    <soap:header>                                
	 */
	private void parseSOAPHeaderElement(Object element) throws WSIFException {

		List extensabilityElements;
		if (element instanceof BindingInput) {
			extensabilityElements =
				((BindingInput) element).getExtensibilityElements();
		} else if (element instanceof BindingOutput) {
			extensabilityElements =
				((BindingOutput) element).getExtensibilityElements();
		} else {
			throw new WSIFException(
				"internal error, unexpected object: " + element);
		}

		Part soapHeaderPart = null;
		Part soapHeaderFaultPart = null;

		SOAPHeader soapHeader =
			(SOAPHeader) wsifPort.getExtElem(
				element,
				javax.wsdl.extensions.soap.SOAPHeader.class,
				extensabilityElements);
		if (soapHeader != null) {
			QName messageName = soapHeader.getMessage();
			if (messageName == null) {
				throw new WSIFException(
					"no message attribute on soap:header: " + soapHeader);
			}
			String messagePart = soapHeader.getPart();
			if (messagePart == null) {
				throw new WSIFException(
					"no part attribute on soap:header: " + soapHeader);
			}
			soapHeaderPart = getPart(messageName, messagePart);
			if (soapHeaderPart == null) {
				throw new WSIFException(
					"non existent part specified on soap:header: "
						+ soapHeader);
			}

			SOAPHeaderFault soapHeaderFault =
				(SOAPHeaderFault) wsifPort.getExtElem(
					soapHeader,
					javax.wsdl.extensions.soap.SOAPHeaderFault.class,
					extensabilityElements);
			if (soapHeaderFault != null) {
				messageName = soapHeader.getMessage();
				if (messageName == null) {
					throw new WSIFException(
						"no message attribute on soap:header: " + soapHeader);
				}
				messagePart = soapHeader.getPart();
				if (messagePart == null) {
					throw new WSIFException(
						"no part attribute on soap:header: " + soapHeader);
				}
				soapHeaderFaultPart = getPart(messageName, messagePart);
				if (soapHeaderFaultPart == null) {
					throw new WSIFException(
						"non existent part specified on soap:header: "
							+ soapHeader);
				}
			}
		}

		if (element instanceof BindingInput) {
			this.inputSOAPHeader = soapHeaderPart;
			this.inputSOAPHeaderFault = soapHeaderFaultPart;
		} else {
			this.outputSOAPHeader = soapHeaderPart;
			this.outputSOAPHeaderFault = soapHeaderFaultPart;
		}
		//TODO now go and do something with them...
	}

	/**
	 * Validates the soap:body WSDL element.
	 * The soap:body WSDL element has the form:
	 *   <soap:body parts="nmtokens"? use="literal|encoded"
	 *              encodingStyle="uri-list"? namespace="uri"?>
	 * Returns an ArrayList of the WSDL parts 
	 */
	private List parseSoapBody(SOAPBody soapbody, boolean isInput)
		throws WSIFException {
		Trc.entry(this, soapbody, new Boolean(isInput));

        // get input namespace
		if (isInput) {
			String ns = soapbody.getNamespaceURI();
			if (ns != null) {
				setInputNamespace(soapbody.getNamespaceURI());
			}
		}

        // get use
		String use = soapbody.getUse();
		if (!WSIFAXISConstants.VALID_USES.contains(use)) {
			throw new WSIFException(
				"unsupported use " + use + " in " + soapOperation);
		}
		if (isInput == true) {
			setInputUse(use);
		} else {
//			setOutputUse(use);
		}

        // get encoding style
		if (isInput) {
			List l = soapbody.getEncodingStyles();
			if (l != null && l.size() > 0) {
				setInputEncodingStyle((String) l.get(0));
			}
		}

        // get all the WSDL parts. If the soap:body parts= is defined
        // only get those parts, otherwise get all parts in the WSDL message
        javax.wsdl.Message m = null;
        if (isInput) {
        	Input in = portTypeOperation.getInput();
        	if (in != null) {
                m = in.getMessage();
        	}
        } else {
        	Output out = portTypeOperation.getOutput();
        	if (out != null) {
        		m = out.getMessage();
        	}
        }
        ArrayList al = getParts(m, soapbody.getParts());

		Trc.exit(al);
		return al;
	}

    /**
     * Gets the parts from a WSDL message.
     * If the partNames list (from WSDL soap:body parts=) is 
     * not null only the parts named in the partName list are
     * returned, otherwise all parts are returned.
     */
    private ArrayList getParts(javax.wsdl.Message m, List partNames ) throws WSIFException{
        ArrayList al = new ArrayList();
        List parts = null;
        if (m != null) {
            parts = m.getOrderedParts(null);
        }

        if ((parts == null || parts.size() < 1)
        && (partNames != null && partNames.size() > 0)) {
   		    throw new WSIFException("part '" + 
   		        partNames.get(0) + 
   		        "' not defined in message " + m);
        }
        
        if (partNames == null) {
            if (parts != null) {
        	    al.addAll(parts);
            }
	    } else {
	        for (Iterator i = partNames.iterator(); i.hasNext(); ) {
	    	    String partName = (String) i.next();
	    		Part p = m.getPart(partName);
	    		if (p == null) {
	    		    throw new WSIFException("Part '" + 
	    		        partName + 
	    		        "' in soap:body parts not in message " + m);
	    		} 
	    		// as there can be multiple mime:content elements which
	    		// specify a coice of types (TODO: which wsif ignores for now)
	    		// we only want each mime part once
	    		if (!al.contains(p)) { 
	    		    al.add(p);
	    		}
	    	}
        }
        return al;
    }

    /**
     * Parses the mime:multipartRelated WSDL element
     * The mime:multipartRelated element has the form:
     *   <mime:multipartRelated>
     *      <mime:part> *
     *         <-- mime element -->
     *      </mime:part>
     *   </mime:multipartRelated>
     */
	private void parseMimeMultipart(
		MIMEMultipartRelated mimeMultipart,
		boolean isInput)
		throws WSIFException {
		Trc.entry(this, mimeMultipart);

		ArrayList mimePartNames = new ArrayList();
		SOAPBody soapBody = null;
		
		Operation op = bindingOperation.getOperation();
		Map inMessageParts = op.getInput().getMessage().getParts();
		Map outMessageParts =
			op.getOutput() == null
				? new HashMap()
				: op.getOutput().getMessage().getParts();

		List mimeParts = mimeMultipart.getMIMEParts();
		Iterator mimePartIt = mimeParts.iterator();
		while (mimePartIt.hasNext()) {
			Object nextMimePart = mimePartIt.next();
			if (nextMimePart instanceof MIMEPart) {
				MIMEPart mimePart = (MIMEPart) nextMimePart;
				if (!MIMEConstants
					.NS_URI_MIME
					.equals(mimePart.getElementType().getNamespaceURI()))
					throw new WSIFException(
						"A MIME part in binding operation "
							+ bindingOperation.getName()
							+ " did not have the correct namespace URI of "
							+ MIMEConstants.NS_URI_MIME
							+ ".");

				boolean containsSoapBody = false;
				boolean containsMimeContent = false;
				List mimePartChildren = mimePart.getExtensibilityElements();
				Iterator mimePartChildrenIt = mimePartChildren.iterator();
				while (mimePartChildrenIt.hasNext()) {
					Object nextChild = mimePartChildrenIt.next();
					if (nextChild instanceof MIMEContent) {
						MIMEContent mimeContent = (MIMEContent) nextChild;
						if (!MIMEConstants
							.NS_URI_MIME
							.equals(
								mimePart.getElementType().getNamespaceURI()))
							throw new WSIFException(
								"A MIME part in binding operation "
									+ bindingOperation.getName()
									+ " did not have the correct namespace URI of "
									+ MIMEConstants.NS_URI_MIME
									+ ".");
						containsMimeContent = true;
						if (containsSoapBody)
							throw new WSIFException(
								"A mime:part that contains a mime:content also "
									+ "contains a soap:body. Operation="
									+ getName());

						String partName = mimeContent.getPart();
						if (partName == null || partName.length() == 0)
							throw new WSIFException(
								"No part name for a mime:content. Operation="
									+ getName());

						if ((isInput && inMessageParts.get(partName) == null)
							|| (!isInput && outMessageParts.get(partName) == null))
							throw new WSIFException(
								"The part specified in a mime:content does "
									+ "not exist in the operation. Operation="
									+ getName()
									+ " Part="
									+ partName);

						mimePartNames.add(partName);

					} else if (nextChild instanceof SOAPBody) {
						if (soapBody!=null) {
							throw new WSIFException(
								"Multiple soap:body tags found in a "
									+ "mime:multipartRelated. Operation="
									+ getName());
						}
						soapBody = (SOAPBody)nextChild;

						containsSoapBody = true;
						if (containsMimeContent)
							throw new WSIFException(
								"A mime:part that contains a mime:content also "
									+ "contains a soap:body. Operation="
									+ getName());

					} else if (nextChild instanceof MIMEMultipartRelated) {
						throw new WSIFException(
							"WSIF does not support nesting mime:multipartRelated "
								+ "inside a mime:part. Operation="
								+ getName());
					} else if (nextChild instanceof MIMEMimeXml) {
						throw new WSIFException(
							"WSIF does not support mime:mimeXml. Operation="
								+ getName());
					}
				}
			}
		}

        ArrayList mimePartList = new ArrayList();
        if (mimePartNames != null && !mimePartNames.isEmpty()) {
            javax.wsdl.Message m = null;
            if (isInput) {
                Input in = portTypeOperation.getInput();
                if (in != null) {
                    m = in.getMessage();
                }
            } else {
                Output out = portTypeOperation.getOutput();
                if (out != null) {
                    m = out.getMessage();
                }
            }

            /* 
             * Mime parts can be referenced or unreferenced from the
             * soap:body. By default they are all referenced. Mime parts
             * can be unreferenced if there is a soap:body parts="a b c"
             * which does not reference all the mime parts.
             */
            mimePartList = getParts(m, mimePartNames);
        }

        // There is at most one soap:body so process it here.		
        Map messageParts = null;
        if (isInput)
            messageParts = inMessageParts;
        else
            messageParts = outMessageParts;

        // There is at most one soap:body so process it here.		
        List soapParts = null;
        if (soapBody != null) {
            soapParts = parseSoapBody(soapBody, isInput);
            if (isInput)
                this.inputSOAPParts = soapParts;
            else
                this.outputSOAPParts = soapParts;
        }

        if (isInput) {
            this.inputMIMEParts = mimePartList;
        } else {
            this.outputMIMEParts = mimePartList;
        }

   		Trc.exit();
	}

    /**
     * For document style operations the input and/or output
     * may be 'wrapped'. A wrapped operation has  a single top-
     * level element wrapping the argument elements. For an input 
     * message the top level wrapper element name must be the same
     * as the operation name, the output wrapper element name must 
     * be the name of the operation suffixed with "Response".
     * WSIF clients may use either the wrapped or unwrapped parts.
     */
    private void unwrapSOAPParts() throws WSIFException {
		if (WSIFAXISConstants.STYLE_DOCUMENT.equals(operationStyle)) {
			String operationName = getName();
			if (inputSOAPParts.size() == 1) {
				Part p = (Part)inputSOAPParts.get(0);
				QName elementName = p.getElementName();
				if (elementName != null && operationName.equals(elementName.getLocalPart())) {
				   this.inputUnwrappedSOAPParts = 
				      ProviderUtils.unWrapPart(p, getDefinition(), context);
				}
			}
			if (outputSOAPParts.size() == 1) {
				String s = operationName + "Response";
				Part p = (Part)outputSOAPParts.get(0);
				QName elementName = p.getElementName();
				if (elementName != null && s.equals(elementName.getLocalPart())) {
				   this.outputUnwrappedSOAPParts = 
				      ProviderUtils.unWrapPart(p, getDefinition(), context);
				}
			}
		}
    }

    /**
     * Gets the WSDL binding:operation element for this operation
     */
	private BindingOperation getBindingOperation(Operation operation)
		throws WSIFException {
		Binding binding = wsifPort.getPort().getBinding();
		BindingOperation bindingOp =
			WSIFUtils.getBindingOperation(binding, operation);
		if (bindingOp == null) {
			throw new WSIFException(
				"cannot find binding operation for port operation:"
					+ operation.getName());
		}
		return bindingOp;
	}

	/**
	 * Returns the Definition object for the WSDL file
	 * @return Definition   the WSDL4J Definition object
	 */
	public Definition getDefinition() {
		Trc.entry(this);
		Definition d = wsifPort.getDefinition();
		Trc.exit(d);
		return d;
	}

    /**
     * @deprecated use getWSIFPort
     */ 
	public WSIFPort_ApacheAxis getDynamicWSIFPort() {
		Trc.entry(this);
		Trc.exit(wsifPort);
		return wsifPort;
	}

	public String getInputEncodingStyle() {
		Trc.entry(this);
		Trc.exit(inputEncodingStyle);
		return inputEncodingStyle;
	}

	public String getInputNamespace() {
		Trc.entry(this);
		Trc.exit(inputNamespace);
		return inputNamespace;
	}

    /**
     * Returns the inputUse.
     * @return String
     */
    public String getInputUse() {
        return inputUse;
    }

    /**
     * Sets the inputUse.
     * @param inputUse The inputUse to set
     */
    protected void setInputUse(String inputUse) {
        this.inputUse = inputUse;
    }

	/**
	 * Gets the name of the portType wsdl:operation element
	 * being used by this WSIFOperation
	 * @return String   the operation name
	 */
	public String getName() {
		Trc.entry(this);
		String s = portTypeOperation.getName();
		Trc.exit(s);
		return s;
	}

	/**
	 * @deprecated use getPortTypeOperation
	 */
	public Operation getOperation() {
		Trc.entry(this);
		Operation o = getPortTypeOperation();
		Trc.exit(o);
		return o;
	}

	/**
	 * Returns the WSDL Part for the named part in a WSDL Message
	 */
	private Part getPart(QName message, String partName) {
		Part p = null;
		Definition def = wsifPort.getDefinition();
		javax.wsdl.Message m = def.getMessage(message);
		if (m != null) {
			p = m.getPart(partName);
		}
		return p;
	}

	/**
	 * Gets the portType wsdl:operation element used by this WSIFOperation
	 * @return Operation   the WSDL4J portType Operation object
	 */
	public Operation getPortTypeOperation() {
		Trc.entry(this);
		Trc.exit(portTypeOperation);
		return portTypeOperation;
	}

	public String getOutputEncodingStyle() {
		Trc.entry(this);
		Trc.exit(outputEncodingStyle);
		return outputEncodingStyle;
	}

	public String getSoapActionURI() {
		Trc.entry(this);
		Trc.exit(soapActionURI);
		return soapActionURI;
	}

	public Transport getTransport() throws WSIFException {
		Trc.entry(this);
		Transport t = wsifPort.getTransport();
		Trc.exit(t);
		return t;
	}

	public WSIFCorrelationId getAsyncRequestID() {
		Trc.entry(this);
		Trc.exit(asyncRequestID);
		return asyncRequestID;
	}

	private HashMap getResponseMessageParameters() {
		return responseMessageParameters;
	}

    public WSIFPort getWSIFPort() {
        Trc.entry(this);
        Trc.exit(wsifPort);
        return wsifPort;
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

	public void executeInputOnlyOperation(WSIFMessage inMsg)
		throws WSIFException {
		Trc.entry(this, inMsg);

        if (inMsg == null) {
            throw new IllegalArgumentException("input message is null");
        }

		setAsyncOperation(false);
		invokeRequestResponseOperation(inMsg, null, null);
		Trc.exit();
		return;
	}

	public boolean executeRequestResponseOperation(
		WSIFMessage inMsg,
		WSIFMessage outMsg,
		WSIFMessage faultMsg)
		throws WSIFException {

		Trc.entry(this, inMsg, outMsg, faultMsg);

        if (inMsg == null) {
            throw new IllegalArgumentException("input message is null");
        }
        if (outMsg == null) {
            throw new IllegalArgumentException("output message is null");
        }
        if (faultMsg == null) {
            throw new IllegalArgumentException("fault message is null");
        }

		close();
		setAsyncOperation(false);

		boolean succ =
				invokeRequestResponseOperation(inMsg, outMsg, faultMsg);

		Trc.exit(succ);
		return succ;
	}

	/**
	 * Performs a request response operation asynchronously.
	 * 
	 * @param input   input message to send to the operation
	 * @return the correlation ID or the request. The correlation ID
	 *         is used to associate the request with the WSIFOperation.
	 * @exception WSIFException if something goes wrong.
	 * @see WSIFOperation#executeRequestResponseAsync(WSIFMessage)
	 */
	public WSIFCorrelationId executeRequestResponseAsync(WSIFMessage inMsg)
		throws WSIFException {
		Trc.entry(this, inMsg);

        if (inMsg == null) {
            throw new IllegalArgumentException("input message is null");
        }

		WSIFCorrelationId id = executeRequestResponseAsync(inMsg, null);
		Trc.exit(id);
		return id;

	}

	/**
	 * Performs a request response operation asynchronously.
	 * 
	 * @param input   input message to send to the operation
	 * @param handler   the response handler that will be notified 
	 *        when the asynchronous response becomes available.
	 * @return the correlation ID or the request. The correlation ID
	 *         is used to associate the request with the WSIFOperation.
	 * @exception WSIFException if something goes wrong.
	 * @see WSIFOperation#executeRequestResponseAsync(WSIFMessage,WSIFResponseHandler)
	 */
	public WSIFCorrelationId executeRequestResponseAsync(
		WSIFMessage inMsg,
		WSIFResponseHandler handler)
		throws WSIFException {
		Trc.entry(this, inMsg, handler);

        if (inMsg == null) {
            throw new IllegalArgumentException("input message is null");
        }
		close();
		
		if (!wsifPort.supportsAsync()) {
			throw new WSIFException("asynchronous operations not available");
		}

		setAsyncOperation(true);
		setResponseHandler(handler);
		WSIFJmsTransport transport = (WSIFJmsTransport) getTransport();
		transport.setWsifOperation(this);
		transport.setAsyncOperation("true");

		invokeRequestResponseOperation(inMsg, null, null);

		transport.setAsyncOperation("false");
		WSIFCorrelationId id = getAsyncRequestID();
		Trc.exit(id);
		return id;

	}

	/**
	 * fireAsyncResponse is called by an AsyncListener when a response
	 * has been received for a previous executeRequestResponseAsync call.
	 * It passes the response to the executeAsyncResponse method of the
	 * associated WSIFResponseHandler.
	 * @see WSIFOperation#fireAsyncResponse(Object)
	 * @param response   an Object representing the response. The response
	 *            should be a JMS TextMessage containging the XML response.
	 */
	public void fireAsyncResponse(Object response) throws WSIFException {
		Trc.entry(this, response);

		Object result = deserialiseResponseObject(response);

		WSIFMessage outMsg = createOutputMessage();
		WSIFMessage faultMsg = createFaultMessage();
		buildResponseMessages(result, outMsg, faultMsg);

		getResponseHandler().executeAsyncResponse(outMsg, faultMsg);

		Trc.exit(outMsg);
	}

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
	 */
	public boolean processAsyncResponse(
		Object response,
		WSIFMessage output,
		WSIFMessage fault)
		throws WSIFException {
		Trc.entry(this, response, output, fault);

		Object result = deserialiseResponseObject(response);
		boolean ok = buildResponseMessages(result, output, fault);

		Trc.exit(ok);
		return ok;
	}

	/**
	 * Deserialise and unmarshall the JMS response message.
	 * Used to process the response to an asynchronous request.
	 * This is copied, with minor changes, from the 2nd half
	 * of the Apache Axis Call class invoke method.
	 */
	private Object deserialiseResponseObject(Object msg) throws WSIFException {
		if (msg == null) {
			throw new WSIFException("null response to async send");
		}
		if (!(msg instanceof TextMessage)) {
			throw new WSIFException("response not a javax.jms.TextMessage");
		}

		try {
			TextMessage m = (TextMessage) msg;
			Message responseMessage = new Message(m.getText());
			responseMessage.setMessageType(Message.RESPONSE);

			Service service = new Service();
			MessageContext msgContext = new MessageContext(service.getEngine());
			msgContext.setResponseMessage(responseMessage);

			TypeMappingRegistry tmr = msgContext.getTypeMappingRegistry();
			org.apache.axis.encoding.TypeMapping tm =
				(org.apache.axis.encoding.TypeMapping) tmr.getTypeMapping(
					outputEncodingStyle);

        	// register any default type mappings    	
    	    registerDefaultTypeMappings(tm, getContext());
    	
    	    // register any mappings from WSIFService.mapType calls
    	    registerDynamicTypes(tm, typeMap, getContext());

			Message resMsg = msgContext.getResponseMessage();
			SOAPEnvelope resEnv = resMsg.getSOAPEnvelope();

			Object b = resEnv.getFirstBody();
			if (b instanceof SOAPFaultElement) {
				return new AxisFault(b.toString());
			}

			RPCElement body = (RPCElement) b;

			Object result = null;
			HashMap outParams;
			Vector resArgs = body.getParams();

			if (resArgs != null && resArgs.size() > 0) {
				RPCParam param = (RPCParam) resArgs.get(0);
				result = param.getValue();

				if (resArgs.size() > 1) {
					outParams = new HashMap();
					for (int i = 1; i < resArgs.size(); i++) {
						RPCParam p = (RPCParam) resArgs.get(i);
						outParams.put(p.getName(), p.getValue());
					}
					setResponseMessageParameters(outParams);
				}
			}
			return result;
		} catch (Exception ex) {
			Trc.exception(ex);
			throw new WSIFException(ex.getMessage());
		}

	}

	/**
	 * Extracts the output or fault message parts from the axis response.
	 */
	private boolean buildResponseMessages(
		Object resp,
		WSIFMessage outMsg,
		WSIFMessage faultMsg)
		throws WSIFException {

		boolean respOK;

		if (resp instanceof AxisFault) {
			respOK = false;
			if (faultMsg != null) {
				AxisFault f = (AxisFault) resp;
				faultMsg.setName(WSIFConstants.SOAP_FAULT_MSG_NAME);
				faultMsg.setObjectPart(WSIFConstants.SOAP_FAULT_OBJECT, f);
			}
		} else {
			respOK = true;
			populateOutMsgReturnPart(resp, outMsg);
			populateOutMsgParts(outMsg);
		}

		return respOK;
	}

	/**
	 * Populate the outMessage with the response return value.
	 */
	private void populateOutMsgReturnPart(Object resp, WSIFMessage outMsg)
		throws WSIFException {
		if (outMsg != null) {
			
            // style=wrapped uses the unwrapped parts  
            List soapParts;
		    if (WSIFAXISConstants.AXIS_STYLE_WRAPPED.equals(operationStyle)) {
			    soapParts = outputUnwrappedSOAPParts;
		    } else {
			    soapParts = outputSOAPParts;
		    }

			Part returnPart = null;
			if (soapParts.size() > 0) {
				returnPart = (Part)soapParts.get(0);
				setSOAPMessagePart(outMsg,returnPart.getName(), resp);
			} else if (outputMIMEParts.size() > 0) {
				returnPart = (Part)outputMIMEParts.get(0);
			    setMIMEMessagePart(
				    outMsg,
				    returnPart.getName(),
				    resp,
				    resp == null ? null : resp.getClass());
			}
		}
	}

	/**
	 * Populates the outMessage with the expected parts.
	 * (this only does the out parameters not the return part)
	 */
	private void populateOutMsgParts(WSIFMessage outMsg) throws WSIFException {
		if (outMsg != null) {
			HashMap respParms = getResponseMessageParameters();

			if (respParms != null) {
                // style=wrapped uses the unwrapped parts  
                List soapParts;
		        if (WSIFAXISConstants.AXIS_STYLE_WRAPPED.equals(operationStyle)) {
			        soapParts = outputUnwrappedSOAPParts;
		        } else {
			        soapParts = outputSOAPParts;
		        }
		        
                if (!outputMIMEParts.isEmpty()) {
                    soapParts = new ArrayList(soapParts);
                    soapParts.removeAll(outputMIMEParts);
                }
		        
				for (int i=1; i < soapParts.size(); i++) {
					Part p = (Part) soapParts.get(i);
					String name = p.getName();
					Object value = respParms.get(name);
					setSOAPMessagePart(outMsg, name, value);
				}
				
				// TODO: Bug: this next code assumes that if a method returns 
				// attachments and non-attachments, the return part is
				// never an attachment.
				int startMIMEindex;
				if (soapParts.size() > 0) {
					startMIMEindex = 0;
				} else {
					startMIMEindex = 1;
				}
				for (int i=startMIMEindex; i < outputMIMEParts.size(); i++) {
					Part p = (Part) outputMIMEParts.get(i);
					String name = p.getName();
					Object value = respParms.get(name);
					setMIMEMessagePart(
						outMsg,
						name,
						value,
						value == null ? null : value.getClass());
				}
			}
		}

	}

	private static void setSOAPMessagePart(
		WSIFMessage msg,
		String name,
		Object value)
		throws WSIFException {
		Trc.entry(null, msg, name, value);

        //TODO type checking against the WSDL part type
		msg.setObjectPart(name, value);

		Trc.exit();
	}


	private static void setMIMEMessagePart(
		WSIFMessage msg,
		String name,
		Object value,
		Class type)
		throws WSIFException {
		Trc.entry(null, msg, name, value, type);

        MIMEHelper.setMIMEMessagePart(msg, name, value, type);

		Trc.exit();
	}	

    /**
     * Gets the type of a Part, if the Part doesn't have a type,
     * then gets the Element name as WSIF treats this as the same thing.
     * (for now? probably the wrong thing to be doing)
     */
    private QName getPartType(Part p) {
		QName type = p.getTypeName();
		if (type == null) {
			type = p.getElementName();
		}
		return type;
    }

	protected boolean invokeRequestResponseOperation(
		WSIFMessage inMsg,
		WSIFMessage outMsg,
		WSIFMessage faultMsg)
		throws WSIFException {
		Trc.entry(this, inMsg, outMsg, faultMsg);
		boolean workedOK = false;

        Call call = wsifPort.getCall();

        // Make sure we're making a fresh start.
        call.removeAllParameters();
        call.clearHeaders();

		call.setTargetEndpointAddress(wsifPort.getEndPoint());

		if (inputSOAPParts == null) {
			prepare(call);
		}
		call.setSOAPActionURI(getSoapActionURI());

        operationStyle = getOperationStyle(inMsg);

		Transport axistransport = getTransport();
		WSIFJMSDestination dest = null;
		if (axistransport != null) {
			call.setTransport(axistransport);
			if (axistransport instanceof WSIFJmsTransport) {
				WSIFJmsTransport jmst = (WSIFJmsTransport) axistransport;
				dest = jmst.getDestination();
				dest.setAsyncMode(isAsyncOperation());
				jmst.setSyncTimeout(null); // reset timeouts to defaults
				jmst.setAsyncTimeout(null);
			}
		}

		if (dest != null
			&& inJmsPropVals != null
			&& !inJmsPropVals.isEmpty()) {
			checkForTimeoutProperties(inJmsPropVals, dest);
			dest.setProperties(inJmsPropVals);
		}

		//TODO: jms:property parts

		setDestinationContext(dest);
		setCallContext(call);

		if (WSIFAXISConstants.AXIS_STYLE_MESSAGE.equals(operationStyle)) {
			workedOK = invokeAXISMessaging(call, inMsg, outMsg, faultMsg);
		} else if (WSIFAXISConstants.STYLE_RPC.equals(operationStyle)) {
			workedOK = invokeAXISRPCStyle(call, inMsg, outMsg, faultMsg, dest);
		} else {
			workedOK = invokeAXISDocStyle(call, inMsg, outMsg, faultMsg);
		}
		
		setResponseContext(call);

		Trc.exit(workedOK);
		return workedOK;
	}

    protected String getOperationStyle(WSIFMessage msg) {
    	String style = operationStyle;
    	
        try {
        	WSIFMessage context = getContext();
    	    style = (String) context.getObjectPart(WSIFConstants.CONTEXT_OPERATION_STYLE);
        } catch (WSIFException e) {
            Trc.ignoredException(e);

      		if (WSIFAXISConstants.STYLE_DOCUMENT.equals(operationStyle)
            && isInputMessageUnWrapped(msg)) {
		        style = WSIFAXISConstants.AXIS_STYLE_WRAPPED;
            }

            // TODO: what about wrapped messaging? Not supported yet
        	if (isMessaging(msg)) {
	        	style = WSIFAXISConstants.AXIS_STYLE_MESSAGE;
    	    }
        }
    		
    	return style;
    }

    /**
     * This attempts to determine if the WSIF input message parts are 
     * for a wrapped or unwrapped style operation. Tricky to tell for
     * sure so this just checks parts with the correct names exist in
     * the message.  
     * @return true if the input message has a multiple parts matching 
     *          the unwrapped SOAP parts, otherwise false
     */
	private boolean isInputMessageUnWrapped(WSIFMessage msg) {
		boolean unWrapped = true;

        Object style = null; 
        try {
            WSIFMessage context = getContext();
    	    style = context.getObjectPart(WSIFConstants.CONTEXT_OPERATION_STYLE);
        } catch (WSIFException e) {
            Trc.ignoredException(e);
        }
        if (WSIFConstants.CONTEXT_OPERATION_STYLE_WRAPPED.equals(style)) {
           	unWrapped = true;
        } else if (WSIFConstants.CONTEXT_OPERATION_STYLE_UNWRAPPED.equals(style)) {
           	unWrapped = false;
        } else if (inputUnwrappedSOAPParts != null 
        && inputUnwrappedSOAPParts.size() > 0) {
           	unWrapped = true;
			for (Iterator i=inputUnwrappedSOAPParts.iterator(); i.hasNext() && unWrapped; ) {
                Part p = (Part) i.next();
				try {
					msg.getObjectPart(p.getName());
				} catch (WSIFException e) {
					unWrapped = false;
				}
			}
        } else {
        	unWrapped = false;
		}

		return unWrapped;
	}

    /**
     * This attempts to determine if the WSIF input message parts are 
     * for a 'message' style document operation.
     * Note: messaging cannot use unwrapped parts
     * @return true if all the WSIF input message part types for the
     *          soap parts have a type of DOM Element, otherwise false
     */
	private boolean isMessaging(WSIFMessage msg) {
		boolean allDOMElements = true;
		boolean anyDOMElements = false;

		if (msg != null) {
			for (Iterator i = inputSOAPParts.iterator(); i.hasNext(); ) {
				Part p = (Part) i.next();
				try {
					Object o = msg.getObjectPart(p.getName());
				    if (o instanceof Element) {
					    anyDOMElements = true;
				    } else {
					    allDOMElements = false;
				    }
				} catch (WSIFException e) {
					Trc.ignoredException(e);
				}
			}
		}
		return anyDOMElements && allDOMElements;
	}


    /**
     * This does the AXIS Call invoke for document style operations
     * when the WSIF input message parts are NOT DOM elements 
     */
	private boolean invokeAXISRPCStyle(
		Call call,
		WSIFMessage inMsg,
		WSIFMessage outMsg,
		WSIFMessage faultMsg,
		WSIFJMSDestination dest)
		throws WSIFException {

		call.setOperationName(
			new QName(getInputNamespace(), portTypeOperation.getName()));

		setCallParameterNames(call);

        Object[] inputValues = getInputMessageValues(inMsg, dest);
        addUnreferencedAttachments(call);

		Object response = null;
		boolean respOK = true;
		try {
			String name = portTypeOperation.getName();
			Trc.event(
				this,
				"Invoking operation ",
				name,
				" input namespace ",
				getInputNamespace(),
				" parameters ",
				inputValues,
				" call object ",
				call);

		    response = call.invoke(getInputNamespace(), name, inputValues);

		} catch (AxisFault e) {
			Trc.exception(e);
			response = e;
			respOK = false;
		}

		Trc.event(this, "Returned from operation, response ", response);

		// setJMSOutPropsInContext( dest ); TODO doesn't work yet

        if (!isAsyncOperation()) {
            if (outputSOAPParts.size() > 0 || outputMIMEParts.size() > 0) {
                Map callParams = call.getOutputParams();
                if (callParams != null) {
                    HashMap outParams = new HashMap();
                    QName qn;
                    for (Iterator i = callParams.keySet().iterator();
                        i.hasNext();
                        ) {
                        qn = (QName) i.next();
                        outParams.put(qn.getLocalPart(), callParams.get(qn));
                    }
                    setResponseMessageParameters(outParams);
                }                
            }
			respOK = buildResponseMessages(response, outMsg, faultMsg);
            setResponseUnreferencedAttachments(call, outMsg);
        }

		return respOK;
	}

	/**
	 * This tells AXIS the name and type of the input, return, and output parameters. 
	 */
    private void setCallParameterNames(Call call) throws WSIFException {

        boolean wrappedStyleOp = 
            ProviderUtils.isUnwrapable(getPortTypeOperation());

		String inputNamespace;
        if (WSIFAXISConstants.USE_LITERAL.equals(getInputUse())
        && !wrappedStyleOp) {
            inputNamespace = "";
        } else {
            inputNamespace = getInputNamespace();
        }

		String outputNamespace = "";

		List soapParts;
        // style=wrapped uses the unwrapped parts  
		if (WSIFAXISConstants.AXIS_STYLE_WRAPPED.equals(operationStyle)) {
			soapParts = inputUnwrappedSOAPParts;
		} else {
			soapParts = inputSOAPParts;
		}

        /* If there are no attachments take the part ordering from the 
         * soap:body. If there are attachments then the soap:body will 
         * not include the attachments, so take the part ordering from 
         * the ordering of the parts in the original message.
         */
        List parts;
        if (inputMIMEParts.isEmpty()) {
        	parts = soapParts;
        } else {
         	parts = portTypeOperation.getInput().getMessage().getOrderedParts(null);
        }
        for (Iterator i = parts.iterator(); i.hasNext(); ) {
            Part p = (Part) i.next();
            String partName = p.getName();
            if (WSIFAXISConstants.STYLE_DOCUMENT.equals(operationStyle)) {
                QName qn = p.getElementName();
                if (qn != null) {
                    partName = qn.getLocalPart();
                }
            }
            if (!inJmsProps.containsKey(partName)) {
                if (inputMIMEParts.contains(p) || soapParts.contains(p)) {
                    QName name = new QName(inputNamespace, partName);
                    QName type = getPartType(p);
                    call.addParameter(name, type, ParameterMode.IN);
                }
            }
        }

        // style=wrapped uses the unwrapped parts  
		if (WSIFAXISConstants.AXIS_STYLE_WRAPPED.equals(operationStyle)) {
			soapParts = outputUnwrappedSOAPParts;
		} else {
			soapParts = outputSOAPParts;
		}

		// setup the return part
		Part returnPart = null;
		if (soapParts.size() > 0) {
		    returnPart = (Part)soapParts.get(0);
		} else if (outputMIMEParts.size() > 0) {
		    returnPart = (Part)outputMIMEParts.get(0);
		}
		if (returnPart == null) {
            call.setReturnType(org.apache.axis.encoding.XMLType.AXIS_VOID);
		} else {
		    QName type = getPartType(returnPart);
		    call.setReturnType(type);
		}

		// setup output SOAP parts
		// from 1 to skip the return part
		for (int i = 1; i < soapParts.size(); i++) {
			Part p = (Part) soapParts.get(i);
			QName name = new QName(outputNamespace, p.getName());
			QName type = getPartType(p);
			call.addParameter(name, type, ParameterMode.OUT);
		}
		
		// setup the output MIME parts
		// if no soap parts dont add 1st as its the return part
		int startMIMEIndex = (soapParts.size() > 0) ? 0 : 1;
		for (int i = startMIMEIndex; i < outputMIMEParts.size(); i++) {
			Part p = (Part) outputMIMEParts.get(i);
			QName name = new QName(outputNamespace, p.getName());
			QName type = getPartType(p);
			call.addParameter(name, type, ParameterMode.OUT);
		}
    }

    /**
     * This does the AXIS Call invoke for document style operations
     * when the WSIF input message parts are NOT DOM elements 
     */
	private boolean invokeAXISDocStyle(
		Call call,
		WSIFMessage inMsg,
		WSIFMessage outMsg,
		WSIFMessage faultMsg)
		throws WSIFException {

		boolean respOK = false;

		// setup the call object
		call.setOperationName(
			new QName(wsifPort.getPortTypeNamespace(), portTypeOperation.getName()));

		call.setProperty(Call.SEND_TYPE_ATTR, Boolean.FALSE);
		call.setProperty(AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);

		call.setOperationStyle(operationStyle);
//TODO???        call.setOperationUse("literal");
        setCallParameterNames(call);

		Object[] inputValues = getInputMessageValues(inMsg, null);
        addUnreferencedAttachments(call);

		// invoke the AXIS call
		Trc.event(this, "Invoking AXIS call", call, inputValues);
		Object response;
		try {
			response = call.invoke(inputValues);
			respOK = true;
		} catch (RemoteException ex) {
			Trc.exception(ex);
			throw new WSIFException(
				"exception on AXIS invoke: " + ex.getLocalizedMessage(),
				ex);
		}
		Trc.event(this, "Returned from AXIS invoke, response: ", response);

		// process the AXIS response
		if (!isAsyncOperation()) {

            // style=wrapped uses the unwrapped parts  
            List soapParts;
		    if (WSIFAXISConstants.AXIS_STYLE_WRAPPED.equals(operationStyle)) {
			    soapParts = outputUnwrappedSOAPParts;
		    } else {
			    soapParts = outputSOAPParts;
		    }
			 
		    if (soapParts.size() > 0 || outputMIMEParts.size() > 0) {
			    Map callParams = call.getOutputParams();
			    if (callParams != null) {
				    HashMap outParams = new HashMap();
				    QName qn;
				    for (Iterator i = callParams.keySet().iterator(); i.hasNext(); ) {
					    qn = (QName) i.next();
					    outParams.put(qn.getLocalPart(), callParams.get(qn));
				    }
				    setResponseMessageParameters(outParams);
			    }
			    respOK =
				    buildResponseMessages(response, outMsg, faultMsg);
		    }
            setResponseUnreferencedAttachments(call, outMsg);
		}

		return respOK;
	}

    /**
     * This does the AXIS Call invoke for 'messaging' - when all the 
     * WSIF input message parts are DOM elements 
     */
	private boolean invokeAXISMessaging(
		Call call,
		WSIFMessage inMsg,
		WSIFMessage outMsg,
		WSIFMessage faultMsg)
		throws WSIFException {

		boolean workedOK = false;

        List attachments = addReferencedAttachments(inMsg, call);
        addUnreferencedAttachments(call);

		Object[] inputValues = getInputMessageValues(inMsg, null);
		ArrayList soapBodies = new ArrayList();
		for (int i = 0; i < inputValues.length; i++) {
			if (inputValues[i] instanceof Element) {
				Element el = (Element) inputValues[i];
				
				if ((attachments.size() > 0) && (i == 0)) {
					fixAttachmentPartsCID(el, attachments);
				}
				
				soapBodies.add(new SOAPBodyElement(el));
			} else {
				throw new WSIFException(
					"unexpected input type: " + inputValues[i]);
			}
		}
		
        Object[] axisInputs = soapBodies.toArray(); 

//TODO:        call.setEncodingStyle("");

		Trc.event(this, "Invoking AXIS call", call, axisInputs);
		Object axisResponse; // the response should be a Vector of RPCElement objects
		try {
			axisResponse = call.invoke(axisInputs);
		} catch (RemoteException ex) {
			throw new WSIFException(
				"exception on AXIS invoke: " + ex.getLocalizedMessage(),
				ex);
		}
		Trc.event(this, "Returned from AXIS invoke, response: ", axisResponse);

		setOutputMessageValues(axisResponse, outMsg);
        setResponseUnreferencedAttachments(call, outMsg);

		workedOK = true;
		return workedOK;
	}

    /**
     * Fix the CID in the href of an attachment.
     * When sending an attachent with messaging two parts will
     * be in the WSIF input message, a DOM Element for the SOAP body contents,
     * and a DataHandler for the attachment. The contents Element must
     * include a href part for the attachment. This method will find
     * that href part and correct the CID value for the new AttachmentPart.
     * SOAPBody example with a href part named 'file':
     *	<ns1:bounceImage4 xmlns:ns1="http://mime/">
   	 *	   <ns1:shouldBounce xsi:type="xsd:boolean">true</ns1:shouldBounce>
   	 *	   <ns1:file href="cid:413B07CE410E48EB9D89DC0A4DDD715D"/>
  	 *  </ns1:bounceImage4>
     */
    private void fixAttachmentPartsCID(Element body, List attachments) throws WSIFException {

        // find all the attachment Elements in the body
        ArrayList al = new ArrayList();
        getAttachmentElements(al, body);
        
        /* this isn't so nice: the AttachmentPart has no name
         * so we have to assume the the List is in the same
         * order as the attachment Elements in the body. 
         */ 
        if (al.size() != attachments.size()) {
        	throw new WSIFException("unexpected number of attachments,"
        	    + attachments.size() + " AttachmentParts, "
        	    + al.size() + " attachment href elements");   
        }

        // fiddle the cid: for each attachment
        for (int i = 0; i < attachments.size(); i++ ) {
        	AttachmentPart attachment = (AttachmentPart) attachments.get(i);
            Element attachmentElement = (Element) al.get(i);
            attachmentElement.setAttribute("href", "cid:" + attachment.getContentId());                     
        }
    }

    /**
     * Gets all attachment Elements within an Element
     * This recurses through the DOM tree structure adding any
     * attachment Element's to the ArrayList. 
     * An attachment Element is one which has an href attribute
     * which has a value starting with the String "cid:"
     */
    private static void getAttachmentElements(ArrayList al, Element el) {
        NodeList childNodes = el.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++) {
            Node n = childNodes.item(j);
            if (n instanceof Element) {
                Element childElement = (Element) n;
                String s = childElement.getAttribute("href");
                if (s != null && s.toLowerCase().startsWith("cid:")) {
                    al.add(childElement);
                } else {
                    getAttachmentElements(al, childElement);
                }
            }
        }
    }

	/**
	 * Prepares this operation.
	 * The intention of this is to setup everything that can be reused
	 * by the WSIFOperation. Clients must not reuse a WSIFOperation 
	 * but a WSIFPort may cache WSIFOperation instances and use the
	 * WSIFOperation copy method to return copies to clients
	 */
	private void prepare(Call call) throws WSIFException {
		Trc.entry(this, call);

        /* Note: if you change anything here make sure you consider
         * the impact to the constructor, copy and prepare methods
         * and to if any instance variables are transient 
         */

        /* Create the ArrayList identify each part and populate
         * by calling the appropriate parseXxxx method.
         */
        this.inputSOAPParts = new ArrayList();
        this.inputMIMEParts = new ArrayList();
        this.outputSOAPParts = new ArrayList();
        this.outputMIMEParts = new ArrayList();

		parseSoapOperation();
		parseBindingInput();
		parseBindingOutput();
		unwrapSOAPParts();
		
        // register any jms:address propertyValues
		addInputJmsPropertyValues(wsifPort.getJmsAddressPropVals());
		
		if (!WSIFAXISConstants.STYLE_RPC.equals(operationStyle)) {
		    call.setEncodingStyle(null);
		    inputEncodingStyle = "";
		    outputEncodingStyle = "";
		}

		if (inputNamespace == null || inputNamespace.length() < 1) {
		    this.inputNamespace = getTargetNamespaceURI();
		}

    	TypeMapping tm = call.getTypeMapping();
    	WSIFMessage context = getContext();
    	
    	// register any default type mappings    	
    	registerDefaultTypeMappings(tm, context);
    	
    	// register any mappings from WSIFService.mapType calls
    	registerDynamicTypes(tm, typeMap, context);
			
		registerMIMETypes(
			inputMIMEParts,
			call);

		registerMIMETypes(
			outputMIMEParts,
			call);

		if (Trc.ON) {
			Trc.exit(deep());
		}
	}
	
	/**
     * Register any default mappings from the context with the AXIS Call
	 */
	static void registerDefaultTypeMappings(TypeMapping callTypeMappings, WSIFMessage context) throws WSIFException {
		Object value = null;
		try {
			value = context.getObjectPart(WSIFAXISConstants.CONTEXT_DEFAULT_SOAP_TYPE_SERIALIZERS);
		} catch (WSIFException e) {
			Trc.ignoredException(e);
		}
		if (value == null) {
			return;
		}
        if (!(value instanceof List)) {
        	throw new WSIFException(
        	    "context part '"
        	    + WSIFAXISConstants.CONTEXT_DEFAULT_SOAP_TYPE_SERIALIZERS
        	    + "' value is not an instance of java.util.List: "
        	    + value); 
        }
        List defaultMappings = (List) value;
        for (Iterator i = defaultMappings.iterator(); i.hasNext(); ) {
        	Object o = i.next();
        	if (!(o instanceof TypeSerializerInfo)) {
          	    throw new WSIFException(
        	        "context part '"
        	        + WSIFAXISConstants.CONTEXT_DEFAULT_SOAP_TYPE_SERIALIZERS
        	        + "' value List contains an entry that is not an instance "
        	        + "of org.apache.wsif.util.TypeSerializer: "
        	        + value); 
        	}
        	TypeSerializerInfo ts = (TypeSerializerInfo) o;
        	
           	Class javaType = ts.getJavaType();
           	QName elementType = ts.getElementType();     
           	Object tmp = ts.getSerializer();
            SerializerFactory sf = null;
           	if (tmp instanceof SerializerFactory) {
                sf = (SerializerFactory) tmp;
           	}
           	tmp = ts.getDeserializer();
            DeserializerFactory df = null;
           	if (tmp instanceof DeserializerFactory) {
                df = (DeserializerFactory) tmp;
           	}
           	      
        	if (javaType != null
        	&& elementType != null
        	&& (sf != null || df != null) ) {
		        callTypeMappings.register(javaType, elementType, sf, df);
        	} else {
        		Trc.event(null, "ignoring default TypeSerializer invalid for AXIS:" + ts);
        	}
        } 
	}

	/**
     * Register any mappings from WSIFService.mapType calls with the AXIS Call
	 */
	static void registerDynamicTypes(
	    TypeMapping tm, 
	    WSIFDynamicTypeMap typeMap,
	    WSIFMessage context) 
	    throws WSIFException {
	    	
		Class objClass;
		String namespaceURI, localPart;
		WSIFDynamicTypeMapping wsifdynamictypemapping;
		for (Iterator iterator = typeMap.iterator(); iterator.hasNext();) {
			wsifdynamictypemapping = (WSIFDynamicTypeMapping) iterator.next();
			objClass = wsifdynamictypemapping.getJavaType();
			QName xmlType = wsifdynamictypemapping.getXmlType();

			SerializerFactory sf = null;
			DeserializerFactory df = null;
			
			// the context may override the default (de)serializer for a type
			TypeSerializerInfo contextTypeSerializer = 
			    findContextTypeSerialzer(context, objClass, xmlType);
			if (contextTypeSerializer != null) {
				objClass = contextTypeSerializer.getJavaType();
				xmlType = contextTypeSerializer.getElementType();
   			    sf = (SerializerFactory) contextTypeSerializer.getSerializer();
   			    df = (DeserializerFactory) contextTypeSerializer.getDeserializer();
			} 
			
		    if (sf == null && tm.getSerializer(objClass, xmlType) == null) {
   		   	    if (objClass.isArray()) {
    			    sf = new ArraySerializerFactory();
	    	    } else if (SimpleType.class.isAssignableFrom(objClass)) {
		    	    sf = new SimpleSerializerFactory(objClass, xmlType);
		        } else {
			        sf = new BeanSerializerFactory(objClass, xmlType);
	   	        }
		    }

   		    if (df == null && tm.getDeserializer(objClass, xmlType) == null) {
		        if (objClass.isArray()) {
			        df = new ArrayDeserializerFactory();
	    	    } else if (SimpleType.class.isAssignableFrom(objClass)) {
		    	    df = new SimpleDeserializerFactory(objClass, xmlType);
    	        } else {
	    	        df = new BeanDeserializerFactory(objClass, xmlType);
		        }
		    }

			namespaceURI =
				wsifdynamictypemapping.getXmlType().getNamespaceURI();

			// Filter out XSD and SOAP-ENC types from those we explicitly map.      
			// Axis already knows how to deal with these; using the BeanSerializer 
			// would be wrong anyway as they represent simple types and not beans. 
			if (!isDefaultSOAPNamespace(namespaceURI) ) {
					
				localPart = wsifdynamictypemapping.getXmlType().getLocalPart();
				QName qn = new QName(namespaceURI, localPart);

				if (sf != null || df != null) {
					tm.register(objClass, qn, sf, df);
				}
			}
		}
	}

    private static TypeSerializerInfo findContextTypeSerialzer(
        WSIFMessage context,
        Class clazz,
        QName xmlType)
        throws WSIFException {

		Object value = null;
		try {
			value = context.getObjectPart(WSIFAXISConstants.CONTEXT_SOAP_TYPE_SERIALIZERS);
		} catch (WSIFException e) {
			Trc.ignoredException(e);
		}
		if (value == null) {
			return null;
		}
        if (!(value instanceof List)) {
        	throw new WSIFException(
        	    "context part '"
        	    + WSIFAXISConstants.CONTEXT_SOAP_TYPE_SERIALIZERS
        	    + "' value is not an instance of java.util.List: "
        	    + value); 
        }
        
        List typeSerializers = (List) value;
        for (Iterator i = typeSerializers.iterator(); i.hasNext(); ) {
        	Object o = i.next();
        	if (!(o instanceof TypeSerializerInfo)) {
          	    throw new WSIFException(
        	        "context part '"
        	        + WSIFAXISConstants.CONTEXT_SOAP_TYPE_SERIALIZERS
        	        + "' value List contains an entry that is not an instance "
        	        + "of org.apache.wsif.util.TypeSerializer: "
        	        + value); 
        	}
        	TypeSerializerInfo tm = (TypeSerializerInfo) o;
        	    
           	Class javaType = tm.getJavaType();
           	QName elementType = tm.getElementType();     
           	Object serializer = tm.getSerializer();     
           	Object deserializer = tm.getDeserializer();
           	     
        	if ( (javaType != null) && (javaType.isAssignableFrom(clazz))
        	&& ( (elementType != null) && (elementType.equals(xmlType)) ) ){
        	    if ((serializer == null || serializer instanceof SerializerFactory)
        	    && (deserializer == null || deserializer instanceof DeserializerFactory)
        	    && (serializer != null || deserializer != null)) {
        		    return tm;
        	    }
            }
        }

    	return null; // couldn't find a TypeSerializer
    }
    
	private static boolean isDefaultSOAPNamespace(String ns) {
		boolean soapNamespace = false;
		 if (WSIFConstants.NS_URI_1999_SCHEMA_XSD.equals(ns)
		 || WSIFConstants.NS_URI_2000_SCHEMA_XSD.equals(ns)
		 || WSIFConstants.NS_URI_2001_SCHEMA_XSD.equals(ns)
		 || WSIFConstants.NS_URI_SOAP_ENC.equals(ns) ){
		 	soapNamespace = true;
		 } 
		 return soapNamespace;
	}

	/**
	 * Gets an array of all the input WSIFMessage values 
	 */
	private Object[] getInputMessageValues(WSIFMessage inMsg, WSIFJMSDestination dest) throws WSIFException {
		ArrayList axisInputs = new ArrayList();
		List soapParts;

        // style=wrapped uses the unwrapped parts  
		if (WSIFAXISConstants.AXIS_STYLE_WRAPPED.equals(operationStyle)) {
			soapParts = inputUnwrappedSOAPParts;
		} else {
			soapParts = inputSOAPParts;
		}
		
        /* If there are no attachments take the part ordering from the 
         * soap:body. If there are attachments then the soap:body will 
         * not include the attachments, so take the part ordering from 
         * the ordering of the parts in the original message.
         */
        if (inputMIMEParts.isEmpty())
        {
            for (int i = 0; i < soapParts.size(); i++)
            {
                Part p = (Part) soapParts.get(i);
                String partName = p.getName();
                Object value;
                try
                {
                    value = inMsg.getObjectPart(partName);
                }
                catch (WSIFException e)
                {
                    Trc.ignoredException(e);
                    value = null; // missing part values default to null
                }
                if (inJmsProps.containsKey(partName) && dest != null)
                {
                    String name = (String) (inJmsProps.get(partName));
                    if (!timeoutProperty(dest, name, value))
                    {
                        dest.setProperty(name, value);
                    }
                }
                else
                {
                    axisInputs.add(value);
                }

            }
        }
        else
        {
            // This order includes both the attachments and 
            // the non-attachments.
            List order =
                portTypeOperation.getInput().getMessage().getOrderedParts(null);
            Iterator it = order.iterator();
            while (it.hasNext())
            {
                Part p = (Part) it.next();
                String partName = p.getName();

                // Only add the part if it hasn't been excluded by the 
                // soap:body parts="?"
                if (soapParts.contains(p)
                    || (inputMIMEParts.contains(p)
                        && !WSIFAXISConstants.AXIS_STYLE_MESSAGE.equals(
                            operationStyle)))
                {
                    Object value;
                    try
                    {
                        value = inMsg.getObjectPart(partName);
                    }
                    catch (WSIFException e)
                    {
                        Trc.ignoredException(e);
                        value = null; // missing part values default to null
                    }
                    if (inJmsProps.containsKey(partName) && dest != null)
                    {
                        String name = (String) (inJmsProps.get(partName));
                        if (!timeoutProperty(dest, name, value))
                        {
                            dest.setProperty(name, value);
                        }
                    } else if (value instanceof WSIFAttachmentPart) {
                        WSIFAttachmentPart ap = (WSIFAttachmentPart) value;
                        axisInputs.add(ap.getDataHandler());
                    } else {
                        axisInputs.add(value);
                    }
                }
            }
        }
		return axisInputs.toArray();
	}

	/**
	 * adds all the referenced attachments to the AXIS call
	 * returns a List of all the AttachmentPart so that href parts
	 * can be made for each attachment later if required.
	 */
    private List addReferencedAttachments(WSIFMessage inMsg, Call call)
        throws WSIFException {
        Trc.entry(this, inMsg, call);

        ArrayList attachments = new ArrayList();
        for (int i = 0; i < inputMIMEParts.size(); i++) {
            Part p = (Part) inputMIMEParts.get(i);
            try {
                String partName = p.getName();
                Object value = inMsg.getObjectPart(partName);
                AttachmentPart ap = MIMEHelper.getAttachementPart(value);
                call.addAttachmentPart(ap);
                attachments.add(ap);
            } catch (WSIFException e) {
                throw new WSIFException(
                    "attachment part '"
                        + p.getName()
                        + "' not in input WSIFMessage");
            }
        }

        Trc.exit(attachments);
        return attachments;
    }

    /**
     * Adds all the unreferenced attachments to the AXIS call
     */
    private void addUnreferencedAttachments(Call call)
        throws WSIFException {
        Trc.entry(this, call);

        if (WSIFProperties.areUnreferencedAttachmentsSupported()) {
            List l = null;
            try {
                WSIFMessage context = getContext();
                if (context != null) {
                    l =
                        (List) context.getObjectPart(
                            WSIFConstants.CONTEXT_REQUEST_UNREFERENCED_ATTACHMENT_PARTS);
                }
            } catch (Exception e) {
                Trc.ignoredException(e);
            }

            if (l != null && !l.isEmpty()) {
                Iterator it = l.listIterator();
                while (it.hasNext()) {
                    Object next = it.next();
                    AttachmentPart ap = MIMEHelper.getAttachementPart(next);
                    call.addAttachmentPart(ap);
                }
            }
        }

        Trc.exit();
    }

    /**
     * Sets all the output unreferenced attachments on the message context.
     * The list of unreferenced attachments is calculated by discovering the 
     * Content-Id's of all the DataHandlers/WSIFAttachmentParts in the output 
     * message. Then the unreferenced attachments are all the attachments, 
     * excluding those that have a Content-Id that matches one in the list of
     * referenced Content-Id's.
     */
    private void setResponseUnreferencedAttachments(
        Call call,
        WSIFMessage outMsg)
        throws WSIFException {
        Trc.entry(this, call, outMsg);

        if (WSIFProperties.areUnreferencedAttachmentsSupported()) {
            Set refCids = null;
            List unrefAps = new ArrayList();
            MessageContext mc = call.getMessageContext();
            Message m = mc.getResponseMessage();
            Iterator it = m.getAttachments();
            while (it.hasNext()) {
                Object o = it.next();
                AttachmentPart ap = MIMEHelper.getAttachementPart(o);
                String cid = ap.getContentId();
                if (refCids == null) {
                    refCids = new HashSet();
                    Iterator mit = outMsg.getPartNames();
                    while (mit.hasNext()) {
                        String name = (String) mit.next();
                        Object part = outMsg.getObjectPart(name);
                        AttachmentPart refAp = null;
                        try {
                            refAp = MIMEHelper.getAttachementPart(part);
                        } catch (WSIFException we) {
                            Trc.ignoredException(we);
                        }
                        if (refAp != null) {
                            refCids.add(refAp.getContentId());
                        }
                    }
                }
                if (!refCids.contains(cid)) {
                    unrefAps.add(WSIFAXISUtils.axisToWsifAttachmentPart(ap));
                }
            }
            if (!unrefAps.isEmpty()) {
                WSIFMessage context = getContext();
                context.setObjectPart(
                    WSIFConstants.CONTEXT_RESPONSE_UNREFERENCED_ATTACHMENT_PARTS,
                    unrefAps);
                setContext(context);
            }
        }

        Trc.exit();
    }

    /**
     * This extracts the values from the AXIS response when using messaging
     * The response could have DOM elements for the SOAP parts, or 
     * AttachmentParts for the attachments.
     * TODO: only tested with a single response part - either SOAP or MIME
     *       need to test with multiple outputs as probably doesn't work yet.
     */
	private void setOutputMessageValues(
		Object axisResponse,
		WSIFMessage outMsg)
		throws WSIFException {
		if (!(axisResponse instanceof Vector)) {
			throw new WSIFException(
				"expect response type of java.util.Vector of SOAPBodyElement, found: "
					+ axisResponse);
		}

		Vector v = (Vector) axisResponse;
		for (int i = 0; i < v.size(); i++) {
			if (v.elementAt(i) instanceof RPCElement) {
				RPCElement rpcEl = (RPCElement) v.elementAt(i);

				QName qn = new QName(rpcEl.getNamespaceURI(), rpcEl.getName());
				Part p = findPart(outputSOAPParts, qn);
				if (p != null) {
					setSOAPPart(outMsg, rpcEl, p);
				} else {
					setAttachmentPart(outMsg, rpcEl);
				}
				
			} else {
				throw new WSIFException(
					"expecting response type org.w3c.dom.Element, found: "
						+ v.elementAt(i));
			}
		}
	}

    /**
     * Extract a SOAP part from response and put in the output WSIFMessage 
     */
    private void setSOAPPart(WSIFMessage outMsg, RPCElement rpcEl, Part p) throws WSIFException {
        Object responseValue = null;
        try {
            responseValue = rpcEl.getAsDOM();
        } catch (Exception e) {
	   	    throw new WSIFException(
		        "exception getting soap body as DOM: " + 
			    e.getLocalizedMessage(),
			    e);
        }
	    String partName = p.getName();
	    outMsg.setObjectPart(partName, responseValue);
    }

    /**
     * Extract an attachment DataHandler from response and put in the output WSIFMessage 
     */
    private void setAttachmentPart(WSIFMessage outMsg, RPCElement rpcEl) throws WSIFException {

        Vector params;
        try {
            params = rpcEl.getParams();
        } catch (SAXException e) {
  		    throw new WSIFException(
  			    "SAXException getting response MIME part: " + 
				e.getLocalizedMessage(),
				e);
        }

        if (params == null || params.size() < 1) {
  		    throw new WSIFException(
  			    "no attachments found in response element: " + rpcEl); 
        }
        
        //TODO: will there ever be more than 1?
        RPCParam rpcParam = (RPCParam) params.get(0);        

        QName qn = rpcParam.getQName();
        Part p = findPart(outputMIMEParts, qn);
	    if (p != null) {
        	Object responseValue = rpcParam.getValue();
	    	if (responseValue instanceof AttachmentPart) {
	    		try {
                    Object attachment = ((AttachmentPart)responseValue).getDataHandler();
            		String partName = p.getName();
		            outMsg.setObjectPart(partName, attachment);
                } catch (SOAPException e) {
                    throw new WSIFException(
  	                    "SOAPException getting DataHandler from AttachmentPart: " + 
			 	        e.getLocalizedMessage(),
			            e);
                }
			} else {
     		    throw new WSIFException(
    			    "expecting response AttachmentPart but found: " + responseValue);
	    	}
	    } else {
	    	throw new WSIFException("cannot find a WSDL output MIME part for response element: " + rpcEl);
	    }

    }

    /**
     * Searches the list of parts for one that matches the name.
     * The list of parts will be the outputSOAPParts or outputMIMEParts
     */
    private Part findPart(List partsList, QName partName) {
    	Part part = null;
    	for (Iterator i = partsList.iterator(); i.hasNext() && part == null; ) {
    		Part p = (Part) i.next();
    		if (partName.equals(p.getElementName())) {
    			part = p;
    		} else if (partName.getLocalPart().equals(p.getName())) {
    			part = p;
    		}
    	}
    	return part;
    }

	/**
	 * Automatically register MIME types as DataHandler.
	 */
	private void registerMIMETypes(
		List mimeParts,
		Call call) {
		if (mimeParts != null && !mimeParts.isEmpty()) {
			for (Iterator i = mimeParts.iterator(); i.hasNext(); ) {
				Part p = (Part) i.next();
 			    QName type = getPartType(p);
			    MIMEHelper.registerAttachmentType(call, type);
			}
		}
	}

	/**
	 * Sets the response handler that will be used to
	 * process the response to an asynchronous request.
	 * @param responseHandler   the responseHandler to use 
	 */
	private void setResponseHandler(WSIFResponseHandler responseHandler) {
		this.responseHandler = responseHandler;
	}

	/**
	 * Gets the response handler that will be used to
	 * process the response to a asynchronous request.
	 * @return the current response handler.
	 * package visable as its used by the transport
	 */
	WSIFResponseHandler getResponseHandler() {
		return responseHandler;
	}

	/**
	 * @deprecated should anyone be calling this?
	 */
	public void setDefinition(Definition definition1) {
		Trc.entry(this, definition1);
		throw new RuntimeException("method nolonger supported");
	}

	public void setDynamicWSIFPort(WSIFPort_ApacheAxis wsifport_apacheaxis) {
		Trc.entry(this, wsifport_apacheaxis);
		wsifPort = wsifport_apacheaxis;
		Trc.exit();
	}

	public void setInputEncodingStyle(String s) {
		Trc.entry(this, s);
		inputEncodingStyle = s;
		Trc.exit();
	}

	public void setInputNamespace(String s) {
		Trc.entry(this, s);
		inputNamespace = s;
		Trc.exit();
	}

	public void setOperation(Operation operation1) {
		Trc.entry(this, operation1);
		portTypeOperation = operation1;
		Trc.exit();
	}

	public void setOutputEncodingStyle(String s) {
		Trc.entry(this, s);
		outputEncodingStyle = s;
		Trc.exit();
	}

	public void setSoapActionURI(String s) {
		Trc.entry(this, s);
		soapActionURI = s;
		Trc.exit();
	}

	private void setResponseMessageParameters(HashMap hm) {
		this.responseMessageParameters = hm;
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
	 * Gets the target namespace URI of this WSIFOperation 
	 * 
	 * @return the target namespace URI
	 */
	public String getTargetNamespaceURI() {
		Trc.entry(this);
		Definition d = getDefinition();
		String s = (d == null) ? "" : d.getTargetNamespace();
		Trc.exit(s);
		return s;
	}

	/**
	 * This sets up the output JMS property values in the context
	 */
	private void setJMSOutPropsInContext(WSIFJMSDestination dest)
		throws WSIFException {
		if (dest == null) {
			return;
		}
		HashMap props = dest.getProperties();
		if (props != null) {
			getContext().setParts(props);
		}
	}

	/**
	 * This sets up any context JMS property values in the Destination
	 */
	private void setDestinationContext(WSIFJMSDestination dest) throws WSIFException {
		if (dest == null) {
			return;
		}
		WSIFMessage context = getContext();
		HashMap jmsProps = new HashMap();
		for (Iterator i = context.getPartNames(); i.hasNext();) {
			String partName = (String) i.next();
			try {
				Object value = context.getObjectPart(partName);
				if (!timeoutProperty(dest, partName, value)) {
					if (partName
						.startsWith(WSIFConstants.CONTEXT_JMS_PREFIX)) {
						String propertyName =
							partName.substring(
								WSIFConstants.CONTEXT_JMS_PREFIX.length());
						jmsProps.put(propertyName, value);
					}
				}
			} catch (WSIFException ex) {
				Trc.ignoredException(ex);
			}
		}
		if (jmsProps.size() > 0) {
			dest.setProperties(jmsProps);
		}
	}

	/**
	 * This sets up the context headers in the axis 
	 * Call object prior to invoke method being issued.
	 */
	private void setCallContext(Call call) throws WSIFException {
		Object o;
		String name;
		WSIFMessage context = getContext();

		name = WSIFConstants.CONTEXT_HTTP_USER;
		try {
			o = context.getObjectPart(name);
			if (o instanceof String) {
				addHTTPHeader(call, name, (String) o);
			}
		} catch (WSIFException ex) {
			Trc.ignoredException(ex);
		}

		name = WSIFConstants.CONTEXT_HTTP_PSWD;
		try {
			o = context.getObjectPart(name);
			if (o instanceof String) {
				addHTTPHeader(call, name, (String) o);
			}
		} catch (WSIFException ex) {
			Trc.ignoredException(ex);
		}

		try {
			name = WSIFConstants.CONTEXT_REQUEST_SOAP_HEADERS;
			o = context.getObjectPart(name);
			if (o instanceof List) {
				addSOAPHeader(call, name, (List) o);
			}
		} catch (WSIFException ex) {
			Trc.ignoredException(ex);
		}

        o = null;
		try {
			name = WSIFConstants.CONTEXT_REQUEST_HTTP_HEADERS;
			o = context.getObjectPart(name);
		} catch (WSIFException ex) {
			Trc.ignoredException(ex);
		}
    	if (o != null) {
    	    if (o != null && o instanceof Hashtable) {
        	    call.setProperty(
    	            HTTPConstants.REQUEST_HEADERS,
    		        (Hashtable) o);
	    	} else {
		    	throw new WSIFException(
			        "value type must be java.util.Hashtable for context part '"
                    + WSIFConstants.CONTEXT_REQUEST_HTTP_HEADERS
                    + "'"); 			    
    	    }
    	}
    	
    	setProxyUserFromContext(context);
    	
	}

    /**
     * Sets the authenticating proxy user id and password if in context
     */
    private void setProxyUserFromContext(WSIFMessage ctx) throws WSIFException {
    	String uid = null;
    	String pswd = null;
    	try {
            Object o = ctx.getObjectPart(WSIFConstants.CONTEXT_HTTP_PROXY_USER);
            if (o != null) {
                if ( o instanceof String) {
                	uid = (String) o;
                } else {
                	throw new WSIFException(
                	    "invalid value type for context part '"
                	    + WSIFConstants.CONTEXT_HTTP_PROXY_USER
                	    + "', found value: " 
                	    + o);
                }
            }
            o = ctx.getObjectPart(WSIFConstants.CONTEXT_HTTP_PROXY_PSWD);
            if (o != null) {
                if ( o instanceof String) {
                	pswd = (String) o;
                } else {
                	throw new WSIFException(
                	    "invalid value type for context part '"
                	    + WSIFConstants.CONTEXT_HTTP_PROXY_PSWD
                	    + "', found value: " 
                	    + o);
                }
            }
        } catch (WSIFException e) {
        	Trc.ignoredException(e);
        }
        if (uid != null) {
        	AxisProperties.setProperty("http.proxyUser", uid);
        	AxisProperties.setProperty("http.proxyPassword", pswd);
        }
    }

    /**
     * This sets up any context from the response message
     */
    private void setResponseContext(Call call) throws WSIFException {
        org.apache.axis.Message m = call.getResponseMessage();
        if (m != null) {
            javax.xml.soap.SOAPEnvelope env;
            try {
                env = m.getSOAPEnvelope();
            } catch (AxisFault e) {
        	    throw new WSIFException(
        	        "AxisFault getting response SOAP envelope", e);
            }
            if (env != null) {
                javax.xml.soap.SOAPHeader soapHeader;
                try {
                    soapHeader = env.getHeader();
                } catch (SOAPException e) {
        	        throw new WSIFException(
        	            "SOAPException getting response headers from SOAP envelope", e);
                }
            	addContextResponseSOAPHeaders(soapHeader);
            }
        }
    }

	/**
	 * Sets any SOAP headers from the context message.
	 */
	private void addSOAPHeader(Call call, String name, List soapHeaders) {
		for (Iterator i = soapHeaders.iterator(); i.hasNext();) {
			Object o = i.next();
			if (o instanceof Element) {
				call.addHeader(new SOAPHeaderElement((Element) o));
			}
		}
	}

    /**
     * This adds any SOAP headers in the response to the context 
     */ 
    private void addContextResponseSOAPHeaders(
        javax.xml.soap.SOAPHeader soapHeader) throws WSIFException {
        	
        if( soapHeader != null ) {
            ArrayList headers = new ArrayList();
            for(Iterator i = soapHeader.getChildElements(); i.hasNext(); ) {
          	    Object o = i.next();
          	    if (o instanceof SOAPHeaderElement) {
          		    SOAPHeaderElement she = (SOAPHeaderElement) o;
          		    try {
                        headers.add(she.getAsDOM());
                    } catch (Exception e) {
                        throw new WSIFException(
                            "exception getting response SOAP header",
                            e);
          		    }
          	    } else {
          	    	Trc.event("unexpected response SOAP header type: ", o);
          	    }
            }
            if (headers.size() > 0) {
                WSIFMessage context = getContext();
                context.setObjectPart(
                    WSIFConstants.CONTEXT_RESPONSE_SOAP_HEADERS,
                    headers);
                setContext(context);
            }
        }
    }

	/**
	 * Sets the HTTP header value in the message context.
	 * How these are used depends on the underlying transport, 
	 * eg. org.apache.axis.transport.http.HTTPSender
	 * will use the 'user.id' and 'user.password' headers
	 * for HTTP basic authentication..  
	 */
	private void addHTTPHeader(Call call, String name, String value) {
		if (name.equals(WSIFConstants.CONTEXT_HTTP_USER)) {
			call.setProperty(Call.USERNAME_PROPERTY, value);
		} else if (name.equals(WSIFConstants.CONTEXT_HTTP_PSWD)) {
			call.setProperty(Call.PASSWORD_PROPERTY, value);
		}
	}

    /**
     * This checks if any of the JMS propertyValues are for the
     * sync or async timeout values. If so it set the appropriate
     * value on the JMS transport and removes the property from 
     * the JMS propertyValues list.
     */
	private void checkForTimeoutProperties(
		HashMap inJmsPropVals,
		WSIFJMSDestination dest)
		throws WSIFException {
		for (Iterator i = inJmsPropVals.keySet().iterator(); i.hasNext();) {
			String name = (String) i.next();
			Object value = inJmsPropVals.get(name);
			if (timeoutProperty(dest, name, value)) {
				i.remove();
			}
		}
	}

	private boolean timeoutProperty(
		WSIFJMSDestination dest,
		String propertyName,
		Object value)
		throws WSIFException {
		boolean isTimeoutProperty = false;
		try {
			if (WSIFConstants.WSIF_PROP_SYNC_TIMEOUT.equals(propertyName)) {
				isTimeoutProperty = true;
				Long syncTimeout = new Long(value.toString());
				WSIFJmsTransport transport = (WSIFJmsTransport) getTransport();
				transport.setSyncTimeout(syncTimeout);
				Trc.event(this, "overridding syncTimeout to " + syncTimeout);
			} else if (
				WSIFConstants.WSIF_PROP_ASYNC_TIMEOUT.equals(propertyName)) {
				isTimeoutProperty = true;
				Long asyncTimeout = new Long(value.toString());
				WSIFJmsTransport transport = (WSIFJmsTransport) getTransport();
				transport.setAsyncTimeout(asyncTimeout);
				Trc.event(this, "overridding asyncTimeout to " + asyncTimeout);
			}
		} catch (NumberFormatException ex) {
			Trc.ignoredException(ex);
		}
		return isTimeoutProperty;
	}

	/**
	 * Returns true if primitive is the equivalent primitive class of clazz.
	 * Why doesn't java provide this function?
	 */
	private static boolean isPrimitiveOf(Class clazz, Class primitive) {
		if ((primitive.equals(boolean.class)
			&& Boolean.class.isAssignableFrom(clazz))
			|| (primitive.equals(char.class)
				&& Character.class.isAssignableFrom(clazz))
			|| (primitive.equals(byte.class)
				&& Byte.class.isAssignableFrom(clazz))
			|| (primitive.equals(short.class)
				&& Short.class.isAssignableFrom(clazz))
			|| (primitive.equals(int.class)
				&& Integer.class.isAssignableFrom(clazz))
			|| (primitive.equals(long.class)
				&& Long.class.isAssignableFrom(clazz))
			|| (primitive.equals(float.class)
				&& Float.class.isAssignableFrom(clazz))
			|| (primitive.equals(double.class)
				&& Double.class.isAssignableFrom(clazz)))
			return true;
		else
			return false;
	}

	// package visable as it's used by WSIFJmsTransport
	void setAsyncRequestID(WSIFCorrelationId asyncRequestID) {
		Trc.entry(this, asyncRequestID);
		this.asyncRequestID = asyncRequestID;
		Trc.exit();
	}

	/**
	 * @deprecated use getOperationStyle
	 */
	public String getStyle() {
		return operationStyle;
	}

	/**
	 * Returns the operation style.
	 * @return String
	 */
	public String getOperationStyle() {
		return operationStyle;
	}

	/**
	 * @deprecated use setOperationStyle
	 */
	public void setStyle(String style) {
		this.operationStyle = style;
	}

	/**
	 * @deprecated should anyone be doing this?
	 */
	public void setOperationStyle(String style) {
		this.operationStyle = style;
	}

	public String deep() {
		StringBuffer buff = new StringBuffer();
		try {
			buff.append(super.toString()).append(":\n");
			buff.append("wsifPort:").append(wsifPort);
			buff.append(" portTypeOperation:").append(Trc.brief(portTypeOperation));
			buff.append(" bindingOperation:").append(bindingOperation);
			buff.append(" soapOperation:").append(soapOperation);
			buff.append(" operationStyle:").append(operationStyle);
			buff.append(" inputSOAPParts:").append(inputSOAPParts);
			buff.append(" inputUnwrappedSOAPParts:").append(inputUnwrappedSOAPParts);
			buff.append(" inputMIMEParts:").append(inputMIMEParts);
			buff.append(" inputSOAPHeader:").append(inputSOAPHeader);
			buff.append(" inputSOAPHeaderFault:").append(inputSOAPHeaderFault);
			buff.append(" outputSOAPParts:").append(outputSOAPParts);
			buff.append(" outputUnwrappedSOAPParts:").append(outputUnwrappedSOAPParts);
			buff.append(" outputMIMEParts:").append(outputMIMEParts);
			buff.append(" outputSOAPHeader:").append(outputSOAPHeader);
			buff.append(" outputSOAPHeaderFault:").append(outputSOAPHeaderFault);
			buff.append(" inputEncodingStyle:").append(inputEncodingStyle);
			buff.append(" inputNamespace:").append(inputNamespace);
			buff.append(" actionUri:").append(soapActionURI);
			buff.append(" inJmsProps:").append(inJmsProps);
			buff.append(" outJmsProps:").append(outJmsProps);
			buff.append(" inJmsPropVals:").append(inJmsPropVals);
			buff.append(" context:").append(context);
			buff.append(" asyncOperation:").append(asyncOperation);
			buff.append(" asyncRequestID:").append(asyncRequestID);
			buff.append(" responseHandler:").append(responseHandler);
			buff.append(" responseMessageParameters:").append(responseMessageParameters);
			buff.append(" outputEncodingStyle:").append(outputEncodingStyle);
			buff.append(" typeMap:").append(typeMap);
		} catch (Exception e) {
			Trc.exceptionInTrace(e);
		}
		return buff.toString();
	}
}

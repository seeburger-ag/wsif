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

package org.apache.wsif.providers.soap.apachesoap;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;

import org.apache.soap.Body;
import org.apache.soap.Constants;
import org.apache.soap.Envelope;
import org.apache.soap.Header;
import org.apache.soap.SOAPException;
import org.apache.soap.encoding.SOAPMappingRegistry;
import org.apache.soap.encoding.soapenc.BeanSerializer;
import org.apache.soap.messaging.Message;
import org.apache.soap.rpc.Call;
import org.apache.soap.rpc.Parameter;
import org.apache.soap.rpc.Response;
import org.apache.soap.rpc.SOAPContext;
import org.apache.soap.transport.SOAPTransport;
import org.apache.soap.transport.http.SOAPHTTPConnection;
import org.apache.soap.util.Bean;
import org.apache.soap.util.xml.Deserializer;
import org.apache.soap.util.xml.Serializer;
import org.apache.soap.util.xml.XMLParserUtils;
import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFCorrelationId;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFResponseHandler;
import org.apache.wsif.base.WSIFDefaultMessage;
import org.apache.wsif.base.WSIFDefaultOperation;
import org.apache.wsif.logging.MessageLogger;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.providers.WSIFDynamicTypeMap;
import org.apache.wsif.util.WSIFProperties;
import org.apache.wsif.util.WSIFUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Provide concrete implementation of WSDL operation with Apache SOAP
 * RPC method invocation.
 *
 * @author Alekander Slominski
 * @author Owen Burroughs <owenb@apache.org>
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class WSIFOperation_ApacheSOAP
    extends WSIFDefaultOperation
    implements WSIFOperation, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    	
    transient protected String style = null;

    transient protected WSIFPort_ApacheSOAP portInstance;
    transient protected Operation operation;
    transient protected Definition definition;

    // cached information to allow efficinet operation calls
    transient protected List partNames;
    transient protected String[] names;
    transient protected Class[] types;
    transient protected String inputEncodingStyle = Constants.NS_URI_SOAP_ENC;
    transient protected String inputNamespace;

    transient protected Class returnType;
    transient protected String actionUri;

    // info for async operation
    transient protected boolean asyncOperation;
    transient protected WSIFCorrelationId asyncRequestID;
    // everything other than what is needed for async response should be transient
    protected WSIFResponseHandler responseHandler;
    protected String outputEncodingStyle = Constants.NS_URI_SOAP_ENC;
    protected String returnName;
    protected boolean prepared = false;
    protected WSIFDynamicTypeMap typeMap;
    protected String inputUse = null;
    protected String outputUse = null;
    protected String partSerializerName = null;
    protected ArrayList wsdlOutParams;

    private static final String HTTP_PROXY_HOST_PROPERTY =
       "http.proxyHost";     
    private static final String HTTP_PROXY_PORT_PROPERTY =
       "http.proxyPort";     
    private static final String PROXY_EXCLUDES_PROPERTY = 
       "http.nonProxyHosts";
       
    /**
     * Create Apache SOAP operation instance that encapsultes all necessary
     * information required to create and execute Apache SOAP Call.
     */
    public WSIFOperation_ApacheSOAP(
        WSIFPort_ApacheSOAP pi,
        Operation op,
        WSIFDynamicTypeMap typeMap)
        throws WSIFException {
        Trc.entry(this, pi, op, typeMap);

        this.typeMap = typeMap;
        setDynamicWSIFPort(pi);
        setOperation(op);
        setDefinition(pi.getDefinition());

        if (Trc.ON)
            Trc.exit(deep());
    }

    /**
     * Create a new copy of this object. This is not a clone, since 
     * it does not copy the referenced objects as well.
     */
    public WSIFOperation_ApacheSOAP copy() throws WSIFException {
        Trc.entry(this);
        WSIFOperation_ApacheSOAP op =
            new WSIFOperation_ApacheSOAP(portInstance, operation, typeMap);

        op.setSoapActionURI(getSoapActionURI());
        op.setInputNamespace(getInputNamespace());
        op.setInputEncodingStyle(getInputEncodingStyle());
        op.setOutputEncodingStyle(getOutputEncodingStyle());
        op.setPartNames(getPartNames());
        op.setReturnName(getReturnName());
        op.setStyle(getStyle());
        op.setInputUse(getInputUse());
        op.setOutputUse(getOutputUse());
        op.setPartSerializerName(getPartSerializerName());
        op.setResponseHandler(getResponseHandler());
        op.setInputJmsProperties(getInputJmsProperties());
        op.setOutputJmsProperties(getOutputJmsProperties());
        op.setInputJmsPropertyValues(getInputJmsPropertyValues());

        if (Trc.ON)
            Trc.exit(op.deep());
        return op;
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
     * This is utility method that when called initializes operation
     * (including reconstruction of method signature).
     */
    void prepare(WSIFMessage inputMessage, WSIFMessage outputMessage)
        throws WSIFException {
        Trc.entry(this, inputMessage, outputMessage);
        HashMap mapOfUserTypes = portInstance.getLocalTypeMap();
        SOAPMappingRegistry smr = portInstance.getSOAPMappingRegistry();

        // Instantiate serializers once for the entire prepare
        BeanSerializer beanSer = new BeanSerializer();
        PartSerializer partSer = null;
        if (partSerializerName != null) {
            try {
                partSer =
                    (PartSerializer) Class
                        .forName(
                            partSerializerName,
                            true,
                            Thread.currentThread().getContextClassLoader())
                        .newInstance();
            } catch (Throwable ignored) {
                Trc.ignoredException(ignored);
            }
        }

        // first determine list of arguments
        Input input = operation.getInput();
        if (input != null) {
            List parts;
            if (partNames != null) {
                parts = new Vector();
                for (Iterator i = partNames.iterator(); i.hasNext();) {
                    String partName = (String) i.next();
                    Part part = input.getMessage().getPart(partName);
                    if (part == null) {
                        throw new WSIFException(
                            "no input part named "
                                + partName
                                + " for binding operation "
                                + getName());
                    }
                    parts.add(part);
                }
            } else {
                parts = input.getMessage().getOrderedParts(null);
            }
            int count = parts.size();
            names = new String[count];
            types = new Class[count];

            // get parts in correct order
            for (int i = 0; i < count; ++i) {
                Part part = (Part) parts.get(i);
                names[i] = part.getName();
                QName partType = part.getTypeName();
                if (partType == null)
                    partType = part.getElementName();
                if (partType == null) {
                    throw new WSIFException(
                        "part " + names[i] + " must have type name declared");
                }

                org.apache.soap.util.xml.QName qname =
                    new org.apache.soap.util.xml.QName(
                        partType.getNamespaceURI(),
                        partType.getLocalPart());
                try {
                    types[i] = (Class) mapOfUserTypes.get(qname);
                } catch (Throwable ignored) {
                    Trc.ignoredException(ignored);
                }
                if (types[i] == null) {
                    try {
                        types[i] =
                            (Class) smr.queryJavaType(
                                qname,
                                inputEncodingStyle);
                    } catch (Throwable exn) {
                        if (types[i] == null) {
                            try {
                                String packageName =
                                    WSIFUtils.getPackageNameFromNamespaceURI(
                                        qname.getNamespaceURI());
                                String className =
                                    WSIFUtils.getJavaClassNameFromXMLName(
                                        qname.getLocalPart());
                                Class inputClass = null;
                                try {
                                    inputClass =
                                    Class.forName(
                                        packageName + "." + className,
                                        true,
                                        Thread
                                            .currentThread()
                                            .getContextClassLoader());
                                } catch (ClassNotFoundException exn5) {
                                    inputClass =
                                        Class.forName(
                                            packageName
                                                + "."
                                                + className
                                                + "Element",
                                            true,
                                            Thread
                                                .currentThread()
                                                .getContextClassLoader());
                                }
                                types[i] = inputClass;
                                if ("literal".equals(inputUse)) {
                                	smr.mapTypes("literal", qname, inputClass, partSer, partSer);
                                } else {
                                smr.mapTypes(
                                    Constants.NS_URI_SOAP_ENC,
                                    qname,
                                    inputClass,
                                    beanSer,
                                    beanSer);
                                } 
                                mapSubtypes(inputClass, beanSer, partSer, smr, inputUse);
                            } catch (ClassNotFoundException exn1) {
                            	Trc.ignoredException(exn1);
                            }
                        }
                    }

                }
            }
        } else {
            names = new String[0];
            types = new Class[0];
        }

        // now prepare return value
        Output output = operation.getOutput();
        if (output != null) {
            Part returnPart = null;
            if (returnName != null) {
                returnPart = output.getMessage().getPart(returnName);
                if (returnPart == null) {
                    throw new WSIFException(
                        "no output part named "
                            + returnName
                            + " for bining operation "
                            + getName());
                }
            } else {
                List parts = output.getMessage().getOrderedParts(null);
                if (parts.size() > 0) {
                    returnPart = (Part) parts.getFirst();
                    returnName = returnPart.getName();
                }
            }

            if (returnPart != null) {
                QName partType = returnPart.getTypeName();
                if (partType == null)
                    partType = returnPart.getElementName();

                org.apache.soap.util.xml.QName qname =
                    new org.apache.soap.util.xml.QName(
                        partType.getNamespaceURI(),
                        partType.getLocalPart());

                try {
                    returnType = (Class) mapOfUserTypes.get(qname);
                } catch (Throwable ignored) {
                    Trc.ignoredException(ignored);
                }

                if (returnType == null) {
                    try {
                        returnType =
                            (Class) smr.queryJavaType(
                                qname,
                                outputEncodingStyle);
                    } catch (Throwable exn) {
                        if (returnType == null) {
                            try {
                                String packageName =
                                    WSIFUtils.getPackageNameFromNamespaceURI(
                                        qname.getNamespaceURI());
                                String className =
                                    WSIFUtils.getJavaClassNameFromXMLName(
                                        qname.getLocalPart());
                                try {
                                    returnType =
                                        Class.forName(
                                            packageName + "." + className,
                                            true,
                                            Thread
                                                .currentThread()
                                                .getContextClassLoader());
                                } catch (ClassNotFoundException exn5) {
                                    returnType =
                                        Class.forName(
                                            packageName
                                                + "."
                                                + className
                                                + "Element",
                                            true,
                                            Thread
                                                .currentThread()
                                                .getContextClassLoader());
                                }
                                if ("literal".equals(outputUse)) {
                                    smr.mapTypes(
                                        "literal",
                                        qname,
                                        returnType,
                                        partSer,
                                        partSer);
                                } else {
                                    smr.mapTypes(
                                        Constants.NS_URI_SOAP_ENC,
                                        qname,
                                        returnType,
                                        beanSer,
                                        beanSer);
                                }
                                mapSubtypes(returnType, beanSer, partSer, smr, outputUse);
                            } catch (ClassNotFoundException exn1) {
                                Trc.ignoredException(exn1);
                            }
                        }
                    }

                }
            }
            // setup any output paramter part names defined in the WSDL
            List parts = output.getMessage().getOrderedParts(null);
            if (parts.size() > 1) {
                ArrayList al = new ArrayList();
                for (int i = 1; i < parts.size(); i++) {
                    al.add(((Part) parts.get(i)).getName());
                }
                setWSDLOutParams(al);
            }
        }
        prepared = true;
        Trc.exit();
    }

    private void mapSubtypes(
        Class javaType,
        BeanSerializer beanSer,
        PartSerializer partSer,
        SOAPMappingRegistry smr,
        String use) {

        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(javaType);
        } catch (IntrospectionException e) {
            Trc.ignoredException(e);
        }

        if (beanInfo != null) {
            PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();
            if (properties != null) {
                for (int i = 0; i < properties.length; i++) {
                    Class propType = properties[i].getPropertyType();
                    try {
                        org.apache.soap.util.xml.QName qname =
                            smr.queryElementType(propType, inputEncodingStyle);
                    } catch (IllegalArgumentException exn) {
                        Trc.exception(exn);
                        // not found in the registry - complex type, not registered
                        // before
                        if (!propType.equals(Class.class)) {
                            String packageName =
                                propType.getPackage().getName();
                            String fullClassName = propType.getName();
                            String className =
                                fullClassName.substring(
                                    fullClassName.lastIndexOf(".") + 1);
                            org.apache.soap.util.xml.QName qname =
                                new org.apache.soap.util.xml.QName(
                                    WSIFUtils.getXSDNamespaceFromPackageName(
                                        packageName),
                                    className);

                            if ("literal".equals(use)) {
                                smr.mapTypes(
                                    "literal",
                                    qname,
                                    propType,
                                    partSer,
                                    partSer);
                            } else {
                                smr.mapTypes(
                                    Constants.NS_URI_SOAP_ENC,
                                    qname,
                                    propType,
                                    beanSer,
                                    beanSer);
                            }
                            mapSubtypes(propType, beanSer, partSer, smr, use);
                        }
                    }
                }
            }
        }
    }

    private void setupTypeMappings(
        HashMap mapOfUserTypes,
        SOAPMappingRegistry smr)
        throws WSIFException {

        // Mappings are set in the WSIFPort_ApacheSOAP which created this operation.
        // However, if this operation was deserialized for an async response then
        // we will have no reference to a WSIFPort and so we need to explicitly
        // create the mappings again.    	
        if (portInstance == null) {
            HashMap tempMap = new HashMap();
            WSIFPort_ApacheSOAP.prepareTypeMappings(
                smr,
                typeMap,
                partSerializerName,
                tempMap);
        }
    }

    public boolean executeRequestResponseOperation(
        WSIFMessage input,
        WSIFMessage output,
        WSIFMessage fault)
        throws WSIFException {

        Trc.entry(this, input, output, fault);
        close();

        setAsyncOperation(false);

        boolean succ;
        if ("document".equals(style))
            succ = invokeRequestResponseOperationDocument(input, output, fault);
        else
            succ = invokeRequestResponseOperation(input, output, fault);

        Trc.exit(succ);
        return succ;
    }

    public void executeInputOnlyOperation(WSIFMessage input)
        throws WSIFException {

        Trc.entry(this, input);
        setAsyncOperation(false);
        invokeRequestResponseOperation(input, null, null);
        Trc.exit();
    }

    /**
     * Invoke RPC operation using ApacheSOAP
     */
    public boolean invokeRequestResponseOperation(
        WSIFMessage input,
        WSIFMessage output,
        WSIFMessage fault)
        throws WSIFException {
        Trc.entry(this, input, output, fault);
        if (!prepared)
            prepare(input, output);

        Call call = portInstance.getCall();

        call.setEncodingStyleURI(getInputEncodingStyle());

        SOAPTransport st = getTransport();
        if (st != null) {
            call.setSOAPTransport(st);
            if (st instanceof SOAPJMSConnection sjt) {
            	sjt.setSyncTimeout(WSIFProperties.getSyncTimeout());
            	sjt.setAsyncTimeout(WSIFProperties.getAsyncTimeout());
            }
        }

        call.setTargetObjectURI(getInputNamespace());
        call.setMethodName(getName());

		if (inJmsPropVals != null && !inJmsPropVals.isEmpty()) {
			checkForTimeoutProperties(st, inJmsPropVals);
            ((SOAPJMSConnection) st).setJmsProperties(inJmsPropVals);
		}

        Vector params = new Vector();
        Object partInst;
        for (int i = 0; i < names.length; ++i) {
            try {
                partInst = input.getObjectPart(names[i]);
            } catch (WSIFException ex) {
                Trc.exception(ex);
                partInst = null;
            }
            Object value = partInst;
            // some runtime param validity check

            if (value != null
                && !types[i].isPrimitive()
                && !(types[i].isAssignableFrom(value.getClass()))) {
                throw new WSIFException(
                    "value "
                        + value
                        + " has unexpected type "
                        + value.getClass()
                        + " instead of "
                        + types[i]);
            }

			if (inJmsProps.containsKey(names[i]) && st != null) {
				String name = (String) (inJmsProps.get(names[i]));
				if (!timeoutProperty(st, name, value)) {
                   ((SOAPJMSConnection) st).setJmsProperty(
				      name,
				      value);
				}
			} else {
                Parameter param =
                    new Parameter(
                        names[i],
                        types[i],
                        value,
                        inputEncodingStyle);
                params.addElement(param);
            }
        }

        call.setParams(params);

        setTransportContext(st); 

        setCallContext(call);

        // invoke the operation through ApacheSOAP
        Response resp;
        boolean respOK = true;
        URL locationUri = portInstance.getEndPoint();
        if ( locationUri != null && !isHostInNonProxyProperty( locationUri ) ) {
           setSOAPProxy( st );
        }

        Trc.event(
            this,
            "Invoking operation ",
            getName(),
            " on ",
            locationUri,
            " call object ",
            call);

        try {
            resp = call.invoke(locationUri, getSoapActionURI());
        } catch (SOAPException e) {
            Trc.exception(e);

            // Log message
            MessageLogger.log("WSIF.0005E", "ApacheSOAP", getName());

            throw new WSIFException(
                "SOAPException: " + e.getFaultCode() + e.getMessage(),
                e);
        }

        Trc.event(this, "Operation returned ", resp);

        // setJMSOutPropsInContext( st ); TODO doesn't work yet

        if (!isAsyncOperation() && returnType != null) {
            respOK = buildResponseMessages(resp, output, fault);
        }
        
        if (resp instanceof Response) {
            Header soapHeader = resp.getHeader();
   		    addContextResponseSOAPHeaders(soapHeader);
        }

        Trc.exit(respOK);
        return respOK;
    }

    public boolean invokeRequestResponseOperationDocument(
        WSIFMessage input,
        WSIFMessage output,
        WSIFMessage fault)
        throws WSIFException {
        Trc.entry(this, input, output, fault);

        if (!prepared)
            prepare(input, output);
        Envelope msgEnv = new Envelope();
        Body msgBody = new Body();
        Vector vect = new Vector();

        Iterator iterator =
            operation.getInput().getMessage().getParts().keySet().iterator();
        while (iterator.hasNext()) {
            String partName = (String) iterator.next();
            Object part = input.getObjectPart(partName);
            String encoding = null;
            if ("literal".equals(inputUse)) {
                // Should also include namespace
                encoding = "literal";
            } else {
                encoding = this.inputEncodingStyle;
            }

            PartSerializer partSerializer = null;
            Object o =
                portInstance.getSOAPMappingRegistry().querySerializer(
                    part.getClass(),
                    inputUse);
            if (o instanceof PartSerializer tmp) {
                try {
                    partSerializer =
                        (PartSerializer) tmp.getClass().newInstance();
                } catch (InstantiationException e) {
                    Trc.ignoredException(e);
                } catch (IllegalAccessException e) {
                    Trc.ignoredException(e);
                }
                partSerializer.setPart(part);
                Part modelPart =
                    operation.getInput().getMessage().getPart(partName);
                javax.xml.namespace.QName partType = modelPart.getTypeName();
                if (partType == null)
                    partType = modelPart.getElementName();
                partSerializer.setPartQName(partType);
            } else // document with soap encoding - never been tried
                {
                partSerializer = new SOAPEncSerializerWrapper();
                partSerializer.setPart(part);
                (
                    (
                        SOAPEncSerializerWrapper) partSerializer)
                            .setTargetSerializer(
                    (Serializer) o);
            }

            Bean bean = new Bean(partSerializer.getClass(), partSerializer);
            vect.add(bean);
        }

        // create message envelope and body
        URL url = portInstance.getEndPoint();
        Envelope env = null;
        msgBody.setBodyEntries(vect);
        msgEnv.setBody(msgBody);

        SOAPTransport st = getTransport();

        if (st instanceof SOAPJMSConnection sjt) {
           sjt.setSyncTimeout(WSIFProperties.getSyncTimeout());
           sjt.setAsyncTimeout(WSIFProperties.getAsyncTimeout());
        }

        if (inJmsPropVals != null && !inJmsPropVals.isEmpty()) {
 		   checkForTimeoutProperties(st, inJmsPropVals);
           ((SOAPJMSConnection) st).setJmsProperties(inJmsPropVals);
        }

        //TODO docstyle headers
        //setCallContext( call );

        if ( url != null && !isHostInNonProxyProperty( url ) ) {
           setSOAPProxy( st );
        }

        // create and send message
        try {
            Message msg = new Message();
            if (st != null)
                msg.setSOAPTransport(st);

            Trc.event(
                this,
                "Invoking operation ",
                getName(),
                " url ",
                url,
                " soapaction ",
                getSoapActionURI(),
                " envelope ",
                msgEnv,
                " message ",
                msg);

            msg.send(url, getSoapActionURI(), msgEnv);

            // receive response envelope
            env = msg.receiveEnvelope();
        } catch (SOAPException exn) {
            Trc.exception(exn);
            WSIFException e =
                new WSIFException("SOAP Exception: " + exn.getMessage());
            e.setTargetException(exn);
            throw e;
        }

        Trc.event(this, "Returned from operation, envelope ", env);

        Body retbody = env.getBody();
        java.util.Vector v = retbody.getBodyEntries();
        int index = 0;

        String encoding = null;
        if ("literal".equals(outputUse)) {
            // Should also include namespace
            encoding = "literal";
        } else {
            encoding = this.outputEncodingStyle;
        }

        iterator =
            operation.getOutput().getMessage().getParts().keySet().iterator();
        while (iterator.hasNext()) {
            Element element = (Element) v.get(index++);
            String partName = (String) iterator.next();
            Part modelPart =
                operation.getOutput().getMessage().getPart(partName);
            javax.xml.namespace.QName partType = modelPart.getTypeName();
            if (partType == null)
                partType = modelPart.getElementName();

            PartSerializer partSerializer = null;
            Object o =
                this.portInstance.getSOAPMappingRegistry().queryDeserializer(
                    new org.apache.soap.util.xml.QName(
                        partType.getNamespaceURI(),
                        partType.getLocalPart()),
                    encoding);

            if (o instanceof PartSerializer tmp) {
                try {
                    partSerializer =
                        (PartSerializer) tmp.getClass().newInstance();
                } catch (InstantiationException e) {
                    Trc.ignoredException(e);
                } catch (IllegalAccessException e) {
                    Trc.ignoredException(e);
                }
                partSerializer.setPartQName(partType);
                partSerializer.unmarshall(null, null, element, null, null);
                Object retBean = partSerializer.getPart();
                output.setObjectPart(partName, retBean);
            } else // document with soap encoding - never been tried	
                {
                Bean bean =
                    ((Deserializer) o).unmarshall(
                        null,
                        null,
                        element,
                        null,
                        null);
                Object retBean = bean.value;
            }
        }
        
        Header soapHeader = env.getHeader();
	    addContextResponseSOAPHeaders(soapHeader);

        Trc.exit(true);
        return true;
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
    public WSIFCorrelationId executeRequestResponseAsync(WSIFMessage input)
        throws WSIFException {

        Trc.entry(this, input);
        WSIFCorrelationId id = executeRequestResponseAsync(input, null);
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
        WSIFMessage input,
        WSIFResponseHandler handler)
        throws WSIFException {

        Trc.entry(this, input, handler);
        close();

        if (!prepared)
            prepare(input, null);

        if (!portInstance.supportsAsync()) {
            throw new WSIFException("asynchronous operations not available");
        }

        if ("document".equals(style)) {
            throw new WSIFException("docstyle asynchronous operations not implemented");
        }

        setAsyncOperation(true);
        setResponseHandler(handler);
        SOAPJMSConnection transport = (SOAPJMSConnection) getTransport();
        transport.setWsifOperation(this);
        transport.setAsyncOperation(true);

        invokeRequestResponseOperation(input, null, null);

        transport.setAsyncOperation(false);
        WSIFCorrelationId id = getAsyncRequestID();
        Trc.exit(id);
        return id;

    }

    /**
     * fireAsyncResponse is called by an AsyncListener when a response
     * has been received for a previous executeRequestResponseAsync call.
     * This WSIFOperation will have been serialized in the correlation
     * service when the request was sent. When the AsynListener is notified
     * that a response has arrived for the request it unserializes this
     * WSIFOperation from the corelation service and calls this method. 
     * This method will then unmarshal the reponse and pass it to the
     * executeAsyncResponse method of the associated WSIFResponseHandler.
     * @see WSIFOperation#fireAsyncResponse(Object)
     * @param response   an Object representing the response. The response
     *                   will be raw XML
     */
    public void fireAsyncResponse(Object response) throws WSIFException {
        Trc.entry(this, response);

        Response resp = deserialiseResponseObject(response);

        WSIFMessage outMsg = createOutputMessage();
        WSIFMessage faultMsg = createFaultMessage();
        buildResponseMessages(resp, outMsg, faultMsg);

        getResponseHandler().executeAsyncResponse(outMsg, faultMsg);

        Trc.exit(outMsg); // , faultMsg } ); ??
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

        Response resp = deserialiseResponseObject(response);
        boolean ok = buildResponseMessages(resp, output, fault);

        Trc.exit( ok ); 
        return ok;
    }

    /**
     * This deserialises and unmarshalls the response message.
     * Its copied, with minor changes, from the 2nd half of 
     * the SOAP 2.2 Call class invoke method.
     */
    private Response deserialiseResponseObject(Object msg)
        throws WSIFException {

        if (msg == null) {
            throw new WSIFException("null response to async send");
        }

        if (!(msg instanceof javax.jms.TextMessage)) {
            throw new WSIFException("response not a javax.jms.TextMessage");
        }

        HashMap mapOfUserTypes = new HashMap();
        SOAPMappingRegistry smr =
            WSIFPort_ApacheSOAP.createSOAPMappingRegistry(new Call());
          
        // clear port to ensure setupTypeMappings does something
        setDynamicWSIFPort( null );

        setupTypeMappings(mapOfUserTypes, smr);

        try {

            javax.jms.TextMessage m = (javax.jms.TextMessage) msg;
            String payloadStr = m.getText();

            // Get the response context.
            SOAPContext respCtx = new SOAPContext();
            respCtx.setRootPart(payloadStr, "text/xml");

            // Parse the incoming response stream.
            DocumentBuilder xdb = XMLParserUtils.getXMLDocBuilder();
            Document respDoc =
                xdb.parse(new InputSource(new StringReader(payloadStr)));
            Element payload = null;

            if (respDoc != null) {
                payload = respDoc.getDocumentElement();
            } else { //probably does not happen
                throw new SOAPException(
                    Constants.FAULT_CODE_CLIENT,
                    "Parsing error, response was:\n" + payloadStr);
            }

            // Unmarshall the response envelope.
            Envelope respEnv = Envelope.unmarshall(payload, respCtx);

            // Extract the response from the response envelope.
            Response resp = Response.extractFromEnvelope(respEnv, smr, respCtx);

            return resp;

        } catch (Exception ex) {
            Trc.exception(ex);
            throw new WSIFException(ex.getMessage());
        }
    }

    /**
     * Extracts the message parts from the response.
     */
    private boolean buildResponseMessages(
        Response resp,
        WSIFMessage outMsg,
        WSIFMessage faultMsg)
        throws WSIFException {

        boolean respOK;

        if (resp == null) {
            throw new WSIFException("soap response is null");
        }

        if (resp.generatedFault()) {
            respOK = false;
            if (faultMsg != null) {
                org.apache.soap.Fault soapFault = resp.getFault();
                faultMsg.setName(WSIFConstants.SOAP_FAULT_MSG_NAME);
                faultMsg.setObjectPart(
                    WSIFConstants.SOAP_FAULT_OBJECT,
                    soapFault);
            }
        } else {
            respOK = true;
            populateOutMsgReturnPart(resp, outMsg);
            populateOutMsgParts(resp, outMsg);
        }
        return respOK;
    }

    /**
     * Populate the outMessage with the response return value.
     */
    private void populateOutMsgReturnPart(Response resp, WSIFMessage outMsg)
        throws WSIFException {
        if (outMsg != null && returnName != null) {
            Parameter retValue = resp.getReturnValue();
            if (retValue == null) {
                throw new WSIFException("return value not found in response message");
            }
            Object result = retValue.getValue();
            if (returnType != null) { // will be null for async responses
                if (!returnType.isPrimitive()
                    && result != null
                    && !(returnType.isAssignableFrom(result.getClass()))) {
                    throw new WSIFException(
                        "return value "
                            + result
                            + " has unexpected type "
                            + result.getClass()
                            + " instead of "
                            + returnType);
                }
            }
            outMsg.setObjectPart(returnName, result);
        }
    }

    /**
     * Populate the outMessage with the expected parts.
     * (this only does the out parameters not the return part)
     */
    private void populateOutMsgParts(Response resp, WSIFMessage outMsg)
        throws WSIFException {
        if (outMsg != null) {
            Vector respParms = resp.getParams();
            ArrayList wsdlOutParameters = getWSDLOutParams();
            if (respParms != null) {
                Parameter p;
                for (Iterator i = respParms.iterator(); i.hasNext();) {
                    p = (Parameter) i.next();
                    outMsg.setObjectPart(p.getName(), p.getValue());
                    wsdlOutParameters.remove(p.getName());
                }
            }
            // missing parts default to null
            for (Iterator i = wsdlOutParameters.iterator(); i.hasNext();) {
                outMsg.setObjectPart((String) i.next(), null);
            }
        }
    }

    /**
     * Return name of operation.
     */
    public String getName() {
        Trc.entry(this);
        String s = operation.getName();
        Trc.exit(s);
        return s;
    }

    public String getSoapActionURI() {
        Trc.entry(this);
        Trc.exit(actionUri);
        return actionUri;
    }

    public void setSoapActionURI(String value) {
        Trc.entry(this, value);
        actionUri = value;
        Trc.exit();
    }

    public String getInputNamespace() {
        Trc.entry(this);
        Trc.exit(inputNamespace);
        return inputNamespace;
    }

    public void setInputNamespace(String value) {
        Trc.entry(this, value);
        inputNamespace = value;
        Trc.exit();
    }

    public String getInputEncodingStyle() {
        Trc.entry(this);
        Trc.exit(inputEncodingStyle);
        return inputEncodingStyle;
    }

    public void setInputEncodingStyle(String value) {
        Trc.entry(this, value);
        inputEncodingStyle = value;
        Trc.exit();
    }

    public String getOutputEncodingStyle() {
        Trc.entry(this);
        Trc.exit(outputEncodingStyle);
        return outputEncodingStyle;
    }

    public void setOutputEncodingStyle(String value) {
        Trc.entry(this, value);
        outputEncodingStyle = value;
        Trc.exit();
    }

    public List getPartNames() {
        Trc.entry(this);
        Trc.exit(partNames);
        return partNames;
    }

    public void setPartNames(List value) {
        Trc.entry(this, value);
        partNames = value;
        Trc.exit();
    }

    public String getReturnName() {
        Trc.entry(this);
        Trc.exit(returnName);
        return returnName;
    }

    public void setReturnName(String value) {
        Trc.entry(this, value);
        returnName = value;
        Trc.exit();
    }

    // where is WSDL defining this abstract mesage
    public Operation getOperation() {
        Trc.entry(this);
        Trc.exit(operation);
        return operation;
    }

    public void setOperation(Operation value) {
        Trc.entry(this, value);
        operation = value;
        Trc.exit();
    }

    public Definition getDefinition() {
        Trc.entry(this);
        Trc.exit(definition);
        return definition;
    }

    public void setDefinition(Definition value) {
        Trc.entry(this, value);
        definition = value;
        Trc.exit();
    }

    // WSIF related
    public WSIFPort_ApacheSOAP getDynamicWSIFPort() {
        Trc.entry(this);
        Trc.exit(portInstance);
        return portInstance;
    }

    public void setDynamicWSIFPort(WSIFPort_ApacheSOAP value) {
        Trc.entry(this, value);
        portInstance = value;
        Trc.exit();
    }

    /**
     * Gets the style.
     * @return Returns a String
     */
    public String getStyle() {
        Trc.entry(this);
        Trc.exit(style);
        return style;
    }

    /**
     * Gets the response handler that will be used to
     * process the response to a asynchronous request.
     * @return the current response handler.
     * (package visable as called from Transport)
     */
    WSIFResponseHandler getResponseHandler() {
        Trc.entry(this);
        Trc.exit(responseHandler);
        return responseHandler;
    }

    /**
     * Gets the transport being used by this operation
     * @return the current transport
     */
    public SOAPTransport getTransport() {
        Trc.entry(this);
        SOAPTransport t = portInstance.getSOAPTransport();
        Trc.exit(t);
        return t;
    }

    /**
     * Gets the correlation ID of the last request sent by the
     * executeRequestResponseAsync method.
     * @return the corelation ID of the previous request
     */
    public WSIFCorrelationId getAsyncRequestID() {
        Trc.entry(this);
        Trc.exit(asyncRequestID);
        return asyncRequestID;
    }

    /**
     * Sets the correlation ID of the last request sent by the
     * executeRequestResponseAsync method.
     * @return the corelation ID of the previous request
     * (package visable as its called by WSIFJmsTransport)
     */
    void setAsyncRequestID(WSIFCorrelationId asyncRequestID) {
        Trc.entry(this, asyncRequestID);
        this.asyncRequestID = asyncRequestID;
        Trc.exit();
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
     * Sets the style.
     * @param style The style to set
     */
    public void setStyle(String style) {
        Trc.entry(this, style);
        this.style = style;
        Trc.exit();
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
     * Gets the inputUse.
     * @return Returns a String
     */
    public String getInputUse() {
        Trc.entry(this);
        Trc.exit(inputUse);
        return inputUse;
    }

    /**
     * Sets the inputUse.
     * @param inputUse The inputUse to set
     */
    public void setInputUse(String inputUse) {
        Trc.entry(this, inputUse);
        this.inputUse = inputUse;
        Trc.exit();
    }

    /**
     * Gets the outputUse.
     * @return Returns a String
     */
    public String getOutputUse() {
        Trc.entry(this);
        Trc.exit(outputUse);
        return outputUse;
    }

    /**
     * Sets the outputUse.
     * @param outputUse The outputUse to set
     */
    public void setOutputUse(String outputUse) {
        Trc.entry(this, outputUse);
        this.outputUse = outputUse;
        Trc.exit();
    }

    /**
     * Gets the partSerializerName.
     * @return Returns a String
     */
    public String getPartSerializerName() {
        Trc.entry(this);
        Trc.exit(partSerializerName);
        return partSerializerName;
    }

    private ArrayList getWSDLOutParams() {
        if (wsdlOutParams == null) {
            wsdlOutParams = new ArrayList();
        }
        return wsdlOutParams;
    }

    private void setWSDLOutParams(ArrayList al) {
        wsdlOutParams = al;
    }

    public WSIFPort getWSIFPort() {
        Trc.entry(this);
        Trc.exit(portInstance);
        return portInstance;
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
     * Sets the partSerializerName.
     * @param partSerializerName The partSerializerName to set
     */
    public void setPartSerializerName(String partSerializerName) {
        Trc.entry(this, partSerializerName);
        this.partSerializerName = partSerializerName;
        Trc.exit();
    }

    /**
     * This sets up the output JMS property values in the context
     */
    private void setJMSOutPropsInContext(SOAPTransport t)
        throws WSIFException {
        if (!(t instanceof SOAPJMSConnection)) {
            return;
        }
        HashMap props = ((SOAPJMSConnection) t).getJmsProperties();
        if (props != null) {
            if (context == null) {
                context = new WSIFDefaultMessage();
            }
            context.setParts(props);
        }
    }

    /**
     * This sets up any context property values in the transport
     */
    private void setTransportContext(SOAPTransport t) {
        if (context == null || !(t instanceof SOAPJMSConnection)) {
            return;
        }
        HashMap jmsProps = new HashMap();
        for (Iterator i = context.getPartNames(); i.hasNext();) {
           try {
              String partName = (String) i.next();
      		  Object value = context.getObjectPart(partName);
	    	  if (!timeoutProperty(t, partName, value)) {
                 if (partName.startsWith(WSIFConstants.CONTEXT_JMS_PREFIX)) {
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
            ((SOAPJMSConnection) t).setJmsProperties(jmsProps);
        }
    }

    /**
     * This sets up the context headers in the SOAP 
     * Call object prior to invoke method being issued.
     */
    private void setCallContext(Call call) {
        Object o;
        String name;

        // clear out any old header values
        call.setHeader(null);
        addHTTPHeader(call, WSIFConstants.CONTEXT_HTTP_USER, null);
        addHTTPHeader(call, WSIFConstants.CONTEXT_HTTP_PSWD, null);

        if (context == null) {
            return;
        }

        name = WSIFConstants.CONTEXT_HTTP_USER;
        try {
            o = context.getObjectPart(name);
            if (o instanceof String string) {
                addHTTPHeader(call, name, string);
            }
        } catch (WSIFException ex) {
            Trc.ignoredException(ex);
            addHTTPHeader(call, name, null);
        }

        name = WSIFConstants.CONTEXT_HTTP_PSWD;
        try {
            o = context.getObjectPart(name);
            if (o instanceof String string) {
                addHTTPHeader(call, name, string);
            }
        } catch (WSIFException ex) {
            Trc.ignoredException(ex);
            addHTTPHeader(call, name, null);
        }

        try {
            name = WSIFConstants.CONTEXT_REQUEST_SOAP_HEADERS;
            o = context.getObjectPart(name);
            if (o instanceof List list) {
                addSOAPHeader(call, name, list);
            }
        } catch (WSIFException ex) {
            Trc.ignoredException(ex);
        }
    }

    /**
     * Sets the SOAP headers in the message context.
     */
    private void addSOAPHeader(Call call, String name, List soapHeaders) {
        Header h = new Header();
        h.setHeaderEntries(new Vector(soapHeaders));
        call.setHeader(h);
    }

    /**
     * Sets the HTTP header value if the underlying transport
     * is the SOAP HTTP transport. The only support HTTP headers
     * are the basic authentication user id and password.
     */
    private void addHTTPHeader(Call call, String name, String value) {
        SOAPTransport st = call.getSOAPTransport();
        if (st instanceof SOAPHTTPConnection) {
            SOAPHTTPConnection httpTransport = (SOAPHTTPConnection) st;
            if (name.equals(WSIFConstants.CONTEXT_HTTP_USER)) {
                httpTransport.setUserName(value);
            } else if (name.equals(WSIFConstants.CONTEXT_HTTP_PSWD)) {
                httpTransport.setPassword(value);
            }
        }
    }

    /**
     * This adds any SOAP headers in the response to the context 
     */ 
    private void addContextResponseSOAPHeaders(Header soapHeader)
        throws WSIFException {

        if( soapHeader != null ) {
   	        Vector headerEntries = soapHeader.getHeaderEntries();
   	        if (headerEntries != null) {
                ArrayList headers = new ArrayList();
                for(Iterator i = headerEntries.iterator(); i.hasNext(); ) {
              	    Object o = i.next();
             	    if (o instanceof Element) {
                        headers.add(o);
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
    }

    private boolean isHostInNonProxyProperty(URL u) {
    	if ( u != null ) {
           String excludeList = 
              (String) AccessController.doPrivileged(new PrivilegedAction() {
                 public Object run() {
                    return System.getProperty( PROXY_EXCLUDES_PROPERTY );
                 }
              });
           if ( excludeList != null ) { 
              StringTokenizer st = new StringTokenizer( excludeList, "|" );
              while ( st.hasMoreTokens() ) {
        	     String xhost = st.nextToken().trim();
        	     if ( WSIFUtils.wildcardCompare( xhost, u.getHost(), '*' ) ) {
        	     	return true;
        	     }
              }
           }
    	}
    	return false;
    }
    
    /**
     * Unike axis, soap doesn't automatically use the Java
     * system properties for a proxy server, we need to set
     * it up manually.
     */
    private void setSOAPProxy(SOAPTransport st) throws WSIFException {
        if (st instanceof SOAPHTTPConnection) {
           SOAPHTTPConnection shttpc = (SOAPHTTPConnection) st;
           String proxyHost = 
              (String) AccessController.doPrivileged(new PrivilegedAction() {
                 public Object run() {
                    return System.getProperty( HTTP_PROXY_HOST_PROPERTY );
                 }
              });
           String proxyPort = 
              (String) AccessController.doPrivileged(new PrivilegedAction() {
                 public Object run() {
                    return System.getProperty( HTTP_PROXY_PORT_PROPERTY );
                 }
              });
           if ( proxyHost != null && proxyHost.length() > 0 ) {
              shttpc.setProxyHost( proxyHost );
              if ( proxyPort != null && proxyPort.length() > 0 ) {
              	 try {
              	 	int port = Integer.parseInt( proxyPort );
                    shttpc.setProxyPort( port );
              	 } catch (NumberFormatException ex) {
                    Trc.ignoredException(ex);
              	 }
              }
              setProxyUserFromContext(shttpc);
           }
        }
    }
    
    private void setProxyUserFromContext(SOAPHTTPConnection shttpc) throws WSIFException {
    	WSIFMessage ctx = getContext();
    	String uid = null;
    	String pswd = null;
    	try {
            Object o = ctx.getObjectPart(WSIFConstants.CONTEXT_HTTP_PROXY_USER);
            if (o != null) {
                if ( o instanceof String string) {
                	uid = string;
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
                if ( o instanceof String string) {
                	pswd = string;
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
        	shttpc.setProxyUserName(uid);
        	shttpc.setProxyPassword(pswd);
        }
    }

	private void checkForTimeoutProperties(SOAPTransport st, HashMap inJmsPropVals) {
       if (inJmsPropVals != null) {
		  for (Iterator i = inJmsPropVals.keySet().iterator(); i.hasNext();) {
		 	 String name = (String) i.next();
			 Object value = inJmsPropVals.get(name);
			 if (timeoutProperty(st, name, value)) {
				i.remove();
			 }
		  }
       }
	}

	private boolean timeoutProperty(
        SOAPTransport t,
		String propertyName,
		Object value) {
		Trc.entry(this, t, propertyName, value);
		boolean isTimeoutProperty = false;
		if (t != null 
		&& t instanceof SOAPJMSConnection st) {
   		   try {
			  if (WSIFConstants.WSIF_PROP_SYNC_TIMEOUT.equals(propertyName)) {
			     isTimeoutProperty = true;
				 long syncTimeout = Long.parseLong(value.toString());
				 st.setSyncTimeout(syncTimeout);
				 Trc.event(this, "overridding syncTimeout to " + syncTimeout);
			  } else if (
			 	 WSIFConstants.WSIF_PROP_ASYNC_TIMEOUT.equals(propertyName)) {
				 isTimeoutProperty = true;
				 long asyncTimeout = Long.parseLong(value.toString());
				 st.setAsyncTimeout(asyncTimeout);
				 Trc.event(this, "overridding asyncTimeout to " + asyncTimeout);
			  }
		   } catch (NumberFormatException ex) {
			  Trc.ignoredException(ex);
		   }
		}
		Trc.exit(isTimeoutProperty);
		return isTimeoutProperty;
	}

    public String deep() {
        String buff = "";
        try {
            buff = new String(super.toString() + ":\n");
            buff += " style:" + style;
            buff += " portInstance:" + portInstance;
            buff += " operation:" + Trc.brief(operation);
            buff += " definition:" + Trc.brief(definition);
            buff += " context:" + context;
            buff += " partNames:" + partNames;
            buff += Trc.brief("names", names);
            buff += Trc.brief("types", types);
            buff += " inputEncodingStyle:" + inputEncodingStyle;
            buff += " inputNamespace:" + inputNamespace;
            buff += " returnType:" + returnType;
            buff += " actionUri:" + actionUri;
            buff += " inJmsProps:" + inJmsProps;
            buff += " outJmsProps:" + outJmsProps;
            buff += " inJmsPropVals:" + inJmsPropVals;
            buff += " asyncOperation:" + asyncOperation;
            buff += " asyncRequestID:" + asyncRequestID;
            buff += " responseHandler:" + responseHandler;
            buff += " outputEncodingStyle:" + outputEncodingStyle;
            buff += " returnName:" + returnName;
            buff += " prepared:" + prepared;
            buff += " typeMap:" + typeMap;
            buff += " inputUse:" + inputUse;
            buff += " outputUse:" + outputUse;
            buff += " partSerializerName:" + partSerializerName;
        } catch (Exception e) {
            Trc.exceptionInTrace(e);
        }
        return buff;
    }
}

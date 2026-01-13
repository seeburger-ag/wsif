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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;

import org.apache.soap.Constants;
import org.apache.soap.encoding.SOAPMappingRegistry;
import org.apache.soap.encoding.soapenc.Base64Serializer;
import org.apache.soap.encoding.soapenc.BeanSerializer;
import org.apache.soap.encoding.soapenc.DateSerializer;
import org.apache.soap.rpc.Call;
import org.apache.soap.transport.SOAPTransport;
import org.apache.soap.transport.http.SOAPHTTPConnection;
import org.apache.soap.util.xml.Deserializer;
import org.apache.soap.util.xml.Serializer;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.base.WSIFDefaultPort;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.providers.WSIFDynamicTypeMap;
import org.apache.wsif.providers.WSIFDynamicTypeMapping;
import org.apache.wsif.util.WSIFUtils;
import org.apache.wsif.wsdl.extensions.jms.JMSAddress;
import org.apache.wsif.wsdl.extensions.jms.JMSProperty;
import org.apache.wsif.wsdl.extensions.jms.JMSPropertyValue;

/**
 * This is Apache SOAP dynamic WSIF port  that is driven by WSDL.
 *
 * @author Alekander Slominski
 * @author Owen Burroughs <owenb@apache.org>
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class WSIFPort_ApacheSOAP extends WSIFDefaultPort {

    @Serial
    private static final long serialVersionUID = 1L;

    transient protected Map operationInstances = new HashMap();
    protected Port port;
    protected Definition definition;
    transient protected Call call;
    transient protected SOAPTransport st;
    protected URL url;
    protected String style = "document";
    protected String partSerializerName = null;

    private WSIFDynamicTypeMap wsifTypeMap = null;
    private HashMap localTypeMap;
    private List jmsAddressPropVals = null;

    private static final String LITERAL_ENCODING = "literal";
    
    /**
     * Create dynamic port instance from WDL model defintion and port.
     * <p><b>NOTE:</b> this constructor is doing full initialization
     *  therefore after dynamic port is created overhead of executing
     *  operation should be as small as possible for dynamic case...
     */
    public WSIFPort_ApacheSOAP(
        Definition def,
        Service service,
        Port port,
        WSIFDynamicTypeMap typeMap,
        String partSerName)
        throws WSIFException {
        Trc.entry(this, def, service, port, typeMap, partSerName);

        setPartSerializerName(partSerName);
        setDefinition(def);
        setPort(port);
        wsifTypeMap = typeMap;

        JMSAddress ja =
            (JMSAddress) getExtElem(port,
                JMSAddress.class,
                port.getExtensibilityElements());
        SOAPAddress sa =
            (SOAPAddress) getExtElem(port,
                SOAPAddress.class,
                port.getExtensibilityElements());

        if (sa != null && ja != null)
            throw new WSIFException(
                "Both soap:address and jms:address cannot be specified for port "
                    + port.getName());

        if (sa == null && ja == null)
            throw new WSIFException(
                "Either soap:address or jms:address must be specified for port "
                    + port.getName());

        if (ja != null) {
            // Port jms:address binding element
            jmsAddressPropVals = ja.getJMSPropertyValues();
            st = new SOAPJMSConnection(ja, port.getName());
        } else {
            // Port soap:address bindng element
            st = new SOAPHTTPConnection();
            // call.getSOAPTransport() is null...

            String s = sa.getLocationURI();
            try {
                url = URI.create(s).toURL();
            } catch (MalformedURLException meu) {
	        	Trc.exception(meu);
                throw new WSIFException(
                    "could not set SOAP address to " + s,
                    meu);
            }
            if (url == null) {
                throw new WSIFException(
                    "soap:address with location URI is required for " + port.getName());
            }
        }

        // check soap:binding element
        Binding binding = port.getBinding();
        SOAPBinding soapBinding =
            (SOAPBinding) getExtElem(binding,
                SOAPBinding.class,
                binding.getExtensibilityElements());
        if (soapBinding != null) {
            style = soapBinding.getStyle();
            if (style == null)
                style = "document"; //it is default style value as of WSDL 1.1

            String transport = soapBinding.getTransportURI();
            if ((ja != null
                && !"http://schemas.xmlsoap.org/soap/jms".equals(transport))
                || (sa != null
                    && !"http://schemas.xmlsoap.org/soap/http".equals(
                        transport))) {
                throw new WSIFException(
                    "unsupported transport "
                        + transport
                        + " for "
                        + soapBinding);
            }
        }
        
        if (Trc.ON)
            Trc.exit(deep());
    }

    public static SOAPMappingRegistry createSOAPMappingRegistry(Call call) {
        Trc.entry(null,call);
        SOAPMappingRegistry smr = call.getSOAPMappingRegistry();

        // Add mapping registry entry for dateTime
        DateSerializer dateSer = new DateSerializer();
        // 1999 deserializer
        smr.mapTypes(
            Constants.NS_URI_SOAP_ENC,
            new org.apache.soap.util.xml.QName(
                Constants.NS_URI_1999_SCHEMA_XSD,
                "dateTime"),
            java.util.Date.class,
            null,
            dateSer);
        // 2000 deserializer
        smr.mapTypes(
            Constants.NS_URI_SOAP_ENC,
            new org.apache.soap.util.xml.QName(
                Constants.NS_URI_2000_SCHEMA_XSD,
                "dateTime"),
            java.util.Date.class,
            null,
            dateSer);
        // 2001 deserializer
        smr.mapTypes(
            Constants.NS_URI_SOAP_ENC,
            new org.apache.soap.util.xml.QName(
                Constants.NS_URI_2001_SCHEMA_XSD,
                "dateTime"),
            java.util.Date.class,
            null,
            dateSer);
        // Use current serializer for serialization
        smr.mapTypes(
            Constants.NS_URI_SOAP_ENC,
            new org.apache.soap.util.xml.QName(
                Constants.NS_URI_CURRENT_SCHEMA_XSD,
                "dateTime"),
            java.util.Date.class,
            dateSer,
            null);

        // Add mapping registry entry for base64
        Base64Serializer base64Ser = new Base64Serializer();
        // 1999 deserializer
        smr.mapTypes(
            Constants.NS_URI_SOAP_ENC,
            new org.apache.soap.util.xml.QName(
                Constants.NS_URI_1999_SCHEMA_XSD,
                "base64Binary"),
            byte[].class,
            null,
            base64Ser);
        // 2000 deserializer
        smr.mapTypes(
            Constants.NS_URI_SOAP_ENC,
            new org.apache.soap.util.xml.QName(
                Constants.NS_URI_2000_SCHEMA_XSD,
                "base64Binary"),
            byte[].class,
            null,
            base64Ser);
        // 2001 deserializer
        smr.mapTypes(
            Constants.NS_URI_SOAP_ENC,
            new org.apache.soap.util.xml.QName(
                Constants.NS_URI_2001_SCHEMA_XSD,
                "base64Binary"),
            byte[].class,
            null,
            base64Ser);
        // Use current serializer for serialization
        smr.mapTypes(
            Constants.NS_URI_SOAP_ENC,
            new org.apache.soap.util.xml.QName(
                Constants.NS_URI_CURRENT_SCHEMA_XSD,
                "base64Binary"),
            byte[].class,
            base64Ser,
            null);

        Trc.exit(smr);
        return smr;
    }

    public Call getCall() {
        Trc.entry(this);
        if ( call == null ) {
           call = new Call();
           call.setSOAPMappingRegistry(
               new WSIFSOAPMappingRegistry(createSOAPMappingRegistry( call )));
           prepareTypeMappings();
        }
        Trc.exit(call);
        return call;
    }

    public SOAPMappingRegistry getSOAPMappingRegistry() {
        Trc.entry(this);
        SOAPMappingRegistry smr = getCall().getSOAPMappingRegistry();
        Trc.exit(smr);
        return smr;
    }

    protected HashMap getLocalTypeMap() {
    	if (localTypeMap == null ) {
    		localTypeMap = new HashMap();
    		// need to ensure prepareTypeMappings is run
    	    getCall();
    	}
    	return localTypeMap;
    }

    private void prepareTypeMappings() {
    	// If getSOAPMappingRegistry() as been called direcly then there
    	// is a possibility that the local type map has not yet been
    	// initialized so check now.
    	if (localTypeMap == null ) {
    		localTypeMap = new HashMap();
    	}
        prepareTypeMappings(
            getSOAPMappingRegistry(),
            this.wsifTypeMap,
            partSerializerName,
            localTypeMap);
    }

    static void prepareTypeMappings(
        SOAPMappingRegistry theSMR,
        WSIFDynamicTypeMap theTypeMap,
        String thePartSerializerName,
        HashMap theLocalTypeMap) {
        	
        BeanSerializer beanSer = new BeanSerializer();
        PartSerializer partSer = null;
        if (thePartSerializerName != null)
            try {
                partSer =
                    (PartSerializer) Class
                        .forName(
                            thePartSerializerName,
                            true,
                            Thread.currentThread().getContextClassLoader())
                        .newInstance();
            } catch (Throwable ignored) {
	        	Trc.ignoredException(ignored);
            }

        Serializer literalSerializer = partSer;
        Deserializer literalDeserializer = partSer;
        Serializer soapSerializer = beanSer;
        Deserializer soapDeserializer = beanSer;
        String soapEncoding = Constants.NS_URI_SOAP_ENC;

        // initialize ApacheSOAP specific mappings here
        for (Iterator i = theTypeMap.iterator(); i.hasNext();) {
            WSIFDynamicTypeMapping mapping = (WSIFDynamicTypeMapping) i.next();

            Class javaClass = mapping.getJavaType();
            org.apache.soap.util.xml.QName typeName =
                new org.apache.soap.util.xml.QName(
                    mapping.getXmlType().getNamespaceURI(),
                    mapping.getXmlType().getLocalPart());
                    
            // Add mappings to a local hashmap for use in preparation of the operation
            theLocalTypeMap.put(typeName, javaClass);

            Serializer ser = null;
            // Set up SOAP encoding mappings
            try {
                ser = theSMR.querySerializer(javaClass, soapEncoding);
            } catch (IllegalArgumentException iae) {
	        	Trc.ignoredException(iae);
            }            
            // Only add a mapping if a serializer does not already exist
            if (ser == null) {
                theSMR.mapTypes(
                    soapEncoding,
                    typeName,
                    javaClass,
                    soapSerializer,
                    soapDeserializer);
            }
            
            // Set up literal encoding mappings
            try {
                ser = null;
                ser = theSMR.querySerializer(javaClass, LITERAL_ENCODING);
            } catch (IllegalArgumentException iae) {
	        	Trc.ignoredException(iae);
            }
            // Only add a mapping if a serializer does not already exist
            if (ser == null) {
                theSMR.mapTypes(
                    LITERAL_ENCODING,
                    typeName,
                    javaClass,
                    literalSerializer,
                    literalDeserializer);
            }
        }
    }

    public URL getEndPoint() {
        Trc.entry(this);
        Trc.exit(url);
        return url;
    }

    public void setEndPoint(URL url) {
        Trc.entry(this, url);
        this.url = url;
        Trc.exit();
    }

    public SOAPTransport getSOAPTransport() {
        Trc.entry(this);
        Trc.exit(st);
        return st;
    }

    public void setSOAPTransport(SOAPTransport st) {
        Trc.entry(this, st);
        this.st = st;
        Trc.exit();
    }

    // where is WSDL defining this abstract mesage
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

    public Port getPort() {
        Trc.entry(this);
        Trc.exit(port);
        return port;
    }

    public void setPort(Port value) {
        Trc.entry(this, value);
        port = value;
        Trc.exit();
    }

    // WSIF: keep list of operations available in this port
    public void setDynamicWSIFOperation(
        String name,
        String inputName,
        String outputName,
        WSIFOperation_ApacheSOAP value) {
        Trc.entry(this, name, inputName, outputName, value);
        operationInstances.put(getKey(name, inputName, outputName), value);
        Trc.exit();
    }

    public WSIFOperation createOperation(String operationName)
        throws WSIFException {
        Trc.entry(this, operationName);
        WSIFOperation wo = createOperation(operationName, null, null);
        Trc.exit(wo);
        return wo;
    }

    public WSIFOperation createOperation(
        String operationName,
        String inputName,
        String outputName)
        throws WSIFException {
        Trc.entry(this, operationName, inputName, outputName);
        WSIFOperation_ApacheSOAP op =
            getDynamicWSIFOperation(operationName, inputName, outputName);
        if (op == null) {
            throw new WSIFException(
                "Could not create operation: "
                    + operationName
                    + ":"
                    + inputName
                    + ":"
                    + outputName);
        }
        WSIFOperation wo = op.copy();
        Trc.exit(wo);
        return wo;
    }

    public WSIFOperation_ApacheSOAP getDynamicWSIFOperation(
        String name,
        String inputName,
        String outputName)
        throws WSIFException {
        Trc.entry(this, name, inputName, outputName);

        WSIFOperation_ApacheSOAP tempOp =
            (WSIFOperation_ApacheSOAP) operationInstances.get(
                getKey(name, inputName, outputName));

        WSIFOperation_ApacheSOAP operation = null;
		if (tempOp != null) {
			operation = tempOp.copy();
		}
		
        if (operation == null) {
            BindingOperation bop =
               WSIFUtils.getBindingOperation( 
                  port.getBinding(), name, inputName, outputName );

            if (bop != null) {
                operation =
                    new WSIFOperation_ApacheSOAP(
                        this,
                        bop.getOperation(),
                        wsifTypeMap);
                if (operation == null) {
                    throw new WSIFException(
                        "Operation not found from binding operation: "
                            + bop.getName());
                }

                operation.setStyle(this.style);
                operation.setPartSerializerName(this.partSerializerName);

                if (jmsAddressPropVals != null
                    && jmsAddressPropVals.size() > 0) {
                    if (st instanceof SOAPJMSConnection)
                        operation.addInputJmsPropertyValues(jmsAddressPropVals);
                    else
                        throw new WSIFException("jms:propertyValue found in non-jms address");
                }

                // get soapActionURI and style from soap:operation
                SOAPOperation soapOperation =
                    (SOAPOperation) getExtElem(bop,
                        SOAPOperation.class,
                        bop.getExtensibilityElements());
                if (soapOperation == null) {
                    throw new WSIFException(
                        "soapAction must be specified in "
                            + " required by WSDL 1.1 soap:operation binding for "
                            + bop.getName());
                }
                String soapActionURI = soapOperation.getSoapActionURI();
                operation.setSoapActionURI(soapActionURI);

                Trc.event(
                    this,
                    "setting actionURI ",
                    soapActionURI,
                    " for op ",
                    operation.getName());
                String opStyle = soapOperation.getStyle();

                // try to get soap:body for input message
                BindingInput binpt = bop.getBindingInput();
                SOAPBody soapInputBody =
                    (SOAPBody) getExtElem(binpt,
                        SOAPBody.class,
                        binpt.getExtensibilityElements());
                if (soapInputBody != null) {
                    String namespaceURI = soapInputBody.getNamespaceURI();

                    Trc.event(
                        this,
                        "setting namespace ",
                        namespaceURI,
                        " for op ",
                        operation.getName());

                    operation.setInputNamespace(namespaceURI);
                    String use = soapInputBody.getUse();
                    operation.setInputUse(use);

                    List encodingStyles = soapInputBody.getEncodingStyles();
                    if (encodingStyles != null) {
                        if (encodingStyles.size() == 0) {
                        }
                        operation.setInputEncodingStyle(
                            (String) encodingStyles.getFirst());
                        // quietly ignore if encodingStyles.size() > 1 ...
                    }
                    List parts = soapInputBody.getParts();
                    if (parts != null)
                        operation.setPartNames(parts);
                }

                SOAPHeader soapHeader =
                    (SOAPHeader) getExtElem(binpt,
                        SOAPHeader.class,
                        binpt.getExtensibilityElements());
                if (soapHeader != null) {
                    throw new WSIFException(
                        "not supported input soap:header " + soapHeader);
                }

                List inJmsProps =
                    getExtElems(
                        binpt,
                        JMSProperty.class,
                        binpt.getExtensibilityElements());
                if (inJmsProps != null && inJmsProps.size() > 0) {
                    if (st instanceof SOAPJMSConnection)
                        operation.setInputJmsProperties(inJmsProps);
                    else
                        throw new WSIFException("jms:properties found in non-jms binding");
                }

                List inJmsPropVals =
                    getExtElems(
                        binpt,
                        JMSPropertyValue.class,
                        binpt.getExtensibilityElements());
                if (inJmsPropVals != null && inJmsPropVals.size() > 0) {
                    if (st instanceof SOAPJMSConnection)
                        operation.addInputJmsPropertyValues(inJmsPropVals);
                    else
                        throw new WSIFException("jms:propertyValue found in non-jms binding");
                }

                // try to get soap:body for output message
                BindingOutput boutpt = bop.getBindingOutput();
                if (boutpt != null) {
                    SOAPBody soapOutputBody =
                        (SOAPBody) getExtElem(boutpt,
                            SOAPBody.class,
                            boutpt.getExtensibilityElements());
                    if (soapOutputBody != null) {
                        // NOTE: element ignored
                        //String namespaceURI = soapOutputBody.getNamespaceURI();
                        String use = soapOutputBody.getUse();
                        operation.setOutputUse(use);

                        // NOTE: element ignored
                        //List encodingStyles = soapInputBody.getEncodingStyles();
                        List parts = soapOutputBody.getParts();
                        if (parts != null && parts.size() > 0) {
                            operation.setReturnName((String) parts.getFirst());
                        }
                    }
                    soapHeader =
                        (SOAPHeader) getExtElem(boutpt,
                            SOAPHeader.class,
                            boutpt.getExtensibilityElements());
                    if (soapHeader != null) {
                        throw new WSIFException(
                            "not supported output soap:header " + soapHeader);
                    }

                    List outJmsProps =
                        getExtElems(
                            boutpt,
                            JMSProperty.class,
                            boutpt.getExtensibilityElements());
                    if (outJmsProps != null && outJmsProps.size() > 0) {
                        if (st instanceof SOAPJMSConnection)
                            operation.setOutputJmsProperties(outJmsProps);
                        else
                            throw new WSIFException("jms:properties found in non-jms binding");
                    }
                }

                // Only now the operation has enough information to initialize itself
                //        operation.prepare();

                // make this operation instance accessible
                setDynamicWSIFOperation(name, inputName, outputName, operation);
            }
        }

        Trc.exit(operation);
        return operation;
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
     * Closes the port. All methods are invalid after calling this method.
     */
    public void close() throws WSIFException {
        Trc.entry(this);
        if (st != null && st instanceof SOAPJMSConnection connection)
             connection.close();
        Trc.exit();
    }

    /**
     * Tests if this port supports asynchronous calls to operations.
     * 
     * @return true if the port is using a JMS transport, otherwise false
     */
    public boolean supportsAsync() {
        if (st instanceof SOAPJMSConnection) {
            return true;
        } else {
            return false;
        }
    }

    public String deep() {
        String buff = "";
        try {
            buff = new String(super.toString() + ":\n");

            if (operationInstances == null) {
                buff += " operationInstances: null";
            } else {
                buff += " operationInstances: size:"
                    + operationInstances.size();
                Iterator it = operationInstances.keySet().iterator();
                int i = 0;
                while (it.hasNext()) {
                    String key = (String) it.next();
                    WSIFOperation_ApacheSOAP woas =
                        (WSIFOperation_ApacheSOAP) operationInstances.get(key);
                    buff += "\noperationInstances["
                        + i
                        + "]:"
                        + key
                        + " "
                        + woas.getName()
                        + " ";
                    i++;
                }
            }

            buff += "\nport:" + Trc.brief(port);
            buff += " definition:" + Trc.brief(definition);
            buff += " call:" + call;
            buff += " soapTransport:" + st;
            buff += " url:" + url;
            buff += " style:" + style;
            buff += " partSerializerName:" + partSerializerName;
            buff += " wsifTypeMap:" + wsifTypeMap;
            buff += " jmsAddressPropVals:" + jmsAddressPropVals;
        } catch (Exception e) {
            Trc.exceptionInTrace(e);
        }

        return buff;
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }

    private void readObject(ObjectInputStream ois)
        throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
                
        // reset the operation instances
        operationInstances = new HashMap();
        
        // The transient call reference does not need to be re-established here since it will
        // be taken care of when getCall is next invoked. However, the SOAPTransport needs to
        // be set again
        JMSAddress ja =
            (JMSAddress) getExtElem(port,
                JMSAddress.class,
                port.getExtensibilityElements());
        SOAPAddress sa =
            (SOAPAddress) getExtElem(port,
                SOAPAddress.class,
                port.getExtensibilityElements());

        if (sa != null && ja != null)
            throw new WSIFException(
                "Both soap:address and jms:address cannot be specified for port "
                    + port.getName());

        if (sa == null && ja == null)
            throw new WSIFException(
                "Either soap:address or jms:address must be specified for port "
                    + port.getName());

        if (ja != null) {
            // Port jms:address binding element
            jmsAddressPropVals = ja.getJMSPropertyValues();
            st = new SOAPJMSConnection(ja, port.getName());
        } else {
            // Port soap:address bindng element
            st = new SOAPHTTPConnection();
        }        
    }   
}
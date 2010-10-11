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

import java.util.ArrayList;
import java.util.Iterator;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Message;

import org.apache.wsif.WSIFException;
import org.apache.wsif.base.WSIFDefaultMessage;
import org.apache.wsif.format.jms.JMSFormatHandler;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.util.WSIFUtils;
import org.apache.wsif.wsdl.extensions.format.TypeMapping;
import org.apache.wsif.wsdl.extensions.jms.JMSBinding;
import org.apache.wsif.wsdl.extensions.jms.JMSConstants;
/**
 * JMSMessage 
 * 
 * @author <a href="mailto:seto@ca.ibm.com">Norman Seto</a>
 * @author <a href="mailto:antelder@apache.org">Ant Elder</a>
 */
public class JMSMessage extends WSIFDefaultMessage {
	private static final long serialVersionUID = 1L;

    // FIXME Derive these constants from Format Binding?
    private static String XML_ENCODING = "XML";
    private static String JAVA_ENCODING = "Java";

    static String XML_SCHEMA_1999 = "http://www.w3.org/1999/XMLSchema";
    static String XML_SCHEMA_2000_10 = "http://www.w3.org/2000/10/XMLSchema";
    static String XML_SCHEMA_2001 = "http://www.w3.org/2001/XMLSchema";

    static final java.util.HashMap PRIMITIVE_JAVA_MAPPING =
        new java.util.HashMap();

    // Maps the XSD Type to the equivalent Java class
    // Missing support for anyType	
    static {
        PRIMITIVE_JAVA_MAPPING.put("anySimpleType", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put("anyURI", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put("base64Binary", byte[].class);
        PRIMITIVE_JAVA_MAPPING.put("boolean", java.lang.Boolean.class);
        PRIMITIVE_JAVA_MAPPING.put("byte", java.lang.Byte.class);
        PRIMITIVE_JAVA_MAPPING.put("date", java.util.GregorianCalendar.class);
        PRIMITIVE_JAVA_MAPPING.put("dateTime", java.util.Date.class);
        PRIMITIVE_JAVA_MAPPING.put("decimal", java.math.BigDecimal.class);
        PRIMITIVE_JAVA_MAPPING.put("double", java.lang.Double.class);
        PRIMITIVE_JAVA_MAPPING.put("duration", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put("ENTITIES", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put("ENTITY", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put("float", java.lang.Float.class);
        PRIMITIVE_JAVA_MAPPING.put("gDay", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put("gMonth", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put("gMonthDay", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put("gYear", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put("gYearMonth", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put("hexBinary", byte[].class);
        PRIMITIVE_JAVA_MAPPING.put("ID", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put("IDREF", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put("IDREFS", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put("int", java.lang.Integer.class);
        PRIMITIVE_JAVA_MAPPING.put("integer", java.math.BigInteger.class);
        PRIMITIVE_JAVA_MAPPING.put("language", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put("long", java.lang.Long.class);
        PRIMITIVE_JAVA_MAPPING.put("Name", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put("NCName", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put(
            "negativeInteger",
            java.math.BigInteger.class);
        PRIMITIVE_JAVA_MAPPING.put("NMTOKEN", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put("NMTOKENS", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put(
            "nonNegativeInteger",
            java.math.BigInteger.class);
        PRIMITIVE_JAVA_MAPPING.put(
            "nonPositiveInteger",
            java.math.BigInteger.class);
        PRIMITIVE_JAVA_MAPPING.put("normalizedString", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put("NOTATION", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put(
            "positiveInteger",
            java.math.BigInteger.class);
        PRIMITIVE_JAVA_MAPPING.put("QName", javax.xml.namespace.QName.class);
        PRIMITIVE_JAVA_MAPPING.put("short", java.lang.Short.class);
        PRIMITIVE_JAVA_MAPPING.put("string", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put("time", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put("token", java.lang.String.class);
        PRIMITIVE_JAVA_MAPPING.put("unsignedByte", java.lang.Short.class);
        PRIMITIVE_JAVA_MAPPING.put("unsignedInt", java.lang.Long.class);
        PRIMITIVE_JAVA_MAPPING.put("unsignedLong", java.math.BigInteger.class);
        PRIMITIVE_JAVA_MAPPING.put("unsignedShort", java.lang.Integer.class);
    }

    private Definition fieldDefinitionModel;
    private Binding fieldBindingModel;
    private Message fieldMessageModel;
    private java.util.List fieldMessageParts;

    /**
     * Constructor for WSIFMessageFormatHandlerImpl
     */
    public JMSMessage(
        Definition definitionModel,
        Binding bindingModel,
        Message messageModel,
        java.util.List parts) {
        super();
        Trc.entry(this, definitionModel, bindingModel, messageModel, parts);
        fieldDefinitionModel = definitionModel;
        fieldBindingModel = bindingModel;
        fieldMessageModel = messageModel;
        fieldMessageParts = parts;
        Trc.exit();
    }

    /**
     * @see JMSFormatHandler#write(Message)
     */
    public void write(javax.jms.Message message) throws WSIFException {
        Trc.entry(this, message);

        if (!isCorrectMessageType(message))
            throw new WSIFException("Incorrect message type");

        if (message instanceof javax.jms.TextMessage)
            write((javax.jms.TextMessage) message);
        else if (message instanceof javax.jms.ObjectMessage)
            write((javax.jms.ObjectMessage) message);
        else
            throw new WSIFException(
                "Unsupported Message Type: " + message.getClass().getName());
        Trc.exit();
    }

    /**
     * Writes the JMS TextMessage
     */
    private void write(javax.jms.TextMessage message) throws WSIFException {
        Trc.entry(this, message);

        // Need to determine the format type mapping 
        if (!XML_ENCODING.equals(getFormatEncoding(fieldBindingModel)))
            throw new WSIFException("Unable to support non XML encodings in a JMS Text Message");

        try {
            ArrayList al = new ArrayList();
            for (Iterator i = this.getPartNames(); i.hasNext();) {
                al.add(i.next());
            }
            String[] partNames = (String[]) al.toArray(new String[al.size()]);

            if (partNames.length == 1) {
                // If there is only one part, set it into the message as is
                String partName = partNames[0];

                javax.wsdl.Part partModel = fieldMessageModel.getPart(partName);

                javax.xml.namespace.QName partQName =
                    partModel.getElementName() != null
                        ? partModel.getElementName()
                        : partModel.getTypeName();
                	
                JMSFormatHandler fh = getFormatHandler(partName);

                if (fh != null) {
                    fh.setPartQName(partQName);
                    fh.setObjectPart(parts.get(partName));
                    fh.write(message);
                } else {
                    // No format handler
                    Object part = parts.get(partName);
                    message.setText(part.toString());
                }
            } else {
                // Use my own XML Structure	
                // Prepare the stream writers and the serializers
                java.io.ByteArrayOutputStream os =
                    new java.io.ByteArrayOutputStream();
                java.io.OutputStreamWriter writer =
                    new java.io.OutputStreamWriter(os);

                org.apache.xml.serialize.OutputFormat format =
                    new org.apache.xml.serialize.OutputFormat();
                org.apache.xml.serialize.XMLSerializer serializer =
                    new org.apache.xml.serialize.XMLSerializer(writer, format);

                String namespace = "";

                // Start the document			
                serializer.startDocument();

                // Add the message tag
                serializer.startElement(
                    namespace,
                    fieldMessageModel.getQName().getLocalPart(),
                    "",
                    new org.xml.sax.helpers.AttributesImpl());

                // Cycle the parts, invoking each format handler
                for (int i = 0; i < partNames.length; i++) {
                    String partName = partNames[i];

                    // Add a beginning tag
                    serializer.startElement(
                        namespace,
                        partName,
                        "",
                        new org.xml.sax.helpers.AttributesImpl());

                    javax.wsdl.Part partModel =
                        fieldMessageModel.getPart(partName);
                    
                    javax.xml.namespace.QName partQName =
                        partModel.getElementName() != null
                            ? partModel.getElementName()
                            : partModel.getTypeName();
                    
                    // Determine the format handler
                    JMSFormatHandler fh = getFormatHandler(partName);

                    if (fh != null) {
                        fh.setPartQName(partQName);
                        fh.setObjectPart(parts.get(partName));

                        // Send the message since it is the native format
                        // Reinitialize the message content
                        message.setText("");
                        fh.write(message);
                        char[] c = message.getText().toCharArray();
                        serializer.characters(c, 0, c.length);
                    } else {
                        // No format handler
                        Object part = parts.get(partName);
                        char[] c = part.toString().toCharArray();
                        serializer.characters(c, 0, c.length);
                    }

                    // Add the end tag
                    serializer.endElement(partName);
                }

                // Add the end message tag
                serializer.endElement(
                    fieldMessageModel.getQName().getLocalPart());

                // End the document
                serializer.endDocument();

                writer.flush();
                String msgContents = os.toString();
                // Put contents into the message
                message.setText(msgContents);
            }
            Trc.event(this, message.getText());
    
        } catch (JMSException e) {
            Trc.exception(e);
            throw new WSIFException("Error in write.", e);
        } catch (java.io.IOException e) {
            Trc.exception(e);
            throw new WSIFException("Error in write.", e);
        } catch (org.xml.sax.SAXException e) {
            Trc.exception(e);
            throw new WSIFException("Error in write.", e);
        }
        Trc.exit();
    }

    /**
     * Writes the JMS ObjectMessage
     */
    private void write(javax.jms.ObjectMessage message) throws WSIFException {
        Trc.entry(this, message);

        // Need to determine the format type mapping 
        if (!JAVA_ENCODING.equals(getFormatEncoding(fieldBindingModel)))
            throw new WSIFException("Unable to support non Java encodings in a JMS Object Message");

        try {

            ArrayList al = new ArrayList();
            for (Iterator i = this.getPartNames(); i.hasNext();) {
                al.add(i.next());
            }
            String[] partNames = (String[]) al.toArray(new String[al.size()]);

            if (partNames.length == 1) {
                // If there is only one part, set it into the message as is
                String partName = partNames[0];

                javax.wsdl.Part partModel = fieldMessageModel.getPart(partName);

                javax.xml.namespace.QName partQName =
                    partModel.getElementName() != null
                        ? partModel.getElementName()
                        : partModel.getTypeName();

                JMSFormatHandler fh = getFormatHandler(partName);

                if (fh != null) {
                    fh.setPartQName(partQName);
                    fh.setObjectPart(parts.get(partName));
                    fh.write(message);
                } else {
                    // No format handler
                    Object part = parts.get(partName);
                    // Check to see if it is Serializable.
                    // If so, serialize it 
                    try {
                        message.setObject((java.io.Serializable) part);
                    } catch (ClassCastException e) {
                        Trc.exception(e);
                        throw new WSIFException("Unable to serialize a part");
                    }
                }
            } else {
                // Use a hash map to hold the objects
                java.util.HashMap result = new java.util.HashMap();
                for (int i = 0; i < partNames.length; i++) {
                    String partName = partNames[i];

                    javax.wsdl.Part partModel =
                        fieldMessageModel.getPart(partName);

                    javax.xml.namespace.QName partQName =
                        partModel.getElementName() != null
                            ? partModel.getElementName()
                            : partModel.getTypeName();

                    JMSFormatHandler fh = getFormatHandler(partName);

                    if (fh != null) {
                        fh.setPartQName(partQName);
                        fh.setObjectPart(parts.get(partName));
                        fh.write(message);
                        result.put(partName, message.getObject());
                    } else {
                        // No format handler
                        Object part = parts.get(partName);
                        // Check to see if it is Serializable.
                        // If so, serialize it 
                        try {
                            result.put(partName, (java.io.Serializable) part);
                        } catch (ClassCastException e) {
                            Trc.exception(e);
                            throw new WSIFException("Unable to serialize a part");
                        }
                    }
                }
                message.setObject(result);
            }
            Trc.event(this, message.getObject());

        } catch (JMSException e) {
            Trc.exception(e);
            throw new WSIFException("Error in write.", e);
        }
        Trc.exit();
    }

    /**
     * @see JMSFormatHandler#read(Message)
     */
    public void read(javax.jms.Message message) throws WSIFException {
        Trc.entry(this, message);

        if (!isCorrectMessageType(message))
            throw new WSIFException("Incorrect message type");

        if (message instanceof javax.jms.TextMessage)
            read((javax.jms.TextMessage) message);
        else if (message instanceof javax.jms.ObjectMessage)
            read((javax.jms.ObjectMessage) message);
        else
            throw new WSIFException(
                "Unsupported Message Type: " + message.getClass().getName());
        Trc.exit();
    }

    /**
     * Reads the JMS TextMessage
     */
    private void read(javax.jms.TextMessage message) throws WSIFException {
        Trc.entry(this, message);

        // Need to determine the format type mapping 
        if (!XML_ENCODING.equals(getFormatEncoding(fieldBindingModel)))
            throw new WSIFException("Unable to support non XML encodings in a JMS Text Message");

        boolean wsifFormat = false;

        try {
        	Trc.event(this, message.getText());

            // Read the text into the document
            javax.xml.parsers.DocumentBuilderFactory factory =
                javax.xml.parsers.DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);

            javax.xml.parsers.DocumentBuilder builder =
                factory.newDocumentBuilder();
            builder.setErrorHandler(null);

            String text = message.getText();
            java.io.ByteArrayInputStream is =
                new java.io.ByteArrayInputStream(text.getBytes());
            org.w3c.dom.Document doc = builder.parse(is);

            // Check to see if the document element is the message
            if (fieldMessageModel
                .getQName()
                .getLocalPart()
                .equals(doc.getDocumentElement().getLocalName())) {

                wsifFormat = true;

                // Need to make the message mutable
                message.clearBody();

                // Parse the document ignoring the document element which should be the message name
                // The contents should represent the parts as elements
                for (org.w3c.dom.Node n =
                    doc.getDocumentElement().getFirstChild();
                    n != null;
                    n = n.getNextSibling()) {
                    if (n.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        String partName = n.getLocalName();

                        java.io.ByteArrayOutputStream os =
                            new java.io.ByteArrayOutputStream();

                        org.apache.xml.serialize.OutputFormat format =
                            new org.apache.xml.serialize.OutputFormat();
                        org.apache.xml.serialize.TextSerializer serializer =
                            new org.apache.xml.serialize.TextSerializer();
                        serializer.setOutputFormat(format);
                        serializer.setOutputByteStream(os);

                        format.setOmitXMLDeclaration(true);
                        //format.setOmitDocumentType(true);

                        serializer.serialize((org.w3c.dom.Element) n);
                        os.flush();
                        // Get the contents
                        String partText = os.toString();

                        javax.wsdl.Part partModel =
                            fieldMessageModel.getPart(partName);
                        JMSFormatHandler fh = getFormatHandler(partName);

                        javax.xml.namespace.QName partQName =
                            partModel.getElementName() != null
                                ? partModel.getElementName()
                                : partModel.getTypeName();

                        if (fh != null) {

							fh.setPartQName(partQName);
							
                            message.setText(partText);
                            fh.read(message);
                            //fh.setObjectPart(n);

                            setObjectPart(partName, fh.getObjectPart());

                            // ?? Do I want to store the format handler instead???
                            //partToFHMap.put(partKey, formatHandler);

                        } else {
                            // No format handler - pass the part contents directly
                            setObjectPart(partName, partText);
                        }
                    }
                }

                // Reset the message to original text
                message.setText(text);
            }
        } catch (JMSException e) {
            Trc.exception(e);
            throw new WSIFException("Error in read.", e);
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            Trc.exception(e);
            throw new WSIFException("Error in read.", e);
        } catch (Exception e) {
            Trc.exception(e);
            // For all other exceptions ignore since it is likely due to parsing of a non-XML document
        }

        try {
            if (!wsifFormat) {
                // Unknown format - either XML or Text
                // Pass the contents of the message to each part of the message model 
                Object[] partNames =
                    fieldMessageParts != null
                        ? fieldMessageParts.toArray()
                        : fieldMessageModel.getParts().keySet().toArray();

                // should only be one part
                if (partNames.length != 1)
                    throw new WSIFException(
                        "There should only be one part defined in "
                            + fieldMessageModel.getQName().getLocalPart());

                String partName = partNames[0].toString();

                javax.wsdl.Part partModel = fieldMessageModel.getPart(partName);
                JMSFormatHandler fh = getFormatHandler(partName);

                javax.xml.namespace.QName partQName =
                    partModel.getElementName() != null
                        ? partModel.getElementName()
                        : partModel.getTypeName();

                if (fh != null) {
                    fh.setPartQName(partQName);
                    fh.read(message);

                    setObjectPart(partName, fh.getObjectPart());

                    // ?? Do I want to store the format handler instead???
                    //partToFHMap.put(partKey, formatHandler);

                } else {
                    // No format handler - pass the part contents directly
                    setObjectPart(partName, message.getText());
                }
            }
        } catch (JMSException e) {
            Trc.exception(e);
            throw new WSIFException("Error in read.", e);
        }
        Trc.exit();
    }

    /**
     * Reads the JMS ObjectMessage
     */
    private void read(javax.jms.ObjectMessage message) throws WSIFException {
        Trc.entry(this, message);

        // Need to determine the format type mapping 
        if (!JAVA_ENCODING.equals(getFormatEncoding(fieldBindingModel)))
            throw new WSIFException("Unable to support non Java encodings in a JMS Object Message");

        try {
            Trc.event(this, message.getObject());
            Object object = message.getObject();
            Object[] partNames =
                fieldMessageParts != null
                    ? fieldMessageParts.toArray()
                    : fieldMessageModel.getParts().keySet().toArray();

            // Check to see if there are any parts
            if (partNames.length == 0)
                return;

            // Check to see if it is a known format
            if (object instanceof java.util.Map) {
                // Need to make the message mutable			
                message.clearBody();

                java.util.Map map = (java.util.Map) object;

                // Cycle through the parts of the model
                for (int i = 0; i < partNames.length; i++) {
                    String partName = partNames[i].toString();

                    if (map.containsKey(partName)) {
                        javax.wsdl.Part partModel =
                            fieldMessageModel.getPart(partName);
                        JMSFormatHandler fh = getFormatHandler(partName);

                        javax.xml.namespace.QName partQName =
                            partModel.getElementName() != null
                                ? partModel.getElementName()
                                : partModel.getTypeName();

                        if (fh != null) {
							fh.setPartQName(partQName);
							
                            // Should be serializable since retrieved it over the wire
                            message.setObject(
                                (java.io.Serializable) map.get(partName));
                            fh.read(message);
                            //fh.setObjectPart(map.get(partName));
                            setObjectPart(partName, fh.getObjectPart());

                            // ?? Do I want to store the format handler instead???
                            //partToFHMap.put(partKey, formatHandler);

                        } else {
                            // No format handler defined
                            setObjectPart(partName, map.get(partName));
                        }
                    }

                }
                // Reset the message to original text
                message.setObject((java.io.Serializable) object);
            } else {
                // It is an unknown format
                // Pass the contents of the message to each part of the message model 

                // should only be one part
                if (partNames.length != 1)
                    throw new WSIFException(
                        "There should only be one part defined in "
                            + fieldMessageModel.getQName().getLocalPart()
                            + " or the JMS ObjectMessage should be a Map of "
                            + "all the parts in the message");

                String partName = partNames[0].toString();

                javax.wsdl.Part partModel = fieldMessageModel.getPart(partName);
                JMSFormatHandler fh = getFormatHandler(partName);

                javax.xml.namespace.QName partQName =
                    partModel.getElementName() != null
                        ? partModel.getElementName()
                        : partModel.getTypeName();

                if (fh != null) {
                	fh.setPartQName(partQName);
                    fh.read(message);

                    setObjectPart(partName, fh.getObjectPart());

                    // ?? Do I want to store the format handler instead???
                    //partToFHMap.put(partKey, formatHandler);

                } else {
                    // No format handler defined
                    setObjectPart(partName, message.getObject());
                }

            }
        } catch (JMSException e) {
            Trc.exception(e);
            throw new WSIFException("Error in read.", e);
        }
        Trc.exit();
    }

    // Move to WSIF utils ??
    private static String getFormatEncoding(Binding bindingModel) {
        Trc.entry(null, bindingModel);

        java.util.Iterator iterator =
            bindingModel.getExtensibilityElements().iterator();

        while (iterator.hasNext()) {
            javax.wsdl.extensions.ExtensibilityElement ee =
                (javax.wsdl.extensions.ExtensibilityElement) iterator.next();
            if (ee instanceof TypeMapping) {
                TypeMapping typeMapping = (TypeMapping) ee;
                String s = typeMapping.getEncoding();
                Trc.exit(s);
                return s;
            }
        }

        Trc.exit(null);
        return null;
    }

    private JMSFormatHandler getFormatHandler(String partName) {
        Trc.entry(this, partName);

        javax.wsdl.Part partModel = fieldMessageModel.getPart(partName);
        JMSFormatHandler fh = null;

        javax.xml.namespace.QName partType = partModel.getTypeName();
        if (partType == null)
            partType = partModel.getElementName();

        try {
            // no type exists or it is not a XSD primitive type
            if (isSchemaNamespace(partType.getNamespaceURI())
                && isXSDPrimitiveType(partType.getLocalPart())) {
                fh =
                    new PrimitiveTypeFormatHandler(
                        (Class) PRIMITIVE_JAVA_MAPPING.get(
                            partType.getLocalPart().toLowerCase()));
            } else {
                //antxxx  fh = org.apache.wsif.jca.util.JCAUtil.getFormatHandler(
                fh =
                    (JMSFormatHandler) WSIFUtils.getFormatHandler(
                        partModel,
                        this.fieldDefinitionModel,
                        this.fieldBindingModel);
            }
        } catch (java.lang.Exception e) {
            Trc.exception(e);
        }

        Trc.exit(fh);
        return fh;
    }

    /**
     * Returns true if the namespace is one of the existing XML Schema namespaces
     */
    static boolean isSchemaNamespace(String namespaceURI) {
        return XML_SCHEMA_1999.equals(namespaceURI)
            || XML_SCHEMA_2000_10.equals(namespaceURI)
            || XML_SCHEMA_2001.equals(namespaceURI);
    }

    static boolean isXSDPrimitiveType(String type) {
        Trc.entry(null, type);

        Object[] types = PRIMITIVE_JAVA_MAPPING.keySet().toArray();
        for (int i = 0; i < types.length; i++) {
            if (types[i].toString().equalsIgnoreCase(type)) {
                Trc.exit(true);
                return true;
            }
        }
        Trc.exit(false);
        return false;
    }

    private int getMessageType() {
        Trc.entry(this);

        java.util.Iterator iterator =
            fieldBindingModel.getExtensibilityElements().iterator();

        while (iterator.hasNext()) {
            javax.wsdl.extensions.ExtensibilityElement ee =
                (javax.wsdl.extensions.ExtensibilityElement) iterator.next();
            if (ee instanceof JMSBinding) {
                JMSBinding jmsBinding = (JMSBinding) ee;
                int type = jmsBinding.getJmsMessageType();
                Trc.exit(type);
                return type;
            }
        }

        int type =
            org
                .apache
                .wsif
                .wsdl
                .extensions
                .jms
                .JMSConstants
                .MESSAGE_TYPE_NOTSET;
        Trc.exit(type);
        return type;
    }

    private boolean isCorrectMessageType(javax.jms.Message message) {
        Trc.entry(this, message);
        int type = getMessageType();

        boolean result =
            (message instanceof TextMessage
                && type == JMSConstants.MESSAGE_TYPE_TEXTMESSAGE)
                || (message instanceof ObjectMessage
                    && type == JMSConstants.MESSAGE_TYPE_OBJECTMESSAGE)
                || (message instanceof javax.jms.StreamMessage
                    && type == JMSConstants.MESSAGE_TYPE_STREAMMESSAGE)
                || (message instanceof javax.jms.BytesMessage
                    && type == JMSConstants.MESSAGE_TYPE_BYTEMESSAGE)
                || (message instanceof javax.jms.MapMessage
                    && type == JMSConstants.MESSAGE_TYPE_MAPMESSAGE);

        Trc.exit(result);
        return result;
    }
}

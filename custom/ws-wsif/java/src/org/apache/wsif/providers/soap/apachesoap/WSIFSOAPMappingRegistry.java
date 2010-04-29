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

import java.math.BigDecimal;

import org.apache.soap.Constants;
import org.apache.soap.encoding.SOAPMappingRegistry;
import org.apache.soap.util.xml.Deserializer;
import org.apache.soap.util.xml.QName;
import org.apache.soap.util.xml.Serializer;
import org.apache.wsif.WSIFConstants;
import org.apache.wsif.logging.Trc;

/**
 * An extension of SOAPMappingRegistry which can handle SOAP-ENC simple types
 * @author Owen Burroughs <owenb@apache.org>
 */
public class WSIFSOAPMappingRegistry extends SOAPMappingRegistry {

    /**
     * Constructor for WSIFSOAPMappingRegistry
     */
    public WSIFSOAPMappingRegistry() {
        super();
        Trc.entry(this);
        Trc.exit();
    }

    /**
     * Constructor for WSIFSOAPMappingRegistry
     * @param registry The parent SOAP mapping registry      
     */
    public WSIFSOAPMappingRegistry(SOAPMappingRegistry registry) {
        super(registry);
        Trc.entry(this, registry);
        Trc.exit();
    }

    /**
     * Constructor for WSIFSOAPMappingRegistry
     * @param registry The parent SOAP mapping registry
     * @param schemaURI The namespace URI of XSD to be used for serializers.
     */
    public WSIFSOAPMappingRegistry(
        SOAPMappingRegistry registry,
        String schemaURI) {
        super(registry, schemaURI);
        Trc.entry(this, registry, schemaURI);
        Trc.exit();
    }

    /**
     * Get the serializer for the specified class and encoding
     * @param javaType The class to serialize
     * @param encodingStyleURI The namespace of the encoding 
     * @return The serializer if found or null otherwise 
     */
    protected Serializer querySerializer_(
        Class javaType,
        String encodingStyleURI) {
        Trc.entry(this, javaType, encodingStyleURI);
        Serializer ser = super.querySerializer_(javaType, encodingStyleURI);
        Trc.exit(ser);
        return ser;
    }

    /**
     * Get the deserializer for the specified element and encoding 
     * @param elementType The element to deserialize
     * @param encodingStyleURI The namespace of the encoding
     * @return The deserializer if found or null otherwise 
     */
    protected Deserializer queryDeserializer_(
        QName elementType,
        String encodingStyleURI) {
        Trc.entry(this, elementType, encodingStyleURI);
        Deserializer deser = null;
        if (elementType != null
            && elementType.getNamespaceURI().equals(
                WSIFConstants.NS_URI_SOAP_ENC)) {
            QName qn = getEquivalentXSDSimpleType(elementType);
            if (qn != null) {
            	deser = super.queryDeserializer_(qn, encodingStyleURI);
            }
        }
        if (deser == null) {
            deser = super.queryDeserializer_(elementType, encodingStyleURI);
        }
        Trc.exit(deser);
        return deser;
    }

    /**
     * Find the element type that is mapped to the given Java class
     * @param javaType The Java class
     * @param encodingStyleURI The namespace of the encoding 
     * @return The corresponding element type or null is no mapping found
     */
    protected QName queryElementType_(
        Class javaType,
        String encodingStyleURI) {
        return super.queryElementType_(javaType, encodingStyleURI);
    }

    /**
     * Find the Java class that is mapped to the given element type
     * @param elementType The element type
     * @param encodingStyleURI The namespace of the encoding 
     * @return The corresponding Java class or null is no mapping found
     */
    protected Class queryJavaType_(
        QName elementType,
        String encodingStyleURI) {
        Class cls = null;
        if (elementType != null
            && elementType.getNamespaceURI().equals(
                WSIFConstants.NS_URI_SOAP_ENC)) {
            cls = resolveSOAPENCSimpleType(elementType);
        }
        if (cls == null) {
        	return super.queryJavaType_(elementType, encodingStyleURI);
        }
        return cls;
    }
    
    private QName getEquivalentXSDSimpleType(QName elementType) {
    	if (elementType == null
            || !elementType.getNamespaceURI().equals(
                WSIFConstants.NS_URI_SOAP_ENC)) {
            return null;    	
        }
        
        String lp = elementType.getLocalPart();
        if (lp == null) {
        	return null;
        }
        
        if (lp.equals("string")) {
        	return new QName(WSIFConstants.NS_URI_2001_SCHEMA_XSD, "string");
        } else if (lp.equals("boolean")) {
        	return new QName(WSIFConstants.NS_URI_2001_SCHEMA_XSD, "boolean");
        } else if (lp.equals("float")) {
        	return new QName(WSIFConstants.NS_URI_2001_SCHEMA_XSD, "float");
        } else if (lp.equals("double")) {
        	return new QName(WSIFConstants.NS_URI_2001_SCHEMA_XSD, "double");
        } else if (lp.equals("decimal")) {
        	return new QName(WSIFConstants.NS_URI_2001_SCHEMA_XSD, "decimal");
        } else if (lp.equals("int")) {
        	return new QName(WSIFConstants.NS_URI_2001_SCHEMA_XSD, "int");
        } else if (lp.equals("short")) {
        	return new QName(WSIFConstants.NS_URI_2001_SCHEMA_XSD, "short");
        } else if (lp.equals("byte")) {
        	return new QName(WSIFConstants.NS_URI_2001_SCHEMA_XSD, "byte");
        } else if (lp.equals("base64")) {
        	return new QName(WSIFConstants.NS_URI_2001_SCHEMA_XSD, "base64Binary");
        } else {
        	return null;
        }       
    }
    
    private Class resolveSOAPENCSimpleType(QName elementType) {
    	if (elementType == null
            || !elementType.getNamespaceURI().equals(
                WSIFConstants.NS_URI_SOAP_ENC)) {
            return null;    	
        }
        
        String lp = elementType.getLocalPart();
        if (lp == null) {
        	return null;
        }
        
        if (lp.equals("string")) {
        	return String.class;
        } else if (lp.equals("boolean")) {
        	return Boolean.class;
        } else if (lp.equals("float")) {
        	return Float.class;
        } else if (lp.equals("double")) {
        	return Double.class;
        } else if (lp.equals("decimal")) {
        	return BigDecimal.class;
        } else if (lp.equals("int")) {
        	return Integer.class;
        } else if (lp.equals("short")) {
        	return Short.class;
        } else if (lp.equals("byte")) {
        	return Byte.class;
        } else if (lp.equals("base64")) {
        	return byte[].class;
        } else {
        	return null;
        }       
    }    
}
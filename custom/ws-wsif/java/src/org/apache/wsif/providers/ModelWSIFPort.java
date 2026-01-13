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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.base.WSIFDefaultMessage;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.util.WSIFUtils;
import org.apache.wsif.wsdl.extensions.format.TypeMap;
import org.apache.wsif.wsdl.extensions.format.TypeMapping;

/**
 * ModelWSIFPort
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
 * For a subclass to use ModelWSIFPort, as a minimum it
 * would implement the makeWSIFOperation method, and the
 * getImplementedAddressClass and validateAddress methods
 * to define and validate the WSDL port address extensibility
 * element being used by the provider.  
 * 
 * Other methods may be overriden to customise the behaviour,
 * see the method javadoc for details. 
 *  
 * @author Ant Elder <ant.elder@uk.ibm.com>
 */
abstract public class ModelWSIFPort implements WSIFPort {

    protected Definition def;
    protected Service service;
    protected Port port;

    transient protected WSIFMessage context;

    protected WSIFDynamicTypeMap typeMap;

    protected ExtensibilityElement binding;

    protected boolean cacheOperations;
    transient protected HashMap cachedOperations;

    protected boolean autoTypeMappingSupported;

    protected boolean formatBindingSupported;
    protected HashMap formatBindingTypes;

    /**
     * Construct a ModelWSIFPort
     */
    public ModelWSIFPort(Definition def, Port port, WSIFDynamicTypeMap typeMap)
        throws WSIFException {

        this(def, null, port, typeMap, null);
    }

    /**
     * Construct a ModelWSIFPort
     */
    public ModelWSIFPort(
        Definition def,
        Service service,
        Port port,
        WSIFDynamicTypeMap typeMap,
        ExtensibilityElement binding)
        throws WSIFException {
        Trc.entry(this, def, service, port, typeMap, binding);

        this.def = def;
        this.service = service;
        this.port = port;
        this.binding = binding;
        this.typeMap = typeMap;

        //TODO: autoTypeMappingSupported = wsifService.isAutoTypeMappingSupported();
        autoTypeMappingSupported = true;

        //TODO: cacheOperations = wsifService.isCacheOperations();
        cacheOperations = true;

        if (cacheOperations == true) {
            cachedOperations = new HashMap();
        }

        formatBindingSupported = false;

        if (Trc.ON) {
            Trc.exit(deep());
        }
    }

    /**
     * @see WSIFPort#createOperation(String)
     */
    public WSIFOperation createOperation(String opName) throws WSIFException {
        Trc.entry(this, opName);
        WSIFOperation op = createOperation(opName, null, null);
        Trc.exit(op);
        return op;
    }

    /**
     * @see WSIFPort#createOperation(String, String, String)
     */
    public WSIFOperation createOperation(
        String opName,
        String inputName,
        String outputName)
        throws WSIFException {
        Trc.entry(this, opName, inputName, outputName);

        ModelWSIFOperation wsifOp = null;
        String cacheKey = null;

        if (cacheOperations == true) {
            cacheKey =
                new StringBuffer(opName)
                    .append(":")
                    .append(inputName)
                    .append(":")
                    .append(outputName)
                    .toString();
            wsifOp = (ModelWSIFOperation) cachedOperations.get(cacheKey);
        }

        if (wsifOp == null) {
            BindingOperation wsdlBindingOperation =
                WSIFUtils.getBindingOperation(
                    port.getBinding(),
                    opName,
                    inputName,
                    outputName);
            if (wsdlBindingOperation != null) {
                wsifOp = makeWSIFOperation(this, wsdlBindingOperation);
            }
        }

        if (wsifOp == null) {
            throw new WSIFException(
                "Could not create operation: "
                    + opName
                    + ":"
                    + inputName
                    + ":"
                    + outputName);
        }

        if (cacheOperations == true) {
            cachedOperations.put(cacheKey, wsifOp);
        }

        wsifOp.initialise();

        Trc.exit(wsifOp);
        return wsifOp;
    }

    /**
     * Returns the autoTypeMappingSupported.
     * @return boolean
     */
    protected boolean isAutoTypeMappingSupported() {
        return autoTypeMappingSupported;
    }

    /**
     * Sets the autoTypeMappingSupported.
     * @param autoTypeMappingSupported The autoTypeMappingSupported to set
     */
    protected void setAutoTypeMappingSupported(boolean autoTypeMappingSupported) {
        this.autoTypeMappingSupported = autoTypeMappingSupported;
    }

    /**
     * Returns the cacheOperations.
     * @return boolean
     */
    public boolean isCacheOperations() {
        return cacheOperations;
    }

    /**
     * Sets the cacheOperations.
     * @param cacheOperations The cacheOperations to set
     */
    protected void setCacheOperations(boolean cacheOperations) {
        this.cacheOperations = cacheOperations;
    }

    /**
     * Returns the binding ExtensibilityElement.
     * @return ExtensibilityElement
     */
    public ExtensibilityElement getBinding() {
        return binding;
    }

    /**
     * Returns the WSDL Definition object
     * @return Definition
     */
    public Definition getDefinition() {
        return def;
    }

    /**
     * Returns the WSDL port.
     * @return Port
     */
    public Port getPort() {
        return port;
    }

    /**
     * Returns the WSDL service.
     * @return Service
     */
    public Service getService() {
        return service;
    }

    /**
     * Returns the typeMap.
     * @return WSIFDynamicTypeMap
     */
    public WSIFDynamicTypeMap getTypeMap() {
        return typeMap;
    }

    /**
     * Gets the context information for this WSIFPort.
     * @return context
     */
    public WSIFMessage getContext() throws WSIFException {
        Trc.entry(this);
        if (context == null) {
            // TODO: really this should call getContext on the WSIFService but
            // theres no reference to that so it has to rely on WSIFService
            // should call setContext on any WSIFPort it creates.
            this.context = new WSIFDefaultMessage();
        }
        WSIFMessage contextCopy;
        try {
            contextCopy = (WSIFMessage) this.context.clone();
        } catch (CloneNotSupportedException e) {
            throw new WSIFException(
                "CloneNotSupportedException cloning context: ",
                e);
        }
        Trc.exit(contextCopy);
        return contextCopy;
    }

    /**
     * Sets the context information for this WSIFPort.
     * @param WSIFMessage the new context information
     */
    public void setContext(WSIFMessage context) {
        Trc.entry(this, context);
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        this.context = context;
        Trc.exit(null);
    }

    /**
     * Tests if this port supports synchronous calls to operations.
     * 
     * @return true   by default WSIFPorts do support synchronous calls
     */
    public boolean supportsSync() {
        Trc.entry(this);
        Trc.exit(true);
        return true;
    }

    /**
     * Tests if this port supports asynchronous calls to operations.
     * 
     * @return false   by default ports do not support asynchronous calls
     */
    public boolean supportsAsync() {
        Trc.entry(this);
        Trc.exit(false);
        return false;
    }

    /**
     * Returns the formatBindingSupported.
     * @return boolean
     */
    public boolean isFormatBindingSupported() {
        return formatBindingSupported;
    }

    /**
     * Sets the formatBindingSupported.
     * @param formatBindingSupported The formatBindingSupported to set
     */
    protected void setFormatBindingSupported(boolean formatBindingSupported) {
        this.formatBindingSupported = formatBindingSupported;
    }

    /**
     * Override java.lang.Object finalize method to close the WSIFPort
     */
    public void finalize() throws Throwable {
        Trc.entry(this);
        try {
            close();
        } catch (WSIFException ex) {
            Trc.ignoredException(ex);
        }
        super.finalize();
        Trc.exit();
    }

    /**
     * Gets the Java class for a WSDL part 
     * TODO: what should format binding mappings take preference, should 
     *      there be a way to disbale auto type mapping?
     */
    protected Class getClassForPart(Part p) throws WSIFException {
        QName partType = ProviderUtils.getPartType(p);

        Class partClass = null;

        // first see if there's format binding mapping
        if (partClass == null && isFormatBindingSupported() == true) {
            partClass = getFormatBindingType(partType);
        }

        if (isFormatBindingSupported() == false
            || isAutoTypeMappingSupported() == true) {

            // see if there's a WSIFDynamicTypeMap mapping
            partClass = getDynamicTypeClass(partType);

            // perhaps a known standard type
            if (partClass == null) {
                partClass = getStandardType(partType);
            }

            // try a default name?
            if (partClass == null) {
                // TODO: should there be a default? partClass = getDefaultType(partType);
            }
        }

        // handle unknown type 
        if (partClass == null) {
            partClass = doNoClassForPart(partType, p);
        }

        return partClass;
    }

    /**
     * Process a part where the Java class for the part type is unknown
     * Defaults to just throwing an exception, subclasses may override
     * with their own implementation 
     */
    protected Class doNoClassForPart(QName partType, Part p)
        throws WSIFException {
        throw new WSIFException("cannot determine class for type: " + partType);
    }

    /**
     * Gets the Java class for a type from the WSIFDynamicTypeMap
     */
    protected Class getDynamicTypeClass(QName partType) {
        Class partClass = null;
        for (Iterator i = typeMap.iterator();
            partClass == null && i.hasNext();
            ) {

            WSIFDynamicTypeMapping mapping = (WSIFDynamicTypeMapping) i.next();
            if (partType.equals(mapping.getXmlType())) {
                partClass = mapping.javaType;
            }
        }

        return partClass;
    }

    /**
     * Gets the Java class for a type from the WSDL format binding typeMap
     */
    protected Class getFormatBindingType(QName partType) throws WSIFException {
        Class partClass = null;

        if (formatBindingTypes == null && isFormatBindingSupported()) {
            initializeFormatBindingTypes();
        }

        if (formatBindingTypes != null) {
            for (Iterator i = formatBindingTypes.keySet().iterator();
                partClass == null && i.hasNext();
                ) {

                QName fbType = (QName) i.next();
                if (fbType.equals(partType)) {
                    partClass = (Class) formatBindingTypes.get(partType);
                }
            }
        }

        return partClass;
    }

    /**
     * Gets the Java class for a known type
     * A known type is a standard Java type like java.lang.String
     */
    protected Class getStandardType(QName partType) throws WSIFException {
        Class partClass = null;
        Map simpleTypes = WSIFUtils.getSimpleTypesMap();
        String className = (String) simpleTypes.get(partType);

        //TODO: how to do this???
        //      classname is something like "int", "[B", or "java.lang.String" 
        //      hack...
        if (className != null && className.length() > 0) {
            char c = className.charAt(0);
            if (className.indexOf('.') < 0 && Character.isLowerCase(c)) {
                partClass = getPrimitiveClass(className);
            } else if ('[' == c) {
                partClass = getArrayClass(className);
            } else {
                partClass = getClassForType(className);
            }
        }
        return partClass;
    }

    /**
     * Gets the Java class for the named primitive type
     * TODO: usesthe wrapper classes, does this matter?  
     */
    protected Class getPrimitiveClass(String primitiveName) {
        Class javaClass = null;
        if (primitiveName.equals("int")) {
            javaClass = Integer.class;
        } else if (primitiveName.equals("float")) {
            javaClass = Float.class;
        } else if (primitiveName.equals("double")) {
            javaClass = Double.class;
        } else if (primitiveName.equals("boolean")) {
            javaClass = Boolean.class;
        } else if (primitiveName.equals("long")) {
            javaClass = Long.class;
        } else if (primitiveName.equals("short")) {
            javaClass = Short.class;
        } else if (primitiveName.equals("byte")) {
            javaClass = Byte.class;
        } else if (primitiveName.equals("void")) {
            javaClass = Void.class;
        }
        return javaClass;
    }

    protected Class getArrayClass(String type) {
        Class typeClass = null;
        //TODO:
        return typeClass;
    }

    /**
     * Gets a Java class object from the fully qualified String className  
     */
    protected Class getClassForType(String className) throws WSIFException {
        Class clazz = null;
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            clazz = Class.forName(className, true, cl);
        } catch (Throwable ex) {
            throw new WSIFException(
                "exception getting class for simple type '"
                    + clazz
                    + "': "
                    + ex);
        }
        return clazz;
    }

    /**
     * Builds the type map from the WSDL format binding.
     * The formatBindingTypes is a HashTable with the key
     * a type QName, and the value a Java class.
     * 
     * The format binding has the form:
     * <format:typeMapping style="uri" encoding="..."/>?
     *    <format:typeMap typeName="qname"|elementName="qname" formatType="nmtoken"/>*
     * </format:typeMapping> 
     * 
     * TODO: what are the valid style, enocoding to support?
     * TODO: need to make this more pluggable - JMS provider uses diff styles
     */
    protected void initializeFormatBindingTypes() throws WSIFException {
        Trc.entry(this);

        TypeMapping typeMapping = getFormatTypeMapping();
        if (typeMapping == null) {
            doMissingFormatTypeMapping();
        }

        // Build the formatBindingTypes hashmap 
        formatBindingTypes = new HashMap();
        if (typeMapping != null) {
            List typeMaps = typeMapping.getMaps();
            for (Iterator i = typeMaps.iterator(); i.hasNext();) {
                TypeMap typeMap = (TypeMap) i.next();
                QName typeName = typeMap.getTypeName();
                if (typeName == null) {
                    typeName = typeMap.getElementName();
                }
                String className = typeMap.getFormatType();
                Class typeClass = getNamedClass(className);
                if (typeName != null && typeClass != null) {
                    //TODO: should duplicate mappings be allowed?              	
                    this.formatBindingTypes.put(typeName, typeClass);
                } else {
                    throw new WSIFException("Error in binding TypeMap. Key or Value is null");
                }
            }
        }
        Trc.exit();
    }

    /**
     * Gets the format:typeMapping ExtensibilityElement from the WSDL
     */
    protected TypeMapping getFormatTypeMapping() {
        TypeMapping typeMapping = null;

        // Get the TypeMappings from the binding
        Iterator bindingIterator =
            this.port.getBinding().getExtensibilityElements().iterator();

        // Choose the first typeMap that has encoding=Java and style=Java. 
        // Ignore any other typeMap's that have other encodings and styles.
        while (bindingIterator.hasNext()) {
            Object next = bindingIterator.next();
            if (next instanceof TypeMapping mapping) {
                typeMapping = mapping;
                if ("Java".equals(typeMapping.getEncoding())
                    && "Java".equals(typeMapping.getStyle())) {
                    break;
                }
                typeMapping = null;
            }
        }
        return typeMapping;
    }

    /**
     * Handles the case where there is no format binding in the WSDL
     * This does nothing, subclasses may override
     */
    protected void doMissingFormatTypeMapping() throws WSIFException {
        //TODO: maybe it should throw an exception by default?
    }

    /**
     * Finds the Java class object for the String class name
     */
    protected Class getNamedClass(String className) throws WSIFException {
        Class clazz = null;
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            clazz = Class.forName(className, true, cl);
        } catch (Throwable ex) {
            Trc.exception(ex);
            doUnknownClassName(className);
        }
        return clazz;
    }

    /**
     * Handles case where the class named in the WSDL formatType is not found
     * This just throws an exception, subclasses may override
     */
    protected void doUnknownClassName(String className) throws WSIFException {
        throw new WSIFException(
            "can not find Java class for formatType: " + className);
    }

    /**
     * Initialise the WSIFPort
     * This is called after the constructor has run and
     * all extensebility elements have been validated 
     * (ie validateAddress has been called). This default
     * implementation does nothing, subclasses may override.
     */
    protected void doInitialize() throws WSIFException {
    }

    // the methods subclasses must implement

    abstract protected ModelWSIFOperation makeWSIFOperation(
        ModelWSIFPort port,
        BindingOperation bop)
        throws WSIFException;

    abstract protected Class getImplementedAddressClass();

    abstract protected void validateAddress(ExtensibilityElement address)
        throws WSIFException;

    abstract public void close() throws WSIFException;

    /**
     * String representation of this WSIFPort for WSIF Trc.
     */
    public String deep() {
        StringBuffer buff = new StringBuffer();
        buff.append(super.toString() + ":\n");
        buff.append("definition:");
        buff.append(Trc.brief(def));
        buff.append(" service:");
        buff.append(Trc.brief(service));
        buff.append(" portModel:");
        buff.append(Trc.brief(port));
        buff.append(" context:");
        buff.append(context);
        buff.append(" typeMap:");
        buff.append(typeMap);
        buff.append(" binding:");
        buff.append(binding);
        buff.append(" autoTypeMappingSupported:");
        buff.append(autoTypeMappingSupported);
        buff.append(" cacheOperations:");
        buff.append(cacheOperations);
        buff.append(" cachedOperations:");
        buff.append(cachedOperations);
        buff.append(" formatBindingSupported:");
        buff.append(formatBindingSupported);
        buff.append(" formatBindingTypes:");
        buff.append(formatBindingTypes);
        return buff.toString();
    }

}
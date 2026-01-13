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

package org.apache.wsif.providers.java;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.base.WSIFDefaultPort;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.providers.WSIFDynamicTypeMap;
import org.apache.wsif.util.WSIFUtils;
import org.apache.wsif.wsdl.extensions.format.TypeMap;
import org.apache.wsif.wsdl.extensions.format.TypeMapping;
import org.apache.wsif.wsdl.extensions.java.JavaAddress;

/**
 * Java WSIF Port.
 * 
 * This implements the WSDL Java binding allowing a Java object
 * to be used as a WSDL described service. The Class of the 
 * target service object is identified by the WSDL java:address
 * element. Each instance of a WSIFPort_Java will instantiate 
 * a new instance of the target service class, and this instance
 * will be shared by all WSIFOperations created from the WSIFPort.  
 *  
 * @author <a href="mailto:gpfau@de.ibm.com">Gerhard Pfau</a>
 * @author Owen Burroughs <owenb@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 * @author <a href="mailto:ant.elder@uk.ibm.com">Ant Elder</a>
 */
public class WSIFPort_Java extends WSIFDefaultPort implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Definition fieldDefinition;
    private Port fieldPortModel;

    transient private Class serviceObjectClass;
    transient private Method[] serviceObjectMethods;
    transient private Constructor[] serviceObjectConstructors;

    transient private java.lang.Object fieldObjectReference; // 'physical connection'
    private final boolean separatedObjectRef; // DO NOT INITIALIZE UNTIL CONSTRUCTOR

    private Map fieldTypeMaps;
    transient protected Map operationInstances;

    /**
     * Construct a new instance of WSIFPort_Java
     * 
     * @param def   the WSDL4J Definintion object   
     * @param port   the WSDL4J Port this WSIFPort_Java represents   
     * @param typeMap   the WSIF type mappings (TODO: not used)
     */
    public WSIFPort_Java(Definition def, Port port, WSIFDynamicTypeMap typeMap)
        throws WSIFException {
        Trc.entry(this, def, port, typeMap);

		 // Flag to indicate that the service object reference is written after this object
		 // when this object is serialized to a stream. Older versions of this class may
		 // not do this and so the flag will be missing in the stream and default to false
		 // when the object is deserialized (flag must not be initialized where declared
		 // because it is final and would not default to false!)
		separatedObjectRef = true;
		                           
        fieldDefinition = def;
        fieldPortModel = port;

        operationInstances = new HashMap();

        buildTypeMap();

        if (Trc.ON) {
            Trc.exit(deep());
        }
    }

    /**
     * Creates a WSIFOperation for the given operation name
     * @return WSIFOperation   the WSIFOperation
     * @see org.apache.wsif.WSIFPort#createOperation(String) 
     * @throws WSIFException  if there is an exception creating the WSIFOperation
     */
    public WSIFOperation createOperation(String operationName)
        throws WSIFException {
        Trc.entry(this, operationName);
        WSIFOperation wo = createOperation(operationName, null, null);
        Trc.exit(wo);
        return wo;
    }

    /**
     * Creates a WSIFOperation for the given operation name
     * @return WSIFOperation   the WSIFOperation_ApacheAxis
     * @see org.apache.wsif.WSIFPort#createOperation(String, String, String) 
     * @throws WSIFException  if there is an exception creating the WSIFOperation
     */
    public WSIFOperation createOperation(
        String operationName,
        String inputName,
        String outputName)
        throws WSIFException {
        Trc.entry(this, operationName, inputName, outputName);

        WSIFOperation_Java op =
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

    /**
     * Gets a WSIFOperation_Java instance for given names.
     * If an instance has already been created and exists in the 
     * operationInstances cache return that, otherwise construct
     * a new instance, and add it to the operationInstances cache.  
     */
    protected WSIFOperation_Java getDynamicWSIFOperation(
        String name,
        String inputName,
        String outputName)
        throws WSIFException {
        Trc.entry(this, name, inputName, outputName);

        WSIFOperation_Java operation =
            (WSIFOperation_Java) operationInstances.get(
                getKey(name, inputName, outputName));

        if (operation == null) {
            BindingOperation bindingOperationModel =
                WSIFUtils.getBindingOperation(
                    fieldPortModel.getBinding(),
                    name,
                    inputName,
                    outputName);

            if (bindingOperationModel != null) {
                operation =
                    new WSIFOperation_Java(
                        fieldPortModel,
                        bindingOperationModel,
                        this,
                        fieldTypeMaps);
                setDynamicWSIFOperation(name, inputName, outputName, operation);
            }
        }
        Trc.exit(operation);
        return operation;
    }

    /**
     * Add a WSIFOperation_Java instance to the cache
     */
    protected void setDynamicWSIFOperation(
        String name,
        String inputName,
        String outputName,
        WSIFOperation_Java value) {
        Trc.entry(this, name, inputName, outputName, value);
        operationInstances.put(getKey(name, inputName, outputName), value);
        Trc.exit();
    }

    /**
     * Gets the Java class of the service object
     * @return Class   the class of the service object
     */
    Class getServiceObjectClass() throws WSIFException {
        // no method access modifier as it used by WSIFOperation_Java
        Trc.entry(this);
        if (serviceObjectClass == null) {

            ExtensibilityElement portExtension =
                (ExtensibilityElement) fieldPortModel.getExtensibilityElements().getFirst();

            if (portExtension == null) {
                throw new WSIFException("missing port extension");
            }

            JavaAddress address = (JavaAddress) portExtension;

            try {
                serviceObjectClass =
                    Class.forName(
                        address.getClassName(),
                        true,
                        Thread.currentThread().getContextClassLoader());
            } catch (Throwable ex) {
                Trc.exception(ex);
                throw new WSIFException(
                    "Exception getting target object class '"
                        + address.getClassName()
                        + "'",
                    ex);
            }
        }
        Trc.exit(serviceObjectClass);
        return serviceObjectClass;
    }

    /**
     * Gets the constructors of the service object
     * @return Constructor[]   the constructors of the service object
     */
    Constructor[] getServiceObjectConstructors() throws WSIFException {
        // no method access modifier as it used by WSIFOperation_Java
        Trc.entry(this);
        if (serviceObjectConstructors == null) {
            Class c = getServiceObjectClass();
            serviceObjectConstructors = c.getConstructors();
        }
        Trc.exit(serviceObjectConstructors);
        return serviceObjectConstructors;
    }

    /**
     * Gets the methods of the service object
     * @return Method[]   the methods of the service object
     */
    Method[] getServiceObjectMethods() throws WSIFException {
        // no method access modifier as it used by WSIFOperation_Java
        Trc.entry(this);
        if (serviceObjectMethods == null) {
            Class c = getServiceObjectClass();
            serviceObjectMethods = c.getMethods();
        }
        Trc.exit(serviceObjectMethods);
        return serviceObjectMethods;
    }

    /**
     * Gets the service object.
     * @return Object   the service object instance
     */
    public Object getObjectReference() throws WSIFException {
        Trc.entry(this);
        if (fieldObjectReference == null) {
            Class c = getServiceObjectClass();
            try {
                fieldObjectReference = c.newInstance();
            } catch (Exception ex) {
                Trc.exception(ex);
                throw new WSIFException(
                    "Could not instantiate target object of class '"
                        + c.getName()
                        + "'",
                    ex);
            }
        }
        Trc.exit(fieldObjectReference);
        return fieldObjectReference;
    }

    /**
     * Sets a new instance of the target service object
     * Used by WSIFOperation_Java when it runs a Constructor
     * instead of an instance method. 
     */
    void setObjectReference(java.lang.Object newObjectReference) {
        /* no method access modifier as this is called by WSIFOperation_Java
         * I don't think this should be made public */
        Trc.entry(this, newObjectReference);
        fieldObjectReference = newObjectReference;
        Trc.exit();
    }

    /**
     * Builds the type map from the WSDL format binding.
     * The fieldTypeMaps is a HashTable with the key
     * a type QName, and the value a String class name.
     * The format binding has the form:
     * <format:typeMapping style="uri" encoding="..."/>?
     *    <format:typeMap typeName="qname"|elementName="qname" formatType="nmtoken"/>*
     * </format:typeMapping> 
     */
    private void buildTypeMap() throws WSIFException {
        Trc.entry(this);
        TypeMapping typeMapping = null;

        // Get the TypeMappings from the binding
        Iterator bindingIterator =
            this.fieldPortModel.getBinding().getExtensibilityElements().iterator();

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

        if (typeMapping == null) {
            QName bindingName = fieldPortModel.getBinding().getQName();
            throw new WSIFException(
                "Binding "
                    + (bindingName == null ? "<null>" : bindingName.toString())
                    + " does not contain a typeMap with encoding=Java and style=Java");
        }

        // Build the formatTypeMaps hashmap 
        fieldTypeMaps = new HashMap();
        bindingIterator = typeMapping.getMaps().iterator();
        while (bindingIterator.hasNext()) {
            TypeMap typeMap = (TypeMap) bindingIterator.next();
            ///////////////////////////////////
            QName typeName = typeMap.getTypeName();
            if (typeName == null) {
                typeName = typeMap.getElementName();
            }
            String type = typeMap.getFormatType();
            if (typeName != null && type != null) {
                if (fieldTypeMaps.containsKey(typeName)) {
                    Vector v = null;
                    Object obj = fieldTypeMaps.get(typeName);
                    if (obj instanceof Vector vector) {
                        v = vector;
                    } else {
                        v = new Vector();
                        v.addElement(obj);
                    }
                    v.addElement(type);
                    this.fieldTypeMaps.put(typeName, v);
                } else {
                    this.fieldTypeMaps.put(typeName, type);
                }
            } else {
                throw new WSIFException("Error in binding TypeMap. Key or Value is null");
            }
        }
        Trc.exit();
    }

    /**
     * Gets the WSDL4J Definition asscociated with this WSIFPort
     * @return Definition   the WSDL4J Definition object
     */
    public Definition getDefinition() {
        Trc.entry(this);
        Trc.exit(fieldDefinition);
        return fieldDefinition;
    }

    /**
     * Gets the WSDL4J Port asscociated with this WSIFPort
     * @return Port   the WSDL4J Port object
     */
    public Port getPortModel() {
        Trc.entry(this);
        Trc.exit(fieldPortModel);
        return fieldPortModel;
    }

    /**
     * Used by WSIF Trc
     */
    public String deep() {
        StringBuffer buff = new StringBuffer();

        buff.append(super.toString()).append(":\n");

        buff.append("definition:");
        if (fieldDefinition == null) {
            buff.append("null");
        } else {
            if (fieldDefinition.getQName() == null) {
                buff.append("unknown");
            } else {
                buff.append(fieldDefinition.getQName().toString());
            }
        }

        buff.append(" portModel:");
        if (fieldPortModel == null) {
            buff.append("null");
        } else {
            if (fieldPortModel.getName() == null) {
                buff.append("unknown");
            } else {
                buff.append(fieldPortModel.getName().toString());
            }
        }

        buff.append(" serviceObjectReference:").append(fieldObjectReference);

        buff.append(" serviceObjectClass: ").append(serviceObjectClass);

        buff.append(" serviceObjectConstructors: ");
        if (serviceObjectConstructors == null) {
            buff.append("null");
        } else {
            buff.append(serviceObjectConstructors);
            buff.append(" size:").append(serviceObjectConstructors.length);
        }

        buff.append(" serviceObjectMethods: ");
        if (serviceObjectMethods == null) {
            buff.append("null");
        } else {
            buff.append(serviceObjectMethods);
            buff.append(" size:").append(serviceObjectMethods.length);
        }

        buff.append(" formatTypeMaps: ");
        if (fieldTypeMaps == null) {
            buff.append("null");
        } else {
            buff.append(" size:").append(fieldTypeMaps.size());
            int i = 0;
            for (Iterator it = fieldTypeMaps.keySet().iterator();
                it.hasNext();
                ) {
                QName type = (QName) it.next();
                Object value = fieldTypeMaps.get(type);
                buff.append("\nformatTypeMaps[").append(i++).append("]:");
                buff.append(type).append(", ").append(value);
            }
            buff.append("\n");
        }

        buff.append(" operationInstances:");
        if (operationInstances == null) {
            buff.append("null");
        } else {
            buff.append(" size:").append(operationInstances.size());
            int i = 0;
            for (Iterator it = operationInstances.keySet().iterator();
                it.hasNext();
                ) {
                String key = (String) it.next();
                WSIFOperation_Java woj =
                    (WSIFOperation_Java) operationInstances.get(key);
                buff.append("\noperationInstances[").append(i++).append("]:");
                buff.append(key).append(" ").append(woj).append(" ");
            }
            buff.append("\n");
        }

        return buff.toString();
    }

	/**
	 * Override default serialization
	 */
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();

    	if (fieldObjectReference != null && !(fieldObjectReference instanceof Serializable)) {
    		// Cannot serialize web service object reference and so it will have to be stored as null
    		// Any state information is therefore going to be lost
    		oos.writeObject(null);
    	} else {
    		oos.writeObject(fieldObjectReference);
    	}    	
      
    }

	/**
	 * Override default deserialization
	 */
    private void readObject(ObjectInputStream ois)
        throws ClassNotFoundException, IOException {
        ois.defaultReadObject();

		// Recover the web service object reference
        if (separatedObjectRef) {
        	Object ref = ois.readObject();
        	fieldObjectReference = ref;
        }
                
        // reset the operation instances
        operationInstances = new HashMap();
    }   
}

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
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

    private final Definition fieldDefinition;
    private final Port fieldPortModel;

    /*
     * The three reflection caches below are lazily populated and read from every thread
     * that drives an operation on this (shared) port. They must be volatile: publishing an
     * array reference through a data race allows another thread to observe the reference
     * while still reading null elements out of the array.
     */
    transient private volatile Class<?> serviceObjectClass;
    transient private volatile Method[] serviceObjectMethods;
    transient private volatile Constructor<?>[] serviceObjectConstructors;

    /*
     * The service object is shared by every WSIFOperation created from this port, and is
     * replaced wholesale by setObjectReference() when a methodType="constructor" operation
     * runs. Volatile guarantees safe publication of the instance; note that it does NOT
     * make the service class itself thread-safe - see the class javadoc.
     */
    transient private volatile java.lang.Object fieldObjectReference; // 'physical connection'
    private final boolean separatedObjectRef; // DO NOT INITIALIZE UNTIL CONSTRUCTOR

    /** Immutable after construction, so it can be shared with every WSIFOperation_Java. */
    private final Map<QName, Object> fieldTypeMaps;

    /**
     * Memoized {@link Class} resolutions of {@link #fieldTypeMaps}, keyed by part type.
     * <p>
     * The format binding stores class <em>names</em>, so every operation used to re-run
     * Class.forName for each of its parameter and return types - and it did so two or
     * three times per operation, because getMethodArgumentClasses() is called from both
     * getMethods() and getConstructors(). Resolution stays lazy so that a binding
     * referencing a class which is absent from the classpath still only fails when an
     * operation actually needs it, exactly as before.
     */
    transient private volatile Map<QName, Object> resolvedTypeMaps;

    /**
     * Cache of operation prototypes. Concurrent because ports are routinely shared between
     * threads and createOperation() performs a check-then-act on this map.
     */
    transient protected Map<String, WSIFOperation_Java> operationInstances;

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

        operationInstances = new ConcurrentHashMap<>();
        resolvedTypeMaps = new ConcurrentHashMap<>();

        fieldTypeMaps = buildTypeMap();

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
     * <p>
     * Two threads racing on the same key may both build a prototype; that is harmless
     * because the prototype is immutable once constructed and callers always receive a
     * {@link WSIFOperation_Java#copy()} of it.
     */
    protected WSIFOperation_Java getDynamicWSIFOperation(
        String name,
        String inputName,
        String outputName)
        throws WSIFException {
        Trc.entry(this, name, inputName, outputName);

        WSIFOperation_Java operation =
            operationInstances.get(getKey(name, inputName, outputName));

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
    Class<?> getServiceObjectClass() throws WSIFException {
        // no method access modifier as it used by WSIFOperation_Java
        Trc.entry(this);
        Class<?> cls = serviceObjectClass;
        if (cls == null) {

            // Note: getFirst() throws NoSuchElementException on an empty list, so the
            // emptiness test has to come first. This used to be a null check on the
            // result, which became unreachable when get(0) was migrated to getFirst().
            List portExtensions = fieldPortModel.getExtensibilityElements();
            if (portExtensions == null || portExtensions.isEmpty()) {
                throw new WSIFException("missing port extension");
            }

            Object portExtension = portExtensions.getFirst();
            if (!(portExtension instanceof JavaAddress address)) {
                throw new WSIFException(
                    "expected a java:address port extension but found "
                        + (portExtension == null
                            ? "null"
                            : portExtension.getClass().getName()));
            }

            try {
                cls =
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
            serviceObjectClass = cls;
        }
        Trc.exit(cls);
        return cls;
    }

    /**
     * Gets the constructors of the service object
     * @return Constructor[]   the constructors of the service object
     */
    Constructor<?>[] getServiceObjectConstructors() throws WSIFException {
        // no method access modifier as it used by WSIFOperation_Java
        Trc.entry(this);
        Constructor<?>[] ctors = serviceObjectConstructors;
        if (ctors == null) {
            Class<?> c = getServiceObjectClass();
            // getConstructors() already hands back a defensive copy
            ctors = c.getConstructors();
            serviceObjectConstructors = ctors;
        }
        Trc.exit(ctors);
        return ctors;
    }

    /**
     * Gets the methods of the service object
     * @return Method[]   the methods of the service object
     */
    Method[] getServiceObjectMethods() throws WSIFException {
        // no method access modifier as it used by WSIFOperation_Java
        Trc.entry(this);
        Method[] methods = serviceObjectMethods;
        if (methods == null) {
            Class<?> c = getServiceObjectClass();
            // getMethods() already hands back a defensive copy
            methods = c.getMethods();
            serviceObjectMethods = methods;
        }
        Trc.exit(methods);
        return methods;
    }

    /**
     * Gets the service object.
     * <p>
     * Two threads arriving here concurrently may each create an instance; the loser's
     * instance is simply discarded. That is the pre-existing behaviour and is harmless,
     * because the reference is published safely through a volatile field.
     *
     * @return Object   the service object instance
     */
    public Object getObjectReference() throws WSIFException {
        Trc.entry(this);
        Object ref = fieldObjectReference;
        if (ref == null) {
            Class<?> c = getServiceObjectClass();
            try {
                ref = c.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                Trc.exception(ex);
                throw new WSIFException(
                    "Could not instantiate target object of class '"
                        + c.getName()
                        + "'",
                    ex);
            }
            fieldObjectReference = ref;
        }
        Trc.exit(ref);
        return ref;
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
     * The returned map is keyed on the type QName, with a String class name
     * (or a {@code List} of String class names, when the binding maps one XML type
     * onto several Java types) as the value.
     * The format binding has the form:
     * <format:typeMapping style="uri" encoding="..."/>?
     *    <format:typeMap typeName="qname"|elementName="qname" formatType="nmtoken"/>*
     * </format:typeMapping>
     * <p>
     * The result is wrapped unmodifiable and stored in a final field so that it can be
     * shared with every WSIFOperation_Java - and therefore across threads - without
     * further synchronisation.
     *
     * @return the immutable type map
     */
    private Map<QName, Object> buildTypeMap() throws WSIFException {
        Trc.entry(this);
        TypeMapping typeMapping = null;

        // Get the TypeMappings from the binding
        // Choose the first typeMap that has encoding=Java and style=Java.
        // Ignore any other typeMap's that have other encodings and styles.
        for (Object next : fieldPortModel.getBinding().getExtensibilityElements()) {
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
        Map<QName, Object> typeMaps = new HashMap<>();
        for (Object o : typeMapping.getMaps()) {
            TypeMap typeMap = (TypeMap) o;
            QName typeName = typeMap.getTypeName();
            if (typeName == null) {
                typeName = typeMap.getElementName();
            }
            String type = typeMap.getFormatType();
            if (typeName == null || type == null) {
                throw new WSIFException("Error in binding TypeMap. Key or Value is null");
            }

            Object existing = typeMaps.get(typeName);
            if (existing == null) {
                typeMaps.put(typeName, type);
            } else if (existing instanceof List<?> list) {
                @SuppressWarnings("unchecked")
                List<String> types = (List<String>) list;
                types.add(type);
            } else {
                List<String> types = new ArrayList<>();
                types.add((String) existing);
                types.add(type);
                typeMaps.put(typeName, types);
            }
        }
        Map<QName, Object> result = Collections.unmodifiableMap(typeMaps);
        Trc.exit();
        return result;
    }

    /**
     * Resolves the format-binding entry for a part type into the Java type(s) it maps to,
     * memoizing the result for the lifetime of this port.
     * <p>
     * Resolution is deliberately lazy rather than done up-front in {@link #buildTypeMap()}:
     * a binding may legitimately declare a mapping for a type that no invoked operation
     * uses, and eagerly loading it would turn a working port into one that fails to
     * construct. A failure to load still propagates to the caller, uncached, so the next
     * attempt retries exactly as it did before.
     *
     * @param partType the XML type (or element) name of the message part
     * @return a {@code Class}, or a {@code List<Class>} when the binding maps the type
     *         onto several Java types, or null if the binding has no entry for it
     */
    Object getResolvedTypeMapping(QName partType) throws WSIFException {
        // no method access modifier as it is used by WSIFOperation_Java
        Object mapped = fieldTypeMaps.get(partType);
        if (mapped == null) {
            return null;
        }

        Map<QName, Object> cache = resolvedTypeMaps;
        if (cache == null) {
            // Can be null after deserialization (the field is transient)
            cache = new ConcurrentHashMap<>();
            resolvedTypeMaps = cache;
        }

        Object resolved = cache.get(partType);
        if (resolved == null) {
            if (mapped instanceof List<?> names) {
                List<Class<?>> classes = new ArrayList<>(names.size());
                for (Object name : names) {
                    classes.add(WSIFOperation_Java.getClassForName((String) name));
                }
                resolved = Collections.unmodifiableList(classes);
            } else {
                resolved = WSIFOperation_Java.getClassForName((String) mapped);
            }
            // Racing threads simply compute the same value; last write wins
            cache.put(partType, resolved);
        }
        return resolved;
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
        StringBuilder buff = new StringBuilder();

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
                buff.append(fieldPortModel.getName());
            }
        }

        buff.append(" serviceObjectReference:").append(fieldObjectReference);

        buff.append(" serviceObjectClass: ").append(serviceObjectClass);

        buff.append(" serviceObjectConstructors: ");
        if (serviceObjectConstructors == null) {
            buff.append("null");
        } else {
            buff.append(Arrays.toString(serviceObjectConstructors));
            buff.append(" size:").append(serviceObjectConstructors.length);
        }

        buff.append(" serviceObjectMethods: ");
        if (serviceObjectMethods == null) {
            buff.append("null");
        } else {
            buff.append(Arrays.toString(serviceObjectMethods));
            buff.append(" size:").append(serviceObjectMethods.length);
        }

        buff.append(" formatTypeMaps: ");
        if (fieldTypeMaps == null) {
            buff.append("null");
        } else {
            buff.append(" size:").append(fieldTypeMaps.size());
            int i = 0;
            for (Map.Entry<QName, Object> entry : fieldTypeMaps.entrySet()) {
                buff.append("\nformatTypeMaps[").append(i++).append("]:");
                buff.append(entry.getKey()).append(", ").append(entry.getValue());
            }
            buff.append("\n");
        }

        buff.append(" operationInstances:");
        if (operationInstances == null) {
            buff.append("null");
        } else {
            buff.append(" size:").append(operationInstances.size());
            int i = 0;
            for (Map.Entry<String, WSIFOperation_Java> entry : operationInstances.entrySet()) {
                buff.append("\noperationInstances[").append(i++).append("]:");
                buff.append(entry.getKey()).append(" ").append(entry.getValue()).append(" ");
            }
            buff.append("\n");
        }

        return buff.toString();
    }

	/**
	 * Override default serialization
	 */
    @Serial
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
    @Serial
    private void readObject(ObjectInputStream ois)
        throws ClassNotFoundException, IOException {
        ois.defaultReadObject();

		// Recover the web service object reference
        if (separatedObjectRef) {
            fieldObjectReference = ois.readObject();
        }

        // reset the operation instances
        operationInstances = new ConcurrentHashMap<>();
        resolvedTypeMaps = new ConcurrentHashMap<>();
    }
}

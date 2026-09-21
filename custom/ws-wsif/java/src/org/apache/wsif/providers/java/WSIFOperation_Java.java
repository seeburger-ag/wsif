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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.BindingFault;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Fault;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.xml.namespace.QName;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.attachments.WSIFAttachmentPart;
import org.apache.wsif.base.WSIFDefaultOperation;
import org.apache.wsif.logging.MessageLogger;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.providers.InvocationHelper;
import org.apache.wsif.providers.ProviderUtils;
import org.apache.wsif.wsdl.extensions.java.JavaOperation;

/**
 * Java operation. 
 * @see WSIFPort_Java
 * @author <a href="mailto:gpfau@de.ibm.com">Gerhard Pfau</a>
 * @author Owen Burroughs <owenb@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 */
public class WSIFOperation_Java
        extends WSIFDefaultOperation
        implements WSIFOperation {

    @Serial
    private static final long serialVersionUID = 1L;
        	
    protected javax.wsdl.Port fieldPortModel;
    protected WSIFPort_Java fieldPort;
    protected javax.wsdl.BindingOperation fieldBindingOperationModel;
    protected JavaOperation fieldJavaOperationModel;
    protected Operation wsdlOperation;
    
    protected String[] fieldInParameterNames = null;
    protected String[] fieldOutParameterNames = null;

    /**
     * Immutable map of fault class name -&gt; {@link FaultMessageInfo}, built once during
     * construction.
     * <p>
     * This map is shared by reference with every {@link #copy()} of this operation, and
     * those copies are handed to different threads. It used to be built lazily inside
     * {@link #getFaultMessageInfos()} on the first fault, which meant concurrent invocations
     * structurally modified the same unsynchronised HashMap. It must stay immutable.
     */
    protected Map<String, FaultMessageInfo> fieldFaultMessageInfos = Map.of();

    /**
     * Deferred failure from building {@link #fieldFaultMessageInfos}.
     * <p>
     * The fault map used to be built on the first fault, so a binding whose fault types
     * cannot be mapped only failed when a fault was actually raised. Building it eagerly
     * would turn that into a failure at operation-creation time, so the exception is
     * captured here and rethrown from {@link #getFaultMessageInfos()} instead.
     */
    private WSIFException faultMessageInfosFailure = null;

    transient protected Method[] fieldMethods = null;
    transient protected Constructor<?>[] fieldConstructors = null;
    protected String fieldOutputMessageName = null;
    protected boolean fieldIsStatic = false;
    protected boolean fieldIsConstructor = false;
    protected Map<QName, Object> fieldTypeMaps = null;
    protected boolean multiOutParts = false;
    transient private Object returnClass = null;

    /**
     * Immutable descriptor of a WSDL fault message.
     * <p>
     * Static (so it does not pin the enclosing operation) and final-fielded, so instances
     * can be published safely between threads via {@link #fieldFaultMessageInfos}.
     */
    static final class FaultMessageInfo {
        final String fieldMessageName;
        final String fieldPartName;
        final String fieldFormatType;
        // Note: In Java fault messages contain only one part: the Java exception

        FaultMessageInfo(String messageName, String partName, String formatType) {
            fieldMessageName = messageName;
            fieldPartName = partName;
            fieldFormatType = formatType;
        }
    }

    public WSIFOperation_Java(
        javax.wsdl.Port portModel,
        BindingOperation bindingOperationModel,
        WSIFPort_Java port,
        Map typeMaps)
        throws WSIFException {
        Trc.entry(this, portModel, bindingOperationModel, port, typeMaps);

        fieldPortModel = portModel;
        fieldBindingOperationModel = bindingOperationModel;
        fieldPort = port;
        fieldTypeMaps = typeMaps;
        
        // Find out all the methods on the Java class that is the backend service.
        Method[] allMethods = fieldPort.getServiceObjectMethods();

        try {
            fieldJavaOperationModel =
                (JavaOperation) fieldBindingOperationModel.getExtensibilityElements().getFirst();
        } catch (Exception e) {
        	Trc.exception(e);
            throw new WSIFException(
                "Unable to resolve Java binding for operation '"
                    + bindingOperationModel.getName()
                    + "'");
        }

        // Check what kind of method we have...
        String methodType = fieldJavaOperationModel.getMethodType();
        if ("static".equals(methodType)) {
        	fieldIsStatic = true;
            fieldMethods = getMethods(allMethods);
        } else if ("constructor".equals(methodType)) {
        	fieldIsConstructor = true;
            fieldConstructors = getConstructors();
        } else {
        	// Assume instance method...
            fieldMethods = getMethods(allMethods);
        }

        // Build the fault map now, while we are still single threaded, so that it can be
        // shared immutably with every copy() of this operation.
        Map<String, FaultMessageInfo> faultInfos;
        WSIFException faultInfosFailure = null;
        try {
            faultInfos = buildFaultMessageInfos();
        } catch (WSIFException | RuntimeException e) {
            // Keep the legacy failure timing - see faultMessageInfosFailure.
            Trc.ignoredException(e);
            faultInfos = Map.of();
            faultInfosFailure =
                (e instanceof WSIFException wsifException)
                    ? wsifException
                    : new WSIFException(
                        "Unable to resolve the fault messages for operation '"
                            + bindingOperationModel.getName()
                            + "'",
                        e);
        }
        fieldFaultMessageInfos = faultInfos;
        faultMessageInfosFailure = faultInfosFailure;

        if (Trc.ON)
            Trc.exit(deep());
    }

    private WSIFOperation_Java(
        Port p,
        WSIFPort_Java pj,
        BindingOperation bo,
        JavaOperation jo,
        String[] inPNames,
        String[] outPNames,
        Map<String, FaultMessageInfo> faultMsgInfos,
        Method[] m,
        Constructor[] c,
        String outMName,
        boolean isSttc,
        boolean isCnstr,
        Map tMap,
        boolean mulOP,
        Object retCl) {
        Trc.entry(
            this,
            p,
            pj,
            bo,
            jo,
            inPNames,
            outPNames,
            faultMsgInfos,
            m,
            c,
            outMName,
            Boolean.valueOf(isSttc),
            Boolean.valueOf(isCnstr),
            tMap);
        Trc.event(this, "mulOP was " + mulOP + ", retCl was " + retCl);            

        fieldPortModel = p;
        fieldPort = pj;
        fieldBindingOperationModel = bo;
        fieldJavaOperationModel = jo;
        fieldInParameterNames = inPNames;
        fieldOutParameterNames = outPNames;
        fieldFaultMessageInfos = faultMsgInfos;
        fieldMethods = m;
        fieldConstructors = c;
        fieldOutputMessageName = outMName;
        fieldIsStatic = isSttc;
        fieldIsConstructor = isCnstr;
        fieldTypeMaps = tMap;
        multiOutParts = mulOP;
        returnClass = retCl;

        if (Trc.ON)
            Trc.exit(deep());
    }
    
    /**
     * Create a new copy of this object. This is not a clone, since
     * it does not copy the referenced objects as well.
     */
    public WSIFOperation_Java copy() throws WSIFException {
        Trc.entry(this);
        WSIFOperation_Java woj =
            new WSIFOperation_Java(
                fieldPortModel,
                fieldPort,
                fieldBindingOperationModel,
                fieldJavaOperationModel,
                fieldInParameterNames,
                fieldOutParameterNames,
                fieldFaultMessageInfos,
                fieldMethods,
                fieldConstructors,
                fieldOutputMessageName,
                fieldIsStatic,
                fieldIsConstructor,
                fieldTypeMaps,
                multiOutParts,
                returnClass);
        
        woj.wsdlOperation = wsdlOperation;
        // The fault map is immutable and shared; carry over any deferred build failure
        // so the copy reports it at exactly the same point the original would have.
        woj.faultMessageInfosFailure = faultMessageInfosFailure;

        Trc.exit(woj);
        return woj;
    }

    protected static Class<?> getClassForName(String classname) throws WSIFException {
    	Trc.entry(null,classname);
    	
        Class<?> cls;

        if (classname == null) {
            throw new WSIFException("Error in getClassForName(): No class name specified!");
        }

        try {
            if (classname.lastIndexOf('.') == -1) {
                // Have to check for built in data types
                cls = switch (classname)
                {
                    case "char" -> char.class;
                    case "boolean" -> boolean.class;
                    case "byte" -> byte.class;
                    case "short" -> short.class;
                    case "int" -> int.class;
                    case "long" -> long.class;
                    case "float" -> float.class;
                    case "double" -> double.class;
                    default ->
                        // Load the class using the Thread context's class loader
                                    Class.forName(classname, true, Thread.currentThread().getContextClassLoader());
                };
            } else {
                cls =
                    Class.forName(classname, true, Thread.currentThread().getContextClassLoader());
            }
        } catch (ClassNotFoundException ex) {
        	Trc.exception(ex);
            throw new WSIFException("Could not instantiate class '" + classname + "'", ex);
        }
        Trc.exit(cls);
        return cls;
    }

    /**
     * Normalises a resolved format-binding type mapping into a list of candidate classes.
     * <p>
     * A WSDL format binding may map a single XML type onto several Java types, in which
     * case {@link #getMethodArgumentClasses()} / {@link #getMethodReturnClass()} yield a
     * {@code List} of Classes rather than a single Class.
     */
    @SuppressWarnings("unchecked")
    private static List<Class<?>> toCandidateClasses(Object mappedType) {
        if (mappedType instanceof List<?> list) {
            return (List<Class<?>>) list;
        }
        return List.of((Class<?>) mappedType);
    }

    /**
     * Fallback type equality, used only after {@code isAssignableFrom} has already failed.
     * <p>
     * The format binding is resolved through the thread context class loader, which inside
     * a container (OSGi/Karaf, app servers) is frequently not the loader that defined the
     * service class. The two Class objects are then unrelated as far as isAssignableFrom
     * is concerned even though they denote the same type, so compare names as a fallback.
     * <p>
     * Note this check used to come FIRST, so every successful match paid for a String
     * comparison that isAssignableFrom had already settled. It is now only reached on the
     * failure path.
     */
    private static boolean sameClassName(Class<?> a, Class<?> b) {
        return a.getName().equals(b.getName());
    }

    /**
     * Tests whether a value of the WSDL-mapped type can be passed to a method or
     * constructor parameter declared as {@code declaredType}.
     */
    private static boolean isAcceptableParameterType(Object mappedType, Class<?> declaredType) {
        if (mappedType == null || declaredType == null) {
            // Nothing to constrain the parameter with
            return true;
        }
        for (Class<?> candidate : toCandidateClasses(mappedType)) {
            if (declaredType.isAssignableFrom(candidate)
                || sameClassName(declaredType, candidate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests whether a method declaring a return type of {@code declaredType} can satisfy
     * the WSDL-mapped return type. A null {@code mappedType} means the binding declared no
     * returnPart, so the return value is unconstrained.
     */
    private static boolean isAcceptableReturnType(Object mappedType, Class<?> declaredType) {
        if (mappedType == null || declaredType == null) {
            return true;
        }
        for (Class<?> candidate : toCandidateClasses(mappedType)) {
            if (candidate.isAssignableFrom(declaredType)
                || sameClassName(candidate, declaredType)) {
                return true;
            }
        }
        return false;
    }

    protected Constructor<?>[] getConstructors()
        throws WSIFException {
        Trc.entry(this);
        // Get the possible constructors with the argument classes we've found.
        Constructor<?>[] constructors = fieldPort.getServiceObjectConstructors();
        Object[] args = getMethodArgumentClasses();
        List<Constructor<?>> possibles = new ArrayList<>();
        for (Constructor<?> constructor : constructors) {
            Class<?>[] params = constructor.getParameterTypes();
            if (params.length != args.length)
                continue;

            boolean match = true;
            for (int j = 0; j < params.length; j++) {
                if (!isAcceptableParameterType(args[j], params[j])) {
                    match = false;
                    break;
                }
            }
            if (match) {
                possibles.add(constructor);
            }
        }
        Constructor<?>[] candidates = possibles.toArray(new Constructor<?>[0]);
        Trc.exit(candidates);
        return candidates;
    }

    /**
     * Returns the fault message descriptors for this operation.
     * <p>
     * The map is built once during construction and is immutable, so it can be shared
     * with every {@link #copy()} and read concurrently without synchronisation.
     *
     * @return an immutable map of fault class name to {@link FaultMessageInfo}
     */
    protected Map<String, FaultMessageInfo> getFaultMessageInfos() throws WSIFException {
    	Trc.entry(this);
        if (faultMessageInfosFailure != null) {
            throw faultMessageInfosFailure;
        }
        Trc.exit(fieldFaultMessageInfos);
        return fieldFaultMessageInfos;
    }

    /**
     * Derives the fault message descriptors from the WSDL binding. Called once, from the
     * constructor.
     */
    private Map<String, FaultMessageInfo> buildFaultMessageInfos() throws WSIFException {
        // Get the current operation
        Operation operation;
        try {
            operation = getOperation();
        } catch (Exception e) {
        	Trc.exception(e);
            throw new WSIFException("Failed to get Operation", e);
        }

        Map<String, FaultMessageInfo> infos = new HashMap<>();

        Map bindingFaultModels = fieldBindingOperationModel.getBindingFaults();

        for (Object model : bindingFaultModels.values()) {
            BindingFault bindingFaultModel = (BindingFault) model;
            String name = bindingFaultModel.getName();
            if (name == null) {
                throw new WSIFException("Fault name not found in binding");
            }

            Map map = operation.getFault(name).getMessage().getParts();
            if (!map.isEmpty()) {
                Part part = (Part) map.values().iterator().next();
                QName partType = part.getTypeName();
                if (partType == null) partType = part.getElementName();
                Object formatType = fieldTypeMaps.get(partType);
                if (formatType == null) {
                    throw new WSIFException(
                        "formatType for typeName '" + part.getName() + "' not found in document");
                }

                if (formatType instanceof List<?> types) {
                    for (Object t : types) {
                        String type = (String) t;
                        // Add new fault message information to the map
                        infos.put(type, new FaultMessageInfo(name, part.getName(), type));
                    }
                } else {
                    String type = (String) formatType;
                    // Add new fault message information to the map
                    infos.put(type, new FaultMessageInfo(name, part.getName(), type));
                }
            }
        }
        return Map.copyOf(infos);
    }

    /**
     * Of all the methods on the Java class that is the backend service,
     * getMethods finds those that match this operation in the WSDL.
     * @param allMethods all the methods on the service's java class
     * @return the subset of allMethods that match this WSDL operation
     */
    protected Method[] getMethods(Method[] allMethods) throws WSIFException {
        try {
            Trc.entry(this, allMethods);

            ArrayList<Method> candidates = new ArrayList<>();

            if (!fieldIsConstructor) {

                // Java methods start with a lower case but WSDL uses either case 
                String bindingMethodName =
                    fieldJavaOperationModel.getMethodName();
                String bindingMethodName2 = null;
                if (Character.isUpperCase(bindingMethodName.charAt(0))) {
                    StringBuilder sb = new StringBuilder(bindingMethodName);
                    sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
                    bindingMethodName2 = sb.toString();
                }

                Object[] args = getMethodArgumentClasses();
                Object retClass = getMethodReturnClass();

                for (Method method : allMethods) {
                    String methodName = method.getName();
                    if (!(methodName.equals(bindingMethodName)
                        || methodName.equals(bindingMethodName2))) {
                        continue;
                    }

                    Class<?>[] params = method.getParameterTypes();
                    if (params.length != args.length)
                        continue;
                    Class<?> retType = method.getReturnType();

                    // A multi-part output message is returned as a single java.util.Map,
                    // in which case the mapped return type does not apply.
                    boolean tryAMap =
                        multiOutParts && Map.class.isAssignableFrom(retType);

                    if (!tryAMap && !isAcceptableReturnType(retClass, retType)) {
                        continue;
                    }

                    boolean match = true;
                    for (int j = 0; j < params.length; j++) {
                        if (!isAcceptableParameterType(args[j], params[j])) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        candidates.add(method);
                    }
                }
            }

            Method[] methods = candidates.toArray(new Method[0]);
            Trc.exit(methods);
            return methods;
        } catch (Exception e) {
            if (e instanceof WSIFException exception) {
                throw exception;
            } else {
                throw new WSIFException(
                    "Failure to get list of possible methods for Java service"
                        + e);
            }
        }
    }

    protected Operation getOperation() throws Exception {
    	Trc.entry(this);

        if (wsdlOperation == null) {

            // <input> and <output> tags in binding operations are not mandatory
            // so deal with null BindingInputs or BindingOutputs
            String inputName = null;
            if (fieldBindingOperationModel.getBindingInput()!=null) {
                inputName = fieldBindingOperationModel.getBindingInput().getName();
            }

            String outputName = null;
            if (fieldBindingOperationModel.getBindingOutput()!=null) {
                outputName = fieldBindingOperationModel.getBindingOutput().getName();
            }

            // Build the parts list
            //this.fieldBindingOperationModel.getBindingInput().getName(),
            wsdlOperation =
                this.fieldPortModel.getBinding().getPortType().getOperation(
                    this.fieldBindingOperationModel.getName(),
                    inputName,
                    outputName);
        }

        Trc.exit(wsdlOperation);
        return wsdlOperation;
    }

    protected Object getMethodReturnClass() throws WSIFException {
    	Trc.entry(this);
        Object methodReturnClass = null;
        try {
            String returnPartString = fieldJavaOperationModel.getReturnPart();
            List<String> parameterOrder = fieldJavaOperationModel.getParameterOrder();

			// Service with multiple output parts
            if (fieldOutParameterNames.length > 1) {
            	multiOutParts = true;
                for (String pName : fieldOutParameterNames) {
                    if (pName != null
                        && parameterOrder.contains(pName)) {
                        multiOutParts = false;
                    }
                }
            }
            
            if (returnPartString != null) {
                // A returnPart has been specified so check that this method has the correct
                // return type
                Part returnPart =
                    getOperation().getOutput().getMessage().getPart(returnPartString);

                // If there is no returnPart specified then not interested in return value
                if (returnPart != null) {
                    QName partType = returnPart.getTypeName();                    
                    if (partType == null) partType = returnPart.getElementName();
                    // Memoized on the port, so the Class.forName only happens once per
                    // part type for the whole port rather than once per operation.
                    Object obj = fieldPort.getResolvedTypeMapping(partType);
                    if (obj == null)
                        // Note the parentheses: without them the '+' bound tighter than
                        // the '?:', so the condition was "<concatenated string> == null",
                        // always false, and the whole message was discarded.
                        throw new WSIFException(
                            "Could not map type "
                                + partType
                                + " to a java type. Part name was "
                                + (returnPart.getName() == null
                                    ? "<null>" : returnPart.getName()));

                    methodReturnClass = obj;
                } else {
                    // If we get here then the return part specified on the java operation was not
                    // in the output message
                    throw new Exception(
                        "returnPart '" + returnPartString + "' was not in the output message");
                }
            }
            // returnPart attribute was not present so return methodReturnClass as default null
        } catch (Exception ex) {
        	Trc.exception(ex);
            throw new WSIFException(
                "Error while determining return class of method "
                    + fieldJavaOperationModel.getMethodName()
                    + " : The meta information is not consistent.",
                ex);
        }
        returnClass = methodReturnClass;
        Trc.exit(methodReturnClass);
        return methodReturnClass;
    }

    protected Object[] getMethodArgumentClasses() throws WSIFException {
    	Trc.entry(this);
        Object[] methodArgClasses = null;
        try {

            Operation operation = getOperation();

            /*
            The order of the parameters as passed to the Java method is
            determined in this way:
            1. from the <wsdl:operation parameterOrder="xxx yyy"> attribute if present
            2. from the <java:operation parameterOrder="xxx yyy"> attribute if present
            3. from the order the parts are specified in the input message
            */

            // Get the parameter order according to the above rules
            List<String> parameterOrder = fieldJavaOperationModel.getParameterOrder();

            if (parameterOrder == null) {
                parameterOrder = operation.getParameterOrdering();
            }

            /*
            The MessageImpl actually the order the parts were added in a List, but
            this can only be accessed via the getOrderedParts() methods which
            of course returns a List of Parts!
            So here I (rather inefficiently) convert the list of parts to a list of
            string part names.
            */
            if (parameterOrder == null) {
                List partList = operation.getInput().getMessage().getOrderedParts(null);
                parameterOrder = new ArrayList<>();
                for (Object o : partList) {
                    parameterOrder.add(((Part) o).getName());
                }
            }

            /*
            	Operations do not specify whether they are to be used with RPC-like bindings 
            	or not. However, when using an operation with an RPC-binding, it is useful to 
            	be able to capture the original RPC function signature. For this reason, 
            	a request-response or solicit-response operation MAY specify a list of parameter
            	names via the parameterOrder attribute (of type nmtokens). The value of the 
            	attribute is a list of message part names separated by a single space. 
            	The value of the parameterOrder attribute MUST follow the following rules:
            
                * The part name order reflects the order of the parameters in the RPC signature
                * The return value part is not present in the list
                * If a part name appears in both the input and output message, it is an in/out parameter
            	* If a part name appears in only the input message, it is an in parameter
            	* If a part name appears in only the output message, it is an out parameter
            
            	Note that this information serves as a "hint" and may safely be ignored by 
            	those not concerned with RPC signatures. Also, it is not required to be present, 
            	even if the operation is to be used with an RPC-like binding.
            */

            ArrayList<String> argNames = new ArrayList<>();
            ArrayList<Object> argTypes = new ArrayList<>();

            for (String param : parameterOrder) {
                Part part = operation.getInput().getMessage().getPart(param);
                if (part == null) {
                    part = operation.getOutput().getMessage().getPart(param);
                }
                if (part == null)
                    throw new Exception(
                        "Part '"
                            + param
                            + "' from parameterOrder not found in input or output message");
                argNames.add(part.getName());

                // should also check for the element
                QName partType = part.getTypeName();
                if (partType == null) partType = part.getElementName();                
                // Memoized on the port - see getResolvedTypeMapping
                Object obj = fieldPort.getResolvedTypeMapping(partType);
                if (obj == null)
                    // Note the parentheses - see the matching fix in getMethodReturnClass()
                    throw new WSIFException(
                        "Could not map type "
                            + partType
                            + " to a java type. Part name was "
                            + (part.getName() == null ? "<null>" : part.getName()));

                argTypes.add(obj);
            }

            methodArgClasses = argTypes.toArray();
            fieldInParameterNames = argNames.toArray(new String[0]);

            // Deal with output parts if operation is Request-Response
            if (operation.getStyle().equals(OperationType.REQUEST_RESPONSE)) {
                argNames = new ArrayList<>();
                // Get the returnPart attribute if it exists
                String returnPart = fieldJavaOperationModel.getReturnPart();                
                for (Object o : operation.getOutput().getMessage().getOrderedParts(null)) {
                    Part part = (Part) o;
                    String partName = part.getName();
                    if (partName != null && partName.equals(returnPart)) {
                    	// Put return part first in the list of output parts
                    	argNames.addFirst(partName);
                    } else {                    
                        argNames.add(partName);
                    }
                }                               

                // Populate an array of output message part names
				fieldOutParameterNames = argNames.toArray(new String[0]);
            } else {
            	fieldOutParameterNames = new String[0];
            }
        } catch (Exception ex) {
        	Trc.exception(ex);
            throw new WSIFException(
                "Error while determining signature of method "
                    + fieldJavaOperationModel.getMethodName()
                    + " : The meta information is not consistent.",
                ex);
        }
        Trc.exit(methodArgClasses);
        return methodArgClasses;
    }

    // Turns an array of arguments into a form compatible with a method
    // If they are compatible, the object array is populated
    // otherwise returns null
    protected Object[] getCompatibleArguments(Class<?>[] parmTypes, Object[] args) {
    	Trc.entry(this,parmTypes,args);
        // Go through each argument checking it's compatability with the method arg
        // creating a compatible set along the way.
        // In essence this just converts from String to Character when necessary
        // Also converts between WSIFAttachmentParts and DataHandlers
        // if there are further special case classes such as these which are dependent
        // on the object value, PUT THEM HERE :-)
        Class<?>[] types = (parmTypes == null) ? new Class<?>[0] : parmTypes;
        Object[] actuals = (args == null) ? new Object[0] : args;

        // Arity mismatch - this candidate cannot accept the parts. Returning null tells
        // the caller to move on to the next overload. (The loop used to index args with
        // parmTypes' length, which could blow up with ArrayIndexOutOfBoundsException.)
        if (types.length != actuals.length) {
            Trc.exit(null);
            return null;
        }

        Object[] compatibleArgs = new Object[actuals.length];
        for (int i = 0; i < types.length; i++) {
            // If the arg is a null then skip it
            if (actuals[i] == null) {
                compatibleArgs[i] = ProviderUtils.getDefaultObject(types[i]);
                continue;
            }
            // Consider the special cas, squeezing a String into a Character
            Object convertedArg = getCompatibleObject(types[i], actuals[i]);
            if (convertedArg == null) {
                // can't convert one of the arguments so return null
                Trc.exit(null);
                return null;
            } else {
                compatibleArgs[i] = convertedArg;
            }

        }
        Trc.exit(compatibleArgs);
        return compatibleArgs;
    }

    /**
     * Instantiates a holder object for an in/out parameter.
     * <p>
     * Any reflective failure is deliberately wrapped in a WSIFException rather than being
     * allowed to escape as an InvocationTargetException, which the callers reserve for
     * faults thrown by the service implementation itself.
     */
    private static Object newOutputHolder(Class<?> type) throws WSIFException {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException | RuntimeException e) {
            Trc.exception(e);
            throw new WSIFException(
                "Could not create an instance of '"
                    + type.getName()
                    + "' to hold an in/out parameter",
                e);
        }
    }

    protected Object getCompatibleReturn(Method method, Object returnObj) {
    	Trc.entry(this,method,returnObj);
    	Object o;
    	Class<?> rt = method.getReturnType();
    	Class<?> ct = null;
    	int dims = 0;
    	if (rt.isArray()) {
    		ct = rt.getComponentType();
    		dims++;
        	while (ct.isArray()) {
        		ct = ct.getComponentType();
        		dims++;
        	}
    	}
        if (returnObj instanceof java.lang.Character) {
            o = getCompatibleObject(java.lang.String.class, returnObj);
        } else if (ct != null && (ct.equals(java.lang.Character.class) || ct.equals(char.class))) {
            // Build the equally-dimensioned String array type directly. This used to
            // assemble a "[[Ljava.lang.String;" descriptor and hand it to Class.forName
            // on every call.
            Class<?> stringArrayClass = String[].class;
            for (int d = 1; d < dims; d++) {
                stringArrayClass = stringArrayClass.arrayType();
            }
            o = getCompatibleObject(stringArrayClass, returnObj);
        } else {
            o = returnObj;
        }
        Trc.exit(o);
        return o;
    }

    // Usually cls1.isAssignableFrom(cls2) returning false means you can't cast 
    // instance of cls1 to cls2. There are some special cases we need to cover ...
    // String->Character and Character->String
    // WSIFAttachmentPart->DataHandler and DataHandler->WSIFAttachmentPart
    // If a conversion is known then the obj is converted to class cls
    //   if that conversion failed, null is returned
    //   else the converted obj is returned
    // If a conversion is not known about then the obj is returned
    // Note: if you are adding other cases ensure you add both directions since the
    //       this conversion may be needed on method args AND returns
    protected Object getCompatibleObject(Class<?> cls, Object obj) {
    	Trc.entry(this,cls,obj);
    	
    	// Fast identity check first, falling back to a name comparison for the split
    	// class loader case - see sameClassName(). This was previously a bare
    	// getName().equals(), so the common case paid for a String comparison.
    	if (cls == obj.getClass() || sameClassName(cls, obj.getClass())) return obj;

        // String -> Character
        if ((cls.equals(java.lang.Character.class) || cls.equals(char.class))
            && obj.getClass().equals(java.lang.String.class)) {
            Character charArg = ProviderUtils.stringToCharacter((String) obj);
            if (charArg == null) {
                // Can't convert this string to character so return null
                Trc.exit(null);
                return null;
            }
            Trc.exit(charArg);
            return charArg;
        }
        
        // String arrays -> char/Character arrays and Character arrays -> String arrays
        if (cls.isArray() && obj.getClass().isArray()) {
        	Class<?> cct = cls.getComponentType();
        	Class<?> objct = obj.getClass().getComponentType();
        	while (cct.isArray()) {
        		cct = cct.getComponentType();
        	}
        	while (objct.isArray()) {
        		objct = objct.getComponentType();
        	}         	
        	if (objct.equals(java.lang.String.class) && cct.equals(char.class)) {
        		try {
        			Object charArray = ProviderUtils.stringArrayToCharArray(obj);
        			Trc.exit(charArray);
        			return charArray;
        		} catch (Exception e) {
        			Trc.ignoredException(e);
            		Trc.exit(null);
            		return null;
        		}
        	} else if (objct.equals(java.lang.String.class) && cct.equals(java.lang.Character.class)) {
        		try {
        			Object charArray = ProviderUtils.stringArrayToCharacterArray(obj);
        			Trc.exit(charArray);
        			return charArray;
        		} catch (Exception e) {
        			Trc.ignoredException(e);
            		Trc.exit(null);
            		return null;
        		}
        	} else if (objct.equals(java.lang.Character.class) && cct.equals(java.lang.String.class)) {
        		try {
        			Object charArray = ProviderUtils.characterArrayToStringArray(obj);
        			Trc.exit(charArray);
        			return charArray;
        		} catch (Exception e) {
        			Trc.ignoredException(e);
            		Trc.exit(null);
            		return null;
        		}
        	} else if (objct.equals(char.class) && cct.equals(java.lang.String.class)) {
        		try {
        			Object charArray = ProviderUtils.charArrayToStringArray(obj);
        			Trc.exit(charArray);
        			return charArray;
        		} catch (Exception e) {
        			Trc.ignoredException(e);
            		Trc.exit(null);
            		return null;
        		}
        	}
        }
        
        if (cls.equals(java.lang.String.class)
            && obj.getClass().equals(java.lang.Character.class)) {
            Trc.exit(obj.toString());
            return (obj.toString());
        }
        
        if (cls.equals(WSIFAttachmentPart.class)
            && obj.getClass().getName().equals("javax.activation.DataHandler")) {
            obj = new WSIFAttachmentPart(obj);
        }

        if (cls.getName().equals("javax.activation.DataHandler")
            && obj.getClass().equals(WSIFAttachmentPart.class)) {
            WSIFAttachmentPart wap = (WSIFAttachmentPart) obj;
            obj = wap.getDataHandler();
        }
        
        Trc.exit(obj);
        return obj;
    }

    protected String getOutputMessageName() throws WSIFException {
    	Trc.entry(this);
        if (fieldOutputMessageName == null) {
            BindingOutput bindingOutputModel =
                fieldBindingOperationModel.getBindingOutput();
            if (bindingOutputModel != null) {
                fieldOutputMessageName = bindingOutputModel.getName();
            }
        }
        Trc.exit(fieldOutputMessageName);
        return fieldOutputMessageName;
    }

    public WSIFPort getWSIFPort() {
        Trc.entry(this);
        Trc.exit(fieldPort);
        return fieldPort;
    }
    
    public boolean executeRequestResponseOperation(
        WSIFMessage input,
        WSIFMessage output,
        WSIFMessage fault)
        throws WSIFException {

        Trc.entry(this, input, output, fault);
        close();
        WSIFMessage ctxt = getContext();
        if (ctxt != null) {
            InvocationHelper.setMessageContext(ctxt);
            setContext(ctxt);
        }

        boolean operationSucceeded = true;
        boolean usedOutputParam = false;
        Method chosenMethod = null;

        try {
            Object result = null;
            // Need to get the stuff here because this also initializes fieldInParameterNames
            if (fieldIsConstructor) {
                if (fieldConstructors.length <= 0)
                    throw new WSIFException("No constructor found that match the parts specified");
            } else {
                if (fieldMethods.length <= 0)
                    throw new WSIFException(
                        "No method named '"
                            + fieldJavaOperationModel.getMethodName()
                            + "' found that match the parts specified");
            }

            Object[] arguments = null;
            Object part = null;
            if ((fieldInParameterNames != null)
                && (fieldInParameterNames.length > 0)) {
                arguments = new Object[fieldInParameterNames.length];
                for (int i = 0; i < fieldInParameterNames.length; i++) {
                    try {
                        part = input.getObjectPart(fieldInParameterNames[i]);
                        arguments[i] = part;
                    } catch (WSIFException e) {
                        Trc.exception(e);
                        arguments[i] = null;
                        if (fieldOutParameterNames.length > 0) {
                            // getParameterTypes() clones its array on every call, so
                            // resolve it at most once instead of once per out-parameter.
                            Class[] firstMethodParamTypes = null;
                            for (int j = 0;
                                j < fieldOutParameterNames.length;
                                j++) {
                                String outParameterName = fieldOutParameterNames[j];
                                if ((outParameterName != null)
                                    && (outParameterName
                                        .equals(fieldInParameterNames[i]))) {
                                    if (firstMethodParamTypes == null) {
                                        firstMethodParamTypes =
                                            fieldMethods[0].getParameterTypes();
                                    }
                                    arguments[i] =
                                        newOutputHolder(firstMethodParamTypes[i]);
                                    usedOutputParam = true;
                                }
                            }
                        }
                    }
                }
            }

            boolean invokedOK = false;
            if (fieldIsConstructor) {
                for (int a = 0; a < fieldConstructors.length; a++) {
                    // getParameterTypes() clones its array on every call - read it once
                    Class[] ctorParamTypes = fieldConstructors[a].getParameterTypes();
                    try {
                        // Get a set of arguments which are compatible with the ctor
                        Object[] compatibleArguments =
                            getCompatibleArguments(ctorParamTypes, arguments);
                        // The parts aren't compatible with this ctor, try the next candidate
                        if (compatibleArguments == null)
                            continue;
                        // Parts are compatible so invoke the ctor with the compatible set

                        Trc.event(
                            this,
                            "Invoking constructor ",
                            fieldConstructors[a],
                            " with arguments ",
                            compatibleArguments);

                        result =
                            fieldConstructors[a].newInstance(compatibleArguments);

                        Trc.event(
                            this,
                            "Returned from constructor, result is ",
                            result);

                        fieldPort.setObjectReference(result);
                        invokedOK = true;
                        break;
                    } catch (IllegalArgumentException ia) {
                        Trc.ignoredException(ia);
                        // Ignore and try next constructor
                    }
                }
                if (!invokedOK)
                    throw new WSIFException("Failed to call constructor for object in Java operation");
                // Side effect: Initialize port's object reference
            } else {
                if (fieldIsStatic) {
                    for (int a = 0; a < fieldMethods.length; a++) {
                        // getParameterTypes() clones its array on every call - read it
                        // once per candidate rather than inside the nested loops below
                        Class[] methodParamTypes =
                            fieldMethods[a].getParameterTypes();
                        if (usedOutputParam) {
                            for (int i = 0;
                                i < fieldInParameterNames.length;
                                i++) {
                                String outParameterName = null;
                                for (int j = 0;
                                    j < fieldOutParameterNames.length;
                                    j++) {
                                    outParameterName =
                                        fieldOutParameterNames[j];
                                    if ((outParameterName != null)
                                        && (outParameterName
                                            .equals(fieldInParameterNames[i]))) {
                                        arguments[i] =
                                            newOutputHolder(methodParamTypes[i]);
                                    }
                                }
                            }
                        }
                        try {
                            // Get a set of arguments which are compatible with the method
                            Object[] compatibleArguments =
                                getCompatibleArguments(methodParamTypes, arguments);
                            // The parts aren't compatible with this method, try the next one
                            if (compatibleArguments == null)
                                continue;
                            // Parts are compatible so invoke the method with the compatible set

                            Trc.event(
                                this,
                                "Invoking method ",
                                fieldMethods[a],
                                " with arguments ",
                                compatibleArguments);

                            result =
                                fieldMethods[a].invoke(null, compatibleArguments);

                            Trc.event(
                                this,
                                "Returned from method, result is ",
                                result);

                            chosenMethod = fieldMethods[a];
                            invokedOK = true;
                            break;
                        } catch (IllegalArgumentException ia) {
                            Trc.ignoredException(ia);
                            // Ignore and try next method
                        }
                    }
                    if (!invokedOK)
                        throw new WSIFException(
                            "Failed to invoke method '"
                                + fieldJavaOperationModel.getMethodName()
                                + "'");
                } else {
                    for (int a = 0; a < fieldMethods.length; a++) {
                        // getParameterTypes() clones its array on every call - read it
                        // once per candidate rather than inside the nested loops below
                        Class[] methodParamTypes =
                            fieldMethods[a].getParameterTypes();
                        if (usedOutputParam) {
                            for (int i = 0;
                                i < fieldInParameterNames.length;
                                i++) {
                                String outParameterName = null;
                                for (int j = 0;
                                    j < fieldOutParameterNames.length;
                                    j++) {
                                    outParameterName =
                                        fieldOutParameterNames[j];
                                    if ((outParameterName != null)
                                        && (outParameterName
                                            .equals(fieldInParameterNames[i]))) {
                                        arguments[i] =
                                            newOutputHolder(methodParamTypes[i]);
                                    }
                                }
                            }
                        }
                        try {
                            // Get a set of arguments which are compatible with the method
                            Object[] compatibleArguments =
                                getCompatibleArguments(methodParamTypes, arguments);
                            // The parts aren't compatible with this method, try the next one
                            if (compatibleArguments == null)
                                continue;
                            // Parts are compatible so invoke the method with the compatible set

                            Object objRef = fieldPort.getObjectReference();
                            Trc.event(
                                this,
                                "Invoking object ",
                                objRef,
                                " method ",
                                fieldMethods[a],
                                " with arguments ",
                                compatibleArguments);

                            result =
                                fieldMethods[a].invoke(objRef, compatibleArguments);

                            Trc.event(
                                this,
                                "Returned from method, result is ",
                                result);

                            chosenMethod = fieldMethods[a];
                            invokedOK = true;
                            break;
                        } catch (IllegalArgumentException ia) {
                            Trc.ignoredException(ia);
                            // Ignore and try next method
                        }
                    }
                    if (!invokedOK)
                        throw new WSIFException(
                            "Failed to invoke method '"
                                + fieldJavaOperationModel.getMethodName()
                                + "'");
                }

                // Deal with the output message
                String outParameterName;
                if (fieldOutParameterNames.length == 1) {
                    // Only one output part - it must be the object returned by the
                    // Java service invocation
                    output.setName(getOutputMessageName());
                    outParameterName = fieldOutParameterNames[0];
                    if (outParameterName != null) {
                        output.setObjectPart(
                            outParameterName,
                            getCompatibleReturn(chosenMethod, result));
                    }
                } else if (fieldOutParameterNames.length > 1) {
                    if (multiOutParts) {
                        if (Map
                            .class
                            .isAssignableFrom(chosenMethod.getReturnType())) {
                            // Method should have returned a Map
                            if (!(result instanceof Map<?, ?> returnedMap)) {
                                throw new WSIFException(
                                    "Operation "
                                        + getOperation().getName()
                                        + " defined as returning multiple parts "
                                        + "and the Java method did not return an instance of java.util.Map");
                            }

                            output.setName(getOutputMessageName());

                            // Get multiple output parts from the map

                            for (String pName : fieldOutParameterNames) {
                                if (returnedMap.containsKey(pName)) {
                                    Object outPart = returnedMap.get(pName);
                                    Message outputMessage =
                                        getOperation().getOutput().getMessage();
                                    Part wsdlPart =
                                        outputMessage.getPart(pName);
                                    QName partType = wsdlPart.getTypeName();
                                    if (partType == null) {
                                        partType = wsdlPart.getElementName();
                                    }
                                    Object typeObj =
                                        fieldPort.getResolvedTypeMapping(partType);
                                    if (typeObj != null) {
                                        // A type may map onto several Java types; the
                                        // first is the binding's primary choice. This
                                        // used to cast straight to String and so threw
                                        // ClassCastException for such a mapping.
                                        Class<?> c =
                                            (typeObj instanceof List<?> mapped)
                                                ? (Class<?>) mapped.getFirst()
                                                : (Class<?>) typeObj;
                                        Object outPart2 =
                                            getCompatibleObject(c, outPart);
                                        output.setObjectPart(pName, outPart2);
                                    } else {
                                        throw new WSIFException(
                                            "The Class of part "
                                                + pName
                                                + ", returned by the Java method for"
                                                + " operation "
                                                + getOperation().getName()
                                                + " does not match the Class defined in the format "
                                                + "binding for that part.");
                                    }
                                } else {
                                    // A part was not found in the map. Check the return
                                    // class. If it's a Map, just add the map to the output
                                    // message. If not, throw an exception to say the map
                                    // does not contain the missing part.
                                    if (returnClass instanceof Class<?> class1
                                        && Map.class.isAssignableFrom(
                                            class1)) {
                                        Map<String, Object> m = new HashMap<>();
                                        m.put(
                                            fieldOutParameterNames[0],
                                            result);
                                        output.setParts(m);
                                        break;
                                    }
                                    throw new WSIFException(
                                        "Operation "
                                            + getOperation().getName()
                                            + " defined as returning multiple parts."
                                            + " Part "
                                            + pName
                                            + " was missing from the Map returned by "
                                            + "the Java service class");
                                }
                            }

                        } else {
                            // Backwards compatiblity - method returns just the output part specified
                            // by returnPart
                            output.setName(getOutputMessageName());
                            outParameterName =
                                (String) fieldOutParameterNames[0];
                            if (outParameterName != null) {
                                output.setObjectPart(
                                    outParameterName,
                                    getCompatibleReturn(chosenMethod, result));
                            }
                        }
                    } else {
                        // Method has returned one object and updated the references of
                        // other objects passed into the method.             
                        output.setName(getOutputMessageName());

                        // Add the returned object to the output message
                        outParameterName = (String) fieldOutParameterNames[0];
                        if (outParameterName != null) {
                            output.setObjectPart(
                                outParameterName,
                                getCompatibleReturn(chosenMethod, result));
                        }

                        // Deal with objects whose reference has been updated by the Java
                        // service method. Parts for such objects will appear in the WSDL
                        // output message and their names will also be included in the 
                        // parameterOrder attribute
                        if (arguments != null) {
                            for (int i = 1;
                                i < fieldOutParameterNames.length;
                                i++) {
                                outParameterName = fieldOutParameterNames[i];
                                if (outParameterName != null) {
                                    try {
                                        for (int r = 0;
                                            r < fieldInParameterNames.length;
                                            r++) {
                                            if (outParameterName
                                                .equals(fieldInParameterNames[r])) {
                                                output.setObjectPart(
                                                    outParameterName,
                                                    arguments[r]);
                                                break;
                                            }
                                        }
                                    } catch (WSIFException e) {
                                        Trc.ignoredException(e);
                                        //ignore
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (InvocationTargetException ex) {
            Trc.exception(ex);
            Throwable invocationFault = ex.getTargetException();
            String className = invocationFault.getClass().getName();
            Map<String, FaultMessageInfo> faultMessageInfos = getFaultMessageInfos();
            FaultMessageInfo faultMessageInfo = faultMessageInfos.get(className);
            if ((faultMessageInfo != null)
                && (faultMessageInfo.fieldPartName != null)) { // Found fault
                Object faultPart = invocationFault;
                // Should we use the class of the method signature here ?
                fault.setObjectPart(faultMessageInfo.fieldPartName, faultPart);
                fault.setName(faultMessageInfo.fieldMessageName);
                if (faultMessageInfo.fieldMessageName != null) {
                    Fault wsdlFault =
                        fieldBindingOperationModel.getOperation().getFault(
                            faultMessageInfo.fieldMessageName);
                    if (wsdlFault != null) {
                        fault.setMessageDefinition(wsdlFault.getMessage());
                    }
                }
                operationSucceeded = false;
            } else {
                // Try to find a matching class:
                Class<?> invocationFaultClass = invocationFault.getClass();
                boolean found = false;
                for (FaultMessageInfo info : faultMessageInfos.values()) {
                    faultMessageInfo = info;
                    try {
                        Class<?> tempClass =
                            Class.forName(
                                faultMessageInfo.fieldFormatType,
                                true,
                                Thread.currentThread().getContextClassLoader());
                        if (tempClass.isAssignableFrom(invocationFaultClass)) {
                            found = true;
                            Object faultPart = invocationFault;
                            // Should we use the class of the method signature here ?
                            fault.setObjectPart(
                                faultMessageInfo.fieldPartName,
                                faultPart);
                            fault.setName(faultMessageInfo.fieldMessageName);
                            if (faultMessageInfo.fieldMessageName != null) {
                                Fault wsdlFault =
                                    fieldBindingOperationModel
                                        .getOperation()
                                        .getFault(
                                        faultMessageInfo.fieldMessageName);
                                if (wsdlFault != null) {
                                    fault.setMessageDefinition(
                                        wsdlFault.getMessage());
                                }
                            }
                            operationSucceeded = false;
                        }
                    } catch (Exception exc) { // Nothing to do - just try the next one...
                        Trc.ignoredException(exc);
                    }
                }
                if (!found) {
                    throw new WSIFException(
                        "Operation failed!",
                        invocationFault);
                }
            }
        } catch (Exception ex) {
            Trc.exception(ex);

            // Log message
            MessageLogger.log(
                "WSIF.0005E",
                "Java",
                fieldJavaOperationModel.getMethodName());

            throw new WSIFException(
                this
                    + " : Could not invoke '"
                    + fieldJavaOperationModel.getMethodName()
                    + "'",
                ex);
        }

        Trc.exit(operationSucceeded);
        return operationSucceeded;
    }

    public void executeInputOnlyOperation(WSIFMessage input) throws WSIFException {

        Trc.entry(this, input);
        close();
        WSIFMessage ctxt = getContext();
        if (ctxt != null) {
            InvocationHelper.setMessageContext(ctxt);
            setContext(ctxt);
        }

        try {
            Object result = null;
            // Need to get the stuff here because this also initializes fieldInParameterNames
            if (fieldIsConstructor) {
                if (fieldConstructors.length <= 0)
                    throw new WSIFException("No constructors found that match the parts specified");
            } else {
                if (fieldMethods.length <= 0)
                    throw new WSIFException("No methods named '"
                        + fieldJavaOperationModel.getMethodName()
                        + "' found that match the parts specified");
            }

            Object[] arguments = null;
            Object part;
            if ((fieldInParameterNames != null) && (fieldInParameterNames.length > 0)) {
                arguments = new Object[fieldInParameterNames.length];
                for (int i = 0; i < fieldInParameterNames.length; i++) {
                   try {
                        part = input.getObjectPart(fieldInParameterNames[i]);
                        arguments[i] = part;
                    } catch (WSIFException e) {
                     	Trc.exception(e);
                        arguments[i] = null;
                    }
                }
            }

            boolean invokedOK = false;

            if (fieldIsConstructor) {
                for (int a = 0; a < fieldConstructors.length; a++) {
                    // getParameterTypes() clones its array on every call - read it once
                    Class[] ctorParamTypes = fieldConstructors[a].getParameterTypes();
                    try {
                        // Get a set of arguments which are compatible with the ctor
                        Object[] compatibleArguments =
                            getCompatibleArguments(ctorParamTypes, arguments);
                        // The parts aren't compatible with this ctor, try the next candidate
                        if (compatibleArguments == null)
                            continue;
                        // Parts are compatible so invoke the ctor with the compatible set

                        Trc.event(
                            this,
                            "Invoking constructor ",
                            fieldConstructors[a],
                            " with arguments ",
                            compatibleArguments);

                        result =
                            fieldConstructors[a].newInstance(
                                compatibleArguments);

                        Trc.event(
                            this,
                            "Returned from constructor, result is ",
                            result);

                        fieldPort.setObjectReference(result);
                        invokedOK = true;
                        break;
                    } catch (IllegalArgumentException ia) {
                    	Trc.ignoredException(ia);
                        // Ignore and try next constructor
                    }
                }
                if (!invokedOK)
                    throw new WSIFException("Failed to call constructor for object in Java operation");
            } else {
                if (fieldIsStatic) {
                    for (int a = 0; a < fieldMethods.length; a++) {
                        // getParameterTypes() clones its array on every call - read it once
                        Class[] methodParamTypes = fieldMethods[a].getParameterTypes();
                        try {
                            // Get a set of arguments which are compatible with the method
                            Object[] compatibleArguments =
                                getCompatibleArguments(methodParamTypes, arguments);
                            // The parts aren't compatible with this method, try the next one
                            if (compatibleArguments == null)
                                continue;
                            // Parts are compatible so invoke the method with the compatible set

                            Trc.event(
                                this,
                                "Invoking method ",
                                fieldMethods[a],
                                " with arguments ",
                                compatibleArguments);

                            result =
                                fieldMethods[a].invoke(
                                    null,
                                    compatibleArguments);

                            Trc.event(
                                this,
                                "Returned from method, result is ",
                                result);

                            invokedOK = true;
                            break;
                        } catch (IllegalArgumentException ia) {
                        	Trc.ignoredException(ia);
                            // Ignore and try next method
                        }
                    }
                    if (!invokedOK)
                        throw new WSIFException(
                            "Failed to invoke method '" + fieldJavaOperationModel.getMethodName() + "'");
                } else {
                    for (int a = 0; a < fieldMethods.length; a++) {
                        // getParameterTypes() clones its array on every call - read it once
                        Class[] methodParamTypes = fieldMethods[a].getParameterTypes();
                        try {
                            // Get a set of arguments which are compatible with the method
                            Object[] compatibleArguments =
                                getCompatibleArguments(methodParamTypes, arguments);
                            // The parts aren't compatible with this method, try the next one
                            if (compatibleArguments == null)
                                continue;
                            // Parts are compatible so invoke the method with the compatible set

                            Object objRef = fieldPort.getObjectReference();
                            Trc.event(
                                this,
                                "Invoking object ",
                                objRef,
                                " method ",
                                fieldMethods[a],
                                " with arguments ",
                                compatibleArguments);

                            result =
                                fieldMethods[a].invoke(
                                    objRef,
                                    compatibleArguments);

                            Trc.event(
                                this,
                                "Returned from method, result is ",
                                result);

                            invokedOK = true;
                            break;
                        } catch (IllegalArgumentException ia) {
                        	Trc.ignoredException(ia);
                            // Ignore and try next method
                        }
                    }
                    if (!invokedOK)
                        throw new WSIFException(
                            "Failed to invoke method '" + fieldJavaOperationModel.getMethodName() + "'");
                }
            }
        } catch (InvocationTargetException ex) {
            Trc.exception(ex);

            // Log message
            MessageLogger.log(
                "WSIF.0005E",
                "Java",
                fieldJavaOperationModel.getMethodName());

            throw new WSIFException(
                this
                    + " : Invocation of '"
                    + fieldJavaOperationModel.getMethodName()
                    + "' failed.",
                ex);
        } catch (Exception ex) {
            Trc.exception(ex);

            // Log message
            MessageLogger.log(
                "WSIF.0005E",
                "Java",
                fieldJavaOperationModel.getMethodName());

            throw new WSIFException(
                this
                    + " : Could not invoke '"
                    + fieldJavaOperationModel.getMethodName()
                    + "'",
                ex);
        }

        Trc.exit();
    }
    
    public String deep() {
        StringBuilder buff = new StringBuilder();
        try {
            buff.append(super.toString()).append(":\n");
            buff.append("portModel:").append(Trc.brief(fieldPortModel));
            buff.append(" wsifPort_Java:").append(fieldPort);
            buff.append(" bindingOperationModel:").append(Trc.brief(fieldBindingOperationModel));
            buff.append(" JavaOperation:").append(fieldJavaOperationModel);

            buff.append(Trc.brief("inParameterNames", fieldInParameterNames));
            buff.append(Trc.brief("outParameterNames", fieldOutParameterNames));

            if (fieldFaultMessageInfos == null) {
                buff.append(" faultMessageInfos:null");
            } else {
                int i = 0;
                for (Map.Entry<String, FaultMessageInfo> entry
                        : fieldFaultMessageInfos.entrySet()) {
                    buff.append(" faultMessageInfos[")
                        .append(i++)
                        .append("]:")
                        .append(entry.getKey())
                        .append(" ")
                        .append(entry.getValue());
                }
            }

            buff.append(Trc.brief("methods", fieldMethods));
            buff.append(Trc.brief("constructors", fieldConstructors));

            buff.append(" outputMessageName:").append(fieldOutputMessageName);
            buff.append(" isStatic:").append(fieldIsStatic);
            buff.append(" isConstructor:").append(fieldIsConstructor);

            if (fieldTypeMaps == null) {
                buff.append(" faultTypeMaps:null");
            } else {
                int i = 0;
                for (Object o : fieldTypeMaps.entrySet()) {
                    Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
                    buff.append(" typeMaps[")
                        .append(i++)
                        .append("]:")
                        .append(entry.getKey())
                        .append(" ")
                        .append(entry.getValue());
                }
            }
        } catch (Exception e) {
            Trc.exceptionInTrace(e);
        }

        return buff.toString();
    }

	/**
	 * Override default serialization
	 */
    @Serial
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }

	/**
	 * Override default deserialization
	 */
    @Serial
    private void readObject(ObjectInputStream ois)
        throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        
        // Re-establish all the Method information
        Method[] allMethods = fieldPort.getServiceObjectMethods();
        if (fieldIsConstructor) {
        	fieldConstructors = getConstructors();
        } else {
            fieldMethods = getMethods(allMethods);
        }
    }       
}

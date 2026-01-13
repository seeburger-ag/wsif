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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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
    protected Map fieldFaultMessageInfos = null;
    transient protected Method[] fieldMethods = null;
    transient protected Constructor[] fieldConstructors = null;
    protected String fieldOutputMessageName = null;
    protected boolean fieldIsStatic = false;
    protected boolean fieldIsConstructor = false;
    protected Map fieldTypeMaps = null;
    protected boolean multiOutParts = false;
    transient private Object returnClass = null;

    private class FaultMessageInfo {
        String fieldMessageName;
        String fieldPartName;
        String fieldFormatType;
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
        Map faultMsgInfos,
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
        
        Trc.exit(woj);
        return woj;
    }

    protected static Class getClassForName(String classname) throws WSIFException {
    	Trc.entry(null,classname);
    	
        Class cls = null;

        if (classname == null) {
            throw new WSIFException("Error in getClassForName(): No class name specified!");
        }

        try {
            if (classname.lastIndexOf('.') == -1) {
                // Have to check for built in data types
                if (classname.equals("char")) {
                    cls = char.class;
                } else if (classname.equals("boolean")) {
                    cls = boolean.class;
                } else if (classname.equals("byte")) {
                    cls = byte.class;
                } else if (classname.equals("short")) {
                    cls = short.class;
                } else if (classname.equals("int")) {
                    cls = int.class;
                } else if (classname.equals("long")) {
                    cls = long.class;
                } else if (classname.equals("float")) {
                    cls = float.class;
                } else if (classname.equals("double")) {
                    cls = double.class;
                } else {
                    // Load the class using the Thread context's class loader
                    cls =
                        Class.forName(classname, true, Thread.currentThread().getContextClassLoader());
                }
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

    protected Constructor[] getConstructors()
        throws WSIFException {
        Trc.entry(this);
        Constructor[] candidates;
        // Get the possible constructors with the argument classes we've found.
        Constructor[] constructors = fieldPort.getServiceObjectConstructors();
        Object[] args = getMethodArgumentClasses();
        Vector possibles = new Vector();
        for (int i = 0; i < constructors.length; i++) {
            Class[] params = constructors[i].getParameterTypes();
            if (params.length != args.length)
                continue;

            boolean match = true;
            for (int j = 0; j < params.length; j++) {
                Object obj = args[j];
                if (obj instanceof Vector vec) {
                    boolean found = false;
                    for (int p = 0; p < vec.size(); p++) {
                        Class cl = (Class) vec.get(p);
                        if (cl.getName().equals(params[j].getName())) {
                            found = true;
                            break;
                        } else if (params[j].isAssignableFrom(cl)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        match = false;
                        break;
                    }
                } else {
                    if (!(((Class) obj).getName().equals(params[j].getName()))
                        && !(params[j].isAssignableFrom((Class) obj))) {
                        match = false;
                        break;
                    }
                }
            }
            if (match) {
                possibles.addElement(constructors[i]);
            }
        }
        candidates = new Constructor[possibles.size()];
        for (int k = 0; k < candidates.length; k++) {
            candidates[k] = (Constructor) possibles.get(k);
        }
        Trc.exit(candidates);
        return candidates;
    }

    protected Map getFaultMessageInfos() throws WSIFException {
    	Trc.entry(this);
        // Get the current operation
        Operation operation = null;
        try {
            operation = getOperation();
        } catch (Exception e) {
        	Trc.exception(e);
            throw new WSIFException("Failed to get Operation", e);
        }

        if (fieldFaultMessageInfos == null) {
            fieldFaultMessageInfos = new HashMap();
        }

        BindingFault bindingFaultModel = null;
        Map bindingFaultModels = fieldBindingOperationModel.getBindingFaults();
        List parts = null;
        Iterator modelsIterator = bindingFaultModels.values().iterator();

        while (modelsIterator.hasNext()) {
            bindingFaultModel = (BindingFault) modelsIterator.next();
            String name = bindingFaultModel.getName();
            if (name == null) {
                throw new WSIFException("Fault name not found in binding");
            }

            Map map = operation.getFault(name).getMessage().getParts();
            if (map.size() >= 1) {
                Part part = (Part) map.values().iterator().next();
                QName partType = part.getTypeName();
                if (partType == null) partType = part.getElementName();
                Object formatType = fieldTypeMaps.get(partType);
                if (formatType == null) {
                    throw new WSIFException(
                        "formatType for typeName '" + part.getName() + "' not found in document");
                }

                if (formatType instanceof Vector types) {
                    Enumeration enum_ = types.elements();
                    while (enum_.hasMoreElements()) {
                        String type = (String) enum_.nextElement();
                        // Add new fault message information to the map
                        fieldFaultMessageInfos.put(
                            type,
                            new FaultMessageInfo(name, part.getName(), type));
                    }
                } else {
                    String type = (String) formatType;
                    // Add new fault message information to the map
                    fieldFaultMessageInfos.put(
                        type,
                        new FaultMessageInfo(name, part.getName(), type));
                }
            }
        }
        Trc.exit(fieldFaultMessageInfos);
        return fieldFaultMessageInfos;
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

            ArrayList candidates = new ArrayList();

            if (!fieldIsConstructor) {

                // Java methods start with a lower case but WSDL uses either case 
                String bindingMethodName =
                    fieldJavaOperationModel.getMethodName();
                String bindingMethodName2 = null;
                if (Character.isUpperCase(bindingMethodName.charAt(0))) {
                    StringBuffer sb = new StringBuffer(bindingMethodName);
                    sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
                    bindingMethodName2 = sb.toString();
                }

                Object[] args = getMethodArgumentClasses();
                Object retClass = getMethodReturnClass();

                for (int i = 0; i < allMethods.length; i++) {
                    String methodName = allMethods[i].getName();
                    if (!(methodName.equals(bindingMethodName)
                        || methodName.equals(bindingMethodName2))) {
                        continue;
                    }

                    Class[] params = allMethods[i].getParameterTypes();
                    if (params.length != args.length)
                        continue;
                    Class retType = allMethods[i].getReturnType();

                    boolean tryAMap = false;
                    if (multiOutParts) {
                        Class mapClass = java.util.Map.class;
                        boolean found = false;
                        if (mapClass.getName().equals(retType.getName())) {
                            tryAMap = true;
                        } else if (mapClass.isAssignableFrom(retType)) {
                            tryAMap = true;
                        }
                    }
                    if (!tryAMap) {
                        if (retClass != null && retClass instanceof Vector vec) {
                            boolean found = false;
                            for (int p = 0; p < vec.size(); p++) {
                                Class cl = (Class) vec.get(p);
                                if (cl.getName().equals(retType.getName())) {
                                    found = true;
                                    break;
                                } else if (cl.isAssignableFrom(retType)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found)
                                continue;
                        } else {
                            if (retType != null && retClass != null) {
                                if (!(((Class) retClass)
                                    .getName()
                                    .equals(retType.getName()))
                                    && !(((Class) retClass)
                                        .isAssignableFrom(retType)))
                                    continue;
                            }
                        }
                    }

                    boolean match = true;
                    for (int j = 0; j < params.length; j++) {
                        Object obj = args[j];
                        if (obj instanceof Vector vec) {
                            boolean found = false;
                            for (int p = 0; p < vec.size(); p++) {
                                Class cl = (Class) vec.get(p);
                                if (cl.getName().equals(params[j].getName())) {
                                    found = true;
                                    break;
                                } else if (params[j].isAssignableFrom(cl)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                match = false;
                                break;
                            }
                        } else {
                            if (!(((Class) obj)
                                .getName()
                                .equals(params[j].getName()))
                                && !(params[j].isAssignableFrom((Class) obj))) {
                                match = false;
                                break;
                            }
                        }
                    }
                    if (match) {
                        candidates.add(allMethods[i]);
                    }
                }
            }

            Method[] methods =
                (Method[]) candidates.toArray(new Method[candidates.size()]);
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
            List parameterOrder = fieldJavaOperationModel.getParameterOrder();

			// Service with multiple output parts
            if (fieldOutParameterNames.length > 1) {
            	multiOutParts = true;
                for (int p = 0; p < fieldOutParameterNames.length; p++) {
                    String pName = fieldOutParameterNames[p];
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
                    Object obj = this.fieldTypeMaps.get(partType);
                    if (obj == null)
                        throw new WSIFException(
                            "Could not map type "
                                + partType
                                + " to a java type. Part name was "
                                + returnPart.getName() == null 
                                    ? "<null>" : returnPart.getName());
                                
                    if (obj instanceof Vector v) {
                        Vector argv = new Vector();
                        Enumeration enum_ = v.elements();
                        while (enum_.hasMoreElements()) {
                            String cls = (String) enum_.nextElement();
                            argv.addElement(getClassForName(cls));
                        }
                        methodReturnClass = argv;
                    } else {
                        methodReturnClass =
                            getClassForName((String) obj);
                    }
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
            List parameterOrder = null;

            parameterOrder = fieldJavaOperationModel.getParameterOrder();

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
                parameterOrder = new Vector();
                Iterator partListIterator = partList.iterator();
                while (partListIterator.hasNext()) {
                    Part part = (Part) partListIterator.next();
                    parameterOrder.add(part.getName());
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

            ArrayList argNames = new ArrayList();
            ArrayList argTypes = new ArrayList();

            Iterator parameterIterator = parameterOrder.iterator();
            while (parameterIterator.hasNext()) {
                String param = (String) parameterIterator.next();
                Part part = (Part) operation.getInput().getMessage().getPart(param);
                if (part == null) {
                    part = (Part) operation.getOutput().getMessage().getPart(param);
                }
                if (part == null)
                    throw new Exception(
                        "Part '"
                            + param
                            + "' from parameterOrder not found in input or output message");
                argNames.add((String) part.getName());

                // should also check for the element
                QName partType = part.getTypeName();;
                if (partType == null) partType = part.getElementName();                
                Object obj = this.fieldTypeMaps.get(partType);
                if (obj == null)
                    throw new WSIFException(
                        "Could not map type "
                            + partType
                            + " to a java type. Part name was "
                            + part.getName() == null ? "<null>" : part.getName());
                
                if (obj instanceof Vector v) {
                    Vector argv = new Vector();
                    Enumeration enum_ = v.elements();
                    while (enum_.hasMoreElements()) {
                        String cls = (String) enum_.nextElement();
                        argv.addElement(getClassForName(cls));
                    }
                    argTypes.add(argv);
                } else {
                    argTypes.add(getClassForName((String)obj));
                }

            }

            methodArgClasses = argTypes.toArray();
            fieldInParameterNames = new String[argNames.size()];
            for (int i = 0; i < argNames.size(); i++) {
                fieldInParameterNames[i] = (String) argNames.get(i);
            }

            // Deal with output parts if operation is Request-Response
            if (operation.getStyle().equals(OperationType.REQUEST_RESPONSE)) {
                argNames = new ArrayList();
                // Get the returnPart attribute if it exists
                String returnPart = fieldJavaOperationModel.getReturnPart();                
                Iterator outputPartsIterator =
                    operation.getOutput().getMessage().getOrderedParts(null).iterator();
                while (outputPartsIterator.hasNext()) {
                    Part part = (Part) outputPartsIterator.next();
                    String partName = part.getName();
                    if (partName != null && returnPart != null && partName.equals(returnPart)) {
                    	// Put return part first in the list of output parts
                    	argNames.addFirst(partName);
                    } else {                    
                        argNames.add((String) part.getName());
                    }
                }                               

                // Populate an array of output message part names
				fieldOutParameterNames = new String[argNames.size()];
                for (int i = 0; i < argNames.size(); i++) {
                    fieldOutParameterNames[i] = (String) argNames.get(i);
                }
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
    protected Object[] getCompatibleArguments(Class[] parmTypes, Object[] args) {
    	Trc.entry(this,parmTypes,args);
        // Go through each argument checking it's compatability with the method arg
        // creating a compatible set along the way.
        // In essence this just converts from String to Character when necessary
        // Also converts between WSIFAttachmentParts and DataHandlers
        // if there are further special case classes such as these which are dependent
        // on the object value, PUT THEM HERE :-)
        if (args == null || parmTypes == null) {
            Object[] compatibleArgs = new Object[0];
            return compatibleArgs;
        }

        Object[] compatibleArgs = new Object[args.length];
        for (int i = 0; i < parmTypes.length; i++) {
            // If the arg is a null then skip it
            if (args[i] == null) {
                compatibleArgs[i] = ProviderUtils.getDefaultObject(parmTypes[i]);
                continue;
            }
            // Consider the special cas, squeezing a String into a Character
            Object convertedArg = getCompatibleObject(parmTypes[i], args[i]);
            if (convertedArg == null) {
                // can't convert one of the arguments so return null
                return null;
            } else {
                compatibleArgs[i] = convertedArg;
            }

        }
        Trc.exit(compatibleArgs);
        return compatibleArgs;
    }

    protected Object getCompatibleReturn(Method method, Object returnObj) {
    	Trc.entry(this,method,returnObj);
    	Object o = null;
    	Class rt = method.getReturnType();
    	Class ct = null;
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
        	String stringArrayClassName = "[Ljava.lang.String;";
        	for (int d=1; d<dims; d++) {
        		stringArrayClassName = "["+stringArrayClassName;
        	}
        	try {
        		Class stringArrayClass = Class.forName(stringArrayClassName, true, Thread.currentThread().getContextClassLoader());
        		o = getCompatibleObject(stringArrayClass, returnObj);
        	} catch(ClassNotFoundException cnf) {
        		Trc.ignoredException(cnf);
        		o = returnObj;
        	}
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
    protected Object getCompatibleObject(Class cls, Object obj) {
    	Trc.entry(this,cls,obj);
    	
    	if (cls.getName().equals(obj.getClass().getName())) return obj;
    	  	
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
        	Class cct = cls.getComponentType();
        	Class objct = obj.getClass().getComponentType();
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
                            String outParameterName = null;
                            for (int j = 0;
                                j < fieldOutParameterNames.length;
                                j++) {
                                outParameterName = fieldOutParameterNames[j];
                                if ((outParameterName != null)
                                    && (outParameterName
                                        .equals(fieldInParameterNames[i]))) {
                                    arguments[i] =
                                        (fieldMethods[0]
                                            .getParameterTypes()[i])
                                            .newInstance();
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
                    try {
                        // Get a set of arguments which are compatible with the ctor
                        Object[] compatibleArguments =
                            getCompatibleArguments(
                                fieldConstructors[a].getParameterTypes(),
                                arguments);
                        // If we didn't get any arguments then the parts aren't compatible with the ctor
                        if (compatibleArguments == null)
                            break;
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
                                            (fieldMethods[a].getParameterTypes()[i])
                                                .newInstance();
                                    }
                                }
                            }
                        }
                        try {
                            // Get a set of arguments which are compatible with the method
                            Object[] compatibleArguments =
                                getCompatibleArguments(
                                    fieldMethods[a].getParameterTypes(),
                                    arguments);
                            // If we didn't get any arguments then the parts aren't compatible with the method
                            if (compatibleArguments == null)
                                break;
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
                                            (fieldMethods[a].getParameterTypes()[i])
                                                .newInstance();
                                    }
                                }
                            }
                        }
                        try {
                            // Get a set of arguments which are compatible with the method
                            Object[] compatibleArguments =
                                getCompatibleArguments(
                                    fieldMethods[a].getParameterTypes(),
                                    arguments);
                            // If we didn't get any arguments then the parts aren't compatible with the method
                            if (compatibleArguments == null)
                                break;
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
                String outParameterName = null;
                if (fieldOutParameterNames.length == 1) {
                    // Only one output part - it must be the object returned by the
                    // Java service invocation
                    output.setName(getOutputMessageName());
                    outParameterName = (String) fieldOutParameterNames[0];
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
                            if (!(result instanceof Map)) {
                                throw new WSIFException(
                                    "Operation "
                                        + getOperation().getName()
                                        + " defined as returning multiple parts "
                                        + "and the Java method did not return an instance of java.util.Map");
                            }

                            Map returnedMap = (Map) result;
                            output.setName(getOutputMessageName());

                            // Get multiple output parts from the map

                            for (int p = 0;
                                p < fieldOutParameterNames.length;
                                p++) {
                                String pName = fieldOutParameterNames[p];
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
                                        this.fieldTypeMaps.get(partType);
                                    if (typeObj != null) {
                                        Class c =
                                            getClassForName((String) typeObj);
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
                                    if (returnClass != null
                                        && returnClass instanceof Class class1
                                        && Map.class.isAssignableFrom(
                                            class1)) {
                                        Map m = new HashMap();
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
            Map faultMessageInfos = getFaultMessageInfos();
            FaultMessageInfo faultMessageInfo =
                (FaultMessageInfo) faultMessageInfos.get(className);
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
                Class invocationFaultClass = invocationFault.getClass();
                Class tempClass = null;
                Iterator it = faultMessageInfos.values().iterator();
                boolean found = false;
                while (it.hasNext()) {
                    faultMessageInfo = (FaultMessageInfo) it.next();
                    try {
                        tempClass =
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
            Object part = null;
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
                    try {
                        // Get a set of arguments which are compatible with the ctor
                        Object[] compatibleArguments =
                            getCompatibleArguments(fieldConstructors[a].getParameterTypes(), arguments);
                        // If we didn't get any arguments then the parts aren't compatible with the ctor
                        if (compatibleArguments == null)
                            break;
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
                        try {
                            // Get a set of arguments which are compatible with the method
                            Object[] compatibleArguments =
                                getCompatibleArguments(fieldMethods[a].getParameterTypes(), arguments);
                            // If we didn't get any arguments then the parts aren't compatible with the method
                            if (compatibleArguments == null)
                                break;
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
                        try {
                            // Get a set of arguments which are compatible with the method
                            Object[] compatibleArguments =
                                getCompatibleArguments(fieldMethods[a].getParameterTypes(), arguments);
                            // If we didn't get any arguments then the parts aren't compatible with the method
                            if (compatibleArguments == null)
                                break;
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
        String buff = "";
        try {
            buff = new String(super.toString() + ":\n");
            buff += "portModel:" + Trc.brief(fieldPortModel);
            buff += " wsifPort_Java:" + fieldPort;
            buff += " bindingOperationModel:" + Trc.brief(fieldBindingOperationModel);
            buff += " JavaOperation:" + fieldJavaOperationModel;

            buff += Trc.brief("inParameterNames", fieldInParameterNames);
            buff += Trc.brief("outParameterNames", fieldOutParameterNames);          

            if (fieldFaultMessageInfos == null) {
                buff += " faultMessageInfos:null";
            } else {
                Iterator it = fieldFaultMessageInfos.keySet().iterator();
                int i = 0;
                while (it.hasNext()) {
                    String key = (String) it.next();
                    buff += " faultMessageInfos["
                        + i
                        + "]:"
                        + key
                        + " "
                        + fieldFaultMessageInfos.get(key);
                    i++;
                }
            }

            buff += Trc.brief("methods", fieldMethods);          
            buff += Trc.brief("constructors", fieldConstructors);          

            buff += " outputMessageName:" + fieldOutputMessageName;
            buff += " isStatic:" + fieldIsStatic;
            buff += " isConstructor:" + fieldIsConstructor;

            if (fieldTypeMaps == null) {
                buff += " faultTypeMaps:null";
            } else {
                Iterator it = fieldTypeMaps.keySet().iterator();
                int i = 0;
                while (it.hasNext()) {
                    QName key = (QName) it.next();
                    buff += " typeMaps[" + i + "]:" + key + " " + fieldTypeMaps.get(key);
                    i++;
                }
            }
        } catch (Exception e) {
            Trc.exceptionInTrace(e);
        }

        return buff;
    }

	/**
	 * Override default serialization
	 */
    private void writeObject(ObjectOutputStream oos) throws IOException {	
        oos.defaultWriteObject();
    }

	/**
	 * Override default deserialization
	 */
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

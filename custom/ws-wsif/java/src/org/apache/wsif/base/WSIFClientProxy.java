/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
 * Business Machines, Inc., http://www.ibm.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.wsif.base;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.compiler.util.TypeMapping;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.providers.ProviderUtils;
import org.apache.wsif.providers.WSIFDynamicTypeMap;
import org.apache.wsif.providers.WSIFDynamicTypeMapping;
import org.apache.wsif.util.WSIFUtils;

/**
 * WSIFClientProxy is a dynamic proxy (or stub) used by the 
 * WSIFServiceImpl when the application is using the stubs to 
 * invoke the web service. A WSIFClientProxy is created using the 
 * static newInstance method. A WSIFClientProxy dynamically implements
 * exactly one interface passed by the application. This class
 * invokes the web service using the WSIFOperation and WSIFPort 
 * interfaces and so is independent of any provider implementation.
 * Operation overloading is supported.
 * 
 * @author Owen Burroughs <owenb@apache.org>
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 * @author Nirmal Mukhi <nmukhi@apache.org>
 */
public class WSIFClientProxy implements InvocationHandler {
    protected Class iface = null;
    protected Definition def = null;
    protected String serviceNS = null;
    protected String serviceName = null;
    protected String portTypeNS = null;
    protected String portTypeName = null;
    protected WSIFDynamicTypeMap typeMap = null;
    protected Map simpleTypeReg = null;
    protected PortType portType = null;
    protected WSIFPort wsifport = null;
    protected Object proxy = null;
    private Map wsdlOperationTable = null;

    /**
     * Factory method to create a new dynamic proxy.
     * @param iface the user interface that is to be dynamically implemented
     * @param def the WSDL definition
     * @param serviceNS WSDL service namespace
     * @param serviceName WSDL service name
     * @param portTypeNS WSDL port type namespace
     * @param portTypeName WSDL port type name
     * @param typeMap table of mappings between XML and Java types
     * @return the new WSIFClientProxy
     * @exception WSIFException 
     */
    public static WSIFClientProxy newInstance(
        Class iface,
        Definition def,
        String serviceNS,
        String serviceName,
        String portTypeNS,
        String portTypeName,
        WSIFDynamicTypeMap typeMap)
        throws WSIFException {
        Trc.entry(
            null,
            iface,
            def,
            serviceNS,
            serviceName,
            portTypeNS,
            portTypeName,
            typeMap);

        if (!iface.isInterface())
            throw new WSIFException(
                "Cannot get a stub for " + iface + " because it is not an interface");

        WSIFClientProxy clientProxy =
            new WSIFClientProxy(
                iface,
                def,
                serviceNS,
                serviceName,
                portTypeNS,
                portTypeName,
                typeMap);

        Object proxy =
            Proxy.newProxyInstance(
                iface.getClassLoader(),
                new Class[] { iface },
                clientProxy);

        clientProxy.setProxy(proxy);

        if (Trc.ON)
            Trc.exit(clientProxy.deep());
        return clientProxy;
    }

    /**
     * Private constructor because newInstance() should be used instead.
     */
    private WSIFClientProxy(
        Class iface,
        Definition def,
        String serviceNS,
        String serviceName,
        String portTypeNS,
        String portTypeName,
        WSIFDynamicTypeMap typeMap)
        throws WSIFException {
        Trc.entry(
            this,
            iface,
            def,
            serviceNS,
            serviceName,
            portTypeNS,
            portTypeName,
            typeMap);
        this.iface = iface;
        this.def = def;
        this.serviceNS = serviceNS;
        this.serviceName = serviceName;
        this.portTypeNS = portTypeNS;
        this.portTypeName = portTypeName;
        this.typeMap = typeMap;
        this.portType = WSIFUtils.selectPortType(def, portTypeNS, portTypeName);

        simpleTypeReg = WSIFUtils.getSimpleTypesMap();
        wsdlOperationTable = new HashMap();
        Trc.exit();
    }

    private void setProxy(Object proxy) {
        this.proxy = proxy;
    }
    
    public Object getProxy() {
        return this.proxy;
    }

    /**
     * Select which port to use for this proxy.
     */
    public void setPort(WSIFPort wsifport) {
        Trc.entry(this, wsifport);
        this.wsifport = wsifport;
        Trc.exit();
    }

    /**
     * Invoke a user method. The java proxy support calls this method.
     */
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
        Trc.entry(this, method, args); // Tracing proxy cause a hang

        Operation operation = findMatchingOperation(method, args);

        // Now set up the input and output messages.      
        Input input = operation.getInput();
        Output output = operation.getOutput();
        String inputName = (input == null) ? null : input.getName();
        String outputName = (output == null) ? null : output.getName();
        Message inputMessage = (input == null) ? null : input.getMessage();
        Message outputMessage = (output == null) ? null : output.getMessage();

        WSIFOperation wsifOperation =
            wsifport.createOperation(method.getName(), inputName, outputName);

        // make the msg names for compiled msgs xxxAnt ask Mark why diff from inputName 
        String inputMsgName = "";
        if (input != null) {
            Message m = input.getMessage();
            if (m != null) {
                QName qn = m.getQName();
                inputMsgName = (qn == null) ? "" : qn.getLocalPart();
            }
        }

        String outputMsgName = "";
        if (output != null) {
            Message m = output.getMessage();
            if (m != null) {
                QName qn = m.getQName();
                outputMsgName = (qn == null) ? "" : qn.getLocalPart();
            }
        }

        // There must be an inputMessage.
        WSIFMessage wsifInputMessage =
            wsifOperation.createInputMessage(inputMsgName);

        // There may not be an output message.
        WSIFMessage wsifOutputMessage = null;
        WSIFMessage wsifFaultMessage = null;
        if (output != null) {
            wsifOutputMessage = wsifOperation.createOutputMessage(inputMsgName);
            wsifFaultMessage = wsifOperation.createFaultMessage(inputMsgName);
        }

        boolean usingwrapped = false;
        List inputParts =
            (inputMessage == null)
                ? new ArrayList()
                : inputMessage.getOrderedParts(null);
        if (args != null) {
            usingwrapped =
                unWrapIfRequired(operation.getName(), inputParts, args);
            Iterator partIt = inputParts.iterator();
            for (int argIndex = 0; partIt.hasNext(); argIndex++) {
                Part part = (Part) partIt.next();
                String partName = part.getName();
                wsifInputMessage.setObjectPart(partName, args[argIndex]);
            }
        }

        if (output == null)
            wsifOperation.executeInputOnlyOperation(wsifInputMessage);
        else {
            boolean success =
                wsifOperation.executeRequestResponseOperation(
                    wsifInputMessage,
                    wsifOutputMessage,
                    wsifFaultMessage);
            if (!success) {
                Iterator it = wsifFaultMessage.getParts();
                while (it.hasNext()) {
                    Object fault = it.next();
                    if (fault instanceof Throwable throwable) {
                        Class[] exs = method.getExceptionTypes();
                        for (int e = 0; e < exs.length; e++) {
                            if (exs[e].isAssignableFrom(fault.getClass())) {
                                throw throwable;
                            }
                        }
                        throw new WSIFException("Operation failed, fault message contains a non-declared throwable", 
                            throwable);
                    } else {
                        throw new WSIFException(
                            "Operation failed and a non-throwable fault message part was returned: "
                                + fault);
                    }
                }
                throw new WSIFException("Operation failed but returned fault message contained no part");
            }
        }

        // Copy the output part out of the message. 
        Object result = null;
        if (outputMessage != null) {
            List outputParts = outputMessage.getOrderedParts(null);
            if (outputParts != null && outputParts.size() > 0) {
                if (usingwrapped || isWrappedInContext()) {
                    unwrap(operation.getName() + "Response", outputParts);
                }

                // The return value is always the first output part
                Iterator outPartIt = outputParts.iterator();
                Part returnPart = (Part) outPartIt.next();
                result = wsifOutputMessage.getObjectPart(returnPart.getName());

                // Are there any inout parts? Multiple output-only parts
                // are not allowed in java. Skip over input-only parts in the message.
                if (outPartIt.hasNext()) {
                    Object[] inPartArr =
                        inputMessage.getOrderedParts(null).toArray();
                    Part nextOutPart = (Part) outPartIt.next();

                    for (int argIndex = 0;
                        argIndex < args.length;
                        argIndex++) {
                        if (((Part) (inPartArr[argIndex]))
                            .getName()
                            .equals(nextOutPart.getName())) {
                            args[argIndex] =
                                wsifOutputMessage.getObjectPart(
                                    nextOutPart.getName());
                            if (outPartIt.hasNext())
                                nextOutPart = (Part) outPartIt.next();
                            else
                                break; // No more output parameters
                        }
                    } // end for
                }
            }
        }
        Trc.exit(result);
        return result;
    }

    /**
     * Find an operation in the list that matches the method and arguments.
     * 
     * Java only allows one output-only parameter and that is the return 
     * value. Java also does not allow overloading based on the return value.
     * Consequently we only look at the input parameters when deciding 
     * which overloaded operation to pick.
     * 
     * If the user invoked an overloaded method, MyMethod(null) seems to be 
     * ambiguous. However it is not since java forces the user to cast the 
     * null to one of the types that are valid on the method. So the invoke
     * method on our client proxy gets passed args[0]==null which is not typed.
     * However method.getParameterTypes()[0] is the type that java picked to 
     * invoke. So we use getParameterTypes() to choose the operation, as well 
     * as args[i].getClass().
     * 
     * We also use args[i].getClass() to choose the operation in case
     * getParameterTypes()[i].equals(Object.class) (no match) but 
     * args[i].getClass() is the class specified in the operation.
     * 
     * The typeMap only contains complexTypes, so this class also uses the 
     * simpleTypeReg for simple types (int, string, etc).
     * 
     * We compare the class in the mapping with the one from types using
     * isAssignableFrom() not equals() because we allow the user to pass
     * a subclass.
     *
     * If there are two methods MyMethod(Address) and MyMethod(SubAddress) 
     * then MyMethod(new SubAddress()) would match both methods. So if we 
     * find an operation which exactly matches the method we return it. 
     * But if we find an operation whose types are assignable from the 
     * method's types, we carry on searching for an exact match. If we fail 
     * to find an exact match then we return the "assignable" match. There 
     * is a problem if there are multiple "assignable" matches and no exact 
     * match as would happen if MyMethod(SubSubAddress) where SubSubAddress
     * extends Address. This code does not cope with that case and it is a 
     * known restriction (bug).
     * 
     * If the WSDL is correct, we do not expect that there will be multiple 
     * exact matches, so we do not test for this.
     */
    private Operation findMatchingOperation(Method method, Object[] args)
        throws WSIFException {
        	
        // create a key consisting of the method and these args
        String key = createWSDLOperationKey(method, args);
        
        // check if we have found an operation matching the signature of this 
        // invocation before
        Operation previousOp = (Operation) wsdlOperationTable.get(key);
        if (previousOp != null) {
            return previousOp;
        }
        
        // Check here that the method is in the interface iface
        Method[] allMethods = iface.getMethods();
        int i;
        for (i = 0; i < allMethods.length; i++)
            if (allMethods[i].equals(method))
                break;
        if (i >= allMethods.length || !method.equals(allMethods[i]))
            throw new WSIFException(
                "Method "
                    + method.getName()
                    + " is not in interface "
                    + iface.getName());

        String methodName = method.getName();
        Class[] types = method.getParameterTypes();
        List opList = portType.getOperations();
        Iterator opIt = opList.iterator();
        Operation matchingOperation = null;

        // First try to find this method in the portType's list of operations.
        // Be careful of overloaded operations.
        while (opIt.hasNext()) {
            Operation operation = (Operation) opIt.next();
            // If the method name doesn't match the operation name this isn't the operation
            if (!methodName.equalsIgnoreCase(operation.getName()))
                continue;

            Input input = operation.getInput();
            Message inputMessage = (input == null) ? null : input.getMessage();
            List inputParts = (inputMessage == null) 
               ? new ArrayList() 
               : inputMessage.getOrderedParts(null);
            
            int numInputParts = inputParts.size();

            // Check for a match if neither args nor the operation has any parameters
            if (numInputParts == 0 && types.length == 0) {
                wsdlOperationTable.put(key, operation);
                return operation;
            }

            // No match if there are different numbers of parameters
            if (isWrappedInContext() || (types != null && (numInputParts != types.length))) {
            	if (ProviderUtils.isUnwrapable(operation)) {
                    unWrapIfRequired(operation.getName(), inputParts, args);
                    numInputParts = inputParts.size();
                    if (numInputParts != types.length) {
                        continue;
                    }
            	} else {
            		continue;
            	}
            }

            // Go through all the parameters making sure all their datatypes match
            Iterator partIt = inputParts.iterator();
            boolean foundAllArgs = true;
            boolean exactMatchAllArgs = true;
            for (int argIndex = 0;
                partIt.hasNext() && foundAllArgs;
                argIndex++) {
                	
                Part part = (Part) partIt.next();
                QName partTypeName = part.getTypeName();
                if (partTypeName==null) {
                    partTypeName = part.getElementName();
                }

                /* for wrapped document literal operations AXIS uses a wrapper
                 * element class with ">" prefixed to the namespace local part
                 */
                QName partTypeNameWrapped = 
                   new QName(partTypeName.getNamespaceURI(), ">" + partTypeName.getLocalPart());

                boolean foundThisArg = false;
                boolean exactMatchThisArg = false;

                // Look this parameter up in the typeMap.
                for (Iterator mapIt = typeMap.iterator();
                    mapIt.hasNext() && !foundThisArg;
                    ) {
                    WSIFDynamicTypeMapping mapping =
                        (WSIFDynamicTypeMapping) mapIt.next();
                    if (mapping.getXmlType().equals(partTypeName)
                    || (mapping.getXmlType().equals(partTypeNameWrapped))) {
                        if (mapping
                            .getJavaType()
                            .isAssignableFrom(types[argIndex])
                            || (args[argIndex] != null
                                && mapping.getJavaType().isAssignableFrom(
                                    args[argIndex].getClass()))) {
                            foundThisArg = true;
                            if (mapping.getJavaType().equals(types[argIndex])
                                || (args[argIndex] != null
                                    && mapping.getJavaType().equals(
                                        args[argIndex].getClass())))
                                exactMatchThisArg = true;
                        }
                    }
                }

                // Look for a simple type that matches
                String simpleType =
                    (String) (simpleTypeReg.get(partTypeName));
                if (!foundThisArg) {
                    if (simpleType != null) {
                        if (types[argIndex].toString().equals(simpleType)) {
                            // this works for simple types (float, int)
                            foundThisArg = true;
                            exactMatchThisArg = true;
                        } else
                            try // this works for String, Date
                                {
                                Class simpleClass =
                                    Class.forName(
                                        simpleType,
                                        true,
                                        Thread
                                            .currentThread()
                                            .getContextClassLoader());
                                if (simpleClass
                                    .isAssignableFrom(types[argIndex])) {
                                    foundThisArg = true;
                                    if (simpleClass.equals(types[argIndex]))
                                        exactMatchThisArg = true;
                                }
                            } catch (ClassNotFoundException ignored) {
                                Trc.ignoredException(ignored);
                            }
                    } else if (types[argIndex].equals(DataHandler.class))
                        // There is no (simple or complex) type mapping for 
                        // this argument. If it's a DataHandler, then assume
                        // it's a mime type, since we do automatic registering
                        // of DataHandlers for Mime types. We should really look
                        // in the WSDL binding to make sure it is a mime part.
                        foundThisArg = true;
                }

                if (!foundThisArg)
                    foundAllArgs = false;
                if (!exactMatchThisArg)
                    exactMatchAllArgs = false;
            }

            if (foundAllArgs) {
                if (exactMatchAllArgs) {
                    wsdlOperationTable.put(key, operation);
                    return operation;
                }

                // if matchingOperation!=null then write trace statement.
                matchingOperation = operation;
            }
        } // end while

        if (matchingOperation != null) {
            wsdlOperationTable.put(key, matchingOperation);
            return matchingOperation;
        }

        // if we get here then we haven't found a matching operation       
        String argString = new String();
        if (types != null)
            for (i = 0; i < types.length; i++) {
                if (i != 0)
                    argString += ", ";
                argString += types[i];
            }

        throw new WSIFException(
            "Method "
                + methodName
                + "("
                + argString
                + ") was not found in portType "
                + portType.getQName());
    }

    /**
     * Create a key consisting of the method name and the types of all the args
     */
    private String createWSDLOperationKey(Method method, Object[] args) {
        Trc.entry(this, method, args);

        StringBuffer sb = new StringBuffer();
        sb.append(method.getName()).append(":");

        Class[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++)
            sb.append(types[i].getName()).append(":");

        if (args != null)
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null)
                    sb.append("null");
                else
                    sb.append(args[i].getClass().getName());
                sb.append(":");
            }

        Trc.exit(sb.toString());
        return sb.toString();
    }

    /**
     * Create a key consisting of all names concatenated
     */
    private String createWSIFOperationKey(
        String operationName,
        String inputName,
        String outputName) {
        Trc.entry(this,operationName,inputName,outputName);
        
        StringBuffer sb = new StringBuffer();
        sb.append(operationName).append(inputName).append(outputName);
        
        Trc.exit(sb.toString());
        return sb.toString();
    }

    protected boolean isUnwrapRequired() {
    	boolean unwrapRequired = false;
    	try {
        	WSIFMessage ctx = wsifport.getContext();
        	String style = (String) ctx.getObjectPart(WSIFConstants.CONTEXT_OPERATION_STYLE);
        	unwrapRequired = WSIFConstants.CONTEXT_OPERATION_STYLE_WRAPPED.equals(style);
    	} catch (WSIFException e) {
    		Trc.ignoredException(e);
    	}
    	return unwrapRequired;
    }

	/**
	 * Unwraps the top level element if this a wrapped message.
	 */
	private boolean unWrapIfRequired(String name, List parts, Object[] args)
		throws WSIFException {
		boolean unwrapped = false;
        boolean unwrap = isWrappedInContext();
        if (!unwrap) { 
        	// TODO: try to work out from part types
        	// for now just hack
        	unwrap = args.length != parts.size();
        } 
        if (unwrap) {
        	unwrapped = unwrap(name, parts);
        }
        return unwrapped;
	}


	private boolean unwrap(String name, List parts) throws WSIFException {
		boolean unwrapped = false;
   	    Part p = ProviderUtils.getWrapperPart(parts, name);
    	if (p != null) {
	        List unWrappedParts = ProviderUtils.unWrapPart(p, def, wsifport.getContext());
		    parts.remove(p);
		    parts.addAll(unWrappedParts);
		    unwrapped = true;
	    }
	    return unwrapped;
	}
	
	private boolean isWrappedInContext() throws WSIFException {
        WSIFMessage context = wsifport.getContext();
        String style = null;
        try {
            style =
                (String) context.getObjectPart(
                    WSIFConstants.CONTEXT_OPERATION_STYLE);
        } catch (WSIFException e) {
        	Trc.ignoredException(e);
        }
        boolean wrappedInContext; 
        if (WSIFConstants.CONTEXT_OPERATION_STYLE_WRAPPED.equals(style)) {
        	wrappedInContext = true;
        } else {
        	wrappedInContext = false;
        }
		return wrappedInContext;
	}

    public String deep() {
        String buff = "";
        try {
            buff = new String(this.toString() + "\n");
            buff += "iface: " + iface;
            buff += " def: " + Trc.brief(def);
            buff += " serviceNS: " + serviceNS;
            buff += " serviceName: " + serviceName;
            buff += " portTypeNS: " + portTypeNS;
            buff += " portTypeName: " + portTypeName;
            buff += " typeMap: " + typeMap;
            buff += "\nsimpleTypeReg: " + simpleTypeReg;
            buff += "\nportType: " + Trc.brief(portType);
            buff += " wsifport: " + wsifport;

            // Can't trace proxy here because it causes a hang.
            //buff += "proxy: " + proxy;
        } catch (Exception e) {
            Trc.exceptionInTrace(e);
        }
        return buff;
    }
}

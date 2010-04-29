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

package models.javaProvider;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.wsdl.BindingOperation;
import javax.wsdl.extensions.ExtensibilityElement;

import org.apache.wsif.WSIFException;
import org.apache.wsif.providers.ModelWSIFOperation;
import org.apache.wsif.providers.ModelWSIFPort;
import org.apache.wsif.wsdl.extensions.java.JavaOperation;

/**
 * @author <a href="mailto:ant.elder@uk.ibm.com">Ant Elder</a>
 */
public class JavaWSIFOperation extends ModelWSIFOperation {

    protected String methodName;
    protected String methodType;

    protected static final ArrayList validMethodTypes =
        new ArrayList(
            Arrays.asList(
                new String[] { "instance", "static", "constructor" }));

    public JavaWSIFOperation(ModelWSIFPort wsifPort, BindingOperation bop)
        throws WSIFException {
        super(wsifPort, bop);
    }

    protected Class getOperationExtensibilityClass() {
        return JavaOperation.class;
    }

    protected void validateOperationExtensibilityElement(ExtensibilityElement ee)
        throws WSIFException {
        JavaOperation javaOperation = (JavaOperation) ee;

        this.methodName = javaOperation.getMethodName();
        if (methodName == null || methodName.length() < 1) {
            methodName = bindingOperation.getName();
        }

        this.methodType = javaOperation.getMethodType();
        if (methodType == null || methodType.length() < 1) {
            methodType = "instance";
        } else if (!validMethodTypes.contains(methodType)) {
            throw new WSIFException("invalid methodType: " + methodType);
        }

        List partNames = javaOperation.getParameterOrder();
        if (partNames != null) {
            setInputPartOrder(partNames);
        }

        String returnPartName = javaOperation.getReturnPart();
        if (returnPartName != null && returnPartName.length() > 0) {
            SetReturnPart(returnPartName);
        }

    }

    protected boolean doInvokeRequestResponse(List inputArgs, List responseArgs)
        throws WSIFException {
        boolean workedOK = false;

        Object target;
        if ("static".equalsIgnoreCase(methodType)) {
            target = null;
        } else {
            target = ((JavaWSIFPort) wsifPort).getObjectReference();
        }

        Object response;
        try {
            response = getMethod().invoke(target, inputArgs.toArray());
        } catch (Throwable e) {
            throw new WSIFException(
                "exception invoking method: " + e.getLocalizedMessage(),
                e);
        }

        responseArgs.add(response);
        workedOK = true;

        return workedOK;
    }

    protected Method getMethod() throws WSIFException {
        Method method = null;

        JavaWSIFPort p = (JavaWSIFPort) wsifPort;
        Method[] methods = p.getServiceObjectMethods();
        for (int i = 0; method == null && i < methods.length; i++) {
            if (methods[i].getName().equals(methodName)) {
                if (isMethodArgsOK(methods[i])) {
                    method = methods[i];
                }
            }
        }

        if (method == null) {
            throw new WSIFException(
                "no method on target object named: " + methodName);
        }

        return method;
    }

    protected boolean isMethodArgsOK(Method m) throws WSIFException {
        boolean ok = false;

        if (m.getParameterTypes().length == getInputParts().size()) {
            //TODO: check types
            ok = true;
        }

        return ok;
    }

}

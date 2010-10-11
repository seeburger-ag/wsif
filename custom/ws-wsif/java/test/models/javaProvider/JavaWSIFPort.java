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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.extensions.ExtensibilityElement;

import org.apache.wsif.WSIFException;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.providers.ModelWSIFOperation;
import org.apache.wsif.providers.ModelWSIFPort;
import org.apache.wsif.providers.WSIFDynamicTypeMap;
import org.apache.wsif.wsdl.extensions.java.JavaAddress;

/**
 * @author <a href="mailto:ant.elder@uk.ibm.com">Ant Elder</a>
 */
public class JavaWSIFPort extends ModelWSIFPort {

    protected String targetObjectClassName;
    transient private Class targetObjectClass;
    transient private Method[] targetObjectMethods;
    transient private Constructor[] targetObjectConstructors;

    transient private java.lang.Object targetObject;

    public JavaWSIFPort(Definition def, Port port, WSIFDynamicTypeMap typeMap)
        throws WSIFException {
        super(def, port, typeMap);
        setFormatBindingSupported(true);
    }

    protected ModelWSIFOperation makeWSIFOperation(
        ModelWSIFPort port,
        BindingOperation bop)
        throws WSIFException {
        return new JavaWSIFOperation(port, bop);
    }

    protected Class getImplementedAddressClass() {
        return JavaAddress.class;
    }

    protected void validateAddress(ExtensibilityElement ee) {
        JavaAddress address = (JavaAddress) ee;
        targetObjectClassName = address.getClassName();
    }

    public void close() {
        targetObject = null;
    }

    /**
     * Gets the Java class of the service object
     * @return Class   the class of the service object
     */
    Class getTargetObjectClass() throws WSIFException {
        // no method access modifier as it used by WSIFOperation_Java
        if (targetObjectClass == null) {
            try {
                targetObjectClass =
                    Class.forName(
                        targetObjectClassName,
                        true,
                        Thread.currentThread().getContextClassLoader());
            } catch (Throwable ex) {
                Trc.exception(ex);
                throw new WSIFException(
                    "Exception getting target object class '"
                        + targetObjectClassName
                        + "': "
                        + ex.getLocalizedMessage());
            }
        }
        return targetObjectClass;
    }

    /**
     * Gets the constructors of the service object
     * @return Constructor[]   the constructors of the service object
     */
    Constructor[] getServiceObjectConstructors() throws WSIFException {
        // no method access modifier as it used by JavaWSIFOperation
        Trc.entry(this);
        if (targetObjectConstructors == null) {
            Class c = getTargetObjectClass();
            targetObjectConstructors = c.getConstructors();
        }
        Trc.exit(targetObjectConstructors);
        return targetObjectConstructors;
    }

    /**
     * Gets the methods of the service object
     * @return Method[]   the methods of the service object
     */
    Method[] getServiceObjectMethods() throws WSIFException {
        // no method access modifier as it used by WSIFOperation_Java
        Trc.entry(this);
        if (targetObjectMethods == null) {
            Class c = getTargetObjectClass();
            targetObjectMethods = c.getMethods();
        }
        Trc.exit(targetObjectMethods);
        return targetObjectMethods;
    }

    /**
     * Gets the service object.
     * @return Object   the service object instance
     */
    public Object getObjectReference() throws WSIFException {
        Trc.entry(this);
        if (targetObject == null) {
            Class c = getTargetObjectClass();
            try {
                targetObject = c.newInstance();
            } catch (Throwable ex) {
                Trc.exception(ex);
                throw new WSIFException(
                    "exception instantiating target object: "
                        + ex.getLocalizedMessage());
            }
        }
        Trc.exit(targetObject);
        return targetObject;
    }

    /**
     * Sets a new instance of the target service object
     * Used by WSIFOperation_Java when it runs a Constructor
     * instead of an instance method. 
     */
    void setTargetObject(Object targetObject) {
        Trc.entry(this, targetObject);
        this.targetObject = targetObject;
        Trc.exit();
    }

    /**
     * Bypass WSIFDynamicProxy with direct access to the target object
     */
    public Object getStub(Class iface) throws WSIFException {
        Trc.entry(this);
        Object stub = null;
        Object o = getObjectReference();
        if (iface.isAssignableFrom(o.getClass())) {
            stub = o;
        }
        Trc.exit(stub);
        return stub;
    }

}
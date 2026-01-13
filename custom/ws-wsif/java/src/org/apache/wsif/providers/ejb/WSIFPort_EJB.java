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

package org.apache.wsif.providers.ejb;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.extensions.ExtensibilityElement;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.providers.WSIFDynamicTypeMap;
import org.apache.wsif.util.WSIFUtils;
import org.apache.wsif.wsdl.extensions.ejb.EJBAddress;

/**
 * EJB WSIF Port.
 * @author <a href="mailto:gpfau@de.ibm.com">Gerhard Pfau</a>
 * Partially based on WSIFPort_ApacheSOAP from Alekander Slominski,
 * Paul Fremantle, Sanjiva Weerawarana and Matthew J. Duftler
 * @author Owen Burroughs <owenb@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 */
public class WSIFPort_EJB
    extends org.apache.wsif.base.WSIFDefaultPort
    implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private javax.wsdl.Definition fieldDefinition = null;
    private javax.wsdl.Port fieldPortModel = null;
    transient private EJBHome fieldEjbHome = null; // 'factory for physical connection'
    transient private EJBObject fieldEjbObject = null; // 'physical connection'
    private final boolean separatedEJBRefs; // DO NOT INITIALIZE UNTIL CONSTRUCTOR
    transient private Class ejbObjectClass;
    transient private Method[] ejbObjectMethods = null;    

    transient protected Map operationInstances = new HashMap();

    public WSIFPort_EJB(Definition def, Port port, WSIFDynamicTypeMap typeMap) {
        Trc.entry(this, def, port, typeMap);
        
		 // Flag to indicate that the EJB references are written after this object
		 // when this object is serialized to a stream. Older versions of this class may
		 // not do this and so the flag will be missing in the stream and default to false
		 // when the object is deserialized (flag must not be initialized where declared
		 // because it is final and would not default to false!)
		separatedEJBRefs = true;

        fieldDefinition = def;
        fieldPortModel = port;

        Trc.exit();
    }

    public Definition getDefinition() {
        return fieldDefinition;
    }

    public WSIFOperation_EJB getDynamicWSIFOperation(
        String name,
        String inputName,
        String outputName)
        throws WSIFException {
        Trc.entry(this, name, inputName, outputName);

        WSIFOperation_EJB tempOp =
            (WSIFOperation_EJB) operationInstances.get(getKey(name, inputName, outputName));

		WSIFOperation_EJB operation = null;
		if (tempOp != null) {
			operation = tempOp.copy();
		}

        if (operation == null) {
            BindingOperation bindingOperationModel =
               WSIFUtils.getBindingOperation( 
                  fieldPortModel.getBinding(), name, inputName, outputName );

            if (bindingOperationModel != null) {
                operation = new WSIFOperation_EJB(fieldPortModel, bindingOperationModel, this);
                setDynamicWSIFOperation(name, inputName, outputName, operation);
            }
        }

        Trc.exit(operation);
        return operation;
    }

    public EJBHome getEjbHome() throws WSIFException {
        Trc.entry(this);

        if (fieldEjbHome == null) {
            EJBAddress address = null;

            try {
                ExtensibilityElement portExtension =
                    (ExtensibilityElement) fieldPortModel.getExtensibilityElements().getFirst();

                if (portExtension == null) {
                    throw new WSIFException("missing port extension");
                }

                address = (EJBAddress) portExtension;

                Hashtable hash = new Hashtable();

                // If the initial context factory is specified in the wsdl use it
                String icf = address.getInitialContextFactory();
                if (icf != null) {
                    hash.put(InitialContext.INITIAL_CONTEXT_FACTORY, icf);
                }

                // If the jndi provider url is specified in the wsdl use it
                String providerURL = address.getJndiProviderURL();
                if (providerURL != null) {
                    hash.put(InitialContext.PROVIDER_URL, providerURL);
                }

                // Lookup from an authoritative source								
                hash.put(InitialContext.AUTHORITATIVE, "true");

                InitialContext initContext;
                initContext = new InitialContext(hash);
                
                Class homeClass = null;
                try {
                    if (address.getClassName() != null) {
                        homeClass =
                            Class.forName(
                                address.getClassName(),
                                true,
                                Thread.currentThread().getContextClassLoader());
                        if (!(EJBHome.class.isAssignableFrom(homeClass))) {
                            homeClass = null;
                        }
                    }
                } catch (ClassNotFoundException cnf) {
                	Trc.ignoredException(cnf);
                }
                
                if (homeClass != null) {
                	Object tempHome = initContext.lookup(address.getJndiName());
                	if (homeClass.isAssignableFrom(tempHome.getClass())) {
                		fieldEjbHome = (EJBHome) tempHome;
                	} else {
                	    fieldEjbHome = (EJBHome) PortableRemoteObject.narrow(tempHome, homeClass);
                	}
                } else {
                	fieldEjbHome = (EJBHome) initContext.lookup(address.getJndiName());
                }

            } catch (Exception ex) {
            	Trc.exception(ex);
                throw new WSIFException(
                    "Failed to lookup EJB home using JNDI name '" + address.getJndiName() + "'",
                    ex);
            }
        }
        Trc.exit(fieldEjbHome);
        return fieldEjbHome;
    }   

    public EJBObject getEjbObject() throws WSIFException {
        Trc.entry(this);

        if (fieldEjbObject == null) {
            EJBHome ejbHome = getEjbHome();
            try {
                Method createMethod = ejbHome.getClass().getDeclaredMethod("create", null);
                fieldEjbObject = (EJBObject) createMethod.invoke(ejbHome, null);
            } catch (Exception ex) {
            	Trc.exception(ex);
                throw new WSIFException(
                    "Could not create instance for home '" + ejbHome + "'",
                    ex);
            }
        }
        Trc.exit(fieldEjbObject);
        return fieldEjbObject;
    }

    /**
     * Gets the Java class of the EJB remote interface. The method attempts to get the class
     * information from the EJBMetaData for the EJBHome but if this fails, it will create
     * the EJBObject and get the class from the instamce.
     * 
     * @return Class   the class of the service object
     */
    Class getEjbObjectClass() throws WSIFException {
        // no method access modifier as it used by WSIFOperation_Java
        Trc.entry(this);
        if (ejbObjectClass == null) {

            if (fieldEjbObject != null) {
                ejbObjectClass = fieldEjbObject.getClass();
            } else {
                // Lookup the EJBHome. If it has already been resolved, this call will simply
                // return it, which we ignore.
                getEjbHome();

                if (fieldEjbHome == null) {
                    throw new WSIFException("Unable to get EJBObject class, fieldEjbHome is null");
                }

                // Get the Class of the remote interface by looking for the return type of the
                // create method. In general only stateless session beans are supported as
                // web services so look for a no args create method.
                try {
                	Method createMethod = fieldEjbHome.getClass().getDeclaredMethod("create", null);
                	ejbObjectClass = createMethod.getReturnType();
                } catch (NoSuchMethodException nsme) {
                    Trc.ignoredException(nsme);
                } catch (SecurityException se) {
                	Trc.ignoredException(se);
                }

                // If it's a stateful session bean it may not have a no args create method.
                // Since WSIF allows you to call a create method on the home interface for
                // such a bean, look for any create method - they should all return the same
                // object
                if (ejbObjectClass == null) {
                	Method[] methods = fieldEjbHome.getClass().getDeclaredMethods();
                	for (int m = 0; m < methods.length; m++) {
                		Method temp = methods[m];
                		if (temp.getName().equals("create")) {
                			ejbObjectClass = temp.getReturnType();
                			break;
                		}
                	}
                }
            }
        }

        Trc.exit(ejbObjectClass);
        return ejbObjectClass;
    }

    /**
     * Gets the methods of the EJB's remote interface without having to create the object
     * @return Method[] the methods of the EJB's remote interface
     */
    Method[] getEjbObjectMethods() throws WSIFException {
        // no method access modifier as it used by WSIFOperation_EJB
        Trc.entry(this);
        if (ejbObjectMethods == null) {
            Class c = getEjbObjectClass();
            ejbObjectMethods = c.getMethods();
        }
        Trc.exit(ejbObjectMethods);
        return ejbObjectMethods;
    } 

    public Port getPortModel() {
        Trc.entry(this);
        Trc.exit(fieldPortModel);
        return fieldPortModel;
    }

    public void setDefinition(Definition value) {
        Trc.entry(this, value);
        fieldDefinition = value;
        Trc.exit();
    }

    // WSIF: keep list of operations available in this port
    public void setDynamicWSIFOperation(
        String name,
        String inputName,
        String outputName,
        WSIFOperation_EJB value) {
        Trc.entry(this, name, inputName, outputName);

        operationInstances.put(getKey(name, inputName, outputName), value);
        Trc.exit();
    }

    public void setEjbHome(EJBHome newEjbHome) {
        Trc.entry(this, newEjbHome);
        fieldEjbHome = newEjbHome;
        Trc.exit();
    }

    public void setEjbObject(EJBObject newEjbObject) {
        Trc.entry(this, newEjbObject);
        fieldEjbObject = newEjbObject;
        if (fieldEjbObject != null) {
        	ejbObjectClass = fieldEjbObject.getClass();
        }
        Trc.exit();
    }

    public void setPortModel(Port value) {
        Trc.entry(this, value);
        fieldPortModel = value;
        Trc.exit();
    }

    public WSIFOperation createOperation(String operationName)
        throws WSIFException {
        Trc.entry(this, operationName);
        WSIFOperation wo = createOperation(operationName, null, null);
        Trc.exit(wo);
        return wo;
    }

    public WSIFOperation createOperation(
        String operationName,
        String inputName,
        String outputName)
        throws WSIFException {
        Trc.entry(this, operationName, inputName, outputName);
        WSIFOperation_EJB op =
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

    public String deep() {
        String buff = "";
        try {
            buff = new String(super.toString() + ":\n");

            buff += "definition:" + Trc.brief(fieldDefinition);
            buff += " portModel:" + Trc.brief(fieldPortModel);
            buff += " ejbHome:" + fieldEjbHome;
            buff += " ejbObject" + fieldEjbObject;

            buff += " operationInstances:";
            if (operationInstances == null)
                buff += "null";
            else {
                buff += " size:" + operationInstances.size();
                Iterator it = operationInstances.keySet().iterator();
                int i = 0;
                while (it.hasNext()) {
                    String key = (String) it.next();
                    WSIFOperation_EJB woejb = (WSIFOperation_EJB) operationInstances.get(key);
                    buff += "\noperationInstances[" + i + "]:" + key + " " + woejb + " ";
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

		// References to the EJBHome and EJBObject objects should be stored as handles
        if (fieldEjbHome != null) {
        	HomeHandle homeHandle = fieldEjbHome.getHomeHandle();
        	oos.writeObject(homeHandle);
        } else {
        	oos.writeObject(fieldEjbHome);        	
        }
        if (fieldEjbObject != null) {
        	Handle handle = fieldEjbObject.getHandle();
        	oos.writeObject(handle);
        } else {
        	oos.writeObject(fieldEjbObject);        	
        }        
    }

	/**
	 * Override default deserialization
	 */
    private void readObject(ObjectInputStream ois)
        throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        
        // If the EJBHome and EJBObject handles have been written after the WSIFPort, recover
        // the objects now
        if (separatedEJBRefs) {
        	Object objHome = ois.readObject();
        	if (objHome != null && objHome instanceof HomeHandle homeHandle) {
        		fieldEjbHome = homeHandle.getEJBHome();
        	}
        	// else - If the object is null then we don't need to do anything
        	
        	Object obj = ois.readObject();
        	if (obj != null && obj instanceof Handle handle) {
        		fieldEjbObject = handle.getEJBObject();
        	}
        	// else - If the object is null then we don't need to do anything        	
        }
        
        // Reset the operation instances
        operationInstances = new HashMap();
    }     
}
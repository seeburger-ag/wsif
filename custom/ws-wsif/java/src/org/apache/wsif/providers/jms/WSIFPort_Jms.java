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

package org.apache.wsif.providers.jms;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.extensions.ExtensibilityElement;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.base.WSIFDefaultPort;
import org.apache.wsif.format.WSIFFormatter;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.providers.WSIFDynamicTypeMap;
import org.apache.wsif.util.WSIFProperties;
import org.apache.wsif.util.WSIFUtils;
import org.apache.wsif.util.jms.WSIFJMSDestination;
import org.apache.wsif.util.jms.WSIFJMSFinder;
import org.apache.wsif.wsdl.extensions.jms.JMSAddress;

/**
 * Jms WSIF port
 * 
 * @author <a href="mailto:ake@de.ibm.com">Hermann Akermann</a>
 * @author <a href="mailto:antelder@apache.org">Ant Elder</a>
 * @author <a href="mailto:seto@ca.ibm.com">Norman Seto</a>
 */
public class WSIFPort_Jms extends WSIFDefaultPort implements Serializable {

	private static final long serialVersionUID = 1L;

    private Definition fieldDefinition = null;
    private Port fieldPortModel = null;
    private JMSAddress fieldObjectReference = null; // 'physical connection'

    protected Map operationInstances = new HashMap();
    transient private WSIFJMSDestination jmsDest;

    /**
     * ctor
     */
    public WSIFPort_Jms(
        Definition def,
        Port port,
        WSIFDynamicTypeMap typeMap) throws WSIFException {
        Trc.entry(this, def, port, typeMap);

        fieldDefinition = def;
        fieldPortModel = port;

        if (Trc.ON)
            Trc.exit(deep());
    }

    /**
     * @see WSIFPort#createOperation(String)
     */
    public WSIFOperation createOperation(String operationName)
        throws WSIFException {
        Trc.entry(this, operationName);
        WSIFOperation wo = createOperation(operationName, null, null);
        Trc.exit(wo);
        return wo;
    }

    /**
     * @see WSIFPort#createOperation(String, String, String)
     */
    public WSIFOperation createOperation(
        String operationName,
        String inputName,
        String outputName)
        throws WSIFException {
        Trc.entry(this, operationName, inputName, outputName);

        WSIFOperation_Jms op =
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
     * get/set WSIF operation
     */
    public WSIFOperation_Jms getDynamicWSIFOperation(
        String name,
        String inputName,
        String outputName)
        throws WSIFException {
        Trc.entry(this, name, inputName, outputName);

        WSIFOperation_Jms tempOp =
            (WSIFOperation_Jms) operationInstances.get(getKey(name, inputName, outputName));

        WSIFOperation_Jms operation = null;
		if (tempOp != null) {
			operation = tempOp.copy();
		}
		
        if (operation == null) {
            BindingOperation bindingOperationModel =
               WSIFUtils.getBindingOperation( 
                  fieldPortModel.getBinding(), name, inputName, outputName );

            if (bindingOperationModel != null) {
                operation =
                    new WSIFOperation_Jms(
                        fieldPortModel,
                        bindingOperationModel,
                        this);
                setDynamicWSIFOperation(name, inputName, outputName, operation);
            }
        }

        Trc.exit(operation);
        return operation;
    }

    // WSIF: keep list of operations available in this port
    public void setDynamicWSIFOperation(
        String name,
        String inputName,
        String outputName,
        WSIFOperation_Jms value) {
        Trc.entry(this, name, inputName, outputName);

        operationInstances.put(getKey(name, inputName, outputName), value);
        Trc.exit();
    }

    /**
     * Tests if this port supports asynchronous/synchronous 
     * 	calls to operations.
     * 
     */
    public boolean supportsSync() {
        Trc.entry(this);
        Trc.exit(true);
        return true;
    }

    public boolean supportsAsync() {
        Trc.entry(this);
        Trc.exit(true);
        return true;
    }

    /**
     * accessor/mutators
     */
    public Definition getDefinition() {
        Trc.entry(this);
        Trc.exit(fieldDefinition);
        return fieldDefinition;
    }

    public void setDefinition(Definition value) {
        Trc.entry(this, value);
        fieldDefinition = value;
        Trc.exit();
    }

    public Port getPortModel() {
        Trc.entry(this);
        Trc.exit(fieldPortModel);
        return fieldPortModel;
    }

    public void setPortModel(Port value) {
        Trc.entry(this, value);
        fieldPortModel = value;
        Trc.exit();
    }

    public JMSAddress getObjectReference() throws WSIFException {
        Trc.entry(this);
        if (fieldObjectReference == null) {

            try {
                ExtensibilityElement portExtension =
                    (ExtensibilityElement) fieldPortModel.getExtensibilityElements().get(0);

                if (portExtension == null) {
                    throw new WSIFException("Jms missing port extension");
                }

                fieldObjectReference = (JMSAddress) portExtension;

            } catch (Exception ex) {
	        	Trc.exception(ex);
                throw new WSIFException(
                    "Could not create object of class '???todo??? " + "'",
                    ex);
            }
        }
        Trc.exit(fieldObjectReference);
        return fieldObjectReference;
    }

    public void setObjectReference(JMSAddress newObjectReference) {
        Trc.entry(this, newObjectReference);
        fieldObjectReference = newObjectReference;
        Trc.exit();
    }

    public WSIFFormatter getFormatter() {
        Trc.entry(this);
        WSIFFormatter wf = new JMSFormatter(fieldDefinition, fieldPortModel);
        Trc.exit(wf);
        return wf;
    }

	/**
	 * Returns the jmsDestination for this WSIFPort.
	 * @return WSIFJMSDestination
	 */
	public WSIFJMSDestination getJmsDestination() throws WSIFException {
		if (jmsDest==null) {
           jmsDest =
              new WSIFJMSDestination(
                 WSIFJMSFinder.newFinder(
                    getObjectReference(),
                    fieldPortModel.getName()),
              getObjectReference().getJmsProvDestName(),
              WSIFProperties.getSyncTimeout());
		}
		return jmsDest;
	}

    /**
     * Closes the port. All methods are invalid after calling this method.
     */
    public void close() throws WSIFException {
        Trc.entry(this);
        if (jmsDest != null) {
        	jmsDest.close();
        }
        Trc.exit();
    }

    /**
     * helper
     */
    public String deep() {
        String buff = "";
        try {
            buff = new String(super.toString() + ":\n");

            buff += "definition:" + Trc.brief(fieldDefinition);
            buff += " portModel:" + Trc.brief(fieldPortModel);
            buff += " objectReference:" + fieldObjectReference;

            buff += " operationInstances: ";
            if (operationInstances == null)
                buff += "null";
            else {
                buff += "size:" + operationInstances.size();
                Iterator it = operationInstances.keySet().iterator();
                int i = 0;
                while (it.hasNext()) {
                    String key = (String) it.next();
                    WSIFOperation_Jms woj = (WSIFOperation_Jms) operationInstances.get(key);
                    buff += "\noperationInstances[" + i + "]:" + key + " " + woj + " ";
                    i++;
                }
            }
        } catch (Exception e) {
            Trc.exceptionInTrace(e);
        }
        return buff;
    }

}
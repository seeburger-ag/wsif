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

package org.apache.wsif.tools.wsdl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

import org.apache.wsif.providers.ProviderUtils;
import org.apache.wsif.util.WSIFUtils;

import com.ibm.wsdl.BindingInputImpl;
import com.ibm.wsdl.BindingOperationImpl;
import com.ibm.wsdl.BindingOutputImpl;
import com.ibm.wsdl.PortImpl;

/**
 * Model for classes to add new bindings to a WSDL4J definition
 * 
 * @author <a href="mailto:ant.elder@uk.ibm.com">Ant Elder</a>
 */
public abstract class ModelBindingGenerator implements BindingGenerator {

    // name of the binding type this generates
    protected String bindingTypeName;

    /**
     * Construct a new binding generator
     */
    public ModelBindingGenerator(String bindingName) {
        this.bindingTypeName = bindingName;
    }

    /**
     * Returns the bindingTypeName.
     * @return String
     */
    public String getBindingTypeName() {
        return bindingTypeName;
    }

    /**
     * Adds new bindings to a WSDL4J Definition
     */
    public void addBindings(Definition def) {
        List newPorts = new ArrayList();
        List donePortTypes = new ArrayList();

        // for all the services in the WSDL
        Map services = def.getServices();
        for (Iterator i = services.keySet().iterator(); i.hasNext();) {
            QName serviceName = (QName) i.next();
            Service service = (Service) services.get(serviceName);

            // for all the ports in the service
            Map ports = service.getPorts();
            for (Iterator j = ports.keySet().iterator(); j.hasNext();) {
                String portName = (String) j.next();
                Port port = (Port) ports.get(portName);
                PortType portType = port.getBinding().getPortType();
                if (donePortTypes.contains(portType) == false) {
                    if (isNewBindingRequired(port) == true) {
                        Binding newBinding = createBinding(def, port);
                        def.addBinding(newBinding);
                        Port newPort =
                            createServicePort(service, port, newBinding);
                        newPorts.add(newPort);
                        donePortTypes.add(portType);
                    }
                }
            }

            // add any new ports to the service
            for (Iterator j = newPorts.iterator(); j.hasNext();) {
                service.addPort((Port) j.next());
            }
        }

        // only add namespaces if a binding was added
        if (donePortTypes.size() > 0) {
            doAddNamespace(def);
        }
    }

    /**
     * Is a new binding required for a Port
     */
    protected boolean isNewBindingRequired(Port port) {
        boolean wanted = true;
        Binding b = port.getBinding();
        Class bindingType = getImplementedBinding();
        List extEls = b.getExtensibilityElements();
        for (Iterator i = extEls.iterator(); wanted && i.hasNext();) {
        	ExtensibilityElement ee = (ExtensibilityElement) i.next();
            if (bindingType.isAssignableFrom(ee.getClass())) {
                wanted = false;
            }
        }
        return wanted;
    }

    /**
     * Get the class of the extensibility element the generator supports
     */
    abstract public Class getImplementedBinding();

    /**
     * Add any new namespaces to the Definition
     */
    protected void doAddNamespace(Definition def) {
    }

    /**
     * Create a new binding from an existing Port
     */
    protected Binding createBinding(Definition def, Port port) {
        Binding oldBinding = port.getBinding();
        PortType p = oldBinding.getPortType();

        Binding newBinding = def.createBinding();
        newBinding.setPortType(p);
        QName oldName = oldBinding.getQName();
        QName newName =
            new QName(
                oldName.getNamespaceURI(),
                oldName.getLocalPart()
                    + ProviderUtils.capitalizeFirst(getBindingTypeName()));
        newBinding.setQName(newName);

        doCreateBinding(p, newBinding);

        addOperations(p, newBinding);
        newBinding.setUndefined(false);
        return newBinding;
    }

    /**
     * Template method for subclasses to customise the binding
     */
    protected void doCreateBinding(PortType portType, Binding binding) {
    }

    /**
     * Adds the PortType operations to the binding
     */
    protected void addOperations(PortType p, Binding b) {
        for (Iterator i = p.getOperations().iterator(); i.hasNext();) {
            Operation op = (Operation) i.next();

            BindingOperation bop = new BindingOperationImpl();
            bop.setName(op.getName());

            doCreateBindingOperation(op, bop);

            Input input = op.getInput();
            if (input != null) {
                BindingInput bin = new BindingInputImpl();
                bin.setName(input.getName());
                doCreateBindingInput(op, bop, bin);
                bop.setBindingInput(bin);
            }

            Output output = op.getOutput();
            if (output != null) {
                BindingOutput bout = new BindingOutputImpl();
                bout.setName(output.getName());
                doCreateBindingOutput(op, bop, bout);
                bop.setBindingOutput(bout);
            }

            b.addBindingOperation(bop);

        }
    }

    /**
     * Template method for subclasses to customise the binding operation
     */
    protected void doCreateBindingOperation(
        Operation op,
        BindingOperation bop) {
    }

    /**
     * Template method for subclasses to customise the binding input
     */
    protected void doCreateBindingInput(
        Operation op,
        BindingOperation bop,
        BindingInput bin) {
    }

    /**
     * Template method for subclasses to customise the binding output
     */
    protected void doCreateBindingOutput(
        Operation op,
        BindingOperation bop,
        BindingOutput bout) {
    }

    /**
     * Adds a port to the service for the new binding
     */
    protected Port createServicePort(Service s, Port oldPort, Binding b) {
        Port port = new PortImpl();
        port.setName(
            oldPort.getName()
                + ProviderUtils.capitalizeFirst(getBindingTypeName()));
        port.setBinding(b);

        doCreateServicePort(port);

        return port;
    }

    /**
     * Template method for subclasses to customise the service port
     */
    protected void doCreateServicePort(Port p) {
    }

}

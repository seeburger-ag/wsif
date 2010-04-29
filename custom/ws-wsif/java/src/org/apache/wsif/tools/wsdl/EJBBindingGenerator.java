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

import java.util.Iterator;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.wsif.wsdl.extensions.ejb.EJBAddress;
import org.apache.wsif.wsdl.extensions.ejb.EJBBinding;
import org.apache.wsif.wsdl.extensions.ejb.EJBBindingConstants;
import org.apache.wsif.wsdl.extensions.ejb.EJBOperation;
import org.apache.wsif.wsdl.extensions.format.FormatBindingConstants;
import org.apache.wsif.wsdl.extensions.format.TypeMap;
import org.apache.wsif.wsdl.extensions.format.TypeMapping;

/**
 * Utility to add a WSIF Java binding to a WSDL4J definition
 * 
 * @author <a href="mailto:ant.elder@uk.ibm.com">Ant Elder</a>
 */
public class EJBBindingGenerator extends ModelBindingGenerator {

    // name of this BindingGenerators binding type
    protected static String ejbBindingTypeName = "ejb";

    // implemeted Binding class
    protected Class implementedBinding = EJBBinding.class;

    /**
     * Construct a new binding generator
     */
    public EJBBindingGenerator() {
        super(ejbBindingTypeName);
    }

    /**
     * Returns the implemetedBinding.
     * @return Class
     */
    public Class getImplementedBinding() {
        return implementedBinding;
    }

    /**
     * Add the EJB and Format binding namespaces
     */
    protected void doAddNamespace(Definition def) {
        def.addNamespace("ejb", EJBBindingConstants.NS_URI_EJB);
        def.addNamespace("format", FormatBindingConstants.NS_URI_FORMAT);
    }

    /**
     * Add the EJB and Format bindings
     */
    protected void doCreateBinding(PortType portType, Binding binding) {
        EJBBinding ejbBinding = new EJBBinding();
        binding.addExtensibilityElement(ejbBinding);

        TypeMapping formatTypeMapping = createFormatTypeMapping();
        binding.addExtensibilityElement(formatTypeMapping);
    }

    /**
     * Create the format binding
     * 
     * <format:typeMapping encoding="Java" style="Java">
     *    <format:typeMap typeName="typens:type" formatType="package.class" />
     *</format:typeMapping>
     * 
     * TODO: automatically create correct type mappings for definition
     */
    protected TypeMapping createFormatTypeMapping() {
        TypeMapping formatTypeMapping = new TypeMapping();
        formatTypeMapping.setEncoding("Java");
        formatTypeMapping.setStyle("Java");

        TypeMap tm = new TypeMap();
        tm.setTypeName(new QName("http://www.w3.org/2001/XMLSchema", "string"));
        tm.setFormatType("java.lang.String");

        formatTypeMapping.addMap(tm);
        return formatTypeMapping;
    }

    /**
     * Create the Java binding operation
     */
    protected void doCreateBindingOperation(
        Operation op,
        BindingOperation bop) {
        EJBOperation ejbOp = new EJBOperation();
        ejbOp.setMethodName(op.getName());
        ejbOp.setEjbInterface("Remote");

        Input input = op.getInput();
        if (input != null) {
            Message msg = input.getMessage();
            if (msg != null) {
                List parts = msg.getOrderedParts(null);
                String partList = "";
                for (int j = 0; j < parts.size(); j++) {
                    partList += ((Part) parts.get(j)).getName();
                    if (j < parts.size()) {
                        partList += " ";
                    }
                }
                ejbOp.setParameterOrder(partList);
            }
        }

        Output output = op.getOutput();
        if (output != null) {
            Message msg = output.getMessage();
            if (msg != null) {
                List parts = msg.getOrderedParts(null);
                if (parts.size() > 0) {
                    Part part = (Part) parts.get(0);
                    String partName = part.getName();
                    ejbOp.setReturnPart(partName);
                }
            }
        }
        bop.addExtensibilityElement(ejbOp);
    }

    /**
     * Create the java address for the port
     * 
     * <ejb:address className="addressbook.wsiftypes.AddressBookHome"
     *     jndiName="/services/addressbook"
     *     initialContextFactory="com.mycompany.server.MyappInitialContext"
     *     jndiProviderURL="ormi://myserver.mycompany.com/ejbsample"/>
     */
    protected void doCreateServicePort(Port p) {
        EJBAddress addr = new EJBAddress();
        addr.setClassName("your.ejb.home.classname.here");
        addr.setJndiName("/your/jndi/name/here");
//        addr.setInitialContextFactory("your.initial.context.here");
//        addr.setJndiProviderURL("your://jndi.provider.url/here");
        p.addExtensibilityElement(addr);
    }

}

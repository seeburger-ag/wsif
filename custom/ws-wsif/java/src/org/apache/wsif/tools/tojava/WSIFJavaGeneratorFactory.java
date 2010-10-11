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

package org.apache.wsif.tools.tojava;

import javax.wsdl.Binding;
import javax.wsdl.Service;

import org.apache.axis.wsdl.gen.Generator;
import org.apache.axis.wsdl.symbolTable.BindingEntry;
import org.apache.axis.wsdl.symbolTable.ServiceEntry;
import org.apache.axis.wsdl.symbolTable.SymbolTable;
import org.apache.axis.wsdl.toJava.JavaGeneratorFactory;
import org.apache.axis.wsdl.toJava.JavaServiceWriter;

/**
 * WSIFJavaGeneratorFactory
 * 
 * WSIF version of the AXIS JavaGeneratorFactory that overrides
 * the service and binding generators to use the WSIF 
 * versions of the JavaServiceWriter and JavaBindingWriter.
 * 
 * @author <a href="mailto:ant.elder@uk.ibm.com">Ant Elder</a>
 */
public class WSIFJavaGeneratorFactory extends JavaGeneratorFactory {

    public Generator getGenerator(Service service, SymbolTable symbolTable) {
        Generator writer =
            new WSIFJavaServiceWriter(emitter, service, symbolTable);
        ServiceEntry sEntry = symbolTable.getServiceEntry(service.getQName());
        serviceWriters.addStuff(writer, sEntry, symbolTable);
        return serviceWriters;
    } // getGenerator

    public Generator getGenerator(Binding binding, SymbolTable symbolTable) {
        Generator writer =
            new WSIFJavaBindingWriter(emitter, binding, symbolTable);
        BindingEntry bEntry = symbolTable.getBindingEntry(binding.getQName());
        bindingWriters.addStuff(writer, bEntry, symbolTable);
        return bindingWriters;
    } // getGenerator

}
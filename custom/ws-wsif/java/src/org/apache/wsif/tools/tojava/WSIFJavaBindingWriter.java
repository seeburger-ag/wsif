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

import java.io.IOException;

import javax.wsdl.Binding;

import org.apache.axis.wsdl.symbolTable.SymbolTable;
import org.apache.axis.wsdl.toJava.Emitter;
import org.apache.axis.wsdl.toJava.JavaBindingWriter;

/**
 * WSIFJavaBindingWriter
 * 
 * WSIF version of the AXIS JavaBindingWriter that overrides
 * the generate method to only generate the interface. The 
 * AXIS version also generates stub, skeleton, and implementation
 * files which are not required by WSIF.
 * 
 * @author <a href="mailto:ant.elder@uk.ibm.com">Ant Elder</a>
 */
public class WSIFJavaBindingWriter extends JavaBindingWriter {

    /**
     * Constructor.
     */
    public WSIFJavaBindingWriter(
        Emitter emitter,
        Binding binding,
        SymbolTable symbolTable) {
        super(emitter, binding, symbolTable);
    } // ctor

    /**
     * Write all the binding bindings: 
     *  - interfaceWriter only 
     *    (but only if required by stub testcase) 
     */
    public void generate() throws IOException {
        setGenerators();
        if (interfaceWriter != null) {
        	if (((WSIFEmitter)emitter).isTestcaseGenStubs()) {
                interfaceWriter.generate();
        	}
        }

    } // generate

}

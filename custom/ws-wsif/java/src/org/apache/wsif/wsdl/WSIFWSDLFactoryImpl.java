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

package org.apache.wsif.wsdl;

import javax.wsdl.Definition;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;

import org.apache.wsif.base.WSIFServiceImpl;
import org.apache.wsif.logging.Trc;

import com.ibm.wsdl.DefinitionImpl;
import com.ibm.wsdl.xml.WSDLReaderImpl;
import com.ibm.wsdl.xml.WSDLWriterImpl;

/**
 * WSIF specific implementation of javax.wsdl.factory.WSDLFactory
 * @author Owen Burroughs <owenb@apache.org>
 */
public class WSIFWSDLFactoryImpl extends WSDLFactory {
    
    public WSIFWSDLFactoryImpl() {
        Trc.entry(this);
        Trc.exit();
    }

    public Definition newDefinition() {
        Trc.entry(this);
        Definition def = new DefinitionImpl();
        def.setExtensionRegistry(newPopulatedExtensionRegistry());
        Trc.exit(def);
        return def;
    }

    public javax.wsdl.xml.WSDLReader newWSDLReader() {
        Trc.entry(this);
        WSDLReaderImpl reader = new WSDLReaderImpl();
        reader.setFactoryImplName(this.getClass().getName());
        reader.setExtensionRegistry(newPopulatedExtensionRegistry());
        Trc.exit(reader);
        return reader;
    }

    public javax.wsdl.xml.WSDLWriter newWSDLWriter() {
        Trc.entry(this);
        WSDLWriterImpl writer = new WSDLWriterImpl();
        Trc.exit(writer);
        return writer;
    }

    public ExtensionRegistry newPopulatedExtensionRegistry() {
        Trc.entry(this);
        ExtensionRegistry extReg = WSIFServiceImpl.getCompositeExtensionRegistry();
        if (extReg == null) {
            extReg = new com.ibm.wsdl.extensions.PopulatedExtensionRegistry();
        }
        Trc.exit(extReg);
        return extReg;
    }
}
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

    /**
     * Returns the WSIF composite extension registry.
     * <p>
     * Despite the inherited "new..." name, this deliberately returns the single
     * process-wide composite registry rather than a fresh one: providers register their
     * WSDL extensions into it at arbitrary times, and every Definition, reader and writer
     * has to see those registrations. The registry is append-only and safe to share
     * across threads.
     * <p>
     * Note there is no null fallback to a plain PopulatedExtensionRegistry here - the
     * composite is created during class initialization of WSIFServiceImpl and can never
     * be null, so the fallback that used to sit here was unreachable. The WSDL4J standard
     * extensions are already present, because the composite seeds itself with a
     * PopulatedExtensionRegistry.
     */
    public ExtensionRegistry newPopulatedExtensionRegistry() {
        Trc.entry(this);
        ExtensionRegistry extReg = WSIFServiceImpl.getCompositeExtensionRegistry();
        Trc.exit(extReg);
        return extReg;
    }
}

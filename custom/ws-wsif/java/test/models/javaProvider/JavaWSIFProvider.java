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

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.extensions.ExtensionRegistry;

import org.apache.wsif.WSIFException;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.providers.ModelWSIFPort;
import org.apache.wsif.providers.ModelWSIFProvider;
import org.apache.wsif.providers.WSIFDynamicTypeMap;
import org.apache.wsif.tools.wsdl.BindingGenerator;
import org.apache.wsif.tools.wsdl.JavaBindingGenerator;
import org.apache.wsif.wsdl.extensions.java.JavaBinding;

/**
 * @author <a href="mailto:antelder@apache.org">Ant Elder</a> 
 */
public class JavaWSIFProvider extends ModelWSIFProvider {

    public JavaWSIFProvider() throws WSIFException {
        super();
    }

    protected String getProviderName() {
        return "java";
    }

    protected String[] getRequiredClasses() {
        return new String[0];
    }

    protected ExtensionRegistry[] getExtensionRegistries() {
        return new ExtensionRegistry[0];
        //TODO: why isn't this done for Java???
    }

    protected Class getImplementedBindingClass() {
        return JavaBinding.class;
    }

    public BindingGenerator[] getBindingGenerators() {
        return new BindingGenerator[] { new JavaBindingGenerator()};
    }

    protected ModelWSIFPort makeWSIFPort(
        Definition def,
        Port port,
        WSIFDynamicTypeMap typeMap)
        throws WSIFException {
        ModelWSIFPort wsifPort = new JavaWSIFPort(def, port, typeMap);
        Trc.exit(wsifPort);
        return wsifPort;
    }

}
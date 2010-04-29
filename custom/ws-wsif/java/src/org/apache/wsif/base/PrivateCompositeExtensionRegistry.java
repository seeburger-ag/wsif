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

package org.apache.wsif.base;

import java.util.Enumeration;
import java.util.Vector;

import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.ExtensionSerializer;
import javax.wsdl.extensions.UnknownExtensionDeserializer;
import javax.wsdl.extensions.UnknownExtensionSerializer;
import javax.xml.namespace.QName;

import org.apache.wsif.logging.Trc;
import org.apache.wsif.wsdl.extensions.ejb.EJBBindingSerializer;
import org.apache.wsif.wsdl.extensions.format.FormatBindingSerializer;
import org.apache.wsif.wsdl.extensions.java.JavaBindingSerializer;

import com.ibm.wsdl.extensions.PopulatedExtensionRegistry;

/**
 * This is utility class that allows to aggregate multiple
 * extensions registries into one. By default all standard WSDL4J
 * extensions are made available.
 * 
 * @author Alekander Slominski
 * @author Sanjiva Weerawarana
 * @author Owen Burroughs <owenb@apache.org>
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */

class PrivateCompositeExtensionRegistry extends ExtensionRegistry {
	private static final long serialVersionUID = 1L;
    private Vector extRegs = new Vector();

    PrivateCompositeExtensionRegistry() {
        Trc.entry(this);

        // Add (de)serializers for Java, EJB and Format extensions to the
        // PopulatedExetensionRegistry and add the registry to our list
        PopulatedExtensionRegistry per = new PopulatedExtensionRegistry();
        JavaBindingSerializer javaBindingSerializer = new JavaBindingSerializer();
        javaBindingSerializer.registerSerializer(per);
        FormatBindingSerializer formatSerializer = new FormatBindingSerializer();
        formatSerializer.registerSerializer(per);
        EJBBindingSerializer ejbBindingSerializer = new EJBBindingSerializer();
        ejbBindingSerializer.registerSerializer(per);
        extRegs.add(per);
        Trc.exit();
    }

    public void addExtensionRegistry(ExtensionRegistry reg) {
        Trc.entry(this, reg);
        extRegs.add(reg);
        Trc.exit();
    }

    public void registerSerializer(
        Class parentType,
        Class extensionType,
        ExtensionSerializer es) {
        throw new RuntimeException(
            getClass() + " does not allow to register serializers");
    }

    public void registerDeserializer(
        Class parentType,
        QName elementType,
        ExtensionDeserializer ed) {
        throw new RuntimeException(
            getClass() + " does not allow to register deserializers");
    }

    public ExtensionSerializer querySerializer(
        Class parentType,
        QName extensionType)
        throws WSDLException {
        Trc.entry(this, parentType, extensionType);

        ExtensionSerializer ser;
        Enumeration enum_ = extRegs.elements();
        while (enum_.hasMoreElements()) {
            ExtensionRegistry reg = (ExtensionRegistry) enum_.nextElement();
            try {
                ser = reg.querySerializer(parentType, extensionType);
                // Check that we're not looking at the default serializer
                ExtensionSerializer def = reg.getDefaultSerializer();
                if (ser != null && !(ser.equals(def))) {
                    Trc.exit(ser);
                    return ser;
                }
            } catch (WSDLException ex) {
	        	Trc.exception(ex);
                throw ex;
            }
        }
        ser = new UnknownExtensionSerializer();
        Trc.exit();
        return ser;
    }

    public ExtensionDeserializer queryDeserializer(
        Class parentType,
        QName elementType)
        throws WSDLException {
        Trc.entry(this, parentType, elementType);

        ExtensionDeserializer deser;
        Enumeration enum_ = extRegs.elements();
        while (enum_.hasMoreElements()) {
            ExtensionRegistry reg = (ExtensionRegistry) enum_.nextElement();
            try {
                deser = reg.queryDeserializer(parentType, elementType);
                // Check that we're not looking at the default deserializer
                ExtensionDeserializer def = reg.getDefaultDeserializer();
                if (deser != null && !(deser.equals(def))) {
                    Trc.exit(deser);
                    return deser;
                }
            } catch (WSDLException ex) {
	        	Trc.exception(ex);
                throw ex;
            }
        }
        deser = new UnknownExtensionDeserializer();
        Trc.exit(deser);
        return deser;
    }

    public ExtensibilityElement createExtension(
        Class parentType,
        QName elementType)
        throws WSDLException {
        Trc.entry(this, parentType, elementType);

        ExtensibilityElement ee;
        Enumeration enum_ = extRegs.elements();
        while (enum_.hasMoreElements()) {
            ExtensionRegistry reg = (ExtensionRegistry) enum_.nextElement();
            try {
                ee = reg.createExtension(parentType, elementType);
                Trc.exit(ee);
                return ee;
            } catch (WSDLException ignored) {
	        	Trc.ignoredException(ignored);
            }
        }
        ee = super.createExtension(parentType, elementType);
        Trc.exit(ee);
        return ee;
    }

}

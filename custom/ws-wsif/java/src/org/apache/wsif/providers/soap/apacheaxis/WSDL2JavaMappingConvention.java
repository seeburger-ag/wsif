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

package org.apache.wsif.providers.soap.apacheaxis;

import java.util.Hashtable;
import javax.xml.namespace.QName;

import org.apache.axis.wsdl.toJava.Utils;
import org.apache.wsif.mapping.WSIFMappingConvention;

/**
 * An implementation of {@link org.apache.wsif.mapping.WSIFMappingConvention} that uses
 * the same methods from Apache Axis, that are used by the WSDL2Java tool.
 * 
 * @author Owen Burroughs <owenb@apache.org>
 */
public class WSDL2JavaMappingConvention implements WSIFMappingConvention {

    // Table of package mappings: namespace -> packageName
	private Hashtable packageMappings = new Hashtable();
	
    /**
     * @see org.apache.wsif.mapping.WSIFMappingConvention#getClassNameForComplexType(QName)
     */
    public String getClassNameForComplexType(QName qn) {
        String namespace = qn.getNamespaceURI();
        String localPart = qn.getLocalPart();
        String packageName = (String) packageMappings.get(namespace);
        if (packageName == null) {
        	packageName = Utils.makePackageName(namespace);
        }
        String className = Utils.xmlNameToJavaClass(localPart);
        if (packageName != null
            && !packageName.equals("")
            && className != null
            && !className.equals("")) {
            return packageName + "." + className;
        }
        return null;
    }

    /**
     * @see org.apache.wsif.mapping.WSIFMappingConvention#getClassNameForSimpleType(QName)
     */
    public String getClassNameForSimpleType(QName qn) {
        String namespace = qn.getNamespaceURI();
        String localPart = qn.getLocalPart();
        String packageName = (String) packageMappings.get(namespace);
        if (packageName == null) {
        	packageName = Utils.makePackageName(namespace);
        }
        String className = Utils.xmlNameToJavaClass(localPart);
        if (packageName != null
            && !packageName.equals("")
            && className != null
            && !className.equals("")) {
            return packageName + "." + className;
        }
        return null;
    }

    /**
     * @see org.apache.wsif.mapping.WSIFMappingConvention#getClassNameForElementType(QName)
     */
    public String getClassNameForElementType(QName qn) {
        String namespace = qn.getNamespaceURI();
        String localPart = qn.getLocalPart();
        String packageName = (String) packageMappings.get(namespace);
        if (packageName == null) {
        	packageName = Utils.makePackageName(namespace);
        }
        String className = Utils.xmlNameToJavaClass(localPart);
        if (packageName != null
            && !packageName.equals("")
            && className != null
            && !className.equals("")) {
            return packageName + "." + className;
        }
        return null;
    }

	
    /**
     * @see org.apache.wsif.mapping.WSIFMappingConvention#overridePackageMapping(String, String)
     */
    public void overridePackageMapping(String namespace, String packageName) {
    	if (packageName != null && namespace != null) {
    		// Store the namespace as the key and the package name as the value
    	    packageMappings.put(namespace, packageName);
    	}
    }
}

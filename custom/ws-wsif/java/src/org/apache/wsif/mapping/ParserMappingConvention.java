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
 
package org.apache.wsif.mapping;

import java.util.Hashtable;
import javax.xml.namespace.QName;

import org.apache.wsif.util.WSIFUtils;

/**
 * Implementation of {@link org.apache.wsif.mapping.WSIFMappingConvention} that matches the
 * convention used by the now deprecated getTypeMappings methods on
 * {@link org.apache.wsif.schema.Parser}<br><br>
 * This convention works as follows:<br>
 * <ul>
 * <li>For all types, the package for the class is determined by in the following way:<br>
 * Take the "hostname" part of the namespace uri. Reverse the components. Take the remainder 
 * of the uri. Split it by the / character and rebuild with . between each component. At 
 * all times strings are checked to make sure that they are valid Java names.<br>
 * </li>
 * <li>Local parts of the xml name are converted to valid Java class names and appended to
 * the generated package name</li>
 * <li>Class names for elements are appended with the string "Element"</li>
 * </ul>
 * So for example, the xml name 
 * <br>&nbsp;&nbsp;&nbsp;<tt>http://www.wsif.com/test/types:address</tt>
 * <br>would map to
 * <br>&nbsp;&nbsp;<tt>com.wsif.www.test.types.Address</tt>
 * 
 * @author Owen Burroughs <owenb@apache.org>
 */
public class ParserMappingConvention implements WSIFMappingConvention {

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
        	packageName = WSIFUtils.getPackageNameFromNamespaceURI(namespace);
        }
        String className = WSIFUtils.getJavaClassNameFromXMLName(localPart);
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
        	packageName = WSIFUtils.getPackageNameFromNamespaceURI(namespace);
        }
        String className = WSIFUtils.getJavaClassNameFromXMLName(localPart);
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
        	packageName = WSIFUtils.getPackageNameFromNamespaceURI(namespace);
        }
        String className = WSIFUtils.getJavaClassNameFromXMLName(localPart);
        if (packageName != null
            && !packageName.equals("")
            && className != null
            && !className.equals("")) {
            return packageName + "." + className + "Element";
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

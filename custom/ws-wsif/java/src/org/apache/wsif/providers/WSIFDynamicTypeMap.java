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

package org.apache.wsif.providers;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.apache.wsif.logging.Trc;
import org.apache.wsif.util.WSIFUtils;

/**
 * Container for type mappings that can be used by dynamic providers.
 *
 * @author Alekander Slominski
 * @author Owen Burroughs <owenb@apache.org>
 */
public class WSIFDynamicTypeMap implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
	
    protected Vector typeMapList = new Vector();
    // Create a Vector of xmlTypes (QNames) for faster lookup of existing mappings
    protected Vector xmlTypes = new Vector();
    protected ArrayList allTypes;

    /**
     * Default constructor
     */
    public WSIFDynamicTypeMap() {
        Trc.entry(this);
        allTypes = new ArrayList();
        Trc.exit();
    }

    /**
     * Constructor
     * @param alist An ArrayList of all custom types from the wsdl
     * @deprecated WSIFDynamicTypeMap should no longer be used to hold a list 
     * of custom types since the mapPackage functionality has been moved to 
     * {@link org.apache.wsif.mapping.WSIFMappingConvention}
     */
    public WSIFDynamicTypeMap(ArrayList aList) {
        Trc.entry(this, aList);
        allTypes = aList;
        Trc.exit();
    }

    /**
     * Add new mapping between XML and Java type. This method is equivalent to
     * calling mapType(xmlType, javaType, true)
     * @param xmlType The qualified xml name
     * @param javaType The Java class
     * @see #mapType(QName, Class, boolean)
     */
    public void mapType(QName xmlType, Class javaType) {
        Trc.entry(this, xmlType, javaType);
        mapType(xmlType, javaType, true);
        Trc.exit();
    }

    /**
     * Add new mapping between XML and Java type.
     * @param xmlType The qualified xml name
     * @param javaType The Java class
     * @param force flag to indicate if mapping should override an existing one
     * for the same xmlType 
     */
    public void mapType(QName xmlType, Class javaType, boolean force) {
        Trc.entry(this, xmlType, javaType);
        int i = xmlTypes.indexOf(xmlType);
        // Only add mapping if no mapping yet exists for this type
        // or if force is set to true
        if (i < 0) {
      	    // No mapping for this type exists so create one.        	
       	    typeMapList.add(new WSIFDynamicTypeMapping(xmlType, javaType));
       	    xmlTypes.add(xmlType);
        } else if (force == true) {
           	// Mapping for this type already exists so replace it.
           	typeMapList.setElementAt(new WSIFDynamicTypeMapping(xmlType, javaType), i);        	
        }
        Trc.exit();
    }

    /** 
     * Map a package name to a namespace URI 
     * @param namespace The wsdl namespace  
     * @param packageName The name of the Java package
     * @deprecated Package mappings should now be set with the 
     * {@link org.apache.wsif.mapping.WSIFMappingConvention} 
     */
    public void mapPackage(String namespace, String packageName) {
        Trc.entry(this, namespace, packageName);
        Iterator it = allTypes.iterator();
        while (it.hasNext()) {
            QName qname = (QName) it.next();
            String ns = qname.getNamespaceURI();
            if (ns != null && ns.equals(namespace)) {
                resolveMapping(qname, packageName);
            }
        }
        Trc.exit();
    }

    protected void resolveMapping(QName qn, String pn) {
        Trc.entry(this, qn, pn);
        try {
            String xmlName = qn.getLocalPart();
            if (xmlName != null) {
                String javaName = WSIFUtils.getJavaClassNameFromXMLName(xmlName);
                String classname = pn + "." + javaName;
                Class cl =
                    Class.forName(classname, true, Thread.currentThread().getContextClassLoader());
                mapType(qn, cl);
            }
        } catch (Exception e) {
        	Trc.ignoredException(e);
            //ignore
        }
        Trc.exit();
    }

    /**
     * Set the list of all custom types from the wsdl
     * @param aList The list
     * @deprecated WSIFDynamicTypeMap should no longer be used to hold a list 
     * of custom types since the mapPackage functionality has been moved to 
     * {@link org.apache.wsif.mapping.WSIFMappingConvention}
     */
    public void setAllTypes(ArrayList aList) {
        Trc.entry(this, aList);
        allTypes = aList;
        Trc.exit();
    }

    /**
     * Return iterator with all mappings.
     * @return The iterator
     */
    public Iterator iterator() {
        Trc.entry(this);
        Trc.exit();
        return typeMapList.iterator();
    }
    /**
     * Produce a copy of the WSIFDynamicTypeMap. This is not a clone;
     * the copy will contain references to the same WSIFDynamicTypeMappings.
     * This method contains synchronized code so that the type map cannot
     * be altered whilst the copy takes place.
     * @return The copy of the WSIFDynamicTypeMap
     */
    public WSIFDynamicTypeMap copy() {
    	// Obtain a lock on the hash map
        synchronized (typeMapList) {
        	WSIFDynamicTypeMap tm = new WSIFDynamicTypeMap();
        	synchronized (allTypes) {
        		tm.setAllTypes((ArrayList) allTypes.clone());
        	}            
            Iterator it = typeMapList.iterator();
            // Copy the mappings
            while (it.hasNext()) {
                WSIFDynamicTypeMapping temp =
                    (WSIFDynamicTypeMapping) it.next();
                tm.mapType(temp.getXmlType(), temp.getJavaType());
            }
            return tm;
        }
    }}

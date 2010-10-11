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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.mapping.MappingHelper;
import org.apache.wsif.mapping.WSIFMapper;
import org.apache.wsif.mapping.WSIFMappingConvention;
import org.apache.wsif.schema.ComplexType;
import org.apache.wsif.schema.ElementType;
import org.apache.wsif.schema.SchemaType;
import org.apache.wsif.schema.SimpleType;

/**
 * An implementation of {@link org.apache.wsif.mapping.WSIFMapper} which assumes that
 * the Axis wsdl2java tool was used in creating the client side Java classes for the 
 * service.<br><br>
 * It is recommended that this WSIFMapper implementation should be used in conjunction 
 * with the corresponding WSIFMappingConvention implementation, 
 * {@link org.apache.wsif.providers.soap.apacheaxis.WSDL2JavaMappingConvention}
 * 
 * @author Owen Burroughs <owenb@apache.org>
 */
public class WSDL2JavaMapper implements WSIFMapper {
	
	private boolean includeStandardMappings = false;
	private WSIFMappingConvention convention = new WSDL2JavaMappingConvention();

	// Table of type mappings: xml name -> Java class name
    private Hashtable typeMappings = new Hashtable();
	
	
    /**
     * @see org.apache.wsif.mapping.WSIFMapper#getIncludeStandardMappings()
     */
	public boolean getIncludeStandardMappings() {
		return includeStandardMappings;
	}

    /**
     * @see org.apache.wsif.mapping.WSIFMapper#setIncludeStandardMappings(boolean)
     */
	public void setIncludeStandardMappings(boolean b) {
		includeStandardMappings = b;
	}

    /**
     * @see org.apache.wsif.mapping.WSIFMapper#getMappingConvention()
     */
	public WSIFMappingConvention getMappingConvention() {
		return convention;
	}

    /**
     * @see org.apache.wsif.mapping.WSIFMapper#setMappingConvention(WSIFMappingConvention)
     */
	public void setMappingConvention(WSIFMappingConvention con) {
		convention = con;
	}

    /**
     * @see org.apache.wsif.mapping.WSIFMapper#overrideTypeMapping(QName, String)
     */
    public void overrideTypeMapping(QName xmlName, String className) {
    	if (className != null && xmlName != null) {
    		// Store the xml name as the key and the Java class name as the value
    	    typeMappings.put(xmlName, className);
    	}
    }
	
    /**
     * @see org.apache.wsif.mapping.WSIFMapper#getMappings(SchemaType[])
     */
    public Map getMappings(SchemaType[] types) {

		Trc.entry(this, types);
		
		HashMap table = new HashMap();
		if (types == null) {
			Trc.exit(table);
			return table;
		}
				
        HashMap standards = null;

        if (includeStandardMappings) {
            // set up all standard mappings
            MappingHelper.populateWithStandardXMLJavaMappings(
                table,
                WSIFConstants.NS_URI_1999_SCHEMA_XSD,
                true);
            MappingHelper.populateWithStandardXMLJavaMappings(
                table,
                WSIFConstants.NS_URI_2000_SCHEMA_XSD,
                false);
            MappingHelper.populateWithStandardXMLJavaMappings(
                table,
                WSIFConstants.NS_URI_2001_SCHEMA_XSD,
                false);
        } else {
            // set up all standard mappings in a seperate map for use when resolving arrays			
            standards = new HashMap();
            MappingHelper.populateWithStandardXMLJavaMappings(
                standards,
                WSIFConstants.NS_URI_1999_SCHEMA_XSD,
                true);
            MappingHelper.populateWithStandardXMLJavaMappings(
                standards,
                WSIFConstants.NS_URI_2000_SCHEMA_XSD,
                false);
            MappingHelper.populateWithStandardXMLJavaMappings(
                standards,
                WSIFConstants.NS_URI_2001_SCHEMA_XSD,
                false);
        }

        // Create temporary list to hold types which are arrays. We can then resolve them
        // after resolving all other types
        List arrays = new ArrayList();

        // Create temporary list to hold types which are elements. We can then resolve them
        // after resolving all other types
        List elements = new ArrayList();

        // Go through the array of SchemaType objects
        for (int i = 0; i < types.length; i++) {

            SchemaType st = types[i];
            // Ignore null types
            if (st == null)
                continue;
            QName typeName = st.getTypeName();
            if (typeName == null)
                continue;

            if (st.isArray()) {
                arrays.add(st);
            } else {
                // Deal with elements
                if (st instanceof ElementType) {
                    QName baseType = ((ElementType) st).getElementType();
                    
                    if (baseType != null) {                    	
                        if (((ElementType) st).isNillable()) {
                            String wrapperClass = getWrapperClassName(baseType);
                            if (wrapperClass != null) {
                                table.put(typeName, wrapperClass);
                                continue;
                            }
                        }
                        
                        String baseClassName = (String) typeMappings.get(baseType);
                        if (baseClassName == null) {                        
                            baseClassName = (String) table.get(baseType);
                        }
                        if (baseClassName == null
                            && !includeStandardMappings) {
                            baseClassName = (String) standards.get(baseType);
                        }
                        if (baseClassName != null) {
                            table.put(typeName, baseClassName);
                        } else {
                            elements.add(st);
                        }
                    } else {
                    	elements.add(st);
                    }
                } else {
                    String className = null;

                    // Deal with complexTypes and simpleTypes
                    if (st instanceof ComplexType) {
                        // If we have already mapped this type, we guess that we have a complexType and
                        // an Element with the same name. Check to see if the WSDL2Java generated classes
                        // for such a conflict exist and if so, set up mappings accordingly.                    	
                        String temp = (String) table.get(typeName);
                        if (temp != null) {
                            try {
                                Class c = Class.forName(temp, true, Thread.currentThread().getContextClassLoader());
                                className = temp;
                            } catch (ClassNotFoundException cnf) {
                                try {
                                    Class c = Class.forName(temp + "_Type");
                                    className = temp + "_Type";
                                    QName tempName =
                                        new QName(
                                            typeName.getNamespaceURI(),
                                            ">" + typeName.getLocalPart());
                                    String tempClassName = temp + "_ElemType";
                                    table.put(tempName, tempClassName);
                                } catch (ClassNotFoundException cnf2) {
                                }
                            }
                        } else {
                            className =
                                convention.getClassNameForComplexType(typeName);
                        }
                    } else if (st instanceof SimpleType) {
                        className =
                            convention.getClassNameForSimpleType(typeName);
                    }
                    if (className != null) {
                        table.put(typeName, className);
                    }
                }
            }
        }

        // Create a temporary list for arrays of arrays so we can resolve them last
        ArrayList multiArrays = new ArrayList();

        // Now we'll resolve any array types that were found
        Iterator ai = arrays.iterator();
        while (ai.hasNext()) {
            SchemaType st = (SchemaType) ai.next();
            // We've already checked that its an array so cut to the chase!
            QName theType = st.getTypeName();
            if (theType == null) continue;
            
            QName arrayType = st.getArrayType();
            if (arrayType != null && theType != null) {
            	// Check overriden mappings first
            	String baseClass = (String) typeMappings.get(arrayType);
            	if (baseClass == null) {
            		// Check current mappings
                    baseClass = (String) table.get(arrayType);
            	}
                if (baseClass == null && standards != null) {
                    // Check for base class in the standard mappings
                    baseClass = (String) standards.get(arrayType);
                }
                if (baseClass == null) {
                    String lp = arrayType.getLocalPart();
                    if (lp != null && lp.startsWith("ArrayOf")) {
                        // This is an array of an array. Perhaps we've
                        // not mapped the base array yet so re-try this
                        // at the end
                        multiArrays.add(st);
                    }
                    continue;
                }
                // Deal with multidimentional array classes
                String extraDims = "";
                for (int x = 1; x < st.getArrayDimension(); x++) {
                    extraDims += "[";
                }
                if (baseClass != null) {
                    // Check for primitive types
                    if (baseClass.equals("int")) {
                        table.put(theType, extraDims + "[I");
                    } else if (baseClass.equals("float")) {
                        table.put(theType, extraDims + "[F");
                    } else if (baseClass.equals("long")) {
                        table.put(theType, extraDims + "[J");
                    } else if (baseClass.equals("double")) {
                        table.put(theType, extraDims + "[D");
                    } else if (baseClass.equals("boolean")) {
                        table.put(theType, extraDims + "[Z");
                    } else if (baseClass.equals("byte")) {
                        table.put(theType, extraDims + "[B");
                    } else if (baseClass.equals("short")) {
                        table.put(theType, extraDims + "[S");
                    } else if (baseClass.startsWith("[")) {
                        // The base for this array is another array!!						
                        String arrayOfBase = "[" + baseClass;
                        table.put(theType, arrayOfBase);
                    } else {
                        String arrayOfBase = extraDims + "[L" + baseClass + ";";
                        table.put(theType, arrayOfBase);
                    }
                }
            }
        }

        // Now we'll resolve any arrays of arrays that are outstanding
        Iterator mi = multiArrays.iterator();
        while (mi.hasNext()) {
            SchemaType st = (SchemaType) mi.next();
            QName theType = st.getTypeName();
            if (theType == null) continue;
            
            QName arrayType = st.getArrayType();
            if (arrayType != null && theType != null) {
                String extraDims = "";
                for (int x = 1; x < st.getArrayDimension(); x++) {
                    extraDims += "[";
                }
                String baseClass = (String) typeMappings.get(arrayType);
                if (baseClass == null) {
                	baseClass = (String) table.get(arrayType);
                }
                if (baseClass != null) {
                    // Base class should be an array
                    if (baseClass.startsWith("[")) {
                        String arrayOfBase = "[" + baseClass;
                        table.put(theType, arrayOfBase);
                    }
                }
            }
        }
        
        // Finally we'll resolve any elements that are outstanding
        Iterator ei = elements.iterator();
        while (ei.hasNext()) {
            SchemaType st = (SchemaType) ei.next();
            QName theType = st.getTypeName();
            if (theType == null)
                continue;
            
            QName baseType = null;
            if (st instanceof ElementType) {
                baseType = ((ElementType) st).getElementType();
            }

        	String temp = (String) table.get(theType);
        	String className = null;
        	// If we have already mapped this type, we guess that we have a complexType and
        	// an Element with the same name. Check to see if the WSDL2Java generated classes
        	// for such a conflict exist and if so, set up mappings accordingly.
        	if (temp != null) {
        		try {
        			Class c = Class.forName(temp);
        			className = temp;
        		} catch (ClassNotFoundException cnf) {
        			try {
        				Class c = Class.forName(temp+"_Type", true, Thread.currentThread().getContextClassLoader());
        				className = temp + "_Type";
        				QName tempName = new QName(theType.getNamespaceURI(), ">"+theType.getLocalPart());
        				String tempClassName = temp+"_ElemType";
        				table.put(tempName, tempClassName);
        			} catch (ClassNotFoundException cnf2) {
        			}
        		}
        	} else if (baseType != null) {
                String baseClassName = (String) typeMappings.get(baseType);
                if (baseClassName == null) {
                	baseClassName = (String) table.get(baseType);
                }            	
                if (baseClassName != null) {
                    table.put(theType, baseClassName);
                }
            } else {
               className = convention.getClassNameForElementType(theType);
        	}
                
            if (className != null) {
                table.put(theType, className);
            }            	
        }
        
        // Copy all of the user defined mappings into the table
   		table.putAll(typeMappings);
                       
        Trc.exit(table);
        return table;
    }
 
    /**
     * Elements which are nillable and are based on xsd simple types should map to
     * the object wrapper version of the corresponding primitive type. This method
     * will return the wrapper class name for a given QName.
     */
    protected String getWrapperClassName(QName qn) {
        if (qn == null) return null;
        String ns = qn.getNamespaceURI();
        if (WSIFConstants.NS_URI_1999_SCHEMA_XSD.equals(ns)
            || WSIFConstants.NS_URI_2000_SCHEMA_XSD.equals(ns)
            || WSIFConstants.NS_URI_2001_SCHEMA_XSD.equals(ns)) {
            String lp = qn.getLocalPart();
            if (lp == null) return null;
            if (lp.equals("int")) {
            	return "java.lang.Integer";
            } else if (lp.equals("long")) {
            	return "java.lang.Long";            	
            } else if (lp.equals("float")) {
            	return "java.lang.Float";            	
            } else if (lp.equals("short")) {
            	return "java.lang.Short";
            } else if (lp.equals("double")) {
            	return "java.lang.Double";
            } else if (lp.equals("boolean")) {
            	return "java.lang.Boolean";
            } else if (lp.equals("byte")) {
            	return "java.lang.Byte";            	
            }           
        }
        return null;
    }   
}

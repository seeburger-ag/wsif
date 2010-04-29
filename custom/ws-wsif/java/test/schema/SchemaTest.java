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

package schema;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.schema.Parser;
import org.apache.wsif.util.WSIFUtils;
import org.apache.wsif.wsdl.WSIFWSDLLocatorImpl;

import util.TestUtilities;

/**
 * Junit test to test parsing of Schemas to generate xml -> Java mappings
 * @author Owen Burroughs
 */
public class SchemaTest extends TestCase {
    String wsdlLocation1 = TestUtilities.getWsdlPath("java\\test\\schema") + "SchemaTest1.wsdl";
    String wsdlLocation2 = TestUtilities.getWsdlPath("java\\test\\schema") + "SchemaTest2.wsdl";    
    String wsdlLocation3 = TestUtilities.getWsdlPath("java\\test\\schema") + "SchemaTest3.wsdl";    

	Hashtable expectedA = new Hashtable();	
	Hashtable expectedB = new Hashtable();
        
    public SchemaTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(SchemaTest.class);
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
        expectedA.put(
            new QName("http://types.wsiftest/", "addresses"),
            "[Lwsiftest.types.Address;");
        expectedA.put(
            new QName("http://types.wsiftest/", "InfoResponse"),
            "wsiftest.types.InfoResponseElement");
        expectedA.put(
            new QName("http://types.wsiftest/", "ArrayOfArrayOfAddress"),
            "[[Lwsiftest.types.Address;");
        expectedA.put(
            new QName("http://types.wsiftest/", "phone"),
            "wsiftest.types.Phone");
        expectedA.put(
            new QName("http://types.wsiftest/", "ArrayOfLong"),
            "[[J");
        expectedA.put(
            new QName("http://types.wsiftest/", "address"),
            "wsiftest.types.Address");
        expectedA.put(
            new QName("http://types.wsiftest/", "InfoRequest"),
            "wsiftest.types.InfoRequestElement");
        expectedA.put(new QName("http://types.wsiftest/", "ArrayOfInt"), "[I");
        expectedA.put(
            new QName("http://types.wsiftest/", "ArrayOfAddress"),
            "[Lwsiftest.types.Address;");
            
            
        // For nested xsd files namepsaces/results are slightly different    
        expectedB.put(
            new QName("http://types.wsiftest/", "addresses"),
            "[Lwsiftest.types2.Address;");
        expectedB.put(
            new QName("http://types.wsiftest/", "InfoResponse"),
            "wsiftest.types.InfoResponseElement");
        expectedB.put(
            new QName("http://types2.wsiftest/", "ArrayOfArrayOfAddress"),
            "[[Lwsiftest.types2.Address;");
        expectedB.put(
            new QName("http://types2.wsiftest/", "phone"),
            "wsiftest.types2.Phone");
        expectedB.put(
            new QName("http://types.wsiftest/", "ArrayOfLong"),
            "[[J");
        expectedB.put(
            new QName("http://types2.wsiftest/", "address"),
            "wsiftest.types2.Address");
        expectedB.put(
            new QName("http://types.wsiftest/", "InfoRequest"),
            "wsiftest.types.InfoRequestElement");
        expectedB.put(new QName("http://types.wsiftest/", "ArrayOfInt"), "[I");
        expectedB.put(
            new QName("http://types2.wsiftest/", "ArrayOfAddress"),
            "[Lwsiftest.types2.Address;");            
    }

    public void testParsing() {
        doIt(wsdlLocation1, expectedA); 
        doIt(wsdlLocation2, expectedA);                               
        doIt(wsdlLocation3, expectedB);        
    }


    private void doIt(String wsdl, Hashtable expectedResults) {
        try {
        	System.out.println("*** " + wsdl + " ***\n");
        	Definition def = WSIFUtils.readWSDL(null, wsdl);
        	Hashtable table = new Hashtable();
        	Parser.getTypeMappings(def, table, false);
        	if (table != null) {
	            System.out.println("WITHOUT STANDARD MAPPINGS:");
	            Enumeration k = table.keys();
				while(k.hasMoreElements()) {
					QName qn = (QName) k.nextElement();
					String cl = (String) table.get(qn);
					System.out.println(qn + " -> " + cl);
				}
        	} else {
        		System.out.println("WITHOUT STANDARD MAPPINGS RETURNED NULL!!");
        		assertTrue("With wsdl "+wsdl+", table without standard mappings was null", false);
        	}
        	checkResult(wsdl, table, expectedResults);
        	System.out.println("- - - -");
        	Hashtable table2 = new Hashtable();
        	Parser.getTypeMappings(def, table2);
        	if (table2 != null) {
	            System.out.println("WITH STANDARD MAPPINGS:");
	            Enumeration k = table2.keys();
				while(k.hasMoreElements()) {
					QName qn = (QName) k.nextElement();
					String cl = (String) table2.get(qn);
					System.out.println(qn + " -> " + cl);
				}
        	} else {
        		System.out.println("WITH STANDARD MAPPINGS RETURNED NULL!!");
        		assertTrue("With wsdl "+wsdl+", table with standard mappings was null", false);
        	}
        	checkResult(wsdl, table2, expectedResults);
        	System.out.println("-----------------------------------------------------\n");        	
        } catch (Exception e) {
            System.err.println(
                "SchemaTest caught exception " + e);
            e.printStackTrace();
            assertTrue(false);
        } finally {
        	TestUtilities.resetDefaultProviders();
        }
    }
    
    private void doItWithClassLoader(String wsdl, Hashtable expectedResults) {
        try {
        	System.out.println("*** " + wsdl + " ***\n");
         	ClassLoader loader = this.getClass().getClassLoader();
        	Definition def = WSIFUtils.readWSDL(null, wsdl, loader);
        	Hashtable table = new Hashtable();
        	Parser.getTypeMappings(def, table, loader, false);
        	if (table != null) {
	            System.out.println("WITHOUT STANDARD MAPPINGS:");
	            Enumeration k = table.keys();
				while(k.hasMoreElements()) {
					QName qn = (QName) k.nextElement();
					String cl = (String) table.get(qn);
					System.out.println(qn + " -> " + cl);
				}
        	} else {
        		System.out.println("WITHOUT STANDARD MAPPINGS RETURNED NULL!!");
        		assertTrue("With wsdl "+wsdl+" and using a classloader, table without standard mappings was null", false);        		
        	}
        	checkResult(wsdl, table, expectedResults);        	
        	System.out.println("- - - -");
        	Hashtable table2 = new Hashtable();
        	Parser.getTypeMappings(def, table2, loader, true);
        	if (table2 != null) {
	            System.out.println("WITH STANDARD MAPPINGS:");
	            Enumeration k = table2.keys();
				while(k.hasMoreElements()) {
					QName qn = (QName) k.nextElement();
					String cl = (String) table2.get(qn);
					System.out.println(qn + " -> " + cl);
				}
        	} else {
        		System.out.println("WITH STANDARD MAPPINGS RETURNED NULL!!");
        		assertTrue("With wsdl "+wsdl+" and using a classloader, table with standard mappings was null", false);        		
        	}
        	checkResult(wsdl, table2, expectedResults);        	
        	System.out.println("-----------------------------------------------------\n");        	
        } catch (Exception e) {
            System.err.println(
                "SchemaTest caught exception " + e);
            e.printStackTrace();
            assertTrue(false);
        } finally {
        	TestUtilities.resetDefaultProviders();
        }
    }
    
    private void checkResult(String wsdl, Hashtable results, Hashtable expected) {
    	Enumeration enum = expected.keys();
    	while(enum.hasMoreElements()) {
    		QName key = (QName) enum.nextElement();
    		String value = (String) expected.get(key);
    		if (results.containsKey(key)) {
    			String resultVal = (String) results.get(key);
    			if (!value.equals(resultVal)) {
    				assertTrue("Using wsdl "+wsdl+" results did not contain correct value for type "+key
    				+ ", value should be "+value+" but result was "+resultVal, false);
    			}
    		} else {
    			assertTrue("Using wsdl "+wsdl+" results did not contain type "+key, false);
    		}
    	}
    }
}

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

package wsdl;

import javax.xml.namespace.QName;

import junit.framework.*;

public class QNameTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(QNameTest.class);
    }

    public QNameTest(String name) {
        super(name);
    }

    /**
     * Tests the Java binding for the shopping cart scenario via WSDL
     */
    public void testEquals() throws Exception {
    	String base = "http://schemas.xmlsoap.org/wsdl/";
		QName qn1 = new QName("http://schemas.xmlsoap.org/wsdl/java/", "java");
		assertTrue("One instance is the same as itself", qn1.equals(qn1));
		QName qn2 = new QName("http://schemas.xmlsoap.org/wsdl/java/", "java");
		assertTrue("Two equivalent instances are equal qn1=qn2", qn1.equals(qn2));
		assertTrue("Two equivalent instances are equal qn2=qn1", qn2.equals(qn1));

		QName qn3 = new QName("http://ibm.com/","java");
		assertTrue("Different URI: instances are not equal qn3!=qn1", !qn3.equals(qn1));
		assertTrue("Different URI: instances are not equal qn1!=qn3", !qn1.equals(qn3));

		QName qn4 = new QName("http://schemas.xmlsoap.org/wsdl/java/", "soap");
		assertTrue("Different name: instances are not equal qn4!=qn1", !qn4.equals(qn1));
		assertTrue("Different name: instances are not equal qn1!=qn4", !qn1.equals(qn4));
		
		QName qn5 = new QName("http://schemas.xmlsoap.org/wsdl/java", "java");
		assertTrue("URI: No trailing / same as trailing slash URI", qn5.equals(qn1));
		assertTrue("URI: No trailing / same as trailing slash URI", qn1.equals(qn5));
		
    }
}

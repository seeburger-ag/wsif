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

/**
 * Junit test to test using ClassLoader to find wsdl and more importantly
 * imports in the wsdl
 * 
 * -----------------------------------------------------------------
 * NOTE: YOU MUST HAVE THE ClassloaderTest.jar ON YOUR CLASSPATH TO
 *       BE ABLE TO RUN THIS TEST
 * -----------------------------------------------------------------
 * 
 * @author Owen Burroughs
 */

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import javax.wsdl.Definition;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.util.WSIFUtils;
import util.TestUtilities;

public class ClassLoaderTest extends TestCase {
    String wsdlLocation = "test/ImportingTest.wsdl";
    String wsdlLocation2 = "test/ImportingTest2.wsdl";
    String wsdlLocation3 = "test/ImportingTest3.wsdl";
    String wsdlLocation4 = "ImportingTest4.wsdl";
    String wsdlLocation5 = "test/subtest/ImportingTest5.wsdl";
    String wsdlLocation6 = "test/subtest/subtest2/ImportingTest6.wsdl";         
    String wsdlLocation7 =
        TestUtilities.getWsdlPath("java\\test\\addressbook")
            + "ImportingAddressBook.wsdl";
    String wsdlLocationError = "test/subtest/subtest2/ImportingTestError.wsdl";
    String wsdlLocationA = "ImportingTestA.wsdl";              

    public ClassLoaderTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(ClassLoaderTest.class);
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
    }

    // wsdl location	= test/ImportingTest.wsdl
    // import location	= AddressBookTest.wsdl
    public void test() {
        doIt(wsdlLocation);
    }

    // wsdl location	= test/ImportingTest2.wsdl
    // import location	= /test/subtest/subtest2/AddressBookTest2.wsdl
    public void test2() {
        doIt(wsdlLocation2);
    }

    // wsdl location	= test/ImportingTest3.wsdl
    // import location	= subtest/subtest2/AddressBookTest3.wsdl
    public void test3() {
        doIt(wsdlLocation3);
    }

    // wsdl location	= ImportingTest4.wsdl
    // import location	= test/subtest/subtest2/AddressBookTest4.wsdl
    public void test4() {
        doIt(wsdlLocation4);
    }

    // wsdl location	= test/subtest/ImportingTest5.wsdl
    // import location	= ../AddressBookTest.wsdl
    public void test5() {
        doIt(wsdlLocation5);
    }

    // wsdl location	= test/subtest/subtest2/ImportingTest6.wsdl
    // import location	= ../../AddressBookTest.wsdl
    public void test6() {
        doIt(wsdlLocation6);
    }

    // wsdl location	= <wsdlPath>/java/test/addressbook/ImportingAddressBook.wsdl
    // import location	= http://localhost:8080/wsdl/AddressBook.wsdl
    public void test7() {
        if (TestUtilities.areWeTesting("remotewsdl")) {
            doIt(wsdlLocation7);
        }
    }
    
    public void test8() {
    	doItWithReader(wsdlLocation2);
    }

    // wsdl location	= ImportingTestA.wsdl
    // import location	= /test/ImportingTest.wsdl
    // import location2	= AddressBookTest.wsdl
    public void test9() {
    	doIt(wsdlLocationA);
    }

    // wsdl location	= test/subtest/subtest2/ImportingTestError.wsdl
    // import location	= ../../../../AddressBookTest.wsdl
    public void testError() {
    	doItError(wsdlLocationError);
    }

    private void doIt(String wsdlLoc) {
        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory
                    .getService(wsdlLoc, this.getClass().getClassLoader(), null,
                                // serviceNS
                                null, // serviceName
                                "http://wsifservice.addressbook/", // portTypeNS
                                "AddressBook"); // portTypeName
        } catch (Exception e) {
            System.err.println(
                "ClassLoaderTest(" + wsdlLoc + ") caught exception " + e);
            e.printStackTrace();
            assertTrue(false);
        }
    }

    private void doItError(String wsdlLoc) {
        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory
                    .getService(wsdlLoc, this.getClass().getClassLoader(), null,
                                // serviceNS
                                null, // serviceName
                               "http://wsifservice.addressbook/", // portTypeNS
                               "AddressBook"); // portTypeName
    		assertTrue("Import location of '../../../../AddressBook.wsdl'" +
    					"should have caused an error", false);
        } catch (Exception e) {
        	// Excpected so ignore
        }
    }

    private void doItWithReader(String wsdlLoc) {
        InputStream in = null;
        ClassLoader loader = this.getClass().getClassLoader();
        try {
            URL url = null;
            if (wsdlLoc.indexOf(":") == -1)
                url = new URL("file", null, wsdlLoc);
            else
                url = new URL(wsdlLoc);
            String wsdlRelativeLocation = url.getPath();
            if (wsdlRelativeLocation.startsWith("/"))
                wsdlRelativeLocation = wsdlRelativeLocation.substring(1);
            in = loader.getResourceAsStream(wsdlRelativeLocation);
            Reader reader = new InputStreamReader(in);
            Definition def = WSIFUtils.readWSDL(url, reader, loader);
            assertNotNull("Definition is null!!", def);
        } catch (Exception e) {
            System.err.println(
                "ClassLoaderTest(" + wsdlLoc + ") caught exception " + e);
            assertTrue(false);
        }
    }
}

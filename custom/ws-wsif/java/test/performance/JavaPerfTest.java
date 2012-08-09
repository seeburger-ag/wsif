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
package performance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.base.WSIFServiceImpl;
import util.AddressUtility;
import util.TestUtilities;

import addressbook.wsifservice.AddressBook;
import addressbook.wsiftypes.Address;
import addressbook.wsiftypes.Phone;

/**
 * Junit test to measure the performance of the WSIF Java provider.
 * 
 * based on the SpeedTest testcase.
 * 
 * @author <a href="mailto:antelder@apache.org">Ant Elder</a>
 */
public class JavaPerfTest extends TestCase {

    static final int DEFAULT_ITERATIONS = 1000000;
    
    static final String wsdlLocation =
        TestUtilities.getWsdlPath("java\\test\\addressbook\\wsifservice")
            + "AddressBook.wsdl";

    static String name1 = "Purdue Boilermaker";
    static Address addr1 =
        new Address(
            1,
            "University Drive",
            "West Lafayette",
            "IN",
            47907,
            new Phone(765, "494", "4900"));

    static String firstName2 = "Someone";
    static String lastName2 = "Else";
    static Address addr2 =
        new Address(
            0,
            "Somewhere Else",
            "No Where",
            "NO",
            71983,
            new Phone(600, "391", "5682"));

    public JavaPerfTest(String name) {
        super(name);
    }

	public static void main(String[] args) {
//	   TestUtilities.startListeners();	
	   junit.textui.TestRunner.run (suite());
//       TestUtilities.stopListeners();
	}

    public static Test suite() {
        TestSuite suite = new TestSuite("Speed Tests");
		suite.addTest(new TestSuite(JavaPerfTest.class) );
		//suite.addTest(new TestSuite(SpeedTest.class) );
		//suite.addTest(new TestSuite(SpeedTest.class) );
        return suite;
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
        Monitor.clear();
    }

    public void testJavaDyn() {
        doitDyn("JavaPort", "java");
    }
    public void testJavaDynTotal() {
        doitDynTotal("JavaPort", "java");
    }
    public void testJavaStub() {
        doitStub("JavaPort", "java");
    }
    public void testJavaStubTotal() {
        doitStubTotal("JavaPort", "java");
    }

    private void doitDyn(String portName, String protocol) {
        int iterations;
        String testName;
//        String testNamePrefix = "doitDyn" + protocol + "." + portName;
        String testNamePrefix = "doitDyn";
        Monitor.clear();

        if (portName.toUpperCase().indexOf("JMS") != -1
            && !TestUtilities.areWeTesting("jms"))
            return;

        TestUtilities.setProviderForProtocol( protocol );

        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service = factory.getService(wsdlLocation, null, // serviceNS 
               null, // serviceName 
               "http://wsifservice.addressbook/", // portTypeNS 
               "AddressBook"); // portTypeName 

            service.mapType(
                new javax.xml.namespace.QName(
                    "http://wsiftypes.addressbook/",
                    "address"),
                Class.forName("addressbook.wsiftypes.Address"));

            service.mapType(
                new javax.xml.namespace.QName(
                    "http://wsiftypes.addressbook/",
                    "phone"),
                Class.forName("addressbook.wsiftypes.Phone"));

            WSIFPort port = null;

            port = service.getPort(portName);

            Monitor.start();
            
            /*
             * Run iterations of getPort
             */
            testName = testNamePrefix + ".getPort";
            iterations = DEFAULT_ITERATIONS;
//            iterations = 1;
            System.out.println( "running " + iterations + " " + testName + " iterations..." ); 
            for (int i = 0; i < iterations; i++ ) {
               Monitor.start( testName );
               port = service.getPort(portName);
               Monitor.stop( testName );
            }

            WSIFOperation operation =
                port.createOperation("addEntry", "AddEntryWholeNameRequest", null);

            WSIFMessage inputMessage = operation.createInputMessage();
            WSIFMessage outputMessage = operation.createOutputMessage();
            WSIFMessage faultMessage = operation.createFaultMessage();

            // Create a name and address to add to the addressbook 
            String nameToAdd = "Chris P. Bacon";
            Address addressToAdd =
                new Address(
                    1,
                    "The Waterfront",
                    "Some City",
                    "NY",
                    47907,
                    new Phone(765, "494", "4900"));

            // Add the name and address to the input message 
            inputMessage.setObjectPart("name", nameToAdd);
            inputMessage.setObjectPart("address", addressToAdd);

            // Execute the operation, obtaining a flag to indicate its success 
            boolean ok=
                operation.executeRequestResponseOperation(
                    inputMessage,
                    outputMessage,
                    faultMessage);

            assertTrue( "failed to add name and address!!", ok );

            /*
             * Run iterations of createOperation
             */
            testName = testNamePrefix + ".createOperation";
            iterations = DEFAULT_ITERATIONS;
//            iterations = 1;
            System.out.println( "running " + iterations + " " + testName + " iterations..." ); 
            for (int i = 0; i < iterations; i++ ) {
               Monitor.start( testName );
			   operation = port.createOperation("getAddressFromName");
               Monitor.stop( testName );
            }

            /*
             * Run iterations of executeRequestResponseOperation
             */
            testName = testNamePrefix + ".getAddressFromName";
            iterations = DEFAULT_ITERATIONS;
//            iterations = 1;
            System.out.println( "running " + iterations + " " + testName + " iterations..." ); 
            for (int i = 0; i < iterations; i++ ) {
			   operation = port.createOperation("getAddressFromName");
			   inputMessage = operation.createInputMessage();
			   outputMessage = operation.createOutputMessage();
			   faultMessage = operation.createFaultMessage();

			   inputMessage.setObjectPart("name", nameToAdd);

               Monitor.start( testName );
			   boolean operationSucceeded =
				  operation.executeRequestResponseOperation(
					 inputMessage,
					 outputMessage,
					 faultMessage);
               Monitor.stop( testName );
			   if (!operationSucceeded) {
				  System.out.println("Failed to lookup name in addressbook");
				  assertTrue("executing op returned false!!", false);
			   }
            }

            // make sure it all worked
 		    Address addressFound =
			   (Address) outputMessage.getObjectPart("address");
			assertTrue( "returned address not correct!!", 
			   addressToAdd.equals( addressFound) ); 
            
            Monitor.stop();
            Monitor.printResults();
           
       } catch (Exception ex) {
	       ex.printStackTrace();
		   assertTrue("exception executing op!!", false);
	   }
    }

    private void doitDynTotal(String portName, String protocol) {
        int iterations;
        String testName;
//        String testNamePrefix = "doitDyn" + protocol + "." + portName;
        String testNamePrefix = "doitDynTotal";
        Monitor.clear();

        if (portName.toUpperCase().indexOf("JMS") != -1
            && !TestUtilities.areWeTesting("jms"))
            return;

        TestUtilities.setProviderForProtocol( protocol );

        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service = factory.getService(wsdlLocation, null, // serviceNS 
               null, // serviceName 
               "http://wsifservice.addressbook/", // portTypeNS 
               "AddressBook"); // portTypeName 

            service.mapType(
                new javax.xml.namespace.QName(
                    "http://wsiftypes.addressbook/",
                    "address"),
                Class.forName("addressbook.wsiftypes.Address"));

            service.mapType(
                new javax.xml.namespace.QName(
                    "http://wsiftypes.addressbook/",
                    "phone"),
                Class.forName("addressbook.wsiftypes.Phone"));

            // Create a name and address to add to the addressbook 
            String nameToAdd = "Chris P. Bacon";
            Address addressToAdd =
                new Address(
                    1,
                    "The Waterfront",
                    "Some City",
                    "NY",
                    47907,
                    new Phone(765, "494", "4900"));


            WSIFPort port = null;
            WSIFMessage inputMessage = null;
            WSIFMessage outputMessage = null;
            WSIFMessage faultMessage = null;

            Monitor.start();

            /*
             * Run iterations
             */
            testName = testNamePrefix + ".total";
            iterations = DEFAULT_ITERATIONS;
//            iterations = 1;
            System.out.println( "running " + iterations + " " + testName + " iterations..." ); 

            for (int i = 0; i < iterations; i++ ) {
               Monitor.start(testNamePrefix + ".total");

               testName = testNamePrefix + ".getPort";
               Monitor.start( testName );
               port = service.getPort(portName);
               Monitor.stop( testName );

               testName = testNamePrefix + ".addEntry.ceateOperation";
               Monitor.start( testName );
               WSIFOperation operation =
                  port.createOperation("addEntry", "AddEntryWholeNameRequest", null);
               Monitor.stop( testName );

               testName = testNamePrefix + ".addEntry.createMessages";
               Monitor.start( testName );
               inputMessage = operation.createInputMessage();
               outputMessage = operation.createOutputMessage();
               faultMessage = operation.createFaultMessage();
               Monitor.stop( testName );

               // Add the name and address to the input message 
               inputMessage.setObjectPart("name", nameToAdd);
               inputMessage.setObjectPart("address", addressToAdd);

               testName = testNamePrefix + ".addEntry.execute";
               Monitor.start( testName );
               // Execute the operation, obtaining a flag to indicate its success 
               boolean ok=
                  operation.executeRequestResponseOperation(
                     inputMessage,
                     outputMessage,
                     faultMessage);
               Monitor.stop( testName );

               assertTrue( "failed to add name and address!!", ok );

               testName = testNamePrefix + ".getAddressFromName.createOperation";
               Monitor.start( testName );
			   operation = port.createOperation("getAddressFromName");
               Monitor.stop( testName );


               testName = testNamePrefix + ".getAddressFromName.createMessages";
               Monitor.start( testName );
			   operation = port.createOperation("getAddressFromName");
			   inputMessage = operation.createInputMessage();
			   outputMessage = operation.createOutputMessage();
			   faultMessage = operation.createFaultMessage();
               Monitor.stop( testName );

			   inputMessage.setObjectPart("name", nameToAdd);

               testName = testNamePrefix + ".getAddressFromName.execute";
               Monitor.start( testName );
			   boolean operationSucceeded =
				  operation.executeRequestResponseOperation(
					 inputMessage,
					 outputMessage,
					 faultMessage);
               Monitor.stop( testName );

			   if (!operationSucceeded) {
				  System.out.println("Failed to lookup name in addressbook");
				  assertTrue("executing op returned false!!", false);
			   }
			   
               Monitor.stop(testNamePrefix + ".total");
            }

            Monitor.stop();
            Monitor.printResults();

            // make sure it all worked
 		    Address addressFound =
			   (Address) outputMessage.getObjectPart("address");
			assertTrue( "returned address not correct!!", 
			   addressToAdd.equals( addressFound) ); 
                       
       } catch (Exception ex) {
	       ex.printStackTrace();
		   assertTrue("exception executing op!!", false);
	   }
    }

    private void doitStub(String portName, String protocol) {
        int iterations;
        String testName;
//        String testNamePrefix = "doitDyn" + protocol + "." + portName;
        String testNamePrefix = "doitStub";
        Monitor.clear();

        if (portName.toUpperCase().indexOf("JMS") != -1
            && !TestUtilities.areWeTesting("jms"))
            return;

        TestUtilities.setProviderForProtocol( protocol );

        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service = factory.getService(
                wsdlLocation, 
                null, // serviceNS 
                null, // serviceName 
                "http://wsifservice.addressbook/", // portTypeNS 
                "AddressBook"); // portTypeName 

            Monitor.start();
            Monitor.start( testNamePrefix + ".total" );

            /*
             * Run iterations of getStub
             */
            AddressBook abStub = null;
            testName = testNamePrefix + ".getStub";
            iterations = DEFAULT_ITERATIONS;
//            iterations = 1;
            System.out.println( "running " + iterations + " " + testName + " iterations..." ); 
            for (int i = 0; i < iterations; i++ ) {
               Monitor.start( testName );
               abStub = (AddressBook) service.getStub(portName, AddressBook.class);
               Monitor.stop( testName );
            }

            /*
             * Run iterations of addEntry
             */
            testName = testNamePrefix + ".addEntry";
            iterations = DEFAULT_ITERATIONS;
//            iterations = 1;
            System.out.println( "running " + iterations + " " + testName + " iterations..." ); 
            for (int i = 0; i < iterations; i++ ) {
               Monitor.start( testName );
               abStub.addEntry(name1, addr1);
               Monitor.stop( testName );
            }

            /*
             * Run iterations of getAddressFromName
             */
            testName = testNamePrefix + ".getAddressFromName";
            iterations = DEFAULT_ITERATIONS;
//            iterations = 1;
            System.out.println( "running " + iterations + " " + testName + " iterations..." ); 
            for (int i = 0; i < iterations; i++ ) {
               Address resp1 = null;
               Monitor.start( testName );
               resp1 = abStub.getAddressFromName(name1);
               Monitor.stop( testName );
               assertTrue(new AddressUtility(addr1).equals(resp1));
            }

            Monitor.start( testNamePrefix + ".total" );
            Monitor.stop();
            Monitor.printResults();
           
       } catch (Exception ex) {
	       ex.printStackTrace();
		   assertTrue("exception executing op!!", false);
	   }
    }

    private void doitStubTotal(String portName, String protocol) {
        int iterations;
        String testName;
//        String testNamePrefix = "doitDyn" + protocol + "." + portName;
        String testNamePrefix = "doitStubTotal";
        Monitor.clear();

        if (portName.toUpperCase().indexOf("JMS") != -1
            && !TestUtilities.areWeTesting("jms"))
            return;

        TestUtilities.setProviderForProtocol( protocol );

        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service = factory.getService(
                wsdlLocation, 
                null, // serviceNS 
                null, // serviceName 
                "http://wsifservice.addressbook/", // portTypeNS 
                "AddressBook"); // portTypeName 

            testName = testNamePrefix + ".total";
            iterations = DEFAULT_ITERATIONS;
//            iterations = 1;
            System.out.println( "running " + iterations + " " + testName + " iterations..." ); 
            Monitor.start();
            for (int j = 0; j < iterations; j++ ) {

               Monitor.start( testNamePrefix + ".total" );

               AddressBook abStub = null;
               testName = testNamePrefix + ".getStub";
               Monitor.start( testName );
               abStub = (AddressBook) service.getStub(portName, AddressBook.class);
               Monitor.stop( testName );


               testName = testNamePrefix + ".addEntry";
               Monitor.start( testName );
               abStub.addEntry(name1, addr1);
               Monitor.stop( testName );

               testName = testNamePrefix + ".getAddressFromName";
               Address resp1 = null;
               Monitor.start( testName );
               resp1 = abStub.getAddressFromName(name1);
               Monitor.stop( testName );
               assertTrue(new AddressUtility(addr1).equals(resp1));

               Monitor.stop( testNamePrefix + ".total" );
            }
            
            Monitor.stop();
            Monitor.printResults();
           
       } catch (Exception ex) {
	       ex.printStackTrace();
		   assertTrue("exception executing op!!", false);
	   }
    }

}
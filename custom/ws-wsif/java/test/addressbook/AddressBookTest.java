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

package addressbook;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFCorrelationId;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.providers.soap.apacheaxis.WSIFDynamicProvider_ApacheAxis;
import org.apache.wsif.util.WSIFPluggableProviders;
import util.AddressUtility;
import util.TestUtilities;

import addressbook.wsifservice.AddressBook;
import addressbook.wsiftypes.Address;
import addressbook.wsiftypes.Phone;
import async.AsyncResponseHandler;

/**
 * Junit test to test out the AddressBook sample.
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class AddressBookTest extends TestCase {
    String wsdlLocation =
        TestUtilities.getWsdlPath("java\\test\\addressbook\\wsifservice")
            + "AddressBook.wsdl";
    static String server = TestUtilities.getSoapServer().toUpperCase();

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

    public AddressBookTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestUtilities.startListeners(
            TestUtilities.ADDRESSBOOK_LISTENER
                | TestUtilities.ASYNC_LISTENER
                | TestUtilities.NATIVEJMS_LISTENER);

        junit.textui.TestRunner.run(suite());
        TestUtilities.stopListeners();
    }

    public static Test suite() {
        return new TestSuite(AddressBookTest.class);
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
    }

    public void testAxis() {
        doit(server+"Port", "axis");
    }
    public void testSoap() {
        doit(server+"Port", "soap");
    }
    public void testJava() {
        doit("JavaPort", "java");
    }
    public void testSoapJms() {
        doit("SOAPJMSPort", "soap");
    }
    public void testAxisJms() {
        doit("SOAPJMSPort", "axis");
    }
    public void testNativeJms() {
        doit("NativeJmsPort", "" ); 
    }
    public void testDynamicSOAP() {
        doitDyn(server+"Port", "soap", false);
    }
    public void testDynamicAxis() {
        doitDyn(server+"Port", "axis", false);
    }
    public void testDynamicJava() {
        doitDyn("JavaPort", "java", false);
    }
    public void testDynamicSoapJms() {
        doitDyn("SOAPJMSPort", "soap", false);
    }
    public void testDynamicAxisJms() {
        doitDyn("SOAPJMSPort", "axis", false);
    }
    public void testDynamicNativeJms() {
        doitDyn("NativeJmsPort", "", false); 
    }
    public void testDynamicAxisAuto() {
        doitDyn(server+"Port", "axis", true);
    }
    public void testDynamicAxisJmsAuto() {
        doitDyn("SOAPJMSPort", "axis", true);
    }

    private void doit(String portName, String protocol) {
        if (portName.toUpperCase().indexOf("JMS") != -1
            && !TestUtilities.areWeTesting("jms"))
            return;

        TestUtilities.setProviderForProtocol( protocol );

        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service = factory.getService(wsdlLocation, null, // serviceNS
               null, // serviceName
               "http://wsifservice.addressbook/", // portTypeNS
               "AddressBook" ); // portTypeName

            AddressBook abStub = (AddressBook) service.getStub(portName, AddressBook.class);

            abStub.addEntry(name1, addr1);
            abStub.addEntry(firstName2, lastName2, addr2);

            Address resp1 = abStub.getAddressFromName(name1);
            assertTrue(new AddressUtility(addr1).equals(resp1));

            Address resp2 = abStub.getAddressFromName(firstName2 + " " + lastName2);
            assertTrue(new AddressUtility(addr2).equals(resp2));
            
      	    if (TestUtilities.areWeTesting("async")) {
      	    	testAsyncOPs( service, portName, name1, addr1 );
      	    	testAsyncOPs( service, portName, firstName2 + " " + lastName2, addr2 );
      	    }
            
        } catch (Exception e) {
            System.err.println("AddressBookTest(" + portName + ") caught exception " + e);
            e.printStackTrace();
            assertTrue(false);
        } finally {
            TestUtilities.resetDefaultProviders();
        }
    }

    private void doitDyn(String portName, String protocol, boolean autoMapTypes) {
        if (portName.toUpperCase().indexOf("JMS") != -1 && !TestUtilities.areWeTesting("jms"))
            return;

        TestUtilities.setProviderForProtocol( protocol );

        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            
            if (autoMapTypes) {
            	factory.setFeature(WSIFConstants.WSIF_FEATURE_AUTO_MAP_TYPES, new Boolean(true));
            }
            
            WSIFService service = factory.getService(wsdlLocation, null, // serviceNS 
                null, // serviceName 
                "http://wsifservice.addressbook/", // portTypeNS 
                "AddressBook"); // portTypeName 

			if (!autoMapTypes) {
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
			}

            WSIFPort port = null;

            port = service.getPort(portName);

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
            operation.executeInputOnlyOperation(inputMessage);

            // Start from fresh 
            operation = null;
            inputMessage = null;
            outputMessage = null;
            faultMessage = null;

            operation = port.createOperation("getAddressFromName");

            // Create the messages 
            inputMessage = operation.createInputMessage();
            outputMessage = operation.createOutputMessage();
            faultMessage = operation.createFaultMessage();

            // Set the name to find in the addressbook 
            String nameToLookup = "Chris P. Bacon";
            inputMessage.setObjectPart("name", nameToLookup);

            // Execute the operation 
            boolean operationSucceeded =
                operation.executeRequestResponseOperation(
                    inputMessage,
                    outputMessage,
                    faultMessage);

            if (operationSucceeded) {
                System.out.println(
                    "Successfull lookup of name '" + nameToLookup + "' in addressbook");

                // We can obtain the address that was found by querying the output message 
                Address addressFound = (Address) outputMessage.getObjectPart("address");
                System.out.println("The address found was:");
                System.out.println(addressFound);
            } else {
                System.out.println("Failed to lookup name in addressbook");
            }

            // Check that we can't reuse an operation.            
            boolean caughtException = false;
            try {
                operationSucceeded =
                    operation.executeRequestResponseOperation(
                        inputMessage,
                        outputMessage,
                        faultMessage);
            } catch (WSIFException we) {
                caughtException = true;
            }
            assertTrue(caughtException);
            	
        } catch (Exception e) {
            System.err.println("AddressBookTest(" + portName + ") caught exception " + e);
            e.printStackTrace();
            assertTrue(false);
        } finally {
        	TestUtilities.resetDefaultProviders();
        }
    }

    private void testAsyncOPs(WSIFService service, String portName, String name, Address addr) {
       try {
            WSIFPort port =
                (portName == null) ? service.getPort() : service.getPort(portName);

            if (!port.supportsAsync() ) {
            	return;
            }    

            WSIFOperation op = port.createOperation( "getAddressFromName" );
            
            AsyncResponseHandler abHandler = new AsyncResponseHandler( 1 ); // 1 async call

            WSIFMessage inMsg = op.createInputMessage();
            inMsg.setObjectPart( "name", name );

            WSIFMessage outmsg = op.createOutputMessage();
            WSIFMessage faultMsg = op.createFaultMessage();

            WSIFMessage context = op.getContext();
            context.setObjectPart( WSIFConstants.CONTEXT_JMS_PREFIX + "JMSReplyTo", 
                                   TestUtilities.getWsifProperty("wsif.async.replytoq") );
            op.setContext( context );

            WSIFCorrelationId id = op.executeRequestResponseAsync(inMsg, abHandler);
            assertTrue("null correlation id returned from async request!", id != null );

            int i = 10;
            while (i-- > 0 && !abHandler.isDone()) {
                System.out.println( "waiting for async responses - " + i );
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                }
            }
            assertTrue("no response from async operation!",i > 0); // no responses in time

            WSIFMessage[] faults = abHandler.getFaults();
            WSIFMessage[] outputs = abHandler.getOutputs();
            Address respAddr = (Address) outputs[0].getObjectPart("address");
            assertTrue("incorrect address response!", addr.equals(respAddr));

       } catch (Exception ex) {
       	  ex.printStackTrace();
          assertTrue( "exception making async request!!", false );
       }
    }
    
}

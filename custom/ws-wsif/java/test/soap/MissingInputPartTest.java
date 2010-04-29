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

package soap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.providers.soap.apacheaxis.WSIFDynamicProvider_ApacheAxis;
import org.apache.wsif.util.WSIFPluggableProviders;
import util.TestUtilities;

import addressbook.wsiftypes.Address;
import addressbook.wsiftypes.Phone;

/**
 * Junit test to test leaving out a part in the inpuyt message.
 * Missing parts should default to null.
 * @author Ant Elder <ant.elder@uk.ibm.com>
 */
public class MissingInputPartTest extends TestCase {
    String wsdlLocation =
        TestUtilities.getWsdlPath("java\\test\\addressbook\\wsifservice")
            + "AddressBook.wsdl";
    static String server = TestUtilities.getSoapServer().toUpperCase();

    public MissingInputPartTest(String name) {
        super(name);
    }

	public static void main(String[] args) {
        TestUtilities.startListeners(
            TestUtilities.ADDRESSBOOK_LISTENER
                | TestUtilities.NATIVEJMS_LISTENER);

	    junit.textui.TestRunner.run (suite());
        TestUtilities.stopListeners();	
	}

    public static Test suite() {
        return new TestSuite(MissingInputPartTest.class);
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
    }

    public void testDynamicSOAP() {
        doitDyn(server+"Port", "soap");
    }
    public void testDynamicJava() {
        doitDyn("JavaPort", "java");
    }
    public void testDynamicSoapJms() {
        doitDyn("SOAPJMSPort", "soap");
    }
// TODO Axis doesn't work as we don't have an axis service
//     the soap service doesn't understand the axis nil value
//    public void testDynamicAxis() {
//        doitDyn(server+"Port", "axis");
//    }
//    public void testDynamicAxisJms() {
//        doitDyn("SOAPJMSPort", "axis");
//    }
    public void testDynamicNativeJms() {
        doitDyn("NativeJmsPort", "" ); 
    }

    private void doitDyn(String portName, String protocol) {

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

            WSIFOperation operation =
                port.createOperation("addEntry", "AddEntryWholeNameRequest", null);

            WSIFMessage inputMessage = operation.createInputMessage();
            WSIFMessage outputMessage = operation.createOutputMessage();
            WSIFMessage faultMessage = operation.createFaultMessage();

            // Create a name and address to add to the addressbook 
            String nameToAdd = "Chris P. Bacon";

            // Add the name and leave out address to the input message 
            inputMessage.setObjectPart("name", nameToAdd);
            //inputMessage.setObjectPart("address", null); // should default to null

            // Execute the operation, obtaining a flag to indicate its success 
            boolean ok =
                operation.executeRequestResponseOperation(
                    inputMessage,
                    outputMessage,
                    faultMessage);

            assertTrue( "Failed to add name and address to addressbook", ok );

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
            ok = operation.executeRequestResponseOperation(
                    inputMessage,
                    outputMessage,
                    faultMessage);

            assertTrue( "Failed to lookup name from addressbook", ok );

            // Should have defaulted to null
            Address addressFound = (Address) outputMessage.getObjectPart("address");
            if ( addressFound != null ) {
               assertTrue( "city not null!!", addressFound.getCity() == null );
               assertTrue( "PhoneNumber not null!!", addressFound.getPhoneNumber() == null );
               assertTrue( "State not null!!", addressFound.getState() == null );
               assertTrue( "StreetName not null!!", addressFound.getStreetName() == null );
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue("AddressBookTest(" + portName + ") caught exception " + ex, false);
        } finally {
            if (protocol.equals("axis")) {
               WSIFPluggableProviders.overrideDefaultProvider(
                    "http://schemas.xmlsoap.org/wsdl/soap/",
                    null);
            }
        }
    }

}

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

import org.apache.wsif.WSIFException;
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
 * Junit test to test out input message parts using the AddressBook sample.
 * 
 * The WSDL input message can define multiple parts, but the parts in the 
 * WSIFMessage passed to the WSIFOperation may not match what the WSDL defines. 
 * Its been decided the parts defined in the WSDL but not in the WSIFMessage 
 * should be sent as null, and parts in the WSIFMessage but not in the WSDL 
 * should be ignored.
 *   
 * @author Ant Elder <ant.elder@uk.ibm.com>
 */
public class InputPartsTest extends TestCase {
    static final String WSDL_LOCATION =
        TestUtilities.getWsdlPath("java\\test\\addressbook\\wsifservice") +
        "AddressBook.wsdl";
    static String server = TestUtilities.getSoapServer().toUpperCase();

    public InputPartsTest(String name) {
        super(name);
    }

    public static void main(String[] args)
    {
        TestUtilities.startListeners(
            TestUtilities.ADDRESSBOOK_LISTENER
                | TestUtilities.NATIVEJMS_LISTENER);

        junit.textui.TestRunner.run(suite());
        TestUtilities.stopListeners();
    }

    public static Test suite() {
        return new TestSuite(InputPartsTest.class);
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
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
//TODO the SOAP service doesn't work with AXIS nulls
//    public void testAxis() {
//        doit(server+"Port", "axis");
//    }
//    public void testAxisJms() {
//        doit("SOAPJMSPort", "axis");
//    }
    public void testNativeJms() {
        doit("NativeJmsPort", "" ); 
    }

    private void doit(String portName, String protocol) {
        if (portName.toUpperCase().indexOf("JMS") != -1 && !TestUtilities.areWeTesting("jms"))
            return;

        TestUtilities.setProviderForProtocol( protocol );

        System.out.println(
           "\n=== " + this.getClass().getName() + " " + portName + " " + protocol );

        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
                WSIFService service = factory.getService(WSDL_LOCATION,
                null, // serviceNS 
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

            WSIFPort port = service.getPort( portName );

            Address addr1 = new Address (1, "University Drive",
                                      "West Lafayette", "IN", 47907,
                                      new Phone (765, "494", "4900"));
            Address addr2 = new Address (0, "Somewhere Else",
                                      "No Where", "NO", 71983,
                                      new Phone (600, "391", "5682"));
            Address addr3 = new Address( 0,"","","",0, new Phone() );

            addName( port, "ant", addr1 );
            Address addressFound = getAddress( port, "ant" );
            assertTrue( "test 1 addresses are not equal!!", addressFound.equals( addr1 ) );

            addName( port, null, addr2 );
            addressFound = getAddress( port, null );
            assertTrue( "test 2 addresses are not equal!!", addressFound.equals( addr2 ) );

            addName( port, "petra", null );
            addressFound = getAddress( port, "petra" );
//TODO Address gets NPE on null address compare 
//          assertTrue( "test 3 addresses are not equal!!", addressFound.equals( addr3 ) );

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue( "got exception: " + e, false);
        } finally {
            if (protocol.equals("axis")) {
			   WSIFPluggableProviders.overrideDefaultProvider(
                    "http://schemas.xmlsoap.org/wsdl/soap/",
                    null);
            }
        }

    }

    private void addName(WSIFPort port, String name, Address addr) 
       throws WSIFException {
       	
       WSIFOperation operation = 
          port.createOperation("addEntry", "AddEntryWholeNameRequest", null); 
       WSIFMessage inputMessage = operation.createInputMessage();
       WSIFMessage outputMessage = operation.createOutputMessage();
       WSIFMessage faultMessage = operation.createFaultMessage();

       if ( name != null ) {
          inputMessage.setObjectPart( "name", name );
       }
       if ( addr != null ) {
          inputMessage.setObjectPart( "address", addr );
       }
       inputMessage.setObjectPart( "extra", "junk" );

       boolean ok = operation.executeRequestResponseOperation(
          inputMessage,
          outputMessage,
          faultMessage);

       assertTrue( "addEntry operation returned false!!", ok );
       
    }
    
    private Address getAddress(WSIFPort port, String name) 
       throws WSIFException {

       WSIFOperation operation = port.createOperation("getAddressFromName");
       WSIFMessage inputMessage = operation.createInputMessage();
       WSIFMessage outputMessage = operation.createOutputMessage();
       WSIFMessage faultMessage = operation.createFaultMessage();

       if ( name != null ) {
          inputMessage.setObjectPart( "name", name );
       }
       inputMessage.setObjectPart( "extra", "junk" );

       boolean ok = operation.executeRequestResponseOperation(
          inputMessage,
          outputMessage,
          faultMessage);

       assertTrue( "getAddressFromName operation returned false!!", ok );
       
       return (Address) outputMessage.getObjectPart( "address" ); 

    }
    
}

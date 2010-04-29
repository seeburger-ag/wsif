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

package serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.providers.WSIFDynamicTypeMapping;
import org.apache.wsif.schema.Parser;
import org.apache.wsif.util.WSIFUtils;

import addressbook.wsiftypes.Address;
import addressbook.wsiftypes.Phone;

import util.TestUtilities;

/**
 * Test the serializing and deserializing of WSIF objects
 * @author Owen Burroughs <owenb@apache.org>
 */
public class SerializationTest extends TestCase {
    static String server = TestUtilities.getSoapServer().toUpperCase();
    
    public SerializationTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(SerializationTest.class);
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
    }

    public void testWSIFDynamicTypeMapping() {
        try {
            String dummyNS = "http://this.is.a.test/";

            // java.lang.String
            QName qn1 = new QName(dummyNS, "string");
            doItTM(qn1, java.lang.String.class);

            // int
            QName qn2 = new QName(dummyNS, "int");
            doItTM(qn2, int.class);

            // float
            QName qn3 = new QName(dummyNS, "float");
            doItTM(qn3, float.class);

            // double
            QName qn4 = new QName(dummyNS, "double");
            doItTM(qn4, double.class);

            // long
            QName qn5 = new QName(dummyNS, "long");
            doItTM(qn5, long.class);

            // short
            QName qn6 = new QName(dummyNS, "short");
            doItTM(qn6, short.class);

            // byte
            QName qn7 = new QName(dummyNS, "byte");
            doItTM(qn7, byte.class);

            // void
            QName qn8 = new QName(dummyNS, "void");
            doItTM(qn8, void.class);

        } catch (Exception e) {
            System.out.println(
                "An error occured when running testWSIFDynamicTypeMapping "
                    + e);
            assertTrue(false);
        }
    }

    public void testWSIFPort_Java() {
        try {
            // TEST JAVA
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory.getService(
                    TestUtilities.getWsdlPath("java\\test\\shop")
                        + "ShoppingCartAll.wsdl",
                    "http://www.shoppingcart.com/definitions/ShoppingCartInterface",
                    "ShoppingCart_JavaService",
                    "http://www.shoppingcart.com/definitions/ShoppingCartInterface",
                    "ShoppingCart_JavaPortType");
            WSIFPort port = service.getPort();
            //WSIFOperation op = port.createOperation("addItemOperation");            
            doItPort(port, "WSIFPort_Java");          
        } catch (Exception e) {
            System.out.println(
                "\nAn error occured when running testWSIFPort_Java " + e);               
            assertTrue(false);
        }
    }

    public void testWSIFPort_EJB() {
    	if (!TestUtilities.areWeTesting("ejb")) return;
        try {
            // TEST EJB
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory.getService(
                    TestUtilities.getWsdlPath("java\\test\\shop")
                        + "ShoppingCartAll.wsdl",
                    "http://www.shoppingcart.com/definitions/ShoppingCartInterface",
                    "ShoppingCart_EJBService",
                    "http://www.shoppingcart.com/definitions/ShoppingCartInterface",
                    "ShoppingCart_EJBPortType");
            WSIFPort port = service.getPort();
            //WSIFOperation op = port.createOperation("addItemOperation");
            doItPort(port, "WSIFPort_EJB");          
        } catch (Exception e) {
            System.out.println(
                "\nAn error occured when running testWSIFPort_EJB " + e);
            assertTrue(false);
        }
    }

    public void testWSIFPort_ApacheSOAP() {
        try {
            // TEST APACHE SOAP
            TestUtilities.setProviderForProtocol("soap");
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory.getService(
                    TestUtilities.getWsdlPath(
                        "java\\test\\addressbook\\wsifservice")
                        + "AddressBook.wsdl",
                    null,
                    null,
                    "http://wsifservice.addressbook/",
                    "AddressBook");
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
            WSIFPort port = service.getPort(server+"Port");
            WSIFOperation op = port.createOperation("addEntry", "AddEntryWholeNameRequest", null);            
            doItPort(port, "WSIFPort_ApacheSOAP");
            WSIFOperation op2 = port.createOperation("addEntry", "AddEntryWholeNameRequest", null);
            WSIFMessage inputMessage = op2.createInputMessage();
            WSIFMessage outputMessage = op2.createOutputMessage();
            WSIFMessage faultMessage = op2.createFaultMessage();

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
            op2.executeInputOnlyOperation(inputMessage);              
        } catch (Exception e) {
            System.out.println(
                "\nAn error occured when running testWSIFPort_ApacheSOAP " + e);
            assertTrue(false);
        }
    }

    public void testWSIFPort_ApacheAxis() {
        try {
            // TEST APACHE AXIS
            TestUtilities.setProviderForProtocol("axis");
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory.getService(
                    TestUtilities.getWsdlPath(
                        "java\\test\\addressbook\\wsifservice")
                        + "AddressBook.wsdl",
                    null,
                    null,
                    "http://wsifservice.addressbook/",
                    "AddressBook");
            WSIFPort port = service.getPort(server+"Port");
            WSIFOperation op = port.createOperation("addEntry", "AddEntryWholeNameRequest", null);            
            doItPort(port, "WSIFPort_ApacheAxis");             
        } catch (Exception e) {
            System.out.println(
                "\nAn error occured when running testWSIFPort_ApacheAxis " + e);
            assertTrue(false);
        }
    }

    public void testWSIFPort_JMS() {
    	if (!TestUtilities.areWeTesting("jms")) return;
        try {
            // TEST APACHE AXIS
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory.getService(
                    TestUtilities.getWsdlPath(
                        "java\\test\\addressbook\\wsifservice")
                        + "AddressBook.wsdl",
                    null,
                    null,
                    "http://wsifservice.addressbook/",
                    "AddressBook");
            WSIFPort port = service.getPort("NativeJmsPort");
            WSIFOperation op = port.createOperation("addEntry", "AddEntryWholeNameRequest", null);           
            doItPort(port, "WSIFPort_JMS"); 
        } catch (Exception e) {
            System.out.println(
                "\nAn error occured when running testWSIFPort_JMS " + e);
            assertTrue(false);
        }
    }

    public void testWSIFMessage() {
        try {
            // TEST JAVA
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory.getService(
                    TestUtilities.getWsdlPath("java\\test\\shop")
                        + "ShoppingCartAll.wsdl",
                    "http://www.shoppingcart.com/definitions/ShoppingCartInterface",
                    "ShoppingCart_JavaService",
                    "http://www.shoppingcart.com/definitions/ShoppingCartInterface",
                    "ShoppingCart_JavaPortType");
            WSIFPort port = service.getPort();
            WSIFOperation op = port.createOperation("addItemOperation");
            WSIFMessage message = op.createInputMessage();
            message.setObjectPart("testPart1", "test1");            
//            message.setObjectPart("testPart2", new StringTokenizer("test2"));            
            doItMessage(message);          
        } catch (Exception e) {
            System.out.println(
                "\nAn error occured when running testWSIFPort_Java " + e);               
            assertTrue(false);
        }
    }	

    public void testSchemaList() {
        try {
            List list = new ArrayList();
            String wsdl = TestUtilities.getWsdlPath("java\\test\\schema") + "SchemaTest1.wsdl";
            Definition def = WSIFUtils.readWSDL(null, wsdl);
        	Parser.getAllSchemaTypes(def, list, null);   
            doItSchemaList(list);          
        } catch (Exception e) {
            System.out.println(
                "\nAn error occured when running testWSIFPort_Java " + e);               
            assertTrue(false);
        }
    }

    private WSIFPort doItPort(WSIFPort port, String portType) {

        try {
            WSIFPort p2 = (WSIFPort) writeItReadIt(port);
            assertNotNull(p2);
            System.out.println("\n" + portType + " was serialized and deserialized without problem");
            return p2;
        } catch (Exception e) {
            System.out.println("\nError testing port: " + portType + " :- " + e);
            System.out.println("The full stack trace is:");
            e.printStackTrace(); 
            assertTrue(false);
            return null;
        }
    }

    private void doItTM(QName qn, Class cls) {
        WSIFDynamicTypeMapping tm = new WSIFDynamicTypeMapping(qn, cls);

        try {
            WSIFDynamicTypeMapping tma =
                (WSIFDynamicTypeMapping) writeItReadIt(tm);
            assertNotNull(tma);
            assertEquals(
                tm.getJavaType().getName(),
                tma.getJavaType().getName());
            assertEquals(tm.getXmlType(), tma.getXmlType());
        } catch (Exception e) {
            System.out.println(
                "Error testing type mapping with for QName: "
                    + qn
                    + "and Java type: "
                    + cls
                    + " :- "
                    + e);
            assertTrue(false);
        }
    }

    private void doItMessage(WSIFMessage message) {
        try {
            WSIFMessage m2 = (WSIFMessage) writeItReadIt(message);
            assertNotNull(m2);
            System.out.println("\n WSIFMessage was serialized and deserialized without problem");
        } catch (Exception e) {
            System.out.println("\nError testing message:- " + e);
            System.out.println("The full stack trace is:");
            e.printStackTrace(); 
            assertTrue(false);
        }
    }

    private void doItSchemaList(List list) {
        try {
            List l2 = (List) writeItReadIt(list);
            assertNotNull(l2);
            System.out.println("\n Schema List was serialized and deserialized without problem");
        } catch (Exception e) {
            System.out.println("\nError testing schema list:- " + e);
            System.out.println("The full stack trace is:");
            e.printStackTrace(); 
            assertTrue(false);
        }
    }

    private Object writeItReadIt(Object o) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(b);
        out.writeObject(o);
        out.flush();
        out.close();
        byte[] data = b.toByteArray();
        ObjectInputStream in =
            new ObjectInputStream(new ByteArrayInputStream(data));
        Object o2 = in.readObject();
        in.close();
        return o2;
    }
}

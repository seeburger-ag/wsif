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

package jndi;

import java.util.Iterator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.wsif.WSIFService;
import org.apache.wsif.naming.WSIFServiceRef;
import org.apache.wsif.naming.WSIFServiceStubRef;
import addressbook.wsifservice.AddressBook;
import addressbook.wsiftypes.Address;
import addressbook.wsiftypes.Phone;
import util.AddressUtility;
import util.TestUtilities;

/**
 * Junit test to test using JNDI to retrieve a service.
 * @author Owen Burroughs <owenb@apache.org>
 */
public class JNDIAddressBookTest extends TestCase {
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

    public JNDIAddressBookTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestUtilities.startListeners(
            TestUtilities.ADDRESSBOOK_LISTENER);
        junit.textui.TestRunner.run(suite());
        TestUtilities.stopListeners();
    }

    public static Test suite() {
        return new TestSuite(JNDIAddressBookTest.class);
    }

    public void setUp() {
        if (!TestUtilities.areWeTesting("jndi"))
            return;
        TestUtilities.setUpExtensionsAndProviders();
        try {
            Context startingContext = new InitialContext();
            WSIFJndiHelper.setInitialContext(startingContext);
            WSIFJndiHelper.bindService(
                new WSIFServiceRef(
                    TestUtilities.getWsdlPath("java\\test\\jndi")
                        + "JNDIAddressBook.wsdl",
                    "http://wsifservice.addressbook/",
                    "AddressBookService",
                    "http://wsifservice.addressbook/",
                    "AddressBook"),
                "comp/env/wsif/addressservice");
            System.out.println("Binding of service using JNDI successful");
            WSIFJndiHelper.bindStub(
                new WSIFServiceStubRef(
                    TestUtilities.getWsdlPath("java\\test\\jndi")
                        + "JNDIAddressBook.wsdl",
                    "http://wsifservice.addressbook/",
                    "AddressBookService",
                    "http://wsifservice.addressbook/",
                    "AddressBook",
                    "SOAPPort",
                    "addressbook.wsifservice.AddressBook"),
                "comp/env/wsif/addressservicestub");
            System.out.println("Binding of service stub using JNDI successful");
        } catch (NameAlreadyBoundException ignore) {
        } catch (Exception e) {
            System.out.println("Error binding server using JNDI: " + e);
        }
    }

    public void testAllPorts() {
        doit();
    }
    public void testAxisStub() {
        doitStub(server+"Port");
    }

    private void doit() {
        if (!TestUtilities.areWeTesting("jndi"))
            return;
        String portName = "";
        try {
            InitialContext ic = new InitialContext();

            // Lookup jndi service name      	
            WSIFService service = (WSIFService) ic.lookup("comp/env/wsif/addressservice");

            Iterator it = service.getAvailablePortNames();
            {
                System.out.println("What ports does this service have?");
                while (it.hasNext()) {
                    String p = (String) it.next();
                    portName = p;
                    System.out.print("Port found named " + p);
                    if (p.toUpperCase().indexOf("JMS") >= 0
                        && !TestUtilities.areWeTesting("jms")) {
                        System.out.println(
                            " - Port is a JMS port and I don't like JMS so I refuse to use it!");
                    } else {
                        if (("SOAPPort".equals(p) || "AXISPort".equals(p))
                            && !p.equals(server + "Port"))
                        {
                            System.out.println(
                                "- not configured to use " + server + " port ");
                            continue;
                        }
                        
                        System.out.println(" - I like this port, I think I'll use it!");
                        AddressBook abStub = (AddressBook) service.getStub(p, AddressBook.class);

                        abStub.addEntry(name1, addr1);
                        abStub.addEntry(firstName2, lastName2, addr2);

                        Address resp1 = abStub.getAddressFromName(name1);
                        assertTrue(new AddressUtility(addr1).equals(resp1));

                        Address resp2 = abStub.getAddressFromName(firstName2 + " " + lastName2);
                        assertTrue(new AddressUtility(addr2).equals(resp2));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(
                "JNDIAddressBookTest(" + portName + ") caught exception " + e);
            e.printStackTrace();
            assertTrue(false);
        }
    }

    private void doitStub(String portName) {
        if (!TestUtilities.areWeTesting("jndi"))
            return;
        try {
            InitialContext ic = new InitialContext();

            AddressBook abStub =
                (AddressBook) ic.lookup("comp/env/wsif/addressservicestub");

            abStub.addEntry(name1, addr1);
            abStub.addEntry(firstName2, lastName2, addr2);

            Address resp1 = abStub.getAddressFromName(name1);
            assertTrue(new AddressUtility(addr1).equals(resp1));

            Address resp2 = abStub.getAddressFromName(firstName2 + " " + lastName2);
            assertTrue(new AddressUtility(addr2).equals(resp2));
        } catch (Exception e) {
            System.err.println(
                "JNDIAddressBookTest(" + portName + ") caught exception " + e);
            e.printStackTrace();
            assertTrue(false);
        }
    }
}

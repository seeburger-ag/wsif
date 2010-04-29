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

package inout;

import java.text.DateFormat;
import java.util.Date;

import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.base.WSIFServiceImpl;
import org.apache.wsif.providers.soap.apacheaxis.WSIFDynamicProvider_ApacheAxis;
import org.apache.wsif.providers.soap.apachesoap.WSIFDynamicProvider_ApacheSOAP;
import org.apache.wsif.util.WSIFPluggableProviders;

import addressbook.wsiftypes.Address;
import addressbook.wsiftypes.Phone;
import inout.wsifservice.Inout;
import inout.wsiftypes.Mutablestring;
import util.AddressUtility;
import util.TestUtilities;

/**
 * Junit test to test out the Inout test
 * @author Mark Whitlock
 */
public class InoutTest extends TestCase {
    String wsdlLocation =
        TestUtilities.getWsdlPath("java\\test\\inout\\wsifservice") + "Inout.wsdl";
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
    static String name2 = "Someone Else";
    static Address addr2 =
        new Address(
            0,
            "Somewhere Else",
            "No Where",
            "NO",
            71983,
            new Phone(600, "391", "5682"));

    static Address nulladdr = new Address(0, "", "", "", 0, new Phone(0, "", ""));

    private final static String ADDRESS = "address";
    private final static String NULLNAME = "nullname";
    private final static String ADD = "add";
    private final static String DATE = "date";
    private final static String SUPC = "supc";
    private final static String GENC = "genericc";                                      
    private final static String WHOAMI = "whoami";
    private final static String INOUT = "inout";

    private final static String RPC = "rpc";
    private final static String DOC = "doc";

    private final static String SOAP = "soap";
    private final static String AXIS = "axis";
    private final static String JAVA = "java";
    private final static String NJMS = "nativeJMS";                                  

    public InoutTest(String name) {
        super(name);
    }

    public static void main(String[] args)
    {
        TestUtilities.startListeners(
            TestUtilities.INOUT_LISTENER | TestUtilities.NATIVEJMS_LISTENER);
        junit.textui.TestRunner.run(suite());
        TestUtilities.stopListeners();
    }

    public static Test suite() {
        return new TestSuite(InoutTest.class);
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
    }

    public void testAxis() {
        doit(server+"Port", AXIS, RPC, ADDRESS);
    }
    public void testSoap() {
        doit(server+"Port", SOAP, RPC, ADDRESS);
    }
    public void testJava() {
        doit("JavaPort", JAVA, RPC, ADDRESS);
    }
    public void testSoapJms() {
        doit("SOAPJMSPort", SOAP, RPC, ADDRESS);
    }
    public void testAxisJms() {
        doit("SOAPJMSPort", AXIS, RPC, ADDRESS);
    }
    public void testNativeJms() { 
    	doit("NativeJMSPort" ,NJMS, RPC, ADDRESS);
    } 
    //public void testAxisDoc       () { doit("SOAPDocPort"   ,AXIS,DOC, ADDRESS ); } FAILS
    //public void testSoapDoc       () { doit("SOAPDocPort"   ,SOAP,DOC, ADDRESS ); } FAILS
    //public void testSoapJmsDoc    () { doit("SOAPJMSDocPort",SOAP,DOC, ADDRESS ); } FAILS
    //public void testAxisJmsDoc    () { doit("SOAPJMSDocPort",AXIS,DOC, ADDRESS ); } FAILS
    //public void testNativeJmsDoc  () { doit("NativeJMSPort" ,NJMS,DOC, ADDRESS ); } FAILS

    //public void testNNAxis        () { doit(server+"Port"      ,AXIS,RPC, NULLNAME); } FAILS
    //public void testNNSoap        () { doit(server+"Port"      ,SOAP,RPC, NULLNAME); } FAILS
    public void testNNJava() {
        doit("JavaPort", JAVA, RPC, NULLNAME);
    }
    //public void testNNSoapJms     () { doit("SOAPJMSPort"   ,SOAP,RPC, NULLNAME); } FAILS
    //public void testNNAxisJms     () { doit("SOAPJMSPort"   ,AXIS,RPC, NULLNAME); } FAILS
    //public void testNNNativeJms   () { doit("NativeJMSPort" ,NJMS,RPC, NULLNAME); } 

    //public void testAddAxis       () { doit(server+"Port"      ,AXIS,RPC, ADD     ); } FAILS
    public void testAddSoap() {
        doit(server+"Port", SOAP, RPC, ADD);
    }
    public void testAddJava() {
        doit("JavaPort", JAVA, RPC, ADD);
    }
    public void testAddSoapJms() {
        doit("SOAPJMSPort", SOAP, RPC, ADD);
    }
    //public void testAddAxisJms    () { doit("SOAPJMSPort"   ,AXIS,RPC, ADD     ); } FAILS
    public void testAddNativeJms() { 
    	doit("NativeJMSPort" , NJMS, RPC, ADD ); 
    } 

    //public void testDateAxis      () { doit(server+"Port"      ,AXIS,RPC, DATE    ); } FAILS
    public void testDateSoap() {
        doit(server+"Port", SOAP, RPC, DATE);
    }
    public void testDateJava() {
        doit("JavaPort", JAVA, RPC, DATE);
    }
    public void testDateSoapJms() {
        doit("SOAPJMSPort", SOAP, RPC, DATE);
    }
    //public void testDateAxisJms   () { doit("SOAPJMSPort"   ,AXIS,RPC, DATE    ); } FAILS
    public void testDateNativeJms() { 
    	doit("NativeJMSPort" , NJMS, RPC, DATE ); 
    } 

    public void testSupCAxis() {
        doit(server+"Port", AXIS, RPC, SUPC);
    }
    public void testSupCSoap() {
        doit(server+"Port", SOAP, RPC, SUPC);
    }
    public void testSupCJava() {
        doit("JavaPort", JAVA, RPC, SUPC);
    }
    public void testSupCSoapJms() {
        doit("SOAPJMSPort", SOAP, RPC, SUPC);
    }
    public void testSupCAxisJms() {
        doit("SOAPJMSPort", AXIS, RPC, SUPC);
    }
    public void testSupCNativeJms() {
    	doit("NativeJMSPort" , NJMS, RPC, SUPC );
    } 
    public void testGenCAxis() {
        doit(server+"Port", AXIS, RPC, GENC);
    }
    public void testGenCSoap() {
        doit(server+"Port", SOAP, RPC, GENC);
    }
    public void testGenCJava() {
        doit("JavaPort", JAVA, RPC, GENC);
    }
    public void testGenCSoapJms() {
        doit("SOAPJMSPort", SOAP, RPC, GENC);
    }
    public void testGenCAxisJms() {
        doit("SOAPJMSPort", AXIS, RPC, GENC);
    }
    public void testGenCNativeJms() {
    	doit("NativeJMSPort" , NJMS, RPC, GENC );
     } 

    //public void testWhoAxis       () { doit(server+"Port"      ,AXIS,RPC, WHOAMI  ); } FAILS
    public void testWhoSoap() {
        doit(server+"Port", SOAP, RPC, WHOAMI);
    }
    public void testWhoJava() {
        doit("JavaPort", JAVA, RPC, WHOAMI);
    }
    public void testWhoSoapJms() {
        doit("SOAPJMSPort", SOAP, RPC, WHOAMI);
    }
    //public void testWhoAxisJms    () { doit("SOAPJMSPort"   ,AXIS,RPC, WHOAMI  ); } FAILS
    public void testWhoNativeJms() {
    	doit("NativeJMSPort" , NJMS, RPC, WHOAMI );
     } 

    // The inout test will never work for any form of soap since soap does not
    // support inout or multiple output parameters.
    public void testInoJava() {
        doit("JavaPort", JAVA, RPC, INOUT);
    }

    private void doit(String portName, String protocol, String style, String cmd) {
        if (portName.toUpperCase().indexOf("JMS") != -1
            && !TestUtilities.areWeTesting("jms"))
            return;

        if (protocol.equals(SOAP)) {
            WSIFDynamicProvider_ApacheSOAP provider = new WSIFDynamicProvider_ApacheSOAP();

            if (style.equals(DOC))
                provider.setPartSerializerName("services.inout.InoutLiteralSerializer");

            WSIFPluggableProviders.overrideDefaultProvider(
                "http://schemas.xmlsoap.org/wsdl/soap/",
                provider);
        }
        if (protocol.equals(AXIS)) {
            WSIFDynamicProvider_ApacheAxis provider = new WSIFDynamicProvider_ApacheAxis();

            WSIFPluggableProviders.overrideDefaultProvider(
                "http://schemas.xmlsoap.org/wsdl/soap/",
                provider);
        }

        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory.getService(
                    wsdlLocation,
                    null,
                    null,
                    "http://wsifservice.inout/",
                    "Inout");

            service.mapType(
                new QName("http://wsiftypes.inout/", "arrayofint"),
                Class.forName("[I"));

            Inout stub = (Inout) service.getStub(portName, Inout.class);

            if (cmd.equals(ADDRESS))
                address(stub);
            else if (cmd.equals(NULLNAME))
                nullname(stub);
            else if (cmd.equals(ADD))
                add(stub);
            else if (cmd.equals(DATE))
                date(stub);
            else if (cmd.equals(SUPC))
                supc(service, portName);
            else if (cmd.equals(GENC)) 
                genericc(service, portName);
            else if (cmd.equals(WHOAMI))
                whoami(stub);
            else if (cmd.equals(INOUT))
                inout(stub);
            else
                assertTrue(false);
        } catch (Exception e) {
            System.err.println("InoutTest(" + portName + ") caught exception " + e);
            e.printStackTrace();
            assertTrue(false);
        } finally {
            if (protocol.equals(SOAP) || protocol.equals(AXIS)) {
                WSIFPluggableProviders.overrideDefaultProvider(
                    "http://schemas.xmlsoap.org/wsdl/soap/",
                    null);
            }
        }

    }

    private void address(Inout stub) throws Exception {
        stub.addEntry(name1.toString(), addr1);

        Mutablestring msName1 = new Mutablestring(name1);
        Address resp1 = stub.getAddressFromName(msName1);
        assertTrue(new AddressUtility(resp1).equals(addr1));

        Mutablestring msName = new Mutablestring("Pur*");
        resp1 = stub.getAddressFromName(msName);
        assertTrue(new AddressUtility(resp1).equals(addr1));
    }

    private void nullname(Inout stub) throws Exception {
        stub.addEntry(null, null);

        Address resp1 = stub.getAddressFromName((String) null);
        assertTrue(resp1 == null);

        Address resp2 = stub.getAddressFromName((Mutablestring) null);
        assertTrue(resp2 == null);

        // This next bit of code works with soap2.2 but not Axis. I've
        // commented it out because getAddressFromName((String)null) doesn't
        // work with either so I can't run this test with soap or axis.
        //Address nullAddr = new Address(0,"","","",0,new Phone(0,null,null));
        //Address resp = stub.getAddressFromName (new Mutablestring(null));
        //assertTrue(resp==null || TestUtilities.AddressEquals(resp,nullAddr));
    }

    private void add(Inout stub) throws Exception {
        int[] nums = new int[] { 1, 2, 3, 4, 5 };
        int total = stub.addNumbers(nums);

        String warmFuzzy = new String();
        int expected = 0;
        for (int i = 0; i < nums.length; i++) {
            expected += nums[i];
            if (i != 0)
                warmFuzzy += "+ ";
            warmFuzzy += nums[i] + " ";
        }
        warmFuzzy += "= " + total + " (expected " + expected + ")";
        System.out.println(warmFuzzy);
        assertTrue(total == expected);
    }

    private void date(Inout stub) throws Exception {
        Date first = new Date();
        Thread.sleep(1000);
        Date remote = stub.getDate();
        Thread.sleep(1000);
        Date later = new Date();

        DateFormat df = DateFormat.getDateTimeInstance();
        System.out.println(
            "Remote date is "
                + df.format(remote)
                + " First date is "
                + df.format(first)
                + " Later date is "
                + df.format(later));

        assertTrue(first.before(remote) || first.equals(remote));
        assertTrue(later.after(remote) || later.equals(remote));
    }

    private interface SubInout extends Inout {
        public void dubiousMethod() throws WSIFException;
    }

    private void supc(WSIFService service, String portName) throws Exception {
        SubInout stub = (SubInout) service.getStub(portName, SubInout.class);

        stub.addEntry(name1, addr1);
        Mutablestring msName1 = new Mutablestring(name1);
        Address resp1 = stub.getAddressFromName(msName1);
        assertTrue(new AddressUtility(addr1).equals(resp1));

        boolean caught = false;
        try {
            stub.dubiousMethod();
        } catch (WSIFException e) {
            caught = true;
        }
        assertTrue(caught);

        stub.addEntry(name2, new SubAddress(addr2));
        Mutablestring msName2 = new Mutablestring(name2);
        Address resp2 = stub.getAddressFromName(msName2);
        assertTrue(new AddressUtility(addr2).equals(resp2));
    }

    private interface GenericInout {
        public void addEntry(String wholeName, Object address)
            throws java.rmi.RemoteException;
        public Address getAddressFromName(Mutablestring name)
            throws java.rmi.RemoteException;
    }

    private void genericc(WSIFService service, String portName)
        throws Exception {
        GenericInout stub =
            (GenericInout) service.getStub(portName, GenericInout.class);

        stub.addEntry(name1, addr1);
        Mutablestring msName1 = new Mutablestring(name1);
        Address resp1 = stub.getAddressFromName(msName1);
        assertTrue(new AddressUtility(addr1).equals(resp1));
    }

    /**
     * whoami tests out dynamic proxies by overloading is various
     * ways.
     */
    private void whoami(Inout stub) throws Exception {
        float f = 1;
        int i = 1;
        Address a = new Address();
        SubAddress suba = new SubAddress();
        String resp;

        resp = stub.whoami("Hello");
        System.out.println("resp was " + resp + " expected String");
        assertTrue("String".equals(resp));

        resp = stub.whoami(f);
        System.out.println("resp was " + resp + " expected float");
        assertTrue("float".equals(resp));

        resp = stub.whoami(i);
        System.out.println("resp was " + resp + " expected int");
        assertTrue("int".equals(resp));

        resp = stub.whoami(a);
        System.out.println("resp was " + resp + " expected Address");
        assertTrue("Address".equals(resp));

        resp = stub.whoami(suba);
        System.out.println("resp was " + resp + " expected Address");
        assertTrue("Address".equals(resp));
    }

    private void inout(Inout stub) throws Exception {
        stub.addEntry(name1.toString(), addr1);

        Mutablestring msName1 = new Mutablestring(name1);
        Address resp1 = new AddressUtility(nulladdr).copy();
        boolean success = stub.getAddressFromName(msName1, resp1);
        assertTrue(success && new AddressUtility(resp1).equals(addr1));
    }

}

class SubAddress extends Address {

   public SubAddress() {
      super();
   }

   public SubAddress(Address a) {
      super(
         a.getStreetNum(),
         a.getStreetName(),
         a.getCity(),
         a.getState(),
         a.getZip(),
         a.getPhoneNumber());
   }

}


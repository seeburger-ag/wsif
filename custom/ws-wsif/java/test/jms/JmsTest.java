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

package jms;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.util.Date;

import javax.jms.DeliveryMode;
import javax.xml.namespace.QName;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.util.WSIFProperties;
import util.AddressUtility;
import util.TestUtilities;

import addressbook.wsifservice.AddressBook;
import addressbook.wsiftypes.Address;
import addressbook.wsiftypes.Phone;

/**
 * Junit test to do various JMS tests
 * @author Mark Whitlock
 */
public class JmsTest extends TestCase {
    String wsdlLocation = TestUtilities.getWsdlPath("java\\test\\jms") + "jms.wsdl";
    static String server = TestUtilities.getSoapServer().toUpperCase();

    static String name = "Purdue Boilermaker";
    static Address addr =
        new Address(
            1,
            "University Drive",
            "West Lafayette",
            "IN",
            47907,
            new Phone(765, "494", "4900"));

    private final static String SOAP = "soap";
    private final static String AXIS = "axis";
    private final static String JAVA = "java";
	private final static String NJMS = "njms";
    private final static String REPLYTOQ = "AddressBookReplyTo";
    private final static int PERS = DeliveryMode.PERSISTENT;
    private final static int NPERS = DeliveryMode.NON_PERSISTENT;
    private final static String  UP       = "user properties";
    private final static String  APV      = "address property values";
    private final static String  BPV      = "binding property values";
    private final static String  ABPV     = "address and binding property values";
    private final static String  UPV      = "user properties values";
    private final static String  OPV      = "overiding property values";
    private final static String  TO       = "timeout";

    private interface AddressBookWithAttrs extends AddressBook {
        public void addEntry(String name, Address address) throws RemoteException;
        public void addEntry(String name, Address address, int deliveryMode)
            throws RemoteException;
        public void addEntry(
            String name,
            Address address,
            int priority,
            String replyTo,
            int deliveryMode,
            long timeToLive)
            throws RemoteException;
    
        public void addEntry(
            String name,
            Address address,
            int deliveryMode,
            boolean boolUp,
            byte byteUp,
            short shUp,
            int intUp,
            long loUp,
            float flUp,
            double doUp,
            String strUp,
            Object obj)
            throws RemoteException;
    
        public Address getAddressFromName(String name) throws RemoteException;
        public Address getAddressFromName(String name, int deliveryMode)
            throws RemoteException;
    }

    public JmsTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestUtilities.startListeners(
            TestUtilities.ADDRESSBOOK_LISTENER
                | TestUtilities.NATIVEJMS_LISTENER);

        junit.textui.TestRunner.run(suite());
        TestUtilities.stopListeners();
    }

    public static Test suite() {
        return new TestSuite(JmsTest.class);
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
    }

    /* *************** Start of SOAP tests ****************** */
    
    public void testSoapDefault() {
        doit("default", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapDefaultAlt() {
        doit("default-alt", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapBadQcfMixQ() {
        doit("bad-qcfmixq", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapBadMixQ() {
        doit("bad-mixq", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapBadMixIcf() {
        doit("bad-mixicf", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapBadQcf() {
        doit("bad-qcf", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapBadQ() {
        doit("bad-queue", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapBadAltQ() {
        doit("bad-altqueue", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapBadStyle() {
        doit("bad-style", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapBadIcf() {
        doit("bad-icf", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapBadJurl() {
        doit("bad-jurl", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapBadNoQ() {
        doit("bad-noq", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapBadNoStyle() {
        doit("bad-nostyle", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapBadNoQcf() {
        doit("bad-noqcf", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapBadNoIcfJurl() {
        doit("bad-noicfjurl", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapBadAltAndQ() {
        doit("bad-altandq", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapBadTopic() {
        doit("bad-topic", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapBadImplSpec() {
        doit("bad-implspec", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapBadMqAndJndi() {
        doit("bad-mqandjndi", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapBadNothing() {
        doit("bad-nothing", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapBadNoBinding() {
        doit("bad-nobinding", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapHttpNoAttrs() {
        doit(server+"-http-noattrs", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapNullReplyTo() {
        doit("default", SOAP, null, 3, NPERS, 0, null);
    }

    public void testSoapShortReplyTo() {
        doit("default", SOAP, "", 3, NPERS, 0, null);
    }

    public void testSoapBadReplyTo() {
        doit("baddefault", SOAP, "trash", 3, NPERS, 0, null);
    }
    
    public void testSoapZeroPriority() {
        doit("default", SOAP, REPLYTOQ, 0, NPERS, 0, null);
    }
    
    public void testSoapNegPriority() {
        doit("baddefault", SOAP, REPLYTOQ, -1, NPERS, 0, null);
    }
    
    public void testSoapBigPriority() {
        doit("baddefault", SOAP, REPLYTOQ, 57, NPERS, 0, null);
    }
    
    public void testSoapPersistent() {
        doit("default", SOAP, REPLYTOQ, 3, PERS, 0, null);
    }

    // This test makes an invalid assumption!!    
//    public void testSoapBadPers() {
//        doit("bad-persist", SOAP, REPLYTOQ, 3, PERS, 0, null);
//    }
    
    public void testSoapNonPers() {
        doit("nonpersist", SOAP, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testSoapUserProp() {
        doit("default", SOAP, REPLYTOQ, 3, NPERS, 0, UP);
    }
    
    public void testSoapAddrPrVals() {
        doit("addrpv", SOAP, REPLYTOQ, 3, PERS, 0, APV);
    }
    
    public void testSoapBindPrVals() {
        doit("bindpv", SOAP, REPLYTOQ, 3, PERS, 0, BPV);
    }
    
//    public void testSoapAdBiPrVals() {
//        doit("adbipv", SOAP, REPLYTOQ, 3, PERS, 0, ABPV);
//    }
//    public void testSoapOvAdBiPrVals() {
//        doit("ovpv", SOAP, REPLYTOQ, 3, PERS, 0, OPV);
//    }
//    public void testSoapUserPrVals() {
//        doit("userpv", SOAP, REPLYTOQ, 3, NPERS, 0, UPV);
//    }
//    public void testSoapBadPVNoName() {
//        doit("bad-pvnoname", SOAP, REPLYTOQ, 3, NPERS, 0, APV);
//    }

    public void testSoapTimeout() {
        doit("timeout", SOAP, REPLYTOQ, 3, NPERS, 0, TO);
    }

    /* ********** Start of AXIS tests ************ */
    
    public void testAxisDefault() {
        doit("default", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testAxisDefaultAlt() {
        doit("default-alt", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisBadQcfMixQ() {
        doit("bad-qcfmixq", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisBadMixQ() {
        doit("bad-mixq", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisBadMixIcf() {
        doit("bad-mixicf", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisBadQcf() {
        doit("bad-qcf", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisBadQ() {
        doit("bad-queue", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisBadAltQ() {
        doit("bad-altqueue", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisBadStyle() {
        doit("bad-style", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisBadIcf() {
        doit("bad-icf", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisBadJurl() {
        doit("bad-jurl", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisBadNoQ() {
        doit("bad-noq", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisBadNoStyle() {
        doit("bad-nostyle", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisBadNoQcf() {
        doit("bad-noqcf", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisBadNoIcfJurl() {
        doit("bad-noicfjurl", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisBadAltAndQ() {
        doit("bad-altandq", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisBadTopic() {
        doit("bad-topic", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisBadImplSpec() {
        doit("bad-implspec", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisBadMqAndJndi() {
        doit("bad-mqandjndi", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisBadNothing() {
        doit("bad-nothing", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisBadNoBinding() {
        doit("bad-nobinding", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisHttpNoAttrs() {
        doit(server+"-http-noattrs", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }
    
    public void testAxisNullReplyTo() {
        doit("default", AXIS, null, 3, NPERS, 0, null);
    }
    
    public void testAxisShortReplyTo() {
        doit("default", AXIS, "", 3, NPERS, 0, null);
    }
    
    public void testAxisBadReplyTo() {
        doit("baddefault", AXIS, "trash", 3, NPERS, 0, null);
    }
    
    public void testAxisZeroPriority() {
        doit("default", AXIS, REPLYTOQ, 0, NPERS, 0, null);
    }
    
    public void testAxisNegPriority() {
        doit("baddefault", AXIS, REPLYTOQ, -1, NPERS, 0, null);
    }
    
    public void testAxisBigPriority() {
        doit("baddefault", AXIS, REPLYTOQ, 57, NPERS, 0, null);
    }
    
    public void testAxisPersistent() {
        doit("default", AXIS, REPLYTOQ, 3, PERS, 0, null);
    }
    
    // This test makes an invalid assumption!!     
//    public void testAxisBadPers() {
//        doit("bad-persist", AXIS, REPLYTOQ, 3, PERS, 0, null);
//    }
    
    public void testAxisNonPers() {
        doit("nonpersist", AXIS, REPLYTOQ, 3, NPERS, 0, null);
    }

    public void testAxisUserProp() {
        doit("default", AXIS, REPLYTOQ, 3, NPERS, 0, UP);
    }

    public void testAxisAddrPrVals() {
        doit("addrpv", AXIS, REPLYTOQ, 3, PERS, 0, APV);
    }

    public void testAxisBindPrVals() {
        doit("bindpv", AXIS, REPLYTOQ, 3, PERS, 0, BPV);
    }

    public void testAxisTimeout() {
        doit("timeout", AXIS, REPLYTOQ, 3, NPERS, 0, TO);
    }

    /* ********** Start of Native JMS tests ************ */

	public void testNativeJMSDefault() {
		doit("NJdefault", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSDefaultAlt() {
		doit("NJdefault-alt", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSBadQcfMixQ() {
		doit("NJbad-qcfmixq", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSBadMixQ() {
		doit("NJbad-mixq", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSBadMixIcf() {
		doit("NJbad-mixicf", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSBadQcf() {
		doit("NJbad-qcf", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSBadQ() {
		doit("NJbad-queue", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSBadAltQ() {
		doit("NJbad-altqueue", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSBadStyle() {
		doit("NJbad-style", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSBadIcf() {
		doit("NJbad-icf", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSBadJurl() {
		doit("NJbad-jurl", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSBadNoQ() {
		doit("NJbad-noq", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSBadNoStyle() {
		doit("NJbad-nostyle", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSBadNoQcf() {
		doit("NJbad-noqcf", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSBadNoIcfJurl() {
		doit("NJbad-noicfjurl", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSBadAltAndQ() {
		doit("NJbad-altandq", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSBadTopic() {
		doit("NJbad-topic", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSBadImplSpec() {
		doit("NJbad-implspec", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSBadMqAndJndi() {
		doit("NJbad-mqandjndi", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSBadNothing() {
		doit("NJbad-nothing", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSBadNoBinding() {
		doit("NJbad-nobinding", NJMS, REPLYTOQ, 3, NPERS, 0, null);
	}
	public void testNativeJMSNullReplyTo() {
		doit("NJdefault", NJMS, null, 3, NPERS, 0, null);
	}
	public void testNativeJMSShortReplyTo() {
		doit("NJdefault", NJMS, "", 3, NPERS, 0, null);
	}
	public void testNativeJMSBadReplyTo() {
		doit("NJbaddefault", NJMS, "trash", 3, NPERS, 0, null);
	}
	public void testNativeJMSZeroPriority() {
		doit("NJdefault", NJMS, REPLYTOQ, 0, NPERS, 0, null);
	}
	public void testNativeJMSNegPriority() {
		doit("NJbaddefault", NJMS, REPLYTOQ, -1, NPERS, 0, null);
	}
	public void testNativeJMSBigPriority() {
		doit("NJbaddefault", NJMS, REPLYTOQ, 57, NPERS, 0, null);
	}
	public void testNativeJMSPersistent() {
		doit("NJdefault", NJMS, REPLYTOQ, 3, PERS, 0, null);
	}

    // This test makes an invalid assumption!! 	
//	public void testNativeJMSBadPers() {
//		doit("NJbad-persist", NJMS, REPLYTOQ, 3, PERS, 0, null);
//	}

//	public void testNativeJMSNonPers() { TODO - doesn't work!
//		doit("NJnonpersist", NJMS, REPLYTOQ, 3, NPERS, 0, null);
//	}
	public void testNativeJMSUserProp() {
		doit("NJdefault", NJMS, REPLYTOQ, 3, NPERS, 0, UP);
	}
	public void testNativeJMSAddrPrVals() {
		doit("NJaddrpv", NJMS, REPLYTOQ, 3, PERS, 0, APV);
	}
	public void testNativeJMSBindPrVals() {
		doit("NJbindpv", NJMS, REPLYTOQ, 3, PERS, 0, BPV);
	}

    public void testNativeJMSTimeout() {
        doit("NJtimeout", NJMS, REPLYTOQ, 3, NPERS, 0, TO);
    }

    public void testJavaNoAttrs() {
        doit("java-noattrs", JAVA, null, 3, NPERS, 0, null);
    }

    private void doit(
        String portName,
        String protocol,
        String replyTo,
        int priority,
        int deliveryMode,
        long timeToLive,
        String cmd) {
        	
        if (!TestUtilities.areWeTesting("jms"))
            return;

        TestUtilities.setProviderForProtocol( protocol );

        boolean ExceptionExpected = (portName.indexOf("bad") != -1);

        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory.getService(
                    wsdlLocation,
                    null,
                    null,
                    "http://wsifservice.addressbook/",
                    "AddressBook");

            service.mapType(
                new QName("http://wsiftypes.addressbook/", "obj"), 
			Object.class);

            AddressBookWithAttrs stub =
                (AddressBookWithAttrs) service.getStub(portName, AddressBookWithAttrs.class);

            if ("timeout".equals(cmd))
                doTimeout(stub, deliveryMode);
            else {
                if (cmd == null)
                    stub.addEntry(
                        name,
                        addr,
                        priority,
                        replyTo,
                        deliveryMode,
                        timeToLive);
                else
                    doProperties(stub, cmd, deliveryMode);
				
                Address resp = stub.getAddressFromName(name, deliveryMode);
                assertTrue(new AddressUtility(resp).equals(addr));
            }
            assertTrue(!ExceptionExpected);
        } catch (Exception e) {
            System.err.println("JmsTest(" + portName + ") caught exception " + e);
            if (!ExceptionExpected || !(e instanceof WSIFException))
                e.printStackTrace();
            assertTrue(ExceptionExpected && e instanceof WSIFException);
        } finally {
        	TestUtilities.resetDefaultProviders();
        }
    }

    private void doProperties(
        AddressBookWithAttrs stub,
        String cmd,
        int deliveryMode)
        throws Exception {

        if (UP.equals(cmd)) {
            byte b = 52;
            short s = 53;
            int i = 54;
            long l = 55;
            float f = 56.57F;
            double d = 58.59D;
            stub.addEntry(
                name,
                addr,
                deliveryMode,
                true,
                b,
                s,
                i,
                l,
                f,
                d,
                "60 through 67",
				new String("hello"));
        } else if (
            APV.equals(cmd)
                || BPV.equals(cmd)
                || ABPV.equals(cmd)
                || UPV.equals(cmd)) {
            stub.addEntry(name, addr, deliveryMode);
        } else
            assertTrue(false);
    }

    /**
     * This test only tests out synchronous timeouts. Asynchronous timeouts
     * are the responsibility of the application since it is the application
     * that does the listen or receive for the response message. The asynchronous
     * timeout in wsif.properties is also used by the correlation service,
     * but this is tested in a separate unit test.
     */
    private void doTimeout(
        AddressBookWithAttrs stub,
        int deliveryMode)
        throws Exception {

        boolean caught = false;
        Date first = new Date();
        try {
            stub.addEntry(name, addr, deliveryMode);
        } catch (WSIFException we) {
            System.out.println("Caught expected " + we);
            caught = true;
        }
        assertTrue(caught);
        Date second = new Date();

        DateFormat df = DateFormat.getDateTimeInstance();
        System.out.println("first date was " + df.format(first));
        System.out.println("second date was " + df.format(second));

        // getTime() returns milliseconds.
        long firstTime = first.getTime();
        long secondTime = second.getTime();
        System.out.println("first time was " + firstTime);
        System.out.println("second time was " + secondTime);

        long timeout = WSIFProperties.getSyncTimeout();
        assertTrue(firstTime<secondTime);
        assertTrue((secondTime-firstTime)>timeout);
        assertTrue((secondTime-firstTime)<(timeout*2));
    }

}
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

import java.text.DateFormat;
import java.util.Date;

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
import org.apache.wsif.util.WSIFProperties;
import util.AddressUtility;
import util.TestUtilities;

import addressbook.wsifservice.AddressBook;
import addressbook.wsiftypes.Address;
import addressbook.wsiftypes.Phone;
import async.AsyncResponseHandler;

/**
 * Junit test to test out the JMS sync and async timeouts.
 * This doesn't work so well yet and you have to run each test 
 * manually and check the timeout is correct. the only way to 
 * do this for async ops is to add a system.out.println to the
 * correlation service to show what timeout value its using.
 * 
 * @author <a href="mailto:ant.elder@uk.ibm.com">Ant Elder</a>
 */
public class JMSTimeoutTest extends TestCase {
	String wsdlLocation =
		TestUtilities.getWsdlPath("java\\test\\jms") + "Timeouts.wsdl";

	private static final long sps1 = 7000;
	private static final long spa1 = 11000;
	private static final long sps2 = 12000;
	private static final long spa2 = 14000;
	private static final long sps3 = 15000;
	private static final long spa3 = 18000;
	private static final long sps4 = 3000;  // wsif.properties
	private static final long spa4 = 10000; // wsif.properties
	private static final long sps5 = 25000;
	private static final long spa5 = 27000;
	private static final long sps6 = 15000;
	private static final long spa6 = 18000;

	public JMSTimeoutTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		//	    TestUtilities.startListeners();   <***** don't listen so it times out	
		junit.textui.TestRunner.run(suite());
		//      TestUtilities.stopListeners();	
	}

	public static Test suite() {
		return new TestSuite(JMSTimeoutTest.class);
	}

	public void setUp() {
		TestUtilities.setUpExtensionsAndProviders();
	}

	//   public void testDynamicAxisJms1() {
	//      doitDyn("SOAPJMSPort1", "soap");
	//   }
	//xxx   public void testDynamicAxisJms2() { 
	//      doitDyn("SOAPJMSPort2", "soap");
	//   }
	//   public void testDynamicAxisJms3() {
	//      doitDyn("SOAPJMSPort3", "soap");
	//   }
	//   public void testDynamicAxisJms4() {
	//      doitDyn("SOAPJMSPort4", "soap");
	//   }
	//   public void testDynamicAxisJms5() {
	//      doitDyn("SOAPJMSPort5", "soap");
	//   }
	//   public void testDynamicAxisJms6() {
	//      doitDyn("SOAPJMSPort6", "soap");
	//   }
	//   public void testDynamicAxisJms1() {
	//      testAsyncOPs("SOAPJMSPort1", "soap");
	//  }
	//   public void testDynamicAxisJms2() {   
	//      testAsyncOPs("SOAPJMSPort2", "soap");
	//   }
	//   public void testDynamicAxisJms3() {
	//      testAsyncOPs("SOAPJMSPort3", "soap");
	//   }
	//   public void testDynamicAxisJms4() {
	//      testAsyncOPs("SOAPJMSPort4", "soap");
	//   }
	//   public void testDynamicAxisJms5() {
	//      testAsyncOPs("SOAPJMSPort5", "soap");
	//   }
	//   public void testDynamicAxisJms6() {
	//      testAsyncOPs("SOAPJMSPort6", "soap");
	//   }

	//   public void testDynamicAxisJms1() {
	//     doitDyn("SOAPJMSPort1", "axis");
	//   }
	//   public void testDynamicAxisJms2() {  
	//      doitDyn("SOAPJMSPort2", "axis");
	//   }
	//   public void testDynamicAxisJms3() {
	//      doitDyn("SOAPJMSPort3", "axis");
	//   }
	//   public void testDynamicAxisJms4() {
	//      doitDyn("SOAPJMSPort4", "axis");
	//   }
	//   public void testDynamicAxisJms5() {
	//      doitDyn("SOAPJMSPort5", "axis");
	//   }
	//   public void testDynamicAxisJms6() {
	//      doitDyn("SOAPJMSPort6", "axis");
	//   }
	//   public void testDynamicAxisJms1() {
	//      testAsyncOPs("SOAPJMSPort1", "axis");
	//  }
	//   public void testDynamicAxisJms2() {   
	//      testAsyncOPs("SOAPJMSPort2", "axis");
	//   }
	//   public void testDynamicAxisJms3() {
	//      testAsyncOPs("SOAPJMSPort3", "axis");
	//   }
	//   public void testDynamicAxisJms4() {
	//      testAsyncOPs("SOAPJMSPort4", "axis");
	//   }
	//   public void testDynamicAxisJms5() {
	//      testAsyncOPs("SOAPJMSPort5", "axis");
	//   }
	   public void testDynamicAxisJms6() {
	      testAsyncOPs("SOAPJMSPort6", "axis");
	   }

	//     public void testDynamicNativeJms() {
	//        doitDyn("NativeJmsPort1", "" ); 
	//     }
	//     public void testDynamicNativeJms() {
	//        doitDyn("NativeJmsPort2", "" ); 
	//     }
	//     public void testDynamicNativeJms() {
	//        doitDyn("NativeJmsPort3", "" ); 
	//     }
	//     public void testDynamicNativeJms() {
	//        doitDyn("NativeJmsPort4", "" ); 
	//     }
	//     public void testDynamicNativeJms() {
	//        doitDyn("NativeJmsPort5", "" ); 
	//     }
	//     public void testDynamicNativeJms() {
	//        doitDyn("NativeJmsPort6", "" ); 
	//     }
	//public void testDynamicNativeJms() {
	//	testAsyncOPs("NativeJmsPort1", "");
	//}
	//     public void testDynamicNativeJms() {
	//        testAsyncOPs("NativeJmsPort2", "" ); 
	//     }
	//     public void testDynamicNativeJms() {
	//        testAsyncOPs("NativeJmsPort3", "" ); 
	//     }
	//     public void testDynamicNativeJms() {
	//        testAsyncOPs("NativeJmsPort4", "" ); 
	//    }
	//     public void testDynamicNativeJms() {
	//        testAsyncOPs("NativeJmsPort5", "" ); 
	//     }
	//     public void testDynamicNativeJms() {
	//        testAsyncOPs("NativeJmsPort6", "" ); 
	//     }

	private void doitDyn(String portName, String protocol) {
		if (portName.toUpperCase().indexOf("JMS") != -1
			&& !TestUtilities.areWeTesting("jms"))
			return;

		TestUtilities.setProviderForProtocol(protocol);

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
				new javax.xml.namespace.QName(
					"http://wsiftypes.addressbook/",
					"address"),
				Class.forName("addressbook.wsiftypes.Address"));

			service.mapType(
				new javax.xml.namespace.QName(
					"http://wsiftypes.addressbook/",
					"phone"),
				Class.forName("addressbook.wsiftypes.Phone"));

			WSIFPort port = service.getPort(portName);

			WSIFOperation operation =
				port.createOperation("getAddressFromName");

			// Create the messages 
			WSIFMessage inputMessage = operation.createInputMessage();
			WSIFMessage outputMessage = operation.createOutputMessage();
			WSIFMessage faultMessage = operation.createFaultMessage();

			// Set the name to find in the addressbook 
			String nameToLookup = "Chris P. Bacon";
			inputMessage.setObjectPart("name", nameToLookup);

			if ("SOAPJMSPort2".equals(portName)
				|| "NativeJmsPort2".equals(portName)) {
				inputMessage.setObjectPart("syncTimeout", "" + sps2);
			}
			if ("SOAPJMSPort3".equals(portName)
				|| "NativeJmsPort3".equals(portName)
				|| "SOAPJMSPort6".equals(portName)
				|| "NativeJmsPort6".equals(portName)) {
				WSIFMessage context = operation.getContext();
				context.setObjectPart(
					WSIFConstants.WSIF_PROP_SYNC_TIMEOUT,
					"" + sps3);
				operation.setContext(context);
			}

			// Execute the operation 
			Date first = new Date();
			boolean operationSucceeded =
				operation.executeRequestResponseOperation(
					inputMessage,
					outputMessage,
					faultMessage);

			if (operationSucceeded) {
				assertTrue("it didn't timeout!!", false);
			} else {
				Date second = new Date();

				DateFormat df = DateFormat.getDateTimeInstance();
				long diff = second.getTime() - first.getTime();
				System.out.println("diff=" + diff);
				//            assertTrue("lookup op failed!!", false);
			}

		} catch (Exception e) {
			e.printStackTrace();
			assertTrue("exception running tests-" + e.getMessage(), false);
		}

	}

	private void testAsyncOPs(String portName, String protocol) {
		if (portName.toUpperCase().indexOf("JMS") != -1
			&& !TestUtilities.areWeTesting("jms"))
			return;

		TestUtilities.setProviderForProtocol(protocol);

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
				new javax.xml.namespace.QName(
					"http://wsiftypes.addressbook/",
					"address"),
				Class.forName("addressbook.wsiftypes.Address"));

			service.mapType(
				new javax.xml.namespace.QName(
					"http://wsiftypes.addressbook/",
					"phone"),
				Class.forName("addressbook.wsiftypes.Phone"));

			WSIFPort port =
				(portName == null)
					? service.getPort()
					: service.getPort(portName);

			if (!port.supportsAsync()) {
				return;
			}

			WSIFOperation op = port.createOperation("getAddressFromName");

			AsyncResponseHandler abHandler = new AsyncResponseHandler(1);
			// 1 async call

			WSIFMessage inMsg = op.createInputMessage();
			inMsg.setObjectPart("name", "fred");

			WSIFMessage outmsg = op.createOutputMessage();
			WSIFMessage faultMsg = op.createFaultMessage();

			WSIFMessage context = op.getContext();
			context.setObjectPart(
				WSIFConstants.CONTEXT_JMS_PREFIX + "JMSReplyTo",
				TestUtilities.getWsifProperty("wsif.async.replytoq"));

			if ("SOAPJMSPort2".equals(portName)
				|| "NativeJmsPort2".equals(portName)) {
				inMsg.setObjectPart("asyncTimeout", "" + spa2);
			}
			if ("SOAPJMSPort3".equals(portName)
				|| "NativeJmsPort3".equals(portName)
				|| "SOAPJMSPort6".equals(portName)
				|| "NativeJmsPort6".equals(portName)) {
				context.setObjectPart(
					WSIFConstants.WSIF_PROP_ASYNC_TIMEOUT,
					"" + spa3);
			}

			op.setContext(context);

			WSIFCorrelationId id =
				op.executeRequestResponseAsync(inMsg, abHandler);
			assertTrue(
				"null correlation id returned from async request!",
				id != null);

			int i = 30;
			while (i-- > 0 && !abHandler.isDone()) {
				System.out.println("waiting for async responses - " + i);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue("exception making async request!!", false);
		}
	}
	/*
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
	*/

}

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

import java.util.Iterator;

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
import util.TestUtilities;

/**
 * Junit test to do various JMS tests
 * @author Mark Whitlock
 */
public class JmsFaultTest extends TestCase {
    String wsdlLocation = TestUtilities.getWsdlPath("java\\test\\jms") + "jmsfault.wsdl";

    private static final boolean SYNC = true;
    private static final boolean ASYNC = false;

    public JmsFaultTest(String name) {
        super(name);
    }

	public static void main(String[] args) {
	   TestUtilities.startListeners(TestUtilities.NATIVEJMS_LISTENER);	
	   junit.textui.TestRunner.run (suite());
	   TestUtilities.stopListeners();	
    }

    public static Test suite() {
        return new TestSuite(JmsFaultTest.class);
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
    }

    public void testNoFaultSync() {
        doit("throwSimple", 0, false, false, null, null, SYNC);
    }

    public void testSimpleSync() {
        doit(
            "throwSimple",
            1,
            true,
            false,
            new String[] { "faultText" },
            new Object[] { "A Simple Fault" }, SYNC);
    }

    public void testIntsSync() {
        doit(
            "throwSimple",
            2,
            true,
            false,
            new String[] { "faultInt1", "faultInt2", "faultInt3" },
            new Object[] { new Integer(1), new Integer(2), new Integer(3)}, SYNC);
    }

    public void testTwoIntsSync() {
        doit(
            "throwSimple",
            3,
            true,
            false,
            new String[] { "faultInt1", "faultInt2" },
            new Object[] { new Integer(1), new Integer(2) }, SYNC);
    }

    public void testEmptyJmsFaultSync() {
        doit(
            "throwSimple",
            2,
            true,
            false,
            new String[] { "faultInt1", "faultInt2", "faultInt3" },
            new Object[] { new Integer(1), new Integer(2), new Integer(3)}, SYNC);
    }

    public void testIndicIntSync() {
        doit(
            "throwIndicInt",
            4,
            true,
            false,
            new String[] { "faultText" },
            new Object[] { "A Simple Fault" }, SYNC);
    }

    public void testNoFaultAltSync() {
        doit("throwSimple", 5, false, false, null, null, SYNC);
    }

    public void testFaultIndicPropSync() {
        doit(
            "throwProperties",
            6,
            true,
            false,
            new String[] { "faultText", "faultIndic" },
            new Object[] { "A Fault Indicator", new Byte((byte) - 1)}, SYNC);
    }

    public void testJmsPropFaultSync() {
        doit(
            "throwProperties",
            7,
            true,
            false,
            new String[] { "faultText", "faultIndic", "faultProp" },
            new Object[] {
                "Another Property Fault",
                new Byte((byte) - 2),
                "Another JMS Property" }, SYNC);
    }

    public void testIndicOnlySync() {
        doit(
            "throwProperties",
            8,
            true,
            false,
            new String[] { "faultIndic" },
            new Object[] { new Byte((byte) - 3)},
            SYNC);
    }

    public void testPropOnlySync() {
        doit(
            "throwProperties",
            9,
            true,
            false,
            new String[] { "faultProp" },
            new Object[] { "Another JMS Property" },
            SYNC);
    }

    public void testNullFaultSync() {
        doit("throwProperties", 10, true, false, null, null, SYNC);
    }

    /* ***************** ASYNC ************************** */
    
    public void testNoFaultAsync() {
        doit("throwSimple", 0, false, false, null, null, ASYNC);
    }

    public void testSimpleAsync() {
        doit(
            "throwSimple",
            1,
            true,
            false,
            new String[] { "faultText" },
            new Object[] { "A Simple Fault" }, ASYNC);
    }

    public void testIntsAsync() {
        doit(
            "throwSimple",
            2,
            true,
            false,
            new String[] { "faultInt1", "faultInt2", "faultInt3" },
            new Object[] { new Integer(1), new Integer(2), new Integer(3)}, ASYNC);
    }

    public void testTwoIntsAsync() {
        doit(
            "throwSimple",
            3,
            true,
            false,
            new String[] { "faultInt1", "faultInt2" },
            new Object[] { new Integer(1), new Integer(2) }, ASYNC);
    }

    public void testEmptyJmsFaultAsync() {
        doit(
            "throwSimple",
            2,
            true,
            false,
            new String[] { "faultInt1", "faultInt2", "faultInt3" },
            new Object[] { new Integer(1), new Integer(2), new Integer(3)}, ASYNC);
    }

    public void testIndicIntAsync() {
        doit(
            "throwIndicInt",
            4,
            true,
            false,
            new String[] { "faultText" },
            new Object[] { "A Simple Fault" }, ASYNC);
    }

    public void testNoFaultAltAsync() {
        doit("throwSimple", 5, false, false, null, null, ASYNC);
    }

    public void testFaultIndicPropAsync() {
        doit(
            "throwProperties",
            6,
            true,
            false,
            new String[] { "faultText", "faultIndic" },
            new Object[] { "A Fault Indicator", new Byte((byte) - 1)}, ASYNC);
    }

    public void testJmsPropFaultAsync() {
        doit(
            "throwProperties",
            7,
            true,
            false,
            new String[] { "faultText", "faultIndic", "faultProp" },
            new Object[] {
                "Another Property Fault",
                new Byte((byte) - 2),
                "Another JMS Property" }, ASYNC);
    }

    public void testIndicOnlyAsync() {
        doit(
            "throwProperties",
            8,
            true,
            false,
            new String[] { "faultIndic" },
            new Object[] { new Byte((byte) - 3)},
            ASYNC);
    }

    public void testPropOnlyAsync() {
        doit(
            "throwProperties",
            9,
            true,
            false,
            new String[] { "faultProp" },
            new Object[] { "Another JMS Property" },
            ASYNC);
    }

    public void testNullFaultAsync() {
        doit("throwProperties", 10, true, false, null, null, ASYNC);
    }

    private void doit(
        String method,
        int choice,
        boolean faultExpected,
        boolean exceptionExpected,
        String[] names,
        Object[] parts,
        boolean blocks) {
        	
        if (!TestUtilities.areWeTesting("jms"))
            return;

        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory.getService(
                    wsdlLocation,
                    null,
                    null,
                    "http://jms/",
                    "JmsFault");

            WSIFPort port = service.getPort("default");
            WSIFOperation operation = port.createOperation(method);

            WSIFMessage inputMessage = operation.createInputMessage();
            inputMessage.setIntPart("choice",choice);
            WSIFMessage outputMessage = operation.createOutputMessage();
            WSIFMessage faultMessage = operation.createFaultMessage();

            boolean operationSucceeded = false;
            if (blocks == SYNC) {
                operationSucceeded =
                    operation.executeRequestResponseOperation(
                        inputMessage,
                        outputMessage,
                        faultMessage);

            } else if (blocks == ASYNC) {
                WSIFMessage context = operation.getContext();
                context.setObjectPart(
                    WSIFConstants.CONTEXT_JMS_PREFIX + "JMSReplyTo",
                    TestUtilities.getWsifProperty("wsif.async.replytoq2"));
                operation.setContext(context);

                WSIFCorrelationId id =
                    operation.executeRequestResponseAsync(inputMessage);
                System.out.println(
                    "async operation done, correlation id="
                        + id.getCorrelationId());

                Object jmsResponse =
                    TestUtilities.getJMSAsyncResponse(
                        id.getCorrelationId(),
                        TestUtilities.getWsifProperty("wsif.async.replytoq2"));

                operationSucceeded =
                    operation.processAsyncResponse(
                        jmsResponse,
                        outputMessage,
                        faultMessage);

            } else
                assertTrue(false);

            System.out.println(
                "JmsFaultTest "
                    + method
                    + " "
                    + operationSucceeded
                    + " "
                    + (names == null));

            assertTrue(
                "Bad boolean success value from executeRequestReponseOperation",
                (operationSucceeded && !faultExpected)
                    || (!operationSucceeded && faultExpected));
                    
            if (!operationSucceeded) {
                Iterator it = faultMessage.getPartNames();
                int i = 0;
                while (it.hasNext()) {
                    it.next();
                    i++;
                }
                
                if (names == null)
                    names = new String[] {};
                if (parts == null)
                    parts = new Object[] {};

                assertTrue(
                    "Bad number of parts in the fault message expected="
                        + names.length
                        + " got="
                        + i,
                    i == names.length);

                for (i = 0; i < names.length; i++) {
                    Object o = faultMessage.getObjectPart(names[i]);

                    assertTrue(
                        "Bad value for partName=" + names[i] + " " + o,
                        parts[i].equals(o));
                }
            }
            
            assertTrue(!exceptionExpected);
        } catch (Exception e) {
            System.err.println("JmsFaultTest(" + method + ") caught exception " + e);
            if (!exceptionExpected || !(e instanceof WSIFException))
                e.printStackTrace();
            assertTrue(exceptionExpected && e instanceof WSIFException);
        }
    }
}
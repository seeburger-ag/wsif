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

package chartype;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import util.TestUtilities;

/**
 * JUnit test to verify that char arguments to Java servies can be sent as parts containing
 * Strings of length 1 and that chars/Characters returned from a Java service are converted
 * to Strings before being added to the output message. These conversions allow Java/EJB
 * services to be more interoperable with SOAP services since XML does not define a char
 * type.
 * 
 * @author Owen Burroughs <owenb@apache.org>
 */
public class CharTest extends TestCase {

    private boolean debugMode = true;
    private String wsdl = TestUtilities.getWsdlPath("java\\test\\chartype") + "CharService.wsdl";;


    public static void main(java.lang.String[] args) {
	   junit.textui.TestRunner.run (suite());
    }

    public static Test suite() {
        return new TestSuite(CharTest.class);
    }

    public CharTest(String arg0) {
        super(arg0);
    }

    protected void setUp() {
    }

    public void testJava() throws Exception {
        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
        WSIFService service = factory.getService(wsdl, null, null, null, null);

        WSIFPort port = service.getPort();

        WSIFOperation operation;
        WSIFMessage inputMessage;
        WSIFMessage outputMessage;
        WSIFMessage faultMessage;

        String tempString;

        Object part;
        boolean operationSucceeded;

        /**********************************************************
         * Test calling a service that takes a char as an argument
         **********************************************************/
        operation = port.createOperation("setCharOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        inputMessage.setObjectPart("in", "a");

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue("setChar failed!!", operationSucceeded);

        if (operationSucceeded) {
            assertTrue(
                "setChar did not return a boolean",
                outputMessage.getObjectPart("out") instanceof Boolean);
            assertTrue("setChar did not return true", ((Boolean) outputMessage.getObjectPart("out")).booleanValue());
            debug("setChar returned "+ outputMessage.getObjectPart("out"));
        } else {
			debug("operation failed");
        }

        /**********************************************************
         * Test calling a service that returns a char
         **********************************************************/
        operation = port.createOperation("getCharOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        inputMessage.setBooleanPart("in", true);

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue("getChar failed!!", operationSucceeded);

        if (operationSucceeded) {
            assertTrue(
                "getChar did not return a String",
                outputMessage.getObjectPart("out") instanceof String);
            debug("getChar returned "+ outputMessage.getObjectPart("out"));
        } else {
			debug("operation failed");
        }
        
        /*************************************************************
         * Test calling a service that takes a char[][] as an argument
         *************************************************************/
        operation = port.createOperation("setCharArrayOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

		String[][] sa = new String[][] {{"a", "b"},{"d", "e", "g"}};

        inputMessage.setObjectPart("in", sa);

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue("setCharArray failed!!", operationSucceeded);

        if (operationSucceeded) {
            assertTrue(
                "setCharArray did not return a boolean",
                outputMessage.getObjectPart("out") instanceof Boolean);
            assertTrue("getCharArray did not return true", ((Boolean) outputMessage.getObjectPart("out")).booleanValue());                
            debug("setCharArray returned "+ outputMessage.getObjectPart("out"));
        } else {
			debug("operation failed");
        }
        
        /*************************************************************
         * Test calling a service that returns a char[][]
         *************************************************************/
        operation = port.createOperation("getCharArrayOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

		inputMessage.setBooleanPart("in", true);

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue("getCharArray failed!!", operationSucceeded);

        if (operationSucceeded) {
            assertTrue(
                "getCharArray did not return a String array",
                outputMessage.getObjectPart("out") instanceof String[][]);
            String[][] out = (String[][]) outputMessage.getObjectPart("out");
            debug("getCharArray returned "); 
            for (int i=0; i<out.length; i++) {
            	for (int j=0; j<out[i].length; j++) {
            		char c = 'X';
            		debug("    out["+i+"]["+j+"] = "+out[i][j]);
            	}
            }
        } else {
			debug("operation failed");
        }                 
    }
    
    private void debug(Object s) {
        if (debugMode)
            System.out.println(s);
    }    
}
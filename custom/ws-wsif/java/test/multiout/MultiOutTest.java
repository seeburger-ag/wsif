/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
 * Business Machines, Inc., http://www.ibm.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package multiout;

import java.util.Map;

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
 * Using the EJB provider, test operations which have multiple output parts
 * 
 * @author Owen Burroughs <owenb@apache.org>
 */
public class MultiOutTest extends TestCase {

    private boolean debugMode = true;
    private String wsdl =
        TestUtilities.getWsdlPath("java\\test\\multiout") + "MultiOut.wsdl";
    ;

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(MultiOutTest.class);
    }

    public MultiOutTest(String arg0) {
        super(arg0);
    }

    protected void setUp() {
    }

    public void testEJB() throws Exception {
        if (!TestUtilities.areWeTesting("ejb")) {
            return;
        }
        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
        WSIFService service =
            factory.getService(
                wsdl,
                "http://test.multiout/",                
                "EJBService",
                null,
                null);

        WSIFPort port = service.getPort();

        WSIFOperation operation;
        WSIFMessage inputMessage;
        WSIFMessage outputMessage;
        WSIFMessage faultMessage;

        String tempString;

        Object part;
        boolean operationSucceeded;

        operation = port.createOperation("getMOOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        inputMessage.setObjectPart("in", "abcd");

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue("getMOOperation failed!!", operationSucceeded);

        if (operationSucceeded) {
            debug("getMOOperation returned:");
            String surname = (String) outputMessage.getObjectPart("surname");
            assertTrue("surname incorrect - value was " + surname, "Test".equals(surname));
            debug("surname: " + surname);
            String firstname = (String) outputMessage.getObjectPart("firstname");
            assertTrue("firstname incorrect - value was " + firstname, "Bob".equals(firstname));
            debug("firstname: " + firstname);
            char initial = outputMessage.getCharPart("initial");
            assertTrue("initial incorrect - value was " + initial, 'A' == initial);
            debug("initial:" + initial);
            int age = outputMessage.getIntPart("age");
            assertTrue("age incorrect - value was " + age, 45 == age);
            debug("age: " + age);
            StringBuffer details = (StringBuffer) outputMessage.getObjectPart("details");
            assertTrue("details incorrect - value was " + details, "some data".equals(details.toString()));
            debug("details: " + details);
        } else {
            debug("operation failed");
        }
    }

    public void testEJBOld() throws Exception {
        if (!TestUtilities.areWeTesting("ejb")) {
            return;
        }    	
        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
        WSIFService service =
            factory.getService(
                wsdl,
                "http://test.multiout/",                
                "JavaService",
                null,
                null);

        WSIFPort port = service.getPort();

        WSIFOperation operation;
        WSIFMessage inputMessage;
        WSIFMessage outputMessage;
        WSIFMessage faultMessage;

        String tempString;

        Object part;
        boolean operationSucceeded;

        operation = port.createOperation("getMO2Operation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        inputMessage.setObjectPart("in", "abcd");

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue("getMO2Operation failed!!", operationSucceeded);

        if (operationSucceeded) {
            debug("getMO2Operation returned:");
            Map person = (Map) outputMessage.getObjectPart("person");
            assertTrue("person incorrect - value was " + person.get(new Integer(100)),
             "Test".equals((String) person.get(new Integer(100))));
            debug("person: " + person.get(new Integer(100)));
        } else {
            debug("operation failed");
        }
    }
    
    public void testJava() throws Exception {
        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
        WSIFService service =
            factory.getService(
                wsdl,
                "http://test.multiout/",                
                "JavaService",
                null,
                null);

        WSIFPort port = service.getPort();

        WSIFOperation operation;
        WSIFMessage inputMessage;
        WSIFMessage outputMessage;
        WSIFMessage faultMessage;

        String tempString;

        Object part;
        boolean operationSucceeded;

        operation = port.createOperation("getMOOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        inputMessage.setObjectPart("in", "abcd");
        StringBuffer sb = new StringBuffer();
        sb.append("Some data");
        inputMessage.setObjectPart("details", sb);

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue("getMOOperation failed!!", operationSucceeded);

        if (operationSucceeded) {
            debug("getMOOperation returned:");
            String surname = (String) outputMessage.getObjectPart("surname");
            assertTrue("surname incorrect - value was " + surname, "Test".equals(surname));
            debug("surname: " + surname);
            String firstname = (String) outputMessage.getObjectPart("firstname");
            assertTrue("firstname incorrect - value was " + firstname, "Bob".equals(firstname));
            debug("firstname: " + firstname);
            char initial = outputMessage.getCharPart("initial");
            assertTrue("initial incorrect - value was " + initial, 'A' == initial);
            debug("initial:" + initial);
            int age = outputMessage.getIntPart("age");
            assertTrue("age incorrect - value was " + age, 45 == age);
            debug("age: " + age);            
            StringBuffer details = (StringBuffer) outputMessage.getObjectPart("details");
            assertTrue("details incorrect - value was " + details, "some data".equals(details.toString()));
            debug("details: " + details);
        } else {
            debug("operation failed");
        }
    }

    public void testJavaOld() throws Exception {
        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
        WSIFService service =
            factory.getService(
                wsdl,
                "http://test.multiout/",                
                "JavaService",
                null,
                null);

        WSIFPort port = service.getPort();

        WSIFOperation operation;
        WSIFMessage inputMessage;
        WSIFMessage outputMessage;
        WSIFMessage faultMessage;

        String tempString;

        Object part;
        boolean operationSucceeded;

        operation = port.createOperation("getMO2Operation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        inputMessage.setObjectPart("in", "abcd");

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue("getMO2Operation failed!!", operationSucceeded);

        if (operationSucceeded) {
            debug("getMO2Operation returned:");
            Map person = (Map) outputMessage.getObjectPart("person");
            assertTrue("person incorrect - value was " + person.get(new Integer(100)),
             "Test".equals((String) person.get(new Integer(100))));
            debug("person: " + person.get(new Integer(100)));
        } else {
            debug("operation failed");
        }
    }

    private void debug(Object s) {
        if (debugMode)
            System.out.println(s);
    }
}
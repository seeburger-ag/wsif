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

package async;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFCorrelationId;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import util.TestUtilities;

import addressbook.wsiftypes.Address;
import addressbook.wsiftypes.Phone;

/**
 * Junit test to test the NativeJMSProvider
 * 
 * need to use the wsifjmssetup.bat and wsifmqset.bat 
 * 
 */
public class AsyncTests extends TestCase {

  public static String name1 = "Purdue Boilermaker";
  public static Address addr1 =
    new Address(
      1,
      "University Drive",
      "West Lafayette",
      "IN",
      47907,
      new Phone(765, "494", "4900"));

  public AsyncTests(String name) {
    super(name);
  }

  public static void main(String[] args) {
	 TestUtilities.startListeners();	
     junit.textui.TestRunner.run(suite());
     TestUtilities.stopListeners();	
  }

  public static Test suite() {
    return new TestSuite(AsyncTests.class);
  }

  public void setUp() {
    TestUtilities.setUpExtensionsAndProviders();
  }

  public void testSoapJms() {
  	 if ( TestUtilities.areWeTesting("jms") ) {
  		 doitStockquote( "SOAPJMSPort", "soap" );
         doitAddressBook_addEntry( "SOAPJMSPort", "soap" );
  	     doitAddressBook_getAddressFromName( "SOAPJMSPort", "soap" );
     } 
  }

  public void testAxisJms() {
  	 if ( TestUtilities.areWeTesting("jms") ) {
  		 doitStockquote( "SOAPJMSPort", "axis" );
         doitAddressBook_addEntry( "SOAPJMSPort", "axis" );
  	     doitAddressBook_getAddressFromName( "SOAPJMSPort", "axis" );
     } 
  }

  public void testNativeJms() {
  	 if ( TestUtilities.areWeTesting("jms") ) {
  		 doitStockquote( "NativeJmsPort", "" );
         doitAddressBook_addEntry( "NativeJmsPort", "" );
  	     doitAddressBook_getAddressFromName( "NativeJmsPort", "" ); 
  	 }
  }

  /**
   * Query Stockquote sample
   */
  public void doitStockquote(String portName, String protocol) {
    if (portName.toUpperCase().indexOf("JMS") != -1
        && !TestUtilities.areWeTesting("jms")) {
        return;
    }

    TestUtilities.setProviderForProtocol( protocol );

    String portType = "StockquotePT";
    String operationName = "getQuote";

    String wsdlLocation =
      TestUtilities.getWsdlPath("java\\test\\stockquote\\wsifservice") + "StockQuote.wsdl";

    System.out.println("\n=== StockQuote");
    try {
      WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
        WSIFService service = factory.getService(wsdlLocation, 
        	null, // serviceNS
    		null, // serviceName
            "http://wsifservice.stockquote/", // portTypeNS
  			portType); // portTypeName

      WSIFPort port = service.getPort(portName);

      // Executing synchronous executeRequestResponseOperation(input, output, fault )
      WSIFOperation operation = port.createOperation(operationName);

      WSIFMessage context = operation.getContext();
      context.setObjectPart( WSIFConstants.CONTEXT_JMS_PREFIX + "JMSReplyTo", 
                             TestUtilities.getWsifProperty("wsif.nativejms.responseq") );
      operation.setContext( context );

      WSIFMessage input = operation.createInputMessage();
      input.setName("GetQuoteInput");
      input.setObjectPart("symbol", "" );

      WSIFMessage output = operation.createOutputMessage();
      WSIFMessage fault = operation.createFaultMessage();

      doSyncOp( operation, input, output, context );
      float value = ((Float)output.getObjectPart( "quote" )).floatValue();
      System.out.println("sync stockquote found value = " + value);
      assertTrue( "doSyncOp stockquote value incorrect!", 
                  value == -1.0F );

      // Executing executeRequestResponseAsync(input, handler)
      operation = port.createOperation(operationName);

      operation.setContext( context );

      input = operation.createInputMessage();
      input.setName("GetQuoteInput");
      input.setObjectPart("symbol", "" );
      
      output = doAsyncOp( operation, input, context );
      value = ((Float)output.getObjectPart( "quote" )).floatValue();
      System.out.println("async stockquote found value = " + value);
      assertTrue( "doAsyncOp stockquote value incorrect!", 
                  value == -1.0F );

      // Executing executeRequestResponseAsync(input)
      operation = port.createOperation(operationName);

      operation.setContext( context );

      input = operation.createInputMessage();
      input.setName("GetQuoteInput");
      input.setObjectPart("symbol", "" );
      
      output = doAsyncOpNoHandler( operation, input, context );
      value = ((Float)output.getObjectPart( "quote" )).floatValue();
      System.out.println("async stockquote found value = " + value);
      assertTrue( "doAsyncOpNoHandler stockquote value incorrect!", 
                  value == -1.0F );

    } catch (Exception e) {
        e.printStackTrace();
        assertTrue("exception during stockquote test: " + e.getMessage(), false);
    }
  }

  /**
   * AddressBook sample - addEntry 
   */
  public void doitAddressBook_addEntry(String portName, String protocol) {

    if ( portName.indexOf( "JMS" ) != -1 && !TestUtilities.areWeTesting("jms") ) { 
       return;
    }

    TestUtilities.setProviderForProtocol( protocol );

    String portType = "AddressBook";
    String operationName = "addEntry";

    String wsdlLocation =
      TestUtilities.getWsdlPath("java\\test\\addressbook\\wsifservice")
        + "AddressBook.wsdl";

    System.out.println("\n=== AddressBook_addEntry");

    try {
      WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
        WSIFService service = factory.getService(wsdlLocation, 
        	null, // serviceNS
    		null, // serviceName
            "http://wsifservice.addressbook/", // portTypeNS
  			portType); // portTypeName

      service.mapType(
         new javax.xml.namespace.QName("http://wsiftypes.addressbook/", "address"),
         Class.forName("addressbook.wsiftypes.Address"));

      service.mapType(
         new javax.xml.namespace.QName("http://wsiftypes.addressbook/", "phone"),
         Class.forName("addressbook.wsiftypes.Phone"));

      WSIFPort port = service.getPort(portName);

      WSIFOperation operation = port.createOperation(operationName,
                                  "AddEntryWholeNameRequest", null);

      WSIFMessage input = operation.createInputMessage();
      input.setObjectPart("name", name1);
      input.setObjectPart("address", addr1); 

      operation.executeInputOnlyOperation(input);

    } catch (Exception e) {
      e.printStackTrace();
      assertTrue("exception during addressbook addEntry: " + e.getMessage(),
                 false);
    }

  }

  /**
   * AddressBook sample - getAddressFromName
   */
  public void doitAddressBook_getAddressFromName(String portName, String protocol) {

    if ( portName.indexOf( "JMS" ) != -1 && !TestUtilities.areWeTesting("jms") ) { 
       return;
    }
    TestUtilities.setProviderForProtocol( protocol );

    String portType = "AddressBook";
    String operationName = "getAddressFromName";

    String wsdlLocation =
      TestUtilities.getWsdlPath("java\\test\\addressbook\\wsifservice")
        + "AddressBook.wsdl";

    System.out.println("\n=== AddressBook_getAddressFromName");

    try {
      WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
        WSIFService service = factory.getService(wsdlLocation, 
        	null, // serviceNS
    		null, // serviceName
            "http://wsifservice.addressbook/", // portTypeNS
  			portType); // portTypeName

      service.mapType(
         new javax.xml.namespace.QName("http://wsiftypes.addressbook/", "address"),
         Class.forName("addressbook.wsiftypes.Address"));

      service.mapType(
         new javax.xml.namespace.QName("http://wsiftypes.addressbook/", "phone"),
         Class.forName("addressbook.wsiftypes.Phone"));

      WSIFPort port = service.getPort(portName);

      // Executing synchronous executeRequestResponseOperation(input, output, fault )
      String inputMsgName = "GetAddressFromNameRequest";
      String outputMsgName = "GetAddressFromNameResponse";
      WSIFOperation operation =
        port.createOperation(operationName, inputMsgName, outputMsgName);

      WSIFMessage context = operation.getContext();
      context.setObjectPart( WSIFConstants.CONTEXT_JMS_PREFIX + "JMSReplyTo", 
                             TestUtilities.getWsifProperty("wsif.nativejms.responseq") );
      operation.setContext( context );
    
      WSIFMessage input = operation.createInputMessage();
      input.setObjectPart("name", name1);

      WSIFMessage output = operation.createOutputMessage();
      WSIFMessage fault = operation.createFaultMessage();

      doSyncOp( operation, input, output, context );
      
      Address addressResponse = (Address) output.getObjectPart("address");
      System.out.println("Found address = " + addressResponse);
      assertTrue( "doSyncOp addresses not equal!", addr1.equals(addressResponse) );

      // Executing executeRequestResponseOperation(input, output, fault )
      operation =
        port.createOperation(operationName, inputMsgName, outputMsgName);

      operation.setContext( context );
    
      input = operation.createInputMessage();
      input.setObjectPart("name", name1);

      output = operation.createOutputMessage();

      output = doAsyncOp( operation, input, context );
      
      addressResponse = (Address) output.getObjectPart("address");
      System.out.println("Found address = " + addressResponse);
      assertTrue( "doAsyncOp addresses not equal!", addr1.equals(addressResponse) );

      // Executing executeRequestResponseAsync(input)
      operation =
        port.createOperation(operationName, inputMsgName, outputMsgName);

      operation.setContext( context );
    
      input = operation.createInputMessage();
      input.setObjectPart("name", name1);

      output = doAsyncOpNoHandler( operation, input, context );
      addressResponse = (Address) output.getObjectPart("address");
      System.out.println("Found address = " + addressResponse);
      assertTrue( "doAsyncOpNoHandler addresses not equal!", addr1.equals(addressResponse) );

    } catch (Exception e) {
      e.printStackTrace();
      assertTrue("exception during addressbook getAddressFromName test: " + e.getMessage(), false);
    }

  }

  private void doSyncOp(WSIFOperation op, 
                                WSIFMessage input, WSIFMessage output, WSIFMessage context) {
      try {
         op.setContext( context );
      	 WSIFMessage fault = op.createFaultMessage();
         boolean ok = op.executeRequestResponseOperation(input, output, fault );
         assertTrue( "executeRequestResponseOperation returned false!", ok );
      } catch (Exception ex) {
       	 ex.printStackTrace();
    	 assertTrue( "exception executing request: " + ex.getMessage(), false );
      }
  }

  private WSIFMessage doAsyncOp(WSIFOperation op, WSIFMessage input, WSIFMessage context) {
      AsyncResponseHandler handler = new AsyncResponseHandler(1); // 2 async calls
      try {
         context.setObjectPart( WSIFConstants.CONTEXT_JMS_PREFIX + "JMSReplyTo", 
                                TestUtilities.getWsifProperty("wsif.async.replytoq") );
         op.setContext( context );
         WSIFCorrelationId id = op.executeRequestResponseAsync(input, handler);
         System.out.println( "async operation done, correlation id=" + id.getCorrelationId() );
      } catch (Exception ex) {
    	 ex.printStackTrace();
    	 assertTrue( "exception executing async op: " + ex.getMessage(), false );
      }
      int i = 5;  // 15 seconds timout
      while ( i-- > 0 && !handler.isDone() ) {
         System.out.println( "async requests sent, waiting for responses - " + i );
    	 try {
    		Thread.sleep(3000);
    	 } catch (InterruptedException ex) {}
      }
      assertTrue( "no response to async operation!", i > 0 ); // no responses in time
      return handler.getOutputs()[0];
  }

  private WSIFMessage doAsyncOpNoHandler(WSIFOperation op, WSIFMessage input, WSIFMessage context) {
      WSIFMessage output = null;
      try {
         context.setObjectPart( WSIFConstants.CONTEXT_JMS_PREFIX + "JMSReplyTo", 
                                TestUtilities.getWsifProperty("wsif.async.replytoq2") );
         context.setObjectPart( "testJMSnoHandler", "true" ); 
         op.setContext( context );
         WSIFCorrelationId id = op.executeRequestResponseAsync( input );
         System.out.println( "async operation done, correlation id=" + id.getCorrelationId() );
         
        Object jmsResponse =
            TestUtilities.getJMSAsyncResponse(
                id.getCorrelationId(),
                TestUtilities.getWsifProperty("wsif.async.replytoq2"));

         output = op.createOutputMessage();
         WSIFMessage fault = op.createFaultMessage();
         op.processAsyncResponse( jmsResponse, output, fault );
      } catch (Exception ex) {
    	 ex.printStackTrace();
    	 assertTrue( "exception executing async op: " + ex.getMessage(), false );
      }
      return output;
  }

    
}
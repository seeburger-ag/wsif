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


import java.util.Iterator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFCorrelationId;
import org.apache.wsif.WSIFCorrelationService;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.base.WSIFServiceImpl;
import org.apache.wsif.providers.soap.apacheaxis.WSIFDynamicProvider_ApacheAxis;
import org.apache.wsif.util.WSIFCorrelationServiceLocator;
import org.apache.wsif.util.WSIFPluggableProviders;
import org.apache.wsif.util.WSIFProperties;
import org.apache.wsif.util.jms.WSIFJMSFinder;
import org.apache.wsif.util.jms.WSIFJMSFinderForJndi;
import util.TestUtilities;

import async.AsyncResponseHandler;

/**
 * Junit test to test out output message parts.
 * 
 * The WSDL output message can define multiple parts, but the response 
 * recieved from the remote service may not match what is defined in the WSDL.
 * Its been decided that parts defined in the WSDL but not received should be
 * set in the output WSIFMessage with a null value, and that parts in the 
 * response but not in the WSDL should be added to the ouput WSIFMessage.
 *   
 * @author Ant Elder <ant.elder@uk.ibm.com>
 */
public class OutputPartsTest extends TestCase {

  static final String WSDL_LOCATION =
      TestUtilities.getWsdlPath( "java\\test\\soap\\wsifservice" ) +  
      "FakeStockQuote.wsdl";

  static final String FAKE_SOAP_MSG_FILENAME1 =
      TestUtilities.getWsdlPath( "java\\test\\soap" ) + 
      "FakeSQRespOK.txt";
  static final String FAKE_SOAP_MSG_FILENAME2 =
      TestUtilities.getWsdlPath( "java\\test\\soap" ) + 
      "FakeSQRespNull.txt";
  static final String FAKE_SOAP_MSG_FILENAME3 =
      TestUtilities.getWsdlPath( "java\\test\\soap" ) + 
      "FakeSQRespMissingReturn.txt";
  static final String FAKE_SOAP_MSG_FILENAME4 =
      TestUtilities.getWsdlPath( "java\\test\\soap" ) + 
      "FakeSQRespMissingPart.txt";
  static final String FAKE_SOAP_MSG_FILENAME5 =
      TestUtilities.getWsdlPath( "java\\test\\soap" ) + 
      "FakeSQRespMissingAllParts.txt";
  static final String FAKE_SOAP_MSG_FILENAME6 =
      TestUtilities.getWsdlPath( "java\\test\\soap" ) + 
      "FakeSQRespRtnTypeErr.txt";
  static final String FAKE_SOAP_MSG_FILENAME7 =
      TestUtilities.getWsdlPath( "java\\test\\soap" ) + 
      "FakeSQRespExtraParts.txt";
      
  public OutputPartsTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
	 TestUtilities.startListeners();	
     junit.textui.TestRunner.run(suite());
     TestUtilities.stopListeners();	
  }

  public static Test suite() {
    return new TestSuite(OutputPartsTest.class);
  }

  public void setUp() {
    TestUtilities.setUpExtensionsAndProviders();
  }
  
  public void tearDown() {
     WSIFPluggableProviders.overrideDefaultProvider(
        "http://schemas.xmlsoap.org/wsdl/soap/",
        null);
  }

  public void testSOAP1() {
     doitStockquote( "SOAPJMSPort", "soap", FAKE_SOAP_MSG_FILENAME1, null );
  }
  public void testAxis1() {
     doitStockquote( "SOAPJMSPort", "axis", FAKE_SOAP_MSG_FILENAME1, null );
  }
  public void testSOAP2() {
  	 doitStockquote( "SOAPJMSPort", "soap", FAKE_SOAP_MSG_FILENAME2, "return value not found in response message" );
  }
  public void testAxis2() {
  	 doitStockquote( "SOAPJMSPort", "axis", FAKE_SOAP_MSG_FILENAME2, "<null>" );
  }
  public void testSOAP3() {
  	 doitStockquote( "SOAPJMSPort", "soap", FAKE_SOAP_MSG_FILENAME3, "java.lang.String" );
  }
  public void testAxis3() {
  	 doitStockquote( "SOAPJMSPort", "axis", FAKE_SOAP_MSG_FILENAME3, "java.lang.String" );
  }
  public void testSOAP4() {
  	 doitStockquote( "SOAPJMSPort", "soap", FAKE_SOAP_MSG_FILENAME4, "p1=null" );
  }
  public void testAxis4() {
  	 doitStockquote( "SOAPJMSPort", "axis", FAKE_SOAP_MSG_FILENAME4, "p1=null" );
  }
  public void testSOAP5() {
  	 doitStockquote( "SOAPJMSPort", "soap", FAKE_SOAP_MSG_FILENAME5, "parms=null" );
  }
  public void testAxis5() {
  	 doitStockquote( "SOAPJMSPort", "axis", FAKE_SOAP_MSG_FILENAME5, "parms=null" );
  }
  public void testSOAP6() {
  	 doitStockquote( "SOAPJMSPort", "soap", FAKE_SOAP_MSG_FILENAME6, "java.lang.String" );
  }

  // AXIS doesn't work with incorect return type
  //  public void testAxis6() {
  //  	 doitStockquote( "SOAPJMSPort", "axis", FAKE_SOAP_MSG_FILENAME6, "java.lang.String" );
  //  }
  public void testSOAP7() {
  	 doitStockquote( "SOAPJMSPort", "soap", FAKE_SOAP_MSG_FILENAME7, null );
  }
  
  // AXIS doesn't like unknown parts coming in
  //public void testAxis7() {
  //	 doitStockquote( "SOAPJMSPort", "axis", FAKE_SOAP_MSG_FILENAME7, null );
  //}

  /**
   * Query Stockquote sample
   */
  public void doitStockquote(String portName, String protocol, String fakeFile, String error) { 
    float value;
    WSIFPort port;
    WSIFOperation operation;
    WSIFMessage input, output, fault, context;

    if (portName.toUpperCase().indexOf("JMS") != -1
        && !TestUtilities.areWeTesting("jms")) {
        return;
    }

    TestUtilities.setProviderForProtocol( protocol );

    System.out.println("\n=== StockQuote");
    try {
      WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
        WSIFService service = factory.getService( WSDL_LOCATION, 
        	null, // serviceNS
    		null, // serviceName
            "http://wsifservice.stockquote/", // portTypeNS
  			"StockquotePT" ); // portTypeName

      port = service.getPort(portName);

      // Executing executeRequestResponseAsync(input)
      operation = port.createOperation( "getQuote" );

      context = operation.getContext();
      context.setObjectPart( 
         WSIFConstants.CONTEXT_JMS_PREFIX + "WSIF_FAKE",
         "\"" + fakeFile + "\"" );

      input = operation.createInputMessage();
      input.setName("GetQuoteInput");
      input.setObjectPart("symbol", "" );
      output = operation.createOutputMessage();

      //output = doAsyncOpNoHandler( operation, input, context );
      doSyncOp( operation, input, output, context );
      
      Object o;
      
      o = output.getObjectPart( "quote" );
      if ( o != null ) {
         value = ((Float)o).floatValue();
         assertTrue( "doAsyncOpNoHandler stockquote value incorrect!", 
            value == -1.0F );
      } else if (error.equals("<null>")) {
			assertNull("return value not null!!", o);
      } else {
         assertTrue( "return value null!!",
            error.equals( "return=null" )
            || error.equals( "all=null" ) );
      }
      	

      System.out.println( "out message contains:" );
      String partName;
      for (Iterator i = output.getPartNames(); i.hasNext(); ) {
         partName = (String) i.next();
         o = output.getObjectPart( partName );
         System.out.println( "part=" + partName + " value=" + o  );
      }
      
      o = output.getObjectPart( "p1" );
      if ( o != null ) {
         assertTrue( "error on part p1!!", o instanceof String );
      } else if (error.equals("<null>")) {
			assertNull("return value not null!!", o);
      } else {
      	assertTrue( "p1 is null!!", 
      	   error.equals( "p1=null" ) 
      	   || error.equals( "parms=null" )
      	   || error.equals( "all=null" ) );
      }
      o = output.getObjectPart( "p2" );
      if ( o != null ) {
         assertTrue( "error on part p2!!", o instanceof Float );
      } else if (error.equals("<null>")) {
			assertNull("return value not null!!", o);
      } else {
      	assertTrue( "p2 is null!!", 
      	   error.equals( "p2=null" ) 
      	   || error.equals( "parms=null" )
      	   || error.equals( "all=null" ) );
      }
      o = output.getObjectPart( "p3" );
      if ( o != null ) {
         assertTrue( "error on part p3!!", o instanceof String );
      } else if (error.equals("<null>")) {
			assertNull("return value not null!!", o);
      } else {
      	assertTrue( "p3 is null!!", 
      	   error.equals( "p3=null" ) 
      	   || error.equals( "parms=null" )
      	   || error.equals( "all=null" ) );
      }

    } catch (Exception ex) {
    	if ( error == null || !error.equals(ex.getMessage()) ) {
           ex.printStackTrace();
           assertTrue("exception during stockquote test: " + ex.getMessage(), false);
    	}
    }
  }

  private void doSyncOp(WSIFOperation op,
                         WSIFMessage input, 
                         WSIFMessage output, 
                         WSIFMessage context) throws WSIFException {
      //try {
         op.setContext( context );
      	 WSIFMessage fault = op.createFaultMessage();
         boolean ok = op.executeRequestResponseOperation(input, output, fault );
         assertTrue( "executeRequestResponseOperation returned false!", ok );
      //} catch (Exception ex) {
      // 	 ex.printStackTrace();
      //	 assertTrue( "exception executing request: " + ex.getMessage(), false );
      //}
  }

  private WSIFMessage doAsyncOp(WSIFOperation op, WSIFMessage input, WSIFMessage context) throws WSIFException{
      AsyncResponseHandler handler = new AsyncResponseHandler(1); // 2 async calls
      context.setObjectPart( WSIFConstants.CONTEXT_JMS_PREFIX + "JMSReplyTo", 
                             TestUtilities.getWsifProperty("wsif.async.replytoq") );
      op.setContext( context );
      WSIFCorrelationId id = op.executeRequestResponseAsync(input, handler);

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

  private WSIFMessage doAsyncOpNoHandler(WSIFOperation op, WSIFMessage input, WSIFMessage context) 
                                                              throws WSIFException, JMSException  {
      WSIFMessage output = null;

         context.setObjectPart( WSIFConstants.CONTEXT_JMS_PREFIX + "JMSReplyTo", 
                                TestUtilities.getWsifProperty("wsif.async.replytoq2") );
         context.setObjectPart( "testJMSnoHandler", "true" ); 
         op.setContext( context );
         WSIFCorrelationId id = op.executeRequestResponseAsync( input );
         System.out.println( "async operation done, correlation id=" + id.getCorrelationId() );
         op = null;
         
        Object jmsResponse =
            TestUtilities.getJMSAsyncResponse(
                id.getCorrelationId(),
                TestUtilities.getWsifProperty("wsif.async.replytoq2"));
         
         WSIFCorrelationService cs = 
            WSIFCorrelationServiceLocator.getCorrelationService();
         Object o;
         synchronized( cs ) { 
            o = cs.get( id ); 
         }
         if ( o != null && o instanceof WSIFOperation ) {      
            cs.remove( id ); 
            op = (WSIFOperation)o;
         } else {
         	assertTrue( "stored correlation object not as expected: " + o, false );
         }
         output = op.createOutputMessage();
         WSIFMessage fault = op.createFaultMessage();
         op.processAsyncResponse( jmsResponse, output, fault );

      return output;
  }
}
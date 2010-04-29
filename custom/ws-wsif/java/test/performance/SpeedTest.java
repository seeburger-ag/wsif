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
package performance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.base.WSIFServiceImpl;
import org.apache.wsif.providers.soap.apacheaxis.WSIFDynamicProvider_ApacheAxis;
import org.apache.wsif.util.WSIFPluggableProviders;

import util.TestUtilities;

import addressbook.wsiftypes.Address;
import addressbook.wsiftypes.Phone;

/**
 * Junit test to measure some simple performance statistics.
 * It times a number of iterations of getPort, createOperation,
 * and executeRequestResponseOperation for each provider. The 
 * results are stored in a properties file to allow each run 
 * to test if the performance has changed since the last run.
 * 
 * The number of iterations to do is determined by the time
 * each test takes and the TIME_PER_TEST constant. You need
 * to run it atleast 3 times to get the iterations calculation
 * to settle on a good value; The MARGIN_OF_ERROR constant 
 * defines how mush variation between run times is ignored
 * when determining if the performance has changed. 
 *  
 * You may need to change the TIME_PER_TEST and MARGIN_OF_ERROR 
 * constants for your machine speed.
 * 
 * For testing you can add futher timing points to the internal
 * WSIF classes which this test will then monitor and report on.
 * 
 * For example, to time the axis provider invoke method, at the
 * start of the invoke method add:
 *    Monitor.start( "axisInvoke" ); 
 * followed by a stop call at the end of the invoke method:
 *    Monitor.stop( "axisInvoke" );
 * 
 * Monitor points can be paused around unwanted code. For example
 * with the above monitors in the invoke method, you could exclude
 * the SOAP call time with:
 *    Monitor.pause( "axisInvoke" );
 *    resp = call.invoke(locationUri, getSoapActionURI());
 *    Monitor.resume( "axisInvoke" ); 
 * 
 * @author <a href="mailto:antelder@apache.org">Ant Elder</a>
 */
public class SpeedTest extends TestCase {

    // This defines how long each test should be run for.
    // There are 3 tests per provider; getPort, createOperation
    // and executeOperation, so the total runnning time will be
    // 3 times this value, times the number of providers tested.
    static final float TIME_PER_TEST = 20000.0F;  // 1 minute

    // This defines the time variation between runs before a
    // result is considered a significant performance change.
    // System.currentTimeMillis() is only acurate to 10msecs
    // so for this to work you need to run enough iterations    
    static final float MARGIN_OF_ERROR = 0.1F;  // tenth of a millisecond
    
    // defines if this run will overwrite existing statistics
    static final boolean UPDATE_STATS = true;
    
    // delete this file to reset the previous statistics
    static final File propFile = new File( "wsifPerformanceStats.txt" );
    
    static final int DEFAULT_ITERATIONS = 500;
    
    static final boolean TEST_GETPORT = true;
    static final boolean TEST_CREATEOP = true;
    static final boolean TEST_EXECOP = true;

    static Properties stats, newStats;
    
    static final String wsdlLocation =
        TestUtilities.getWsdlPath("java\\test\\addressbook\\wsifservice")
            + "AddressBook.wsdl";

    public SpeedTest(String name) {
        super(name);
    }

	public static void main(String[] args) {
	   TestUtilities.startListeners();	
	   junit.textui.TestRunner.run (suite());
       TestUtilities.stopListeners();
	}

    public static Test suite() {
        TestSuite suite = new TestSuite("Speed Tests");
		suite.addTest(new TestSuite(SpeedTest.class) );
		//suite.addTest(new TestSuite(SpeedTest.class) );
		//suite.addTest(new TestSuite(SpeedTest.class) );
        return suite;
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
        newStats = new Properties();
        Monitor.clear();
    }

    public void testJava() {
        doit("JavaPort", "java");
    }
/*
    public void testJava() {
        doit("JavaPort", "java");
    }
    public void testAxis() {
        doit("SOAPPort", "axis");
    }
    JMS seems to slow for most runs
    public void testSoapJms() {
        doit("SOAPJMSPort", "soap");
    }
    public void testAxisJms() {
        doit("SOAPJMSPort", "axis");
    }
    public void testNativeJms() {
        doit("NativeJmsPort", "njms" ); 
    }
    //public void testEJB() { 
    //}
*/

    private void doit(String portName, String protocol) {
        int iterations;
        String testName;
        String testNamePrefix = protocol + "." + portName;

        if (portName.toUpperCase().indexOf("JMS") != -1
            && !TestUtilities.areWeTesting("jms"))
            return;

        TestUtilities.setProviderForProtocol( protocol );

        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service = factory.getService(wsdlLocation, null, // serviceNS 
               null, // serviceName 
               "http://wsifservice.addressbook/", // portTypeNS 
               "AddressBook"); // portTypeName 

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

            WSIFPort port = null;

            port = service.getPort(portName);
            
            /*
             * Run iterations of getPort
             */
            testName = testNamePrefix + ".getPort";
            iterations = (TEST_GETPORT)? getIterations( testName ) : 1;
            System.out.println( "running " + iterations + " " + testName + " iterations..." ); 
            for (int i = 0; i < iterations; i++ ) {
               Monitor.start( testName );
               port = service.getPort(portName);
               Monitor.stop( testName );
            }

            WSIFOperation operation =
                port.createOperation("addEntry", "AddEntryWholeNameRequest", null);

            WSIFMessage inputMessage = operation.createInputMessage();
            WSIFMessage outputMessage = operation.createOutputMessage();
            WSIFMessage faultMessage = operation.createFaultMessage();

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
            boolean ok=
                operation.executeRequestResponseOperation(
                    inputMessage,
                    outputMessage,
                    faultMessage);

            assertTrue( "failed to add name and address!!", ok );

            /*
             * Run iterations of createOperation
             */
            testName = testNamePrefix + ".createOperation";
            iterations = (TEST_CREATEOP)? getIterations( testName ) : 1;
            System.out.println( "running " + iterations + " " + testName + " iterations..." ); 
            for (int i = 0; i < iterations; i++ ) {
               Monitor.start( testName );
			   operation = port.createOperation("getAddressFromName");
               Monitor.stop( testName );
            }

            /*
             * Run iterations of executeRequestResponseOperation
             */
            testName = testNamePrefix + ".executeOperation";
            iterations = (TEST_EXECOP)? getIterations( testName ) : 1;
            System.out.println( "running " + iterations + " " + testName + " iterations..." ); 
            for (int i = 0; i < iterations; i++ ) {
			   operation = port.createOperation("getAddressFromName");
			   inputMessage = operation.createInputMessage();
			   outputMessage = operation.createOutputMessage();
			   faultMessage = operation.createFaultMessage();

			   inputMessage.setObjectPart("name", nameToAdd);

               Monitor.start( testName );
			   boolean operationSucceeded =
				  operation.executeRequestResponseOperation(
					 inputMessage,
					 outputMessage,
					 faultMessage);
               Monitor.stop( testName );
			   if (!operationSucceeded) {
				  System.out.println("Failed to lookup name in addressbook");
				  assertTrue("executing op returned false!!", false);
			   }
            }

            // make sure it all worked
 		    Address addressFound =
			   (Address) outputMessage.getObjectPart("address");
			assertTrue( "returned address not correct!!", 
			   addressToAdd.equals( addressFound) ); 
            
            Monitor.printResults();
           
            // test is theres a performance change
            boolean worse = testResults();
            setIterations( testNamePrefix );
            mergeNewStats();
            if ( UPDATE_STATS ) {
               saveStatsToFile();
            }

            assertTrue( "performance is worse than before!!", !worse );

       } catch (Exception ex) {
	       ex.printStackTrace();
		   assertTrue("exception executing op!!", false);
	   }
    }

    private boolean testResults() {
    	float diff;
        String s1, s2;
    	boolean worse = false;

        HashMap results = Monitor.getAvgResults();
        String testName;
        float duration, oldDuration;
        for (Iterator i = results.keySet().iterator(); i.hasNext(); ) {
        	testName = (String)i.next();
            duration = ((Float)results.get( testName )).floatValue();
            duration = ((float)Math.round( duration * 1000 ) / 1000 );     
            oldDuration = getfloatStat( testName );            
    	    newStats.setProperty( testName, ""+duration );
    	    diff = duration - oldDuration;
    	    if ( Math.abs(diff) - MARGIN_OF_ERROR > 0 ) {
                worse = diff > 0;
   		        System.err.println( testName + " significantly " + 
    		       ( worse? "worse" : "better" ) + 
    		       " by " + diff + " msecs, time = " + duration + " msecs" );
    	    }
        }

    	return worse;
    }
    
    private int getIterations(String testName) {
    	int i;
        i = getintStat( testName + ".Iterations" );
        if ( i == 0 ) {
        	i = DEFAULT_ITERATIONS;
        }
        return i;	
    }
    
    private void setIterations(String prefix) {
        String s;
        int iterations;
        float duration, createDuration;
        HashMap results = Monitor.getAvgResults();
        
        s = prefix + ".getPort";
        duration = ((Float)results.get( s )).floatValue();
        iterations = (duration==0) ? 
           DEFAULT_ITERATIONS : (int)((TIME_PER_TEST*100) / (duration*100)); 
    	newStats.setProperty( s + ".Iterations", ""+iterations );

        s = prefix + ".createOperation";
        duration = ((Float)results.get( s )).floatValue();
        iterations = (duration==0) ? 
           DEFAULT_ITERATIONS : (int)((TIME_PER_TEST*100) / (duration*100)); 
    	newStats.setProperty( s + ".Iterations", ""+iterations );
        createDuration = duration;

        s = prefix + ".executeOperation";
        duration = ((Float)results.get( s )).floatValue();
        iterations = (duration==0) ? 
           DEFAULT_ITERATIONS : (int)((TIME_PER_TEST*100) / ((duration+createDuration)*100)); 
    	newStats.setProperty( s + ".Iterations", ""+iterations );

    }
    
    private int getintStat(String name) {
    	int i;
    	Properties stats = getStats();
    	try {
    	   i = Integer.parseInt(stats.getProperty( name ) );
    	} catch (Exception ex) {
    	   i = 0;
    	}
        return i;	
    }
    
    private float getfloatStat(String name) {
    	float f;
    	Properties stats = getStats();
    	try {
    	   f = Float.parseFloat(stats.getProperty( name ) );
    	} catch (Exception ex) {
    	   f = 0;
    	}
        return f;	
    }
    
    private static Properties getStats() {
    	if ( stats == null ) {
    		loadStatsFromFile();
    	}
    	return stats;
    }

    private static void mergeNewStats() {
        Properties stats = getStats();
        String testName;
        String value;
        int iterations;
        for (Enumeration i = newStats.keys(); i.hasMoreElements(); ) {
           testName = (String)i.nextElement();
           value = (String)newStats.get( testName );
    	   stats.setProperty( testName, value );
        }
    }
    
    private static void loadStatsFromFile() {
    	try {
    	   FileInputStream fis = new FileInputStream( propFile );
    	   stats = new Properties();
    	   stats.load( fis );
    	} catch (Exception ex) {
    		stats = new Properties();
    	}
    }
    
    private static void saveStatsToFile() {
    	try {
   	       FileOutputStream fos = new FileOutputStream( propFile );
    	   stats.store( fos, "WSIF Performance stats" );
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
        
}
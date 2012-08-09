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

package stockquote;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.base.WSIFServiceImpl;
import org.apache.wsif.providers.soap.apacheaxis.WSIFDynamicProvider_ApacheAxis;
import org.apache.wsif.util.WSIFPluggableProviders;

import stockquote.wsifservice.StockquotePT;
import util.TestUtilities;

/**
 * Junit test to test out the Stockquote sample.
 * @author Mark Whitlock
 */
public class StockquoteTest extends TestCase {
    String wsdlLocation =
        TestUtilities.getWsdlPath("java\\test\\stockquote\\wsifservice")
            + "Stockquote.wsdl";
    static String server = TestUtilities.getSoapServer().toUpperCase();

    public StockquoteTest(String name) {
        super(name);
    }

    public static void main(String[] args)
    {
        TestUtilities.startListeners(
            TestUtilities.STOCKQUOTE_LISTENER
                | TestUtilities.NATIVEJMS_LISTENER);

        junit.textui.TestRunner.run(suite());
        TestUtilities.stopListeners();
    }

    public static Test suite() {
        return new TestSuite(StockquoteTest.class);
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
    }

    public void testAxis() {
        doit(server+"Port", "axis");
    }
    public void testSoap() {
        doit(server+"Port", "soap");
    }
    public void testJava() {
        doit("JavaPort", "java");
    }
    public void testSoapJms() {
        doit("SOAPJMSPort", "soap");
    }
    public void testAxisJms() {
        doit("SOAPJMSPort", "axis");
    }
    public void testNativeJms() {
    	doit("NativeJmsPort",""); 
    }

    private void doit(String portName, String protocol) {
        if (portName.toUpperCase().indexOf("JMS") != -1
            && !TestUtilities.areWeTesting("jms"))
            return;

        TestUtilities.setProviderForProtocol( protocol );

        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory.getService(
                    wsdlLocation,
                    null,
                    null,
                    "http://wsifservice.stockquote/",
                    "StockquotePT");

            System.err.println("\n\nUsing '" + portName + "' port:");
            StockquotePT stub =
                (StockquotePT) service.getStub(portName, StockquotePT.class);

            float quote = stub.getQuote("");
            System.err.println(">> Received quote " + quote + " for ''");
            assertTrue(quote == -1.0F);

        } catch (Exception e) {
            System.err.println("StockquoteTest(" + portName + ") caught exception " + e);
            e.printStackTrace();
            assertTrue(false);
        } finally {
        	TestUtilities.resetDefaultProviders();
        }
    }

}

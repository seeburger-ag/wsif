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

package buy;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.util.WSIFPluggableProviders;
import util.TestUtilities;

/**
 * Junit test to test out the Stockquote sample.
 * @author Mark Whitlock
 */
public class BuyTest extends TestCase {
    String wsdlLocation =
        TestUtilities.getWsdlPath("java\\test\\buy") + "buy.wsdl";

    public BuyTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(BuyTest.class);
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
    }

    public void testSoapJms() {
        doit("JMSPort", "soap");
    }
    public void testAxisJms() {
        doit("JMSPort", "axis");
    }

    private void doit(String portName, String protocol) {
        if (portName.indexOf("JMS") != -1 && !TestUtilities.areWeTesting("jms"))
            return;

        TestUtilities.setProviderForProtocol( protocol );

        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
                WSIFService service = factory.getService(wsdlLocation, null, // serviceNS
        null, // serviceName
        "http://www.buyservice.com/buy-interface", // portTypeNS
    "buyService"); // portTypeName

            System.err.println("\n\nUsing '" + portName + "' port:");
            buyService stub = (buyService) service.getStub(portName, buyService.class);

            String resp = stub.buy("IBM", 100);
            System.out.println("\nResponse >> " + resp);
        } catch (Exception e) {
            System.err.println("StockquoteTest(" + portName + ") caught exception " + e);
            e.printStackTrace();
            assertTrue(false);
        }

      	TestUtilities.resetDefaultProviders();

    }

}

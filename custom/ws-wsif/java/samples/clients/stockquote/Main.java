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

package clients.stockquote;

import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.base.WSIFServiceImpl;
import org.apache.wsif.providers.soap.apacheaxis.WSIFDynamicProvider_ApacheAxis;
import org.apache.wsif.util.WSIFPluggableProviders;

import stockquote.wsifservice.StockquotePT;

/**
 * This sample shows how to use WSIF to invoke the addressbook service
 * using the static stub. It uses both the ports avaiable via the stub
 * to invoke the service.
 *
 * @author Sanjiva Weerawarana
 * @author Matthew J. Duftler
 * @author Alekander Slominski
 * @author Paul Fremantle
 * @author Nirmal Mukhi
 * @author Michael Beisiegel
 */
public class Main {

    private static void doit(StockquotePT service) throws Exception {
        String name1 = "IBM";
        System.err.println(">> Getting quote for '" + name1 + "'");
        float quote = service.getQuote(name1);
        System.err.println(">> Received quote: " + quote);
    }

    private static void usage() {
        System.err.println(
            "Usage: java "
                + Main.class.getName()
                + " portName wsdlLocation [soap|axis]");
        System.exit(1);
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 3)
            usage();

        String protocol = null;
        String portName = args.length > 0 ? args[0] : null;
        if (args.length == 1 && (portName.equals("soap") || portName.equals("axis"))) {
            protocol = portName;
            portName = null;
        }

        String wsdlLocation = args.length > 1 ? args[1] : null;
        if (args.length == 2
            && (wsdlLocation.equals("soap") || wsdlLocation.equals("axis"))) {
            protocol = wsdlLocation;
            wsdlLocation = null;
        }

        protocol = args.length == 3 ? args[2] : "";
        if (!protocol.equals("")
            && !protocol.equals("soap")
            && !protocol.equals("axis"))
            usage();

        if (protocol.equals("axis"))
            WSIFPluggableProviders.overrideDefaultProvider(
                "http://schemas.xmlsoap.org/wsdl/soap/",
                new WSIFDynamicProvider_ApacheAxis());

        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service = factory.getService(wsdlLocation, null, // serviceNS
        null, // serviceName
        "http://wsifservice.stockquote/", // portTypeNS
    "StockquotePT"); // portTypeName

        StockquotePT stub = null;
        if (portName != null) {
            System.err.println("\n\nUsing '" + portName + "' port:");
            stub = (StockquotePT) service.getStub(portName, StockquotePT.class);
            doit(stub);
        } else {
            System.err.println("\n\nUsing SOAP port:");
            stub = (StockquotePT) service.getStub("SOAPPort", StockquotePT.class);
            doit(stub);

            System.err.println("\n\nUsing Java port:");
            stub = (StockquotePT) service.getStub("JavaPort", StockquotePT.class);
            doit(stub);
        }

        if (protocol.equals("axis")) { // reset to default provider
            WSIFPluggableProviders.overrideDefaultProvider(
                "http://schemas.xmlsoap.org/wsdl/soap/",
                null);
        }
    }
}

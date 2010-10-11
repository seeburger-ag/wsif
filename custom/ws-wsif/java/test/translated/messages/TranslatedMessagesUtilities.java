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

package translated.messages;

import java.util.Locale;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import stockquote.wsifservice.StockquotePT;
import util.TestUtilities;

/**
 * @author Mark Whitlock
 */
class TranslatedMessagesUtilities {
    static String wsdlLocation =
        TestUtilities.getWsdlPath("java\\test\\stockquote\\wsifservice")
            + "Stockquote.wsdl";
    static String server = TestUtilities.getSoapServer().toUpperCase();

    static void doit(Locale name) {
        try {
            Locale.setDefault(name);
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory.getService(
                    wsdlLocation,
                    null,
                    null,
                    "http://wsifservice.stockquote/",
                    "StockquotePT");

            System.err.println("\nUsing locale '" + name + "'");
            StockquotePT stub =
                (StockquotePT) service.getStub(server+"Port", StockquotePT.class);

            float quote = stub.getQuote("");
            System.err.println(">> Received quote " + quote + " for ''");

        } catch (Exception e) {
            System.out.println("Caught Locale exception " + e);
        }
    }
}


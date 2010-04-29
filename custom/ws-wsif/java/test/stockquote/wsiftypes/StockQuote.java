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

package stockquote.wsiftypes;

import java.net.URL;
import java.io.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;


/**
 * This sample is taken from ApacheSOAP and changed to get
 *  JAXP document builder directly (no need for Apache SOAP classes).
 *
 * @author Alekander Slominski (aslom@watson.ibm.com)
 * @author Sanjiva Weerawarana (sanjiva@watson.ibm.com)
 */
public class StockQuote {
  
  public float getQuote (String symbol) throws Exception {
  	if (symbol==null || symbol.length()==0) return -1.0F;
    
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder xdb= factory.newDocumentBuilder();
    
    // get a real (delayed by 20min) stockquote from
    // http://www.xmltoday.com/examples/stockquote/. The IP addr
    // below came from the host that the above form posts to ..
    URL url = new URL ("http://www.xmltoday.com/examples/stockquote/getxmlquote.vep?s="+symbol);
    InputStream is = url.openStream ();
    Document d = xdb.parse(is);
    Element e = d.getDocumentElement ();
    NodeList nl = e.getElementsByTagName ("price");
    e = (Element) nl.item (0);
    String quoteStr = e.getAttribute ("value");
    try {
      return Float.valueOf (quoteStr).floatValue ();
    } catch (NumberFormatException e1) {
      // mebbe its an int?
      try {
        return Integer.valueOf (quoteStr).intValue () * 1.0F;
      } catch (NumberFormatException e2) {
        return -1.0F;
      }
    }
  }
}


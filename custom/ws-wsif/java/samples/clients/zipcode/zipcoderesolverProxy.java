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

package clients.zipcode;

import java.net.*;
import java.util.*;
import org.w3c.dom.*;
import org.apache.soap.*;
import org.apache.soap.encoding.*;
import org.apache.soap.encoding.soapenc.*;
import org.apache.soap.rpc.*;
import org.apache.soap.util.xml.*;
import org.apache.soap.messaging.*;

public class zipcoderesolverProxy
{
  private Call call = new Call();
  private URL url = null;
  private String stringURL = "http://webservices.eraserver.net/zipcoderesolver/zipcoderesolver.asmx";
  private SOAPMappingRegistry smr = call.getSOAPMappingRegistry();

  public zipcoderesolverProxy()
  {
  }

  public synchronized void setEndPoint(URL url)
  {
    this.url = url;
  }

  public synchronized URL getEndPoint() throws MalformedURLException
  {
    return getURL();
  }

  private URL getURL() throws MalformedURLException
  {
    if (url == null && stringURL != null && stringURL.length() > 0)
    {
      url = URI.create(stringURL).toURL();
    }
    return url;
  }




  public synchronized org.w3c.dom.Element ShortZipCode_(org.w3c.dom.Element parameters) throws Exception
  {
    String targetObjectURI = "";
    String SOAPActionURI = "http://webservices.eraserver.net/ShortZipCode";

    if(getURL() == null)
    {
      throw new SOAPException(Constants.FAULT_CODE_CLIENT,
      "A URL must be specified via zipcoderesolverProxy.setEndPoint(URL).");
    }

    // create message envelope and body
    Envelope msgEnv = new Envelope();
    Body msgBody = new Body();
    Vector vect = new Vector();

    vect.add(parameters);
    msgBody.setBodyEntries(vect);
    msgEnv.setBody(msgBody);

    // create and send message
    Message msg = new Message();
    msg.send(getURL(),SOAPActionURI, msgEnv);

    // receive response envelope
    Envelope env = msg.receiveEnvelope();
    Body retbody = env.getBody();
    java.util.Vector v = retbody.getBodyEntries();

    return (Element)v.firstElement();
    
  }

  public synchronized ShortZipCodeResponse_ElementContentType ShortZipCode(ShortZipCode_ElementContentType parameters) throws Exception
  {
    parameters.changeLocalName("ShortZipCode");
    parameters.changeNamespaceURI("http://webservices.eraserver.net/");
    org.w3c.dom.Element parameters_ = parameters.createElement();

    // delegate to method ShortZipCode_
    org.w3c.dom.Element result_ = ShortZipCode_(parameters_);

    // convert result_ from an org.w3c.dom.Element to ShortZipCodeResponse_ElementContentType
    ShortZipCodeResponse_ElementContentType aShortZipCodeResponse_ElementContentType = new ShortZipCodeResponse_ElementContentType();
    aShortZipCodeResponse_ElementContentType.populateFrom(result_);

    return aShortZipCodeResponse_ElementContentType;
  }











}

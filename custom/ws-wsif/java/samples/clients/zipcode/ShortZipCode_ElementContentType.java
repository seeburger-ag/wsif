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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import org.w3c.dom.Element;

public class ShortZipCode_ElementContentType extends AnyType
{
  public ShortZipCode_ElementContentType()
  {
    addElement("http://webservices.eraserver.net/#accessCode", java.lang.String.class);
    addElement("http://webservices.eraserver.net/#address", java.lang.String.class);
    addElement("http://webservices.eraserver.net/#city", java.lang.String.class);
    addElement("http://webservices.eraserver.net/#state", java.lang.String.class);
  }

  public String getAccessCode()
  {
    return (String)this.basicGet("http://webservices.eraserver.net/#accessCode", 0);
  }

  public void setAccessCode(String accessCode)
  {
    this.basicSet("http://webservices.eraserver.net/#accessCode", 0, accessCode);
  }

  public String getAddress()
  {
    return (String)this.basicGet("http://webservices.eraserver.net/#address", 0);
  }

  public void setAddress(String address)
  {
    this.basicSet("http://webservices.eraserver.net/#address", 0, address);
  }

  public String getCity()
  {
    return (String)this.basicGet("http://webservices.eraserver.net/#city", 0);
  }

  public void setCity(String city)
  {
    this.basicSet("http://webservices.eraserver.net/#city", 0, city);
  }

  public String getState()
  {
    return (String)this.basicGet("http://webservices.eraserver.net/#state", 0);
  }

  public void setState(String state)
  {
    this.basicSet("http://webservices.eraserver.net/#state", 0, state);
  }

}



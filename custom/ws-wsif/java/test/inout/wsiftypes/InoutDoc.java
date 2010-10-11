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

package inout.wsiftypes;

/**
 * Inout document style service used by InoutTest for various miscelleanous tests.
 * @author Mark Whitlock
 */

import java.util.*;
import org.apache.soap.*;
import org.apache.soap.rpc.*;
import org.apache.soap.util.xml.*;

public class InoutDoc 
{
  public void addEntry(Envelope env, SOAPContext reqCtx, SOAPContext resCtx)
    throws Exception
  {
  	System.out.println("Inside doc-style addEntry");
    //resCtx.setRootPart("OK thanks, got the PO; we'll contact you when ready.",
    //    "text/xml");
  }

  public void getAddressFromName(Envelope env, SOAPContext reqCtx, SOAPContext resCtx)
    throws Exception 
  {
    throw new IllegalArgumentException ("Huh?");
  }

  public void addNumbers(Envelope env, SOAPContext reqCtx, SOAPContext resCtx)
    throws Exception 
  {
    throw new IllegalArgumentException ("Huh?");
  }

  public void getDate(Envelope env, SOAPContext reqCtx, SOAPContext resCtx)
    throws Exception 
  {
    throw new IllegalArgumentException ("Huh?");
  }
}

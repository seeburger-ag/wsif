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

package wsdl;

/**
 * Junit test to test out different ways to load WSDL
 * @author Mark Whitlock
 */

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import util.AddressUtility;
import util.TestUtilities;

import addressbook.wsifservice.AddressBook;
import addressbook.wsiftypes.Address;
import addressbook.wsiftypes.Phone;

public class WsdlLoadingTest extends TestCase {
    String urlWsdl = "http://localhost:8080/wsdl/AddressBook.wsdl";
    String fileWsdl = 
      TestUtilities.getWsdlPath("java\\test\\addressbook\\wsifservice") +
      "AddressBook.wsdl";
    static String server = TestUtilities.getSoapServer().toUpperCase();
      
    static String name1 = "Purdue Boilermaker";
    static Address addr1 = new Address (1, "University Drive",
                                      "West Lafayette", "IN", 47907,
                                      new Phone (765, "494", "4900"));

    public WsdlLoadingTest(String name) { super(name); }
        
	public static void main(String[] args) 
	  { junit.textui.TestRunner.run (suite()); }
	
	public static Test suite()
	  { return new TestSuite(WsdlLoadingTest.class); }
	  
	public void setUp() { TestUtilities.setUpExtensionsAndProviders(); }
	
    public void testUrl() 
    { 
      if (TestUtilities.areWeTesting("remotewsdl")) 
        doit("http://localhost:8080/wsdl/AddressBook.wsdl",null); 
    }
      
    public void testFile() 
    {
      doit(TestUtilities.getWsdlPath("java\\test\\addressbook\\wsifservice") +
           "AddressBook.wsdl",null); 
    }

    public void testBadUrl() 
    { 
      if (TestUtilities.areWeTesting("remotewsdl")) 
        doit("http://localhost:8080/wsdl/AddressBok.wsdl","MalformedURLException"); 
    }
      
    public void testBadFile() 
    {
      doit(TestUtilities.getWsdlPath("java\\test\\addressbook\\wsifservice") +
           "AddressBok.wsdl","FileNotFoundException"); 
    }

    public void testImport() 
    { 
      if (TestUtilities.areWeTesting("remotewsdl")) 
        doit("http://localhost:8080/wsdl/ImportingAddressBook.wsdl",null); 
    }
      
    private void doit (String wsdl, String expectedException) 
    {
      try 
      {
      	WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
      	WSIFService service = factory.getService(
          wsdl,
          null,                                           // serviceNS
          null,                                           // serviceName
          "http://wsifservice.addressbook/", // portTypeNS
          "AddressBook");                                 // portTypeName

        AddressBook stub=(AddressBook)service.getStub(server+"Port", AddressBook.class);

        stub.addEntry(name1, addr1);
        Address resp1 = stub.getAddressFromName (name1);
        assertTrue(new AddressUtility(addr1).equals(resp1));
        assertTrue(expectedException==null);
        
  	  } 
  	  catch (Exception e) 
  	  {
  	  	if (expectedException==null) 
  	  	{
   	      System.err.println("WsdlLoadingTest("+wsdl+") caught exception " + e);
	      e.printStackTrace();
	      assertTrue(false);
  	  	}
  	  	else
  	  	{
	   	  StringWriter sw=new StringWriter();
  	      PrintWriter pw=new PrintWriter(sw);
     	  e.printStackTrace(pw);
  	      String stack=sw.getBuffer().toString();
  	      assertTrue(stack.indexOf(expectedException)>0);
  	  	}
  	  }
    }
    
}

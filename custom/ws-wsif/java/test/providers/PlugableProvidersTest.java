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

package providers;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.providers.soap.apacheaxis.WSIFDynamicProvider_ApacheAxis;
import org.apache.wsif.providers.soap.apachesoap.WSIFDynamicProvider_ApacheSOAP;
import org.apache.wsif.spi.WSIFProvider;
import org.apache.wsif.util.WSIFPluggableProviders;
import util.TestUtilities;

/**
 * Junit test to test the plugable providers facility for WSIFDynamicProviders
 */
public class PlugableProvidersTest extends TestCase {

    private static final Class DEFAULT_SOAP_PROVIDER = 
       TestUtilities.DEFAULT_SOAP_PROVIDER_CLASS;       
    private static final Class NON_DEFAULT_SOAP_PROVIDER = 
       TestUtilities.NON_DEFAULT_SOAP_PROVIDER_CLASS;       

    private static final String DEFAULT_PROVIDER_NAME =
       DEFAULT_SOAP_PROVIDER.getName(); 
    private static final String NON_DEFAULT_PROVIDER_NAME =
       NON_DEFAULT_SOAP_PROVIDER.getName(); 

    public PlugableProvidersTest(String name) {
    	 super(name);
    }
        
	public static void main(String[] args) {
		junit.textui.TestRunner.run (suite());
	}
		
	public static Test suite() {
		return new TestSuite(PlugableProvidersTest.class);
	}
	  
    public void setUp() {
       TestUtilities.setUpExtensionsAndProviders();
    }

    public void testEJBProvider() { 
    	reset();
    	assertTrue( "testEJBProvider doit failed!", doit("http://schemas.xmlsoap.org/wsdl/ejb/",
    	     "org.apache.wsif.providers.ejb.WSIFDynamicProvider_EJB") );
    }
        	     
    public void testJavaProvider() { 
    	reset();
    	assertTrue( "testJavaProvider doit failed!", doit("http://schemas.xmlsoap.org/wsdl/java/",
    	     "org.apache.wsif.providers.java.WSIFDynamicProvider_Java") );
    }
    	     
    public void testDefaultSoapProvider() { 
    	reset();

     	assertTrue( 
     	   "testDefaultSoapProvider doit failed!", 
     	   doit(
     	      "http://schemas.xmlsoap.org/wsdl/soap/",
    	      DEFAULT_PROVIDER_NAME ) );
    }
    	     
    public void testSetDefaultSoapProvider() { 
       reset();
       WSIFProvider p = null;
       try {
          p = (WSIFProvider)NON_DEFAULT_SOAP_PROVIDER.newInstance();
       } catch (Exception ex) {
          assertTrue( "exception instantiating non default provider: " + ex.getMessage(), false );
       }
       WSIFPluggableProviders.overrideDefaultProvider( 
          "http://schemas.xmlsoap.org/wsdl/soap/",
          p );
       assertTrue( 
          "testSetDefaultSoapProvider", 
          doit(
             "http://schemas.xmlsoap.org/wsdl/soap/",
    	     NON_DEFAULT_PROVIDER_NAME ) );
       // set null so uses default next time
       WSIFPluggableProviders.overrideDefaultProvider( 
          "http://schemas.xmlsoap.org/wsdl/soap/", null );

    }

    //public void testSoapRMIProvider() { 
    //	assertTrue( doit("http://schemas.xmlsoap.org/wsdl/soap/xxx",
    //	     "org.apache.wsif.providers.soap.soaprmi.WSIFDynamicProvider_SoapRMI") );
    //}
    	     
    public void testPPMethods() { 

    	reset();
    
       // test isProviderAvailable method
       assertTrue( "isProviderAvailable 1", 
          WSIFPluggableProviders.isProviderAvailable( 
             "http://schemas.xmlsoap.org/wsdl/soap/",
             "http://schemas.xmlsoap.org/wsdl/soap/") );
       assertTrue( "isProviderAvailable 2", 
          WSIFPluggableProviders.isProviderAvailable( 
             "http://schemas.xmlsoap.org/wsdl/soap/",
             "http://schemas.xmlsoap.org/wsdl/jms/") );
       assertTrue( "isProviderAvailable 3", 
          !WSIFPluggableProviders.isProviderAvailable( 
             "http://schemas.xmlsoap.org/wsdl/soap/",
             "http://schemas.xmlsoap.org/wsdl/xxx/") );
       assertTrue( "isProviderAvailable 4", 
          !WSIFPluggableProviders.isProviderAvailable( 
             "http://schemas.xmlsoap.org/wsdl/xxx/",
             "http://schemas.xmlsoap.org/wsdl/jms/") );
       assertTrue( "isProviderAvailable 5", 
          !WSIFPluggableProviders.isProviderAvailable( 
             "http://schemas.xmlsoap.org/wsdl/xxx/",
             "http://schemas.xmlsoap.org/wsdl/xxx/") );
       assertTrue( "isProviderAvailable 6", 
          WSIFPluggableProviders.isProviderAvailable( 
             "http://schemas.xmlsoap.org/wsdl/java/") );
       assertTrue( "isProviderAvailable 7", 
          !WSIFPluggableProviders.isProviderAvailable( 
             "http://schemas.xmlsoap.org/wsdl/xxx/") );

       // test the default SOAP provider
       WSIFProvider p1 = WSIFPluggableProviders.getProvider(
          "http://schemas.xmlsoap.org/wsdl/soap/" );
       Class c1 = p1.getClass();
       assertTrue( "defaultprovider 0", DEFAULT_SOAP_PROVIDER.equals( c1 ) );

       // test changing the default provider
       WSIFProvider p = null;
       try {
          p = (WSIFProvider)NON_DEFAULT_SOAP_PROVIDER.newInstance();
       } catch (Exception ex) {
          assertTrue( "exception instantiating non default provider: " + ex.getMessage(), false );
       }
       WSIFPluggableProviders.overrideDefaultProvider( 
          "http://schemas.xmlsoap.org/wsdl/soap/",
          p );
          
       WSIFProvider p2 = WSIFPluggableProviders.getProvider(
          "http://schemas.xmlsoap.org/wsdl/soap/" );
       Class c2 = p2.getClass();
       assertTrue( "defaultprovider 1", NON_DEFAULT_SOAP_PROVIDER.equals( c2 ) );

       WSIFPluggableProviders.overrideDefaultProvider( 
          "http://schemas.xmlsoap.org/wsdl/soap/",
          null );
       WSIFProvider p3 = WSIFPluggableProviders.getProvider(
          "http://schemas.xmlsoap.org/wsdl/soap/" );
       Class c3 = p3.getClass();
       assertTrue( "defaultprovider 2", c1.equals( c3 ) );
       assertTrue( "defaultprovider 3", p1 == p3 );

       WSIFPluggableProviders.overrideDefaultProvider( 
          "http://schemas.xmlsoap.org/wsdl/soap/",
          p1 );
       p2 = WSIFPluggableProviders.getProvider(
          "http://schemas.xmlsoap.org/wsdl/soap/" );
       c2 = p2.getClass();
       assertTrue( "defaultprovider 4", c1.equals( c2 ) );
       assertTrue( "defaultprovider 5", p1 == p2 );

       WSIFPluggableProviders.overrideDefaultProvider( 
          "http://schemas.xmlsoap.org/wsdl/soap/",
          null );
       p2 = WSIFPluggableProviders.getProvider(
          "http://schemas.xmlsoap.org/wsdl/soap/" );
       c2 = p2.getClass();
       assertTrue( "defaultprovider 6", c1.equals( c2 ) );
       assertTrue( "defaultprovider 7", p1 == p2 );
                 
       // test provider auto loading
       WSIFPluggableProviders.setAutoLoadProviders( false );
       assertTrue( "AutoLoadProviders 1", 
          !WSIFPluggableProviders.isAutoLoadProviders() );
       assertTrue( "AutoLoadProviders 2", 
          !WSIFPluggableProviders.isProviderAvailable( 
             "http://schemas.xmlsoap.org/wsdl/soap/",
             "http://schemas.xmlsoap.org/wsdl/soap/") );
       WSIFPluggableProviders.overrideDefaultProvider( 
          "http://schemas.xmlsoap.org/wsdl/soap/",
          new WSIFDynamicProvider_ApacheAxis() );
       assertTrue( "AutoLoadProviders 3", 
          WSIFPluggableProviders.isProviderAvailable( 
             "http://schemas.xmlsoap.org/wsdl/soap/",
             "http://schemas.xmlsoap.org/wsdl/soap/") );
       WSIFPluggableProviders.setAutoLoadProviders( true );
       assertTrue( "AutoLoadProviders 4", 
          WSIFPluggableProviders.isAutoLoadProviders() );
          
       assertTrue( "AutoLoadProviders 5", doit(
          "http://schemas.xmlsoap.org/wsdl/soap/",
          DEFAULT_PROVIDER_NAME ) );

    }
        	     
    private boolean doit (String namespaceURI, String providerClassName) {  
       WSIFProvider provider = WSIFPluggableProviders.getProvider( namespaceURI );
       String pn = provider.getClass().getName();
       return pn.equals( providerClassName );
    }
    
    private void reset() {
       // reset and clear loaded providers
       WSIFPluggableProviders.setAutoLoadProviders( false );
       WSIFPluggableProviders.setAutoLoadProviders( true );
    }

}






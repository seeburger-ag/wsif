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

package util;

/**
 * A mish-mash of various utilities useful in our JUnit tests
 * @author Mark Whitlock
 */

import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFException;
import org.apache.wsif.base.WSIFDefaultCorrelationService;
import org.apache.wsif.providers.soap.apacheaxis.WSIFDynamicProvider_ApacheAxis;
import org.apache.wsif.providers.soap.apachesoap.WSIFDynamicProvider_ApacheSOAP;
import org.apache.wsif.spi.WSIFProvider;
import org.apache.wsif.util.WSIFCorrelationServiceLocator;
import org.apache.wsif.util.WSIFPluggableProviders;
import org.apache.wsif.util.WSIFProperties;
import org.apache.wsif.util.jms.JMSAsyncListener;
import org.apache.wsif.util.jms.NativeJMSRequestListener;
import org.apache.wsif.util.jms.WSIFJMSFinder;
import org.apache.wsif.util.jms.WSIFJMSFinderForJndi;

public class TestUtilities {
    private static final String WSIF_TEST_PROPERTIES = "wsif.test.properties";
    private static final String WSIF_PATH = "wsif.path";
    private static final String WSIF_TEST_COMPONENTS = "wsif.test.components";
    private static final String WSIF_SOAP_SERVER = "wsif.soapserver";

    private static final String SOAP = "soap";
    private static final String AXIS = "axis";

    private static BridgeThread jmsAb = null;
    private static BridgeThread jmsSq = null;
    private static JMSAsyncListener asyncListener = null; 
    private static NativeJMSRequestListener nativeReqListener = null;

    public static final int ADDRESSBOOK_LISTENER = 1;
    public static final int STOCKQUOTE_LISTENER = 2;
    public static final int ASYNC_LISTENER = 4;
    public static final int NATIVEJMS_LISTENER = 8;
    public static final int INOUT_LISTENER = 16;
    public static final int MIME_LISTENER = 32;
    // ALL_LISTENERS doesn't include the INOUT_LISTENER because 
    // inout and addressbook both listen on the same queue.
    public static final int ALL_LISTENERS = 15;

    public static final Class DEFAULT_SOAP_PROVIDER_CLASS = 
       WSIFDynamicProvider_ApacheAxis.class;       
    private static final String DEFAULT_SOAP_PROTOCOL =
       "axis";
    public static final Class NON_DEFAULT_SOAP_PROVIDER_CLASS = 
       WSIFDynamicProvider_ApacheSOAP.class;       
    private static final String NON_DEFAULT_SOAP_PROTOCOL =
       "soap";
/*
    public static final Class DEFAULT_SOAP_PROVIDER_CLASS = 
       WSIFDynamicProvider_ApacheSOAP.class;       
    private static final String DEFAULT_SOAP_PROTOCOL =
       "soap";
    public static final Class NON_DEFAULT_SOAP_PROVIDER_CLASS = 
       WSIFDynamicProvider_ApacheAxis.class;       
    private static final String NON_DEFAULT_SOAP_PROTOCOL =
       "axis";
*/
    public static String getWsdlPath(String relativePath) {
        String wsdlPath = getWsifProperty(WSIF_PATH);
        if (wsdlPath == null)
            wsdlPath = new String("");

        String slash = System.getProperty("file.separator");

        if (!wsdlPath.equals("") && !wsdlPath.endsWith(slash))
            wsdlPath += slash;

        if (relativePath != null && relativePath.length() != 0) {
            StringBuffer relativeBuff = new StringBuffer(relativePath);
            for (int i = 0; i < relativeBuff.length(); i++) {
                if (relativeBuff.charAt(i) == '\\' || relativeBuff.charAt(i) == '/')
                    relativeBuff.setCharAt(i, slash.charAt(0));
            }

            wsdlPath += relativeBuff;

            if (!wsdlPath.endsWith(slash))
                wsdlPath += slash;
        }
        return wsdlPath;
    }

    /**
     * Gets a property from the wsif.test.properties file
     */
    public static String getWsifProperty(String property) {
        try {
            Properties prop = new Properties();
            InputStream in = ClassLoader.getSystemResourceAsStream(WSIF_TEST_PROPERTIES);
            prop.load(in);
            return prop.getProperty(property);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Finds out from the wsif.test.properties file whether we are testing
     * a particular component (EJBs, JMS, etc) of WSIF. If not mentioned
     * in wsif.test.properties, the default is to test everything.
     */
    public static boolean areWeTesting(String component) {
        String prop = getWsifProperty(WSIF_TEST_COMPONENTS);
        if (prop == null)
            return true; // test everything by default

        StringTokenizer tokenizer = new StringTokenizer(prop, ",");
        String token;
        int eqPos;
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            eqPos = token.indexOf('=');
            if (eqPos == -1)
                continue;
            if (token.substring(0, eqPos).equals(component)
                && token.substring(eqPos + 1).equals("off"))
                return false;
        }
        return true;
    }

    /**
     * Queries the soap server being used. Can be either Apache Soap or Apache Axis.
     * Default is soap.
     */
    public static String getSoapServer() {
        String prop = getWsifProperty(WSIF_SOAP_SERVER);
        if (prop == null)
            return SOAP;
        if (SOAP.equals(prop) || AXIS.equals(prop))
            return prop;
        return SOAP;
    }

    public static boolean isJmsVerbose() {
        String strVerbose = TestUtilities.getWsifProperty("wsif.jms.output");
        if ("verbose".equals(strVerbose))
            return true;
        return false;
    }

    public static void setProviderForProtocol(String protocol) { 
       if ( protocol.equals( NON_DEFAULT_SOAP_PROTOCOL ) ) {
       	  try {
             WSIFPluggableProviders.overrideDefaultProvider(
                "http://schemas.xmlsoap.org/wsdl/soap/", 
                (WSIFProvider) NON_DEFAULT_SOAP_PROVIDER_CLASS.newInstance() );
       	  } catch (Exception ex) {
       	     ex.printStackTrace();
       	  }
       } else {
          resetDefaultProviders();
       }
    }

    public static void resetDefaultProviders() { 
       WSIFPluggableProviders.setAutoLoadProviders( false );
       WSIFPluggableProviders.setAutoLoadProviders( true );
    }    

    public static void setUpExtensionsAndProviders() {
        //WSIFServiceImpl.addExtensionRegistry(new JavaExtensionRegistry()) ;    
        //WSIFServiceImpl.addExtensionRegistry(new FormatExtensionRegistry()) ;        
        //WSIFServiceImpl.addExtensionRegistry(new JmsExtensionRegistry()) ;
        //WSIFServiceImpl.addExtensionRegistry(new EJBExtensionRegistry());
        resetDefaultProviders();
        System.setProperty(WSIFConstants.WSIF_MAPPINGCONVENTION_PROPERTY, "org.apache.wsif.providers.soap.apacheaxis.WSDL2JavaMappingConvention");
        System.setProperty(WSIFConstants.WSIF_MAPPER_PROPERTY, "org.apache.wsif.providers.soap.apacheaxis.WSDL2JavaMapper");
    }
    /**
     * This starts what listeners are required to run the testcases.
     * What listeners are needed depends on settings in the 
     * wsif.test.properties file.
     * Possible listeners are:
     *    JMS2HTTPBridge - for SOAP/JMS tests
     *    NativeJMSRequestListener - for the native JMS provider 
     *    JMSAsynListener - for asynchronous operation tests
     */
    public static void startListeners() {
        startListeners(ALL_LISTENERS);
    }

    public static void startListeners(int which) {
        if (TestUtilities.areWeTesting("jms")) {
            if ((which & ADDRESSBOOK_LISTENER) > 0) {
                jmsAb = new BridgeThread("AddressBook");
                jmsAb.start();
            }

            if ((which & STOCKQUOTE_LISTENER) > 0) {
                jmsSq = new BridgeThread("Stockquote");
                jmsSq.start();
            }

            if ((which & INOUT_LISTENER) > 0) {
                jmsAb = new BridgeThread("AddressBook", "Inout");
                jmsAb.start();
            }

            if ((which & MIME_LISTENER) > 0) {
                jmsAb = new BridgeThread("AddressBook", "Mime");
                jmsAb.start();
            }

            if ((which & NATIVEJMS_LISTENER) > 0)
                try {
                    nativeReqListener =
                        new NativeJMSRequestListener(
                            TestUtilities.getWsifProperty(
                                "wsif.nativejms.requestq"));
                } catch (WSIFException ex) {
                    ex.printStackTrace();
                }
        }

        if ((which & ASYNC_LISTENER) > 0
            && TestUtilities.areWeTesting("async"))
            try {
                asyncListener =
                    new JMSAsyncListener(
                        TestUtilities.getWsifProperty("wsif.async.replytoq"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
    }

    /**
     * This starts whatever listeners have been started.
     * Possible listeners are:
     *    JMS2HTTPBridge - for SOAP/JMS tests
     *    NativeJMSRequestListener - for the native JMS provider 
     *    JMSAsynListener - for asynchronous operation tests
     */
    public static void stopListeners() 
    {
       if ( jmsAb != null ) jmsAb.interrupt();
       if ( jmsSq != null ) jmsSq.interrupt();
       if ( asyncListener != null ) asyncListener.stop();
       if ( nativeReqListener != null ) nativeReqListener.stop();
       ((WSIFDefaultCorrelationService)WSIFCorrelationServiceLocator.
          getCorrelationService()).shutdown();
    }

    public static Message getJMSAsyncResponse(String id, String queueName)
        throws WSIFException, JMSException {

        WSIFJMSFinder finder =
            new WSIFJMSFinderForJndi(
                null,
                TestUtilities.getWsifProperty(
                        "wsif.jms2httpbridge.initialcontextfactory"),
                TestUtilities.getWsifProperty(
                        "wsif.jms2httpbridge.jndiproviderurl"),
                WSIFJMSFinder.STYLE_QUEUE,
                TestUtilities.getWsifProperty(
                        "wsif.jms2httpbridge.jndiconnectionfactoryname"),
                null,
                null);

        QueueConnectionFactory factory = finder.getFactory();
        QueueConnection connection = factory.createQueueConnection();
        connection.start();
        QueueSession session =
            connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue readQ = session.createQueue(queueName);
        QueueReceiver receiver =
            session.createReceiver(readQ, "JMSCorrelationID='" + id + "'");
        return receiver.receive(WSIFProperties.getAsyncTimeout());
    }
}

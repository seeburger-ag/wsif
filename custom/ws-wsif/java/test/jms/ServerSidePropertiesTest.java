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

package jms;

import java.util.Iterator;
import java.util.Map;

import javax.jms.Message;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFResponse;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.base.WSIFDefaultMessage;
import org.apache.wsif.providers.jms.JMSFormatter;
import org.apache.wsif.providers.jms.WSIFOperation_Jms;
import org.apache.wsif.providers.jms.WSIFPort_Jms;
import org.apache.wsif.util.jms.WSIFJMSDestination;
import org.apache.wsif.wsdl.extensions.jms.JMSConstants;
import util.TestUtilities;

/**
 * Junit test for setting JMS Message properties on the server side
 * 
 * @author Ant Elder <ant.elder@uk.ibm.com>
 */
public class ServerSidePropertiesTest extends TestCase {
    String wsdlLocation = TestUtilities.getWsdlPath("java\\test\\jms") + "jms.wsdl";

    public ServerSidePropertiesTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(ServerSidePropertiesTest.class);
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
    }

    public void testFormatResponse() {

        try {

            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory.getService(
                    wsdlLocation,
                    null,
                    null,
                    "http://wsifservice.addressbook/",
                    "AddressBook");

            WSIFPort_Jms wsifPort =
                (WSIFPort_Jms) service.getPort("SSOutProperties");

            Definition def = wsifPort.getDefinition();
            Map services = def.getServices();
            QName qn = (QName) services.keySet().iterator().next();
            Service s = (Service) services.get(qn);
            Port port = s.getPort("SSOutProperties");

            JMSFormatter formatter = new JMSFormatter(def, port);
            WSIFResponse wr = new WSIFResponse(null);
            wr.setOperationName("getAddressFromName");
            wr.setInputName("GetAddressFromNameRequest");
            wr.setOutputName("GetAddressFromNameResponse");


            WSIFJMSDestination dest = wsifPort.getJmsDestination();

            Message jmsMsg =
                dest.createMessage(JMSConstants.MESSAGE_TYPE_OBJECTMESSAGE);

            formatter.formatResponse(wr, jmsMsg);

            String value1 = "petra";
            String value2 = "ant";
            WSIFMessage wsifMsg = new WSIFDefaultMessage();
            wsifMsg.setObjectPart("userOutPropPart1", value1);
            wsifMsg.setObjectPart("userOutPropPart2", value2);
            
            wsifMsg.setObjectPart("realPart", "realValue");

            WSIFOperation_Jms.setJMSMessageOutputHeaderProperties(
                jmsMsg,
                wsifMsg,
                formatter);

            String x = jmsMsg.getStringProperty("userOutProperty1");
            assertTrue("property not correctly set!!", value1.equals(x));
            x = jmsMsg.getStringProperty("userOutProperty2");
            assertTrue("property not correctly set!!", value2.equals(x));

            Iterator i = wsifMsg.getPartNames();
            assertTrue("lost realpart from wsifMsg!!", i.hasNext());

            String partName = (String) i.next();            
            assertTrue("realpart name wrong in wsifMsg!!", "realPart".equals(partName));

            String partValue = (String) wsifMsg.getObjectPart(partName);            
            assertTrue("realpart value wrong in wsifMsg!!", "realValue".equals(partValue));

            assertTrue("extra parts in wsifMsg!!", !i.hasNext());

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("got Exception: " + e.getLocalizedMessage(), false);
        }

    }

}

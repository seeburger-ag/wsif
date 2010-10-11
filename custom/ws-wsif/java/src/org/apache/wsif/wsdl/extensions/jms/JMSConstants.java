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

package org.apache.wsif.wsdl.extensions.jms;

import javax.xml.namespace.QName;

import com.ibm.wsdl.Constants;

/**
 * WSDL Jms extension
 * 
 * @author <a href="mailto:ake@de.ibm.com">Hermann Akermann</a>
 * @author Ant Elder <antelder@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class JMSConstants {

    // Namespace URIs. 
    public static final String NS_URI_JMS = "http://schemas.xmlsoap.org/wsdl/jms/";
    public static final String NS_URI_SOAPJMS = "http://schemas.xmlsoap.org/soap/jms";

    // Element names.
    public static final String ELEM_ADDRESS = "address";
    public static final String ELEM_PROPERTY = "property";
    public static final String ELEM_PROPERTY_VALUE = "propertyValue";
    public static final String ELEM_INPUT = "input";
    public static final String ELEM_OUTPUT = "output";
    public static final String ELEM_FAULT = "fault";
    public static final String ELEM_FAULT_INDICATOR = "faultIndicator";
    public static final String ELEM_FAULT_PROPERTY = "faultProperty";
    public static final String ATTR_MESSAGE_TYPE = "type";

    // managed environment elements
    public static final String ELEM_JMS_JMS_VENDOR_URI = "jmsVendorURI";
    public static final String ELEM_JMS_INIT_CXT_FACT = "initialContextFactory";
    public static final String ELEM_JMS_JNDI_PROV_URL = "jndiProviderURL";
    public static final String ELEM_JMS_DEST_STYLE = "destinationStyle";
    public static final String ELEM_JMS_JNDI_CONN_FACT_NAME =
        "jndiConnectionFactoryName";
        
    public static final String ELEM_JMS_JNDI_DEST_NAME = "jndiDestinationName";
    public static final String ELEM_JMS_JMS_PROV_DEST_NAME =
        "jmsProviderDestinationName";
        
    public static final String ELEM_JMS_JMS_IMPL_SPEC_URI =
        "jmsImplementationSpecificURI";
        
    public static final String ELEM_JMS_QUEUE = "queue";
    public static final String ELEM_JMS_TOPIC = "topic";

    // soap:address JMS URL property names
    public static final String JMS_URL_PROTOCOL = "jms:";
    public static final String JMS_URL_QUEUE = "/queue";
    public static final String JMS_URL_TOPIC = "/topic";
    public static final char JMS_URL_QUERY_CHAR = '?';
    public static final String JMS_URL_QUERY_SEPERATOR1 = "&";
    public static final String JMS_URL_QUERY_SEPERATOR2 = "|";
    public static final String JMS_URL_DESTINATION = "destination";
    public static final String JMS_URL_CONNECTION_FACTORY = "connectionFactory";
    public static final String JMS_URL_INITIAL_CONTEXT_FACTORY = "initialContextFactory";
    public static final String JMS_URL_PROVIDER_URL = "jndiProviderURL";
    public static final String JMS_URL_DELIVERY_MODE = "deliveryMode";
    public static final String JMS_URL_TIME_TO_LIVE = "timeToLive";
    public static final String JMS_URL_PRIORITY = "priority";
    public static final String JMS_URL_USERID = "userid";
    public static final String JMS_URL_PASSWORD = "password";
    
    // Jms Header, HeaderValue, Property, PropertyValue extension	
    public static final String ATTR_PART = "part";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_VALUE = "value";

    // Jms Header Field types
    public static final int HEADER_TYPE_JMSMESSAGEID =      0;
    public static final int HEADER_TYPE_JMSTIMESTAMP =     10;
    public static final int HEADER_TYPE_JMSCORRELATIONID = 20;
    public static final int HEADER_TYPE_JMSREPLYTO =       30;
    public static final int HEADER_TYPE_JMSDESTINATION =   40;
    public static final int HEADER_TYPE_JMSDELIVERYMODE =  50;
    public static final int HEADER_TYPE_JMSREDELIVERED =   60;
    public static final int HEADER_TYPE_JMSTYPE =          70;
    public static final int HEADER_TYPE_JMSEXPIRATION =    80;
    public static final int HEADER_TYPE_JMSPRIORITY =      90;

    // Jms Input/Output extension	
    public static final String ATTR_PARTS = "parts";
    public static final String ATTR_SCHEMA = "schema";

    // Qualified element names.
    public static final QName Q_ELEM_JMS_ADDRESS =
        new QName(NS_URI_JMS, ELEM_ADDRESS);
        
    public static final QName Q_ELEM_JMS_BINDING =
        new QName(NS_URI_JMS, Constants.ELEM_BINDING);
        
    public static final QName Q_ELEM_JMS_PROPERTY =
        new QName(NS_URI_JMS, ELEM_PROPERTY);
        
    public static final QName Q_ELEM_JMS_PROPERTY_VALUE =
        new QName(NS_URI_JMS, ELEM_PROPERTY_VALUE);
        
    public static final QName Q_ELEM_JMS_OPERATION =
        new QName(NS_URI_JMS, Constants.ELEM_OPERATION);
        
    public static final QName Q_ELEM_JMS_INPUT =
        new QName(NS_URI_JMS, ELEM_INPUT);
        
    public static final QName Q_ELEM_JMS_OUTPUT =
        new QName(NS_URI_JMS, ELEM_OUTPUT);

    public static final QName Q_ELEM_JMS_FAULT =
        new QName(NS_URI_JMS, ELEM_FAULT);

    public static final QName Q_ELEM_JMS_FAULT_INDICATOR =
        new QName(NS_URI_JMS, ELEM_FAULT_INDICATOR);

    public static final QName Q_ELEM_JMS_FAULT_PROPERTY =
        new QName(NS_URI_JMS, ELEM_FAULT_PROPERTY);

    // Jms message (body) types
    public static final int MESSAGE_TYPE_NOTSET = 0;
    public static final int MESSAGE_TYPE_BYTEMESSAGE = 10;
    public static final int MESSAGE_TYPE_MAPMESSAGE = 20;
    public static final int MESSAGE_TYPE_OBJECTMESSAGE = 30;
    public static final int MESSAGE_TYPE_STREAMMESSAGE = 40;
    public static final int MESSAGE_TYPE_TEXTMESSAGE = 50;
}
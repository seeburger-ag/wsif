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

package org.apache.wsif;

/**
 * Simple class to store constants used by WSIF
 * @author Owen Burroughs <owenb@apache.org>
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class WSIFConstants {

    /**
     * WSIF Property file name
     */
    public static final String WSIF_PROPERTIES = "wsif.properties";

    /**
     *  WSIF property for pluggable provider defaults
     */
    public static final String WSIF_PROP_PROVIDER_PFX1 =
        "wsif.provider.default.";

    /**
     *  WSIF property for pluggable provider defaults
     */
    public static final String WSIF_PROP_PROVIDER_PFX2 = "wsif.provider.uri.";

    /**
     *  WSIF property for asynchronous requests
     */
    public static final String WSIF_PROP_ASYNC_TIMEOUT =
        "wsif.asyncrequest.timeout";

    /**
     *  WSIF property for asynchronous requests
     */
    public static final String WSIF_PROP_ASYNC_USING_MDB =
        "wsif.async.listener.mdb";

    /**
     * WSIFDefaultCorrelationService timeout check delay
     * in milliseconds. Default is 5 seconds  
     */
    public static final int CORRELATION_TIMEOUT_DELAY = 5000; 

    /**
     *  WSIF properties for synchronous requests
     */
    public static final String WSIF_PROP_SYNC_TIMEOUT =
        "wsif.syncrequest.timeout";

    /**
     *  WSIF property for unreferenced attachments
     */
    public static final String WSIF_PROP_UNREFERENCED_ATTACHMENTS =
        "wsif.unreferencedattachments";

    /**
     *  WSIFCorelationService registered JNDI name
     */
    public static final String CORRELATION_SERVICE_NAMESPACE =
        "wsif/WSIFCorrelationService";

    /**
     *  WSIF context part name for HTTP basic authentication userid  
     */
    public static final String CONTEXT_HTTP_USER =
        "org.apache.wsif.http.UserName";

    /**
     *  WSIF context part name for HTTP basic authentication userid  
     */
    public static final String CONTEXT_HTTP_PSWD =
        "org.apache.wsif.http.Password";

    /**
     *  WSIF context part name for proxy userid  
     */
    public static final String CONTEXT_HTTP_PROXY_USER =
        "org.apache.wsif.http.proxy.UserName";

    /**
     *  WSIF context part name for proxy password  
     */
    public static final String CONTEXT_HTTP_PROXY_PSWD =
        "org.apache.wsif.http.proxy.Password";

    /**
     *  WSIF context part name for SOAP headers
     * @deprecated use CONTEXT_REQUEST_SOAP_HEADERS   
     */
    public static final String CONTEXT_SOAP_HEADERS =
        "org.apache.wsif.soap.RequestHeaders";

    /**
     *  WSIF context part name for HTTP headers  
     */
    public static final String CONTEXT_REQUEST_HTTP_HEADERS =
        "org.apache.wsif.http.RequestHeaders";

    /**
     *  WSIF context part name for HTTP headers  
     */
    public static final String CONTEXT_RESPONSE_HTTP_HEADERS =
        "org.apache.wsif.http.ResponseHeaders";

    /**
     *  WSIF context part name for SOAP headers  
     */
    public static final String CONTEXT_REQUEST_SOAP_HEADERS =
        "org.apache.wsif.soap.RequestHeaders";

    /**
     *  WSIF context part name for SOAP headers  
     */
    public static final String CONTEXT_RESPONSE_SOAP_HEADERS =
        "org.apache.wsif.soap.ResponseHeaders";

    /**
     *  WSIF context part name prefix for JMSProperties
     */
    public static final String CONTEXT_JMS_PREFIX = "JMSProperty.";

    /**
     *  WSIF context part name for the AXIS operation style  
     */
    public static final String CONTEXT_OPERATION_STYLE =
        "org.apache.wsif.axis.operationStyle";

    /**
     *  WSIF context value for AXIS document operation style  
     */
    public static final String CONTEXT_OPERATION_STYLE_DOCUMENT =
        "document";

    /**
     *  WSIF context value for AXIS wrapped operation style  
     */
    public static final String CONTEXT_OPERATION_STYLE_WRAPPED =
        "wrapped";

    /**
     *  WSIF context value for AXIS unwrapped operation style  
     */
    public static final String CONTEXT_OPERATION_STYLE_UNWRAPPED =
        "unwrapped";

    /**
     *  WSIF context value for AXIS message operation style  
     */
    public static final String CONTEXT_OPERATION_STYLE_MESSAGE =
        "message";

    /**
     *  WSIF context part name for the list of schema types
     */
    public static final String CONTEXT_SCHEMA_TYPES =
        "org.apache.wsif.schematypes";
        
    /**
     * 
     */
    public static final String CONTEXT_REQUEST_UNREFERENCED_ATTACHMENT_PARTS = 
        "org.apache.wsif.attachments.request.unreferenced_attachment_parts";

    /**
     * 
     */
    public static final String CONTEXT_RESPONSE_UNREFERENCED_ATTACHMENT_PARTS = 
        "org.apache.wsif.attachments.response.unreferenced_attachment_parts";

    /**
     *  SOAP faults WSIFMessage part name for the fault code
     */
    public static final String SOAP_FAULT_MSG_NAME =
        "org.apache.wsif.soap.fault";

    /**
     *  SOAP faults WSIFMessage part name for the fault code
     */
    public static final String SOAP_FAULT_CODE =
        "org.apache.wsif.soap.fault.code";

    /**
     *  SOAP faults WSIFMessage part name for the fault string
     */
    public static final String SOAP_FAULT_STRING =
        "org.apache.wsif.soap.fault.string";

    /**
     *  SOAP faults WSIFMessage part name for the fault actor
     */
    public static final String SOAP_FAULT_ACTOR =
        "org.apache.wsif.soap.fault.actor";

    /**
     *  SOAP faults WSIFMessage part name for the fault object
     */
    public static final String SOAP_FAULT_OBJECT =
        "org.apache.wsif.soap.fault.object";

    /**
     *  WSDLFactory property name
     */
    public static final String WSDLFACTORY_PROPERTY_NAME =
        "javax.wsdl.factory.WSDLFactory";

    /**
     *  WSIF implemetation of WSDLfactory
     */
    public static final String WSIF_WSDLFACTORY =
        "org.apache.wsif.wsdl.WSIFWSDLFactoryImpl";

    /**
     *  JMS provider JMS property containing the operation name
     */
    public static final String JMS_PROP_OPERATION_NAME =
        "WSDLOperation";

    /**
     *  JMS provider JMS property containing the input message name
     */
    public static final String JMS_PROP_INPUT_NAME =
        "WSDLInput";

    /**
     *  JMS provider JMS property containing the output message name
     */
    public static final String JMS_PROP_OUTPUT_NAME =
        "WSDLOutput";

    public static final String NS_URI_1999_SCHEMA_XSD = 
        "http://www.w3.org/1999/XMLSchema";
        
    public static final String NS_URI_2000_SCHEMA_XSD = 
        "http://www.w3.org/2000/10/XMLSchema";
        
    public static final String NS_URI_2001_SCHEMA_XSD = 
        "http://www.w3.org/2001/XMLSchema";
        
    public static final String NS_URI_SOAP_ENC = 
        "http://schemas.xmlsoap.org/soap/encoding/";
        
    public static final String NS_URI_LITERAL_XML = 
        "http://xml.apache.org/xml-soap/literalxml";
        
	public static final String NS_URI_WSDL = 
        "http://schemas.xmlsoap.org/wsdl/";

	public static final String NS_URI_APACHE_SOAP = 
        "http://xml.apache.org/xml-soap";
        
    /**
     * Property name that specifies a WSIFMapper implementation class. This property can be
     * set as a System property or in the wsif,properties file.
     * @see org.apache.wsif.mapping.WSIFMapperFactory
     */
	public static final String WSIF_MAPPER_PROPERTY = "org.apache.wsif.mapper";

    /**
     * Property name that specifies a WSIFMappingConvention implementation class. This property can be
     * set as a System property or in the wsif,properties file.
     * @see org.apache.wsif.mapping.WSIFMappingConventionFactory
     */	
	public static final String WSIF_MAPPINGCONVENTION_PROPERTY = "org.apache.wsif.mappingconvention";
	
    /**
     * Feature name for service caching. The value of this feature should be a <code>java.lang.Boolean</code>
     * object<br><br>
     * Setting this feature as <code>true</code> will cause the WSIFServiceFactory to store shallow 
     * copies of any WSIFService instances it creates and to reuse these whenever possible rather than 
     * creating new WSIFService instances. This can provide a performance improvement if you are creating
     * and using the same WSIFServices multiple times. If this feature is not set, service caching will be
     * off.
     */	
	public static final String WSIF_FEATURE_SERVICE_CACHING = "org.apache.wsif.servicecaching";

    /**
     * Feature name for service cache size. The value of this feature should be a 
     * <code>java.lang.Integer</code> object<br><br>
     * This feature is used in conjunction with the service caching feature. It sets the number of
     * instances of WSIFService to be cached by the WSIFServiceFactory. When this number is reached
     * the oldest entries in the cache will be purged.
     */	
	public static final String WSIF_FEATURE_SERVICE_CACHE_SIZE = "org.apache.wsif.servicecachesize";

    /**
     * Feature name for automatic mapping of types. The value of this feature should be a 
     * <code>java.lang.Boolean</code> object<br><br>
     * Setting this feature as <code>true</code> will cause instances of WSIFService created by this factory,
     * to attempt to automatically calculate mappings between xml names and Java class names for the
	 * types in the wsdl. If this feature is not set, automatic mapping of type will be off.
     */	
	public static final String WSIF_FEATURE_AUTO_MAP_TYPES = "org.apache.wsif.automaptypes";

    /**
     * Feature name for the name of a WSIFMapper class to be used. The value for this feature should be
     * a String giving the name of the WSIFMapper implementation class to be used by all WSIFServices
     * created by the factory. Setting this feature will override the use of the org.apache.wsif.mapper
     * property for WSIFServices created by the factory.
     */	
	public static final String WSIF_FEATURE_MAPPER_CLASS = "org.apache.wsif.mapper";

    /**
     * Feature name for the name of a WSIFMappingConvention class to be used. The value for this feature 
     * should be a String giving the name of the WSIFMappingConvention implementation class to be used by 
     * all WSIFServices created by the factory. Setting this feature will override the use of the 
     * org.apache.wsif.mappingconvention property for WSIFServices created by the factory.
     */	
	public static final String WSIF_FEATURE_MAPPINGCONVENTION_CLASS = "org.apache.wsif.mappingconvention";

    /**
     * Feature name for a authenticating proxy username and password to be used when reading wsdl files and
     * parsing schemas. The value for this feature should be a <code>java.net.PasswordAuthentication</code> 
     * object which encapsultes the username and password.
     */	
	public static final String WSIF_FEATURE_PROXY_AUTHENTICATION = "org.apache.wsif.proxyauthentication";								                        
}

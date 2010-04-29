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

package org.apache.wsif.providers.soap.apacheaxis;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.wsif.wsdl.extensions.jms.JMSConstants;

/**
 * Various constants used by the WSIF AXIS provider classes
 * 
 * @author Ant Elder <ant.elder@uk.ibm.com>
 */
public class WSIFAXISConstants {

	public static final String SOAP_BINDING_NAMESPACE =
		"http://schemas.xmlsoap.org/wsdl/soap/";

	public static final String HTTP_TRANSPORT_URI =
		"http://schemas.xmlsoap.org/soap/http";

    public static final String JMS_NS_URI = JMSConstants.NS_URI_JMS;

	public static final String JMS_TRANSPORT_URI = JMSConstants.NS_URI_SOAPJMS;

	public static final ArrayList VALID_TRANSPORTS =
		new ArrayList(
			Arrays.asList(
				new String[] { HTTP_TRANSPORT_URI, JMS_TRANSPORT_URI }));

	public static final String CLASS_IN_AXIS_JAR = "org.apache.axis.AxisEngine";

	public static final String CLASS_IN_JMS_JAR = "javax.jms.Queue";

	public static final String DEFAULT_SOAP_ENCODING_URI =
		"http://schemas.xmlsoap.org/soap/encoding/";

	public static final String STYLE_RPC = "rpc";
	public static final String STYLE_DOCUMENT = "document";
	public static final ArrayList VALID_STYLES =
		new ArrayList(
			Arrays.asList(new String[] { STYLE_RPC, STYLE_DOCUMENT }));

	public static final String USE_ENCODED = "encoded";
	public static final String USE_LITERAL = "literal";
	public static final ArrayList VALID_USES =
		new ArrayList(
			Arrays.asList(new String[] { USE_ENCODED, USE_LITERAL }));

	public static final String AXIS_STYLE_MESSAGE = "message";
	public static final String AXIS_STYLE_WRAPPED = "wrapped";

    /**
     *  WSIF context part name for any default type serializers
     *  The context value should be an ArrayList of TypeSerializer objects  
     */
    public static final String CONTEXT_DEFAULT_SOAP_TYPE_SERIALIZERS =
        "org.apache.wsif.axis.default.type.serializers";

    /**
     *  WSIF context part name to override default (de)serializers for a type  
     *  The context value should be an ArrayList of TypeSerializer objects  
     */
    public static final String CONTEXT_SOAP_TYPE_SERIALIZERS =
        "org.apache.wsif.axis.type.serializers";

}

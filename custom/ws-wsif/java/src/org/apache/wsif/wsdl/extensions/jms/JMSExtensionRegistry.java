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

import javax.wsdl.extensions.ExtensionRegistry;
import org.apache.wsif.logging.Trc;

/**
 * WSDL Jms extension
 * 
 * @author <a href="mailto:ake@de.ibm.com">Hermann Akermann</a>
 * @author Ant Elder <antelder@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class JMSExtensionRegistry extends ExtensionRegistry {
	private static final long serialVersionUID = 1L;

    public JMSExtensionRegistry() {
        super();
        Trc.entry(this);

        // Register JMS Binding Serializer
        new JMSAddressSerializer().registerSerializer(this);

        // Register JMS Binding Serializer
        new JMSBindingSerializer().registerSerializer(this);

        // Register JMS Operation Serializer
        new JMSOperationSerializer().registerSerializer(this);

        // Register JMS Input Serializer
        new JMSInputSerializer().registerSerializer(this);

        // Register JMS Output Serializer
        new JMSOutputSerializer().registerSerializer(this);

        // Register JMS Fault Serializer
        new JMSFaultSerializer().registerSerializer(this);

        // Register JMS Fault Indicator Serializer
        new JMSFaultIndicatorSerializer().registerSerializer(this);

        // Register JMS Fault Property Serializer
        new JMSFaultPropertySerializer().registerSerializer(this);

        // Register JMS Property Serializer
        new JMSPropertySerializer().registerSerializer(this);

        // Register JMS Property Value Serializer
        new JMSPropertyValueSerializer().registerSerializer(this);

        Trc.exit();
    }
}
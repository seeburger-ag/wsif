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

package org.apache.wsif.wsdl.extensions;

import javax.wsdl.Port;
import javax.wsdl.extensions.ExtensionRegistry;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.wsdl.extensions.instance.InstanceConstants;
import org.apache.wsif.wsdl.extensions.instance.InstanceEstablishmentSerializer;

/**
 * This class extends ExtensionRegistry that pre-registers
 * serializers/deserializers for the Instance WSDL extensions:<ul>
 *
 * @see javax.wsdl.extensions.ExtensionRegistry
 * @author Aleksander Slominski
 * @author Matthew J. Duftler (duftler@us.ibm.com)
 * @author Ant Elder <antelder@apache.org>
 * @author Owen Burroughs <owenb@pache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class PopulatedInstanceExtensionRegistry extends ExtensionRegistry {
	private static final long serialVersionUID = 1L;
    
    public PopulatedInstanceExtensionRegistry() {
        super();
        Trc.entry(this);

        InstanceEstablishmentSerializer instanceEstablishmentSerializer =
            new InstanceEstablishmentSerializer();

        registerSerializer(
            Port.class,
            InstanceConstants.Q_ELEM_ESTABLISHMENT,
            instanceEstablishmentSerializer);
        registerDeserializer(
            Port.class,
            InstanceConstants.Q_ELEM_ESTABLISHMENT,
            instanceEstablishmentSerializer);

        Trc.exit();
    }
}
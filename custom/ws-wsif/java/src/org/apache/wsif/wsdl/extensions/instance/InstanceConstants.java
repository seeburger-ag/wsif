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

package org.apache.wsif.wsdl.extensions.instance;

import javax.xml.namespace.QName;

/**
 * @author Aleksander Slominski
 * @author Ant Elder <antelder@apache.org>
 */
public class InstanceConstants {
    
    // Namespace URIs.
    public static final String NS_URI_INSTANCE =
        "http://schemas.xmlsoap.org/wsdl/instance/";

    // Element names.
    public static final String ELEM_ESTABLISHMENT = "establishment";

    // Qualified element names.
    public static final QName Q_ELEM_ESTABLISHMENT =
        new QName(NS_URI_INSTANCE, ELEM_ESTABLISHMENT);

    // Attribute names.
    //public static final String ATTR_ARCHIVE = "archive";
    public static final String ATTR_OPERATION = "operation";
}
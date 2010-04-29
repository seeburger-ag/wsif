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

package org.apache.wsif.wsdl.extensions.format;

import javax.xml.namespace.QName;

/** 
 * @author Ant Elder <antelder@apache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class FormatBindingConstants {
    
    // Namespace URIs.
    public static final String NS_URI_FORMAT =
        "http://schemas.xmlsoap.org/wsdl/formatbinding/";

    // Element names.
    public static final String ELEM_FORMAT_BINDING = "typeMapping";
    public static final String ELEM_FORMAT_BINDING_MAP = "typeMap";

    // Qualified element names.
    public static final QName Q_ELEM_FORMAT_BINDING =
        new QName(NS_URI_FORMAT, ELEM_FORMAT_BINDING);
        
    public static final QName Q_ELEM_FORMAT_BINDING_MAP =
        new QName(NS_URI_FORMAT, ELEM_FORMAT_BINDING_MAP);
}
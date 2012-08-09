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

package org.apache.wsif.wsdl.extensions.ejb;

import javax.xml.namespace.QName;

import com.ibm.wsdl.Constants;

/** 
 * @author Gerhard Pfau <gpfau@de.ibm.com>
 * @author Ant Elder <antelder@apache.org>
 * @author Owen Burroughs <owenb@pache.org>
 * @author Jeremy Hughes <hughesj@apache.org>
 */
public class EJBBindingConstants {
    
    // Namespace URIs.
    public static final String NS_URI_EJB = "http://schemas.xmlsoap.org/wsdl/ejb/";

    // Element names.
    public static final String ELEM_ADDRESS = "address";

    // Qualified element names.
    public static final QName Q_ELEM_EJB_BINDING =
        new QName(NS_URI_EJB, Constants.ELEM_BINDING);
        
    public static final QName Q_ELEM_EJB_OPERATION =
        new QName(NS_URI_EJB, Constants.ELEM_OPERATION);
        
    public static final QName Q_ELEM_EJB_ADDRESS =
        new QName(NS_URI_EJB, ELEM_ADDRESS);
}
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

import java.io.PrintWriter;
import java.io.Serial;
import java.io.Serializable;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.ExtensionSerializer;
import javax.xml.namespace.QName;

import org.apache.wsif.logging.Trc;
import org.w3c.dom.Element;

import com.ibm.wsdl.Constants;
import com.ibm.wsdl.util.xml.DOMUtils;

/**
 * @author Aleksander Slominski
 * @author Nirmal K. Mukhi (nmukhi@us.ibm.com)
 * @author Owen Burroughs <owenb@apache.org>
 * @author Ant Elder <antelder@apache.org>
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class InstanceEstablishmentSerializer
    implements ExtensionSerializer, ExtensionDeserializer, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
        
    public void marshall(
        Class parentType,
        QName elementType,
        ExtensibilityElement extension,
        PrintWriter pw,
        Definition def,
        ExtensionRegistry extReg)
        throws WSDLException {
        Trc.entry(this, parentType, elementType, extension, pw, def, extReg);

        InstanceEstablishment instanceEstablishment = (InstanceEstablishment) extension;

        if (instanceEstablishment != null) {
            pw.print("      <instance:establishment");

            DOMUtils.printAttribute(
                InstanceConstants.ATTR_OPERATION,
                instanceEstablishment.getOperationName(),
                pw);

            Boolean required = instanceEstablishment.getRequired();

            if (required != null) {
                DOMUtils.printQualifiedAttribute(
                    Constants.Q_ATTR_REQUIRED,
                    required.toString(),
                    def,
                    pw);
            }

            pw.println("/>");
        }
        Trc.exit();
    }

    public ExtensibilityElement unmarshall(
        Class parentType,
        QName elementType,
        Element el,
        Definition def,
        ExtensionRegistry extReg)
        throws WSDLException {
        Trc.entry(this, parentType, elementType, el, def, extReg);

        InstanceEstablishment instanceEstablishment = new InstanceEstablishment();
        String operationName =
            DOMUtils.getAttribute(el, InstanceConstants.ATTR_OPERATION);

        String requiredStr =
            DOMUtils.getAttributeNS(el, Constants.NS_URI_WSDL, Constants.ATTR_REQUIRED);

        if (operationName != null) {
            instanceEstablishment.setOperationName(operationName);
        }

        //    if (archive != null) {
        //      javaAddress.setArchive(archive);
        //    }

        if (requiredStr != null) {
            instanceEstablishment.setRequired(Boolean.valueOf(requiredStr));
        }

        Trc.exit(instanceEstablishment);
        return instanceEstablishment;
    }
}

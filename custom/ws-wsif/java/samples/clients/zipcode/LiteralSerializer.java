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

package clients.zipcode;

import java.io.IOException;
import java.io.Writer;

import org.apache.soap.Utils;
import org.apache.soap.rpc.SOAPContext;
import org.apache.soap.util.Bean;
import org.apache.soap.util.xml.NSStack;
import org.apache.soap.util.xml.QName;
import org.apache.soap.util.xml.XMLJavaMappingRegistry;
import org.apache.wsif.providers.soap.apachesoap.PartSerializer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @version 	1.0
 * @author
 */
public class LiteralSerializer implements PartSerializer {

    private Object customBean;
    private javax.xml.namespace.QName customBeanQName;

    /*
     * @see PartSerializer#setCustomBean(Object)
     */
    public void setPart(Object aCustomBean) {
        this.customBean = aCustomBean;
    }

    /*
     * @see PartSerializer#getCustomBean()
     */
    public Object getPart() {
        return this.customBean;
    }

    public Object getPart(Class resultClass) {
        return null;
    }
    /*
     * @see Serializer#marshall(String, Class, Object, Object, Writer, NSStack, XMLJavaMappingRegistry, SOAPContext)
     */
    public void marshall(
        String inScopeEncStyle,
        Class javaType,
        Object src,
        Object context,
        Writer sink,
        NSStack nsStack,
        XMLJavaMappingRegistry xjmr,
        SOAPContext ctx)
        throws IllegalArgumentException, IOException {

        // Invoke utility method to get the FormatHandler
        PartFormatHandler fh = new ShortZipCodeFormatHandler();
        fh.setCustomBean(customBean);
        fh.setCustomBeanQName(this.customBeanQName);
        Element element = fh.getElement();
        Utils.marshallNode((Node) element, sink);
    }

    /*
     * @see Deserializer#unmarshall(String, QName, Node, XMLJavaMappingRegistry, SOAPContext)
     */
    public Bean unmarshall(
        String inScopeEncStyle,
        QName elementType,
        Node src,
        XMLJavaMappingRegistry xjmr,
        SOAPContext ctx)
        throws IllegalArgumentException {
        // Invoke utility method to get the FormatHandler
        // PartFormatHandler fh = getFormatHandler(customBean, customBeanQName);
        PartFormatHandler fh = new ShortZipCodeResponseFormatHandler();
        fh.setElement((Element) src);
        this.customBean = fh.getCustomBean();
        return new Bean(this.customBean.getClass(), this.customBean);
    }

    private PartFormatHandler getFormatHandler(Object bean, QName namespace) {

        // Invoke utility method to get the FormatHandler
        // PartFormatHandler fh = (PartFormatHandler)JCAUtil.getFormatHandler(bean, namespace, "soap", "literal");
        // return fh;		
        return null;
    }

    /**
     * Gets the customBeanQName.
     * @return Returns a QName
     */
    public javax.xml.namespace.QName getPartQName() {
        return customBeanQName;
    }

    /**
     * Sets the customBeanQName.
     * @param customBeanQName The customBeanQName to set
     */
    public void setPartQName(javax.xml.namespace.QName customBeanQName) {
        this.customBeanQName = customBeanQName;

    }

}

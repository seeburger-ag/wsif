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

package org.apache.wsif.providers.soap.apachesoap;

import java.io.IOException;
import java.io.Writer;

import org.apache.soap.rpc.SOAPContext;
import org.apache.soap.util.Bean;
import org.apache.soap.util.xml.Deserializer;
import org.apache.soap.util.xml.NSStack;
import org.apache.soap.util.xml.QName;
import org.apache.soap.util.xml.Serializer;
import org.apache.soap.util.xml.XMLJavaMappingRegistry;
import org.apache.wsif.logging.Trc;
import org.w3c.dom.Node;

public class SOAPEncSerializerWrapper implements PartSerializer {
    
    private Serializer targetSerializer = null;
    private Deserializer targetDeserializer = null;
    private Object part = null;
    
    /**
     * @see PartSerializer#setPart(Object)
     */
    public void setPart(Object aPart) {
        this.part = aPart;
    }

    /**
     * @see PartSerializer#getPart()
     */
    public Object getPart() {
        return this.part;
    }

    /**
     * @see PartSerializer#getPart(Class)
     */
    public Object getPart(Class partClass) {
        return null;
    }

    /**
     * @see PartSerializer#getPartQName()
     */
    public javax.xml.namespace.QName getPartQName() {
        return null;
    }

    /**
     * @see PartSerializer#setPartQName(QName)
     */
    public void setPartQName(javax.xml.namespace.QName qName) {
    }

    /**
     * @see Serializer#marshall(String, Class, Object, Object, Writer, 
     * NSStack, XMLJavaMappingRegistry, SOAPContext)
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
        Trc.entry(
            this,
            inScopeEncStyle,
            javaType,
            src,
            context,
            sink,
            nsStack,
            xjmr,
            ctx);
        if (this.targetSerializer != null)
            this.targetSerializer.marshall(
                inScopeEncStyle,
                javaType,
                this.part,
                context,
                sink,
                nsStack,
                xjmr,
                ctx);
        Trc.exit();
    }

    /**
     * @see Deserializer#unmarshall(String, QName, Node, XMLJavaMappingRegistry, 
     * SOAPContext)
     */
    public Bean unmarshall(
        String inScopeEncStyle,
        QName elementType,
        Node src,
        XMLJavaMappingRegistry xjmr,
        SOAPContext ctx)
        throws IllegalArgumentException {
        Trc.entry(this, inScopeEncStyle, elementType, xjmr, ctx);
        Bean b = null;
        if (this.targetDeserializer != null)
            b =
                this.targetDeserializer.unmarshall(
                    inScopeEncStyle,
                    elementType,
                    src,
                    xjmr,
                    ctx);
        Trc.exit(b);
        return b;
    }

    /**
     * Gets the targetDeserializer.
     * @return Returns a Deserializer
     */
    public Deserializer getTargetDeserializer() {
        return targetDeserializer;
    }

    /**
     * Sets the targetDeserializer.
     * @param targetDeserializer The targetDeserializer to set
     */
    public void setTargetDeserializer(Deserializer targetDeserializer) {
        this.targetDeserializer = targetDeserializer;
    }

    /**
     * Gets the targetSerializer.
     * @return Returns a Serializer
     */
    public Serializer getTargetSerializer() {
        return targetSerializer;
    }

    /**
     * Sets the targetSerializer.
     * @param targetSerializer The targetSerializer to set
     */
    public void setTargetSerializer(Serializer targetSerializer) {
        this.targetSerializer = targetSerializer;
    }

}
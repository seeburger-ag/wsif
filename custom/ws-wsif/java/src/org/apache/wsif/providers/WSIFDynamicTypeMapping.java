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

package org.apache.wsif.providers;

import java.io.Externalizable;

import javax.xml.namespace.QName;

import org.apache.wsif.logging.Trc;

/**
 * This class encapsultes a simple association between XML and Java type
 * (QName and Class).
 *
 * @author Alekander Slominski
 * @author Owen Burroughs <owenb@apache.org>
 */
public class WSIFDynamicTypeMapping implements Externalizable {
	private static final long serialVersionUID = 1L;
    protected QName xmlType;
    protected Class javaType;

    /**
     * No args constructor required for deserialization purposes.
     * This constructor should not be used directly.
     */
    public WSIFDynamicTypeMapping() {
    	xmlType = null;
    	javaType = null;
    }

    public WSIFDynamicTypeMapping(QName xmlType, Class javaType) {
        Trc.entry(this, xmlType, javaType);
        this.xmlType = xmlType;
        this.javaType = javaType;
        Trc.exit();
    }

    public QName getXmlType() {
        return xmlType;
    }

    public Class getJavaType() {
        return javaType;
    }

    public String toString() {
        return "QName:" + xmlType + " Class:" + javaType;
    }

    /**
     *  Override writeExternal method to allow primitive classes to be serialized. 
     */
    public void writeExternal(java.io.ObjectOutput out)
        throws java.io.IOException {
        out.writeObject(xmlType);
        if (javaType.isPrimitive()) {
            out.writeBoolean(true);
            out.writeObject(javaType.getName());
        } else {
            out.writeBoolean(false);
            out.writeObject(javaType);
        }

    }
    
    /**
     *  Override readExternal method to allow primitive classes to be deserialized. 
     */
    public void readExternal(java.io.ObjectInput in)
        throws java.io.IOException, ClassNotFoundException {
        xmlType = (QName) in.readObject();
        boolean primitive = in.readBoolean();
        if (primitive) {
            String primitiveClassName = (String) in.readObject();
            if (primitiveClassName.equals("int"))
                javaType = int.class;
            else if (primitiveClassName.equals("float"))
                javaType = float.class;
            else if (primitiveClassName.equals("double"))
                javaType = double.class;
            else if (primitiveClassName.equals("boolean"))
                javaType = boolean.class;
            else if (primitiveClassName.equals("long"))
                javaType = long.class;
            else if (primitiveClassName.equals("short"))
                javaType = short.class;
            else if (primitiveClassName.equals("byte"))
                javaType = byte.class;
            else if (primitiveClassName.equals("void"))
                javaType = void.class;
        } else
            javaType = (Class) in.readObject();
    }
}
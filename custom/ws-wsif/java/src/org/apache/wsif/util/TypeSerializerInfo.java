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

package org.apache.wsif.util;

import java.io.Serializable;

import javax.xml.namespace.QName;

import org.apache.wsif.logging.Trc;

/**
 * This class maps a element/java type with its (de)serializers 
 */
public class TypeSerializerInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

	protected QName elementType;
	protected Class javaType;
	protected Object serializer;
	protected Object deserializer;

	public TypeSerializerInfo(
		QName elementType,
		Class javaType,
		Object serializer,
		Object deserializer) {
		Trc.entry(this, elementType, javaType, serializer, deserializer);
		this.elementType = elementType;
		this.javaType = javaType;
		this.serializer = serializer;
		this.deserializer = deserializer;
		Trc.exit();
	}

	/**
	 * Returns the deserializer.
	 * @return Object
	 */
	public Object getDeserializer() {
		Trc.entry(this);
		Trc.exit(deserializer);
		return deserializer;
	}

	/**
	 * Returns the elementType.
	 * @return QName
	 */
	public QName getElementType() {
		Trc.entry(this);
		Trc.exit(elementType);
		return elementType;
	}

	/**
	 * Returns the javaType.
	 * @return Class
	 */
	public Class getJavaType() {
		Trc.entry(this);
		Trc.exit(javaType);
		return javaType;
	}

	/**
	 * Returns the serializer.
	 * @return Object
	 */
	public Object getSerializer() {
		Trc.entry(this);
		Trc.exit(serializer);
		return serializer;
	}

	/**
	 * Sets the deserializer.
	 * @param deserializer The deserializer to set
	 */
	public void setDeserializer(Object deserializer) {
		Trc.entry(this, deserializer);
		this.deserializer = deserializer;
		Trc.exit();
	}

	/**
	 * Sets the elementType.
	 * @param elementType The elementType to set
	 */
	public void setElementType(QName elementType) {
		Trc.entry(this, elementType);
		this.elementType = elementType;
		Trc.exit();
	}

	/**
	 * Sets the javaType.
	 * @param javaType The javaType to set
	 */
	public void setJavaType(Class javaType) {
		Trc.entry(this, javaType);
		this.javaType = javaType;
		Trc.exit();
	}

	/**
	 * Sets the serializer.
	 * @param serializer The serializer to set
	 */
	public void setSerializer(Object serializer) {
		Trc.entry(this, serializer);
		this.serializer = serializer;
		Trc.exit();
	}

	public String toString() {
		return "[TypeSerializerInfo elementType="
			+ elementType
			+ ", "
			+ "javaType="
			+ javaType
			+ ", "
			+ "serializer="
			+ serializer
			+ ", "
			+ "deserializer="
			+ deserializer
			+ "]";
	}

}
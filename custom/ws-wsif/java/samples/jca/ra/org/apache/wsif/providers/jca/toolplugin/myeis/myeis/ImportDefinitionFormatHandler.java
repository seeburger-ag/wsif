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

package org.apache.wsif.providers.jca.toolplugin.myeis.myeis;

import java.io.*;

import javax.resource.cci.InteractionSpec;

import org.apache.wsif.providers.jca.toolplugin.*;
import org.apache.wsif.providers.jca.WSIFFormatHandler_JCA;

public class ImportDefinitionFormatHandler implements WSIFFormatHandler_JCA {

	private ImportDefinition fieldImportDefinition = null;

	/**
	 * @see JCAFormatHandler#read(InputStream)
	 */
	public void read(InputStream inputStream) throws IOException {

		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
			this.fieldImportDefinition =
				(ImportDefinition)objectInputStream.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new IOException("class not found !");
		}		
	}

	/**
	 * @see JCAFormatHandler#write(OutputStream)
	 */
	public void write(OutputStream outputStream) throws IOException {

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(this.fieldImportDefinition);
        objectOutputStream.flush();
    }

	/**
	 * @see WSIFFormatHandler#getElement(String)
	 */
	public Object getElement(String elementName) {
		return null;
	}

	/**
	 * @see WSIFFormatHandler#setElement(String, Object)
	 */
	public void setElement(String elementName, Object element) {
	}

	/**
	 * @see WSIFFormatHandler#getElement(String, int)
	 */
	public Object getElement(String name, int index) {
		return null;
	}

	/**
	 * @see WSIFFormatHandler#setElement(String, int, Object)
	 */
	public void setElement(String name, int index, Object value) {
	}

	/**
	 * @see WSIFFormatHandler#getObjectPart()
	 */
	public Object getObjectPart() {
		return this.fieldImportDefinition;
	}

	/**
	 * @see WSIFFormatHandler#getObjectPart(Class)
	 */
	public Object getObjectPart(Class sourceClass) {
		return null;
	}

	/**
	 * @see WSIFFormatHandler#getCharPart()
	 */
	public char getCharPart() {
		return 0;
	}

	/**
	 * @see WSIFFormatHandler#getBytePart()
	 */
	public byte getBytePart() {
		return 0;
	}

	/**
	 * @see WSIFFormatHandler#getShortPart()
	 */
	public short getShortPart() {
		return 0;
	}

	/**
	 * @see WSIFFormatHandler#getIntPart()
	 */
	public int getIntPart() {
		return 0;
	}

	/**
	 * @see WSIFFormatHandler#getLongPart()
	 */
	public long getLongPart() {
		return 0;
	}

	/**
	 * @see WSIFFormatHandler#getFloatPart()
	 */
	public float getFloatPart() {
		return 0;
	}

	/**
	 * @see WSIFFormatHandler#getDoublePart()
	 */
	public double getDoublePart() {
		return 0;
	}

	/**
	 * @see WSIFFormatHandler#setObjectPart(Object)
	 */
	public void setObjectPart(Object objectPart) {
		this.fieldImportDefinition = (ImportDefinition)objectPart;
	}

	/**
	 * @see WSIFFormatHandler#setCharPart(char)
	 */
	public void setCharPart(char charPart) {
	}

	/**
	 * @see WSIFFormatHandler#setBytePart(byte)
	 */
	public void setBytePart(byte bytePart) {
	}

	/**
	 * @see WSIFFormatHandler#setShortPart(short)
	 */
	public void setShortPart(short shortPart) {
	}

	/**
	 * @see WSIFFormatHandler#setIntPart(int)
	 */
	public void setIntPart(int intPart) {
	}

	/**
	 * @see WSIFFormatHandler#setLongPart(long)
	 */
	public void setLongPart(long longPart) {
	}

	/**
	 * @see WSIFFormatHandler#setFloatPart(float)
	 */
	public void setFloatPart(float floatPart) {
	}

	/**
	 * @see WSIFFormatHandler#setDoublePart(double)
	 */
	public void setDoublePart(double doublePart) {
	}
	public void setPartQName(javax.xml.namespace.QName qname){
	}
	
	public javax.xml.namespace.QName getPartQName(){
		return null;
	}
	public void setInteractionSpec(InteractionSpec is){
		
	}

}


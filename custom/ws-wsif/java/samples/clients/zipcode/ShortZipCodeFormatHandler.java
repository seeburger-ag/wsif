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

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * @version 	1.0
 * @author
 */
public class ShortZipCodeFormatHandler implements PartFormatHandler {


	private ShortZipCode dataBean = null;
	private ShortZipCode_ElementContentType nativeFormat = new ShortZipCode_ElementContentType();
	private QName dataBeanQName;
	
	public void setCustomBeanQName(javax.xml.namespace.QName qName){
		this.dataBeanQName = qName;
		if(nativeFormat != null){
			nativeFormat.changeLocalName(dataBeanQName.getLocalPart());
			nativeFormat.changeNamespaceURI(dataBeanQName.getNamespaceURI());
		}	
	}
	
	public javax.xml.namespace.QName getCustomBeanQName(){
		return this.dataBeanQName;
	}
	
	/*
	 * @see PartFormatHandler#setCustomBean(Object)
	 */
	public void setCustomBean(Object customBean) {
		dataBean = (ShortZipCode)customBean;
		nativeFormat.setAccessCode(dataBean.getAccessCode());
		nativeFormat.setAddress(dataBean.getAddress());
		nativeFormat.setCity(dataBean.getCity());
		nativeFormat.setState(dataBean.getState());
	}

	/*
	 * @see PartFormatHandler#getCustomBean()
	 */
	public Object getCustomBean() {
		dataBean.setAccessCode(nativeFormat.getAccessCode());
		dataBean.setAddress(nativeFormat.getAddress());
		dataBean.setCity(nativeFormat.getCity());
		dataBean.setState(nativeFormat.getState());
		return dataBean;
	}

	/*
	 * @see PartFormatHandler#setElement(Element)
	 */
	public void setElement(Element element) {
		nativeFormat.populateFrom(element);
	}

	/*
	 * @see PartFormatHandler#getElement()
	 */
	public Element getElement() {
		return nativeFormat.createElement();
	}

}

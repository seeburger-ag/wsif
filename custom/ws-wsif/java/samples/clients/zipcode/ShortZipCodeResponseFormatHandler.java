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
public class ShortZipCodeResponseFormatHandler implements PartFormatHandler {

	private ShortZipCodeResponse dataBean = null;
	private ShortZipCodeResponse_ElementContentType nativeFormat = new ShortZipCodeResponse_ElementContentType();
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
		dataBean = (ShortZipCodeResponse)customBean;
		nativeFormat.setShortZipCodeResult(dataBean.getShortZipCodeResult());
	}

	/*
	 * @see PartFormatHandler#getCustomBean()
	 */
	public Object getCustomBean() {
		getDataBean().setShortZipCodeResult(nativeFormat.getShortZipCodeResult());
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

	private ShortZipCodeResponse getDataBean(){
		if(this.dataBean == null)
			this.dataBean = new ShortZipCodeResponse();
		return this.dataBean;
	}
}

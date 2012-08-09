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

package com.myeis.services.internal;
public class PurchaseOrderInfo {

	public PurchaseOrderDataObject getPurchaseOrder(String orderNumber){
		if(orderNumber.equals("1234")){
			PurchaseOrderDataObject po = new PurchaseOrderDataObject();
			po.setDescription("Hard Drive");
			po.setOrderNumber(orderNumber);
			po.setPartNumber("09343");
			po.setQuantity(12);
			po.setUnitPrice(453.99d);
			return po;
		}
		else{
			PurchaseOrderDataObject po = new PurchaseOrderDataObject();
			po.setDescription("DVD Drive");
			po.setOrderNumber(orderNumber);
			po.setPartNumber("09345");
			po.setQuantity(10);
			po.setUnitPrice(219.99d);
			return po;
		}
	}
}


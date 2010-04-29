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

package com.myeis;

import java.io.*;
import org.apache.wsif.providers.jca.toolplugin.*;
import com.myeis.repository.*;
import com.myeis.services.*;
import com.myeis.services.internal.*;

public class MyEIS {

	public byte[] doIt(byte[] input) {
		
		/*
		 * This method represents the actual sample backend (i.e. this method mimics what a real back end system would typically do).
		 * Based on the function name certain tasks are executed.
		 */
	
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
			
			// get the function name
			ObjectInputStream headerInputStream = new ObjectInputStream(inputStream);
			String functionName = (String)headerInputStream.readObject();
		
			if (functionName.equals("IMPORT_PORTTYPES")) {
				// import service porttypes
				ObjectInputStream payloadInputStream = new ObjectInputStream(inputStream);
				String queryString = (String)payloadInputStream.readObject();
				PortTypeArray portTypeArray = (new Repository()).getPortTypes(queryString);
				
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();		
		        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
  				objectOutputStream.writeObject(portTypeArray);
		        objectOutputStream.flush();					
			
				return outputStream.toByteArray();
			}
			if (functionName.equals("IMPORT_DEFINITION")) {
				// import service definitions
				ObjectInputStream payloadInputStream = new ObjectInputStream(inputStream);				
				PortTypeSelection selection = (PortTypeSelection)payloadInputStream.readObject();
				ImportDefinition importDefinition = (new Repository()).getDefinition(selection);
			
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();		
		        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
  				objectOutputStream.writeObject(importDefinition);
		        objectOutputStream.flush();					
			
				return outputStream.toByteArray();
			}
			if (functionName.equals("IMPORT_RAWMETADATA")) {
				// import raw metadata
				ObjectInputStream payloadInputStream = new ObjectInputStream(inputStream);
				String queryString = (String)payloadInputStream.readObject();
				byte[] result  = (new Repository()).getRawEISMetaData(queryString);
				
				return result;
			}
			if (functionName.equals("CUSTOMERINFO_getCustomer")) {
				// invoke a business service
				ObjectInputStream payloadInputStream = new ObjectInputStream(inputStream);				
				String number = (String)payloadInputStream.readObject();
				CustomerDataObject customer = (new CustomerInfo()).getCustomer(number);
			
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();		
		        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
  				objectOutputStream.writeObject(customer);
		        objectOutputStream.flush();					
			
				return outputStream.toByteArray();
			}			
			if (functionName.equals("PURCHASEORDERINFO_getPurchaseOrder")) {
				// invoke a business service
				ObjectInputStream payloadInputStream = new ObjectInputStream(inputStream);				
				String number = (String)payloadInputStream.readObject();
				PurchaseOrderDataObject purchaseOrder = (new PurchaseOrderInfo()).getPurchaseOrder(number);
			
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();		
		        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
  				objectOutputStream.writeObject(purchaseOrder);
		        objectOutputStream.flush();					
			
				return outputStream.toByteArray();
			}			
			
			if (functionName.equals("CUSTOMERINFO_getAddress")) {
				// invoke a business service
				ObjectInputStream payloadInputStream = new ObjectInputStream(inputStream);				
				String number = (String)payloadInputStream.readObject();
				AddressDataObject address = (new CustomerInfo()).getAddress(number);
			
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();		
		        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
  				objectOutputStream.writeObject(address);
		        objectOutputStream.flush();					
			
				return outputStream.toByteArray();
			}			
			// process other business services here
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

}


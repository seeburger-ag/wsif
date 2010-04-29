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

/**
 * @version 	1.0
 * @author
 */
public class ZIPCodeProxyClient {

	public static void main(String[] args) {

        if (args.length!=1) 
        {
        	System.out.println("Usage: ZipCodeProxyClient wsdl");		
        	return;
        }
        
		ShortZipCode address = new ShortZipCode();
		address.setAccessCode("9999");
		address.setAddress("607 Trinity");
		address.setCity("Austin");
		address.setState("TX");

		ZIPCodeProxy proxy = new ZIPCodeProxy(args[0]);
		ShortZipCodeResponse response = 
		  proxy.getShortZipCodeResponse(address,"ZipCodeResolverSoap");
		  
		System.out.println(response.getShortZipCodeResult());
	}
}

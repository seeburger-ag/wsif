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
public class ShortZipCode {

	private String fieldAccessCode;
	private String fieldAddress;
	private String fieldCity;
	private String fieldState;
	/**
	 * Gets the accessCode.
	 * @return Returns a String
	 */
	public String getAccessCode() {
		return fieldAccessCode;
	}

	/**
	 * Sets the accessCode.
	 * @param accessCode The accessCode to set
	 */
	public void setAccessCode(String accessCode) {
		fieldAccessCode = accessCode;
	}

	/**
	 * Gets the address.
	 * @return Returns a String
	 */
	public String getAddress() {
		return fieldAddress;
	}

	/**
	 * Sets the address.
	 * @param address The address to set
	 */
	public void setAddress(String address) {
		fieldAddress = address;
	}

	/**
	 * Gets the city.
	 * @return Returns a String
	 */
	public String getCity() {
		return fieldCity;
	}

	/**
	 * Sets the city.
	 * @param city The city to set
	 */
	public void setCity(String city) {
		fieldCity = city;
	}

	/**
	 * Gets the state.
	 * @return Returns a String
	 */
	public String getState() {
		return fieldState;
	}

	/**
	 * Sets the state.
	 * @param state The state to set
	 */
	public void setState(String state) {
		fieldState = state;
	}

}

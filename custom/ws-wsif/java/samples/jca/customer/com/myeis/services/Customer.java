package com.myeis.services;

import org.apache.wsif.format.WSIFFormatHandler;
import org.apache.wsif.format.WSIFFormatPart;
import org.w3c.dom.Element;

public class Customer implements WSIFFormatPart {
	
	private String lastName;
	private String number;
	private String firstName;
	private WSIFFormatHandler fh;
	
	public Customer() {
		super();
	}


	/**
	 * Returns the irstName.
	 * @return String
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Returns the lastName.
	 * @return String
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Returns the number.
	 * @return String
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * Sets the irstName.
	 * @param irstName The irstName to set
	 */
	public void setFirstName(String irstName) {
		firstName = irstName;
	}

	/**
	 * Sets the lastName.
	 * @param lastName The lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * Sets the number.
	 * @param number The number to set
	 */
	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * Returns the h.
	 * @return WSIFFormatHandler
	 */
	public WSIFFormatHandler _getFormatHandler() {
		return fh;
	}

	/**
	 * Sets the h.
	 * @param h The h to set
	 */
	public void _setFormatHandler(WSIFFormatHandler h) {
		fh = h;
	}

	public void fireElementEvents(){
		
	}
}

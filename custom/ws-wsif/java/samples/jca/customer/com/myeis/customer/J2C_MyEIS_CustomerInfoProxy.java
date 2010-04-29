package com.myeis.customer;
import org.apache.wsif.*;
import org.apache.wsif.base.*;

import com.myeis.services.Customer;

import javax.xml.namespace.QName;
/**
 * J2C_MyEIS_CustomerInfoProxy
 * Generated code. Only edit user code sections.
 * @generated
 */
public class J2C_MyEIS_CustomerInfoProxy {
	/**
	 * @generated
	 */
	private static final int INPUT_ONLY = 0;
	/**
	 * @generated
	 */
	private static final int REQUEST_RESPONSE = 1;
	/**
	 * @generated
	 */
	private WSIFPort fieldPort;
	/**
	 * @generated
	 */
	private WSIFService fieldService;
	/**
	 * @generated
	 */
	private static WSIFService fieldStaticService = null;
	/**
	 * getPort
	 * @generated
	 */
	public WSIFPort getPort() {
		return fieldPort;
	}
	/**
	 * setPort
	 * @generated
	 */
	public void setPort(WSIFPort newPort) {
		fieldPort = newPort;
	}
	/**
	 * getService
	 * @generated
	 */
	public WSIFService getService() {
		return fieldService;
	}
	/**
	 * setService
	 * @generated
	 */
	public void setService(WSIFService newService) {
		fieldService = newService;
	}
	/**
	 * getCustomer
	 * @generated
	 */
	public com.myeis.services.Customer getCustomer(java.lang.String argNumber) throws org.apache.wsif.WSIFException {

		try {

			// user code begin {pre_execution}
			// user code end

			WSIFDefaultMessage inputMessage = new WSIFDefaultMessage();
			inputMessage.setObjectPart("number", argNumber);

			WSIFMessage outputMessage = execute("getCustomer", "getCustomerRequest", "getCustomerResponse", inputMessage, REQUEST_RESPONSE);

			// user code begin {post_execution}
			// user code end

			return (com.myeis.services.Customer) outputMessage.getObjectPart("result");

		}
		catch (Exception e) {
			// user code begin {exception_handling}
			// user code end
			if (e instanceof org.apache.wsif.WSIFException)
				throw (org.apache.wsif.WSIFException) e;
			throw new org.apache.wsif.WSIFException(e.getMessage(), e);
		}
	}
	/**
	 * constructor
	 * @generated
	 */
	public J2C_MyEIS_CustomerInfoProxy() throws WSIFException {

		// user code begin {custom_initialization}
		// user code end

		if (this.fieldStaticService == null) {

			this.fieldStaticService =
				WSIFServiceFactory.newInstance().getService("com/myeis/customer/CustomerInfoMYEISService.wsdl", this.getClass().getClassLoader(), "http://customer.myeis.com/", "CustomerInfoMYEISService", "http://customer.myeis.com/", "CustomerInfo");

			if (this.fieldStaticService == null)
				return;

			this.fieldStaticService.mapType(new QName("http://services.myeis.com/", "Customer"), com.myeis.services.Customer.class);

			// user code begin {port_factory_setup}
			// user code end
		}
	}
	/**
	 * main method (for proxy unit testing)
	 * @generated
	 */
	public static void main(String[] args) {

		try {

			J2C_MyEIS_CustomerInfoProxy aProxy = new J2C_MyEIS_CustomerInfoProxy();

			// user code begin {proxy_method_calls}
			String custNum = "33333";
			Customer customer = aProxy.getCustomer(custNum);
			System.out.println("Customer number: " + custNum);
			System.out.println("Customer name:   " + customer.getFirstName() + " " + customer.getLastName());
			
			// user code end

		}
		catch (Exception e) {

			// user code begin {exception_handling}
			e.printStackTrace();
			// user code end
		}
	}
	/**
	 * execute (base message-level execution)
	 * @generated
	 */
	public WSIFMessage execute(String operationName, String inputName, String outputName, WSIFMessage aMessage, int operationType) throws WSIFException, Exception {

		WSIFPort port;
		if (this.fieldPort == null) {
			if (this.fieldService == null)
				this.fieldService = fieldStaticService;
			if (this.fieldService == null)
				throw new WSIFException("Failed to resolve WSIFService.");
			port = this.fieldService.getPort("CustomerInfoMyEISPort");
		}
		else {
			port = this.fieldPort;
		}

		WSIFOperation operation = port.createOperation(operationName, inputName, outputName);

		WSIFMessage inputMessage = operation.createInputMessage();

		String partName;
		java.util.Iterator iterator = aMessage.getPartNames();
		while (iterator.hasNext()) {
			partName = (String) iterator.next();
			inputMessage.setObjectPart(partName, aMessage.getObjectPart(partName));
		}

		WSIFMessage outputMessage = operation.createOutputMessage();
		WSIFMessage faultMessage = operation.createFaultMessage();
		boolean success = true;
		if (operationType == INPUT_ONLY)
			operation.executeInputOnlyOperation(inputMessage);
		else if (operationType == REQUEST_RESPONSE)
			success = operation.executeRequestResponseOperation(inputMessage, outputMessage, faultMessage);

		if (this.fieldPort == null)
			port.close();

		if (!success) {
			java.util.Iterator i = faultMessage.getParts();
			if (i.hasNext()) {
				Object part = i.next();
				if (part instanceof Exception)
					throw (Exception) part;
				else
					throw new WSIFException(String.valueOf(part));
			}
		}

		return outputMessage;
	}
}

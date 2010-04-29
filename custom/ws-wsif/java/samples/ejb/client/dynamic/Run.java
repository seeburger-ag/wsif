package ejb.client.dynamic;

import javax.xml.namespace.QName;

import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;

import ejb.service.addressbook.wsiftypes.Address;
import ejb.service.addressbook.wsiftypes.Phone;

public class Run {
    private static void addFirstAddress(WSIFPort port) {
	try {
	    // create the operation
	    // note that we have two operations with the same name, so we need to specify the
	    // name of the input and output messages as well
	    WSIFOperation operation = port.createOperation("addEntry","AddEntryWholeNameRequest",null);
	    // create the input message associated with this operation
	    WSIFMessage input = operation.createInputMessage();
	    // populate the input message
	    input.setObjectPart("name","John Smith");
	    // create an address object to populate the input
	    Address address = new Address();
	    address.setStreetNum(25);
	    address.setStreetName("Willow Road");
	    address.setCity("MyTown");
	    address.setState("PA");
	    address.setZip(28382);
	    Phone phone = new Phone();
	    phone.setAreaCode(288);
	    phone.setExchange("555");
	    phone.setNumber("9891");
	    address.setPhoneNumber(phone);
	    input.setObjectPart("address",address);
	    // do the invocation
	    System.out.println("Adding address for John Smith...");
	    operation.executeInputOnlyOperation(input);
	} catch (WSIFException we) {
	    System.out.println("Got exception from WSIF, details:");
	    we.printStackTrace();
	}
    }
	
    private static void addSecondAddress(WSIFPort port) {
	try {
	    // create the operation
	    // note that we have two operations with the same name, so we need to specify the
	    // name of the input and output messages as well
	    WSIFOperation operation = port.createOperation("addEntry","AddEntryFirstAndLastNamesRequest",null);
	    // create the input message associated with this operation
	    WSIFMessage input = operation.createInputMessage();
	    // populate the input message
	    input.setObjectPart("firstName","Jane");
	    input.setObjectPart("lastName","White");
	    // create an address object to populate the input
	    Address address = new Address();
	    address.setStreetNum(20);
	    address.setStreetName("Peachtree Avenue");
	    address.setCity("Atlanta");
	    address.setState("GA");
	    address.setZip(39892);
	    Phone phone = new Phone();
	    phone.setAreaCode(701);
	    phone.setExchange("555");
	    phone.setNumber("8721");
	    address.setPhoneNumber(phone);
	    input.setObjectPart("address",address);
	    // do the invocation
	    System.out.println("Adding address for Jane White...");
	    operation.executeInputOnlyOperation(input);
	} catch (WSIFException we) {
	    System.out.println("Got exception from WSIF, details:");
	    we.printStackTrace();
	}
    }

    private static void queryAddresses(WSIFPort port) {
	try {
	    // create the operation
	    WSIFOperation operation = port.createOperation("getAddressFromName");
	    // create the input message associated with this operation
	    WSIFMessage input = operation.createInputMessage();
	    WSIFMessage output = operation.createOutputMessage();
	    WSIFMessage fault = operation.createFaultMessage();
	    // populate the input message
	    input.setObjectPart("name","John Smith");
	    // do the invocation
	    System.out.println("Querying address for John Smith...");
	    if (operation.executeRequestResponseOperation(input,output,fault)) {
		// invocation succeeded
		// extract the address from the output message
		Address address = (Address) output.getObjectPart("address");
		System.out.println("Service returned the following address:");
		System.out.println(address.getStreetNum()+" "+address.getStreetName()+
				   ", "+address.getCity()+" "+address.getState()+" "+
				   address.getZip()+"; Phone: ("+
				   address.getPhoneNumber().getAreaCode()+") "+
				   address.getPhoneNumber().getExchange()+"-"+
				   address.getPhoneNumber().getNumber());
	    } else {
		// invocation failed, check fault message
	    }
	    // create the operation
	    operation = port.createOperation("getAddressFromName");
	    // create the input message associated with this operation
	    input = operation.createInputMessage();
	    output = operation.createOutputMessage();
	    fault = operation.createFaultMessage();
	    // populate the input message
	    input.setObjectPart("name","Jane White");
	    // do the invocation
	    System.out.println("Querying address for Jane White...");
	    if (operation.executeRequestResponseOperation(input,output,fault)) {
		// invocation succeeded
		// extract the address from the output message
		Address address = (Address) output.getObjectPart("address");
		System.out.println("Service returned the following address:");
		System.out.println(address.getStreetNum()+" "+address.getStreetName()+
				   ", "+address.getCity()+" "+address.getState()+" "+
				   address.getZip()+"; Phone: ("+
				   address.getPhoneNumber().getAreaCode()+") "+
				   address.getPhoneNumber().getExchange()+"-"+
				   address.getPhoneNumber().getNumber());
	    } else {
		// invocation failed, check fault message
	    }
	} catch (WSIFException we) {
	    System.out.println("Got exception from WSIF, details:");
	    we.printStackTrace();
	}
    }

    public static void main(String [] args) throws Exception {
	if(args.length!=1) {
	    System.out.println("Usage: java ejb.client.dynamic.Run <wsdl location>");
	    System.exit(1);
	}
	// create a service factory
        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
	
	WSIFService service = factory.getService(args[0], null,
						 null, "http://wsifservice.addressbook/", 
						 "AddressBook"); 
	// map types
	service.mapType(new QName("http://wsiftypes.addressbook/","Address"),
			Class.forName("ejb.service.addressbook.wsiftypes.Address"));
	service.mapType(new QName("http://wsiftypes.addressbook/","Phone"),
			Class.forName("ejb.service.addressbook.wsiftypes.Phone"));

	// get the port
	WSIFPort port = service.getPort();
	// add the first address
	addFirstAddress(port);
	// add the second address
	addSecondAddress(port);
	// query addresses
	queryAddresses(port);
    }
}

package ejb.client.stub;

import java.rmi.RemoteException;

import ejb.service.addressbook.wsifservice.AddressBook;
import ejb.service.addressbook.wsiftypes.Address;
import ejb.service.addressbook.wsiftypes.Phone;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;

/**
 * Class that runs the ejb sample using a pregenerated stub interface
 * To use this class provide the location of the address book service's WSDL 
 * location on the command line. WSIF 
 * should then invoke the EJB service for populating and then
 * querying an addressbook.
 * @author Nirmal K. Mukhi (nmukhi@us.ibm.com)
 */

public class Run {
    private static void addFirstAddress(AddressBook addressBook) {
        try {
        	
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

            // do the invocation
            System.out.println("Adding address for John Smith...");
            addressBook.addEntry("John Smith", address);

        } catch (WSIFException we) {
            System.out.println("Got exception from WSIF, details:");
            we.printStackTrace();
        } catch (RemoteException re) {
            System.out.println("Got exception while invoking stub, details:");
            re.printStackTrace();
        }
    }

    private static void addSecondAddress(AddressBook addressBook) {
        try {
        	
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

            // do the invocation
            System.out.println("Adding address for Jane White...");
            addressBook.addEntry("Jane", "White", address);

        } catch (WSIFException we) {
            System.out.println("Got exception from WSIF, details:");
            we.printStackTrace();
        } catch (RemoteException re) {
            System.out.println("Got exception while invoking stub, details:");
            re.printStackTrace();
        }
    }

    private static void queryAddresses(AddressBook addressBook) {
        try {

            // do the invocation
            System.out.println("Querying address for John Smith...");
            Address address = addressBook.getAddressFromName("John Smith");

            System.out.println("Service returned the following address:");
            System.out.println(
                address.getStreetNum()
                    + " "
                    + address.getStreetName()
                    + ", "
                    + address.getCity()
                    + " "
                    + address.getState()
                    + " "
                    + address.getZip()
                    + "; Phone: ("
                    + address.getPhoneNumber().getAreaCode()
                    + ") "
                    + address.getPhoneNumber().getExchange()
                    + "-"
                    + address.getPhoneNumber().getNumber());

            System.out.println("Querying address for Jane White...");
            address = addressBook.getAddressFromName("Jane White");

            System.out.println("Service returned the following address:");
            System.out.println(
                address.getStreetNum()
                    + " "
                    + address.getStreetName()
                    + ", "
                    + address.getCity()
                    + " "
                    + address.getState()
                    + " "
                    + address.getZip()
                    + "; Phone: ("
                    + address.getPhoneNumber().getAreaCode()
                    + ") "
                    + address.getPhoneNumber().getExchange()
                    + "-"
                    + address.getPhoneNumber().getNumber());

        } catch (WSIFException we) {
            System.out.println("Got exception from WSIF, details:");
            we.printStackTrace();
        } catch (RemoteException re) {
            System.out.println("Got exception while invoking stub, details:");
            re.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                System.out.println(
                    "Usage: java ejb.client.stub.Run <wsdl location>");
                System.exit(1);
            }

            // create a service factory
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();

            // parse WSDL
            WSIFService service =
                factory.getService(
                    args[0],
                    null,
                    null,
                    "http://wsifservice.addressbook/",
                    "AddressBook");

            // create the stub
            AddressBook stub =
                (AddressBook) service.getStub(AddressBook.class);

            // do the invocations
            addFirstAddress(stub);
            addSecondAddress(stub);
            queryAddresses(stub);

        } catch (WSIFException we) {
            System.out.println("Got exception from WSIF, details:");
            we.printStackTrace();
        }
    }
}

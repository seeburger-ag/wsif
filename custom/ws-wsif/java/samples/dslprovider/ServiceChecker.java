package dslprovider;

/**
 * This is a toy application that demonstrates how WSIF can be used to create flexible 
 * applications.
 * This ServiceChecker application allows users to enter their names and addresses
 * through a command line interface and then checks if DSL service is available in
 * the user's area. If the user has previously registered their addresses, they need not
 * enter them again. The application also verifies that addresses are correct.
 *
 * The application makes use of three WSDL-described services:
 * 1. Zip2Geo: this service provides information about a US zip code, such as the
 * corresponding city and state
 * 2. AddressBook: this service allows names and addresses to be stored and 
 * looked up 
 * 3. ServiceAvailability: this service checks if DSL service is available at a 
 * particular zip code
 * The locations of the WSDL documents corresponding to these services are provided at 
 * runtime as command line arguments.
 *
 * The key feature of this application is that since it is completely WSDL driven, 
 * we can swap the service protocols, change their location, add new protocols and
 * make them available dynamically, etc. without having to touch the application code 
 * here.
 *
 * @author Nirmal Mukhi (nmukhi@us.ibm.com)
 */

// types for address book service
import ejb.service.addressbook.wsifservice.AddressBook;
import ejb.service.addressbook.wsiftypes.Address;

// types for Zip2Geo service
import complexsoap.client.stub.com.cdyne.ws.LatLongReturn;
import complexsoap.client.stub.com.cdyne.ws.Zip2GeoSoap;

// types for service availability service
import jms.client.stub.org.apache.xml.CheckAvailabilityPortType;

// wsif classes
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import javax.xml.namespace.QName;

// java IO classes
import java.io.LineNumberReader;
import java.io.InputStreamReader;

public class ServiceChecker {
    static AddressBook addressBook;
    static Zip2GeoSoap zip2geo;
    static CheckAvailabilityPortType serviceAvailability;

    public static void printError(String message) {
	System.err.println(message);
	System.exit(1);
    }

    public static void printUsage() {
	System.out.println("java dslprovider.ServiceChecker <zip2geo service WSDL>"+
			   "<addressbook service WSDL> <serviceAvailability service WSDL>");
    }

    private static void init(String zip2geoWSDL,
			     String addressbookWSDL,
			     String serviceAvailabilityWSDL) throws Exception {
	// create a service factory
	WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
	// initialize the address book stub
	// parse the WSDL
	WSIFService service =
	    factory.getService(addressbookWSDL,null,null,
			       "http://wsifservice.addressbook/",
			       "AddressBook");
	// create the stub
	addressBook = (AddressBook) service.getStub(AddressBook.class);
	// initialize the zip2geo stub
	// parse the WSDL
	service = factory.getService(zip2geoWSDL,null,null,"http://ws.cdyne.com",
				     "Zip2GeoSoap");
	// map types
	service.mapType(new QName("http://ws.cdyne.com", "LatLongReturn"),
			Class.forName("complexsoap.client.stub.com.cdyne.ws.LatLongReturn"));
	// create the stub
	zip2geo = (Zip2GeoSoap) service.getStub(Zip2GeoSoap.class);
	// initialize the service availability stub
	// parse the WSDL
	service = factory.getService(serviceAvailabilityWSDL,null,null,
				     "http://xml.apache.org/axis/wsif/samples/jms/ServiceAvailability",
				     "CheckAvailabilityPortType");
	// create the stub
	serviceAvailability = 
	    (CheckAvailabilityPortType) service.getStub(CheckAvailabilityPortType.class);
    }

    private static Address lookupAddress(String name) throws Exception { 
	// lookup and return the address for that name
	return addressBook.getAddressFromName(name);
    }

    private static Address createAndAddAddress(String name,String streetNum, String streetName,
				String city, String state, String zip) throws Exception { 
	// create an address
	Address address = new Address();
	address.setStreetNum(Integer.valueOf(streetNum).intValue());
	address.setStreetName(streetName);
	address.setCity(city);
	address.setState(state);
	address.setZip(Integer.valueOf(zip).intValue());
	address.setPhoneNumber(null);
	// add an entry to the addressbook
	addressBook.addEntry(name,address);
	return address;
    }

    private static void verifyAddress(Address address) throws Exception { 
	// extract the zip code from the address
	String zipCode = ""+address.getZip();
	// look up information for that zip
	LatLongReturn zipInfo = zip2geo.GetLatLong(zipCode,"");
	if (!zipInfo.getCity().equals(address.getCity())) {
	    printError("Zip "+zipCode+" is in city "+zipInfo.getCity()+
		       ", not city "+address.getCity()+" as you specified");
	}
	if (!zipInfo.getStateAbbrev().equals(address.getState())) {
	    printError("Zip "+zipCode+" is in state "+zipInfo.getStateAbbrev()+
		       ", not state "+address.getState()+" as you specified");
	}
    }   

    private static String serviceIsAvailable(int zipCode) throws Exception { 
	return serviceAvailability.checkAvailability(""+zipCode);
    }

    private static void loopInput() {
	try {
	    System.out.println("************************");
	    System.out.println("WELCOME TO FAST DSL INC.");
	    System.out.println("************************");
	    System.out.println("\n\nInterested in DSL service? Enter your address "+
			       "in the form below and we will check whether we have "+
			       "service available in your area.");
	    System.out.println();
	    System.out.println("If you have previously expressed interest, just enter "+
			       "your name (leave other fields blank) and we will look "+
			       "up the rest of the information "+
			       "in our records");
	    LineNumberReader reader = new LineNumberReader(new InputStreamReader(System.in));
	    System.out.print("Name: ");
	    String name = reader.readLine();
	    System.out.print("Street Number: ");
	    String streetNum = reader.readLine();
	    System.out.print("Street Name: ");
	    String streetName = reader.readLine();
	    System.out.print("City: ");
	    String city = reader.readLine();
	    System.out.print("State: ");
	    String state = reader.readLine();
	    System.out.print("Zip: ");
	    String zip = reader.readLine();
	    System.out.println();
	    System.out.println();
	    Address address = null;
	    // if street is blank, look for name in addressbook service
	    // otherwise assume this is a new user and add information to addressbook
	    if (streetName==null || streetName.equals("")) {
		System.out.println("Looking up address...");
		address = lookupAddress(name);
		if (address==null) {
		    // if address wasn't found, we have a problem
		    printError("Address for "+name+" wasn't found");
		}
	    } else {
		// create address from provided information and add to address book
		System.out.println("Adding address to records...");
		address = createAndAddAddress(name,streetNum,streetName,city,state,zip);
	    }
	    // verify that address is correct
	    System.out.println();
	    System.out.println();
	    System.out.println("Verifying validity of address...");
	    verifyAddress(address);
	    System.out.println();
	    System.out.println();
	    // check if we offer DSL service in that zip code
	    System.out.println("Customer: "+name);
	    System.out.println("Address: "+
			       address.getStreetNum()
			       + " "
			       + address.getStreetName()
			       + ", "
			       + address.getCity()
			       + " "
			       + address.getState()
			       + " "
			       + address.getZip());
	    System.out.println("Checking service availability...");
	    if (serviceIsAvailable(address.getZip()).equals("true")) {
		System.out.println("Yes, we offer service in your area,"+
				   "please call 800 555 FST-DSL to order");
	    } else {
		System.out.println("No, we do not offer service in your area");
	    }
	    System.out.println();
	    System.out.println();
	    System.out.println("Enter 'q' to quit, any other key continue...");
	    String choice = reader.readLine();
	    if (choice.equals("q"))
		System.exit(0);
	} catch (Exception e) {
	    System.out.println("ServiceChecker application got exception "+e);
	    System.out.println("Details:");
	    e.printStackTrace();
	}
    }	

    public static void main(String [] args) {
	try {
	    // we must have three args
	    // args[0] is the location of the Zip2Geo service WSDL
	    // args[1] is the location of the AddressBook service WSDL
	    // args[2] is the location of the ServiceAvailability service WSDL
	    if (args.length!=3) {
		printUsage();
		System.exit(1);
	    }
	    init(args[0],args[1],args[2]);
	} catch (Exception e) {
	    System.out.println("ServiceChecker application got exception "+e);
	    System.out.println("Details:");
	    e.printStackTrace();
	}
	while(true)
	    loopInput();
    }
}

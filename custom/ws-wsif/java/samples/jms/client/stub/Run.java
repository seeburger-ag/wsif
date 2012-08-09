package jms.client.stub;

import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.WSIFException;
import java.rmi.RemoteException;
import jms.client.stub.org.apache.xml.CheckAvailabilityPortType;

/**
 * Class that runs the jms sample using a pregenerated stub interface.
 * To use this class, provide a zip code on the command line. WSIF 
 * should then invoke the JMS service with this information, returning with a 
 * true or false to indicate whether DSL service is avaiulable or not in that 
 * zip code.
 * @author Nirmal K. Mukhi (nmukhi@us.ibm.com)
 */

public class Run {
	
    public static void main(String[] args) {
        try {

            if (args.length != 2) {
                System.out.println(
                    "Usage: java jms.client.stub.Run <wsdl location> <zip code>");
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
                    "http://xml.apache.org/axis/wsif/samples/jms/ServiceAvailability",
                    "CheckAvailabilityPortType");
            // create the stub
            CheckAvailabilityPortType stub = 
                    (CheckAvailabilityPortType) service.getStub(
                        CheckAvailabilityPortType.class);

            // do the invocation
            // args[1] is the zip code
            String serviceAvailable = stub.checkAvailability(args[1]);
            System.out.println(serviceAvailable);

        } catch (WSIFException we) {
            System.out.println(
                "Error while executing sample, received an exception from WSIF; details:");
            we.printStackTrace();
        } catch (RemoteException re) {
            System.out.println(
                "Error while executing sample, received an exception due to remote invocation; details:");
            re.printStackTrace();
        }
    }
}

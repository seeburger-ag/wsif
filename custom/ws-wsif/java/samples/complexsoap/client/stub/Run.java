package complexsoap.client.stub;

import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.WSIFException;
import javax.xml.namespace.QName;
import java.rmi.RemoteException;
import complexsoap.client.stub.com.cdyne.ws.LatLongReturn;
import complexsoap.client.stub.com.cdyne.ws.Zip2GeoSoap;

/**
 * Simple class that Runs the SimpleSOAP sample using a pregenerated stub interface
 * To use this class, provide a company stock symbol on the command line. WSIF 
 * should then invoke the SOAP service with this information, returning with a recent 
 * stockquote.
 * @author Nirmal K. Mukhi (nmukhi@us.ibm.com)
 */

public class Run {
    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                System.out.println(
                    "Usage: java samples.complexsoap.client.stub.Run <wsdl location> <zip code>");
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
                    "http://ws.cdyne.com",
                    "Zip2GeoSoap");

            // map types
            service.mapType(
                new QName("http://ws.cdyne.com", "LatLongReturn"),
                Class.forName(
                    "complexsoap.client.stub.com.cdyne.ws.LatLongReturn"));

            // create the stub
            Zip2GeoSoap stub = (Zip2GeoSoap) service.getStub(Zip2GeoSoap.class);

            // do the invocation
            // args[1] is the zip code
            LatLongReturn zipInfo = stub.GetLatLong(args[1], "");

            System.out.println(
                "This zip code is in "
                    + zipInfo.getCity()
                    + ","
                    + zipInfo.getStateAbbrev()
                    + " in "
                    + zipInfo.getCounty()
                    + " county\n"
                    + "It extends from longitude "
                    + zipInfo.getFromLongitude()
                    + " to longitude "
                    + zipInfo.getToLongitude()
                    + "\n and from latitude "
                    + zipInfo.getFromLatitude()
                    + " to latitude "
                    + zipInfo.getToLatitude());

        } catch (WSIFException we) {
            System.out.println(
                "Error while executing sample, received an exception from WSIF; details:");
            we.printStackTrace();
        } catch (RemoteException re) {
            System.out.println(
                "Error while executing sample, received an exception due to remote invocation; details:");
            re.printStackTrace();
        } catch (ClassNotFoundException ce) {
            System.out.println(
                "Error while executing sample, could not find required class complexsoap.client.stub.com.cdyne.ws.LatLongReturn; please add it to your classpath; details:");
            ce.printStackTrace();
        }
    }
}

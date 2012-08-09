package complexsoap.client.dynamic;

import javax.xml.namespace.QName;

import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;

import complexsoap.client.stub.com.cdyne.ws.LatLongReturn;

public class Run {
    public static void main(String[] args) throws Exception {
    	
        // args[0] is the zip code
        if (args.length != 2) {
            System.out.println(
                "Usage: java complexsoap.client.dynamic.Run <wsdl location> <zip code>");
            System.exit(1);
        }
        
        // create a service factory
        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
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

        // get the port
        WSIFPort port = service.getPort();

        // create the operation
        WSIFOperation operation = port.createOperation("GetLatLong");

        // create the input, output and fault messages associated with this operation
        WSIFMessage input = operation.createInputMessage();
        WSIFMessage output = operation.createOutputMessage();
        WSIFMessage fault = operation.createFaultMessage();

        // populate the input message
        input.setObjectPart("zipcode", args[1]);
        input.setObjectPart("LicenseKey", "");

        // do the invocation
        if (operation.executeRequestResponseOperation(input, output, fault)) {
            // invocation succeeded, extract information from output 
            // message
            LatLongReturn zipInfo =
                (LatLongReturn) output.getObjectPart("GetLatLongResult");
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
        } else {
            System.out.println("Invocation failed");
            // extract fault message info
        }
    }
}

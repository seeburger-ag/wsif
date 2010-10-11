package customfactory.client.stub;

import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.WSIFException;
import java.rmi.RemoteException;
import customfactory.client.stub.com.themindelectric.www.NetXmethodsServicesStockquoteStockQuotePortType;

/**
 * Simple class that Runs the customfactory sample using a pregenerated stub interface
 * To use this class, provide a company stock symbol 
 * on the command line. WSIF should then invoke the service with this information, 
 * using the port returned by the custom factory and returning with a recent stockquote.
 * @author Nirmal K. Mukhi (nmukhi@us.ibm.com)
 */

public class Run {
	
    public static void main(String[] args) {
        try {

            if (args.length != 2) {
                System.out.println(
                    "Usage: java customfactory.client.stub.Run <wsdl location> <company symbol>");
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
                    "http://www.themindelectric.com/wsdl/net.xmethods.services.stockquote.StockQuote/",
                    "net.xmethods.services.stockquote.StockQuotePortType");
            // create the stub
	    // check if the user specified a preferred port
            NetXmethodsServicesStockquoteStockQuotePortType stub = null;
	    stub = (NetXmethodsServicesStockquoteStockQuotePortType) 
		service.getStub(NetXmethodsServicesStockquoteStockQuotePortType.class);

            // do the invocation
            // args[1] is the company symbol
            float quote = stub.getQuote(args[1]);
            System.out.println(quote);

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

package customfactory.client;

import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFException;

import java.util.Map;
import javax.wsdl.*;

public class CustomServiceFactoryImpl extends WSIFServiceFactory {
    public CustomServiceFactoryImpl() {
	System.out.println("Using custom factory");
    }
    /**
     * Create a WSIFService from WSDL document URL.
     * <br> If serviceName or serviceNS is null,
     *   then WSDL document must have exactly one service in it.
     * <br> If portTypeName or portTypeNS is null,
     *   then WSDL document must have exactly one portType in it
     *   and all ports of the selected service must
     *    implement the same portType.
     * @param wsdlLoc The URL for the wsdl's location
     * @param serviceNS The namespace of the service
     * @param serviceName The name of the service
     * @param portTypeNS The namespace of the port type
     * @param portTypeName The name of the port type
     * @return The service
     * @exception A WSIFException if an error occurs when creating the service
     */
    public WSIFService getService(
        String wsdlLoc,
        String serviceNS,
        String serviceName,
        String portTypeNS,
        String portTypeName)
        throws WSIFException {
        CustomServiceImpl wsi =
            new CustomServiceImpl(
                wsdlLoc,
                serviceNS,
                serviceName,
                portTypeNS,
                portTypeName);
        return wsi;
    }

    /**
     * Create a WSIF service instance from WSDL document URL
     * using a ClassLoader to find local resources.
     * <br> If serviceName or serviceNS is null,
     *   then WSDL document must have exactly one service in it.
     * <br> If portTypeName or portTypeNS is null,
     *   then WSDL document must have exactly one portType in it
     *   and all ports of the selected service must
     *    implement the same portType.
     * @param wsdlLoc The URL for the wsdl's location
     * @param cl A ClassLoader to use in locating the wsdl
     * @param serviceNS The namespace of the service
     * @param serviceName The name of the service
     * @param portTypeNS The namespace of the port type
     * @param portTypeName The name of the port type
     * @return The service
     * @exception A WSIFException if an error occurs when creating the service
     */
    public WSIFService getService(
        String wsdlLoc,
        ClassLoader cl,
        String serviceNS,
        String serviceName,
        String portTypeNS,
        String portTypeName)
        throws WSIFException {
        CustomServiceImpl wsi =
            new CustomServiceImpl(
                wsdlLoc,
                cl,
                serviceNS,
                serviceName,
                portTypeNS,
                portTypeName);
        return wsi;
    }

    /**
     * Returns a new WSIFService.
     * @param def The Definition object representing the wsdl
     * @return The service
     * @exception A WSIFException if an error occurs when creating the service
     */
    public WSIFService getService(Definition def) throws WSIFException {
        CustomServiceImpl wsi = new CustomServiceImpl(def);
        return wsi;
    }

    /**
     * Returns a new WSIFService.
     * @param def The Definition object representing the wsdl
     * @param service The Service object representing the service to use
     * @return The service
     * @exception A WSIFException if an error occurs when creating the service
     */
    public WSIFService getService(Definition def, Service service)
        throws WSIFException {
        CustomServiceImpl wsi = new CustomServiceImpl(def, service);
        return wsi;
    }

    /**
     * Returns a new WSIFService.
     * @param def The Definition object representing the wsdl
     * @param service The Service object representing the service to use
     * @param portType The PortType object representing the port type to use
     * @return The service
     * @exception A WSIFException if an error occurs when creating the service
     */
    public WSIFService getService(
        Definition def,
        Service service,
        PortType portType)
        throws WSIFException {
        CustomServiceImpl wsi = new CustomServiceImpl(def, service, portType);
        return wsi;
    }

    /**
     * Returns a new WSIFService.
     * @param def The Definition object representing the wsdl
     * @param serviceNS The namespace of the service
     * @param serviceName The name of the service
     * @param portTypeNS The namespace of the port type
     * @param portTypeName The name of the port type
     * @return The service
     * @exception A WSIFException if an error occurs when creating the service
     */
    public WSIFService getService(
        Definition def,
        String serviceNS,
        String serviceName,
        String portTypeNS,
        String portTypeName)
        throws WSIFException {
        CustomServiceImpl wsi =
            new CustomServiceImpl(
                def,
                serviceNS,
                serviceName,
                portTypeNS,
                portTypeName);
        return wsi;
    }

    /**
     * @see org.apache.wsif.WSIFServiceFactory#setFeature(String, Object)
     */
    public void setFeature(String name, Object value) {
    }    

    /**
     * @see org.apache.wsif.WSIFServiceFactory#setFeatures(Map)
     */
    public void setFeatures(Map map) {
    }    

    /**
     * @see org.apache.wsif.WSIFServiceFactory#getFeature(String)
     */    
    public Object getFeature(String name) {
    	return null;
    }
    
    /**
     * @see org.apache.wsif.WSIFServiceFactory#getFeatures()
     */    
    public Map getFeatures() {
    	return null;
    }
}

/**
 * ZipCodeResolverLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package docStyle.zipCodeNW;

public class ZipCodeResolverLocator extends org.apache.axis.client.Service implements docStyle.zipCodeNW.ZipCodeResolver {

    /**
     * Given a valid street address, city, and state, this service returns
     * the proper ZIP code, ZIP code+4, or USPS corrected address. NOTE:
     * This service is meant for non-commercial, personal use only.
     */

    // Use to get a proxy class for ZipCodeResolverSoap
    private final java.lang.String ZipCodeResolverSoap_address = "http://webservices.eraserver.net/zipcoderesolver/zipcoderesolver.asmx";
//    private final java.lang.String ZipCodeResolverSoap_address = "http://localhost:8080/soap/servlet/rpcrouter";

    public java.lang.String getZipCodeResolverSoapAddress() {
        return ZipCodeResolverSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String ZipCodeResolverSoapWSDDServiceName = "ZipCodeResolverSoap";

    public java.lang.String getZipCodeResolverSoapWSDDServiceName() {
        return ZipCodeResolverSoapWSDDServiceName;
    }

    public void setZipCodeResolverSoapWSDDServiceName(java.lang.String name) {
        ZipCodeResolverSoapWSDDServiceName = name;
    }

    public docStyle.zipCodeNW.ZipCodeResolverSoap getZipCodeResolverSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ZipCodeResolverSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            return null; // unlikely as URL was validated in WSDL2Java
        }
        return getZipCodeResolverSoap(endpoint);
    }

    public docStyle.zipCodeNW.ZipCodeResolverSoap getZipCodeResolverSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            docStyle.zipCodeNW.ZipCodeResolverSoapStub _stub = new docStyle.zipCodeNW.ZipCodeResolverSoapStub(portAddress, this);
            _stub.setPortName(getZipCodeResolverSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (docStyle.zipCodeNW.ZipCodeResolverSoap.class.isAssignableFrom(serviceEndpointInterface)) {
                docStyle.zipCodeNW.ZipCodeResolverSoapStub _stub = new docStyle.zipCodeNW.ZipCodeResolverSoapStub(new java.net.URL(ZipCodeResolverSoap_address), this);
                _stub.setPortName(getZipCodeResolverSoapWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        java.rmi.Remote _stub = getPort(serviceEndpointInterface);
        ((org.apache.axis.client.Stub) _stub).setPortName(portName);
        return _stub;
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://webservices.eraserver.net/", "ZipCodeResolver");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("ZipCodeResolverSoap"));
        }
        return ports.iterator();
    }

}

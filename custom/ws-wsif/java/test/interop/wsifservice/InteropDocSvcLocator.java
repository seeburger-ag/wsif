/**
 * InteropDocSvcLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package interop.wsifservice;

public class InteropDocSvcLocator extends org.apache.axis.client.Service implements interop.wsifservice.InteropDocSvc {

    // Use to get a proxy class for interopDocPort
    private final java.lang.String interopDocPort_address = "http://www.whitemesa.net/interopdoc";

    public java.lang.String getinteropDocPortAddress() {
        return interopDocPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String interopDocPortWSDDServiceName = "interopDocPort";

    public java.lang.String getinteropDocPortWSDDServiceName() {
        return interopDocPortWSDDServiceName;
    }

    public void setinteropDocPortWSDDServiceName(java.lang.String name) {
        interopDocPortWSDDServiceName = name;
    }

    public interop.wsifservice.Doc_TestPortType getinteropDocPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(interopDocPort_address);
        }
        catch (java.net.MalformedURLException e) {
            return null; // unlikely as URL was validated in WSDL2Java
        }
        return getinteropDocPort(endpoint);
    }

    public interop.wsifservice.Doc_TestPortType getinteropDocPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            interop.wsifservice.Doc_Test_BindingStub _stub = new interop.wsifservice.Doc_Test_BindingStub(portAddress, this);
            _stub.setPortName(getinteropDocPortWSDDServiceName());
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
            if (interop.wsifservice.Doc_TestPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                interop.wsifservice.Doc_Test_BindingStub _stub = new interop.wsifservice.Doc_Test_BindingStub(new java.net.URL(interopDocPort_address), this);
                _stub.setPortName(getinteropDocPortWSDDServiceName());
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
        return new javax.xml.namespace.QName("http://soapinterop.org/", "interopDocSvc");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("interopDocPort"));
        }
        return ports.iterator();
    }

}

/**
 * InteropDocSvc.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package interop.wsifservice;

public interface InteropDocSvc extends javax.xml.rpc.Service {
    public java.lang.String getinteropDocPortAddress();

    public interop.wsifservice.Doc_TestPortType getinteropDocPort() throws javax.xml.rpc.ServiceException;

    public interop.wsifservice.Doc_TestPortType getinteropDocPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}

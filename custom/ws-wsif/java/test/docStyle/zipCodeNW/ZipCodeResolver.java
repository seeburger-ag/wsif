/**
 * ZipCodeResolver.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package docStyle.zipCodeNW;

public interface ZipCodeResolver extends javax.xml.rpc.Service {

    /**
     * Given a valid street address, city, and state, this service returns
     * the proper ZIP code, ZIP code+4, or USPS corrected address. NOTE:
     * This service is meant for non-commercial, personal use only.
     */
    public java.lang.String getZipCodeResolverSoapAddress();

    public docStyle.zipCodeNW.ZipCodeResolverSoap getZipCodeResolverSoap() throws javax.xml.rpc.ServiceException;

    public docStyle.zipCodeNW.ZipCodeResolverSoap getZipCodeResolverSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}

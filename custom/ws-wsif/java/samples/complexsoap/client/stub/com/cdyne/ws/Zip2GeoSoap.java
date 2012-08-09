/**
 * Zip2GeoSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package complexsoap.client.stub.com.cdyne.ws;

public interface Zip2GeoSoap extends java.rmi.Remote {

    /**
     * This method will convert a zip code to Longitude and Latitude.  You
     * will get better accuracy with the plus 4 added to the zipcode.  Use
     * a license key of 0 for testing.
     */
    public complexsoap.client.stub.com.cdyne.ws.LatLongReturn GetLatLong(java.lang.String zipcode, java.lang.String licenseKey) throws java.rmi.RemoteException;
}

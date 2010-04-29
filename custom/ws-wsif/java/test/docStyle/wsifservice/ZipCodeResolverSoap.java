/**
 * ZipCodeResolverSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package docStyle.wsifservice;

public interface ZipCodeResolverSoap extends java.rmi.Remote {

    /**
     * Given a valid street address, city, and state, returns the ZIP code
     * in NNNNN format. If an error occurs, returns 00000 instead. Use accessCode
     * of '0' or '9999' for testing.
     */
    public java.lang.String ShortZipCode(java.lang.String accessCode, java.lang.String address, java.lang.String city, java.lang.String state) throws java.rmi.RemoteException;
}

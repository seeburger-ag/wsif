/**
 * ZipCodeResolverSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package docStyle.zipCodeNW;

public interface ZipCodeResolverSoap extends java.rmi.Remote {

    /**
     * Given a valid street address, city, and state, returns the ZIP code
     * in NNNNN format. If an error occurs, returns 00000 instead. Use accessCode
     * of '0' or '9999' for testing.
     */
    public docStyle.zipCodeNW.ShortZipCodeResponse shortZipCode(docStyle.zipCodeNW.ShortZipCode parameters) throws java.rmi.RemoteException;
}

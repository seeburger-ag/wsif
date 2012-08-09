/**
 * AddressBook.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package localjava.client.stub.addressbook.wsifservice;

public interface AddressBook extends java.rmi.Remote {
    public void addEntry(java.lang.String name, localjava.client.stub.addressbook.wsiftypes.Address address) throws java.rmi.RemoteException;
    public void addEntry(java.lang.String firstName, java.lang.String lastName, localjava.client.stub.addressbook.wsiftypes.Address address) throws java.rmi.RemoteException;
    public localjava.client.stub.addressbook.wsiftypes.Address getAddressFromName(java.lang.String name) throws java.rmi.RemoteException;
}

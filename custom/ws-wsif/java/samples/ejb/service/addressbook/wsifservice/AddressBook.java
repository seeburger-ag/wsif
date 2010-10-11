/**
 * AddressBook.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package ejb.service.addressbook.wsifservice;

public interface AddressBook extends java.rmi.Remote {
    public void addEntry(java.lang.String name, ejb.service.addressbook.wsiftypes.Address address) throws java.rmi.RemoteException;
    public void addEntry(java.lang.String firstName, java.lang.String lastName, ejb.service.addressbook.wsiftypes.Address address) throws java.rmi.RemoteException;
    public ejb.service.addressbook.wsiftypes.Address getAddressFromName(java.lang.String name) throws java.rmi.RemoteException;
}

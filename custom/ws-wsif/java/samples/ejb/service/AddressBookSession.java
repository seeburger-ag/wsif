/*
 * Generated file - Do not edit!
 */
package ejb.service;

import java.lang.*;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import ejb.service.addressbook.wsiftypes.Address;

/**
 * Remote interface for service/AddressBookSession.
 * @xdoclet-generated at Dec 13, 2002 1:41:52 PM
 */
public interface AddressBookSession
   extends javax.ejb.EJBObject
{

   public void addEntry( java.lang.String name,ejb.service.addressbook.wsiftypes.Address address ) throws java.rmi.RemoteException;

   public void addEntry( java.lang.String firstName,java.lang.String lastName,ejb.service.addressbook.wsiftypes.Address address ) throws java.rmi.RemoteException;

   public ejb.service.addressbook.wsiftypes.Address getAddressFromName( java.lang.String name ) throws java.lang.IllegalArgumentException, java.rmi.RemoteException;

}

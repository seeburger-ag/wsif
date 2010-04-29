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
 * Home interface for service/AddressBookSession. Lookup using {1}
 * @xdoclet-generated at Dec 13, 2002 1:41:52 PM
 */
public interface AddressBookSessionHome
   extends javax.ejb.EJBHome
{
   public static final String COMP_NAME="java:comp/env/ejb/service/AddressBookSession";
   public static final String JNDI_NAME="ejb/service/AddressBook";

   /**
    * Create the Session Bean
    * @throws CreateException
    */
   public ejb.service.AddressBookSession create() throws java.rmi.RemoteException,javax.ejb.CreateException;

}

/*
 * Copyright 2002-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2001, 2002, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package ejb.service;

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
 * AddressBook Session Bean
 *
 * ATTENTION: Some of the XDoclet tags are hidden from XDoclet by
 *            adding a "--" between @ and the namespace. Please remove
 *            this "--" to make it active or add a space to make an
 *            active tag inactive.
 *
 * @ejb:bean name="service/AddressBookSession"
 *           display-name="AddressBook Bean"
 *           type="Stateful"
 *           transaction-type="Container"
 *           jndi-name="ejb/service/AddressBook"
 *
 * @ejb:ejb-ref ejb-name="service/AddressBookSession"
 *              ref-name="myservice/AddressBook"
 *
 * @ejb:resource-ref res-name="test/Mail"
 *                   res-type="javax.mail.Session"
 *                   res-auth="Container"
 *
 * @jboss:resource-manager res-man-class="javax.mail.Session"
 *                         res-man-name="test/Mail"
 *                         res-man-jndi-name="java:Mail"
 **/
public class AddressBookSessionBean implements SessionBean {
    private HashMap name2AddressTable = new HashMap();
    private SessionContext mContext;

    /**
     * @ejb:interface-method view-type="remote"
     **/
    public void addEntry(String name, Address address)
    {
	name2AddressTable.put(name, address);
    }

    /**
     * @ejb:interface-method view-type="remote"
     **/
    public void addEntry(String firstName, String lastName, Address address)
    {
	name2AddressTable.put(firstName+" "+lastName, address);
    }

    /**
     * @ejb:interface-method view-type="remote"
     **/
    public Address getAddressFromName(String name)
	throws IllegalArgumentException
    {
	return (Address)name2AddressTable.get(name);
    }

    /**
     * Create the Session Bean
     *
     * @throws CreateException 
     *
     * @ejb:create-method view-type="remote"
     **/
    public void ejbCreate()
	throws
	    CreateException
    {
	System.out.println( "AddressBookSessionBean.ejbCreate()" );
    }
   
    /**
     * Describes the instance and its content for debugging purpose
     *
     * @return Debugging information about the instance and its content
     **/
    public String toString()
    {
	return "AddressBookSessionBean [ " + " ]";
    }
   
   
    // -------------------------------------------------------------------------
    // Framework Callbacks
    // -------------------------------------------------------------------------  
   
    public void setSessionContext( SessionContext aContext )
	throws
	    EJBException
    {
	mContext = aContext;
    }
   
    public void ejbActivate()
	throws
	    EJBException
    {
    }
   
    public void ejbPassivate()
	throws
	    EJBException
    {
    }
   
    public void ejbRemove()
	throws
	    EJBException
    {
    }
}


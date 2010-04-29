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

package shop;

/**
 * This is a Home interface for the Session Bean
 */
public interface ShoppingCartHome extends javax.ejb.EJBHome {

/**
 * create method for a session bean
 * @return shop.ShoppingCart
 * @exception javax.ejb.CreateException The exception description.
 * @exception java.rmi.RemoteException The exception description.
 */

public shop.ShoppingCart create() throws javax.ejb.CreateException, java.rmi.RemoteException;

/**
 * 
 * @return shop.ShoppingCart
 * @param firstName java.lang.String
 * @param lastName java.lang.String
 * @param address shop.Address
 * @param customerNumber java.lang.String
 * @exception String The exception description.
 * @exception String The exception description.
 * @exception String The exception description.
 */
public shop.ShoppingCart create(java.lang.String firstName, java.lang.String lastName, shop.Address address, java.lang.String customerNumber) throws javax.ejb.CreateException, java.rmi.RemoteException, shop.CreateException;
}

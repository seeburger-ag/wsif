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
 * This is an Enterprise Java Bean Remote Interface
 */
public interface ShoppingCart extends javax.ejb.EJBObject {

/**
 * 
 * @return org.apache.wsif.test.shop.ejb.Item
 * @param itemNumber java.lang.String
 * @param itemName java.lang.String
 * @param itemQuantity int
 * @exception String The exception description.
 * @exception String The exception description.
 * @exception String The exception description.
 */
public Item addItem(java.lang.String itemNumber, java.lang.String itemName, int itemQuantity) throws OutOfStockException, java.rmi.RemoteException, javax.ejb.EJBException, InvalidItemException;

/**
 * 
 * @return org.apache.wsif.test.shop.ejb.SubmitOrderResult
 * @param creditCardInfo org.apache.wsif.test.shop.ejb.CreditCardInfo
 * @param airMilesContainer org.apache.wsif.test.shop.ejb.AirMilesContainer
 * @exception String The exception description.
 */
public SubmitOrderResult submitOrder(CreditCardInfo creditCardInfo,AirMilesContainer airMilesContainer) throws javax.ejb.EJBException, java.rmi.RemoteException;

public void emptyOrder(String customerNumber) throws javax.ejb.EJBException, java.rmi.RemoteException;
}

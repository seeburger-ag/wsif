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

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
/**
 * Creation date: (8/4/2001 2:25:19 PM)
 * @author <a href="mailto:gpfau@de.ibm.com">Gerhard Pfau</a>
 */
public class ShoppingCartBean implements SessionBean {
    private javax.ejb.SessionContext mySessionCtx = null;

    private int fieldTotal = 0;

    private String fieldFirstName;
    private String fieldLastName;
    private Address fieldAddress;
    private String fieldCustomerNumber;

    private Order fieldOrder;

    public Item addItem(String itemNumber, String itemName, int itemQuantity)
        throws javax.ejb.EJBException, InvalidItemException, OutOfStockException {
        Item item = new Item();

        if (itemQuantity > 10)
            throw new OutOfStockException(
                "There are less then '"
                    + itemQuantity
                    + "' pieces of '"
                    + itemName
                    + "' in stock");

        if ((itemNumber == null)
            || (itemName == null)
            || (itemNumber.equals(""))
            || (itemName.equals("")))
            throw new InvalidItemException(
                "Invalid item: Number = '" + itemNumber + "' Name = '" + itemName + "'");

        item.setNumber(itemNumber);
        item.setName(itemName);
        item.setQuantity(itemQuantity);
        item.setPrice(100); // at the moment everything costs 100 currency units :-)

        fieldOrder.addItem(itemNumber, item);

        fieldTotal += item.getPrice();

        return item;
    }

    /**
     * ejbActivate method comment
     * @exception java.rmi.RemoteException The exception description.
     */
    public void ejbActivate() throws javax.ejb.EJBException {
    }

    public void ejbCreate()
        throws javax.ejb.CreateException, javax.ejb.EJBException {
        fieldFirstName = null;
        fieldLastName = null;
        fieldAddress = null;
        fieldCustomerNumber = null;
        fieldOrder = null;
    }

    public void ejbCreate(
        String firstName,
        String lastName,
        Address address,
        String customerNumber)
        throws javax.ejb.CreateException, javax.ejb.EJBException, shop.CreateException {
        fieldFirstName = firstName;
        fieldLastName = lastName;
        fieldAddress = address;
        fieldCustomerNumber = customerNumber;

        fieldOrder = new Order(customerNumber);
    }

    /**
     * ejbPassivate method comment
     * @exception java.rmi.RemoteException The exception description.
     */
    public void ejbPassivate() throws javax.ejb.EJBException {
    }
    /**
     * ejbRemove method comment
     * @exception java.rmi.RemoteException The exception description.
     */

    public void ejbRemove() throws javax.ejb.EJBException {
    }
    /**
     * setSessionContext method comment
     * @param ctx javax.ejb.SessionContext
     * @exception java.rmi.RemoteException The exception description.
     */

    public void setSessionContext(javax.ejb.SessionContext ctx)
        throws javax.ejb.EJBException {
        mySessionCtx = ctx;
    }

    public SubmitOrderResult submitOrder(
        CreditCardInfo creditCardInfo,
        AirMilesContainer airMilesContainer)
        throws javax.ejb.EJBException {
        long orderConfirmationNumber = System.currentTimeMillis();
        fieldOrder.setConfirmationNumber(orderConfirmationNumber);
        // fieldAllOrders.add(fieldOrder) ;
        fieldOrder = null;
        airMilesContainer.addMiles(1000);

        return new SubmitOrderResult(airMilesContainer, orderConfirmationNumber);
    }

    public void emptyOrder(String customerNumber) throws javax.ejb.EJBException {
        //ignore the customer number;
        fieldOrder.empty();
        fieldTotal = 0;
    }
}

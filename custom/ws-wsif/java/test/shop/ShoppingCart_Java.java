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

import java.util.*;
/**
 * Creation date: (8/4/2001 2:25:19 PM)
 * @author <a href="mailto:gpfau@de.ibm.com">Gerhard Pfau</a>
 */
public class ShoppingCart_Java {
    private int fieldTotal = 0;

    private String fieldFirstName;
    private String fieldLastName;
    private Address fieldAddress;
    private String fieldCustomerNumber;

    private Order fieldOrder;

    private static Orders fieldAllOrders = new Orders();
    
    public ShoppingCart_Java() {
        super();
    }
    
    public ShoppingCart_Java(
        String firstName,
        String lastName,
        Address address,
        String customerNumber)
        throws CreateException {
        super();

        fieldFirstName = firstName;
        fieldLastName = lastName;
        fieldAddress = address;
        fieldCustomerNumber = customerNumber;

        fieldOrder = new Order(customerNumber);
    }
    
    public int addItem(
        String itemNumber,
        Item item,
        String itemName,
        int itemQuantity)
        throws InvalidItemException, OutOfStockException {
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

        fieldTotal += (item.getPrice()*item.getQuantity());

        return fieldTotal;
    }
    
    public static Orders queryOrders(String customerNumber)
        throws EmptyResultException {
        return fieldAllOrders.getOrders(customerNumber);
    }
    
    public void emptyOrder(String customerNumber)
    {
    	fieldOrder.empty();
    	fieldTotal = 0;
    }
    
    public long submitOrder(
        CreditCardInfo creditCardInfo,
        AirMilesContainer airMilesContainer) {
        long orderConfirmationNumber = System.currentTimeMillis();
        fieldOrder.setConfirmationNumber(orderConfirmationNumber);
        fieldAllOrders.add(fieldOrder);
        fieldOrder = null;
        airMilesContainer.addMiles(1000);
        return orderConfirmationNumber;
    }
}

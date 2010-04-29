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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import util.TestUtilities;

/**
 * Shopping cart test scenario using Java & EJB invocation.
 * 
 * In order to run this test, set your JNDI settings using the following variables:
 * <UL>
 * <LI>java.naming.provider.url (javax.naming.Context.PROVIDER_URL)</LI>
 * <LI>java.naming.factory.initial (javax.naming.Context.INITIAL_CONTEXT_FACTORY)</LI>
 * </UL>
 *  
 * @author Owen Burroughs
 */

public class ShoppingCartTest extends TestCase {

    private boolean debugMode = true;
    //private boolean debugMode = false;
    private String wsdlPath;

    /**
     * Starts the application.
     * @param args an array of command-line arguments
     */
    public static void main(java.lang.String[] args) {
	   junit.textui.TestRunner.run (suite());
    }

    public static Test suite() {
        return new TestSuite(ShoppingCartTest.class);
    }

    public ShoppingCartTest(String arg0) {
        super(arg0);
    }

    protected void setUp() {
        wsdlPath = TestUtilities.getWsdlPath("java\\test\\shop");
    }

    private void printOrder(Order order) {
        HashMap items = null;
        Iterator it = null;
        Collection entries = null;
        Item item = null;
        Object obj = null;

        debug("\nOrder " + order.getConfirmationNumber());
        items = order.getItems();
        entries = items.values();
        it = entries.iterator();
        while (it.hasNext()) {
            debug(it.next());
        }
    }

    private void printOrders(Orders orders) {
        int size = orders.size();

        for (int i = 0; i < size; i++) {
            printOrder((Order) orders.get(i));
        }
    }

    /**
     * Tests the Java binding for the shopping cart scenario via WSDL
     */
    public void testWSDL_Java() throws Exception {
        debug("\n*** TEST JAVA BINDING ***");

        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory
                    .getService(
                        wsdlPath + "ShoppingCartAll.wsdl",
                        "http://www.shoppingcart.com/definitions/ShoppingCartInterface",
        // serviceNS,
        "ShoppingCart_JavaService", // serviceName,
        "http://www.shoppingcart.com/definitions/ShoppingCartInterface",
        // portTypeNS,
        "ShoppingCart_JavaPortType" //  portTypeName
    );

        Iterator it = service.getAvailablePortNames();
        {
            System.out.println("Available ports for the service are: ");
            while (it.hasNext()) {
                System.out.println((String) it.next());
            }
        }

        WSIFPort port = service.getPort();

        WSIFOperation operation;
        WSIFMessage inputMessage;
        WSIFMessage outputMessage;
        WSIFMessage faultMessage;

        String customerNumber;
        String tempString;
        Address address;
        ShoppingCart_Java shoppingCart;
        Item item;
        CreditCardInfo creditCardInfo;
        AirMilesContainer airMilesContainer;
        Integer currentTotal = null;
        Long orderConfirmationNumber;
        Integer itemQuantity;
        Object part;
        boolean operationSucceeded;

        // -----------------------------------------------------------------------------------------------------
        operation = port.createOperation("createOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        debug("\n---> Invocation: public ShoppingCart_Java(String firstName, String lastName, Address address, String customerNumber)");

        tempString = "Albert";
        inputMessage.setObjectPart("firstName", tempString);

        tempString = "Einstein";
        inputMessage.setObjectPart("lastName", tempString);

        address = new Address("Berlin", "Unter den Linden");
        inputMessage.setObjectPart("address", address);

        customerNumber = "AE001";
        inputMessage.setObjectPart("customerNumber", customerNumber);

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue("Failed to create a ShoppingCart", operationSucceeded);

        // -----------------------------------------------------------------------------------------------------
        operation = port.createOperation("addItemOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        debug("\n---> Invocation: public int addItem(String itemNumber, Item item, String itemName, int itemQuantity)");

        tempString = "100123";
        inputMessage.setObjectPart("itemNumber", tempString);

        item = new Item();
        inputMessage.setObjectPart("item", item);

        tempString = "Pocket calculator";
        inputMessage.setObjectPart("itemName", tempString);

        itemQuantity = new Integer(1);
        inputMessage.setObjectPart("itemQuantity", itemQuantity);

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue("addItem test failed", operationSucceeded);

        if (operationSucceeded) {
			assertTrue("Part is not an Item!!!",
				outputMessage.getObjectPart("item") instanceof Item);
            debug(outputMessage.getObjectPart("item"));
        } else {
            // Cannot get here since test would have already failed!
        }

        // -----------------------------------------------------------------------------------------------------
        operation = port.createOperation("addItemOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        debug("\n---> Invocation: public int addItem(String itemNumber, Item item, String itemName, int itemQuantity)");

        tempString = "234123";
        inputMessage.setObjectPart("itemNumber", tempString);

        item = new Item();
        inputMessage.setObjectPart("item", item);

        tempString = "Pencil";
        inputMessage.setObjectPart("itemName", tempString);

        itemQuantity = new Integer(12);
        inputMessage.setObjectPart("itemQuantity", itemQuantity);

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue(
            "addItem succeeded when it should have thrown a outOfStockException",
            !operationSucceeded);

        if (operationSucceeded) {
            debug(outputMessage.getObjectPart("item"));
        } else {
            assertNotNull(
                "outputMessage should have contained outOfStockException",
                faultMessage.getObjectPart("outOfStockException"));

            // Extra steps useful for debugging
            /*            part = faultMessage.getObjectPart("invalidItemException");
                        if (part != null) {
                            debug(faultMessage.getName() + ":\n" + part);
                        } else {
                            part = faultMessage.getObjectPart("outOfStockException");
                            if (part != null) {
                                debug(faultMessage.getName() + ":\n" + part);
                            } else {
                                debug("ERROR: Unknown fault message!");
                            }
                        }
            */
        }

        // -----------------------------------------------------------------------------------------------------
        operation = port.createOperation("addItemOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        debug("\n---> Invocation: public int addItem(String itemNumber, Item item, String itemName, int itemQuantity)");

        inputMessage.setObjectPart("itemNumber", "234123");

        item = new Item();
        inputMessage.setObjectPart("item", item);

        inputMessage.setObjectPart("itemName", "Pencil");

        inputMessage.setIntPart("itemQuantity", 8);

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue("addItem failed when it should have succeeded", operationSucceeded);

        //Extra steps useful for debugging
        /*        part = faultMessage.getObjectPart("invalidItemException");
                if (operationSucceeded) {
                    debug(outputMessage.getObjectPart("item"));
                } else {
                    part = faultMessage.getObjectPart("invalidItemException");
                    if (part != null) {
                        debug(faultMessage.getName() + ":\n" + part);
                    } else {
                        part = faultMessage.getObjectPart("outOfStockException");
                        if (part != null) {
                            debug(faultMessage.getName() + ":\n" + part);
                        } else {
                            debug("ERROR: Unknown fault message!");
                        }
                    }
                }*/

        // -----------------------------------------------------------------------------------------------------
        operation = port.createOperation("submitOrderOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        debug("\n---> Invocation: public long submitOrder(CreditCardInfo creditCardInfo, AirMilesContainer airMilesContainer)");

        creditCardInfo = new CreditCardInfo();
        inputMessage.setObjectPart("creditCardInfo", creditCardInfo);

        airMilesContainer = new AirMilesContainer();
        inputMessage.setObjectPart("airMilesContainer", airMilesContainer);

        inputMessage.setLongPart("orderConfirmationNumber", 0);

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue(
            "submitOrderOperation failed when it should have succeeded.",
            operationSucceeded);

        debug(
            "order confirmation no. = "
                + outputMessage.getObjectPart("orderConfirmationNumber"));
        debug(airMilesContainer);

        // -----------------------------------------------------------------------------------------------------
        operation = port.createOperation("queryOrdersOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        debug("\n---> Invocation: public static Orders queryOrders(String customerNumber)");

        inputMessage.setObjectPart("customerNumber", "AE001");

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue(
            "queryOrdersOperation failed when it should have succeeded.",
            operationSucceeded);

        printOrders((Orders) outputMessage.getObjectPart("orders"));

        // -----------------------------------------------------------------------------------------------------
    }

    /**
     * Tests the EJB binding for the shopping cart scenario via WSDL
     */
    public void testWSDL_EJB() throws Exception {
        if (!TestUtilities.areWeTesting("ejb"))
            return;

        debug("\n*** TEST EJB BINDING ***");

        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory.getService(wsdlPath + "ShoppingCartAll.wsdl", // WSDL file
        "http://www.shoppingcart.com/definitions/ShoppingCartInterface", // serviceNS,
        "ShoppingCart_EJBService", // serviceName,
        "http://www.shoppingcart.com/definitions/ShoppingCartInterface",
        // portTypeNS,
        "ShoppingCart_EJBPortType" //  portTypeName
    );

        Iterator it = service.getAvailablePortNames();
        {
            System.out.println("Available ports for the service are: ");
            while (it.hasNext()) {
                System.out.println((String) it.next());
            }
        }

        WSIFPort port = service.getPort();
        WSIFOperation operation;
        WSIFMessage inputMessage;
        WSIFMessage outputMessage;
        WSIFMessage faultMessage;

        String customerNumber;
        String tempString;
        Address address;
        ShoppingCart shoppingCart;
        Item item = null;
        CreditCardInfo creditCardInfo;
        AirMilesContainer airMilesContainer;
        Integer currentTotal = null;
        Long orderConfirmationNumber;
        Integer itemQuantity;
        Object part;
        boolean operationSucceeded;

        // -----------------------------------------------------------------------------------------------------
        operation = port.createOperation("createOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        debug("\n---> Invocation (home): public ShoppingCart create(java.lang.String firstName, java.lang.String lastName, org.apache.wsif.ejb.sample.shop.Address address, java.lang.String customerNumber) throws org.apache.wsif.ejb.sample.shop.CreateException, javax.ejb.CreateException, java.rmi.RemoteException");

        inputMessage.setObjectPart("firstName", "Albert");

        inputMessage.setObjectPart("lastName", "Einstein");

        address = new Address("Berlin", "Unter den Linden");
        inputMessage.setObjectPart("address", address);

        inputMessage.setObjectPart("customerNumber", "AE001");

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue("createOperation failed!!", operationSucceeded);

        // -----------------------------------------------------------------------------------------------------
        operation = port.createOperation("addItemOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        debug("\n---> Invocation (remote): public Item addItem(java.lang.String itemNumber, java.lang.String itemName, int itemQuantity) throws org.apache.wsif.ejb.sample.shop.OutOfStockException, java.rmi.RemoteException, org.apache.wsif.ejb.sample.shop.InvalidItemException");

        inputMessage.setObjectPart("itemNumber", "100123");

        inputMessage.setObjectPart("itemName", "Pocket calculator");

        inputMessage.setIntPart("itemQuantity", 1);

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue("addItemOperation failed!!", operationSucceeded);

        if (operationSucceeded) {
            assertTrue(
                "addItemOperation (EJB) did not return an Item Object!!",
                outputMessage.getObjectPart("item") instanceof Item);
            debug(outputMessage.getObjectPart("item"));
        } else {
            part = faultMessage.getObjectPart("invalidItemException");
            if (part != null) {
                debug(faultMessage.getName() + ":\n" + part);
            } else {
                part = faultMessage.getObjectPart("outOfStockException");
                if (part != null) {
                    debug(faultMessage.getName() + ":\n" + part);
                } else {
                    debug("ERROR: Unknown fault message!");
                }
            }
        }

        // -----------------------------------------------------------------------------------------------------
        operation = port.createOperation("addItemOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        debug("\n---> Invocation (remote): public Item addItem(java.lang.String itemNumber, java.lang.String itemName, int itemQuantity) throws org.apache.wsif.ejb.sample.shop.OutOfStockException, java.rmi.RemoteException, org.apache.wsif.ejb.sample.shop.InvalidItemException");

        inputMessage.setObjectPart("itemNumber", "234123");

        inputMessage.setObjectPart("itemName", "Pencil");

        inputMessage.setIntPart("itemQuantity", 12);

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue(
            "addItemOperation succeeded when it should have thrown an outOfStockException",
            !operationSucceeded);

        if (operationSucceeded) {
            assertTrue(
                "addItemOperation (EJB) did not return an Item Object!!",
                outputMessage.getObjectPart("item") instanceof Item);
            debug(outputMessage.getObjectPart("item"));
        } else {
            assertNotNull(
                "outputMessage should have contained outOfStockException",
                faultMessage.getObjectPart("outOfStockException"));

            /*            part = faultMessage.getObjectPart("invalidItemException");
                        if (part != null) {
                            debug(faultMessage.getName() + ":\n" + part);
                        } else {
                            part = faultMessage.getObjectPart("outOfStockException");
                            if (part != null) {
                                debug(faultMessage.getName() + ":\n" + part);
                            } else {
                                debug("ERROR: Unknown fault message!");
                            }
                        }
            */
        }

        // -----------------------------------------------------------------------------------------------------
        operation = port.createOperation("addItemOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        debug("\n---> Invocation (remote): public Item addItem(java.lang.String itemNumber, java.lang.String itemName, int itemQuantity) throws org.apache.wsif.ejb.sample.shop.OutOfStockException, java.rmi.RemoteException, org.apache.wsif.ejb.sample.shop.InvalidItemException");

        inputMessage.setObjectPart("itemNumber", "234123");

        inputMessage.setObjectPart("itemName", "Pencil");

        inputMessage.setIntPart("itemQuantity", 8);

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue("addItemOperation failed!!", operationSucceeded);

        if (operationSucceeded) {
            assertTrue(
                "addItemOperation (EJB) did not return an Item Object!!",
                outputMessage.getObjectPart("item") instanceof Item);
            debug(outputMessage.getObjectPart("item"));
        } else {
            part = faultMessage.getObjectPart("invalidItemException");
            if (part != null) {
                debug(faultMessage.getName() + ":\n" + part);
            } else {
                part = faultMessage.getObjectPart("outOfStockException");
                if (part != null) {
                    debug(faultMessage.getName() + ":\n" + part);
                } else {
                    debug("ERROR: Unknown fault message!");
                }
            }
        }

        // -----------------------------------------------------------------------------------------------------
        operation = port.createOperation("submitOrderOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        debug("\n---> Invocation (remote): public SubmitOrderResult submitOrder(org.apache.wsif.ejb.sample.shop.CreditCardInfo creditCardInfo, org.apache.wsif.ejb.sample.shop.AirMilesContainer airMilesContainer) throws java.rmi.RemoteException");

        creditCardInfo = new CreditCardInfo();
        inputMessage.setObjectPart("creditCardInfo", creditCardInfo);

        airMilesContainer = new AirMilesContainer();
        inputMessage.setObjectPart("airMilesContainer", airMilesContainer);

        inputMessage.setLongPart("orderConfirmationNumber", 0);

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue("submitOrderOperation failed!!", operationSucceeded);

        debug(outputMessage.getObjectPart("submitOrderResult"));

        // -----------------------------------------------------------------------------------------------------

    }

    /**
    * Tests input only operations on the Java binding for the shopping cart scenario via WSDL
    */
    public void testWSDL_Java_InputOnly() throws Exception {
        debug("\n*** TEST INPUT ONLY OPERATION USING JAVA BINDING ***");

        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory
                    .getService(
                        wsdlPath + "ShoppingCartAll.wsdl",
                        "http://www.shoppingcart.com/definitions/ShoppingCartInterface",
        // serviceNS,
        "ShoppingCart_JavaService", // serviceName,
        "http://www.shoppingcart.com/definitions/ShoppingCartInterface",
        // portTypeNS,
        "ShoppingCart_JavaPortType" //  portTypeName
    );

        WSIFPort port = service.getPort();

        WSIFOperation operation;
        WSIFMessage inputMessage;
        WSIFMessage outputMessage;
        WSIFMessage faultMessage;

        String customerNumber;
        String tempString;
        Address address;
        ShoppingCart_Java shoppingCart;
        Item item;
        CreditCardInfo creditCardInfo;
        AirMilesContainer airMilesContainer;
        Integer currentTotal = null;
        Long orderConfirmationNumber;
        Integer itemQuantity;
        Object part;
        boolean operationSucceeded;

        // -----------------------------------------------------------------------------------------------------
        operation = port.createOperation("createOperation");
        inputMessage = operation.createInputMessage();

        debug("\n---> Invocation: public ShoppingCart_Java(String firstName, String lastName, Address address, String customerNumber)");

        inputMessage.setObjectPart("firstName", "Albert");

        inputMessage.setObjectPart("lastName", "Einstein");

        address = new Address("Berlin", "Unter den Linden");
        inputMessage.setObjectPart("address", address);

        inputMessage.setObjectPart("customerNumber", "AE001");

        operation.executeInputOnlyOperation(inputMessage);

        // -----------------------------------------------------------------------------------------------------
        // First add something to the basket
        operation = port.createOperation("addItemOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        debug("\n---> Invocation: public int addItem(String itemNumber, Item item, String itemName, int itemQuantity)");

        inputMessage.setObjectPart("itemNumber", "100123");

        item = new Item();
        inputMessage.setObjectPart("item", item);

        inputMessage.setObjectPart("itemName", "Pocket calculator");

        inputMessage.setIntPart("itemQuantity", 1);

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue("addItem test failed", operationSucceeded);

        if (operationSucceeded) {
        	assertTrue(
                "addItemOperation (EJB) did not return an Item Object!!",
                outputMessage.getObjectPart("item") instanceof Item);
            debug(outputMessage.getObjectPart("item"));
            debug("Current total = " + outputMessage.getObjectPart("currentTotal"));
        } else {
            // Cannot get here since test would have already failed!
        }

        // -----------------------------------------------------------------------------------------------------
        // Test an "Input Only" operation by invoking the emptyBasket method
        operation = port.createOperation("emptyOrderOperation");
        inputMessage = operation.createInputMessage();

        debug("\n---> Invocation: public void emptyOrder(String customerNumber)");

        inputMessage.setObjectPart("customerNumber", "AE001");

        operation.executeInputOnlyOperation(inputMessage);

        // -----------------------------------------------------------------------------------------------------
        // Add to the basket again - the basket should be empty before this if previous operation worked!!
        operation = port.createOperation("addItemOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        debug("\n---> Invocation: public int addItem(String itemNumber, Item item, String itemName, int itemQuantity)");

        inputMessage.setObjectPart("itemNumber", "100123");

        item = new Item();
        inputMessage.setObjectPart("item", item);

        inputMessage.setObjectPart("itemName", "Pocket calculator");

        inputMessage.setIntPart("itemQuantity", 5);

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue("addItem test failed", operationSucceeded);

        if (operationSucceeded) {
            assertTrue(
                "addItemOperation (EJB) did not return an Item Object!!",
                outputMessage.getObjectPart("item") instanceof Item);                
            debug(outputMessage.getObjectPart("item"));
            debug("Current total = " + outputMessage.getObjectPart("currentTotal"));
        } else {
            // Cannot get here since test would have already failed!
        }
    }

    /**
    * Tests input only operations on the EJB binding for the shopping cart scenario via WSDL
    */
    public void testWSDL_EJB_InputOnly() throws Exception {
        if (!TestUtilities.areWeTesting("ejb"))
            return;

        debug("\n*** TEST INPUT ONLY OPERATION USING EJB BINDING ***");

        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            WSIFService service =
                factory
                    .getService(
                        wsdlPath + "ShoppingCartAll.wsdl",
                        "http://www.shoppingcart.com/definitions/ShoppingCartInterface",
        // serviceNS,
        "ShoppingCart_EJBService", // serviceName,
        "http://www.shoppingcart.com/definitions/ShoppingCartInterface",
        // portTypeNS,
        "ShoppingCart_EJBPortType" //  portTypeName
    );

        WSIFPort port = service.getPort();

        WSIFOperation operation;
        WSIFMessage inputMessage;
        WSIFMessage outputMessage;
        WSIFMessage faultMessage;

        String customerNumber;
        String tempString;
        Address address;
        ShoppingCart shoppingCart;
        Item item = null;
        CreditCardInfo creditCardInfo;
        AirMilesContainer airMilesContainer;
        Integer currentTotal = null;
        Long orderConfirmationNumber;
        Integer itemQuantity;
        Object part;
        boolean operationSucceeded;

        // -----------------------------------------------------------------------------------------------------
        operation = port.createOperation("createOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        debug("\n---> Invocation (home): public ShoppingCart create(java.lang.String firstName, java.lang.String lastName, org.apache.wsif.ejb.sample.shop.Address address, java.lang.String customerNumber) throws org.apache.wsif.ejb.sample.shop.CreateException, javax.ejb.CreateException, java.rmi.RemoteException");

        inputMessage.setObjectPart("firstName", "Albert");

        inputMessage.setObjectPart("lastName", "Einstein");

        address = new Address("Berlin", "Unter den Linden");
        inputMessage.setObjectPart("address", address);

        inputMessage.setObjectPart("customerNumber", "AE001");

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue("createOperation failed!!", operationSucceeded);

        // -----------------------------------------------------------------------------------------------------
        // First add something to the basket
        operation = port.createOperation("addItemOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        debug("\n---> Invocation (remote): public Item addItem(java.lang.String itemNumber, java.lang.String itemName, int itemQuantity) throws org.apache.wsif.ejb.sample.shop.OutOfStockException, java.rmi.RemoteException, org.apache.wsif.ejb.sample.shop.InvalidItemException");

        inputMessage.setObjectPart("itemNumber", "100123");

        inputMessage.setObjectPart("itemName", "Pocket calculator");

        inputMessage.setIntPart("itemQuantity", 1);

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue("addItemOperation failed!!", operationSucceeded);

        if (operationSucceeded) {
            assertTrue(
                "addItemOperation (EJB) did not return an Item Object!!",
                outputMessage.getObjectPart("item") instanceof Item);
            debug(outputMessage.getObjectPart("item"));
        } else {
            part = faultMessage.getObjectPart("invalidItemException");
            if (part != null) {
                debug(faultMessage.getName() + ":\n" + part);
            } else {
                part = faultMessage.getObjectPart("outOfStockException");
                if (part != null) {
                    debug(faultMessage.getName() + ":\n" + part);
                } else {
                    debug("ERROR: Unknown fault message!");
                }
            }
        }

        // -----------------------------------------------------------------------------------------------------
        // Test an "Input Only" operation by invoking the emptyBasket method        
        operation = port.createOperation("emptyOrderOperation");
        inputMessage = operation.createInputMessage();

        debug("\n---> Invocation: public void emptyOrder(String customerNumber)");

        inputMessage.setObjectPart("customerNumber", "AE001");

        operation.executeInputOnlyOperation(inputMessage);

        // -----------------------------------------------------------------------------------------------------
        // Add to the basket again - the basket should be empty before this if previous operation worked!!
        operation = port.createOperation("addItemOperation");
        inputMessage = operation.createInputMessage();
        outputMessage = operation.createOutputMessage();
        faultMessage = operation.createFaultMessage();

        debug("\n---> Invocation (remote): public Item addItem(java.lang.String itemNumber, java.lang.String itemName, int itemQuantity) throws org.apache.wsif.ejb.sample.shop.OutOfStockException, java.rmi.RemoteException, org.apache.wsif.ejb.sample.shop.InvalidItemException");

        inputMessage.setObjectPart("itemNumber", "100123");

        inputMessage.setObjectPart("itemName", "Pocket calculator");

        inputMessage.setIntPart("itemQuantity", 5);

        operationSucceeded =
            operation.executeRequestResponseOperation(
                inputMessage,
                outputMessage,
                faultMessage);

        assertTrue("addItemOperation failed!!", operationSucceeded);

        if (operationSucceeded) {
            assertTrue(
                "addItemOperation (EJB) did not return an Item Object!!",
                outputMessage.getObjectPart("item") instanceof Item);
            debug(outputMessage.getObjectPart("item"));
        } else {
            part = faultMessage.getObjectPart("invalidItemException");
            if (part != null) {
                debug(faultMessage.getName() + ":\n" + part);
            } else {
                part = faultMessage.getObjectPart("outOfStockException");
                if (part != null) {
                    debug(faultMessage.getName() + ":\n" + part);
                } else {
                    debug("ERROR: Unknown fault message!");
                }
            }
        }
    }

    private void debug(Object s) {
        if (debugMode)
            System.out.println(s);
    }

}

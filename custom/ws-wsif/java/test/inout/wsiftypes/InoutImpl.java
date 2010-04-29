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

package inout.wsiftypes;

import inout.wsifservice.Inout;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;

import util.AddressUtility;

import addressbook.wsiftypes.Address;
import addressbook.wsiftypes.Phone;

/**
 * Inout service used by InoutTest for various miscelleanous tests.
 * @author Mark Whitlock
 */
public class InoutImpl implements Inout {
    private Hashtable name2AddressTable = new Hashtable();

    public InoutImpl() {
        addEntry(
            "John B. Good",
            new Address(
                123,
                "Main Street",
                "Anytown",
                "NY",
                12345,
                new Phone(123, "456", "7890")));
        addEntry(
            "Bob Q. Public",
            new Address(
                456,
                "North Whatever",
                "Notown",
                "ME",
                12424,
                new Phone(987, "444", "5566")));
    }

    public void addEntry(String name, Address address) {
        if (name != null && address != null)
            name2AddressTable.put(name, address);
    }

    public void addEntry(String firstName, String lastName, Address address) {
        if (firstName != null && lastName != null && address != null)
            name2AddressTable.put(firstName + " " + lastName, address);
    }

    public Address getAddressFromName(String name)
        throws IllegalArgumentException {
        if (name == null)
            return null;
        return getAddressFromName(new Mutablestring(name));
    }

    public Address getAddressFromName(Mutablestring name)
        throws IllegalArgumentException {
        if (name == null)
            return null;

        String found = null;
        int star = name.toString().indexOf("*");
        if (star != -1) {
            String trimmed = name.toString().substring(0, star);
            Iterator it = name2AddressTable.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                if (key.startsWith(trimmed)) {
                    found = key;
                    break;
                }
            }

            if (found == null)
                throw new IllegalArgumentException(
                    "Couldn't find " + name + " trimmed=" + trimmed);
        } else
            found = name.toString();

        return (Address) name2AddressTable.get(found);
    }

    public boolean getAddressFromName(Mutablestring name, Address addr)
        throws IllegalArgumentException {
        Address newAddr = getAddressFromName(name);
        if (newAddr == null)
            return false;
        AddressUtility addrUtil = new AddressUtility(newAddr);
        addrUtil.copy(addr);
        return true;
    }

    public int addNumbers(int[] nums) {
        int result = 0;
        for (int i = 0; i < nums.length; i++)
            result += nums[i];
        return result;
    }

    public Date getDate() {
        return new Date();
    }

    public String whoami(String s) {
        return new String("String");
    }
    public String whoami(float f) {
        return new String("float");
    }
    public String whoami(int i) {
        return new String("int");
    }
    public String whoami(Address a) {
        return new String("Address");
    }
}

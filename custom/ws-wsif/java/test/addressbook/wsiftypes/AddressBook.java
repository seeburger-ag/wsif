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

package addressbook.wsiftypes;

import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;

import addressbook.wsiftypes.*;

/**
 * Sample service that provides add/get functionality.
 *
 * @author Matthew J. Duftler (duftler@us.ibm.com)
 * @author Aleksander Slominski
 */
public class AddressBook {
  private HashMap name2AddressTable = new HashMap();

  public AddressBook() {
    addEntry("John B. Good",
             new Address(123, "Main Street", "Anytown", "NY", 12345,
                         new Phone(123, "456", "7890")));
    addEntry("Bob Q. Public",
             new Address(456, "North Whatever", "Notown", "ME", 12424,
                         new Phone(987, "444", "5566")));
  }

  public void addEntry(String name, Address address)
  {
    name2AddressTable.put(name, address);
  }

  public void addEntry(String firstName, String lastName, Address address)
  {
    name2AddressTable.put(firstName+" "+lastName, address);
  }

  public Address getAddressFromName(String name)
    throws IllegalArgumentException
  {
    return (Address)name2AddressTable.get(name);
  }

}


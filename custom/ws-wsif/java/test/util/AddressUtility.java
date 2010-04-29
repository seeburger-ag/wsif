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

package util;

import addressbook.wsiftypes.Address;
import addressbook.wsiftypes.Phone;

/**
 * @author Mark Whitlock
 */
public class AddressUtility extends Address {
    public AddressUtility() {
        super();
    }

    public AddressUtility(Address a) {
        super(
            a.getStreetNum(),
            a.getStreetName(),
            a.getCity(),
            a.getState(),
            a.getZip(),
            a.getPhoneNumber());
    }

    public boolean equals(Address a) {
        if (a == null)
            return false;

        if (super.getStreetNum() != a.getStreetNum())
            return false;
        if (!matches(super.getStreetName(), a.getStreetName()))
            return false;
        if (!matches(super.getCity(), a.getCity()))
            return false;
        if (!matches(super.getState(), a.getState()))
            return false;
        if (super.getZip() != a.getZip())
            return false;

        Phone p1 = super.getPhoneNumber();
        Phone p2 = a.getPhoneNumber();

        if (p1.getAreaCode() != p2.getAreaCode())
            return false;
        if (!matches(p1.getExchange(), p2.getExchange()))
            return false;
        if (!matches(p1.getNumber(), p2.getNumber()))
            return false;

        return true;
    }

    public void copy(Address a) {
        a.setStreetNum(super.getStreetNum());
        a.setStreetName(super.getStreetName());
        a.setCity(super.getCity());
        a.setState(super.getState());
        a.setZip(super.getZip());
        a.setPhoneNumber(super.getPhoneNumber());
    }

    public Address copy() {
        Address a = new Address();
        copy(a);
        return a;
    }

    private boolean matches(String s1, String s2) {
        if (s1 == null && s2 == null)
            return true;
        if (s1 == null || s2 == null)
            return false;
        if (!s1.equals(s2))
            return false;
        return true;
    }
}

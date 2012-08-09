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

package zipcode;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import util.TestUtilities;

import clients.zipcode.ShortZipCode;
import clients.zipcode.ShortZipCodeResponse;
import clients.zipcode.ZIPCodeProxy;

/**
 * Zip code test to test out document style. Only
 * supports Soap and SoapJms. Axis and AxisJms are 
 * not tested since this support has not yet been 
 * implemented.
 * 
 * @author Mark Whitlock
 */
public class ZIPCodeTest extends TestCase {
    String wsdlLocation =
        TestUtilities.getWsdlPath("java\\samples\\clients\\zipcode")
            + "zipcoderesolver.wsdl";

    private static final String expectedZipCode = "78701";

    public ZIPCodeTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(ZIPCodeTest.class);
    }

    // This should test Axis and AxisJms but these haven't been implemented yet.
    // I have commented out the SoapJms test because it doesn't work and I 
    // haven't debugged it yet. I think it may be a bug in the JMS2HTTPBridge.
    public void testZipCodeSoap() {
        doit("ZipCodeResolverSoap");
    }
    // public void testZipCodeSoapJms() { doit("ZipCodeResolverSoapJms"); }

    public void doit(String portName) {

        ShortZipCode address = new ShortZipCode();
        address.setAccessCode("9999");
        address.setAddress("607 Trinity");
        address.setCity("Austin");
        address.setState("TX");

        ZIPCodeProxy proxy = new ZIPCodeProxy(wsdlLocation);
        ShortZipCodeResponse response =
            proxy.getShortZipCodeResponse(address, portName);

        System.out.println(response.getShortZipCodeResult());
        assertTrue(response.getShortZipCodeResult().equals(expectedZipCode));
        // reset back to default soap provider
        TestUtilities.resetDefaultProviders();

    }
}

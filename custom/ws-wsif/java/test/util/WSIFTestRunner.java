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

import faults.FaultMsgTest;
import features.FeaturesTest;
import headers.HeadersTest;
import inout.InoutTest;
import interop.InteropDocTest;
import interop.InteropDocWrappedTest;
import invocation.DynamicInvokerTest;

import jms.JmsFaultTest;
import jms.JmsTest;
import jndi.JNDIAddressBookTest;
import junit.framework.Test;
import junit.framework.TestSuite;

import mime.MimeTest;
import providers.PlugableProvidersTest;
import providers.ProvidersInitialisationTest;
import serialization.SerializationTest;
import simpletypes.SOAPENCTest;
import soap.InputPartsTest;
import soap.MissingInputPartTest;
import soap.OutputPartsTest;
import soapinterop.InteropTest;
import stockquote.StockquoteTest;
import wsdl.WsdlLoadingTest;
import zipcode.ZIPCodeTest;

import addressbook.AddressBookTest;
import async.AsyncTests;
import chartype.CharTest;
import docStyle.MessagingAttachmentsTest;
import docStyle.NWBankTest;
import docStyle.ZipCodeAxisTest;

/**
 * Run JUnit tests on WSIF code.
 * Add new tests to the suite() method.
 * Comment out lines for tests not required.
 *
 * @author Owen Burroughs
 */

public class WSIFTestRunner {

    public static void main(String[] args) {
	    TestUtilities.startListeners();
        junit.textui.TestRunner.run(suite());
        TestUtilities.stopListeners();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("All WSIF Tests");

        // Test the initialisation of WSIF providers. This test needs to be
        // the FIRST of the functional tests as after this, WSIF will already
        // have been initialised.
        suite.addTest(new TestSuite(ProvidersInitialisationTest.class));

        //suite.addTest(new TestSuite(ShoppingCartTest.class));
        addIfAvaliable(suite, "shop.ShoppingCartTest");
        addIfAvaliable(suite, "multiout.MultiOutTest");
        suite.addTest(new TestSuite(FeaturesTest.class));
        suite.addTest(new TestSuite(AsyncTests.class));
        suite.addTest(new TestSuite(HeadersTest.class));
        suite.addTest(new TestSuite(FaultMsgTest.class));
        suite.addTest(new TestSuite(StockquoteTest.class));
        suite.addTest(new TestSuite(AddressBookTest.class));
        suite.addTest(new TestSuite(WsdlLoadingTest.class));
        suite.addTest(new TestSuite(InoutTest.class));
        suite.addTest(new TestSuite(DynamicInvokerTest.class));
        suite.addTest(new TestSuite(ZIPCodeTest.class));
        suite.addTest(new TestSuite(InteropTest.class));
        suite.addTest(new TestSuite(JNDIAddressBookTest.class));
        suite.addTest(new TestSuite(PlugableProvidersTest.class));
        suite.addTest(new TestSuite(InputPartsTest.class));
        suite.addTest(new TestSuite(OutputPartsTest.class));
        suite.addTest(new TestSuite(WildcardTest.class));
        suite.addTest(new TestSuite(ZipCodeAxisTest.class));
        suite.addTest(new TestSuite(NWBankTest.class));
        suite.addTest(new TestSuite(InteropDocTest.class));
        suite.addTest(new TestSuite(InteropDocWrappedTest.class));
        suite.addTest(new TestSuite(MessagingAttachmentsTest.class));
        suite.addTest(new TestSuite(SerializationTest.class));
        suite.addTest(new TestSuite(CharTest.class));
        suite.addTest(new TestSuite(SOAPENCTest.class));
        suite.addTest(new TestSuite(MissingInputPartTest.class));

        if (TestUtilities.areWeTesting("mime"))
            suite.addTest(new TestSuite(MimeTest.class));

        if (TestUtilities.areWeTesting("jms")) {
            suite.addTest(new TestSuite(JmsTest.class));
            suite.addTest(new TestSuite(JmsFaultTest.class));
        }

        return suite;
    }

    public static void addIfAvaliable(TestSuite suite, String className) {
	    try {
	        Class klass = Class.forName(className);
	        suite.addTest(new TestSuite(klass));
	    } catch(Exception ex) {
	    }
    }
}

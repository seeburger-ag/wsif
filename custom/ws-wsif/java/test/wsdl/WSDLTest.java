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

package wsdl;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.Properties;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import util.TestUtilities;

import com.ibm.wsdl.Constants;

/**
 * Junit test to test the deserializing and serializing
 * of a wsdl document including testing reading extension
 * attributes on Parts.
 * @author Owen Burroughs
 * @author Jeremy Hughes <hughesj@apache.org>
 */
public class WSDLTest extends TestCase {
    private final static String DEF_FACTORY_PROPERTY_NAME =
        "javax.wsdl.factory.DefinitionFactory";
    private final static String PRIVATE_DEF_FACTORY_CLASS =
        "org.apache.wsif.wsdl.WSIFWSDLFactoryImpl";

    private static String WSDL_GOOD = "";
    private static String WSDL_BAD = "";
    private static String WSDL_OUT = "";
    private static boolean debugging = true;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(WSDLTest.class);
    }

    public WSDLTest(String arg0) {
        super(arg0);
    }

    protected void setUp() {
        WSDL_GOOD = TestUtilities.getWsdlPath("java\\test\\wsdl") + "WSDLTestGood.wsdl";
        WSDL_BAD = TestUtilities.getWsdlPath("java\\test\\wsdl") + "WSDLTestBad.wsdl";
        WSDL_OUT = TestUtilities.getWsdlPath("java\\test\\wsdl") + "WSDLTestOut.wsdl";
    }

    public void testReadWriteGoodWSDL() {
        debug("--- Try good WSDL ---");
        Definition d = readWSDL(null, WSDL_GOOD);
        assertNotNull("Definition is null after reading wsdl", d);
        if (d == null) {
            fail("--- Cannot write wsdl since definition is null ---");
        } else {
            debug("--- Definition read OK ---");
            boolean ok = writeWSDL(d);
            assertTrue("Writing WSDL failed", ok);
        }
    }

    public void testReadBadWSDL() {
        debug("--- Try bad WSDL, expect exception ---");
        Definition d2 = readWSDL(null, WSDL_BAD);
        assertNull("Bad wsdl was read without error!!", d2);
    }

    public void testReadMissingDefaultXMLNamepsaceWSDL() {
        debug("--- Test WSDL with missing default XML namespace is good");
        Definition d =
            readWSDL(null, TestUtilities.getWsdlPath("java\\test\\wsdl") + "WSDLTestNested2.wsdl");
        assertNotNull("Definition is null after reading WSDLTestNested2.wsdl", d);
    }
    
    private static Definition readWSDL(URL contextURL, String wsdlLoc) {
        try {
            Properties props = System.getProperties();
            String oldPropValue = props.getProperty(DEF_FACTORY_PROPERTY_NAME);

            props.setProperty(DEF_FACTORY_PROPERTY_NAME, PRIVATE_DEF_FACTORY_CLASS);

            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader wsdlReader = factory.newWSDLReader();
            wsdlReader.setFeature(Constants.FEATURE_VERBOSE, false);
            String context = null;
            if (contextURL != null)
                context = contextURL.toString();
            Definition def = wsdlReader.readWSDL(context, wsdlLoc);

            if (oldPropValue != null) {
                props.setProperty(DEF_FACTORY_PROPERTY_NAME, oldPropValue);
            } else {
                props.remove(DEF_FACTORY_PROPERTY_NAME);
            }
            return def;
        } catch (WSDLException e) {
            debug("EXCEPTION: " + e.getMessage());
            return null;
        }
    }

    private static boolean writeWSDL(Definition def) {
        try {
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLWriter wsdlWriter = factory.newWSDLWriter();
            File f = new File(WSDL_OUT);
            FileWriter out = new FileWriter(f);
            wsdlWriter.writeWSDL(def, out);
            out.close();
            debug("--- Definition written OK ---");
            return true;
        } catch (Exception e) {
            debug("EXCEPTION: " + e);
            return false;
        }
    }

    private static void debug(String s) {
        if (debugging)
            System.out.println(s);
    }
}

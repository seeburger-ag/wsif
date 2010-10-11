/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
 * Business Machines, Inc., http://www.ibm.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package features;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFServiceFactory;
import util.TestUtilities;

/**
 * Junit test to test out setting and getting features on WSIFServiceFactory
 * subclasses
 * @author Owen Burroughs <owenb@apache.org>
 */
public class FeaturesTest extends TestCase {

    public FeaturesTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(FeaturesTest.class);
    }

    public void setUp() {
        TestUtilities.setUpExtensionsAndProviders();
    }

    public void testGetSetFeatures() {
        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
        factory.setFeature(
            WSIFConstants.WSIF_FEATURE_AUTO_MAP_TYPES,
            new Boolean(true));
        factory.setFeature(
            WSIFConstants.WSIF_FEATURE_SERVICE_CACHING,
            new Boolean(true));
        factory.setFeature(WSIFConstants.WSIF_FEATURE_MAPPER_CLASS, "test");
        factory.setFeature(
            WSIFConstants.WSIF_FEATURE_MAPPINGCONVENTION_CLASS,
            "test2");

        Object f1 =
            factory.getFeature(WSIFConstants.WSIF_FEATURE_AUTO_MAP_TYPES);
        assertNotNull("WSIF_FEATURE_AUTO_MAP_TYPES feature was null when retrieved from factory", f1);            
        assertTrue(
            "WSIF_FEATURE_AUTO_MAP_TYPES feature was not a Boolean when retrieved from factory",
            f1 instanceof Boolean);
        assertTrue(
            "WSIF_FEATURE_AUTO_MAP_TYPES feature was not set to true when retrieved from factory",
            ((Boolean) f1).booleanValue());

        Object f2 =
            factory.getFeature(WSIFConstants.WSIF_FEATURE_SERVICE_CACHING);
        assertNotNull("WSIF_FEATURE_SERVICE_CACHING feature was null when retrieved from factory", f2);            
        assertTrue(
            "WSIF_FEATURE_SERVICE_CACHING feature was not a Boolean when retrieved from factory",
            f2 instanceof Boolean);
        assertTrue(
            "WSIF_FEATURE_SERVICE_CACHING feature was not set to true when retrieved from factory",
            ((Boolean) f2).booleanValue());
            
        Object f3 =
            factory.getFeature(WSIFConstants.WSIF_FEATURE_MAPPER_CLASS);
        assertNotNull("WSIF_FEATURE_MAPPER_CLASS feature was null when retrieved from factory", f3);            
        assertTrue(
            "WSIF_FEATURE_MAPPER_CLASS feature was not a String when retrieved from factory",
            f3 instanceof String);
        assertTrue(
            "WSIF_FEATURE_MAPPER_CLASS feature was not set to \"test\" when retrieved from factory",
            ((String) f3).equals("test"));            
                        
        Object f4 =
            factory.getFeature(WSIFConstants.WSIF_FEATURE_MAPPINGCONVENTION_CLASS);
        assertNotNull("WSIF_FEATURE_MAPPINGCONVENTION_CLASS feature was null when retrieved from factory", f4);
        assertTrue(
            "WSIF_FEATURE_MAPPINGCONVENTION_CLASS feature was not a String when retrieved from factory",
            f4 instanceof String);
        assertTrue(
            "WSIF_FEATURE_MAPPINGCONVENTION_CLASS feature was not set to \"test2\" when retrieved from factory",
            ((String) f4).equals("test2"));
            
        factory.setFeature(WSIFConstants.WSIF_FEATURE_MAPPER_CLASS, "test3");
        Object f5 =
            factory.getFeature(WSIFConstants.WSIF_FEATURE_MAPPER_CLASS);
        assertNotNull("WSIF_FEATURE_MAPPER_CLASS feature was null when retrieved from factory", f3);            
        assertTrue(
            "WSIF_FEATURE_MAPPER_CLASS feature was not a String when retrieved from factory a second time",
            f5 instanceof String);
        assertTrue(
            "WSIF_FEATURE_MAPPER_CLASS feature was not set to \"test3\" when retrieved from factory a second time",
            ((String) f5).equals("test3"));                           
    }
    
    public void testOverrideAllFeatures() {
        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
        factory.setFeature(
            WSIFConstants.WSIF_FEATURE_AUTO_MAP_TYPES,
            new Boolean(true));
        factory.setFeature(
            WSIFConstants.WSIF_FEATURE_SERVICE_CACHING,
            new Boolean(true));
        factory.setFeature(WSIFConstants.WSIF_FEATURE_MAPPER_CLASS, "test");
        factory.setFeature(
            WSIFConstants.WSIF_FEATURE_MAPPINGCONVENTION_CLASS,
            "test2");

        Map m = new HashMap();
        m.put(WSIFConstants.WSIF_FEATURE_AUTO_MAP_TYPES, new Boolean(false));
        m.put(WSIFConstants.WSIF_FEATURE_MAPPER_CLASS, "testOverride");
        factory.setFeatures(m);
        
        Object f1 =
            factory.getFeature(WSIFConstants.WSIF_FEATURE_AUTO_MAP_TYPES);
        assertNotNull("WSIF_FEATURE_AUTO_MAP_TYPES feature was null when retrieved from factory", f1);            
        assertTrue(
            "WSIF_FEATURE_AUTO_MAP_TYPES feature was not a Boolean when retrieved from factory",
            f1 instanceof Boolean);
        assertTrue(
            "WSIF_FEATURE_AUTO_MAP_TYPES feature was not set to false when retrieved from factory",
            !((Boolean) f1).booleanValue());

		// this feature was not in the map passed in to setFeatures and so the feature value should be null
        Object f2 =
            factory.getFeature(WSIFConstants.WSIF_FEATURE_SERVICE_CACHING);
        assertNull("WSIF_FEATURE_SERVICE_CACHING feature was not null when retrieved from factory", f2);            
            
        Object f3 =
            factory.getFeature(WSIFConstants.WSIF_FEATURE_MAPPER_CLASS);
        assertNotNull("WSIF_FEATURE_MAPPER_CLASS feature was null when retrieved from factory", f3);            
        assertTrue(
            "WSIF_FEATURE_MAPPER_CLASS feature was not a String when retrieved from factory",
            f3 instanceof String);
        assertTrue(
            "WSIF_FEATURE_MAPPER_CLASS feature was not set to \"testOverride\" when retrieved from factory",
            ((String) f3).equals("testOverride"));            

		// this feature was not in the map passed in to setFeatures and so the feature value should be null                        
        Object f4 =
            factory.getFeature(WSIFConstants.WSIF_FEATURE_MAPPINGCONVENTION_CLASS);
        assertNull("WSIF_FEATURE_MAPPINGCONVENTION_CLASS feature was not null when retrieved from factory", f4);
    }
}

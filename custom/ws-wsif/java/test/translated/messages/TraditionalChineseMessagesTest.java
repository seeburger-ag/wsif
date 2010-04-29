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

package translated.messages;

import java.util.Locale;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Junit test to test out the English messages. Unfortunately calling 
 * Locale.setDefault() multiple times with different locales in the same
 * testcase does not appear to work. Consequently the tests for different
 * languages are split out into different tests and are not run from the 
 * WSIFTestRunner.
 * @author Mark Whitlock
 */
public class TraditionalChineseMessagesTest extends TestCase {
    public TraditionalChineseMessagesTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(TraditionalChineseMessagesTest.class);
    }

    public void setUp() {
    }

    public void testTraditionalChinese() {
        TranslatedMessagesUtilities.doit(Locale.TRADITIONAL_CHINESE);
    }
}

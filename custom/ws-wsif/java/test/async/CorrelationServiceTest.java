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

package async;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.wsif.WSIFCorrelationId;
import org.apache.wsif.WSIFCorrelationService;
import org.apache.wsif.WSIFException;
import org.apache.wsif.util.WSIFCorrelationServiceLocator;
import org.apache.wsif.util.jms.WSIFJMSCorrelationId;

public class CorrelationServiceTest extends TestCase {

    public CorrelationServiceTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(CorrelationServiceTest.class);
    }

    public void setUp() {
    }

    public void testIt() {

        System.out.println("Testing the WSIFCorrelationService...");

        WSIFCorrelationService cs =
            WSIFCorrelationServiceLocator.getCorrelationService();

        WSIFCorrelationService cs2 =
            WSIFCorrelationServiceLocator.getCorrelationService();
        assertTrue(cs == cs2);

        try {

            WSIFCorrelationId cid = new WSIFJMSCorrelationId("1");
            cs.put(cid, "petra", (long) 0);

            cid = new WSIFJMSCorrelationId("2");
            cs.put(cid, "ant", (long) 0);

            cid = new WSIFJMSCorrelationId("3");
            cs2.put(cid, "tanya", 3000);

            cid = new WSIFJMSCorrelationId("1");
            String s = (String) cs.get(cid);
            assertTrue(s.equals("petra"));

            cid = new WSIFJMSCorrelationId("2");
            s = (String) cs.get(cid);
            assertTrue(s.equals("ant"));

            cid = new WSIFJMSCorrelationId("3");
            s = (String) cs.get(cid);
            assertTrue(s.equals("tanya"));

            System.out.println("\nwaiting for timeouts...");
            try {
                Thread.sleep(10000);
            } catch (Exception ex) {
                System.out.println("interupted early");
            }

            cid = new WSIFJMSCorrelationId("1");
            s = (String) cs.get(cid);
            assertTrue(s.equals("petra"));

            cid = new WSIFJMSCorrelationId("2");
            s = (String) cs.get(cid);
            assertTrue(s.equals("ant"));

            cid = new WSIFJMSCorrelationId("3");
            s = (String) cs.get(cid);
            assertTrue(s == null); // should have timed out

            cid = new WSIFJMSCorrelationId("2");
            cs.remove(cid);

            cid = new WSIFJMSCorrelationId("1");
            s = (String) cs.get(cid);
            assertTrue(s.equals("petra"));

            cid = new WSIFJMSCorrelationId("2");
            s = (String) cs.get(cid);
            assertTrue(s == null); // due to remove

			// test primative clasess
			Class[] clss =
				new Class[] {
					int.class,
					float.class,
					long.class,
					double.class,
					short.class,
					byte.class,
					boolean.class,
					void.class };
			Class[] clss2;
			cid = new WSIFJMSCorrelationId("P1");
			cs.put(cid, clss, (long) 0);
			clss2 = (Class[]) cs.get(cid);
			for (int i = 0; i < clss.length; i++) {
				assertTrue(
					"class " + clss[i] + " failed!!",
					clss[i] == clss2[i]);
			}
			
            System.out.println("WSIFCorrealtionService tests complete.");
        } catch (WSIFException ex) {
            ex.printStackTrace();
            assertTrue(false);
        }

    }

}

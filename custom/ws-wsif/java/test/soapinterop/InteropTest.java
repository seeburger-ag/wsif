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

package soapinterop;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.providers.soap.apachesoap.WSIFDynamicProvider_ApacheSOAP;
import org.apache.wsif.util.WSIFPluggableProviders;

public class InteropTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(InteropTest.class);
    }

    public InteropTest(String name) {
        super(name);
    }

    /**
     * Tests the Java binding for the shopping cart scenario via WSDL
     */

    public void testInterop() {
      try
      {
      	// only for soap not axis provider
        WSIFPluggableProviders.overrideDefaultProvider(
           "http://schemas.xmlsoap.org/wsdl/soap/", 
           new WSIFDynamicProvider_ApacheSOAP() );
           
        InteropTestServiceProxy proxy = new InteropTestServiceProxy();

        //echoString(String)
        System.out.println("echoString");
        assertTrue("echoString", proxy.echoString("String").equals("String"));

        //echoInteger(int)
        System.out.println("echoInteger");
        assertTrue("echoInteger", proxy.echoInteger(3) == 3);

        //echoFloat(float)
        System.out.println("echoFloat");
        assertTrue("echoInteger", proxy.echoFloat((float) 5) == 5.0);

        //echoVoid() - exception thrown if problem
        System.out.println("echoVoid");
        proxy.echoVoid();

        //echoDate(Date)
        System.out.println("echoDate");
        Date dateIn = new Date();
        DateFormat fmt = DateFormat.getDateInstance(DateFormat.FULL, Locale.US);
        dateIn = fmt.parse("Thursday, January 10, 2002 ");
        assertTrue("echoDate", proxy.echoDate(dateIn).equals(dateIn));

        //echoStruct(SOAPStruct)
        /*			SOAPStruct structIn = new SOAPStruct();
        			structIn.setVarString("SOAPString");
        			structIn.setVarInt(7);
        			structIn.setVarFloat((float)9);
        			SOAPStruct structOut = proxy.echoStruct(structIn);
        			System.out.println("SOAPStruct start");
        			System.out.println(structOut.getVarString());
        			System.out.println(structOut.getVarInt());
        			System.out.println(structOut.getVarFloat());
        			System.out.println("SOAPStruct end"); */

        //echoBase64(byte [])
        System.out.println("echoBase64");
        byte[] base64In = { -1, 0, 1 };
        byte[] base64Out = proxy.echoBase64(base64In);

        for (int i = 0; i < base64Out.length; i++) {
            assertTrue("echoBase64 [" + i + "]", base64In[i] == base64Out[i]);
        }

        /*			//echoHexBinary(byte [])
        			byte[] hexBinaryIn = {-2, -1, 0, 1, 2};
        			byte[] hexBinaryOut = proxy.echoHexBinary(hexBinaryIn);
        			for( int i=0; i < hexBinaryOut.length; i++) {
        				System.out.print(hexBinaryOut[i]);
        				System.out.print(",");
        			}
        			System.out.println(); 
        */

        //echoDecimal(BigDecimal)
        System.out.println("echoBigDecimal");
        BigDecimal bigDecimalIn = new BigDecimal("7");
        assertTrue("echoDecimal", proxy.echoDecimal(bigDecimalIn).equals(bigDecimalIn));

        //echoBoolean(boolean)
        System.out.println("echoBoolean");
        boolean booleanIn = false;
        assertTrue("echoBoolean", proxy.echoBoolean(booleanIn) == booleanIn);

        //echoStringArray(String [])
        /*System.out.println("echoStringArray");
        String[] stringArrayIn = { "This", "is", "the", "stringArray", "test." };
        String[] stringArrayOut = proxy.echoStringArray(stringArrayIn);
        for (int i = 0; i < stringArrayOut.length; i++) {
            assertTrue(
                "echoStringArray [" + i + "]",
                stringArrayOut[i].equals(stringArrayIn[i]));
        }
        */
        //echoStructArray(SOAPStruct [])
        /*			SOAPStruct struct1 = new SOAPStruct();
        			struct1.setVarString("SOAPString1");
        			struct1.setVarInt(5);
        			struct1.setVarFloat((float)8);
        			SOAPStruct struct2 = new SOAPStruct();
        			struct2.setVarString("SOAPString2");
        			struct2.setVarInt(7);
        			struct2.setVarFloat((float)9);
        			SOAPStruct[] structArrayIn = new SOAPStruct[2];
        			structArrayIn[0] = struct1;
        			structArrayIn[1] = struct2;
        			SOAPStruct[] structArrayOut = proxy.echoStructArray(structArrayIn);
        			for (int i = 0; i < structArrayOut.length; i++) {
        				System.out.println(structArrayOut[i].getVarString());
        				System.out.println(structArrayOut[i].getVarInt());
        				System.out.println(structArrayOut[i].getVarFloat());
        			}	
        */

        //echoFloatArray(float [])
        /*System.out.println("echoFloatArray");
        float[] floatArrayIn = new float[5];
        for (int i = 0; i < floatArrayIn.length; i++) {
            floatArrayIn[i] = (float) (45.0 * Math.random() - 10.0);
        }
        float[] floatArrayOut = proxy.echoFloatArray(floatArrayIn);
        for (int i = 0; i < floatArrayOut.length; i++) {
            assertTrue("echoFloatArray [" + i + "]", floatArrayOut[i] == floatArrayIn[i]);
        }
        */
        //echoIntegerArray(int [])
        /*System.out.println("echoIntegerArray");
        int[] intArrayIn = new int[5];
        for (int i = 0; i < intArrayIn.length; i++) {
            intArrayIn[i] = i;
        }
        int[] intArrayOut = proxy.echoIntegerArray(intArrayIn);
        for (int i = 0; i < intArrayOut.length; i++) {
            assertTrue("echoIntArray [" + i + "]", intArrayOut[i] == intArrayIn[i]);
        }
        */
      }
      catch (Exception e)
      {
      	System.out.println("InteropTest caught exception "+e);
      	e.printStackTrace();
      	assertTrue(false);
      }
      	
    }
}

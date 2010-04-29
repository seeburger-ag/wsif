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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.WSIFOperation;
import org.apache.wsif.base.WSIFDefaultPort;

/**
 * Junit test for the finalize method on the WSIFDefaultPort.
 * 
 * @author Ant Elder <ant.elder@uk.ibm.com>
 */
public class PortFinalizeTest extends TestCase {

  public PortFinalizeTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
     junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {
    return new TestSuite(PortFinalizeTest.class);
  }

  public void setUp() {
    TestUtilities.setUpExtensionsAndProviders();
  }

  public void testFinalize() throws Throwable {
  	DummyPort p = new DummyPort();
  	assertTrue( "freshly created port should not be closed!", !DummyPort.isClosed() );
  	//p = null;
    //System.gc();
    //System.runFinalization();
    // unfortunately the above code doesn't seem to work in WSAD5
    // which makes this test not so useful. TODO - How can I fix that?
  	p.finalize();
  	assertTrue( "port should now be closed!", DummyPort.isClosed() );
  }

}

class DummyPort extends WSIFDefaultPort {
	
	static private boolean closed;
	
    public DummyPort() {
    	closed = false;
    }
    
	public WSIFOperation createOperation(String s1) {
		return null;
	}

	public WSIFOperation createOperation(String s1, String s2, String s3) {
		return null;
	}

    public void close() {
    	closed = true;
    }
    
    static public boolean isClosed() {
    	return closed;
    }

}

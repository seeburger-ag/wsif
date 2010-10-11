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

import java.util.StringTokenizer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.wsif.util.WSIFUtils;

/**
 * Junit test for the WSIFUtils wildcardCompare method.
 * 
 * @author Ant Elder <ant.elder@uk.ibm.com>
 */
public class WildcardTest extends TestCase {

  public WildcardTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
     junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {
    return new TestSuite(WildcardTest.class);
  }

  public void testWildcardCompare() throws Throwable {

     assertTrue( "t1", cmp( "foo.com", "foo.com" ) );
     assertTrue( "t2", !cmp( "xxx.com", "foo.com" ) );
     assertTrue( "t3", !cmp( "foo.com", "xxx.com" ) );
     
     assertTrue( "t4", cmp( "*.com", "foo.com" ) );
     assertTrue( "t5", cmp( "foo.*", "foo.com" ) );
     assertTrue( "t6", !cmp( "*.com", "foo.cxm" ) );
     assertTrue( "t7", !cmp( "foo.*", "fxo.com" ) );
     
     assertTrue( "t8", cmp( "hur.*.com", "hur.ibm.com" ) );
     assertTrue( "t9", !cmp( "hur.*.com", "hxr.ibm.com" ) );
     assertTrue( "t10", !cmp( "hur.*.com", "hur.ibm.cxm" ) );

     assertTrue( "t11", cmp( "hur*y.*.com", "hursley.ibm.com" ) );
     assertTrue( "t12", !cmp( "hur*y.*.com", "xursley.ibm.com" ) );
     assertTrue( "t13", !cmp( "hur*y.*.com", "hurslex.ibm.com" ) );
     assertTrue( "t14", !cmp( "hur*y.*.com", "hursley.ibm.cxm" ) );
 
     assertTrue( "t15", !cmp( "foo.com", "" ) );
     assertTrue( "t16", !cmp( "", "foo.com" ) );
     assertTrue( "t17", cmp( "*", "foo.com" ) );
  
     assertTrue( "t18", !cmp( "hjkj", null ) );
     assertTrue( "t19", !cmp( "*", null ) );
     assertTrue( "t20", !cmp( null, "foo.com" ) );
     assertTrue( "t21", !cmp( null, null ) );

  }

  private boolean cmp(String s1, String s2) {
     return WSIFUtils.wildcardCompare( s1, s2, '*' );
  }

}

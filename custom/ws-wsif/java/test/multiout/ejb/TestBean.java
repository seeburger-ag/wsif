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

package multiout.ejb;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.SessionBean;

/**
 * Multiple output parts test EJB
 **/
public class TestBean implements SessionBean {
    private javax.ejb.SessionContext mySessionCtx;

    /**
     * ejbActivate
     */
    public void ejbActivate() throws javax.ejb.EJBException {
    }
    
    /**
     * ejbCreate
     */
    public void ejbCreate()
        throws javax.ejb.CreateException, javax.ejb.EJBException {
    }

    /**
     * ejbPassivate
     */
    public void ejbPassivate() throws javax.ejb.EJBException {
    }
    
    /**
     * ejbRemove
     */
    public void ejbRemove() throws javax.ejb.EJBException {
    }
    
    /**
     * setSessionContext
     */
    public void setSessionContext(javax.ejb.SessionContext ctx)
        throws javax.ejb.EJBException {
        mySessionCtx = ctx;
    }

    /**
     * getSessionContext
     */
    public javax.ejb.SessionContext getSessionContext() {
        return mySessionCtx;
    }
    
    public Map getMultiOut(String anyString) throws javax.ejb.EJBException {
		HashMap m = new HashMap();
		m.put("surname", "Test");
		m.put("firstname", "Bob");
		m.put("initial", new Character('A'));
		m.put("age", new Integer(45));
	    StringBuffer sb = new StringBuffer("some data");
		m.put("details", sb);		
		return m;
    }
    
    public Map getMultiOutOld(String anyString) throws javax.ejb.EJBException {
		HashMap m = new HashMap();
		m.put(new Integer(100), "Test");		
		return m;
    }    
}

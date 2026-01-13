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

package org.apache.wsif.base;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.logging.Trc;

/**
 * A DefaultWSIFPort is a default implementation of WSIFPort
 * all methods are implemented except execute*.
 *
 * @author Paul Fremantle
 * @author Alekander Slominski
 * @author Matthew J. Duftler
 * @author Sanjiva Weerawarana
 * @author Nirmal Mukhi
 */
public abstract class WSIFDefaultPort implements WSIFPort {
    @Serial
    private static final long serialVersionUID = 1L;
	
	protected WSIFMessage context;

    public void close() throws WSIFException {
        Trc.entry(this);
        Trc.exit();
    }

    public void finalize() throws Throwable {
  	    Trc.entry(this);
  	    try {
  	        close();
  	    } catch (WSIFException ex) {
            Trc.ignoredException( ex );
  	    }
        super.finalize();
  	    Trc.exit();
    }
  
    /**
     * Utility method to return key suitable for hash table.
     */
    protected String getKey(String name, String inputName, String outputName) {
        Trc.entry(this, name, inputName, outputName);
        String s =
            name
                + (inputName != null ? ":" + inputName : "")
                + (outputName != null ? ":" + outputName : "");
        Trc.exit(s);
        return s;
    }

    /**
     * Utility method to retrieve extensibility element from list
     * checks also that it is exactly one extensibility element.
     */
    protected Object getExtElem(Object ctx, Class extType, List extElems)
        throws WSIFException {
        Trc.entry(this, ctx, extType, extElems);

        Object found = null;
        if (extElems != null) {
            for (Iterator i = extElems.iterator(); i.hasNext();) {
                // if so return new
                Object o = i.next();
                if (extType.isAssignableFrom(o.getClass())) {
                    if (found != null) {
                        throw new WSIFException(
                            "duplicated extensibility element "
                                + extType.getClass().getName()
                                + " in "
                                + ctx);
                    }
                    found = o;
                }
            }
        }
        Trc.exit(found);
        return found;
    }

    /**
     * Utility method to retrieve multiple extensibility elements from a list.
     */
    protected List getExtElems(Object ctx, Class extType, List extElems)
        throws WSIFException {
        Trc.entry(this, ctx, extType, extElems);
        List found = new ArrayList();
        if (extElems != null)
            for (Iterator i = extElems.iterator(); i.hasNext();) {
                Object o = i.next();
                if (extType.isAssignableFrom(o.getClass()))
                    found.add(o);
            }
        if (found.size() == 0)
            return null;
        Trc.exit(found);
        return found;
    }

    /**
     * Tests if this port supports synchronous calls to operations.
     * 
     * @return true   by default WSIFPorts do support synchronous calls
     */
    public boolean supportsSync() {
        Trc.entry(this);
        Trc.exit(true);
        return true;
    }

    /**
     * Tests if this port supports asynchronous calls to operations.
     * 
     * @return false   by default ports do not support asynchronous calls
     */
    public boolean supportsAsync() {
        Trc.entry(this);
        Trc.exit(false);
        return false;
    }

    /**
     * Gets the context information for this WSIFPort.
     * @return context
     */
    public WSIFMessage getContext() throws WSIFException {
        Trc.entry(this);
    	WSIFMessage contextCopy;
    	if (this.context == null) {
    		// really this should call getContext on the WSIFService but
    		// theres no reference to that so WSIFService must call setContext
    		// on any WSIFPorts it creates.
    		contextCopy = new WSIFDefaultMessage();
    	} else {
		    try {
			    contextCopy = (WSIFMessage) this.context.clone();
		    } catch (CloneNotSupportedException e) {
			    throw new WSIFException(
			        "CloneNotSupportedException cloning context", e);
		    }
    	}
        Trc.exit(contextCopy);
    	return contextCopy;
    }

    /**
     * Sets the context information for this WSIFPort.
     * @param WSIFMessage the new context information
     */
    public void setContext(WSIFMessage newContext) {
        Trc.entry(this, newContext);
        if (newContext == null) {
        	throw new IllegalArgumentException("context must not be null");
        }
        this.context = newContext;
        Trc.exit(null);
    }

}

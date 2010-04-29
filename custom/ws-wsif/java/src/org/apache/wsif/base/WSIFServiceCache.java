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

package org.apache.wsif.base;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.wsif.logging.Trc;

/**
 * Class to act as a cache of WSIFServices.
 * 
 * @author Paul Harris
 * @author Owen Burroughs
 */

public class WSIFServiceCache extends Hashtable {
    static final int CUSHION_PERCENT = 10;
    // How much of the cache will be cleaned out when it's limit has been reached

    LRUWrapper oldestObject = null; //The oldest (used) object in the cache
    LRUWrapper newestObject = null; //The newest (used) object in the cache

    int cushionSize = 1;
    // Calculated from CUSHION_PERCENT & cacheLimit - this is the number of objects
    // we'll clean out of the cache when we need to shrink it.
    
    int cacheLimit = 0; // The size (in objects) of the cache  
    int currentSize = 0;

    WSIFServiceCache(int size) {
    	super(size);
        cacheLimit = size;
        cushionSize = ((cacheLimit * CUSHION_PERCENT) / 100); // 10%
        if (cushionSize == 0) {
            cushionSize = 1;
        }
        Trc.event(this,
                "WSIFServiceCache created - cache limit:"
                    + cacheLimit
                    + " Cushion:"
                    + cushionSize);
    }
    
    public void setCacheSize(int newSize) {
    	synchronized (this) {
    	    cacheLimit = newSize;
            cushionSize = ((cacheLimit * CUSHION_PERCENT) / 100);
    	}
    }

    public Object put(Object key, Object value) {
        LRUWrapper newObject = new LRUWrapper(key, value);

        if (currentSize >= cacheLimit)
            shrinkCache();
        if (oldestObject == null) {
            oldestObject = newObject;
            newestObject = newObject;
        } else {
            addToTopOfLRUList(newObject);
        }

        newObject = (LRUWrapper) super.put(key, newObject);
        currentSize++;
        Trc.event(this,
                "WSIFServiceCache (put). Current cache size: " + currentSize);
        if (newObject == null)
            return null;
        else
            return newObject.value; //to make this consistent with Hashtable
    }

    public Object get(Object key) {
        LRUWrapper tempObject = (LRUWrapper) super.get(key);
        //Adjust the LRU list

        if (tempObject == null) {
            Trc.event(this,
                    "WSIFServiceCache (get). No hit. Cache size: "
                        + currentSize);
            return null;
        } else {
            Trc.event(this,
                    "WSIFServiceCache (get). MATCH FOUND. Cache size: "
                        + currentSize);
            if (newestObject != tempObject) {
                removeFromLRUList(tempObject);
                addToTopOfLRUList(tempObject);
            }
            return tempObject.value;
        }
    }

    private void addToTopOfLRUList(LRUWrapper newObject) {
        newestObject.nextObject = newObject;
        newObject.prevObject = newestObject;
        newestObject = newObject;
    }

    private void removeFromLRUList(LRUWrapper oldObject) {
        LRUWrapper nextObject, prevObject;

        if (oldObject != oldestObject) {
            oldObject.prevObject.nextObject = oldObject.nextObject;
            oldObject.nextObject.prevObject = oldObject.prevObject;
        } else {
            oldObject.nextObject.prevObject = null;
            oldestObject = oldObject.nextObject;
        }

    }

    private void shrinkCache() {
        LRUWrapper currentLRUWrapper = oldestObject;

        for (int i = 1; i <= cushionSize; i++) {
            super.remove(currentLRUWrapper.key);
            currentLRUWrapper = currentLRUWrapper.nextObject;
        }
        oldestObject = currentLRUWrapper;
        currentSize -= cushionSize;
        Trc.event(this,
                "WSIFServiceCache (put). Cache size after shrinkage: "
                    + currentSize);
    }

    /**
     * This class wraps whatever we're caching with some references used in a double linked list. 
     * This means we can traverse the objects referenced by the Hashtable in the order they were
     * last used.
     **/
    class LRUWrapper {
        LRUWrapper nextObject = null;
        LRUWrapper prevObject = null;
        Object value = null;
        Object key = null;

        LRUWrapper(Object inKey, Object inValue) {
            value = inValue;
            key = inKey;
        }
    }    
}

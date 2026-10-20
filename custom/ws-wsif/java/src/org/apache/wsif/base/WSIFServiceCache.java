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
        cushionSize = cushionFor(cacheLimit);
        if (Trc.ON) {
            Trc.event(this,
                    "WSIFServiceCache created - cache limit:"
                        + cacheLimit
                        + " Cushion:"
                        + cushionSize);
        }
    }

    /** Number of entries evicted at once; at least 1 so the cache stays bounded. */
    private static int cushionFor(int limit) {
        int c = (limit * CUSHION_PERCENT) / 100; // 10%
        return c < 1 ? 1 : c;
    }
    
    public synchronized void setCacheSize(int newSize) {
        cacheLimit = newSize;
        cushionSize = cushionFor(cacheLimit);
    }

    /*
     * get and put mutate the LRU list and the size counter, so they must be atomic
     * as a whole. They synchronize on this - the same monitor Hashtable uses for its
     * own methods - because a factory, and therefore its cache, is shared by all
     * threads creating services.
     */
    public synchronized Object put(Object key, Object value) {
        LRUWrapper existing = (LRUWrapper) super.get(key);
        if (existing != null) {
            // Replace in place: unlink the old node so the LRU list and the size
            // counter stay consistent with the Hashtable content.
            unlink(existing);
            currentSize--;
        }

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
        if (Trc.ON) {
            Trc.event(this,
                    "WSIFServiceCache (put). Current cache size: " + currentSize);
        }
        if (newObject == null)
            return null;
        else
            return newObject.value; //to make this consistent with Hashtable
    }

    public synchronized Object get(Object key) {
        LRUWrapper tempObject = (LRUWrapper) super.get(key);
        //Adjust the LRU list

        if (tempObject == null) {
            if (Trc.ON) {
                Trc.event(this,
                        "WSIFServiceCache (get). No hit. Cache size: "
                            + currentSize);
            }
            return null;
        } else {
            if (Trc.ON) {
                Trc.event(this,
                        "WSIFServiceCache (get). MATCH FOUND. Cache size: "
                            + currentSize);
            }
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
        newObject.nextObject = null;
        newestObject = newObject;
    }

    /** Unlinks a node that is not the newest one (callers check that). */
    private void removeFromLRUList(LRUWrapper oldObject) {
        if (oldObject != oldestObject) {
            oldObject.prevObject.nextObject = oldObject.nextObject;
            oldObject.nextObject.prevObject = oldObject.prevObject;
        } else {
            oldObject.nextObject.prevObject = null;
            oldestObject = oldObject.nextObject;
        }

    }

    /** Unlinks any node, including the newest and the only one. */
    private void unlink(LRUWrapper node) {
        if (node.prevObject != null) {
            node.prevObject.nextObject = node.nextObject;
        } else {
            oldestObject = node.nextObject;
        }
        if (node.nextObject != null) {
            node.nextObject.prevObject = node.prevObject;
        } else {
            newestObject = node.prevObject;
        }
        node.prevObject = null;
        node.nextObject = null;
    }

    private void shrinkCache() {
        LRUWrapper currentLRUWrapper = oldestObject;

        for (int i = 1; i <= cushionSize && currentLRUWrapper != null; i++) {
            super.remove(currentLRUWrapper.key);
            currentLRUWrapper = currentLRUWrapper.nextObject;
            currentSize--;
        }
        oldestObject = currentLRUWrapper;
        if (oldestObject == null) {
            newestObject = null;
        } else {
            oldestObject.prevObject = null;
        }
        if (Trc.ON) {
            Trc.event(this,
                    "WSIFServiceCache (put). Cache size after shrinkage: "
                        + currentSize);
        }
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

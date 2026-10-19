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

package org.apache.wsif.util;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.logging.Trc;

/**
 * The WSIFProperties class contains various static methods to read
 * values from the wsif.properties file. Its main purpose is to prevent the
 * properties file from being read in from disk each time a property is checked.
 * <p>
 * The file is looked up through the thread context class loader. The outcome of
 * that lookup - including "not found" - is cached per context class loader, so a
 * missing file no longer causes a resource lookup (and a swallowed exception) on
 * every call. Lookup semantics are otherwise unchanged: a class loader that cannot
 * see the file still yields <code>null</code> for every property.
 * <p>
 * This class is thread-safe.
 *
 * @author Ant Elder <antelder@apache.org>
 */
public class WSIFProperties {

    /** Sentinel cached for class loaders that cannot see wsif.properties. */
    private static final Properties NOT_FOUND = new Properties();

    /**
     * Lookup results keyed weakly by context class loader so that redeployed
     * bundles / applications can be garbage collected. Guarded by itself.
     */
    private static final Map<ClassLoader, Properties> cache = new WeakHashMap<>();

    /** Lock-free fast path for the most recently used class loader. */
    private static volatile CacheEntry last;

    private static final class CacheEntry {
        final WeakReference<ClassLoader> loader;
        final Properties properties;

        CacheEntry(ClassLoader loader, Properties properties) {
            this.loader = new WeakReference<>(loader);
            this.properties = properties;
        }
    }

    /**
     * Reads a property from the wsif.properties file.
     * 
     * @param property   the property to read
     * @return the value of ther property or null if the property is not defined   
     */
    public static String getProperty(String property) {
        Trc.entry(null, property);
        Properties properties = getProperties();
        if (properties == NOT_FOUND) {
            Trc.exit(null);
            return null;
        }

        String s = properties.getProperty(property);
        Trc.exit(s);
        return s;
    }

    /**
     * Returns the cached properties for the current context class loader,
     * loading them on first use. Never returns null; returns NOT_FOUND if the
     * file is not visible or cannot be read.
     */
    private static Properties getProperties() {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        CacheEntry e = last;
        if (e != null && e.loader.get() == loader) {
            return e.properties;
        }

        Properties p;
        synchronized (cache) {
            p = (Properties) cache.get(loader);
        }
        if (p == null) {
            // Load outside the lock; a concurrent duplicate load is harmless.
            p = (Properties) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return load(loader);
                }
            });
            synchronized (cache) {
                Properties existing = (Properties) cache.get(loader);
                if (existing != null) {
                    p = existing;
                } else {
                    cache.put(loader, p);
                }
            }
        }
        last = new CacheEntry(loader, p);
        return p;
    }

    private static Properties load(ClassLoader loader) {
        if (loader == null) {
            return NOT_FOUND;
        }
        InputStream in = loader.getResourceAsStream(WSIFConstants.WSIF_PROPERTIES);
        if (in == null) {
            return NOT_FOUND;
        }
        try {
            Properties p2 = new Properties();
            p2.load(in);
            return p2;
        } catch (Exception ignored) {
            Trc.exception(ignored);
            return NOT_FOUND;
        } finally {
            try {
                in.close();
            } catch (Exception ignored) {
                Trc.ignoredException(ignored);
            }
        }
    }

    /**
     * Parses a long property value, returning the default when the value is
     * missing or invalid. Avoids throwing NumberFormatException for the common
     * "property not defined" case.
     */
    private static long parseLong(String value, long def) {
        if (value == null) {
            return def;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            Trc.exception(e);
            return def;
        }
    }

    /**
     * Reads the async request timeout value from the wsif.properties file. This 
     * property defines how long WSIF will keep a WSIFOperation stored in the
     * correlation service waiting for the reply to a WSIFOperation 
     * executeRequestResponseAsync method call.
     * 
     * @return the async request timeout value in milliseconds. If the property
     *         is invalid or not defined in the properties file then a value of
     *         zero is returned.
     */
    public static long getAsyncTimeout() {
        Trc.entry(null);
        long t = parseLong(getProperty(WSIFConstants.WSIF_PROP_ASYNC_TIMEOUT), 0);
        if (t < 0) {
            t = 0;
        } else {
            t = t * 1000; // convert to milliseconds
        }
        Trc.exit(Long.valueOf(t));
        return t;
    }

    /**
     * Reads the synchronous request timeout from the wsif.properties file. This defines
     * how long (in milliseconds) WSIF will wait for a synchronous response. 
     * Default of 0 means forever.
     */
    public static long getSyncTimeout() {
        Trc.entry(null);
        long t = parseLong(getProperty(WSIFConstants.WSIF_PROP_SYNC_TIMEOUT), 0);
        if (t < 0) {
            t = 0;
        }
        Trc.exit(Long.valueOf(t));
        return t;
    }
    
    /**
     * Returns whether unreferenced attachments are supported or not.
     * Default is false.
     */
    public static boolean areUnreferencedAttachmentsSupported() {
    	Trc.entry(null);
    	boolean ret = false;
    	String value = getProperty(WSIFConstants.WSIF_PROP_UNREFERENCED_ATTACHMENTS);
    	if ("on".equals(value)) ret = true;
    	Trc.exit(ret);
    	return ret;
    }
}

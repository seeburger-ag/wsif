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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.logging.Trc;

/**
 * The WSIFProperties class contains various static methods to read
 * values from the wsif.properties file. Its main purpose is to prevent the
 * properties file from being read in from disk each time a property is checked.
 * 
 * @author Ant Elder <antelder@apache.org>
 */
public class WSIFProperties {

    private static Properties properties;

    /**
     * Reads a property from the wsif.properties file.
     * 
     * @param property   the property to read
     * @return the value of ther property or null if the property is not defined   
     */
    public static String getProperty(String property) {
        Trc.entry(null, property);
        if (properties == null) {
            properties =
                (Properties) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    InputStream in = (Thread.currentThread().getContextClassLoader())
                       .getResourceAsStream(WSIFConstants.WSIF_PROPERTIES);

                    Properties p2 = new Properties();
                    try {
                        p2.load(in);
                    } catch (Exception ignored) {
			        	Trc.exception(ignored);
                        return null;
                    }
                    return p2;
                }
            });
        }

        if (properties == null) {
            Trc.exit(null);
            return null;
        }

        String s = properties.getProperty(property);
        Trc.exit(s);
        return s;
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
        long t;
        try {
            t = Long.parseLong(getProperty(WSIFConstants.WSIF_PROP_ASYNC_TIMEOUT));
            if (t < 0) {
                t = 0;
            } else {
                t = t * 1000; // convert to milliseconds
            }
        } catch (NumberFormatException e) {
        	Trc.exception(e);
            t = 0;
        }
        Trc.exit(new Long(t));
        return t;
    }

    /**
     * Reads the synchronous request timeout from the wsif.properties file. This defines
     * how long (in milliseconds) WSIF will wait for a synchronous response. 
     * Default of 0 means forever.
     */
    public static long getSyncTimeout() {
        Trc.entry(null);
        long t;
        try {
            t = Long.parseLong(getProperty(WSIFConstants.WSIF_PROP_SYNC_TIMEOUT));
            if (t < 0) {
                t = 0;
            }
        } catch (NumberFormatException e) {
        	Trc.exception(e);
            t = 0;
        }
        Trc.exit(new Long(t));
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
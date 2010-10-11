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

package org.apache.wsif.mapping;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFException;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.util.WSIFProperties;

/**
 * A factory to produce instances of WSIFMapper
 * 
 * @author Owen Burroughs <owenb@apache.org>
 */
public class WSIFMapperFactory {
	
	private static final String DEFAULT_MAPPER_CLASS = "org.apache.wsif.mapping.WSIFDefaultMapper";
	
	// Store the name of the mapper class used so that we don't have to repeatedly look it up.
	// Store as a StringBuffer so that we can synchronize on it
	private static StringBuffer mapperClassNameBuffer = new StringBuffer();

	/**
	 * Get a new WSIFMapper. This method is equivalent to calling newMapper(false)
	 * @return A new instance of WSIFMapper
     * @throws An exception if the class cannot be found or is not an implemenation of
     * WSIFMapper
	 */	
	public static WSIFMapper newMapper() throws WSIFException {
		return newMapper(false);
	}

	/**
	 * Get a new WSIFMapper. This method allows the user to refresh the class name of the
	 * WSIFMapper to return. If the flag is true or a class name has not yet to be established
	 * the class name will be looked up in the following order:<br>
	 * <ol>
	 * <li>Look for a system property named <tt>org.apache.wsif.mapper</tt></li>
	 * <li>Look for a property in wsif.properties called <tt>org.apache.wsif.mapper</tt></li>
	 * <li>Use the default class name - <tt>org.apache.wsif.mapping.WSIFDefaultMapper</tt></li>
	 * </ol>
	 * If the flag is false and a class name has already been establshed, that class name will be used.
	 * @param refresh A flag to indicate whether or not to reuse the class name for the
	 * WSIFMapper implementation if it has already been established rather than determine it
	 * again.
	 * @return A new instance of WSIFMapper
     * @throws An exception if the class cannot be found or is not an implemenation of
     * WSIFMapper
	 */
    public static WSIFMapper newMapper(boolean refresh) throws WSIFException {
        synchronized (mapperClassNameBuffer) {
            if (mapperClassNameBuffer.length() == 0 || refresh) {
                String mapperClassName = null;
                // First try getting the mapper class name from the system properties
                // Don't use doPrivileged since we are loading a class that is user specified		
                try {
                    mapperClassName = System.getProperty(WSIFConstants.WSIF_MAPPER_PROPERTY);
                } catch (SecurityException s) {
                    Trc.ignoredException(s);
                }

                // If no system property set, try the wsif.properties file
                if (mapperClassName == null) {
                    mapperClassName = WSIFProperties.getProperty(WSIFConstants.WSIF_MAPPER_PROPERTY);
                }

                // Finally use the default class
                if (mapperClassName == null) {
                    mapperClassName = DEFAULT_MAPPER_CLASS;
                }
                
                if (refresh) {
                	mapperClassNameBuffer.delete(0, mapperClassNameBuffer.length());
                }
               	mapperClassNameBuffer.insert(0, mapperClassName);
            }
            try {
                Class c =
                    Class.forName(
                        mapperClassNameBuffer.toString(),
                        true,
                        Thread.currentThread().getContextClassLoader());
                return (WSIFMapper) c.newInstance();
            } catch (ClassNotFoundException cnf) {
            	throw new WSIFException("Unable to create new mapper", cnf);
            } catch (Exception e) {
            	throw new WSIFException("Unable to create new mapper", e);
            }
        }
    }

    /**
     * Create a new instance of an implementation WSIFMapper. This method will 
     * attempt to use the class name given. If the class is not an instance of WSIFMapper 
     * an exception will be thrown. Calling this method will not override the class name stored
     * by this instance of WSIFMapperFactory. The class is looked up in isolation.
     * @param mapperClassName The class name of the implementation of WSIFMapper to
     * use
     * @return An instance of WSIFMapper
     * @throws An exception if the class cannot be found or is not an implemenation of
     * WSIFMapper
     */
    public static WSIFMapper newMapper(String mapperClassName)
        throws WSIFException {
        try {
            Class c =
                Class.forName(
                    mapperClassName,
                    true,
                    Thread.currentThread().getContextClassLoader());
            return (WSIFMapper) c.newInstance();
        } catch (ClassNotFoundException cnf) {
            throw new WSIFException("Unable to create new mapper", cnf);
        } catch (Exception e) {
            throw new WSIFException("Unable to create new mapper", e);
        }
    }    
}

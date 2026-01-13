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

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFCorrelationService;
import org.apache.wsif.base.WSIFDefaultCorrelationService;
import org.apache.wsif.logging.MessageLogger;
import org.apache.wsif.logging.Trc;

/**
 * WSIFCorrelationServiceLocator is a helper class for locating 
 * the WSIFCorrelationService. This may either be the WSIF provided
 * WSIFDefaultCorrelationService, or, when running in a managed 
 * container the container may provide its own implementation which
 * this will locate by doing a JNDI lookup.
 * @author Ant Elder <antelder@apache.org>
 */
public class WSIFCorrelationServiceLocator {

    private static WSIFCorrelationService correlationService =
        findCorrelationService();

    /**
     * Returns the WSIFCorrelationService to be used for persisting
     * asynchronous requests. This attempts to locate a CorrelationService
     * with a JNDI lookup for "java:comp/wsif/WSIFCorrelationService",
     * if that fails then the WSIFDefaultCorrelationService is returned.
     * @return a WSIFCorrelationService
     */
    static public WSIFCorrelationService getCorrelationService() {
        Trc.entry(null);
        Trc.exit(correlationService);
        return correlationService;
    }

    static private WSIFCorrelationService findCorrelationService() {
        Trc.entry(null);
        WSIFCorrelationService cs = null;
        ;
        try {
            Context ctx = new InitialContext();
            Object o = ctx.lookup(WSIFConstants.CORRELATION_SERVICE_NAMESPACE);
            if (o != null && o instanceof WSIFCorrelationService service) {
                cs = service;
            }
        } catch (Exception ex) {
            Trc.ignoredException(ex);
        } finally {
            if (cs == null) {
                cs = new WSIFDefaultCorrelationService();
            }
        }

        MessageLogger.log("WSIF.0009I", cs.getClass().getName());

        Trc.exit(cs);
        return cs;
    }
}
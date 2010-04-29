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

package org.apache.wsif;

import java.io.Serializable;

/**
 * A WSIFCorrelationService is used for asynchronous requests
 * to correlate a response with the instance of the handler
 * that issued the request.
 *
 * @author Ant Elder <ant.elder@uk.ibm.com>
 */
public interface WSIFCorrelationService {

    /**
     * Not required yet
     */
    //public Serializable getCorrelator();

    /**
     * Adds an entry to the correlation service.
     * @param correlator   the key to associate with the state. 
     * @param state   the state to be stored. 
     * @param timeout   a timeout period after which the key and associated
     *                  state will be deleted from the correlation service. 
     */
    public void put(WSIFCorrelationId correlator, Serializable state, long timeout)
        throws WSIFException;

    /**
     * Retrieves an entry from the correlation service.
     * @param id   the key of the state to retrieved
     * @return the state associated with the id
     */
    public Serializable get(WSIFCorrelationId id) throws WSIFException;

    /**
     * Removes an entry form the correlation service.
     * @param id   the key of entry to be removed
     */
    public void remove(WSIFCorrelationId id) throws WSIFException;

}

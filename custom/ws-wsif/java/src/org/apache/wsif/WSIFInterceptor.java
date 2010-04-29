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

/**
 * An interceptor gets the opportunity to pre- or post-process
 * a service request or response, respectively, that's travelling
 * through the WSIF. The interceptor also gets the opportunity 
 * to inform WSIF that the service is to be denied; i.e., that 
 * the service request (or response) should not be processed
 * further.
 *
 * @author Sanjiva Weerawarana <sanjiva@watson.ibm.com>
 * @author Paul Fremantle <pzf@uk.ibm.com>
 */
public interface WSIFInterceptor {
    /**
     * Initialize the interceptor. 
     */
    public void init(WSIFInterceptorConfig ic);

    /**
     * This method is invoked to allow the interceptor to process the 
     * incoming request. If the request should not be processed further,
     * then this method should return false. The response object is also
     * made available in case the interceptor wishes to manipulate that too.
     *
     * @param request the incoming service request
     * @param response the outgoing service response
     * @return true or false, indicating whether the request should continue
     *         to be processed or not.
     * @exception WSIFException if something unexpected happens.
     */
    public boolean interceptIncoming(WSIFRequest request, WSIFResponse response)
        throws WSIFException;

    /**
     * This method is invoked to allow the interceptor to process the 
     * outgoing response. If the response should not be processed further,
     * then this method should return false. 
     *
     * @param request the incoming service request
     * @param response the outgoing service response
     * @return true or false, indicating whether the response should continue
     *         to be processed or not.
     * @exception WSIFException if something unexpected happens.
     */
    public boolean interceptOutgoing(WSIFRequest request, WSIFResponse response)
        throws WSIFException;

    /**
     * Destroy this interceptor.
     */
    public void destroy();
}

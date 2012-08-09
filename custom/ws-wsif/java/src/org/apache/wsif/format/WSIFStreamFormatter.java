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

package org.apache.wsif.format;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.wsif.WSIFRequest;
import org.apache.wsif.WSIFResponse;

/**
 * @author Piotr Przybylski <piotrp@ca.ibm.com>
 */
public interface WSIFStreamFormatter extends WSIFFormatter {

    public void formatRequest(WSIFRequest req, OutputStream out); //client

    public void formatResponse(WSIFResponse resp, OutputStream out); // server

    public WSIFRequest unformatRequest(InputStream in); //server

    public WSIFResponse unformatResponse(InputStream in); // client
}
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


package org.apache.wsif.providers.jca;

/**
 * An interface extending WSIF Format Handler with the Connector Architecture's specific
 * methods from the Streamable interface and a setter for the InteractionSpec field which
 * can be used by a resource adapter, when it is required, to support unmarshalling.
 * 
 * @author John Green
 * @author Michael Beisiegel
 * @author Piotr Przybylski <piotrp@ca.ibm.com>
 */
public interface WSIFFormatHandler_JCA extends org.apache.wsif.format.WSIFFormatHandler {

/**
 * Read data contents from the input stream.
 */
public void read(java.io.InputStream inputStream) throws java.io.IOException;
/**
 * Writes the contents of the data to the output stream.
 * @param outputStream
 * @throws IOException
 */
public void write(java.io.OutputStream outputStream) throws java.io.IOException;

/**
 * Sets the interactionSpec on the format handler. This should be set by the
 * resource adapter when the interactionSpec contains information 
 * that is required to unmarshall returned data.
 * @param interactionSpec
 */
public void setInteractionSpec(javax.resource.cci.InteractionSpec interactionSpec);
}

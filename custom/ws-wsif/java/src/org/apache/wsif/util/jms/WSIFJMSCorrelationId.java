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

package org.apache.wsif.util.jms;

import org.apache.wsif.WSIFCorrelationId;

/**
 * A WSIFCorrelationId is used to identify asynchronous 
 * requests so that a request can be matched to its response.
 *
 * @author Ant Elder <ant.elder@uk.ibm.com>
 */
public class WSIFJMSCorrelationId implements WSIFCorrelationId {
	private static final long serialVersionUID = 1L;

    private String id;

    /**
     * Creates a new WSIFJMSCorrelationId.
     * 
     * @param id   the id string
     */
    public WSIFJMSCorrelationId(String id) {
        if (id.length() < 256) {
            this.id = id;
        } else {
            throw new IllegalArgumentException("id length must be less than 256 characters");
        }
    }

    /**
     * gets the value of this WSIFJMSCorrelationId as a String.
     * 
     * @param the String value of this id
     */
    public String getCorrelationId() {
        return id;
    }

    /**
     * gets the value of this WSIFJMSCorrelationId as a byte array.
     * 
     * @param a byte array of this id
     */
    public byte[] getCorrelationIdAsBytes() {
        return id.getBytes();
    }

    /**
     * Compares this WSIFJMSCorrelationId to the specified object.
     * The result is true if and only if the argument is not null 
     * and is a WSIFCorrelationId object that represents the same
     * String id as this object.
     * 
     * @return true if the WSIFCorrelationId's have the same id,
     *         otherwise false;
     */
    public boolean equals(Object cid) {
        if (cid != null && cid instanceof WSIFCorrelationId) {
            return this.id.equals(((WSIFCorrelationId) cid).getCorrelationId());
        } else {
            return false;
        }
    }

    /**
     * The String representation of this object is returned.
     *
     * @return  the id string.
     */
    public String toString() {
        return id;
    }

    /**
     * Returns a hashcode for this object.
     *
     * @return  a hash code value for this object.
     */
    public int hashCode() {
        return id.hashCode();
    }
}
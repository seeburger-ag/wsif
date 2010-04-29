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

package org.apache.wsif.compiler.schema.tools;

/**
 * Insert the type's description here.
 * Creation date: (6/21/00 6:42:07 PM)
 * @author: Tian Zhao (tzhao@cs.purdue.edu)
 * @deprecated
 */
public class SchemaAny implements SchemaType {

    // Ignore: namespace, processContents, or anyAttrs

    private boolean isArray = false;
    private String targetURI;

    /**
     * SchemaAny constructor comment.
     */
    public SchemaAny(boolean isArray, String targetURI) {
        this.isArray = isArray;
        this.targetURI = targetURI;
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (6/21/00 6:45:17 PM)
     * @return int
     */
    public int getElementType() {
        return ANY;
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (6/22/00 3:58:59 PM)
     * @return java.lang.String
     */
    public String getName() {
        return null;
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (6/25/00 5:06:30 PM)
     * @return java.lang.String
     */
    public String getTargetURI() {
        return targetURI;

    }
    
    /**
     * Insert the method's description here.
     * Creation date: (6/24/00 3:04:31 PM)
     * @return boolean
     */
    public boolean isArray() {
        return isArray;
    }
}
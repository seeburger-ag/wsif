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
 * Creation date: (6/21/00 5:21:48 PM)
 * @author: Tian Zhao (tzhao@cs.purdue.edu)
 * @deprecated
 */
public interface SchemaType {

    public static final int COMPLEXTYPE = 0;
    public static final int SIMPLETYPE = 1;
    public static final int ELEMENT = 2;
    public static final int ATTRIBUTE = 3;
    public static final int ATTRIBUTEGROUP = 4;
    public static final int GROUP = 5;
    public static final int ANY = 6;
    public static final int ANYATTRIBUTE = 7;
    public static final int ALL = 8;
    public static final int CHOICE = 9;
    public static final int SEQUENCE = 10;

    /**
     * Insert the method's description here.
     * Creation date: (6/21/00 5:26:52 PM)
     * @return int
     */
    int getElementType();
    
    /**
     * Insert the method's description here.
     * Creation date: (6/22/00 3:58:18 PM)
     * @return java.lang.String
     */
    String getName();
    
    /**
     * Insert the method's description here.
     * Creation date: (6/25/00 5:04:56 PM)
     * @return java.lang.String
     */
    String getTargetURI();
}
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

import java.util.Vector;

import javax.xml.namespace.QName;

/**
 * Processed information of a XML Schema complexType
 * Creation date: (5/29/00 1:55:51 AM)
 * @author: Tian Zhao (tzhao@cs.purdue.edu)
 * @deprecated
 */
public class SchemaComplexType implements SchemaType {

    // list of attributes
    // Ignore: id, content, block, final in restriction or derived by restriction

    private String name;

    // if derived by restriction, then base is not null

    private QName base;
    private boolean isAbstract = false;
    private boolean isFinal = false;

    private Vector children;
    private String targetURI;

    /**
     * Insert the method's description here.
     * Creation date: (6/21/00 5:13:26 PM)
     */
    public SchemaComplexType(
        String name,
        QName base,
        boolean isAbstract,
        boolean isFinal,
        Vector children,
        String targetURI) {

        this.name = name;
        this.base = base;
        this.isAbstract = isAbstract;
        this.isFinal = isFinal;
        this.children = children;
        this.targetURI = targetURI;
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (6/21/00 5:15:20 PM)
     * @return org.apache.soap.util.xml.QName
     */
    public QName getBase() {
        return base;
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (6/21/00 5:31:14 PM)
     * @return java.util.Vector
     */
    public Vector getChildren() {
        return children;
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (6/21/00 5:29:26 PM)
     * @return int
     */
    public int getElementType() {
        return COMPLEXTYPE;
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (6/21/00 5:14:57 PM)
     * @return java.lang.String
     */
    public String getName() {
        return name;
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
     * Creation date: (6/21/00 5:15:40 PM)
     * @return boolean
     */
    public boolean isAbstract() {
        return isAbstract;
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (6/21/00 5:16:16 PM)
     * @return boolean
     */
    public boolean isFinal() {
        return isFinal;
    }
}
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

import javax.xml.namespace.QName;

/**
 * Insert the type's description here.
 * Creation date: (6/21/00 5:32:11 PM)
 * @author: Tian Zhao (tzhao@cs.purdue.edu)
 * @deprecated
 */
public class SchemaAttribute implements SchemaType {

    //Ignore: ID, use, value, or form
    private String name;
    private QName ref;
    private QName type;
    private SchemaSimpleType child;
    private String targetURI;

    /**
     * Insert the method's description here.
     * Creation date: (6/21/00 5:40:04 PM)
     */
    public SchemaAttribute(
        String name,
        QName ref,
        QName type,
        SchemaSimpleType child,
        String targetURI) {

        this.name = name;
        this.ref = ref;
        this.type = type;
        this.child = child;
        this.targetURI = targetURI;

    }
    
    /**
     * Insert the method's description here.
     * Creation date: (6/21/00 6:48:04 PM)
     * @return org.apache.wsif.compiler.schema.newtools.SchemaSimpleType
     */
    public SchemaSimpleType getChild() {
        return child;
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (6/21/00 5:41:20 PM)
     * @return int
     */
    public int getElementType() {
        return ATTRIBUTE;
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (6/21/00 5:42:29 PM)
     * @return java.lang.String
     */
    public String getName() {
        return name;
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (6/21/00 5:42:49 PM)
     * @return org.apache.soap.util.xml.QName
     */
    public QName getRef() {
        return ref;
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
     * Creation date: (6/21/00 5:43:08 PM)
     * @return boolean
     */
    public QName getType() {
        return type;
    }
}
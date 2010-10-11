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

import java.util.*;

/**
 * ClassFile
 * Creation date: (5/30/00 2:04:09 PM)
 * @author: Tian Zhao (tzhao@cs.purdue.edu)
 * @deprecated
 */
public class ClassFile {

    private Vector fields = new Vector();
    private Vector innerClasses = new Vector();
    String className; // This is a required field.
    String packageName; // By default, no package or super class name is required.
    String superClassName;
    boolean isAbstract = false;
    boolean isFinal = false;
    
    /**
     * Insert the method's description here.
     * Creation date: (5/30/00 2:52:00 PM)
     * @param cName java.lang.String
     * @param pName java.lang.String
     */
    public ClassFile(String className) {

        this.className = className;

    }
    
    /**
     * Insert the method's description here.
     * Creation date: (5/30/00 2:11:49 PM)
     * @param field schema2JavaVersion2.ClassField
     */
    public void addField(ClassField field) {

        fields.addElement(field);
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (5/30/00 2:17:15 PM)
     * @param innerClass schema2JavaVersion2.ClassFile
     */
    public void addInnerClass(ClassFile innerClass) {

        innerClasses.addElement(innerClass);
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (5/30/00 2:48:41 PM)
     * @return schema2JavaVersion2.ClassField
     * @param i int
     */
    public ClassField getField(int i) {

        return (ClassField) fields.elementAt(i);
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (5/30/00 2:18:37 PM)
     * @return int
     */
    public int getFieldCount() {
        return fields.size();
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (5/30/00 2:49:54 PM)
     * @return schema2JavaVersion2.ClassFile
     * @param i int
     */
    public ClassFile getInnerClass(int i) {
        return (ClassFile) innerClasses.elementAt(i);
    }
    
    /**
     * Insert the method's description here.
     * Creation date: (5/30/00 2:19:05 PM)
     * @return int
     */
    public int getInnerClassCount() {
        return innerClasses.size();
    }
}
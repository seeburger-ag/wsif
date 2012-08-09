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

package org.apache.wsif.compiler.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Types;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.xml.namespace.QName;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFException;

import com.ibm.wsdl.util.StringUtils;

/**
 * @author Matthew J. Duftler
 * @deprecated
 */
public class Utils {
	
    public static void addAllTypesElements(Definition def, List toList) {
        Types types = def.getTypes();
        if (types != null) {
            Iterator extEleIt = types.getExtensibilityElements().iterator();
            while (extEleIt.hasNext()) {
                // the unknown extensibility element are wrappers for DOM elements
                UnknownExtensibilityElement typesElement =
                    (UnknownExtensibilityElement) extEleIt.next();
                toList.add(typesElement);
            }
        }

        Map imports = def.getImports();

        if (imports != null) {
            Iterator valueIterator = imports.values().iterator();

            while (valueIterator.hasNext()) {
                List importList = (List) valueIterator.next();

                if (importList != null) {
                    Iterator importIterator = importList.iterator();

                    while (importIterator.hasNext()) {
                        Import tempImport = (Import) importIterator.next();

                        if (tempImport != null) {
                            Definition importedDef = tempImport.getDefinition();

                            if (importedDef != null) {
                                addAllTypesElements(importedDef, toList);
                            }
                        }
                    }
                }
            }
        }
    }

    public static List getAllTypesElements(Definition def) {
        List ret = new Vector();

        addAllTypesElements(def, ret);

        return ret;
    }

    public static String getPackageName(String fqClassName) {
        String packageName = "";

        if (fqClassName != null) {
            int index = fqClassName.lastIndexOf('.');

            if (index != -1) {
                packageName = fqClassName.substring(0, index);
            }
        }

        return packageName;
    }

    public static String getClassName(String fqClassName) {
        if (fqClassName != null) {
            int index = fqClassName.lastIndexOf('.');

            if (index != -1) {
                fqClassName = fqClassName.substring(index + 1);
            }
        }

        return fqClassName;
    }

    public static String queryJavaTypeName(
        QName type,
        String encodingStyleURI,
        Hashtable typeReg)
        throws IllegalArgumentException {
        TypeMapping tm = (TypeMapping) typeReg.get(type);

        if (tm != null) {
            return tm.javaType;
        } else if (
            encodingStyleURI != null
                && encodingStyleURI.equals(WSIFConstants.NS_URI_LITERAL_XML)) {
            return "org.w3c.dom.Element";
        } else {
            throw new IllegalArgumentException("No mapping was found for '" + type + "'.");
        }
    }

    public static String getQuotedString(Reader source, int indent)
        throws WSIFException {
        	
        char[] indentArray = new char[indent];
        Arrays.fill(indentArray, ' ');
        String indentStr = new String(indentArray);

        try {
            BufferedReader br = new BufferedReader(source);
            StringWriter sw = new StringWriter();
            int count = 0;
            String tempLine = null;

            while ((tempLine = br.readLine()) != null) {
                sw.write(
                    (count > 0
                        ? " + \"" + StringUtils.lineSeparatorStr + "\" +" + StringUtils.lineSeparator
                        : "")
                        + indentStr
                        + '\"'
                        + StringUtils.cleanString(tempLine)
                        + '\"');

                count++;
            }

            return sw.toString();
        } catch (IOException e) {
            throw new WSIFException("Problem writing strings.", e);
        }
    }

    public static String convertToObject(String sourceClassName, String expr)
        throws WSIFException {
        return convertClass(sourceClassName, expr, "java.lang.Object");
    }

    public static String convertFromObject(String expr, String targetClassName)
        throws WSIFException {
        return convertClass("java.lang.Object", expr, targetClassName);
    }

    private static String convertClass(
        String sourceClassName,
        String expr,
        String targetClassName)
        throws WSIFException {
        if (sourceClassName == null || targetClassName == null) {
            throw new WSIFException(
                "I was unable to convert an object from "
                    + sourceClassName
                    + " to "
                    + targetClassName
                    + ".");
        }

        String shortTargetClassName = getShortName(targetClassName);

        if (sourceClassName.equals("java.lang.Object")) {
            if (isPrimitive(targetClassName)) {
                return "(("
                    + getWrapperClassName(targetClassName)
                    + ")"
                    + expr
                    + ")."
                    + shortTargetClassName
                    + "Value()";
            } else {
                return "(" + targetClassName + ")" + expr;
            }
        } else if (
            isPrimitive(sourceClassName) && targetClassName.equals("java.lang.Object")) {
            return "new " + getWrapperClassName(sourceClassName) + "(" + expr + ")";
        } else {
            // Target class must be "assignableFrom" source class.
            return expr;
        }
    }

    private static String getShortName(String className) {
        if (className.startsWith("java.lang."))
            return className.substring(10);
        else
            return className;
    }

    private static String getWrapperClassName(String primitiveClassName) {
        if (primitiveClassName.equals("int"))
            return "Integer";
        else if (primitiveClassName.equals("char"))
            return "Character";
        else
            return getCapitalized(primitiveClassName);
    }

    private static String getCapitalized(String className) {
        return Character.toUpperCase(className.charAt(0)) + className.substring(1);
    }

    private static boolean isPrimitive(String className) {
        String[] primNames =
            {
                "boolean",
                "byte",
                "char",
                "short",
                "int",
                "long",
                "float",
                "double",
                "void" };

        for (int i = 0; i < primNames.length; i++) {
            if (primNames[i].equals(className)) {
                return true;
            }
        }
        return false;
    }
}
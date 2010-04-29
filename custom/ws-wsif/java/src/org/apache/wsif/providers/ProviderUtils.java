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

package org.apache.wsif.providers;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.xml.namespace.QName;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.schema.ComplexType;
import org.apache.wsif.schema.ElementType;
import org.apache.wsif.schema.Parser;
import org.apache.wsif.schema.SequenceElement;

import com.ibm.wsdl.PartImpl;

/**
 * A class of static utility methods for use across multiple providers
 * 
 * @author Owen Burroughs <owenb@apache.org>
 */
public class ProviderUtils {
	
	/**
	 * Convert an array of any dimensions containing only Strings of length 1, to an array
	 * of java.lang.Characters which has the same dimensions.
	 * @param obj The array of Strings
	 * @return The Character array
	 * @exception A WSIFException thrown if the conversion fails for any reason.
	 */
	public static Object stringArrayToCharacterArray(Object obj) throws WSIFException {
		return stringArrayToCharacterArray(obj, null);
	}
	
	/**
	 * Convert an array of any dimensions containing only Strings of length 1, to an array
	 * of java.lang.Characters which has the same dimensions.
	 * @param obj The array of Strings
	 * @param ret temporary array used in recursion. The first call should be passed null.
	 * @return The Character array
	 * @exception A WSIFException thrown if the conversion fails for any reason.
	 */
    protected static Object stringArrayToCharacterArray(Object obj, Object ret) throws WSIFException {
    	if (obj.getClass().isArray()) {    		
    		Object[] objs = (Object[]) obj;
    		ret = new Object[objs.length];
    		for (int i=0; i<objs.length; i++) {
    			Object temp = stringArrayToCharacterArray(objs[i], ((Object[]) ret)[i]);
    			if (i == 0) {
    				Class tempc = temp.getClass();
    				ret = Array.newInstance(tempc, objs.length);    				
    			}
    			((Object[]) ret)[i] = temp;
    		}
    		return ret;
    	} else if (obj instanceof String) {
    		String s = (String) obj;
    		if (s.length() == 1) {
    			ret = new Character(s.charAt(0));
    			return ret;
    		} else {
    			throw new WSIFException("String is longer than 1 character");
    		}
    	} else {
    		throw new WSIFException("Array entry is not a String or another array");
    	}
    }

	/**
	 * Convert an array of any dimensions containing only Strings of length 1, to an array
	 * of chars which has the same dimensions.
	 * @param obj The array of Strings
	 * @return The char array
	 * @exception A WSIFException thrown if the conversion fails for any reason.
	 */
	public static Object stringArrayToCharArray(Object obj) throws WSIFException {
		return stringArrayToCharArray(obj, null);
	}

	/**
	 * Convert an array of any dimensions containing only Strings of length 1, to an array
	 * of chars which has the same dimensions.
	 * @param obj The array of Strings
	 * @param ret temporary array used in recursion. The first call should be passed null.
	 * @return The char array
	 * @exception A WSIFException thrown if the conversion fails for any reason.
	 */	
    protected static Object stringArrayToCharArray(Object obj, Object ret) throws WSIFException {
    	if (obj.getClass().isArray()) {    		
    		Object[] objs = (Object[]) obj;
    		ret = new Object[objs.length];
    		for (int i=0; i<objs.length; i++) {
    			Object temp = stringArrayToCharArray(objs[i], ((Object[]) ret)[i]);
    			if (i == 0) {
    				Class tempc = temp.getClass();
    				ret = Array.newInstance(tempc, objs.length);    				
    			}
    			((Object[]) ret)[i] = temp;
    			if (temp instanceof Character && (i == objs.length - 1)) {
					char[] tempca = new char[objs.length];
					for (int j=0; j<objs.length; j++) {
						tempca[j] = ((Character[]) ret)[j].charValue();
					}
					ret = tempca;		    				
    			}
    		}
    		return ret;
    	} else if (obj instanceof String) {
    		String s = (String) obj;
    		if (s.length() == 1) {
    			ret = new Character(s.charAt(0));
    			return ret;
    		} else {
    			throw new WSIFException("String is longer than 1 character");
    		}
    	} else {
    		throw new WSIFException("Array entry is not a String or another array");
    	}
    }

	/**
	 * Convert an array of java.lang.Characters of any dimensions to an equivalent array 
	 * of java.lang.Strings which has the same dimensions.
	 * @param obj The array of Characters
	 * @return The array of Strings
	 * @exception A WSIFException thrown if the conversion fails for any reason.
	 */
	public static Object characterArrayToStringArray(Object obj) throws WSIFException {
		return characterArrayToStringArray(obj, null);
	}

	/**
	 * Convert an array of java.lang.Characters of any dimensions to an equivalent array 
	 * of java.lang.Strings which has the same dimensions.
	 * @param obj The array of Characters
	 * @param ret temporary array used in recursion. The first call should be passed null.
	 * @return The array of Strings
	 * @exception A WSIFException thrown if the conversion fails for any reason.
	 */
    protected static Object characterArrayToStringArray(Object obj, Object ret) throws WSIFException {
    	if (obj.getClass().isArray()) {    		
    		Object[] objs = (Object[]) obj;
    		ret = new Object[objs.length];
    		for (int i=0; i<objs.length; i++) {
    			Object temp = characterArrayToStringArray(objs[i], ((Object[]) ret)[i]);
    			if (i == 0) {
    				Class tempc = temp.getClass();
    				ret = Array.newInstance(tempc, objs.length);    				
    			}
    			((Object[]) ret)[i] = temp;
    		}
    		return ret;
    	} else if (obj instanceof Character) {
    		String s = obj.toString();
			return s;
    	} else {
    		throw new WSIFException("Array entry is not a Character or another array");
    	}
    }

	/**
	 * Convert an array of chars of any dimensions to an equivalent array 
	 * of java.lang.Strings which has the same dimensions.
	 * @param obj The array of chars
	 * @return The array of Strings
	 * @exception A WSIFException thrown if the conversion fails for any reason.
	 */
	public static Object charArrayToStringArray(Object obj) throws WSIFException {
		return charArrayToStringArray(obj, null);
	}

	/**
	 * Convert an array of chars of any dimensions to an equivalent array 
	 * of java.lang.Strings which has the same dimensions.
	 * @param obj The array of chars
	 * @param ret temporary array used in recursion. The first call should be passed null.
	 * @return The array of Strings
	 * @exception A WSIFException thrown if the conversion fails for any reason.
	 */
    protected static Object charArrayToStringArray(Object obj, Object ret) throws WSIFException {
    	if (obj.getClass().isArray()) {
    		if (obj instanceof char[]) {
    			char[] ca = (char[]) obj;
    			Character[] chra = new Character[ca.length];
    			for (int j=0; j<ca.length; j++) {
    				chra[j] = new Character(ca[j]);
    			}
    			obj = chra;
    		}    		
    		Object[] objs = (Object[]) obj;
    		ret = new Object[objs.length];
    		for (int i=0; i<objs.length; i++) {
    			Object temp = charArrayToStringArray(objs[i], ((Object[]) ret)[i]);
    			if (i == 0) {
    				Class tempc = temp.getClass();
    				ret = Array.newInstance(tempc, objs.length);    				
    			}
    			((Object[]) ret)[i] = temp;
    		}
    		return ret;
    	} else if (obj instanceof Character) {
    		String s = obj.toString();
			return s;
    	} else {
    		throw new WSIFException("Array entry is not a char or another array");
    	}
    }

	/**
	 * Convert a String to a Character. If the String is longer than one character
	 * this method will return null;
	 * @param str The String
	 * @return The Character or null if the String was longer than one character	 
	 */
    public static Character stringToCharacter(String str) {
        if (str.length() != 1)
            return null;
        return new Character(str.charAt(0));
    }

	/**
	 * Returns a default Object value for a given Class. If the Class is a primitive type
	 * the method will return the default value for that primitive type wrapped up in its
	 * object form. For example, invoking the method with int.class will return a new 
	 * java.lang.Integer with an int value of 0. If the Class does not represent a
	 * primitive type then null will be returned.
	 * @param cls The Class
	 * @return The default object value
	 */
	public static Object getDefaultObject(Class cls) {
		if (cls == null) {
			return null;
		} else if (cls.isPrimitive()) {
			if (cls.getName().equals("int")) {
				return new Integer(0);
			} else if (cls.getName().equals("char")) {
				return new Character('0');
			} else if (cls.getName().equals("long")) {
				return new Long(0);
			} else if (cls.getName().equals("short")) {
				short s = 0;
				return new Short(s);
			} else if (cls.getName().equals("double")) {
				return new Double(0);
			} else if (cls.getName().equals("boolean")) {
				return new Boolean(false);
			} else if (cls.getName().equals("byte")) {
				byte b = 0;
				return new Byte(b);
			} else {
				return new Float(0);
			}
		} else {
			return null;
		}
	}   	

	/**
	 * Tests if this is wrapped stype operation.
	 * An operation is wrapped if:
	 *    - the input message name has a part with an element which
	 *      has the same name as the operation name, and the output
	 *      message has an element with a name the same as the 
	 *      operation name appended with "Response"
	 *    - there is only a single part (but with attachments
	 *      there may be multiple parts so ignore this for now?)
	 */
	public static boolean isUnwrapable(Operation op) {
		boolean unwrapable = true;
		Input in = op.getInput();
		if (in != null) {
			Message inMsg = in.getMessage();
			if (inMsg != null) {
		    	List parts = inMsg.getOrderedParts(null);
		    	if (parts.size() > 0) {
		    	    String name = op.getName();
				    if (getWrapperPart(parts, name) == null) {
					    unwrapable = false;
				    }
		    	}
			}
			
		}
        if (unwrapable) {
   		    Output out = op.getOutput();
		    if (out != null) {
			    Message outMsg = out.getMessage();
			    if (outMsg != null) {
			    	List parts = outMsg.getOrderedParts(null);
    		    	if (parts.size() > 0) {
	    		    	String name = op.getName() + "Response";
    	    			if (getWrapperPart(parts, name) == null) {
	    	    			unwrapable = false;
		    	    	}
    		    	}
			    }
		    }
        }
		return unwrapable;
	}

	/**
	 * Gets the wrapped Part if this is wrapped document literal
	 * stype operation. An operation is wrapped if:
	 *    - there is only one input or output message part
	 *      and that part is an element not a type 
	 *      (MIME means there can be many parts, so all this 
	 *       can check is that there is only one element part)
	 *    - the message name is the same as the operation name
	 *      (for a response the operation name is appened with "Response")
	 */
	public static Part getWrapperPart(List parts, String operationName) {
		boolean wrapped = !(parts==null);
		Part elementPart = null;
		for (int i = 0; wrapped && i < parts.size(); i++) {
			Part p = (Part) parts.get(i);
			if (p.getElementName() != null) {
				if (elementPart == null) {
					elementPart = p;
   				    String pName = p.getElementName().getLocalPart();
				    if (!operationName.equals(pName)) {
					   wrapped = false;
				    }
				} else {
					wrapped = false;
				}
			}
		}
		if (!wrapped) {
			elementPart = null;
		}
		return elementPart;
	}

    /**
	 * Unwraps a wrapped DocLit style part.
	 */
	public static List unWrapPart(Part p, Definition def) throws WSIFException {
		return unWrapPart(p, def, null);
	}
	
	/**
	 * Unwraps a wrapped DocLit style part.
	 */
	public static List unWrapPart(Part p, Definition def, WSIFMessage context) throws WSIFException {

		ArrayList l = null;
        if (context != null) {
            try {
                l = (ArrayList) context.getObjectPart(WSIFConstants.CONTEXT_SCHEMA_TYPES);
            } catch (Exception e) {
                Trc.ignoredException(e);
            }
        }
        if (l == null) {
        	l = new ArrayList();
			Parser.getAllSchemaTypes(def, l, null);
        }
		if (l == null || l.size()<1) {
			throw new WSIFException("no schema elements found");
		}

	    QName partQN = p.getElementName();
		if (partQN == null) {
			throw new WSIFException("part has no QName");
		}
	    
		ElementType et = getElementType(l, partQN);
		if (et == null) {
			throw new WSIFException("no ElementType found for part: " + p);
		}

	    ArrayList unWrappedParts = new ArrayList();
	    
   		List children = et.getChildren();
	    ComplexType ct = null;
    	if (children == null || children.size() < 1) {
       	    ct = getComplexType(l, et.getElementType() );
	    } else {
	        ct = (ComplexType) children.get(0);
    	}
        if (ct == null) {
	        throw new WSIFException("cannot find complex type from ElementType: " + et);
        }
    	
   	    SequenceElement[] se = ct.getSequenceElements();
        if (se == null) {
	        throw new WSIFException("no sequence elements found on: " + ct);
        }
	    for (int i=0; i< se.length; i++) {
	       PartImpl np = new PartImpl();
	       QName type = se[i].getTypeName();
	       if (type==null) {
		       throw new WSIFException("sequence element has no type name: " + se[i]);
	       }
	       np.setName(type.getLocalPart());
	       np.setElementName(se[i].getElementType());
	       if (np.getElementName() == null) {
    	       np.setTypeName(se[i].getTypeName());
	       }
	       unWrappedParts.add(np);
	    }

		return unWrappedParts;
	}

    protected static ElementType getElementType(List l, QName qn) {
    	ElementType et = null;
		for (int i=0; i<l.size() && et==null; i++ ){
			Object o = l.get(i);
			if ( o instanceof ElementType ) {
                QName etQN = ((ElementType)o).getTypeName();
				if ( qn.equals(etQN) ){
					et = (ElementType)o;
				}
			}
		}
		return et;
    }
	
    protected static ComplexType getComplexType(List l, QName type) {
       	ComplexType ct = null;
    	if (type != null && l != null) {
    		String name = type.getLocalPart();
	    	for (int i=0; i<l.size() && ct==null; i++ ) {
		    	Object o = l.get(i);
		    	if (o instanceof ComplexType) {
    			    if (name.equals(  ((ComplexType)o).getTypeName().getLocalPart() )){
	    			    ct = (ComplexType)o;
		    	    }
		    	}
		    }
    	}
		return ct;
    }

    /**
     * Gets the type of a Part, if the Part doesn't have a type,
     * then gets the Element name as WSIF treats this as the same thing.
     */
    public static QName getPartType(Part p) {
		QName type = p.getTypeName();
		if (type == null) {
			type = p.getElementName();
		}
		return type;
    }

    /**
     * Capatalise the first character of a String
     * @param s   the string to be capatalized
     * @return String the capatalized String
     */
    public static String capitalizeFirst(String s) {
        StringBuffer sb = new StringBuffer(s);
        if (sb.length() > 0) {
            sb.setCharAt(0, (char) ((sb.charAt(0) & (char) 223)));
        }
        return sb.toString();
    }

    /**
     * Lowercase the first character of a String
     * @param s   the string to be lowercased
     * @return String the lowercased String
     */
    public static String lowercaseFirst(String s) {
        StringBuffer sb = new StringBuffer(s);
        if (sb.length() > 0) {
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        }
        return sb.toString();
    }

}

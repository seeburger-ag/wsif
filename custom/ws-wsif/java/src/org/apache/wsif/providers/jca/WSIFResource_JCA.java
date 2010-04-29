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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The WSIFResource_JCA class accesses message resources.
 * 
 * @author John Green
 * @author Michael Beisiegel
 * @author Piotr Przybylski <piotrp@ca.ibm.com>
 */
public class WSIFResource_JCA {

	private static final long serialVersionUID = 1L;
	private static ResourceBundle messages = ResourceBundle.getBundle("org.apache.wsif.providers.jca.WSIFResource_JCA", Locale.getDefault());
	private static HashMap defaultMessages = new HashMap();

	// Initialize default messages, used if the NL resources are not available.
	static {
		defaultMessages.put("WSIF1000E", "WSIF1000E: ResourceException thrown during execution of the interaction, check target exception for details.");
		defaultMessages.put("WSIF1001E", "WSIF1001E: Could not instantiate Format Handler.");
		defaultMessages.put("WSIF1002E", "WSIF1002E: Exception thrown during instantiation of the format handler.");
		defaultMessages.put("WSIF1003E", "WSIF1003E: Exception thrown during execute method of the Interaction, check target exception for details.");
		defaultMessages.put("WSIF1004E", "WSIF1004E: Exception thrown in WSIFMessage_JCAStreamable read method.");
		defaultMessages.put("WSIF1005E", "WSIF1005E: Exception thrown in WSIFMessage_JCAStreamable write method.");
		defaultMessages.put("WSIF1006E", "WSIF1006E: Exception thrown in the initialization of the Service SessionBean.");
		defaultMessages.put("WSIF1007E", "WSIF1007E: Exception thrown in getObjectPart() method.");
		defaultMessages.put("WSIF1008E", "WSIF1008E: Exception thrown during execution of the interaction, check target exception for details.");
		defaultMessages.put("WSIF1009E", "UNKNOWN MESSAGE");
	}

	private WSIFResource_JCA() {
		super();
	}

	/**
	 * Returns a message, where no substitution variables are used.
	 */
	public static String get(String key) {
		try {
			if (key != null && messages != null)
				return messages.getString(key);
		}
		catch (MissingResourceException exn) {
			return ((String) WSIFResource_JCA.defaultMessages.get(key));
		}
		return null;
	}

	/**
	 * Returns a message, where one substitution variable is used.
	 */
	public static String get(String key, Object arg) {
		return get(key, new Object[] { arg });
	}

	/**
	 * Returns a message, where two substitution variable is used.
	 */
	public static String get(String key, Object arg1, Object arg2) {
		return get(key, new Object[] { arg1, arg2 });
	}

	/**
	 * Returns a message, where three substitution variable is used.
	 */
	public static String get(String key, Object arg1, Object arg2, Object arg3) {
		return get(key, new Object[] { arg1, arg2, arg3 });
	}

	/**
	 * Returns a message, where four substitution variable is used.
	 */
	public static String get(String key, Object arg1, Object arg2, Object arg3, Object arg4) {
		return get(key, new Object[] { arg1, arg2, arg3, arg4 });
	}
	
	/**
	 * Returns a message, where five substitution variable is used.
	 */
	public static String get(String key, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		return get(key, new Object[] { arg1, arg2, arg3, arg4, arg5 });
	}
	
	/**
	 * Returns a message, where an array of substitution variables is used.
	 */
	public static String get(String key, Object[] args) {
		String text = get(key);
		if (text == null) {
			text = (String) WSIFResource_JCA.defaultMessages.get(key);
		}
		if (text == null) {
			return null;
		}
		return MessageFormat.format(text, args);
	}

}
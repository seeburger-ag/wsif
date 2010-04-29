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

package org.apache.wsif.logging;

import java.util.Locale;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class MessageLogger {
    private static Log log = LogFactory.getLog("wsif");

    /**
     * Message resource bundle.
     */
    private static ResourceBundle messages = null;

    /**
     * Private constructor so no one can instantiate this class.
     */
    private MessageLogger() {
    	Trc.entry(this);
    	Trc.exit();
    }
    
    public static boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    public static boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    public static boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    public static boolean isFatalEnabled() {
        return log.isFatalEnabled();
    }

    public static void log(String key) {
    	Trc.entry(null,key);
        try {
            logIt(key.charAt(key.length() - 1), getMessage(key));
        } catch (MissingResourceException mre) {
            Trc.exception(mre);
            handleMissingResourceException(mre, key, null);
        }
        Trc.exit();
    }

    /**
     * Logs the message with the given key.  If an argument is 
     * specified in the message (in the format of "{0}") then 
     * fill in that argument with the value of var.
     */
    public static void log(String key, Object var) {
    	Trc.entry(null,key,var);
        String[] args = { var == null ? "<null>" : var.toString()};
        try {
            logIt(
                key.charAt(key.length() - 1),
                MessageFormat.format(getMessage(key), args));
        } catch (MissingResourceException mre) {
            Trc.exception(mre);
            handleMissingResourceException(mre, key, args);
        }
        Trc.exit();
    }

    /**
     * Logs the message with the given key.  If arguments are 
     * specified in the message (in the format of "{0} {1}") 
     * then fill them in with the values of var1 and var2, respectively.
     */
    public static void log(String key, Object var1, Object var2) {
    	Trc.entry(null,key,var1,var2);
        String[] args = {
            var1 == null ? "<null>" : var1.toString(),
            var2 == null ? "<null>" : var2.toString()};
        try {
            logIt(
                key.charAt(key.length() - 1),
                MessageFormat.format(getMessage(key), args));
        } catch (MissingResourceException mre) {
            Trc.exception(mre);
            handleMissingResourceException(mre, key, args);
        }
        Trc.exit();
    }

    /**
     * Logs the message with the given key.  Replace each "{X}" 
     * in the message with vars[X].  If there are more vars than 
     * {X}'s, then the extra vars are ignored.  If there are more {X}'s
     * than vars, then a java.text.ParseException (subclass of 
     * RuntimeException) is thrown.
     */
    public static void log(String key, Object[] vars) {
    	Trc.entry(null,key,vars);
        String[] args;
        if (vars == null) {
            args = new String[1];
            args[0] = "<null>";
        } else {
            args = new String[vars.length];
            for (int i = 0; i < vars.length; i++)
                args[i] = (vars[i] == null ? "<null>" : vars[i].toString());
        }

        try {
            logIt(
                key.charAt(key.length() - 1),
                MessageFormat.format(getMessage(key), args));
        } catch (MissingResourceException mre) {
            Trc.exception(mre);
            handleMissingResourceException(mre, key, args);
        }
        Trc.exit();
    }

    /**
     * Get the message with the given key.  
     * Load the ResourceBundle here instead of in a static initialiser so it
     * won't be loaded if we never log a message.
     */
    private static String getMessage(String key) {
        if (messages == null)
            messages = ResourceBundle.getBundle("org.apache.wsif.catalog.Messages");

        return messages.getString(key);
    }

    private static void logIt(char severity, String text) {
        if (severity == 'I') {
            if (isInfoEnabled())
                log.info(text);
        } else if (severity == 'W') {
            if (isWarnEnabled())
                log.warn(text);
        } else if (severity == 'F') {
            if (isFatalEnabled())
                log.fatal(text);
        } else {
            // default is error 
            if (isErrorEnabled())
                log.error(text);
        }
    }

    private static void handleMissingResourceException(
        MissingResourceException mre,
        String key,
        String[] args) {

        StringBuffer sb = new StringBuffer("WSIF: Unable to display message ");
        sb.append(key);
        if (args != null && args.length > 0) {
            sb.append(" with arguments ");
            for (int i = 0; i < args.length; i++) {
                if (i != 0)
                    sb.append(", ");
                sb.append(args[i]);
            }
        }
        sb.append(" because WSIF MessageLogger caught ");
        sb.append(mre.toString());
        log.error(sb.toString());
    }
}

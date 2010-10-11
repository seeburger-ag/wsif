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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Trc provides trace support for WSIF and is a wrapper around
 * commons-logging. It adds value to commons-logging by providing
 * entry/exit/exception trace; never throwing an exception back 
 * even if asked to trace something really stupid; and tracing
 * references to large WSDL objects, unless specifically asked to
 * trace out the entire object. Trc also provides brief() methods
 * (called from deep() methods) which trace out WSDL objects (and
 * others) in a consistent and helpful way. The goals of Trc are
 * to not impact performance if trace is off; to provide a rich
 * and easy-to-use API for WSIF; to enable creation of trace files
 * that will help developers to easily diagnose problems in WSIF.
 * 
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class Trc {
    public static final int LOG_LEVEL_TRACE = 0;
    public static final int LOG_LEVEL_DEBUG = 1;
    public static final int LOG_LEVEL_INFO = 2;
    public static final int LOG_LEVEL_WARN = 3;
    public static final int LOG_LEVEL_ERROR = 4;
    public static final int LOG_LEVEL_FATAL = 5;
    private static final String wsifPackageName = "org.apache.wsif";
    private static Log log = LogFactory.getLog(wsifPackageName+".*");
    public static boolean ON = log.isDebugEnabled();
    private static int currentLogLevel;

    static {
        if (log.isTraceEnabled()) {
            currentLogLevel = LOG_LEVEL_TRACE;
	}
	else if (log.isDebugEnabled()) {
            currentLogLevel = LOG_LEVEL_DEBUG;
	}
	else if (log.isInfoEnabled()) {
            currentLogLevel = LOG_LEVEL_INFO;
	}
	else if (log.isWarnEnabled()) {
            currentLogLevel = LOG_LEVEL_WARN;
	}
	else if (log.isErrorEnabled()) {
            currentLogLevel = LOG_LEVEL_ERROR;
	}
        else {
            currentLogLevel = LOG_LEVEL_FATAL;
	}
    }

    private static Log traceLog = LogFactory.getLog(wsifPackageName+".logging.*");
    /**
     * traceTrace means "is trace itself being traced?"
     */
    private static boolean traceTrace = false;
//    private static boolean traceTrace = traceLog.isDebugEnabled();

    private final static String ENTRY = "ENTRY";
    private final static String EXIT = "EXIT ";
    private final static String EXCEPTION = "EXCEPTION";
    private final static String EVENT = "EVENT";
    
    /**
     * Private constructor ensures no one can instantiate this class.
     */
    private Trc() {
        super();
    }
    
    public static boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    public static boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    /**
     * Traces entry into a method with no parameters.
     */
    public static void entry(Object that) {
        try {
            if (!ON)
                return;
            traceIt(that, ENTRY, false, null);
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    /**
     * Traces entry into a method with one parameter.
     */
    public static void entry(Object that, Object p1) {
        try {
            if (!ON)
                return;
            traceIt(that, ENTRY, false, new Object[] { p1 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    /**
     * Traces entry into a method with two parameters.
     */
    public static void entry(Object that, Object p1, Object p2) {
        try {
            if (!ON)
                return;
            traceIt(that, ENTRY, false, new Object[] { p1, p2 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void entry(Object that, Object p1, Object p2, Object p3) {
        try {
            if (!ON)
                return;
            traceIt(that, ENTRY, false, new Object[] { p1, p2, p3 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void entry(
        Object that,
        Object p1,
        Object p2,
        Object p3,
        Object p4) {
        try {
            if (!ON)
                return;
            traceIt(that, ENTRY, false, new Object[] { p1, p2, p3, p4 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void entry(
        Object that,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5) {
        try {
            if (!ON)
                return;
            traceIt(that, ENTRY, false, new Object[] { p1, p2, p3, p4, p5 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void entry(
        Object that,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6) {
        try {
            if (!ON)
                return;
            traceIt(that, ENTRY, false, new Object[] { p1, p2, p3, p4, p5, p6 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void entry(
        Object that,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7) {
        try {
            if (!ON)
                return;
            traceIt(that, ENTRY, false, new Object[] { p1, p2, p3, p4, p5, p6, p7 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void entry(
        Object that,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8) {
        try {
            if (!ON)
                return;
            traceIt(that, ENTRY, false, new Object[] { p1, p2, p3, p4, p5, p6, p7, p8 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void entry(
        Object that,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9) {
        try {
            if (!ON)
                return;
            traceIt(
                that,
                ENTRY,
                false,
                new Object[] {
                    p1,
                    p2,
                    p3,
                    p4,
                    p5,
                    p6,
                    p7,
                    p8,
                    p9 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void entry(
        Object that,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9,
        Object p10) {
        try {
            if (!ON)
                return;
            traceIt(
                that,
                ENTRY,
                false,
                new Object[] {
                    p1,
                    p2,
                    p3,
                    p4,
                    p5,
                    p6,
                    p7,
                    p8,
                    p9,
                    p10 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void entry(
        Object that,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9,
        Object p10,
        Object p11) {
        try {
            if (!ON)
                return;
            traceIt(
                that,
                ENTRY,
                false,
                new Object[] {
                    p1,
                    p2,
                    p3,
                    p4,
                    p5,
                    p6,
                    p7,
                    p8,
                    p9,
                    p10,
                    p11 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void entry(
        Object that,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9,
        Object p10,
        Object p11,
        Object p12) {
        try {
            if (!ON)
                return;
            traceIt(
                that,
                ENTRY,
                false,
                new Object[] {
                    p1,
                    p2,
                    p3,
                    p4,
                    p5,
                    p6,
                    p7,
                    p8,
                    p9,
                    p10,
                    p11,
                    p12 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void entry(
        Object that,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9,
        Object p10,
        Object p11,
        Object p12,
        Object p13) {
        try {
            if (!ON)
                return;
            traceIt(
                that,
                ENTRY,
                false,
                new Object[] {
                    p1,
                    p2,
                    p3,
                    p4,
                    p5,
                    p6,
                    p7,
                    p8,
                    p9,
                    p10,
                    p11,
                    p12,
                    p13 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void entry(
        Object that,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9,
        Object p10,
        Object p11,
        Object p12,
        Object p13,
        Object p14) {
        try {
            if (!ON)
                return;
            traceIt(
                that,
                ENTRY,
                false,
                new Object[] {
                    p1,
                    p2,
                    p3,
                    p4,
                    p5,
                    p6,
                    p7,
                    p8,
                    p9,
                    p10,
                    p11,
                    p12,
                    p13,
                    p14 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void entry(
        Object that,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9,
        Object p10,
        Object p11,
        Object p12,
        Object p13,
        Object p14,
        Object p15) {
        try {
            if (!ON)
                return;
            traceIt(
                that,
                ENTRY,
                false,
                new Object[] {
                    p1,
                    p2,
                    p3,
                    p4,
                    p5,
                    p6,
                    p7,
                    p8,
                    p9,
                    p10,
                    p11,
                    p12,
                    p13,
                    p14,
                    p15 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void entryExpandWsdl(Object that, Object[] parms) {
        try {
            if (!ON)
                return;
            traceIt(that,ENTRY,true,parms);
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void entry(Object that, boolean b) {
        if (ON)
            entry(that, new Boolean(b));
    }
    public static void entry(Object that, int i) {
        if (ON)
            entry(that, new Integer(i));
    }

    /**
     * Traces exit from a method.
     */
    public static void exit() {
        try {
            if (!ON)
                return;
            traceIt(null,EXIT,false,null);
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    /**
     * Traces exit from a method that returns a value.
     */
    public static void exit(Object returnValue) {
        try {
            if (!ON)
                return;
            traceIt(null, EXIT, false, new Object[] { returnValue });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    /**
     * Traces exit from a method that returns a value.
     */
    public static void exit(boolean b) {
        if (ON)
            exit(new Boolean(b));
    }
    public static void exit(int i) {
        if (ON)
            exit(new Integer(i));
    }

    /**
     * Traces exit from a method that returns a value.
     */
    public static void exitExpandWsdl(Object returnValue) {
        try {
            if (!ON)
                return;
            traceIt(null, EXIT, true, new Object[] { returnValue });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void exception(Throwable exception) {
        try {
            if (!ON)
                return;
            log.debug(EXCEPTION, exception);
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void ignoredException(Throwable exception) {
        try {
            if (!ON)
                return;
            traceIt(
                null,
                EVENT,
                false,
                new Object[] { "Caught and handled throwable: " + exception.toString()});
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void exceptionInTrace(Exception exception) {
        try {
            log.debug("****** Exception in WSIF trace statement ******",exception);
        } catch (Exception ignored) {
            // exceptionInTrace(ignored); 
            // Not much else we can do here - we shouldn't call
            // exceptionInTrace else we may recurse
        }
    }

    public static void event(Object that, Object p1) {
        try {
            if (!ON)
                return;
            traceIt(that, EVENT, false, new Object[] { p1 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void event(Object that, Object p1, Object p2) {
        try {
            if (!ON)
                return;
            traceIt(that, EVENT, false, new Object[] { p1, p2 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void event(Object that, Object p1, Object p2, Object p3) {
        try {
            if (!ON)
                return;
            traceIt(that, EVENT, false, new Object[] { p1, p2, p3 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void event(
        Object that,
        Object p1,
        Object p2,
        Object p3,
        Object p4) {
        try {
            if (!ON)
                return;
            traceIt(that, EVENT, false, new Object[] { p1, p2, p3, p4 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void event(
        Object that,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5) {
        try {
            if (!ON)
                return;
            traceIt(that, EVENT, false, new Object[] { p1, p2, p3, p4, p5 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void event(
        Object that,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6) {
        try {
            if (!ON)
                return;
            traceIt(
                that,
                EVENT,
                false,
                new Object[] { p1, p2, p3, p4, p5, p6 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void event(
        Object that,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7) {
        try {
            if (!ON)
                return;
            traceIt(
                that,
                EVENT,
                false,
                new Object[] { p1, p2, p3, p4, p5, p6, p7 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void event(
        Object that,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8) {
        try {
            if (!ON)
                return;
            traceIt(
                that,
                EVENT,
                false,
                new Object[] { p1, p2, p3, p4, p5, p6, p7, p8 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void event(
        Object that,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9) {
        try {
            if (!ON)
                return;
            traceIt(
                that,
                EVENT,
                false,
                new Object[] { p1, p2, p3, p4, p5, p6, p7, p8, p9 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static void event(
        Object that,
        Object p1,
        Object p2,
        Object p3,
        Object p4,
        Object p5,
        Object p6,
        Object p7,
        Object p8,
        Object p9,
        Object p10) {
        try {
            if (!ON)
                return;
            traceIt(
                that,
                EVENT,
                false,
                new Object[] { p1, p2, p3, p4, p5, p6, p7, p8, p9, p10 });
        } catch (Exception e) {
            exceptionInTrace(e);
        }
    }

    public static String brief(Definition d) {
      return (!ON)?"":brief(LOG_LEVEL_DEBUG, d);
    }

    public static String brief(int outputLevel, Definition d) {
        try {
            if (currentLogLevel > outputLevel)
                return "";
            if (d == null)
                return "null";
            if (d.getQName() == null)
                return "UNNAMED," + Integer.toHexString(d.hashCode());
            return d.getQName() + "," + Integer.toHexString(d.hashCode());
        } catch (Exception e) {
            exceptionInTrace(e);
        }
        return "";
    }

    public static String brief(PortType pt) {
      return (!ON)?"":brief(LOG_LEVEL_DEBUG, pt);
    }

    public static String brief(int outputLevel, PortType pt) {
        try {
            if (currentLogLevel > outputLevel)
                return "";
            if (pt == null)
                return "null";
            if (pt.getQName() == null)
                return "UNNAMED," + Integer.toHexString(pt.hashCode());
            return pt.getQName() + "," + Integer.toHexString(pt.hashCode());
        } catch (Exception e) {
            exceptionInTrace(e);
        }
        return "";
    }

    public static String brief(Service s) {
      return (!ON)?"":brief(LOG_LEVEL_DEBUG, s);
    }

    public static String brief(int outputLevel, Service s) {
        try {
            if (currentLogLevel > outputLevel)
                return "";
            if (s == null)
                return "null";
            if (s.getQName() == null)
                return "UNNAMED," + Integer.toHexString(s.hashCode());
            return s.getQName() + "," + Integer.toHexString(s.hashCode());
        } catch (Exception e) {
            exceptionInTrace(e);
        }
        return "";
    }

    public static String brief(Port p) {
      return (!ON)?"":brief(LOG_LEVEL_DEBUG, p);
    }

    public static String brief(int outputLevel, Port p) {
        try {
            if (currentLogLevel > outputLevel)
                return "";
            if (p == null)
                return "null";
            if (p.getName() == null)
                return "UNNAMED," + Integer.toHexString(p.hashCode());
            return p.getName() + "," + Integer.toHexString(p.hashCode());
        } catch (Exception e) {
            exceptionInTrace(e);
        }
        return "";
    }

    public static String brief(BindingOperation bo) {
      return (!ON)?"":brief(LOG_LEVEL_DEBUG, bo);
    }

    public static String brief(int outputLevel, BindingOperation bo) {
        try {
            if (currentLogLevel > outputLevel)
                return "";
            if (bo == null)
                return "null";
            if (bo.getName() == null)
                return "UNNAMED," + Integer.toHexString(bo.hashCode());
            return bo.getName() + "," + Integer.toHexString(bo.hashCode());
        } catch (Exception e) {
            exceptionInTrace(e);
        }
        return "";
    }

    public static String brief(Operation o) {
      return (!ON)?"":brief(LOG_LEVEL_DEBUG, o);
    }

    public static String brief(int outputLevel, Operation o) {
        try {
            if (currentLogLevel > outputLevel)
                return "";
            if (o == null)
                return "null";
            if (o.getName() == null)
                return "UNNAMED," + Integer.toHexString(o.hashCode());
            return o.getName() + "," + Integer.toHexString(o.hashCode());
        } catch (Exception e) {
            exceptionInTrace(e);
        }
        return "";
    }

    public static String brief(String name, Collection coll) {
      return (!ON)?"":brief(LOG_LEVEL_DEBUG, name, coll);
    }

    public static String brief(int outputLevel, String name, Collection coll) {
      try {
	if (currentLogLevel > outputLevel)
	  return "";
	if (coll == null)
	  return name + ":null";
	if (coll.size() == 0)
	  return name + ":size(0)";
	return brief(outputLevel,name,coll.toArray());
      } catch (Exception e) {
	exceptionInTrace(e);
      }
      return "";
    }

    public static String brief(String name, Object[] objs) {
      return (!ON)?"":brief(LOG_LEVEL_DEBUG, name, objs);
    }

    public static String brief(int outputLevel, String name, Object[] objs) {
        try {
            if (currentLogLevel > outputLevel)
                return "";
            if (objs == null)
                return name + ":null";
            if (objs.length == 0)
                return name + ":size(0)";
            int i;
            StringBuffer buff = new StringBuffer("");
            for (i = 0; i < objs.length; i++) {
            	String s = (objs[i] == null) ? "null" : objs[i].toString();
                buff.append(" ");
                buff.append(name);
                buff.append("[");
                buff.append(i);
                buff.append("]:");
                buff.append(s);
            }
            return buff.toString();
        } catch (Exception e) {
            exceptionInTrace(e);
        }
        return "";
    }

    /**
     * Emulates map.toString() except does a checkWsdl() on all 
     * the elements of the map.
     */
    public static String brief(Map map) {
      return (!ON)?"":brief(LOG_LEVEL_DEBUG, map);
    }

    public static String brief(int outputLevel, Map map) {
        try {
            if (currentLogLevel > outputLevel)
                return "";
            if (map == null)
                return "<null>";
            if (map.isEmpty())
                return "size(0)";

            StringBuffer result = new StringBuffer("{");
            boolean first = true;
            Iterator it = map.keySet().iterator();

            while (it.hasNext()) {
                String n = (String) it.next();
                Object value = map.get(n);
                if (value == null)
                    value = "<null>";
                Object v2 = checkWsdl(value);

                if (!first)
                    result.append(", ");
                first = false;
                result.append(n);
                result.append("=");
                result.append(v2.toString());
            }

            result.append("}");
            return result.toString();
        } catch (Exception e) {
            exceptionInTrace(e);
        }
        return "";
    }
    
    private static void checkWsdl(Object[] parms) {
        if (parms == null)
            return;
        for (int i = 0; i < parms.length; i++)
            parms[i] = checkWsdl(parms[i]);
    }

    private static Object checkWsdl(Object o) {
        String str = null;
        boolean found = false;

        if (o instanceof Definition) {
            found = true;
            Definition d = (Definition) o;
            if (d == null)
                str = "definition(NULL)";
            else if (d.getQName() == null)
                str = "definition(UNNAMED";
            else
                str = "definition(" + d.getQName();
        } else if (o instanceof Service) {
            found = true;
            Service s = (Service) o;
            if (s == null)
                str = "service(NULL)";
            else if (s.getQName() == null)
                str = "service(UNNAMED";
            else
                str = "service(" + s.getQName();
        } else if (o instanceof Port) {
            found = true;
            Port p = (Port) o;
            if (p == null)
                str = "port(NULL)";
            else if (p.getName() == null)
                str = "port(UNNAMED";
            else
                str = "port(" + p.getName();
        } else if (o instanceof PortType) {
            found = true;
            PortType pt = (PortType) o;
            if (pt == null)
                str = "portType(NULL)";
            else if (pt.getQName() == null)
                str = "portType(UNNAMED";
            else
                str = "portType(" + pt.getQName();
        } else if (o instanceof Operation) {
            found = true;
            Operation op = (Operation) o;
            if (op == null)
                str = "operation(NULL)";
            else if (op.getName() == null)
                str = "operation(UNNAMED";
            else
                str = "operation(" + op.getName();
        } else if (o instanceof Binding) {
            found = true;
            Binding b = (Binding) o;
            if (b == null)
                str = "binding(NULL)";
            else if (b.getQName() == null)
                str = "binding(UNNAMED";
            else
                str = "binding(" + b.getQName();
        } else if (o instanceof BindingOperation) {
            found = true;
            BindingOperation bo = (BindingOperation) o;
            if (bo == null)
                str = "bindingOperation(NULL)";
            else if (bo.getName() == null)
                str = "bindingOperation(UNNAMED";
            else
                str = "bindingOperation(" + bo.getName();
        } else if (o instanceof BindingInput) {
            found = true;
            BindingInput bi = (BindingInput) o;
            if (bi == null)
                str = "bindingInput(NULL)";
            else if (bi.getName() == null)
                str = "bindingInput(UNNAMED";
            else
                str = "bindingInput(" + bi.getName();
        } else if (o instanceof BindingOutput) {
            found = true;
            BindingOutput bo = (BindingOutput) o;
            if (bo == null)
                str = "bindingOutput(NULL)";
            else if (bo.getName() == null)
                str = "bindingOutput(UNNAMED";
            else
                str = "bindingOutput(" + bo.getName();
        } else if (o instanceof Map) {
            Map map = (Map) o;
            HashMap newMap = null;
            Iterator it = map.keySet().iterator();
            while (it.hasNext()) {
                Object key = it.next();
                if (key == null)
                    continue;
                Object value = map.get(key);
                if (value == null)
                    continue;
                Object alt = checkWsdl(value);
                if (!value.equals(alt)) {
                    if (newMap == null)
                        newMap = new HashMap(map);
                    newMap.put(key, alt);
                }
            }
            if (newMap != null)
                return newMap;
            return map;
        }

        if (!found)
            return o;
        if (o != null)
            str = str + "," + Integer.toHexString(o.hashCode()) + ")";
        return str;
    }

    /**
     * This method calculates the name of the WSIF method that is
     * being traced. This could be passed as a parameter to the Trc
     * call, but making Trc simpler to invoke encourages developers 
     * to add trace to the code and avoids confusing the code with 
     * lengthy trace statements. We are able to calculate the method
     * name here by parsing a stack trace. This is slow but only happens
     * when trace is on. Hopefully this will be a fraction of the 
     * time it takes to write the trace to disk.
     */
    private static void appendMethodName(StringBuffer buff) {
        Exception e = new Exception();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stack = sw.getBuffer().toString();
        
        if (traceTrace)
            traceLog.debug("TRACE stack=" + stack);

        // The next while loop tries to find the method that called
        // Trc. The line with the method name will be after the last 
        // call to Trc. 
        StringTokenizer st1 =
            new StringTokenizer(
                stack,
                System.getProperty("line.separator", "\n"));
        boolean foundWsifLogging = false;
        String tok1 = null;
        while (st1.hasMoreTokens()) {
            tok1 = st1.nextToken();

            if (tok1.indexOf(wsifPackageName + ".logging") != -1) {
                foundWsifLogging = true;
                continue;
            }

            if (foundWsifLogging)
                break;
        }
        if (traceTrace)
            traceLog.debug("TRACE token=" + tok1);

        // Now find the first word which contains a (. This should be 
        // prefixed by the method name. If there isn't a (, which 
        // is unlikely, use the whole line.
        StringTokenizer st2 = new StringTokenizer(tok1);
        String tok2 = null;
        while (st2.hasMoreTokens()) {
            tok2 = st2.nextToken();
            if (tok2.indexOf("(") != -1)
                break;
        }
        if (tok2.indexOf("(") == -1)
            tok2 = tok1;

        // Indent the method name by the number of WSIF calls 
        // higher up the stack. This improves readability. There
        // were various other ways of calculating the indentation
        // but this seemed the most reliable (and simplest).
        buff.append(" ");
        while (st1.hasMoreTokens()) {
            if (st1.nextToken().indexOf(wsifPackageName) != -1)
                buff.append(" ");
        }

        // Strip off the (... parameters...). I expect there will
        // always be a ( in the token, but this code copes even if 
        // there isn't.
        int idx = tok2.indexOf("(");
        if (idx != -1)
            tok2 = tok2.substring(0, idx);
        if (traceTrace)
            traceLog.debug("TRACE token=" + tok2);

        // Now strip off the WSIF package name off the front of 
        // the class name. All WSIF class names are unique, so 
        // the package name just takes up more space in the trace
        // and provides no added value. If Trc was not called from
        // WSIF (unlikely) then output the whole 
        // packagename.classname.methodname.
        String result = null;
        if (tok2.startsWith(wsifPackageName)) {
            StringTokenizer st3 = new StringTokenizer(tok2, ".");
            String previous = null;
            while (st3.hasMoreTokens()) {
                previous = result;
                result = st3.nextToken();
            }
            if (previous != null)
                result = previous + "." + result;
        } else
            result = tok2;

        if (traceTrace)
            traceLog.debug("TRACE appending result=" + result);
        buff.append(result);
    }
        
    private static void traceIt(
        Object that,
        String type,
        boolean expandWsdl,
        Object[] parms) {

        boolean isEvent = false;
        if (EVENT.equals(type))
            isEvent = true;

        if (!expandWsdl)
            checkWsdl(parms);

        StringBuffer sb = new StringBuffer(type);
        if (isEvent) sb.append(" ");
        appendMethodName(sb);

        if (that != null) {
            sb.append("<");
            sb.append(Integer.toHexString(that.hashCode()));
            sb.append(">");
        }

        if (isEvent)
            sb.append(" ");
        else
            sb.append("(");

        if (parms != null)
            for (int i = 0; i < parms.length; i++) {
                if (i != 0 && !isEvent)
                    sb.append(", ");
                sb.append(parms[i] == null ? "<null>" : parms[i].toString());
            }
        if (!isEvent)
            sb.append(")");

        log.debug(sb);
    }
}

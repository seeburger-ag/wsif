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

package org.apache.wsif.util.jms;

import java.util.Hashtable;

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import org.apache.wsif.WSIFException;
import org.apache.wsif.logging.Trc;

/**
 * Finds JMS objects by looking them up in JNDI.
 * @author Mark Whitlock <whitlock@apache.org>
 */
public class WSIFJMSFinderForJndi extends WSIFJMSFinder {

    private InitialDirContext namedJndiContext = null;
    private InitialDirContext containersJndiContext = null;
    private QueueConnectionFactory factory;
    private Destination initialDestination = null;
    private String style;
    private String portName;

    /**
     * Package private constructor.
     * @param jmsVendorURL uniquely identifies the JMS implementation. Unused at present.
     * @param initialContextFactory used to get JNDI's InitialContext in a non-managed environment
     * @param jndiProviderURL of the JNDI repository
     * @param style is either queue or topic
     * @param jndiConnectionFactory is the JNDI name of the qcf
     * @param jndiDestinationName is the JNDI name of the initial queue.
     * @param jmsproviderDestinationName is the JMS implementation's name of the initial queue.
     */
    public WSIFJMSFinderForJndi(
        String jmsVendorURL,
        String initialContextFactory,
        String jndiProviderURL,
        String style,
        String jndiConnectionFactory,
        String jndiDestinationName,
        String portName)
        throws WSIFException {
        Trc.entry(
            this,
            jmsVendorURL,
            initialContextFactory,
            jndiProviderURL,
            style,
            jndiConnectionFactory,
            jndiDestinationName,
            portName);

        if (!allStyles.contains(style))
            throw new WSIFException("Style must either be queue or topic");
        this.style = style;
        if (portName == null)
            portName = "<null>";
        this.portName = portName;

        if (((initialContextFactory == null) && (jndiProviderURL != null))
            || ((initialContextFactory != null) && (jndiProviderURL == null)))
            throw new WSIFException(
                "Either both initialContextFactory and jndiProviderURL "
                    + "must be specified or neither of them must be specified. Port="
                    + portName);

        /*
         * Go find the container's JNDI and the JNDI specified in the WSDL.
         */
        if (initialContextFactory != null && jndiProviderURL != null) {
            Hashtable environment = new Hashtable();
            environment.put(
                Context.INITIAL_CONTEXT_FACTORY,
                initialContextFactory);
            environment.put(Context.PROVIDER_URL, jndiProviderURL);

            try {
                namedJndiContext = new InitialDirContext(environment);
            } catch (NamingException ne) {
                Trc.exception(ne);
                throw new WSIFException(
                    "WSIFJMSFinderForJndi caught '"
                        + ne
                        + "'. InitialContextFactory was '"
                        + initialContextFactory
                        + "' ProviderUrl was '"
                        + jndiProviderURL
                        + "'. Port="
                        + portName);
            }
        }

        try {
            containersJndiContext = new InitialDirContext();
        } catch (NamingException ne) {
            Trc.exception(ne);
            if (initialContextFactory == null && jndiProviderURL == null)
                throw new WSIFException(
                    "WSIFJMSFinderForJndi caught '"
                        + ne
                        + "' using the default JNDI repository. Port="
                        + portName);
        }

        if (STYLE_TOPIC.equals(style))
            throw new WSIFException("Topics not implemented. Port=" + portName);
        else if (!STYLE_QUEUE.equals(style))
            throw new WSIFException(
                "jms:address must either be a queue or a topic not a '"
                    + (style == null ? "null" : style)
                    + "'. Port="
                    + portName);

        /*
         * Go find the queue connection factory.
         */
        if (jndiConnectionFactory == null)
            throw new WSIFException(
                "jndiConnectionFactory must be specified in port " + portName);
        try {
            factory = (QueueConnectionFactory) lookup(jndiConnectionFactory);
            if (factory == null)
                throw new WSIFException(
                    "WSIFJMSFinderForJndi was not able to lookup the ConnectionFactory "
                        + jndiConnectionFactory
                        + " in JNDI. Port="
                        + portName);
        } catch (ClassCastException cce) {
            Trc.exception(cce);
            throw new WSIFException(
                "WSIFJMSFinderForJndi caught ClassCastException. The ConnectionFactory "
                    + jndiConnectionFactory
                    + " in JNDI was not defined to be a connection factory. Port="
                    + portName
                    + " "
                    + cce);
        } catch (NamingException ne) {
            Trc.exception(ne);
            throw new WSIFException(
                "WSIFJMSFinderForJndi caught NamingException. The ConnectionFactory "
                    + jndiConnectionFactory
                    + " in JNDI was not defined to be a connection factory. Port="
                    + portName
                    + " "
                    + ne);
        }

        /*
         * Go find the initial destination.
         */
        if (jndiDestinationName != null)
            try {
                initialDestination = (Destination) lookup(jndiDestinationName);
                if (initialDestination == null)
                    throw new WSIFException(
                        "WSIFJMSFinderForJndi was not able to lookup the Destination "
                            + jndiDestinationName
                            + " in JNDI. Port="
                            + portName);
            } catch (ClassCastException cce) {
                Trc.exception(cce);
                throw new WSIFException(
                    "WSIFJMSFinderForJndi caught ClassCastException. The Destination "
                        + jndiDestinationName
                        + " in JNDI was not defined to be a destination. Port="
                        + portName
                        + " "
                        + cce);
            } catch (NamingException cce) {
                Trc.exception(cce);
                throw new WSIFException(
                    "WSIFJMSFinderForJndi caught NamingException. The Destination "
                        + jndiDestinationName
                        + " in JNDI was not defined to be a destination. Port="
                        + portName
                        + " "
                        + cce);
            }
        if (Trc.ON)
            Trc.exit(deep());
    }

    public QueueConnectionFactory getFactory() {
        Trc.entry(this);
        Trc.exit(factory);
        return factory;
    }

    public Destination getInitialDestination() {
        Trc.entry(this);
        Trc.exit(initialDestination);
        return initialDestination;
    }

    String getStyle() {
        Trc.entry(this);
        Trc.exit(style);
        return style;
    }

    Queue findQueue(String name) throws WSIFException {
        Trc.entry(this, name);
        Queue q = null;
        try {
            q = (Queue) lookup(name);
            if (q == null)
                throw new WSIFException(
                    "WSIFJMSFinderForJndi was not able to lookup the Destination "
                        + name
                        + " in JNDI.Port="
                        + portName);
        } catch (ClassCastException cce) {
            Trc.exception(cce);
            throw new WSIFException(
                "WSIFJMSFinderForJndi caught ClassCastException. The Queue "
                    + name
                    + " in JNDI was not defined to be a queue. Port="
                    + portName
                    + " "
                    + cce);
        } catch (NamingException ne) {
            Trc.exception(ne);
            throw new WSIFException(
                "WSIFJMSFinderForJndi caught NamingException. The Queue "
                    + name
                    + " in JNDI was not defined to be a queue. Port="
                    + portName
                    + " "
                    + ne);
        }
        Trc.exit(q);
        return q;
    }

    /**
     * There is at least one and at most two JNDI databases to look 
     * objects up in :- the container's default JNDI database and the 
     * one specified in the WSDL. This code looks objects up in the
     * container's JNDI first (if running in a container), and then 
     * looks the object up in the JNDI pointed at by the WSDL (assuming
     * there is a JNDI mentioned in the WSDL). This allows a client
     * administrator to override the JNDI specified in the WSDL. If 
     * both fail, then the exception from the WSDL's JNDI is returned.
     */
    private Object lookupJNDIName(String name) throws NamingException {
        Trc.entry(name);
        Object result = null;
        if (containersJndiContext != null) {
            try {
                result = containersJndiContext.lookup(name);
            } catch (NamingException ne) {
                Trc.exception(ne);
                if (namedJndiContext != null)
                    result = namedJndiContext.lookup(name);
                else
                    throw ne;
            }
        } else
            result = namedJndiContext.lookup(name);
        Trc.exit(result);
        return result;
    }

    /**
     * The argument name is prefixed with "java:comp/env/" context
     * prior to the JNDI lookup.  If an exception is thrown, the
     * JNDI lookup is done on the argument name without the prefix.
     * If the lookup fails, the error based off the lookup of the 
     * argument is returned.
     */
    private Object lookup(String name) throws NamingException {
        Trc.entry(name);

        Object result = null;
        try {
            result = lookupJNDIName("java:comp/env/" + name);
        } catch (NamingException ignored) {
            Trc.exception(ignored);
            result = lookupJNDIName(name);
        }
        Trc.exit(result);
        return result;
    }

    public String deep() {
        StringBuffer buff = new StringBuffer();
        try {
            buff.append(this.toString() + "\n");
            buff.append("containersJndiContext: " + containersJndiContext);
            buff.append(" namedJndiContext: " + namedJndiContext);
            buff.append(" factory: " + factory);
            buff.append(" initialDestination: " + initialDestination);
            buff.append(" style: " + style);
        } catch (Exception e) {
            Trc.exceptionInTrace(e);
        }
        return buff.toString();
    }
}
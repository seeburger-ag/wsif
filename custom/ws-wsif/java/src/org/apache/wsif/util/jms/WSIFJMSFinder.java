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

import java.util.ArrayList;
import java.util.Arrays;

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import org.apache.wsif.WSIFException;
import org.apache.wsif.logging.Trc;
import org.apache.wsif.wsdl.extensions.jms.JMSAddress;

/**
 * Finds JMS objects. Classes that extend WSIFJMSFinder find the factory and queues either
 * by going directly to an implementation of JMS or by looking them up in JNDI, or by some
 * other means. A WSIFJMSDestination can then be constructed from this finder.
 * 
 * @author Mark Whitlock <whitlock@apache.org>
 */
public abstract class WSIFJMSFinder {
    public static final String STYLE_QUEUE = "queue";
    public static final String STYLE_TOPIC = "topic";

    protected static final ArrayList allStyles =
        new ArrayList(Arrays.asList(new Object[] { STYLE_QUEUE, STYLE_TOPIC }));

    private static final String MQ_URL_PREFIX = "mq://";

    abstract public QueueConnectionFactory getFactory();
    abstract public Destination getInitialDestination();
    abstract Queue findQueue(String name) throws WSIFException;
    abstract String getStyle();

    public static WSIFJMSFinder newFinder(JMSAddress ja, String portName) throws WSIFException {
        Trc.entry(null, ja);
        boolean jndiSpecified =
            ja.getInitCxtFact() != null
                || ja.getJndiProvURL() != null
                || ja.getDestStyle() != null
                || ja.getJndiConnFactName() != null
                || ja.getJndiDestName() != null
                || ja.getJmsProvDestName() != null;
        String implSpecURI = ja.getJmsImplSpecURI();

        if (jndiSpecified && implSpecURI != null)
            throw new WSIFException(
                "Cannot specify both JNDI attributes and "
                    + "jmsImplementationSpecificURL in the jms:address in port "
                    + portName);

        if (!jndiSpecified && implSpecURI == null)
            throw new WSIFException(
                "Must specify either JNDI attributes or "
                    + "jmsImplementationSpecificURL in the jms:address in port "
                    + portName);

        WSIFJMSFinder finder;
        if (jndiSpecified)
            finder =
                new WSIFJMSFinderForJndi(
                    ja.getJmsVendorURI(),
                    ja.getInitCxtFact(),
                    ja.getJndiProvURL(),
                    ja.getDestStyle(),
                    ja.getJndiConnFactName(),
                    ja.getJndiDestName(),
                    portName);
        else {
            if (implSpecURI.startsWith(MQ_URL_PREFIX))
                finder = new WSIFJMSFinderForMq(ja.getJmsVendorURI(), implSpecURI);
            else
                throw new WSIFException(
                    "No jms implementation found for jmsImplementationSpecificURI '"
                        + implSpecURI
                        + "' for port "
                        + portName);
        }
        Trc.exit(finder);
        return finder;
    }
}
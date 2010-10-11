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

package util;

import org.apache.wsif.util.WSIFProperties;
import org.apache.wsif.util.jms.JMS2HTTPBridge;
import org.apache.wsif.util.jms.JMS2HTTPBridgeDestination;

/**
 * BridgeThread is a thread used by TestUtilities.startListeners()
 * that runs the JMS2HTTPBridge.
 * 
 * @author Mark Whitlock
 */

class BridgeThread extends Thread {
    private String queueName;
    private String serviceName;

    public BridgeThread(String name) {
        this.queueName = name;
        this.serviceName = name;
    }

    public BridgeThread(String queueName, String serviceName) {
        this.queueName = queueName;
        this.serviceName = serviceName;
    }

    public void run() {
        try {
            if (TestUtilities.isJmsVerbose())
                System.out.println(
                    "Starting "
                        + serviceName
                        + "("
                        + queueName
                        + ") JMS2HTTPBridge");

            String pattern =
                TestUtilities.getWsifProperty(
                    "wsif.jms2httpbridge.jndidestinationnamepattern");
            String queue =
                pattern.substring(0, pattern.indexOf('%'))
                    + queueName
                    + pattern.substring(pattern.indexOf('%') + 1);

            String url =
                TestUtilities.getWsifProperty("wsif.jms2httpbridge.httpurl");
            if (TestUtilities.getSoapServer().equals("axis")) {
                if (!url.endsWith("/"))
                    url = url.concat("/");
                url = url.concat(serviceName);
            }
                        
            // Sync timeout of 10000 milliseconds is the default.
            int syncTimeout = 10000;
            String st = WSIFProperties.getProperty("wsif.syncrequest.timeout");
            if (st != null)
                syncTimeout = new Integer(st).intValue();

            JMS2HTTPBridge j2h =
                new JMS2HTTPBridge(
                    TestUtilities.getWsifProperty(
                        "wsif.jms2httpbridge.initialcontextfactory"),
                    TestUtilities.getWsifProperty(
                        "wsif.jms2httpbridge.jndiproviderurl"),
                    TestUtilities.getWsifProperty(
                        "wsif.jms2httpbridge.jndiconnectionfactoryname"),
                    queue,
                    url,
                    JMS2HTTPBridgeDestination.COLDSTART,
                    syncTimeout,
                    TestUtilities.isJmsVerbose());
            j2h.listen();
        } catch (Exception e) {
            System.err.println("Caught JMS2HTTPBridge exception " + e);
        }
    }
}


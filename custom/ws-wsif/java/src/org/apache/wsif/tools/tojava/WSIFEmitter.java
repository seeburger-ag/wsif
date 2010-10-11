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

package org.apache.wsif.tools.tojava;

import org.apache.axis.wsdl.toJava.Emitter;

/**
 * WSIFEmitter
 * 
 * WSIF version of the AXIS Emitter that uses the WSIF
 * version of JavaGeneratorFactory.
 * 
 * @author <a href="mailto:ant.elder@uk.ibm.com">Ant Elder</a>
 */
public class WSIFEmitter extends Emitter {

    protected boolean testcaseGenDII = false;
    protected boolean testcaseGenStubs = false;

    /**
     * Constructor
     */
    public WSIFEmitter() {
        WSIFJavaGeneratorFactory factory = new WSIFJavaGeneratorFactory();
        setFactory(factory);
        factory.setEmitter(this);
    }

    /**
     * Returns the testcaseGenDII.
     * @return boolean
     */
    public boolean isTestcaseGenDII() {
        return testcaseGenDII;
    }

    /**
     * Returns the testcaseGenStubs.
     * @return boolean
     */
    public boolean isTestcaseGenStubs() {
        return testcaseGenStubs;
    }

    /**
     * Sets the testcaseGenDII.
     * @param testcaseGenDII The testcaseGenDII to set
     */
    public void setTestcaseGenDII(boolean testcaseGenDII) {
        this.testcaseGenDII = testcaseGenDII;
    }

    /**
     * Sets the testcaseGenStubs.
     * @param testcaseGenStubs The testcaseGenStubs to set
     */
    public void setTestcaseGenStubs(boolean testcaseGenStubs) {
        this.testcaseGenStubs = testcaseGenStubs;
    }

}

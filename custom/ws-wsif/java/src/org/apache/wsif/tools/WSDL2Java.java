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

package org.apache.wsif.tools;

import org.apache.axis.utils.CLOption;
import org.apache.axis.utils.CLOptionDescriptor;
import org.apache.axis.wsdl.gen.Parser;
import org.apache.axis.wsdl.gen.WSDL2;
import org.apache.wsif.tools.tojava.WSIFEmitter;

/**
 * WSIF WSDL2Java Utility program
 * 
 * Generates Java source for the service endpoint interface,
 * any complex types, and a WSIF testcase.
 * 
 * @author <a href="mailto:ant.elder@uk.ibm.com">Ant Elder</a>
 */
public class WSDL2Java extends org.apache.axis.wsdl.WSDL2Java {

    protected static final int TESTCASE_OPT = 't';
    protected static final CLOptionDescriptor[] wsifCLOptions =
        new CLOptionDescriptor[] {
             new CLOptionDescriptor(
                "testCase <stubs | DII | both | none>",
                CLOptionDescriptor.ARGUMENT_OPTIONAL,
                TESTCASE_OPT,
                "emit a JUnit testcase class for the web service."
                    + " Optionally specify if the testcase will use the"
                    + " WSIF DII, stubs, or both. The default is to use stubs.")};

    /**
     * Constructor
     */
    public WSDL2Java() {
        removeOption("testCase");
        removeOption("server-side");
        removeOption("skeletonDeploy");
        removeOption("deployScope");
        addOptions(wsifCLOptions);
    }

    protected void parseOption(CLOption option) {
        switch (option.getId()) {
            case TESTCASE_OPT :
                if (parseTestcaseOption(option)) {
                    break;
                } else {
                	System.err.println("invalid testcase option: " + option);
                	printUsage();
                }
            default :
                super.parseOption(option);
        }
    }

    protected boolean parseTestcaseOption(CLOption option) {
        boolean valid = true;
        String arg = option.getArgument();
        ((WSIFEmitter) parser).setTestCaseWanted(true);

        if ("DII".equalsIgnoreCase(arg)) {
            ((WSIFEmitter) parser).setTestcaseGenDII(true);
            ((WSIFEmitter) parser).setTestcaseGenStubs(false);
        } else if ("stubs".equalsIgnoreCase(arg)) {
            ((WSIFEmitter) parser).setTestcaseGenStubs(true);
            ((WSIFEmitter) parser).setTestcaseGenDII(false);
        } else if ("both".equalsIgnoreCase(arg)) {
            ((WSIFEmitter) parser).setTestcaseGenStubs(true);
            ((WSIFEmitter) parser).setTestcaseGenDII(true);
        } else if ("none".equalsIgnoreCase(arg)) {
            ((WSIFEmitter) parser).setTestCaseWanted(false);
            ((WSIFEmitter) parser).setTestcaseGenStubs(true);
            ((WSIFEmitter) parser).setTestcaseGenDII(false);
        } else if (arg == null || arg.length() < 1) {
            ((WSIFEmitter) parser).setTestcaseGenStubs(true);
            ((WSIFEmitter) parser).setTestcaseGenDII(false);
        } else {
            valid = false;
        }

        return valid;
    }

    /**
     * Override createParser to use the WSIF version of Emitter.
     */
    protected Parser createParser() {
        return new WSIFEmitter();
    }

    /**
     * Main
     */
    public static void main(String args[]) {
        WSDL2Java w2j = new WSDL2Java();
        w2j.run(args);
    }

}

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

import org.apache.axis.utils.CLUtil;
import org.apache.axis.utils.Messages;

/**
 * WSIF Java2WSDL Utility program
 * 
 * Generates WSDL source files from a Java class.
 * For now this changes nothing that the AXIS Java2WSDL does,
 * it just lets people use org.apache.wsif.tools.Java2WSDL 
 * 
 * @author <a href="mailto:ant.elder@uk.ibm.com">Ant Elder</a>
 */
public class Java2WSDL extends org.apache.axis.wsdl.Java2WSDL {

    /**
     * Constructor
     */
    public Java2WSDL() {
    	super();
    }

//    /**
//     * Instantiate an Emitter          
//     */
//    protected Emitter createEmitter() {
//        return new WSIFEmitter();
//    } 

    /**
     * printUsage
     * print usage information and quit.
     * TODO: See bugzilla 18067 to not use hard coded class name
     */
    protected void printUsage() {
        String lSep = System.getProperty("line.separator");
        StringBuffer msg = new StringBuffer();
        msg.append("Java2WSDL " 
                   + Messages.getMessage("j2wemitter00")).append(lSep);
        msg.append(Messages.getMessage("j2wusage00", 
//                   "java " + Java2WSDL.class.getName() + " [options] class-of-portType")).append(lSep);
                   "java " + getClass().getName() + " [options] class-of-portType")).append(lSep);
        msg.append(Messages.getMessage("j2woptions00")).append(lSep);
        msg.append(CLUtil.describeOptions(options).toString());
        msg.append(Messages.getMessage("j2wdetails00")).append(lSep);
        System.out.println(msg.toString());
    }

    /**
     * Main
     * 
     * For arg details see:
     * http://cvs.apache.org/viewcvs.cgi/~checkout~/xml-axis/java/docs/reference.html#Java2WSDL
     */
    public static void main(String args[]) {
        Java2WSDL java2wsdl = new Java2WSDL();
        System.exit(java2wsdl.run(args));
    }

}

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;

import org.apache.axis.utils.CLArgsParser;
import org.apache.axis.utils.CLOption;
import org.apache.axis.utils.CLOptionDescriptor;
import org.apache.axis.utils.CLUtil;
import org.apache.wsif.WSIFException;
import org.apache.wsif.tools.wsdl.BindingGenerator;
import org.apache.wsif.tools.wsdl.EJBBindingGenerator;
import org.apache.wsif.tools.wsdl.JavaBindingGenerator;
import org.apache.wsif.tools.wsdl.SOAPJMSBindingGenerator;
import org.apache.wsif.util.WSIFUtils;

/**
 * WSIF WSDL2WSDL Utility program
 * 
 * Add new WSDL bindings to a WSDL file
 * 
 * TODO: Support bindings for all WSIF providers, ideally
 *       by designing a way for each provider to add its
 *       own binding.
 * 
 * Based on the Apache AXIS Java2WSDL class by Ravi Kumar and Rich Scheuerle
 * 
 * @author <a href="mailto:ant.elder@uk.ibm.com">Ant Elder</a>
 */
public class WSDL2WSDL {

    // input WSDL to read
    protected String wsdlURI;

    // new WSDL file to be written
    protected String outputWSDLName;

    // where to write the output WSDL
    protected String outputDirectory;

    // the binding types to be added to the WSDL
    protected ArrayList requestedBindings;

    // all the available generators of new WSDL Bindings
    protected ArrayList availableGenerators;

    // Define the short one-letter option identifiers.
    protected static final int HELP_OPT = 'h';
    protected static final int BINDINGS_OPT = 'b';
    protected static final int OUTPUT_DIR_OPT = 'o';
    protected static final int OUTPUT_WSDL_OPT = 'n';

    /**
     *  Define the understood options. Each CLOptionDescriptor contains:
     * - The "long" version of the option. Eg, "help" means that "--help" will
     * be recognised.
     * - The option flags, governing the option's argument(s).
     * - The "short" version of the option. Eg, 'h' means that "-h" will be
     * recognised.
     * - A description of the option for the usage message
     */
    protected CLOptionDescriptor[] options =
        new CLOptionDescriptor[] {
            new CLOptionDescriptor(
                "help",
                CLOptionDescriptor.ARGUMENT_DISALLOWED,
                HELP_OPT,
                "print usage information"),
            new CLOptionDescriptor(
                "outputWSDL",
                CLOptionDescriptor.ARGUMENT_REQUIRED,
                OUTPUT_WSDL_OPT,
                "the name of the new WSDL file"),
            new CLOptionDescriptor(
                "bindings",
                CLOptionDescriptor.ARGUMENT_REQUIRED,
                BINDINGS_OPT,
                "comma seperated list of binding types to add to the WSDL"),
            new CLOptionDescriptor(
                "output",
                CLOptionDescriptor.ARGUMENT_REQUIRED,
                OUTPUT_DIR_OPT,
                "output directory for the updated WSDL")};

    /**
     * WSDL2WSDL constructor
     */
    public WSDL2WSDL() {
        requestedBindings = new ArrayList();
    	availableGenerators = new ArrayList();
        addGenerators();
    }

    /**
     * The generators available by default
     */
    protected void addGenerators() {
    	availableGenerators.add(new JavaBindingGenerator());
    	availableGenerators.add(new EJBBindingGenerator());
    	availableGenerators.add(new SOAPJMSBindingGenerator());
    }

    /**
     * Add all the requested bindings to the WSDL
     */
    protected void addBindings(Definition def) {
    	for (Iterator i = availableGenerators.iterator(); i.hasNext(); ) {
    		BindingGenerator bg = (BindingGenerator) i.next();
    		if (requestedBindings.contains(bg)) {
    			bg.addBindings(def);
    		}
    	}
    }

    /**
     * Parse an option
     * @param option CLOption is the option
     */
    protected boolean parseOption(CLOption option) {
        String value;
        boolean status = true;

        switch (option.getId()) {

            case CLOption.TEXT_ARGUMENT :
                if (wsdlURI != null) {
                    System.out.println(
                        "duplicate WSDL specified:" + option.getArgument());
                    printUsage();
                    status = false; // error
                }
                wsdlURI = option.getArgument();
                break;

            case HELP_OPT :
                printUsage();
                status = false;
                break;

            case OUTPUT_WSDL_OPT :
                outputWSDLName = option.getArgument();
                break;

            case OUTPUT_DIR_OPT :
                outputDirectory = option.getArgument();
                break;

            case BINDINGS_OPT :
                if (!parseBindingOptions(option)) {
                    printUsage();
                    status = false; // error
                }
                break;

            default :
                break;
        }
        return status;
    }

    /**
     * Extracts the binding types to be added from the CLOption string
     */
    protected boolean parseBindingOptions(CLOption option) {
        boolean ok = true;

        String bindings = option.getArgument();
        StringTokenizer st = new StringTokenizer(bindings, ",");
        while (st.hasMoreTokens()) {
            String binding = st.nextToken();
            BindingGenerator bg = getGenerator(binding);
            if (bg == null) {
                System.err.println("invalid binding: " + binding);
                ok = false;
            } else if (requestedBindings.contains(bg)) {
                System.err.println("duplicate binding: " + binding);
                ok = false;
            } else {
                requestedBindings.add(bg);
            }
        }

        return ok;
    }

    protected BindingGenerator getGenerator(String name) {
    	BindingGenerator bg = null;
    	for (Iterator i = availableGenerators.iterator(); bg == null && i.hasNext(); ) {
            BindingGenerator x = (BindingGenerator) i.next();
            if (x.getBindingTypeName().equalsIgnoreCase(name)) {
            	bg = x;
            }
    	}
    	return bg;
    }

    /**
     * validateOptions
     * This method is invoked after the options are set to validate 
     * the option settings.
     */
    protected boolean validateOptions() {
        boolean ok = true;

        if (wsdlURI == null || wsdlURI.length() < 1) {
            System.err.println("must specify a WSDL URI");
            ok = false;
        }

        if (requestedBindings.size() < 1) {
            System.err.println("must specify atleast one binding to add");
            ok = false;
        }

        if (ok) {
            if (outputWSDLName == null || outputWSDLName.length() < 1) {
                int i = wsdlURI.lastIndexOf(File.separatorChar);
                if (i > -1) {
                    outputWSDLName = wsdlURI.substring(i + 1);
                } else {
                    outputWSDLName = wsdlURI;
                }
            }
        }

        return ok;
    }

    /**
     * run 
     * checks the command-line arguments and runs the tool.
     * @param args String[] command-line arguments.
     */
    protected int run(String[] args) {
        // Parse the arguments
        CLArgsParser argsParser = new CLArgsParser(args, options);

        // Print parser errors, if any
        if (null != argsParser.getErrorString()) {
            System.err.println(argsParser.getErrorString());
            printUsage();
            return (1);
        }

        // Get a list of parsed options
        List clOptions = argsParser.getArguments();

        // Parse the options and configure as appropriate.
        for (int i = 0; i < clOptions.size(); i++) {
            if (parseOption((CLOption) clOptions.get(i)) == false) {
                printUsage();
                return (1);
            }
        }

        // validate argument combinations
        if (validateOptions() == false) {
            printUsage();
            return (1);
        }

        // read in the existing WSDL file
        Definition def = null;
        try {
            def = readWSDL(wsdlURI);
        } catch (WSIFException e) {
            System.err.println("Exception reading WSDL file: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        // do the work of adding new bindings
        addBindings(def);

        // write out the new WSDL file
        try {
            writeWSDL(def, outputDirectory, outputWSDLName);
        } catch (WSIFException e) {
            System.err.println("Exception writing WSDL file: " + e);
            e.printStackTrace();
            System.exit(1);
        }

        // everything is good
        return (0);

    } // run

    /**
     * Returns the name of the class that called this method.
     *
     * <p>This replaces the former <code>FindThisClassName extends SecurityManager</code>
     * helper, which relied on <code>SecurityManager.getClassContext()[1]</code>.
     * {@link SecurityManager} has been deprecated for removal since JDK 17.
     * {@link StackWalker#getCallerClass()} returns exactly the same frame: the
     * caller of the method that invokes it.</p>
     */
    private static String findThisClassName() {
        return StackWalker
            .getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
            .getCallerClass()
            .getName();
    }

    /**
     * Reads a WSDL file into a WSDL4J Definition object
     */
    protected Definition readWSDL(String wsdlFileURI) throws WSIFException {
        System.out.println("reading WSDL: " + wsdlFileURI);
        Definition def = null;
        try {
            def = WSIFUtils.readWSDL(null, wsdlFileURI);
        } catch (WSDLException e) {
            throw new WSIFException(
                "exception reading WSDL '" + wsdlFileURI + "'",
                e);
        }
        return def;
    }

    /**
     * Writes a WSDL Definition object to a file
     */
    private void writeWSDL(Definition def, String dir, String fileName)
        throws WSIFException {

        try {
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLWriter wsdlWriter = factory.newWSDLWriter();

            File file;
            if (dir != null && dir.length() > 0) {
                file = new File(dir.replace('.', '/'));
                file.mkdirs();
                file = new File(file, fileName);
            } else {
                file = new File(fileName);
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
            	// ignored, writeWSDL call below will report any error 
            }

            System.out.println("writing WSDL: " + file);

            FileWriter out = new FileWriter(file);
            wsdlWriter.writeWSDL(def, out);

            out.close();
        } catch (Exception e) {
            throw new WSIFException("exception writing WSDL", e);
        }
    }

    /**
     * Prints help on how to use this
     */
    private void printUsage() {
        String thisClassName = findThisClassName();
        System.err.println(
            "Usage: java " + thisClassName + " <options>" + " inputWSDLURI");

        System.err.println("where posible options include:");
        System.err.println(CLUtil.describeOptions(options).toString());

        System.err.print("available binding types to add are: ");
        for (Iterator i = availableGenerators.iterator(); i.hasNext();) {
        	BindingGenerator bg = (BindingGenerator) i.next();
            System.err.print(bg.getBindingTypeName());
            if (i.hasNext()) {
                System.err.print(", ");
            }
        }

        System.exit(1);
    }

    /**
     * Front end for the WSIF WSDL utilities
     */
    public static void main(String[] args) {
        WSDL2WSDL wsdl2wsdl = new WSDL2WSDL();
        System.exit(wsdl2wsdl.run(args));
    }

}

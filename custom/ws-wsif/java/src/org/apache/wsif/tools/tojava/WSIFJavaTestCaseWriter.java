/*
 * Copyright 2001,2004 The Apache Software Foundation.
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
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.wsif.tools.tojava;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import org.apache.axis.utils.JavaUtils;
import org.apache.axis.wsdl.symbolTable.BindingEntry;
import org.apache.axis.wsdl.symbolTable.PortTypeEntry;
import org.apache.axis.wsdl.symbolTable.ServiceEntry;
import org.apache.axis.wsdl.symbolTable.SymbolTable;
import org.apache.axis.wsdl.symbolTable.Type;
import org.apache.axis.wsdl.toJava.Emitter;
import org.apache.axis.wsdl.toJava.JavaClassWriter;
import org.apache.axis.wsdl.toJava.Utils;
import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFException;
import org.apache.wsif.providers.ProviderUtils;
import org.apache.wsif.util.WSIFUtils;

/**
 * WSIFJavaTestCaseWriter
 * 
 * WSIF version of the AXIS JavaTestCaseWriter that
 * generates a WSIF JUnit testcase program source.
 * 
 * @author <a href="mailto:ant.elder@uk.ibm.com">Ant Elder</a>
 */
public class WSIFJavaTestCaseWriter extends JavaClassWriter {

    protected ServiceEntry sEntry;
    protected SymbolTable symbolTable;

    protected String indent;
    protected static final String TAB = "    ";

    /**
     * Constructor.
     */
    protected WSIFJavaTestCaseWriter(
        Emitter emitter,
        ServiceEntry sEntry,
        SymbolTable symbolTable) {

        super(emitter, sEntry.getName() + "TestCase", "testCase");
        this.sEntry = sEntry;
        this.symbolTable = symbolTable;

    }

    /**
     * Write the body of the TestCase file.
     * For each port in the service get the portType
     * and call writePortTypeTestCode.
     */
    protected void writeFileBody(PrintWriter pw) throws IOException {

        indent = TAB;

        writeTestConstructor(pw);
        skipLines(pw, 1);
        writeTestSuite(pw);

        ArrayList donePortTypes = new ArrayList();
        Service service = sEntry.getService();
        Map portMap = service.getPorts();

        HashMap diiPortTypeMethods = new HashMap();
        HashMap stubPortTypeMethods = new HashMap();

        /* for each port in the service, get the portType
         * used by its binding, and if hasn't already been
         * done for another port then write a portType method */
        for (Iterator i = portMap.values().iterator(); i.hasNext();) {
            Port p = (Port) i.next();

            Binding binding = p.getBinding();
            BindingEntry bEntry =
                symbolTable.getBindingEntry(binding.getQName());

            PortType portType = binding.getPortType();

            if (donePortTypes.contains(portType)) {
                continue;
            }
            donePortTypes.add(portType);

            String portName = p.getName();
            if (!JavaUtils.isJavaId(portName)) {
                portName = Utils.xmlNameToJavaClass(portName);
            }

            PortTypeEntry ptEntry =
                symbolTable.getPortTypeEntry(portType.getQName());

            if (((WSIFEmitter) emitter).isTestcaseGenDII()) {
                writeComment(pw, p.getDocumentationElement());
                String methodName =
                    writeDIITestPortType(
                        pw,
                        service,
                        portName,
                        portType,
                        ptEntry);
                diiPortTypeMethods.put(portType, methodName);
            }
            if (((WSIFEmitter) emitter).isTestcaseGenStubs()) {
                writeComment(pw, p.getDocumentationElement());
                String methodName =
                    writeStubTestPortType(
                        pw,
                        service,
                        portName,
                        portType,
                        ptEntry);
                stubPortTypeMethods.put(portType, methodName);
            }
        }

        /* for each port in the service write a JUnit test
         * method to call the portType method for the port. */
        for (Iterator i = portMap.values().iterator(); i.hasNext();) {
            Port p = (Port) i.next();
            Binding binding = p.getBinding();
            PortType portType = binding.getPortType();
            if (((WSIFEmitter) emitter).isTestcaseGenDII()) {
                writeTestMethod(
                    pw,
                    (String) diiPortTypeMethods.get(portType),
                    p.getName());
            }
            if (((WSIFEmitter) emitter).isTestcaseGenStubs()) {
                writeTestMethod(
                    pw,
                    (String) stubPortTypeMethods.get(portType),
                    p.getName());
            }
        }

        writeMainMethod(pw);

    } // writeFileBody

    /**
     * Write the header comments.
     * (Overriden to make WSIF specific)
     */
    protected void writeHeaderComments(PrintWriter pw) throws IOException {
        String localFile = getFileName();
        int lastSepChar = localFile.lastIndexOf(File.separatorChar);
        if (lastSepChar >= 0) {
            localFile = localFile.substring(lastSepChar + 1);
        }
        pw.println("/**");
        pw.println(" * " + localFile);
        pw.println(" *");
        pw.println(" * This file was auto-generated from WSDL");
        pw.println(" * by the Apache WSIF WSDL2Java emitter.");
        pw.println(" *");
        pw.println(" * Search for TODO: for code requiring completing");
        pw.println(" */");
        pw.println();
    } // writeHeaderComments

    /**
     * Write the package declaration statement.
     * (Overriden so it can add the imports as well)
     */
    protected void writePackage(PrintWriter pw) throws IOException {
        if (getPackage() != null) {
            pw.println("package " + getPackage() + ";");
            pw.println();
        }

        writeImports(pw);
        skipLines(pw, 1);

    } // writePackage

    /**
     * Write the import statements.
     */
    protected void writeImports(PrintWriter pw) throws IOException {
        if (((WSIFEmitter) emitter).isTestcaseGenStubs()) {
            pw.println("import java.rmi.RemoteException;");
        }
        if (((WSIFEmitter) emitter).isTestcaseGenDII()) {
            pw.println("import java.util.Iterator;");
        }
        skipLines(pw, 1);
        pw.println("import junit.framework.Test;");
        pw.println("import junit.framework.TestCase;");
        pw.println("import junit.framework.TestSuite;");
        pw.println("import junit.textui.TestRunner;");
        skipLines(pw, 1);
        if (((WSIFEmitter) emitter).isTestcaseGenDII()) {
            pw.println("import org.apache.wsif.WSIFConstants;");
            pw.println("import org.apache.wsif.WSIFException;");
            pw.println("import org.apache.wsif.WSIFMessage;");
            pw.println("import org.apache.wsif.WSIFOperation;");
            pw.println("import org.apache.wsif.WSIFPort;");
        }
        pw.println("import org.apache.wsif.WSIFService;");
        pw.println("import org.apache.wsif.WSIFServiceFactory;");
    }

    /**
     * Returns "extends junit.framework.TestCase ".
     */
    protected String getExtendsText() {
        return "extends TestCase ";
    } // getExtendsText

    /**
     * Write the constructor.
     */
    protected void writeTestConstructor(PrintWriter pw) throws IOException {

        addNewCodeLine(pw, "public " + getClassName() + "(String name) {");

        tabIn();
        addNewCodeLine(pw, "super(name);");

        tabOut();
        addNewCodeLine(pw, "}");
    }

    /**
     * Write the JUnit suite method.
     */
    protected void writeTestSuite(PrintWriter pw) throws IOException {

        addNewCodeLine(pw, "public static Test suite() {");
        tabIn();
        addNewCodeLine(
            pw,
            "return new TestSuite(" + getClassName() + ".class);");
        tabOut();
        addNewCodeLine(pw, "}");
    }

    /**
     * Write the testPortType method using stubs
     */
    protected String writeStubTestPortType(
        PrintWriter pw,
        Service service,
        String portName,
        PortType portType,
        PortTypeEntry ptEntry)
        throws IOException {

        QName serviceName = service.getQName();
        QName portTypeName = portType.getQName();

        String methodName = "doit" + "Stub" + portTypeName.getLocalPart();

        addNewCodeLine(
            pw,
            "protected void " + methodName + "(String portName) {");

        tabIn();

        addNewCodeLine(pw, "try {");

        tabIn();
        addNewCodeLine(
            pw,
            "WSIFServiceFactory factory = WSIFServiceFactory.newInstance();");

        skipLines(pw, 1);

        addNewCodeLine(pw, "WSIFService service =");
        tabIn();
        addNewCodeLine(pw, "factory.getService(");
        tabIn();
        addNewCodeLine(
            pw,
            "\"" + emitter.getWSDLURI().replace('\\', '/') + "\",");
        addNewCodeLine(pw, "\"" + serviceName.getNamespaceURI() + "\",");
        addNewCodeLine(pw, "\"" + serviceName.getLocalPart() + "\",");
        addNewCodeLine(pw, "\"" + portTypeName.getNamespaceURI() + "\",");
        addNewCodeLine(pw, "\"" + portTypeName.getLocalPart() + "\");");

        tabOut();
        tabOut();
        skipLines(pw, 1);

        writeMapPackages(pw);

        String seiName = getSEIName(portType);

        addNewCodeLine(pw, seiName);
        addCode(pw, " stub =");
        tabIn();
        addNewCodeLine(pw, "(");
        addCode(pw, seiName);
        addCode(pw, ") service.getStub(portName, ");
        addCode(pw, seiName);
        addCode(pw, ".class);");

        tabOut();

        skipLines(pw, 1);
        for (Iterator i = portType.getOperations().iterator(); i.hasNext();) {
            Operation op = (Operation) i.next();
            String opMethodName = makeExecuteOpMethodName(portType, op);
            addNewCodeLine(pw, opMethodName + "(portName, stub);");
        }

        skipLines(pw, 1);

        tabOut();
        addNewCodeLine(pw, "} catch (Exception ex) {");

        tabIn();
        addNewCodeLine(pw, "ex.printStackTrace();");
        addNewCodeLine(pw, "assertTrue(\"");
        addCode(pw, methodName);
        addCode(pw, " got exception: \" + ex.getLocalizedMessage(), false);");

        tabOut();
        addNewCodeLine(pw, "}");

        tabOut();
        addNewCodeLine(pw, "}");

        skipLines(pw, 1);

        for (Iterator i = portType.getOperations().iterator(); i.hasNext();) {
            Operation op = (Operation) i.next();
            writeStubOperationMethod(pw, seiName, portType, op);
        }

        return methodName;
    }

    /**
     * Write the executeOp method using stubs
     */
    protected void writeStubOperationMethod(
        PrintWriter pw,
        String seiName,
        PortType portType,
        Operation op) {

        addNewCodeLine(pw, "protected void ");

        String opMethodName = makeExecuteOpMethodName(portType, op);
        addCode(pw, opMethodName);

        addCode(pw, "(String portName, ");
        addCode(pw, seiName);
        addCode(pw, " stub) throws RemoteException {");

        skipLines(pw, 1);
        tabIn();

        ArrayList params = new ArrayList();
        Input in = op.getInput();
        if (in != null && in.getMessage() != null) {
            List parts = in.getMessage().getOrderedParts(null);
            unwrapIfWrapped(
                emitter.getCurrentDefinition(),
                parts,
                op.getName());

            if (parts.size() > 0) {
                addNewCodeLine(
                    pw,
                    "// TODO: initialize these parameters with required values");
            }

            int parameterCounter = 0;
            for (Iterator i = parts.iterator(); i.hasNext();) {
                Part p = (Part) i.next();
                QName type = ProviderUtils.getPartType(p);
                String className = Utils.getJavaLocalName(getClassName(type));
                String paramName = "p" + parameterCounter++;
                params.add(paramName);
                addNewCodeLine(
                    pw,
                    className
                        + " "
                        + paramName
                        + " = "
                        + getTypeInitialiser(type)
                        + ";");
            }
            skipLines(pw, 1);
        }

        String returnPartName = null;
        String returnClassName = "";
        Output out = op.getOutput();
        if (out != null
            && out.getMessage() != null
            && out.getMessage().getParts().size() > 0) {
            List parts = out.getMessage().getOrderedParts(null);
            unwrapIfWrapped(
                emitter.getCurrentDefinition(),
                parts,
                op.getName() + "Response");
            Part p = (Part) parts.getFirst();
            returnPartName = p.getName();
            if (JavaUtils.isJavaKeyword(returnPartName)) {
                returnPartName = "_" + returnPartName;
            }
            QName type = ProviderUtils.getPartType(p);
            returnClassName = Utils.getJavaLocalName(getClassName(type));
            addNewCodeLine(pw, returnClassName);
            addCode(pw, " ");
            addCode(pw, returnPartName);
            addCode(pw, " = stub.");
            addCode(pw, ProviderUtils.lowercaseFirst(op.getName()));
            addCode(pw, "(");
        } else {
            addNewCodeLine(pw, "stub.");
            addCode(pw, ProviderUtils.lowercaseFirst(op.getName()));
            addCode(pw, "(");
        }

        for (Iterator i = params.iterator(); i.hasNext();) {
            String paramName = (String) i.next();
            addCode(pw, paramName);
            if (i.hasNext()) {
                addCode(pw, ", ");
            }
        }

        addCode(pw, ");");
        skipLines(pw, 1);

        if (returnPartName != null) {
            addNewCodeLine(
                pw,
                "System.out.println(\" port \" + portName + \" operation '");
            addCode(pw, op.getName());
            addCode(pw, "' returned: \" + ");
            addCode(pw, returnPartName);
            addCode(pw, ");");

            skipLines(pw, 1);
            addNewCodeLine(pw, "// TODO: validate response parts here");
            if (Character.isLowerCase(returnClassName.charAt(0))) {
                addNewCodeLine(pw, "// assertTrue(\"operation ");
            } else {
                addNewCodeLine(pw, "assertTrue(\"operation ");
            }
            addCode(pw, op.getName());
            addCode(pw, " returned null!!\", ");
            addCode(pw, returnPartName);
            addCode(pw, " != null);");
        }

        tabOut();
        addNewCodeLine(pw, "}");
        skipLines(pw, 1);

    }

    /**
     * Write the testPortType method using the WSIF DII
     */
    protected String writeDIITestPortType(
        PrintWriter pw,
        Service service,
        String portName,
        PortType portType,
        PortTypeEntry ptEntry)
        throws IOException {

        QName serviceName = service.getQName();
        QName portTypeName = portType.getQName();

        String methodName = "doit" + "DII" + portTypeName.getLocalPart();

        skipLines(pw, 1);
        addNewCodeLine(pw, "protected void ");
        addCode(pw, methodName);
        addCode(pw, "(String portName) {");

        tabIn();

        addNewCodeLine(pw, "try {");

        tabIn();
        addNewCodeLine(
            pw,
            "WSIFServiceFactory factory = WSIFServiceFactory.newInstance();");

        addNewCodeLine(pw, "factory.setFeature(");
        tabIn();
        addNewCodeLine(pw, "WSIFConstants.WSIF_FEATURE_AUTO_MAP_TYPES,");
        addNewCodeLine(pw, "new Boolean(true));");
        tabOut();
        skipLines(pw, 1);

        addNewCodeLine(pw, "WSIFService service =");

        tabIn();
        addNewCodeLine(pw, "factory.getService(");

        tabIn();
        addNewCodeLine(
            pw,
            "\"" + emitter.getWSDLURI().replace('\\', '/') + "\",");
        addNewCodeLine(pw, "\"" + serviceName.getNamespaceURI() + "\",");
        addNewCodeLine(pw, "\"" + serviceName.getLocalPart() + "\",");
        addNewCodeLine(pw, "\"" + portTypeName.getNamespaceURI() + "\",");
        addNewCodeLine(pw, "\"" + portTypeName.getLocalPart() + "\");");

        tabOut();
        tabOut();
        skipLines(pw, 1);

        writeMapPackages(pw);

        addNewCodeLine(pw, "WSIFPort port = service.getPort(portName);");

        skipLines(pw, 1);
        for (Iterator i = portType.getOperations().iterator(); i.hasNext();) {
            Operation op = (Operation) i.next();
            String opMethodName = makeExecuteOpMethodName(portType, op);
            addNewCodeLine(pw, opMethodName + "(port);");
        }

        skipLines(pw, 1);

        tabOut();
        addNewCodeLine(pw, "} catch (Exception ex) {");

        tabIn();
        addNewCodeLine(pw, "ex.printStackTrace();");
        addNewCodeLine(pw, "assertTrue(\"");
        addCode(pw, methodName);
        addCode(pw, " got exception: \" + ex.getLocalizedMessage(), false);");

        tabOut();
        addNewCodeLine(pw, "}");

        tabOut();
        addNewCodeLine(pw, "}");

        skipLines(pw, 1);

        for (Iterator i = portType.getOperations().iterator(); i.hasNext();) {
            Operation op = (Operation) i.next();
            writeDIIOperationMethod(pw, portType, op);
        }

        return methodName;
    }

    /**
     */
    protected void writeMapPackages(PrintWriter pw) {
        SymbolTable st = emitter.getSymbolTable();
        String packageMapping = emitter.getPackageName();
        if (packageMapping != null) {
            ArrayList nss = new ArrayList();
            Map m = st.getElementIndex();
            for (Iterator i = m.keySet().iterator(); i.hasNext();) {
                QName qn = (QName) i.next();
                String ns = qn.getNamespaceURI();
                if (!nss.contains(ns)) {
                    nss.add(ns);
                }
            }
            m = st.getTypeIndex();
            for (Iterator i = m.keySet().iterator(); i.hasNext();) {
                QName qn = (QName) i.next();
                String ns = qn.getNamespaceURI();
                if (!nss.contains(ns)) {
                    nss.add(ns);
                }
            }
            for (Iterator i = nss.iterator(); i.hasNext();) {
                String ns = (String) i.next();
                if (!(ns.equals(WSIFConstants.NS_URI_SOAP_ENC)
                    || ns.equals(WSIFConstants.NS_URI_1999_SCHEMA_XSD)
                    || ns.equals(WSIFConstants.NS_URI_2000_SCHEMA_XSD)
                    || ns.equals(WSIFConstants.NS_URI_2001_SCHEMA_XSD))) {
                    addNewCodeLine(
                        pw,
                        "service.mapPackage(\""
                            + ns
                            + "\", \""
                            + packageMapping
                            + "\");");
                }
            }
            skipLines(pw, 1);
        }
    }

    /**
     * Write the executeOp method using the WSIF DII
     */
    protected void writeDIIOperationMethod(
        PrintWriter pw,
        PortType portType,
        Operation op) {

        addNewCodeLine(pw, "protected void ");

        String opMethodName = makeExecuteOpMethodName(portType, op);
        addCode(pw, opMethodName);

        addCode(pw, "(WSIFPort wsifPort) throws WSIFException {");

        skipLines(pw, 1);
        tabIn();

        addNewCodeLine(pw, "WSIFOperation op = wsifPort.createOperation(");
        if (isOverloaded(portType, op)) {
            tabIn();
            addNewCodeLine(pw, "\"");
            addCode(pw, op.getName());
            addCode(pw, "\",");
            addNewCodeLine(pw, "\"");
            addCode(pw, getInMsgName(op));
            addCode(pw, "\",");
            addNewCodeLine(pw, "null);");
            tabOut();
        } else {
            addCode(pw, "\"");
            addCode(pw, op.getName());
            addCode(pw, "\");");
        }

        skipLines(pw, 1);
        addNewCodeLine(pw, "WSIFMessage inMsg = op.createInputMessage();");
        addNewCodeLine(pw, "WSIFMessage outMsg = op.createOutputMessage();");
        addNewCodeLine(pw, "WSIFMessage faultMsg = op.createFaultMessage();");

        skipLines(pw, 1);
        boolean requireWrapperFlag = true;
        Input in = op.getInput();
        if (in != null && in.getMessage() != null) {
            List parts = in.getMessage().getOrderedParts(null);
            boolean wasUnwrapped =
                unwrapIfWrapped(
                    emitter.getCurrentDefinition(),
                    parts,
                    op.getName());

            if (wasUnwrapped) {
                if (parts.size() > 1) {
                    requireWrapperFlag = false;
                }
            } else {
                requireWrapperFlag = false;
            }

            if (parts.size() > 0) {
                addNewCodeLine(pw, "// TODO: initialize these parameters with required values");
            }

            int parameterCounter = 0;
            for (Iterator i = parts.iterator(); i.hasNext();) {
                Part p = (Part) i.next();
                QName type = ProviderUtils.getPartType(p);
                String className = Utils.getJavaLocalName(getClassName(type));
                String paramName = "p" + parameterCounter++;
                addNewCodeLine(
                    pw,
                    className
                        + " "
                        + paramName
                        + " = "
                        + getTypeInitialiser(type)
                        + ";");
                String setType;
                if (Character.isLowerCase(className.charAt(0))) {
                    setType = ProviderUtils.capitalizeFirst(className);
                } else {
                    setType = "Object";
                }
                addNewCodeLine(pw, "inMsg.set");
                addCode(pw, setType);
                addCode(pw, "Part(\"");
                addCode(pw, p.getName());
                addCode(pw, "\", " + paramName + ");");
                skipLines(pw, 1);
            }
        }

        if (requireWrapperFlag) {
            addNewCodeLine(pw, "WSIFMessage ctx = op.getContext();");
            addNewCodeLine(pw, "ctx.setObjectPart(");
            tabIn();
            addNewCodeLine(pw, "WSIFConstants.CONTEXT_OPERATION_STYLE,");
            addNewCodeLine(
                pw,
                "WSIFConstants.CONTEXT_OPERATION_STYLE_WRAPPED);");
            tabOut();
            addNewCodeLine(pw, "op.setContext(ctx);");
            skipLines(pw, 1);
        }

        addNewCodeLine(pw, "boolean success =");
        tabIn();
        addNewCodeLine(
            pw,
            "op.executeRequestResponseOperation(inMsg, outMsg, faultMsg);");
        tabOut();

        skipLines(pw, 1);

        addNewCodeLine(pw, "if (success) {");

        tabIn();
        addNewCodeLine(
            pw,
            "System.out.println(\"operation '"
                + op.getName()
                + "' successfull:\");");

        Output out = op.getOutput();
        if (out != null && out.getMessage() != null) {
            List parts = out.getMessage().getOrderedParts(null);
            unwrapIfWrapped(
                emitter.getCurrentDefinition(),
                parts,
                op.getName() + "Response");
            int parameterCounter = 0;
            for (Iterator i = parts.iterator(); i.hasNext();) {
                Part p = (Part) i.next();
                QName type = ProviderUtils.getPartType(p);
                String className = Utils.getJavaLocalName(getClassName(type));
                String paramName = "r" + parameterCounter++;
                String getType;
                String cast;
                if (Character.isLowerCase(className.charAt(0))) {
                    getType = ProviderUtils.capitalizeFirst(className);
                    cast = "";
                } else {
                    getType = "Object";
                    cast = "(" + className + ")";
                }
                addNewCodeLine(
                    pw,
                    className
                        + " "
                        + paramName
                        + " = "
                        + cast
                        + " outMsg.get"
                        + getType
                        + "Part(\""
                        + p.getName()
                        + "\");");
                addNewCodeLine(
                    pw,
                    "System.out.println(\""
                        + p.getName()
                        + ": \" + "
                        + paramName
                        + ");");
            }
            if (parts.size() > 0) {
                skipLines(pw, 1);
                addNewCodeLine(pw, "// TODO: validate response parts here");
                skipLines(pw, 1);
            }
        }

        tabOut();
        addNewCodeLine(pw, "} else {");

        tabIn();
        addNewCodeLine(
            pw,
            "System.err.println(\"operation '"
                + op.getName()
                + "' returned a fault:\");");
        addNewCodeLine(
            pw,
            "for (Iterator i = faultMsg.getPartNames(); i.hasNext();) {");

        tabIn();
        addNewCodeLine(
            pw,
            "System.err.println(faultMsg.getObjectPart((String) i.next()));");

        tabOut();
        addNewCodeLine(pw, "}");

        addNewCodeLine(pw, "assertTrue(\"");
        addCode(pw, opMethodName);
        addCode(pw, " returned a fault!!\", false);");

        tabOut();
        addNewCodeLine(pw, "}");

        tabOut();
        addNewCodeLine(pw, "}");
        skipLines(pw, 1);

    }

    /**
     * Write the JUnit test method
     */
    protected void writeTestMethod(
        PrintWriter pw,
        String methodName,
        String portName)
        throws IOException {
        String testMethodname =
            "test" + ProviderUtils.capitalizeFirst(methodName) + portName;
        addNewCodeLine(pw, "public void " + testMethodname + "() {");
        tabIn();
        addNewCodeLine(pw, methodName + "(\"" + portName + "\");");
        tabOut();
        addNewCodeLine(pw, "}");
        skipLines(pw, 1);
    } // writeTestMethod

    /**
     * Write the main method
     */
    protected void writeMainMethod(PrintWriter pw) throws IOException {
        addNewCodeLine(pw, "public static void main(String[] args) {");
        tabIn();
        addNewCodeLine(pw, "TestRunner.run(suite());");
        tabOut();
        addNewCodeLine(pw, "}");
        skipLines(pw, 1);
    } // writeMainMethod

    protected String makeExecuteOpMethodName(PortType portType, Operation op) {
        String javaOpName = Utils.xmlNameToJavaClass(op.getName());
        String opMethodName = "executeOp" + javaOpName;
        if (isOverloaded(portType, op)) {
            opMethodName += ProviderUtils.capitalizeFirst(getInMsgName(op));
        }
        return opMethodName;
    }

    /**
     * Unwraps a WSDL message if required
     */
    protected boolean unwrapIfWrapped(
        Definition def,
        List parts,
        String operationName) {

        boolean wasUnwrapped = false;
        if (!emitter.isNowrap()) {
            Part p = ProviderUtils.getWrapperPart(parts, operationName);
            if (p != null) {
                List unwrappedParts;
                try {
                    unwrappedParts = ProviderUtils.unWrapPart(p, def);
                } catch (WSIFException e) {
                    throw new RuntimeException(
                        "exception unwrapping operation "
                            + operationName
                            + ": "
                            + e.getLocalizedMessage());
                }
                int i = parts.indexOf(p);
                parts.remove(i);
                parts.addAll(i, unwrappedParts);
                wasUnwrapped = true;
            }
        }
        return wasUnwrapped;
    }

    /**
     * Gets the name of a WSDL operation's input message
     */
    protected String getInMsgName(Operation op) {
        String inMsgName = "";
        Input inMsg = op.getInput();
        if (inMsg != null) {
            inMsgName = inMsg.getName();
        }
        return inMsgName;
    }

    /**
     * Tests if the operation is overloaded in the portType
     */
    protected boolean isOverloaded(PortType pt, Operation op) {
        boolean overloaded = false;
        String opName = op.getName();
        for (Iterator i = pt.getOperations().iterator();
            !overloaded && i.hasNext();
            ) {
            Operation o2 = (Operation) i.next();
            if (!op.equals(o2) && opName.equals(o2.getName())) {
                overloaded = true;
            }
        }
        return overloaded;
    }

    /**
     * Gets the fully qualified Java class name from a QName 
     */
    protected String getClassName(QName type) {
        String className = "";
        SymbolTable st = emitter.getSymbolTable();
        Type t = st.getType(type);
        if (t != null) {
            className = t.getName();
        } else {
            Map types = WSIFUtils.getSimpleTypesMap();
            Object o = types.get(type);
            if (o != null) {
                String s = (String) o;
                if (s.indexOf('[') < 0) {
                    className = s;
                } else {
                    className = getArrayType(s);
                }
            } else {
                className =
                    Utils.makePackageName(type.getNamespaceURI())
                        + "."
                        + Utils.xmlNameToJavaClass(type.getLocalPart());
            }
        }
        return className;
    }

    protected static String getArrayType(String type) {
        String dimensions = "";
        for (int i = 0; type.charAt(i) == '['; i++) {
            dimensions += "[]";
        }
        String arrayType = "";
        switch (type.charAt(type.lastIndexOf('[') + 1)) {
            case 'I' :
                arrayType = "int";
                break;
            case 'F' :
                arrayType = "float";
                break;
            case 'J' :
                arrayType = "long";
                break;
            case 'D' :
                arrayType = "double";
                break;
            case 'Z' :
                arrayType = "boolean";
                break;
            case 'B' :
                arrayType = "byte";
                break;
            case 'S' :
                arrayType = "short";
                break;
            case 'L' :
                arrayType = type.substring(type.lastIndexOf('[') + 2);
                break;
        }
        return arrayType + dimensions;
    }

    /**
     * Gets the initiliser for a type
     */
    protected String getTypeInitialiser(QName type) {
        String s = "";
        String className = Utils.getJavaLocalName(getClassName(type));
        String uqClassName = Utils.getJavaLocalName(className);
        if ("int".equals(uqClassName)) {
            s = "0";
        } else if ("short".equals(uqClassName)) {
            s = "0";
        } else if ("boolean".equals(uqClassName)) {
            s = "false";
        } else if ("byte".equals(uqClassName)) {
            s = "0";
        } else if ("long".equals(uqClassName)) {
            s = "0";
        } else if ("double".equals(uqClassName)) {
            s = "0";
        } else if ("float".equals(uqClassName)) {
            s = "0";
        } else if ("char".equals(uqClassName)) {
            s = "''";
        } else if ("String".equals(uqClassName)) {
            s = "\"\"";
        } else {
            StringBuffer constructor = new StringBuffer(className);
            if (className.indexOf('[') < 0) {
                constructor.append("()");
            } else {
                for (int i = 0; i < constructor.length(); i++) {
                    if (constructor.charAt(i) == '[') {
                        constructor.insert(i + 1, '0');
                    }
                }
            }
            s = "new " + constructor.toString();
        }
        return s;
    }

    /**
     * Gets the Service Endpoint Interface name for a WSDL portType
     */
    protected String getSEIName(PortType portType) {
        return ProviderUtils.capitalizeFirst(
            Utils.getJavaLocalName(getClassName(portType.getQName())));
    }

    /**
     * Start a new line of code correctly indented
     */
    protected void addNewCodeLine(PrintWriter pw, String line) {
        pw.println();
        pw.print(indent);
        pw.print(line);
    }

    /**
     * add code to an already started code line 
     */
    protected void addCode(PrintWriter pw, String lineFragment) {
        pw.print(lineFragment);
    }

    /**
     * Insert some blank lines into the output
     */
    protected void skipLines(PrintWriter pw, int lines) {
        for (int i = 0; i < lines; i++) {
            pw.println();
        }
    }

    /**
     * Indent all subsequent code by one tab space
     */
    protected void tabIn() {
        indent += TAB;
    }

    /**
     * Un-indent all subsequent code by one tab space
     */
    protected void tabOut() {
        if (indent.length() >= TAB.length()) {
            indent = indent.substring(0, indent.length() - TAB.length());
        }
    }

} // class JavaTestCase

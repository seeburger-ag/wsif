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

package soapinterop;

import javax.xml.namespace.QName;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.providers.soap.apachesoap.WSIFDynamicProvider_ApacheSOAP;
import org.apache.wsif.util.WSIFPluggableProviders;
import util.TestUtilities;

/**
 * InteropTestServiceProxy
 */
public class InteropTestServiceProxy {
    private WSIFServiceFactory factory;
    private WSIFService svc;
        String wsdlLocation = //"com/ibm/wsif/test/soapinterop/ApacheAxis.wsdl";
    TestUtilities.getWsdlPath("java\\test\\soapinterop") + "ApacheAxis.wsdl";

    /**
     * getPortFactory
     * @generated
     */
    /*	public WSIFDynamicPortFactory getPortFactory() {
    		return fieldPortFactory;
    	}*/
    /**
     * setPortFactory
     * @generated
     */
    /*	public void setPortFactory(WSIFDynamicPortFactory newPortFactory) {
    		fieldPortFactory= newPortFactory;
    	}*/

    /**
     * echoString
     * @generated
     */
    public java.lang.String echoString(java.lang.String argInputString)
        throws WSIFException {

        WSIFPort port = this.svc.getPort();

        WSIFOperation operation = port.createOperation("echoString", null, null);

        WSIFMessage inputMessage = operation.createInputMessage();

        WSIFMessage outputMessage = operation.createOutputMessage();

        inputMessage.setObjectPart("inputString", argInputString);

        operation.executeRequestResponseOperation(inputMessage, outputMessage, null);

        port.close();

        return (java.lang.String) outputMessage.getObjectPart("return");

    }

    /**
     * echoStringArray
     * @generated
     */
    public java.lang.String[] echoStringArray(
        java.lang.String[] argInputStringArray)
        throws WSIFException {

        WSIFPort port = this.svc.getPort();

        WSIFOperation operation =
            port.createOperation(
                "echoStringArray",
                "echoStringArrayRequest",
                "echoStringArrayResponse");

        WSIFMessage inputMessage = operation.createInputMessage();
        inputMessage.setName("echoStringArrayRequest");

        WSIFMessage outputMessage = operation.createOutputMessage();
        outputMessage.setName("echoStringArrayResponse");

        inputMessage.setObjectPart("inputStringArray", argInputStringArray);

        operation.executeRequestResponseOperation(inputMessage, outputMessage, null);

        port.close();

        return (java.lang.String[]) outputMessage.getObjectPart("return");

    }

    /**
     * echoInteger
     * @generated
     */
    public int echoInteger(int argInputInteger) throws WSIFException {

        WSIFPort port = this.svc.getPort();

        WSIFOperation operation =
            port.createOperation(
                "echoInteger",
                "echoIntegerRequest",
                "echoIntegerResponse");

        WSIFMessage inputMessage = operation.createInputMessage();
        inputMessage.setName("echoIntegerRequest");

        WSIFMessage outputMessage = operation.createOutputMessage();
        outputMessage.setName("echoIntegerResponse");

        inputMessage.setObjectPart(
            "inputInteger",
            new java.lang.Integer(argInputInteger));

        operation.executeRequestResponseOperation(inputMessage, outputMessage, null);

        port.close();

        return ((java.lang.Integer) outputMessage.getObjectPart("return")).intValue();

    }

    /**
     * echoIntegerArray
     * @generated
     */
    public int[] echoIntegerArray(int[] argInputIntegerArray)
        throws WSIFException {

        WSIFPort port = this.svc.getPort();

        WSIFOperation operation =
            port.createOperation(
                "echoIntegerArray",
                "echoIntegerArrayRequest",
                "echoIntegerArrayResponse");

        WSIFMessage inputMessage = operation.createInputMessage();
        inputMessage.setName("echoIntegerArrayRequest");

        WSIFMessage outputMessage = operation.createOutputMessage();
        outputMessage.setName("echoIntegerArrayResponse");

        inputMessage.setObjectPart("inputIntegerArray", argInputIntegerArray);

        operation.executeRequestResponseOperation(inputMessage, outputMessage, null);

        port.close();

        return (int[]) outputMessage.getObjectPart("return");

    }

    /**
     * echoFloat
     * @generated
     */
    public float echoFloat(float argInputFloat) throws WSIFException {

        WSIFPort port = this.svc.getPort();

        WSIFOperation operation =
            port.createOperation("echoFloat", "echoFloatRequest", "echoFloatResponse");

        WSIFMessage inputMessage = operation.createInputMessage();
        inputMessage.setName("echoFloatRequest");

        WSIFMessage outputMessage = operation.createOutputMessage();
        outputMessage.setName("echoFloatResponse");

        inputMessage.setObjectPart("inputFloat", new java.lang.Float(argInputFloat));

        operation.executeRequestResponseOperation(inputMessage, outputMessage, null);

        port.close();

        return ((java.lang.Float) outputMessage.getObjectPart("return")).floatValue();

    }

    /**
     * echoFloatArray
     * @generated
     */
    public float[] echoFloatArray(float[] argInputFloatArray)
        throws WSIFException {

        WSIFPort port = this.svc.getPort();

        WSIFOperation operation =
            port.createOperation(
                "echoFloatArray",
                "echoFloatArrayRequest",
                "echoFloatArrayResponse");

        WSIFMessage inputMessage = operation.createInputMessage();
        inputMessage.setName("echoFloatArrayRequest");

        WSIFMessage outputMessage = operation.createOutputMessage();
        outputMessage.setName("echoFloatArrayResponse");

        inputMessage.setObjectPart("inputFloatArray", argInputFloatArray);

        operation.executeRequestResponseOperation(inputMessage, outputMessage, null);

        port.close();

        return (float[]) outputMessage.getObjectPart("return");

    }

    /**
     * echoStruct
     * @generated
     */
    /*    public org.soapinterop.xsd.SOAPStruct echoStruct(
            org.soapinterop.xsd.SOAPStruct argInputStruct)
            throws WSIFException {
    
            WSIFPort port = this.svc.getPort();
    
            WSIFOperation operation =
                port.createOperation("echoStruct", "echoStructRequest", "echoStructResponse");
    
            WSIFMessage inputMessage = operation.createInputMessage();
            inputMessage.setName("echoStructRequest");
    
            WSIFMessage outputMessage = operation.createOutputMessage();
            outputMessage.setName("echoStructResponse");
    
            inputMessage.setObjectPart("inputStruct", argInputStruct);
    
            operation.executeRequestResponseOperation(inputMessage, outputMessage, null);
    
            port.close();
    
            return (org.soapinterop.xsd.SOAPStruct) outputMessage.getObjectPart("return");
    
        }*/

    /**
     * echoStructArray
     * @generated
     */
    /*    public org.soapinterop.xsd.SOAPStruct[] echoStructArray(
            org.soapinterop.xsd.SOAPStruct[] argInputStructArray)
            throws WSIFException {
    
            WSIFPort port = this.svc.getPort();
    
            WSIFOperation operation =
                port.createOperation(
                    "echoStructArray",
                    "echoStructArrayRequest",
                    "echoStructArrayResponse");
    
            WSIFMessage inputMessage = operation.createInputMessage();
            inputMessage.setName("echoStructArrayRequest");
    
            WSIFMessage outputMessage = operation.createOutputMessage();
            outputMessage.setName("echoStructArrayResponse");
    
            inputMessage.setObjectPart("inputStructArray", argInputStructArray);
    
            operation.executeRequestResponseOperation(inputMessage, outputMessage, null);
    
            port.close();
    
            return (org.soapinterop.xsd.SOAPStruct[]) outputMessage.getObjectPart("return");
    
        }*/

    /**
     * echoVoid
     * @generated
     */
    public void echoVoid() throws WSIFException {

        WSIFPort port = this.svc.getPort();

        WSIFOperation operation =
            port.createOperation("echoVoid", "echoVoidRequest", "echoVoidResponse");

        WSIFMessage inputMessage = operation.createInputMessage();
        inputMessage.setName("echoVoidRequest");

        WSIFMessage outputMessage = operation.createOutputMessage();
        outputMessage.setName("echoVoidResponse");

        operation.executeRequestResponseOperation(inputMessage, outputMessage, null);

        port.close();

    }

    /**
     * echoBase64
     * @generated
     */
    public byte[] echoBase64(byte[] argInputBase64) throws WSIFException {

        WSIFPort port = this.svc.getPort();

        WSIFOperation operation =
            port.createOperation("echoBase64", "echoBase64Request", "echoBase64Response");

        WSIFMessage inputMessage = operation.createInputMessage();
        inputMessage.setName("echoBase64Request");

        WSIFMessage outputMessage = operation.createOutputMessage();
        outputMessage.setName("echoBase64Response");

        inputMessage.setObjectPart("inputBase64", argInputBase64);

        operation.executeRequestResponseOperation(inputMessage, outputMessage, null);

        port.close();

        return (byte[]) outputMessage.getObjectPart("return");

    }

    /**
     * echoDate
     * @generated
     */
    public java.util.Date echoDate(java.util.Date argInputDate)
        throws WSIFException {

        WSIFPort port = this.svc.getPort();

        WSIFOperation operation =
            port.createOperation("echoDate", "echoDateRequest", "echoDateResponse");

        WSIFMessage inputMessage = operation.createInputMessage();
        inputMessage.setName("echoDateRequest");

        WSIFMessage outputMessage = operation.createOutputMessage();
        outputMessage.setName("echoDateResponse");

        inputMessage.setObjectPart("inputDate", argInputDate);

        operation.executeRequestResponseOperation(inputMessage, outputMessage, null);

        port.close();

        return (java.util.Date) outputMessage.getObjectPart("return");

    }

    /**
     * echoHexBinary
     * @generated
     */
    public byte[] echoHexBinary(byte[] argInputHexBinary) throws WSIFException {

        WSIFPort port = this.svc.getPort();

        WSIFOperation operation =
            port.createOperation(
                "echoHexBinary",
                "echoHexBinaryRequest",
                "echoHexBinaryResponse");

        WSIFMessage inputMessage = operation.createInputMessage();
        inputMessage.setName("echoHexBinaryRequest");

        WSIFMessage outputMessage = operation.createOutputMessage();
        outputMessage.setName("echoHexBinaryResponse");

        inputMessage.setObjectPart("inputHexBinary", argInputHexBinary);

        operation.executeRequestResponseOperation(inputMessage, outputMessage, null);

        port.close();

        return (byte[]) outputMessage.getObjectPart("return");

    }

    /**
     * echoDecimal
     * @generated
     */
    public java.math.BigDecimal echoDecimal(java.math.BigDecimal argInputDecimal)
        throws WSIFException {

        WSIFPort port = this.svc.getPort();

        WSIFOperation operation =
            port.createOperation(
                "echoDecimal",
                "echoDecimalRequest",
                "echoDecimalResponse");

        WSIFMessage inputMessage = operation.createInputMessage();
        inputMessage.setName("echoDecimalRequest");

        WSIFMessage outputMessage = operation.createOutputMessage();
        outputMessage.setName("echoDecimalResponse");

        inputMessage.setObjectPart("inputDecimal", argInputDecimal);

        operation.executeRequestResponseOperation(inputMessage, outputMessage, null);

        port.close();

        return (java.math.BigDecimal) outputMessage.getObjectPart("return");

    }

    /**
     * echoBoolean
     * @generated
     */
    public boolean echoBoolean(boolean argInputBoolean) throws WSIFException {

        WSIFPort port = this.svc.getPort();

        WSIFOperation operation =
            port.createOperation(
                "echoBoolean",
                "echoBooleanRequest",
                "echoBooleanResponse");

        WSIFMessage inputMessage = operation.createInputMessage();
        inputMessage.setName("echoBooleanRequest");

        WSIFMessage outputMessage = operation.createOutputMessage();
        outputMessage.setName("echoBooleanResponse");

        inputMessage.setObjectPart(
            "inputBoolean",
            new java.lang.Boolean(argInputBoolean));

        operation.executeRequestResponseOperation(inputMessage, outputMessage, null);

        port.close();

        return ((java.lang.Boolean) outputMessage.getObjectPart("return"))
            .booleanValue();

    }

    /**
     * Constructor
     * @generated
     */
    public InteropTestServiceProxy() throws WSIFException {

        this.factory = WSIFServiceFactory.newInstance();

        WSIFPluggableProviders.overrideDefaultProvider(
            "http://schemas.xmlsoap.org/wsdl/soap/",
            new WSIFDynamicProvider_ApacheSOAP());

        this.svc = factory.getService(wsdlLocation,
            //this.getClass().getClassLoader(),
    	    "http://soapinterop.org/",
            "interopLab",
            "http://soapinterop.org/",
            "InteropTestPortType");

        if (this.svc == null)
            throw new WSIFException("Failed to create WSIFDynamicPortFactory");
        //this.svc.mapType(
        //    new QName("http://soapinterop.org/xsd", "SOAPStruct"),
        //    SOAPStruct.class);
        this.svc.mapType(
            new QName("http://soapinterop.org/xsd", "ArrayOfstring"),
            java.lang.String[].class);
        this.svc.mapType(
            new QName("http://soapinterop.org/xsd", "ArrayOfint"),
            int[].class);
        //this.svc.mapType(
        //    new QName("http://soapinterop.org/xsd", "ArrayOfSOAPStruct"),
        //    SOAPStruct[].class);
        this.svc.mapType(
            new QName("http://soapinterop.org/xsd", "ArrayOffloat"),
            float[].class);

    }

}

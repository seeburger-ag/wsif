/**
 * CounterServiceTestCase.java
 *
 * This file was auto-generated from WSDL
 * by the Apache WSIF WSDL2Java emitter.
 */

package defects.ogsa;

import java.rmi.RemoteException;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.wsif.WSIFConstants;
import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;

public class CounterServiceTestCase extends TestCase {

    public CounterServiceTestCase(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(CounterServiceTestCase.class);
    }

    protected void doitDIICounterPortType(String portName) {
        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
            factory.setFeature(
                WSIFConstants.WSIF_FEATURE_AUTO_MAP_TYPES,
                new Boolean(true));

            WSIFService service =
                factory.getService(
                    "bin/defects/ogsa/counter.wsdl",
                    "http://samples.ogsa.globus.org/counter",
                    "CounterService",
                    "http://samples.ogsa.globus.org/counter/counter_port_type",
                    "CounterPortType");

            WSIFPort port = service.getPort(portName);

            executeOpAdd(port);
            executeOpSubtract(port);
            executeOpGetValue(port);

        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue("doitDIICounterPortType got exception: " + ex.getLocalizedMessage(), false);
        }
    }

    protected void executeOpAdd(WSIFPort wsifPort) throws WSIFException {

        WSIFOperation op = wsifPort.createOperation("add");

        WSIFMessage inMsg = op.createInputMessage();
        WSIFMessage outMsg = op.createOutputMessage();
        WSIFMessage faultMsg = op.createFaultMessage();

        // TODO: change these parameter values
        int p0 = 1;
        inMsg.setIntPart("value", p0);

        boolean success =
            op.executeRequestResponseOperation(inMsg, outMsg, faultMsg);

        if (success) {
            System.out.println("operation 'add' successfull:");
            int r0 = outMsg.getIntPart("returnValue");
            System.out.println("returnValue: " + r0);

            // TODO: add tests for return parts here

        } else {
            System.err.println("operation 'add' returned a fault:");
            for (Iterator i = faultMsg.getPartNames(); i.hasNext();) {
                System.err.println(faultMsg.getObjectPart((String) i.next()));
            }
            assertTrue("executeOpAdd returned a fault!!", false);
        }
    }

    protected void executeOpSubtract(WSIFPort wsifPort) throws WSIFException {

        WSIFOperation op = wsifPort.createOperation("subtract");

        WSIFMessage inMsg = op.createInputMessage();
        WSIFMessage outMsg = op.createOutputMessage();
        WSIFMessage faultMsg = op.createFaultMessage();

        // TODO: change these parameter values
        int p0 = 2;
        inMsg.setIntPart("value", p0);

        boolean success =
            op.executeRequestResponseOperation(inMsg, outMsg, faultMsg);

        if (success) {
            System.out.println("operation 'subtract' successfull:");
            int r0 = outMsg.getIntPart("returnValue");
            System.out.println("returnValue: " + r0);

            // TODO: add tests for return parts here

        } else {
            System.err.println("operation 'subtract' returned a fault:");
            for (Iterator i = faultMsg.getPartNames(); i.hasNext();) {
                System.err.println(faultMsg.getObjectPart((String) i.next()));
            }
            assertTrue("executeOpSubtract returned a fault!!", false);
        }
    }

    protected void executeOpGetValue(WSIFPort wsifPort) throws WSIFException {

        WSIFOperation op = wsifPort.createOperation("getValue");

        WSIFMessage ctx = op.getContext();
        ctx.setObjectPart(
            WSIFConstants.CONTEXT_OPERATION_STYLE,
            WSIFConstants.CONTEXT_OPERATION_STYLE_WRAPPED);
        op.setContext(ctx);

        WSIFMessage inMsg = op.createInputMessage();
        WSIFMessage outMsg = op.createOutputMessage();
        WSIFMessage faultMsg = op.createFaultMessage();

        // TODO: change these parameter values
        boolean success =
            op.executeRequestResponseOperation(inMsg, outMsg, faultMsg);

        if (success) {
            System.out.println("operation 'getValue' successfull:");
            int r0 = outMsg.getIntPart("returnValue");
            System.out.println("returnValue: " + r0);

            // TODO: add tests for return parts here

        } else {
            System.err.println("operation 'getValue' returned a fault:");
            for (Iterator i = faultMsg.getPartNames(); i.hasNext();) {
                System.err.println(faultMsg.getObjectPart((String) i.next()));
            }
            assertTrue("executeOpGetValue returned a fault!!", false);
        }
    }

    protected void doitStubCounterPortType(String portName) {
        try {
            WSIFServiceFactory factory = WSIFServiceFactory.newInstance();

            WSIFService service =
                factory.getService(
                    "bin/defects/ogsa/counter.wsdl",
                    "http://samples.ogsa.globus.org/counter",
                    "CounterService",
                    "http://samples.ogsa.globus.org/counter/counter_port_type",
                    "CounterPortType");

            WSIFMessage ctx = service.getContext();
            ctx.setObjectPart(
                WSIFConstants.CONTEXT_OPERATION_STYLE,
                WSIFConstants.CONTEXT_OPERATION_STYLE_WRAPPED);
            service.setContext(ctx);

            CounterPortType stub =
                (CounterPortType) service.getStub(portName, CounterPortType.class);

            executeOpAdd(stub);
            executeOpSubtract(stub);
            executeOpGetValue(stub);

        } catch (Exception ex) {
            ex.printStackTrace();
            assertTrue("doitStubCounterPortType got exception: " + ex.getLocalizedMessage(), false);
        }
    }

    protected void executeOpAdd(CounterPortType stub) throws RemoteException {

        // TODO: change these params to required values
        int p0 = 3;

        int returnValue = stub.add(p0);

        System.out.println("operation 'add' returned: " + returnValue);

        // TODO: test return parts here
        // assertTrue("operation add returned null!!", returnValue != null);
    }

    protected void executeOpSubtract(CounterPortType stub) throws RemoteException {

        // TODO: change these params to required values
        int p0 = 4;

        int returnValue = stub.subtract(p0);

        System.out.println("operation 'subtract' returned: " + returnValue);

        // TODO: test return parts here
        // assertTrue("operation subtract returned null!!", returnValue != null);
    }

    protected void executeOpGetValue(CounterPortType stub) throws RemoteException {

        // TODO: change these params to required values

        int returnValue = stub.getValue();

        System.out.println("operation 'getValue' returned: " + returnValue);

        // TODO: test return parts here
        // assertTrue("operation getValue returned null!!", returnValue != null);
    }

    public void testDoitDIICounterPortTypeCounterPort() {
        doitDIICounterPortType("CounterPort");
    }

    public void testDoitStubCounterPortTypeCounterPort() {
        doitStubCounterPortType("CounterPort");
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
}

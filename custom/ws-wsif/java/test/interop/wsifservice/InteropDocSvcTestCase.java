/**
 * InteropDocSvcTestCase.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package interop.wsifservice;

public class InteropDocSvcTestCase extends junit.framework.TestCase {
    public InteropDocSvcTestCase(java.lang.String name) {
        super(name);
    }
    public void test1interopDocPortSingleTag() {
        interop.wsifservice.Doc_TestPortType binding;
        try {
            binding = new interop.wsifservice.InteropDocSvcLocator().getinteropDocPort();
        }
        catch (javax.xml.rpc.ServiceException jre) {
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
            throw new junit.framework.AssertionFailedError("JAX-RPC ServiceException caught: " + jre);
        }
        assertTrue("binding is null", binding != null);

        try {
            interop.wsifservice.SingleTag_Type value = null;
            value = binding.singleTag(new interop.wsifservice.SingleTag_Type());
        }
        catch (java.rmi.RemoteException re) {
            throw new junit.framework.AssertionFailedError("Remote Exception caught: " + re);
        }
    }

    public void test2interopDocPortSimpleDocument() {
        interop.wsifservice.Doc_TestPortType binding;
        try {
            binding = new interop.wsifservice.InteropDocSvcLocator().getinteropDocPort();
        }
        catch (javax.xml.rpc.ServiceException jre) {
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
            throw new junit.framework.AssertionFailedError("JAX-RPC ServiceException caught: " + jre);
        }
        assertTrue("binding is null", binding != null);

        try {
            interop.wsifservice.SimpleDocument_Type value = null;
            value = binding.simpleDocument(new interop.wsifservice.SimpleDocument_Type());
        }
        catch (java.rmi.RemoteException re) {
            throw new junit.framework.AssertionFailedError("Remote Exception caught: " + re);
        }
    }

    public void test3interopDocPortComplexDocument() {
        interop.wsifservice.Doc_TestPortType binding;
        try {
            binding = new interop.wsifservice.InteropDocSvcLocator().getinteropDocPort();
        }
        catch (javax.xml.rpc.ServiceException jre) {
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
            throw new junit.framework.AssertionFailedError("JAX-RPC ServiceException caught: " + jre);
        }
        assertTrue("binding is null", binding != null);

        try {
            interop.wsifservice.ComplexDocument_Type value = null;
            value = binding.complexDocument(new interop.wsifservice.ComplexDocument_Type());
        }
        catch (java.rmi.RemoteException re) {
            throw new junit.framework.AssertionFailedError("Remote Exception caught: " + re);
        }
    }

}

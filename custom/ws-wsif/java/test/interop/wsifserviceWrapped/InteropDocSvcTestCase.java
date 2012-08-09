/**
 * InteropDocSvcTestCase.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package interop.wsifserviceWrapped;

public class InteropDocSvcTestCase extends junit.framework.TestCase {
    public InteropDocSvcTestCase(java.lang.String name) {
        super(name);
    }
    public void test1interopDocPortSingleTag() {
        interop.wsifserviceWrapped.Doc_TestPortType binding;
        try {
            binding = new interop.wsifserviceWrapped.InteropDocSvcLocator().getinteropDocPort();
        }
        catch (javax.xml.rpc.ServiceException jre) {
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
            throw new junit.framework.AssertionFailedError("JAX-RPC ServiceException caught: " + jre);
        }
        assertTrue("binding is null", binding != null);

        try {
            interop.wsifserviceWrapped.SingleTagResponse value = null;
            value = binding.singleTag(new interop.wsifserviceWrapped.SingleTag_ElemType());
        }
        catch (java.rmi.RemoteException re) {
            throw new junit.framework.AssertionFailedError("Remote Exception caught: " + re);
        }
    }

    public void test2interopDocPortSimpleDocument() {
        interop.wsifserviceWrapped.Doc_TestPortType binding;
        try {
            binding = new interop.wsifserviceWrapped.InteropDocSvcLocator().getinteropDocPort();
        }
        catch (javax.xml.rpc.ServiceException jre) {
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
            throw new junit.framework.AssertionFailedError("JAX-RPC ServiceException caught: " + jre);
        }
        assertTrue("binding is null", binding != null);

        try {
            interop.wsifserviceWrapped.SimpleDocumentResponse value = null;
            value = binding.simpleDocument(new interop.wsifserviceWrapped.SimpleDocument_ElemType());
        }
        catch (java.rmi.RemoteException re) {
            throw new junit.framework.AssertionFailedError("Remote Exception caught: " + re);
        }
    }

    public void test3interopDocPortComplexDocument() {
        interop.wsifserviceWrapped.Doc_TestPortType binding;
        try {
            binding = new interop.wsifserviceWrapped.InteropDocSvcLocator().getinteropDocPort();
        }
        catch (javax.xml.rpc.ServiceException jre) {
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
            throw new junit.framework.AssertionFailedError("JAX-RPC ServiceException caught: " + jre);
        }
        assertTrue("binding is null", binding != null);

        try {
            interop.wsifserviceWrapped.ComplexDocumentResponse value = null;
            value = binding.complexDocument(new interop.wsifserviceWrapped.ComplexDocument_ElemType());
        }
        catch (java.rmi.RemoteException re) {
            throw new junit.framework.AssertionFailedError("Remote Exception caught: " + re);
        }
    }

}

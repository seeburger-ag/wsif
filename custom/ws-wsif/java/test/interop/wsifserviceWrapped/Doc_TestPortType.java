/**
 * Doc_TestPortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package interop.wsifserviceWrapped;

public interface Doc_TestPortType extends java.rmi.Remote {
    public interop.wsifserviceWrapped.SingleTagResponse singleTag(interop.wsifserviceWrapped.SingleTag_ElemType parameters) throws java.rmi.RemoteException;
    public interop.wsifserviceWrapped.SimpleDocumentResponse simpleDocument(interop.wsifserviceWrapped.SimpleDocument_ElemType parameters) throws java.rmi.RemoteException;
    public interop.wsifserviceWrapped.ComplexDocumentResponse complexDocument(interop.wsifserviceWrapped.ComplexDocument_ElemType parameters) throws java.rmi.RemoteException;
}

/**
 * CounterPortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package defects.ogsa;

public interface CounterPortType extends java.rmi.Remote {
    public int add(int value) throws java.rmi.RemoteException;
    public int subtract(int value) throws java.rmi.RemoteException;
    public int getValue() throws java.rmi.RemoteException;
}

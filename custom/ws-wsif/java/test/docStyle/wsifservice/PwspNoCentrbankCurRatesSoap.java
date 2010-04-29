/**
 * PwspNoCentrbankCurRatesSoap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package docStyle.wsifservice;

import http.net.pointwsp.www.ws.finance.Currencyrates;



public interface PwspNoCentrbankCurRatesSoap extends java.rmi.Remote {

    /**
     * Last daily rates quoted by norwegian central bank
     */
    public Currencyrates GetRatesXML() throws java.rmi.RemoteException;
}

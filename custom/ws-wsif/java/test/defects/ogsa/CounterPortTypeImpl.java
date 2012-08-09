/**
 * CounterPortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package defects.ogsa;

public class CounterPortTypeImpl {

    int value;
    	
	public CounterPortTypeImpl() {
		value = 0;
	}


    public int add(int x) {
    	value += x;
    	return value;
    }

    public int subtract(int x) {
    	value -= x;
    	return value;
    }
    
    public int getValue() {
    	return value;
    }
    
}

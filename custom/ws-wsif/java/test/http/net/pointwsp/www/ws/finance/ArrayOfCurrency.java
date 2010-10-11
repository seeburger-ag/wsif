/**
 * ArrayOfCurrency.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package http.net.pointwsp.www.ws.finance;

public class ArrayOfCurrency  implements java.io.Serializable {
    private http.net.pointwsp.www.ws.finance.Currency[] currency;

    public ArrayOfCurrency() {
    }

    public http.net.pointwsp.www.ws.finance.Currency[] getCurrency() {
        return currency;
    }

    public void setCurrency(http.net.pointwsp.www.ws.finance.Currency[] currency) {
        this.currency = currency;
    }

    public http.net.pointwsp.www.ws.finance.Currency getCurrency(int i) {
        return currency[i];
    }

    public void setCurrency(int i, http.net.pointwsp.www.ws.finance.Currency value) {
        this.currency[i] = value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ArrayOfCurrency)) return false;
        ArrayOfCurrency other = (ArrayOfCurrency) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((currency==null && other.getCurrency()==null) || 
             (currency!=null &&
              java.util.Arrays.equals(currency, other.getCurrency())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getCurrency() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getCurrency());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getCurrency(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ArrayOfCurrency.class);

    static {
        org.apache.axis.description.FieldDesc field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("currency");
        field.setXmlName(new javax.xml.namespace.QName("http/www.pointwsp.net/ws/finance", "currency"));
        field.setMinOccursIs0(true);
        typeDesc.addFieldDesc(field);
    };

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}

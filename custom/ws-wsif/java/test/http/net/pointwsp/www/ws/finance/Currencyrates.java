/**
 * Currencyrates.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package http.net.pointwsp.www.ws.finance;

public class Currencyrates  implements java.io.Serializable {
    private java.lang.String source;
    private java.lang.String basecurrency;
    private http.net.pointwsp.www.ws.finance.ArrayOfCurrency ratelist;

    public Currencyrates() {
    }

    public java.lang.String getSource() {
        return source;
    }

    public void setSource(java.lang.String source) {
        this.source = source;
    }

    public java.lang.String getBasecurrency() {
        return basecurrency;
    }

    public void setBasecurrency(java.lang.String basecurrency) {
        this.basecurrency = basecurrency;
    }

    public http.net.pointwsp.www.ws.finance.ArrayOfCurrency getRatelist() {
        return ratelist;
    }

    public void setRatelist(http.net.pointwsp.www.ws.finance.ArrayOfCurrency ratelist) {
        this.ratelist = ratelist;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Currencyrates)) return false;
        Currencyrates other = (Currencyrates) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((source==null && other.getSource()==null) || 
             (source!=null &&
              source.equals(other.getSource()))) &&
            ((basecurrency==null && other.getBasecurrency()==null) || 
             (basecurrency!=null &&
              basecurrency.equals(other.getBasecurrency()))) &&
            ((ratelist==null && other.getRatelist()==null) || 
             (ratelist!=null &&
              ratelist.equals(other.getRatelist())));
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
        if (getSource() != null) {
            _hashCode += getSource().hashCode();
        }
        if (getBasecurrency() != null) {
            _hashCode += getBasecurrency().hashCode();
        }
        if (getRatelist() != null) {
            _hashCode += getRatelist().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Currencyrates.class);

    static {
        org.apache.axis.description.FieldDesc field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("source");
        field.setXmlName(new javax.xml.namespace.QName("http/www.pointwsp.net/ws/finance", "source"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        field.setMinOccursIs0(true);
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("basecurrency");
        field.setXmlName(new javax.xml.namespace.QName("http/www.pointwsp.net/ws/finance", "basecurrency"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        field.setMinOccursIs0(true);
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("ratelist");
        field.setXmlName(new javax.xml.namespace.QName("http/www.pointwsp.net/ws/finance", "ratelist"));
        field.setXmlType(new javax.xml.namespace.QName("http/www.pointwsp.net/ws/finance", "ArrayOfCurrency"));
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

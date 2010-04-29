/**
 * SingleTag_ElemType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package interop.wsifserviceWrapped;

public class SingleTag_ElemType  implements java.io.Serializable {
    private interop.wsifserviceWrapped.SingleTag_Type singleTag;

    public SingleTag_ElemType() {
    }

    public interop.wsifserviceWrapped.SingleTag_Type getSingleTag() {
        return singleTag;
    }

    public void setSingleTag(interop.wsifserviceWrapped.SingleTag_Type singleTag) {
        this.singleTag = singleTag;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SingleTag_ElemType)) return false;
        SingleTag_ElemType other = (SingleTag_ElemType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((singleTag==null && other.getSingleTag()==null) || 
             (singleTag!=null &&
              singleTag.equals(other.getSingleTag())));
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
        if (getSingleTag() != null) {
            _hashCode += getSingleTag().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SingleTag_ElemType.class);

    static {
        org.apache.axis.description.FieldDesc field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("singleTag");
        field.setXmlName(new javax.xml.namespace.QName("http://soapinterop.org/", "SingleTag"));
        field.setXmlType(new javax.xml.namespace.QName("http://soapinterop.org/", "SingleTag"));
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

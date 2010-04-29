/**
 * ChildDocument.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package interop.wsifserviceWrapped;

public class ChildDocument  implements java.io.Serializable {
    private interop.wsifserviceWrapped.ArrayOfSimpleDocument childSimpleDoc;

    public ChildDocument() {
    }

    public interop.wsifserviceWrapped.ArrayOfSimpleDocument getChildSimpleDoc() {
        return childSimpleDoc;
    }

    public void setChildSimpleDoc(interop.wsifserviceWrapped.ArrayOfSimpleDocument childSimpleDoc) {
        this.childSimpleDoc = childSimpleDoc;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ChildDocument)) return false;
        ChildDocument other = (ChildDocument) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((childSimpleDoc==null && other.getChildSimpleDoc()==null) || 
             (childSimpleDoc!=null &&
              childSimpleDoc.equals(other.getChildSimpleDoc())));
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
        if (getChildSimpleDoc() != null) {
            _hashCode += getChildSimpleDoc().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ChildDocument.class);

    static {
        org.apache.axis.description.FieldDesc field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("childSimpleDoc");
        field.setXmlName(new javax.xml.namespace.QName("http://soapinterop.org/", "childSimpleDoc"));
        field.setXmlType(new javax.xml.namespace.QName("http://soapinterop.org/", "ArrayOfSimpleDocument"));
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

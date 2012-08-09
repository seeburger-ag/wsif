/**
 * ComplexDocument_Type.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package interop.wsifserviceWrapped;

public class ComplexDocument_Type  implements java.io.Serializable {
    private interop.wsifserviceWrapped.ArrayOfSimpleDocument simpleDoc;
    private interop.wsifserviceWrapped.ChildDocument child;
    private java.lang.String anAttribute;  // attribute

    public ComplexDocument_Type() {
    }

    public interop.wsifserviceWrapped.ArrayOfSimpleDocument getSimpleDoc() {
        return simpleDoc;
    }

    public void setSimpleDoc(interop.wsifserviceWrapped.ArrayOfSimpleDocument simpleDoc) {
        this.simpleDoc = simpleDoc;
    }

    public interop.wsifserviceWrapped.ChildDocument getChild() {
        return child;
    }

    public void setChild(interop.wsifserviceWrapped.ChildDocument child) {
        this.child = child;
    }

    public java.lang.String getAnAttribute() {
        return anAttribute;
    }

    public void setAnAttribute(java.lang.String anAttribute) {
        this.anAttribute = anAttribute;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof ComplexDocument_Type)) return false;
        ComplexDocument_Type other = (ComplexDocument_Type) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((simpleDoc==null && other.getSimpleDoc()==null) || 
             (simpleDoc!=null &&
              simpleDoc.equals(other.getSimpleDoc()))) &&
            ((child==null && other.getChild()==null) || 
             (child!=null &&
              child.equals(other.getChild()))) &&
            ((anAttribute==null && other.getAnAttribute()==null) || 
             (anAttribute!=null &&
              anAttribute.equals(other.getAnAttribute())));
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
        if (getSimpleDoc() != null) {
            _hashCode += getSimpleDoc().hashCode();
        }
        if (getChild() != null) {
            _hashCode += getChild().hashCode();
        }
        if (getAnAttribute() != null) {
            _hashCode += getAnAttribute().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ComplexDocument_Type.class);

    static {
        org.apache.axis.description.FieldDesc field = new org.apache.axis.description.AttributeDesc();
        field.setFieldName("anAttribute");
        field.setXmlName(new javax.xml.namespace.QName("", "AnAttribute"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("simpleDoc");
        field.setXmlName(new javax.xml.namespace.QName("http://soapinterop.org/", "simpleDoc"));
        field.setXmlType(new javax.xml.namespace.QName("http://soapinterop.org/", "ArrayOfSimpleDocument"));
        field.setMinOccursIs0(true);
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("child");
        field.setXmlName(new javax.xml.namespace.QName("http://soapinterop.org/", "child"));
        field.setXmlType(new javax.xml.namespace.QName("http://soapinterop.org/", "ChildDocument"));
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

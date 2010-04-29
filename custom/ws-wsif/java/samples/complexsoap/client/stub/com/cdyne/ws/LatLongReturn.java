/**
 * LatLongReturn.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis WSDL2Java emitter.
 */

package complexsoap.client.stub.com.cdyne.ws;

public class LatLongReturn  implements java.io.Serializable {
    private boolean serviceError;
    private boolean addressError;
    private java.lang.String city;
    private java.lang.String stateAbbrev;
    private java.lang.String zipCode;
    private java.lang.String county;
    private java.math.BigDecimal fromLongitude;
    private java.math.BigDecimal fromLatitude;
    private java.math.BigDecimal toLongitude;
    private java.math.BigDecimal toLatitude;
    private java.math.BigDecimal avgLongitude;
    private java.math.BigDecimal avgLatitude;
    private java.lang.String CMSA;
    private java.lang.String PMSA;

    public LatLongReturn() {
    }

    public boolean isServiceError() {
        return serviceError;
    }

    public void setServiceError(boolean serviceError) {
        this.serviceError = serviceError;
    }

    public boolean isAddressError() {
        return addressError;
    }

    public void setAddressError(boolean addressError) {
        this.addressError = addressError;
    }

    public java.lang.String getCity() {
        return city;
    }

    public void setCity(java.lang.String city) {
        this.city = city;
    }

    public java.lang.String getStateAbbrev() {
        return stateAbbrev;
    }

    public void setStateAbbrev(java.lang.String stateAbbrev) {
        this.stateAbbrev = stateAbbrev;
    }

    public java.lang.String getZipCode() {
        return zipCode;
    }

    public void setZipCode(java.lang.String zipCode) {
        this.zipCode = zipCode;
    }

    public java.lang.String getCounty() {
        return county;
    }

    public void setCounty(java.lang.String county) {
        this.county = county;
    }

    public java.math.BigDecimal getFromLongitude() {
        return fromLongitude;
    }

    public void setFromLongitude(java.math.BigDecimal fromLongitude) {
        this.fromLongitude = fromLongitude;
    }

    public java.math.BigDecimal getFromLatitude() {
        return fromLatitude;
    }

    public void setFromLatitude(java.math.BigDecimal fromLatitude) {
        this.fromLatitude = fromLatitude;
    }

    public java.math.BigDecimal getToLongitude() {
        return toLongitude;
    }

    public void setToLongitude(java.math.BigDecimal toLongitude) {
        this.toLongitude = toLongitude;
    }

    public java.math.BigDecimal getToLatitude() {
        return toLatitude;
    }

    public void setToLatitude(java.math.BigDecimal toLatitude) {
        this.toLatitude = toLatitude;
    }

    public java.math.BigDecimal getAvgLongitude() {
        return avgLongitude;
    }

    public void setAvgLongitude(java.math.BigDecimal avgLongitude) {
        this.avgLongitude = avgLongitude;
    }

    public java.math.BigDecimal getAvgLatitude() {
        return avgLatitude;
    }

    public void setAvgLatitude(java.math.BigDecimal avgLatitude) {
        this.avgLatitude = avgLatitude;
    }

    public java.lang.String getCMSA() {
        return CMSA;
    }

    public void setCMSA(java.lang.String CMSA) {
        this.CMSA = CMSA;
    }

    public java.lang.String getPMSA() {
        return PMSA;
    }

    public void setPMSA(java.lang.String PMSA) {
        this.PMSA = PMSA;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof LatLongReturn)) return false;
        LatLongReturn other = (LatLongReturn) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            serviceError == other.isServiceError() &&
            addressError == other.isAddressError() &&
            ((city==null && other.getCity()==null) || 
             (city!=null &&
              city.equals(other.getCity()))) &&
            ((stateAbbrev==null && other.getStateAbbrev()==null) || 
             (stateAbbrev!=null &&
              stateAbbrev.equals(other.getStateAbbrev()))) &&
            ((zipCode==null && other.getZipCode()==null) || 
             (zipCode!=null &&
              zipCode.equals(other.getZipCode()))) &&
            ((county==null && other.getCounty()==null) || 
             (county!=null &&
              county.equals(other.getCounty()))) &&
            ((fromLongitude==null && other.getFromLongitude()==null) || 
             (fromLongitude!=null &&
              fromLongitude.equals(other.getFromLongitude()))) &&
            ((fromLatitude==null && other.getFromLatitude()==null) || 
             (fromLatitude!=null &&
              fromLatitude.equals(other.getFromLatitude()))) &&
            ((toLongitude==null && other.getToLongitude()==null) || 
             (toLongitude!=null &&
              toLongitude.equals(other.getToLongitude()))) &&
            ((toLatitude==null && other.getToLatitude()==null) || 
             (toLatitude!=null &&
              toLatitude.equals(other.getToLatitude()))) &&
            ((avgLongitude==null && other.getAvgLongitude()==null) || 
             (avgLongitude!=null &&
              avgLongitude.equals(other.getAvgLongitude()))) &&
            ((avgLatitude==null && other.getAvgLatitude()==null) || 
             (avgLatitude!=null &&
              avgLatitude.equals(other.getAvgLatitude()))) &&
            ((CMSA==null && other.getCMSA()==null) || 
             (CMSA!=null &&
              CMSA.equals(other.getCMSA()))) &&
            ((PMSA==null && other.getPMSA()==null) || 
             (PMSA!=null &&
              PMSA.equals(other.getPMSA())));
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
        _hashCode += new Boolean(isServiceError()).hashCode();
        _hashCode += new Boolean(isAddressError()).hashCode();
        if (getCity() != null) {
            _hashCode += getCity().hashCode();
        }
        if (getStateAbbrev() != null) {
            _hashCode += getStateAbbrev().hashCode();
        }
        if (getZipCode() != null) {
            _hashCode += getZipCode().hashCode();
        }
        if (getCounty() != null) {
            _hashCode += getCounty().hashCode();
        }
        if (getFromLongitude() != null) {
            _hashCode += getFromLongitude().hashCode();
        }
        if (getFromLatitude() != null) {
            _hashCode += getFromLatitude().hashCode();
        }
        if (getToLongitude() != null) {
            _hashCode += getToLongitude().hashCode();
        }
        if (getToLatitude() != null) {
            _hashCode += getToLatitude().hashCode();
        }
        if (getAvgLongitude() != null) {
            _hashCode += getAvgLongitude().hashCode();
        }
        if (getAvgLatitude() != null) {
            _hashCode += getAvgLatitude().hashCode();
        }
        if (getCMSA() != null) {
            _hashCode += getCMSA().hashCode();
        }
        if (getPMSA() != null) {
            _hashCode += getPMSA().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(LatLongReturn.class);

    static {
        org.apache.axis.description.FieldDesc field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("serviceError");
        field.setXmlName(new javax.xml.namespace.QName("http://ws.cdyne.com", "ServiceError"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("addressError");
        field.setXmlName(new javax.xml.namespace.QName("http://ws.cdyne.com", "AddressError"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("city");
        field.setXmlName(new javax.xml.namespace.QName("http://ws.cdyne.com", "City"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        field.setMinOccursIs0(true);
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("stateAbbrev");
        field.setXmlName(new javax.xml.namespace.QName("http://ws.cdyne.com", "StateAbbrev"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        field.setMinOccursIs0(true);
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("zipCode");
        field.setXmlName(new javax.xml.namespace.QName("http://ws.cdyne.com", "ZipCode"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        field.setMinOccursIs0(true);
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("county");
        field.setXmlName(new javax.xml.namespace.QName("http://ws.cdyne.com", "County"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        field.setMinOccursIs0(true);
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("fromLongitude");
        field.setXmlName(new javax.xml.namespace.QName("http://ws.cdyne.com", "FromLongitude"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("fromLatitude");
        field.setXmlName(new javax.xml.namespace.QName("http://ws.cdyne.com", "FromLatitude"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("toLongitude");
        field.setXmlName(new javax.xml.namespace.QName("http://ws.cdyne.com", "ToLongitude"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("toLatitude");
        field.setXmlName(new javax.xml.namespace.QName("http://ws.cdyne.com", "ToLatitude"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("avgLongitude");
        field.setXmlName(new javax.xml.namespace.QName("http://ws.cdyne.com", "AvgLongitude"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("avgLatitude");
        field.setXmlName(new javax.xml.namespace.QName("http://ws.cdyne.com", "AvgLatitude"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "decimal"));
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("CMSA");
        field.setXmlName(new javax.xml.namespace.QName("http://ws.cdyne.com", "CMSA"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        field.setMinOccursIs0(true);
        typeDesc.addFieldDesc(field);
        field = new org.apache.axis.description.ElementDesc();
        field.setFieldName("PMSA");
        field.setXmlName(new javax.xml.namespace.QName("http://ws.cdyne.com", "PMSA"));
        field.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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

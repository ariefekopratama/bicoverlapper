/**
 * BiostrucScriptStep_featureDoType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package gov.nih.nlm.ncbi.www.soap.eutils.efetch;

public class BiostrucScriptStep_featureDoType  implements java.io.Serializable {
    private gov.nih.nlm.ncbi.www.soap.eutils.efetch.OtherFeatureType[] otherFeature;

    public BiostrucScriptStep_featureDoType() {
    }

    public BiostrucScriptStep_featureDoType(
           gov.nih.nlm.ncbi.www.soap.eutils.efetch.OtherFeatureType[] otherFeature) {
           this.otherFeature = otherFeature;
    }


    /**
     * Gets the otherFeature value for this BiostrucScriptStep_featureDoType.
     * 
     * @return otherFeature
     */
    public gov.nih.nlm.ncbi.www.soap.eutils.efetch.OtherFeatureType[] getOtherFeature() {
        return otherFeature;
    }


    /**
     * Sets the otherFeature value for this BiostrucScriptStep_featureDoType.
     * 
     * @param otherFeature
     */
    public void setOtherFeature(gov.nih.nlm.ncbi.www.soap.eutils.efetch.OtherFeatureType[] otherFeature) {
        this.otherFeature = otherFeature;
    }

    public gov.nih.nlm.ncbi.www.soap.eutils.efetch.OtherFeatureType getOtherFeature(int i) {
        return this.otherFeature[i];
    }

    public void setOtherFeature(int i, gov.nih.nlm.ncbi.www.soap.eutils.efetch.OtherFeatureType _value) {
        this.otherFeature[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof BiostrucScriptStep_featureDoType)) return false;
        BiostrucScriptStep_featureDoType other = (BiostrucScriptStep_featureDoType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.otherFeature==null && other.getOtherFeature()==null) || 
             (this.otherFeature!=null &&
              java.util.Arrays.equals(this.otherFeature, other.getOtherFeature())));
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
        if (getOtherFeature() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getOtherFeature());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getOtherFeature(), i);
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
        new org.apache.axis.description.TypeDesc(BiostrucScriptStep_featureDoType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Biostruc-script-step_feature-doType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("otherFeature");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Other-feature"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Other-featureType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
        typeDesc.addFieldDesc(elemField);
    }

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

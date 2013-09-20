/**
 * RegionSimilarity_similarityType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package gov.nih.nlm.ncbi.www.soap.eutils.efetch;

public class RegionSimilarity_similarityType  implements java.io.Serializable {
    private gov.nih.nlm.ncbi.www.soap.eutils.efetch.RegionPntrsType[] regionPntrs;

    public RegionSimilarity_similarityType() {
    }

    public RegionSimilarity_similarityType(
           gov.nih.nlm.ncbi.www.soap.eutils.efetch.RegionPntrsType[] regionPntrs) {
           this.regionPntrs = regionPntrs;
    }


    /**
     * Gets the regionPntrs value for this RegionSimilarity_similarityType.
     * 
     * @return regionPntrs
     */
    public gov.nih.nlm.ncbi.www.soap.eutils.efetch.RegionPntrsType[] getRegionPntrs() {
        return regionPntrs;
    }


    /**
     * Sets the regionPntrs value for this RegionSimilarity_similarityType.
     * 
     * @param regionPntrs
     */
    public void setRegionPntrs(gov.nih.nlm.ncbi.www.soap.eutils.efetch.RegionPntrsType[] regionPntrs) {
        this.regionPntrs = regionPntrs;
    }

    public gov.nih.nlm.ncbi.www.soap.eutils.efetch.RegionPntrsType getRegionPntrs(int i) {
        return this.regionPntrs[i];
    }

    public void setRegionPntrs(int i, gov.nih.nlm.ncbi.www.soap.eutils.efetch.RegionPntrsType _value) {
        this.regionPntrs[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RegionSimilarity_similarityType)) return false;
        RegionSimilarity_similarityType other = (RegionSimilarity_similarityType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.regionPntrs==null && other.getRegionPntrs()==null) || 
             (this.regionPntrs!=null &&
              java.util.Arrays.equals(this.regionPntrs, other.getRegionPntrs())));
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
        if (getRegionPntrs() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRegionPntrs());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getRegionPntrs(), i);
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
        new org.apache.axis.description.TypeDesc(RegionSimilarity_similarityType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Region-similarity_similarityType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("regionPntrs");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Region-pntrs"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Region-pntrsType"));
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

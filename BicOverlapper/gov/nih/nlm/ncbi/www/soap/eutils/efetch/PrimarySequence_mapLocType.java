/**
 * PrimarySequence_mapLocType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package gov.nih.nlm.ncbi.www.soap.eutils.efetch;

public class PrimarySequence_mapLocType  implements java.io.Serializable {
    private gov.nih.nlm.ncbi.www.soap.eutils.efetch.MapLocType[] mapLoc;

    public PrimarySequence_mapLocType() {
    }

    public PrimarySequence_mapLocType(
           gov.nih.nlm.ncbi.www.soap.eutils.efetch.MapLocType[] mapLoc) {
           this.mapLoc = mapLoc;
    }


    /**
     * Gets the mapLoc value for this PrimarySequence_mapLocType.
     * 
     * @return mapLoc
     */
    public gov.nih.nlm.ncbi.www.soap.eutils.efetch.MapLocType[] getMapLoc() {
        return mapLoc;
    }


    /**
     * Sets the mapLoc value for this PrimarySequence_mapLocType.
     * 
     * @param mapLoc
     */
    public void setMapLoc(gov.nih.nlm.ncbi.www.soap.eutils.efetch.MapLocType[] mapLoc) {
        this.mapLoc = mapLoc;
    }

    public gov.nih.nlm.ncbi.www.soap.eutils.efetch.MapLocType getMapLoc(int i) {
        return this.mapLoc[i];
    }

    public void setMapLoc(int i, gov.nih.nlm.ncbi.www.soap.eutils.efetch.MapLocType _value) {
        this.mapLoc[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof PrimarySequence_mapLocType)) return false;
        PrimarySequence_mapLocType other = (PrimarySequence_mapLocType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.mapLoc==null && other.getMapLoc()==null) || 
             (this.mapLoc!=null &&
              java.util.Arrays.equals(this.mapLoc, other.getMapLoc())));
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
        if (getMapLoc() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getMapLoc());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getMapLoc(), i);
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
        new org.apache.axis.description.TypeDesc(PrimarySequence_mapLocType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "PrimarySequence_mapLocType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("mapLoc");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "MapLoc"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "MapLocType"));
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

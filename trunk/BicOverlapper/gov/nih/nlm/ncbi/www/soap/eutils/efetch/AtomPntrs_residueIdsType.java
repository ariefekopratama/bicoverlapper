/**
 * AtomPntrs_residueIdsType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package gov.nih.nlm.ncbi.www.soap.eutils.efetch;

public class AtomPntrs_residueIdsType  implements java.io.Serializable {
    private java.lang.String[] residueId;

    public AtomPntrs_residueIdsType() {
    }

    public AtomPntrs_residueIdsType(
           java.lang.String[] residueId) {
           this.residueId = residueId;
    }


    /**
     * Gets the residueId value for this AtomPntrs_residueIdsType.
     * 
     * @return residueId
     */
    public java.lang.String[] getResidueId() {
        return residueId;
    }


    /**
     * Sets the residueId value for this AtomPntrs_residueIdsType.
     * 
     * @param residueId
     */
    public void setResidueId(java.lang.String[] residueId) {
        this.residueId = residueId;
    }

    public java.lang.String getResidueId(int i) {
        return this.residueId[i];
    }

    public void setResidueId(int i, java.lang.String _value) {
        this.residueId[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof AtomPntrs_residueIdsType)) return false;
        AtomPntrs_residueIdsType other = (AtomPntrs_residueIdsType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.residueId==null && other.getResidueId()==null) || 
             (this.residueId!=null &&
              java.util.Arrays.equals(this.residueId, other.getResidueId())));
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
        if (getResidueId() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getResidueId());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getResidueId(), i);
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
        new org.apache.axis.description.TypeDesc(AtomPntrs_residueIdsType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Atom-pntrs_residue-idsType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("residueId");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Residue-id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Residue-id"));
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

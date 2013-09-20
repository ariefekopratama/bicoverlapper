/**
 * DeleteCatalogRecordType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package gov.nih.nlm.ncbi.www.soap.eutils.efetch;

public class DeleteCatalogRecordType  implements java.io.Serializable {
    private java.lang.String[] nlmUniqueID;

    public DeleteCatalogRecordType() {
    }

    public DeleteCatalogRecordType(
           java.lang.String[] nlmUniqueID) {
           this.nlmUniqueID = nlmUniqueID;
    }


    /**
     * Gets the nlmUniqueID value for this DeleteCatalogRecordType.
     * 
     * @return nlmUniqueID
     */
    public java.lang.String[] getNlmUniqueID() {
        return nlmUniqueID;
    }


    /**
     * Sets the nlmUniqueID value for this DeleteCatalogRecordType.
     * 
     * @param nlmUniqueID
     */
    public void setNlmUniqueID(java.lang.String[] nlmUniqueID) {
        this.nlmUniqueID = nlmUniqueID;
    }

    public java.lang.String getNlmUniqueID(int i) {
        return this.nlmUniqueID[i];
    }

    public void setNlmUniqueID(int i, java.lang.String _value) {
        this.nlmUniqueID[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DeleteCatalogRecordType)) return false;
        DeleteCatalogRecordType other = (DeleteCatalogRecordType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.nlmUniqueID==null && other.getNlmUniqueID()==null) || 
             (this.nlmUniqueID!=null &&
              java.util.Arrays.equals(this.nlmUniqueID, other.getNlmUniqueID())));
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
        if (getNlmUniqueID() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getNlmUniqueID());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getNlmUniqueID(), i);
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
        new org.apache.axis.description.TypeDesc(DeleteCatalogRecordType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "DeleteCatalogRecordType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nlmUniqueID");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "NlmUniqueID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "NlmUniqueID"));
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

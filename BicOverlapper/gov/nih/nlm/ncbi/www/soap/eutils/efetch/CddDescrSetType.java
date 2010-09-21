/**
 * CddDescrSetType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package gov.nih.nlm.ncbi.www.soap.eutils.efetch;

public class CddDescrSetType  implements java.io.Serializable {
    private gov.nih.nlm.ncbi.www.soap.eutils.efetch.CddDescrType[] cddDescr;

    public CddDescrSetType() {
    }

    public CddDescrSetType(
           gov.nih.nlm.ncbi.www.soap.eutils.efetch.CddDescrType[] cddDescr) {
           this.cddDescr = cddDescr;
    }


    /**
     * Gets the cddDescr value for this CddDescrSetType.
     * 
     * @return cddDescr
     */
    public gov.nih.nlm.ncbi.www.soap.eutils.efetch.CddDescrType[] getCddDescr() {
        return cddDescr;
    }


    /**
     * Sets the cddDescr value for this CddDescrSetType.
     * 
     * @param cddDescr
     */
    public void setCddDescr(gov.nih.nlm.ncbi.www.soap.eutils.efetch.CddDescrType[] cddDescr) {
        this.cddDescr = cddDescr;
    }

    public gov.nih.nlm.ncbi.www.soap.eutils.efetch.CddDescrType getCddDescr(int i) {
        return this.cddDescr[i];
    }

    public void setCddDescr(int i, gov.nih.nlm.ncbi.www.soap.eutils.efetch.CddDescrType _value) {
        this.cddDescr[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CddDescrSetType)) return false;
        CddDescrSetType other = (CddDescrSetType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.cddDescr==null && other.getCddDescr()==null) || 
             (this.cddDescr!=null &&
              java.util.Arrays.equals(this.cddDescr, other.getCddDescr())));
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
        if (getCddDescr() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getCddDescr());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getCddDescr(), i);
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
        new org.apache.axis.description.TypeDesc(CddDescrSetType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Cdd-descr-setType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("cddDescr");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Cdd-descr"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Cdd-descrType"));
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

/**
 * Taxon1Resp_getproptypesType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package gov.nih.nlm.ncbi.www.soap.eutils.efetch;

public class Taxon1Resp_getproptypesType  implements java.io.Serializable {
    private gov.nih.nlm.ncbi.www.soap.eutils.efetch.Taxon1InfoType[] taxon1Info;

    public Taxon1Resp_getproptypesType() {
    }

    public Taxon1Resp_getproptypesType(
           gov.nih.nlm.ncbi.www.soap.eutils.efetch.Taxon1InfoType[] taxon1Info) {
           this.taxon1Info = taxon1Info;
    }


    /**
     * Gets the taxon1Info value for this Taxon1Resp_getproptypesType.
     * 
     * @return taxon1Info
     */
    public gov.nih.nlm.ncbi.www.soap.eutils.efetch.Taxon1InfoType[] getTaxon1Info() {
        return taxon1Info;
    }


    /**
     * Sets the taxon1Info value for this Taxon1Resp_getproptypesType.
     * 
     * @param taxon1Info
     */
    public void setTaxon1Info(gov.nih.nlm.ncbi.www.soap.eutils.efetch.Taxon1InfoType[] taxon1Info) {
        this.taxon1Info = taxon1Info;
    }

    public gov.nih.nlm.ncbi.www.soap.eutils.efetch.Taxon1InfoType getTaxon1Info(int i) {
        return this.taxon1Info[i];
    }

    public void setTaxon1Info(int i, gov.nih.nlm.ncbi.www.soap.eutils.efetch.Taxon1InfoType _value) {
        this.taxon1Info[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Taxon1Resp_getproptypesType)) return false;
        Taxon1Resp_getproptypesType other = (Taxon1Resp_getproptypesType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.taxon1Info==null && other.getTaxon1Info()==null) || 
             (this.taxon1Info!=null &&
              java.util.Arrays.equals(this.taxon1Info, other.getTaxon1Info())));
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
        if (getTaxon1Info() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getTaxon1Info());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTaxon1Info(), i);
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
        new org.apache.axis.description.TypeDesc(Taxon1Resp_getproptypesType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Taxon1-resp_getproptypesType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("taxon1Info");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Taxon1-info"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Taxon1-infoType"));
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

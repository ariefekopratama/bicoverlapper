/**
 * Txinit_synType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package gov.nih.nlm.ncbi.www.soap.eutils.efetch;

public class Txinit_synType  implements java.io.Serializable {
    private java.lang.String[] txinit_syn_E;

    public Txinit_synType() {
    }

    public Txinit_synType(
           java.lang.String[] txinit_syn_E) {
           this.txinit_syn_E = txinit_syn_E;
    }


    /**
     * Gets the txinit_syn_E value for this Txinit_synType.
     * 
     * @return txinit_syn_E
     */
    public java.lang.String[] getTxinit_syn_E() {
        return txinit_syn_E;
    }


    /**
     * Sets the txinit_syn_E value for this Txinit_synType.
     * 
     * @param txinit_syn_E
     */
    public void setTxinit_syn_E(java.lang.String[] txinit_syn_E) {
        this.txinit_syn_E = txinit_syn_E;
    }

    public java.lang.String getTxinit_syn_E(int i) {
        return this.txinit_syn_E[i];
    }

    public void setTxinit_syn_E(int i, java.lang.String _value) {
        this.txinit_syn_E[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Txinit_synType)) return false;
        Txinit_synType other = (Txinit_synType) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.txinit_syn_E==null && other.getTxinit_syn_E()==null) || 
             (this.txinit_syn_E!=null &&
              java.util.Arrays.equals(this.txinit_syn_E, other.getTxinit_syn_E())));
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
        if (getTxinit_syn_E() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getTxinit_syn_E());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTxinit_syn_E(), i);
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
        new org.apache.axis.description.TypeDesc(Txinit_synType.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Txinit_synType"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("txinit_syn_E");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Txinit_syn_E"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Txinit_syn_E"));
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

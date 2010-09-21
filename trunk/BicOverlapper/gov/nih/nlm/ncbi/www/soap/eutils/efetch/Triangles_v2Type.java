/**
 * Triangles_v2Type.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package gov.nih.nlm.ncbi.www.soap.eutils.efetch;

public class Triangles_v2Type  implements java.io.Serializable {
    private java.lang.String[] triangles_v2_E;

    public Triangles_v2Type() {
    }

    public Triangles_v2Type(
           java.lang.String[] triangles_v2_E) {
           this.triangles_v2_E = triangles_v2_E;
    }


    /**
     * Gets the triangles_v2_E value for this Triangles_v2Type.
     * 
     * @return triangles_v2_E
     */
    public java.lang.String[] getTriangles_v2_E() {
        return triangles_v2_E;
    }


    /**
     * Sets the triangles_v2_E value for this Triangles_v2Type.
     * 
     * @param triangles_v2_E
     */
    public void setTriangles_v2_E(java.lang.String[] triangles_v2_E) {
        this.triangles_v2_E = triangles_v2_E;
    }

    public java.lang.String getTriangles_v2_E(int i) {
        return this.triangles_v2_E[i];
    }

    public void setTriangles_v2_E(int i, java.lang.String _value) {
        this.triangles_v2_E[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Triangles_v2Type)) return false;
        Triangles_v2Type other = (Triangles_v2Type) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.triangles_v2_E==null && other.getTriangles_v2_E()==null) || 
             (this.triangles_v2_E!=null &&
              java.util.Arrays.equals(this.triangles_v2_E, other.getTriangles_v2_E())));
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
        if (getTriangles_v2_E() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getTriangles_v2_E());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTriangles_v2_E(), i);
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
        new org.apache.axis.description.TypeDesc(Triangles_v2Type.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Triangles_v2Type"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("triangles_v2_E");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Triangles_v2_E"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.ncbi.nlm.nih.gov/soap/eutils/efetch", "Triangles_v2_E"));
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

package trb.xml;

/**
 * A key, value pair defined in the Element tag.
 * 
 * @author tomrbryn
 */
public class XMLAttribute {
    /** The attribute key */
    public String key;
    /** The attribute value */
    public String value;

    /** Only allowed inside XMLDocument */
    public XMLAttribute(String k, String v) {
        this.key = k;
        this.value = v;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }
}

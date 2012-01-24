package trb.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An XMLElement represent a block of data in the xml document enclosed by a
 * tag pair.
 * 
 * @author tomrbryn
 */
public class XMLElement implements Iterable<XMLElement> {

    /** The wrapped DOM node */
    public Node node;

    /** The tag name */
    public String name = "";
    /** The text enclosed by the start and end tag */
    public String text = "";
    /** The list of children */
    public List<XMLElement> children = new ArrayList();
    /** The map of attributes */
    private Map<String, XMLAttribute> attributesByKey = new LinkedHashMap();

    public XMLElement() {
        
    }

    public XMLElement(String content) throws Exception {
        init(new ByteArrayInputStream(content.getBytes("UTF-8")));
    }

    public XMLElement(InputStream in) throws Exception {
        init(in);
    }

    private void init(InputStream in) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(in);

        // The root of the tree is a #document
        parse(document);
    }

    public XMLElement(Node n) {
        parse(n);
    }

    /**
     * Creates and adds a child element with the specified name.
     */
    public XMLElement createChild(String childName) {
        XMLElement e = create(childName);
        children.add(e);
        return e;
    }

    /**
     * Creates and adds a child element with the specified name and content.
     */
    public XMLElement createChild(String childName, String childText) {
        XMLElement e = create(childName, childText);
        children.add(e);
        return e;
    }

    /**
     * Creates and adds a child element with the specified name and content.
     */
    public XMLElement createChild(String childName, String childText, String childAttributes) {
        XMLElement e = create(childName, childText, childAttributes);
        children.add(e);
        return e;
    }

    private void parse(Node n) {
        this.node = n;
        name = n.getNodeName();

        // add attributes
        boolean binary = false;
        NamedNodeMap map = n.getAttributes();
        if (map != null) {
            for (int i = 0; i < map.getLength(); i++) {
                Node attrNode = map.item(i);
                if ("binary".equals(attrNode.getNodeName()) && "true".equals(attrNode.getNodeValue())) {
                    binary = true;
                } else {
                    XMLAttribute attr = new XMLAttribute(attrNode.getNodeName(), attrNode.getNodeValue());
                    addAttribute(attr);
                    attributesByKey.put(attr.key, attr);
                }
            }
        }

        // add children. this will recurevly build the tree.
        NodeList nodeChildren = n.getChildNodes();
        for (int i = 0; i < nodeChildren.getLength(); i++) {
            Node nodeChild = nodeChildren.item(i);
            if ("#text".equals(nodeChild.getNodeName())
                    || "#cdata-section".equals(nodeChild.getNodeName())) {
                text = nodeChild.getNodeValue().trim();
                if (binary) {
                    text = XMLElementWriter.fromBinary(text);
                }
            } else if (nodeChild.getNodeName().equals("#comment")) {
                // ignore comments
            } else {
                children.add(new XMLElement(nodeChild));
            }
        }
    }

    public void addAttribute(XMLAttribute newAttribute) {
        attributesByKey.put(newAttribute.key, newAttribute);
    }

    public Collection<XMLAttribute> attributes() {
        return attributesByKey.values();
    }

    /** Gets the attribute by key */
    public XMLAttribute attribute(String key) {
        return attributesByKey.get(key);
    }

    public String attributeValue(String key) {
        return attributesByKey.containsKey(key) ? attributesByKey.get(key).value : null;
    }

    /** Gets the number of children */
    public int children() {
        return children.size();
    }

    /** Gets child at the specified index */
    public XMLElement child(int idx) {
        return children.get(idx);
    }

    public List<XMLElement> getChildrenWithName(String name) {
        List<XMLElement> result = new ArrayList();
        for (XMLElement child : children) {
            if (name.equals(child.name)) {
                result.add(child);
            }
        }

        return result;
    }

    public XMLElement getFirstChildWithName(String name) {
        for (XMLElement child : children) {
            if (name.equals(child.name)) {
                return child;
            }
        }

        return null;
    }

    public String getTextFromFirstChildWithName(String name) {
        XMLElement e = getFirstChildWithName(name);
        return e == null ? null : e.text;
    }

    @Override
    public Iterator<XMLElement> iterator() {
        return new ArrayList(children).iterator();
    }

    /** Gets a String containing all the content of the XMLElement. */
    @Override
    public String toString() {
        StringWriter stringWriter = new StringWriter();
        XMLElementWriter.write(new PrintWriter(stringWriter), this);
        return stringWriter.toString();
    }

    /**
     * Creates a child element with the specified name.
     */
    public static XMLElement create(String childName) {
        XMLElement e = new XMLElement();
        e.name = childName;
        return e;
    }

    /**
     * Creates a child element with the specified name and content.
     */
    public static XMLElement create(String childName, String childText) {
        XMLElement e = create(childName);
        e.text = childText;
        return e;
    }

    /**
     * Creates and adds a child element with the specified name and content.
     */
    public static XMLElement create(String childName, String childText, String childAttributes) {
        XMLElement e = create(childName, childText);
        for (String keyValue : childAttributes.split(" ")) {
            String[] keyValueArray = keyValue.split("=");
            if (keyValueArray.length == 2) {
                e.addAttribute(new XMLAttribute(keyValueArray[0], keyValueArray[1]));
            } else {
                System.err.println("Failed to parse key value pair: " + keyValue);
            }
        }
        return e;
    }

    public static XMLElement createFromName(String name) {
        XMLElement e = new XMLElement();
        e.name = name;
        return e;
    }

    public static XMLElement createFromContent(String content) {
        try {
            XMLElement contentElem = new XMLElement("<content>" + XMLUtils.escape(content) + "</content>");
            return contentElem.child(0);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println(new XMLElement("<property>1</property>").toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.exit(0);
    }
}

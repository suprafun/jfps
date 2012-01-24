/*
 * HALDEN VR PLATFORM
 *
 * RADIATION MODULE
 *
 * $RCSfile: $
 *
 * Author :
 * Date   :
 * Version: $Revision: $ ($Date: $)
 *
 * (c) 2000-2011 Halden Virtual Reality Centre <http://www.ife.no/vr/>,
 * Institutt for energiteknikk. All rights reserved.
 *
 * This code is the property of Halden VR Centre <vr-info@hrp.no> and may
 * only be used in accordance with the terms of the license agreement
 * granted.
 */

package trb.fps.editor;

import java.util.ArrayList;
import java.util.List;
import trb.fps.property.Property;
import trb.fps.property.PropertyOwner;
import trb.xml.XMLAttribute;
import trb.xml.XMLElement;

/**
 *
 * @author tomrbryn
 */
public class IO {

    public static void writeLevel(XMLElement parent, List<? extends PropertyOwner> owners) {
        for (PropertyOwner owner : owners) {
            writeOwner(parent, owner);
        }
    }
    public static void writeOwner(XMLElement parent, PropertyOwner owner) {
        XMLElement ownerElem = parent.createChild("object");
        ownerElem.addAttribute(new XMLAttribute("type", owner.getClass().getName()));
        for (Property p : owner) {
            writeProperty(ownerElem, p);
        }
    }

    public static void writeProperty(XMLElement parent, Property property) {
        XMLElement propertyElem = parent.createChild("property", "" + property.get());
        propertyElem.addAttribute(new XMLAttribute("name", property.getName()));
    }

    public static List<PropertyOwner> readLevel(XMLElement levelElem) {
        List<PropertyOwner> list = new ArrayList();
        for (XMLElement objectElem : levelElem.getChildrenWithName("object")) {
            try {
                list.add(readObject(objectElem));
            } catch (Exception ex) {
                System.err.println("Failed to read object: \n" + objectElem);
                ex.printStackTrace();
            }
        }
        return list;
    }

    public static PropertyOwner readObject(XMLElement objectElem) throws Exception {
        PropertyOwner owner = (PropertyOwner) Class.forName(objectElem.attributeValue("type")).newInstance();
        for (XMLElement propertyElem : objectElem.getChildrenWithName("property")) {
            Property p = owner.getProperty(propertyElem.attributeValue("name"));
            if (p != null) {
                p.set(parse(p.getType(), propertyElem.text));
            }
        }
        return owner;
    }

    public static Object parse(Class type, String text) {
        if (String.class.equals(type)) {
            return text;
        } else if (Float.class.equals(type)) {
            return Float.parseFloat(text);
        } else if (Integer.class.equals(type)) {
            return Integer.parseInt(text);
        } else if (Double.class.equals(type)) {
            return Double.parseDouble(text);
        } else if (Long.class.equals(type)) {
            return Long.parseLong(text);
        }

        System.err.println(IO.class.getSimpleName() + " failed to parse \"" + text + "\" of type " + type);
        return null;
    }

    public static void main(String[] args) {
        List<PropertyOwner> boxPropsList = new ArrayList();
        boxPropsList.add(BoxProps.fromMinMax("abc", 0, 0, 0, 10, 11, 12));
        boxPropsList.add(BoxProps.fromMinMax("abc", -1, -1, -1, 1, 1, 1));
        XMLElement root = XMLElement.createFromName("boxes");
        writeLevel(root, boxPropsList);
        System.out.println(root.toString());

        print(boxPropsList);
        boxPropsList = readLevel(root);
        print(boxPropsList);
    }

    public static void print(List<? extends PropertyOwner> owners) {
        for (PropertyOwner owner : owners) {
            System.out.println(owner.getClass());
            for (Property p : owner) {
                System.out.println("  " + p.getName() + " = " + p.get());
            }
        }
    }
}

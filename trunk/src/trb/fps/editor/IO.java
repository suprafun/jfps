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

    public static void writeLevel(XMLElement parent, List<Entity> entities) {
        for (Entity entity : entities) {
            writeEntity(parent.createChild("entity"), entity);
        }
    }

    public static void writeEntity(XMLElement parent, Entity entity) {
        writeOwners(parent, entity.getComponents(Component.class));
    }

    public static void writeOwners(XMLElement parent, List<? extends PropertyOwner> owners) {
        for (PropertyOwner owner : owners) {
            writeOwner(parent, owner);
        }
    }

    public static void writeOwner(XMLElement parent, PropertyOwner owner) {
        XMLElement ownerElem = parent.createChild("component");
        ownerElem.addAttribute(new XMLAttribute("type", owner.getClass().getName()));
        for (Property p : owner) {
            writeProperty(ownerElem, p);
        }
    }

    public static void writeProperty(XMLElement parent, Property property) {
        XMLElement propertyElem = parent.createChild("property", "" + property.get());
        propertyElem.addAttribute(new XMLAttribute("name", property.getName()));
    }

    public static List<Entity> readLevel(XMLElement levelElem) {
        List<Entity> list = new ArrayList();
        for (XMLElement entityElem : levelElem.getChildrenWithName("entity")) {
            Entity entity = new Entity();
            list.add(entity);
            List<PropertyOwner> owners = readPropertyOwners(entityElem, "component");
            for (PropertyOwner owner : owners) {
                if (owner instanceof Component) {
                    entity.addComponent((Component) owner);
                }
            }
        }
        return list;
    }

    public static List<PropertyOwner> readPropertyOwners(XMLElement elem, String ownerTagName) {
        List<PropertyOwner> list = new ArrayList();
        for (XMLElement objectElem : elem.getChildrenWithName(ownerTagName)) {
            try {
                list.add(readPropertyOwner(objectElem));
            } catch (Exception ex) {
                System.err.println("Failed to read object: \n" + objectElem);
                ex.printStackTrace();
            }
        }
        return list;
    }

    public static PropertyOwner readPropertyOwner(XMLElement objectElem) throws Exception {
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
        List<Entity> entities = new ArrayList();
        entities.add(Box.fromMinMax("abc", 0, 0, 0, 10, 11, 12));
        entities.add(Box.fromMinMax("abc", -1, -1, -1, 1, 1, 1));
        XMLElement root = XMLElement.createFromName("level");
        writeLevel(root, entities);
        System.out.println(root.toString());

        printEntities(entities);
        entities = readLevel(root);
        printEntities(entities);
    }

    public static void printEntities(List<Entity> entities) {
        for (Entity e : entities) {
            printOwners(e.getComponents(Component.class));
        }
    }

    public static void printOwners(List<? extends PropertyOwner> owners) {
        for (PropertyOwner owner : owners) {
            System.out.println(owner.getClass());
            for (Property p : owner) {
                System.out.println("  " + p.getName() + " = " + p.get());
            }
        }
    }
}

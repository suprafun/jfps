package trb.fps.entity;

import java.awt.Color;
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

    public static XMLElement writeLevel(List<Entity> entities) {
        XMLElement parent = XMLElement.createFromName("level");
        for (Entity entity : entities) {
            writeEntity(parent.createChild("entity"), entity);
        }
        return parent;
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
        XMLElement propertyElem = parent.createChild("property", toText(property));
        propertyElem.addAttribute(new XMLAttribute("name", property.getName()));
    }

    public static String toText(Property p) {
        if (p.getType().isAssignableFrom(Color.class)) {
            Color color = (Color) p.get();
            return Integer.toHexString(color.getRGB());
        }
        return "" + p.get();
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
        } else if (Color.class.equals(type)) {
            return new Color((int) Long.parseLong(text, 16));
        }

        System.err.println(IO.class.getSimpleName() + " failed to parse \"" + text + "\" of type " + type);
        try {
            return type.newInstance();
        } catch (Exception ex) {
        }
        return null;
    }

    public static void main(String[] args) {
        List<Entity> entities = new ArrayList();
        entities.add(Box.fromMinMax("abc", 0, 0, 0, 10, 11, 12));
        entities.add(Box.fromMinMax("abc", -1, -1, -1, 1, 1, 1));
        XMLElement root = writeLevel(entities);
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

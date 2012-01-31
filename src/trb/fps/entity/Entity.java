package trb.fps.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Entity implements Iterable<Component> {

    private final List<Component> components = new CopyOnWriteArrayList();

    public void addComponent(Component comp) {
        components.add(comp);
        comp.entity = this;
    }

    /**
     * Adds a new component of the specified type
     * @param compClass the type of component to add
     */
    public <T extends Component> T addComponent(Class<T> compClass) {
        try {
            T c = compClass.newInstance();
            addComponent(c);
            return c;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public <T> List<T> getComponents(Class<T> type) {
        ArrayList<T> list = new ArrayList<T>();
        for (Component comp : this) {
            if (type.isAssignableFrom(comp.getClass())) {
                list.add((T) comp);
            }
        }

        return list;
    }

    public <T> T getComponent(Class<T> type) {
        List<T> list = getComponents(type);
        if (list.size() > 0) {
            return list.get(0);
        }

        return null;
    }

    public Iterator<Component> iterator() {
        return components.iterator();
    }

    public static Entity create(Class... components) {
        Entity obj = new Entity();
        for (Class c : components) {
            obj.addComponent(c);
        }
        return obj;
    }

    @Override
    public String toString() {
        Meta meta = getComponent(Meta.class);
        return meta != null ? meta.name.get() : getClass().getSimpleName();
    }
}

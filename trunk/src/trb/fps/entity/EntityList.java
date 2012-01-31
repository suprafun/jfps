package trb.fps.entity;

import java.util.ArrayList;
import java.util.List;

public class EntityList {

    public final List<Entity> list = new ArrayList();

    public EntityList() {

    }

    public EntityList(List<Entity> entities) {
        this.list.addAll(entities);
    }

    public void add(Entity entity) {
        list.add(entity);
    }

    public void remove(Entity entity) {
        list.remove(entity);
    }

    /**
     * Gets a list of all the components of the specified type.
     */
    public <T> List<T> getComponents(Class<T> type) {
        ArrayList<T> components = new ArrayList<T>();
        for (Entity e : list) {
            components.addAll(e.getComponents(type));
        }
        return components;
    }

    public List<Entity> getAll() {
        return new ArrayList(list);
    }
}

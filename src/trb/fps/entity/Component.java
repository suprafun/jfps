package trb.fps.entity;

import java.beans.PropertyChangeEvent;
import java.util.List;
import trb.fps.property.Property;
import trb.fps.property.PropertyOwner;

public class Component extends PropertyOwner {

    public Entity entity;

    public Entity getEntity() {
        return entity;
    }

    public <T> List<T> getComponents(Class<T> type) {
        return entity.getComponents(type);
    }

    public <T> T getComponent(Class<T> type) {
        return entity.getComponent(type);
    }

    @Override
    protected final void propertyChanged(PropertyChangeEvent e) {
        componentPropertyChanged(e);
        if (entity != null) {
            for (Component c : entity) {
                c.entityPropertyChanged(e);
            }
        }
    }

    protected void componentPropertyChanged(PropertyChangeEvent e) {

    }

    protected void entityPropertyChanged(PropertyChangeEvent e) {
        
    }

    public long getLastChange() {
        long lastChange = -1;
        for (Property p : this) {
            lastChange = Math.max(lastChange, p.lastChange);
        }
        return lastChange;
    }
}


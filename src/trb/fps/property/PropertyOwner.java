package trb.fps.property;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PropertyOwner implements Iterable<Property> {

    public List<Property> properties = new CopyOnWriteArrayList();
    public ListenerList<PropertyChangeListener> listeners = new ListenerList();
    private PropertyChangeListener listener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            propertyChanged(evt);
            for (PropertyChangeListener listener : listeners) {
                listener.propertyChange(evt);
            }
        }
    };

    public Property add(String name, Object initialValue) {
        return add(name, initialValue.getClass(), initialValue);
    }

    public Property add(String name, Class type, Object initialValue) {
        Property p = new Property(name, type, initialValue);
        p.listeners.addListener(listener);
        properties.add(p);
        return p;
    }

    public Property getProperty(String name) {
        for (Property p : properties) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    protected void propertyChanged(PropertyChangeEvent e) {
        
    }

    public Iterator<Property> iterator() {
        return properties.iterator();
    }
}

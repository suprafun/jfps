package trb.fps.property;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PropertyOwner {

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

    public Property add(String name, Class type, Object initialValue) {
        Property p = new Property(name, type, initialValue);
        p.listeners.addListener(listener);
        properties.add(p);
        return p;
    }

    protected void propertyChanged(PropertyChangeEvent e) {
        
    }
}

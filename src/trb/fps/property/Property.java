package trb.fps.property;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class Property<T> {

    public final String name;
    public final Class type;
    public T value;
    public final ListenerList<PropertyChangeListener> listeners = new ListenerList();

    public Property(String name, Class type, T initialValue) {
        this.name = name;
        this.type = type;
        this.value = initialValue;        
    }

    public String getName() {
        return name;
    }

    public T get() {
        return value;
    }

    public void set(T v) {
        T old = get();
        this.value = v;
        notifyListeners(new PropertyChangeEvent(this, getName(), old, get()));
    }

    void notifyListeners(PropertyChangeEvent e) {
        for (PropertyChangeListener listener : listeners) {
            listener.propertyChange(e);
        }
    }

    public Class getType() {
        return type;
    }
}

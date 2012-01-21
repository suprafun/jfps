package trb.fps.property;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

/**
 * A list of weak and normal listeners. Listeners can not be removed threw the
 * iterator. The order of the elements are undefined and may change.
 *
 * @author Tom-Robert Bryntesen
 * @version $Revision: 1.2 $
 */
public class ListenerList<T> implements Iterable<T> {

    private ArrayList<T> listeners;
    private WeakHashMap<T, String> weakListeners;
    private ArrayList<T> iteratorList;

    public synchronized void addListener(T listener) {
        if (listeners == null) {
            listeners = new ArrayList();
        }
        listeners.add(listener);
        iteratorList = null;
    }

    public synchronized void addWeakListener(T listener) {
        if (weakListeners == null) {
            weakListeners = new WeakHashMap();
        }
        weakListeners.put(listener, "");
        iteratorList = null;
    }

    public synchronized void removeListener(T listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
        if (weakListeners != null) {
            weakListeners.remove(listener);
        }
        iteratorList = null;
    }

    @Override
    public synchronized Iterator<T> iterator() {
        populateIteratorList();
        return iteratorList.iterator();
    }

    private void populateIteratorList() {
        if (iteratorList == null) {
            iteratorList = new ArrayList();
            if (listeners != null) {
                iteratorList.addAll(listeners);
            }
            if (weakListeners != null) {
                iteratorList.addAll(weakListeners.keySet());
            }
        }
    }

    synchronized List<T> getListeners() {
        populateIteratorList();
        return new ArrayList(iteratorList);
    }
}

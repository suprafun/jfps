package trb.fps.predict;

import java.util.ArrayList;
import java.util.List;

public class AnimatedState<T extends TimedState> {
    List<T> timedStateList = new ArrayList();

    public AnimatedState(T initialsState) {
        timedStateList.add(initialsState);
    }

    public void add(T state) {
        timedStateList.add(state);
    }

    public T interpolate(long time) {
        int i = 0;
        while (i < timedStateList.size()) {
            if (time < timedStateList.get(i).getTime()) {
                break;
            }
            i++;
        }
        int i1 = Math.max(i - 1, 0);
        int i2 = Math.min(i, timedStateList.size() - 1);
        T s1 = timedStateList.get(i1);
        T s2 = timedStateList.get(i2);
        if (s1.getTime() == s2.getTime()) {
            return (T) s2.setTime(time);
        }
        float t = (time - s1.getTime()) / (float) (s2.getTime() - s1.getTime());
        return (T) s1.interpolate(t, s2);
    }

    public boolean isEmpty() {
        return timedStateList.isEmpty();
    }

    public T newest() {
        return timedStateList.get(timedStateList.size() - 1);
    }

    public void removeOlderThan(long oldestTime) {
        while (timedStateList.get(0).getTime() < oldestTime) {
            timedStateList.remove(0);
        }
    }

    public int size() {
        return timedStateList.size();
    }

}

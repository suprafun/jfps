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

package trb.fps.predict.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class DelayedQueue<T> {

    List<TimedEntry> list = Collections.synchronizedList(new ArrayList());

    void add(final T object) {
        list.add(new TimedEntry(System.currentTimeMillis() + getDelay(), object));
    }

    T remove() {
        if (list.isEmpty()) {
            return null;
        }

        TimedEntry entry = list.get(0);
        if (System.currentTimeMillis() > entry.time) {
            return (T) list.remove(0).object;
        }
        return null;
    }

    private long getDelay() {
        return 300 + (int) (Math.random() * 100);
    }

    class TimedEntry {

        long time;
        Object object;

        TimedEntry(long time, Object object) {
            this.time = time;
            this.object = object;
        }
    }
}

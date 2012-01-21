package trb.fps.property;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;

public class MutateLabel {

    private final Property property;
    private JLabel label;
    private Point dragStart;
    private Number valueStart;

    private MouseAdapter listener = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            dragStart = e.getPoint();
            valueStart = (Number) property.get();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            Number newValue = mutate(valueStart, e.getPoint().x - dragStart.x);
            //property.set(valueStart, newValue, false);
            property.set(newValue);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Number newValue = mutate(valueStart, e.getPoint().x - dragStart.x);
            //setValue(newValue);
            //property.set(valueStart, newValue, true);
            property.set(newValue);
        }
    };

    public MutateLabel(Property p) {
        this.property = p;
        label = new JLabel(p.getName());
        label.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
        label.addMouseListener(listener);
        label.addMouseMotionListener(listener);
    }

    public JLabel get() {
        return label;
    }

    public Number mutate(Number valueStart, int amount) {
        Class type = property.getType();
        if (Double.class == type || double.class == type) {
            return pow(valueStart.doubleValue(), amount);
        } else if (Float.class == type || float.class == type) {
            return (float) pow(valueStart.doubleValue(), amount);
        } else if (Integer.class == type || int.class == type) {
            return valueStart.intValue() + amount;
        } else if (Long.class == type || long.class == type) {
            return valueStart.longValue() + amount;
        } else if (Short.class == type || short.class == type) {
            return (short) (valueStart.shortValue() + amount);
        } else if (Byte.class == type || byte.class == type) {
            return (byte) (valueStart.byteValue() + amount);
        }

        return valueStart;
    }

    public static double pow(double start, int dx) {
        double factor = Math.pow(1.005, Math.abs(dx));
        double sign = dx < 0 ? -1 : 1;
        return start + Math.max(1, Math.abs(start)) * (factor - 1) * sign;
    }
}

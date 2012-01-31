package trb.fps.property;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;

public class MutateLabel {

    private final Property property;
    private final JLabel label;
    private final Mutator mutator;
    private static final Mutator DEFAULT_MUTATOR = new NumberMutator();
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
            property.set(mutator.mutate(property.getType(), valueStart, e.getPoint().x - dragStart.x));
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            property.set(mutator.mutate(property.getType(), valueStart, e.getPoint().x - dragStart.x));
        }
    };

    public MutateLabel(Property p) {
        this(p, (Mutator) p.getUserData(Mutator.class));
    }

    public MutateLabel(Property p, Mutator mutator) {
        this.property = p;
        this.mutator = (mutator != null) ? mutator : DEFAULT_MUTATOR;
        label = new JLabel(p.getName());
        label.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
        label.addMouseListener(listener);
        label.addMouseMotionListener(listener);
    }

    public JLabel get() {
        return label;
    }

    public static void main(String[] args) {
        System.out.println("" + Number.class.isAssignableFrom(Float.class));
    }
}

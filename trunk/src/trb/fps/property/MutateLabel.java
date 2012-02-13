package trb.fps.property;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.JLabel;

public class MutateLabel {

    private final Property property;
    private final JLabel label;
    private final Mutator mutator;
    private static final Mutator DEFAULT_MUTATOR = new NumberMutator();
    private Point dragStart;
    private Object valueStart;

    private MouseAdapter listener = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            dragStart = e.getPoint();
            valueStart = property.get();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            property.set(mutator.mutate(property.getType(), valueStart, e.getPoint().x - dragStart.x));
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            property.set(mutator.mutate(property.getType(), valueStart, e.getPoint().x - dragStart.x));
        }

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			property.set(mutator.mutate(property.getType(), property.get(), -e.getWheelRotation()));
		}
    };

    public MutateLabel(Property p) {
        this(p, (Mutator) p.getUserData(Mutator.class));
    }

    public MutateLabel(Property p, Mutator mutator) {
        this.property = p;
		Mutator newMutator = mutator;
		if (newMutator == null) {
			if (Enum.class.isAssignableFrom(p.getType())) {
				newMutator = new EnumMutator();
				System.out.println("XXXXXXXXXXXXXX");
			} else {
				newMutator = DEFAULT_MUTATOR;
			}
		}
        this.mutator = newMutator;
        label = new JLabel(p.getName());
        label.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
        label.addMouseListener(listener);
        label.addMouseMotionListener(listener);
		label.addMouseWheelListener(listener);
    }

    public JLabel get() {
        return label;
    }

    public static void main(String[] args) {
        System.out.println("" + Number.class.isAssignableFrom(Float.class));
    }
}

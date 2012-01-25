package trb.fps.property;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;
import trb.fps.editor.Box;
import trb.fps.editor.Component;
import trb.fps.editor.Entity;
import trb.fps.editor.Meta;
import trb.fps.editor.Transform;

public class PropertyListPanel {

    JScrollPane scroll;

    public PropertyListPanel(final Entity entity) {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("fillx", "", ""));

        for (Component c : entity) {
            for (Property p : c) {
                panel.add(new MutateLabel(p).get(), "growx");
                panel.add(new NumberEditor(p).getComponent(), "growx, wrap");
            }
        }

        scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
                , JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    public JComponent get() {
        return scroll;
    }

    public static void main(String[] args) {
        Entity entity = Entity.create(Meta.class, Transform.class, Box.class);
//        entity.listeners.addListener(new PropertyChangeListener() {
//
//            public void propertyChange(PropertyChangeEvent evt) {
//                System.out.println(evt.getPropertyName() + " old=" + evt.getOldValue()
//                        + " new=" + evt.getNewValue());
//            }
//        });

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(250, 400);
        frame.add(new PropertyListPanel(entity).get());
        frame.setVisible(true);
    }
}

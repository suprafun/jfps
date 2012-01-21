package trb.fps.property;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;
import trb.fps.editor.BoxProps;

public class PropertyListPanel {

    JScrollPane scroll;

    public PropertyListPanel(final List<Property> props) {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("fillx", "", ""));

        for (Property p : props) {
            panel.add(new MutateLabel(p).get(), "growx");
            panel.add(new NumberEditor(p).getComponent(), "growx, wrap");
        }

        scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
                , JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    public JComponent get() {
        return scroll;
    }

    public static void main(String[] args) {
        BoxProps boxProps = new BoxProps();
        boxProps.listeners.addListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println(evt.getPropertyName() + " old=" + evt.getOldValue()
                        + " new=" + evt.getNewValue());
            }
        });

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(250, 400);
        frame.add(new PropertyListPanel(boxProps.properties).get());
        frame.setVisible(true);
    }
}

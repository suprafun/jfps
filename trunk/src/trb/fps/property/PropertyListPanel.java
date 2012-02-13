package trb.fps.property;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;
import trb.fps.entity.Box;
import trb.fps.entity.Component;
import trb.fps.entity.Entity;
import trb.fps.entity.Meta;
import trb.fps.entity.PointLightComp;
import trb.fps.entity.Transform;

public class PropertyListPanel {

    JScrollPane scroll;

    public PropertyListPanel(final Entity entity) {
        List<Property> properties = new ArrayList();
        for (Component c : entity) {
            for (Property p : c) {
                properties.add(p);
            }
        }
        init(properties);
    }

    public PropertyListPanel(List<Property> properties) {
        init(properties);
    }


    private void init(List<Property> properties) {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("fillx", "[][]", ""));

        for (Property p : properties) {
            panel.add(new MutateLabel(p).get(), "growx");
            panel.add(createEditor(p), "growx, wrap");
        }

        scroll = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
                , JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    private JComponent createEditor(final Property p) {
        if (p.getType().equals(Color.class)) {
            return new ColorEditor(p).getComponent();
        } else if (Enum.class.isAssignableFrom(p.getType())) {
			return new EnumEditor(p).getComponent();
		}
        return new NumberEditor(p).getComponent();
    }

    public JComponent get() {
        return scroll;
    }

    public static void main(String[] args) {
        Entity entity = Entity.create(Meta.class, Transform.class, Box.class, PointLightComp.class);
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(250, 400);
        frame.add(new PropertyListPanel(entity).get());
        frame.setVisible(true);
    }
}

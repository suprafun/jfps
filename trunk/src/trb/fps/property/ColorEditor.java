package trb.fps.property;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ColorEditor {

    private Property<Color> property;
    private JLabel label = new JLabel();
    private PropertyChangeListener propertyListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            updateUI();
        }
    };

    public ColorEditor(Property<Color> property) {
        this.property = property;
        property.listeners.addWeakListener(propertyListener);
        JTextField txt = new JTextField();

        label = new JLabel();
        label.setPreferredSize(txt.getPreferredSize());
        label.setBorder(txt.getBorder());
        label.setOpaque(true);
        label.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                Color oldColor = ColorEditor.this.property.get();
                Color newColor = JColorChooser.showDialog(label, "Choose_Color", oldColor);
                if (newColor != null) {
                    ColorEditor.this.property.set(newColor);
                    label.setBackground(newColor);
                }
            }
        });
        label.setBackground(property.get());
        updateUI();
    }

    private void updateUI() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                System.out.println("AAAAA " + property.get());
                label.setBackground(property.get());
            }
        });
    }

//    private void updateProperty() {
//        try {
//            Number number = NumberFormat.getInstance().parse(label.getText());
//            if (property.getType().equals(Float.class)) {
//                property.set(number.floatValue());
//            }
//        } catch (Exception ex) {
//            updateUI();
//        }
//    }

    public JComponent getComponent() {
        return label;
    }

    public static void main(String[] args) {
        PropertyListPanel.main(args);
    }
}

//    private final Property property;
//    private JLabel name;
//    JLabel value;
//
//    public ColorEditor(final PropertyEditorListener listener, final Property property) {
//        this.property = property;
//        property.addWeakListener(this);
//
//        name = PropertyEditorUI.createLabel(property.getDisplayName(), property.getType());
//
//        JTextField txt = new JTextField();
//        value = new JLabel();
//        value.setOpaque(true);
//        value.setBackground((Color) property.get());
//        value.setPreferredSize(txt.getPreferredSize());
//        value.setBorder(txt.getBorder());
//        value.addMouseListener(new MouseAdapter() {
//
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                Color oldColor = (Color) ColorEditor.this.property.get();
//                Color newColor = JColorChooser.showDialog(ColorEditor.this, PropertyI18N.get("Choose_Color"), oldColor);
//                if (newColor != null) {
//                    listener.requestPropertyChange(property, oldColor, newColor);
//                    value.setBackground(newColor);
//                }
//            }
//        });
//        value.setEnabled(!property.isReadOnly());
//        value.setBackground((Color) property.get());
//
//        setLayout(new PropertyLayout());
//        add(name, "name");
//        add(value, "value");
//    }
//
//    @Override
//    public void removeListeners() {
//        property.removeListener(this);
//    }
//
//    @Override
//    public JComponent getEditorUI() {
//        return this;
//    }
//
//    public void propertyChange(PropertyChangeEvent evt) {
//        UIUtils.invokeInEDT(new Runnable() {
//
//            @Override
//            public void run() {
//                value.setBackground((Color) property.get());
//                value.setEnabled(!property.isReadOnly());
//            }
//        });
//    }

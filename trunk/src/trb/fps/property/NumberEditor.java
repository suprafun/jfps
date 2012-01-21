package trb.fps.property;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import javax.swing.JComponent;
import javax.swing.JTextField;

public class NumberEditor {

    private Property property;
    private JTextField txt = new JTextField();
    private PropertyChangeListener propertyListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            updateUI();
        }
    };

    public NumberEditor(Property<? extends Number> property) {
        this.property = property;
        property.listeners.addWeakListener(propertyListener);
        txt.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateProperty();
            }
        });
        txt.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                updateProperty();
            }
        });
        updateUI();
    }

    private void updateUI() {
        txt.setText("" + property.get());
    }

    private void updateProperty() {
        try {
            Number number = NumberFormat.getInstance().parse(txt.getText());
            if (property.getType().equals(Float.class)) {
                property.set(number.floatValue());
            }
        } catch (Exception ex) {
            updateUI();
        }
    }

    public JComponent getComponent() {
        return txt;
    }

    public static void main(String[] args) {
        PropertyListPanel.main(args);
    }
}

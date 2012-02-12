package trb.fps.property;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Locale;
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
            if (property.getType().equals(Boolean.class)) {
                property.set(Boolean.parseBoolean(txt.getText()));
            } else {
                Number number = NumberFormat.getInstance(Locale.ENGLISH).parse(txt.getText().replace(",", "."));
                if (property.getType().equals(Float.class)) {
                    property.set(number.floatValue());
                } else if (property.getType().equals(Double.class)) {
                    property.set(number.doubleValue());
                } else if (property.getType().equals(Integer.class)) {
                    property.set(number.intValue());
                } else if (property.getType().equals(Long.class)) {
                    property.set(number.longValue());
                }
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

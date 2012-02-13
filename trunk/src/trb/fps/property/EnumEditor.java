package trb.fps.property;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class EnumEditor {

    private Property property;
    private JComboBox combo = new JComboBox();
    private PropertyChangeListener propertyListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            updateUI();
        }
    };

    public EnumEditor(Property property) {
        this.property = property;
        property.listeners.addWeakListener(propertyListener);

		Object[] enumConstants = property.getType().getEnumConstants();
		DefaultComboBoxModel model = new DefaultComboBoxModel(enumConstants);
        combo = new JComboBox();
		combo.setModel(model);
		combo.addFocusListener(new FocusListener() {

			public void focusGained(FocusEvent fe) {
			}

			public void focusLost(FocusEvent fe) {
				updateProperty();
			}
		});
		combo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				updateProperty();
			}
		});
        updateUI();
    }

    private void updateUI() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                combo.setSelectedItem(property.get());
            }
        });
    }

	private void updateProperty() {
		property.set(combo.getSelectedItem());
	}

    public JComponent getComponent() {
        return combo;
    }

    public static void main(String[] args) {
        PropertyListPanel.main(args);
    }
}

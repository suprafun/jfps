package trb.fps;

import java.awt.BorderLayout;
import java.util.Random;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

public class ConnectPanel extends JPanel {

    public JTextField nameTxt = new JTextField("Player" + new Random().nextInt(100));
    public JSpinner killLimit = new JSpinner(new SpinnerNumberModel(10, 1, 50, 1));
    public JCheckBox startEditor = new JCheckBox("", true);
    public JRadioButton hostBtn = new JRadioButton("host");
	public JRadioButton connectBtn = new JRadioButton("connect");
	public JTextField ipTxt = new JTextField("localhost");

	public ConnectPanel() {
		setLayout(new MigLayout("", "", ""));
		
		ButtonGroup serverClientGroup = new ButtonGroup();
		serverClientGroup.add(hostBtn);
		serverClientGroup.add(connectBtn);
		serverClientGroup.setSelected(hostBtn.getModel(), true);

        add(new JLabel("Name:"));
        add(nameTxt, "pushx, growx, wrap");
        add(hostBtn, "span 2, wrap");
        add(new JLabel("Kill limit"));
        add(killLimit, "wrap");
        add(new JLabel("Start Editor"));
        add(startEditor, "wrap");
		add(connectBtn, "span 2, wrap");
		add(new JLabel("IP address:"));
		add(ipTxt, "growx");
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new ConnectPanel(), BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}
}

package trb.fps;

import java.awt.BorderLayout;
import java.util.Random;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

public class ConnectPanel extends JPanel {

    public JTextField nameTxt = new JTextField("Player"+new Random().nextInt(100));
    public JRadioButton serverBtn = new JRadioButton("host");
	public JRadioButton clientBtn = new JRadioButton("connect");
	public JTextField ipTxt = new JTextField("localhost");

	public ConnectPanel() {
		setLayout(new MigLayout("", "", ""));
		
		ButtonGroup serverClientGroup = new ButtonGroup();
		serverClientGroup.add(serverBtn);
		serverClientGroup.add(clientBtn);
		serverClientGroup.setSelected(serverBtn.getModel(), true);

        add(new JLabel("Name:"));
        add(nameTxt, "pushx, growx, wrap");
		add(serverBtn, "span 2, wrap");
		add(clientBtn, "span 2, wrap");
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

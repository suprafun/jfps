package trb.fps.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.miginfocom.swing.MigLayout;
import trb.fps.LevelGenerator;
import trb.fps.property.PropertyListPanel;
import trb.jsg.Shape;
import trb.xml.XMLElement;
import trb.xml.XMLElementWriter;

public class LevelEditor {

    public JFrame frame = new JFrame("Level editor");

    public List<BoxProps> boxes = new ArrayList();
    private final JPanel propertyPanel = new JPanel(new BorderLayout());
    private final JList list;
    private File currentFile = null;

    public LevelEditor(final LevelGenerator levelGenerator) {
        boxes.add(BoxProps.fromMinMax("ground", -400, -1, -400, 400, 0, 400));
        boxes.add(BoxProps.fromMinMax("pole", -0.1f, 0, -0.1f, 0.1f, 100, 0.1f));

        list = new JList(boxes.toArray());
        list.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updatePropertyList();
                }
            }
        });
        JScrollPane listPane = new JScrollPane(list);

        JPanel panel = new JPanel(new MigLayout("fill", "", "[50%][50%][0%]"));
        panel.add(listPane, "grow, span 3, wrap");
        panel.add(propertyPanel, "grow, span 3, wrap");
        panel.add(new JButton(new AbstractAction("+") {

            public void actionPerformed(ActionEvent e) {
                System.out.println("add");
                BoxProps selection = new BoxProps();
                boxes.add(selection);
                updateBoxList(selection);
            }
        }), "growx");
        panel.add(new JButton(new AbstractAction("-") {

            public void actionPerformed(ActionEvent e) {
                Object value = list.getSelectedValue();
                if (value instanceof BoxProps) {
                    boxes.remove((BoxProps) value);
                    updateBoxList(null);
                }
            }
        }), "growx");
        panel.add(new JButton(new AbstractAction("Update") {

            public void actionPerformed(ActionEvent e) {
                System.out.println("update");
                if (levelGenerator != null) {
                    ArrayList<Shape> shapes = new ArrayList();
                    for (BoxProps boxProps : boxes) {
                        shapes.add(boxProps.getShape());
                    }
                    levelGenerator.replace(shapes);
                }
            }
        }), "growx");

        frame.setJMenuBar(new EditorMenu(this).menuBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(250, 600);
        frame.add(panel);
        frame.setLocation(frame.getToolkit().getScreenSize().width-250, 0);
        frame.setVisible(true);
    }

    private void updateBoxList(final Object selection) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                final List<BoxProps> modelData = new ArrayList(boxes);
                list.setModel(new AbstractListModel() {

                    public int getSize() {
                        return modelData.size();
                    }

                    public Object getElementAt(int index) {
                        return modelData.get(index);
                    }
                });
                list.setSelectedValue(selection, true);
                updatePropertyList();
            }
        });
    }

    private void updatePropertyList() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                propertyPanel.removeAll();
                Object value = list.getSelectedValue();
                if (value instanceof BoxProps) {
                    BoxProps boxProps = (BoxProps) value;
                    propertyPanel.add(new PropertyListPanel(boxProps.properties).get(), BorderLayout.CENTER);
                }
                propertyPanel.revalidate();
                propertyPanel.repaint();
            }
        });
    }

    void newLevel() {
    }

    void open() {
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        JFileChooser chooser = new JFileChooser(prefs.get("chooserPath", "."));
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(frame)) {
            File file = chooser.getSelectedFile();
            prefs.put("chooserPath", file.getParent());
            open(file);
        }
    }

    void save() {
        save(currentFile);
    }

    void saveAs() {
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        JFileChooser chooser = new JFileChooser(prefs.get("chooserPath", "."));
        if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(frame)) {
            File file = chooser.getSelectedFile();
            prefs.put("chooserPath", file.getParent());
            save(file);
        }
    }

    void open(File file) {
        if (file == null) {
            return;
        }

        try {
            XMLElement level = new XMLElement(new FileInputStream(file)).getFirstChildWithName("level");
            System.out.println(level);
            boxes = new ArrayList(IO.readLevel(level.getFirstChildWithName("boxes")));
            updateBoxList(null);
            currentFile = file;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void save(File file) {
        if (file == null) {
            return;
        }

        XMLElement level = XMLElement.createFromName("level");
        IO.writeLevel(level.createChild("boxes"), boxes);
        try {
            XMLElementWriter.write(new PrintWriter(file), level);
            currentFile = file;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new LevelEditor(null);
    }
}

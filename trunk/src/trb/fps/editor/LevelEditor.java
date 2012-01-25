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
import trb.jsg.TreeNode;
import trb.xml.XMLElement;
import trb.xml.XMLElementWriter;

public final class LevelEditor {

    public JFrame frame = new JFrame("Level editor");
    private final LevelGenerator levelGenerator;

    public EntityList entities = new EntityList();
    private final JPanel propertyPanel = new JPanel(new BorderLayout());
    private final JList list = new JList();

    public LevelEditor(LevelGenerator levelGenerator) {
        this.levelGenerator = levelGenerator;
        File currentFile = getCurrentFile();
        if (currentFile != null && currentFile.exists()) {
            open(currentFile);
        }
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
                addBox();
            }
        }), "growx");
        panel.add(new JButton(new AbstractAction("-") {

            public void actionPerformed(ActionEvent e) {
                removeSelection();
            }
        }), "growx");
        panel.add(new JButton(new AbstractAction("Update") {

            public void actionPerformed(ActionEvent e) {
                updateLevelGenerator();
            }
        }), "growx");

        frame.setJMenuBar(new EditorMenu(this).menuBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(250, 1024);
        frame.add(panel);
        frame.setLocation(frame.getToolkit().getScreenSize().width-250, 0);
        frame.setVisible(true);
    }

    private void addBox() {
        Entity selection = Entity.create(Meta.class, Transform.class, Box.class);
        entities.add(selection);
        updateBoxList(selection);
    }

    private void removeSelection() {
        Object value = list.getSelectedValue();
        if (value instanceof Entity) {
            entities.remove((Entity) value);
            updateBoxList(null);
        }
    }

    private void updateBoxList(final Object selection) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                final List<Entity> modelData = entities.getAll();
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
        updateLevelGenerator();
    }

    private void updatePropertyList() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                propertyPanel.removeAll();
                Object value = list.getSelectedValue();
                if (value instanceof Entity) {
                    propertyPanel.add(new PropertyListPanel((Entity) value).get(), BorderLayout.CENTER);
                }
                propertyPanel.revalidate();
                propertyPanel.repaint();
            }
        });
    }

    private void updateLevelGenerator() {
        if (levelGenerator != null) {
            ArrayList<TreeNode> nodes = new ArrayList();
            for (Box box : entities.getComponents(Box.class)) {
                nodes.add(box.getNode());
            }
            levelGenerator.replace(nodes);
        }
    }


    void newLevel() {
    }

    void open() {
        JFileChooser chooser = new JFileChooser(getCurrentFolder());
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(frame)) {
            open(chooser.getSelectedFile());
        }
    }

    void save() {
        save(getCurrentFile());
    }

    void saveAs() {
        JFileChooser chooser = new JFileChooser(getCurrentFolder());
        if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(frame)) {
            save(chooser.getSelectedFile());
        }
    }

    void open(File file) {
        if (file == null) {
            return;
        }

        try {
            XMLElement level = new XMLElement(new FileInputStream(file)).getFirstChildWithName("level");
            System.out.println(level);
            entities = new EntityList(IO.readLevel(level));
            updateBoxList(null);
            setCurrentFile(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void save(File file) {
        if (file == null) {
            return;
        }

        XMLElement level = XMLElement.createFromName("level");
        IO.writeLevel(level, entities.getAll());
        try {
            XMLElementWriter.write(new PrintWriter(file), level);
            setCurrentFile(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setCurrentFile(File file) {
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        prefs.put("currentFile", file.getPath());
    }

    public File getCurrentFile() {
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        File file = new File(prefs.get("currentFile", "."));
        if (file.isFile()) {
            return file;
        }
        return null;
    }

    public File getCurrentFolder() {
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        File file = new File(prefs.get("currentFile", "."));
        if (file.isFile()) {
            return file.getParentFile();
        }
        return file;
    }

    public static void main(String[] args) {
        new LevelEditor(null);
    }
}

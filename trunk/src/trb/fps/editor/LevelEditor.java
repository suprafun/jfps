package trb.fps.editor;

import trb.fps.entity.Entity;
import trb.fps.entity.EntityList;
import trb.fps.entity.Box;
import trb.fps.entity.PointLightComp;
import trb.fps.entity.IO;
import trb.fps.entity.Meta;
import trb.fps.entity.HemisphereLightComp;
import trb.fps.entity.Transform;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
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
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.miginfocom.swing.MigLayout;
import trb.fps.ai.NavigationMeshEditor;
import trb.fps.client.FpsClient;
import trb.fps.entity.Powerup;
import trb.fps.server.FpsServer;
import trb.fps.entity.SpawnPoint;
import trb.fps.property.PropertyListPanel;
import trb.jsg.Shape;
import trb.xml.XMLElement;
import trb.xml.XMLElementWriter;

public final class LevelEditor {

    public static LevelEditor instance;

    public final JFrame frame = new JFrame("Level editor");
    private final FpsClient client;
    private final FpsServer server;

    private final JTabbedPane tabbs = new JTabbedPane();

    public final NavigationMeshEditor navMeshEditor;

    public EntityList entities = new EntityList();
    private final JPanel propertyPanel = new JPanel(new BorderLayout());
    private final JList list = new JList();
    private Entity selectedEntity = null;

    public final SelectionViualisation selectionVisualisation = new SelectionViualisation();

    public LevelEditor(FpsServer server, FpsClient client) {
        this.server = server;
        this.client = client;
		navMeshEditor = new NavigationMeshEditor(new NavigationMeshEditorUser(
				client.jsgDeferredRenderer.deferredSystem.boxNodeMap));

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

        JPanel panel = new JPanel(new MigLayout("fill", "[10%][90%]", "[50%][50%][0%]"));
        panel.add(listPane, "grow, span 3, wrap");
        panel.add(propertyPanel, "grow, span 3, wrap");
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

        tabbs.addTab("Entities", panel);
        tabbs.addTab("NavMesh", navMeshEditor.parameters.createUI());

        frame.setJMenuBar(new EditorMenu(this).menuBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 1024);
        frame.add(tabbs);
        frame.setLocation(frame.getToolkit().getScreenSize().width-250, 0);
        frame.setVisible(true);
    }

    private void removeSelection() {
        Object value = list.getSelectedValue();
        if (value instanceof Entity) {
            entities.remove((Entity) value);
            updateSwingList(null);
        }
    }

    private void updateSwingList(final Object selection) {
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
                    selectedEntity = (Entity) value;
                    propertyPanel.add(new PropertyListPanel(selectedEntity).get(), BorderLayout.CENTER);
                }
                propertyPanel.revalidate();
                propertyPanel.repaint();
            }
        });
    }

    private void updateLevelGenerator() {
        client.jsgDeferredRenderer.deferredSystem.recreate(entities);
        server.changeLevel(entities);
        navMeshEditor.generateNavMesh();
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
            entities = new EntityList(IO.readLevel(level));
            updateSwingList(null);
            setCurrentFile(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void save(File file) {
        if (file == null) {
            return;
        }

        XMLElement level = IO.writeLevel(entities.getAll());
        
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
        File file = new File(prefs.get("currentFile", "./data/level.txt"));
        if (file.isFile()) {
            return file;
        }
        return null;
    }

    public File getCurrentFolder() {
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        File file = new File(prefs.get("currentFile", "./data"));
        if (file.isFile()) {
            return file.getParentFile();
        }
        return file;
    }

    public void addBox() {
        Entity selection = Entity.create(Meta.class, Transform.class, Box.class);
        selection.getComponent(Meta.class).name.set("Box");
        entities.add(selection);
        updateSwingList(selection);
    }

    public void createPointLight() {
        Entity selection = Entity.create(Meta.class, Transform.class, PointLightComp.class);
        selection.getComponent(Meta.class).name.set("PointLight");
        entities.add(selection);
        updateSwingList(selection);
    }

    public void createHemisphereLight() {
        Entity selection = Entity.create(Meta.class, Transform.class, HemisphereLightComp.class);
        selection.getComponent(Meta.class).name.set("HemisphereLight");
        entities.add(selection);
        updateSwingList(selection);
    }

    public void createSpawnPoint() {
        Entity selection = Entity.create(Meta.class, Transform.class, Box.class, SpawnPoint.class);
        selection.getComponent(Meta.class).name.set("SpawnPoint");
        selection.getComponent(Box.class).height.set(2f);
        entities.add(selection);
        updateSwingList(selection);
    }

	public void createPowerup(Powerup.Type type) {
		Entity selection = Entity.create(Meta.class, Transform.class, Box.class, Powerup.class);
		selection.getComponent(Meta.class).name.set("Powerup"+type.name());
		selection.getComponent(Powerup.class).type.set(type);
		entities.add(selection);
		updateSwingList(selection);
	}

    public void updateSelectionVisualisation() {
        Entity selection = selectedEntity;
        Shape shape = client.jsgDeferredRenderer.deferredSystem.getBounds(selection);
        if (shape != null) {
            selectionVisualisation.selectionShape.setVisible(true);
            selectionVisualisation.selectionShape.setModelMatrix(shape.getModelMatrix());
            selectionVisualisation.selectionShape.setVertexData(shape.getVertexData());
        } else {
            selectionVisualisation.selectionShape.setVisible(false);
        }
    }
}

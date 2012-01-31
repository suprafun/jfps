package trb.fps.editor;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class EditorMenu {

    private final LevelEditor editor;
    public final JMenuBar menuBar = new JMenuBar();

    public EditorMenu(LevelEditor editor) {
        this.editor = editor;
        createFileMenu();
        createLevelMenu();
    }

    private void createFileMenu() {
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        menu.add(new AbstractAction("New") {

            public void actionPerformed(ActionEvent e) {
                editor.newLevel();
            }
        });
        menu.add(new AbstractAction("Open") {

            public void actionPerformed(ActionEvent e) {
                editor.open();
            }
        });
        menu.add(new AbstractAction("Save") {

            public void actionPerformed(ActionEvent e) {
                editor.save();
            }
        });
        menu.add(new AbstractAction("Save...") {

            public void actionPerformed(ActionEvent e) {
                editor.saveAs();
            }
        });
    }

    private void createLevelMenu() {
        JMenu menu = new JMenu("Create");
        menuBar.add(menu);
        menu.add(new AbstractAction("Box") {

            public void actionPerformed(ActionEvent e) {
                editor.addBox();
            }
        });
        menu.add(new AbstractAction("Point Light") {

            public void actionPerformed(ActionEvent e) {
                editor.createPointLight();
            }
        });
        menu.add(new AbstractAction("Hemisphere Light") {

            public void actionPerformed(ActionEvent e) {
                editor.createHemisphereLight();
            }
        });
        menu.add(new AbstractAction("Spawn Point") {

            public void actionPerformed(ActionEvent e) {
                editor.createSpawnPoint();
            }
        });
    }
}

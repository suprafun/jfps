/*
 * HALDEN VR PLATFORM
 *
 * RADIATION MODULE
 *
 * $RCSfile: $
 *
 * Author :
 * Date   :
 * Version: $Revision: $ ($Date: $)
 *
 * (c) 2000-2011 Halden Virtual Reality Centre <http://www.ife.no/vr/>,
 * Institutt for energiteknikk. All rights reserved.
 *
 * This code is the property of Halden VR Centre <vr-info@hrp.no> and may
 * only be used in accordance with the terms of the license agreement
 * granted.
 */

package trb.fps.editor;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

/**
 *
 * @author tomrbryn
 */
public class EditorMenu {

    private final LevelEditor editor;
    public final JMenuBar menuBar = new JMenuBar();

    public EditorMenu(LevelEditor editor) {
        this.editor = editor;
        createFileMenu();        
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
}

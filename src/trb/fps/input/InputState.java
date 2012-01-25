package trb.fps.input;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class InputState {

    public int mouseDX;
    public int mouseDY;
    public final List<KeyEvent> keyEvents = new ArrayList();
    public final List<MouseEvent> mouseEvents = new ArrayList();

    public void poll() {
        mouseDX = Mouse.isInsideWindow() ? Mouse.getDX() : 0;
        mouseDY = Mouse.isInsideWindow() ? Mouse.getDY() : 0;

        keyEvents.clear();
        while (Keyboard.next()) {
            keyEvents.add(new KeyEvent(Keyboard.getEventKeyState(), Keyboard.getEventKey()));
        }

        mouseEvents.clear();
        while (Mouse.next()) {
            mouseEvents.add(new MouseEvent(Mouse.getEventButtonState(), Mouse.getEventButton()));
        }
    }

    public boolean wasButtonPressed(int button) {
        for (MouseEvent e : mouseEvents) {
            if (e.state && e.button == button) {
                return true;
            }
        }

        return false;
    }

    public boolean wasKeyPressed(int key) {
        for (KeyEvent e : keyEvents) {
            if (e.state && e.key == key) {
                return true;
            }
        }

        return false;
    }
}

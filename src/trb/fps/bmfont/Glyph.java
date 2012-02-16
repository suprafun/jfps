package trb.fps.bmfont;

import java.util.HashMap;
import java.util.Map;

public class Glyph {

    public int srcX;
    public int srcY;
    int width, height;
    float u, v, u2, v2;
    int xoffset, yoffset;
    int xadvance;
    Map<Character, Integer> kerning;

    int getKerning(char ch) {
        if (kerning != null) {
            return kerning.get(ch);
        }
        return 0;
    }

    void setKerning(int ch, int value) {
        if (kerning == null) {
            kerning = new HashMap<Character, Integer>();
        }

        kerning.put((char) ch, value);
    }

    @Override
    public String toString() {
        return srcX + " " + srcY + " " + width + " " + height;
    }
}

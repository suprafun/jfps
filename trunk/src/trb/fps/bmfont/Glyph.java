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

	public Glyph() {
		
	}

	public Glyph(int srcX, int srcY, int width, int height, int xoffset, int yoffset, int xadvance) {
		this.srcX = srcX;
		this.srcY = srcY;
		this.width = width;
		this.height = height;
		this.xoffset = xoffset;
		this.yoffset = yoffset;
		this.xadvance = xadvance;
	}

	public void initUV(int textureWidth, int textureHeight, boolean flipped) {
		float invTexWidth = 1.0f / textureWidth;
		float invTexHeight = 1.0f / textureHeight;
		u = srcX * invTexWidth;
		u2 = (srcX + width) * invTexWidth;
		if (flipped) {
			v = srcY * invTexHeight;
			v2 = (srcY + height) * invTexHeight;
		} else {
			v2 = srcY * invTexHeight;
			v = (srcY + height) * invTexHeight;
		}

        // seems like v is up side down compared to what we use
        v = 1f - v;
        v2 = 1f - v2;
	}

    int getKerning(char ch) {
        if (kerning != null) {
//            return kerning.get(ch);
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

package trb.fps.bmfont;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class BitmapFontData {

	static final char[] xChars = {'x', 'e', 'a', 'o', 'n', 's', 'r', 'c', 'u', 'm', 'v', 'w', 'z'};
	static final char[] capChars = {'M', 'N', 'B', 'D', 'C', 'E', 'F', 'K', 'A',
		'G', 'H', 'I', 'J', 'L', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    final boolean flipped;
    final float lineHeight;
    float capHeight = 1;
    float ascent;
    float descent;
    float down;
    final Map<Character, Glyph> glyphs = new HashMap();
    float spaceWidth;
    float xHeight = 1;
	float scaleX = 1f;
	float scaleY = 1f;
    String imageFile;

    public BitmapFontData(InputStream fontFile, boolean flip) {
        this.flipped = flip;
        BufferedReader reader = new BufferedReader(new InputStreamReader(fontFile), 512);
        try {
            reader.readLine(); // info

            String line = reader.readLine();
            String[] common = line.split(" ");
            if (!common[1].startsWith("lineHeight=")) {
                throw new RuntimeException("Invalid font file: " + fontFile);
            }
            lineHeight = Integer.parseInt(common[1].substring(11));

            if (!common[2].startsWith("base=")) {
                throw new RuntimeException("Invalid font file: " + fontFile);
            }
            int baseLine = Integer.parseInt(common[2].substring(5));

			int scaleW = Integer.parseInt(common[3].substring(7));
			int scaleH = Integer.parseInt(common[4].substring(7));

            line = reader.readLine();
            String[] pageLine = line.split(" ", 4);
            if (!pageLine[2].startsWith("file=")) {
                throw new RuntimeException("Invalid font file: " + fontFile);
            }
            imageFile = pageLine[2];
            descent = 0;

            while (true) {
                line = reader.readLine();
                if (line.startsWith("kernings ")) {
                    break;
                }
                if (!line.startsWith("char ")) {
                    continue;
                }

                StringTokenizer tokens = new StringTokenizer(line, " =");
                tokens.nextToken();
                tokens.nextToken();
                int ch = Integer.parseInt(tokens.nextToken());
                tokens.nextToken();
                int srcX = Integer.parseInt(tokens.nextToken());
                tokens.nextToken();
                int srcY = Integer.parseInt(tokens.nextToken());
                tokens.nextToken();
                int width = Integer.parseInt(tokens.nextToken());
                tokens.nextToken();
                int height = Integer.parseInt(tokens.nextToken());
                tokens.nextToken();
                int xoffset = Integer.parseInt(tokens.nextToken());
                tokens.nextToken();
				int yoffset = 0;
                if (flip) {
                    yoffset = Integer.parseInt(tokens.nextToken());
                } else {
                    yoffset = -(height + Integer.parseInt(tokens.nextToken()));
                }
                tokens.nextToken();
                int xadvance = Integer.parseInt(tokens.nextToken());
                if (width > 0 && height > 0) {
                    descent = Math.min(baseLine + yoffset, descent);
                }

				Glyph glyph = new Glyph(srcX, srcY, width, height, xoffset, yoffset, xadvance);
				glyph.initUV(scaleW, scaleH, flipped);
				setGlyph(ch, glyph);
            }

            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (!line.startsWith("kerning ")) {
                    break;
                }

                StringTokenizer tokens = new StringTokenizer(line, " =");
                tokens.nextToken();
                tokens.nextToken();
                int first = Integer.parseInt(tokens.nextToken());
                tokens.nextToken();
                int second = Integer.parseInt(tokens.nextToken());
                if (first < 0 || first > Character.MAX_VALUE || second < 0 || second > Character.MAX_VALUE) {
                    continue;
                }
                Glyph glyph = getGlyph((char) first);
                tokens.nextToken();
                int amount = Integer.parseInt(tokens.nextToken());
                glyph.setKerning(second, amount);
            }

            Glyph spaceGlyph = getGlyph(' ');
            if (spaceGlyph == null) {
                spaceGlyph = new Glyph();
                Glyph xadvanceGlyph = getGlyph('l');
                if (xadvanceGlyph == null) {
                    xadvanceGlyph = getFirstGlyph();
                }
                spaceGlyph.xadvance = xadvanceGlyph.xadvance;
                setGlyph(' ', spaceGlyph);
            }
            spaceWidth = spaceGlyph != null ? spaceGlyph.xadvance + spaceGlyph.width : 1;

            Glyph xGlyph = null;
            for (int i = 0; i < xChars.length; i++) {
                xGlyph = getGlyph(xChars[i]);
                if (xGlyph != null) {
                    break;
                }
            }
            if (xGlyph == null) {
                xGlyph = getFirstGlyph();
            }
            xHeight = xGlyph.height;

            Glyph capGlyph = null;
            for (int i = 0; i < capChars.length; i++) {
                capGlyph = getGlyph(capChars[i]);
                if (capGlyph != null) {
                    break;
                }
            }
            if (capGlyph == null) {
                for (Glyph glyph : glyphs.values()) {
                    if (glyph.height == 0 || glyph.width == 0) {
                        continue;
                    }
                    capHeight = Math.max(capHeight, glyph.height);
                }
            } else {
                capHeight = capGlyph.height;
            }

            ascent = baseLine - capHeight;
            down = -lineHeight;
            if (flip) {
                ascent = -ascent;
                down = -down;
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error loading font file: " + fontFile, ex);
        } finally {
            try {
                reader.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void setGlyph(int ch, Glyph glyph) {
		glyphs.put((char) ch, glyph);
    }

    private Glyph getFirstGlyph() {
        for (Glyph glyph : glyphs.values()) {
            if (glyph.height == 0 || glyph.width == 0) {
                continue;
            }
            return glyph;
        }
        throw new RuntimeException("No glyphs found!");
    }

    public Glyph getGlyph(char ch) {
		return glyphs.get(ch);
    }

    public String getImageFile() {
        return imageFile;
    }
}

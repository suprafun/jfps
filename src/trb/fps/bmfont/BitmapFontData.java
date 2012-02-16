package trb.fps.bmfont;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;

public class BitmapFontData {

    final boolean flipped;
    final float lineHeight;
    float capHeight = 1;
    float ascent;
    float descent;
    float down;
    float scaleX = 1, scaleY = 1;
    final Glyph[] glyphs = new Glyph[256];
    float spaceWidth;
    float xHeight = 1;
    String imageFile;

    public BitmapFontData(InputStream fontFile, boolean flip) {
        this.flipped = flip;
        BufferedReader reader = new BufferedReader(new InputStreamReader(fontFile), 512);
        try {
            reader.readLine(); // info

            String line = reader.readLine();
            String[] common = line.split(" ", 4);
            if (!common[1].startsWith("lineHeight=")) {
                throw new RuntimeException("Invalid font file: " + fontFile);
            }
            lineHeight = Integer.parseInt(common[1].substring(11));

            if (!common[2].startsWith("base=")) {
                throw new RuntimeException("Invalid font file: " + fontFile);
            }
            int baseLine = Integer.parseInt(common[2].substring(5));

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

                Glyph glyph = new Glyph();

                StringTokenizer tokens = new StringTokenizer(line, " =");
                tokens.nextToken();
                tokens.nextToken();
                int ch = Integer.parseInt(tokens.nextToken());
                if (ch <= Character.MAX_VALUE) {
                    setGlyph(ch, glyph);
                } else {
                    continue;
                }
                tokens.nextToken();
                glyph.srcX = Integer.parseInt(tokens.nextToken());
                tokens.nextToken();
                glyph.srcY = Integer.parseInt(tokens.nextToken());
                tokens.nextToken();
                glyph.width = Integer.parseInt(tokens.nextToken());
                tokens.nextToken();
                glyph.height = Integer.parseInt(tokens.nextToken());
                tokens.nextToken();
                glyph.xoffset = Integer.parseInt(tokens.nextToken());
                tokens.nextToken();
                if (flip) {
                    glyph.yoffset = Integer.parseInt(tokens.nextToken());
                } else {
                    glyph.yoffset = -(glyph.height + Integer.parseInt(tokens.nextToken()));
                }
                tokens.nextToken();
                glyph.xadvance = Integer.parseInt(tokens.nextToken());
                if (glyph.width > 0 && glyph.height > 0) {
                    descent = Math.min(baseLine + glyph.yoffset, descent);
                }
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
            for (int i = 0; i < BitmapFont.xChars.length; i++) {
                xGlyph = getGlyph(BitmapFont.xChars[i]);
                if (xGlyph != null) {
                    break;
                }
            }
            if (xGlyph == null) {
                xGlyph = getFirstGlyph();
            }
            xHeight = xGlyph.height;

            Glyph capGlyph = null;
            for (int i = 0; i < BitmapFont.capChars.length; i++) {
                capGlyph = getGlyph(BitmapFont.capChars[i]);
                if (capGlyph != null) {
                    break;
                }
            }
            if (capGlyph == null) {
                for (Glyph glyph : glyphs) {
                    if (glyph == null || glyph.height == 0 || glyph.width == 0) {
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
        glyphs[ch] = glyph;
    }

    private Glyph getFirstGlyph() {
        for (Glyph glyph : glyphs) {
            if (glyph == null || glyph.height == 0 || glyph.width == 0) {
                continue;
            }
            return glyph;
        }
        throw new RuntimeException("No glyphs found!");
    }

    public Glyph getGlyph(char ch) {
        return (ch < glyphs.length) ? glyphs[ch] : null;
    }

    public String getImageFile() {
        return imageFile;
    }

    public static void main(String[] args) throws Exception {
        URL url = BitmapFontData.class.getResource("Candara-38-Bold.fnt");
        BitmapFontData bitmapFontData = new BitmapFontData(url.openStream(), false);
        String str = "abcABC";
        for (int i=0; i<str.length(); i++) {
            char c = str.charAt(i);
            Glyph glyph = bitmapFontData.getGlyph(c);
            System.out.println(c + " = " + glyph);
        }
        //System.out.println(in.available());
    }
}

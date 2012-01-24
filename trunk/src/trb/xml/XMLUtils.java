package trb.xml;

public class XMLUtils {
    public static String escape(String text) {
        boolean needsEscaping = false;
        for (int i = 0; i < text.length(); i++) {
            if (!isLegal(text.charAt(i))) {
                needsEscaping = true;
                break;
            }
        }

        if (!needsEscaping) {
            return text;
        }

        if (text.contains("]]>") == false) {
            return "<![CDATA[" + text + "]]>";
        }

        return escapeString(text);
    }

    /**
     * Takes a normal unicode Java string and returns an string escaped with HTML/XML
     * entities. It takes the "big five" and uses simple numeric encoding
     * for other non a-z A-Z 0-9 (or space) characters.
     *
     * @param string the original string
     * @return An escaped string containing the originalText HTML/XML escaped
     */
    public static String escapeString(String string) {
        int length = string.length();
        StringBuilder stBuff = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            char ch = string.charAt(i);

            switch (ch) {
                case '<':
                    stBuff.append("&lt;");
                    break;
                case '>':
                    stBuff.append("&gt;");
                    break;
                case '&':
                    stBuff.append("&amp;");
                    break;
                case '\"':
                    stBuff.append("&quot;");
                    break;
                case '\'':
                    stBuff.append("&#x27;");
                    break;
                default:
                    if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' || ch == ' ') {
                        stBuff.append(ch);
                    } else {
                        stBuff.append("&#" + (int) ch + ";");
                    }
                    break;
            }
        }

        return stBuff.toString();
    }

    private static boolean isLegal(char c) {
        return c >= 32 && c < 127 && c != '<' && c != '&';
//        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
//                || (c >= '0' && c <= '9') || c == ' ' || c == '_'
//                // the following should be considered if should be legal
//                || c == '.' || c == '-'
//                ;
    }

    public static String fromFloats(float[] floats) {
        StringBuilder str = new StringBuilder(floats.length * 8);
        for (int i = 0; i < floats.length; i++) {
            str.append(floats[i]);
            if (i < floats.length - 1) {
                str.append(" ");
            }
        }
        return str.toString();
    }

    public static float[] toFloats(String str) {
        return toFloats(str.split("\\s"));
    }

    public static float[] toFloats(String[] strings) {
        float[] floats = new float[strings.length];
        for (int i = 0; i < strings.length; i++) {
            floats[i] = Float.parseFloat(strings[i]);
        }
        return floats;
    }

    public static String fromInts(int[] ints) {
        StringBuilder str = new StringBuilder(ints.length * 8);
        for (int i = 0; i < ints.length; i++) {
            str.append(ints[i]);
            if (i < ints.length - 1) {
                str.append(" ");
            }
        }
        return str.toString();
    }

    public static int[] toInts(String str) {
        return toInts(str.split("\\s"));
    }

    public static int[] toInts(String[] strings) {
        int[] ints = new int[strings.length];
        for (int i = 0; i < strings.length; i++) {
            ints[i] = Integer.parseInt(strings[i]);
        }
        return ints;
    }
}

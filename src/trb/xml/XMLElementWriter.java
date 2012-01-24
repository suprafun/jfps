package trb.xml;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * Class that can write an XMLElement to a PrintWriter.
 * 
 * @author tomrbryn
 * @version $Revision: 1.2 $
 */
public class XMLElementWriter {

    public static void write(PrintWriter out, XMLElement elem) {
        new XMLElementWriter(out, 0, 2).write(elem);
    }

    public static void write(PrintWriter out, XMLElement elem, int startIndent, int indentAmount) {
        new XMLElementWriter(out, startIndent, indentAmount).write(elem);
    }

    private PrintWriter out;
    private int indent = 0;
    private int indentAmount = 2;

    private XMLElementWriter(PrintWriter out, int indent, int indentAmount) {
        this.out = out;
        this.indent = indent;
        this.indentAmount = indentAmount;
    }

    private void write(XMLElement elem) {
        indentPrint("<" + elem.name);
        for (XMLAttribute attr : elem.attributes()) {
            out.print(" " + attr.key + "=\"" + attr.value + "\"");
        }
        EscapedText elemText = new EscapedText(elem.text);
        if (elemText.type == EscapeType.BINARY) {
            out.print(" binary=\"true\"");
        }
        out.print(">");
        if (elem.children.size() > 0) {
            out.println();
            for (XMLElement child : elem) {
                indent += indentAmount;
                write(child);
                indent -= indentAmount;
            }
            if (!"".equals(elem.text)) {
                indentPrintln(elemText.toString());
            }
            indentPrintln("</" + elem.name + ">");
        } else {
            out.println(elemText.toString() + "</" + elem.name + ">");
        }
        out.flush();
    }

    public static String toBinary(String text) {
        try {
            byte[] data = text.getBytes("utf-8");
            StringBuffer strBuf = new StringBuffer(data.length * 2);
            for (int i = 0; i < data.length; i++) {
                byte b = data[i];
                strBuf.append((char) ('a' + (b & 0xf)));
                strBuf.append((char) ('a' + ((b >> 4) & 0xf)));
            }
            return strBuf.toString();
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String fromBinary(String binaryText) {
        byte[] out = new byte[binaryText.length()/2];
        for (int i=0; i<binaryText.length(); i+=2) {
            int lowBits = binaryText.charAt(i) - 'a';
            int highBits = binaryText.charAt(i + 1) - 'a';
            int nextByte = (highBits << 4) | lowBits;
            out[i/2] = (byte) nextByte;
        }
        try {
            return new String(out, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void indent(int amount) {
        for (int i = 0; i < amount; i++) {
            out.print(" ");
        }
    }

    private void indentPrint(String text) {
        indent(indent);
        out.print(text);
    }

    private void indentPrintln(String text) {
        indent(indent);
        out.println(text);
    }

    public enum EscapeType {NONE, CDATA, BINARY};

    static class EscapedText {
        private String escapedText;
        public EscapeType type = EscapeType.NONE;

        public EscapedText(String text) {
            boolean notCDATA = text.contains("]]>");
            if (notCDATA) {
                type = EscapeType.BINARY;
                escapedText = toBinary(text);
                return;
            }
            boolean isLegal = true;
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);

                isLegal &= c >= 32 && c <= 126 && c != '<' && c != '>' && c != '&';

                if ((c < 32 && c != 10 && c != 13) || c > 256) {
                    type = EscapeType.BINARY;
                    escapedText = toBinary(text);
                    return;
                }
            }

            if (isLegal) {
                escapedText = text;
                return;
            }

            type = EscapeType.CDATA;
            escapedText = "<![CDATA["  + text + "]]>";
        }

        @Override
        public String toString() {
            return escapedText;
        }
    }
}

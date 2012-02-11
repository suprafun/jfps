package trb.fps.jsg.shadow;

import java.io.InputStream;
import trb.jsg.ShaderProgram;
import trb.xml.XMLElement;

public class ShaderLoader {

    public static ShaderProgram load(InputStream in, String shaderName) {
        try {
            XMLElement xml = new XMLElement(in);
            for (XMLElement shaderElem : xml.getFirstChildWithName("shaders").getChildrenWithName("shader")) {
                if (shaderName.equals(shaderElem.attributeValue("name"))) {
                    String vertexShader = shaderElem.getFirstChildWithName("vertex").text;
                    String fragmentShader = shaderElem.getFirstChildWithName("fragment").text;
                    return new ShaderProgram(vertexShader, fragmentShader);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("" + load(ShaderLoader.class.getResourceAsStream("varianceShadowMapShaders.xml"), "fill"));
    }
}

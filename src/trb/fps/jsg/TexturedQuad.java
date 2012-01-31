package trb.fps.jsg;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import org.lwjgl.BufferUtils;
import trb.jsg.Shape;
import trb.jsg.Texture;
import trb.jsg.Unit;
import trb.jsg.VertexData;
import trb.jsg.enums.BlendDstFunc;
import trb.jsg.enums.BlendSrcFunc;
import trb.jsg.enums.Format;
import trb.jsg.enums.TextureType;
import trb.jsg.util.geometry.VertexDataUtils;

/**
 *
 * @author tomrbryn
 */
public class TexturedQuad {

    public BufferedImage image;
    public Shape shape;

    private int[] rgbArray;
    private ByteBuffer byteBuffer;
    private Texture texture = new Texture();

    public TexturedQuad(int x, int y, int w, int h) {
        rgbArray = new int[w * h];
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        byteBuffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);

        // a simple triangle
        VertexData vertexData = VertexDataUtils.createQuad(x, y, w, h, 0);

        ByteBuffer[][] pixels = {{byteBuffer}};

        texture.setTextureData(TextureType.TEXTURE_2D, 4, image.getWidth(), image.getHeight(), 0, Format.BGRA, pixels, false);

        shape = new Shape();
        shape.setVertexData(vertexData);
        shape.getState().setUnit(0, new Unit(texture));
        shape.getState().setBlendEnabled(true);
        shape.getState().setBlendSrcFunc(BlendSrcFunc.SRC_ALPHA);
        shape.getState().setBlendDstFunc(BlendDstFunc.ONE_MINUS_SRC_ALPHA);
        shape.getState().setDepthTestEnabled(false);
        shape.getState().setDepthWriteEnabled(false);
    }

    public void copyImageToTexture() {
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), rgbArray, 0, image.getWidth());
        for (int i=0; i<rgbArray.length; i++) {
            int argb = rgbArray[i];
            // bgra
            byteBuffer.put((byte) argb)
                    .put((byte) (argb >> 8))
                    .put((byte) (argb >> 16))
                    .put((byte) (argb >>> 24))
                    ;
        }
        
        byteBuffer.rewind();
        texture.pixelsChanged();
    }
}

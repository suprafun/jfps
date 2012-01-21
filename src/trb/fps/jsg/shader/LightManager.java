package trb.fps.jsg.shader;

import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Color4f;
import javax.vecmath.Tuple2f;
import org.lwjgl.opengl.GL11;
import trb.jsg.DepthBuffer;
import trb.jsg.RenderPass;
import trb.jsg.RenderTarget;
import trb.jsg.ShaderProgram;
import trb.jsg.Texture;
import trb.jsg.VertexData;
import trb.jsg.View;
import trb.jsg.util.ShaderUtils;
import trb.jsg.util.Vec3;
import trb.jsg.util.geometry.VertexDataUtils;

public class LightManager {
    private final Texture baseTexture;
    private final ShaderProgram pointLightShaderProgram;
    private final ShaderProgram hemisphereLightShaderProgram;
    private final List<PointLight> pointLights = new ArrayList();
    private final List<HemisphereLight> hemisphereLights = new ArrayList();
    public final RenderPass renderPass = new RenderPass();
    private final VertexData fullScreenVertexData;

    private String hemisphereVertexShader =
            "void main(void) {\n"
            + "    gl_Position = ftransform();\n"
            + "}";

    private String hemisphereFragmentShader =
              "uniform vec3 skyColor;\n"
            + "uniform vec3 groundColor;\n"
            + "uniform vec3 direction;\n"
            + "uniform sampler2D texture;\n"
            + "uniform vec2 bufferSize;"
            + "void main(void) {\n"
            + "    vec2 texCoord = gl_FragCoord.xy / bufferSize;\n"
            + "    vec3 normal = texture2D(texture, texCoord).xyz * 2.0 - 1.0;\n"
            + "    float costheta = dot(normal, direction);\n"
            + "    float a = 0.5 + 0.5 * costheta;\n"
            + "    gl_FragColor = vec4(mix(groundColor, skyColor, a), 1);\n"
            + "}";

    
    public LightManager(Texture baseTexture, DepthBuffer baseDepth, Texture lightTexture
            , View view, Tuple2f bufferSize) {
        this.baseTexture = baseTexture;
        pointLightShaderProgram = ShaderUtils.loadFromResource(
                "/trb/fps/jsg/shader/deferredPointVertex.shader"
                , "/trb/fps/jsg/shader/deferredPointFragment.shader").getShaderProgram();

        hemisphereLightShaderProgram = new ShaderProgram(hemisphereVertexShader, hemisphereFragmentShader);

        RenderTarget renderTarget = new RenderTarget(
                lightTexture.getWidth(), lightTexture.getHeight()
                , baseDepth, false, lightTexture);

        // create a renderpass that renders to the screen
        renderPass.setClearMask(GL11.GL_COLOR_BUFFER_BIT);
        renderPass.setClearColor(new Color4f(0f, 0f, 0f, 0));
        renderPass.setView(view);
        renderPass.setRenderTarget(renderTarget);

        fullScreenVertexData = VertexDataUtils.createQuad(-20, -20, 40, 40, -5);
    }

    public PointLight createPointLight(Vec3 color, Vec3 positionWorld, float radius) {
        PointLight light = new PointLight(color, positionWorld, radius, baseTexture
                , pointLightShaderProgram, fullScreenVertexData);
        pointLights.add(light);
        light.addShapes(renderPass);
        return light;
    }

    public HemisphereLight createHemisphereLight(Vec3 skyColor, Vec3 groundColor, Vec3 direction) {
        HemisphereLight light = new HemisphereLight(skyColor, groundColor, direction
                , baseTexture, hemisphereLightShaderProgram, fullScreenVertexData);
        hemisphereLights.add(light);
        renderPass.getRootNode().addShape(light.getShape());
        return light;
    }

    public void update(View view) {
        for (PointLight light : pointLights) {
            light.updateUniforms(view);
        }
        for (HemisphereLight light : hemisphereLights) {
            light.updateUniforms(view);
        }
    }
}

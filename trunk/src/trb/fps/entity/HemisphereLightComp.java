package trb.fps.entity;

import java.awt.Color;
import trb.fps.property.Property;
import trb.jsg.util.Vec3;

public class HemisphereLightComp extends Component {
    public final Property<Color> skyColor = add("SkyColor", Color.WHITE);
    public final Property<Color> groundColor = add("GroundColor", Color.BLACK);

    public Vec3 getDirection() {
        return getComponent(Transform.class).get().getForward();
    }
}

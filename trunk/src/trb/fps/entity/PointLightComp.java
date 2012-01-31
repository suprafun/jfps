package trb.fps.entity;

import java.awt.Color;
import trb.fps.property.Property;

public class PointLightComp extends Component {
    public final Property<Color> color = add("Color", Color.WHITE);
    public final Property<Float> radius = add("Radius", 1f);
}

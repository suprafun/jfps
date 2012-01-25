package trb.fps.editor;

import java.awt.Color;
import trb.fps.property.Property;
import trb.fps.property.PropertyOwner;

public class PointLight extends PropertyOwner {
    public final Property<String> name = add("Name", "Name");
    public final Property<Float> positionx = add("PositionX", 0f);
    public final Property<Float> positiony = add("PositionY", 0f);
    public final Property<Float> positionz = add("PositionZ", 0f);
    public final Property<Color> color = add("Color", Color.WHITE);
    public final Property<Float> radius = add("Radius", 1f);
}

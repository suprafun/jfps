package trb.fps.editor;

import trb.fps.property.Property;
import trb.jsg.util.Mat4;

public class Transform extends Component {
    public final Property<Float> positionx = add("PositionX", 0f);
    public final Property<Float> positiony = add("PositionY", 0f);
    public final Property<Float> positionz = add("PositionZ", 0f);
    public final Property<Float> rotationx = add("RotationX", 0f);
    public final Property<Float> rotationy = add("RotationY", 0f);
    public final Property<Float> rotationz = add("RotationZ", 0f);

    public Mat4 get() {
        return new Mat4()
                .rotateEulerDeg(rotationx.get(), rotationy.get(), rotationz.get())
                .translate(positionx.get(), positiony.get(), positionz.get());
    }
}

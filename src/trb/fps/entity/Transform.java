package trb.fps.entity;

import trb.fps.property.Mutator;
import trb.fps.property.Property;
import trb.fps.property.SnapMutator;
import trb.jsg.util.Mat4;
import trb.jsg.util.Vec3;

public class Transform extends Component {
	private static final Mutator POS_MUTATOR = new SnapMutator(0.1f);
	private static final Mutator ROT_MUTATOR = new SnapMutator(10);

    public final Property<Float> positionx = add("PositionX", 0f);
    public final Property<Float> positiony = add("PositionY", 0f);
    public final Property<Float> positionz = add("PositionZ", 0f);
    public final Property<Float> rotationx = add("RotationX", 0f);
    public final Property<Float> rotationy = add("RotationY", 0f);
    public final Property<Float> rotationz = add("RotationZ", 0f);

	public Transform() {
		positionx.setUserData(Mutator.class, POS_MUTATOR);
		positiony.setUserData(Mutator.class, POS_MUTATOR);
		positionz.setUserData(Mutator.class, POS_MUTATOR);
		rotationx.setUserData(Mutator.class, ROT_MUTATOR);
		rotationy.setUserData(Mutator.class, ROT_MUTATOR);
		rotationz.setUserData(Mutator.class, ROT_MUTATOR);
	}

    public Mat4 get() {
        return new Mat4()
                .setEulerDeg(new Vec3(rotationx.get(), rotationy.get(), rotationz.get()))
                .setTranslation_(new Vec3(positionx.get(), positiony.get(), positionz.get()));
    }

    public void set(Mat4 mat) {
        Vec3 pos = mat.getTranslation();
        positionx.set(pos.x);
        positiony.set(pos.y);
        positionz.set(pos.z);
        Vec3 eulerDeg = mat.getEulerDeg();
        rotationx.set(eulerDeg.x);
        rotationy.set(eulerDeg.y);
        rotationz.set(eulerDeg.z);
    }
}

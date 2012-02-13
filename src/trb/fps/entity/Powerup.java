package trb.fps.entity;

import trb.fps.property.Property;

public class Powerup extends Component {
	public enum Type {HEALTH_10, AMMO_10}

	public final Property<Type> type = add("Type", Type.HEALTH_10);
}

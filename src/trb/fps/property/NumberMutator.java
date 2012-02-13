package trb.fps.property;

public class NumberMutator implements Mutator {

	public enum Type {LINEAR, EXPONENTIAL};

	private final Type type;
	private final float config;


	public NumberMutator() {
		this(Type.EXPONENTIAL, 1.005f);
	}

	public NumberMutator(Type type, float snap) {
		this.type = type;
		this.config = snap;
	}

    public Object mutate(Class type, Object valueStart, int amount) {
        if (Double.class == type || double.class == type) {
			return mutateDouble((Double) valueStart, amount);
        } else if (Float.class == type || float.class == type) {
            return (float) mutateDouble((Float) valueStart, amount);
        } else if (Integer.class == type || int.class == type) {
            return ((Integer) valueStart) + amount;
        } else if (Long.class == type || long.class == type) {
            return ((Long) valueStart) + amount;
        } else if (Short.class == type || short.class == type) {
            return ((Short) valueStart) + amount;
        } else if (Byte.class == type || byte.class == type) {
            return ((Byte) valueStart) + amount;
        }

        return valueStart;
    }

	private double mutateDouble(double start, int dx) {
		if (type == Type.EXPONENTIAL) {
			return pow(start, dx, config);
		}
		return start + dx * config;
	}
    
    public static double pow(double start, int dx, double base) {
        double factor = Math.pow(base, Math.abs(dx));
        double sign = dx < 0 ? -1 : 1;
        return start + Math.max(1, Math.abs(start)) * (factor - 1) * sign;
    }
}

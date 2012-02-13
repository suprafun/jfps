package trb.fps.property;

public class EnumMutator implements Mutator {
	public Object mutate(Class type, Object valueStart, int amount) {
		if (Enum.class.isAssignableFrom(type)) {
			Enum e = (Enum) valueStart;
			Object[] constants = type.getEnumConstants();
			return constants[Math.max(0, Math.min(constants.length-1, e.ordinal() + amount))];
		}
		return valueStart;
	}
}

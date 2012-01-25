package trb.fps.property;

public class NumberMutator implements Mutator {

    public Object mutate(Class type, Object valueStart, int amount) {
        if (Double.class == type || double.class == type) {
            return pow((Double) valueStart, amount);
        } else if (Float.class == type || float.class == type) {
            return (float) pow((Float) valueStart, amount);
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
    
    public static double pow(double start, int dx) {
        double factor = Math.pow(1.005, Math.abs(dx));
        double sign = dx < 0 ? -1 : 1;
        return start + Math.max(1, Math.abs(start)) * (factor - 1) * sign;
    }
}

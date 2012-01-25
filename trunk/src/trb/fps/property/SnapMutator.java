package trb.fps.property;

public class SnapMutator implements Mutator {

    float snap = 0.25f;

    public SnapMutator() {

    }

    public Object mutate(Class type, Object valueStart, int amount) {
        if (Float.class == type || float.class == type) {
            float value = (Float) valueStart;
            return snap(value + amount * snap, snap);
        }

        return valueStart;
    }

    public static float snap(float value, float snap) {
        return Math.round(value / snap) * snap;
    }

    public static void main(String[] args) {
        for (float value = 0f; value < 4f; value+=0.37f) {
            float snap = 0.25f;
            float dif = value % snap;
            float snapped = value - dif;
            if (dif > (snap / 2)) {
                snapped += snap;
            }
            System.out.println("" + value + " " + dif + " " + snapped);
        }
    }
}

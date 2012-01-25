package trb.fps.property;

public interface Mutator {
    Object mutate(Class type, Object valueStart, int amount);
}

package trb.fps.predict;

public interface TimedState<T extends TimedState, I extends TimedInput> {

    public T setTime(long time);

    public long getTime();

    /**
     * The predicted state is rewinded and inputs reaplied if the state is not
     * within threshold.
     */
    public boolean withinPredictThreshold(T state);

    /**
     * Update this state by the specified input the amount of time between the
     * input and this state. (timeDeltaMillis = input.getTime() - getTime()).
     * The time of the returned TimedState must bet the same as the input.
     */
    public T update(I input);

    public T interpolate(float t, T s2);
}

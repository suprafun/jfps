package trb.fps.predict;

public class DelayedInterpolatedState<T extends TimedState> {

    AnimatedState<T> animatedState;
    long prevStateTime = 0;
    T prevState;
    long newestStateTime = 0;
    T newestState;
    T currentState;
    long currentTime = 0;
    long prevTime = 0;
    float averageTimeBetweenPackets = 150;
    long timeCorrection = 0;
    float averageJitter = 50;

    public DelayedInterpolatedState(T initialState) {
        prevState = initialState;
        newestState = initialState;
        currentState = initialState;
        animatedState = new AnimatedState(initialState);
    }

    public T getNewestState() {
        return newestState;
    }

    public T getCurrentState() {
        return currentState;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void update(long now, T timedState) {
        long delta = now - prevTime;
        prevTime = now;

        if (timedState != null) {
            animatedState.add(timedState);
            prevState = newestState;
            prevStateTime = newestStateTime;
            newestState = timedState;
            newestStateTime = now;

            if (prevState.getTime() != 0 && newestState.getTime() != 0) {
                if (currentTime == 0) {
                    currentTime = newestState.getTime();
                }
                if (Math.abs(newestState.getTime() - prevState.getTime()) < 1000) {
                    float timeBetweenPackets = newestState.getTime() - prevState.getTime();
                    averageTimeBetweenPackets += (timeBetweenPackets - averageTimeBetweenPackets) * 0.1f;

                    long recieveDelta = newestStateTime - prevStateTime;
                    long sendDelta = newestState.getTime() - prevState.getTime();
                    long jitter = recieveDelta - sendDelta;
                    averageJitter += (jitter - averageJitter) * 0.1f;

                    long currentTimeBehindNewestState = newestState.getTime() - currentTime;
                    long targetTimeBehindNewestState = (long) (averageTimeBetweenPackets + averageJitter * 2);
                    timeCorrection = targetTimeBehindNewestState - currentTimeBehindNewestState;
                }
            }
        }

        long correction = 0;
        if (Math.abs(timeCorrection) > 0) {
            correction = timeCorrection / Math.abs(timeCorrection);
        }
        timeCorrection -= correction;
        currentTime += (delta - correction);
        if (animatedState.size() > 0) {
            currentTime = Math.min(animatedState.newest().getTime(), currentTime);
            currentTime = Math.max(animatedState.newest().getTime() - 1000, currentTime);
            currentState = animatedState.interpolate(currentTime);
            animatedState.removeOlderThan(animatedState.newest().getTime() - 1000);
        }
    }
}

package trb.fps.predict.app;

import trb.fps.predict.PredictedState;
import trb.fps.predict.TimedInput;
import trb.fps.predict.TimedState;

public class Test {

    public static void main(String[] args) {
        PredictedState predictedState = new PredictedState(new Position(0l, 0f, 0f));
        check(predictedState.updateAndCorrect(new PositionInput(1000l, 1f, 0f), null), 1f, 0);
        check(predictedState.updateAndCorrect(new PositionInput(2000l, 1f, 0f), null), 2f, 0);
        check(predictedState.updateAndCorrect(new PositionInput(3000l, 1f, 0f), new Position(0l, 0f, 1f)), 3f, 1);
    }

    private static void check(TimedState state, float x, float y) {
        Position p = (Position) state;
        System.out.println(state + " " + p.distance(new Position(0l, x, y)));
    }

    static class Position implements TimedState {

        final long time;
        final float x;
        final float y;

        Position(long time, float x, float y) {
            this.time = time;
            this.x = x;
            this.y = y;
        }

        public Position setTime(long time) {
            return new Position(time, x, y);
        }

        public long getTime() {
            return time;
        }

        public boolean withinPredictThreshold(TimedState state) {
            Position pos = (Position) state;
            return distance(pos) < 0.1;
        }

        float distance(TimedState state) {
            Position s = (Position) state;
            float dx = s.x - x;
            float dy = s.y - y;
            return (float) Math.sqrt(dx * dx + dy * dy);
        }

        public Position update(TimedInput input) {
            if (input.getTime() < time) {
                Thread.dumpStack();
            }
            PositionInput posInput = (PositionInput) input;
            long timeDeltaMillis = posInput.getTime() - getTime();
            float newx = x + posInput.dx * timeDeltaMillis / 1000f;
            float newy = y + posInput.dy * timeDeltaMillis / 1000f;
            return new Position(input.getTime(), newx, newy);
        }

        public Position interpolate(float t, TimedState timedState) {
            Position s2 = (Position) timedState;
            return new Position((long) (time + t * (s2.time - time)), x + t * (s2.x - x), y + t * (s2.y - y));
        }

        @Override
        public String toString() {
            return "" + time + " x=" + x + " y=" + y;
        }
    }

    static class PositionInput implements TimedInput {

        final long time;
        final float dx;
        final float dy;

        PositionInput(long time, float dx, float dy) {
            this.time = time;
            this.dx = dx;
            this.dy = dy;
        }

        public long getTime() {
            return time;
        }
    }
}

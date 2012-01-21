package trb.fps.predict;

import java.util.ArrayList;
import java.util.List;

public class PredictedState<T extends TimedState, I extends TimedInput> {
    InputAndStateList moveList = new InputAndStateList();
    T currentState;

    public PredictedState(T startState) {
        this.currentState = startState;
    }

    public T updateAndCorrect(I timedInput, T correctState) {
        update(timedInput);
        correct(correctState);
        return currentState;
    }

    public void update(I timedInput) {
        currentState = (T) currentState.update(timedInput);
        moveList.add(new InputAndState(timedInput, currentState));
    }

    public void correct(TimedState correctState) {
        currentState = (T) moveList.correct(currentState, correctState);
    }

    public T getCurrentState() {
        return currentState;
    }

    /** The state and the last input used to create state */
    class InputAndState {

        final TimedInput input;
        final T timedState;

        public InputAndState(TimedInput input, T timedState) {
            this.input = input;
            this.timedState = timedState;
        }
    }

    class InputAndStateList {

        List<InputAndState> list = new ArrayList();

        void add(InputAndState move) {
            list.add(move);
        }

        TimedState correct(TimedState currentState, TimedState serverState) {
            if (serverState != null) {
                removeBefore(serverState.getTime());
                if (!isOldestWithinThresholdTo(serverState)) {
                    //System.out.println("perform correction " + list.get(0).timedState + " != " + serverState);
                    return update(serverState);
                }
            }
            return currentState;
        }

        private void removeBefore(long time) {
            while (list.size() > 0 && list.get(0).timedState.getTime() < time) {
                list.remove(0);
            }
        }

        private boolean isOldestWithinThresholdTo(TimedState state) {
            return (list.size() > 0 && list.get(0).timedState.withinPredictThreshold(state));
        }

        private TimedState update(TimedState currentState) {
            for (InputAndState oldMove : list) {
                currentState = currentState.update(oldMove.input);
            }
            return currentState;
        }
    }
}

package trb.fps.predicttest;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * Tests client side prediction by emulating client/server with delayed packages.
 */
public class PredictTest extends JComponent {

    static final float TRESHOLD = 0.2f;
    static final int RADIUS = 10;
    static final boolean[] keyState = new boolean[0xffff];
    static final Client[] clients = {new Client(0) , new Client(1)};
    static final Server server = new Server(clients);
    static boolean aiGoingLeft = false;
    static State shooterState = new State(0, 0);
    static State targetState = new State(0, 0);
    static final JComponent renderer = new JComponent() {

        @Override
        public void paintComponent(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            drawPlayer(g, shooterState, Color.CYAN);
            drawPlayer(g, targetState, Color.CYAN);
            for (int i=0; i<clients.length; i++) {
                Client client = clients[i];
                drawPlayer(g, server.currentState[i].newest().state, Color.BLUE.darker());
                drawPlayer(g, server.shooterState, Color.PINK);
                drawPlayer(g, server.targetState, Color.PINK);
                g.drawLine((int)server.shooterState.x, (int)server.shooterState.y
                        , (int)server.shooterState.x, (int)server.targetState.y);
                drawPlayer(g, client.interpolatedState[i].newestState.state, Color.RED.darker());
                drawPlayer(g, client.interpolatedState[i].currentState.state, Color.GREEN.darker());
                drawPlayer(g, client.predictedState.currentState.state, Color.BLACK);
            }
            g.drawLine(400, 0, 400, getHeight());
        }

        void drawPlayer(Graphics g, State state, Color color) {
            g.setColor(color);
            g.fillOval((int) state.x - RADIUS, (int) state.y - RADIUS, RADIUS * 2, RADIUS * 2);
        }
    };

    public static void main(String[] args) {
        renderer.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                keyState[e.getKeyCode()] = true;
            }

            @Override
            public void keyReleased(KeyEvent e) {
                keyState[e.getKeyCode()] = false;
            }
        });

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(100, 100, 640, 480);
        frame.add(renderer);
        frame.setVisible(true);

        renderer.requestFocusInWindow();

        for (final Client client : clients) {
            new Thread(new Runnable() {
                public void run() {
                    client.gameLoop(server);
                }
            }).start();
        }
        server.gameLoop();
    }

    static Input getInput(Client client) {
        if (client == clients[0]) {
            float dx = (keyState[KeyEvent.VK_LEFT] ? -150 : 0) + (keyState[KeyEvent.VK_RIGHT] ? 150 : 0);
            float dy = (keyState[KeyEvent.VK_UP] ? -150 : 0) + (keyState[KeyEvent.VK_DOWN] ? 150 : 0);
            boolean fire = keyState[KeyEvent.VK_SPACE];
            keyState[KeyEvent.VK_SPACE] = false;
            return new Input(dx, dy, fire);
        }

        if (aiGoingLeft && (clients[1].predictedState.currentState.state.x < 50)
                || !aiGoingLeft && (clients[1].predictedState.currentState.state.x > 350)) {
            aiGoingLeft = !aiGoingLeft;
        }
        return new Input(aiGoingLeft ? -150 : 150, 0, false);
    }

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Updates state using input to the specified time */
    static TimedState updateState(TimedState state, TimedInput timedInput, boolean doCollisions) {
        long timeDeltaMillis = timedInput.time - state.time;
        float x = state.state.x + timedInput.input.vx * timeDeltaMillis / 1000f;
        float y = state.state.y + timedInput.input.vy * timeDeltaMillis / 1000f;
        // do only on server
        if (doCollisions) {
            x = Math.min(400, x);
        }
        TimedState newState = new TimedState(timedInput.time, new State(x, y));
        return newState;
    }


    static class Client {
        private final int index;
        PredictedState predictedState = new PredictedState(new TimedState(System.currentTimeMillis(), new State(100, 100)));
        InterpolatedState[] interpolatedState = {new InterpolatedState(), new InterpolatedState()};
        DelayedFifo<TimedState[]> in = new DelayedFifo();
        DelayedFifo<PacketToServer> out = new DelayedFifo();

        Client(int index) {
            this.index = index;
        }

        void gameLoop(Server server) {
            while (true) {
                long now = System.currentTimeMillis();
                TimedInput timedInput = new TimedInput(now, getInput(this));
                TimedState[] timedStateFromServer = in.remove();
                timedStateFromServer = timedStateFromServer != null ? timedStateFromServer : new TimedState[2];
                predictedState.update(timedInput, timedStateFromServer[index]);
                for (int i = 0; i < interpolatedState.length; i++) {
                    interpolatedState[i].update(now, timedStateFromServer[i]);
                }
                out.add(new PacketToServer(timedInput, interpolatedState[0].currentTime, interpolatedState[1].currentTime));
                if (timedInput.input.fire) {
                    shooterState = clients[0].predictedState.currentState.state;
                    targetState = clients[0].interpolatedState[1].currentState.state;
                }

                renderer.repaint();
                sleep(16);
            }
        }
    }


    static class PacketToServer {
        TimedInput timedInput;
        // as seen by the sending client
        long[] clientTimes;

        PacketToServer(TimedInput timedInput, long... clientTimes) {
            this.timedInput = timedInput;
            this.clientTimes = clientTimes;
        }
    }


    static class Server {

        final Client[] clients;
        final AnimatedState[] currentState;

        State shooterState = new State(0, 0);
        State targetState = new State(0, 0);

        Server(Client[] clients) {
            this.clients = clients;
            currentState = new AnimatedState[] {
                new AnimatedState(clients[0].predictedState.currentState),
                new AnimatedState(clients[1].predictedState.currentState),
            };            
        }

        void gameLoop() {
            while (true) {
                tick();
                renderer.repaint();
                // run server at 10hz
                sleep(100);
            }
        }

        private void tick() {
            for (int clientIdx=0; clientIdx<clients.length; clientIdx++) {
                while (true) {
                    PacketToServer toServer = clients[clientIdx].out.remove();
                    if (toServer == null) {
                        break;
                    }

                    TimedState newState = updateState(currentState[clientIdx].newest(), toServer.timedInput, true);
                    currentState[clientIdx].add(newState);
                    currentState[clientIdx].removeOlderThan(newState.time-1000);
                    if (toServer.timedInput.input.fire) {
                        for (int targetIdx = 0; targetIdx < clients.length; targetIdx++) {
                            if (targetIdx != clientIdx) {
                                headShot(clientIdx, targetIdx, toServer.clientTimes);
                            }
                        }
                    }
                }
            }
            TimedState[] toClient = new TimedState[currentState.length];
            for (int i=0; i<toClient.length; i++) {
                toClient[i] = currentState[i].newest();
            };
            for (int i = 0; i < clients.length; i++) {
                clients[i].in.add(toClient.clone());
            }
        }


        void headShot(int shooterIdx, int targetIdx, long[] clientTimes) {
            shooterState = currentState[shooterIdx].newest().state;
            targetState = getClientStateAtTime(targetIdx, clientTimes[targetIdx]);
            if (Math.abs(targetState.x - shooterState.x) < RADIUS) {
                System.out.println("HIT");
            }
        }

        State getClientStateAtTime(int targetIdx, long time) {
            TimedState targetTimedState = currentState[targetIdx].interpolate(time);
            System.out.println("timeDelta at bullet check " + (targetTimedState.time - time));
            return targetTimedState.state;
        }
    }


    static class AnimatedState {

        List<TimedState> timedStateList = new ArrayList();

        AnimatedState() {}

        AnimatedState(TimedState initialState) {
            add(initialState);
        }

        public int size() {
            return timedStateList.size();
        }

        boolean isEmpty() {
            return timedStateList.isEmpty();
        }

        void add(TimedState state) {
            timedStateList.add(state);
        }

        void removeOlderThan(long oldestTime) {
            while (timedStateList.get(0).time < oldestTime) {
                timedStateList.remove(0);
            }
        }

        TimedState newest() {
            return timedStateList.get(timedStateList.size()-1);
        }

        TimedState interpolate(long time) {
            if (timedStateList.isEmpty()) {
                return new TimedState(time, new State(0, 0));
            }

            int i=0;
            while (i<timedStateList.size()) {
                if (time < timedStateList.get(i).time) {
                    break;
                }
                i++;
            }
            int i1 = Math.max(i-1, 0);
            int i2 = Math.min(i, timedStateList.size()-1);
            TimedState s1 = timedStateList.get(i1);
            TimedState s2 = timedStateList.get(i2);
            if (s1.time == s2.time) {
                return new TimedState(time, s2.state);
            }
            float t = (time - s1.time) / (float) (s2.time - s1.time);
            return new TimedState(time, s1.state.interpolate(t, s2.state));
        }
    }

    static class DelayedFifo<T> {
        List<TimedEntry> list = Collections.synchronizedList(new ArrayList());

        void add(final T object) {
            list.add(new TimedEntry(System.currentTimeMillis() + getDelay(), object));
        }

        T remove() {
            if (list.isEmpty()) {
                return null;
            }

            TimedEntry entry = list.get(0);
            if (System.currentTimeMillis() > entry.time) {
                return (T) list.remove(0).object;
            }
            return null;
        }

        private long getDelay() {
            return 300 + (int) (Math.random() * 100);
        }

        class TimedEntry {

            long time;
            Object object;

            TimedEntry(long time, Object object) {
                this.time = time;
                this.object = object;
            }
        }
    }
    
    static class Input {
        final float vx;
        final float vy;
        final boolean fire;

        Input(float vx, float vy, boolean fire) {
            this.vx = vx;
            this.vy = vy;
            this.fire = fire;
        }
    }
    

    static class State {
        final float x;
        final float y;

        State(float x, float y) {
            this.x = x;
            this.y = y;
        }

        float distance(State s) {
            float dx = s.x - x;
            float dy = s.y - y;
            return (float) Math.sqrt(dx*dx + dy*dy);
        }

        State interpolate(float t, State s2) {
            return new State(x + t * (s2.x - x), y + t * (s2.y - y));
        }
    }
    

    static class TimedState {
        final long time;
        final State state;

        TimedState(long time, State state) {
            this.time = time;
            this.state = state;
        }
    }


    static class TimedInput {

        final long time;
        final Input input;

        TimedInput(long time, Input input) {
            this.time = time;
            this.input = input;
        }
    }

    
    static class PredictedState {
        InputAndStateList moveList = new InputAndStateList();
        TimedState currentState;

        PredictedState(TimedState startState) {
            this.currentState = startState;
        }

        void update(TimedInput timedInput, TimedState correctState) {
            currentState = updateState(currentState, timedInput, false);
            InputAndState move = new InputAndState(timedInput.input, currentState);
            moveList.add(move);
            currentState = moveList.correct(currentState, correctState);
        }
    }


    static class InputAndState {
        final Input input;
        final TimedState timedState;

        public InputAndState(Input input, TimedState timedState) {
            this.input = input;
            this.timedState = timedState;
        }

        TimedInput getTimedInput() {
            return new TimedInput(timedState.time, input);
        }
    }


    static class InputAndStateList {
        List<InputAndState> list = new ArrayList();

        void add(InputAndState move) {
            list.add(move);
        }

        TimedState correct(TimedState currentState, TimedState serverState) {
            if (serverState != null) {
                removeBefore(serverState.time);
                if (!isOldestWithinThresholdTo(serverState.state)) {
                    System.out.println("perform correction " + list.get(0).timedState + " != " + serverState);
                    return update(serverState);
                }
            }
            return currentState;
        }

        private void removeBefore(long time) {
            while (list.size() > 0 && list.get(0).timedState.time < time) {
                list.remove(0);
            }
        }

        private boolean isOldestWithinThresholdTo(State state) {
            return (list.size() > 0 && list.get(0).timedState.state.distance(state) <= TRESHOLD);
        }

        private TimedState update(TimedState currentState) {
            for (InputAndState oldMove : list) {
                currentState = updateState(currentState, oldMove.getTimedInput(), false);
            }
            return currentState;
        }
    }


    static class InterpolatedState {
        AnimatedState animatedState = new AnimatedState();
        long prevStateTime = 0;
        TimedState prevState = new TimedState(0, new State(0, 0));
        long newestStateTime = 0;
        TimedState newestState = new TimedState(0, new State(0, 0));
        TimedState currentState = new TimedState(0, new State(0, 0));
        long currentTime = 0;
        long prevTime = 0;
        float averageTimeBetweenPackets = 150;
        long timeCorrection = 0;
        float averageJitter = 50;

        void update(long now, TimedState timedState) {
            long delta = now - prevTime;
            prevTime = now;
            
            if (timedState != null) {
                animatedState.add(timedState);
                prevState = newestState;
                prevStateTime = newestStateTime;
                newestState = timedState;
                newestStateTime = now;

                if (prevState.time != 0 && newestState.time != 0) {
                    if (currentTime == 0) {
                        currentTime = newestState.time;
                    }
                    if (Math.abs(newestState.time - prevState.time) < 1000) {
                        float timeBetweenPackets = newestState.time - prevState.time;
                        averageTimeBetweenPackets += (timeBetweenPackets - averageTimeBetweenPackets) * 0.1f;

                        long recieveDelta = newestStateTime - prevStateTime;
                        long sendDelta = newestState.time - prevState.time;
                        long jitter = recieveDelta - sendDelta;
                        averageJitter += (jitter - averageJitter) * 0.1f;

                        long currentTimeBehindNewestState = newestState.time - currentTime;
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
                currentTime = Math.min(animatedState.newest().time, currentTime);
                currentTime = Math.max(animatedState.newest().time - 1000, currentTime);
                currentState = animatedState.interpolate(currentTime);
                animatedState.removeOlderThan(animatedState.newest().time - 1000);
            }
        }
    }
}

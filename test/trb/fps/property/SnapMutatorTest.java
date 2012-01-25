package trb.fps.property;

import org.junit.Test;
import static org.junit.Assert.*;

public class SnapMutatorTest {

    @Test
    public void testSnap() {
        System.out.println("snap");
        assertEquals(0f, SnapMutator.snap(0.24f, 0.5f), 0.001);
        assertEquals(0f, SnapMutator.snap(-0.24f, 0.5f), 0.001);
        assertEquals(-0.5f, SnapMutator.snap(-0.26f, 0.5f), 0.001);
        assertEquals(-1.5f, SnapMutator.snap(-1.75f, 0.5f), 0.001);
        assertEquals(-2f, SnapMutator.snap(-1.76f, 0.5f), 0.001);
    }
}
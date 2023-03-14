package dev.mcannavan.dotdash;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParisTimingTest {
    @Test
    public void testGetters() {
        ParisTiming timing = new ParisTiming();
        timing.calculateSpeedFromWpm(20);

        assertEquals(60f, timing.getDitLength());
        assertEquals(180f, timing.getDahLength());
        assertEquals(180f, timing.getInterCharLength());
        assertEquals(60f, timing.getIntraCharLength());
        assertEquals(420f, timing.getInterWordLength());
        assertEquals(20f, timing.getWpm());
    }

    @Test
    public void testCalculateSpeedFromMillis() {
        ParisTiming timing = new ParisTiming();
        timing.calculateSpeedFromMillis(50f);

        assertEquals(50f, timing.getDitLength());
        assertEquals(150f, timing.getDahLength());
        assertEquals(150f, timing.getInterCharLength());
        assertEquals(50f, timing.getIntraCharLength());
        assertEquals(350f, timing.getInterWordLength());
    }

    @Test
    public void testCalculateSpeedFromWpm() {
        ParisTiming timing = new ParisTiming();
        timing.calculateSpeedFromWpm(10);
        assertEquals(120f, timing.getDitLength());
        assertEquals(360f, timing.getDahLength());
        assertEquals(360f, timing.getInterCharLength());
        assertEquals(120f, timing.getIntraCharLength());
        assertEquals(840f, timing.getInterWordLength());
        assertEquals(10f, timing.getWpm());

        ArithmeticException e = assertThrows(ArithmeticException.class, () ->
            timing.calculateSpeedFromWpm(0)
        );
    }
}
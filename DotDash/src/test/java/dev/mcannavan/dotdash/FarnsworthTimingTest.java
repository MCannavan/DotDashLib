package dev.mcannavan.dotdash;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FarnsworthTimingTest {

    @Test
    void calculateSpeedFromMillis_standardCase() {
        FarnsworthTiming farnsworthTiming = new FarnsworthTiming();
        farnsworthTiming.calculateSpeedFromMillis(550, 50);

        assertEquals(24, farnsworthTiming.getPWpm());
        assertEquals(5, farnsworthTiming.getFWpm());
        assertEquals(50, farnsworthTiming.getDitLength(), 0.01);
        assertEquals(150, farnsworthTiming.getDahLength(), 0.01);
        assertEquals(50, farnsworthTiming.getIntraCharLength(), 0.01);
        assertEquals(1650, farnsworthTiming.getInterCharLength(), 0.01);
        assertEquals(3850, farnsworthTiming.getInterWordLength(), 0.01);
    }

    @Test
    void calculateSpeedFromMillis_invalidInput() {
        FarnsworthTiming farnsworthTiming = new FarnsworthTiming();

        assertThrows(IllegalArgumentException.class, () -> farnsworthTiming.calculateSpeedFromMillis(-1, 100));
        assertThrows(IllegalArgumentException.class, () -> farnsworthTiming.calculateSpeedFromMillis(100, -1));
        assertThrows(IllegalArgumentException.class, () -> farnsworthTiming.calculateSpeedFromMillis(-1, -1));
    }

    @Test
    void calculateSpeedFromWpm_standardCase() {
        FarnsworthTiming farnsworthTiming = new FarnsworthTiming();
        farnsworthTiming.calculateSpeedFromWpm(5, 13);

        assertEquals(13, farnsworthTiming.getPWpm());
        assertEquals(5, farnsworthTiming.getFWpm());
        assertEquals(92.31, farnsworthTiming.getDitLength(), 0.01);
        assertEquals(276.92, farnsworthTiming.getDahLength(), 0.01);
        assertEquals(92.31, farnsworthTiming.getIntraCharLength(), 0.01);
        assertEquals(1442.91, farnsworthTiming.getInterCharLength(), 0.01);
        assertEquals(3366.80, farnsworthTiming.getInterWordLength(), 0.01);
    }
}
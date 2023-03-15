package dev.mcannavan.dotdash;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FarnsworthTimingTest {

    @Test
    void temp() {
        FarnsworthTiming temp = MorseTimingFactory.createFarnsworthTimingFromMs(550,50);
        assertEquals(24, temp.getPWpm());
        assertEquals(5, temp.getFWpm());
        assertEquals(50, temp.getDitLength(), 0.01, "");
        assertEquals(150, temp.getDahLength(), 0.01, "");
        assertEquals(50, temp.getIntraCharLength(), 0.01, "");
        assertEquals(1650, temp.getInterCharLength(), 0.01, "");
        assertEquals(3850, temp.getInterWordLength(), 0.01, "");

        assertThrows(IllegalArgumentException.class, () -> temp.calculateSpeedFromMillis(-1, 100), "");

    }


    //calculateSpeedFromMillis test cases

    @Test
    void calculateSpeedFromMillis_WithValidInput_ReturnsCorrectValues() {

    }

    @Test
    void calculateSpeedFromMillis_WithZeroAndNegativeInput_ThrowsException() {

    }

    @Test
    void calculateSpeedFromMillis_WithInputCausingOverflowAndSmallRatio_ThrowsException() {

    }

    @Test
    void calculateSpeedFromMillis_WithEdgeCaseInput_ReturnsCorrectValues() {

    }


}
package dev.mcannavan.dotdash;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FarnsworthTimingTest {

    //calculateSpeedFromMillis test cases

    @Test
    void calculateSpeedFromMillis_WithValidInput_ReturnsCorrectValues() {
        FarnsworthTiming temp = MorseTimingFactory.createFarnsworthTimingFromMs(550,50);
        assertEquals(24, temp.getPWpm());
        assertEquals(5, temp.getFWpm());
        assertEquals(50, temp.getDitLength(), 0.01, "");
        assertEquals(150, temp.getDahLength(), 0.01, "");
        assertEquals(50, temp.getIntraCharLength(), 0.01, "");
        assertEquals(1650, temp.getInterCharLength(), 0.01, "");
        assertEquals(3850, temp.getInterWordLength(), 0.01, "");
    }

    @Test
    void calculateSpeedFromMillis_WithZeroAndNegativeInput_ThrowsException() {

        //test correct instantiation with incorrect method calls
        FarnsworthTiming correctInstantiationWithTwoParams = MorseTimingFactory.createFarnsworthTimingFromMs(550,50);
        assertThrows(IllegalArgumentException.class, () -> correctInstantiationWithTwoParams.calculateSpeedFromMillis(-1, 100), "");
        assertThrows(IllegalArgumentException.class, () -> correctInstantiationWithTwoParams.calculateSpeedFromMillis(100, -1), "");
        assertThrows(IllegalArgumentException.class, () -> correctInstantiationWithTwoParams.calculateSpeedFromMillis(0, 100), "");
        assertThrows(IllegalArgumentException.class, () -> correctInstantiationWithTwoParams.calculateSpeedFromMillis(100, 0), "");
        assertThrows(IllegalArgumentException.class, () -> correctInstantiationWithTwoParams.calculateSpeedFromMillis(-1, 0), "");

        FarnsworthTiming correctInstantiationWithOneParam = MorseTimingFactory.createFarnsworthTimingFromMs(100);


        //test incorrect factory calls
        assertThrows(IllegalArgumentException.class, () -> { FarnsworthTiming incorrectInstantiation = MorseTimingFactory.createFarnsworthTimingFromMs(-1,100);});

    }

    @Test
    void calculateSpeedFromMillis_WithInputCausingOverflowAndSmallRatio_ThrowsException() {

    }

    @Test
    void calculateSpeedFromMillis_WithEdgeCaseInput_ReturnsCorrectValues() {

    }


}
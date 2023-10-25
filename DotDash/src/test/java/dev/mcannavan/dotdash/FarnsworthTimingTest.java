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
        assertEquals(50, temp.getDitLength(), 0.01, "failed getDitLength. Expected 50, actual: " + temp.getDitLength());
        assertEquals(150, temp.getDahLength(), 0.01, "failed getDahLength. Expected 150, actual: " + temp.getDahLength());
        assertEquals(50, temp.getIntraCharLength(), 0.01, "failed getIntraCharLength. Expected 50, actual: " + temp.getIntraCharLength());
        assertEquals(1650, temp.getInterCharLength(), 0.01, "failed getInterCharLength. Expected 1650, actual: " + temp.getInterCharLength());
        assertEquals(3850, temp.getInterWordLength(), 0.01, "failed getInterWordLength. Expected 3850, actual: " + temp.getInterWordLength());
    }

    @Test
    void calculateSpeedFromMillis_WithZeroAndNegativeInput_ThrowsException() {

        //test correct instantiation with incorrect method calls
        FarnsworthTiming correctInstantiationWithTwoParams = MorseTimingFactory.createFarnsworthTimingFromMs(550,50);
        assertThrows(IllegalArgumentException.class, () -> correctInstantiationWithTwoParams.calculateSpeedFromMillis(-1, 100), "failed negative fMs throws exception");
        assertThrows(IllegalArgumentException.class, () -> correctInstantiationWithTwoParams.calculateSpeedFromMillis(100, -1), "failed negative pMs throws exception");
        assertThrows(IllegalArgumentException.class, () -> correctInstantiationWithTwoParams.calculateSpeedFromMillis(0, 100), "failed zero fMs throws exception");
        assertThrows(IllegalArgumentException.class, () -> correctInstantiationWithTwoParams.calculateSpeedFromMillis(100, 0), "failed zero pMs throws exception");

        FarnsworthTiming correctInstantiationWithOneParam = MorseTimingFactory.createFarnsworthTimingFromMs(100);
        assertThrows(IllegalArgumentException.class, () -> correctInstantiationWithOneParam.calculateSpeedFromMillis(-1, 100), "failed negative fMs throws exception");
        assertThrows(IllegalArgumentException.class, () -> correctInstantiationWithOneParam.calculateSpeedFromMillis(100, -1), "failed negative pMs throws exception");
        assertThrows(IllegalArgumentException.class, () -> correctInstantiationWithOneParam.calculateSpeedFromMillis(0, 100), "failed zero fMs throws exception");
        assertThrows(IllegalArgumentException.class, () -> correctInstantiationWithOneParam.calculateSpeedFromMillis(100, 0), "failed zero pMs throws exception");

        //test incorrect factory calls
        assertThrows(IllegalArgumentException.class, () -> { MorseTimingFactory.createFarnsworthTimingFromMs(-1,100);},"failed negative fMs throws exception");
        assertThrows(IllegalArgumentException.class, () -> { MorseTimingFactory.createFarnsworthTimingFromMs(100, -1);},"failed negative pMs throws exception");

    }

    @Test
    void calculateSpeedFromMillis_WithInputCausingOverflowAndSmallRatio_ThrowsException() {

    }

    @Test
    void calculateSpeedFromMillis_WithEdgeCaseInput_ReturnsCorrectValues() {

    }


}
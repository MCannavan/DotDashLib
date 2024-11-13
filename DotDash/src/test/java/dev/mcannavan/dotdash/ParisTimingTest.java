package dev.mcannavan.dotdash;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParisTimingTest {
    @BeforeEach
    void setUp() {
    }

    @Test
    void calculateSpeedFromMillis_WithValidInput_ReturnsCorrectValues() {
        ParisTiming temp = MorseTimingFactory.createParisTimingFromMs(200);
        assertEquals(0, temp.getWpm());
    }

    @Test
    void calculateSpeedFromMillis_WithZeroAndNegativeInput_ThrowsException() {

    }
}
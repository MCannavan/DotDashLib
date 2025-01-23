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
        ParisTiming correctAviationAverage = new ParisTiming();
        correctAviationAverage.calculateSpeedFromMillis(240);
        assertEquals(5, correctAviationAverage.getWpm(), "failed get WPM from correct Ms");
        assertEquals(240, correctAviationAverage.getDitLength(), "failed getDitLength from correct Ms");
        assertEquals(720, correctAviationAverage.getDahLength(), "failed getDahLength from correct Ms");
        assertEquals(240, correctAviationAverage.getIntraCharLength(), "failed getIntraCharLength from correct Ms");
        assertEquals(720, correctAviationAverage.getInterCharLength(), "failed getInterCharLength from correct Ms");
        assertEquals(1680, correctAviationAverage.getInterWordLength(), "failed getInterWordLength from correct Ms");

        ParisTiming correctRadioAverage = new ParisTiming();
        correctRadioAverage.calculateSpeedFromMillis(60);
        assertEquals(20, correctRadioAverage.getWpm(), "failed get WPM from correct Ms");
        assertEquals(60, correctRadioAverage.getDitLength(), "failed getDitLength from correct Ms");
        assertEquals(180, correctRadioAverage.getDahLength(), "failed getDahLength from correct Ms");
        assertEquals(60, correctRadioAverage.getIntraCharLength(), "failed getIntraCharLength from correct Ms");
        assertEquals(180, correctRadioAverage.getInterCharLength(), "failed getInterCharLength from correct Ms");
        assertEquals(420, correctRadioAverage.getInterWordLength(), "failed getInterWordLength from correct Ms");
    }

    @Test
    void calculateSpeedFromMillis_WithZeroAndNegativeInput_ThrowsException() {
        ParisTiming incorrectTiming = new ParisTiming();
        assertThrows(IllegalArgumentException.class, () -> incorrectTiming.calculateSpeedFromMillis(0));
        assertThrows(IllegalArgumentException.class, () -> incorrectTiming.calculateSpeedFromMillis(-10));

    }

    @Test
    void calculateSpeedFromWpm_WithValidInput_ReturnsCorrectValues() {
        ParisTiming correctAviationAverage = new ParisTiming();
        correctAviationAverage.calculateSpeedFromWpm(5);
        assertEquals(5, correctAviationAverage.getWpm(), "failed get WPM from correct Wpm");
        assertEquals(240, correctAviationAverage.getDitLength(), "failed getDitLength from correct Wpm");
        assertEquals(720, correctAviationAverage.getDahLength(), "failed getDahLength from correct Wpm");
        assertEquals(240, correctAviationAverage.getIntraCharLength(), "failed getIntraCharLength from correct Wpm");
        assertEquals(720, correctAviationAverage.getInterCharLength(), "failed getInterCharLength from correct Wpm");
        assertEquals(1680, correctAviationAverage.getInterWordLength(), "failed getInterWordLength from correct Wpm");
    }

    @Test
    void calculateSpeedFromWpm_WithZeroAndNegativeInput_ThrowsException() {
        ParisTiming incorrectTiming = new ParisTiming();
        assertThrows(IllegalArgumentException.class, () -> incorrectTiming.calculateSpeedFromWpm(0));
        assertThrows(IllegalArgumentException.class, () -> incorrectTiming.calculateSpeedFromWpm(-10));
    }

}
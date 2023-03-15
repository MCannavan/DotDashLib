package dev.mcannavan.dotdash;

/**
 * A set of Morse timings following the PARIS approach. Implements {@link IMorseTiming}.
 */
public class ParisTiming implements IMorseTiming {
    private float ditLengthMillis; // length of a dit (dot)
    private float dahLengthMillis; // length of a dah (dash)
    private float intraCharLengthMillis; // space between dits and dahs within a character
    private float interCharLengthMillis; // space between characters within a word
    private float interWordLengthMillis; // space between words
    private float wpm; // words per minute

    /**
     * Returns the length of a dit (dot) in milliseconds.
     */
    @Override
    public float getDitLength() {
        return ditLengthMillis;
    }

    /**
     * Returns the length of a dah (dash) in milliseconds.
     */
    @Override
    public float getDahLength() {
        return dahLengthMillis;
    }

    /**
     * Returns the space between characters within a word in milliseconds.
     */
    @Override
    public float getInterCharLength() {
        return interCharLengthMillis;
    }

    /**
     * Returns the space between dits and dahs within a character in milliseconds.
     */
    @Override
    public float getIntraCharLength() {
        return intraCharLengthMillis;
    }

    /**
     * Returns the space between words in milliseconds.
     */
    @Override
    public float getInterWordLength() {
        return interWordLengthMillis;
    }

    /**
     * Returns the words per minute.
     */
    public float getWpm() {
        return wpm;
    }

    /**
     * Calculates the length of all instance variables from the given length of a dit in milliseconds.
     *
     * @param ms A non-negative, non-zero {@code float} representing the length of 1 unit (equivalent to a dit) in milliseconds.
     * @throws IllegalArgumentException If input is negative or zero.
     */
    public void calculateSpeedFromMillis(float ms) throws IllegalArgumentException {
        if (ms < 0.005) {
            throw new IllegalArgumentException("Input ms must be greater than or equal to " + 0.005 + ". Actual value: " + ms);
        }
        ms = (float) (Math.round((double) ms * 1000d) / 1000d);
        wpm = Math.round((60f * (1f / (ms / 1000f)) / 50f) * 100f) / 100f;
        ditLengthMillis = ms;
        dahLengthMillis = ms * 3;
        interCharLengthMillis = ms * 3;
        intraCharLengthMillis = ms;
        interWordLengthMillis = ms * 7;
    }

    /**
     * Calculate the length of all instance variables from a given words per minute.
     *
     * @param wpm A non-negative, non-zero {@code float} representing the desired words per minute.
     * @throws IllegalArgumentException If input wpm is negative or zero or greater than the maximum allowed WPM.
     * @throws ArithmeticException If wpm is too small, causing a floating-point overflow when calculating the output
     */
    public void calculateSpeedFromWpm(float wpm) throws IllegalArgumentException, ArithmeticException {
        if (wpm <= 0) {
            throw new IllegalArgumentException("Input wpm must be greater than 0. Actual value: " + wpm);
        }
        double ms = 1f / ((wpm * 50f) / 60f) * 1000f;
        if (ms > Float.MAX_VALUE) { //checking for overflow during calculation, as actual min value hard to determine
            throw new ArithmeticException("");
        }
        calculateSpeedFromMillis((float) ms);
    }
}

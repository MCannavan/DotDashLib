package dev.mcannavan.dotdash;

/**
 * This class represents the Paris timing for Morse code, implementing the {@link IMorseTiming} interface.
 * It provides methods to calculate Morse code timings based on words per minute or milliseconds per dit.
 */
class ParisTiming implements IMorseTiming {
    private float ditLengthMillis; // length of a dit (dot)
    private float dahLengthMillis; // length of a dah (dash)
    private float intraCharLengthMillis; // space between dits and dahs within a character
    private float interCharLengthMillis; // space between characters within a word
    private float interWordLengthMillis; // space between words
    private float wpm; // words per minute

    @Override
    public float getDitLength() {
        return ditLengthMillis;
    }

    @Override
    public float getDahLength() {
        return dahLengthMillis;
    }

    @Override
    public float getInterCharLength() {
        return interCharLengthMillis;
    }

    @Override
    public float getIntraCharLength() {
        return intraCharLengthMillis;
    }

    @Override
    public float getInterWordLength() {
        return interWordLengthMillis;
    }

    public float getWpm() {
        return wpm;
    }


    /**
     * Calculates the Morse code transmission speed from the input length of a dit (dot) in milliseconds.
     *
     * @param ms the length of a dit (dot) in milliseconds
     * @throws IllegalArgumentException if the input length of a dit is less than 0
     */
    public void calculateSpeedFromMillis(float ms) throws IllegalArgumentException {
        if (ms < 0) {
            throw new IllegalArgumentException("Input ms must be greater than or equal to " + 0 + ". Actual value: " + ms);
        }
        wpm = (60f * (1f / (ms / 1000)) / 50f);
        ditLengthMillis = Math.round(ms);
        dahLengthMillis = Math.round(ms * 3);
        intraCharLengthMillis = Math.round(ms);
        interCharLengthMillis = Math.round(ms * 3);
        interWordLengthMillis = Math.round(ms * 7);
    }

    /**
     * Calculates the Morse code transmission speed from the input words per minute (WPM).
     *
     * @param wpm the words per minute (WPM) using the PARIS approach.
     * @throws IllegalArgumentException if the input WPM is less than or equal to 0
     * @throws ArithmeticException if there is a floating-point overflow when calculating the length of a dit
     */
    public void calculateSpeedFromWpm(float wpm) throws IllegalArgumentException, ArithmeticException {
        if (wpm <= 0) {
            throw new IllegalArgumentException("Input wpm must be greater than 0. Actual value: " + wpm);
        }
        float ms = 1f / ((wpm * 50f) / 60f) * 1000;
        if (Float.isInfinite(7*ms)) { //check largest instance variable for overflow
            throw new ArithmeticException("floating-point overflow when calculating ms. ms = "+ ms + ", wpm = " + wpm);
        }
        calculateSpeedFromMillis(ms);
    }
}

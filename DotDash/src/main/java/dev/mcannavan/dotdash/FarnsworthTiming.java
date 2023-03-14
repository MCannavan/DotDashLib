package dev.mcannavan.dotdash;

/**
 * A set of morse timings following the Farnsworth approach. Implements {@link IMorseTiming}
 */
public class FarnsworthTiming implements IMorseTiming {

    private float ditLengthMillis; //length of a dit (dot)
    private float dahLengthMillis; //length of a dah (dash)
    private float intraCharLengthMillis; //space between dits and dahs within a character (in PARIS wpm / pWpm)
    private float interCharLengthMillis; //space between characters within a word (in Farnsworth wpm / fWpm)
    private float interWordLengthMillis; //space between words (in Farnsworth wpm /fWpm)
    private float pWpm; // the words per minute using the PARIS approach for dit, dah, and intra-character lang
    private float fWpm; // the words per minute using the Farnsworth approach for other spacing

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

    /**
     * Returns the words per minute (WPM) using the PARIS approach for dit, dah, and intra-character length.
     *
     * @return A {@code float} representing the PARIS WPM.
     */
    public float getpWpm() {
        return pWpm;
    }

    /**
     * Returns the words per minute (WPM) using the Farnsworth approach for inter-character and inter-word spacing.
     *
     * @return A {@code float} representing the Farnsworth WPM.
     */
    public float getfWpm() {
        return fWpm;
    }

    /**
     * Calculates the Morse timings based on the given Farnsworth and PARIS unit lengths in milliseconds.
     *
     * @param fMs A non-negative, non-zero {@code float} representing the Farnsworth unit length in milliseconds.
     * @param pMs A non-negative, non-zero {@code float} representing the PARIS unit length in milliseconds.
     * @throws IllegalArgumentException If the input values are negative or zero.
     */
    public void calculateSpeedFromMillis(float fMs, float pMs) throws IllegalArgumentException {
        // ...
    }

    /**
     * Calculates the Morse timings based on the given Farnsworth and PARIS words per minute (WPM).
     *
     * @param fWpm A non-negative, non-zero {@code float} representing the Farnsworth words per minute.
     * @param pWpm A non-negative, non-zero {@code float} representing the PARIS words per minute.
     * @throws IllegalArgumentException If the input values are negative or zero.
     */
    public void calculateSpeedFromWpm(float fWpm, float pWpm) throws IllegalArgumentException {
        // ...
    }

}

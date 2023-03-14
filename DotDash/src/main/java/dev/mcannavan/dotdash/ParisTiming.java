package dev.mcannavan.dotdash;

/**
 * A set of morse timings following the PARIS approach. Implements {@link IMorseTiming}
 */
public class ParisTiming implements IMorseTiming {
    private float ditLengthMillis; //length of a dit (dot)
    private float dahLengthMillis; //length of a dah (dash)
    private float intraCharLengthMillis; //space between dits and dahs within a character
    private float interCharLengthMillis; //space between characters within a word
    private float interWordLengthMillis; //space between words
    private float wpm; // the words per minute

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

    @Override
    public float getWpm() {
        return wpm;
    }

    /**
     * Calculates the length of all instance variables from the given length of a dit in milliseconds.
     *
     * @param ms a non-negative, non-zero {@code float} representing the length of 1 unit (equivalent to a dit) in milliseconds
     * @throws IllegalArgumentException If input is negative or zero
     */
    public void calculateSpeedFromMillis(float ms) throws IllegalArgumentException {
        if(ms <= 0) {
            throw new IllegalArgumentException("expected non-negative, non-zero  value of ms. Actual value:" + ms);
        }
        ms = Math.round(ms*100f)/100f; //round to 2 d.p.
        wpm = Math.round(60f * (1f / (ms / 1000f)) / 50f *100f)/100f; //calculate the rounded wpm from the rounded ms
        ditLengthMillis = ms;
        dahLengthMillis = ms*3;
        interCharLengthMillis = ms*3;
        intraCharLengthMillis = ms;
        interWordLengthMillis = ms*7;
    }

    /**
     * Calculate the length of all instance variables from a given words per minute.
     *
     * @param wpm a non-negative, non-zero {@code float} representing the desired words per minute.
     * @throws IllegalArgumentException If input is negative or zero
     */
    public void calculateSpeedFromWpm(float wpm) throws IllegalArgumentException {
        if(wpm <= 0) {
            throw new IllegalArgumentException("expected non-negative, non-zero value of wpm. Actual value:" + wpm);
        }
        float ms = 1f / ((wpm * 50f) / 60f) * 1000f;

        calculateSpeedFromMillis(ms);
    }
}

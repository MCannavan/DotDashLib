package dev.mcannavan.dotdash;

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

    public void calculateSpeedFromMillis(float ms) throws IllegalArgumentException {
        if (ms < 0) {
            throw new IllegalArgumentException("Input ms must be greater than or equal to " + 0 + ". Actual value: " + ms);
        }
        //ms = (float) (Math.round((double) ms * 1000d) / 1000d);
        wpm = (60f * (1f / (ms / 1000f)) / 50f);
        ditLengthMillis = ms;
        dahLengthMillis = ms * 3;
        interCharLengthMillis = ms * 3;
        intraCharLengthMillis = ms;
        interWordLengthMillis = ms * 7;
    }

    public void calculateSpeedFromWpm(float wpm) throws IllegalArgumentException, ArithmeticException {
        if (wpm <= 0) {
            throw new IllegalArgumentException("Input wpm must be greater than 0. Actual value: " + wpm);
        }
        float ms = 1f / ((wpm * 50f) / 60f) * 1000f;
        if (Float.isInfinite(7*ms)) { //check largest instance variable for overflow
            throw new ArithmeticException("floating-point overflow when calculating ms. ms = "+ ms + ", wpm = " + wpm);
        }
        calculateSpeedFromMillis(ms);
    }
}

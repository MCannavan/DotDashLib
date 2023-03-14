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
        return 0;
    }

    @Override
    public float getDahLength() {
        return 0;
    }

    @Override
    public float getInterCharLength() {
        return 0;
    }

    @Override
    public float getIntraCharLength() {
        return 0;
    }

    @Override
    public float getInterWordLength() {
        return 0;
    }

    @Override
    public float getWpm() {
        return 0;
    }

    public void calculateSpeedFromMillis(float ms, float fms) {

    }

    public void calculateSpeedFromWpm(float wpm, float fwpm) throws IllegalArgumentException {
    }

}

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

    public float getpWpm() {
        return pWpm;
    }

    public float getfWpm() {
        return fWpm;
    }

    public void calculateSpeedFromMillis(float pMs, float fMs) throws IllegalArgumentException {
        if (pMs <= 0 || fMs <= 0) {
            throw new IllegalArgumentException("expected non-negative, non-zero values of pMs and fMs. Actual values: pMs=" + pMs + ", fMs=" + fMs);
        }

        ParisTiming parisTiming = new ParisTiming();
        parisTiming.calculateSpeedFromMillis(pMs);
        ditLengthMillis = parisTiming.getDitLength();
        dahLengthMillis = parisTiming.getDahLength();
        intraCharLengthMillis = parisTiming.getIntraCharLength();

        float pWpm = parisTiming.getWpm();
        float fWpm = 60 / ((19 * fMs) / 1000f + 37.2f/pWpm);

        this.pWpm = pWpm;
        this.fWpm = fWpm;

        interCharLengthMillis = 3 * fMs;
        interWordLengthMillis = 7 * fMs;

    }

    public void calculateSpeedFromWpm(float pWpm, float fWpm) throws IllegalArgumentException {
        if (pWpm <= 0 || fWpm <= 0) {
            throw new IllegalArgumentException("expected non-negative, non-zero values of pWpm and fWpm. Actual values: pWpm=" + pWpm + ", fWpm=" + fWpm);
        }
        this.pWpm = pWpm;
        this.fWpm = fWpm;

        ParisTiming parisTiming = new ParisTiming();
        parisTiming.calculateSpeedFromWpm(pWpm);
        ditLengthMillis = parisTiming.getDitLength();
        dahLengthMillis = parisTiming.getDahLength();
        intraCharLengthMillis = parisTiming.getIntraCharLength();

        float fMs = (((60/fWpm) - (37.2f/pWpm)) * 1000f) / 19f;

        interCharLengthMillis = 3*fMs;
        interWordLengthMillis = 7*fMs;
    }

}
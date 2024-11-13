package dev.mcannavan.dotdash;

public class FarnsworthTiming implements IMorseTiming {

    private float ditLengthMillis; //length of a dit (dot)
    private float dahLengthMillis; //length of a dah (dash)
    private float intraCharLengthMillis; //space between dits and dahs within a character (in PARIS ms / wMs)
    private float interCharLengthMillis; //space between characters within a word (in Farnsworth ms / fMs)
    private float interWordLengthMillis; //space between words (in Farnsworth ms /fMs)
    private float pWpm; // the words per minute using the PARIS approach for dit, dah, and intra-character lang, i.e. the character transmission speed
    private float fWpm; // the words per minute using the Farnsworth approach for all other spacing, i.e. the overall transmission speed

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

    public float getPWpm() {
        return pWpm;
    }

    public float getFWpm() {
        return fWpm;
    }

    public void calculateSpeedFromMillis(float fMs, float pMs) throws IllegalArgumentException {
        if (fMs <= 0) {
            throw new IllegalArgumentException("expected non-negative, non-zero value for fMs, but got " + fMs + ".");
        } else if (pMs <= 0) {
            throw new IllegalArgumentException("expected non-negative, non-zero value for pMs, but got " + pMs + ".");
        }

        float pWpm = (60f * (1f / (pMs / 1000)) / 50f);
        float fWpm = 60 / ((19 * fMs) / 1000f + 37.2f / pWpm);
        this.pWpm = pWpm;
        this.fWpm = fWpm;

        ditLengthMillis = Math.round(pMs);
        dahLengthMillis = Math.round(3* pMs);
        intraCharLengthMillis = Math.round(pMs);

        interCharLengthMillis = Math.round(3 * fMs);
        interWordLengthMillis = Math.round(7 * fMs);
    }

    public void calculateSpeedFromWpm(float fWpm, float pWpm) throws IllegalArgumentException, ArithmeticException {
        if (fWpm <= 0) {
            throw new IllegalArgumentException("expected non-negative, non-zero value for fWpm, but got " + fWpm + ".");
        } else if (pWpm <= 0) {
            throw new IllegalArgumentException("expected non-negative, non-zero value for pWpm, but got " + pWpm + ".");
        }

        this.pWpm = pWpm;
        this.fWpm = fWpm;

        //float fMs = (float) (((60 / fWpm) - (37.2 / pWpm)) * 1000) / 19;
        float fMs = (float) 1 / ((fWpm * 50) / 60) * 1000;
        float pMs = (float) 1 / ((pWpm * 50) / 60) * 1000;

            if (Float.isInfinite(7 * fMs)) {
                throw new ArithmeticException("floating-point overflow when calculating Farnsworth timing lengths (fMs*7 must be less than Float.MAX_VALUE). fMs: " + fMs + " from fWpm: " + fWpm);
            } else if (Float.isInfinite(3 * pMs)) {
                throw new ArithmeticException("floating-point overflow when calculating Paris timing lengths (pMs*3 must be less than Float.MAX_VALUE). pMs: " + pMs + " from pWpm: " + pWpm);
            }
    }
}
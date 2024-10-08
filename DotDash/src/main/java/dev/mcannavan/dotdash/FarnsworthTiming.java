package dev.mcannavan.dotdash;

class FarnsworthTiming implements IMorseTiming {

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

        if (pMs <= 0 || fMs <= 0) {
            throw new IllegalArgumentException("expected non-negative, non-zero values of pMs and fMs. Actual values: pMs=" + pMs + ", fMs=" + fMs);
        }

        ParisTiming parisTiming = new ParisTiming();
        parisTiming.calculateSpeedFromMillis(pMs);
        ditLengthMillis = parisTiming.getDitLength();
        dahLengthMillis = parisTiming.getDahLength();
        intraCharLengthMillis = parisTiming.getIntraCharLength();

        float pWpm = parisTiming.getWpm();
        float fWpm = 60 / ((19 * fMs) / 1000f + 37.2f / pWpm);
        this.pWpm = pWpm;
        this.fWpm = fWpm;

        interCharLengthMillis = Math.round(3 * fMs);
        interWordLengthMillis = Math.round(7 * fMs);
    }

    //TODO: rework calculations
    public void calculateSpeedFromWpm(float fWpm, float pWpm) throws IllegalArgumentException, ArithmeticException {
        if (pWpm <= 0 || fWpm <= 0) {
            throw new IllegalArgumentException("expected non-negative, non-zero values of pWpm and fWpm. Actual value: pWpm = " + pWpm + ", fWpm = " + fWpm);
        } else if (pWpm/fWpm <= 0.62) { //max ratio fWpm:pWpm is ~ 1.6129 or min pWpm:fWpm = 0.62
            throw new ArithmeticException("ratio between fWpm and pWpm above maximum ratio ~1.6129. actual: "+pWpm/fWpm);
        }

        this.pWpm = pWpm;
        this.fWpm = fWpm;

        float fMs = (float) (((60 / fWpm) - (37.2 / pWpm)) * 1000) / 19;
        float pMs = (float) 1 / ((pWpm * 50) / 60) * 1000;
        if (Float.isInfinite(7*fMs) || Float.isInfinite(3*pMs)) {
            throw new ArithmeticException("floating-point overflow when calculating fMs or pMs." +
                    "\nfMs: " + fMs + ", pMs: " + pMs +
                    "\nfWpm: " + fWpm +  ", pWpm: " + pWpm);
        }
        calculateSpeedFromMillis(fMs, pMs);
    }
}
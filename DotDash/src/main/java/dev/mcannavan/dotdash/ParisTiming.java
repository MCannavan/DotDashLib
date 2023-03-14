package dev.mcannavan.dotdash;

public class ParisTiming implements IMorseTiming {
    private float ditLengthMillis;
    private float dahLengthMillis;
    private float interCharLengthMillis;
    private float intraCharLengthMillis;
    private float interWordLengthMillis;
    private float wpm;

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

    public void calculateSpeedFromMillis(float ms) {
        ms = Math.round(ms*100)/100f;
        wpm = Math.round(60f * (1f / (ms / 1000f)) / 50f *100f)/100f;
        ditLengthMillis = ms;
        dahLengthMillis = ms*3;
        interCharLengthMillis = ms*3;
        intraCharLengthMillis = ms;
        interWordLengthMillis = ms*7;
    }

    public void calculateSpeedFromWpm(float wpm) throws ArithmeticException, IllegalArgumentException {
        if(wpm < 0) {
            throw new IllegalArgumentException("expected non-negative value of float wpm in calculateSpeedFromWpm. Actual value:" + wpm);
        }
        float ms = 1f / ((wpm * 50f) / 60f) * 1000f;
        if(Float.isInfinite(ms)) {
            throw new ArithmeticException("Attempted divide by zero: float wpm cannot be zero");
        }
        calculateSpeedFromMillis(ms);
    }
}

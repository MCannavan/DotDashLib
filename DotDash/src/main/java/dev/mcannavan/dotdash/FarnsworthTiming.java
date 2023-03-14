package dev.mcannavan.dotdash;

/**
 * A set of morse timings following the Farnsworth approach. Implements {@link IMorseTiming}
 */
public class FarnsworthTiming extends ParisTiming implements IMorseTiming {

    @Override
    public void calculateSpeedFromMillis(float ms) {
        super.calculateSpeedFromMillis(ms);
    }

    @Override
    public void calculateSpeedFromWpm(float wpm) throws IllegalArgumentException {
        super.calculateSpeedFromWpm(wpm);
    }
    public void calculateSpeedFromWpm(float wpm, float fwpm) throws IllegalArgumentException {
        super.calculateSpeedFromWpm(wpm);
    }

}

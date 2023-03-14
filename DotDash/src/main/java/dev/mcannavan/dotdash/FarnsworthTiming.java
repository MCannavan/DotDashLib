package dev.mcannavan.dotdash;

public class FarnsworthTiming extends ParisTiming implements IMorseTiming {
    @Override
    public void calculateSpeedFromMillis(float ms) {
        super.calculateSpeedFromMillis(ms);
    }

    @Override
    public void calculateSpeedFromWpm(float wpm) throws ArithmeticException {
        super.calculateSpeedFromWpm(wpm);
    }
}

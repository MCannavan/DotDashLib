package dev.mcannavan.dotdash;

public class MorseTimingFactory {

    public static ParisTiming createParisTimingFromWpm(float wpm) {
        ParisTiming timing = new ParisTiming();
        timing.calculateSpeedFromWpm(wpm);
        return timing;
    }

    public static ParisTiming createParisTimingFromMs(float ms) {
        ParisTiming timing = new ParisTiming();
        timing.calculateSpeedFromMillis(ms);
        return timing;
    }

    public static FarnsworthTiming createFarnsworthTimingFromMs(float fMs, float pMs) {
        FarnsworthTiming timing = new FarnsworthTiming();
        timing.calculateSpeedFromMillis(fMs, pMs);
        return timing;
    }

    public static FarnsworthTiming createFarnsworthTimingFromMs(float ms) {
        FarnsworthTiming timing = new FarnsworthTiming();
        timing.calculateSpeedFromMillis(ms*1.5f, ms);
        return timing;
    }

    public static FarnsworthTiming createFarnsworthTimingFromWpm(float fWpm, float pWpm) {
        FarnsworthTiming timing = new FarnsworthTiming();
        timing.calculateSpeedFromWpm(fWpm, pWpm);
        return timing;
    }

    public static FarnsworthTiming createFarnsworthTimingFromWpm(float wpm) {
        FarnsworthTiming timing = new FarnsworthTiming();
        timing.calculateSpeedFromWpm(wpm*0.75f, wpm); // default farnsworth is 75% of wpm
        return timing;
    }
}

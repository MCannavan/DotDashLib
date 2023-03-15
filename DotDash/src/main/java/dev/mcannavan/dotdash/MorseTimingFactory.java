package dev.mcannavan.dotdash;

/**
 * a factory class for creating objects that implement {@link IMorseTiming}
 */
public class MorseTimingFactory {

    /**
     * Creates a {@link ParisTiming} object from a given words per minute.
     *
     * @param wpm a non-negative, non-zero {@code float} representing the desired words per minute
     * @return an object of type {@code ParisTiming}
     */
    public static ParisTiming createParisTimingFromWpm(float wpm) {
        ParisTiming timing = new ParisTiming();
        timing.calculateSpeedFromWpm(wpm);
        return timing;
    }

    /**
     * Creates a {@link ParisTiming} object from a given length of 1 unit (dit) in milliseconds.
     *
     * @param ms a non-negative, non-zero {@code float} representing the length of 1 unit (dit) in milliseconds
     * @return an object of type {@code ParisTiming}
     */
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
        timing.calculateSpeedFromMillis(ms*0.75f, ms);
        return timing;
    }

        /**
         * Creates a {@link FarnsworthTiming} object from a given words per minute and farnsworth words per minute
         *
         * @param pWpm A non-negative, non-zero {@code float} representing the words per minute from the PARIS approach.
         * @param fWpm A non-negative, non-zero {@code float} representing the alternate speed between characters and between words.
         * @return an object of type {@code FarnsworthTiming}
         */
    public static FarnsworthTiming createFarnsworthTimingFromWpm(float fWpm, float pWpm) {
        FarnsworthTiming timing = new FarnsworthTiming();
        timing.calculateSpeedFromWpm(fWpm, pWpm);
        return timing;
    }

    public static FarnsworthTiming createFarnsworthTimingFromWpm(float wpm) {
        FarnsworthTiming timing = new FarnsworthTiming();
        timing.calculateSpeedFromWpm(wpm*0.75f, wpm); // using wpm for both pWpm and fWpm
        return timing;
    }
}

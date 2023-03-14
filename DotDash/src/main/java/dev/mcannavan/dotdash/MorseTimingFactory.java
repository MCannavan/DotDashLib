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

    /**
     * Creates a {@link FarnsworthTiming} object from a given words per minute and farnsworth words per minute
     *
     * @param wpm A non-negative, non-zero {@code float} representing the words per minute from the PARIS approach.
     * @param fwpm A non-negative, non-zero {@code float} representing the alternate speed between characters and between words.
     * @return an object of type {@code FarnsworthTiming}
     */
    public static FarnsworthTiming createFarnsworthTimingFromWpm(float fwpm, float wpm) {
        FarnsworthTiming timing = new FarnsworthTiming();
        timing.calculateSpeedFromWpm(fwpm, wpm);
        return timing;
    }

    public static FarnsworthTiming createFarnsworthTimingFromWpm(float wpm) {
        FarnsworthTiming timing = new FarnsworthTiming();
        timing.calculateSpeedFromWpm(wpm*0.75f, wpm); // using wpm for both pWpm and fWpm
        return timing;
    }
}

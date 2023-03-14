package dev.mcannavan.dotdash;

/**
 * An {@code Interface} representing the timings/durations of morse code
 */
public interface IMorseTiming {
    /**
     * Gets the length of a dit.
     *
     * @return the dit length as a {@code float}
     */
    float getDitLength();

    /**
     * Gets the length of a dah.
     *
     * @return the dah length as a {@code float}
     */
    float getDahLength();

    /**
     * Gets the inter-char length.
     *
     * @return the inter char length as a {@code float}
     */
    float getInterCharLength();

    /**
     * Gets the intra-char length.
     *
     * @return the intra char length as a {@code float}
     */
    float getIntraCharLength();

    /**
     * Gets the inter-word length.
     *
     * @return the inter word length as a {@code float}
     */
    float getInterWordLength();

    /**
     * Gets the words per minute
     *
     * @return the words per minute as a {@code float}
     */
    float getWpm();

    //public void calculateSpeedFromMillis(float ms);
    //public void calculateSpeedFromWpm(float wpm);

}

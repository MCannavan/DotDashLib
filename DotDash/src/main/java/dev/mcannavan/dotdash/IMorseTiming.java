package dev.mcannavan.dotdash;

/**
 * An interface for timing of morse code.
 */
public interface IMorseTiming {

    /**
     * Gets the length of a dit (dot).
     *
     * @return the dit length
     */
    float getDitLength();

    /**
     * Gets the length of a dah (dash).
     *
     * @return the dah length
     */
    float getDahLength();

    /**
     * Gets intra-char length, the space between dits and dahs within a character.
     *
     * @return the intra-char length
     */
    float getIntraCharLength();

    /**
     * Gets inter-char length, the space between character within a word.
     *
     * @return the inter-char length
     */
    float getInterCharLength();

    /**
     * Gets inter-word length, the space between words.
     *
     * @return the inter-word length
     */
    float getInterWordLength();

}

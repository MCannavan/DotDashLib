package dev.mcannavan.dotdash;

import javax.sound.sampled.*;

/**
 * The type Morse player.
 */
public class MorsePlayer {

    /**
     * The constant SAMPLE_FREQUENCY.
     */
    private static final float SAMPLE_FREQUENCY = 44100;
    /**
     * The Line.
     */
    private final SourceDataLine line;
    /**
     * The Translator.
     */
    private MorseTranslator translator;
    /**
     * The Timing.
     */
    private IMorseTiming timing;
    /**
     * The Frequency.
     */
    private double frequency = 700;

    /**
     * Instantiates a new Morse player.
     */
    private MorsePlayer() {
        try {
            line = AudioSystem.getSourceDataLine(new AudioFormat(
                    SAMPLE_FREQUENCY, 16,
                    1, true, false));
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Opens and starts the {@code SourceDataLine} if possible and returns a boolean representing the success of the operation.
     *
     * @return true if the line was successfully opened and started, false if the line was already open
     * @throws LineUnavailableException if the line is unavailable
     */
    private boolean openLine() throws LineUnavailableException {
        if (!line.isOpen()) {
            line.open();
            line.start();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Closes the {@code SourceDataLine} and returns a boolean representing the success of the operation.
     *
     * @return true if the line was successfully closed, false if the line was already closed
     */
    private boolean closeLine() {
        if (line.isOpen()) {
            line.drain();
            line.close();
            return true;
        } else {
            //System.out.println("line is not open");
            return false;
        }
    }

    /* original playTone method, kept for reference
    public void playTone(double duration, double frequency, double amplitude) {
        int numSamples = (int) (duration * SAMPLE_FREQUENCY);
        byte[] buffer = new byte[2 * numSamples];

        double step = 2 * Math.PI * frequency / SAMPLE_FREQUENCY;
        for (int i = 0; i < numSamples; i++) {
            short sample = (short) (amplitude * Math.sin(i * step));
            buffer[2 * i] = (byte) sample;
            buffer[2 * i + 1] = (byte) (sample >> 8);
        }
        line.write(buffer, 0, buffer.length);
        try {
            Thread.sleep(Math.round(duration * 1000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
     */

    /**
     * plays a sinusoidal (pure) tone of the specified frequency and amplitude for the specified duration, with a short fade-in and fade-out
     *
     * @param duration the duration in seconds, as a {@code double}
     * @param frequency the frequency of the tone in Hertz, as a {@code double}
     * @param amplitude the amplitude of the tone, as a {@code double}
     */
    private void playTone(double duration, double frequency, double amplitude) {
        final double FADE_IN_DURATION = duration * 0.02; // 1.5% fade-in
        final double FADE_OUT_DURATION = duration * 0.05; // 5% fade-out
        int numSamples = (int) (duration * SAMPLE_FREQUENCY);
        byte[] buffer = new byte[2 * numSamples];

        double step = 2 * Math.PI * frequency / SAMPLE_FREQUENCY;
        for (int i = 0; i < numSamples; i++) {
            double fade = 1.0;
            if (i < FADE_IN_DURATION * SAMPLE_FREQUENCY) { // fade-in when during fade-in duration
                fade = i / (FADE_IN_DURATION * SAMPLE_FREQUENCY);
            } else if (i > numSamples - (FADE_OUT_DURATION * SAMPLE_FREQUENCY)) { // fade-out when during fade-out duration
                fade = 1.0 - ((i - (numSamples - (FADE_OUT_DURATION * SAMPLE_FREQUENCY))) / (FADE_OUT_DURATION * SAMPLE_FREQUENCY));
            }
            short sample = (short) (amplitude * Math.sin(i * step) * fade);
            buffer[2 * i] = (byte) sample;
            buffer[2 * i + 1] = (byte) (sample >> 8);
        }
        line.write(buffer, 0, buffer.length);
        try {
            Thread.sleep(Math.round(duration * 1000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Translates and plays text as morse code at the specified volume and returns the {@code Thread} that plays the morse.
     *
     * @param volumePercent the volume percent (0-100) to play the morse at, as a {@code double}
     * @param morse the text to be played as morse, as a {@code String}
     * @return a {@code Thread} that is playing the morse
     * @throws IllegalArgumentException if the text contains invalid characters for the current {@code MorseTranslator}
     */
    public Thread playMorse(double volumePercent, String morse) throws IllegalArgumentException{
        double amplitude = Math.round(volumePercent/100*32767d);
        Thread t = new Thread(() -> {
            try {
                openLine();
                if (translator.validateInput(morse)) {
                    char[][][] phrase = translator.toMorseCharArray(morse);
                    for (int i = 0; i < phrase.length; i++) { //for each word
                        for (int j = 0; j < phrase[i].length; j++) { //for each letter
                            for (int k = 0; k < phrase[i][j].length; k++) { //for each symbol
                                //System.out.print(phrase[i][j][k]);
                                switch (phrase[i][j][k]) {
                                    case '-':
                                        playTone(timing.getDahLength() / 1000d, frequency, amplitude);
                                        break;
                                    case '.':
                                        playTone(timing.getDitLength() / 1000d, frequency, amplitude);
                                        break;
                                }
                                if (k < phrase[i][j].length - 1) {
                                    Thread.sleep(Math.round(timing.getIntraCharLength()));
                                }
                            } //end for each symbol

                            if (j < phrase[i].length - 1) {
                                //System.out.print(" / ");
                                Thread.sleep(Math.round(timing.getInterCharLength()));
                            }
                        } //end for each letter
                        if (i < phrase.length - 1) {
                            //System.out.print(" // ");
                            Thread.sleep(Math.round(timing.getInterWordLength()));
                        } else {
                            // For some reason, opening the project has a chance to cause the last tone to be cut-off
                            // line.drain() should block until the line is done playing, but it doesn't seem to work
                            // This is a workaround until the actual cause can be found
                            Thread.sleep(Math.round(timing.getInterWordLength()));
                        }
                    } //end for each word
                } else {
                    throw new IllegalArgumentException("Invalid morse code");
                }
            } catch (InterruptedException | LineUnavailableException e) {
                e.printStackTrace();
            } finally {
                closeLine();
            }
        });
        t.start();
        return t;
    }

    /**
     * Gets the {@link MorseTranslator}.
     *
     * @return the {@code MorseTranslator}
     */
    public MorseTranslator getTranslator() {
        return translator;
    }

    /**
     * Sets the {@link MorseTranslator}.
     *
     * @param translator the {@code MorseTranslator} to be used
     */
    public void setTranslator(MorseTranslator translator) {
        this.translator = translator;
    }

    /**
     * Gets the {@link IMorseTiming}.
     *
     * @return the {@code IMorseTiming}
     */
    public IMorseTiming getTiming() {
        return timing;
    }

    /**
     * Sets the {@link IMorseTiming}.
     *
         * @param timing the {@code IMorseTiming} to be used
     */
    public void setTiming(IMorseTiming timing) {
        this.timing = timing;
    }

    /**
     * Gets the frequency which morse is played at.
     *
     * @return the frequency, as a {@code double}
     */
    public double getFrequency() {
        return frequency;
    }

    /**
     * Sets the frequency which morse is played at.
     *
     * @param frequency the frequency to be used, as a {@code double}
     */
    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    /**
     * The builder pattern for new instances of {@link MorsePlayer}
     *
     * <br> <br> These are the attributes that can be set:
     * <ul>
     *     <li>{@link #setTranslator(MorseTranslator) MorseTranslator}</li>
     *     <li>{@link #setTiming(IMorseTiming) IMorseTiming}</li>
     *     <li>{@code double} Frequency </li>
     * </ul>
     *
     * With defaults:
     * <ul>
     *     <li> {@link MorseTranslator} - a {@code MorseTranslator} with {@link CharacterSet}
     *     {@link CharacterSet#LATIN LATIN},
     *     {@link CharacterSet#PUNCTUATION PUNCTUATION},
     *     and {@link CharacterSet#ARABIC_NUMERALS ARABIC_NUMERALS}
     *     </li>
     *     <li> {@link IMorseTiming} - a {@link ParisTiming} with a speed of {@link MorseTimingFactory#createParisTimingFromWpm(float 20) 20 wpm}</li>
     *     <li> Frequency - {@code 700}</li>
     *
     * </ul>
     */
    public static final class MorsePlayerBuilder {
        /**
         * The {@link MorseTranslator}
         */
        private MorseTranslator translator;
        /**
         * The {@link IMorseTiming}
         */
        private IMorseTiming timing;
        /**
         * The frequency
         */
        private double frequency;

        /**
         * Instantiates a new {@code MorsePlayerBuilder} with default values.
         */
        public MorsePlayerBuilder() {
            this.translator = new MorseTranslator()
                    .addMap(CharacterSet.LATIN)
                    .addMap(CharacterSet.PUNCTUATION)
                    .addMap(CharacterSet.ARABIC_NUMERALS);
            this.timing = MorseTimingFactory.createParisTimingFromWpm(20);
            this.frequency = 700;
        }

        /**
         * Sets the {@link MorseTranslator} for the new instance of {@link MorsePlayer}
         *
         * @param translator the {@code MorseTranslator} to be used
         * @return this {@code MorsePlayerBuilder}
         */
        public MorsePlayerBuilder withTranslator(MorseTranslator translator) {
            this.translator = translator;
            return this;
        }

        /**
         * Sets the {@link IMorseTiming} for the new instance of {@link MorsePlayer}
         *
         * @param timing the {@code IMorseTiming} to be used
         * @return this {@code MorsePlayerBuilder}
         */
        public MorsePlayerBuilder withTiming(IMorseTiming timing) {
            this.timing = timing;
            return this;
        }

        /**
         * Sets the frequency which morse is played at for the new instance of {@link MorsePlayer}
         *
         * @param frequency the frequency in Hertz, as a {@code double}
         * @return this {@code MorsePlayerBuilder}
         */
        public MorsePlayerBuilder withFrequency(double frequency) {
            this.frequency = frequency;
            return this;
        }

        /**
         * Creates a new instance of {@link MorsePlayer}
         *
         * @return the new instance
         */
        public MorsePlayer build() {
            MorsePlayer morsePlayer = new MorsePlayer();
            morsePlayer.setTranslator(translator);
            morsePlayer.setTiming(timing);
            morsePlayer.setFrequency(frequency);
            return morsePlayer;
        }
    }
}

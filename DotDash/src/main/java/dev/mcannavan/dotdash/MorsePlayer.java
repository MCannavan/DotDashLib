package dev.mcannavan.dotdash;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MorsePlayer {

    private static final float SAMPLE_FREQUENCY = 44100;
    private final SourceDataLine line;
    //private final TimeTable timeTable = new TimeTable("MorseEpochs");
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private MorseTranslator translator;
    private IMorseTiming timing;
    private double frequency = 700;

    private InterruptBehavior interruptBehavior = InterruptBehavior.NONE;

    private MorsePlayer() {
        try {
            line = AudioSystem.getSourceDataLine(new AudioFormat(
                    SAMPLE_FREQUENCY, 16,
                    1, true, false));
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean openLine() throws LineUnavailableException {
        if (!line.isOpen()) {
            line.open();
            line.start();
            return true;
        } else {
            return false;
        }
    }

    private boolean closeLine() {
        if (line.isOpen()) {
            line.drain();
            line.close();
            return true;
        } else {
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

    private void playTone(double duration, double frequency, double amplitude) {
        final double FADE_IN_DURATION = duration * 0.05;
        final double FADE_OUT_DURATION = duration * 0.055;
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
        //long actualTime = System.currentTimeMillis();
        //timeTable.addRecord(expectedTime, actualTime);
        line.write(buffer, 0, buffer.length);
    }

    public void playMorse(double volumePercent, String morse) {
        playMorse(volumePercent, 0, morse);
    }

    public void playMorse(double volumePercent, int initialDelay, String morse) throws IllegalArgumentException{
        double amplitude = Math.round(volumePercent/100*32767d);
        int delayTotal = initialDelay;
        String toPlay = morse;
        boolean longText = false;
        if (morse.length() > 100) {
            longText = true;
            toPlay = morse.substring(0, 100);
            morse = morse.substring(100);
        }
        try {
            openLine();
            if (translator.validateInput(morse)) {
                char[][][] phrase = translator.toMorseCharArray(toPlay);
                for (int i = 0; i < phrase.length; i++) { //for each word
                    for (int j = 0; j < phrase[i].length; j++) { //for each letter
                        for (int k = 0; k < phrase[i][j].length; k++) { //for each symbol
                            switch (phrase[i][j][k]) {
                                case '-':
                                    executor.schedule(() -> playTone(timing.getDahLength() / 1000d, frequency, amplitude), delayTotal, TimeUnit.MILLISECONDS);
                                    delayTotal += timing.getDahLength();
                                    break;
                                case '.':
                                    executor.schedule(() -> playTone(timing.getDitLength() / 1000d, frequency, amplitude), delayTotal, TimeUnit.MILLISECONDS);delayTotal += timing.getDitLength();
                                    break;
                            }
                            if (k < phrase[i][j].length - 1) {
                                delayTotal += timing.getIntraCharLength();
                            }
                        } //end for each symbol
                        if (j < phrase[i].length - 1) {
                            delayTotal += timing.getInterCharLength();
                        }
                    } //end for each letter
                    delayTotal += timing.getInterWordLength();
                } //end for each word

            } else {
                throw new IllegalArgumentException("Invalid morse code");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!longText) {
                executor.schedule(this::closeLine, delayTotal, TimeUnit.MILLISECONDS);
            } else {
                playMorse(volumePercent, delayTotal, morse);
            }
        }
    }

    /*
    private void playMorseRecursive(double volumePercent, char[][][] phrase, int wordIndex, int letterIndex, int symbolIndex, long delay) {
        try {
            openLine();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }

        if (wordIndex >= phrase.length) {
            executor.schedule(this::closeLine, delay, TimeUnit.MILLISECONDS);
            return;
        }

        if (letterIndex >= phrase[wordIndex].length) {
            delay += timing.getInterWordLength();
            playMorseRecursive(volumePercent, phrase, wordIndex + 1, 0, 0, delay);
            return;
        }

        if (symbolIndex >= phrase[wordIndex][letterIndex].length) {
            delay += timing.getInterCharLength();
            playMorseRecursive(volumePercent, phrase, wordIndex, letterIndex + 1, 0, delay);
            return;
        }

        char symbol = phrase[wordIndex][letterIndex][symbolIndex];
        double duration = 0;

        switch (symbol) {
            case '-':
                duration = timing.getDahLength() / 1000d;
                break;
            case '.':
                duration = timing.getDitLength() / 1000d;
                break;
            default:
                throw new IllegalArgumentException("Invalid morse code");
        }

        double amplitude = Math.round(volumePercent / 100 * 32767d);
        long expectedTime = System.currentTimeMillis() + delay;

        double finalDuration = duration;
        executor.schedule(() -> {
            playTone(finalDuration, frequency, amplitude, expectedTime);
            long nextDelay = (long) ((long) (finalDuration * 1000) + timing.getIntraCharLength());
            playMorseRecursive(volumePercent, phrase, wordIndex, letterIndex, symbolIndex + 1, nextDelay);
        }, delay, TimeUnit.MILLISECONDS);
    }
    */

    public MorseTranslator getTranslator() {
        return translator;
    }

    public void setTranslator(MorseTranslator translator) {
        this.translator = translator;
    }

    public IMorseTiming getTiming() {
        return timing;
    }

    public void setTiming(IMorseTiming timing) {
        this.timing = timing;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public InterruptBehavior getInterruptBehavior() {
        return interruptBehavior;
    }
    public void setInterruptBehavior(InterruptBehavior behavior) {
        this.interruptBehavior = behavior;
    }

    /*
    private static class TimeTable {
        private final String fileName;

        public TimeTable(String baseFileName) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timestamp = formatter.format(new Date());
            this.fileName = baseFileName + "_" + timestamp + ".csv";
            initializeFile();
        }
        private void initializeFile() {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write("Expected Time,Actual Time\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public synchronized void addRecord(long expectedTime, long actualTime) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
                writer.write(expectedTime + "," + actualTime + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
     */

    private enum InterruptBehavior { //enum for interrupt behavior of playMorse() calls
        NONE, //calls will be ignored if currently playing
        DELAY, //new calls will be delayed until current call is finished
        INTERRUPT //new calls will interrupt current call
    }

    public static final class MorsePlayerBuilder {
        private MorseTranslator translator;
        private IMorseTiming timing;
        private double frequency;

        private InterruptBehavior interruptBehavior;

        public MorsePlayerBuilder() {
            this.translator = new MorseTranslator()
                    .addMap(CharacterSet.LATIN)
                    .addMap(CharacterSet.PUNCTUATION)
                    .addMap(CharacterSet.ARABIC_NUMERALS);
            this.timing = MorseTimingFactory.createParisTimingFromWpm(20);
            this.frequency = 700;
            this.interruptBehavior = InterruptBehavior.NONE;
        }

        public MorsePlayerBuilder withTranslator(MorseTranslator translator) {
            this.translator = translator;
            return this;
        }

        public MorsePlayerBuilder withTiming(IMorseTiming timing) {
            this.timing = timing;
            return this;
        }

        public MorsePlayerBuilder withFrequency(double frequency) {
            this.frequency = frequency;
            return this;
        }

        public MorsePlayerBuilder withInterruptBehavior(InterruptBehavior interruptBehavior) {
            this.interruptBehavior = interruptBehavior;
            return this;
        }

        public MorsePlayer build() {
            MorsePlayer morsePlayer = new MorsePlayer();
            morsePlayer.setTranslator(translator);
            morsePlayer.setTiming(timing);
            morsePlayer.setFrequency(frequency);
            morsePlayer.setInterruptBehavior(interruptBehavior);
            return morsePlayer;
        }
    }
}

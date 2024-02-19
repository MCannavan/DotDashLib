package dev.mcannavan.dotdash;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.concurrent.*;

//TODO:
// - fix playMorse() method not working on windows
// - write unit tests for all methods
//    - create dummy class & methods for unit testing
// - code cleanup
// - implement better exception handling & throwing
// - remove unused methods
// - consider interface (better for abstraction)
// - pot. add stereo mode
//    - virtual positioning?

public class MorsePlayer {

    private static final float SAMPLE_FREQUENCY = 44100;
    private static final int SAMPLES_SIZE_IN_BITS = 16;
    private static final int N_CHANNELS = 1; // Number of channels; mono
    private final SourceDataLine line;

    //private final TimeTable timeTable = new TimeTable("MorseEpochs");

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final ArrayList<Future<?>> scheduledFutures = new ArrayList<>();
    private final ArrayList<Future<?>> discardedFutures = new ArrayList<>();
    private MorseTranslator translator;
    private IMorseTiming timing;
    private double frequency; //Tone frequency in Hertz (Hz)
    private int globalDelay = 0;
    private InterruptBehavior interruptBehavior = InterruptBehavior.NONE;

    private enum InterruptBehavior { //enum for interrupt behavior of playMorse() calls
        NONE, //calls will be ignored if currently playing
        DELAY, //new calls will be delayed until current call is finished
        INTERRUPT //new calls will interrupt current call
    }

    MorsePlayer() {
        try {
            line = AudioSystem.getSourceDataLine(new AudioFormat(
                    SAMPLE_FREQUENCY, 16,
                    1, true, false));
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
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
            this.timing = MorseTimingFactory.createParisTimingFromWpm(20); //Average morse speed and format
            this.frequency = 750;
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

    private boolean openLine() throws LineUnavailableException {
        if (!line.isOpen()) {
            line.open();
            line.start();
            return true;
        } else {
            return false;
        }
    }

    //TODO finish line closing rework
    private boolean closeLine(boolean forced) {
        if (line.isOpen()) {
            line.drain();
            line.close();
            return true;
        } else {
            return false;
        }
    }

    private void submitWithAutoRemoval(Runnable task, int delay) {
        scheduledFutures.add(executor.schedule(
                () -> {
                    try {
                        task.run();
                    } finally {
                        scheduledFutures.removeIf(Future::isDone);
                        if(!discardedFutures.isEmpty()) {
                            discardedFutures.removeIf(Future::isDone);
                        }
                    }
                }, delay, TimeUnit.MILLISECONDS));
    }

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
            buffer[2 * i + 1] = (byte) (sample >> 8); //shift bits rightwards by 8
        }
        line.write(buffer, 0, buffer.length);
        if(globalDelay > 0) {
            globalDelay -= duration;
        }
    }

    public void playMorse(double volumePercent, String morse) {
        playMorse(volumePercent, 0, morse);
    }

    //TODO:
    // - find out why this doesn't function on Windows, saveToWav works as an alternative
    public void playMorse(double volumePercent, int initialDelay, String morse) throws IllegalArgumentException {
        double amplitude = Math.round(volumePercent / 100 * Short.MAX_VALUE);
        int delayTotal = initialDelay;

        switch (interruptBehavior) {
            case INTERRUPT:
                int interruptDelay = initialDelay < 100 ? 0 : initialDelay-100; //if initial delay is less than 100 ms, set to 0
                discardedFutures.addAll(scheduledFutures);
                scheduledFutures.clear();
                globalDelay = 0;

                submitWithAutoRemoval(() -> {
                    for(int i = 0; i < discardedFutures.size(); i++) {
                        discardedFutures.get(i).cancel(true);
                    }
                }, interruptDelay);
                break;
            case DELAY:
                break;
            case NONE:
                if(!scheduledFutures.isEmpty()) {
                    return;
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value of InterruptBehavior: " + interruptBehavior);
        }
        try {
            openLine();
            System.out.println("success");
            if (translator.validateInput(morse)) {
                char[][][] phrase = translator.toMorseCharArray(morse);
                for (int i = 0; i < phrase.length; i++) { //for each word
                    for (int j = 0; j < phrase[i].length; j++) { //for each letter
                        for (int k = 0; k < phrase[i][j].length; k++) { //for each symbol
                            switch (phrase[i][j][k]) {
                                case '-':
                                    submitWithAutoRemoval(() -> playTone(timing.getDahLength() / 1000, //convert Ms to seconds
                                            frequency, amplitude), delayTotal);
                                    delayTotal += timing.getDahLength();
                                    break;
                                case '.':
                                    submitWithAutoRemoval(() -> playTone(timing.getDitLength() / 1000, //convert Ms to seconds
                                            frequency, amplitude), delayTotal);
                                    delayTotal += timing.getDitLength();
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
            globalDelay += delayTotal;
        }
    }

    public MorseTranslator getTranslator() {
        return translator;
    }

    public void setTranslator(MorseTranslator translator) {
        this.translator = translator;
    }

    private void writeWavHeader(RandomAccessFile raw) throws IOException {
        raw.setLength(0);
        raw.writeBytes("RIFF");
        raw.writeInt(0);  //init length
        raw.writeBytes("WAVE"); //file type header
        raw.writeBytes("fmt "); //format chunk marker
        raw.writeInt(Integer.reverseBytes(SAMPLES_SIZE_IN_BITS));
        raw.writeShort(Short.reverseBytes((short) 1));
        raw.writeShort(Short.reverseBytes((short) N_CHANNELS));
        raw.writeInt(Integer.reverseBytes((int)SAMPLE_FREQUENCY));
        raw.writeInt(Integer.reverseBytes((int)SAMPLE_FREQUENCY* SAMPLES_SIZE_IN_BITS * N_CHANNELS /8));
        raw.writeShort(Short.reverseBytes((short)(N_CHANNELS * SAMPLES_SIZE_IN_BITS /8)));
        raw.writeShort(Short.reverseBytes((short) SAMPLES_SIZE_IN_BITS));
        raw.writeBytes("data");
        raw.writeInt(0); //init size of data section
    }

    private static void closeWavFile(RandomAccessFile raw) throws IOException {
        raw.seek(4); // bytes 4-8: total file size
        raw.writeInt(Integer.reverseBytes((int) raw.length() - 8)); //size of the total file subtract 8 bytes
        raw.seek(40); // bytes 40-44: data section size
        raw.writeInt(Integer.reverseBytes((int) raw.length() - 44)); //size of data section (total size subtract the header size
        raw.close();

        System.out.println("Finished writing file ");
    }

    private static void writeSample(RandomAccessFile raw, byte[] sample) throws IOException {
        raw.write(sample);
    }

    private byte[] saveToneToWav(float duration, double frequency, double amplitude) {
        final double FADE_IN_DURATION = duration * 0.05;
        final double FADE_OUT_DURATION = duration * 0.055;

        int numSamples = (int) (duration * SAMPLE_FREQUENCY);
        byte[] result = new byte[numSamples*2];
        int head = 0;

        double step = 2 * Math.PI * frequency / SAMPLE_FREQUENCY;
            for (int i = 0; i < numSamples; i++) {
                float sampleValue;
                if(amplitude > 0) {
                    double fade = 1.0;

                    if (i < FADE_IN_DURATION * SAMPLE_FREQUENCY) {
                        fade = i / (FADE_IN_DURATION * SAMPLE_FREQUENCY);
                    } else if (i > numSamples - (FADE_OUT_DURATION * SAMPLE_FREQUENCY)) {
                        fade = 1.0 - ((i - (numSamples - (FADE_OUT_DURATION * SAMPLE_FREQUENCY))) / (FADE_OUT_DURATION * SAMPLE_FREQUENCY));
                    }
                    sampleValue = (float) (amplitude * Math.sin(i * step) * fade);
                } else {
                    sampleValue = 0;
                }
                byte[] temp = ByteBuffer.allocate(2).putShort(Short.reverseBytes((short) sampleValue)).array();
                for(byte b : temp) {
                    result[head] = b;
                    head++;
                }
            }
        return result;
    }
    public void saveMorseToWav(double volumePercent, String filePath, String fileName, String morse) throws IllegalArgumentException {
        double amplitude = Math.round(volumePercent / 100 * Short.MAX_VALUE);

        fileName = !fileName.endsWith(".wav") ? fileName.concat(".wav") : fileName; //add .wav if not already included
        filePath = !filePath.endsWith(File.separator) ? filePath.concat(File.separator) : filePath;// add file separator to end of filepath if not already included


        filePath = filePath.concat(fileName);
        File file = new File(filePath);
        try(RandomAccessFile raw = new RandomAccessFile(file.getAbsolutePath(), "rw")) {
            writeWavHeader(raw);
            if (translator.validateInput(morse)) {

                char[][][] phrase = translator.toMorseCharArray(morse);
                byte[] bytes = new byte[calculateTotalSamples(phrase)];
                byte[] temp = new byte[0];
                int head = 0;

                for(int i = 0; i < phrase.length; i++) { //for each word (e.g.: lorem, ipsum, dolor)
                    for(int j = 0; j < phrase[i].length; j++) { //for each letter (e.g.: a,b,c)
                        for(int k = 0; k < phrase[i][j].length; k++) { //for each symbol (e.g.: .,-)
                            switch (phrase[i][j][k]) {
                                case '-':
                                    temp = saveToneToWav(timing.getDahLength() / 1000, frequency, amplitude);
                                    break;
                                case '.':
                                    temp = saveToneToWav(timing.getDitLength() / 1000, frequency, amplitude);
                                    break;
                            }
                            for(byte b : temp) {
                                bytes[head] = b;
                                head++;
                            }

                            if(k < phrase[i][j].length-1) {
                                temp = saveToneToWav(timing.getIntraCharLength() / 1000, frequency, 0); //intra-char space
                                for(byte b : temp) {
                                    bytes[head] = b;
                                    head++;
                                }
                            }
                        }
                        if(j < phrase[i].length-1) {
                            temp = saveToneToWav(timing.getInterCharLength() / 1000, frequency, 0); //inter-char space
                            for(byte b : temp) {
                                bytes[head] = b;
                                head++;
                            }
                        }
                    }
                    if(i < phrase.length-1) {
                        temp = saveToneToWav(timing.getInterWordLength() / 1000, frequency, 0); //inter-word space
                        for(byte b : temp) {
                            bytes[head] = b;
                            head++;
                        }
                    }
                }
                writeSample(raw, bytes);
                closeWavFile(raw);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    //TODO figure out why method over-estimates sample size;
    private int calculateTotalSamples(char[][][] phrase) {
        int total = 0;
        float dit = timing.getDitLength() / 1000;
        float dah = timing.getDahLength() / 1000;
        float intraChar = timing.getIntraCharLength() / 1000;
        float interChar = timing.getInterCharLength() / 1000;
        float interWord = timing.getInterWordLength() / 1000;
        for(int i = 0; i < phrase.length; i++) {
            for(int j = 0; j < phrase[i].length; j++) {
                for(int k = 0; k < phrase[i][j].length; k++) {
                    switch (phrase[i][j][k]) {
                        case '-':
                            total += ((int) (dah*SAMPLE_FREQUENCY))*2;
                            break;
                        case '.':
                            total += ((int) (dit*SAMPLE_FREQUENCY))*2;
                            break;
                    }
                    if(k < phrase[i][j].length-1) {
                        total += ((int) (intraChar*SAMPLE_FREQUENCY))*2;
                    }
                }
                if(j < phrase[i].length-1) {
                    total += ((int) (interChar*SAMPLE_FREQUENCY))*2;
                }
            }
            if(i < phrase.length-1) {
                total += ((int) (interWord*SAMPLE_FREQUENCY))*2;
            }
        }
        return total;
    }

    public static void main(String[] args) {
        MorsePlayer morsePlayer = new MorsePlayerBuilder()
                .withInterruptBehavior(InterruptBehavior.DELAY)
                .build();

        String morse = "dui faucibus in ornare quam viverra orci sagittis eu volutpat odio " +
                "facilisis mauris sit amet massa vitae tortor condimentum lacinia quis vel " +
                "eros donec ac odio tempor orci dapibus ultrices in iaculis nunc sed augue " +
                "lacus viverra vitae congue eu consequat ac felis donec et odio pellentesque " +
                "diam volutpat commodo sed egestas egestas fringilla phasellus faucibus scelerisque " +
                "eleifend donec pretium vulputate sapien nec sagittis aliquam malesuada bibendum " +
                "arcu vitae elementum curabitur vitae nunc sed velit dignissim sodales ut eu " +
                "sem integer vitae justo eget magna fermentum iaculis eu non diam phasellus " +
                "vestibulum lorem sed risus ultricies tristique nulla aliquet enim tortor at " +
                "auctor urna nunc id cursus metus aliquam eleifend mi in nulla posuere sollicitudin " +
                "aliquam ultrices sagittis orci a scelerisque purus semper eget duis at tellus " +
                "at urna condimentum mattis pellentesque id nibh tortor id aliquet lectus proin " +
                "nibh nisl condimentum id venenatis a condimentum vitae sapien pellentesque " +
                "habitant morbi tristique senectus et netus et malesuada fames ac turpis " +
                "egestas sed tempus urna et pharetra pharetra massa massa ultricies mi quis " +
                "hendrerit dolor magna eget est lorem ipsum dolor sit amet consectetur adipiscing " +
                "elit pellentesque habitant morbi tristique senectus et netus et malesuada " +
                "fames ac turpis egestas integer eget aliquet nibh praesent tristique magna " +
                "sit amet purus gravida quis blandit turpis cursus in hac habitasse platea " +
                "dictumst quisque sagittis purus sit amet volutpat consequat mauris nunc congue " +
                "nisi vitae suscipit tellus mauris a diam maecenas sed enim ut sem viverra aliquet " +
                "eget sit amet tellus cras adipiscing enim eu turpis egestas pretium aenean pharetra " +
                "magna ac placerat vestibulum lectus mauris ultrices eros in cursus turpis massa " +
                "tincidunt dui ut ornare lectus sit amet est placerat in egestas erat imperdiet " +
                "sed euismod nisi porta lorem mollis aliquam ut porttitor leo a diam sollicitudin";

        //morse = "lorem ipsum dolor sit amet";

        morsePlayer.saveMorseToWav(100,"DotDash/src/test/","test",morse);
        //morsePlayer.playMorse(100,"Lorem ipsum dolor sit amet, consectetur adipiscing");
        //morsePlayer.playMorse(100, 5000,"OOOOO");

    }
}

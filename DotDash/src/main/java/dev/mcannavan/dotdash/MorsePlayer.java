package dev.mcannavan.dotdash;

import javax.sound.sampled.*;
import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.*;

//TODO:
// - fix playMorse() method not working on windows
// - write unit tests for all methods
//    - create dummy class & methods for unit testing
// - code cleanup
// - implement better exception handling & throwing
// - remove unused methods
// consider pre-generating individual letter/symbol sequences (reload on changing IMorseTiming or MorseTranslator)

public class MorsePlayer {

    private static final float SAMPLE_FREQUENCY = 44100;
    private static final int SAMPLES_SIZE_IN_BITS = 16;
    private static final int N_CHANNELS = 1; // Number of channels; mono
    private final SourceDataLine line;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final ArrayList<Future<?>> scheduledFutures = new ArrayList<>();
    private final ArrayList<Future<?>> discardedFutures = new ArrayList<>();
    private MorseTranslator translator;
    private IMorseTiming timing;
    private double frequency; //Tone frequency in Hertz (Hz)
    private int globalDelay = 0;
    private InterruptBehavior interruptBehavior = InterruptBehavior.NONE;
    private RandomAccessFile raw;

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

    public MorseTranslator getTranslator() {
        return translator;
    }

    public void setTranslator(MorseTranslator translator) {
        this.translator = translator;
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

    //TODO: decouple above methods from RandomAccessFile via ByteArrayOutputStream

    private byte[] generateTone(float duration, double frequency, double amplitude) {
        final double FADE_IN_DURATION = duration * 0.05;
        final double FADE_OUT_DURATION = duration * 0.055;

        int numSamples = (int) (duration * SAMPLE_FREQUENCY * N_CHANNELS);
        byte[] result = new byte[numSamples*2];
        int head = 0;

        double step = 2 * Math.PI * frequency / SAMPLE_FREQUENCY / N_CHANNELS;
        for (int i = 0; i < numSamples; i++) {
            float sampleValue;
            if(amplitude > 0) {
                double fade = 1.0;

                if (i < FADE_IN_DURATION * SAMPLE_FREQUENCY) {
                    fade = i / (FADE_IN_DURATION * SAMPLE_FREQUENCY);
                } else if (i > numSamples - (FADE_OUT_DURATION * SAMPLE_FREQUENCY)) {
                    fade = 1.0 - ((i - (numSamples - (FADE_OUT_DURATION * SAMPLE_FREQUENCY))) / (FADE_OUT_DURATION * SAMPLE_FREQUENCY));
                }
                sampleValue = (float) (amplitude * Math.sin(i * step) * fade );
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

    public ByteArrayOutputStream generateMorseAudio(String morse, double volumePercent) throws IllegalArgumentException, IOException {
        ByteArrayOutputStream audioStream = new ByteArrayOutputStream();

        double amplitude = Math.round(volumePercent / 100 * Short.MAX_VALUE);

        if (translator.validateInput(morse)) {
            char[][][] phrase = translator.toMorseCharArray(morse);
            int head = 0;

            for(int i = 0; i < phrase.length; i++) { //for each word (e.g.: lorem, ipsum, dolor)
                for(int j = 0; j < phrase[i].length; j++) { //for each letter (e.g.: a,b,c)
                    for(int k = 0; k < phrase[i][j].length; k++) { //for each symbol (e.g.: .,-)
                        switch (phrase[i][j][k]) {
                            case '-':
                                audioStream.write(generateTone(timing.getDahLength() / 1000, frequency, amplitude));
                                break;
                            case '.':
                                audioStream.write(generateTone(timing.getDitLength() / 1000, frequency, amplitude));
                                break;
                        }
                        if(k < phrase[i][j].length-1) {
                            audioStream.write(generateTone(timing.getIntraCharLength() / 1000, frequency, 0)); //intra-char space
                        }
                    }
                    if(j < phrase[i].length-1) {
                        audioStream.write(generateTone(timing.getInterCharLength() / 1000, frequency, 0)); //inter-char space
                    }
                }
                if(i < phrase.length-1) {
                    audioStream.write(generateTone(timing.getInterWordLength() / 1000, frequency, 0)); //inter-word space
                }
            }


            saveMorseToWavFile(audioStream,"/Users/Mark/Desktop/MorseRecordings/","GenTest2");

            return audioStream;
        } else  {
            throw new IllegalArgumentException("Invalid Morse Code");
        }

    }

    public void saveMorseToWavFile(ByteArrayOutputStream audioStream, String filePath, String fileName) throws IOException {
        fileName = !fileName.endsWith(".wav") ? fileName.concat(".wav") : fileName; //append .wav if not already included
        filePath = !filePath.endsWith(File.separator) ? filePath.concat(File.separator) : filePath;// append file separator if not already included


        int dataSize = audioStream.size();
        byte[] temp = audioStream.toByteArray();

        audioStream.reset();

        audioStream.write("RIFF".getBytes());
        audioStream.write(intToByteArray(Integer.reverseBytes(dataSize+36))); // sound data + 44 header bytes - 8 bytes for previous bytes
        audioStream.write("WAVE".getBytes());
        audioStream.write("fmt ".getBytes());
        audioStream.write(intToByteArray(Integer.reverseBytes(SAMPLES_SIZE_IN_BITS)));
        audioStream.write(intToByteArray(Short.reverseBytes((short) 1)));
        audioStream.write(intToByteArray(Short.reverseBytes((short) N_CHANNELS)));
        audioStream.write(intToByteArray(Integer.reverseBytes((int)SAMPLE_FREQUENCY)));
        audioStream.write(intToByteArray(Integer.reverseBytes((int)SAMPLE_FREQUENCY* SAMPLES_SIZE_IN_BITS * N_CHANNELS / 8)));
        audioStream.write(intToByteArray(Short.reverseBytes((short)(N_CHANNELS * SAMPLES_SIZE_IN_BITS / 8))));
        audioStream.write(intToByteArray(Short.reverseBytes((short) SAMPLES_SIZE_IN_BITS)));
        audioStream.write("data".getBytes());
        audioStream.write(intToByteArray(Integer.reverseBytes(dataSize + 44))); //comparing the hex values wih the original method there seems to be a descrepancy here, but it doesn't seem to affect it functioning
        audioStream.write(temp);

        try(OutputStream outputStream = Files.newOutputStream(Paths.get(filePath+fileName))) {
            audioStream.writeTo(outputStream);
        }
    }

    private byte[] intToByteArray(int i) {
        return BigInteger.valueOf(i).toByteArray();
    }

    public static void main(String[] args) {
        MorsePlayer morsePlayer = new MorsePlayerBuilder()
                .withInterruptBehavior(InterruptBehavior.DELAY)
                .build();


            String morse = "Lorem";

        try {
            morsePlayer.generateMorseAudio(morse,100);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}

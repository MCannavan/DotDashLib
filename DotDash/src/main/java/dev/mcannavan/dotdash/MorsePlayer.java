package dev.mcannavan.dotdash;

import javax.sound.sampled.*;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.*;

public class MorsePlayer {

    private static final float SAMPLE_FREQUENCY = 44100;
    private final SourceDataLine line;
    //private final TimeTable timeTable = new TimeTable("MorseEpochs");
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final ArrayList<Future<?>> scheduledFutures = new ArrayList<>();
    private MorseTranslator translator;
    private IMorseTiming timing;
    private double frequency = 700;
    private int globalDelay = 0;
    private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private static final int bitDepth = 16; // the Bit depth; assuming 16 bits per sample
    private static final int nChannels = 1; // Number of channels; assuming mono
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

    public void  submitWithAutoRemoval(Runnable task, int delay) {
        scheduledFutures.add(executor.schedule(
        () -> {
            try {
                task.run();
            } finally {
                scheduledFutures.removeIf(Future::isDone);
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
            buffer[2 * i + 1] = (byte) (sample >> 8);
        }
        //long actualTime = System.currentTimeMillis();
        //timeTable.addRecord(expectedTime, actualTime);
        line.write(buffer, 0, buffer.length);
        if(globalDelay > 0) {
            globalDelay -= duration;
        }
    }

    public void playMorse(double volumePercent, String morse) {
        playMorse(volumePercent, 0, morse);
    }

    //TODO find out why this doesn't function on Windows, for now using saveToWav works as an alternative
    public void playMorse(double volumePercent, int initialDelay, String morse) throws IllegalArgumentException {
        if (interruptBehavior == InterruptBehavior.NONE && line.isActive()) {
            return;
        }
        double amplitude = Math.round(volumePercent / 100 * 32767d);
        int delayTotal = initialDelay;

        switch (interruptBehavior) {
            case NONE:
            case INTERRUPT:
                for(int i = 0; i <scheduledFutures.size(); i++) {
                    scheduledFutures.get(i).cancel(true);
                }
                scheduledFutures.clear();
                line.flush();
                globalDelay = 0;
                break;
            case DELAY:
                delayTotal += globalDelay;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + interruptBehavior);
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
                                    submitWithAutoRemoval(() -> playTone(timing.getDahLength() / 1000d, frequency, amplitude),delayTotal);
                                    delayTotal += timing.getDahLength();
                                    break;
                                case '.':
                                    submitWithAutoRemoval(() -> playTone(timing.getDitLength() / 1000d, frequency, amplitude),delayTotal);
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
            executor.schedule(this::closeLine, delayTotal, TimeUnit.MILLISECONDS);
            globalDelay += delayTotal;
        }
    }

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
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
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

    public void saveToWav(double volumePercent, String fileName, String morse) throws IllegalArgumentException {
        double amplitude = Math.round(volumePercent / 100 * 32767d);

        if(fileName.endsWith(".wav")) { //remove .wav from filename
            fileName = fileName.substring(0,fileName.length()-3);
        }

        fileName = fileName + ".wav";
        File file = new File(fileName);
        String filePath = file.getAbsolutePath();

        try(RandomAccessFile raw = new RandomAccessFile(file.getAbsolutePath(), "rw")) {
            writeWavHeader(raw);
            if (translator.validateInput(morse)) {
                char[][][] phrase = translator.toMorseCharArray(morse);

                for(int i = 0; i < phrase.length; i++) { //for each word (e.g.: lorem, ipsum, dolor)
                    for(int j = 0; j < phrase[i].length; j++) { //for each letter (e.g.: a,b,c)
                        for(int k = 0; k < phrase[i][j].length; k++) { //for each symbol (e.g.: .,-)
                            switch (phrase[i][j][k]) {
                                case '-':
                                    saveToneToWav(raw, timing.getDahLength() / 1000d, frequency, amplitude);
                                    break;
                                case '.':
                                    saveToneToWav(raw, timing.getDitLength() / 1000d, frequency, amplitude);
                                    break;
                            }
                            saveToneToWav(raw, timing.getIntraCharLength() / 1000d,frequency,0); //intra-char space
                        }
                        saveToneToWav(raw, timing.getInterCharLength() / 1000d,frequency,0); //inter-char space
                    }
                    saveToneToWav(raw, timing.getInterWordLength() / 1000d,frequency,0); //inter-word space
                }


                closeFile(raw);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToneToWav(RandomAccessFile raw, double duration, double frequency, double amplitude) {
        final double FADE_IN_DURATION = duration * 0.05;
        final double FADE_OUT_DURATION = duration * 0.055;

        int numSamples = (int) (duration * SAMPLE_FREQUENCY);
        double step = 2 * Math.PI * frequency / SAMPLE_FREQUENCY;
        try {
            for (int i = 0; i < numSamples; i++) {
                double fade = 1.0;

                if (i < FADE_IN_DURATION * SAMPLE_FREQUENCY) {
                    fade = i / (FADE_IN_DURATION * SAMPLE_FREQUENCY);
                } else if (i > numSamples - (FADE_OUT_DURATION * SAMPLE_FREQUENCY)) {
                    fade = 1.0 - ((i - (numSamples - (FADE_OUT_DURATION * SAMPLE_FREQUENCY))) / (FADE_OUT_DURATION * SAMPLE_FREQUENCY));
                }

                float sampleValue = (float) (amplitude * Math.sin(i * step) * fade);

                writeSample(raw, sampleValue);
            }
        } catch (IOException e) {
            System.out.println("I/O exception occurred while writing data");
        }
    }

    private void writeWavHeader(RandomAccessFile raw) throws IOException {
        raw.setLength(0);
        raw.writeBytes("RIFF");
        raw.writeInt(0); // Final file size not known yet, write 0. This is = sample count + 36 bytes from header.
        raw.writeBytes("WAVE");
        raw.writeBytes("fmt ");
        raw.writeInt(Integer.reverseBytes(16)); // Sub-chunk size, 16 for PCM
        raw.writeShort(Short.reverseBytes((short) 1)); // AudioFormat, 1 for PCM
        raw.writeShort(Short.reverseBytes((short)nChannels));// Number of channels, 1 for mono, 2 for stereo
        raw.writeInt(Integer.reverseBytes((int)SAMPLE_FREQUENCY)); // Sample rate
        raw.writeInt(Integer.reverseBytes((int)SAMPLE_FREQUENCY*bitDepth*nChannels/8)); // Byte rate, SampleRate*NumberOfChannels*bitDepth/8
        raw.writeShort(Short.reverseBytes((short)(nChannels*bitDepth/8))); // Block align, NumberOfChannels*bitDepth/8
        raw.writeShort(Short.reverseBytes((short)bitDepth)); // Bit Depth
        raw.writeBytes("data");
        raw.writeInt(0); // Data chunk size not known yet, write 0. This is = sample count.
    }

    private static void writeSample(RandomAccessFile raw, float floatValue) throws IOException {
        short sample = (short) (floatValue);
        raw.writeShort(Short.reverseBytes(sample));
    }


    private static void closeFile(RandomAccessFile raw) throws IOException {
        raw.seek(4);
        raw.writeInt(Integer.reverseBytes((int) raw.length() - 8));
        raw.seek(40);
        raw.writeInt(Integer.reverseBytes((int) raw.length() - 44));
        raw.close();
    }

    private AudioFormat getAudioFormat() {
        int sampleSizeInBits = 16;
        int channels = 1;
        boolean signed = true;
        boolean bigEndian = true;
        return new AudioFormat(SAMPLE_FREQUENCY, sampleSizeInBits, channels, signed, bigEndian);
    }


}

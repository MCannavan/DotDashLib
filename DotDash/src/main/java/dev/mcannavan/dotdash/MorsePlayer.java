package dev.mcannavan.dotdash;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;


//TODO
// - move on-demand playing to separate class
// - write unit tests for all methods
// - code cleanup
// - implement better exception handling & throwing

public class MorsePlayer {
    private static final float SAMPLE_FREQUENCY = 44100;
    private static final int SAMPLES_SIZE_IN_BITS = 16;
    private static final int N_CHANNELS = 1;

    private MorseTranslator translator;
    private IMorseTiming timing;
    private double frequency; //Tone frequency in Hertz (Hz)
    
    //private final SourceDataLine line;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final ArrayList<Future<?>> scheduledFutures = new ArrayList<>();
    private final ArrayList<Future<?>> discardedFutures = new ArrayList<>();

//    private int globalDelay = 0;

//    private InterruptBehavior interruptBehavior = InterruptBehavior.NONE;

    private HashMap<Character,byte[]> pregeneratedChars = new HashMap<Character,byte[]>();

    private byte[][] pregeneratedSpaces = new byte[2][];

    private int volumePercent = 100;

//    public enum InterruptBehavior { //enum for interrupt behavior of playMorse() calls
//        NONE, //calls will be ignored if currently playing
//        DELAY, //new calls will be delayed until current call is finished
//        INTERRUPT //new calls will interrupt current call
//    }

    MorsePlayer() {
//        try {
//            line = AudioSystem.getSourceDataLine(new AudioFormat(
//                    SAMPLE_FREQUENCY, 16,
//                    N_CHANNELS, true, false));
//        } catch (LineUnavailableException e) {
//            throw new RuntimeException(e);
//        }
    }

    public static final class MorsePlayerBuilder {
        private static final int DEFAULT_WPM = 20;
        private static final int DEFAULT_FREQUENCY = 750;

        private MorseTranslator translator;
        private IMorseTiming timing;
        private double frequency;

//        private InterruptBehavior interruptBehavior;

        public MorsePlayerBuilder() {
            this.timing = null; //Average morse speed and format
            this.frequency = DEFAULT_FREQUENCY;
        }

        private MorseTranslator getDefaultTranslator() {
            if (this.translator == null) {
                this.translator = new MorseTranslator()
                        .addMap(CharacterSet.LATIN)
                        .addMap(CharacterSet.PUNCTUATION)
                        .addMap(CharacterSet.ARABIC_NUMERALS);
            }
            return this.translator;
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

        public MorsePlayer build() {
            MorsePlayer morsePlayer = new MorsePlayer();

            if (this.timing == null) { //use default timing if not set
                this.timing = MorseTimingFactory.createParisTimingFromWpm(DEFAULT_WPM);
            }
            if (this.translator == null) {
                this.translator = getDefaultTranslator();
            }
            morsePlayer.builderSetTiming(timing);
            morsePlayer.buildersetTranslator(translator);
            morsePlayer.builderSetFrequency(frequency);
            try {
                morsePlayer.generateCharacters(100);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return morsePlayer;
        }
    }

    public IMorseTiming getTiming() {
        return timing;
    }



    public void setTiming(IMorseTiming timing) {
        this.timing = timing;
        try {
            generateCharacters(volumePercent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void builderSetTiming(IMorseTiming timing) {
        this.timing = timing;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
        try {
            generateCharacters(volumePercent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void builderSetFrequency(double frequency) {
        this.frequency = frequency;
    }

//    public InterruptBehavior getInterruptBehavior() {
//        return interruptBehavior;
//    }

//    public void setInterruptBehavior(InterruptBehavior behavior) {
//        this.interruptBehavior = behavior;
//    }

    public MorseTranslator getTranslator() {
        return translator;
    }

    public void setTranslator(MorseTranslator translator) {
        this.translator = translator;
        try {
            generateCharacters(volumePercent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void buildersetTranslator(MorseTranslator translator) {
            this.translator = translator;
    }

//    private boolean openLine() throws LineUnavailableException {
//        if (!line.isOpen()) {
//            line.open();
//            line.start();
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    private void closeLine(boolean forced) {
//        if (!forced) {
//            if (line.isOpen()) {
//                line.drain();
//                line.close();
//            }
//        } else {
//            line.close();
//        }
//    }
//
//    private void submitWithAutoRemoval(Runnable task, int delay) {
//        scheduledFutures.add(executor.schedule(
//                () -> {
//                    try {
//                        task.run();
//                    } finally {
//                        scheduledFutures.removeIf(Future::isDone);
//                        if(!discardedFutures.isEmpty()) {
//                            discardedFutures.removeIf(Future::isDone);
//                        }
//                    }
//                }, delay, TimeUnit.MILLISECONDS));
//    }
//
//    private void playTone(double duration, double frequency, double amplitude) {
//        final double FADE_IN_DURATION = duration * 0.05;
//        final double FADE_OUT_DURATION = duration * 0.055;
//        int numSamples = (int) (duration * SAMPLE_FREQUENCY);
//        byte[] buffer = new byte[2 * numSamples];
//
//        double step = 2 * Math.PI * frequency / SAMPLE_FREQUENCY;
//        for (int i = 0; i < numSamples; i++) {
//            double fade = 1.0;
//            if (i < FADE_IN_DURATION * SAMPLE_FREQUENCY) { // fade-in when during fade-in duration
//                fade = i / (FADE_IN_DURATION * SAMPLE_FREQUENCY);
//            } else if (i > numSamples - (FADE_OUT_DURATION * SAMPLE_FREQUENCY)) { // fade-out when during fade-out duration
//                fade = 1.0 - ((i - (numSamples - (FADE_OUT_DURATION * SAMPLE_FREQUENCY))) / (FADE_OUT_DURATION * SAMPLE_FREQUENCY));
//            }
//            short sample = (short) (amplitude * Math.sin(i * step) * fade);
//            buffer[2 * i] = (byte) sample;
//            buffer[2 * i + 1] = (byte) (sample >> 8); //arithmetic shift bits rightwards by 8 for monotone
//        }
//        line.write(buffer, 0, buffer.length);
//        if(globalDelay > 0) {
//            globalDelay -= (int) duration;
//        }
//    }
//
//    private void playMorse(double volumePercent, String morse) {
//        playMorse(volumePercent, 0, morse);
//    }
//
//    //TODO: find out why this doesn't function on Windows, saveToWav works as an alternative
//    private void playMorse(double volumePercent, int initialDelay, String morse) throws IllegalArgumentException {
//        double amplitude = Math.round(volumePercent / 100 * Short.MAX_VALUE);
//        int delayTotal = initialDelay;
//
//        switch (interruptBehavior) {
//            case INTERRUPT:
//                int interruptDelay = initialDelay < 100 ? 0 : initialDelay-100; //if initial delay is less than 100 ms, set to 0
//                discardedFutures.addAll(scheduledFutures);
//                scheduledFutures.clear();
//                globalDelay = 0;
//
//                submitWithAutoRemoval(() -> {
//                    for(int i = 0; i < discardedFutures.size(); i++) {
//                        discardedFutures.get(i).cancel(true);
//                    }
//                }, interruptDelay);
//                break;
//            case DELAY:
//                break;
//            case NONE:
//                if(!scheduledFutures.isEmpty()) {
//                    return;
//                }
//                break;
//            default:
//                throw new IllegalStateException("Unexpected value of InterruptBehavior: " + interruptBehavior);
//        }
//        try {
//            openLine();
//            if (translator.validateInput(morse)) {
//                char[][][] phrase = translator.toMorseCharArray(morse);
//                for (int i = 0; i < phrase.length; i++) { //for each word
//                    for (int j = 0; j < phrase[i].length; j++) { //for each letter
//                        for (int k = 0; k < phrase[i][j].length; k++) { //for each symbol
//                            switch (phrase[i][j][k]) {
//                                case '-':
//                                    submitWithAutoRemoval(() -> playTone(timing.getDahLength() / 1000, //convert Ms to seconds
//                                            frequency, amplitude), delayTotal);
//                                    delayTotal += (int) timing.getDahLength();
//                                    break;
//                                case '.':
//                                    submitWithAutoRemoval(() -> playTone(timing.getDitLength() / 1000, //convert Ms to seconds
//                                            frequency, amplitude), delayTotal);
//                                    delayTotal += (int) timing.getDitLength();
//                                    break;
//                            }
//                            if (k < phrase[i][j].length - 1) {
//                                delayTotal += (int) timing.getIntraCharLength();
//                            }
//                        } //end for each symbol
//                        if (j < phrase[i].length - 1) {
//                            delayTotal += (int) timing.getInterCharLength();
//                        }
//                    } //end for each letter
//                    delayTotal += (int) timing.getInterWordLength();
//                } //end for each word
//
//            } else {
//                throw new IllegalArgumentException("Invalid morse code character in string");
//            }
//        } catch (LineUnavailableException e) {
//            e.printStackTrace();
//            System.out.println("test");
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            globalDelay += delayTotal;
//        }
//    }

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
            byte[] temp = ByteBuffer.allocate(2).putShort(Short.reverseBytes((short) Math.round(sampleValue))).array();
            for(byte b : temp) {
                result[head] = b;
                head++;
            }
        }
        return result;
    }

    //TODO
    // test method functionality
    // benchmark speed compared to on demand generation
    private void generateCharacters(double volumePercent) throws IOException {
        if (timing == null) {
            return;
        } else if (translator == null) {
            return;
        }
        pregeneratedChars = null;
        HashMap<Character,byte[]> result = new HashMap<>();
        byte[][] spaces = new byte[2][];
        double amplitude = volumePercent /100*Short.MAX_VALUE;
        for (Map.Entry<Character, String> entry : translator.getMap().entrySet()) {
            Character key = entry.getKey();
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();

            byte[] dit = generateTone(timing.getDitLength() / 1000, frequency, amplitude);
            byte[] dah = generateTone(timing.getDahLength() / 1000, frequency, amplitude);
            byte[] intraCharSpace = generateTone(timing.getIntraCharLength() / 1000, frequency, 0);

            char[] temp = translator.toMorseCharArray(key.toString())[0][0];
            for(int i = 0; i < temp.length; i++) {
                switch (temp[i]) {
                    case '.':
                        bytes.write(dit);
                        break;
                    case '-':
                        bytes.write(dah);
                        break;
                }
                if(i < temp.length-1) {
                    bytes.write(intraCharSpace); //intra-char space
                }
            }
            result.put(key,bytes.toByteArray());
        }
        spaces[0] = generateTone(timing.getInterCharLength() / 1000, frequency, 0);
        spaces[1] = generateTone(timing.getInterWordLength() / 1000, frequency, 0);

        pregeneratedSpaces = spaces;
        pregeneratedChars = result;
    }

    public ByteArrayOutputStream generateMorseAudio(String morse, int volumePercent) throws IllegalArgumentException, IOException {
        if (translator.validateInput(morse)) {
            if(volumePercent != this.volumePercent) {
                System.out.println("regenning");
                generateCharacters(volumePercent);
                this.volumePercent = volumePercent;
            }
            ByteArrayOutputStream audioStream = new ByteArrayOutputStream();
            morse = morse.toUpperCase();
            String[] split = morse.split(" ");
            char[][] phrase = new char[split.length][];
            for (int i = 0; i < split.length; i++) {
                phrase[i] = split[i].toCharArray();
            }

            for (int i = 0; i < phrase.length; i++) {
                for (int j = 0; j < phrase[i].length; j++) {

                    audioStream.write(pregeneratedChars.get(phrase[i][j]));
                    if (j < phrase[i].length - 1) {

                        audioStream.write(pregeneratedSpaces[0]); //inter-char space
                    }
                }
                if (i < phrase.length - 1) {
                    audioStream.write(pregeneratedSpaces[1]); //inter-word space
                }
            }

            return audioStream;
        } else {
            throw new IllegalArgumentException("Invalid Morse Code");
        }
    }


    public void saveMorseToWavFile(ByteArrayOutputStream audioStream, String filePath, String fileName) throws IOException {
        fileName = !fileName.endsWith(".wav") ? fileName.concat(".wav") : fileName; //append .wav if not already included
        //filePath = !filePath.endsWith(File.separator) ? filePath.concat(File.separator) : filePath;// append file separator if not already included

        Path relativePath = Paths.get(filePath, fileName);
        Path absolutePath = relativePath.toAbsolutePath().normalize();

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
        audioStream.write(intToByteArray(Integer.reverseBytes(dataSize + 44))); //comparing the hex values wih the original method there is a descrepancy here, but it doesn't seem to affect it functioning
        audioStream.write(temp);

        try(OutputStream outputStream = Files.newOutputStream(absolutePath)) {
            audioStream.writeTo(outputStream);
        }
    }

    private byte[] intToByteArray(int i) {
        return BigInteger.valueOf(i).toByteArray();
    }

    public static void main(String[] args) {
        MorsePlayer morsePlayer = new MorsePlayerBuilder()
                //.withInterruptBehavior(InterruptBehavior.DELAY)
                .build();


            String morse = "Nestled within the bustling marketplace a vibrant tapestry of sights and sounds unfolded" +
                    " The air hung heavy with the aroma of exotic spices freshly baked bread, and sizzling meats. Merchants" +
                    " hawked their wares in a cacophony of languages their voices weaving a rhythmic chant as they boasted of" +
                    " exquisite silks handcrafted jewelry and glistening fruits from distant lands Curious onlookers " +
                    "adorned in colorful garments meandered through the throngs, their eyes wide with wonder Children " +
                    "chased after playful pigeons their laughter echoing through the cobblestone streets Above a canopy" +
                    " of awnings cast a kaleidoscope of shadows sheltering the crowd from the relentless sun In a secluded" +
                    "corner a lone musician strummed a melancholic melody on his lute The mournful notes seemed to tell a" +
                    "tale of love lost and journeys undertaken Nearby a group of artisans hammered away at metal their " +
                    "rhythmic clanging a stark counterpoint to the musicians gentle serenade As the day wore on the shadows" +
                    " stretched long painting the marketplace in a golden glow. The energy slowly began to wane as the " +
                    "merchants tallied their earnings and prepared to call it a day Yet, a sense of satisfaction lingered" +
                    " in the air a testament to the successful exchange of goods and the stories woven within the bustling" +
                    " marketplace";

            String shortMorse = "SOS";

            String[] alphabetTest = {
                    "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
            };

        try {

            ByteArrayOutputStream audio = morsePlayer.generateMorseAudio(shortMorse,100);
            morsePlayer.saveMorseToWavFile(audio, "DotDash/src/test/java/dev/mcannavan/dotdash/morseSamples/","shortMorse.wav");
            audio = morsePlayer.generateMorseAudio(morse,100);
            morsePlayer.saveMorseToWavFile(audio, "DotDash/src/test/java/dev/mcannavan/dotdash/morseSamples/","LongMorse.wav");

            for (String item : alphabetTest) {
                String temp = "";
                for(char i : morsePlayer.getTranslator().getCharacter(item)) {
                    temp = temp.concat(String.valueOf(i));
                }
                String name = item + "_["+ temp + "].wav";
                audio = morsePlayer.generateMorseAudio(item,100);
                morsePlayer.saveMorseToWavFile(audio, "DotDash/src/test/java/dev/mcannavan/dotdash/morseSamples/",name);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //System.exit(0);

    }
}

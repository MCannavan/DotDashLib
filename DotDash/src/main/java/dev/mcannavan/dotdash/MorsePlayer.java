package dev.mcannavan.dotdash;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

//TODO
// - write unit tests for all methods
// - code cleanup
// - implement better exception handling & throwing
// - add javadocs

public class MorsePlayer {
    private static final float SAMPLE_FREQUENCY = 44100;
    private static final int SAMPLES_SIZE_IN_BITS = 16;
    private static final int N_CHANNELS = 1;

    private MorseTranslator translator;
    private IMorseTiming timing;
    private double frequency; //Tone frequency in Hertz (Hz)


    private HashMap<Character,byte[]> pregeneratedChars = new HashMap<Character,byte[]>();

    private byte[][] pregeneratedSpaces = new byte[2][];

    private int volumePercent = 100;

    MorsePlayer() {

    }


    public static final class MorsePlayerBuilder {
        private static final int DEFAULT_WPM = 20;
        private static final int DEFAULT_FREQUENCY = 750;

        private MorseTranslator translator;
        private IMorseTiming timing;
        private double frequency;

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
            morsePlayer.builderSetTranslator(translator);
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

    private void builderSetTranslator(MorseTranslator translator) {
            this.translator = translator;
    }

    private byte[] generateTone(float duration, double frequency, double amplitude) {
        if(duration <= 0) {
            throw new IllegalArgumentException("Duration must be greater than 0");
        }

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

    /**
     * Generates a {@code ByteArrayOutputStream} containing a given {@code String} of morse code
     *
     * @param morse the {@code String} to generate audio data from
     * @param volumePercent the volume of the generated audio data as an {@code int} out of 100
     * @return a {@code ByteArrayOutputStream} containing Bytes of morse audio
     * @throws IllegalArgumentException if the given morse contains a character not found in the {@code MorseTranslator} translator
     * @throws IOException if an IO Exception occurs
     */
    public ByteArrayOutputStream generateMorseAudio(String morse, int volumePercent) throws IllegalArgumentException, IOException {
        if (translator.validateInput(morse)) {
            if(volumePercent != this.volumePercent) {
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
            Map<Integer,Character> invalidChars = translator.findInvalidSymbols(morse);
            int[] indices = new int[invalidChars.size()];
            char[] characters = new char[invalidChars.size()];
            {
                int i = 0;
                for (Map.Entry<Integer, Character> entry : invalidChars.entrySet()) {
                    indices[i] = entry.getKey();
                    characters[i] = entry.getValue();
                    i++;
                }
            }

            for(int i=0; i<indices.length-1; i+=1){
                int m = i;
                for(int j=i+1; j<indices.length; j+=1){
                    if(indices[m]>indices[j])
                        m = j;
                }
                int t = indices[m];
                indices[m] = indices[i];
                indices[i] = t;

                char c = characters[m];
                characters[m] = characters[i];
                characters[i] = c;
            }


            String error = "Invalid Morse Code: ";
            for (int i = 0; i < indices.length; i++) {
                error = error.concat("'"+characters[i]+"' at index "+indices[i]);
                if (i < indices.length - 1) {
                    error = error.concat(", ");
                }
            }
            throw new IllegalArgumentException(error);
        }
    }


    public void saveMorseToWavFile(ByteArrayOutputStream audioStream, String filePath, String fileName) throws IOException {
        fileName = !fileName.endsWith(".wav") ? fileName.concat(".wav") : fileName; //append .wav if not already included

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
        audioStream.write(intToByteArray(Integer.reverseBytes(dataSize + 44))); //comparing the hex values wih the original method there is a discrepancy here, but it doesn't seem to affect it functioning
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
        System.exit(0);

    }
}

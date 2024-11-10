package dev.mcannavan.dotdash;

import java.nio.ByteBuffer;
import java.util.HashMap;

//TODO:
// - add Javadocs
// - add Unit Testing
public class WaveGenerator {

    private static int SAMPLE_FREQUENCY = 44100;
    private static int N_CHANNELS = 1;

    public WaveGenerator() {

    }

    public WaveGenerator(int sampleFrequency) {
        SAMPLE_FREQUENCY = sampleFrequency;
    }


    public static int getnChannels() {
        return N_CHANNELS;
    }

    protected byte[] generateTone(float duration, double frequency, double amplitude) {
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

}

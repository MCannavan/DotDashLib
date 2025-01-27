package dev.mcannavan.dotdash;

import java.nio.ByteBuffer;

//TODO:
// - add Javadocs
// - add Unit Testing
public class WaveGenerator {

    private int sampleFrequency = 44100;
    private final int N_CHANNELS = 1;

    public WaveGenerator() {

    }

    public WaveGenerator(int sampleFrequency) {
        this.sampleFrequency = sampleFrequency;
    }

    public int getNChannels() {
        return this.N_CHANNELS;
    }

    public int getSampleFrequency() {
        return this.sampleFrequency;
    }

    public void setSampleFrequency(int sampleFrequency) {
        this.sampleFrequency = sampleFrequency;
    }

    protected byte[] generateTone(float duration, double frequency, double amplitude) throws IllegalArgumentException {
        if(duration <= 0) {
            throw new IllegalArgumentException("Duration must be greater than 0");
        }

        final double FADE_IN_DURATION = duration * 0.075;
        final double FADE_OUT_DURATION = duration * 0.08;

        int numSamples = (int) (duration * sampleFrequency * N_CHANNELS);
        byte[] result = new byte[numSamples*2];
        int head = 0;

        double step = 2 * Math.PI * frequency / sampleFrequency / N_CHANNELS;
        for (int i = 0; i < numSamples; i++) {
            float sampleValue;
            if(amplitude > 0) {
                double fade = 1.0;

                if (i < FADE_IN_DURATION * sampleFrequency) {
                    fade = i / (FADE_IN_DURATION * sampleFrequency);
                } else if (i > numSamples - (FADE_OUT_DURATION * sampleFrequency)) {
                    fade = 1.0 - ((i - (numSamples - (FADE_OUT_DURATION * sampleFrequency))) / (FADE_OUT_DURATION * sampleFrequency));
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

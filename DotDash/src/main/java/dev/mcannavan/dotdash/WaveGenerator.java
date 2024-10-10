package dev.mcannavan.dotdash;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class WaveGenerator {

    private static int sampleFrequency = 44100;

    public WaveGenerator(int sampleFrequency) {
        WaveGenerator.sampleFrequency = sampleFrequency;
    }

    private static final HashMap<Character, byte[]> MORSE_CODE_MAP = new HashMap<>();

//    private byte[] saveToneToWav(float duration, double frequency, double amplitude) {
//        final double FADE_IN_DURATION = duration * 0.05;
//        final double FADE_OUT_DURATION = duration * 0.055;
//
//        int numSamples = (int) (duration * sampleFrequency);
//        byte[] result = new byte[numSamples*2];
//        int head = 0;
//
//        double step = 2 * Math.PI * frequency / sampleFrequency;
//        for (int i = 0; i < numSamples; i++) {
//            float sampleValue;
//            if(amplitude > 0) {
//                double fade = 1.0;
//
//                if (i < FADE_IN_DURATION * sampleFrequency) {
//                    fade = i / (FADE_IN_DURATION * sampleFrequency);
//                } else if (i > numSamples - (FADE_OUT_DURATION * sampleFrequency)) {
//                    fade = 1.0 - ((i - (numSamples - (FADE_OUT_DURATION * sampleFrequency))) / (FADE_OUT_DURATION * sampleFrequency));
//                }
//                sampleValue = (float) (amplitude * Math.sin(i * step) * fade);
//            } else {
//                sampleValue = 0;
//            }
//            byte[] temp = ByteBuffer.allocate(2).putShort(Short.reverseBytes((short) sampleValue)).array();
//            for(byte b : temp) {
//                result[head] = b;
//                head++;
//            }
//        }
//        return result;
//    }

}

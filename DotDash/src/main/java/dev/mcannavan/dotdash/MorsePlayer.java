package dev.mcannavan.dotdash;

import javax.sound.sampled.*;

public class MorsePlayer {

    private static final float SAMPLE_FREQUENCY = 44100;
    private final SourceDataLine line;
    private MorseTranslator translator;
    private IMorseTiming timing;
    private double frequency = 700;

    public MorsePlayer() {
        try {
            line = AudioSystem.getSourceDataLine(new AudioFormat(
                    SAMPLE_FREQUENCY, 16,
                    1, true, false));
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
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

    public boolean openLine() throws LineUnavailableException {
        if (!line.isOpen()) {
            line.open();
            line.start();
            return true;
        } else {
            return false;
        }
    }

    public boolean closeLine() {
        if (line.isOpen()) {
            line.drain();
            line.close();
            return true;
        } else {
            System.out.println("line is not open");
            return false;
        }
    }

    public void playTone(double duration, double cycles, double amplitude) {
        int numSamples = (int) (duration * SAMPLE_FREQUENCY);
        byte[] buffer = new byte[2 * numSamples];

        double step = 2 * Math.PI * cycles / SAMPLE_FREQUENCY;
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

    public void playMorse(String morse) {
        Thread t = new Thread(() -> {
            try {
                openLine();
                if (translator.validateInput(morse)) {
                    char[][][] phrase = translator.toMorseCharArray(morse);
                    for (int i = 0; i < phrase.length; i++) {
                        for (int j = 0; j < phrase[i].length; j++) {
                            for (int k = 0; k < phrase[i][j].length; k++) {
                                switch (phrase[i][j][k]) {
                                    case '-':
                                        playTone(timing.getDahLength() / 1000d, frequency, 32767d);
                                        break;
                                    case '.':
                                        playTone(timing.getDitLength() / 1000d, frequency, 32767d);
                                        break;
                                }
                                if (k < phrase[i][j].length - 1) {
                                    Thread.sleep(Math.round(timing.getIntraCharLength()));
                                }
                            }

                            if (j < phrase[i].length - 1) {
                                Thread.sleep(Math.round(timing.getInterCharLength()));
                            }
                        }
                        if (i < phrase.length - 1) {
                            Thread.sleep(Math.round(timing.getInterWordLength()));
                        }
                    }
                } else {
                    System.out.println("Failed to validate input");
                }
            } catch (InterruptedException | LineUnavailableException e) {
                e.printStackTrace();
            } finally {
                closeLine();
            }
        });
        t.start();
    }

}
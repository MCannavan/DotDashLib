package dev.mcannavan.dotdash;

public class MorsePlayerTest {

    public static void main(String[] args) {

        MorsePlayer player = new MorsePlayer
                .MorsePlayerBuilder()
                .withTiming(MorseTimingFactory.createParisTimingFromWpm(20))
                .withFrequency(500)
                .build();

        String temp = "SOS SOS SOS";
        MorseTranslator translator = player.getTranslator();
        System.out.println(translator.validateInput(temp));
        try {
            System.out.println(translator.toMorseString(temp));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread thread = player.playMorse(100, temp);
        while (thread.isAlive()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        player.playMorse(100, temp.toString());
    }
}

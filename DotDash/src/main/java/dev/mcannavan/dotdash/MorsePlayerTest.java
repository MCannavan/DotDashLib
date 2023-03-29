package dev.mcannavan.dotdash;

import java.util.concurrent.TimeUnit;

public class MorsePlayerTest {

    public static void main(String[] args) {

        MorsePlayer player = new MorsePlayer
                .MorsePlayerBuilder()
                .withTiming(MorseTimingFactory.createParisTimingFromWpm(20))
                .withFrequency(700)
                .build();
        System.out.println(player.getTiming().getDahLength());
        System.out.println(player.getTiming().getDitLength());
        System.out.println(player.getTiming().getIntraCharLength());
        System.out.println(player.getTiming().getInterCharLength());
        System.out.println(player.getTiming().getInterWordLength());

        String temp = "SOS SOS SOS";
        player.playMorse(100, temp);

    }
}

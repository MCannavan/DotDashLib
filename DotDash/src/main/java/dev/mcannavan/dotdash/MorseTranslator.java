package dev.mcannavan.dotdash;

import com.google.common.collect.BiMap;

public class MorseTranslator {
    private final BiMap<Character, String> morseCodeMap;

    // Constructor that accepts a predefined CharacterSet enum
    public MorseTranslator(CharacterSet characterSet) {
        morseCodeMap = characterSet.getCharacterSet();
    }

    // Constructor that accepts a custom character set as a BiMap
    public MorseTranslator(BiMap<Character, String> customCharacterSet) {
        morseCodeMap = customCharacterSet;
    }

    public String toMorse(char c) {
        return morseCodeMap.get(c);
    }

    public char fromMorse(String morse) {
        return morseCodeMap.inverse().get(morse);
    }
}

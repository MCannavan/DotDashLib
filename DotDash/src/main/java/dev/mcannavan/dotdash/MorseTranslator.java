package dev.mcannavan.dotdash;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import java.util.Map;

public class MorseTranslator {

    private final BiMap<Character, String> characterMap;

    public MorseTranslator() {
        characterMap = HashBiMap.create();
    }

    public BiMap<Character, String> getMap() {
        return ImmutableBiMap.copyOf(characterMap);
    }

    public MorseTranslator addMap(Map<Character, String> map) throws IllegalArgumentException {
        BiMap<Character, String> temp = BiMap.create(characterMap);
        try {
            temp.putAll(map);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Exception when adding map to characterMap", e);
        }
        characterMap.putAll(map);
        return this;
    }

    public MorseTranslator addPair(char key, String value) {
        characterMap.put(key, value);
        return this;
    }

    public MorseTranslator replacePair(char key, String value) {
        characterMap.forcePut(key, value);
        return this;
    }

    public boolean containsCharacter(char key) {
        return characterMap.containsKey(key);
    }

    public boolean containsMorse(String value) {
        return characterMap.containsValue(value);
    }

    public boolean containsMorse(char[] value) {
        String morse = new String(value);
        return containsMorse(morse);
    }

    public char[][][] toMorse(String text) throws NullPointerException{
        text = text.toUpperCase();
        String[] words = text.split(" ");
        char[][][] morse = new char[words.length][][];
        for (int i = 0; i < words.length; i++) {
            char[] letters = words[i].toCharArray();
            morse[i] = new char[letters.length][];
            for (int j = 0; j < letters.length; j++) {
                if(characterMap.get(letters[j]) != null) {
                    morse[i][j] = characterMap.get(letters[j]).toCharArray();
                } else {
                    throw new NullPointerException("could not find character \""+letters[j]+"\" in characterMap");
                }
            }
        }
        return morse;
    }

    public String toMorseString(String text) throws NullPointerException {
        String[] words = text.split(" ");
        StringBuilder morseBuilder = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            char[] letters = words[i].toCharArray();
            for (int j = 0; j < letters.length; j++) {
                if(characterMap.get(letters[j]) != null) {
                    morseBuilder.append(characterMap.get(letters[j]));
                } else {
                    throw new NullPointerException("could not find character \""+letters[j]+"\" in characterMap");
                }
                if (j < letters.length - 1) {
                    morseBuilder.append(" / "); // Space between letters
                }
            }
            if (i < words.length - 1) {
                morseBuilder.append(" // "); // Space between words
            }
        }

        return morseBuilder.toString();
    }
}

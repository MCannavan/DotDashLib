package dev.mcannavan.dotdash;

import com.google.common.collect.BiMap;

import java.util.Arrays;
import java.util.Map;

public class MorseTranslator implements IMorseTranslator{

    private BiMap<Character, String> characterMap;

    @Override
    public BiMap<Character, String> getMap() {
        return characterMap;
    }

    @Override
    public void addMap(Map<Character, String> map) throws IllegalArgumentException{
        BiMap<Character, String> temp = characterMap;
        try {
            temp.putAll(map);
        } catch(IllegalArgumentException e) {
            throw new IllegalArgumentException("Exception when adding map to characterMap");
        }
        characterMap.putAll(map);
    }

    @Override
    public void addPair(char key, String value) throws IllegalArgumentException {
        BiMap<Character, String> temp = characterMap;
        try {
            temp.put(key, value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("");
        }
        characterMap.put(key, value);
    }

    @Override
    public void replacePair(char key, String value) {
        characterMap.forcePut(key, value);
    }

    @Override
    public boolean containsCharacter(char key) {
        return characterMap.containsKey(key);
    }

    @Override
    public boolean containsMorse(String value) {
        return characterMap.containsValue(value);
    }

    @Override
    public boolean containsMorse(char[] value) {
        String morse = Arrays.toString(value);
        return containsMorse(morse);
    }

    @Override
    public char[][][] toMorse(String text) {
        String[] words = text.split(" ");
        char[][][] morse = new char[words.length][][];
        for (int i = 0; i < words.length; i++) {
            char[] letters = words[i].toCharArray();
            morse[i] = new char[letters.length][];
            for (int j = 0; j < letters.length; j++) {
                morse[i][j] = characterMap.get(letters[j]).toCharArray();
            }
        }
        return morse;
    }
}

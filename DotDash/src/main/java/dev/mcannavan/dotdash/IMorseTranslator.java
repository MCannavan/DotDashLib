package dev.mcannavan.dotdash;

import com.google.common.collect.BiMap;

import java.util.Map;

public interface IMorseTranslator {

    BiMap<Character,String> getMap();

    void addMap(Map<Character,String> map) throws IllegalArgumentException;

    void addPair(char key, String value) throws IllegalArgumentException;

    void replacePair(char key, String value);

    boolean containsCharacter(char key);

    boolean containsMorse(String value);

    boolean containsMorse(char[] value);

    char[][][] toMorse(String text);

}

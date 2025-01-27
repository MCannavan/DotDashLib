package dev.mcannavan.dotdash;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;

import java.util.HashMap;
import java.util.Map;

//TODO add builder pattern
/**
 * A class for translating morse code to and from characters.
 */
public class MorseTranslator {

    private String wordSeparator = " / ";

    private String letterSeparator = " ";

    public String getWordSeparator() {
        return wordSeparator;
    }

    public void setWordSeparator(String wordSeparator) {
        this.wordSeparator = wordSeparator;
    }

    public String getLetterSeparator() {
        return letterSeparator;
    }

    public void setLetterSeparator(String letterSeparator) {
        this.letterSeparator = letterSeparator;
    }


    /**
     * A map of characters and their morse code equivalents.
     */
    private final BiMap<Character, String> characterMap;

    /**
     * Instantiates a new Morse translator.
     */
    public MorseTranslator() {
        characterMap = HashBiMap.create();
    }

    /**
     * Gets a copy of the character map as a {@code BiMap}.
     *
     * @return the map
     */
    public BiMap<Character, String> getMap() {
        return ImmutableBiMap.copyOf(characterMap);
    }

    /**
     * Adds a {@code Map} to the character map.
     *
     * @param map the {@code Map} to add
     * @return This {@code MorseTranslator} object
     * @throws IllegalArgumentException if the {@code Map} contains a key or value that already exists in the map
     */
    public MorseTranslator addMap(Map<Character, String> map) throws IllegalArgumentException {
        BiMap<Character, String> temp = HashBiMap.create(characterMap);
        try {
            temp.putAll(map);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Exception when adding map to characterMap", e);
        }
        characterMap.putAll(map);
        return this;
    }

    /**
     * Add a {@code CharacterSet} to the character map.
     *
     * @param set the {@link CharacterSet} to add
     * @return this {@code MorseTranslator} object
     * @throws IllegalArgumentException if the {@code CharacterSet} contains a key or value that already exists in the map
     */
    public MorseTranslator addMap(CharacterSet set) throws IllegalArgumentException {
        BiMap<Character, String> temp = HashBiMap.create(characterMap);
        try {
            temp.putAll(set.getCharacterSet());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Exception when adding CharacterSet to characterMap", e);
        }
        characterMap.putAll(set.getCharacterSet());
        return this;
    }

    /**
     * Adds a character key-value pair to the character map if the key and value do not already exist.
     *
     * @param key the key
     * @param value the value
     * @return the morse translator
     * @throws IllegalArgumentException if the key or value already exists in the map
     */
    public MorseTranslator addPair(char key, String value) throws IllegalArgumentException {
        if (characterMap.containsValue(value)) {
            throw new IllegalArgumentException("The value \""+value+"\" already exists in the map for key '"+characterMap.inverse().get(value)+"'.");
        } else if (characterMap.containsKey(key)) {
            throw new IllegalArgumentException("The key '"+key+"' already exists in the map with value \""+characterMap.get(key)+"\".");
        } else {
            characterMap.put(key, value);
        }
        return this;
    }

    /**
     * Replaces a character key-value pair in the character map with a new key-value pair.
     *
     * @param key the {@code char} key to be replaced
     * @param value the new morse code sequence to be mapped to the key, as a {@code String}
     * @return this {@code MorseTranslator} object
     * @throws IllegalArgumentException if the value already exists in the map for a different key
     */
    public MorseTranslator replacePair(char key, String value) throws IllegalArgumentException {
        if (characterMap.containsValue(value)) {
            throw new IllegalArgumentException("The value \""+value+ "\" already exists in the map for key '" +characterMap.inverse().get(value)+"'.");
        } else {
            characterMap.replace(key, value);
        }
        return this;
    }

    /**
     * Replaces a character key-value pair in the character map with a new key-value pair.
     *
     * @param key the {@code char} key to be replaced
     * @param value the new morse code sequence to be mapped to the key, as a {@code char[]}
     * @return this {@code MorseTranslator} object
     * @throws IllegalArgumentException if the value already exists in the map for a different key
     */
    public MorseTranslator replacePair(char key, char[] value) throws IllegalArgumentException {
        String valueString = new String(value);
        return replacePair(key, valueString);
    }

    /**
     * Checks if the input character exists with a mapping in the character map.
     *
     * @param key the character to be checked for a mapping
     * @return true if the input character is mapped to a Morse code sequence in the existing map, false otherwise
     */
    public boolean containsCharacter(char key) {
        return characterMap.containsKey(key);
    }

    /**
     * Checks if the input Morse code sequence exists with a mapping in the character map.
     *
     * @param value the Morse code sequence to be checked for a mapping as a {@code String}
     * @return true if the input Morse code sequence is mapped to a character in the existing map, false otherwise
     */
    public boolean containsMorse(String value) {
        return characterMap.containsValue(value);
    }

    /**
     * Checks if the input Morse code sequence exists with a mapping in the character map.
     *
     * @param value the Morse code sequence to be checked for a mapping as a {@code char[]}
     * @return true if the input Morse code sequence is mapped to a character in the existing map, false otherwise
     */
    public boolean containsMorse(char[] value) {
        String morse = new String(value);
        return containsMorse(morse);
    }

    /**
     * Validates whether the input text can be translated to Morse code.
     *
     * @param text the text to be validated as a{@code String}
     * @return true if the input can be translated to Morse code with the current character map, false otherwise
     */
    public boolean validateInput(String text) {
        text = text.toUpperCase();
        char[] characters = text.toCharArray();

        for (char character : characters) {
            if (!Character.isWhitespace(character) && !characterMap.containsKey(character)) {
                return false;
            }
        }
        return true;
    }

    public Map<Integer,Character> findInvalidSymbols(String text) {
        text = text.toUpperCase();
        Map<Integer,Character> result = new HashMap<>();
        char[] characters = text.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            if(!characterMap.containsKey(characters[i]) && !Character.isWhitespace(characters[i])) {
                result.put(i,characters[i]);
            }
        }
        return result;
    }

    //TODO add exception handling
    public String removeInvalidSymbols(String text) {
        if(validateInput(text)) {
            return text;
        }
        String result = text;
        Map<Integer,Character> invalidSymbols = findInvalidSymbols(text);
        for (Map.Entry<Integer,Character> entry : invalidSymbols.entrySet()) {
            int index = entry.getKey();
            String first = result.substring(0,index);
            String second = result.substring(index+1);
            result = first+second;
        }
        return result;
    }

    public int symbolCount(String text) {
        text = text.toUpperCase();
        char[] characters = text.toCharArray();
        int count = 0;
        if(!validateInput(text)) {
            throw new IllegalArgumentException("Failed to validate input: the input contains a character without a corresponding key-value pair in the character map.");
        } else {
            for (char character : characters) {
                if (!Character.isWhitespace(character)) {
                    count += characterMap.get(character).length();
                }
            }
        }
        return count;
    }

    public char[] getCharacter(String character) {
        return getCharacter(character.charAt(0));
    }

    public char[] getCharacter(char character) {
        return characterMap.get(character).toCharArray();
    }

    /**
     * Converts the input text to a {@code char[][][]} of morse code symbols.
     * <ul>
     *     <li>The first dimension of the array represents the words in the input text</li>
     *     <li>The second dimension of the array represents the characters in each word</li>
     *     <li>The third dimension of the array represents the morse code symbols in each character</li>
     * </ul>
     * <br> The {@link #validateInput} method can be used before calling {@code toMorseCharArray} to avoid throwing an {@code IllegalArgumentException}
     *
     * @param text the text to be converted, as a {@code String}
     * @return a {@code char[][][]} of Morse code symbols
     * @throws IllegalArgumentException if the input contains a character that is not in the character map
     */
    public char[][][] toMorseCharArray(String text) throws IllegalArgumentException {
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
                    throw new IllegalArgumentException("could not find character \""+letters[j]+"\" in characterMap");
                }
            }
        }
        return morse;
    }


    /**
     * Converts the input text to a {@code String[][]} of morse code .
     * <ul>
     *     <li>The first dimension of the array represents the words in the input text</li>
     *     <li>The second dimension of the array represents the characters in each word as a string of morse symbols</li>
     * </ul>
     * <br> The {@link #validateInput} method can be used to avoid throwing an {@code IllegalArgumentException}
     * @param text the text to be converted, as a {@code String}
     * @return a {@code String[][]} of Morse code
     * @throws IllegalArgumentException if the input contains a character that is not in the character map
     */
    public String[][] toMorseStringArray(String text) throws IllegalArgumentException {
        char[][][] morseCharArray = toMorseCharArray(text);
        String[][] morseStringArray = new String[morseCharArray.length][];

        for (int i = 0; i < morseCharArray.length; i++) {
            morseStringArray[i] = new String[morseCharArray[i].length];
            for (int j = 0; j < morseCharArray[i].length; j++) {
                morseStringArray[i][j] = new String(morseCharArray[i][j]);
            }
        }

        return morseStringArray;
    }

    /**
     * Converts the input text to a human-readable {@code String} of morse code. Each character is separated by a forward slash, and each word is separated by a double forward slash.
     *
     * @param text the text to be converted, as a {@code String}
     * @return a formatted {@code String} of morse code
     * @throws IllegalArgumentException if the input contains a character that is not in the character map
     */
    public String toMorseString(String text) throws IllegalArgumentException {
        text = text.toUpperCase();
        String[] words = text.split(" ");
        StringBuilder morseBuilder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            char[] letters = words[i].toCharArray();
            for (int j = 0; j < letters.length; j++) {
                if(characterMap.get(letters[j]) != null) {
                    morseBuilder.append(characterMap.get(letters[j]));
                } else {
                    throw new IllegalArgumentException("could not find character \""+letters[j]+"\" in characterMap");
                }
                if (j < letters.length - 1) {
                    morseBuilder.append(" / "); // Space between characters
                }
            }
            if (i < words.length - 1) {
                morseBuilder.append(" // "); // Space between words
            }
        }
        return morseBuilder.toString();
    }
}

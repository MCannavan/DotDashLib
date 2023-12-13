package dev.mcannavan.dotdash;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MorseTranslatorTest {
    private MorseTranslator morseTranslator;

    @BeforeEach
    void setUp() {
        morseTranslator = new MorseTranslator();
        Map<Character, String> morseMap = new HashMap<>();
        morseMap.put('A', ".-");
        morseMap.put('B', "-...");
        morseMap.put('C', "-.-.");
        morseTranslator.addMap(morseMap);
    }

    @Test
    void addPair_newCharacter_characterAdded() {
        morseTranslator.addPair('D', "-..");
        assertTrue(morseTranslator.containsCharacter('D'));
        assertTrue(morseTranslator.containsMorse("-.."));
    }

    @Test
    void replacePair_existingCharacter_characterReplaced() {
        morseTranslator.replacePair('A', ".--");
        assertEquals(".--", morseTranslator.getMap().get('A'));
    }

    @Test
    void containsCharacter_characterExists_returnsTrue() {
        assertTrue(morseTranslator.containsCharacter('A'));
    }

    @Test
    void containsCharacter_characterNotExists_returnsFalse() {
        assertFalse(morseTranslator.containsCharacter('D'));
    }

    @Test
    void containsMorse_morseExists_returnsTrue() {
        assertTrue(morseTranslator.containsMorse(".-"));
    }

    @Test
    void containsMorse_morseNotExists_returnsFalse() {
        assertFalse(morseTranslator.containsMorse(".--."));
    }

    @Test
    void toMorse_inputValidText_returns3DArray() {
        char[][][] expectedResult = new char[][][]{{{'.', '-'}, {'-', '.', '.', '.'}}};
        assertArrayEquals(expectedResult, morseTranslator.toMorseCharArray("AB"));

        morseTranslator = new MorseTranslator();
        morseTranslator.addMap(CharacterSet.LATIN.getCharacterSet());
        char[][][] expectedResult2 = new char[][][]{
                {
                        {'.', '-', '.', '.'},   // L: .-..
                        {'-', '-', '-'},        // O: ---
                        {'.', '-', '.'},        // R: .-.
                        {'.'},                  // E: .
                        {'-', '-'}              // M: --
                },
                {
                        {'.', '.'},             // I: ..
                        {'.', '-', '-', '.'},   // P: .--.
                        {'.', '.', '.'},        // S: ...
                        {'.', '.', '-'},        // U: ..-
                        {'-', '-'}              // M: --
                }
        };
        assertArrayEquals(expectedResult2, morseTranslator.toMorseCharArray("Lorem Ipsum"));
    }

    @Test
    void toMorseString_inputValidText_returnsHumanReadableString() {
        String expectedResult = ".- / -... // .- / -.-.";
        assertEquals(expectedResult, morseTranslator.toMorseString("AB AC"));
    }
}

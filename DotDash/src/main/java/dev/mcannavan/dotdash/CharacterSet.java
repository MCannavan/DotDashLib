package dev.mcannavan.dotdash;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public enum CharacterSet {
    LATIN {
        @Override
        public BiMap<Character, String> getCharacterSet() {
            BiMap<Character, String> latinCharacterSet = HashBiMap.create();
            latinCharacterSet.put('A', ".-");
            latinCharacterSet.put('B', "-...");
            latinCharacterSet.put('C', "-.-.");
            latinCharacterSet.put('D', "-..");
            latinCharacterSet.put('E', ".");
            latinCharacterSet.put('F', "..-.");
            latinCharacterSet.put('G', "--.");
            latinCharacterSet.put('H', "....");
            latinCharacterSet.put('I', "..");
            latinCharacterSet.put('J', ".---");
            latinCharacterSet.put('K', "-.-");
            latinCharacterSet.put('L', ".-..");
            latinCharacterSet.put('M', "--");
            latinCharacterSet.put('N', "-.");
            latinCharacterSet.put('O', "---");
            latinCharacterSet.put('P', ".--.");
            latinCharacterSet.put('Q', "--.-");
            latinCharacterSet.put('R', ".-.");
            latinCharacterSet.put('S', "...");
            latinCharacterSet.put('T', "-");
            latinCharacterSet.put('U', "..-");
            latinCharacterSet.put('V', "...-");
            latinCharacterSet.put('W', ".--");
            latinCharacterSet.put('X', "-..-");
            latinCharacterSet.put('Y', "-.--");
            latinCharacterSet.put('Z', "--..");
            return latinCharacterSet;
        }
    },
    PUNCTUATION {
        @Override
        public BiMap<Character, String> getCharacterSet() {
            BiMap<Character, String> punctuationCharacterSet = HashBiMap.create();
            punctuationCharacterSet.put('.', ".-.-.-");
            punctuationCharacterSet.put(',', "--..--");
            punctuationCharacterSet.put('?', "..--..");
            punctuationCharacterSet.put('!', "-.-.--");
            punctuationCharacterSet.put(':', "---...");
            punctuationCharacterSet.put(';', "-.-.-.");
            punctuationCharacterSet.put('-', "-....-");
            punctuationCharacterSet.put('_', "..--.-");
            punctuationCharacterSet.put('(', "-.--.");
            punctuationCharacterSet.put(')', "-.--.-");
            punctuationCharacterSet.put('"', ".-..-.");
            punctuationCharacterSet.put('\'', ".----.");
            punctuationCharacterSet.put('/', "-..-.");
            punctuationCharacterSet.put('@', ".--.-.");
            punctuationCharacterSet.put('=', "-...-");
            punctuationCharacterSet.put('+', ".-.-.");
            punctuationCharacterSet.put('*', "-..-");
            punctuationCharacterSet.put('$', "...-..-");
            return punctuationCharacterSet;
        }
    },
    ARABIC_NUMERALS {
        @Override
        public BiMap<Character, String> getCharacterSet() {
            BiMap<Character, String> arabicNumeralsCharacterSet = HashBiMap.create();
            arabicNumeralsCharacterSet.put('0', "-----");
            arabicNumeralsCharacterSet.put('1', ".----");
            arabicNumeralsCharacterSet.put('2', "..---");
            arabicNumeralsCharacterSet.put('3', "...--");
            arabicNumeralsCharacterSet.put('4', "....-");
            arabicNumeralsCharacterSet.put('5', ".....");
            arabicNumeralsCharacterSet.put('6', "-....");
            arabicNumeralsCharacterSet.put('7', "--...");
            arabicNumeralsCharacterSet.put('8', "---..");
            arabicNumeralsCharacterSet.put('9', "----.");
            return arabicNumeralsCharacterSet;
        }
    };

    public abstract BiMap<Character, String> getCharacterSet();
}

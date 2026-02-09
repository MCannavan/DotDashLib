# Code Review: DotDashLib

**Date:** February 9, 2026  
**Reviewer:** GitHub Copilot  
**Repository:** MCannavan/DotDashLib

## Executive Summary

DotDashLib is a well-structured Java library for morse code translation and audio generation. The code is generally clean with good test coverage (23 passing tests). However, there are several areas for improvement including bug fixes, consistency issues, and missing documentation.

## Overall Assessment

✅ **Strengths:**
- Clean, readable code structure
- Good use of design patterns (Builder pattern for MorsePlayer)
- Comprehensive test coverage with JUnit 5
- Well-defined interfaces (IMorseTiming)
- Good separation of concerns

⚠️ **Areas for Improvement:**
- Inconsistent use of configurable separators
- Missing JavaDocs in some classes
- Several TODO comments indicating incomplete features
- Potential null pointer risks
- Some code duplication

## Detailed Findings

### 1. Critical Issues (High Priority)

#### 1.1 Inconsistent Separator Usage in MorseTranslator
**Location:** `MorseTranslator.java:311-332`

**Issue:** The `toMorseString()` method hardcodes separators (" / " and " // ") instead of using the configurable `letterSeparator` and `wordSeparator` fields.

**Current Code:**
```java
morseBuilder.append(" / "); // Line 324
morseBuilder.append(" // "); // Line 328
```

**Impact:** The `setWordSeparator()` and `setLetterSeparator()` methods are defined but never used, breaking the API contract.

**Recommendation:** Update the method to use the configurable separators:
```java
morseBuilder.append(letterSeparator);
morseBuilder.append(wordSeparator);
```

#### 1.2 Inconsistent Exception Messages
**Location:** Multiple files

**Issue:** Exception messages have inconsistent capitalization and formatting:
- `MorseTranslator.java:321`: `"could not find character"` (lowercase)
- `MorseTranslator.java:271`: `"could not find character"` (lowercase)
- `MorseTranslator.java:228`: `"Failed to validate input:"` (capitalized)

**Recommendation:** Standardize exception messages to start with capital letters.

### 2. Code Quality Issues (Medium Priority)

#### 2.1 Missing JavaDocs
**Location:** `MorsePlayer.java`, `WaveGenerator.java`

**Issue:** Several classes and methods lack JavaDoc documentation:
- `WaveGenerator` class and all its methods (noted in TODO on line 6)
- Several methods in `MorsePlayer` (noted in TODO on line 16)

**Recommendation:** Add comprehensive JavaDoc comments for all public methods and classes.

#### 2.2 Magic Numbers
**Location:** `WaveGenerator.java:38-39`

**Issue:** Hard-coded fade duration percentages (0.075 and 0.08) should be constants:
```java
final double FADE_IN_DURATION = duration * 0.075;
final double FADE_OUT_DURATION = duration * 0.08;
```

**Recommendation:** Extract these as class-level constants with descriptive names.

#### 2.3 Unused HashMap Type
**Location:** `MorsePlayer.java:38`

**Issue:** Uses `HashMap` instead of `Map` interface:
```java
private HashMap<Character, byte[]> pregenChars = new HashMap<Character, byte[]>();
```

**Recommendation:** Declare as `Map<Character, byte[]>` for better flexibility.

#### 2.4 Code Duplication in addMap Methods
**Location:** `MorseTranslator.java:65-92`

**Issue:** Both `addMap(Map<Character, String>)` and `addMap(CharacterSet)` have identical validation logic.

**Recommendation:** Consider extracting validation into a private method.

### 3. Design Considerations (Low Priority)

#### 3.1 Builder Pattern TODO
**Location:** `MorseTranslator.java:10`

**Issue:** TODO comment indicates planned builder pattern for MorseTranslator.

**Observation:** This is noted for future enhancement. Consider whether a builder would improve the API given that the current approach with `addMap()` is already quite fluent.

#### 3.2 Character Set Expansion
**Location:** `CharacterSet.java:6-7`

**Issue:** TODOs indicate plans to add additional character sets (Greek, Cyrillic, Hebrew, etc.).

**Recommendation:** Document this as a planned feature in README.md to set user expectations.

#### 3.3 Empty Catch Block Risk
**Location:** `MorseTranslator.java:66-74`

**Issue:** Exception is caught and re-thrown, but the temporary BiMap might cause memory overhead for large maps.

**Recommendation:** Consider optimizing this validation approach.

### 4. Potential Bugs

#### 4.1 Empty String Handling in toMorseString
**Location:** `MorseTranslator.java:311-332`

**Issue:** When `text.split(" ")` is called on an empty string or a string with only spaces, it may produce unexpected results.

**Test Required:** Verify behavior with:
- Empty string ""
- Multiple consecutive spaces "A  B"
- Leading/trailing spaces " A B "

#### 4.2 Null Safety in replaceInvalidSymbols
**Location:** `MorseTranslator.java:208-221`

**Issue:** The method modifies indices without accounting for the changed string length when replacement strings are different lengths.

**Example Bug:** If replacing at index 5 with a longer string, subsequent indices become invalid.

**Recommendation:** This method appears to have a logic error and should be refactored to work correctly with variable-length replacements.

### 5. Testing Observations

✅ **Good Test Coverage:**
- MorseTranslatorTest: 10 tests
- MorsePlayerTest: 6 tests  
- ParisTimingTest: 4 tests
- FarnsworthTimingTest: 3 tests

⚠️ **Missing Test Coverage:**
- `WaveGenerator` class has no tests (noted in TODO)
- `MorseTimingFactory` has no tests
- Separator configuration in `MorseTranslator` is not tested
- `replaceInvalidSymbols` method behavior with variable-length replacements

### 6. Dependencies

**Current Dependencies:**
- JUnit 5.9.1 (test)
- Mockito 3.5.9 (test) - ⚠️ Slightly outdated, current is 5.x
- Guava 33.0.0-jre (production)
- Apache Commons Math3 3.6.1 (production)

**Recommendations:**
- Consider updating Mockito to 5.x for newer features and fixes
- Verify that Commons Math3 is actually used in the code (I didn't see any direct usage)
- Evaluate if Guava is needed only for BiMap - could be replaced with custom implementation if trying to minimize dependencies

### 7. Documentation

✅ **Good Documentation:**
- Comprehensive README with examples
- License file present (MIT)
- Clear installation instructions

⚠️ **Documentation Improvements:**
- The separator methods in `MorseTranslator` are not documented in README
- Maven Central version in README (1.0.8) doesn't match pom.xml (1.0.10)
- No CONTRIBUTING.md for external contributors
- No CHANGELOG.md to track version changes

### 8. Security Considerations

✅ **No Critical Security Issues Found**

**Observations:**
- File path handling in `saveMorseToWavFile` uses `normalize()` and `toAbsolutePath()` which is good
- No SQL injection risks (no database usage)
- No command injection risks
- No sensitive data exposure

**Note:** A full CodeQL security scan should still be run to verify.

### 9. Performance Considerations

**Good:**
- Pre-generation of morse characters in `MorsePlayer.generateCharacters()` is efficient
- Use of `StringBuilder` in string concatenation

**Potential Improvements:**
- The `replaceInvalidSymbols` method recreates the string for each invalid character, resulting in O(n²) complexity
- Consider lazy initialization for character sets in `CharacterSet` enum

## Recommendations Priority Matrix

| Priority | Issue | Estimated Effort | Impact |
|----------|-------|------------------|--------|
| HIGH | Fix separator usage in toMorseString | Small | High |
| HIGH | Fix replaceInvalidSymbols logic bug | Medium | High |
| MEDIUM | Add missing JavaDocs | Large | Medium |
| MEDIUM | Standardize exception messages | Small | Low |
| MEDIUM | Add WaveGenerator tests | Medium | Medium |
| LOW | Update README version numbers | Small | Low |
| LOW | Extract magic numbers | Small | Low |
| LOW | Update Mockito dependency | Small | Low |

## Conclusion

DotDashLib is a solid, functional library with good code structure and test coverage. The most critical issues are:
1. The separator configuration bug that breaks the API contract
2. The logic error in `replaceInvalidSymbols` method

These should be addressed in the next release. The codebase is maintainable and follows good Java practices overall. With the recommended improvements, this library would be even more robust and professional.

## Testing Notes

All 23 existing tests pass successfully:
```
mvn clean test
[INFO] Tests run: 23, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

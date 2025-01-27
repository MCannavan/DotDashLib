# DotDashLib
A small Java library for handling the 
translation and audio generation of morse code.

## Table of Contents
- [Installation](#Installation)
- [Usage](#Usage)
- [License](#License)

## About the project
I could not find any library for Java which made it easy to produce the pure tones and timing
needed for morse code, and the java sound API is annoying and difficult to work with, so I created
this library for the niche purpose of playing accurate morse code in pure java.

## Features

- Translate from standard text into human-readable morse code strings
- Ability to use custom morse code translation sets 
  - presets include: latin alphabet, arabic numerals, & punctuation
- Generate Wav Files from text
- Control over the speed, pitch, & volume of the output

## Installation
For maven, add this to your `pom.xml`
```xml
<dependency>
  <groupId>dev.mcannavan</groupId>
  <artifactId>DotDash</artifactId>
  <version>1.0.7</version>
</dependency>
```
then run
```
$ mvn install
```
to install via CLI

Using gradle add these to your build.gradle:

```java
repositories {
    mavenCentral()
}
  
dependencies {
    implementation 'dev.mcannavan.dotdash:1.0.7'
}

```

Also available as a .jar file including sources.jar and javadocs.jar from the releases page.

## Usage
Some basic usage patterns:
#### Translating text to morse code:
```java
    MorseTranslator morseTranslator = new MorseTranslator();
    morseTranslator.addMap(CharacterSet.LATIN.getCharacterSet());
    
    System.out.println(morseTranslator.toMorseString("Lorem Ipsum"));
    
    output:
    > ".-.. / --- / .-. / . / -- // .. / .--. / ... / ..- / --"
```
#
#### adding custom morse translation mappings
```java
    morseTranslator = new MorseTranslator(); //morse translator without any maps
    Map<Character, String> morseMap = new HashMap<>();
    morseMap.put('A', ".-");
    morseMap.put('B', "-...");
    morseMap.put('C', "-.-.");
    morseTranslator.addMap(morseMap);
```

#
#### Initialising a MorsePlayer object
```java
    //A custom timing object (available classes are ParisTiming & Farnsworth Timing  
    IMorseTiming timing = MorseTimingFactory.createParisTimingFromWpm(15);

    //A custom translator object
    Morse Translator morseTranslator = new MorseTranslator();
    morseTranslator.addMap(CharacterSet.LATIN.getCharacterSet());

    MorsePlayer player = new MorsePlayer.MorsePlayerBuilder()
        .withFrequency(700) //pitch, default is 750
        .withTiming(timing) //timing object, default to a PARIS timing of 20 wpm
        .withTranslator(translator) //translator object, default to a translator with latin alphabet, arabic numerals, & punctuation
        .build();

```
#
#### Creating a morse wav file
```java
  MorsePlayer player = new MorsePlayer.MorsePlayerBuilder().build();

  String morse = "Lorem ipsum dolor sit amet consectetur adipiscing elit sed"
        
  try {
    ByteArrayOutputStream audio = morsePlayer.generateMorseAudio(morse,100); //generate an output stream of morse at 100% volume
    morsePlayer.saveMorseToWavFile(audio, "path/to/directory/","morse.wav"); //save an audio file "morse.wav" at the specified location
  } catch (IOException e) {
    throw new RuntimeException(e);
  }
```

## License
Licensed under the [MIT License](LICENSE)

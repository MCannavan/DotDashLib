package dev.mcannavan.dotdash;

import static org.mockito.Mockito.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MorsePlayerTest {

    @Mock
    private MorseTranslator mockTranslator;

    @Mock
    private IMorseTiming mockTiming;

    @Mock
    private WaveGenerator mockWaveGenerator;

    private MorsePlayer morsePlayer;


    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        Map<Character, String> translatorMap = HashBiMap.create();
        translatorMap.put('A', ".-");
        translatorMap.put('B', "-...");

        when(mockTranslator.getMap()).thenReturn((BiMap<Character, String>) translatorMap);
        when(mockTranslator.validateInput(anyString())).thenReturn(true);

        when(mockTranslator.toMorseCharArray(anyString()))
                .thenAnswer(invocation -> {
                    String arg = invocation.getArgument(0);
                    return new char[][][]{{ arg.toCharArray() }};
                });

        when(mockTiming.getDitLength()).thenReturn(50.0F);
        when(mockTiming.getDahLength()).thenReturn(150.0F);
        when(mockTiming.getIntraCharLength()).thenReturn(50.0F);
        when(mockTiming.getInterCharLength()).thenReturn(150.0F);
        when(mockTiming.getInterWordLength()).thenReturn(350.0F);

        //return known byte arrays for any tone request
        when(mockWaveGenerator.generateTone(
                anyFloat(), anyDouble(), anyDouble()))
                .thenReturn(new byte[]{1, 2, 3, 4}); // arbitrary data

        MorsePlayer.MorsePlayerBuilder builder = new MorsePlayer.MorsePlayerBuilder()
                .withTranslator(mockTranslator)
                .withTiming(mockTiming)
                .withWaveGenerator(mockWaveGenerator)
                .withFrequency(750);

        morsePlayer = builder.build();
    }

    @Test
    void builder_withDefaults_returnsTranslator() throws IOException {

        MorsePlayer.MorsePlayerBuilder builder = new MorsePlayer.MorsePlayerBuilder();

        MorsePlayer player = builder.build();

        assertNotNull(player.getTiming());
        assertEquals(750, player.getFrequency(), 0.0001);
        assertNotNull(player.getWaveGenerator());
        assertNotNull(player.getTranslator());
    }

    @Test
    void builder_withCustomTranslatorAndTiming_returnsTranslator() throws IOException {

        MorsePlayer.MorsePlayerBuilder builder = new MorsePlayer.MorsePlayerBuilder()
                .withTranslator(mockTranslator)
                .withTiming(mockTiming)
                .withFrequency(600);

        MorsePlayer player = builder.build();

        // Verify
        assertEquals(600, player.getFrequency(), 0.0001);
        assertSame(mockTranslator, player.getTranslator());
        assertSame(mockTiming, player.getTiming());
    }

    @Test
    void setTiming_withValidInputs_updatesTimingAndGeneratedCharacters() throws IOException {
        IMorseTiming newTiming = mock(IMorseTiming.class);
        when(newTiming.getDitLength()).thenReturn(60.0F);
        when(newTiming.getDahLength()).thenReturn(180.0F);
        when(newTiming.getIntraCharLength()).thenReturn(60.0F);
        when(newTiming.getInterCharLength()).thenReturn(180.0f);
        when(newTiming.getInterWordLength()).thenReturn(420.0F);

        morsePlayer.setTiming(newTiming);

        // Then
        assertSame(newTiming, morsePlayer.getTiming());

        verify(mockWaveGenerator, atLeastOnce()).generateTone(anyFloat(), anyDouble(), anyDouble());
    }

    @Test
    void setTiming_withNullInput_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> morsePlayer.setTiming(null));
    }

    @Test
    void setTranslator_withValidInput_updatesTranslatorAndGeneratedCharacters() throws IOException {
        MorseTranslator newTranslator = mock(MorseTranslator.class);
        Map<Character,String> map = HashBiMap.create();
        map.put('E', ".");
        map.put('T', "-");
        when(newTranslator.getMap()).thenReturn((BiMap<Character, String>) map);
        when(newTranslator.toMorseCharArray(anyString()))
                .thenAnswer(invocation -> {
                    String arg = invocation.getArgument(0);
                    return new char[][][]{{ arg.toCharArray() }};
                });

        morsePlayer.setTranslator(newTranslator);

        assertSame(newTranslator, morsePlayer.getTranslator());
        verify(mockWaveGenerator, atLeastOnce()).generateTone(anyFloat(), anyDouble(), anyDouble());
    }

    @Test
    void setTranslator_withNullInput_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> morsePlayer.setTranslator(null));
    }




    //TODO test morsetranslator methods:
    // morseplayerbuilder_withDefaults_returnsMorsePlayer
    // morsePlayerBuilder_withValidInputs_returnsMorsePlayer
    // morsePlayerBuilder_withInvalidInputs_throwsException
    // setter test methods
    // generateCharacters_
    // generateMorseAudio
    // generateWavFileData
    // intToLittleEndian
    // shortToLittleEndian
}
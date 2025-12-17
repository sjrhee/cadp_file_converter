package com.cadp.fileconverter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class ConverterTest {

    @TempDir
    Path tempDir;

    @Test
    void testProtectProcess() throws Exception {
        // Given
        Path inputFile = tempDir.resolve("input.csv");
        Path outputFile = tempDir.resolve("output.csv");
        
        Files.write(inputFile, List.of("header1,header2,header3", "data1,data2,data3"));

        Config config = Mockito.mock(Config.class);
        when(config.getInputFilePath()).thenReturn(inputFile.toString());
        when(config.getOutputFilePath()).thenReturn(outputFile.toString());
        when(config.getDelimiter()).thenReturn(",");
        when(config.isSkipHeader()).thenReturn(false); // Process header as data for simplicity or set true
        
        // Let's set skipHeader=true to test that logic too
        when(config.isSkipHeader()).thenReturn(true);
        when(config.getMode()).thenReturn("protect");

        Map<Integer, String> policies = new HashMap<>();
        policies.put(1, "policy1"); // encrypt 2nd column
        when(config.getColumnPolicies()).thenReturn(policies);

        CadpService cadpService = Mockito.mock(CadpService.class);
        when(cadpService.protect(anyString(), anyString())).thenAnswer(invocation -> {
            String data = invocation.getArgument(0);
            return "ENC(" + data + ")";
        });

        Converter converter = new Converter(config, cadpService);

        // When
        converter.process();

        // Then
        List<String> lines = Files.readAllLines(outputFile);
        assertEquals(2, lines.size());
        assertEquals("header1,header2,header3", lines.get(0)); // Header copied as is
        assertEquals("data1,ENC(data2),data3", lines.get(1)); // Data processed
    }
}

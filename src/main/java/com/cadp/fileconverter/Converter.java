package com.cadp.fileconverter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Converter {
    private Config config;
    private CadpService cadpService;

    public Converter(Config config) {
        this.config = config;
        this.cadpService = new CadpService(config);
    }

    public void process() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(config.getInputFilePath()));
             BufferedWriter writer = new BufferedWriter(new FileWriter(config.getOutputFilePath()))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                if (lineNumber == 0 && config.isSkipHeader()) {
                    writer.write(line);
                    writer.newLine();
                    lineNumber++;
                    continue;
                }

                String[] parts = line.split(config.getDelimiter(), -1); // Keep empty strings
                
                for (java.util.Map.Entry<Integer, String> entry : config.getColumnPolicies().entrySet()) {
                    int colIdx = entry.getKey();
                    String policy = entry.getValue();
                    
                    if (parts.length > colIdx) {
                        String target = parts[colIdx];
                        String processed;

                        if ("protect".equalsIgnoreCase(config.getMode())) {
                            processed = cadpService.protect(target, policy);
                        } else if ("reveal".equalsIgnoreCase(config.getMode())) {
                            processed = cadpService.reveal(target, policy);
                        } else {
                            processed = target;
                        }

                        parts[colIdx] = processed;
                    }
                }

                writer.write(String.join(config.getDelimiter(), parts));
                writer.newLine();
                lineNumber++;
            }
        }
    }
}

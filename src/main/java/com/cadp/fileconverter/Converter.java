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
        this(config, new CadpService(config));
    }

    public Converter(Config config, CadpService cadpService) {
        this.config = config;
        this.cadpService = cadpService;
    }

    public void process() throws IOException {
        long successCount = 0;
        long failureCount = 0;
        int consecutiveFailures = 0;
        final int MAX_CONSECUTIVE_FAILURES = 10;

        try (BufferedReader reader = new BufferedReader(new FileReader(config.getInputFilePath()));
             BufferedWriter writer = new BufferedWriter(new FileWriter(config.getOutputFilePath()))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                // Header handling
                if (lineNumber == 0 && config.isSkipHeader()) {
                    writer.write(line);
                    writer.newLine();
                    lineNumber++;
                    continue;
                }

                try {
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
                    successCount++;
                    consecutiveFailures = 0; // Reset counter on success

                } catch (Exception e) {
                    failureCount++;
                    consecutiveFailures++;
                    System.err.println("Error processing line " + (lineNumber + 1) + ": " + e.getMessage());
                    
                    if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                        System.err.println("Aborting: Too many consecutive errors (" + MAX_CONSECUTIVE_FAILURES + ").");
                        break; 
                    }
                }
                
                lineNumber++;
            }
        }
        
        System.out.println("Processing complete.");
        System.out.println("Total lines processed: " + (successCount + failureCount) + " (Success: " + successCount + ", Failed: " + failureCount + ")");
    }
}

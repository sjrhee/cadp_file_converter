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
        
        int threads = config.getParallelWorkers();
        if (threads < 1) threads = 1;
        
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(threads);
        java.util.Queue<java.util.concurrent.Future<String>> futures = new java.util.LinkedList<>();
        int pendingLimit = threads * 10; 

        // Shared counters for tracking consecutive failures (must be handled sequentially in writer)
        int consecutiveFailures = 0;
        final int MAX_CONSECUTIVE_FAILURES = 10;

        try (BufferedReader reader = new BufferedReader(new FileReader(config.getInputFilePath()));
             BufferedWriter writer = new BufferedWriter(new FileWriter(config.getOutputFilePath()))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                // Skip empty lines
                if (line.strip().isEmpty()) {
                    continue;
                }

                // Header handling (Main thread)
                if (lineNumber == 0 && config.isSkipHeader()) {
                    writer.write(line);
                    writer.newLine();
                    lineNumber++;
                    continue;
                }

                // Submitting tasks
                final String currentLine = line;
                final int currentLineNum = lineNumber + 1; // 1-based index for logging

                java.util.concurrent.Callable<String> task = () -> {
                    String[] parts = currentLine.split(config.getDelimiter(), -1);
                    
                    for (java.util.Map.Entry<Integer, String> entry : config.getColumnPolicies().entrySet()) {
                        int colIdx = entry.getKey();
                        String policy = entry.getValue();
                        
                        if (parts.length > colIdx) {
                            String target = parts[colIdx];
                            String processed;
                            
                            // Skip processing if target is empty
                            if (target == null || target.strip().isEmpty()) {
                                processed = target;
                            } else {
                                try {
                                    if ("protect".equalsIgnoreCase(config.getMode())) {
                                        processed = cadpService.protect(target, policy);
                                    } else if ("reveal".equalsIgnoreCase(config.getMode())) {
                                        processed = cadpService.reveal(target, policy);
                                    } else {
                                        processed = target;
                                    }
                                } catch (Exception e) {
                                    throw new RuntimeException("Error processing value '" + target + "': " + e.getMessage(), e);
                                }
                            }
                            parts[colIdx] = processed;
                        }
                    }
                    return String.join(config.getDelimiter(), parts);
                };

                futures.add(executor.submit(task));

                // Flow control: Write output if buffer is full
                while (futures.size() >= pendingLimit) {
                    try {
                        String result = futures.poll().get();
                        writer.write(result);
                        writer.newLine();
                        successCount++;
                        consecutiveFailures = 0;
                    } catch (Exception e) {
                        failureCount++;
                        consecutiveFailures++;
                        // Unwrap ExecutionException
                        Throwable cause = e.getCause() != null ? e.getCause() : e;
                        System.err.println("Error processing line (approx order): " + cause.getMessage());
                        
                        if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                            System.err.println("Aborting: Too many consecutive errors (" + MAX_CONSECUTIVE_FAILURES + ").");
                            executor.shutdownNow(); // Stop processing
                            return; // Exit method
                        }
                    }
                }
                
                lineNumber++;
            }

            // Drain remaining futures
            while (!futures.isEmpty()) {
                 try {
                    String result = futures.poll().get();
                    writer.write(result);
                    writer.newLine();
                    successCount++;
                    consecutiveFailures = 0;
                } catch (Exception e) {
                    failureCount++;
                    consecutiveFailures++;
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    System.err.println("Error processing line (approx order): " + cause.getMessage());
                     if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                        System.err.println("Aborting: Too many consecutive errors (" + MAX_CONSECUTIVE_FAILURES + ").");
                        break;
                    }
                }
            }

        } finally {
            executor.shutdownNow();
        }
        
        System.out.println("Processing complete.");
        System.out.println("Total lines processed: " + (successCount + failureCount) + " (Success: " + successCount + ", Failed: " + failureCount + ")");
    }
}

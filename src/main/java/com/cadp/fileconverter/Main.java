package com.cadp.fileconverter;

import org.apache.commons.cli.*;

public class Main {
    public static void main(String[] args) {
        Options options = new Options();
        // Core features
        options.addOption("m", "mode", true, "Operation mode: protect or reveal");
        
        // Input/Output
        options.addOption(Option.builder("i").longOpt("input").hasArg().desc("Input file path").build());
        options.addOption(Option.builder("o").longOpt("output").hasArg().desc("Output file path").build());
        
        // Processing Options
        options.addOption("s", "skip-header", false, "Skip header line");
        options.addOption("d", "delimiter", true, "Column delimiter");
        options.addOption("c", "column", true, "Column index");

        // Advanced Options
        options.addOption("t", "threads", true, "Number of parallel workers");
        options.addOption(Option.builder().longOpt("timeout").hasArg().type(Number.class).desc("Request timeout").build());
        
        options.addOption("h", "help", false, "Print help");

        options.addOption(Option.builder().longOpt("init").desc("Generate sample .env file").build());

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(120);
        formatter.setOptionComparator(null); // Preserve insertion order

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("help")) {
                formatter.printHelp("cadp-file-converter [input_file]", null, options, "\nEnvironment variables (or .env in current dir) required:\nCADP_KMS_HOST, CADP_REGISTRATION_TOKEN, etc.", true);
                return;
            }

            if (cmd.hasOption("init")) {
                java.io.File envFile = new java.io.File(".env");
                if (envFile.exists()) {
                    System.out.println("Warning: .env file already exists in current directory. Skipping generation.");
                } else {
                    try (java.io.FileWriter writer = new java.io.FileWriter(envFile)) {
                        writer.write("# CADP Connection Configuration\n");
                        writer.write("CADP_KMS_HOST=192.168.0.10\n");
                        writer.write("CADP_KMS_PORT=32082\n\n");
                        writer.write("# Authentication\n");
                        writer.write("CADP_REGISTRATION_TOKEN=your_token_here\n");
                        writer.write("CADP_USER_NAME=your_username\n\n");
                        writer.write("# Protection Defaults\n");
                        writer.write("CADP_PROTECTION_POLICY_NAME=dev-users-policy\n");
                        System.out.println("Success: Sample .env file generated in current directory.");
                    }
                }
                return;
            }

            Config config = new Config();
            
            // Handle Input File (Positional or -i)
            if (cmd.getArgList().size() > 0) {
                config.setInputFilePath(cmd.getArgList().get(0));
            } else if (cmd.hasOption("input")) {
                config.setInputFilePath(cmd.getOptionValue("input"));
            } else {
                System.out.println("Error: Input file required");
                formatter.printHelp("cadp-file-converter [input_file]", options);
                System.exit(1);
            }

            // Handle Mode
            if (cmd.hasOption("mode")) {
                String mode = cmd.getOptionValue("mode");
                if ("protect".equalsIgnoreCase(mode) || "encode".equalsIgnoreCase(mode)) {
                    config.setMode("protect");
                } else if ("reveal".equalsIgnoreCase(mode) || "decode".equalsIgnoreCase(mode)) {
                    config.setMode("reveal");
                } else {
                    System.out.println("Error: Invalid mode. Use 'protect' or 'reveal'");
                    System.exit(1);
                }
            } else {
                System.out.println("Error: Mode required (-m protect or -m reveal)");
                formatter.printHelp("cadp-file-converter [input_file]", null, options, "\nEnvironment variables (or .env in current dir) required:\nCADP_KMS_HOST, CADP_REGISTRATION_TOKEN, etc.", true);
                System.exit(1);
            }

            // Output
            if (cmd.hasOption("output")) {
                config.setOutputFilePath(cmd.getOptionValue("output"));
            } else {
                // Auto-generate output filename
                String input = config.getInputFilePath();
                String mode = config.getMode();
                int idx = input.lastIndexOf('.');
                String base = (idx == -1) ? input : input.substring(0, idx);
                String ext = (idx == -1) ? "" : input.substring(idx);
                config.setOutputFilePath(base + "_" + mode + ext);
            }
            

            // Other Options
            if (cmd.hasOption("column")) {
                String[] colValues = cmd.getOptionValues("column");
                for (String val : colValues) {
                    try {
                        String[] parts = val.split("=", 2);
                        int colIdx = Integer.parseInt(parts[0].trim());
                        if (colIdx < 1) {
                            System.err.println("Error: Column index must be >= 1 (value: " + parts[0] + ")");
                            System.exit(1);
                        }
                        String policy = (parts.length > 1) ? parts[1].trim() : config.getDefaultPolicy();
                        config.addColumnPolicy(colIdx - 1, policy); // Convert 1-based to 0-based
                    } catch (NumberFormatException e) {
                        System.err.println("Error: Invalid column index format: " + val);
                        System.exit(1);
                    }
                }
            }

            if (cmd.hasOption("skip-header")) config.setSkipHeader(true);
            if (cmd.hasOption("delimiter")) config.setDelimiter(cmd.getOptionValue("delimiter"));

            if (cmd.hasOption("threads")) config.setParallelWorkers(Integer.parseInt(cmd.getOptionValue("threads")));
            if (cmd.hasOption("timeout")) config.setTimeout(Integer.parseInt(cmd.getOptionValue("timeout")));

            System.out.println("Starting conversion...");
            System.out.println("Input: " + config.getInputFilePath());
            System.out.println("Output: " + config.getOutputFilePath());
            System.out.println("Mode: " + config.getMode());
            System.out.println("Config Loaded - Host: " + config.getApiHost() + ", Default Policy: " + config.getDefaultPolicy());
            System.out.println("Columns to process: " + config.getColumnPolicies());

            long startTime = System.currentTimeMillis();
            java.time.LocalDateTime startDateTime = java.time.LocalDateTime.now();
            System.out.println("Start Time: " + startDateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            Converter converter = new Converter(config);
            converter.process();

            long endTime = System.currentTimeMillis();
            java.time.LocalDateTime endDateTime = java.time.LocalDateTime.now();
            long durationMillis = endTime - startTime;
            
            System.out.println("End Time: " + endDateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            System.out.println("Elapsed Time: " + durationMillis + " ms (" + String.format("%.2f", durationMillis / 1000.0) + " s)");

            System.out.println("Conversion completed successfully.");

        } catch (ParseException e) {
            System.err.println("Command line error: " + e.getMessage());
            formatter.printHelp("cadp-file-converter", options);
            System.exit(1);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("missing host or token")) {
                System.err.println("Configuration Error: Missing required connection parameters.");
                System.err.println("Please ensure CADP_KMS_HOST, CADP_REGISTRATION_TOKEN, etc. are set in .env or environment variables.");
                System.err.println("Tip: Run with --init to generate a sample .env file.");
            } else {
                System.err.println("Initialization Error: " + e.getMessage());
            }
            System.exit(1);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("missing host or token")) {
                System.err.println("Configuration Error: Missing required connection parameters.");
                System.err.println("Please ensure CADP_KMS_HOST, CADP_REGISTRATION_TOKEN, etc. are set in .env or environment variables.");
                System.err.println("Tip: Run with --init to generate a sample .env file.");
            } else {
                System.err.println("Unexpected Error: " + msg);
                e.printStackTrace();
            }
            System.exit(1);
        }
    }
}

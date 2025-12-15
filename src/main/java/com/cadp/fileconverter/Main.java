package com.cadp.fileconverter;

import org.apache.commons.cli.*;

public class Main {
    public static void main(String[] args) {
        Options options = new Options();
        // Core features
        options.addOption("e", "encode", false, "Encode (protect) data");
        options.addOption("d", "decode", false, "Decode (reveal) data");
        
        // Input/Output
        options.addOption("i", "input", true, "Input file path"); // Keeping explicit -i for now or should I use args[0]? crdp uses args[0]. Let's support both or stick to args[0] if I can. 
        // crdp-file-converter <input_file> [flags].  So input file is a positional arg.
        // But my current Main uses -i. I should probably switch to positional if I want to match exactly, but let's stick to -i for safety or check if args has positional? 
        // User said "Implement options". I will keep -i as named option to be safe with Commons CLI or add it as an option if I want to be cleaner? 
        // Actually crdp usage: crdp-file-converter <input_file> [flags]
        // I will stick to -i for now as it makes parsing easier with Commons CLI default parser, or I can check getArgList(). 
        // Let's stick to CLI options provided in request. 
        // Wait, the request output shows <input_file> as positional. 
        // I will ADD support for positional arg if -i is missing? 
        // For now, let's keep -i option for simplicity as I am 'Implementing options'.
        // Let's add the requested flags.

        options.addOption(Option.builder("i").longOpt("input").hasArg().desc("Input file path (deprecated, use positional argument)").build());
        options.addOption(Option.builder("o").longOpt("output").hasArg().desc("Output file path").build());
        
        // Processing Options
        options.addOption("s", "skip-header", false, "Skip header line");
        options.addOption(Option.builder().longOpt("delimiter").hasArg().desc("Column delimiter").build());
        options.addOption("c", "column", true, "Column index");

        // Advanced Options
        options.addOption(Option.builder().longOpt("batch-size").hasArg().type(Number.class).desc("Batch size").build());
        options.addOption("p", "parallel", true, "Number of parallel workers");
        options.addOption(Option.builder().longOpt("timeout").hasArg().type(Number.class).desc("Request timeout").build());
        
        options.addOption("h", "help", false, "Print help");

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("help")) {
                formatter.printHelp("cadp-file-converter [input_file]", options);
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
            if (cmd.hasOption("encode")) {
                config.setMode("protect");
            } else if (cmd.hasOption("decode")) {
                config.setMode("reveal");
            } else {
                System.out.println("Error: Mode required (-e or -d)");
                formatter.printHelp("cadp-file-converter [input_file]", options);
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
            if (cmd.hasOption("batch-size")) config.setBatchSize(Integer.parseInt(cmd.getOptionValue("batch-size")));
            if (cmd.hasOption("parallel")) config.setParallelWorkers(Integer.parseInt(cmd.getOptionValue("parallel")));
            if (cmd.hasOption("timeout")) config.setTimeout(Integer.parseInt(cmd.getOptionValue("timeout")));

            System.out.println("Starting conversion...");
            System.out.println("Input: " + config.getInputFilePath());
            System.out.println("Output: " + config.getOutputFilePath());
            System.out.println("Mode: " + config.getMode());
            System.out.println("Config Loaded - Host: " + config.getApiHost() + ", Default Policy: " + config.getDefaultPolicy());
            System.out.println("Columns to process: " + config.getColumnPolicies());

            Converter converter = new Converter(config);
            converter.process();

            System.out.println("Conversion completed successfully.");

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("cadp-file-converter", options);
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

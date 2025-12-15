package com.cadp.fileconverter;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.HashMap;
import java.util.Map;

public class Config {
    // API Connection
    private String apiHost = "192.168.0.10";
    private int apiPort = 32082;
    private boolean apiTls = false;
    private String registrationToken;
    private String userName;

    // Protection
    private String defaultPolicy = "dev-users-policy";

    // File Processing
    private String delimiter = ",";
    // Map of 0-based column index to policy name
    private Map<Integer, String> columnPolicies = new HashMap<>();
    private boolean skipHeader = false;
    private String inputFilePath;
    private String outputFilePath;
    
    // Batch & Parallel
    private int batchSize = 100;
    private int parallelWorkers = 1;

    // API Config
    private int timeout = 5;

    // Mode
    private String mode; // "protect" or "reveal"

    public Config() {
        loadEnv();
    }
    
    // ...

    public Map<Integer, String> getColumnPolicies() { return columnPolicies; }
    public void addColumnPolicy(int index, String policy) {
        this.columnPolicies.put(index, policy);
    }
    
    // ...

    public String getDefaultPolicy() { return defaultPolicy; }
    public void setDefaultPolicy(String policy) { this.defaultPolicy = policy; }

    public String getDelimiter() { return delimiter; }
    public void setDelimiter(String delimiter) { this.delimiter = delimiter; }

    // Removed single getColumn/setColumn as we now use map
    // ...

    private void loadEnv() {
        // ... (existing loadEnv logic) ...
    }
    
    // ... (existing getters/setters) ...

    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }

    public int getParallelWorkers() { return parallelWorkers; }
    public void setParallelWorkers(int parallelWorkers) { this.parallelWorkers = parallelWorkers; }

    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }



    public String getApiHost() { return apiHost; }
    public void setApiHost(String apiHost) { this.apiHost = apiHost; }

    public int getApiPort() { return apiPort; }
    public void setApiPort(int apiPort) { this.apiPort = apiPort; }

    public boolean isApiTls() { return apiTls; }
    public void setApiTls(boolean apiTls) { this.apiTls = apiTls; }

    public String getRegistrationToken() { return registrationToken; }
    public void setRegistrationToken(String registrationToken) { this.registrationToken = registrationToken; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public boolean isSkipHeader() { return skipHeader; }
    public void setSkipHeader(boolean skipHeader) { this.skipHeader = skipHeader; }

    public String getInputFilePath() { return inputFilePath; }
    public void setInputFilePath(String inputFilePath) { this.inputFilePath = inputFilePath; }

    public String getOutputFilePath() { return outputFilePath; }
    public void setOutputFilePath(String outputFilePath) { this.outputFilePath = outputFilePath; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
}

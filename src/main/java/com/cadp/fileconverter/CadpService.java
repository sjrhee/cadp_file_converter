package com.cadp.fileconverter;

public class CadpService {
    private Config config;

    public CadpService(Config config) {
        this.config = config;
        
        // Initialize CadpClient with values from Config
        try {
            CadpClient.getInstance().init(
                config.getApiHost(),
                String.valueOf(config.getApiPort()),
                config.getRegistrationToken(),
                config.getDefaultPolicy(),
                config.getUserName()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize CADP Client: " + e.getMessage(), e);
        }
    }


    public String protect(String data, String policy) throws Exception {
        if (data == null) return null;
        // Use CadpClient to encrypt
        // Note: CadpClient.enc(policy, data) takes params in that order
        return CadpClient.getInstance().enc(policy, data);
    }

    public String reveal(String data, String policy) throws Exception {
        if (data == null) return null;
        // Use CadpClient to decrypt
        return CadpClient.getInstance().dec(policy, data);
    }
}

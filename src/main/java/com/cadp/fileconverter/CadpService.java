package com.cadp.fileconverter;

public class CadpService {
    private Config config;

    public CadpService(Config config) {
        this.config = config;
        
        // Initialize CadpClient with values from Config
        CadpClient.getInstance().init(
            config.getApiHost(),
            String.valueOf(config.getApiPort()),
            config.getRegistrationToken(),
            config.getDefaultPolicy(),
            config.getUserName()
        );
    }


    public String protect(String data, String policy) {
        if (data == null) return null;
        // Use CadpClient to encrypt
        // Note: CadpClient.enc(policy, data) takes params in that order
        String result = CadpClient.getInstance().enc(policy, data);
        if (result == null) {
            throw new RuntimeException("Encryption failed for policy: " + policy);
        }
        return result;
    }

    public String reveal(String data, String policy) {
        if (data == null) return null;
        // Use CadpClient to decrypt
        String result = CadpClient.getInstance().dec(policy, data);
        if (result == null) {
            // Depending on requirements, we might return original data or throw exception.
            // For now, consistent with typical decryption failures, allow throwing or return null.
            // But let's verify if CadpClient returns null on failure. Yes it does.
            // Returning null might break CSV writer if not handled.
            throw new RuntimeException("Decryption failed for policy: " + policy);
        }
        return result;
    }
}

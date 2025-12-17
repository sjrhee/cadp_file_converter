package com.cadp.fileconverter;

import com.centralmanagement.CentralManagementProvider;
import com.centralmanagement.CipherTextData;
import com.centralmanagement.ClientObserver;
import com.centralmanagement.RegisterClientParameters;
import com.centralmanagement.policy.CryptoManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.nio.charset.StandardCharsets;

public class CadpClient {

    private String protectionPolicyName;
    private String userName;

    private static volatile CadpClient instance;

    private CadpClient() {
        // No automatic initialization
    }

    public static CadpClient getInstance() {
        if (instance == null) {
            synchronized (CadpClient.class) {
                if (instance == null) {
                    instance = new CadpClient();
                }
            }
        }
        return instance;
    }

    /**
     * Initialize the client with explicit configuration parameters.
     * Use this method to inject configuration from .env or other sources.
     */
    public synchronized void init(String keyManagerHost, String keyManagerPort, String registrationToken, String policyName, String userName) {
        // Avoid re-initialization if parameters haven't changed, or allow re-init to update?
        // For simplicity, we just update and re-register.
        this.protectionPolicyName = policyName;
        this.userName = userName;

        registerClient(keyManagerHost, keyManagerPort, registrationToken);
    }

    private void registerClient(String keyManagerHost, String keyManagerPort, String registrationToken) {
        try {
            if (keyManagerHost == null || registrationToken == null) {
                System.err.println("Cannot register client: missing host or token");
                return;
            }

            RegisterClientParameters.Builder builder = new RegisterClientParameters.Builder(keyManagerHost,
                    registrationToken.toCharArray());

            if (keyManagerPort != null && !keyManagerPort.isEmpty()) {
                try {
                    int port = Integer.parseInt(keyManagerPort);
                    builder.setWebPort(port);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid port: " + keyManagerPort);
                }
            }

            RegisterClientParameters registerClientParams = builder.build();

            CentralManagementProvider centralManagementProvider = new CentralManagementProvider(registerClientParams);
            centralManagementProvider.addProvider();

            // Status update implementation
            centralManagementProvider.subscribeToStatusUpdate(new ClientObserver<Object, Object>() {
                @Override
                public void notifyStatusUpdate(Object status, Object message) {
                    System.out.println("[CADP Status] Status: " + status + ", Message: " + message);
                }
            });
        } catch (Exception e) {
            System.err.println("Error registering client: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String enc(String plainText) {
        return enc(this.protectionPolicyName, plainText);
    }

    public String enc(String policyName, String plainText) {
        if (plainText == null)
            return null;
        try {
            CipherTextData cipherTextData = CryptoManager.protect(plainText.getBytes(StandardCharsets.UTF_8),
                    policyName);
            return new String(cipherTextData.getCipherText(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String dec(String cipherText) {
        return dec(this.protectionPolicyName, cipherText);
    }

    public String dec(String policyName, String cipherText) {
        if (cipherText == null)
            return null;
        try {
            CipherTextData cipherTextData = new CipherTextData();
            cipherTextData.setCipherText(cipherText.getBytes(StandardCharsets.UTF_8));

            byte[] revealedData = CryptoManager.reveal(cipherTextData, policyName, this.userName);
            return new String(revealedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

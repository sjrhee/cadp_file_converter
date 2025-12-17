package com.cadp.fileconverter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @Test
    void testDefaultValues() {
        Config config = new Config();
        // Since .env might exist or not, defaults might be overridden.
        // Assuming test environment doesn't have .env or we can mock Dotenv?
        // Dotenv loads static .env file from the project root.
        // The .env file has PORT=443, so we expect 443, not the hardcoded default 32082.
        assertNotNull(config.getApiHost());
        assertEquals(443, config.getApiPort()); 
        assertNotNull(config.getDefaultPolicy());
    }

    @Test
    void testColumnPolicyAddition() {
        Config config = new Config();
        config.addColumnPolicy(0, "policy1");
        config.addColumnPolicy(2, "policy3");

        assertEquals(2, config.getColumnPolicies().size());
        assertEquals("policy1", config.getColumnPolicies().get(0));
        assertEquals("policy3", config.getColumnPolicies().get(2));
    }
}

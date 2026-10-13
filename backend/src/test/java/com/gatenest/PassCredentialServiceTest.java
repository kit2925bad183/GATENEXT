package com.gatenest;

import com.gatenest.service.PassCredentialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PassCredentialServiceTest {

    private PassCredentialService service;

    @BeforeEach
    void setUp() {
        service = new PassCredentialService();
    }

    @Test
    @DisplayName("Should generate non-empty unique pass tokens")
    void testGeneratePassToken() {
        String token1 = service.generatePassToken();
        String token2 = service.generatePassToken();

        assertNotNull(token1);
        assertNotNull(token2);
        assertFalse(token1.isBlank());
        assertFalse(token2.isBlank());
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Should correctly hash credentials consistently using SHA-256")
    void testHashCredential() {
        String token = "TEST_PASS_TOKEN_123";
        String hash1 = service.hashCredential(token);
        String hash2 = service.hashCredential(token);

        assertNotNull(hash1);
        assertEquals(hash1, hash2);
        assertEquals(64, hash1.length()); // SHA-256 hex is 64 characters
    }

    @Test
    @DisplayName("Should generate valid Base64 Data URL for QR Code")
    void testGenerateQrCodeDataUrl() {
        String payload = "GATENEST:GN-20261007-1001:randomSecret123";
        String dataUrl = service.generateQrCodeDataUrl(payload);

        assertNotNull(dataUrl);
        assertTrue(dataUrl.startsWith("data:image/png;base64,"));
    }
}

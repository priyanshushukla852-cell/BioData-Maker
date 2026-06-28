package com.biodataai.backend.service.ad;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Verifies AdMob rewarded-ad Server-Side Verification (SSV) callbacks.
 *
 * <p>Google signs each callback with an ECDSA key and publishes the matching public keys at
 * {@link #VERIFIER_KEYS_URL}. The signed content is the callback's query string up to (but not
 * including) {@code &signature=...}; the {@code signature} and {@code key_id} params are always
 * last. We fetch and cache the public keys, then verify {@code SHA256withECDSA} over that content.
 *
 * <p>Without this check anyone could call our SSV endpoint and mint free AI generations, so a
 * grant is recorded only when {@link #verify} returns true.
 *
 * @see <a href="https://developers.google.com/admob/android/rewarded-video-ssv">AdMob SSV docs</a>
 */
@Component
public class AdMobSsvVerifier {

    private static final Logger log = LoggerFactory.getLogger(AdMobSsvVerifier.class);
    private static final String VERIFIER_KEYS_URL = "https://www.gstatic.com/admob/reward/verifier-keys.json";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;
    private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();

    public AdMobSsvVerifier(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * @param content the callback query string up to (not including) {@code &signature=}
     * @param signatureBase64Url the {@code signature} param value (base64url, ECDSA DER)
     * @param keyId the {@code key_id} param value
     * @return true only if the signature is valid for Google's public key {@code keyId}
     */
    public boolean verify(String content, String signatureBase64Url, String keyId) {
        if (content == null || signatureBase64Url == null || keyId == null) {
            return false;
        }
        PublicKey publicKey = resolveKey(keyId);
        if (publicKey == null) {
            log.warn("AdMob SSV: no verifier key for key_id={}", keyId);
            return false;
        }
        try {
            byte[] signatureBytes = Base64.getUrlDecoder().decode(signatureBase64Url);
            Signature ecdsa = Signature.getInstance("SHA256withECDSA");
            ecdsa.initVerify(publicKey);
            ecdsa.update(content.getBytes(StandardCharsets.UTF_8));
            return ecdsa.verify(signatureBytes);
        } catch (Exception e) {
            log.warn("AdMob SSV signature verification error: {}", e.getClass().getSimpleName());
            return false;
        }
    }

    private PublicKey resolveKey(String keyId) {
        PublicKey cached = keyCache.get(keyId);
        if (cached != null) {
            return cached;
        }
        // Unknown key id: keys rotate, so (re)load the published set, then look again.
        refreshKeys();
        return keyCache.get(keyId);
    }

    private synchronized void refreshKeys() {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(VERIFIER_KEYS_URL)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("AdMob SSV: verifier-keys fetch returned HTTP {}", response.statusCode());
                return;
            }
            JsonNode keys = objectMapper.readTree(response.body()).path("keys");
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            for (JsonNode key : keys) {
                String keyId = key.path("keyId").asText();
                String base64 = key.path("base64").asText(null);
                if (base64 == null) {
                    continue;
                }
                byte[] der = Base64.getDecoder().decode(base64);
                PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(der));
                keyCache.put(keyId, publicKey);
            }
        } catch (Exception e) {
            log.warn("AdMob SSV: failed to refresh verifier keys: {}", e.getClass().getSimpleName());
        }
    }
}

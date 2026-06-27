package com.biodataai.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Initializes the Firebase Admin SDK if a service account credential is configured.
 *
 * <p>The credential can be supplied two ways, in this precedence order:
 * <ol>
 *   <li>{@code firebase.service-account-json} (env {@code FIREBASE_SERVICE_ACCOUNT_JSON}) — the
 *       raw service-account JSON as a string. This is the production path on Railway, where the
 *       JSON is stored as an encrypted secret variable rather than a file checked into the repo
 *       (keeping keys server-side only, per CLAUDE.md).</li>
 *   <li>{@code firebase.service-account-path} (env {@code FIREBASE_SERVICE_ACCOUNT_PATH}) — a path
 *       to a JSON file on disk. Convenient for local development.</li>
 * </ol>
 *
 * <p>Until a Firebase project + service account exist, this intentionally does NOT throw at
 * startup — {@link com.biodataai.backend.service.AuthService} checks {@link FirebaseAvailability}
 * and degrades to 503 rather than the whole backend failing to boot, per the "maximum
 * availability" priority.
 *
 * <p>The credential contains a private key, so its contents are never logged.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    public FirebaseAvailability firebaseAvailability(
            @Value("${firebase.service-account-json:}") String serviceAccountJson,
            @Value("${firebase.service-account-path:}") String serviceAccountPath) {

        if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
            return initialize(
                    new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8)),
                    "FIREBASE_SERVICE_ACCOUNT_JSON env var");
        }
        if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
            try (FileInputStream serviceAccount = new FileInputStream(serviceAccountPath)) {
                return initialize(serviceAccount, "file " + serviceAccountPath);
            } catch (IOException e) {
                log.error("Failed to read Firebase service account from {}: {}", serviceAccountPath, e.getMessage());
                return new FirebaseAvailability(false);
            }
        }

        log.warn("No Firebase credential configured (set FIREBASE_SERVICE_ACCOUNT_JSON or "
                + "FIREBASE_SERVICE_ACCOUNT_PATH); Firebase ID token verification is disabled.");
        return new FirebaseAvailability(false);
    }

    private FirebaseAvailability initialize(InputStream credentialStream, String source) {
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialStream))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            log.info("Firebase Admin SDK initialized from {}; ID token verification enabled.", source);
            return new FirebaseAvailability(true);
        } catch (IOException e) {
            // Never log the credential contents (private key) — only the source and message.
            log.error("Failed to initialize Firebase Admin SDK from {}: {}", source, e.getMessage());
            return new FirebaseAvailability(false);
        }
    }
}

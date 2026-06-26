package com.biodataai.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.FileInputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Initializes the Firebase Admin SDK if a service account credential is configured.
 *
 * <p>Until a Firebase project + service account exist (not yet created for this app), this
 * intentionally does NOT throw at startup — {@link com.biodataai.backend.service.AuthService}
 * checks {@link FirebaseAvailability} and degrades to 503 rather than the whole backend
 * failing to boot, per the "maximum availability" priority.
 */
@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Bean
    public FirebaseAvailability firebaseAvailability(
            @Value("${firebase.service-account-path:}") String serviceAccountPath) {
        if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
            log.warn("firebase.service-account-path not set; Firebase ID token verification is disabled.");
            return new FirebaseAvailability(false);
        }
        try (FileInputStream serviceAccount = new FileInputStream(serviceAccountPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            return new FirebaseAvailability(true);
        } catch (IOException e) {
            log.error("Failed to initialize Firebase Admin SDK from {}: {}", serviceAccountPath, e.getMessage());
            return new FirebaseAvailability(false);
        }
    }
}

package com.biodataai.backend.service;

import com.biodataai.backend.config.FirebaseAvailability;
import com.biodataai.backend.entity.AuthProvider;
import com.biodataai.backend.entity.User;
import com.biodataai.backend.exception.AuthUnavailableException;
import com.biodataai.backend.exception.InvalidTokenException;
import com.biodataai.backend.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final FirebaseAvailability firebaseAvailability;
    private final UserRepository userRepository;

    public AuthService(FirebaseAvailability firebaseAvailability, UserRepository userRepository) {
        this.firebaseAvailability = firebaseAvailability;
        this.userRepository = userRepository;
    }

    public FirebaseToken verifyIdToken(String idToken) {
        if (!firebaseAvailability.isAvailable()) {
            throw new AuthUnavailableException("Auth service is not configured yet.");
        }
        try {
            return FirebaseAuth.getInstance().verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            throw new InvalidTokenException("Invalid or expired ID token.");
        }
    }

    @Transactional
    public UpsertResult upsertUser(FirebaseToken token) {
        var existing = userRepository.findByFirebaseUid(token.getUid());
        if (existing.isPresent()) {
            return new UpsertResult(existing.get(), false);
        }

        User user = new User();
        user.setFirebaseUid(token.getUid());
        user.setAuthProvider(resolveProvider(token));
        user.setDisplayName(token.getName() != null ? token.getName() : "User");
        user.setEmail(token.getEmail());
        User saved = userRepository.save(user);
        return new UpsertResult(saved, true);
    }

    private AuthProvider resolveProvider(FirebaseToken token) {
        Object firebaseClaim = token.getClaims().get("firebase");
        if (firebaseClaim instanceof java.util.Map<?, ?> map) {
            Object signInProvider = map.get("sign_in_provider");
            if (signInProvider instanceof String s && s.contains("google")) {
                return AuthProvider.GOOGLE;
            }
        }
        return AuthProvider.OTP;
    }

    public record UpsertResult(User user, boolean isNewUser) {
    }
}

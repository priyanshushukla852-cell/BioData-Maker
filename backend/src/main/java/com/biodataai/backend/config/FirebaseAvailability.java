package com.biodataai.backend.config;

public class FirebaseAvailability {

    private final boolean available;

    public FirebaseAvailability(boolean available) {
        this.available = available;
    }

    public boolean isAvailable() {
        return available;
    }
}

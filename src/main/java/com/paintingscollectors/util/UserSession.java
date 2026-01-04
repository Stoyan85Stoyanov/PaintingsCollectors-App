package com.paintingscollectors.util;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.UUID;

@SessionScope
@Component
@Getter
public class UserSession {

    private UUID id;
    private String username;

    public void login(UUID id, String username) {
        this.id = id;
        this.username = username;
    }

    public boolean isLoggedIn() {
        return id != null;
    }

    public UUID id() {
        return id;
    }

    public String username() {
        return username;
    }

    public void logout() {
        id = null;
        username = null;
    }
}

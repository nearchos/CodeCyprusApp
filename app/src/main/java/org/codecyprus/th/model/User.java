package org.codecyprus.th.model;

import java.io.Serializable;

public class User implements Serializable {

    private final String uuid;
    private final String email;
    private final String nickname;
    private final boolean isAdmin;

    public User(String email, String nickname, boolean isAdmin) {
        this(null, email, nickname, isAdmin);
    }

    public User(String uuid, String email, String nickname, boolean isAdmin) {
        this.uuid = uuid;
        this.email = email;
        this.nickname = nickname;
        this.isAdmin = isAdmin;
    }

    public String getUuid() {
        return uuid;
    }

    public String getEmail() {
        return email;
    }

    public String getNickname() {
        return nickname;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}
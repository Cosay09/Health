package com.hms.model;

public class User {

    private int userId;
    private String username;
    private String name;
    private String roleName;
    private final String passwordHash;

    public User(int userId, String username, String name, String roleName, String passwordHash) {
        this.userId   = userId;
        this.username = username;
        this.name     = name;
        this.roleName = roleName;
        this.passwordHash = passwordHash;
    }

    public int    getUserId()   { return userId; }
    public String getUsername() { return username; }
    public String getName()     { return name; }
    public String getRoleName() { return roleName; }
    public String getPasswordHash() { return passwordHash; };
}
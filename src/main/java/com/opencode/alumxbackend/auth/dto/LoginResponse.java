package com.opencode.alumxbackend.auth.dto;

public class LoginResponse {
    private String accessToken;
    private Long tokenExpiryTime;
    private UserBasicInfo user;

    public LoginResponse() {}

    public LoginResponse(String accessToken, Long tokenExpiryTime, UserBasicInfo user) {
        this.accessToken = accessToken;
        this.tokenExpiryTime = tokenExpiryTime;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Long getTokenExpiryTime() {
        return tokenExpiryTime;
    }

    public void setTokenExpiryTime(Long tokenExpiryTime) {
        this.tokenExpiryTime = tokenExpiryTime;
    }

    public UserBasicInfo getUser() {
        return user;
    }

    public void setUser(UserBasicInfo user) {
        this.user = user;
    }

    public static class UserBasicInfo {
        private Long id;
        private String username;
        private String email;
        private String name;
        private String role;

        public UserBasicInfo() {}

        public UserBasicInfo(Long id, String username, String email, String name, String role) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.name = name;
            this.role = role;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}

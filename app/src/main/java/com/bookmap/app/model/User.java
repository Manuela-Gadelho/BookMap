package com.bookmap.app.model;

/**
 * User model representing a BookMap user.
 * Roles: GUEST, READER, ORGANIZER
 */
public class User {
    private long id;
    private String name;
    private String email;
    private String passwordHash;
    private String bio;
    private String photoPath;
    private String favoriteGenres;
    private String role;
    private double latitude;
    private double longitude;
    private String language;
    private String createdAt;

    public User() {}

    public User(String name, String email, String passwordHash, String favoriteGenres, String role) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.favoriteGenres = favoriteGenres;
        this.role = role;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public String getFavoriteGenres() { return favoriteGenres; }
    public void setFavoriteGenres(String favoriteGenres) { this.favoriteGenres = favoriteGenres; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isGuest() { return "GUEST".equals(role); }
    public boolean isReader() { return "READER".equals(role); }
    public boolean isOrganizer() { return "ORGANIZER".equals(role); }
}

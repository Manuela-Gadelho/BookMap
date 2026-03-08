package com.bookmap.app.model;

/**
 * ClubMember model representing a membership in a reading club.
 * Role: MEMBER, MODERATOR, ORGANIZER
 * Status: PENDING, APPROVED
 */
public class ClubMember {
    private long id;
    private long clubId;
    private long userId;
    private String role;
    private String status;
    private String joinedAt;

    // Joined fields
    private String userName;
    private String userEmail;

    public ClubMember() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getClubId() { return clubId; }
    public void setClubId(long clubId) { this.clubId = clubId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getJoinedAt() { return joinedAt; }
    public void setJoinedAt(String joinedAt) { this.joinedAt = joinedAt; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}

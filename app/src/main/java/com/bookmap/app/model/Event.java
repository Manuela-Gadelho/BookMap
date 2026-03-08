package com.bookmap.app.model;

/**
 * Event model representing a scheduled meeting within a reading club.
 */
public class Event {
    private long id;
    private long clubId;
    private String title;
    private String description;
    private String dateTime;
    private String location;
    private long bookId;
    private long createdBy;
    private String createdAt;

    public Event() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getClubId() { return clubId; }
    public void setClubId(long clubId) { this.clubId = clubId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public long getBookId() { return bookId; }
    public void setBookId(long bookId) { this.bookId = bookId; }

    public long getCreatedBy() { return createdBy; }
    public void setCreatedBy(long createdBy) { this.createdBy = createdBy; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

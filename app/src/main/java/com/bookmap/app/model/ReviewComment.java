package com.bookmap.app.model;

public class ReviewComment {
    private long id;
    private long reviewId;
    private long userId;
    private String userName; // Join from users table
    private String text;
    private String timestamp;

    public ReviewComment(long id, long reviewId, long userId, String userName, String text, String timestamp) {
        this.id = id;
        this.reviewId = reviewId;
        this.userId = userId;
        this.userName = userName;
        this.text = text;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public long getReviewId() { return reviewId; }
    public long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getText() { return text; }
    public String getTimestamp() { return timestamp; }
}

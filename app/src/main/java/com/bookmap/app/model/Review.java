package com.bookmap.app.model;

/**
 * Review model. Constraint 1: rating is mandatory with review.
 * WriteReview <<include>> RateBook
 */
public class Review {
    private long id;
    private long userId;
    private long bookId;
    private String text;
    private int rating;
    private String userName;
    private String createdAt;

    public Review() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public long getBookId() { return bookId; }
    public void setBookId(long bookId) { this.bookId = bookId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getStars() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < rating ? "\u2605" : "\u2606");
        }
        return sb.toString();
    }
}

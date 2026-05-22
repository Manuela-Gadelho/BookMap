package com.bookmap.app.model;
public class UserBook {
    private long id;
    private long userId;
    private long bookId;
    private String status;
    private int progress;
    private String createdAt;
    private String bookTitle;
    private String bookAuthor;
    private String bookGenre;
    private String bookCoverPath;
    private String bookSynopsis;
    private String bookIsbn;
    public UserBook() {}
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public long getBookId() { return bookId; }
    public void setBookId(long bookId) { this.bookId = bookId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getBookAuthor() { return bookAuthor; }
    public void setBookAuthor(String bookAuthor) { this.bookAuthor = bookAuthor; }
    public String getBookGenre() { return bookGenre; }
    public void setBookGenre(String bookGenre) { this.bookGenre = bookGenre; }
    public String getBookCoverPath() { return bookCoverPath; }
    public void setBookCoverPath(String bookCoverPath) { this.bookCoverPath = bookCoverPath; }
    public String getBookSynopsis() { return bookSynopsis; }
    public void setBookSynopsis(String bookSynopsis) { this.bookSynopsis = bookSynopsis; }
    public String getBookIsbn() { return bookIsbn; }
    public void setBookIsbn(String bookIsbn) { this.bookIsbn = bookIsbn; }
}

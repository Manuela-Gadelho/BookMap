package com.bookmap.app.model;

/**
 * Book model representing a book in the BookMap catalog.
 */
public class Book {
    private long id;
    private String title;
    private String author;
    private String synopsis;
    private String coverPath;
    private String genre;
    private String isbn;
    private String createdAt;

    public Book() {}

    public Book(String title, String author, String synopsis, String genre) {
        this.title = title;
        this.author = author;
        this.synopsis = synopsis;
        this.genre = genre;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getSynopsis() { return synopsis; }
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }

    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}

package com.bookmap.app.h2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * H2 in-memory database helper for testing.
 * Mirrors the SQLite schema from DatabaseHelper to validate SQL operations
 * in a pure JVM environment without Android dependencies.
 */
public class H2DatabaseHelper {

    private Connection connection;

    public H2DatabaseHelper() throws SQLException {
        connection = DriverManager.getConnection(
                "jdbc:h2:mem:bookmap_test;DB_CLOSE_DELAY=-1;MODE=MySQL", "sa", "");
        createTables();
    }

    public Connection getConnection() {
        return connection;
    }

    private void createTables() throws SQLException {
        Statement stmt = connection.createStatement();

        stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "email VARCHAR(255) NOT NULL UNIQUE, " +
                "password_hash VARCHAR(255) NOT NULL, " +
                "bio VARCHAR(1000) DEFAULT '', " +
                "photo_path VARCHAR(500) DEFAULT '', " +
                "favorite_genres VARCHAR(500) DEFAULT '', " +
                "role VARCHAR(50) DEFAULT 'READER', " +
                "latitude DOUBLE DEFAULT 0.0, " +
                "longitude DOUBLE DEFAULT 0.0, " +
                "language VARCHAR(50) DEFAULT 'Portugues', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        stmt.execute("CREATE TABLE IF NOT EXISTS books (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "title VARCHAR(255) NOT NULL, " +
                "author VARCHAR(255) NOT NULL, " +
                "synopsis VARCHAR(2000) DEFAULT '', " +
                "cover_path VARCHAR(500) DEFAULT '', " +
                "genre VARCHAR(100) DEFAULT '', " +
                "isbn VARCHAR(20) DEFAULT '', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        stmt.execute("CREATE TABLE IF NOT EXISTS user_books (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id BIGINT NOT NULL, " +
                "book_id BIGINT NOT NULL, " +
                "status VARCHAR(50) NOT NULL DEFAULT 'QUERO_LER', " +
                "progress INT DEFAULT 0, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (book_id) REFERENCES books(id), " +
                "UNIQUE(user_id, book_id))");

        stmt.execute("CREATE TABLE IF NOT EXISTS reviews (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id BIGINT NOT NULL, " +
                "book_id BIGINT NOT NULL, " +
                "text VARCHAR(2000) NOT NULL, " +
                "rating INT NOT NULL CHECK(rating >= 1 AND rating <= 5), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (book_id) REFERENCES books(id))");

        stmt.execute("CREATE TABLE IF NOT EXISTS clubs (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "description VARCHAR(1000) DEFAULT '', " +
                "is_public INT DEFAULT 1, " +
                "creator_id BIGINT NOT NULL, " +
                "banner_path VARCHAR(500) DEFAULT '', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (creator_id) REFERENCES users(id))");

        stmt.execute("CREATE TABLE IF NOT EXISTS club_members (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "club_id BIGINT NOT NULL, " +
                "user_id BIGINT NOT NULL, " +
                "role VARCHAR(50) DEFAULT 'MEMBER', " +
                "status VARCHAR(50) DEFAULT 'PENDING', " +
                "joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (club_id) REFERENCES clubs(id), " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "UNIQUE(club_id, user_id))");

        stmt.execute("CREATE TABLE IF NOT EXISTS events (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "club_id BIGINT NOT NULL, " +
                "title VARCHAR(255) NOT NULL, " +
                "description VARCHAR(1000) DEFAULT '', " +
                "date_time VARCHAR(50) NOT NULL, " +
                "location VARCHAR(255) DEFAULT '', " +
                "book_id BIGINT, " +
                "created_by BIGINT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (club_id) REFERENCES clubs(id), " +
                "FOREIGN KEY (book_id) REFERENCES books(id), " +
                "FOREIGN KEY (created_by) REFERENCES users(id))");

        stmt.execute("CREATE TABLE IF NOT EXISTS reports (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "reporter_id BIGINT NOT NULL, " +
                "reported_user_id BIGINT, " +
                "reported_content_id BIGINT, " +
                "content_type VARCHAR(50) DEFAULT '', " +
                "reason VARCHAR(1000) NOT NULL, " +
                "status VARCHAR(50) DEFAULT 'PENDING', " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (reporter_id) REFERENCES users(id), " +
                "FOREIGN KEY (reported_user_id) REFERENCES users(id))");

        stmt.close();
    }

    // ==================== USER OPERATIONS ====================

    public long insertUser(String name, String email, String passwordHash,
                           String bio, String favoriteGenres, String role) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO users (name, email, password_hash, bio, favorite_genres, role) " +
                        "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, name);
        ps.setString(2, email);
        ps.setString(3, passwordHash);
        ps.setString(4, bio);
        ps.setString(5, favoriteGenres);
        ps.setString(6, role);
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        return rs.next() ? rs.getLong(1) : -1;
    }

    public ResultSet getUserByEmail(String email) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM users WHERE email = ?");
        ps.setString(1, email);
        return ps.executeQuery();
    }

    public ResultSet getUserById(long id) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM users WHERE id = ?");
        ps.setLong(1, id);
        return ps.executeQuery();
    }

    public boolean updateUserPassword(long userId, String newHash) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "UPDATE users SET password_hash = ? WHERE id = ?");
        ps.setString(1, newHash);
        ps.setLong(2, userId);
        return ps.executeUpdate() > 0;
    }

    // ==================== BOOK OPERATIONS ====================

    public long insertBook(String title, String author, String synopsis,
                           String coverPath, String genre, String isbn) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO books (title, author, synopsis, cover_path, genre, isbn) " +
                        "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, title);
        ps.setString(2, author);
        ps.setString(3, synopsis);
        ps.setString(4, coverPath);
        ps.setString(5, genre);
        ps.setString(6, isbn);
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        return rs.next() ? rs.getLong(1) : -1;
    }

    public ResultSet searchBooks(String query) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM books WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? OR LOWER(genre) LIKE ?");
        String like = "%" + query.toLowerCase() + "%";
        ps.setString(1, like);
        ps.setString(2, like);
        ps.setString(3, like);
        return ps.executeQuery();
    }

    // ==================== USER_BOOKS OPERATIONS ====================

    public long insertUserBook(long userId, long bookId, String status, int progress)
            throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO user_books (user_id, book_id, status, progress) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        ps.setLong(1, userId);
        ps.setLong(2, bookId);
        ps.setString(3, status);
        ps.setInt(4, progress);
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        return rs.next() ? rs.getLong(1) : -1;
    }

    public boolean updateUserBookStatus(long userId, long bookId, String status, int progress)
            throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "UPDATE user_books SET status = ?, progress = ? WHERE user_id = ? AND book_id = ?");
        ps.setString(1, status);
        ps.setInt(2, progress);
        ps.setLong(3, userId);
        ps.setLong(4, bookId);
        return ps.executeUpdate() > 0;
    }

    // ==================== REVIEW OPERATIONS ====================

    public long insertReview(long userId, long bookId, String text, int rating)
            throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO reviews (user_id, book_id, text, rating) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        ps.setLong(1, userId);
        ps.setLong(2, bookId);
        ps.setString(3, text);
        ps.setInt(4, rating);
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        return rs.next() ? rs.getLong(1) : -1;
    }

    public double getBookAverageRating(long bookId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "SELECT AVG(CAST(rating AS DOUBLE)) as avg_rating FROM reviews WHERE book_id = ?");
        ps.setLong(1, bookId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getDouble("avg_rating");
        return 0.0;
    }

    public int getBookReviewCount(long bookId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) as cnt FROM reviews WHERE book_id = ?");
        ps.setLong(1, bookId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt("cnt");
        return 0;
    }

    // ==================== CLUB OPERATIONS ====================

    public long insertClub(String name, String description, boolean isPublic, long creatorId)
            throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO clubs (name, description, is_public, creator_id) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, name);
        ps.setString(2, description);
        ps.setInt(3, isPublic ? 1 : 0);
        ps.setLong(4, creatorId);
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        return rs.next() ? rs.getLong(1) : -1;
    }

    public long addClubMember(long clubId, long userId, String role, String status)
            throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO club_members (club_id, user_id, role, status) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        ps.setLong(1, clubId);
        ps.setLong(2, userId);
        ps.setString(3, role);
        ps.setString(4, status);
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        return rs.next() ? rs.getLong(1) : -1;
    }

    public boolean updateMemberStatus(long clubId, long userId, String status)
            throws SQLException {
        PreparedStatement ps = connection.prepareStatement(
                "UPDATE club_members SET status = ? WHERE club_id = ? AND user_id = ?");
        ps.setString(1, status);
        ps.setLong(2, clubId);
        ps.setLong(3, userId);
        return ps.executeUpdate() > 0;
    }

    // ==================== CLEANUP ====================

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public void clearAllTables() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute("DELETE FROM reports");
        stmt.execute("DELETE FROM events");
        stmt.execute("DELETE FROM club_members");
        stmt.execute("DELETE FROM clubs");
        stmt.execute("DELETE FROM reviews");
        stmt.execute("DELETE FROM user_books");
        stmt.execute("DELETE FROM books");
        stmt.execute("DELETE FROM users");
        stmt.close();
    }
}

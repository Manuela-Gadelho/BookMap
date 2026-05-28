package com.bookmap.app.h2;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;


public class H2DatabaseIntegrationTest {

    private H2DatabaseHelper db;

    @Before
    public void setUp() throws SQLException {
        db = new H2DatabaseHelper();
        db.clearAllTables();
    }

    @After
    public void tearDown() throws SQLException {
        db.close();
    }

    

    @Test
    public void testInsertAndRetrieveUser() throws SQLException {
        long userId = db.insertUser("Maria Silva", "maria@bookmap.com",
                "hash123", "Bio da Maria", "Fantasia, Romance", "READER");
        assertTrue(userId > 0);

        ResultSet rs = db.getUserById(userId);
        assertTrue(rs.next());
        assertEquals("Maria Silva", rs.getString("name"));
        assertEquals("maria@bookmap.com", rs.getString("email"));
        assertEquals("READER", rs.getString("role"));
        assertEquals("Bio da Maria", rs.getString("bio"));
    }

    @Test
    public void testGetUserByEmail() throws SQLException {
        db.insertUser("Joao", "joao@bookmap.com", "hash", "", "Terror", "READER");
        ResultSet rs = db.getUserByEmail("joao@bookmap.com");
        assertTrue(rs.next());
        assertEquals("Joao", rs.getString("name"));
    }

    @Test
    public void testUserNotFound() throws SQLException {
        ResultSet rs = db.getUserByEmail("inexistente@bookmap.com");
        assertFalse(rs.next());
    }

    @Test(expected = SQLException.class)
    public void testDuplicateEmail() throws SQLException {
        db.insertUser("User1", "dup@bookmap.com", "hash1", "", "", "READER");
        db.insertUser("User2", "dup@bookmap.com", "hash2", "", "", "READER");
    }

    @Test
    public void testUpdatePassword() throws SQLException {
        long userId = db.insertUser("User", "pwd@bookmap.com", "oldHash", "", "", "READER");
        assertTrue(db.updateUserPassword(userId, "newHash"));
        ResultSet rs = db.getUserById(userId);
        assertTrue(rs.next());
        assertEquals("newHash", rs.getString("password_hash"));
    }

    @Test
    public void testDefaultLatLong() throws SQLException {
        long userId = db.insertUser("User", "loc@bookmap.com", "hash", "", "", "READER");
        ResultSet rs = db.getUserById(userId);
        assertTrue(rs.next());
        assertEquals(0.0, rs.getDouble("latitude"), 0.001);
        assertEquals(0.0, rs.getDouble("longitude"), 0.001);
    }

    

    @Test
    public void testInsertAndSearchBooks() throws SQLException {
        db.insertBook("Engenharia de Software", "Ian Sommerville",
                "Livro de tecnologia", "", "Tecnologia", "978-123");
        db.insertBook("Dom Casmurro", "Machado de Assis",
                "Classico brasileiro", "", "Literatura Brasileira", "978-456");

        ResultSet rs = db.searchBooks("engenharia");
        assertTrue(rs.next());
        assertEquals("Engenharia de Software", rs.getString("title"));
    }

    @Test
    public void testSearchBooksByAuthor() throws SQLException {
        db.insertBook("1984", "George Orwell", "Distopia", "", "Ficcao", "978-789");
        ResultSet rs = db.searchBooks("orwell");
        assertTrue(rs.next());
        assertEquals("1984", rs.getString("title"));
    }

    @Test
    public void testSearchBooksNoResults() throws SQLException {
        db.insertBook("Livro A", "Autor A", "", "", "Fantasia", "");
        ResultSet rs = db.searchBooks("xyzinexistente");
        assertFalse(rs.next());
    }

    

    @Test
    public void testAddBookToShelf() throws SQLException {
        long userId = db.insertUser("User", "shelf@bookmap.com", "hash", "", "", "READER");
        long bookId = db.insertBook("Livro", "Autor", "", "", "Fantasia", "");
        long ubId = db.insertUserBook(userId, bookId, "QUERO_LER", 0);
        assertTrue(ubId > 0);
    }

    @Test
    public void testUpdateReadingProgress() throws SQLException {
        long userId = db.insertUser("User", "progress@bookmap.com", "hash", "", "", "READER");
        long bookId = db.insertBook("Livro", "Autor", "", "", "Fantasia", "");
        db.insertUserBook(userId, bookId, "LENDO", 30);
        assertTrue(db.updateUserBookStatus(userId, bookId, "LENDO", 75));
    }

    @Test
    public void testMarkAsRead() throws SQLException {
        long userId = db.insertUser("User", "read@bookmap.com", "hash", "", "", "READER");
        long bookId = db.insertBook("Livro", "Autor", "", "", "Fantasia", "");
        db.insertUserBook(userId, bookId, "LENDO", 50);
        assertTrue(db.updateUserBookStatus(userId, bookId, "LIDO", 100));
    }

    @Test(expected = SQLException.class)
    public void testDuplicateUserBook() throws SQLException {
        long userId = db.insertUser("User", "dup_ub@bookmap.com", "hash", "", "", "READER");
        long bookId = db.insertBook("Livro", "Autor", "", "", "Fantasia", "");
        db.insertUserBook(userId, bookId, "QUERO_LER", 0);
        db.insertUserBook(userId, bookId, "LENDO", 50); 
    }

    

    @Test
    public void testInsertReview() throws SQLException {
        long userId = db.insertUser("User", "review@bookmap.com", "hash", "", "", "READER");
        long bookId = db.insertBook("Livro", "Autor", "", "", "Fantasia", "");
        long reviewId = db.insertReview(userId, bookId, "Otimo livro!", 5);
        assertTrue(reviewId > 0);
    }

    @Test(expected = SQLException.class)
    public void testInvalidRatingTooHigh() throws SQLException {
        long userId = db.insertUser("User", "rating_hi@bookmap.com", "hash", "", "", "READER");
        long bookId = db.insertBook("Livro", "Autor", "", "", "Fantasia", "");
        db.insertReview(userId, bookId, "Texto", 6); 
    }

    @Test(expected = SQLException.class)
    public void testInvalidRatingTooLow() throws SQLException {
        long userId = db.insertUser("User", "rating_lo@bookmap.com", "hash", "", "", "READER");
        long bookId = db.insertBook("Livro", "Autor", "", "", "Fantasia", "");
        db.insertReview(userId, bookId, "Texto", 0); 
    }

    @Test
    public void testAverageRating() throws SQLException {
        long userId1 = db.insertUser("User1", "avg1@bookmap.com", "hash", "", "", "READER");
        long userId2 = db.insertUser("User2", "avg2@bookmap.com", "hash", "", "", "READER");
        long bookId = db.insertBook("Livro", "Autor", "", "", "Fantasia", "");

        db.insertReview(userId1, bookId, "Bom", 4);
        db.insertReview(userId2, bookId, "Otimo", 5);

        double avg = db.getBookAverageRating(bookId);
        assertEquals(4.5, avg, 0.01);
    }

    @Test
    public void testReviewCount() throws SQLException {
        long userId1 = db.insertUser("User1", "cnt1@bookmap.com", "hash", "", "", "READER");
        long userId2 = db.insertUser("User2", "cnt2@bookmap.com", "hash", "", "", "READER");
        long bookId = db.insertBook("Livro", "Autor", "", "", "Fantasia", "");

        db.insertReview(userId1, bookId, "Review 1", 3);
        db.insertReview(userId2, bookId, "Review 2", 4);

        assertEquals(2, db.getBookReviewCount(bookId));
    }

    @Test
    public void testNoReviewsAverageIsZero() throws SQLException {
        long bookId = db.insertBook("Livro", "Autor", "", "", "Fantasia", "");
        assertEquals(0.0, db.getBookAverageRating(bookId), 0.01);
    }

    

    @Test
    public void testCreateClub() throws SQLException {
        long userId = db.insertUser("Org", "org@bookmap.com", "hash", "", "", "ORGANIZER");
        long clubId = db.insertClub("Clube de Leitura", "Descricao do clube", true, userId);
        assertTrue(clubId > 0);
    }

    @Test
    public void testAddMemberToClub() throws SQLException {
        long orgId = db.insertUser("Org", "org2@bookmap.com", "hash", "", "", "ORGANIZER");
        long memberId = db.insertUser("Member", "mem@bookmap.com", "hash", "", "", "READER");
        long clubId = db.insertClub("Clube", "Desc", true, orgId);

        long cmId = db.addClubMember(clubId, orgId, "ORGANIZER", "APPROVED");
        assertTrue(cmId > 0);

        long cmId2 = db.addClubMember(clubId, memberId, "MEMBER", "PENDING");
        assertTrue(cmId2 > 0);
    }

    @Test
    public void testApproveMember() throws SQLException {
        long orgId = db.insertUser("Org", "org3@bookmap.com", "hash", "", "", "ORGANIZER");
        long memberId = db.insertUser("Member", "mem2@bookmap.com", "hash", "", "", "READER");
        long clubId = db.insertClub("Clube", "Desc", true, orgId);
        db.addClubMember(clubId, memberId, "MEMBER", "PENDING");

        assertTrue(db.updateMemberStatus(clubId, memberId, "APPROVED"));
    }

    @Test
    public void testRejectMember() throws SQLException {
        long orgId = db.insertUser("Org", "org4@bookmap.com", "hash", "", "", "ORGANIZER");
        long memberId = db.insertUser("Member", "mem3@bookmap.com", "hash", "", "", "READER");
        long clubId = db.insertClub("Clube", "Desc", true, orgId);
        db.addClubMember(clubId, memberId, "MEMBER", "PENDING");

        assertTrue(db.updateMemberStatus(clubId, memberId, "REJECTED"));
    }

    @Test(expected = SQLException.class)
    public void testDuplicateClubMember() throws SQLException {
        long orgId = db.insertUser("Org", "org5@bookmap.com", "hash", "", "", "ORGANIZER");
        long clubId = db.insertClub("Clube", "Desc", true, orgId);
        db.addClubMember(clubId, orgId, "ORGANIZER", "APPROVED");
        db.addClubMember(clubId, orgId, "MEMBER", "PENDING"); 
    }

    

    @Test
    public void testCompleteUserJourney() throws SQLException {
        
        long userId = db.insertUser("Ana", "ana@bookmap.com", "hash_seguro",
                "Amante de livros", "Fantasia, Romance", "READER");
        assertTrue(userId > 0);

        
        long bookId = db.insertBook("O Hobbit", "Tolkien", "Aventura na Terra Media",
                "", "Fantasia", "978-000");
        assertTrue(bookId > 0);

        
        long ubId = db.insertUserBook(userId, bookId, "LENDO", 25);
        assertTrue(ubId > 0);

        
        long reviewId = db.insertReview(userId, bookId, "Excelente!", 5);
        assertTrue(reviewId > 0);

        
        long orgId = db.insertUser("Org", "org_journey@bookmap.com", "hash", "", "", "ORGANIZER");
        long clubId = db.insertClub("Tolkien Fans", "Clube para fas", true, orgId);
        db.addClubMember(clubId, orgId, "ORGANIZER", "APPROVED");
        db.addClubMember(clubId, userId, "MEMBER", "PENDING");

        
        assertTrue(db.updateMemberStatus(clubId, userId, "APPROVED"));

        
        assertTrue(db.updateUserBookStatus(userId, bookId, "LIDO", 100));

        
        assertEquals(5.0, db.getBookAverageRating(bookId), 0.01);
        assertEquals(1, db.getBookReviewCount(bookId));
    }

    @Test
    public void testMultipleUsersMultipleBooks() throws SQLException {
        long user1 = db.insertUser("User1", "multi1@bookmap.com", "h1", "", "Fantasia", "READER");
        long user2 = db.insertUser("User2", "multi2@bookmap.com", "h2", "", "Terror", "READER");
        long user3 = db.insertUser("User3", "multi3@bookmap.com", "h3", "", "Romance", "READER");

        long book1 = db.insertBook("Livro 1", "Autor 1", "", "", "Fantasia", "");
        long book2 = db.insertBook("Livro 2", "Autor 2", "", "", "Terror", "");

        
        db.insertUserBook(user1, book1, "LENDO", 50);
        db.insertUserBook(user2, book1, "LIDO", 100);
        db.insertUserBook(user2, book2, "LENDO", 30);
        db.insertUserBook(user3, book2, "QUERO_LER", 0);

        
        db.insertReview(user1, book1, "Bom", 3);
        db.insertReview(user2, book1, "Excelente", 5);
        db.insertReview(user2, book2, "Regular", 2);

        assertEquals(4.0, db.getBookAverageRating(book1), 0.01);
        assertEquals(2, db.getBookReviewCount(book1));
        assertEquals(2.0, db.getBookAverageRating(book2), 0.01);
        assertEquals(1, db.getBookReviewCount(book2));
    }

    @Test
    public void testForeignKeyIntegrity() throws SQLException {
        long userId = db.insertUser("User", "fk@bookmap.com", "hash", "", "", "READER");
        long bookId = db.insertBook("Livro", "Autor", "", "", "Fantasia", "");

        
        assertTrue(db.insertUserBook(userId, bookId, "LENDO", 0) > 0);
        assertTrue(db.insertReview(userId, bookId, "Texto", 4) > 0);
    }
}

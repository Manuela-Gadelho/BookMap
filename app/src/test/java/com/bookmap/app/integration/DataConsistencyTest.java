package com.bookmap.app.integration;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.Book;
import com.bookmap.app.model.ClubMember;
import com.bookmap.app.model.Review;
import com.bookmap.app.model.User;
import com.bookmap.app.model.UserBook;
import com.bookmap.app.util.PasswordUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, manifest = Config.NONE)
public class DataConsistencyTest {

    private DatabaseHelper dbHelper;

    @Before
    public void setUp() {
        DatabaseHelper.resetInstance();
        Context context = ApplicationProvider.getApplicationContext();
        dbHelper = DatabaseHelper.getInstance(context);
    }

    @Test
    public void testFullUserFlow() {
        
        String email = "flow_" + System.currentTimeMillis() + "@test.com";
        String hash = PasswordUtil.hashPassword("senha123");
        long userId = dbHelper.insertUser("FlowUser", email, hash, "", "Fantasia", "READER");
        assertTrue(userId > 0);

        
        User user = dbHelper.getUserByEmail(email);
        assertNotNull(user);
        assertEquals(userId, user.getId());
        assertTrue(PasswordUtil.verifyPassword("senha123", user.getPasswordHash()));

        
        List<Book> books = dbHelper.getAllBooks();
        long bookId = books.get(0).getId();
        long userBookId = dbHelper.insertUserBook(userId, bookId, "LENDO", 25);
        assertTrue(userBookId > 0);

        
        boolean updated = dbHelper.updateUserBookStatus(userId, bookId, "LENDO", 75);
        assertTrue(updated);

        UserBook ub = dbHelper.getUserBook(userId, bookId);
        assertEquals(75, ub.getProgress());

        
        long reviewId = dbHelper.insertReview(userId, bookId, "Otimo livro!", 5);
        assertTrue(reviewId > 0);

        
        List<Review> reviews = dbHelper.getBookReviews(bookId);
        boolean found = false;
        for (Review r : reviews) {
            if (r.getId() == reviewId) {
                found = true;
                assertEquals(5, r.getRating());
                break;
            }
        }
        assertTrue("Review deve ser encontrada", found);

        
        double avg = dbHelper.getBookAverageRating(bookId);
        assertTrue("Media deve ser > 0 apos review", avg > 0);
    }

    @Test
    public void testClubMembershipFlow() {
        
        String emailOrg = "org_flow_" + System.currentTimeMillis() + "@test.com";
        long orgId = dbHelper.insertUser("OrgFlow", emailOrg,
                PasswordUtil.hashPassword("123"), "", "Fantasia", "ORGANIZER");

        
        String emailMem = "mem_flow_" + System.currentTimeMillis() + "@test.com";
        long memId = dbHelper.insertUser("MemFlow", emailMem,
                PasswordUtil.hashPassword("123"), "", "Fantasia", "READER");

        
        long clubId = dbHelper.insertClub("Flow Club", "Desc", true, orgId);
        assertTrue(clubId > 0);

        
        long result = dbHelper.addClubMember(clubId, memId, "MEMBER", "PENDING");
        assertTrue(result > 0);

        
        List<ClubMember> pending = dbHelper.getPendingMemberRequests(orgId);
        boolean foundPending = false;
        for (ClubMember m : pending) {
            if (m.getUserId() == memId) {
                foundPending = true;
                assertEquals("PENDING", m.getStatus());
            }
        }
        assertTrue("Membro deve estar pendente", foundPending);

        
        dbHelper.updateMemberStatus(clubId, memId, "APPROVED");

        
        List<ClubMember> afterApproval = dbHelper.getPendingMemberRequests(orgId);
        for (ClubMember m : afterApproval) {
            assertNotEquals("Membro aprovado nao deve estar pendente",
                    memId, m.getUserId());
        }
    }

    @Test
    public void testBookShelfIntegrity() {
        String email = "integrity_" + System.currentTimeMillis() + "@test.com";
        long userId = dbHelper.insertUser("IntegrityUser", email,
                PasswordUtil.hashPassword("123"), "", "Terror", "READER");

        List<Book> books = dbHelper.getAllBooks();
        assertTrue(books.size() >= 3);

        
        dbHelper.insertUserBook(userId, books.get(0).getId(), "LENDO", 50);
        dbHelper.insertUserBook(userId, books.get(1).getId(), "LIDO", 100);
        dbHelper.insertUserBook(userId, books.get(2).getId(), "QUERO_LER", 0);

        
        int total = dbHelper.getUserBookCount(userId);
        assertEquals(3, total);

        int lendo = dbHelper.getUserBookCountByStatus(userId, "LENDO");
        assertEquals(1, lendo);

        int lido = dbHelper.getUserBookCountByStatus(userId, "LIDO");
        assertEquals(1, lido);

        int queroLer = dbHelper.getUserBookCountByStatus(userId, "QUERO_LER");
        assertEquals(1, queroLer);
    }

    @Test
    public void testPasswordResetIntegrity() {
        String email = "reset_" + System.currentTimeMillis() + "@test.com";
        String oldHash = PasswordUtil.hashPassword("senhaAntiga");
        long userId = dbHelper.insertUser("ResetUser", email, oldHash, "", "Fantasia", "READER");

        
        User userBefore = dbHelper.getUserByEmail(email);
        assertNotNull(userBefore);
        assertTrue(PasswordUtil.verifyPassword("senhaAntiga", userBefore.getPasswordHash()));

        
        String newHash = PasswordUtil.hashPassword("senhaNova");
        assertTrue(dbHelper.updateUserPassword(userId, newHash));

        
        User userAfter = dbHelper.getUserByEmail(email);
        assertFalse(PasswordUtil.verifyPassword("senhaAntiga", userAfter.getPasswordHash()));
        assertTrue(PasswordUtil.verifyPassword("senhaNova", userAfter.getPasswordHash()));
    }

    @Test
    public void testReviewAverageConsistency() {
        long bookId = dbHelper.insertBook("Avg Test", "Author", "S", "", "Terror", "avg-test");

        String email1 = "avgc1_" + System.currentTimeMillis() + "@test.com";
        String email2 = "avgc2_" + System.currentTimeMillis() + "@test.com";
        String email3 = "avgc3_" + System.currentTimeMillis() + "@test.com";

        long u1 = dbHelper.insertUser("AvgC1", email1, PasswordUtil.hashPassword("1"), "", "F", "READER");
        long u2 = dbHelper.insertUser("AvgC2", email2, PasswordUtil.hashPassword("1"), "", "F", "READER");
        long u3 = dbHelper.insertUser("AvgC3", email3, PasswordUtil.hashPassword("1"), "", "F", "READER");

        dbHelper.insertReview(u1, bookId, "R1", 5);
        dbHelper.insertReview(u2, bookId, "R2", 3);
        dbHelper.insertReview(u3, bookId, "R3", 1);

        double avg = dbHelper.getBookAverageRating(bookId);
        assertEquals("Media de 5+3+1 deve ser 3.0", 3.0, avg, 0.01);

        int count = dbHelper.getBookReviewCount(bookId);
        assertEquals(3, count);
    }
}

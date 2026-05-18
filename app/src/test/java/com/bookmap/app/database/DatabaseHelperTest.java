package com.bookmap.app.database;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.bookmap.app.model.Book;
import com.bookmap.app.model.Club;
import com.bookmap.app.model.ClubMember;
import com.bookmap.app.model.Review;
import com.bookmap.app.model.User;
import com.bookmap.app.model.UserBook;
import com.bookmap.app.util.PasswordUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Testes de integracao para DatabaseHelper.
 * Usa Robolectric com SQLite em memoria para validar operacoes CRUD.
 * Cobre todos os 4 modulos: Auth, Library, Map, Clubs.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, manifest = Config.NONE)
public class DatabaseHelperTest {

    private DatabaseHelper dbHelper;

    @Before
    public void setUp() {
        DatabaseHelper.resetInstance();
        Context context = ApplicationProvider.getApplicationContext();
        dbHelper = DatabaseHelper.getInstance(context);
    }

    @After
    public void tearDown() {
        DatabaseHelper.resetInstance();
    }

    // ==================== USER OPERATIONS ====================

    @Test
    public void testInsertUser() {
        String hash = PasswordUtil.hashPassword("senha123");
        long userId = dbHelper.insertUser("TestUser", "test@email.com", hash, "", "Fantasia", "READER");
        assertTrue("Insert user deve retornar ID > 0", userId > 0);
    }

    @Test
    public void testGetUserByEmail() {
        String email = "unique_" + System.currentTimeMillis() + "@test.com";
        String hash = PasswordUtil.hashPassword("senha123");
        dbHelper.insertUser("UserEmail", email, hash, "", "Terror", "READER");

        User user = dbHelper.getUserByEmail(email);
        assertNotNull("Usuario deve existir", user);
        assertEquals("UserEmail", user.getName());
        assertEquals(email, user.getEmail());
    }

    @Test
    public void testGetUserByEmailNotFound() {
        User user = dbHelper.getUserByEmail("naoexiste@test.com");
        assertNull("Usuario nao encontrado deve retornar null", user);
    }

    @Test
    public void testAuthenticateUser() {
        String email = "auth_" + System.currentTimeMillis() + "@test.com";
        String password = "senhaSegura";
        String hash = PasswordUtil.hashPassword(password);
        dbHelper.insertUser("AuthUser", email, hash, "", "Romance", "READER");

        User user = dbHelper.getUserByEmail(email);
        assertNotNull("Usuario deve existir", user);
        assertTrue("Senha correta deve autenticar",
                PasswordUtil.verifyPassword(password, user.getPasswordHash()));
    }

    @Test
    public void testAuthenticateUserWrongPassword() {
        String email = "wrong_" + System.currentTimeMillis() + "@test.com";
        String hash = PasswordUtil.hashPassword("correta");
        dbHelper.insertUser("WrongPass", email, hash, "", "Tecnologia", "READER");

        User user = dbHelper.getUserByEmail(email);
        assertNotNull(user);
        assertFalse("Senha incorreta nao deve autenticar",
                PasswordUtil.verifyPassword("errada", user.getPasswordHash()));
    }

    @Test
    public void testUpdateUserPassword() {
        String email = "pwd_" + System.currentTimeMillis() + "@test.com";
        String oldHash = PasswordUtil.hashPassword("senhaAntiga");
        long userId = dbHelper.insertUser("PwdUser", email, oldHash, "", "Fantasia", "READER");

        String newHash = PasswordUtil.hashPassword("senhaNova");
        boolean updated = dbHelper.updateUserPassword(userId, newHash);
        assertTrue("Atualizacao de senha deve retornar true", updated);

        User user = dbHelper.getUserByEmail(email);
        assertNotNull(user);
        assertTrue("Nova senha deve autenticar",
                PasswordUtil.verifyPassword("senhaNova", user.getPasswordHash()));
    }

    // ==================== BOOK OPERATIONS ====================

    @Test
    public void testInsertBook() {
        long bookId = dbHelper.insertBook("Livro Teste", "Autor Teste", "Sinopse", "", "Fantasia", "123-456");
        assertTrue("Insert book deve retornar ID > 0", bookId > 0);
    }

    @Test
    public void testGetBookById() {
        long bookId = dbHelper.insertBook("Livro Get", "Autor Get", "Sinopse Get", "", "Terror", "789-012");
        Book book = dbHelper.getBookById(bookId);
        assertNotNull("Livro deve existir", book);
        assertEquals("Livro Get", book.getTitle());
        assertEquals("Autor Get", book.getAuthor());
        assertEquals("Terror", book.getGenre());
    }

    @Test
    public void testGetAllBooks() {
        List<Book> books = dbHelper.getAllBooks();
        assertNotNull("Lista de livros nao deve ser null", books);
        assertTrue("Deve haver livros semeados", books.size() >= 6);
    }

    @Test
    public void testSearchBooks() {
        List<Book> results = dbHelper.searchBooks("Engenharia");
        assertNotNull(results);
        assertTrue("Busca por 'Engenharia' deve encontrar resultados", results.size() > 0);
    }

    // ==================== USER_BOOK OPERATIONS (ESTANTE VIRTUAL)
    // ====================

    @Test
    public void testInsertUserBook() {
        String email = "shelf_" + System.currentTimeMillis() + "@test.com";
        long userId = dbHelper.insertUser("ShelfUser", email,
                PasswordUtil.hashPassword("123"), "", "Fantasia", "READER");

        List<Book> books = dbHelper.getAllBooks();
        long bookId = books.get(0).getId();

        long result = dbHelper.insertUserBook(userId, bookId, "QUERO_LER", 0);
        assertTrue("Insert user_book deve retornar ID > 0", result > 0);
    }

    @Test
    public void testGetUserBook() {
        String email = "getub_" + System.currentTimeMillis() + "@test.com";
        long userId = dbHelper.insertUser("GetUBUser", email,
                PasswordUtil.hashPassword("123"), "", "Fantasia", "READER");

        List<Book> books = dbHelper.getAllBooks();
        long bookId = books.get(0).getId();

        dbHelper.insertUserBook(userId, bookId, "LENDO", 30);

        UserBook ub = dbHelper.getUserBook(userId, bookId);
        assertNotNull("UserBook deve existir", ub);
        assertEquals("LENDO", ub.getStatus());
        assertEquals(30, ub.getProgress());
    }

    @Test
    public void testUpdateUserBookStatus() {
        String email = "update_" + System.currentTimeMillis() + "@test.com";
        long userId = dbHelper.insertUser("UpdateUser", email,
                PasswordUtil.hashPassword("123"), "", "Fantasia", "READER");

        List<Book> books = dbHelper.getAllBooks();
        long bookId = books.get(0).getId();

        dbHelper.insertUserBook(userId, bookId, "QUERO_LER", 0);
        boolean updated = dbHelper.updateUserBookStatus(userId, bookId, "LIDO", 100);
        assertTrue("Update status deve retornar true", updated);

        UserBook ub = dbHelper.getUserBook(userId, bookId);
        assertEquals("LIDO", ub.getStatus());
        assertEquals(100, ub.getProgress());
    }

    @Test
    public void testDeleteUserBook() {
        String email = "del_" + System.currentTimeMillis() + "@test.com";
        long userId = dbHelper.insertUser("DelUser", email,
                PasswordUtil.hashPassword("123"), "", "Fantasia", "READER");

        List<Book> books = dbHelper.getAllBooks();
        long bookId = books.get(0).getId();

        dbHelper.insertUserBook(userId, bookId, "QUERO_LER", 0);
        boolean deleted = dbHelper.deleteUserBook(userId, bookId);
        assertTrue("Delete deve retornar true", deleted);
    }

    @Test
    public void testGetUserBookCount() {
        String email = "count_" + System.currentTimeMillis() + "@test.com";
        long userId = dbHelper.insertUser("CountUser", email,
                PasswordUtil.hashPassword("123"), "", "Fantasia", "READER");

        List<Book> books = dbHelper.getAllBooks();
        dbHelper.insertUserBook(userId, books.get(0).getId(), "LENDO", 50);
        dbHelper.insertUserBook(userId, books.get(1).getId(), "LIDO", 100);

        int count = dbHelper.getUserBookCount(userId);
        assertEquals(2, count);
    }

    @Test
    public void testGetUserBookCountByStatus() {
        String email = "status_" + System.currentTimeMillis() + "@test.com";
        long userId = dbHelper.insertUser("StatusUser", email,
                PasswordUtil.hashPassword("123"), "", "Fantasia", "READER");

        List<Book> books = dbHelper.getAllBooks();
        dbHelper.insertUserBook(userId, books.get(0).getId(), "LENDO", 50);
        dbHelper.insertUserBook(userId, books.get(1).getId(), "LIDO", 100);
        dbHelper.insertUserBook(userId, books.get(2).getId(), "LENDO", 25);

        int lendo = dbHelper.getUserBookCountByStatus(userId, "LENDO");
        assertEquals(2, lendo);

        int lido = dbHelper.getUserBookCountByStatus(userId, "LIDO");
        assertEquals(1, lido);
    }

    // ==================== REVIEW OPERATIONS ====================

    @Test
    public void testInsertReview() {
        String email = "rev_" + System.currentTimeMillis() + "@test.com";
        long userId = dbHelper.insertUser("ReviewUser", email,
                PasswordUtil.hashPassword("123"), "", "Fantasia", "READER");

        List<Book> books = dbHelper.getAllBooks();
        long bookId = books.get(0).getId();

        long reviewId = dbHelper.insertReview(userId, bookId, "Otimo livro!", 5);
        assertTrue("Insert review deve retornar ID > 0", reviewId > 0);
    }

    @Test
    public void testGetBookReviews() {
        String email = "getrev_" + System.currentTimeMillis() + "@test.com";
        long userId = dbHelper.insertUser("GetRevUser", email,
                PasswordUtil.hashPassword("123"), "", "Fantasia", "READER");

        List<Book> books = dbHelper.getAllBooks();
        long bookId = books.get(0).getId();

        dbHelper.insertReview(userId, bookId, "Muito bom!", 4);
        List<Review> reviews = dbHelper.getBookReviews(bookId);
        assertNotNull(reviews);
        assertTrue("Deve haver pelo menos 1 review", reviews.size() >= 1);
    }

    @Test
    public void testGetBookAverageRating() {
        String email1 = "avg1_" + System.currentTimeMillis() + "@test.com";
        String email2 = "avg2_" + System.currentTimeMillis() + "@test.com";
        long userId1 = dbHelper.insertUser("Avg1", email1, PasswordUtil.hashPassword("123"), "", "Fantasia", "READER");
        long userId2 = dbHelper.insertUser("Avg2", email2, PasswordUtil.hashPassword("123"), "", "Fantasia", "READER");

        long bookId = dbHelper.insertBook("Avg Book", "Avg Author", "Sinopse", "", "Fantasia", "avg-isbn");

        dbHelper.insertReview(userId1, bookId, "Bom", 4);
        dbHelper.insertReview(userId2, bookId, "Legal", 2);

        double avg = dbHelper.getBookAverageRating(bookId);
        assertEquals("Media deve ser 3.0", 3.0, avg, 0.01);
    }

    @Test
    public void testGetBookReviewCount() {
        long bookId = dbHelper.insertBook("Count Book", "Count Author", "S", "", "Terror", "cnt-isbn");

        int countBefore = dbHelper.getBookReviewCount(bookId);
        assertEquals(0, countBefore);

        String email = "cntrev_" + System.currentTimeMillis() + "@test.com";
        long userId = dbHelper.insertUser("CntUser", email, PasswordUtil.hashPassword("123"), "", "Terror", "READER");
        dbHelper.insertReview(userId, bookId, "Review 1", 3);

        int countAfter = dbHelper.getBookReviewCount(bookId);
        assertEquals(1, countAfter);
    }

    // ==================== CLUB OPERATIONS ====================

    @Test
    public void testInsertClub() {
        String email = "club_" + System.currentTimeMillis() + "@test.com";
        long creatorId = dbHelper.insertUser("ClubCreator", email,
                PasswordUtil.hashPassword("123"), "", "Fantasia", "ORGANIZER");

        long clubId = dbHelper.insertClub("Clube Teste", "Descricao", true, creatorId);
        assertTrue("Insert club deve retornar ID > 0", clubId > 0);
    }

    @Test
    public void testGetAllClubs() {
        List<Club> clubs = dbHelper.getAllClubs();
        assertNotNull("Lista de clubes nao deve ser null", clubs);
    }

    @Test
    public void testAddMemberToClub() {
        String emailCreator = "creator_" + System.currentTimeMillis() + "@test.com";
        String emailMember = "member_" + System.currentTimeMillis() + "@test.com";

        long creatorId = dbHelper.insertUser("Creator", emailCreator,
                PasswordUtil.hashPassword("123"), "", "Fantasia", "ORGANIZER");
        long memberId = dbHelper.insertUser("Member", emailMember,
                PasswordUtil.hashPassword("123"), "", "Fantasia", "READER");

        long clubId = dbHelper.insertClub("Club Members", "Desc", true, creatorId);

        long result = dbHelper.addClubMember(clubId, memberId, "MEMBER", "PENDING");
        assertTrue("Add member deve retornar ID > 0", result > 0);
    }

    @Test
    public void testGetPendingMemberRequests() {
        String emailCreator = "pending_cr_" + System.currentTimeMillis() + "@test.com";
        String emailMember = "pending_mb_" + System.currentTimeMillis() + "@test.com";

        long creatorId = dbHelper.insertUser("PendCreator", emailCreator,
                PasswordUtil.hashPassword("123"), "", "Fantasia", "ORGANIZER");
        long memberId = dbHelper.insertUser("PendMember", emailMember,
                PasswordUtil.hashPassword("123"), "", "Fantasia", "READER");

        long clubId = dbHelper.insertClub("Pending Club", "Desc", true, creatorId);
        dbHelper.addClubMember(clubId, memberId, "MEMBER", "PENDING");

        List<ClubMember> pending = dbHelper.getPendingMemberRequests(creatorId);
        assertNotNull(pending);
        assertTrue("Deve haver pelo menos 1 pedido pendente", pending.size() >= 1);
    }

    @Test
    public void testUpdateMemberStatus() {
        String emailCreator = "upd_cr_" + System.currentTimeMillis() + "@test.com";
        String emailMember = "upd_mb_" + System.currentTimeMillis() + "@test.com";

        long creatorId = dbHelper.insertUser("UpdCreator", emailCreator,
                PasswordUtil.hashPassword("123"), "", "Fantasia", "ORGANIZER");
        long memberId = dbHelper.insertUser("UpdMember", emailMember,
                PasswordUtil.hashPassword("123"), "", "Fantasia", "READER");

        long clubId = dbHelper.insertClub("Update Club", "Desc", true, creatorId);
        dbHelper.addClubMember(clubId, memberId, "MEMBER", "PENDING");
        dbHelper.updateMemberStatus(clubId, memberId, "APPROVED");

        List<ClubMember> pending = dbHelper.getPendingMemberRequests(creatorId);
        for (ClubMember m : pending) {
            assertNotEquals("Membro aprovado nao deve estar pendente",
                    memberId, m.getUserId());
        }
    }

    // ==================== DATA CONSISTENCY ====================

    @Test
    public void testSeedDataCreatesBooks() {
        List<Book> books = dbHelper.getAllBooks();
        assertTrue("Seed data deve criar pelo menos 6 livros", books.size() >= 6);
    }

    @Test
    public void testBookGenresFromSeed() {
        List<Book> books = dbHelper.getAllBooks();
        boolean hasTecnologia = false;
        boolean hasFantasia = false;
        for (Book b : books) {
            if ("Tecnologia".equals(b.getGenre()))
                hasTecnologia = true;
            if ("Fantasia".equals(b.getGenre()))
                hasFantasia = true;
        }
        assertTrue("Deve haver livro de Tecnologia", hasTecnologia);
        assertTrue("Deve haver livro de Fantasia", hasFantasia);
    }
}

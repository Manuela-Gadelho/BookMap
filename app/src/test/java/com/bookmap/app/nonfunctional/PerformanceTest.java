package com.bookmap.app.nonfunctional;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.Book;
import com.bookmap.app.model.User;
import com.bookmap.app.util.LocationHelper;
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
public class PerformanceTest {

    private DatabaseHelper dbHelper;

    @Before
    public void setUp() {
        DatabaseHelper.resetInstance();
        Context context = ApplicationProvider.getApplicationContext();
        dbHelper = DatabaseHelper.getInstance(context);
    }

    @Test
    public void testPasswordHashPerformance() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            PasswordUtil.hashPassword("senhaTest" + i);
        }
        long elapsed = System.currentTimeMillis() - start;
        assertTrue("100 hashes devem completar em menos de 2 segundos: " + elapsed + "ms",
                elapsed < 2000);
    }

    @Test
    public void testDistanceCalculationPerformance() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            LocationHelper.calculateDistance(
                    -23.5505 + (i * 0.001), -46.6333,
                    -22.9068, -43.1729);
        }
        long elapsed = System.currentTimeMillis() - start;
        assertTrue("10000 calculos de distancia devem completar em menos de 1 segundo: " + elapsed + "ms",
                elapsed < 1000);
    }

    @Test
    public void testDatabaseQueryPerformance() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 50; i++) {
            List<Book> books = dbHelper.getAllBooks();
        }
        long elapsed = System.currentTimeMillis() - start;
        assertTrue("50 queries de livros devem completar em menos de 2 segundos: " + elapsed + "ms",
                elapsed < 2000);
    }

    @Test
    public void testDatabaseInsertPerformance() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            dbHelper.insertBook("Perf Book " + i, "Perf Author",
                    "Synopsis", "", "Fantasia", "perf-" + i);
        }
        long elapsed = System.currentTimeMillis() - start;
        assertTrue("20 insercoes devem completar em menos de 2 segundos: " + elapsed + "ms",
                elapsed < 2000);
    }

    @Test
    public void testSearchBooksPerformance() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 50; i++) {
            dbHelper.searchBooks("Engenharia");
        }
        long elapsed = System.currentTimeMillis() - start;
        assertTrue("50 buscas devem completar em menos de 2 segundos: " + elapsed + "ms",
                elapsed < 2000);
    }

    @Test
    public void testUserAuthenticationPerformance() {
        String email = "perftest_" + System.currentTimeMillis() + "@test.com";
        String hash = PasswordUtil.hashPassword("senha123");
        dbHelper.insertUser("PerfUser", email, hash, "", "Fantasia", "READER");

        long start = System.currentTimeMillis();
        for (int i = 0; i < 50; i++) {
            User user = dbHelper.getUserByEmail(email);
            PasswordUtil.verifyPassword("senha123", user.getPasswordHash());
        }
        long elapsed = System.currentTimeMillis() - start;
        assertTrue("50 autenticacoes devem completar em menos de 2 segundos: " + elapsed + "ms",
                elapsed < 2000);
    }

    @Test
    public void testPasswordVerifyPerformance() {
        String hash = PasswordUtil.hashPassword("senhaSegura");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            PasswordUtil.verifyPassword("senhaSegura", hash);
        }
        long elapsed = System.currentTimeMillis() - start;
        assertTrue("100 verificacoes devem completar em menos de 2 segundos: " + elapsed + "ms",
                elapsed < 2000);
    }
}

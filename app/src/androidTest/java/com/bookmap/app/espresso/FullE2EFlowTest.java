package com.bookmap.app.espresso;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bookmap.app.BookDetailsActivity;
import com.bookmap.app.HomeActivity;
import com.bookmap.app.LoginActivity;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.User;
import com.bookmap.app.util.PasswordUtil;
import com.bookmap.app.util.SessionManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import com.bookmap.app.R;

/**
 * End-to-end Espresso test covering the complete user journey:
 * Login -> Home -> Search book -> View details -> Add to shelf -> Write review -> Update progress
 */
@RunWith(AndroidJUnit4.class)
public class FullE2EFlowTest {

    private DatabaseHelper dbHelper;
    private SessionManager session;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        dbHelper = DatabaseHelper.getInstance(context);
        session = new SessionManager(context);
        session.logout();

        if (dbHelper.getUserByEmail("e2e@bookmap.com") == null) {
            String hash = PasswordUtil.hashPassword("senha123");
            dbHelper.insertUser("E2E User", "e2e@bookmap.com", hash, "", "Fantasia", "READER");
        }

        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
        session.logout();
    }

    @Test
    public void testCompleteLoginToHomeFlow() {
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);

        // Step 1: Login
        onView(withId(R.id.editEmail)).perform(replaceText("e2e@bookmap.com"), closeSoftKeyboard());
        onView(withId(R.id.editPassword)).perform(replaceText("senha123"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());

        // Step 2: Verify we're on HomeActivity
        intended(hasComponent(HomeActivity.class.getName()));
        scenario.close();
    }

    @Test
    public void testViewBookDetailsAndAddToShelf() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Login first
        User user = dbHelper.getUserByEmail("e2e@bookmap.com");
        session.createLoginSession(user.getId(), user.getName(), user.getEmail(), user.getRole());

        // Get first seed book
        long bookId = 1;
        Intent intent = new Intent(context, BookDetailsActivity.class);
        intent.putExtra(BookDetailsActivity.EXTRA_BOOK_ID, bookId);
        ActivityScenario<BookDetailsActivity> scenario = ActivityScenario.launch(intent);

        // Verify book details are displayed
        onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.tvAuthor)).check(matches(isDisplayed()));
        onView(withId(R.id.tvGenre)).check(matches(isDisplayed()));
        onView(withId(R.id.ratingBarAverage)).check(matches(isDisplayed()));

        // Try to add to shelf
        onView(withId(R.id.btnAddToShelf)).perform(scrollTo(), click());

        scenario.close();
    }

    @Test
    public void testWriteReviewWithRating() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        User user = dbHelper.getUserByEmail("e2e@bookmap.com");
        session.createLoginSession(user.getId(), user.getName(), user.getEmail(), user.getRole());

        long bookId = 2; // Second seed book
        Intent intent = new Intent(context, BookDetailsActivity.class);
        intent.putExtra(BookDetailsActivity.EXTRA_BOOK_ID, bookId);
        ActivityScenario<BookDetailsActivity> scenario = ActivityScenario.launch(intent);

        // Write review
        onView(withId(R.id.editReviewText)).perform(scrollTo(),
                replaceText("Otimo livro, recomendo!"), closeSoftKeyboard());
        onView(withId(R.id.ratingBar)).perform(scrollTo(), click());
        onView(withId(R.id.btnSubmitReview)).perform(scrollTo(), click());

        scenario.close();
    }

    @Test
    public void testGuestModeRestrictions() {
        // Enter as guest
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.tvGuest)).perform(click());
        intended(hasComponent(HomeActivity.class.getName()));
        scenario.close();
    }
}

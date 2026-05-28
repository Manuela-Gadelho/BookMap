package com.bookmap.app.espresso;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bookmap.app.AddBookActivity;
import com.bookmap.app.BookDetailsActivity;
import com.bookmap.app.HomeActivity;
import com.bookmap.app.SearchActivity;
import com.bookmap.app.UpdateProgressActivity;
import com.bookmap.app.database.DatabaseHelper;
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
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import com.bookmap.app.R;


@RunWith(AndroidJUnit4.class)
public class BookFlowTest {

    private DatabaseHelper dbHelper;
    private SessionManager session;
    private long testBookId;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        dbHelper = DatabaseHelper.getInstance(context);
        session = new SessionManager(context);

        
        if (dbHelper.getUserByEmail("booktest@bookmap.com") == null) {
            String hash = PasswordUtil.hashPassword("senha123");
            dbHelper.insertUser("Book Tester", "booktest@bookmap.com", hash, "", "Fantasia", "READER");
        }
        long userId = dbHelper.getUserByEmail("booktest@bookmap.com").getId();
        session.createLoginSession(userId, "Book Tester", "booktest@bookmap.com", "READER");

        
        testBookId = dbHelper.insertBook("Livro Espresso", "Autor Teste",
                "Sinopse do teste espresso", "", "Fantasia", "978-0000000001");
        if (testBookId <= 0) {
            
            testBookId = 1;
        }

        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testHomeScreenDisplayed() {
        ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(HomeActivity.class);
        onView(withId(R.id.recyclerBooks)).check(matches(isDisplayed()));
        onView(withId(R.id.btnLendo)).check(matches(isDisplayed()));
        onView(withId(R.id.btnQueroLer)).check(matches(isDisplayed()));
        onView(withId(R.id.btnLidos)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testFilterButtons() {
        ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(HomeActivity.class);
        onView(withId(R.id.btnLendo)).perform(click());
        onView(withId(R.id.btnQueroLer)).perform(click());
        onView(withId(R.id.btnLidos)).perform(click());
        
        onView(withId(R.id.recyclerBooks)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testNavigateToSearch() {
        ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(HomeActivity.class);
        onView(withId(R.id.btnSearch)).perform(click());
        intended(hasComponent(SearchActivity.class.getName()));
        scenario.close();
    }

    @Test
    public void testNavigateToAddBook() {
        ActivityScenario<HomeActivity> scenario = ActivityScenario.launch(HomeActivity.class);
        onView(withId(R.id.fabAddBook)).perform(click());
        intended(hasComponent(AddBookActivity.class.getName()));
        scenario.close();
    }

    @Test
    public void testAddBookScreenDisplayed() {
        ActivityScenario<AddBookActivity> scenario = ActivityScenario.launch(AddBookActivity.class);
        onView(withId(R.id.editTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.editAuthor)).check(matches(isDisplayed()));
        onView(withId(R.id.editSynopsis)).check(matches(isDisplayed()));
        onView(withId(R.id.spinnerGenre)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testAddBookValidation() {
        ActivityScenario<AddBookActivity> scenario = ActivityScenario.launch(AddBookActivity.class);
        
        onView(withId(R.id.btnSaveBook)).perform(scrollTo(), click());
        
        onView(withId(R.id.editTitle)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testAddBookSuccess() {
        ActivityScenario<AddBookActivity> scenario = ActivityScenario.launch(AddBookActivity.class);
        long timestamp = System.currentTimeMillis();
        onView(withId(R.id.editTitle)).perform(replaceText("Livro Teste " + timestamp), closeSoftKeyboard());
        onView(withId(R.id.editAuthor)).perform(replaceText("Autor Espresso"), closeSoftKeyboard());
        onView(withId(R.id.editSynopsis)).perform(replaceText("Sinopse de teste"), closeSoftKeyboard());
        onView(withId(R.id.btnSaveBook)).perform(scrollTo(), click());
        
        scenario.close();
    }

    @Test
    public void testBookDetailsScreenDisplayed() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(context, BookDetailsActivity.class);
        intent.putExtra(BookDetailsActivity.EXTRA_BOOK_ID, testBookId);
        ActivityScenario<BookDetailsActivity> scenario = ActivityScenario.launch(intent);
        onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.tvAuthor)).check(matches(isDisplayed()));
        onView(withId(R.id.ratingBarAverage)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testWriteReviewValidation() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(context, BookDetailsActivity.class);
        intent.putExtra(BookDetailsActivity.EXTRA_BOOK_ID, testBookId);
        ActivityScenario<BookDetailsActivity> scenario = ActivityScenario.launch(intent);

        
        onView(withId(R.id.btnSubmitReview)).perform(scrollTo(), click());
        
        onView(withId(R.id.tvTitle)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testSearchScreenDisplayed() {
        ActivityScenario<SearchActivity> scenario = ActivityScenario.launch(SearchActivity.class);
        onView(withId(R.id.editSearch)).check(matches(isDisplayed()));
        onView(withId(R.id.btnSearch)).check(matches(isDisplayed()));
        onView(withId(R.id.btnTabBooks)).check(matches(isDisplayed()));
        onView(withId(R.id.btnTabUsers)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testSearchBooks() {
        ActivityScenario<SearchActivity> scenario = ActivityScenario.launch(SearchActivity.class);
        onView(withId(R.id.editSearch)).perform(replaceText("Engenharia"), closeSoftKeyboard());
        onView(withId(R.id.btnSearch)).perform(click());
        
        onView(withId(R.id.recyclerResults)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testSearchUsers() {
        ActivityScenario<SearchActivity> scenario = ActivityScenario.launch(SearchActivity.class);
        onView(withId(R.id.btnTabUsers)).perform(click());
        onView(withId(R.id.editSearch)).perform(replaceText("Book Tester"), closeSoftKeyboard());
        onView(withId(R.id.btnSearch)).perform(click());
        
        scenario.close();
    }

    @Test
    public void testSearchEmptyQuery() {
        ActivityScenario<SearchActivity> scenario = ActivityScenario.launch(SearchActivity.class);
        onView(withId(R.id.btnSearch)).perform(click());
        
        onView(withId(R.id.editSearch)).check(matches(isDisplayed()));
        scenario.close();
    }
}

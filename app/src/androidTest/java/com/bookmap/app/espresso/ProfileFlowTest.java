package com.bookmap.app.espresso;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bookmap.app.LoginActivity;
import com.bookmap.app.ProfileActivity;
import com.bookmap.app.PublicProfileActivity;
import com.bookmap.app.ReportActivity;
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

/**
 * Espresso instrumented tests for the profile flow.
 * Tests: view profile, edit profile, view public profile, logout.
 */
@RunWith(AndroidJUnit4.class)
public class ProfileFlowTest {

    private DatabaseHelper dbHelper;
    private SessionManager session;
    private long testUserId;
    private long otherUserId;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        dbHelper = DatabaseHelper.getInstance(context);
        session = new SessionManager(context);

        if (dbHelper.getUserByEmail("profile@bookmap.com") == null) {
            String hash = PasswordUtil.hashPassword("senha123");
            dbHelper.insertUser("Profile User", "profile@bookmap.com", hash, "", "Romance", "READER");
        }
        testUserId = dbHelper.getUserByEmail("profile@bookmap.com").getId();

        if (dbHelper.getUserByEmail("outro@bookmap.com") == null) {
            String hash = PasswordUtil.hashPassword("senha123");
            dbHelper.insertUser("Outro Usuario", "outro@bookmap.com", hash, "", "Terror", "READER");
        }
        otherUserId = dbHelper.getUserByEmail("outro@bookmap.com").getId();

        session.createLoginSession(testUserId, "Profile User", "profile@bookmap.com", "READER");
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testProfileScreenDisplayed() {
        ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(ProfileActivity.class);
        onView(withId(R.id.tvUserName)).check(matches(isDisplayed()));
        onView(withId(R.id.tvUserEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.tvUserRole)).check(matches(isDisplayed()));
        onView(withId(R.id.editName)).check(matches(isDisplayed()));
        onView(withId(R.id.editBio)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testEditProfileName() {
        ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(ProfileActivity.class);
        onView(withId(R.id.editName)).perform(replaceText("Novo Nome"), closeSoftKeyboard());
        onView(withId(R.id.btnSave)).perform(scrollTo(), click());
        onView(withId(R.id.tvUserName)).check(matches(withText("Novo Nome")));
        scenario.close();
    }

    @Test
    public void testEditProfileEmptyName() {
        ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(ProfileActivity.class);
        onView(withId(R.id.editName)).perform(replaceText(""), closeSoftKeyboard());
        onView(withId(R.id.btnSave)).perform(scrollTo(), click());
        // Should remain on ProfileActivity (toast shown)
        onView(withId(R.id.editName)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testLogout() {
        ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(ProfileActivity.class);
        onView(withId(R.id.btnLogout)).perform(click());
        intended(hasComponent(LoginActivity.class.getName()));
        scenario.close();
    }

    @Test
    public void testPublicProfileScreenDisplayed() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(context, PublicProfileActivity.class);
        intent.putExtra(PublicProfileActivity.EXTRA_USER_ID, otherUserId);
        ActivityScenario<PublicProfileActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.tvName)).check(matches(isDisplayed()));
        onView(withId(R.id.tvBio)).check(matches(isDisplayed()));
        onView(withId(R.id.tvGenres)).check(matches(isDisplayed()));
        onView(withId(R.id.btnReport)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testNavigateToReport() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(context, PublicProfileActivity.class);
        intent.putExtra(PublicProfileActivity.EXTRA_USER_ID, otherUserId);
        ActivityScenario<PublicProfileActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.btnReport)).perform(scrollTo(), click());
        intended(hasComponent(ReportActivity.class.getName()));
        scenario.close();
    }
}

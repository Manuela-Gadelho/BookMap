package com.bookmap.app.espresso;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bookmap.app.MapActivity;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.util.PasswordUtil;
import com.bookmap.app.util.SessionManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import com.bookmap.app.R;

/**
 * Espresso instrumented tests for the literary map flow.
 * Tests: map display, filters, location toggle, nearby users.
 */
@RunWith(AndroidJUnit4.class)
public class MapFlowTest {

    private SessionManager session;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        session = new SessionManager(context);

        if (dbHelper.getUserByEmail("maptest@bookmap.com") == null) {
            String hash = PasswordUtil.hashPassword("senha123");
            dbHelper.insertUser("Map Tester", "maptest@bookmap.com", hash, "", "Fantasia", "READER");
        }
        long userId = dbHelper.getUserByEmail("maptest@bookmap.com").getId();
        session.createLoginSession(userId, "Map Tester", "maptest@bookmap.com", "READER");
    }

    @Test
    public void testMapScreenDisplayed() {
        ActivityScenario<MapActivity> scenario = ActivityScenario.launch(MapActivity.class);
        onView(withId(R.id.recyclerUsers)).check(matches(isDisplayed()));
        onView(withId(R.id.spinnerGenre)).check(matches(isDisplayed()));
        onView(withId(R.id.spinnerLanguage)).check(matches(isDisplayed()));
        onView(withId(R.id.seekDistance)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testLocationPrivacyToggle() {
        ActivityScenario<MapActivity> scenario = ActivityScenario.launch(MapActivity.class);
        onView(withId(R.id.switchLocationVisible)).check(matches(isDisplayed()));
        onView(withId(R.id.switchLocationVisible)).perform(click());
        onView(withId(R.id.tvLocationStatus)).check(matches(isDisplayed()));
        // Toggle back
        onView(withId(R.id.switchLocationVisible)).perform(click());
        onView(withId(R.id.tvLocationStatus)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testDistanceSeekBar() {
        ActivityScenario<MapActivity> scenario = ActivityScenario.launch(MapActivity.class);
        onView(withId(R.id.seekDistance)).check(matches(isDisplayed()));
        onView(withId(R.id.tvDistance)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testGenreFilter() {
        ActivityScenario<MapActivity> scenario = ActivityScenario.launch(MapActivity.class);
        onView(withId(R.id.spinnerGenre)).check(matches(isDisplayed()));
        onView(withId(R.id.spinnerGenre)).perform(click());
        scenario.close();
    }

    @Test
    public void testLocationStatusDisplayed() {
        ActivityScenario<MapActivity> scenario = ActivityScenario.launch(MapActivity.class);
        onView(withId(R.id.tvLocationStatus)).check(matches(isDisplayed()));
        scenario.close();
    }
}

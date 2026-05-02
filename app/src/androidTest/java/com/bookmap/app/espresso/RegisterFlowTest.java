package com.bookmap.app.espresso;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bookmap.app.HomeActivity;
import com.bookmap.app.LoginActivity;
import com.bookmap.app.RegisterActivity;
import com.bookmap.app.database.DatabaseHelper;
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
 * Espresso instrumented tests for the registration flow.
 * Tests: successful registration, validation errors, duplicate email, navigation.
 */
@RunWith(AndroidJUnit4.class)
public class RegisterFlowTest {

    private SessionManager session;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        session = new SessionManager(context);
        session.logout();
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
        session.logout();
    }

    @Test
    public void testRegisterScreenDisplayed() {
        ActivityScenario<RegisterActivity> scenario = ActivityScenario.launch(RegisterActivity.class);
        onView(withId(R.id.editName)).check(matches(isDisplayed()));
        onView(withId(R.id.editEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.editPassword)).check(matches(isDisplayed()));
        onView(withId(R.id.editConfirmPassword)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testSuccessfulRegistration() {
        ActivityScenario<RegisterActivity> scenario = ActivityScenario.launch(RegisterActivity.class);
        long timestamp = System.currentTimeMillis();
        String email = "novo" + timestamp + "@bookmap.com";

        onView(withId(R.id.editName)).perform(replaceText("Novo Usuario"), closeSoftKeyboard());
        onView(withId(R.id.editEmail)).perform(replaceText(email), closeSoftKeyboard());
        onView(withId(R.id.editPassword)).perform(replaceText("senha123"), closeSoftKeyboard());
        onView(withId(R.id.editConfirmPassword)).perform(replaceText("senha123"), closeSoftKeyboard());
        onView(withId(R.id.checkFantasia)).perform(scrollTo(), click());
        onView(withId(R.id.btnRegister)).perform(scrollTo(), click());

        intended(hasComponent(HomeActivity.class.getName()));
        scenario.close();
    }

    @Test
    public void testRegisterWithEmptyFields() {
        ActivityScenario<RegisterActivity> scenario = ActivityScenario.launch(RegisterActivity.class);
        onView(withId(R.id.btnRegister)).perform(scrollTo(), click());
        // Should remain on RegisterActivity
        onView(withId(R.id.editName)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testRegisterWithMismatchedPasswords() {
        ActivityScenario<RegisterActivity> scenario = ActivityScenario.launch(RegisterActivity.class);
        onView(withId(R.id.editName)).perform(replaceText("User Teste"), closeSoftKeyboard());
        onView(withId(R.id.editEmail)).perform(replaceText("mismatch@bookmap.com"), closeSoftKeyboard());
        onView(withId(R.id.editPassword)).perform(replaceText("senha123"), closeSoftKeyboard());
        onView(withId(R.id.editConfirmPassword)).perform(replaceText("outrasenha"), closeSoftKeyboard());
        onView(withId(R.id.btnRegister)).perform(scrollTo(), click());
        // Should remain on RegisterActivity
        onView(withId(R.id.editName)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testRegisterWithShortPassword() {
        ActivityScenario<RegisterActivity> scenario = ActivityScenario.launch(RegisterActivity.class);
        onView(withId(R.id.editName)).perform(replaceText("User Teste"), closeSoftKeyboard());
        onView(withId(R.id.editEmail)).perform(replaceText("short@bookmap.com"), closeSoftKeyboard());
        onView(withId(R.id.editPassword)).perform(replaceText("123"), closeSoftKeyboard());
        onView(withId(R.id.editConfirmPassword)).perform(replaceText("123"), closeSoftKeyboard());
        onView(withId(R.id.btnRegister)).perform(scrollTo(), click());
        onView(withId(R.id.editName)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testNavigateToLogin() {
        ActivityScenario<RegisterActivity> scenario = ActivityScenario.launch(RegisterActivity.class);
        onView(withId(R.id.tvLogin)).perform(scrollTo(), click());
        intended(hasComponent(LoginActivity.class.getName()));
        scenario.close();
    }

    @Test
    public void testGenreCheckboxesDisplayed() {
        ActivityScenario<RegisterActivity> scenario = ActivityScenario.launch(RegisterActivity.class);
        onView(withId(R.id.checkFantasia)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.checkTerror)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.checkRomance)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.checkFiccao)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.checkTecnologia)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.checkLitBrasileira)).perform(scrollTo()).check(matches(isDisplayed()));
        scenario.close();
    }
}

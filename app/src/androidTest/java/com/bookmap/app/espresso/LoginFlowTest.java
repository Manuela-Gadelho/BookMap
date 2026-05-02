package com.bookmap.app.espresso;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bookmap.app.HomeActivity;
import com.bookmap.app.LoginActivity;
import com.bookmap.app.RegisterActivity;
import com.bookmap.app.ForgotPasswordActivity;
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
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import com.bookmap.app.R;

/**
 * Espresso instrumented tests for the login flow.
 * Tests: successful login, failed login, empty fields, navigation to register/forgot password.
 */
@RunWith(AndroidJUnit4.class)
public class LoginFlowTest {

    private DatabaseHelper dbHelper;
    private SessionManager session;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        dbHelper = DatabaseHelper.getInstance(context);
        session = new SessionManager(context);
        session.logout();

        // Ensure test user exists
        if (dbHelper.getUserByEmail("teste@bookmap.com") == null) {
            String hash = PasswordUtil.hashPassword("senha123");
            dbHelper.insertUser("Teste User", "teste@bookmap.com", hash, "", "Fantasia", "READER");
        }
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
        session.logout();
    }

    @Test
    public void testLoginScreenDisplayed() {
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.editEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.editPassword)).check(matches(isDisplayed()));
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));
        onView(withId(R.id.tvRegister)).check(matches(isDisplayed()));
        onView(withId(R.id.tvForgotPassword)).check(matches(isDisplayed()));
        onView(withId(R.id.tvGuest)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testSuccessfulLogin() {
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.editEmail)).perform(replaceText("teste@bookmap.com"), closeSoftKeyboard());
        onView(withId(R.id.editPassword)).perform(replaceText("senha123"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());
        intended(hasComponent(HomeActivity.class.getName()));
        scenario.close();
    }

    @Test
    public void testLoginWithEmptyFields() {
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.btnLogin)).perform(click());
        // Should remain on LoginActivity (toast shown but activity doesn't navigate)
        onView(withId(R.id.editEmail)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testLoginWithWrongPassword() {
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.editEmail)).perform(replaceText("teste@bookmap.com"), closeSoftKeyboard());
        onView(withId(R.id.editPassword)).perform(replaceText("senhaerrada"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());
        // Should remain on LoginActivity
        onView(withId(R.id.editEmail)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testLoginWithNonexistentUser() {
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.editEmail)).perform(replaceText("naoexiste@bookmap.com"), closeSoftKeyboard());
        onView(withId(R.id.editPassword)).perform(replaceText("qualquer"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());
        onView(withId(R.id.editEmail)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testNavigateToRegister() {
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.tvRegister)).perform(click());
        intended(hasComponent(RegisterActivity.class.getName()));
        scenario.close();
    }

    @Test
    public void testNavigateToForgotPassword() {
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.tvForgotPassword)).perform(click());
        intended(hasComponent(ForgotPasswordActivity.class.getName()));
        scenario.close();
    }

    @Test
    public void testGuestAccess() {
        ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class);
        onView(withId(R.id.tvGuest)).perform(click());
        intended(hasComponent(HomeActivity.class.getName()));
        scenario.close();
    }
}

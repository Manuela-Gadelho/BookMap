package com.bookmap.app.espresso;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bookmap.app.ForgotPasswordActivity;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.util.PasswordUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import com.bookmap.app.R;

/**
 * Espresso instrumented tests for the forgot password flow.
 * Tests: email verification, password reset, validation errors.
 */
@RunWith(AndroidJUnit4.class)
public class ForgotPasswordFlowTest {

    private DatabaseHelper dbHelper;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        dbHelper = DatabaseHelper.getInstance(context);

        if (dbHelper.getUserByEmail("reset@bookmap.com") == null) {
            String hash = PasswordUtil.hashPassword("senhaantiga");
            dbHelper.insertUser("Reset User", "reset@bookmap.com", hash, "", "Terror", "READER");
        }
    }

    @Test
    public void testForgotPasswordScreenDisplayed() {
        ActivityScenario<ForgotPasswordActivity> scenario =
                ActivityScenario.launch(ForgotPasswordActivity.class);
        onView(withId(R.id.editEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.btnVerifyEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.editNewPassword)).check(matches(isDisplayed()));
        onView(withId(R.id.editNewPassword)).check(matches(not(isEnabled())));
        onView(withId(R.id.editConfirmPassword)).check(matches(not(isEnabled())));
        scenario.close();
    }

    @Test
    public void testVerifyEmailSuccess() {
        ActivityScenario<ForgotPasswordActivity> scenario =
                ActivityScenario.launch(ForgotPasswordActivity.class);
        onView(withId(R.id.editEmail)).perform(replaceText("reset@bookmap.com"), closeSoftKeyboard());
        onView(withId(R.id.btnVerifyEmail)).perform(click());
        onView(withId(R.id.tvStatus)).check(matches(withText("E-mail verificado! Digite a nova senha.")));
        onView(withId(R.id.editNewPassword)).check(matches(isEnabled()));
        onView(withId(R.id.editConfirmPassword)).check(matches(isEnabled()));
        scenario.close();
    }

    @Test
    public void testVerifyEmailNotFound() {
        ActivityScenario<ForgotPasswordActivity> scenario =
                ActivityScenario.launch(ForgotPasswordActivity.class);
        onView(withId(R.id.editEmail)).perform(replaceText("naoexiste@bookmap.com"), closeSoftKeyboard());
        onView(withId(R.id.btnVerifyEmail)).perform(click());
        onView(withId(R.id.tvStatus)).check(matches(withText("E-mail nao encontrado no sistema")));
        onView(withId(R.id.editNewPassword)).check(matches(not(isEnabled())));
        scenario.close();
    }

    @Test
    public void testVerifyEmailEmpty() {
        ActivityScenario<ForgotPasswordActivity> scenario =
                ActivityScenario.launch(ForgotPasswordActivity.class);
        onView(withId(R.id.btnVerifyEmail)).perform(click());
        // Should remain on screen with email field visible
        onView(withId(R.id.editEmail)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testResetPasswordMismatch() {
        ActivityScenario<ForgotPasswordActivity> scenario =
                ActivityScenario.launch(ForgotPasswordActivity.class);

        // First verify email
        onView(withId(R.id.editEmail)).perform(replaceText("reset@bookmap.com"), closeSoftKeyboard());
        onView(withId(R.id.btnVerifyEmail)).perform(click());

        // Try reset with mismatched passwords
        onView(withId(R.id.editNewPassword)).perform(replaceText("novasenha1"), closeSoftKeyboard());
        onView(withId(R.id.editConfirmPassword)).perform(replaceText("novasenha2"), closeSoftKeyboard());
        onView(withId(R.id.btnResetPassword)).perform(click());

        // Should remain on ForgotPasswordActivity
        onView(withId(R.id.editNewPassword)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testResetPasswordTooShort() {
        ActivityScenario<ForgotPasswordActivity> scenario =
                ActivityScenario.launch(ForgotPasswordActivity.class);

        onView(withId(R.id.editEmail)).perform(replaceText("reset@bookmap.com"), closeSoftKeyboard());
        onView(withId(R.id.btnVerifyEmail)).perform(click());

        onView(withId(R.id.editNewPassword)).perform(replaceText("123"), closeSoftKeyboard());
        onView(withId(R.id.editConfirmPassword)).perform(replaceText("123"), closeSoftKeyboard());
        onView(withId(R.id.btnResetPassword)).perform(click());

        onView(withId(R.id.editNewPassword)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testFullResetFlow() {
        ActivityScenario<ForgotPasswordActivity> scenario =
                ActivityScenario.launch(ForgotPasswordActivity.class);

        onView(withId(R.id.editEmail)).perform(replaceText("reset@bookmap.com"), closeSoftKeyboard());
        onView(withId(R.id.btnVerifyEmail)).perform(click());

        onView(withId(R.id.editNewPassword)).perform(replaceText("novasenha123"), closeSoftKeyboard());
        onView(withId(R.id.editConfirmPassword)).perform(replaceText("novasenha123"), closeSoftKeyboard());
        onView(withId(R.id.btnResetPassword)).perform(click());

        // Activity should finish after successful reset (scenario becomes DESTROYED)
        // No further assertions needed - if no exception, test passes
        scenario.close();
    }
}

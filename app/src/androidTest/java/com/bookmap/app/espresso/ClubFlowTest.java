package com.bookmap.app.espresso;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bookmap.app.ClubActivity;
import com.bookmap.app.ClubListActivity;
import com.bookmap.app.CreateClubActivity;
import com.bookmap.app.NotificationsActivity;
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
public class ClubFlowTest {

    private DatabaseHelper dbHelper;
    private SessionManager session;
    private long testClubId;
    private long organizerUserId;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        dbHelper = DatabaseHelper.getInstance(context);
        session = new SessionManager(context);

        
        if (dbHelper.getUserByEmail("organizer@bookmap.com") == null) {
            String hash = PasswordUtil.hashPassword("senha123");
            dbHelper.insertUser("Organizador", "organizer@bookmap.com", hash, "", "Fantasia", "ORGANIZER");
        }
        organizerUserId = dbHelper.getUserByEmail("organizer@bookmap.com").getId();

        
        if (dbHelper.getUserByEmail("membro@bookmap.com") == null) {
            String hash = PasswordUtil.hashPassword("senha123");
            dbHelper.insertUser("Membro Teste", "membro@bookmap.com", hash, "", "Terror", "READER");
        }

        
        session.createLoginSession(organizerUserId, "Organizador", "organizer@bookmap.com", "ORGANIZER");

        
        testClubId = dbHelper.insertClub("Clube Espresso", "Clube para testes", true, organizerUserId);
        if (testClubId > 0) {
            dbHelper.addClubMember(testClubId, organizerUserId, "ORGANIZER", "APPROVED");
        } else {
            testClubId = 1;
        }

        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testClubListScreenDisplayed() {
        ActivityScenario<ClubListActivity> scenario = ActivityScenario.launch(ClubListActivity.class);
        onView(withId(R.id.recyclerClubs)).check(matches(isDisplayed()));
        onView(withId(R.id.btnMyClubs)).check(matches(isDisplayed()));
        onView(withId(R.id.btnAllClubs)).check(matches(isDisplayed()));
        onView(withId(R.id.btnCreateClub)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testToggleMyClubsAllClubs() {
        ActivityScenario<ClubListActivity> scenario = ActivityScenario.launch(ClubListActivity.class);
        onView(withId(R.id.btnAllClubs)).perform(click());
        onView(withId(R.id.recyclerClubs)).check(matches(isDisplayed()));
        onView(withId(R.id.btnMyClubs)).perform(click());
        onView(withId(R.id.recyclerClubs)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testNavigateToCreateClub() {
        ActivityScenario<ClubListActivity> scenario = ActivityScenario.launch(ClubListActivity.class);
        onView(withId(R.id.btnCreateClub)).perform(click());
        intended(hasComponent(CreateClubActivity.class.getName()));
        scenario.close();
    }

    @Test
    public void testCreateClubScreenDisplayed() {
        ActivityScenario<CreateClubActivity> scenario = ActivityScenario.launch(CreateClubActivity.class);
        onView(withId(R.id.editClubName)).check(matches(isDisplayed()));
        onView(withId(R.id.editClubDescription)).check(matches(isDisplayed()));
        onView(withId(R.id.checkPublic)).check(matches(isDisplayed()));
        onView(withId(R.id.recyclerMembers)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testCreateClubValidation() {
        ActivityScenario<CreateClubActivity> scenario = ActivityScenario.launch(CreateClubActivity.class);
        
        onView(withId(R.id.btnCreateClub)).perform(scrollTo(), click());
        onView(withId(R.id.editClubName)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testClubDetailsScreenDisplayed() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(context, ClubActivity.class);
        intent.putExtra(ClubActivity.EXTRA_CLUB_ID, testClubId);
        ActivityScenario<ClubActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.tvClubName)).check(matches(isDisplayed()));
        onView(withId(R.id.tvClubDescription)).check(matches(isDisplayed()));
        onView(withId(R.id.tvClubType)).check(matches(isDisplayed()));
        onView(withId(R.id.recyclerMembers)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void testClubJoinButtonForMember() {
        
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(context, ClubActivity.class);
        intent.putExtra(ClubActivity.EXTRA_CLUB_ID, testClubId);
        ActivityScenario<ClubActivity> scenario = ActivityScenario.launch(intent);

        
        onView(withId(R.id.btnJoinClub)).check(matches(withText("Membro")));
        scenario.close();
    }

    @Test
    public void testClubJoinRequestAsNonMember() {
        
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        long memberId = dbHelper.getUserByEmail("membro@bookmap.com").getId();
        session.createLoginSession(memberId, "Membro Teste", "membro@bookmap.com", "READER");

        Intent intent = new Intent(context, ClubActivity.class);
        intent.putExtra(ClubActivity.EXTRA_CLUB_ID, testClubId);
        ActivityScenario<ClubActivity> scenario = ActivityScenario.launch(intent);

        onView(withId(R.id.btnJoinClub)).check(matches(isDisplayed()));
        onView(withId(R.id.btnJoinClub)).perform(click());
        
        onView(withId(R.id.btnJoinClub)).check(matches(withText("Pendente")));
        scenario.close();

        
        session.createLoginSession(organizerUserId, "Organizador", "organizer@bookmap.com", "ORGANIZER");
    }

    @Test
    public void testNotificationsScreenDisplayed() {
        ActivityScenario<NotificationsActivity> scenario =
                ActivityScenario.launch(NotificationsActivity.class);
        
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
        scenario.close();
    }
}

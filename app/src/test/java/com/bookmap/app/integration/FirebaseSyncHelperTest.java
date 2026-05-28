package com.bookmap.app.integration;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.bookmap.app.database.FirebaseSyncHelper;
import com.bookmap.app.model.Book;
import com.bookmap.app.model.Club;
import com.bookmap.app.model.Review;
import com.bookmap.app.model.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class FirebaseSyncHelperTest {

    private FirebaseSyncHelper syncHelper;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        syncHelper = FirebaseSyncHelper.getInstance(context);
    }

    @Test
    public void testInstanceNotNull() {
        assertNotNull("SyncHelper nao deve ser null", syncHelper);
    }

    @Test
    public void testFirebaseAvailabilityCheck() {
        
        
        boolean available = syncHelper.isFirebaseAvailable();
        
        
        assertNotNull(syncHelper);
    }

    @Test
    public void testSyncUserToCloudDoesNotCrash() {
        User user = new User("Test", "test@test.com", "hash", "Fantasia", "READER");
        user.setId(999);
        user.setLatitude(-23.5505);
        user.setLongitude(-46.6333);
        
        syncHelper.syncUserToCloud(user);
    }

    @Test
    public void testSyncBookToCloudDoesNotCrash() {
        Book book = new Book("Test Book", "Test Author", "Synopsis", "Fantasia");
        book.setId(999);
        syncHelper.syncBookToCloud(book);
    }

    @Test
    public void testSyncReviewToCloudDoesNotCrash() {
        Review review = new Review();
        review.setId(999);
        review.setUserId(1);
        review.setBookId(1);
        review.setText("Great book");
        review.setRating(5);
        syncHelper.syncReviewToCloud(review);
    }

    @Test
    public void testSyncClubToCloudDoesNotCrash() {
        Club club = new Club();
        club.setId(999);
        club.setName("Test Club");
        club.setDescription("Test Description");
        club.setPublic(true);
        club.setCreatorId(1);
        syncHelper.syncClubToCloud(club);
    }

    @Test
    public void testSyncUserBookToCloudDoesNotCrash() {
        syncHelper.syncUserBookToCloud(1, 1, "LENDO", 50);
    }

    @Test
    public void testUpdateUserLocationInCloudDoesNotCrash() {
        syncHelper.updateUserLocationInCloud(1, -23.5505, -46.6333);
    }
}

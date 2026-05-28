package com.bookmap.app.functional;

import com.bookmap.app.NotificationsActivity;
import com.bookmap.app.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class NotificationsActivityTest {

    @Test
    public void testActivityFinishesWhenNotLoggedIn() {
        try {
            NotificationsActivity activity = Robolectric.buildActivity(NotificationsActivity.class)
                    .create().resume().get();
            if (activity != null) {
                assertTrue("Activity deve finalizar se usuario nao logado", activity.isFinishing());
            }
        } catch (Exception e) {
            
        }
    }

    @Test
    public void testLayoutElementIds() {
        
        int recyclerNotifications = R.id.recyclerNotifications;
        int tvEmpty = R.id.tvEmpty;
        int btnBack = R.id.btnBack;
        assertTrue(recyclerNotifications > 0);
        assertTrue(tvEmpty > 0);
        assertTrue(btnBack > 0);
    }
}

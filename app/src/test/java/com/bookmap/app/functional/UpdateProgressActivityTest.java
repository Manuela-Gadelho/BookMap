package com.bookmap.app.functional;

import android.content.Intent;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.bookmap.app.R;
import com.bookmap.app.UpdateProgressActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class UpdateProgressActivityTest {

    @Test
    public void testActivityFinishesWithoutBookId() {
        UpdateProgressActivity activity = Robolectric.buildActivity(UpdateProgressActivity.class)
                .create().resume().get();
        assertTrue("Activity deve finalizar sem bookId", activity.isFinishing());
    }

    @Test
    public void testActivityFinishesWithInvalidBookId() {
        Intent intent = new Intent();
        intent.putExtra(UpdateProgressActivity.EXTRA_BOOK_ID, -1L);
        UpdateProgressActivity activity = Robolectric.buildActivity(UpdateProgressActivity.class, intent)
                .create().resume().get();
        assertTrue("Activity deve finalizar com bookId invalido", activity.isFinishing());
    }
}

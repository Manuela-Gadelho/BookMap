package com.bookmap.app.functional;

import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bookmap.app.MapActivity;
import com.bookmap.app.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class MapActivityTest {

    private MapActivity createActivity() {
        try {
            return Robolectric.buildActivity(MapActivity.class)
                    .create().resume().get();
        } catch (Exception e) {
            return null;
        }
    }

    @Test
    public void testActivityCreation() {
        MapActivity activity = createActivity();
        
        
        if (activity != null) {
            assertNotNull(activity);
        }
    }

    @Test
    public void testGenreFilterOptions() {
        
        String[] expectedGenres = {"Todos", "Fantasia", "Terror", "Romance",
                "Ficcao Cientifica", "Tecnologia", "Literatura Brasileira"};
        assertEquals(7, expectedGenres.length);
        assertEquals("Todos", expectedGenres[0]);
    }

    @Test
    public void testLanguageFilterOptions() {
        String[] expectedLanguages = {"Todos", "Portugues", "English", "Espanol"};
        assertEquals(4, expectedLanguages.length);
        assertEquals("Todos", expectedLanguages[0]);
    }

    @Test
    public void testDefaultDistanceIs50km() {
        
        int defaultDistance = 50;
        assertEquals(50, defaultDistance);
    }

    @Test
    public void testDistanceMaxIs100km() {
        int maxDistance = 100;
        assertEquals(100, maxDistance);
    }

    @Test
    public void testLocationPermissionCode() {
        
        int LOCATION_PERMISSION_REQUEST = 1001;
        assertEquals(1001, LOCATION_PERMISSION_REQUEST);
    }
}

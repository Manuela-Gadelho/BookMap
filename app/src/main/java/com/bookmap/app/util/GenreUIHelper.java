package com.bookmap.app.util;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

public class GenreUIHelper {
    public static void setupGenreSpinner(Context context, Spinner spinner, ChipGroup chipGroup, List<String> selectedGenres) {
        List<String> options = new ArrayList<>();
        options.add("Selecione os gêneros...");
        options.addAll(GenreUtil.getGenres());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, options);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    String selected = options.get(position);
                    if (!selectedGenres.contains(selected)) {
                        selectedGenres.add(selected);
                        addChipToGroup(context, chipGroup, selected, selectedGenres);
                    }
                    spinner.setSelection(0);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Load initial
        chipGroup.removeAllViews();
        for (String g : selectedGenres) {
            addChipToGroup(context, chipGroup, g, selectedGenres);
        }
    }

    private static void addChipToGroup(Context context, ChipGroup chipGroup, String genre, List<String> selectedGenres) {
        Chip chip = new Chip(context);
        chip.setText(genre);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            chipGroup.removeView(chip);
            selectedGenres.remove(genre);
        });
        chipGroup.addView(chip);
    }
}

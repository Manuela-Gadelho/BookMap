package com.bookmap.app.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bookmap.app.R;
import java.util.ArrayList;
import java.util.List;

public class GenreUIHelper {

    public interface OnGenreSelectionChanged {
        void onChange(List<String> selectedGenres);
    }

    
    public static List<String> setupGenreRecycler(Context context, RecyclerView rv,
                                                   List<String> preSelected,
                                                   OnGenreSelectionChanged listener) {
        List<String> genres = GenreUtil.getGenres();
        List<String> selected = new ArrayList<>();
        if (preSelected != null) selected.addAll(preSelected);

        GenreChipAdapter adapter = new GenreChipAdapter(genres, selected, () -> {
            if (listener != null) listener.onChange(new ArrayList<>(selected));
        });
        rv.setLayoutManager(new GridLayoutManager(context, 2));
        rv.setAdapter(adapter);
        rv.setNestedScrollingEnabled(false);
        return selected;
    }

    
    
    
    private static class GenreChipAdapter
            extends RecyclerView.Adapter<GenreChipAdapter.VH> {

        interface OnChanged { void changed(); }

        private final List<String> genres;
        private final List<String> selected;
        private final OnChanged onChange;

        GenreChipAdapter(List<String> genres, List<String> selected, OnChanged cb) {
            this.genres   = genres;
            this.selected = selected;
            this.onChange = cb;
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_genre, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            String genre = genres.get(pos);
            h.tv.setText(genre);
            boolean isSelected = selected.contains(genre);
            applyState(h.tv, isSelected);

            h.itemView.setOnClickListener(v -> {
                boolean nowSelected = !selected.contains(genre);
                if (nowSelected) selected.add(genre);
                else             selected.remove(genre);
                applyState(h.tv, nowSelected);
                if (onChange != null) onChange.changed();
            });
        }

        private void applyState(TextView tv, boolean sel) {
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(50f);
            if (sel) {
                bg.setColor(0xFF1565C0);   
                tv.setTextColor(Color.WHITE);
            } else {
                bg.setColor(Color.WHITE);
                bg.setStroke(2, 0xFF1565C0);
                tv.setTextColor(0xFF1565C0);
            }
            tv.setBackground(bg);
        }

        @Override public int getItemCount() { return genres.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tv;
            VH(View v) {
                super(v);
                tv = v.findViewById(R.id.tvGenreName);
            }
        }
    }
}

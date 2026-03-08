package com.bookmap.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookmap.app.R;
import com.bookmap.app.model.Club;

import java.util.List;

public class ClubAdapter extends RecyclerView.Adapter<ClubAdapter.ViewHolder> {

    private final List<Club> clubs;
    private OnClubClickListener listener;

    public interface OnClubClickListener {
        void onClubClick(Club club);
    }

    public ClubAdapter(List<Club> clubs, OnClubClickListener listener) {
        this.clubs = clubs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_club, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Club club = clubs.get(position);
        holder.tvName.setText(club.getName());
        holder.tvDescription.setText(club.getDescription());
        holder.tvType.setText(club.isPublic() ? "Publico" : "Privado");
        holder.tvMembers.setText(club.getMemberCount() + " membros");
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClubClick(club);
        });
    }

    @Override
    public int getItemCount() {
        return clubs.size();
    }

    public void updateData(List<Club> newClubs) {
        clubs.clear();
        clubs.addAll(newClubs);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvType, tvMembers;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvClubName);
            tvDescription = itemView.findViewById(R.id.tvClubDescription);
            tvType = itemView.findViewById(R.id.tvClubType);
            tvMembers = itemView.findViewById(R.id.tvClubMembers);
        }
    }
}

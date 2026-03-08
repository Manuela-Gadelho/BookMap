package com.bookmap.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookmap.app.R;
import com.bookmap.app.model.Event;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private final List<Event> events;

    public EventAdapter(List<Event> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.tvTitle.setText(event.getTitle());
        holder.tvDateTime.setText(event.getDateTime());
        holder.tvLocation.setText(event.getLocation());
        holder.tvDescription.setText(event.getDescription());
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void updateData(List<Event> newEvents) {
        events.clear();
        events.addAll(newEvents);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDateTime, tvLocation, tvDescription;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvDateTime = itemView.findViewById(R.id.tvEventDateTime);
            tvLocation = itemView.findViewById(R.id.tvEventLocation);
            tvDescription = itemView.findViewById(R.id.tvEventDescription);
        }
    }
}

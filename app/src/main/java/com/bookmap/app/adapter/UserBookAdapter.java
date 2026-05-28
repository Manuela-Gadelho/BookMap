package com.bookmap.app.adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bookmap.app.R;
import com.bookmap.app.model.UserBook;
import com.bookmap.app.util.PhotoHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import java.util.List;
import android.widget.ImageView;
public class UserBookAdapter extends RecyclerView.Adapter<UserBookAdapter.ViewHolder> {
    private final List<UserBook> userBooks;
    private OnUserBookClickListener listener;
    public interface OnUserBookClickListener {
        void onUserBookClick(UserBook userBook);
    }
    public UserBookAdapter(List<UserBook> userBooks, OnUserBookClickListener listener) {
        this.userBooks = userBooks;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserBook ub = userBooks.get(position);
        holder.tvTitle.setText(ub.getBookTitle());
        holder.tvAuthor.setText(ub.getBookAuthor());
        holder.tvGenre.setText(ub.getBookGenre());
        String statusLabel;
        switch (ub.getStatus()) {
            case "LENDO": statusLabel = "Lendo"; break;
            case "LIDO": statusLabel = "Lido"; break;
            case "QUERO_LER": statusLabel = "Quero Ler"; break;
            default: statusLabel = ub.getStatus(); break;
        }
        holder.tvStatus.setText(statusLabel);
        holder.tvStatus.setVisibility(View.VISIBLE);

        
        String isbn = ub.getBookIsbn();
        String coverPath = ub.getBookCoverPath();
        String fallbackAssetPath = PhotoHelper.getGenreAssetPath(ub.getBookGenre());

        if (coverPath != null && !coverPath.isEmpty()) {
            if (coverPath.startsWith("http") || coverPath.startsWith("asset:")) {
                String glidePath = coverPath.startsWith("asset:") ? coverPath.replaceFirst("^asset:", "file:///android_asset/") : coverPath;
                Glide.with(holder.itemView.getContext())
                    .load(glidePath)
                    .transform(new CenterCrop(), new RoundedCorners(8))
                    .error(Glide.with(holder.itemView.getContext()).load(fallbackAssetPath).transform(new CenterCrop(), new RoundedCorners(8)))
                    .into(holder.imgCover);
            } else {
                Glide.with(holder.itemView.getContext())
                    .load(new java.io.File(coverPath))
                    .transform(new CenterCrop(), new RoundedCorners(8))
                    .error(Glide.with(holder.itemView.getContext()).load(fallbackAssetPath).transform(new CenterCrop(), new RoundedCorners(8)))
                    .into(holder.imgCover);
            }
        } else if (isbn != null && !isbn.isEmpty()) {
            String url = "https://covers.openlibrary.org/b/isbn/" + isbn + "-M.jpg?default=false";
            Glide.with(holder.itemView.getContext())
                .load(url)
                .transform(new CenterCrop(), new RoundedCorners(8))
                .error(Glide.with(holder.itemView.getContext()).load(fallbackAssetPath).transform(new CenterCrop(), new RoundedCorners(8)))
                .into(holder.imgCover);
        } else {
            Glide.with(holder.itemView.getContext())
                .load(fallbackAssetPath)
                .transform(new CenterCrop(), new RoundedCorners(8))
                .into(holder.imgCover);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onUserBookClick(ub);
        });
    }
    @Override
    public int getItemCount() {
        return userBooks.size();
    }
    public void updateData(List<UserBook> newBooks) {
        userBooks.clear();
        userBooks.addAll(newBooks);
        notifyDataSetChanged();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvGenre, tvStatus;
        ImageView imgCover;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvBookTitle);
            tvAuthor = itemView.findViewById(R.id.tvBookAuthor);
            tvGenre = itemView.findViewById(R.id.tvBookGenre);
            tvStatus = itemView.findViewById(R.id.tvBookStatus);
            imgCover = itemView.findViewById(R.id.imgBookCover);
        }
    }
}

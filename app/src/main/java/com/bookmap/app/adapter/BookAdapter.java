package com.bookmap.app.adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bookmap.app.R;
import com.bookmap.app.model.Book;
import com.bookmap.app.util.PhotoHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import java.util.List;
import android.widget.ImageView;
public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {
    private final List<Book> books;
    private OnBookClickListener listener;
    public interface OnBookClickListener {
        void onBookClick(Book book);
    }
    public BookAdapter(List<Book> books, OnBookClickListener listener) {
        this.books = books;
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
        Book book = books.get(position);
        holder.tvTitle.setText(book.getTitle());
        holder.tvAuthor.setText(book.getAuthor());
        holder.tvGenre.setText(book.getGenre());
        holder.tvStatus.setVisibility(View.GONE);
        
        // Load cover dynamically
        String isbn = book.getIsbn();
        String coverPath = book.getCoverPath();
        String fallbackAssetPath = PhotoHelper.getGenreAssetPath(book.getGenre());
        
        if (coverPath != null && !coverPath.isEmpty()) {
            if (coverPath.startsWith("http") || coverPath.startsWith("asset:")) {
                Glide.with(holder.itemView.getContext())
                    .load(coverPath)
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
            if (listener != null) listener.onBookClick(book);
        });
    }
    @Override
    public int getItemCount() {
        return books.size();
    }
    public void updateData(List<Book> newBooks) {
        books.clear();
        books.addAll(newBooks);
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

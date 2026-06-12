package com.bookmap.app.database;

import android.content.Context;
import android.util.Log;
import android.database.sqlite.SQLiteDatabase;
import com.bookmap.app.model.Book;
import com.bookmap.app.model.Club;
import com.bookmap.app.model.ClubMember;
import com.bookmap.app.model.Review;
import com.bookmap.app.model.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseSyncHelper {
    private static final String TAG = "FirebaseSyncHelper";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_BOOKS = "books";
    private static final String COLLECTION_REVIEWS = "reviews";
    private static final String COLLECTION_CLUBS = "clubs";
    private static final String COLLECTION_EVENTS = "events";
    private static final String COLLECTION_USER_BOOKS = "user_books";
    private static final String COLLECTION_FOLLOWERS = "followers";
    private final FirebaseFirestore firestore;
    private final DatabaseHelper dbHelper;
    private boolean isFirebaseAvailable;
    private static FirebaseSyncHelper instance;
    private ListenerRegistration usersListener;

    public static synchronized FirebaseSyncHelper getInstance(Context context) {
        if (instance == null) {
            instance = new FirebaseSyncHelper(context);
        }
        return instance;
    }

    private FirebaseSyncHelper(Context context) {
        this.dbHelper = DatabaseHelper.getInstance(context);
        FirebaseFirestore fs;
        try {
            fs = FirebaseFirestore.getInstance();
            isFirebaseAvailable = true;
        } catch (Exception e) {
            Log.w(TAG, "Firebase not configured, running in offline-only mode", e);
            fs = null;
            isFirebaseAvailable = false;
        }
        this.firestore = fs;
    }

    public boolean isFirebaseAvailable() {
        return isFirebaseAvailable && firestore != null;
    }

    public void syncUserToCloud(User user) {
        if (!isFirebaseAvailable())
            return;
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());
        userData.put("bio", user.getBio() != null ? user.getBio() : "");
        userData.put("favorite_genres", user.getFavoriteGenres() != null ? user.getFavoriteGenres() : "");
        userData.put("role", user.getRole());
        userData.put("latitude", user.getLatitude());
        userData.put("longitude", user.getLongitude());
        userData.put("language", user.getLanguage() != null ? user.getLanguage() : "pt_BR");
        userData.put("photo_path", user.getPhotoPath() != null ? user.getPhotoPath() : "");
        userData.put("is_private", user.isPrivate());
        firestore.collection(COLLECTION_USERS)
                .document(String.valueOf(user.getId()))
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User synced to cloud: " + user.getName()))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to sync user to cloud", e));
    }

    public void syncBookToCloud(Book book) {
        if (!isFirebaseAvailable())
            return;
        Map<String, Object> bookData = new HashMap<>();
        bookData.put("title", book.getTitle());
        bookData.put("author", book.getAuthor());
        bookData.put("synopsis", book.getSynopsis() != null ? book.getSynopsis() : "");
        bookData.put("genre", book.getGenre() != null ? book.getGenre() : "");
        bookData.put("isbn", book.getIsbn() != null ? book.getIsbn() : "");
        bookData.put("cover_path", book.getCoverPath() != null ? book.getCoverPath() : "");
        bookData.put("creator_id", book.getCreatorId());
        firestore.collection(COLLECTION_BOOKS)
                .document(String.valueOf(book.getId()))
                .set(bookData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Book synced to cloud: " + book.getTitle()))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to sync book to cloud", e));
    }

    public void deleteBookFromCloud(long bookId) {
        if (!isFirebaseAvailable())
            return;
        firestore.collection(COLLECTION_BOOKS)
                .document(String.valueOf(bookId))
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Book deleted from cloud"))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to delete book from cloud", e));
    }

    public void syncReviewToCloud(Review review) {
        if (!isFirebaseAvailable())
            return;
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("user_id", review.getUserId());
        reviewData.put("book_id", review.getBookId());
        reviewData.put("text", review.getText());
        reviewData.put("rating", review.getRating());
        reviewData.put("user_name", review.getUserName() != null ? review.getUserName() : "");
        firestore.collection(COLLECTION_REVIEWS)
                .document(String.valueOf(review.getId()))
                .set(reviewData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Review synced to cloud"))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to sync review to cloud", e));
    }

    public void syncClubToCloud(Club club) {
        if (!isFirebaseAvailable())
            return;
        Map<String, Object> clubData = new HashMap<>();
        clubData.put("name", club.getName());
        clubData.put("description", club.getDescription() != null ? club.getDescription() : "");
        clubData.put("is_public", club.isPublic());
        clubData.put("creator_id", club.getCreatorId());
        clubData.put("banner_path", club.getBannerPath() != null ? club.getBannerPath() : "");
        firestore.collection(COLLECTION_CLUBS)
                .document(String.valueOf(club.getId()))
                .set(clubData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Club synced to cloud: " + club.getName()))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to sync club to cloud", e));
    }

    public void deleteClubFromCloud(long clubId) {
        if (!isFirebaseAvailable())
            return;
        firestore.collection(COLLECTION_CLUBS)
                .document(String.valueOf(clubId))
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Club deleted from cloud: " + clubId))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to delete club from cloud", e));
    }

    public void removeClubMemberFromCloud(long clubId, long userId) {
        if (!isFirebaseAvailable())
            return;
        String docId = clubId + "_" + userId;
        firestore.collection("club_members")
                .document(docId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "ClubMember deleted from cloud"))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to delete ClubMember", e));
    }

    public void syncUserBookToCloud(long userId, long bookId, String status, int progress) {
        if (!isFirebaseAvailable())
            return;
        Map<String, Object> data = new HashMap<>();
        data.put("user_id", userId);
        data.put("book_id", bookId);
        data.put("status", status);
        data.put("progress", progress);
        String docId = userId + "_" + bookId;
        firestore.collection(COLLECTION_USER_BOOKS)
                .document(docId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "UserBook synced to cloud"))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to sync user book to cloud", e));
    }
    
    public void syncFollowToCloud(long followerId, long followedId, boolean isFollowing, String status) {
        if (!isFirebaseAvailable()) return;
        String docId = followerId + "_" + followedId;
        if (isFollowing) {
            Map<String, Object> data = new HashMap<>();
            data.put("follower_id", followerId);
            data.put("followed_id", followedId);
            data.put("status", status);
            data.put("timestamp", FieldValue.serverTimestamp());
            firestore.collection(COLLECTION_FOLLOWERS).document(docId).set(data, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Follow synced to cloud"))
                    .addOnFailureListener(e -> Log.w(TAG, "Failed to sync follow to cloud", e));
        } else {
            firestore.collection(COLLECTION_FOLLOWERS).document(docId).delete()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Unfollow synced to cloud"))
                    .addOnFailureListener(e -> Log.w(TAG, "Failed to sync unfollow to cloud", e));
        }
    }

    public void syncAllDataToCloud() {
        if (!isFirebaseAvailable())
            return;
        List<User> users = dbHelper.getAllUsers();
        for (User user : users) {
            syncUserToCloud(user);
        }
        List<Book> books = dbHelper.getAllBooks();
        for (Book book : books) {
            syncBookToCloud(book);
        }
        List<Club> clubs = dbHelper.getAllClubs();
        for (Club club : clubs) {
            syncClubToCloud(club);
        }
        Log.d(TAG, "Full sync initiated");
    }

    public void pullBooksFromCloud(SyncCallback callback) {
        if (!isFirebaseAvailable()) {
            if (callback != null)
                callback.onComplete(false);
            return;
        }
        firestore.collection(COLLECTION_BOOKS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        String title = doc.getString("title");
                        String author = doc.getString("author");
                        String synopsis = doc.getString("synopsis");
                        String genre = doc.getString("genre");
                        String isbn = doc.getString("isbn");
                        String coverPath = doc.getString("cover_path");
                        Long creatorId = doc.getLong("creator_id");
                        if (title != null && author != null) {
                            List<Book> existing = dbHelper.searchBooks(title);
                            boolean found = false;
                            for (Book b : existing) {
                                if (b.getTitle().equals(title) && b.getAuthor().equals(author)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                dbHelper.insertBook(title, author,
                                        synopsis != null ? synopsis : "",
                                        coverPath != null ? coverPath : "",
                                        genre != null ? genre : "",
                                        isbn != null ? isbn : "",
                                        creatorId != null ? creatorId : 0L);
                            }
                        }
                    }
                    if (callback != null)
                        callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to pull books from cloud", e);
                    if (callback != null)
                        callback.onComplete(false);
                });
    }

    public void updateUserLocationInCloud(long userId, double latitude, double longitude) {
        if (!isFirebaseAvailable())
            return;
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);
        firestore.collection(COLLECTION_USERS)
                .document(String.valueOf(userId))
                .set(locationData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User location updated in cloud"))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to update user location in cloud", e));
    }

    public void pullUsersFromCloud(SyncCallback callback) {
        if (!isFirebaseAvailable()) {
            if (callback != null)
                callback.onComplete(false);
            return;
        }
        firestore.collection(COLLECTION_USERS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        try {
                            String docId = doc.getId();
                            long id;
                            try {
                                id = Long.parseLong(docId);
                            } catch (NumberFormatException e) {
                                continue;
                            }
                            String name = doc.getString("name");
                            String email = doc.getString("email");
                            String bio = doc.getString("bio");
                            String favoriteGenres = doc.getString("favorite_genres");
                            String role = doc.getString("role");
                            Double latitude = doc.getDouble("latitude");
                            Double longitude = doc.getDouble("longitude");
                            String language = doc.getString("language");
                            String photoPath = doc.getString("photo_path");
                            Boolean isPrivateObj = doc.getBoolean("is_private");
                            boolean isPrivate = isPrivateObj != null ? isPrivateObj : false;
                            if (name != null && email != null) {
                                dbHelper.insertUserWithId(id, name, email, "google_or_synced_pass",
                                        bio != null ? bio : "",
                                        photoPath != null ? photoPath : "",
                                        favoriteGenres != null ? favoriteGenres : "",
                                        role != null ? role : "READER",
                                        latitude != null ? latitude : 0.0,
                                        longitude != null ? longitude : 0.0,
                                        language != null ? language : "pt_BR",
                                        isPrivate);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing synced user", e);
                        }
                    }
                    if (callback != null)
                        callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to pull users from cloud", e);
                    if (callback != null)
                        callback.onComplete(false);
                });
    }

    public void pullClubsFromCloud(SyncCallback callback) {
        if (!isFirebaseAvailable()) {
            if (callback != null)
                callback.onComplete(false);
            return;
        }
        firestore.collection(COLLECTION_CLUBS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        try {
                            String docId = doc.getId();
                            long id;
                            try {
                                id = Long.parseLong(docId);
                            } catch (NumberFormatException e) {
                                continue;
                            }
                            String name = doc.getString("name");
                            String description = doc.getString("description");
                            Boolean isPublic = doc.getBoolean("is_public");
                            Long creatorId = doc.getLong("creator_id");
                            String bannerPath = doc.getString("banner_path");
                            if (name != null && creatorId != null) {
                                dbHelper.insertClubWithId(id, name,
                                        description != null ? description : "",
                                        isPublic != null ? isPublic : true,
                                        creatorId,
                                        bannerPath != null ? bannerPath : "");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing synced club", e);
                        }
                    }
                    if (callback != null)
                        callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to pull clubs from cloud", e);
                    if (callback != null)
                        callback.onComplete(false);
                });
    }

    public void syncClubMemberToCloud(long clubId, long userId, String role, String status) {
        if (!isFirebaseAvailable())
            return;
        Map<String, Object> memberData = new HashMap<>();
        memberData.put("club_id", clubId);
        memberData.put("user_id", userId);
        memberData.put("role", role);
        memberData.put("status", status);

        String docId = clubId + "_" + userId;
        firestore.collection("club_members")
                .document(docId)
                .set(memberData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Club member synced to cloud"))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to sync club member to cloud", e));
    }

    public void pullClubMembersFromCloud(long clubId, SyncCallback callback) {
        if (!isFirebaseAvailable()) {
            if (callback != null)
                callback.onComplete(false);
            return;
        }
        firestore.collection("club_members")
                .whereEqualTo("club_id", clubId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        try {
                            Long uId = doc.getLong("user_id");
                            String role = doc.getString("role");
                            String status = doc.getString("status");
                            if (uId != null && role != null && status != null) {
                                ClubMember existing = dbHelper.getClubMember(clubId, uId);
                                if (existing == null) {
                                    dbHelper.addClubMember(clubId, uId, role, status);
                                } else {
                                    dbHelper.updateMemberStatus(clubId, uId, status);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing club member sync", e);
                        }
                    }
                    if (callback != null)
                        callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to pull club members", e);
                    if (callback != null)
                        callback.onComplete(false);
                });
    }

    

    public void pushMessageToCloud(com.bookmap.app.model.Message message, SyncCallback callback) {
        if (!isFirebaseAvailable()) {
            if (callback != null) callback.onComplete(false);
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", message.getId());
        data.put("sender_id", message.getSenderId());
        data.put("receiver_id", message.getReceiverId());
        data.put("content", message.getContent());
        data.put("timestamp", message.getTimestamp());
        data.put("is_read", message.isRead());
        data.put("deletedFor", new java.util.ArrayList<Long>()); 

        firestore.collection("messages")
                .document(message.getId())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onComplete(true); })
                .addOnFailureListener(e -> { if (callback != null) callback.onComplete(false); });
    }

    public void softDeleteMessageFromCloud(String messageId, long userId) {
        if (!isFirebaseAvailable()) return;
        firestore.collection("messages").document(messageId)
                .update("deletedFor", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Message soft deleted for user " + userId))
                .addOnFailureListener(e -> Log.e(TAG, "Failed soft delete message", e));
    }

    public void pullMessagesFromCloud(long myUserId, SyncCallback callback) {
        if (!isFirebaseAvailable()) {
            if (callback != null) callback.onComplete(false);
            return;
        }

        firestore.collection("messages")
                .whereEqualTo("receiver_id", myUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        try {
                            List<Number> deletedFor = (List<Number>) doc.get("deletedFor");
                            boolean isDeleted = false;
                            if (deletedFor != null) {
                                for (Number n : deletedFor) {
                                    if (n.longValue() == myUserId) {
                                        isDeleted = true;
                                        break;
                                    }
                                }
                            }
                            if (isDeleted) {
                                dbHelper.deleteMessageLocal(doc.getId());
                                continue;
                            }

                            String id = doc.getString("id");
                            Long senderId = doc.getLong("sender_id");
                            Long receiverId = doc.getLong("receiver_id");
                            String content = doc.getString("content");
                            String timestamp = doc.getString("timestamp");
                            Boolean isRead = doc.getBoolean("is_read");

                            if (id != null && senderId != null && receiverId != null && content != null && timestamp != null) {
                                com.bookmap.app.model.Message msg = new com.bookmap.app.model.Message(
                                        id, senderId, receiverId, content, timestamp, isRead != null && isRead
                                );
                                dbHelper.insertMessage(msg);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing message sync", e);
                        }
                    }
                    if (callback != null) callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to pull messages", e);
                    if (callback != null) callback.onComplete(false);
                });
    }

    
    public void pushReviewLike(long reviewId, long userId, boolean isLike) {
        if (!isFirebaseAvailable()) return;
        String docId = reviewId + "_" + userId;
        if (isLike) {
            Map<String, Object> data = new HashMap<>();
            data.put("reviewId", reviewId);
            data.put("userId", userId);
            data.put("timestamp", FieldValue.serverTimestamp());
            firestore.collection("review_likes").document(docId).set(data, SetOptions.merge());
        } else {
            firestore.collection("review_likes").document(docId).delete();
        }
    }

    public void pushReviewComment(com.bookmap.app.model.ReviewComment comment, SyncCallback callback) {
        if (!isFirebaseAvailable()) {
            if (callback != null) callback.onComplete(false);
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", comment.getId());
        data.put("reviewId", comment.getReviewId());
        data.put("userId", comment.getUserId());
        data.put("text", comment.getText());
        data.put("timestamp", comment.getTimestamp());
        
        firestore.collection("review_comments").document(String.valueOf(comment.getId()))
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> { if (callback != null) callback.onComplete(true); })
                .addOnFailureListener(e -> { if (callback != null) callback.onComplete(false); });
    }

    public void softDeleteReviewCommentFromCloud(long commentId) {
        if (!isFirebaseAvailable()) return;
        firestore.collection("review_comments").document(String.valueOf(commentId)).delete();
    }

    public void pullReviewInteractions(SyncCallback callback) {
        if (!isFirebaseAvailable()) {
            if (callback != null) callback.onComplete(false);
            return;
        }
        
        firestore.collection("review_likes").get().addOnSuccessListener(querySnapshot -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_REVIEW_LIKES);
            for (DocumentSnapshot doc : querySnapshot) {
                Long reviewId = doc.getLong("reviewId");
                Long userId = doc.getLong("userId");
                if (reviewId != null && userId != null) {
                    dbHelper.insertReviewLike(reviewId, userId);
                }
            }
        });

        firestore.collection("review_comments").get().addOnSuccessListener(querySnapshot -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_REVIEW_COMMENTS);
            for (DocumentSnapshot doc : querySnapshot) {
                Long id = doc.getLong("id");
                Long reviewId = doc.getLong("reviewId");
                Long userId = doc.getLong("userId");
                String text = doc.getString("text");
                String timestamp = doc.getString("timestamp");
                if (id != null && reviewId != null && userId != null && text != null && timestamp != null) {
                    dbHelper.insertReviewCommentSync(id, reviewId, userId, text, timestamp);
                }
            }
            if (callback != null) callback.onComplete(true);
        }).addOnFailureListener(e -> {
            if (callback != null) callback.onComplete(false);
        });
    }

    public void startListeningToUsers(SyncCallback callback) {
        if (!isFirebaseAvailable()) {
            if (callback != null) callback.onComplete(false);
            return;
        }
        if (usersListener != null) {
            return; // Already listening
        }
        usersListener = firestore.collection(COLLECTION_USERS)
                .addSnapshotListener((querySnapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Users listen failed.", e);
                        if (callback != null) callback.onComplete(false);
                        return;
                    }
                    if (querySnapshot != null) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            try {
                                String docId = doc.getId();
                                long id;
                                try {
                                    id = Long.parseLong(docId);
                                } catch (NumberFormatException ex) {
                                    continue;
                                }
                                String name = doc.getString("name");
                                String email = doc.getString("email");
                                String bio = doc.getString("bio");
                                String favoriteGenres = doc.getString("favorite_genres");
                                String role = doc.getString("role");
                                Double latitude = doc.getDouble("latitude");
                                Double longitude = doc.getDouble("longitude");
                                String language = doc.getString("language");
                                String photoPath = doc.getString("photo_path");
                                Boolean isPrivateObj = doc.getBoolean("is_private");
                                boolean isPrivate = isPrivateObj != null ? isPrivateObj : false;
                                if (name != null && email != null) {
                                    dbHelper.insertUserWithId(id, name, email, "google_or_synced_pass",
                                            bio != null ? bio : "",
                                            photoPath != null ? photoPath : "",
                                            favoriteGenres != null ? favoriteGenres : "",
                                            role != null ? role : "READER",
                                            latitude != null ? latitude : 0.0,
                                            longitude != null ? longitude : 0.0,
                                            language != null ? language : "pt_BR",
                                            isPrivate);
                                }
                            } catch (Exception ex) {
                                Log.e(TAG, "Error parsing synced user", ex);
                            }
                        }
                        if (callback != null) {
                            callback.onComplete(true);
                        }
                    }
                });
    }

    public void stopListeningToUsers() {
        if (usersListener != null) {
            usersListener.remove();
            usersListener = null;
        }
    }

    public interface SyncCallback {
        void onComplete(boolean success);
    }
}

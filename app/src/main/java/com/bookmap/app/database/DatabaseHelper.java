package com.bookmap.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bookmap.app.model.Book;
import com.bookmap.app.model.Club;
import com.bookmap.app.model.ClubMember;
import com.bookmap.app.model.Event;
import com.bookmap.app.model.Report;
import com.bookmap.app.model.Review;
import com.bookmap.app.model.User;
import com.bookmap.app.model.UserBook;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite Database Helper for BookMap.
 * Local-First architecture with modular table structure for offline performance.
 * Prepared for optional future server synchronization.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bookmap.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_BOOKS = "books";
    public static final String TABLE_USER_BOOKS = "user_books";
    public static final String TABLE_REVIEWS = "reviews";
    public static final String TABLE_CLUBS = "clubs";
    public static final String TABLE_CLUB_MEMBERS = "club_members";
    public static final String TABLE_EVENTS = "events";
    public static final String TABLE_REPORTS = "reports";

    // Singleton instance
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users table
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE, " +
                "password_hash TEXT NOT NULL, " +
                "bio TEXT DEFAULT '', " +
                "photo_path TEXT DEFAULT '', " +
                "favorite_genres TEXT DEFAULT '', " +
                "role TEXT DEFAULT 'READER', " +
                "latitude REAL DEFAULT 0.0, " +
                "longitude REAL DEFAULT 0.0, " +
                "language TEXT DEFAULT 'Portugues', " +
                "created_at TEXT DEFAULT (datetime('now')))");

        // Books table
        db.execSQL("CREATE TABLE " + TABLE_BOOKS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "author TEXT NOT NULL, " +
                "synopsis TEXT DEFAULT '', " +
                "cover_path TEXT DEFAULT '', " +
                "genre TEXT DEFAULT '', " +
                "isbn TEXT DEFAULT '', " +
                "created_at TEXT DEFAULT (datetime('now')))");

        // User-Books relationship (Virtual Shelf)
        db.execSQL("CREATE TABLE " + TABLE_USER_BOOKS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "book_id INTEGER NOT NULL, " +
                "status TEXT NOT NULL DEFAULT 'QUERO_LER', " +
                "progress INTEGER DEFAULT 0, " +
                "created_at TEXT DEFAULT (datetime('now')), " +
                "FOREIGN KEY (user_id) REFERENCES " + TABLE_USERS + "(id), " +
                "FOREIGN KEY (book_id) REFERENCES " + TABLE_BOOKS + "(id), " +
                "UNIQUE(user_id, book_id))");

        // Reviews table (Constraint 1: rating is mandatory with review)
        db.execSQL("CREATE TABLE " + TABLE_REVIEWS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "book_id INTEGER NOT NULL, " +
                "text TEXT NOT NULL, " +
                "rating INTEGER NOT NULL CHECK(rating >= 1 AND rating <= 5), " +
                "created_at TEXT DEFAULT (datetime('now')), " +
                "FOREIGN KEY (user_id) REFERENCES " + TABLE_USERS + "(id), " +
                "FOREIGN KEY (book_id) REFERENCES " + TABLE_BOOKS + "(id))");

        // Clubs table
        db.execSQL("CREATE TABLE " + TABLE_CLUBS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "description TEXT DEFAULT '', " +
                "is_public INTEGER DEFAULT 1, " +
                "creator_id INTEGER NOT NULL, " +
                "banner_path TEXT DEFAULT '', " +
                "created_at TEXT DEFAULT (datetime('now')), " +
                "FOREIGN KEY (creator_id) REFERENCES " + TABLE_USERS + "(id))");

        // Club Members table
        db.execSQL("CREATE TABLE " + TABLE_CLUB_MEMBERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "club_id INTEGER NOT NULL, " +
                "user_id INTEGER NOT NULL, " +
                "role TEXT DEFAULT 'MEMBER', " +
                "status TEXT DEFAULT 'PENDING', " +
                "joined_at TEXT DEFAULT (datetime('now')), " +
                "FOREIGN KEY (club_id) REFERENCES " + TABLE_CLUBS + "(id), " +
                "FOREIGN KEY (user_id) REFERENCES " + TABLE_USERS + "(id), " +
                "UNIQUE(club_id, user_id))");

        // Events table
        db.execSQL("CREATE TABLE " + TABLE_EVENTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "club_id INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "description TEXT DEFAULT '', " +
                "date_time TEXT NOT NULL, " +
                "location TEXT DEFAULT '', " +
                "book_id INTEGER, " +
                "created_by INTEGER NOT NULL, " +
                "created_at TEXT DEFAULT (datetime('now')), " +
                "FOREIGN KEY (club_id) REFERENCES " + TABLE_CLUBS + "(id), " +
                "FOREIGN KEY (book_id) REFERENCES " + TABLE_BOOKS + "(id), " +
                "FOREIGN KEY (created_by) REFERENCES " + TABLE_USERS + "(id))");

        // Reports table
        db.execSQL("CREATE TABLE " + TABLE_REPORTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "reporter_id INTEGER NOT NULL, " +
                "reported_user_id INTEGER, " +
                "reported_content_id INTEGER, " +
                "content_type TEXT DEFAULT '', " +
                "reason TEXT NOT NULL, " +
                "status TEXT DEFAULT 'PENDING', " +
                "created_at TEXT DEFAULT (datetime('now')), " +
                "FOREIGN KEY (reporter_id) REFERENCES " + TABLE_USERS + "(id), " +
                "FOREIGN KEY (reported_user_id) REFERENCES " + TABLE_USERS + "(id))");

        // Seed some sample data
        seedData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLUB_MEMBERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLUBS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    private void seedData(SQLiteDatabase db) {
        // Sample books
        insertBookDirect(db, "Engenharia de Software", "Ian Sommerville",
                "A decima edicao de Engenharia de Software, um classico da area, foi totalmente atualizada e reestruturada para refletir as mudancas tecnologicas mais recentes.",
                "", "Tecnologia", "978-8543024974");
        insertBookDirect(db, "O Senhor dos Aneis: A Sociedade do Anel", "J.R.R. Tolkien",
                "A primeira parte da trilogia O Senhor dos Aneis, que conta a historia do hobbit Frodo e a Sociedade formada para destruir o Um Anel.",
                "", "Fantasia", "978-8595084742");
        insertBookDirect(db, "Dom Casmurro", "Machado de Assis",
                "Um dos maiores classicos da literatura brasileira, narra a historia de Bentinho e Capitu.",
                "", "Literatura Brasileira", "978-8544001561");
        insertBookDirect(db, "A Hora da Estrela", "Clarice Lispector",
                "A historia de Macabea, uma jovem nordestina que vive no Rio de Janeiro.",
                "", "Literatura Brasileira", "978-8532511454");
        insertBookDirect(db, "O Iluminado", "Stephen King",
                "Jack Torrance aceita o cargo de zelador de inverno no Hotel Overlook, isolado nas montanhas do Colorado.",
                "", "Terror", "978-8556510761");
        insertBookDirect(db, "1984", "George Orwell",
                "Uma distopia sobre um regime totalitario que controla todos os aspectos da vida.",
                "", "Ficcao Cientifica", "978-8535914849");
    }

    private void insertBookDirect(SQLiteDatabase db, String title, String author,
                                   String synopsis, String coverPath, String genre, String isbn) {
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("author", author);
        values.put("synopsis", synopsis);
        values.put("cover_path", coverPath);
        values.put("genre", genre);
        values.put("isbn", isbn);
        db.insert(TABLE_BOOKS, null, values);
    }

    // ==================== USER OPERATIONS ====================

    public long insertUser(String name, String email, String passwordHash,
                           String bio, String favoriteGenres, String role) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("email", email);
        values.put("password_hash", passwordHash);
        values.put("bio", bio);
        values.put("favorite_genres", favoriteGenres);
        values.put("role", role);
        return db.insert(TABLE_USERS, null, values);
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, "email = ?",
                new String[]{email}, null, null, null);
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    public User getUserById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        User user = null;
        if (cursor.moveToFirst()) {
            user = cursorToUser(cursor);
        }
        cursor.close();
        return user;
    }

    public boolean updateUser(User user) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", user.getName());
        values.put("bio", user.getBio());
        values.put("photo_path", user.getPhotoPath());
        values.put("favorite_genres", user.getFavoriteGenres());
        values.put("latitude", user.getLatitude());
        values.put("longitude", user.getLongitude());
        values.put("language", user.getLanguage());
        int rows = db.update(TABLE_USERS, values, "id = ?",
                new String[]{String.valueOf(user.getId())});
        return rows > 0;
    }

    public List<User> searchUsers(String query) {
        SQLiteDatabase db = getReadableDatabase();
        List<User> users = new ArrayList<>();
        Cursor cursor = db.query(TABLE_USERS, null,
                "name LIKE ? OR email LIKE ?",
                new String[]{"%" + query + "%", "%" + query + "%"},
                null, null, "name ASC");
        while (cursor.moveToNext()) {
            users.add(cursorToUser(cursor));
        }
        cursor.close();
        return users;
    }

    public List<User> getNearbyUsers(double lat, double lng, double radiusKm, String genreFilter, String languageFilter) {
        SQLiteDatabase db = getReadableDatabase();
        List<User> users = new ArrayList<>();
        // Simple distance approximation using lat/lng difference
        double latDiff = radiusKm / 111.0;
        double lngDiff = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)));

        String selection = "latitude BETWEEN ? AND ? AND longitude BETWEEN ? AND ?";
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(lat - latDiff));
        args.add(String.valueOf(lat + latDiff));
        args.add(String.valueOf(lng - lngDiff));
        args.add(String.valueOf(lng + lngDiff));

        if (genreFilter != null && !genreFilter.isEmpty()) {
            selection += " AND favorite_genres LIKE ?";
            args.add("%" + genreFilter + "%");
        }
        if (languageFilter != null && !languageFilter.isEmpty()) {
            selection += " AND language = ?";
            args.add(languageFilter);
        }

        Cursor cursor = db.query(TABLE_USERS, null, selection,
                args.toArray(new String[0]), null, null, null);
        while (cursor.moveToNext()) {
            users.add(cursorToUser(cursor));
        }
        cursor.close();
        return users;
    }

    public List<User> getAllUsers() {
        SQLiteDatabase db = getReadableDatabase();
        List<User> users = new ArrayList<>();
        Cursor cursor = db.query(TABLE_USERS, null, null, null, null, null, "name ASC");
        while (cursor.moveToNext()) {
            users.add(cursorToUser(cursor));
        }
        cursor.close();
        return users;
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        user.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
        user.setPasswordHash(cursor.getString(cursor.getColumnIndexOrThrow("password_hash")));
        user.setBio(cursor.getString(cursor.getColumnIndexOrThrow("bio")));
        user.setPhotoPath(cursor.getString(cursor.getColumnIndexOrThrow("photo_path")));
        user.setFavoriteGenres(cursor.getString(cursor.getColumnIndexOrThrow("favorite_genres")));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));
        user.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")));
        user.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")));
        user.setLanguage(cursor.getString(cursor.getColumnIndexOrThrow("language")));
        user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return user;
    }

    // ==================== BOOK OPERATIONS ====================

    public long insertBook(String title, String author, String synopsis,
                           String coverPath, String genre, String isbn) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("author", author);
        values.put("synopsis", synopsis);
        values.put("cover_path", coverPath);
        values.put("genre", genre);
        values.put("isbn", isbn);
        return db.insert(TABLE_BOOKS, null, values);
    }

    public Book getBookById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_BOOKS, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        Book book = null;
        if (cursor.moveToFirst()) {
            book = cursorToBook(cursor);
        }
        cursor.close();
        return book;
    }

    public List<Book> getAllBooks() {
        SQLiteDatabase db = getReadableDatabase();
        List<Book> books = new ArrayList<>();
        Cursor cursor = db.query(TABLE_BOOKS, null, null, null, null, null, "title ASC");
        while (cursor.moveToNext()) {
            books.add(cursorToBook(cursor));
        }
        cursor.close();
        return books;
    }

    public List<Book> searchBooks(String query) {
        SQLiteDatabase db = getReadableDatabase();
        List<Book> books = new ArrayList<>();
        Cursor cursor = db.query(TABLE_BOOKS, null,
                "title LIKE ? OR author LIKE ? OR genre LIKE ?",
                new String[]{"%" + query + "%", "%" + query + "%", "%" + query + "%"},
                null, null, "title ASC");
        while (cursor.moveToNext()) {
            books.add(cursorToBook(cursor));
        }
        cursor.close();
        return books;
    }

    private Book cursorToBook(Cursor cursor) {
        Book book = new Book();
        book.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        book.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        book.setAuthor(cursor.getString(cursor.getColumnIndexOrThrow("author")));
        book.setSynopsis(cursor.getString(cursor.getColumnIndexOrThrow("synopsis")));
        book.setCoverPath(cursor.getString(cursor.getColumnIndexOrThrow("cover_path")));
        book.setGenre(cursor.getString(cursor.getColumnIndexOrThrow("genre")));
        book.setIsbn(cursor.getString(cursor.getColumnIndexOrThrow("isbn")));
        book.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return book;
    }

    // ==================== USER-BOOK OPERATIONS ====================

    public long insertUserBook(long userId, long bookId, String status, int progress) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("book_id", bookId);
        values.put("status", status);
        values.put("progress", progress);
        return db.insertWithOnConflict(TABLE_USER_BOOKS, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean updateUserBookStatus(long userId, long bookId, String status, int progress) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        values.put("progress", progress);
        int rows = db.update(TABLE_USER_BOOKS, values,
                "user_id = ? AND book_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(bookId)});
        return rows > 0;
    }

    public List<UserBook> getUserBooksByStatus(long userId, String status) {
        SQLiteDatabase db = getReadableDatabase();
        List<UserBook> userBooks = new ArrayList<>();
        String selection = "ub.user_id = ?";
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(userId));

        if (status != null && !status.isEmpty()) {
            selection += " AND ub.status = ?";
            args.add(status);
        }

        Cursor cursor = db.rawQuery(
                "SELECT ub.*, b.title, b.author, b.genre, b.cover_path, b.synopsis " +
                        "FROM " + TABLE_USER_BOOKS + " ub " +
                        "INNER JOIN " + TABLE_BOOKS + " b ON ub.book_id = b.id " +
                        "WHERE " + selection + " ORDER BY ub.created_at DESC",
                args.toArray(new String[0]));

        while (cursor.moveToNext()) {
            userBooks.add(cursorToUserBook(cursor));
        }
        cursor.close();
        return userBooks;
    }

    public UserBook getUserBook(long userId, long bookId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT ub.*, b.title, b.author, b.genre, b.cover_path, b.synopsis " +
                        "FROM " + TABLE_USER_BOOKS + " ub " +
                        "INNER JOIN " + TABLE_BOOKS + " b ON ub.book_id = b.id " +
                        "WHERE ub.user_id = ? AND ub.book_id = ?",
                new String[]{String.valueOf(userId), String.valueOf(bookId)});
        UserBook userBook = null;
        if (cursor.moveToFirst()) {
            userBook = cursorToUserBook(cursor);
        }
        cursor.close();
        return userBook;
    }

    private UserBook cursorToUserBook(Cursor cursor) {
        UserBook ub = new UserBook();
        ub.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        ub.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
        ub.setBookId(cursor.getLong(cursor.getColumnIndexOrThrow("book_id")));
        ub.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        ub.setProgress(cursor.getInt(cursor.getColumnIndexOrThrow("progress")));
        ub.setBookTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        ub.setBookAuthor(cursor.getString(cursor.getColumnIndexOrThrow("author")));
        ub.setBookGenre(cursor.getString(cursor.getColumnIndexOrThrow("genre")));
        ub.setBookCoverPath(cursor.getString(cursor.getColumnIndexOrThrow("cover_path")));
        ub.setBookSynopsis(cursor.getString(cursor.getColumnIndexOrThrow("synopsis")));
        ub.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return ub;
    }

    // ==================== REVIEW OPERATIONS ====================

    /**
     * Insert a review. Constraint 1: rating is mandatory (enforced by DB CHECK constraint).
     * WriteReview <<include>> RateBook
     */
    public long insertReview(long userId, long bookId, String text, int rating) {
        if (text == null || text.trim().isEmpty()) {
            return -1; // Cannot rate without writing a review
        }
        if (rating < 1 || rating > 5) {
            return -1;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("book_id", bookId);
        values.put("text", text);
        values.put("rating", rating);
        return db.insert(TABLE_REVIEWS, null, values);
    }

    public List<Review> getBookReviews(long bookId) {
        SQLiteDatabase db = getReadableDatabase();
        List<Review> reviews = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT r.*, u.name as user_name FROM " + TABLE_REVIEWS + " r " +
                        "INNER JOIN " + TABLE_USERS + " u ON r.user_id = u.id " +
                        "WHERE r.book_id = ? ORDER BY r.created_at DESC",
                new String[]{String.valueOf(bookId)});
        while (cursor.moveToNext()) {
            reviews.add(cursorToReview(cursor));
        }
        cursor.close();
        return reviews;
    }

    public double getBookAverageRating(long bookId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT AVG(rating) as avg_rating, COUNT(*) as count FROM " + TABLE_REVIEWS +
                        " WHERE book_id = ?",
                new String[]{String.valueOf(bookId)});
        double avg = 0;
        if (cursor.moveToFirst()) {
            avg = cursor.getDouble(cursor.getColumnIndexOrThrow("avg_rating"));
        }
        cursor.close();
        return avg;
    }

    public int getBookReviewCount(long bookId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) as count FROM " + TABLE_REVIEWS + " WHERE book_id = ?",
                new String[]{String.valueOf(bookId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
        }
        cursor.close();
        return count;
    }

    private Review cursorToReview(Cursor cursor) {
        Review review = new Review();
        review.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        review.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
        review.setBookId(cursor.getLong(cursor.getColumnIndexOrThrow("book_id")));
        review.setText(cursor.getString(cursor.getColumnIndexOrThrow("text")));
        review.setRating(cursor.getInt(cursor.getColumnIndexOrThrow("rating")));
        review.setUserName(cursor.getString(cursor.getColumnIndexOrThrow("user_name")));
        review.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return review;
    }

    // ==================== CLUB OPERATIONS ====================

    public long insertClub(String name, String description, boolean isPublic, long creatorId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        values.put("is_public", isPublic ? 1 : 0);
        values.put("creator_id", creatorId);
        long clubId = db.insert(TABLE_CLUBS, null, values);

        // Auto-add creator as ORGANIZER
        if (clubId > 0) {
            ContentValues memberValues = new ContentValues();
            memberValues.put("club_id", clubId);
            memberValues.put("user_id", creatorId);
            memberValues.put("role", "ORGANIZER");
            memberValues.put("status", "APPROVED");
            db.insert(TABLE_CLUB_MEMBERS, null, memberValues);
        }
        return clubId;
    }

    public Club getClubById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CLUBS, null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        Club club = null;
        if (cursor.moveToFirst()) {
            club = cursorToClub(cursor);
        }
        cursor.close();
        return club;
    }

    public List<Club> getAllClubs() {
        SQLiteDatabase db = getReadableDatabase();
        List<Club> clubs = new ArrayList<>();
        Cursor cursor = db.query(TABLE_CLUBS, null, null, null, null, null, "name ASC");
        while (cursor.moveToNext()) {
            clubs.add(cursorToClub(cursor));
        }
        cursor.close();
        return clubs;
    }

    public List<Club> getUserClubs(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        List<Club> clubs = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT c.* FROM " + TABLE_CLUBS + " c " +
                        "INNER JOIN " + TABLE_CLUB_MEMBERS + " cm ON c.id = cm.club_id " +
                        "WHERE cm.user_id = ? AND cm.status = 'APPROVED' ORDER BY c.name ASC",
                new String[]{String.valueOf(userId)});
        while (cursor.moveToNext()) {
            clubs.add(cursorToClub(cursor));
        }
        cursor.close();
        return clubs;
    }

    public int getClubMemberCount(long clubId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) as count FROM " + TABLE_CLUB_MEMBERS +
                        " WHERE club_id = ? AND status = 'APPROVED'",
                new String[]{String.valueOf(clubId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
        }
        cursor.close();
        return count;
    }

    private Club cursorToClub(Cursor cursor) {
        Club club = new Club();
        club.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        club.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        club.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        club.setPublic(cursor.getInt(cursor.getColumnIndexOrThrow("is_public")) == 1);
        club.setCreatorId(cursor.getLong(cursor.getColumnIndexOrThrow("creator_id")));
        club.setBannerPath(cursor.getString(cursor.getColumnIndexOrThrow("banner_path")));
        club.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return club;
    }

    // ==================== CLUB MEMBER OPERATIONS ====================

    public long addClubMember(long clubId, long userId, String role, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("club_id", clubId);
        values.put("user_id", userId);
        values.put("role", role);
        values.put("status", status);
        return db.insertWithOnConflict(TABLE_CLUB_MEMBERS, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean updateMemberStatus(long clubId, long userId, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        int rows = db.update(TABLE_CLUB_MEMBERS, values,
                "club_id = ? AND user_id = ?",
                new String[]{String.valueOf(clubId), String.valueOf(userId)});
        return rows > 0;
    }

    public ClubMember getClubMember(long clubId, long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CLUB_MEMBERS, null,
                "club_id = ? AND user_id = ?",
                new String[]{String.valueOf(clubId), String.valueOf(userId)},
                null, null, null);
        ClubMember member = null;
        if (cursor.moveToFirst()) {
            member = cursorToClubMember(cursor);
        }
        cursor.close();
        return member;
    }

    public List<ClubMember> getClubMembers(long clubId) {
        SQLiteDatabase db = getReadableDatabase();
        List<ClubMember> members = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT cm.*, u.name as user_name, u.email as user_email FROM " +
                        TABLE_CLUB_MEMBERS + " cm INNER JOIN " + TABLE_USERS +
                        " u ON cm.user_id = u.id WHERE cm.club_id = ? ORDER BY cm.role, u.name",
                new String[]{String.valueOf(clubId)});
        while (cursor.moveToNext()) {
            members.add(cursorToClubMember(cursor));
        }
        cursor.close();
        return members;
    }

    private ClubMember cursorToClubMember(Cursor cursor) {
        ClubMember member = new ClubMember();
        member.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        member.setClubId(cursor.getLong(cursor.getColumnIndexOrThrow("club_id")));
        member.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
        member.setRole(cursor.getString(cursor.getColumnIndexOrThrow("role")));
        member.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        member.setJoinedAt(cursor.getString(cursor.getColumnIndexOrThrow("joined_at")));
        // Optional joined fields
        int nameIdx = cursor.getColumnIndex("user_name");
        if (nameIdx >= 0) {
            member.setUserName(cursor.getString(nameIdx));
        }
        int emailIdx = cursor.getColumnIndex("user_email");
        if (emailIdx >= 0) {
            member.setUserEmail(cursor.getString(emailIdx));
        }
        return member;
    }

    // ==================== EVENT OPERATIONS ====================

    public long insertEvent(long clubId, String title, String description,
                            String dateTime, String location, long bookId, long createdBy) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("club_id", clubId);
        values.put("title", title);
        values.put("description", description);
        values.put("date_time", dateTime);
        values.put("location", location);
        if (bookId > 0) {
            values.put("book_id", bookId);
        }
        values.put("created_by", createdBy);
        return db.insert(TABLE_EVENTS, null, values);
    }

    public List<Event> getClubEvents(long clubId) {
        SQLiteDatabase db = getReadableDatabase();
        List<Event> events = new ArrayList<>();
        Cursor cursor = db.query(TABLE_EVENTS, null, "club_id = ?",
                new String[]{String.valueOf(clubId)}, null, null, "date_time ASC");
        while (cursor.moveToNext()) {
            events.add(cursorToEvent(cursor));
        }
        cursor.close();
        return events;
    }

    private Event cursorToEvent(Cursor cursor) {
        Event event = new Event();
        event.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        event.setClubId(cursor.getLong(cursor.getColumnIndexOrThrow("club_id")));
        event.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
        event.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        event.setDateTime(cursor.getString(cursor.getColumnIndexOrThrow("date_time")));
        event.setLocation(cursor.getString(cursor.getColumnIndexOrThrow("location")));
        int bookIdIdx = cursor.getColumnIndex("book_id");
        if (bookIdIdx >= 0 && !cursor.isNull(bookIdIdx)) {
            event.setBookId(cursor.getLong(bookIdIdx));
        }
        event.setCreatedBy(cursor.getLong(cursor.getColumnIndexOrThrow("created_by")));
        event.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return event;
    }

    // ==================== REPORT OPERATIONS ====================

    public long insertReport(long reporterId, long reportedUserId,
                              long reportedContentId, String contentType, String reason) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("reporter_id", reporterId);
        values.put("reported_user_id", reportedUserId);
        if (reportedContentId > 0) {
            values.put("reported_content_id", reportedContentId);
        }
        values.put("content_type", contentType);
        values.put("reason", reason);
        return db.insert(TABLE_REPORTS, null, values);
    }

    public List<Report> getAllReports() {
        SQLiteDatabase db = getReadableDatabase();
        List<Report> reports = new ArrayList<>();
        Cursor cursor = db.query(TABLE_REPORTS, null, null, null, null, null, "created_at DESC");
        while (cursor.moveToNext()) {
            reports.add(cursorToReport(cursor));
        }
        cursor.close();
        return reports;
    }

    public boolean updateReportStatus(long reportId, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("status", status);
        int rows = db.update(TABLE_REPORTS, values, "id = ?",
                new String[]{String.valueOf(reportId)});
        return rows > 0;
    }

    private Report cursorToReport(Cursor cursor) {
        Report report = new Report();
        report.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
        report.setReporterId(cursor.getLong(cursor.getColumnIndexOrThrow("reporter_id")));
        int reportedUserIdx = cursor.getColumnIndex("reported_user_id");
        if (reportedUserIdx >= 0 && !cursor.isNull(reportedUserIdx)) {
            report.setReportedUserId(cursor.getLong(reportedUserIdx));
        }
        int reportedContentIdx = cursor.getColumnIndex("reported_content_id");
        if (reportedContentIdx >= 0 && !cursor.isNull(reportedContentIdx)) {
            report.setReportedContentId(cursor.getLong(reportedContentIdx));
        }
        report.setContentType(cursor.getString(cursor.getColumnIndexOrThrow("content_type")));
        report.setReason(cursor.getString(cursor.getColumnIndexOrThrow("reason")));
        report.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        report.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return report;
    }

    /**
     * Get the current reading book for a user (for map display).
     */
    public UserBook getCurrentReading(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT ub.*, b.title, b.author, b.genre, b.cover_path, b.synopsis " +
                        "FROM " + TABLE_USER_BOOKS + " ub " +
                        "INNER JOIN " + TABLE_BOOKS + " b ON ub.book_id = b.id " +
                        "WHERE ub.user_id = ? AND ub.status = 'LENDO' " +
                        "ORDER BY ub.created_at DESC LIMIT 1",
                new String[]{String.valueOf(userId)});
        UserBook userBook = null;
        if (cursor.moveToFirst()) {
            userBook = cursorToUserBook(cursor);
        }
        cursor.close();
        return userBook;
    }
}

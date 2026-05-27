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

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "bookmap.db";
    private static final int DATABASE_VERSION = 10;
    public static final String TABLE_USERS = "users";
    public static final String TABLE_BOOKS = "books";
    public static final String TABLE_USER_BOOKS = "user_books";
    public static final String TABLE_REVIEWS = "reviews";
    public static final String TABLE_REVIEW_LIKES = "review_likes";
    public static final String TABLE_REVIEW_COMMENTS = "review_comments";
    public static final String TABLE_CLUBS = "clubs";
    public static final String TABLE_CLUB_MEMBERS = "club_members";
    public static final String TABLE_EVENTS = "events";
    public static final String TABLE_REPORTS = "reports";
    public static final String TABLE_FOLLOWERS = "followers";
    public static final String TABLE_MESSAGES = "messages";
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public static synchronized void resetInstance() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
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
                "language TEXT DEFAULT 'Português', " +
                "created_at TEXT DEFAULT (datetime('now')))");
        db.execSQL("CREATE TABLE " + TABLE_BOOKS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "author TEXT NOT NULL, " +
                "synopsis TEXT DEFAULT '', " +
                "cover_path TEXT DEFAULT '', " +
                "genre TEXT DEFAULT '', " +
                "isbn TEXT DEFAULT '', " +
                "creator_id INTEGER DEFAULT 0, " +
                "created_at TEXT DEFAULT (datetime('now')))");
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
        db.execSQL("CREATE TABLE " + TABLE_REVIEWS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "book_id INTEGER NOT NULL, " +
                "text TEXT NOT NULL, " +
                "rating INTEGER NOT NULL CHECK(rating >= 1 AND rating <= 5), " +
                "created_at TEXT DEFAULT (datetime('now')), " +
                "FOREIGN KEY (user_id) REFERENCES " + TABLE_USERS + "(id), " +
                "FOREIGN KEY (book_id) REFERENCES " + TABLE_BOOKS + "(id))");
        db.execSQL("CREATE TABLE " + TABLE_REVIEW_LIKES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "review_id INTEGER NOT NULL, " +
                "user_id INTEGER NOT NULL, " +
                "is_like INTEGER DEFAULT 1, " +
                "FOREIGN KEY (review_id) REFERENCES " + TABLE_REVIEWS + "(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE, " +
                "UNIQUE(review_id, user_id))");
        db.execSQL("CREATE TABLE " + TABLE_REVIEW_COMMENTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "review_id INTEGER NOT NULL, " +
                "user_id INTEGER NOT NULL, " +
                "text TEXT NOT NULL, " +
                "timestamp TEXT DEFAULT (datetime('now')), " +
                "FOREIGN KEY (review_id) REFERENCES " + TABLE_REVIEWS + "(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES " + TABLE_USERS + "(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE " + TABLE_CLUBS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "description TEXT DEFAULT '', " +
                "is_public INTEGER DEFAULT 1, " +
                "creator_id INTEGER NOT NULL, " +
                "banner_path TEXT DEFAULT '', " +
                "created_at TEXT DEFAULT (datetime('now')), " +
                "FOREIGN KEY (creator_id) REFERENCES " + TABLE_USERS + "(id))");
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
        db.execSQL("CREATE TABLE " + TABLE_FOLLOWERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "follower_id INTEGER NOT NULL, " +
                "followed_id INTEGER NOT NULL, " +
                "created_at TEXT DEFAULT (datetime('now')), " +
                "FOREIGN KEY (follower_id) REFERENCES " + TABLE_USERS + "(id), " +
                "FOREIGN KEY (followed_id) REFERENCES " + TABLE_USERS + "(id), " +
                "UNIQUE(follower_id, followed_id))");
        db.execSQL("CREATE TABLE " + TABLE_MESSAGES + " (" +
                "id TEXT PRIMARY KEY, " +
                "sender_id INTEGER NOT NULL, " +
                "receiver_id INTEGER NOT NULL, " +
                "content TEXT NOT NULL, " +
                "timestamp TEXT NOT NULL, " +
                "is_read INTEGER DEFAULT 0, " +
                "FOREIGN KEY (sender_id) REFERENCES " + TABLE_USERS + "(id), " +
                "FOREIGN KEY (receiver_id) REFERENCES " + TABLE_USERS + "(id))");
        seedData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLLOWERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REPORTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLUB_MEMBERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLUBS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEW_COMMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEW_LIKES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    private void seedData(SQLiteDatabase db) {
        insertBookDirect(db, "O Conde de Monte Cristo", "Alexandre Dumas", "A história de um homem injustamente preso que busca vingança.", "asset:covers/çãoeaventura.jpg", "Ação e Aventura", "978-8537801605");
        insertBookDirect(db, "A Ilha do Tesouro", "Robert Louis Stevenson", "Uma aventura clássica sobre piratas e tesouros enterrados.", "asset:covers/real_cover_1.jpg", "Ação e Aventura", "978-8542217629");
        insertBookDirect(db, "As Aventuras de Robin Hood", "Howard Pyle", "O lendário herói que roubava dos ricos para dar aos pobres.", "asset:covers/çãoeaventura.jpg", "Ação e Aventura", "978-8537801834");
        insertBookDirect(db, "Vinte Mil Léguas Submarinas", "Júlio Verne", "A incrível jornada do Capitão Nemo em seu submarino Náutilus.", "asset:covers/real_cover_3.jpg", "Ação e Aventura", "978-8537801643");
        insertBookDirect(db, "O Último dos Moicanos", "James Fenimore Cooper", "A luta de uma tribo nativa americana durante a guerra franco-indígena.", "asset:covers/real_cover_4.jpg", "Ação e Aventura", "978-8520921005");

        insertBookDirect(db, "O Poder do Hábito", "Charles Duhigg", "Por que fazemos o que fazemos na vida e nos negócios.", "asset:covers/autoajuda.jpg", "Autoajuda", "978-8539004119");
        insertBookDirect(db, "Mindset", "Carol S. Dweck", "A nova psicologia do sucesso.", "asset:covers/real_cover_6.jpg", "Autoajuda", "978-8547001063");
        insertBookDirect(db, "Como Fazer Amigos e Influenciar Pessoas", "Dale Carnegie", "O guia clássico para melhorar relacionamentos pessoais e profissionais.", "asset:covers/autoajuda.jpg", "Autoajuda", "978-8504020360");
        insertBookDirect(db, "A Sutil Arte de Ligar o F*da-se", "Mark Manson", "Uma estratégia inusitada para uma vida melhor.", "asset:covers/autoajuda.jpg", "Autoajuda", "978-8551002490");
        insertBookDirect(db, "Hábitos Atômicos", "James Clear", "Um método fácil e comprovado de criar bons hábitos e se livrar dos maus.", "asset:covers/autoajuda.jpg", "Autoajuda", "978-8550805246");

        insertBookDirect(db, "Steve Jobs", "Walter Isaacson", "A biografia exclusiva de um dos maiores inovadores do mundo.", "asset:covers/real_cover_10.jpg", "Biografia", "978-8535919714");
        insertBookDirect(db, "Minha História", "Michelle Obama", "Um relato íntimo e poderoso da ex-primeira-dama dos Estados Unidos.", "asset:covers/biografia.jpg", "Biografia", "978-8547000646");
        insertBookDirect(db, "O Diário de Anne Frank", "Anne Frank", "O emocionante relato de uma jovem judia escondida durante a Segunda Guerra Mundial.", "asset:covers/biografia.jpg", "Biografia", "978-8501069509");
        insertBookDirect(db, "Long Walk to Freedom", "Nelson Mandela", "A autobiografia de um dos maiores líderes morais e políticos de nosso tempo.", "asset:covers/real_cover_13.jpg", "Biografia", "978-8535921861");
        insertBookDirect(db, "Leonardo da Vinci", "Walter Isaacson", "A biografia do maior gênio da história da humanidade.", "asset:covers/real_cover_14.jpg", "Biografia", "978-8551002575");

        insertBookDirect(db, "Laços de Família", "Clarice Lispector", "Contos que exploram a complexidade das relações humanas.", "asset:covers/real_cover_15.jpg", "Contos", "978-8532508126");
        insertBookDirect(db, "Ficções", "Jorge Luis Borges", "Uma coletânea das melhores histórias curtas e ensaios do autor argentino.", "asset:covers/contos.jpg", "Contos", "978-8535911305");
        insertBookDirect(db, "Contos de Machado de Assis", "Machado de Assis", "Os contos mais consagrados do maior escritor brasileiro.", "asset:covers/real_cover_17.jpg", "Contos", "978-8525411709");
        insertBookDirect(db, "Dublinenses", "James Joyce", "Quinze contos que retratam a vida em Dublin no início do século XX.", "asset:covers/contos.jpg", "Contos", "978-8535921502");
        insertBookDirect(db, "Histórias Extraordinárias", "Edgar Allan Poe", "As melhores histórias de terror e suspense do mestre do gênero.", "asset:covers/contos.jpg", "Contos", "978-8535911435");

        insertBookDirect(db, "A Sangue Frio", "Truman Capote", "A história real de um assassinato brutal que marcou a literatura de não-ficção.", "asset:covers/crimesverdadeiros.jpg", "Crimes Verdadeiros", "978-8535904338");
        insertBookDirect(db, "Zodiac", "Robert Graysmith", "A investigação sobre o famoso serial killer do zodíaco.", "asset:covers/real_cover_21.jpg", "Crimes Verdadeiros", "978-8542217636");
        insertBookDirect(db, "Helter Skelter", "Vincent Bugliosi", "A história verdadeira dos assassinatos de Charles Manson.", "asset:covers/real_cover_22.jpg", "Crimes Verdadeiros", "978-8501069516");
        insertBookDirect(db, "O Maníaco do Parque", "Ullisses Campbell", "A história do serial killer brasileiro Francisco de Assis Pereira.", "asset:covers/crimesverdadeiros.jpg", "Crimes Verdadeiros", "978-8520421005");
        insertBookDirect(db, "I'll Be Gone in the Dark", "Michelle McNamara", "A caçada ao assassino do Estado Dourado nos EUA.", "asset:covers/real_cover_24.jpg", "Crimes Verdadeiros", "978-8595084759");

        insertBookDirect(db, "A Última Crônica", "Rubem Braga", "Crônicas memoráveis de um dos maiores cronistas do Brasil.", "asset:covers/crnicas.jpg", "Crônicas", "978-8501069523");
        insertBookDirect(db, "As Cem Melhores Crônicas Brasileiras", "Joaquim Ferreira dos Santos", "Uma seleção das melhores crônicas da literatura nacional.", "asset:covers/real_cover_26.jpg", "Crônicas", "978-8539004126");
        insertBookDirect(db, "Comédias para Ler na Escola", "Luis Fernando Verissimo", "Crônicas humorísticas perfeitas para jovens leitores.", "asset:covers/real_cover_27.jpg", "Crônicas", "978-8539004133");
        insertBookDirect(db, "O Melhor das Comédias da Vida Privada", "Luis Fernando Verissimo", "As melhores crônicas sobre o dia a dia e o comportamento brasileiro.", "asset:covers/real_cover_28.jpg", "Crônicas", "978-8539004140");
        insertBookDirect(db, "Cem Crônicas", "Nelson Rodrigues", "A visão única e irônica do cotidiano carioca.", "asset:covers/crnicas.jpg", "Crônicas", "978-8535919721");

        insertBookDirect(db, "1984", "George Orwell", "Uma distopia sobre um regime totalitário que controla todos os aspectos da vida.", "asset:covers/real_cover_30.jpg", "Distopia", "978-8535914849");
        insertBookDirect(db, "Admirável Mundo Novo", "Aldous Huxley", "Um mundo futuro onde o sofrimento foi erradicado, mas a que custo?", "asset:covers/real_cover_31.jpg", "Distopia", "978-8525056009");
        insertBookDirect(db, "O Conto da Aia", "Margaret Atwood", "A história de Offred na opressiva República de Gilead.", "asset:covers/distopia.jpg", "Distopia", "978-8532520661");
        insertBookDirect(db, "Fahrenheit 451", "Ray Bradbury", "Uma sociedade onde os livros são proibidos e queimados.", "asset:covers/real_cover_33.jpg", "Distopia", "978-8525056016");
        insertBookDirect(db, "Laranja Mecânica", "Anthony Burgess", "A história de Alex e a ultraviolência em uma sociedade distópica.", "asset:covers/distopia.jpg", "Distopia", "978-8525056023");

        insertBookDirect(db, "Sobre a Brevidade da Vida", "Sêneca", "Um ensaio clássico sobre o valor do tempo e como vivemos.", "asset:covers/real_cover_35.jpg", "Ensaios", "978-8520421012");
        insertBookDirect(db, "A Morte de Ivan Ilitch", "Liev Tolstói", "Uma reflexão profunda sobre o sentido da vida e da morte.", "asset:covers/ensaios.jpg", "Ensaios", "978-8535919738");
        insertBookDirect(db, "Ensaios", "Michel de Montaigne", "Os ensaios fundamentais que deram origem ao gênero.", "asset:covers/real_cover_37.jpg", "Ensaios", "978-8535919745");
        insertBookDirect(db, "Cultura e Sociedade", "Zygmunt Bauman", "Uma análise crítica da sociedade contemporânea.", "asset:covers/ensaios.jpg", "Ensaios", "978-8535919752");
        insertBookDirect(db, "O Mito de Sísifo", "Albert Camus", "Um ensaio sobre o absurdo da existência e a rebelião.", "asset:covers/ensaios.jpg", "Ensaios", "978-8535919769");

        insertBookDirect(db, "O Senhor dos Anéis: A Sociedade do Anel", "J.R.R. Tolkien", "A jornada de Frodo Bolseiro para destruir o Um Anel.", "asset:covers/fantasia.jpg", "Fantasia", "978-8595084742");
        insertBookDirect(db, "Harry Potter e a Pedra Filosofal", "J.K. Rowling", "O início da jornada mágica do menino que sobreviveu.", "asset:covers/fantasia.jpg", "Fantasia", "978-8532511010");
        insertBookDirect(db, "O Nome do Vento", "Patrick Rothfuss", "A história do mago e músico Kvothe.", "asset:covers/fantasia.jpg", "Fantasia", "978-8599296573");
        insertBookDirect(db, "As Crônicas de Nárnia", "C.S. Lewis", "Aventura mágica de quatro irmãos em um mundo paralelo.", "asset:covers/real_cover_43.jpg", "Fantasia", "978-8578270698");
        insertBookDirect(db, "O Hobbit", "J.R.R. Tolkien", "A jornada de Bilbo Bolseiro para recuperar o tesouro dos anões.", "asset:covers/real_cover_44.jpg", "Fantasia", "978-8595084766");

        insertBookDirect(db, "Duna", "Frank Herbert", "Uma saga épica de política, religião e sobrevivência no planeta Arrakis.", "asset:covers/real_cover_45.jpg", "Ficção Científica", "978-8576573135");
        insertBookDirect(db, "Fundação", "Isaac Asimov", "O início do colapso do Império Galáctico e o plano de Hari Seldon.", "asset:covers/real_cover_46.jpg", "Ficção Científica", "978-8576573142");
        insertBookDirect(db, "Eu, Robô", "Isaac Asimov", "Contos fundamentais sobre a relação entre humanos e inteligência artificial.", "asset:covers/real_cover_47.jpg", "Ficção Científica", "978-8576573159");
        insertBookDirect(db, "Neuromancer", "William Gibson", "A obra seminal do cyberpunk e da exploração do ciberespaço.", "asset:covers/real_cover_48.jpg", "Ficção Científica", "978-8576573166");
        insertBookDirect(db, "Androides Sonham com Ovelhas Elétricas?", "Philip K. Dick", "O caçador de recompensas Rick Deckard e os androides fugitivos.", "asset:covers/ficodecientfica.jpg", "Ficção Científica", "978-8576573173");

        insertBookDirect(db, "O Mundo de Sofia", "Jostein Gaarder", "Uma introdução acessível e fascinante à história da filosofia.", "asset:covers/real_cover_50.jpg", "Filosofia", "978-8535919776");
        insertBookDirect(db, "Assim Falou Zaratustra", "Friedrich Nietzsche", "Um romance filosófico sobre a superação do homem.", "asset:covers/real_cover_51.jpg", "Filosofia", "978-8535919783");
        insertBookDirect(db, "Meditações", "Marco Aurélio", "Reflexões de um imperador estoico sobre a vida e o dever.", "asset:covers/real_cover_52.jpg", "Filosofia", "978-8535919790");
        insertBookDirect(db, "Ética a Nicômaco", "Aristóteles", "Tratado sobre a moralidade e a busca pela felicidade.", "asset:covers/real_cover_53.jpg", "Filosofia", "978-8535919806");
        insertBookDirect(db, "A República", "Platão", "Diálogo sobre a justiça e a estrutura da sociedade ideal.", "asset:covers/real_cover_54.jpg", "Filosofia", "978-8535919813");

        insertBookDirect(db, "Sapiens", "Yuval Noah Harari", "Uma breve história da humanidade.", "asset:covers/real_cover_55.jpg", "História", "978-8535919820");
        insertBookDirect(db, "1808", "Laurentino Gomes", "Como uma rainha louca e um príncipe medroso fugiram para o Brasil.", "asset:covers/real_cover_56.jpg", "História", "978-8535919837");
        insertBookDirect(db, "O Povo Brasileiro", "Darcy Ribeiro", "A formação cultural e histórica do Brasil.", "asset:covers/real_cover_57.jpg", "História", "978-8535919844");
        insertBookDirect(db, "Guns, Germs, and Steel", "Jared Diamond", "As influências geográficas e ambientais na história da humanidade.", "asset:covers/real_cover_58.jpg", "História", "978-8535919851");
        insertBookDirect(db, "A Segunda Guerra Mundial", "Antony Beevor", "O relato definitivo sobre o maior conflito da história humana.", "asset:covers/histria.jpg", "História", "978-8535919868");

        insertBookDirect(db, "It: A Coisa", "Stephen King", "O palhaço Pennywise e o terror na pequena cidade de Derry.", "asset:covers/horror_terror.jpg", "Horror / Terror", "978-8535919875");
        insertBookDirect(db, "Drácula", "Bram Stoker", "O clássico romance sobre o vampiro mais famoso de todos os tempos.", "asset:covers/real_cover_61.jpg", "Horror / Terror", "978-8535919882");
        insertBookDirect(db, "O Exorcista", "William Peter Blatty", "A possessão demoníaca da jovem Regan MacNeil.", "asset:covers/horror_terror.jpg", "Horror / Terror", "978-8535919899");
        insertBookDirect(db, "O Chamado de Cthulhu", "H.P. Lovecraft", "A descoberta do horror cósmico e de entidades além da compreensão.", "asset:covers/real_cover_63.jpg", "Horror / Terror", "978-8535919905");
        insertBookDirect(db, "O Iluminado", "Stephen King", "O horror psicológico e sobrenatural no isolado Hotel Overlook.", "asset:covers/real_cover_64.jpg", "Horror / Terror", "978-8535919912");

        insertBookDirect(db, "O Guia do Mochileiro das Galáxias", "Douglas Adams", "Uma aventura intergaláctica absurdamente cômica.", "asset:covers/humor.jpg", "Humor", "978-8535919929");
        insertBookDirect(db, "Memórias Póstumas de Brás Cubas", "Machado de Assis", "As memórias irônicas de um defunto autor.", "asset:covers/real_cover_66.jpg", "Humor", "978-8535919936");
        insertBookDirect(db, "Bons Omens", "Neil Gaiman e Terry Pratchett", "As profecias bizarras sobre o fim do mundo e os esforços para impedi-lo.", "asset:covers/humor.jpg", "Humor", "978-8535919943");
        insertBookDirect(db, "Três Homens em um Barco", "Jerome K. Jerome", "A cômica jornada de três amigos pelo rio Tâmisa.", "asset:covers/humor.jpg", "Humor", "978-8535919950");
        insertBookDirect(db, "A Vida Não é Útil", "Ailton Krenak", "Ensaios curtos com uma visão crítica e humorada da modernidade.", "asset:covers/real_cover_69.jpg", "Humor", "978-8535919967");

        insertBookDirect(db, "O Pequeno Príncipe", "Antoine de Saint-Exupéry", "Uma história poética sobre a infância e o valor da amizade.", "asset:covers/real_cover_70.jpg", "Infantil", "978-8535919974");
        insertBookDirect(db, "O Menino Maluquinho", "Ziraldo", "A história de um garoto com o olho maior que a barriga e muita imaginação.", "asset:covers/real_cover_71.jpg", "Infantil", "978-8535919981");
        insertBookDirect(db, "A Bolsa Amarela", "Lygia Bojunga", "A jornada de Raquel e seus desejos guardados em uma bolsa amarela.", "asset:covers/infantil.jpg", "Infantil", "978-8535919998");
        insertBookDirect(db, "Reinações de Narizinho", "Monteiro Lobato", "As incríveis aventuras do Sítio do Picapau Amarelo.", "asset:covers/real_cover_73.jpg", "Infantil", "978-8535920000");
        insertBookDirect(db, "Alice no País das Maravilhas", "Lewis Carroll", "Uma garota descobre um mundo mágico e absurdo ao cair em uma toca de coelho.", "asset:covers/real_cover_74.jpg", "Infantil", "978-8535920017");

        insertBookDirect(db, "Jogos Vorazes", "Suzanne Collins", "Jovens lutam até a morte em um evento televisionado em uma sociedade distópica.", "asset:covers/jovemadultoya.jpg", "Jovem Adulto (YA)", "978-8535920024");
        insertBookDirect(db, "A Culpa é das Estrelas", "John Green", "Um romance emocionante sobre dois adolescentes lutando contra o câncer.", "asset:covers/jovemadultoya.jpg", "Jovem Adulto (YA)", "978-8535920031");
        insertBookDirect(db, "Percy Jackson e o Ladrão de Raios", "Rick Riordan", "Aventuras de um jovem semideus no mundo moderno.", "asset:covers/jovemadultoya.jpg", "Jovem Adulto (YA)", "978-8535920048");
        insertBookDirect(db, "Divergente", "Veronica Roth", "Uma sociedade dividida por facções e a jornada de uma jovem para se encontrar.", "asset:covers/real_cover_78.jpg", "Jovem Adulto (YA)", "978-8535920055");
        insertBookDirect(db, "Crepúsculo", "Stephenie Meyer", "O romance sobrenatural entre uma humana e um vampiro.", "asset:covers/real_cover_79.jpg", "Jovem Adulto (YA)", "978-8535920062");

        insertBookDirect(db, "Grande Sertão: Veredas", "João Guimarães Rosa", "A jornada e reflexões do jagunço Riobaldo no sertão brasileiro.", "asset:covers/literaturabrasileira.jpg", "Literatura Brasileira", "978-8535920079");
        insertBookDirect(db, "Capitães da Areia", "Jorge Amado", "A vida de um grupo de meninos abandonados nas ruas de Salvador.", "asset:covers/real_cover_81.jpg", "Literatura Brasileira", "978-8535920086");
        insertBookDirect(db, "O Cortiço", "Aluísio Azevedo", "A dinâmica social e moral de um cortiço carioca do século XIX.", "asset:covers/real_cover_82.jpg", "Literatura Brasileira", "978-8535920093");
        insertBookDirect(db, "Vidas Secas", "Graciliano Ramos", "A luta pela sobrevivência de uma família de retirantes nordestinos.", "asset:covers/real_cover_83.jpg", "Literatura Brasileira", "978-8535920109");
        insertBookDirect(db, "Macunaíma", "Mário de Andrade", "A história do herói sem nenhum caráter em busca de sua identidade brasileira.", "asset:covers/real_cover_84.jpg", "Literatura Brasileira", "978-8535920116");

        insertBookDirect(db, "Orgulho e Preconceito", "Jane Austen", "Um retrato irônico da sociedade inglesa do início do século XIX.", "asset:covers/literaturaclssica.jpg", "Literatura Clássica", "978-8535920123");
        insertBookDirect(db, "Crime e Castigo", "Fiódor Dostoiévski", "A culpa e a redenção de Raskólnikov após cometer um assassinato.", "asset:covers/literaturaclssica.jpg", "Literatura Clássica", "978-8535920130");
        insertBookDirect(db, "Os Miseráveis", "Victor Hugo", "A saga de redenção de Jean Valjean na França do século XIX.", "asset:covers/real_cover_87.jpg", "Literatura Clássica", "978-8535920147");
        insertBookDirect(db, "Dom Quixote", "Miguel de Cervantes", "As aventuras do engenhoso fidalgo e seu escudeiro Sancho Pança.", "asset:covers/real_cover_88.jpg", "Literatura Clássica", "978-8535920154");
        insertBookDirect(db, "Moby Dick", "Herman Melville", "A obsessiva caçada do Capitão Ahab à grande baleia branca.", "asset:covers/real_cover_89.jpg", "Literatura Clássica", "978-8535920161");

        insertBookDirect(db, "Cem Anos de Solidão", "Gabriel García Márquez", "A saga da família Buendía na cidade fictícia de Macondo.", "asset:covers/literaturaestrangeira.jpg", "Literatura Estrangeira", "978-8535920178");
        insertBookDirect(db, "O Caçador de Pipas", "Khaled Hosseini", "Uma história de amizade e redenção ambientada no Afeganistão.", "asset:covers/literaturaestrangeira.jpg", "Literatura Estrangeira", "978-8535920185");
        insertBookDirect(db, "Norwegian Wood", "Haruki Murakami", "Uma nostálgica e comovente história de amor e perda na juventude.", "asset:covers/real_cover_92.jpg", "Literatura Estrangeira", "978-8535920192");
        insertBookDirect(db, "A Sombra do Vento", "Carlos Ruiz Zafón", "Um mistério envolvendo um livro amaldiçoado e o Cemitério dos Livros Esquecidos.", "asset:covers/real_cover_93.jpg", "Literatura Estrangeira", "978-8535920208");
        insertBookDirect(db, "Ensaio Sobre a Cegueira", "José Saramago", "Uma epidemia incontrolável de cegueira branca testa os limites humanos.", "asset:covers/real_cover_94.jpg", "Literatura Estrangeira", "978-8535920215");

        insertBookDirect(db, "Naruto Vol. 1", "Masashi Kishimoto", "A jornada de um jovem ninja em busca de aceitação e de seu sonho de ser Hokage.", "asset:covers/real_cover_95.jpg", "Mangás e Quadrinhos", "978-8535920222");
        insertBookDirect(db, "One Piece Vol. 1", "Eiichiro Oda", "A aventura de Monkey D. Luffy para encontrar o grande tesouro One Piece.", "asset:covers/real_cover_96.jpg", "Mangás e Quadrinhos", "978-8535920239");
        insertBookDirect(db, "Death Note Vol. 1", "Tsugumi Ohba", "O embate mortal entre Kira e o detetive L envolvendo um caderno sobrenatural.", "asset:covers/real_cover_97.jpg", "Mangás e Quadrinhos", "978-8535920246");
        insertBookDirect(db, "Turma da Mônica: Laços", "Vitor Cafaggi e Lu Cafaggi", "Uma emocionante jornada da turminha do Bairro do Limoeiro para encontrar o Floquinho.", "asset:covers/mangsequadrinhos.jpg", "Mangás e Quadrinhos", "978-8535920253");
        insertBookDirect(db, "Maus", "Art Spiegelman", "A dolorosa e impressionante narrativa do Holocausto retratando judeus como ratos e nazistas como gatos.", "asset:covers/real_cover_99.jpg", "Mangás e Quadrinhos", "978-8535920260");

        insertBookDirect(db, "Assassinato no Expresso do Oriente", "Agatha Christie", "Hercule Poirot investiga um crime intricado em um luxuoso trem.", "asset:covers/mistrio.jpg", "Mistério", "978-8535920277");
        insertBookDirect(db, "O Código Da Vinci", "Dan Brown", "Robert Langdon descobre segredos ocultos que abalam a base do cristianismo.", "asset:covers/mistrio.jpg", "Mistério", "978-8535920284");
        insertBookDirect(db, "A Garota no Trem", "Paula Hawkins", "O tenso thriller psicológico envolvendo o desaparecimento de uma mulher.", "asset:covers/real_cover_102.jpg", "Mistério", "978-8535920291");
        insertBookDirect(db, "Garota Exemplar", "Gillian Flynn", "O misterioso e sombrio desaparecimento de Amy Dunne e as suspeitas sobre seu marido.", "asset:covers/mistrio.jpg", "Mistério", "978-8535920307");
        insertBookDirect(db, "Os Homens que Não Amavam as Mulheres", "Stieg Larsson", "O início da trilogia Millennium, revelando segredos sombrios de uma rica família.", "asset:covers/mistrio.jpg", "Mistério", "978-8535920314");

        insertBookDirect(db, "Pai Rico, Pai Pobre", "Robert Kiyosaki", "O que os ricos ensinam aos seus filhos sobre dinheiro.", "asset:covers/negciosefinanas.jpg", "Negócios e Finanças", "978-8535920321");
        insertBookDirect(db, "A Startup Enxuta", "Eric Ries", "Como o empreendedorismo inovador transforma a maneira de desenvolver novos produtos.", "asset:covers/real_cover_106.jpg", "Negócios e Finanças", "978-8535920338");
        insertBookDirect(db, "O Homem Mais Rico da Babilônia", "George S. Clason", "Antigas parábolas sobre sucesso financeiro e sabedoria econômica.", "asset:covers/negciosefinanas.jpg", "Negócios e Finanças", "978-8535920345");
        insertBookDirect(db, "Rápido e Devagar", "Daniel Kahneman", "Duas formas de pensar que impactam nossas decisões financeiras e de vida.", "asset:covers/real_cover_108.jpg", "Negócios e Finanças", "978-8535920352");
        insertBookDirect(db, "Os Segredos da Mente Milionária", "T. Harv Eker", "Aprenda a enriquecer mudando seus conceitos sobre o dinheiro e adotando o comportamento de pessoas ricas.", "asset:covers/negciosefinanas.jpg", "Negócios e Finanças", "978-8535920369");

        insertBookDirect(db, "Antologia Poética", "Vinícius de Moraes", "Uma seleção das melhores poesias do grande mestre brasileiro.", "asset:covers/poesia.jpg", "Poesia", "978-8535920376");
        insertBookDirect(db, "Sentimento do Mundo", "Carlos Drummond de Andrade", "Poesias que retratam a dor e a melancolia do ser humano frente aos horrores da guerra.", "asset:covers/poesia.jpg", "Poesia", "978-8535920383");
        insertBookDirect(db, "Flor de Poemas", "Florbela Espanca", "Uma intensa coletânea que expressa a alma inquieta da poeta portuguesa.", "asset:covers/poesia.jpg", "Poesia", "978-8535920390");
        insertBookDirect(db, "A Rosa do Povo", "Carlos Drummond de Andrade", "Uma das obras primas da poesia brasileira moderna.", "asset:covers/poesia.jpg", "Poesia", "978-8535920406");
        insertBookDirect(db, "Odes Elementares", "Pablo Neruda", "Uma celebração poética de coisas simples do cotidiano e da vida.", "asset:covers/poesia.jpg", "Poesia", "978-8535920413");

        insertBookDirect(db, "O Silêncio dos Inocentes", "Thomas Harris", "A investigação sobre um serial killer com a ajuda de Hannibal Lecter.", "asset:covers/policial.jpg", "Policial", "978-8535920420");
        insertBookDirect(db, "Sherlock Holmes", "Arthur Conan Doyle", "As clássicas aventuras do detetive mais brilhante da literatura.", "asset:covers/real_cover_116.jpg", "Policial", "978-8535920437");
        insertBookDirect(db, "O Caso dos Dez Negrinhos", "Agatha Christie", "O maior e mais famoso mistério já escrito pela rainha do crime.", "asset:covers/policial.jpg", "Policial", "978-8535920444");
        insertBookDirect(db, "Boneco de Neve", "Jo Nesbø", "O inspetor Harry Hole investiga assassinatos conectados pela presença de bonecos de neve.", "asset:covers/real_cover_118.jpg", "Policial", "978-8535920451");
        insertBookDirect(db, "Morte no Nilo", "Agatha Christie", "Hercule Poirot investiga o assassinato de uma herdeira milionária a bordo de um navio.", "asset:covers/policial.jpg", "Policial", "978-8535920468");

        insertBookDirect(db, "O Homem em Busca de Sentido", "Viktor E. Frankl", "O relato comovente e inspirador do psiquiatra austríaco sobre a sobrevivência em campos de concentração.", "asset:covers/psicologia.jpg", "Psicologia", "978-8535920475");
        insertBookDirect(db, "Inteligência Emocional", "Daniel Goleman", "A teoria inovadora que redefine o que significa ser inteligente e bem sucedido.", "asset:covers/real_cover_121.jpg", "Psicologia", "978-8535920482");
        insertBookDirect(db, "O Poder do Agora", "Eckhart Tolle", "Um guia espiritual que inspira milhões a descobrir a liberdade através de viver no momento presente.", "asset:covers/psicologia.jpg", "Psicologia", "978-8535920499");
        insertBookDirect(db, "O Corpo Fala", "Pierre Weil", "A linguagem secreta de nosso corpo e seu significado na comunicação interpessoal.", "asset:covers/real_cover_123.jpg", "Psicologia", "978-8535920505");
        insertBookDirect(db, "O Que a Vida Me Ensinou", "Luiz Alberto Py", "Uma reflexão madura e acessível sobre como enfrentar os desafios da mente moderna.", "asset:covers/psicologia.jpg", "Psicologia", "978-8535920512");

        insertBookDirect(db, "O Alquimista", "Paulo Coelho", "A mágica jornada de Santiago em busca de um tesouro nas pirâmides do Egito.", "asset:covers/real_cover_125.jpg", "Religião e Espiritualidade", "978-8535920529");
        insertBookDirect(db, "Sidarta", "Hermann Hesse", "O clássico romance sobre a busca espiritual e iluminação durante o tempo de Buda.", "asset:covers/religioeespiritualidade.jpg", "Religião e Espiritualidade", "978-8535920536");
        insertBookDirect(db, "O Monge e o Executivo", "James C. Hunter", "A história que ensina liderança por meio dos princípios da essência e serviço.", "asset:covers/religioeespiritualidade.jpg", "Religião e Espiritualidade", "978-8535920543");
        insertBookDirect(db, "Conversando com Deus", "Neale Donald Walsch", "Um diálogo inusitado que propõe profundas questões sobre o universo e o ser humano.", "asset:covers/religioeespiritualidade.jpg", "Religião e Espiritualidade", "978-8535920550");
        insertBookDirect(db, "A Cabana", "William P. Young", "Uma emocionante ficção que debate a dor, fé e o perdão diante de tragédias.", "asset:covers/real_cover_129.jpg", "Religião e Espiritualidade", "978-8535920567");

        insertBookDirect(db, "Me Chame Pelo Seu Nome", "André Aciman", "Uma belíssima história de amor e autodescoberta no verão da Itália.", "asset:covers/romance.jpg", "Romance", "978-8535920574");
        insertBookDirect(db, "Como Eu Era Antes de Você", "Jojo Moyes", "A envolvente e triste jornada de amor de Will Traynor e Louisa Clark.", "asset:covers/real_cover_131.jpg", "Romance", "978-8535920581");
        insertBookDirect(db, "É Assim Que Acaba", "Colleen Hoover", "Uma reflexão profunda sobre o amor, a família e a dor no meio da paixão.", "asset:covers/romance.jpg", "Romance", "978-8535920598");
        insertBookDirect(db, "O Diário de Uma Paixão", "Nicholas Sparks", "Um amor atemporal e emocionante que transcende até a doença.", "asset:covers/romance.jpg", "Romance", "978-8535920604");
        insertBookDirect(db, "Orgulho e Preconceito e Zumbis", "Seth Grahame-Smith", "A adorada obra clássica agora mesclada à sobrevivência num apocalipse zumbi.", "asset:covers/romance.jpg", "Romance", "978-8535920611");

        insertBookDirect(db, "A Paciente Silenciosa", "Alex Michaelides", "O envolvente suspense sobre uma mulher que assassina o marido e nunca mais fala.", "asset:covers/suspense.jpg", "Suspense", "978-8535920628");
        insertBookDirect(db, "A Mulher na Janela", "A.J. Finn", "Um tenso thriller psicológico envolvendo uma reclusa e os segredos dos vizinhos.", "asset:covers/suspense.jpg", "Suspense", "978-8535920635");
        insertBookDirect(db, "Objetos Cortantes", "Gillian Flynn", "Uma repórter retorna à sua cidade natal para cobrir brutais crimes e os próprios fantasmas.", "asset:covers/real_cover_137.jpg", "Suspense", "978-8535920642");
        insertBookDirect(db, "Os Últimos Dias de Krypton", "Kevin J. Anderson", "O fascinante relato focado na destruição do planeta natal do Superman e seus mistérios.", "asset:covers/suspense.jpg", "Suspense", "978-8535920659");
        insertBookDirect(db, "O Colecionador", "John Fowles", "A obsessão macabra de um homem introvertido que decide sequestrar a garota que ama.", "asset:covers/real_cover_139.jpg", "Suspense", "978-8535920666");

        insertBookDirect(db, "Clean Code", "Robert C. Martin", "Um manual prático e atemporal de boas práticas para a engenharia de software ágil.", "asset:covers/real_cover_140.jpg", "Tecnologia", "978-8535920673");
        insertBookDirect(db, "O Programador Pragmático", "Andrew Hunt", "A jornada definitiva da iniciação até a maestria técnica no mundo da programação.", "asset:covers/tecnologia.jpg", "Tecnologia", "978-8535920680");
        insertBookDirect(db, "Design Patterns", "Erich Gamma", "Elementos fundamentais e essenciais no desenvolvimento de software orientado a objetos.", "asset:covers/real_cover_142.jpg", "Tecnologia", "978-8535920697");
        insertBookDirect(db, "Código Limpo", "Robert C. Martin", "A versão nacional essencial de habilidades práticas para programadores.", "asset:covers/real_cover_143.jpg", "Tecnologia", "978-8535920703");
        insertBookDirect(db, "Engenharia de Software", "Ian Sommerville", "O livro base na formação acadêmica que guia toda a criação moderna de softwares e metodologias.", "asset:covers/tecnologia.jpg", "Tecnologia", "978-8535920710");

        insertBookDirect(db, "O Gene Egoísta", "Richard Dawkins", "A fascinante perspectiva de vida do mundo biológico focada na evolução centrada no gene.", "asset:covers/outros.jpg", "Outros", "978-8535920727");
        insertBookDirect(db, "Freakonomics", "Steven D. Levitt", "Explorando a economia por um lado obscuro, investigando o lado divertido de escolhas diárias.", "asset:covers/real_cover_146.jpg", "Outros", "978-8535920734");
        insertBookDirect(db, "Armas, Germes e Aço", "Jared Diamond", "As razões ocultas e o curso de evolução e história da desigualdade global.", "asset:covers/outros.jpg", "Outros", "978-8535920741");
        insertBookDirect(db, "Cisne Negro", "Nassim Nicholas Taleb", "O surpreendente impacto das consequências de eventos altamente improváveis e da incerteza.", "asset:covers/real_cover_148.jpg", "Outros", "978-8535920758");
        insertBookDirect(db, "Breves Respostas Para Grandes Questões", "Stephen Hawking", "As conclusões e previsões essenciais do cientista brilhante a respeito do futuro universal e civilizacional.", "asset:covers/outros.jpg", "Outros", "978-8535920765");
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

    public long insertUserWithId(long id, String name, String email, String passwordHash,
            String bio, String photoPath, String favoriteGenres, String role,
            double latitude, double longitude, String language) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("name", name);
        values.put("email", email);
        values.put("password_hash", passwordHash);
        values.put("bio", bio);
        values.put("photo_path", photoPath);
        values.put("favorite_genres", favoriteGenres);
        values.put("role", role);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("language", language);
        return db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, "email = ?",
                new String[] { email }, null, null, null);
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
                new String[] { String.valueOf(id) }, null, null, null);
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
                new String[] { String.valueOf(user.getId()) });
        return rows > 0;
    }

    public List<User> searchUsers(String query) {
        SQLiteDatabase db = getReadableDatabase();
        List<User> users = new ArrayList<>();
        Cursor cursor = db.query(TABLE_USERS, null,
                "name LIKE ? OR email LIKE ?",
                new String[] { "%" + query + "%", "%" + query + "%" },
                null, null, "name ASC");
        while (cursor.moveToNext()) {
            users.add(cursorToUser(cursor));
        }
        cursor.close();
        return users;
    }

    public List<User> getNearbyUsers(double lat, double lng, double radiusKm, String genreFilter,
            String languageFilter) {
        SQLiteDatabase db = getReadableDatabase();
        List<User> users = new ArrayList<>();
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

    public boolean isBookTitleExists(String title) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_BOOKS, new String[] { "id" }, "title COLLATE NOCASE = ?",
                new String[] { title }, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public long insertBook(String title, String author, String synopsis,
            String coverPath, String genre, String isbn, long creatorId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("author", author);
        values.put("synopsis", synopsis);
        values.put("cover_path", coverPath);
        values.put("genre", genre);
        values.put("isbn", isbn);
        values.put("creator_id", creatorId);
        return db.insert(TABLE_BOOKS, null, values);
    }

    public Book getBookById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_BOOKS, null, "id = ?",
                new String[] { String.valueOf(id) }, null, null, null);
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
                new String[] { "%" + query + "%", "%" + query + "%", "%" + query + "%" },
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
        int creatorIdIdx = cursor.getColumnIndex("creator_id");
        if (creatorIdIdx >= 0) {
            book.setCreatorId(cursor.getLong(creatorIdIdx));
        }
        return book;
    }

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
                new String[] { String.valueOf(userId), String.valueOf(bookId) });
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
                "SELECT ub.*, b.title, b.author, b.genre, b.cover_path, b.synopsis, b.isbn " +
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
                "SELECT ub.*, b.title, b.author, b.genre, b.cover_path, b.synopsis, b.isbn " +
                        "FROM " + TABLE_USER_BOOKS + " ub " +
                        "INNER JOIN " + TABLE_BOOKS + " b ON ub.book_id = b.id " +
                        "WHERE ub.user_id = ? AND ub.book_id = ?",
                new String[] { String.valueOf(userId), String.valueOf(bookId) });
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
        ub.setBookIsbn(cursor.getString(cursor.getColumnIndexOrThrow("isbn")));
        ub.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
        return ub;
    }

    public long insertReview(long userId, long bookId, String text, int rating) {
        if (text == null || text.trim().isEmpty()) {
            return -1;
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
                new String[] { String.valueOf(bookId) });
        while (cursor.moveToNext()) {
            reviews.add(cursorToReview(cursor));
        }
        cursor.close();
        return reviews;
    }
    
    // --- FOLLOW SYSTEM METHODS ---
    
    public boolean followUser(long followerId, long followedId) {
        if (followerId == followedId) return false;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("follower_id", followerId);
        values.put("followed_id", followedId);
        try {
            long result = db.insertWithOnConflict(TABLE_FOLLOWERS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            return result != -1;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean unfollowUser(long followerId, long followedId) {
        SQLiteDatabase db = getWritableDatabase();
        int deleted = db.delete(TABLE_FOLLOWERS, "follower_id = ? AND followed_id = ?", 
            new String[]{String.valueOf(followerId), String.valueOf(followedId)});
        return deleted > 0;
    }
    
    public boolean isFollowing(long followerId, long followedId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_FOLLOWERS + " WHERE follower_id = ? AND followed_id = ?",
                new String[]{String.valueOf(followerId), String.valueOf(followedId)});
        boolean following = cursor.moveToFirst();
        cursor.close();
        return following;
    }
    
    public int getFollowersCount(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_FOLLOWERS + " WHERE followed_id = ?",
                new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
    
    public int getFollowingCount(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_FOLLOWERS + " WHERE follower_id = ?",
                new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
    
    public List<Review> getUserReviews(long userId) {
        List<Review> reviews = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT r.*, u.name as user_name, u.photo_path as user_photo, b.title as book_title " +
                "FROM " + TABLE_REVIEWS + " r " +
                "INNER JOIN " + TABLE_USERS + " u ON r.user_id = u.id " +
                "INNER JOIN " + TABLE_BOOKS + " b ON r.book_id = b.id " +
                "WHERE r.user_id = ? ORDER BY r.created_at DESC", 
                new String[]{String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                Review r = new Review();
                r.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
                r.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
                r.setBookId(cursor.getLong(cursor.getColumnIndexOrThrow("book_id")));
                r.setText(cursor.getString(cursor.getColumnIndexOrThrow("text")));
                r.setRating(cursor.getInt(cursor.getColumnIndexOrThrow("rating")));
                r.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
                r.setUserName(cursor.getString(cursor.getColumnIndexOrThrow("user_name")));
                r.setUserPhotoPath(cursor.getString(cursor.getColumnIndexOrThrow("user_photo")));
                r.setBookTitle(cursor.getString(cursor.getColumnIndexOrThrow("book_title")));
                reviews.add(r);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return reviews;
    }
    
    public List<Review> getTimelineReviews(long currentUserId) {
        List<Review> reviews = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT r.*, u.name as user_name, u.photo_path as user_photo, b.title as book_title " +
                "FROM " + TABLE_REVIEWS + " r " +
                "INNER JOIN " + TABLE_USERS + " u ON r.user_id = u.id " +
                "INNER JOIN " + TABLE_BOOKS + " b ON r.book_id = b.id " +
                "INNER JOIN " + TABLE_FOLLOWERS + " f ON r.user_id = f.followed_id " +
                "WHERE f.follower_id = ? " +
                "ORDER BY r.created_at DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(currentUserId)});
        if (cursor.moveToFirst()) {
            do {
                Review r = new Review();
                r.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
                r.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow("user_id")));
                r.setBookId(cursor.getLong(cursor.getColumnIndexOrThrow("book_id")));
                r.setText(cursor.getString(cursor.getColumnIndexOrThrow("text")));
                r.setRating(cursor.getInt(cursor.getColumnIndexOrThrow("rating")));
                r.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
                r.setUserName(cursor.getString(cursor.getColumnIndexOrThrow("user_name")));
                r.setUserPhotoPath(cursor.getString(cursor.getColumnIndexOrThrow("user_photo")));
                r.setBookTitle(cursor.getString(cursor.getColumnIndexOrThrow("book_title")));
                reviews.add(r);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return reviews;
    }

    public double getBookAverageRating(long bookId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT AVG(rating) as avg_rating, COUNT(*) as count FROM " + TABLE_REVIEWS +
                        " WHERE book_id = ?",
                new String[] { String.valueOf(bookId) });
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
                new String[] { String.valueOf(bookId) });
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

    public long insertClub(String name, String description, boolean isPublic, long creatorId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        values.put("is_public", isPublic ? 1 : 0);
        values.put("creator_id", creatorId);
        long clubId = db.insert(TABLE_CLUBS, null, values);
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

    public long insertClubWithId(long id, String name, String description, boolean isPublic, long creatorId,
            String bannerPath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("name", name);
        values.put("description", description);
        values.put("is_public", isPublic ? 1 : 0);
        values.put("creator_id", creatorId);
        values.put("banner_path", bannerPath != null ? bannerPath : "");
        long result = db.insertWithOnConflict(TABLE_CLUBS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        if (result > 0) {
            ContentValues memberValues = new ContentValues();
            memberValues.put("club_id", id);
            memberValues.put("user_id", creatorId);
            memberValues.put("role", "ORGANIZER");
            memberValues.put("status", "APPROVED");
            db.insertWithOnConflict(TABLE_CLUB_MEMBERS, null, memberValues, SQLiteDatabase.CONFLICT_REPLACE);
        }
        return result;
    }

    public Club getClubById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CLUBS, null, "id = ?",
                new String[] { String.valueOf(id) }, null, null, null);
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
                new String[] { String.valueOf(userId) });
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
                new String[] { String.valueOf(clubId) });
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
                new String[] { String.valueOf(clubId), String.valueOf(userId) });
        return rows > 0;
    }

    public ClubMember getClubMember(long clubId, long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CLUB_MEMBERS, null,
                "club_id = ? AND user_id = ?",
                new String[] { String.valueOf(clubId), String.valueOf(userId) },
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
                        " u ON cm.user_id = u.id WHERE cm.club_id = ? AND cm.status = 'APPROVED' ORDER BY cm.role, u.name",
                new String[] { String.valueOf(clubId) });
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
                new String[] { String.valueOf(clubId) }, null, null, "date_time ASC");
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
                new String[] { String.valueOf(reportId) });
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

    public boolean updateUserPassword(long userId, String newPasswordHash) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password_hash", newPasswordHash);
        int rows = db.update(TABLE_USERS, values, "id = ?",
                new String[] { String.valueOf(userId) });
        return rows > 0;
    }

    public int getPendingMemberRequestsCount(long organizerId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_CLUB_MEMBERS + " cm INNER JOIN " + TABLE_CLUBS +
                        " c ON cm.club_id = c.id WHERE c.creator_id = ? AND cm.status = 'PENDING'",
                new String[] { String.valueOf(organizerId) });
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public int getUnreadMessagesCount(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + TABLE_MESSAGES + " WHERE receiver_id = ? AND is_read = 0",
                new String[] { String.valueOf(userId) });
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    public List<ClubMember> getPendingMemberRequests(long organizerId) {
        SQLiteDatabase db = getReadableDatabase();
        List<ClubMember> members = new ArrayList<>();
        Cursor cursor = db.rawQuery(
                "SELECT cm.*, u.name as user_name, u.email as user_email, c.name as club_name " +
                        "FROM " + TABLE_CLUB_MEMBERS + " cm " +
                        "INNER JOIN " + TABLE_USERS + " u ON cm.user_id = u.id " +
                        "INNER JOIN " + TABLE_CLUBS + " c ON cm.club_id = c.id " +
                        "WHERE cm.status = 'PENDING' AND c.creator_id = ? " +
                        "ORDER BY cm.joined_at DESC",
                new String[] { String.valueOf(organizerId) });
        while (cursor.moveToNext()) {
            ClubMember member = cursorToClubMember(cursor);
            int clubNameIdx = cursor.getColumnIndex("club_name");
            if (clubNameIdx >= 0) {
                member.setClubName(cursor.getString(clubNameIdx));
            }
            members.add(member);
        }
        cursor.close();
        return members;
    }

    public boolean deleteUserBook(long userId, long bookId) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(TABLE_USER_BOOKS,
                "user_id = ? AND book_id = ?",
                new String[] { String.valueOf(userId), String.valueOf(bookId) });
        return rows > 0;
    }

    public int getUserBookCount(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) as count FROM " + TABLE_USER_BOOKS + " WHERE user_id = ?",
                new String[] { String.valueOf(userId) });
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
        }
        cursor.close();
        return count;
    }

    public int getUserBookCountByStatus(long userId, String status) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) as count FROM " + TABLE_USER_BOOKS +
                        " WHERE user_id = ? AND status = ?",
                new String[] { String.valueOf(userId), status });
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow("count"));
        }
        cursor.close();
        return count;
    }

    public UserBook getCurrentReading(long userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT ub.*, b.title, b.author, b.genre, b.cover_path, b.synopsis, b.isbn " +
                        "FROM " + TABLE_USER_BOOKS + " ub " +
                        "INNER JOIN " + TABLE_BOOKS + " b ON ub.book_id = b.id " +
                        "WHERE ub.user_id = ? AND ub.status = 'LENDO' " +
                        "ORDER BY ub.created_at DESC LIMIT 1",
                new String[] { String.valueOf(userId) });
        UserBook userBook = null;
        if (cursor.moveToFirst()) {
            userBook = cursorToUserBook(cursor);
        }
        cursor.close();
        return userBook;
    }

    public boolean updateBook(long bookId, String title, String author, String synopsis,
            String coverPath, String genre, String isbn) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("author", author);
        values.put("synopsis", synopsis);
        if (coverPath != null) {
            values.put("cover_path", coverPath);
        }
        values.put("genre", genre);
        values.put("isbn", isbn);
        return db.update(TABLE_BOOKS, values, "id = ?", new String[] { String.valueOf(bookId) }) > 0;
    }

    public boolean deleteBook(long bookId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_REVIEWS, "book_id = ?", new String[] { String.valueOf(bookId) });
        db.delete(TABLE_USER_BOOKS, "book_id = ?", new String[] { String.valueOf(bookId) });
        db.delete(TABLE_EVENTS, "book_id = ?", new String[] { String.valueOf(bookId) });
        return db.delete(TABLE_BOOKS, "id = ?", new String[] { String.valueOf(bookId) }) > 0;
    }

    public boolean updateClub(long clubId, String name, String description, boolean isPublic, String bannerPath) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("description", description);
        values.put("is_public", isPublic ? 1 : 0);
        if (bannerPath != null) {
            values.put("banner_path", bannerPath);
        }
        return db.update(TABLE_CLUBS, values, "id = ?", new String[] { String.valueOf(clubId) }) > 0;
    }

    public boolean removeClubMember(long clubId, long userId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_CLUB_MEMBERS, "club_id = ? AND user_id = ?",
                new String[] { String.valueOf(clubId), String.valueOf(userId) }) > 0;
    }

    public boolean deleteClub(long clubId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_EVENTS, "club_id = ?", new String[] { String.valueOf(clubId) });
        db.delete(TABLE_CLUB_MEMBERS, "club_id = ?", new String[] { String.valueOf(clubId) });
        return db.delete(TABLE_CLUBS, "id = ?", new String[] { String.valueOf(clubId) }) > 0;
    }

    public boolean updateEvent(long eventId, String title, String description, String dateTime, String location,
            Long bookId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("description", description);
        values.put("date_time", dateTime);
        values.put("location", location);
        if (bookId != null) {
            values.put("book_id", bookId);
        } else {
            values.putNull("book_id");
        }
        return db.update(TABLE_EVENTS, values, "id = ?", new String[] { String.valueOf(eventId) }) > 0;
    }

    public boolean deleteEvent(long eventId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_EVENTS, "id = ?", new String[] { String.valueOf(eventId) }) > 0;
    }

    public Event getEventById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_EVENTS, null, "id = ?",
                new String[] { String.valueOf(id) }, null, null, null);
        Event event = null;
        if (cursor.moveToFirst()) {
            event = cursorToEvent(cursor);
        }
        cursor.close();
        return event;
    }

    // --- Message Methods ---

    public boolean insertMessage(com.bookmap.app.model.Message msg) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", msg.getId());
        values.put("sender_id", msg.getSenderId());
        values.put("receiver_id", msg.getReceiverId());
        values.put("content", msg.getContent());
        values.put("timestamp", msg.getTimestamp());
        values.put("is_read", msg.isRead() ? 1 : 0);
        
        long result = db.insertWithOnConflict(TABLE_MESSAGES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        return result != -1;
    }

    public void markMessagesAsRead(long currentUserId, long senderId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_read", 1);
        db.update(TABLE_MESSAGES, values, "receiver_id = ? AND sender_id = ? AND is_read = 0",
                new String[] { String.valueOf(currentUserId), String.valueOf(senderId) });
    }

    public boolean deleteMessageLocal(String messageId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_MESSAGES, "id = ?", new String[]{messageId}) > 0;
    }

    public List<com.bookmap.app.model.Message> getMessagesBetween(long user1, long user2) {
        List<com.bookmap.app.model.Message> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_MESSAGES + 
                       " WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) " +
                       " ORDER BY timestamp ASC";
        Cursor cursor = db.rawQuery(query, new String[]{
            String.valueOf(user1), String.valueOf(user2),
            String.valueOf(user2), String.valueOf(user1)
        });

        if (cursor.moveToFirst()) {
            do {
                list.add(new com.bookmap.app.model.Message(
                        cursor.getString(cursor.getColumnIndexOrThrow("id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("sender_id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("receiver_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("content")),
                        cursor.getString(cursor.getColumnIndexOrThrow("timestamp")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_read")) > 0
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<com.bookmap.app.model.Message> getInbox(long userId) {
        List<com.bookmap.app.model.Message> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT m1.* FROM " + TABLE_MESSAGES + " m1 " +
                       "LEFT JOIN " + TABLE_MESSAGES + " m2 " +
                       "ON ( " +
                       "  ((m1.sender_id = m2.sender_id AND m1.receiver_id = m2.receiver_id) OR " +
                       "   (m1.sender_id = m2.receiver_id AND m1.receiver_id = m2.sender_id)) " +
                       "  AND m1.timestamp < m2.timestamp " +
                       ") " +
                       "WHERE (m1.sender_id = ? OR m1.receiver_id = ?) AND m2.id IS NULL " +
                       "ORDER BY m1.timestamp DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(userId)});
        if (cursor.moveToFirst()) {
            do {
                list.add(new com.bookmap.app.model.Message(
                        cursor.getString(cursor.getColumnIndexOrThrow("id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("sender_id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("receiver_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("content")),
                        cursor.getString(cursor.getColumnIndexOrThrow("timestamp")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_read")) > 0
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // --- REVIEW INTERACTIONS ---
    public boolean toggleReviewLike(long reviewId, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_REVIEW_LIKES, new String[]{"id", "is_like"},
                "review_id = ? AND user_id = ?", new String[]{String.valueOf(reviewId), String.valueOf(userId)},
                null, null, null);

        boolean isLikedNow = false;
        if (cursor != null && cursor.moveToFirst()) {
            int isLikeIndex = cursor.getColumnIndex("is_like");
            if (isLikeIndex != -1) {
                int isLike = cursor.getInt(isLikeIndex);
                if (isLike == 1) {
                    // Unlike
                    db.delete(TABLE_REVIEW_LIKES, "review_id = ? AND user_id = ?", new String[]{String.valueOf(reviewId), String.valueOf(userId)});
                } else {
                    ContentValues values = new ContentValues();
                    values.put("is_like", 1);
                    db.update(TABLE_REVIEW_LIKES, values, "review_id = ? AND user_id = ?", new String[]{String.valueOf(reviewId), String.valueOf(userId)});
                    isLikedNow = true;
                }
            }
            cursor.close();
        } else {
            // Like
            ContentValues values = new ContentValues();
            values.put("review_id", reviewId);
            values.put("user_id", userId);
            values.put("is_like", 1);
            db.insert(TABLE_REVIEW_LIKES, null, values);
            isLikedNow = true;
        }
        return isLikedNow;
    }

    public int getReviewLikesCount(long reviewId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_REVIEW_LIKES + " WHERE review_id = ? AND is_like = 1", new String[]{String.valueOf(reviewId)});
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public boolean hasUserLikedReview(long reviewId, long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM " + TABLE_REVIEW_LIKES + " WHERE review_id = ? AND user_id = ? AND is_like = 1", new String[]{String.valueOf(reviewId), String.valueOf(userId)});
        boolean liked = false;
        if (cursor != null) {
            liked = cursor.moveToFirst();
            cursor.close();
        }
        return liked;
    }

    public long addReviewComment(long reviewId, long userId, String text) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("review_id", reviewId);
        values.put("user_id", userId);
        values.put("text", text);
        return db.insert(TABLE_REVIEW_COMMENTS, null, values);
    }

    public boolean deleteReviewComment(long commentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_REVIEW_COMMENTS, "id = ?", new String[]{String.valueOf(commentId)}) > 0;
    }

    public int getReviewCommentsCount(long reviewId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_REVIEW_COMMENTS + " WHERE review_id = ?", new String[]{String.valueOf(reviewId)});
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public List<com.bookmap.app.model.ReviewComment> getReviewComments(long reviewId) {
        List<com.bookmap.app.model.ReviewComment> comments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT c.*, u.name as user_name FROM " + TABLE_REVIEW_COMMENTS + " c " +
                "INNER JOIN " + TABLE_USERS + " u ON c.user_id = u.id " +
                "WHERE c.review_id = ? ORDER BY c.timestamp ASC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(reviewId)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex("id");
                int userIdIndex = cursor.getColumnIndex("user_id");
                int userNameIndex = cursor.getColumnIndex("user_name");
                int textIndex = cursor.getColumnIndex("text");
                int timestampIndex = cursor.getColumnIndex("timestamp");
                if (idIndex != -1 && userIdIndex != -1 && userNameIndex != -1 && textIndex != -1 && timestampIndex != -1) {
                    comments.add(new com.bookmap.app.model.ReviewComment(
                            cursor.getLong(idIndex),
                            reviewId,
                            cursor.getLong(userIdIndex),
                            cursor.getString(userNameIndex),
                            cursor.getString(textIndex),
                            cursor.getString(timestampIndex)
                    ));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return comments;
    }

    public void insertReviewLike(long reviewId, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("review_id", reviewId);
        values.put("user_id", userId);
        values.put("is_like", 1);
        db.insertWithOnConflict(TABLE_REVIEW_LIKES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void removeReviewLike(long reviewId, long userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REVIEW_LIKES, "review_id = ? AND user_id = ?", new String[]{String.valueOf(reviewId), String.valueOf(userId)});
    }
    
    public void insertReviewCommentSync(long id, long reviewId, long userId, String text, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("review_id", reviewId);
        values.put("user_id", userId);
        values.put("text", text);
        values.put("timestamp", timestamp);
        db.insertWithOnConflict(TABLE_REVIEW_COMMENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void removeReviewCommentSync(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REVIEW_COMMENTS, "id = ?", new String[]{String.valueOf(id)});
    }
}


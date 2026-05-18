package com.bookmap.app.util;

import java.util.Arrays;
import java.util.List;

public class GenreUtil {
    private static final String[] GENRES = {
            "Ação e Aventura",
            "Autoajuda",
            "Biografia",
            "Contos",
            "Crimes Verdadeiros",
            "Crônicas",
            "Distopia",
            "Ensaios",
            "Fantasia",
            "Ficção Científica",
            "Filosofia",
            "História",
            "Horror / Terror",
            "Humor",
            "Infantil",
            "Jovem Adulto (YA)",
            "Literatura Brasileira",
            "Literatura Clássica",
            "Mangás e Quadrinhos",
            "Mistério",
            "Negócios e Finanças",
            "Poesia",
            "Policial",
            "Psicologia",
            "Religião e Espiritualidade",
            "Romance",
            "Suspense",
            "Tecnologia",
            "Outros"
    };

    public static List<String> getGenres() {
        return Arrays.asList(GENRES);
    }
    
    public static String[] getGenresArray() {
        return GENRES;
    }
}

package it.unicas.project.template.address.model;

public class Genre {

    private final Integer idGenre;
    private final String genre;

    public Genre(Integer idGenre, String genre){
        this.idGenre = idGenre;
        this.genre = genre;
    }

    public Integer getIdGenre() { return idGenre; }
    public String getGenre() { return genre; }

    @Override
    public String toString() {
        return genre;
    }
}

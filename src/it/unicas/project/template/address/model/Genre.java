package it.unicas.project.template.address.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Genre (tabla estática) - solo lectura para UI
 */
public class Genre {

    private final IntegerProperty idGenre;
    private final StringProperty genre;

    public Genre(Integer idGenre, String genre){
        this.idGenre = new SimpleIntegerProperty(idGenre);
        this.genre = new SimpleStringProperty(genre);
    }

    // GETTERS
    public Integer getIdGenre() { return idGenre.get(); }
    public IntegerProperty idGenreProperty() { return idGenre; }

    public String getGenre() { return genre.get(); }
    public StringProperty genreProperty() { return genre; }

    @Override
    public String toString() {
        return genre.get();
    }
}

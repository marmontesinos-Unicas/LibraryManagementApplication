package it.unicas.project.template.address.model;

import javafx.beans.property.*;

/**
 * Represents a genre in the system.
 * <p>
 * This is a read-only data holder used in the UI, typically for
 * static genre tables or dropdowns. It wraps the genre ID and name
 * as JavaFX properties for binding in TableViews or other controls.
 * </p>
 */
public class Genre {

    /** Unique identifier of the genre */
    private final IntegerProperty idGenre;

    /** Name of the genre */
    private final StringProperty genre;

    /**
     * Constructs a {@code Genre} instance with the given ID and name.
     *
     * @param idGenre the unique ID of the genre
     * @param genre the name of the genre
     */
    public Genre(Integer idGenre, String genre){
        this.idGenre = new SimpleIntegerProperty(idGenre);
        this.genre = new SimpleStringProperty(genre);
    }

    /**
     * Returns the ID of the genre.
     *
     * @return genre ID
     */
    public Integer getIdGenre() { return idGenre.get(); }

    /**
     * Returns the genre ID property for JavaFX bindings.
     *
     * @return genre ID property
     */
    public IntegerProperty idGenreProperty() { return idGenre; }

    /**
     * Returns the name of the genre.
     *
     * @return genre name
     */
    public String getGenre() { return genre.get(); }

    /**
     * Returns the genre property for JavaFX bindings.
     *
     * @return genre name property
     */
    public StringProperty genreProperty() { return genre; }

    /**
     * Returns the string representation of the genre.
     *
     * @return genre name as string
     */
    @Override
    public String toString() {
        return genre.get();
    }
}

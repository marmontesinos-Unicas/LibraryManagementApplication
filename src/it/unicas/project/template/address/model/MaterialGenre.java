package it.unicas.project.template.address.model;

import javafx.beans.property.*;

/**
 * Represents the relationship between a Material and a Genre.
 * <p>
 * This class is used to map which genres a material belongs to.
 * Provides JavaFX properties for TableView or other UI bindings.
 * </p>
 */
public class MaterialGenre {

    /** ID of the material */
    private final IntegerProperty idMaterial;

    /** ID of the genre */
    private final IntegerProperty idGenre;

    /**
     * Constructs a MaterialGenre with specified material and genre IDs.
     *
     * @param idMaterial the ID of the material
     * @param idGenre the ID of the genre
     */
    public MaterialGenre(Integer idMaterial, Integer idGenre) {
        this.idMaterial = new SimpleIntegerProperty(idMaterial);
        this.idGenre = new SimpleIntegerProperty(idGenre);
    }


    /** Getters, setters, and JavaFX properties for UI binding */
    public Integer getIdMaterial() { return idMaterial.get(); }
    public void setIdMaterial(Integer idMaterial) { this.idMaterial.set(idMaterial); }
    public IntegerProperty idMaterialProperty() { return idMaterial; }

    public Integer getIdGenre() { return idGenre.get(); }
    public void setIdGenre(Integer idGenre) { this.idGenre.set(idGenre); }
    public IntegerProperty idGenreProperty() { return idGenre; }

    /** Returns a string representation of the MaterialGenre. */
    @Override
    public String toString() {
        return "(" + getIdMaterial() + ", " + getIdGenre() + ")";
    }
}

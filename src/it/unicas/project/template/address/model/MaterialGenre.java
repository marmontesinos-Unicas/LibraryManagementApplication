package it.unicas.project.template.address.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class MaterialGenre {

    private final IntegerProperty idMaterial;
    private final IntegerProperty idGenre;

    public MaterialGenre(Integer idMaterial, Integer idGenre) {
        this.idMaterial = new SimpleIntegerProperty(idMaterial);
        this.idGenre = new SimpleIntegerProperty(idGenre);
    }

    public Integer getIdMaterial() { return idMaterial.get(); }
    public void setIdMaterial(Integer idMaterial) { this.idMaterial.set(idMaterial); }
    public IntegerProperty idMaterialProperty() { return idMaterial; }

    public Integer getIdGenre() { return idGenre.get(); }
    public void setIdGenre(Integer idGenre) { this.idGenre.set(idGenre); }
    public IntegerProperty idGenreProperty() { return idGenre; }

    @Override
    public String toString() {
        return "(" + getIdMaterial() + ", " + getIdGenre() + ")";
    }
}

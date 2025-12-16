package it.unicas.project.template.address.model;

import javafx.beans.property.*;
/**
 * Represents a type of library material (e.g., book, DVD) for the UI.
 * <p>
 * This is a static reference table used for read-only purposes in the UI.
 * Provides JavaFX properties for TableView or other UI bindings.
 * </p>
 */
public class MaterialType {

    /** Unique identifier of the material type */
    private final IntegerProperty idMaterialType;

    /** Name of the material type */
    private final StringProperty material_type;

    /**
     * Constructs a {@code MaterialType} with specified ID and name.
     *
     * @param idMaterialType the ID of the material type
     * @param material_type the name of the material type
     */
    public MaterialType(Integer idMaterialType, String material_type) {
        this.idMaterialType = new SimpleIntegerProperty(idMaterialType);
        this.material_type = new SimpleStringProperty(material_type);
    }

    /** Getters, setters, and JavaFX properties for UI binding */
    public Integer getIdMaterialType() { return idMaterialType.get(); }

    public String getMaterial_type() { return material_type.get(); }

    @Override
    public String toString() {
        return material_type.get();
    }
}

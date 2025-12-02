package it.unicas.project.template.address.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * MaterialType (tabla estática) - solo lectura para UI
 */
public class MaterialType {

    private final IntegerProperty idMaterialType;
    private final StringProperty material_type;

    public MaterialType(Integer idMaterialType, String material_type) {
        this.idMaterialType = new SimpleIntegerProperty(idMaterialType);
        this.material_type = new SimpleStringProperty(material_type);
    }

    // GETTERS
    public Integer getIdMaterialType() { return idMaterialType.get(); }
    public IntegerProperty idMaterialTypeProperty() { return idMaterialType; }

    public String getMaterial_type() { return material_type.get(); }
    public StringProperty material_typeProperty() { return material_type; }

    @Override
    public String toString() {
        return material_type.get();
    }
}

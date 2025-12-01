package it.unicas.project.template.address.model;

public class MaterialType {

    private final Integer idMaterialType;
    private final String material_type;  // coincide con columna SQL

    public MaterialType(Integer idMaterialType, String material_type) {
        this.idMaterialType = idMaterialType;
        this.material_type = material_type;
    }

    public Integer getIdMaterialType() { return idMaterialType; }
    public String getMaterial_type() { return material_type; }

    @Override
    public String toString() {
        return material_type;
    }
}

package it.unicas.project.template.address.model;

import javafx.beans.property.*;

/**
 * Model class for the Material Inventory View.
 * <p>
 * Extends {@link Material} to reuse existing properties (title, author, ISBN)
 * and adds calculated fields and status breakdowns required for TableView display.
 * </p>
 */
public class MaterialInventory extends Material {

    private final IntegerProperty quantity;
    private final StringProperty materialTypeName;
    private final IntegerProperty availableCount;
    private final IntegerProperty onHoldCount;
    private final IntegerProperty loanedCount;

    /**
     * Constructs a {@code MaterialInventory} instance from a base {@link Material} object
     * and a total quantity.
     *
     * @param material the base material
     * @param quantity total quantity of this material
     */
    public MaterialInventory(Material material, Integer quantity) {
        super(material.getIdMaterial(), material.getTitle(), material.getAuthor(), material.getYear(),
                material.getISBN(), material.getIdMaterialType(), material.getMaterial_status());

        this.quantity = new SimpleIntegerProperty(quantity != null ? quantity : 0);
        this.materialTypeName = new SimpleStringProperty("");
        this.availableCount = new SimpleIntegerProperty(0);
        this.onHoldCount = new SimpleIntegerProperty(0);
        this.loanedCount = new SimpleIntegerProperty(0);
    }

    /** Getters, setters, and JavaFX properties for UI binding */
    public Integer getQuantity() { return quantity.get(); }

    public String getMaterialTypeName() { return materialTypeName.get(); }
    public void setMaterialTypeName(String materialTypeName) { this.materialTypeName.set(materialTypeName); }

    public void setAvailableCount(Integer availableCount) { this.availableCount.set(availableCount); }

    public void setOnHoldCount(Integer onHoldCount) { this.onHoldCount.set(onHoldCount); }

    public void setLoanedCount(Integer loanedCount) { this.loanedCount.set(loanedCount); }
}
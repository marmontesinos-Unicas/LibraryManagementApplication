package it.unicas.project.template.address.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty; // NEW IMPORT
import javafx.beans.property.StringProperty; // NEW IMPORT

/**
 * Model class for the Material Inventory View.
 * It extends Material to reuse existing properties (title, author, ISBN)
 * and adds the calculated 'quantity' property and status breakdowns required for the TableView.
 */
public class MaterialInventory extends Material {

    private final IntegerProperty quantity;

    private final StringProperty materialTypeName;
    private final IntegerProperty availableCount;
    private final IntegerProperty onHoldCount;
    private final IntegerProperty loanedCount;

    /**
     * Constructor used by the MaterialDAOMySQLImpl after fetching grouped data.
     */
    public MaterialInventory(Material material, Integer quantity) {
        // Call the superclass constructor to initialize all base Material fields
        super(material.getIdMaterial(), material.getTitle(), material.getAuthor(), material.getYear(),
                material.getISBN(), material.getIdMaterialType(), material.getMaterial_status());

        // Initialize the new quantity property
        this.quantity = new SimpleIntegerProperty(quantity != null ? quantity : 0);

        // Initialize new properties with defaults. They will be set by the DAO via setters.
        this.materialTypeName = new SimpleStringProperty("");
        this.availableCount = new SimpleIntegerProperty(0);
        this.onHoldCount = new SimpleIntegerProperty(0);
        this.loanedCount = new SimpleIntegerProperty(0);
    }

    // --- GETTERS, SETTERS, and PROPERTY METHODS for Quantity ---
    // (Existing methods for quantity remain here)
    public Integer getQuantity() { return quantity.get(); }
    public void setQuantity(Integer quantity) { this.quantity.set(quantity); }
    public IntegerProperty quantityProperty() { return quantity; }

    // --- GETTERS, SETTERS, and PROPERTY METHODS for NEW FIELDS ---

    public String getMaterialTypeName() { return materialTypeName.get(); }
    public void setMaterialTypeName(String materialTypeName) { this.materialTypeName.set(materialTypeName); }
    public StringProperty materialTypeNameProperty() { return materialTypeName; }

    public Integer getAvailableCount() { return availableCount.get(); }
    public void setAvailableCount(Integer availableCount) { this.availableCount.set(availableCount); }
    public IntegerProperty availableCountProperty() { return availableCount; }

    public Integer getOnHoldCount() { return onHoldCount.get(); }
    public void setOnHoldCount(Integer onHoldCount) { this.onHoldCount.set(onHoldCount); }
    public IntegerProperty onHoldCountProperty() { return onHoldCount; }

    public Integer getLoanedCount() { return loanedCount.get(); }
    public void setLoanedCount(Integer loanedCount) { this.loanedCount.set(loanedCount); }
    public IntegerProperty loanedCountProperty() { return loanedCount; }
}
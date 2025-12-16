package it.unicas.project.template.address.model;

import javafx.beans.property.*;

/**
 * Represents a library material (book, CD, etc.) for the JavaFX UI.
 * <p>
 * Provides JavaFX properties for TableView and other UI bindings.
 * Can be persisted using the DAO layer.
 * </p>
 */
public class Material {

    /** Unique identifier of the material (Primary Key) */
    private IntegerProperty idMaterial;

    /** Title of the material */
    private StringProperty title;

    /** Author of the material */
    private StringProperty author;

    /** Publication year of the material */
    private IntegerProperty year;

    /** ISBN of the material */
    private StringProperty ISBN;

    /** Type ID of the material (foreign key to MaterialType) */
    private IntegerProperty idMaterialType;

    /** Status of the material (available, loaned, holded, etc.) */
    private StringProperty material_status;

    /**
     * Default constructor.
     * Initializes all fields to default values.
     */
    public Material() {
        this(null, null, null, null, null, null, null);
    }

    /**
     * Constructs a Material with all fields specified.
     *
     * @param idMaterial material ID (primary key)
     * @param title title of the material
     * @param author author of the material
     * @param year publication year
     * @param ISBN ISBN code
     * @param idMaterialType type ID of the material
     * @param material_status status (available, loaned, etc.)
     */
    public Material(Integer idMaterial, String title, String author, Integer year,
                    String ISBN, Integer idMaterialType, String material_status) {
        if (idMaterial != null) {
            this.idMaterial = new SimpleIntegerProperty(idMaterial);
        } else {
            this.idMaterial = null;
        }

        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.year = new SimpleIntegerProperty(year != null ? year : 0);
        this.ISBN = new SimpleStringProperty(ISBN != null ? ISBN : "");
        this.idMaterialType = new SimpleIntegerProperty(idMaterialType != null ? idMaterialType : 0);
        this.material_status = new SimpleStringProperty(material_status);
    }

    /**
     * Constructor without material ID.
     * Useful when creating new materials before insertion into the database.
     */
    public Material(String title, String author, Integer year,
                    String ISBN, Integer idMaterialType, String material_status) {
        this(null, title, author, year, ISBN, idMaterialType, material_status);
    }


    /** Getters, setters, and JavaFX properties for UI binding */

    public Integer getIdMaterial() {
        if (idMaterial == null) idMaterial = new SimpleIntegerProperty(-1);
        return idMaterial.get();
    }

    public void setIdMaterial(Integer idMaterial) {
        if (this.idMaterial == null) this.idMaterial = new SimpleIntegerProperty();
        this.idMaterial.set(idMaterial);
    }

    public IntegerProperty idMaterialProperty() {
        if (idMaterial == null) idMaterial = new SimpleIntegerProperty();
        return idMaterial;
    }

    public String getTitle() { return title.get(); }
    public void setTitle(String title) { this.title.set(title); }
    public StringProperty titleProperty() { return title; }

    public String getAuthor() { return author.get(); }
    public void setAuthor(String author) { this.author.set(author); }
    public StringProperty authorProperty() { return author; }

    public Integer getYear() { return year.get(); }
    public void setYear(Integer year) { this.year.set(year); }
    public IntegerProperty yearProperty() { return year; }

    public String getISBN() { return ISBN.get(); }
    public void setISBN(String ISBN) { this.ISBN.set(ISBN); }
    public StringProperty ISBNProperty() { return ISBN; }

    public Integer getIdMaterialType() { return idMaterialType.get(); }
    public void setIdMaterialType(Integer idMaterialType) { this.idMaterialType.set(idMaterialType); }
    public IntegerProperty idMaterialTypeProperty() { return idMaterialType; }

    public String getMaterial_status() { return material_status.get(); }
    public void setMaterial_status(String material_status) { this.material_status.set(material_status); }
    public StringProperty material_statusProperty() { return material_status; }

    /**
     * Returns a string representation of the material.
     *
     * @return material title and ID
     */
    @Override
    public String toString() {
        return title.get() + " (" + getIdMaterial() + ")";
    }
}
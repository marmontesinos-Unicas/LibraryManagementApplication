package it.unicas.project.template.address.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class for Material - JavaFX version
 */
public class Material {

    private IntegerProperty idMaterial;       // PK
    private StringProperty title;
    private StringProperty author;
    private IntegerProperty year;
    private StringProperty ISBN;
    private IntegerProperty idMaterialType;
    private StringProperty material_status;

    /**
     * Default constructor
     */
    public Material() {
        this(null, null, null, null, null, null, null);
    }

    /**
     * Constructor with all fields
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
        this.ISBN = new SimpleStringProperty(ISBN != null ? ISBN : ""); //this.ISBN = new SimpleStringProperty(ISBN);
        this.idMaterialType = new SimpleIntegerProperty(idMaterialType != null ? idMaterialType : 0);
        this.material_status = new SimpleStringProperty(material_status);
    }

    /**
     * Constructor without idMaterial
     */
    public Material(String title, String author, Integer year,
                    String ISBN, Integer idMaterialType, String material_status) {
        this(null, title, author, year, ISBN, idMaterialType, material_status);
    }

    // GETTERS & SETTERS
    public Integer getIdMaterial() {
        if (idMaterial == null){
            idMaterial = new SimpleIntegerProperty(-1);
        }
        return idMaterial.get();
    }

    public void setIdMaterial(Integer idMaterial) {
        if (this.idMaterial == null){
            this.idMaterial = new SimpleIntegerProperty();
        }
        this.idMaterial.set(idMaterial);
    }

    public IntegerProperty idMaterialProperty() {
        if (idMaterial == null){
            idMaterial = new SimpleIntegerProperty();
        }
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

    @Override
    public String toString() {
        return title.get() + " (" + getIdMaterial() + ")";
    }
}

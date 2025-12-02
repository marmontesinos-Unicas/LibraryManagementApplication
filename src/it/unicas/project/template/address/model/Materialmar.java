package it.unicas.project.template.address.model;

public class Materialmar {

    private Integer idMaterial;
    private String title;
    private String author;
    private Integer year;
    private String ISBN;
    private Integer idMaterialType;
    private String material_status;

    public Materialmar() {}

    public Materialmar(Integer idMaterial, String title, String author, Integer year,
                       String ISBN, Integer idMaterialType, String material_status) {
        this.idMaterial = idMaterial;
        this.title = title;
        this.author = author;
        this.year = year;
        this.ISBN = ISBN;
        this.idMaterialType = idMaterialType;
        this.material_status = material_status;
    }

    public Materialmar(String title, String author, Integer year,
                       String ISBN, Integer idGenre, Integer idMaterialType, String material_status) {
        this(null, title, author, year, ISBN, idMaterialType, material_status);
    }

    // GETTERS & SETTERS
    public Integer getIdMaterial() { return idMaterial; }
    public void setIdMaterial(Integer idMaterial) { this.idMaterial = idMaterial; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getISBN() { return ISBN; }
    public void setISBN(String ISBN) { this.ISBN = ISBN; }

    public Integer getIdMaterialType() { return idMaterialType; }
    public void setIdMaterialType(Integer idMaterialType) { this.idMaterialType = idMaterialType; }

    public String getMaterial_status() { return material_status; }
    public void setMaterial_status(String material_status) { this.material_status = material_status; }

    @Override
    public String toString() {
        return title + " (" + idMaterial + ")";
    }
}

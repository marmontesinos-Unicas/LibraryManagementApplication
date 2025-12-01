package it.unicas.project.template.address.model;

public class Hold {

    private Integer idHold;
    private Integer idUser;
    private Integer idMaterial;
    private String hold_date;
    private String hold_status;

    public Hold() {}

    public Hold(Integer idHold, Integer idUser, Integer idMaterial,
                String hold_date, String hold_status) {
        this.idHold = idHold;
        this.idUser = idUser;
        this.idMaterial = idMaterial;
        this.hold_date = hold_date;
        this.hold_status = hold_status;
    }

    public Hold(Integer idUser, Integer idMaterial,
                String hold_date, String hold_status) {
        this(null, idUser, idMaterial, hold_date, hold_status);
    }

    // GETTERS & SETTERS
    public Integer getIdHold() { return idHold; }
    public void setIdHold(Integer idHold) { this.idHold = idHold; }

    public Integer getIdUser() { return idUser; }
    public void setIdUser(Integer idUser) { this.idUser = idUser; }

    public Integer getIdMaterial() { return idMaterial; }
    public void setIdMaterial(Integer idMaterial) { this.idMaterial = idMaterial; }

    public String getHold_date() { return hold_date; }
    public void setHold_date(String hold_date) { this.hold_date = hold_date; }

    public String getHold_status() { return hold_status; }
    public void setHold_status(String hold_status) { this.hold_status = hold_status; }

    @Override
    public String toString() {
        return "Hold " + idHold;
    }
}
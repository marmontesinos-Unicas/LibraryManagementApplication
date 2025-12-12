package it.unicas.project.template.address.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.time.LocalDateTime;

public class Hold {

    private IntegerProperty idHold;
    private IntegerProperty idUser;
    private IntegerProperty idMaterial;
    private ObjectProperty<LocalDateTime> hold_date;

    // Constructor por defecto
    public Hold() {
        this(null, null, null, null);
    }

    // Constructor completo
    public Hold(Integer idHold, Integer idUser, Integer idMaterial, LocalDateTime hold_date) {
        this.idHold = (idHold != null) ? new SimpleIntegerProperty(idHold) : new SimpleIntegerProperty(-1);
        this.idUser = (idUser != null) ? new SimpleIntegerProperty(idUser) : new SimpleIntegerProperty(-1);
        this.idMaterial = (idMaterial != null) ? new SimpleIntegerProperty(idMaterial) : new SimpleIntegerProperty(-1);
        this.hold_date = new SimpleObjectProperty<>(hold_date);
    }


    // Constructor sin idHold
    public Hold(Integer idUser, Integer idMaterial, LocalDateTime hold_date) {
        this(null, idUser, idMaterial, hold_date);
    }

    // GETTERS & SETTERS
    public Integer getIdHold() {
        if (idHold == null) idHold = new SimpleIntegerProperty(-1);
        return idHold.get();
    }

    public void setIdHold(Integer idHold) {
        if (this.idHold == null) this.idHold = new SimpleIntegerProperty();
        this.idHold.set(idHold);
    }

    public IntegerProperty idHoldProperty() {
        if (idHold == null) idHold = new SimpleIntegerProperty();
        return idHold;
    }

    public Integer getIdUser() { return idUser.get(); }
    public void setIdUser(Integer idUser) { this.idUser.set(idUser); }
    public IntegerProperty idUserProperty() { return idUser; }

    public Integer getIdMaterial() { return idMaterial.get(); }
    public void setIdMaterial(Integer idMaterial) { this.idMaterial.set(idMaterial); }
    public IntegerProperty idMaterialProperty() { return idMaterial; }

    public LocalDateTime getHold_date() { return hold_date.get(); }
    public void setHold_date(LocalDateTime hold_date) { this.hold_date.set(hold_date); }
    public ObjectProperty<LocalDateTime> holdDateProperty() { return hold_date; }

    @Override
    public String toString() {
        return "Hold " + getIdHold();
    }
}

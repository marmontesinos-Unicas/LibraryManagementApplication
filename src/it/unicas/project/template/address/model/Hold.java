package it.unicas.project.template.address.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.time.LocalDateTime;
import java.util.Objects;

public class Hold {

    private final IntegerProperty idHold = new SimpleIntegerProperty(-1);
    private final IntegerProperty idUser = new SimpleIntegerProperty(-1);
    private final IntegerProperty idMaterial = new SimpleIntegerProperty(-1);
    private final ObjectProperty<LocalDateTime> hold_date = new SimpleObjectProperty<>(null);

    // Constructor por defecto
    public Hold() {
        // defaults already set (-1, -1, -1, null)
    }
    // Constructor completo
    public Hold(Integer idHold, Integer idUser, Integer idMaterial, LocalDateTime hold_date) {
        this.idHold.set(idHold != null ? idHold : -1);
        this.idUser.set(idUser != null ? idUser : -1);
        this.idMaterial.set(idMaterial != null ? idMaterial : -1);
        this.hold_date.set(hold_date);
    }

    // Constructor sin idHold
    public Hold(Integer idUser, Integer idMaterial, LocalDateTime hold_date) {
        this(null, idUser, idMaterial, hold_date);
    }

    /* getters and setters (return -1 when not set) */
    public int getIdHold() { return idHold.get(); }
    public void setIdHold(int idHold) { this.idHold.set(idHold); }
    public IntegerProperty idHoldProperty() { return idHold; }

    public int getIdUser() { return idUser.get(); }
    public void setIdUser(int idUser) { this.idUser.set(idUser); }
    public IntegerProperty idUserProperty() { return idUser; }

    public int getIdMaterial() { return idMaterial.get(); }
    public void setIdMaterial(int idMaterial) { this.idMaterial.set(idMaterial); }
    public IntegerProperty idMaterialProperty() { return idMaterial; }

    public LocalDateTime getHold_date() { return hold_date.get(); }
    public void setHold_date(LocalDateTime hold_date) { this.hold_date.set(hold_date); }
    public ObjectProperty<LocalDateTime> holdDateProperty() { return hold_date; }

    @Override
    public String toString() {
        return "Hold{" +
                "idHold=" + getIdHold() +
                ", idUser=" + getIdUser() +
                ", idMaterial=" + getIdMaterial() +
                ", hold_date=" + getHold_date() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hold)) return false;
        Hold hold = (Hold) o;
        return getIdHold() == hold.getIdHold();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdHold());
    }
}

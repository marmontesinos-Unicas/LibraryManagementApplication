package it.unicas.project.template.address.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a hold placed by a user on a material in the library system.
 * <p>
 * This class is used as a domain entity and can be persisted via the DAO layer.
 * It also provides JavaFX properties for TableView bindings in the UI.
 * </p>
 */
public class Hold {

    /** Unique identifier of the hold */
    private final IntegerProperty idHold = new SimpleIntegerProperty(-1);

    /** ID of the user who placed the hold */
    private final IntegerProperty idUser = new SimpleIntegerProperty(-1);

    /** ID of the material being held */
    private final IntegerProperty idMaterial = new SimpleIntegerProperty(-1);

    /** Date and time when the hold was placed */
    private final ObjectProperty<LocalDateTime> hold_date = new SimpleObjectProperty<>(null);

    /**
     * No-argument constructor.
     * Initializes all fields with default values.
     */
    public Hold() {
        // Defaults already set (-1, -1, -1, null)
    }

    /**
     * Constructs a Hold instance with all fields specified.
     *
     * @param idHold unique hold ID (can be null)
     * @param idUser user ID placing the hold (can be null)
     * @param idMaterial material ID being held (can be null)
     * @param hold_date date and time the hold was placed
     */
    public Hold(Integer idHold, Integer idUser, Integer idMaterial, LocalDateTime hold_date) {
        this.idHold.set(idHold != null ? idHold : -1);
        this.idUser.set(idUser != null ? idUser : -1);
        this.idMaterial.set(idMaterial != null ? idMaterial : -1);
        this.hold_date.set(hold_date);
    }

    /**
     * Constructs a Hold instance without specifying an ID.
     *
     * @param idUser user ID placing the hold
     * @param idMaterial material ID being held
     * @param hold_date date and time the hold was placed
     */
    public Hold(Integer idUser, Integer idMaterial, LocalDateTime hold_date) {
        this(null, idUser, idMaterial, hold_date);
    }

    /** Getters and setters with JavaFX properties for UI binding */

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

    /**
     * Returns a string representation of the hold.
     *
     * @return string with hold ID, user ID, material ID, and hold date
     */
    @Override
    public String toString() {
        return "Hold{" +
                "idHold=" + getIdHold() +
                ", idUser=" + getIdUser() +
                ", idMaterial=" + getIdMaterial() +
                ", hold_date=" + getHold_date() +
                '}';
    }

    /**
     * Two holds are considered equal if they have the same ID.
     *
     * @param o object to compare
     * @return true if IDs match
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hold)) return false;
        Hold hold = (Hold) o;
        return getIdHold() == hold.getIdHold();
    }

    /**
     * Hash code based on hold ID.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(getIdHold());
    }
}
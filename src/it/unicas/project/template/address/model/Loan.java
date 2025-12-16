package it.unicas.project.template.address.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

/**
 * Represents a loan of a material by a user in the library system.
 * <p>
 * This class is used as a domain entity and can be persisted via the DAO layer.
 * It also provides JavaFX properties for TableView bindings in the UI.
 * </p>
 */
public class Loan {

    /** Unique identifier of the loan */
    private IntegerProperty idLoan;

    /** ID of the user who borrowed the material */
    private IntegerProperty idUser;

    /** ID of the material being borrowed */
    private IntegerProperty idMaterial;

    /** Date and time when the loan started */
    private ObjectProperty<LocalDateTime> start_date;

    /** Date and time when the loan is due */
    private ObjectProperty<LocalDateTime> due_date;

    /** Date and time when the material was returned */
    private ObjectProperty<LocalDateTime> return_date;

    /**
     * Default constructor.
     * Initializes all fields with default values.
     */
    public Loan() {
        this(null, null, null, null, null, null);
    }

    /**
     * Constructs a Loan instance with all fields specified.
     *
     * @param idLoan unique loan ID (can be null)
     * @param idUser user ID borrowing the material (can be null)
     * @param idMaterial material ID being borrowed (can be null)
     * @param start_date start date and time of the loan
     * @param due_date due date and time of the loan
     * @param return_date return date and time of the material
     */
    public Loan(Integer idLoan, Integer idUser, Integer idMaterial,
                LocalDateTime start_date, LocalDateTime due_date, LocalDateTime return_date) {
        this.idLoan = idLoan != null ? new SimpleIntegerProperty(idLoan) : new SimpleIntegerProperty(-1);
        this.idUser = idUser != null ? new SimpleIntegerProperty(idUser) : new SimpleIntegerProperty(-1);
        this.idMaterial = idMaterial != null ? new SimpleIntegerProperty(idMaterial) : new SimpleIntegerProperty(-1);
        this.start_date = new SimpleObjectProperty<>(start_date);
        this.due_date = new SimpleObjectProperty<>(due_date);
        this.return_date = new SimpleObjectProperty<>(return_date);
    }

    /**
     * Constructs a Loan instance without specifying the loan ID.
     * Typically used for creating new loans before insertion into the database.
     *
     * @param idUser user ID borrowing the material
     * @param idMaterial material ID being borrowed
     * @param start_date start date and time of the loan
     * @param due_date due date and time of the loan
     * @param return_date return date and time of the material
     */
    public Loan(Integer idUser, Integer idMaterial,
                LocalDateTime start_date, LocalDateTime due_date, LocalDateTime return_date) {
        this(null, idUser, idMaterial, start_date, due_date, return_date);
    }

    /** Getters, setters, and JavaFX properties for UI binding */

    public Integer getIdLoan() {
        if (idLoan == null) idLoan = new SimpleIntegerProperty(-1);
        return idLoan.get();
    }
    public void setIdLoan(Integer idLoan) {
        if (this.idLoan == null) this.idLoan = new SimpleIntegerProperty();
        this.idLoan.set(idLoan);
    }
    public IntegerProperty idLoanProperty() {
        if (idLoan == null) idLoan = new SimpleIntegerProperty();
        return idLoan;
    }

    public Integer getIdUser() { return idUser.get(); }
    public void setIdUser(Integer idUser) { this.idUser.set(idUser); }
    public IntegerProperty idUserProperty() { return idUser; }

    public Integer getIdMaterial() { return idMaterial.get(); }
    public void setIdMaterial(Integer idMaterial) { this.idMaterial.set(idMaterial); }
    public IntegerProperty idMaterialProperty() { return idMaterial; }

    public LocalDateTime getStart_date() { return start_date.get(); }
    public void setStart_date(LocalDateTime start_date) { this.start_date.set(start_date); }
    public ObjectProperty<LocalDateTime> startDateProperty() { return start_date; }

    public LocalDateTime getDue_date() { return due_date.get(); }
    public void setDue_date(LocalDateTime due_date) { this.due_date.set(due_date); }
    public ObjectProperty<LocalDateTime> dueDateProperty() { return due_date; }

    public LocalDateTime getReturn_date() { return return_date.get(); }
    public void setReturn_date(LocalDateTime return_date) { this.return_date.set(return_date); }
    public ObjectProperty<LocalDateTime> returnDateProperty() { return return_date; }

    /**
     * Returns a string representation of the loan.
     *
     * @return string containing the loan ID
     */
    @Override
    public String toString() {
        return "Loan " + getIdLoan();
    }
}
package it.unicas.project.template.address.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import java.time.LocalDateTime;

public class Loan {

    private IntegerProperty idLoan;
    private IntegerProperty idUser;
    private IntegerProperty idMaterial;
    private ObjectProperty<LocalDateTime> start_date;
    private ObjectProperty<LocalDateTime> due_date;
    private ObjectProperty<LocalDateTime> return_date;

    // Constructor por defecto
    public Loan() {
        this(null, null, null, null, null, null);
    }

    // Constructor completo
    public Loan(Integer idLoan, Integer idUser, Integer idMaterial,
                LocalDateTime start_date, LocalDateTime due_date, LocalDateTime return_date) {
        this.idLoan = idLoan != null ? new SimpleIntegerProperty(idLoan) : new SimpleIntegerProperty(-1);
        this.idUser = idUser != null ? new SimpleIntegerProperty(idUser) : new SimpleIntegerProperty(-1);
        this.idMaterial = idMaterial != null ? new SimpleIntegerProperty(idMaterial) : new SimpleIntegerProperty(-1);
        this.start_date = new SimpleObjectProperty<>(start_date);
        this.due_date = new SimpleObjectProperty<>(due_date);
        this.return_date = new SimpleObjectProperty<>(return_date);
    }


    // Constructor sin idLoan (para insertar nuevos loans)
    public Loan(Integer idUser, Integer idMaterial,
                LocalDateTime start_date, LocalDateTime due_date, LocalDateTime return_date) {
        this(null, idUser, idMaterial, start_date, due_date, return_date);
    }

    // GETTERS & SETTERS
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

    @Override
    public String toString() {
        return "Loan " + getIdLoan();
    }
}

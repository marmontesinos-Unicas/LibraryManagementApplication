package it.unicas.project.template.address.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LoanRow {
    private final int idLoan;
    private final SimpleStringProperty materialType;
    private final SimpleStringProperty title;
    private final SimpleStringProperty user;
    private final SimpleStringProperty dueDate;
    private final SimpleStringProperty delayed;

    public LoanRow(int idLoan, String materialType, String title, String user, String dueDate, String delayed) {
        this.idLoan = idLoan;
        this.materialType = new SimpleStringProperty(materialType);
        this.title = new SimpleStringProperty(title);
        this.user = new SimpleStringProperty(user);
        this.dueDate = new SimpleStringProperty(dueDate);
        this.delayed = new SimpleStringProperty(delayed);
    }

    public int getIdLoan() { return idLoan; }

    public StringProperty materialTypeProperty() { return materialType; }
    public StringProperty titleProperty() { return title; }
    public StringProperty userProperty() { return user; }
    public StringProperty dueDateProperty() { return dueDate; }
    public StringProperty delayedProperty() { return delayed; }
}

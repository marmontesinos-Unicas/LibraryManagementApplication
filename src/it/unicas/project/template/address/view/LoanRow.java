package it.unicas.project.template.address.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDateTime;

public class LoanRow {
    private final int idLoan;
    private final SimpleStringProperty materialType;
    private final SimpleStringProperty title;
    private final SimpleStringProperty author;
    private final SimpleStringProperty isbn;
    private final SimpleStringProperty user;
    private final SimpleStringProperty dueDate;
    private final SimpleStringProperty delayed;

    public LoanRow(int idLoan, String materialType, String title, String author, String isbn, String user, String dueDate, String delayed) {
        this.idLoan = idLoan;
        this.materialType = new SimpleStringProperty(materialType);
        this.title = new SimpleStringProperty(title);
        this.author = new SimpleStringProperty(author);
        this.isbn = new SimpleStringProperty(isbn);
        this.user = new SimpleStringProperty(user);
        this.dueDate = new SimpleStringProperty(dueDate);
        this.delayed = new SimpleStringProperty(delayed);
    }

    // Getter methods for each property
    public int getIdLoan() { return idLoan; }
    public SimpleStringProperty materialTypeProperty() { return materialType; }
    public SimpleStringProperty titleProperty() { return title; }
    public SimpleStringProperty authorProperty() { return author; }
    public SimpleStringProperty isbnProperty() { return isbn; }
    public SimpleStringProperty userProperty() { return user; }
    public SimpleStringProperty dueDateProperty() { return dueDate; }
    public SimpleStringProperty delayedProperty() { return delayed; }

    public String getTitle() { return title.get(); }
    public String getUser() { return user.get(); }

    public LocalDateTime getDueDateAsLocalDate() {
        if (dueDate.get() == null || dueDate.get().equals("—")) return LocalDateTime.MAX;
        return LocalDateTime.parse(dueDate.get() + "T00:00:00");
    }
}

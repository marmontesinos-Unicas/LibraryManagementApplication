package it.unicas.project.template.address.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDateTime;

/**
 * Represents a single row in the loans TableView.
 * Holds all necessary properties for displaying loan information in the UI.
 */
public class LoanRow {

    private final int idLoan;                     // Unique identifier for the loan
    private final SimpleStringProperty materialType; // Type of material (e.g., Book, CD)
    private final SimpleStringProperty title;        // Title of the material
    private final SimpleStringProperty author;       // Author of the material
    private final SimpleStringProperty isbn;         // ISBN of the material
    private final SimpleStringProperty user;         // Full name of the user
    private final SimpleStringProperty dueDate;      // Due date of the loan
    private final SimpleStringProperty delayed;      // Indicates if the loan is delayed ("Yes"/"No")

    /**
     * Constructs a LoanRow object with all relevant information.
     * @param idLoan Unique loan ID
     * @param materialType Type of material (Book, CD, etc.)
     * @param title Title of the material
     * @param author Author of the material
     * @param isbn ISBN of the material
     * @param user Full name of the user
     * @param dueDate Due date as string
     * @param delayed "Yes" if loan is delayed, "No" otherwise
     */
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

    /** Returns the unique loan ID. */
    public int getIdLoan() { return idLoan; }

    /** Returns the property for material type for TableView binding. */
    public StringProperty materialTypeProperty() { return materialType; }

    /** Returns the property for title for TableView binding. */
    public StringProperty titleProperty() { return title; }

    /** Returns the property for author for TableView binding. */
    public StringProperty authorProperty() { return author; }

    /** Returns the property for ISBN for TableView binding. */
    public StringProperty isbnProperty() { return isbn; }

    /** Returns the property for user name for TableView binding. */
    public StringProperty userProperty() { return user; }

    /** Returns the property for due date for TableView binding. */
    public StringProperty dueDateProperty() { return dueDate; }

    /** Returns the property for delayed status for TableView binding. */
    public StringProperty delayedProperty() { return delayed; }

    /** Returns the title of the material. */
    public String getTitle() { return title.get(); }

    /** Returns the full name of the user. */
    public String getUser() { return user.get(); }

    /**
     * Converts the due date string to a LocalDateTime object.
     * @return LocalDateTime representation of the due date, or MAX if invalid
     */
    public LocalDateTime getDueDateAsLocalDate() {
        if (dueDate.get() == null || dueDate.get().equals("—")) return LocalDateTime.MAX;
        return LocalDateTime.parse(dueDate.get() + "T00:00:00");
    }
}

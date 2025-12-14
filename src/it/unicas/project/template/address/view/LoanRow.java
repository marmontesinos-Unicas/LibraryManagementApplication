package it.unicas.project.template.address.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents a single row in the loans TableView.
 * Holds all necessary properties for displaying loan information in the UI.
 */
public class LoanRow {

    private final int idLoan;                     // Unique identifier for the loan
    private final SimpleStringProperty materialType; // Type of material (e.g., Book, CD)
    private final SimpleStringProperty title;        // Title of the material
    private final SimpleStringProperty user;         // Full name of the user
    private final SimpleStringProperty dueDate;      // Due date of the loan
    private final SimpleStringProperty delayed;      // Indicates if the loan is delayed ("Yes"/"No")

    /**
     * Constructs a LoanRow object with all relevant information.
     * @param idLoan Unique loan ID
     * @param materialType Type of material (Book, CD, etc.)
     * @param title Title of the material
     * @param user Full name of the user
     * @param dueDate Due date as string
     * @param delayed "Yes" if loan is delayed, "No" otherwise
     */
    public LoanRow(int idLoan, String materialType, String title, String user, String dueDate, String delayed) {
        this.idLoan = idLoan;
        this.materialType = new SimpleStringProperty(materialType);
        this.title = new SimpleStringProperty(title);
        this.user = new SimpleStringProperty(user);
        this.dueDate = new SimpleStringProperty(dueDate);
        this.delayed = new SimpleStringProperty(delayed);
    }

    /**
     * Returns the unique loan ID.
     * @return loan ID
     */
    public int getIdLoan() { return idLoan; }

    /**
     * Returns the property for material type for TableView binding.
     * @return material type property
     */
    public StringProperty materialTypeProperty() { return materialType; }

    /**
     * Returns the property for title for TableView binding.
     * @return title property
     */
    public StringProperty titleProperty() { return title; }

    /**
     * Returns the property for user name for TableView binding.
     * @return user property
     */
    public StringProperty userProperty() { return user; }

    /**
     * Returns the property for due date for TableView binding.
     * @return due date property
     */
    public StringProperty dueDateProperty() { return dueDate; }

    /**
     * Returns the property for delayed status for TableView binding.
     * @return delayed property
     */
    public StringProperty delayedProperty() { return delayed; }
}

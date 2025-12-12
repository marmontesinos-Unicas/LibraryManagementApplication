package it.unicas.project.template.address.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents a single row in the holds TableView.
 * Holds all necessary properties for displaying hold information in the UI.
 */
public class HoldRow {

    private final int idHold;                     // Unique identifier for the hold
    private final SimpleStringProperty title;     // Title of the material on hold
    private final SimpleStringProperty maxDate;   // Maximum hold date as string

    /**
     * Constructs a HoldRow object with all relevant information.
     * @param idHold Unique hold ID
     * @param title Title of the material on hold
     * @param maxDate Maximum date until the hold is valid
     */
    public HoldRow(int idHold, String title, String maxDate) {
        this.idHold = idHold;
        this.title = new SimpleStringProperty(title);
        this.maxDate = new SimpleStringProperty(maxDate);
    }

    /**
     * Returns the unique hold ID.
     * @return hold ID
     */
    public int getIdHold() {
        return idHold;
    }

    /**
     * Returns the property for title for TableView binding.
     * @return title property
     */
    public StringProperty titleProperty() {
        return title;
    }

    /**
     * Returns the property for maximum hold date for TableView binding.
     * @return maxDate property
     */
    public StringProperty maxDateProperty() {
        return maxDate;
    }

    /**
     * Returns the title of the material on hold.
     * @return title
     */
    public String getTitle() {
        return title.get();
    }

    /**
     * Returns the maximum date until the hold is valid.
     * @return maxDate
     */
    public String getMaxDate() {
        return maxDate.get();
    }
}

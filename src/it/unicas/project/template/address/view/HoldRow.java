package it.unicas.project.template.address.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class HoldRow {
    private final int idHold;
    private final SimpleStringProperty title;
    private final SimpleStringProperty maxDate;

    public HoldRow(int idHold, String title, String maxDate) {
        this.idHold = idHold;
        this.title = new SimpleStringProperty(title);
        this.maxDate = new SimpleStringProperty(maxDate);
    }

    public int getIdHold() {
        return idHold;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty maxDateProperty() {
        return maxDate;
    }

    public String getTitle() {
        return title.get();
    }

    public String getMaxDate() {
        return maxDate.get();
    }
}

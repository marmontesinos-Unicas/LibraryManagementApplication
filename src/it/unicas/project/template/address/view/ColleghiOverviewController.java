package it.unicas.project.template.address.view;

import it.unicas.project.template.address.model.Amici;
import it.unicas.project.template.address.model.dao.mysql.ColleghiDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.DAOException;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.util.List;

public class ColleghiOverviewController {
    @FXML
    private TableView<Amici> colleghiTableView;
    @FXML
    private TableColumn<Amici, String> nomeColumn;
    @FXML
    private TableColumn<Amici, String> cognomeColumn;

    @FXML
    private Label nomeLabel;
    @FXML
    private Label cognomeLabel;
    @FXML
    private Label telefonoLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label compleannoLabel;

    // Reference to the main application (no se usa más)
    // private MainApp mainApp;

    public ColleghiOverviewController() {
    }

    @FXML
    private void initialize() {
        nomeColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Amici, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Amici, String> p) {
                return p.getValue().nomeProperty();
            }
        });
        cognomeColumn.setCellValueFactory(cellData -> cellData.getValue().cognomeProperty());

        // Clear Amici details
        showColleghiDetails(null);

        colleghiTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showColleghiDetails(newValue));
        colleghiTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> System.out.println("Click on the table"));
    }

    // Ya no necesitamos setMainApp
    /*
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        colleghiTableView.setItems(mainApp.getColleghiData());
    }
    */

    private void showColleghiDetails(Amici colleghi) {
        if (colleghi != null) {
            nomeLabel.setText(colleghi.getNome());
            cognomeLabel.setText(colleghi.getCognome());
            telefonoLabel.setText(colleghi.getTelefono());
            emailLabel.setText(colleghi.getEmail());
            compleannoLabel.setText(colleghi.getCompleanno());
        } else {
            nomeLabel.setText("");
            cognomeLabel.setText("");
            telefonoLabel.setText("");
            emailLabel.setText("");
            compleannoLabel.setText("");
        }
    }

    @FXML
    private void handleDeleteColleghi() {
        int selectedIndex = colleghiTableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            Amici colleghi = colleghiTableView.getItems().get(selectedIndex);
            try {
                ColleghiDAOMySQLImpl.getInstance().delete(colleghi);
                colleghiTableView.getItems().remove(selectedIndex); // solo eliminamos de la tabla
            } catch (DAOException e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error during DB interaction");
                alert.setHeaderText("Error during delete ...");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Amici Selected");
            alert.setContentText("Please select a Amici in the table.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleNewColleghi() {
        Amici tempColleghi = new Amici();
        boolean okClicked = true; // Puedes implementar un diálogo aquí si quieres

        if (okClicked) {
            try {
                ColleghiDAOMySQLImpl.getInstance().insert(tempColleghi);
                colleghiTableView.getItems().add(tempColleghi);
            } catch (DAOException e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error during DB interaction");
                alert.setHeaderText("Error during insert ...");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleSearchColleghi() {
        Amici tempColleghi = new Amici("", "", "", "", "", null);
        boolean okClicked = true; // Implementa un diálogo si quieres

        if (okClicked) {
            try {
                List<Amici> list = ColleghiDAOMySQLImpl.getInstance().select(tempColleghi);
                colleghiTableView.getItems().clear();
                colleghiTableView.getItems().addAll(list);
            } catch (DAOException e) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error during DB interaction");
                alert.setHeaderText("Error during search ...");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleEditColleghi() {
        Amici selectedColleghi = colleghiTableView.getSelectionModel().getSelectedItem();
        if (selectedColleghi != null) {
            boolean okClicked = true; // Implementa un diálogo si quieres
            if (okClicked) {
                try {
                    ColleghiDAOMySQLImpl.getInstance().update(selectedColleghi);
                    showColleghiDetails(selectedColleghi);
                } catch (DAOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Amici Selected");
            alert.setContentText("Please select a Amici into the table.");
            alert.showAndWait();
        }
    }
}

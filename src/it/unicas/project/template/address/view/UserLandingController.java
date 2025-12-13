package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class UserLandingController {

    private MainApp mainApp;

    @FXML
    private ListView<String> myLoansList;
    @FXML
    private ListView<String> myReservationsList;
    @FXML
    private Button deleteLoanButton;
    @FXML
    private Button deleteReservationButton;
    @FXML
    private Button searchButton;
    @FXML
    private Button notificationsButton;
    @FXML
    private Button closeButton;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        myLoansList.setItems(mainApp.getUserLoans());
        myReservationsList.setItems(mainApp.getUserReservations());
    }

    @FXML
    protected void handleSearch(ActionEvent event) {
        if (mainApp != null) {
            mainApp.showUserCatalog();
        } else {
            System.err.println("mainApp is null - call setMainApp(...) when loading the user view.");
        }
    }

    @FXML
    protected void handleNotifications(ActionEvent event) {
        System.out.println("Acción: Ver notificaciones (Pendiente de implementación).");
    }

    @FXML
    protected void handleDeleteLoan(ActionEvent event) {
        String selectedLoan = myLoansList.getSelectionModel().getSelectedItem();
        if (selectedLoan != null) {
            System.out.println("Eliminar préstamo: " + selectedLoan);
            mainApp.getUserLoans().remove(selectedLoan);
        }
    }

    @FXML
    protected void handleDeleteReservation(ActionEvent event) {
        String selectedReservation = myReservationsList.getSelectionModel().getSelectedItem();
        if (selectedReservation != null) {
            System.out.println("Eliminar reserva: " + selectedReservation);
            mainApp.getUserReservations().remove(selectedReservation);
        }
    }

    @FXML
    protected void handleCloseButton(ActionEvent event) {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}

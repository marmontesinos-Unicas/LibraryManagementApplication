package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

/**
 * Controller for the FXML interface of the user landing screen.
 * Contains the action methods for the main buttons and list views.
 */
public class UserLandingController {

    private MainApp mainApp;

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        // Example: Initialize data for the lists
        // myLoansList.setItems(mainApp.getLoanData());
        // myReservationsList.setItems(mainApp.getReservationData());
    }

    // References to FXML elements
    @FXML
    private ListView<String> myLoansList; // Assuming the list items are Strings for simplicity
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

    /**
     * Handles the action for the "Search" button.
     * Future logic: Open the view to perform searches in the catalog.
     * @param event The action event.
     */
    @FXML
    protected void handleSearch(ActionEvent event) {
        System.out.println("Acción: Búsqueda de material (Pendiente de implementación).");
        // The code to switch to the search screen will go here.
    }

    /**
     * Handles the action for the "Notifications" button.
     * Future logic: Open the view to see recent notifications.
     * @param event The action event.
     */
    @FXML
    protected void handleNotifications(ActionEvent event) {
        System.out.println("Acción: Ver notificaciones (Pendiente de implementación).");
        // The code to switch to the notifications screen will go here.
    }

    /**
     * Handles the action for the "Delete" button in "My loans".
     * Future logic: Delete the selected loan.
     * @param event The action event.
     */
    @FXML
    protected void handleDeleteLoan(ActionEvent event) {
        String selectedLoan = myLoansList.getSelectionModel().getSelectedItem();
        if (selectedLoan != null) {
            System.out.println("Acción: Eliminar préstamo seleccionado: " + selectedLoan + " (Pendiente de implementación).");
            // Logic to delete the selected loan will go here.
        } else {
            System.out.println("No hay préstamo seleccionado para eliminar.");
        }
    }

    /**
     * Handles the action for the "Delete" button in "My reservations".
     * Future logic: Delete the selected reservation.
     * @param event The action event.
     */
    @FXML
    protected void handleDeleteReservation(ActionEvent event) {
        String selectedReservation = myReservationsList.getSelectionModel().getSelectedItem();
        if (selectedReservation != null) {
            System.out.println("Acción: Eliminar reserva seleccionada: " + selectedReservation + " (Pendiente de implementación).");
            // Logic to delete the selected reservation will go here.
        } else {
            System.out.println("No hay reserva seleccionada para eliminar.");
        }
    }

    /**
     * Handles the action for the close button.
     * Closes the current window.
     * @param event The action event.
     */
    @FXML
    protected void handleCloseButton(ActionEvent event) {
        // Gets the window (Stage) from the button and closes it
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
        // Optional: call mainApp.handleExit() if necessary for the application lifecycle
        // mainApp.handleExit();
    }
}
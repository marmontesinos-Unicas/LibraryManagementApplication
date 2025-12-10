package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.service.NotificationsService;
import it.unicas.project.template.address.model.User;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class UserLandingController {

    private MainApp mainApp;

    // Notification Logic Fields
    private final NotificationsService notificationsService = new NotificationsService();
    private List<String> overdueNotifications;

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

        // Use the proper MainApp getter to retrieve the logged-in user
        User currentUser = mainApp.getLoggedUser();

        // Run the overdue check after the mainApp is set
        if (currentUser != null) {
            checkOverdueStatus(currentUser);
        }
    }

    /**
     * Executes the overdue check, retrieves the messages, and updates the button's appearance.
     */
    private void checkOverdueStatus(User currentUser) {

        try {
            // 1. Retrieve the list of formatted messages
            overdueNotifications = notificationsService.getFormattedNotifications(currentUser.getIdUser());
            int count = overdueNotifications.size();

            if (count > 0) {
                // Update button text and style
                notificationsButton.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-weight: bold;");
                notificationsButton.setText("Notifications (" + count + ")");
            } else {
                // Ensure default state
                notificationsButton.setStyle("");
                notificationsButton.setText("Notifications");
                overdueNotifications = null;
            }
        } catch (DAOException e) {
            System.err.println("CRITICAL ERROR: Could not load overdue status: " + e.getMessage());
            notificationsButton.setText("Notifications (Error)");
            overdueNotifications = null;
        }
    }

    /**
     * Handles the click on the Notifications button.
     */
    @FXML
    protected void handleNotifications(ActionEvent event) {
        // Pass the list of messages to the MainApp method for display
        mainApp.showNotificationsView(overdueNotifications);
    }

    @FXML
    protected void handleSearch(ActionEvent event) {
        System.out.println("Acción: Búsqueda de material (Pendiente de implementación).");
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
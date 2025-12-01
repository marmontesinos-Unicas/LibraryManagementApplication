package it.unicas.project.template.address.view;

import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.service.ServiceException;
import it.unicas.project.template.address.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class UserRegistrationController {

    // UI Elements (must match fx:id in your FXML file - Tarea #55)
    @FXML
    private TextField nameField;
    @FXML
    private TextField surnameField;
    @FXML
    private TextField nationalIDField; // User ID
    @FXML
    private TextField postalCodeField; // Required by User Story #42
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField emailField; // Assuming your User model requires email/username/password

    // Instance of the Service Layer (The bridge to the business logic)
    private UserService userService = new UserService();

    /**
     * Handles the 'Register' button click event.
     */
    @FXML
    private void handleRegisterUser() {
        // 1. Get data from UI fields
        String name = nameField.getText();
        String surname = surnameField.getText();
        String nationalID = nationalIDField.getText();
        String postalCode = postalCodeField.getText();

        // --- NOTE: Add logic to get username, password, and email based on your FXML ---
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();

        // 2. Build the User object (idUser=null as it's a new record)
        // idRole=1 is assumed for a default role
        User newUser = new User(null, name, surname, username, nationalID, password, email, 1);

        try {
            // 3. Call the Business Logic (Service Layer)
            userService.registerUser(newUser, postalCode);

            // 4. Success Feedback (Acceptance Criteria 3)
            showAlert("Success", "Registration Successful", "The user has been registered and can now borrow items.", AlertType.INFORMATION);

            // Clear fields after successful registration
            clearFields();

        } catch (ServiceException e) {
            // 5. Validation/Business Error Feedback (Acceptance Criteria 2)
            showAlert("Validation Error", "Cannot Register User", e.getMessage(), AlertType.ERROR);

        } catch (DAOException e) {
            // 6. System/DB Error Feedback (Acceptance Criteria 2)
            showAlert("System Error", "Database Operation Failed", "An internal error occurred: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Helper method to display JavaFX alerts.
     */
    private void showAlert(String title, String header, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Helper method to clear all input fields.
     */
    private void clearFields() {
        nameField.setText("");
        surnameField.setText("");
        nationalIDField.setText("");
        postalCodeField.setText("");
        usernameField.setText("");
        passwordField.setText("");
        emailField.setText("");
    }

    /**
     * Handles the 'Cancel' button click event.
     * Closes the registration dialog window.
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        // 1. Get the source of the event (the Button that was clicked)
        // 2. Get the scene and the stage from that source.

        // We cast the source to a Node (which is what Control extends) and get its scene and stage.
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

        // 3. Close the window
        stage.close();
    }
}
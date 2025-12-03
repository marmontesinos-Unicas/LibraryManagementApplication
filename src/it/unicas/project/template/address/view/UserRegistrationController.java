package it.unicas.project.template.address.view;

import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.service.UserServiceException;
import it.unicas.project.template.address.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.Window; // <-- added
import javafx.event.ActionEvent;
import java.time.LocalDate;

public class UserRegistrationController {

    // UI Elements (must match fx:id in your FXML file)
    @FXML
    private TextField nameField;
    @FXML
    private TextField surnameField;
    @FXML
    private TextField nationalIDField; // User ID
    @FXML
    private DatePicker birthdateField; // <-- changed type to DatePicker
    //@FXML
    //private TextField postalCodeField; // Required by User Story #42
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField; // changed to PasswordField to match FXML
    @FXML
    private TextField emailField; // Assuming your User model requires email/username/password
    @FXML
    public ComboBox roleComboBox;

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
        LocalDate birthdate = birthdateField.getValue(); // <-- obtain LocalDate directly

        // --- NOTE: Add logic to get username, password, and email based on your FXML ---
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();

        // Validate birthdate presence
        if (birthdate == null) {
            showAlert("Validation Error", "Missing Birthdate", "Birthdate is required.", AlertType.ERROR);
            return;
        }

        // 2. Build the User object (idUser=null as it's a new record)
        // idRole=2 is assumed for a default role (i.e. regular user)
        User newUser = new User(null, name, surname, username, nationalID, birthdate, password, email, 2);

        try {
            // 3. Call the Business Logic (Service Layer)
            userService.registerUser(newUser);

            // 4. Success Feedback (Acceptance Criteria 3)
            showAlert("Success", "Registration Successful", "The user has been registered and can now borrow items.", AlertType.INFORMATION);

            // Clear fields after successful registration
            clearFields();

        } catch (UserServiceException e) {

            //showAlert("Validation Error", "Could not perform the registration", "The registration of the new user could not be completed: " + e.getMessage(), AlertType.ERROR);

            // Map known service errors to more specific alerts
            String msg = e.getMessage() != null ? e.getMessage() : "";

            if (msg.contains("Name is mandatory") || msg.toLowerCase().contains("name") && msg.toLowerCase().contains("mandatory")) {
                showAlert("Validation Error", "Missing Name", "Please enter the user's name.", AlertType.ERROR);
            } else if (msg.contains("Surname is mandatory") || msg.toLowerCase().contains("surname")) {
                showAlert("Validation Error", "Missing Surname", "Please enter the user's surname.", AlertType.ERROR);
            } else if (msg.contains("National ID is mandatory") || msg.toLowerCase().contains("national id")) {
                showAlert("Validation Error", "Missing National ID", "Please enter the user's national ID.", AlertType.ERROR);
            } else if (msg.contains("Username is mandatory") || msg.toLowerCase().contains("username")) {
                showAlert("Validation Error", "Missing Username", "Please enter a username.", AlertType.ERROR);
            } else if (msg.contains("Password is mandatory") || msg.toLowerCase().contains("password is mandatory")) {
                showAlert("Validation Error", "Missing Password", "Please enter a password.", AlertType.ERROR);
            } else if (msg.toLowerCase().contains("password must be") || msg.toLowerCase().contains("at least 8")) {
                showAlert("Validation Error", "Invalid Password Format", "Password must be at least 8 characters long and include at least one uppercase letter and one number.", AlertType.ERROR);
            } else if (msg.contains("Birth Date is mandatory") || msg.toLowerCase().contains("birth date is mandatory")) {
                showAlert("Validation Error", "Missing Birthdate", "Please provide the user's birthdate.", AlertType.ERROR);
            } else if (msg.contains("yyyy-MM-dd") || msg.toLowerCase().contains("birth date must be")) {
                showAlert("Validation Error", "Invalid Birthdate Format", "Birthdate must be in yyyy-MM-dd format.", AlertType.ERROR);
            } else if (msg.toLowerCase().contains("already registered") || msg.toLowerCase().contains("already")) {
                showAlert("Duplicate Error", "National ID Already Registered", "The provided national ID is already registered in the system.", AlertType.ERROR);
            } else if (msg.contains("Role is mandatory") || msg.toLowerCase().contains("role") && msg.toLowerCase().contains("mandatory")) {
                showAlert("Validation Error", "Missing Role", "Please enter the user's role.", AlertType.ERROR);
            }
            else {
                // Fallback generic message
                showAlert("Validation Error", "Cannot Register User", msg.isEmpty() ? "Validation failed." : msg, AlertType.ERROR);
            }

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

        Window owner = null;
        if (nameField != null && nameField.getScene() != null) {
            owner = nameField.getScene().getWindow();
        }
        if (owner != null) {
            alert.initOwner(owner);
        }

        alert.showAndWait();
    }

    /**
     * Helper method to clear all input fields.
     */
    private void clearFields() {
        nameField.setText("");
        surnameField.setText("");
        nationalIDField.setText("");
        birthdateField.setValue(null);
        //postalCodeField.setText("");
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


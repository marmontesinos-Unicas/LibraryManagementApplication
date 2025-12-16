package it.unicas.project.template.address.view;

import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.service.UserServiceException;
import it.unicas.project.template.address.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.event.ActionEvent;
import java.time.LocalDate;

/**
 * Controller for the User Registration dialog.
 * Handles input validation, user creation, and interaction with the {@code UserService}.
 */
public class UserRegistrationController {

    // Reference back to the main UserManagementController
    private UserManagementController userManagementController;

    // UI Elements
    @FXML
    private TextField nameField;
    @FXML
    private TextField surnameField;
    @FXML
    private TextField nationalIDField; // User ID
    @FXML
    private DatePicker birthdateField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField emailField;
    @FXML
    public ComboBox<String> roleComboBox; // Changed to parameterized type for better safety

    // Instance of the Service Layer
    private UserService userService = new UserService();

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // This method is here for future expansion if a custom format is required.
        // It's good practice to have an initialize method for FXML controllers.
    }

    /**
     * Sets the reference to the main management controller.
     * This reference is used to refresh the user table in the main view after a successful registration.
     * @param userManagementController The controller managing the user table.
     */
    public void setUserManagementController(UserManagementController userManagementController) {
        this.userManagementController = userManagementController;
    }

    /**
     * Handles the 'Register' button click event.
     * Gathers data from the form, validates it via {@code UserService}, registers the new user,
     * and closes the dialog upon success.
     */
    @FXML
    private void handleRegisterUser() {

        String name = nameField.getText();
        String surname = surnameField.getText();
        String nationalID = nationalIDField.getText();
        LocalDate birthdate = birthdateField.getValue();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();
        String roleText = roleComboBox.getSelectionModel().getSelectedItem();
        // Simple mapping: 1 for Admin, 2 for User. Adjust this based on your database roles.
        Integer idRole = roleText != null && roleText.equals("Admin") ? 1 : 2;

        User newUser = new User(null, name, surname, username, nationalID, birthdate, password, email, idRole);

        try {
            userService.registerUser(newUser);

            showAlert("Success", "Registration Successful", "The user has been registered.", AlertType.INFORMATION);

            // 1. Tell the main controller to refresh its table (if the reference exists)
            if (userManagementController != null) {
                userManagementController.loadInitialUserData();
            }

            // 2. Close the registration dialog
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.close();


        } catch (UserServiceException e) {
            // Catches business logic and input validation errors from the service layer.
            String msg = e.getMessage() != null ? e.getMessage() : "";

            if (msg.contains("Name is mandatory")) { // || msg.toLowerCase().contains("name") && msg.toLowerCase().contains("mandatory")) {
                showAlert("Validation Error", "Missing Name", "Please enter the user's name.", AlertType.ERROR);
            } else if (msg.contains("Surname is mandatory") || msg.toLowerCase().contains("surname")) {
                showAlert("Validation Error", "Missing Surname", "Please enter the user's surname.", AlertType.ERROR);
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
            } else if (msg.contains("Role is mandatory") || msg.toLowerCase().contains("role") && msg.toLowerCase().contains("mandatory")) {
                showAlert("Validation Error", "Missing Role", "Please select a role.", AlertType.ERROR);
            } else if (msg.toLowerCase().contains("already registered") || msg.toLowerCase().contains("already")) {
                showAlert("Duplicate Error", "User Combination Already Registered", "The provided National ID and Role combination is already registered in the system.", AlertType.ERROR);
            } else if (msg.contains("National ID is mandatory") || msg.toLowerCase().contains("national id")) {
                showAlert("Validation Error", "Missing National ID", "Please enter the user's national ID.", AlertType.ERROR);
            } else {
                showAlert("Validation Error", "Cannot Register User", msg.isEmpty() ? "Validation failed." : msg, AlertType.ERROR);
            }

        } catch (DAOException e) {
            // Catches database/system errors
            showAlert("System Error", "Database Operation Failed", "An internal error occurred: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Handles the 'Cancel' button click event.
     * Closes the registration dialog window without registering the user.
     * @param event The action event triggering the cancel operation.
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    /**
     * Helper method to display various JavaFX alerts.
     * @param title The title of the alert window.
     * @param header The header text.
     * @param content The main content message.
     * @param type The type of alert (e.g., INFORMATION, ERROR).
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
     * Helper method to clear all input fields (currently unused but provided for completeness).
     */
    private void clearFields() {
        nameField.setText("");
        surnameField.setText("");
        nationalIDField.setText("");
        birthdateField.setValue(null);
        usernameField.setText("");
        passwordField.setText("");
        emailField.setText("");
        roleComboBox.getSelectionModel().clearSelection();
    }
}
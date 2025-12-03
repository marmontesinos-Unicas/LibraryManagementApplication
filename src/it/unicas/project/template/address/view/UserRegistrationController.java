package it.unicas.project.template.address.view;

// ... (rest of imports remain the same) ...
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

public class UserRegistrationController {

    // NEW FIELD: Reference back to the main UserManagementController
    private UserManagementController userManagementController;

    // UI Elements (must match fx:id in your FXML file)
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
     * NEW METHOD: Sets the reference to the main management controller.
     */
    public void setUserManagementController(UserManagementController userManagementController) {
        this.userManagementController = userManagementController;
    }


    /**
     * Handles the 'Register' button click event.
     */
    @FXML
    private void handleRegisterUser() {
        // ... (rest of data retrieval and validation logic remains the same) ...
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

        if (birthdate == null) {
            showAlert("Validation Error", "Missing Birthdate", "Birthdate is required.", AlertType.ERROR);
            return;
        }

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
            // ... (rest of error mapping and alert logic remains the same) ...
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
                showAlert("Validation Error", "Missing Role", "Please select a role.", AlertType.ERROR);
            }
            else {
                showAlert("Validation Error", "Cannot Register User", msg.isEmpty() ? "Validation failed." : msg, AlertType.ERROR);
            }

        } catch (DAOException e) {
            showAlert("System Error", "Database Operation Failed", "An internal error occurred: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Handles the 'Cancel' button click event.
     * Closes the registration dialog window.
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    // ... (rest of showAlert and clearFields helper methods remain the same) ...
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
        usernameField.setText("");
        passwordField.setText("");
        emailField.setText("");
        roleComboBox.getSelectionModel().clearSelection();
    }
}
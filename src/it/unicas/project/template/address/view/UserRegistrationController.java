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

public class UserRegistrationController {

    // Reference back to the main UserManagementController
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

    // Error labels
    @FXML private Label nameErrorLabel;
    @FXML private Label surnameErrorLabel;
    @FXML private Label nationalIDErrorLabel;
    @FXML private Label birthdateErrorLabel;
    @FXML private Label usernameErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Label roleErrorLabel;

    // Instance of the Service Layer
    private UserService userService = new UserService();

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        setupFieldValidation();
    }

    /**
     * Sets the reference to the main management controller.
     */
    public void setUserManagementController(UserManagementController userManagementController) {
        this.userManagementController = userManagementController;
    }

    /**
     * Handles the 'Register' button click event.
     */
    @FXML
    private void handleRegisterUser() {
        // Clear all previous errors
        clearAllErrors();

        boolean hasErrors = false;

        // Validate name
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            setFieldError(nameField, nameErrorLabel, "Name is required");
            hasErrors = true;
        }

        // Validate surname
        String surname = surnameField.getText();
        if (surname == null || surname.trim().isEmpty()) {
            setFieldError(surnameField, surnameErrorLabel, "Surname is required");
            hasErrors = true;
        }

        // Validate national ID
        String nationalID = nationalIDField.getText();
        if (nationalID == null || nationalID.trim().isEmpty()) {
            setFieldError(nationalIDField, nationalIDErrorLabel, "National ID is required");
            hasErrors = true;
        }

        // Validate birthdate
        LocalDate birthdate = birthdateField.getValue();
        if (birthdate == null) {
            setFieldError(birthdateField, birthdateErrorLabel, "Birthdate is required");
            hasErrors = true;
        }

        // Validate username
        String username = usernameField.getText();
        if (username == null || username.trim().isEmpty()) {
            setFieldError(usernameField, usernameErrorLabel, "Username is required");
            hasErrors = true;
        }

        // Validate password
        String password = passwordField.getText();
        if (password == null || password.trim().isEmpty()) {
            setFieldError(passwordField, passwordErrorLabel, "Password is required");
            hasErrors = true;
        } else if (password.length() < 8 || !password.matches(".*[A-Z].*") || !password.matches(".*\\d.*")) {
            setFieldError(passwordField, passwordErrorLabel, "Password must be at least 8 characters with uppercase and number");
            hasErrors = true;
        }

        // Validate email
        String email = emailField.getText();

        // Validate role
        String roleText = roleComboBox.getSelectionModel().getSelectedItem();
        if (roleText == null) {
            setFieldError(roleComboBox, roleErrorLabel, "Role is required");
            hasErrors = true;
        }

        // If there are validation errors, stop here
        if (hasErrors) {
            return;
        }

        // Simple mapping: 1 for Admin, 2 for User
        Integer idRole = roleText.equals("Admin") ? 1 : 2;

        User newUser = new User(null, name, surname, username, nationalID, birthdate, password, email, idRole);

        try {
            userService.registerUser(newUser);

            showAlert("Success", "Registration Successful", "The user has been registered.", AlertType.INFORMATION);

            // Refresh the main table
            if (userManagementController != null) {
                userManagementController.loadInitialUserData();
            }

            // Close the registration dialog
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.close();

        } catch (UserServiceException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";

            if (msg.toLowerCase().contains("already registered") || msg.toLowerCase().contains("already")) {
                showAlert("Duplicate Error", "User Already Registered", "The provided National ID and Role combination is already registered in the system.", AlertType.ERROR);
            } else {
                showAlert("Validation Error", "Cannot Register User", msg.isEmpty() ? "Validation failed." : msg, AlertType.ERROR);
            }

        } catch (DAOException e) {
            showAlert("System Error", "Database Operation Failed", "An internal error occurred: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void setFieldError(Control field, Label errorLabel, String message) {
        // Set red border on field
        field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");

        // Show error message
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    private void clearFieldError(Control field, Label errorLabel) {
        // Remove red border
        field.setStyle("");

        // Hide error message
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    private void clearAllErrors() {
        clearFieldError(nameField, nameErrorLabel);
        clearFieldError(surnameField, surnameErrorLabel);
        clearFieldError(nationalIDField, nationalIDErrorLabel);
        clearFieldError(birthdateField, birthdateErrorLabel);
        clearFieldError(usernameField, usernameErrorLabel);
        clearFieldError(passwordField, passwordErrorLabel);
        clearFieldError(roleComboBox, roleErrorLabel);
    }

    private void setupFieldValidation() {
        // Clear error when user types in name field
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                clearFieldError(nameField, nameErrorLabel);
            }
        });

        // Clear error when user types in surname field
        surnameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                clearFieldError(surnameField, surnameErrorLabel);
            }
        });

        // Clear error when user types in national ID field
        nationalIDField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                clearFieldError(nationalIDField, nationalIDErrorLabel);
            }
        });

        // Clear error when user selects birthdate
        birthdateField.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                clearFieldError(birthdateField, birthdateErrorLabel);
            }
        });

        // Clear error when user types in username field
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                clearFieldError(usernameField, usernameErrorLabel);
            }
        });

        // Clear error when user types in password field
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                clearFieldError(passwordField, passwordErrorLabel);
            }
        });

        // Clear error when user selects role
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                clearFieldError(roleComboBox, roleErrorLabel);
            }
        });
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
}
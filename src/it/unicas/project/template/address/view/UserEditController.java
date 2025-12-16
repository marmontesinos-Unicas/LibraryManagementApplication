package it.unicas.project.template.address.view;

import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.service.UserService;
import it.unicas.project.template.address.service.UserServiceException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.util.Optional;

public class UserEditController {

    @FXML private TextField nameField;
    @FXML private TextField surnameField;
    @FXML private TextField nationalIDField;
    @FXML private DatePicker birthdateField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private PasswordField currentPasswordField;
    @FXML private TextField visiblePasswordField;
    @FXML private ToggleButton showPasswordToggle;
    @FXML private PasswordField newPasswordField;

    // Error labels
    @FXML private Label nameErrorLabel;
    @FXML private Label surnameErrorLabel;
    @FXML private Label nationalIDErrorLabel;
    @FXML private Label birthdateErrorLabel;
    @FXML private Label usernameErrorLabel;
    @FXML private Label roleErrorLabel;
    @FXML private Label newPasswordErrorLabel;

    private Stage dialogStage;
    private User selectedUser;
    private UserManagementController userManagementController;
    private UserService userService = new UserService();


    @FXML
    private void initialize() {
        setupFieldValidation();
    }

    /**
     * Handles the action of the password toggle button.
     * Toggles visibility between the PasswordField and the TextField.
     */
    @FXML
    private void handlePasswordToggle() {
        if (showPasswordToggle.isSelected()) {
            // Show password (use visiblePasswordField)
            currentPasswordField.setVisible(false);
            currentPasswordField.setManaged(false);
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            showPasswordToggle.setText("Hide");
        } else {
            // Hide password (use currentPasswordField)
            currentPasswordField.setVisible(true);
            currentPasswordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            showPasswordToggle.setText("Show");
        }
    }

    /**
     * Handles the 'Save Changes' button click event.
     * Updates the user information based on the form fields.
     */
    @FXML
    private void handleSave() {
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
        if (birthdateField.getValue() == null) {
            setFieldError(birthdateField, birthdateErrorLabel, "Birthdate is required");
            hasErrors = true;
        }

        // Validate username
        String username = usernameField.getText();
        if (username == null || username.trim().isEmpty()) {
            setFieldError(usernameField, usernameErrorLabel, "Username is required");
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

        // Validate new password (only if user entered something)
        String newPassword = newPasswordField.getText();
        if (!newPassword.isEmpty()) {
            if (newPassword.length() < 8 || !newPassword.matches(".*[A-Z].*") || !newPassword.matches(".*\\d.*")) {
                setFieldError(newPasswordField, newPasswordErrorLabel, "Password must be at least 8 characters with uppercase and number");
                hasErrors = true;
            }
        }

        // If there are validation errors, stop here
        if (hasErrors) {
            return;
        }

        // Determine the password to save: new password if entered, otherwise keep old one
        String passwordToSave = newPassword.isEmpty() ? selectedUser.getPassword() : newPassword;

        // Convert Role String back to Integer ID
        Integer newIdRole = roleText.equals("Admin") ? 1 : 2;

        // Create the updated User object
        User updatedUser = new User(
                selectedUser.getIdUser(),
                name,
                surname,
                username,
                nationalID,
                birthdateField.getValue(),
                passwordToSave,
                email,
                newIdRole
        );

        try {
            // Check if the user ID has been tampered with (for safety)
            if (updatedUser.getIdUser() == null || updatedUser.getIdUser() <= 0) {
                throw new UserServiceException("Error: Cannot save changes; user ID is invalid.");
            }

            // Call the service layer to perform the update
            userService.updateUser(updatedUser);

            showAlert("Success", "Update Complete", "User details have been successfully updated.", AlertType.INFORMATION);

            // Update the selectedUser object with the new data
            this.selectedUser = updatedUser;

            userManagementController.loadInitialUserData();
            dialogStage.close();

        } catch (UserServiceException e) {
            showAlert("Validation Error", "Cannot Save Changes", e.getMessage(), AlertType.ERROR);
        } catch (DAOException e) {
            showAlert("System Error", "Database Operation Failed", "An internal error occurred: " + e.getMessage(), AlertType.ERROR);
        }
    }

    /**
     * Handles the 'Delete User' button click.
     */
    @FXML
    private void handleDelete() {
        if (selectedUser == null) return;

        // 1. Show Confirmation Dialog ("Are you sure you want to delete this user?")
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete User: " + selectedUser.getName()+" " + selectedUser.getSurname()+ " (with ID: " + selectedUser.getNationalID()+")");
        confirmAlert.setContentText("Are you sure you want to permanently delete this user? This action cannot be undone.");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // VALIDATION STEP: Check for outstanding loans
                if (userService.hasOutstandingLoans(selectedUser)) {
                    showAlert("Deletion Failed", "Outstanding Loans Detected",
                            "This user cannot be deleted because they currently have unreturned material.",
                            AlertType.WARNING);
                    return; // Stop the deletion process immediately
                }

                // 2. Call the Business Logic to delete the user
                userService.deleteUser(selectedUser);

                // 3. Success Feedback ("Deletion completed")
                showAlert("Success", "Deletion Complete", "The user has been successfully deleted.", AlertType.INFORMATION);

                // 4. Notify main controller to refresh and close dialog
                userManagementController.loadInitialUserData();
                dialogStage.close();

            } catch (UserServiceException e) {
                // Catches errors like "ID is missing" (due to business logic validation)
                showAlert("Deletion Failed", "Validation Error", e.getMessage(), AlertType.ERROR);
            } catch (DAOException e) {
                showAlert("System Error", "Database Operation Failed", "An internal error occurred during deletion: " + e.getMessage(), AlertType.ERROR);
            }
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
        clearFieldError(roleComboBox, roleErrorLabel);
        clearFieldError(newPasswordField, newPasswordErrorLabel);
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

        // Clear error when user selects role
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                clearFieldError(roleComboBox, roleErrorLabel);
            }
        });

        // Clear error when user types in new password field
        newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                clearFieldError(newPasswordField, newPasswordErrorLabel);
            }
        });
    }
    /**
     * Handles the 'Cancel' button click event.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Helper method to display JavaFX alerts.
     */
    private void showAlert(String title, String header, String content, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.initOwner(dialogStage);
        alert.showAndWait();
    }

    /**
     * Sets the stage of this dialog.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Sets the user to be edited and populates the fields.
     * Initializes the current password fields.
     * @param selectedUser The user object to display and edit.
     */
    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;

        nameField.setText(selectedUser.getName());
        surnameField.setText(selectedUser.getSurname());
        nationalIDField.setText(selectedUser.getNationalID());
        birthdateField.setValue(selectedUser.getBirthdate());
        usernameField.setText(selectedUser.getUsername());
        emailField.setText(selectedUser.getEmail());

        // Map idRole back to Role String for ComboBox display
        String roleText = selectedUser.getIdRole() != null && selectedUser.getIdRole() == 1 ? "Admin" : "User";
        roleComboBox.getSelectionModel().select(roleText);

        // Populate the Current Password field(s)
        String currentPassword = selectedUser.getPassword();
        currentPasswordField.setText(currentPassword);
        visiblePasswordField.setText(currentPassword);
    }

    /**
     * Sets the reference to the main management controller.
     * @param userManagementController The controller managing the user table.
     */
    public void setUserManagementController(UserManagementController userManagementController) {
        this.userManagementController = userManagementController;
    }

}
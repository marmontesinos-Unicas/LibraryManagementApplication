package it.unicas.project.template.address.view;

import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.service.UserService;
import it.unicas.project.template.address.service.UserServiceException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Optional;

public class UserEditController {

    @FXML private TextField nameField;
    @FXML private TextField surnameField;
    @FXML private TextField nationalIDField;
    @FXML private DatePicker birthdateField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private PasswordField passwordField; // Used for setting a new password

    private Stage dialogStage;
    private User selectedUser;
    private UserManagementController userManagementController;
    private UserService userService = new UserService();

    /**
     * Sets the stage of this dialog.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Sets the user to be edited and populates the fields.
     */
    public void setSelectedUser(User user) {
        this.selectedUser = user;

        if (user != null) {
            nameField.setText(user.getName());
            surnameField.setText(user.getSurname());
            nationalIDField.setText(user.getNationalID());
            birthdateField.setValue(user.getBirthdate());
            usernameField.setText(user.getUsername());
            emailField.setText(user.getEmail());

            // Set Role based on ID (assuming 1=Admin, 2=User)
            String role = (user.getIdRole() == 1) ? "Admin" : "User";
            roleComboBox.getSelectionModel().select(role);
        }
    }

    /**
     * Sets the reference to the main management controller to allow refreshing the table.
     */
    public void setUserManagementController(UserManagementController controller) {
        this.userManagementController = controller;
    }

    /**
     * Handles the 'Save Changes' button click.
     * Updates the user information.
     */
    @FXML
    private void handleSave() {
        if (selectedUser == null) return;

        // 1. Validate Input (Basic check - more detailed validation in UserService)
        if (birthdateField.getValue() == null) {
            showAlert("Validation Error", "Missing Birthdate", "Birthdate is required.", AlertType.ERROR);
            return;
        }

        // 2. Update the User Model
        selectedUser.setName(nameField.getText());
        selectedUser.setSurname(surnameField.getText());
        selectedUser.setNationalID(nationalIDField.getText());
        selectedUser.setBirthdate(birthdateField.getValue());
        selectedUser.setUsername(usernameField.getText());
        selectedUser.setEmail(emailField.getText());

        // Map Role ComboBox value back to ID
        String roleText = roleComboBox.getSelectionModel().getSelectedItem();
        Integer idRole = roleText != null && roleText.equals("Admin") ? 1 : 2;
        selectedUser.setIdRole(idRole);

        // Handle Password Update ONLY if the field is not empty
        String newPassword = passwordField.getText();
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            selectedUser.setPassword(newPassword);
        }

        try {
            // 3. Call the Business Logic to update the user
            userService.updateUser(selectedUser); // Assuming you have an updateUser method in UserService

            // 4. Success Feedback (Changes saved correctly)
            showAlert("Success", "Changes Saved", "The user details have been updated correctly.", AlertType.INFORMATION);

            // 5. Notify main controller to refresh and close dialog
            userManagementController.loadInitialUserData();
            dialogStage.close();

        } catch (UserServiceException e) {
            showAlert("Validation Error", "Update Failed", e.getMessage(), AlertType.ERROR);
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
}
package it.unicas.project.template.address.view;

import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.service.LoginService; // <-- Keep this import
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginDialogController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private Stage dialogStage;
    private boolean loginSuccessful = false;
    private String username; // Stores the logged-in username

    // 1. Service Instantiation: Instantiate the LoginService
    private final LoginService loginService = new LoginService();

    /**
     * Called by the MainApp to pass the reference of the Stage.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Returns true if the login was successful.
     */
    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }

    /**
     * Returns the username of the logged-in user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Action for the Login button.
     */
    @FXML
    private void handleLogin() {
        // Clear any previous error message
        errorLabel.setText("");

        String inputUsername = usernameField.getText();
        String inputPassword = passwordField.getText();

        try {
            // 2. Delegate the authentication logic to the LoginService
            // The service now handles fetching the user and comparing the password.
            User matchedUser = loginService.authenticate(inputUsername, inputPassword);

            if (matchedUser != null) {
                // Login successful
                loginSuccessful = true;
                username = matchedUser.getUsername(); // Store the username
                dialogStage.close(); // Close the login dialog
            } else {
                // 3. Authentication failed (User not found OR incorrect password)
                errorLabel.setText("Incorrect username or password");
            }

        } catch (DAOException e) {
            // 4. Handle DAOException: This signals a system/database error, not a user error.
            errorLabel.setText("System Error: Could not connect to the authentication service.");
            e.printStackTrace();
            // Optionally show an error dialog box here for better user experience
        }
    }

    /**
     * Action for the Cancel button.
     */
    @FXML
    private void handleCancel() {
        loginSuccessful = false;
        dialogStage.close();
    }
}

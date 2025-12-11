package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.service.LoginService;
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
    private MainApp mainApp;
    private boolean loginSuccessful = false; // sets the boolean loginSuccessful flag to false as a default value.
    private String username; // Stores the logged-in username

    // 1. Service Instantiation: Instantiate the LoginService
    private final LoginService loginService = new LoginService();

    /**
     * Called by the MainApp to pass the reference of the MainApp object.
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

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
                if (dialogStage != null) {
                    // SCENARIO 1: Startup. This is a modal dialog.
                    dialogStage.close();
                } else if (mainApp != null) {
                    // SCENARIO 2: Logout. This is the main stage scene.
                    // We call MainApp to switch the scene to the appropriate landing page.
                    mainApp.handleSuccessfulLogin(username);
                } // Close the login dialog
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
        if (dialogStage != null) {
            dialogStage.close();
        } else if (mainApp != null) {
            // If we are in the scene (logout), and the user cancels, we simply let the app sit on the login screen.
            System.out.println("Login canceled on main scene.");
        }
    }
}

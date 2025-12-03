package it.unicas.project.template.address.view;

import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class LoginDialogController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private Stage dialogStage;
    private boolean loginSuccessful = false;
    private String username; // guardamos el username logueado

    /**
     * Llamado desde MainApp para pasar la referencia del Stage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Retorna true si el login fue exitoso
     */
    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }

    /**
     * Retorna el username del usuario logueado
     */
    public String getUsername() {
        return username;
    }

    /**
     * Acción del botón Login
     */
    @FXML
    private void handleLogin() {
        String inputUsername = usernameField.getText();
        String inputPassword = passwordField.getText();

        try {
            // Obtenemos todos los usuarios desde la base de datos
            List<User> users = UserDAOMySQLImpl.getInstance().select(null);

            User matchedUser = users.stream()
                    .filter(u -> u.getUsername().equals(inputUsername) && u.getPassword().equals(inputPassword))
                    .findFirst()
                    .orElse(null);

            if (matchedUser != null) {
                loginSuccessful = true;
                username = matchedUser.getUsername(); // guardamos el username
                dialogStage.close(); // cerramos el login
            } else {
                errorLabel.setText("Usuario o contraseña incorrectos");
            }

        } catch (DAOException e) {
            errorLabel.setText("Error al acceder a la base de datos");
            e.printStackTrace();
        }
    }

    /**
     * Acción del botón Cancel
     */
    @FXML
    private void handleCancel() {
        loginSuccessful = false;
        dialogStage.close();
    }
}

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
     * Acción del botón Login
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            // Obtenemos todos los usuarios desde la base de datos
            List<User> users = UserDAOMySQLImpl.getInstance().select(null);

            boolean valid = users.stream()
                    .anyMatch(u -> u.getUsername().equals(username) && u.getPassword().equals(password));

            if (valid) {
                loginSuccessful = true;
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

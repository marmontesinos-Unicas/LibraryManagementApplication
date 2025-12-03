package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.dao.mysql.DAOMySQLSettings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class RootLayoutController {

    // Reference to the main application
    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleSettings() {
        DAOMySQLSettings daoMySQLSettings = DAOMySQLSettings.getCurrentDAOMySQLSettings();
        // Comentado porque mainApp.showSettingsEditDialog ya no existe
        /*
        if (mainApp.showSettingsEditDialog(daoMySQLSettings)){
            DAOMySQLSettings.setCurrentDAOMySQLSettings(daoMySQLSettings);
        }
        */
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("AddressApp");
        alert.setHeaderText("About");
        alert.setContentText("Author: XXXXX\nWebsite: http://www.xyz.it");
        alert.showAndWait();
    }

    @FXML
    private void handleExit() {
        // Comentado porque mainApp.handleExit() ya no se quiere usar
        // mainApp.handleExit();
    }

    @FXML
    private void handleShowBirthdayStatistics() {
        // Comentado porque mainApp.showBirthdayStatistics() ya no existe
        // mainApp.showBirthdayStatistics();
    }
}

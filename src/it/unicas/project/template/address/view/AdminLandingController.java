package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the FXML interface of the administrator landing screen.
 * Contains the action methods for the four main buttons.
 */
public class AdminLandingController {

    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private Button addMaterialButton;
    @FXML
    private Button manageUsersButton;
    @FXML
    private Button loanReturnButton;
    @FXML
    private Button searchButton;

    /**
     * Handles the action for the "Add Material" button.
     * Future logic: Open the view to add new books or resources.
     * @param event The action event.
     */
    @FXML
    protected void handleAddMaterial(ActionEvent event) {
        // Open the Add Material dialog (modal). mainApp provides the method.
        if (mainApp != null) {
            mainApp.showAddMaterialView();
        } else {
            System.err.println("mainApp is null - call setMainApp(...) when loading the admin view.");
        }
    }

    /**
     * Handles the action for the "Manage Users" button.
     * Future logic: Open the view to add, remove, or modify user profiles.
     * @param event The action event.
     */
    @FXML
    protected void handleManageUsers(ActionEvent event) {
        System.out.println("Acción: Gestionar usuarios (Pendiente de implementación).");
        // The code to switch to the user management screen will go here.
    }

    /**
     * Handles the action for the "Loan / Return" button.
     * Future logic: Open the view to register loans and returns.
     * @param event The action event.
     */
    @FXML
    protected void handleLoanReturn(ActionEvent event) {
        try {
            // Cargar LoadReturn.fxml
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("LoadReturn.fxml"));
            AnchorPane page = loader.load();

            // Crear nueva ventana (Stage)
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Loan and Return");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            if (mainApp != null && mainApp.getPrimaryStage() != null) {
                dialogStage.initOwner(mainApp.getPrimaryStage());
            }
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Pasar el Stage al controlador de LoadReturn
            LoadReturnController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the action for the "Advanced Search" button.
     * Future logic: Open the view to perform detailed searches in the catalog.
     * @param event The action event.
     */
    @FXML
    protected void handleSearch(ActionEvent event) {
        System.out.println("Acción: Búsqueda avanzada de catálogo (Pendiente de implementación).");
        // The code to switch to the search screen will go here.
    }

    //@FXML
    //protected void handleCloseButton(ActionEvent event) {
        // Gets the window (Stage) from the button and closes it
        //Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        //stage.close();
        //mainApp.handleExit();
    //}
}
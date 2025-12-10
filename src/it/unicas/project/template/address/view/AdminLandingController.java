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

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    // References to the buttons
    @FXML
    private Button addMaterialButton;
    @FXML
    private Button manageUsersButton;
    @FXML
    private Button loanReturnButton;
    @FXML
    private Button searchButton;

    // ... (handleAddMaterial remains the same) ...
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
     * Implemented logic: Calls MainApp to switch the scene to the User Management view.
     * @param event The action event.
     */
    @FXML
    protected void handleManageUsers(ActionEvent event) {
        if (mainApp != null) {
            System.out.println("Acción: Gestionar usuarios. Cambiando a la vista User Management.");
            mainApp.showUserManagement(); // CALL TO THE NEW METHOD
        } else {
            System.err.println("Error: MainApp reference is null. Cannot show User Management.");
        }
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
        System.out.println("Action: Advanced search of the catalog.");
        // The code to switch to the search screen will go here.
        if (mainApp != null) {
            mainApp.showCatalogView();
        } else {
            System.err.println("mainApp is null - call setMainApp(...) when loading the admin view.");
        }
    }

}

    //@FXML
    //protected void handleCloseButton(ActionEvent event) {
        // Gets the window (Stage) from the button and closes it
        //Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        //stage.close();
        //mainApp.handleExit();
    //}
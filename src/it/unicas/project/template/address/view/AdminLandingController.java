package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

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
        System.out.println("Acción: Añadir nuevo material (Pendiente de implementación).");
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

    // ... (handleLoanReturn and handleSearch remain the same) ...
    @FXML
    protected void handleLoanReturn(ActionEvent event) {
        System.out.println("Acción: Registrar préstamo o devolución (Pendiente de implementación).");
    }

    @FXML
    protected void handleSearch(ActionEvent event) {
        System.out.println("Acción: Búsqueda avanzada de catálogo (Pendiente de implementación).");
    }
}
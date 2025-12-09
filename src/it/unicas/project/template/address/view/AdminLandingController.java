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

    // References to the buttons, although not strictly necessary
    // for the action methods, they are good FXML practice.
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
        System.out.println("Acción: Añadir nuevo material (Pendiente de implementación).");
        // The code to switch to the add material screen will go here.
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
        System.out.println("Acción: Registrar préstamo o devolución (Pendiente de implementación).");
        // The code to switch to the transactions screen will go here.
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
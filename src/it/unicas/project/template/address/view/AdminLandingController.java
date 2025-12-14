    package it.unicas.project.template.address.view;

    import it.unicas.project.template.address.MainApp;
    import javafx.event.ActionEvent;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Node;
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


        @FXML
        protected void handleAddMaterial(ActionEvent event) {
            if (mainApp != null) {
                mainApp.showAddMaterialView();
            } else {
                System.err.println("mainApp is null - call setMainApp(...) when loading the admin view.");
            }
        }

        /**
         * Handles the action for the "Manage Users" button.
         * Implemented logic: Calls MainApp to switch the scene to the User Management view.
         *
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
         * Logic: Calls MainApp to switch the scene to the Loan/Return view.
         *
         * @param event The action event.
         */
        @FXML
        protected void handleLoanReturn(ActionEvent event) {
            if (mainApp != null) {
                System.out.println("Action: Loans / Returns. Changing to Loan/Return view.");
                mainApp.showLoadReturn();
            } else {
                System.err.println("Error: MainApp reference is null. Cannot show Loan/Return view.");
            }
        }

        /**
         * Handles the action for the "Advanced Search" button.
         * Future logic: Open the view to perform detailed searches in the catalog.
         *
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

        /**
         * Handles the action for the "Logout" button.
         * Logic: Shows the Login dialog, then closes the current Admin Landing window.
         *
         * @param event The action event.
         */
        @FXML
        protected void handleLogout(ActionEvent event) {
            if (mainApp != null) {
                System.out.println("Action: Logging out.");

                // 1. Show the Login Dialog
                mainApp.showLoginDialog();

                // 2. Explicitly close the current Admin Landing window/stage.
                try {
                    Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                    // Hide the window
                    currentStage.hide();

                    // Call System.gc() to try and force cleanup of the Admin Scene
                    // data before the stage can reappear if login fails. (Use sparingly, but necessary here)
                    System.gc();

                } catch (Exception e) {
                    System.err.println("Error closing Admin Landing stage: " + e.getMessage());
                }

            } else {
                System.err.println("Error: MainApp reference is null. Cannot log out.");
            }
        }
    }
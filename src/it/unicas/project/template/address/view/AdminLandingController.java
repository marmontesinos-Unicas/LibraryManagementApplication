    package it.unicas.project.template.address.view;

    import it.unicas.project.template.address.MainApp;
    import javafx.event.ActionEvent;
    import javafx.fxml.FXML;
    import javafx.scene.Node;
    import javafx.scene.control.Button;
    import javafx.stage.Stage;
    import javafx.scene.control.Alert; // NEW IMPORT
    import javafx.scene.control.Alert.AlertType; // NEW IMPORT
    import javafx.scene.control.ButtonType; // NEW IMPORT

    import java.util.Optional; // NEW IMPORT

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
        private Button MaterialManagementButton;
        @FXML
        private Button manageUsersButton;
        @FXML
        private Button loanReturnButton;
        @FXML
        private Button searchButton;


        @FXML
        protected void handleMaterialManagement(ActionEvent event) {
            // MODIFIED: Open the Material Management view instead of the Add Material dialog
            if (mainApp != null) {
                System.out.println("Action: Managing Material. Switching to Material Management view.");
                mainApp.showMaterialManagement(); // CALL TO THE NEW METHOD IN MainApp
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
                System.out.println("Action: Changing to User Management view.");
                mainApp.showUserManagement();
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
                mainApp.showLoanReturn();
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
                mainApp.showCatalog();
            } else {
                System.err.println("mainApp is null - call setMainApp(...) when loading the admin view.");
            }
        }

        /**
         * Handles the action for the "Logout" button.
         * Logic: Shows a confirmation dialog. If confirmed, shows the Login dialog,
         * then closes the current Admin Landing window.
         *
         * @param event The action event.
         */
        @FXML
        protected void handleLogout(ActionEvent event) {
            if (mainApp == null) {
                System.err.println("Error: MainApp reference is null. Cannot log out.");
                return;
            }

            // Create a confirmation alert dialog
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirm Logout");
            alert.setHeaderText("You are about to log out.");
            alert.setContentText("Are you sure you want to log out and return to the login screen?");

            // Show the dialog and wait for the user's response
            Optional<ButtonType> result = alert.showAndWait();

            // Check if the user clicked OK (which is the default button type for confirmation)
            if (result.isPresent() && result.get() == ButtonType.OK) {
                System.out.println("Action: Logging out confirmed.");

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
                // User cancelled or closed the dialog. Do nothing.
                System.out.println("Logout cancelled by the user.");
            }
        }
    }
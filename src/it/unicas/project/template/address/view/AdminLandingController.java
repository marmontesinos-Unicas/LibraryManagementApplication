    package it.unicas.project.template.address.view;

    import it.unicas.project.template.address.MainApp;
    import javafx.event.ActionEvent;
    import javafx.fxml.FXML;
    import javafx.scene.Node;
    import javafx.scene.control.Button;
    import javafx.stage.Stage;
    import javafx.scene.control.Alert;
    import javafx.scene.control.Alert.AlertType;
    import javafx.scene.control.ButtonType;
    import java.util.Optional;

    /**
     * Controller for the FXML interface of the administrator landing screen.
     * Contains the action methods for the four main buttons (Material Management, User Management, Loan/Return, Search).
     *
     * Access Keyword Explanation: {@code public} - This class must be public so that
     * the JavaFX {@code FXMLLoader} can instantiate it when loading the {@code AdminLanding.fxml} file.
     */
    public class AdminLandingController {

        private MainApp mainApp;
        // Access Keyword Explanation: {@code private} - Encapsulates the reference to the main application.

        // References to the buttons (Injected from FXML)
        // Access Keyword Explanation: {@code private} - FXML injection works on private fields,
        // and they should be kept private as they are UI elements specific to this controller.
        @FXML
        private Button MaterialManagementButton;
        @FXML
        private Button manageUsersButton;
        @FXML
        private Button loanReturnButton;
        @FXML
        private Button searchButton;

        /**
         * Handles the action for the "Material Management" button.
         * Logic: Calls MainApp to switch the scene to the Material Management (Inventory) view.
         *
         * Access Keyword Explanation: {@code protected} - By convention, FXML handler methods
         * are often protected or public to ensure they are accessible by the JavaFX runtime
         * framework (which may access them via reflection, similar to FXML fields).
         *
         * @param event The action event.
         */
        @FXML
        protected void handleMaterialManagement(ActionEvent event) {
            // Open the Material Management view:
            if (mainApp != null) {
                System.out.println("Action: Managing Material. Switching to Material Management view.");
                mainApp.showMaterialManagement(); // Calls a mainApp Method to show the Material Management interface.
            } else {
                System.err.println("mainApp is null - call setMainApp(...) when loading the admin view.");
            }
        }

        /**
         * Handles the action for the "Manage Users" button.
         * Logic: Calls MainApp to switch the scene to the User Management view.
         *
         * Access Keyword Explanation: {@code protected} - By convention, FXML handler methods
         * are often protected or public to ensure they are accessible by the JavaFX runtime
         * framework (which may access them via reflection, similar to FXML fields).
         *
         * @param event The action event.
         */
        @FXML
        protected void handleManageUsers(ActionEvent event) {
            if (mainApp != null) {
                System.out.println("Action: Changing to User Management view.");
                mainApp.showUserManagement(); // Calls a mainApp Method to show the Users Management interface.
            } else {
                System.err.println("Error: MainApp reference is null. Cannot show User Management.");
            }
        }

        /**
         * Handles the action for the "Loan / Return" button.
         * Logic: Calls MainApp to switch the scene to the Loan/Return view.
         *
         * Access Keyword Explanation: {@code protected} - By convention, FXML handler methods
         * are often protected or public to ensure they are accessible by the JavaFX runtime
         * framework (which may access them via reflection, similar to FXML fields).
         *
         * @param event The action event.
         */
        @FXML
        protected void handleLoanReturn(ActionEvent event) {
            if (mainApp != null) {
                System.out.println("Action: Changing to Loans/Return view.");
                mainApp.showLoanReturn(); // Calls a mainApp Method to show the Loans and Return interface.
            } else {
                System.err.println("Error: MainApp reference is null. Cannot show Loans/Return view.");
            }
        }

        /**
         * Handles the action for the "Advanced Search" button (Material Catalog).
         * Logic: Calls MainApp to switch the scene to the Admin Material Catalog view.
         *
         * Access Keyword Explanation: {@code protected} - By convention, FXML handler methods
         * are often protected or public to ensure they are accessible by the JavaFX runtime
         * framework (which may access them via reflection, similar to FXML fields).
         *
         * @param event The action event.
         */
        @FXML
        protected void handleSearch(ActionEvent event) {
            System.out.println("Action: Advanced search of the catalog.");
            if (mainApp != null) {
                mainApp.showCatalog(); // Calls a mainApp Method to show the catalog of the admins.
            } else {
                System.err.println("mainApp is null - call setMainApp(...) when loading the admin view.");
            }
        }

        /**
         * Handles the action for the "Logout" button.
         * Logic: Shows a confirmation dialog. If confirmed, calls MainApp to re-open the Login dialog,
         * then closes the current Admin Landing stage.
         *
         * Access Keyword Explanation: {@code protected} - By convention, FXML handler methods
         * are often protected or public to ensure they are accessible by the JavaFX runtime
         * framework (which may access them via reflection, similar to FXML fields).
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

                // Show the Login Dialog (MainApp handles scene removal/hiding internally)
                mainApp.showLoginDialog();

                // Explicitly close the current Admin Landing window/stage.
                try {
                    Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    currentStage.hide();// Hides the window
                    System.gc(); // This tries to force cleanup of the Admin Scene before the stage can reappear if login fails.

                } catch (Exception e) {
                    System.err.println("Error closing Admin Landing stage: " + e.getMessage());
                }

            } else {
                // User cancelled or closed the dialog. Do nothing.
                System.out.println("Logout cancelled by the user.");
            }
        }

        /**
         * Is called by the main application to give a reference back to itself.
         * This establishes the link necessary for navigation (switching scenes).
         *
         * Access Keyword Explanation: {@code public} - This method must be public because
         * it is called explicitly by the {@code MainApp} class immediately after loading this FXML view.
         *
         * @param mainApp The reference to the main application instance.
         */
        public void setMainApp(MainApp mainApp) {
            this.mainApp = mainApp;
        }
    }
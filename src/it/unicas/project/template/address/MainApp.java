package it.unicas.project.template.address;

import it.unicas.project.template.address.model.Hold;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.HoldDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.MaterialDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;
import it.unicas.project.template.address.view.*;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import java.time.LocalDateTime;
import java.util.List;
import java.time.ZoneId;


import java.io.IOException;

/**
 * Main application class for the Library Management System.
 * It extends JavaFX's Application and serves as the primary controller for
 * view navigation and managing the application's state, including the
 * currently logged-in user.
 *
 * Access Keyword Explanation: {@code public} - This class must be public because
 * it is the main entry point for the JavaFX application runtime.
 */
public class MainApp extends Application {

    private Stage primaryStage; // Primary stage for the main application window
    private User loggedUser; // Stores the currently logged-in user object

    // Access Keyword Explanation: {@code private} - These fields are marked private
    // to encapsulate the application's core state and ensure they are only modified
    // internally or via controlled public methods (getters).

    // Lists for the user's loans and reservations that will be used when the User Landing page is called.
    private ObservableList<String> userLoans = FXCollections.observableArrayList();
    private ObservableList<String> userReservations = FXCollections.observableArrayList();

    /**
     * Gets the ObservableList of the current user's active loans (as String representations).
     *
     * Access Keyword Explanation: {@code public} - This method is public so that
     * FXML controllers (like the User Dashboard) can access the data needed for
     * displaying lists of loans via data binding.
     *
     * @return The list of user loans.
     */
    public ObservableList<String> getUserLoans() {
        return userLoans;
    }

    /**
     * Gets the ObservableList of the current user's active reservations (as String representations).
     *
     * Access Keyword Explanation: {@code public} - This method is public so that
     * FXML controllers can access the data needed for displaying lists of reservations.
     *
     * @return The list of user reservations.
     */
    public ObservableList<String> getUserReservations() {
        return userReservations;
    }

    /**
     * The main entry point for all JavaFX applications.
     * Initializes the primary stage, sets up the window properties, performs
     * necessary cleanup (like expired holds), and launches the login dialog.
     *
     * Access Keyword Explanation: {@code public} - This method must be public
     * because it is an abstract method inherited from {@code javafx.application.Application}
     * and is called by the JavaFX runtime to start the application.
     *
     * @param primaryStage The primary stage for this application, onto which
     * the application scene can be set.
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Library Management App");
        // Set application icon
        this.primaryStage.getIcons().add(new Image("file:resources/images/address_book_32.png"));

        // Set initial and minimum window size
        this.primaryStage.setMinWidth(800);
        this.primaryStage.setMinHeight(520);
        this.primaryStage.setWidth(800);
        this.primaryStage.setHeight(520);

        // Clean up expired holds before showing login. This way each time the app starts,
        // expired holds are removed from the database.
        try {
            cleanupExpiredHolds(); // Essential maintenance step on startup
        } catch (DAOException e) {
            e.printStackTrace();
            // Show error if cleanup fails (usually database access issue)
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cleanup Failed");
            alert.setContentText("Could not clean up expired holds.");
            alert.showAndWait();
        }

        // Show the login window. Application flow depends on success here.
        boolean loggedIn = showLoginDialog();
        if (loggedIn) {
            // Depending on role, show the appropriate landing page.
            if (loggedUser.getIdRole() == 1) {
                showAdminLanding(); // If Role ID is 1, show the Admin Landing page.
            } else {
                showUserLanding(); // Otherwise (Role ID is 2), show the User Landing page.
            }
            primaryStage.show(); // Show the main stage after the appropriate scene is set
        } else {
            primaryStage.close(); // Close the application if login is cancelled or fails at startup
        }
    }

    /**
     * Borra los holds caducados (anteriores a ayer) y marca los materiales como available.
     * A hold is considered expired if it was created more than 24 hours ago.
     *
     * Access Keyword Explanation: {@code private} - This is a utility method used only
     * internally by the {@code start()} method for application maintenance. It does not
     * need to be exposed to external classes.
     *
     * @throws DAOException If there is an issue accessing or updating the database.
     */
    private void cleanupExpiredHolds() throws DAOException {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Rome")); // Current hour in Europe/Rome timezone
        LocalDateTime cutoffDate = now.minusHours(24); // We define a cut-off date of 24 hours
        System.out.println("CutoffDate for expired holds: " + cutoffDate);

        // Fetch all current holds from the database:
        List<Hold> allHolds = HoldDAOMySQLImpl.getInstance().select(new Hold());

        for (Hold hold : allHolds) {
            if (hold.getHold_date() != null) {
                // Adjust the hold date to Rome timezone if necessary
                LocalDateTime holdAdjusted = hold.getHold_date().minusHours(1); // Adjusts to +2 if Rome is UTC+2
                System.out.println("Checking hold " + hold.getIdHold() + " with date " + holdAdjusted);

                if (holdAdjusted.isBefore(cutoffDate)) {
                    System.out.println("Deleting hold: " + hold.getIdHold() + " with date: " + holdAdjusted);

                    // Update material status to "available"
                    Material material = new Material();
                    material.setIdMaterial(hold.getIdMaterial()); // We set the material ID from the hold data
                    material = MaterialDAOMySQLImpl.getInstance().select(material).stream().findFirst().orElse(null); // And we fetch the full material record

                    if (material != null) {
                        material.setMaterial_status("available"); // Set status to available
                        MaterialDAOMySQLImpl.getInstance().update(material); // Update in DB
                        System.out.println("Material '" + material.getTitle() + "' (Author: " + material.getAuthor() + ") set to available after a hold on this material expired.");
                    }

                    // Erase expired hold
                    HoldDAOMySQLImpl.getInstance().delete(hold);

                } else {
                    System.out.println("Hold " + hold.getIdHold() + " is not expired yet.");
                }
            }
        }
    }

    /**
     * Shows the login dialog window.
     * If login is successful, it sets the {@code loggedUser} and loads the appropriate
     * dashboard (Admin or User) onto the primary stage.
     *
     * Access Keyword Explanation: {@code public} - This method is public because it must
     * be accessible from the {@code LoginController} (typically used when a user clicks
     * "Logout" and needs to return to the login screen).
     *
     * @return true if a user successfully logged in, false otherwise (login failed or cancelled).
     */
    public boolean showLoginDialog() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/Login.fxml"));
            AnchorPane page = loader.load();

            // When logging out, ensure the primary stage is hidden and cleared so it does not hide behind the Log in window.
            if (primaryStage.isShowing()) {
                primaryStage.hide();
                primaryStage.setScene(null);
            }

            // Create a new modal dialog stage for the login form
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Login");
            dialogStage.initModality(Modality.WINDOW_MODAL); // Makes it block interaction with primaryStage
            dialogStage.initOwner(primaryStage); // Links it to the main window
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false); // Login is kept fixed size

            LoginController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setMainApp(this); // Allows the controller to communicate back to MainApp (e.g., successful re-login)

            dialogStage.showAndWait();

            // The controller handles setting loggedUser/loans/reservations only if login was successful
            if (controller.isLoginSuccessful()) {
                String username = controller.getUsername();

                // Fetch user data and initialize lists
                loggedUser = UserDAOMySQLImpl.getInstance().getByUsername(username);

                // Depending on the role, set the correct scene on the primaryStage (which is still hidden)
                if (loggedUser.getIdRole() == 1) { // Admin Role
                    showAdminLanding();
                } else {                           // User Role
                    showUserLanding();
                }

                // Show the primaryStage with the newly loaded Admin/User scene
                primaryStage.show();

                return true;
            } else {
                // Login failed or was cancelled.
                return false;
            }
        } catch (IOException | DAOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Displays the User Landing (User Dashboard) on the primary stage.
     * Preserves the stage size and maximization status.
     *
     * Access Keyword Explanation: {@code public} - This method is public so that
     * the {@code LoginController} can display the correct dashboard upon successful
     * authentication, and other controllers (like the Catalog view) can navigate
     * back to the dashboard.
     */
    public void showUserLanding() {
        try {
            // Save current window state (width, height, maximized) before loading new scene
            double currentWidth = primaryStage.isShowing() ? primaryStage.getWidth() : 800;
            double currentHeight = primaryStage.isShowing() ? primaryStage.getHeight() : 520;
            boolean wasMaximized = primaryStage.isShowing() && primaryStage.isMaximized();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/UserLanding.fxml"));
            BorderPane userPane = loader.load();

            Scene scene = new Scene(userPane);
            primaryStage.setScene(scene);
            primaryStage.setTitle("User Dashboard");

            // Restore dimensions
            if (wasMaximized) {
                primaryStage.setMaximized(true);
            } else {
                primaryStage.setWidth(currentWidth);
                primaryStage.setHeight(currentHeight);
            }

            UserLandingController controller = loader.getController();
            controller.setMainApp(this);        // Provide access to main application methods
            controller.setCurrentUser(loggedUser); // Pass logged-in user details

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays the Admin Landing (Admin Dashboard) on the primary stage.
     * Preserves the stage size and maximization status.
     *
     * Access Keyword Explanation: {@code public} - This method is public for the same
     * reasons as {@code showUserLandingView()}: it is called by the {@code LoginController}
     * upon successful login and by other admin views to navigate back to the main admin hub.
     */
    public void showAdminLanding() {
        try {
            // Save current window state (width, height, maximized) before loading new scene
            double currentWidth = primaryStage.isShowing() ? primaryStage.getWidth() : 800;
            double currentHeight = primaryStage.isShowing() ? primaryStage.getHeight() : 520;
            boolean wasMaximized = primaryStage.isShowing() && primaryStage.isMaximized();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/AdminLanding.fxml"));
            BorderPane adminPane = loader.load();

            Scene scene = new Scene(adminPane);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Admin Dashboard"); // Set title back

            // Restore dimensions
            if (wasMaximized) {
                primaryStage.setMaximized(true);
            } else {
                primaryStage.setWidth(currentWidth);
                primaryStage.setHeight(currentHeight);
            }

            // Pass the MainApp reference to the AdminLandingController
            it.unicas.project.template.address.view.AdminLandingController controller = loader.getController();
            controller.setMainApp(this); // Allows controller to navigate back or to other views

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays the User Management interface, replacing the current scene on the primary stage.
     * Preserves the stage size and maximization status.
     *
     * Access Keyword Explanation: {@code public} - This method is public as it must
     * be called from the Admin Dashboard controller to switch views.
     */
    public void showUserManagement() {
        try {
            // Store current dimensions
            double currentWidth = primaryStage.getWidth();
            double currentHeight = primaryStage.getHeight();
            boolean wasMaximized = primaryStage.isMaximized();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/UserManagement.fxml"));
            BorderPane userManagementPane = loader.load();

            // Set the new scene on the primary stage
            Scene scene = new Scene(userManagementPane);
            primaryStage.setScene(scene);
            primaryStage.setTitle("User Management");

            // Restore dimensions
            if (wasMaximized) {
                primaryStage.setMaximized(true);
            } else {
                primaryStage.setWidth(currentWidth);
                primaryStage.setHeight(currentHeight);
            }

            // Get the controller and initialize if needed (already done in initialize method)
            UserManagementController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
            // Show an alert if the FXML file can't be loaded
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("View Loading Failed");
            alert.setContentText("Could not load UserInterface.fxml.");
            alert.showAndWait();
        }
    }

    /**
     * Displays the Material Management interface (Inventory list), replacing the current scene.
     * Preserves the stage size and maximization status.
     *
     * Access Keyword Explanation: {@code public} - This method is public as it must
     * be called from the Admin Dashboard controller to switch views.
     */
    public void showMaterialManagement() {
        try {
            // Store current dimensions
            double currentWidth = primaryStage.getWidth();
            double currentHeight = primaryStage.getHeight();
            boolean wasMaximized = primaryStage.isMaximized();

            FXMLLoader loader = new FXMLLoader();
            // Load the new FXML file
            loader.setLocation(MainApp.class.getResource("view/MaterialManagement.fxml"));
            BorderPane materialManagementPane = loader.load();

            // Set the new scene on the primary stage
            Scene scene = new Scene(materialManagementPane);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Material Management (Inventory)");

            // Restore dimensions
            if (wasMaximized) {
                primaryStage.setMaximized(true);
            } else {
                primaryStage.setWidth(currentWidth);
                primaryStage.setHeight(currentHeight);
            }
            // Get the controller and initialize
            MaterialManagementController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
            // Show an alert if the FXML file can't be loaded
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("View Loading Failed");
            alert.setContentText("Could not load MaterialManagement.fxml.");
            alert.showAndWait();
        }
    }

    /**
     * Shows a modal dialog for adding a new material.
     *
     * Access Keyword Explanation: {@code public} - This method is public as it is
     * called by the {@code MaterialManagementController} to open the secondary window.
     */
    public void showAddMaterial() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/AddMaterial.fxml"));
            AnchorPane page = loader.load();

            // Create a new modal DIALOG stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Material");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage); // Set owner to main window

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            AddMaterialController controller = loader.getController();
            controller.setDialogStage(dialogStage); // Pass the dialog stage to the controller so it can close itself

            dialogStage.showAndWait(); // Wait for dialog to close

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays the Admin Material Catalog view on the primary stage.
     * Preserves the stage size and maximization status.
     *
     * Access Keyword Explanation: {@code public} - This method is public as it must
     * be called from the Admin Dashboard controller to switch views.
     */
    public void showCatalog() {
        try {
            // Store current dimensions
            double currentWidth = primaryStage.getWidth();
            double currentHeight = primaryStage.getHeight();
            boolean wasMaximized = primaryStage.isMaximized();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/AdminMaterialCatalog.fxml"));
            AnchorPane page = loader.load();

            Scene scene = new Scene(page);
            primaryStage.setScene(scene);

            // Restore dimensions
            if (wasMaximized) {
                primaryStage.setMaximized(true);
            } else {
                primaryStage.setWidth(currentWidth);
                primaryStage.setHeight(currentHeight);
            }

            AdminMaterialCatalogController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays the Loan and Return management interface, replacing the current scene on the primary stage.
     * Preserves the stage size and maximization status.
     *
     * Access Keyword Explanation: {@code public} - This method is public as it must
     * be called from the Admin Dashboard controller to switch views.
     */
    public void showLoanReturn() {
        try {
            // Store current dimensions
            double currentWidth = primaryStage.getWidth();
            double currentHeight = primaryStage.getHeight();
            boolean wasMaximized = primaryStage.isMaximized();

            FXMLLoader loader = new FXMLLoader();
            // Note: Uses an absolute path for resource loading here, unlike others.
            loader.setLocation(MainApp.class.getResource("/it/unicas/project/template/address/view/LoanReturn.fxml"));
            AnchorPane loadReturnPane = loader.load();

            Scene scene = new Scene(loadReturnPane);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Loans and Return Management");

            // Restore dimensions
            if (wasMaximized) {
                primaryStage.setMaximized(true);
            } else {
                primaryStage.setWidth(currentWidth);
                primaryStage.setHeight(currentHeight);
            }

            // Get the controller and pass the MainApp reference
            LoanReturnController controller = loader.getController();
            controller.setMainApp(this); // This line ensures that we can go back to the landing page.

        } catch (IOException e) {
            e.printStackTrace();
            // Error handling using the correctly imported Alert class
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("View Loading Failed");
            alert.setContentText("Could not load LoanReturn.fxml.");
            alert.showAndWait();
        }
    }

    /**
     * Displays a modal dialog with the user's overdue notifications.
     *
     * Access Keyword Explanation: {@code public} - This method is public as it is
     * called by the {@code UserLandingController} to show notifications.
     *
     * @param notifications A list of formatted String messages to display.
     */
    public void showNotificationsView(List<String> notifications) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/Notifications.fxml"));
            AnchorPane page = loader.load();

            // Create the dialog Stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Overdue Notifications");

            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Get the controller and pass the data and stage
            NotificationsController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setNotifications(notifications); // Pass the list of messages to display

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("View Loading Failed");
            alert.setContentText("Could not load Notifications.fxml.");
            alert.showAndWait();
        }
    }

    /**
     * Displays the User Material Catalog view on the primary stage so that users
     * can search through the list of materials to start a new reservation or check
     * the availability of a material.
     *
     * Access Keyword Explanation: {@code public} - This method is public as it must
     * be called from the User Dashboard controller to switch views.
     *
     * Preserves the stage size and maximization status.
     */
    public void showUserCatalog() {
        try {
            // Store current dimensions
            double currentWidth = primaryStage.getWidth();
            double currentHeight = primaryStage.getHeight();
            boolean wasMaximized = primaryStage.isMaximized();

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/UserCatalog.fxml")); // The 'usercatalog' refers to the user version of the catalog of materials
            AnchorPane page = loader.load();

            UserCatalogController controller = loader.getController();
            controller.setMainApp(this);
            // Pass the logged user, which the catalog controller needs to load specific data (like current loans)
            controller.setCurrentUser(loggedUser);

            Scene scene = new Scene(page);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Material Catalog");
            // Restore dimensions
            if (wasMaximized) {
                primaryStage.setMaximized(true);
            } else {
                primaryStage.setWidth(currentWidth);
                primaryStage.setHeight(currentHeight);
            }

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("View Loading Failed");
            alert.setContentText("Could not load UserCatalog.fxml.");
            alert.showAndWait();
        }
    }

    // -----------------------------------------------------------------------------------------------
    // No-Usage methods that we don't erase in case they do something useful that the compiler misses.
    // -----------------------------------------------------------------------------------------------

    /**
     * Shows the user edit dialog (e.g., for creating or editing a user) as a modal window.
     * The dialog size is based on the FXML/content.
     *
     * Access Keyword Explanation: {@code public} - This method is public as it is
     * called by the {@code UserManagementController} to open the secondary window
     * for creating or editing a user record.
     *
     * @param user the user object to be edited or created.
     * @param userManagementController the controller to refresh the table after save/delete.
     */
    public void showEditUser(User user, UserManagementController userManagementController) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/UserEdit.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit User Details");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);


            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            UserEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            controller.setSelectedUser(user); // Pass the user data to be edited

            controller.setUserManagementController(userManagementController); // Allows refreshing the parent table view

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            // Show an alert if the FXML file can't be loaded
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Dialog Loading Failed");
            alert.setContentText("Could not load UserEdit.fxml.");
            alert.showAndWait();
        }
    }

    /**
     * Returns the currently logged-in user.
     *
     * Access Keyword Explanation: {@code public} - This method is public to allow
     * any view controller (Admin or User dashboard, catalog, etc.) to retrieve
     * the necessary user information (ID, role) to personalize the view and fetch data.
     *
     * @return The User object for the logged-in user.
     */
    public User getLoggedUser() {
        return loggedUser;
    }

    /**
     * Returns the primary stage of this application.
     *
     * Access Keyword Explanation: {@code public} - This method is public to allow
     * any view controller to retrieve the main stage reference, typically used
     * to set it as the owner of a modal dialog.
     *
     * @return The primary Stage.
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * The main method, which is ignored by the IDE (if any), but needed
     * for running the application outside of an IDE.
     *
     * Access Keyword Explanation: {@code public static} - This is the standard
     * signature for the Java entry point, required by the JVM to launch the program.
     * The static modifier allows it to be called without an instance of the class.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
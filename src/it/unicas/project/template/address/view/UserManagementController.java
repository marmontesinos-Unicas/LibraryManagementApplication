package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;
import it.unicas.project.template.address.service.UserCatalogService;
import java.util.Collections;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future; // Explicitly importing Future for clarity

/**
 * Controller for User Management view.
 * This class handles the display, searching, registration, and editing of user data
 * within an administrative interface. It implements a debouncing mechanism to optimize search performance.
 */
public class UserManagementController {

    private MainApp mainApp;

    // FXML fields for UI components
    @FXML private TextField searchField;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> surnameColumn;
    @FXML private TableColumn<User, String> nationalIdColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private Button registerButton;
    @FXML private Button backButton;

    // Data structure
    private ObservableList<User> userList = FXCollections.observableArrayList();

    // Cached list of all users to avoid repeated DB calls
    private List<User> cachedAllUsers = null;

    // Service for user catalog operations
    private UserCatalogService userCatalogService = new UserCatalogService();

    // Debounce mechanism for search
    private ScheduledExecutorService searchScheduler = Executors.newSingleThreadScheduledExecutor();
    private java.util.concurrent.Future<?> searchTask;

    /**
     * Sets the reference to the main application.
     * @param mainApp The main application instance.
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Initializes the controller. This method is automatically called after the FXML file has been loaded.
     * It sets up column binding, loads initial data, and configures the debounced search listener.
     */
    @FXML
    public void initialize() {
        // 1. Initialize TableView Columns
        /**
         * Binds the name column to the "name" property of the User object.
         */
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        /**
         * Binds the surname column to the "surname" property of the User object.
         */
        surnameColumn.setCellValueFactory(new PropertyValueFactory<>("surname"));
        /**
         * Binds the national ID column to the "nationalID" property of the User object.
         */
        nationalIdColumn.setCellValueFactory(new PropertyValueFactory<>("nationalID"));
        /**
         * Binds the email column to the "email" property of the User object.
         */
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // 2. Load the initial data (from DB)
        loadInitialUserData();

        // 3. Wrap the ObservableList in a SortedList
        SortedList<User> sortedData = new SortedList<>(userList);

        // 4. Bind the SortedList comparator to the TableView comparator (allows sorting via column headers)
        sortedData.comparatorProperty().bind(userTable.comparatorProperty());

        // 5. Set the sorted data to the table
        userTable.setItems(sortedData);

        userTable.setPlaceholder(new Label("No users registered in the database."));

        // Add debounced listener for search (300ms delay)
        /**
         * Adds a listener to the search field text property to trigger a debounced search.
         */
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            scheduleSearch();
        });
    }

    /**
     * Implements the debouncing mechanism by canceling any pending search task
     * and scheduling a new search execution after a 300ms delay. The actual search
     * execution is run on the JavaFX application thread via {@code Platform.runLater()}.
     */
    private void scheduleSearch() {
        // Cancel previous search task if still pending
        if (searchTask != null && !searchTask.isDone()) {
            searchTask.cancel(false);
        }

        // Schedule new search after 300ms delay
        searchTask = searchScheduler.schedule(() -> {
            Platform.runLater(this::handleSearch);
        }, 300, TimeUnit.MILLISECONDS);
    }

    /**
     * Loads all user data from the database into the {@code userList} and caches it
     * in {@code cachedAllUsers}. This method is called upon initialization and after
     * a user registration or edit action.
     */
    public void loadInitialUserData() {
        userList.clear();
        cachedAllUsers = null; // Clear cache to force reload

        try {
            List<User> usersFromDB = UserDAOMySQLImpl.getInstance().select(null);
            cachedAllUsers = usersFromDB; // Cache the results
            userList.addAll(usersFromDB);
            System.out.println("Successfully loaded " + userList.size() + " users from the database.");

        } catch (DAOException e) {
            System.err.println("Database Error: Could not load users: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Connection Error");
            alert.setHeaderText("Failed to Load Users");
            alert.setContentText("An error occurred while fetching user data from the database.\nDetails: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Handles search execution using the cached user data.
     * Filters the cached list based on the current text in the search field and updates the table.
     */
    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        userList.clear();

        try {
            // If cache is empty, reload from database (safety net)
            if (cachedAllUsers == null) {
                cachedAllUsers = UserDAOMySQLImpl.getInstance().select(null);
            }

            if (query.isEmpty()) {
                // If search is empty, show all users from cache
                userList.addAll(cachedAllUsers);
            } else {
                // Use UserCatalogService to search cached data (performs client-side filtering)
                List<User> searchResults = userCatalogService.filterUsers(
                        cachedAllUsers,           // Use cached data
                        Collections.emptyMap(),   // No role map needed for basic user search
                        Collections.emptySet(),   // No role filter needed for basic user search
                        query
                );
                userList.addAll(searchResults);
            }

            System.out.println("Search executed. Found " + userList.size() + " matches for query: " + query);

        } catch (DAOException e) {
            System.err.println("Database Error: Could not perform search: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Search Failed");
            alert.setContentText("An error occurred while searching.\nDetails: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Handles the 'Register New User' button click event.
     * Opens the {@code UserRegistration.fxml} dialog modally.
     */
    @FXML
    private void handleRegisterNewUser() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("UserRegistration.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Register New User");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setResizable(false); // Prevent resizing of the dialog window

            Stage parentStage = (Stage) userTable.getScene().getWindow();
            dialogStage.initOwner(parentStage);

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            UserRegistrationController controller = loader.getController();
            // Pass this controller instance to the registration dialog to allow it to trigger data refresh
            controller.setUserManagementController(this);

            dialogStage.showAndWait();

            // Refresh data after dialog closes
            loadInitialUserData();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not open registration dialog");
            alert.setContentText("Check if 'UserRegistration.fxml' is correctly located and accessible.");
            alert.showAndWait();
        }
    }

    /**
     * Handles the 'Go Back' button click.
     * Navigates the application back to the Admin Landing page.
     */
    @FXML
    private void handleGoBack() {
        if (mainApp != null) {
            mainApp.showAdminLanding();
        } else {
            System.err.println("Error: MainApp reference is null. Cannot go back.");
        }
    }

    /**
     * Handles the 'View/Edit User' action, typically triggered by a button or double-click.
     * Opens the {@code UserEdit.fxml} dialog modally for the selected user.
     */
    @FXML
    private void handleViewEditUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No User Selected");
            alert.setContentText("Please select a user in the table to view or edit.");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("UserEdit.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit User: " + selectedUser.getName() + " " + selectedUser.getSurname());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setResizable(false); // Prevent resizing of the dialog window

            Stage parentStage = (Stage) userTable.getScene().getWindow();
            dialogStage.initOwner(parentStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            UserEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            // Pass this controller to the edit dialog to allow data refresh after save/delete
            controller.setUserManagementController(this);
            controller.setSelectedUser(selectedUser);

            dialogStage.showAndWait();
            // Data refresh is handled within UserEditController calling loadInitialUserData()

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not open edit dialog");
            alert.setContentText("Check if 'UserEdit.fxml' is correctly located and accessible.");
            alert.showAndWait();
        }
    }

    /**
     * Cleans up resources used by the controller, specifically shutting down the
     * {@code ScheduledExecutorService} used for search debouncing to prevent resource leaks.
     */
    public void cleanup() {
        if (searchScheduler != null && !searchScheduler.isShutdown()) {
            searchScheduler.shutdown();
        }
    }
}
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

/**
 * Controller for User Management view.
 * FIXED: Added debouncing to prevent connection leaks on search
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
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        // 1. Initialize TableView Columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        surnameColumn.setCellValueFactory(new PropertyValueFactory<>("surname"));
        nationalIdColumn.setCellValueFactory(new PropertyValueFactory<>("nationalID"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // 2. Load the initial data (from DB)
        loadInitialUserData();

        // 3. Wrap the ObservableList in a SortedList
        SortedList<User> sortedData = new SortedList<>(userList);

        // 4. Bind the SortedList comparator to the TableView comparator
        sortedData.comparatorProperty().bind(userTable.comparatorProperty());

        // 5. Set the sorted data to the table
        userTable.setItems(sortedData);

        userTable.setPlaceholder(new Label("No users registered in the database."));

        // Add debounced listener for search (300ms delay)
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            scheduleSearch();
        });
    }

    /**
     * Schedule search execution after 300ms delay
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
     * Loads initial data from the database and caches it.
     * Changed to public so it can be called externally after registration.
     */
    public void loadInitialUserData() {
        userList.clear();
        cachedAllUsers = null; // Clear cache

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
     * Handles search using cached data.
     * Uses cached data instead of hitting DB on every keystroke.
     */
    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        userList.clear();

        try {
            // If cache is empty, load from database
            if (cachedAllUsers == null) {
                cachedAllUsers = UserDAOMySQLImpl.getInstance().select(null);
            }

            if (query.isEmpty()) {
                // If search is empty, show all users from cache
                userList.addAll(cachedAllUsers);
            } else {
                // Use UserCatalogService to search cached data
                List<User> searchResults = userCatalogService.filterUsers(
                        cachedAllUsers,           // Use cached data
                        Collections.emptyMap(),   // No role map needed
                        Collections.emptySet(),   // No role filter
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
     * Handles the 'View/Edit User' button click.
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
            controller.setUserManagementController(this);
            controller.setSelectedUser(selectedUser);

            dialogStage.showAndWait();

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
     * Cleanup when controller is destroyed.
     */
    public void cleanup() {
        if (searchScheduler != null && !searchScheduler.isShutdown()) {
            searchScheduler.shutdown();
        }
    }
}
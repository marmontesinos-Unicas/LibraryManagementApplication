package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; // Import for loading FXML
import javafx.scene.Scene; // Import for setting the scene
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane; // Import for the dialog's root layout
import javafx.stage.Modality; // Import for modal behavior
import javafx.stage.Stage; // Import for the new window

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * Sets the reference to the main application.
     * Called by MainApp when loading the scene.
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    // Data structure
    private ObservableList<User> userList = FXCollections.observableArrayList();


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
        // The SortedList listens to the ObservableList and the TableView's sort properties.

        // --- IMPORTANT CHANGE FOR SORTING ---
        SortedList<User> sortedData = new SortedList<>(userList);

        // 4. Bind the SortedList comparator to the TableView comparator.
        // This makes the sorting happen when the column headers are clicked.
        sortedData.comparatorProperty().bind(userTable.comparatorProperty());

        // 5. Set the sorted and filtered data to the table.
        userTable.setItems(sortedData); // Bind the SortedList instead of userList

        userTable.setPlaceholder(new Label("No users registered in the database."));

        searchField.setOnKeyReleased(event -> {
            if (event.getCode().equals(javafx.scene.input.KeyCode.ENTER)) {
                handleSearch();
            }
        });
    }

    /**
     * Loads initial data from the database using UserDAOMySQLImpl.
     */
    public void loadInitialUserData() { // Changed to public so it can be called externally after registration
        userList.clear();

        try {
            List<User> usersFromDB = UserDAOMySQLImpl.getInstance().select(null);
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


    // --- Event Handlers ---
    /**
     * Handles the 'Search' button click event.
     * Filters the user list by Name, Surname, National ID, and Email using the DAO's select method.
     */
    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        userList.clear(); // Clear the current list

        if (query.isEmpty()) {
            // If the search field is empty, reload all initial data
            loadInitialUserData();
            return;
        }

        try {
            // Use a Set to store unique users retrieved from all search fields
            // This is crucial because a single user might match both 'name' AND 'email' for the same query.
            Set<User> uniqueResults = new HashSet<>();

            // --- Emulating OR Search using multiple DAO calls (due to DAO's AND logic) ---

            // 1. Search by Name
            User searchByName = new User();
            searchByName.setName(query);
            uniqueResults.addAll(UserDAOMySQLImpl.getInstance().select(searchByName));

            // 2. Search by Surname
            User searchBySurname = new User();
            searchBySurname.setSurname(query);
            uniqueResults.addAll(UserDAOMySQLImpl.getInstance().select(searchBySurname));

            // 3. Search by National ID
            User searchByID = new User();
            searchByID.setNationalID(query);
            uniqueResults.addAll(UserDAOMySQLImpl.getInstance().select(searchByID));

            // 4. Search by Email
            User searchByEmail = new User();
            searchByEmail.setEmail(query);
            uniqueResults.addAll(UserDAOMySQLImpl.getInstance().select(searchByEmail));

            // Add all unique results to the observable list
            userList.addAll(uniqueResults);

            System.out.println("Search executed. Found " + userList.size() + " matches for query: " + query);

        } catch (DAOException e) {
            System.err.println("Database Error: Could not perform search: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Search Failed");
            alert.setContentText("An error occurred while searching the database.\nDetails: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Handles the 'Register New User' button click event.
     * Opens the User Registration Dialog in a new modal window.
     */
    @FXML
    private void handleRegisterNewUser() {
        try {
            // Load the FXML file for the dialog
            FXMLLoader loader = new FXMLLoader();
            // Assuming UserRegistrationDialog.fxml is in the same package as the controller or a relative path
            loader.setLocation(getClass().getResource("UserRegistrationDialog.fxml"));
            AnchorPane page = loader.load();

            // Create the Dialog Stage (the new window)
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Register New User");
            // Set modality to WINDOW_MODAL so the main window is blocked until the dialog is closed
            dialogStage.initModality(Modality.WINDOW_MODAL);

            // Set the owner/parent stage (optional, but good for blocking)
            Stage parentStage = (Stage) userTable.getScene().getWindow();
            dialogStage.initOwner(parentStage);

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Get the Controller for the dialog and give it a reference back to this controller
            UserRegistrationController controller = loader.getController();
            controller.setUserManagementController(this); // Pass a reference to this controller

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            // When the dialog closes, refresh the user list to show the new user
            loadInitialUserData();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not open registration dialog");
            alert.setContentText("Check if 'UserRegistrationDialog.fxml' is correctly located and accessible.");
            alert.showAndWait();
        }
    }

    /**
     * Handles the 'Go Back' button click, switching the scene back to the Admin Landing.
     */
    @FXML
    private void handleGoBack() {
        if (mainApp != null) {
            mainApp.showAdminLanding();
        } else {
            System.err.println("Error: MainApp reference is null. Cannot go back.");
        }
    }

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
            // Load the FXML file for the dialog
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("UserEditDialog.fxml")); // Load the new FXML
            AnchorPane page = loader.load();

            // Create the Dialog Stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit User: " + selectedUser.getName()+" " + selectedUser.getSurname());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Stage parentStage = (Stage) userTable.getScene().getWindow();
            dialogStage.initOwner(parentStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Get the Controller and set the data
            UserEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setUserManagementController(this); // Pass reference back for refreshing
            controller.setSelectedUser(selectedUser);    // Pass the user object

            // Show the dialog and wait until the user closes it
            dialogStage.showAndWait();

            // Note: Data refresh is handled within UserEditController after successful save/delete.

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not open edit dialog");
            alert.setContentText("Check if 'UserEditDialog.fxml' is correctly located and accessible.");
            alert.showAndWait();
        }
    }
}
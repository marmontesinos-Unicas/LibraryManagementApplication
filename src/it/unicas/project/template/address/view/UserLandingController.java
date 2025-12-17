package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.*;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.HoldDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.LoanDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.MaterialDAOMySQLImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import it.unicas.project.template.address.service.NotificationsService;
import java.util.List;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.control.ListView;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * Controller for the User Landing page.
 * This class manages the UI for loans, holds, notifications, and user interactions.
 */
public class    UserLandingController {

    private MainApp mainApp; // Reference to MainApp so we can navigate between scenes

    // Notification logic fields
    private final NotificationsService notificationsService = new NotificationsService(); // Service for overdue notifications
    private List<String> overdueNotifications; // Cached list of notifications to display

    // ----- TableView references (Loans) -----
    @FXML
    private TableView<LoanRow> myLoansTable;
    @FXML
    private TableColumn<LoanRow, String> loanTitleColumn;
    @FXML
    private TableColumn<LoanRow, String> loanReturnDateColumn;
    @FXML
    private TableColumn<LoanRow, String> loanStatusColumn;

    // ----- TableView references (Holds) -----
    @FXML
    private TableView<HoldRow> myHoldsTable;
    @FXML
    private TableColumn<HoldRow, String> holdTitleColumn;
    @FXML
    private TableColumn<HoldRow, String> holdMaxDateColumn;

    // Lists
    @FXML
    private ListView<String> myLoansList; // Old UI component, still used for display
    @FXML
    private ListView<String> myReservationsList; // Same as above

    // Buttons
    @FXML
    private Button searchButton;
    @FXML
    private Button notificationsButton;
    @FXML
    private Button deleteHoldButton;
    @FXML
    private Button logoutButton;

    private User currentUser; // Logged-in user reference

    // Observable lists that back the TableViews
    private final ObservableList<LoanRow> loanList = FXCollections.observableArrayList();
    private final ObservableList<HoldRow> holdList = FXCollections.observableArrayList();

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    /**
     * Handle the Notifications button click.
     * Opens the notifications view.
     * @param event The action event.
     */
    @FXML
    protected void handleNotifications(ActionEvent event) {
        mainApp.showNotificationsView(overdueNotifications);
    }

    /**
     * Called automatically after FXML initialization.
     * Sets up table columns, formatting, and bindings.
     */
    @FXML
    public void initialize() {

        // ----- Loan Table Setup -----
        /**
         * Binds the loan table column to the title property of the LoanRow.
         */
        loanTitleColumn.setCellValueFactory(cell -> cell.getValue().titleProperty());
        /**
         * Binds the loan table column to the due date property of the LoanRow.
         */
        loanReturnDateColumn.setCellValueFactory(cell -> cell.getValue().dueDateProperty());
        /**
         * Binds the loan table column to the delayed status property of the LoanRow.
         */
        loanStatusColumn.setCellValueFactory(cell -> cell.getValue().delayedProperty());
        myLoansTable.setItems(loanList);

        // Color the loan status column depending on the value
        /**
         * Customizes the appearance of the loan status column based on whether the loan is "Delayed" (red) or "Active" (green).
         */
        loanStatusColumn.setCellFactory(column -> new TableCell<LoanRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Delayed" -> setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        case "Active" -> setStyle("-fx-text-fill: green;-fx-font-weight: bold;");
                        default -> setStyle("");
                    }
                }
            }
        });

        // ----- Hold Table Setup -----
        /**
         * Binds the hold table column to the title property of the HoldRow.
         */
        holdTitleColumn.setCellValueFactory(cell -> cell.getValue().titleProperty());
        /**
         * Binds the hold table column to the maximum date property of the HoldRow.
         */
        holdMaxDateColumn.setCellValueFactory(cell -> cell.getValue().maxDateProperty());
        myHoldsTable.setItems(holdList);

        // Disable delete button when nothing is selected
        /**
         * Binds the disabled property of the delete hold button to the selection state of the hold table.
         * The button is disabled when no hold is selected.
         */
        deleteHoldButton.disableProperty().bind(
                myHoldsTable.getSelectionModel().selectedItemProperty().isNull()
        );
    }

    /**
     * Checks for overdue notifications for the specified user and updates the appearance of the notifications button.
     * @param currentUser The user whose overdue status is being checked.
     */
    private void checkOverdueStatus(User currentUser) {
        try {
            overdueNotifications = notificationsService.getFormattedNotifications(currentUser.getIdUser());
            int count = overdueNotifications.size();

            if (count > 0) {
                // Highlight button if notifications exist
                notificationsButton.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-font-weight: bold;");
                notificationsButton.setText("Notifications (" + count + ")");
            } else {
                notificationsButton.setStyle("");
                notificationsButton.setText("Notifications");
                overdueNotifications = null;
            }
        } catch (DAOException e) {
            System.err.println("CRITICAL ERROR: Could not load overdue status: " + e.getMessage());
            notificationsButton.setText("Notifications (Error)");
            overdueNotifications = null;
        }
    }

    /**
     * Loads the current loans and holds for the logged-in user from the database and populates the UI tables.
     */
    private void loadUserData() {
        if (currentUser == null) return;

        try {
            // ----- Load Loans -----
            List<Loan> userLoans = LoanDAOMySQLImpl.getInstance()
                    .select(new Loan(null, currentUser.getIdUser(), null, null, null, null))
                    .stream().collect(Collectors.toList());

            loanList.clear();

            for (Loan loan : userLoans) {

                // Fetch the material to get its title
                Material mat = new Material();
                mat.setIdMaterial(loan.getIdMaterial());
                mat = MaterialDAOMySQLImpl.getInstance().select(mat).stream().findFirst().orElse(null);

                String title = (mat != null && mat.getTitle() != null) ? mat.getTitle() : "Unknown";

                String returnDate = (loan.getReturn_date() != null)
                        ? loan.getReturn_date().format(dateFormatter)
                        : "Not Returned";

                // Determine loan status (Active, Delayed, Returned)
                String status;

                if (loan.getReturn_date() != null) {
                    status = "Returned";
                } else if (loan.getDue_date() != null &&
                        loan.getDue_date().isBefore(java.time.LocalDateTime.now())) {
                    status = "Delayed";
                } else {
                    status = "Active";
                }

                loanList.add(new LoanRow(loan.getIdLoan(), "", title, "", "", "", returnDate, status));
            }

            // ----- Load Holds -----
            List<Hold> userHolds = HoldDAOMySQLImpl.getInstance()
                    .select(new Hold(null, currentUser.getIdUser(), null, null))
                    .stream().collect(Collectors.toList());

            holdList.clear();
            for (Hold hold : userHolds) {
                Material mat = null;

                if (hold.getIdMaterial() != -1) {
                    mat = new Material();
                    mat.setIdMaterial(hold.getIdMaterial());
                    mat = MaterialDAOMySQLImpl.getInstance().select(mat).stream().findFirst().orElse(null);
                }

                String title = (mat != null && mat.getTitle() != null) ? mat.getTitle() : "Unknown";
                String maxDate = (hold.getHold_date() != null)
                        ? hold.getHold_date().format(dateFormatter)
                        : "-";

                holdList.add(new HoldRow(hold.getIdHold(), title, maxDate));
            }

        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Event handler for the search button.
     * Navigates to the user catalog view to allow the user to search for materials.
     * @param event The action event.
     */
    @FXML
    protected void handleSearch(ActionEvent event) {
        if (mainApp != null) {
            mainApp.showUserCatalog();
        } else {
            System.err.println("mainApp is null - call setMainApp(...) when loading the user view.");
        }
    }

    /**
     * Handles deleting a selected hold from the {@code myHoldsTable}.
     * Displays a confirmation dialog, removes the hold from the database, and updates the material status if necessary.
     */
    @FXML
    private void handleDeleteHold() {
        HoldRow selected = myHoldsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Hold Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a hold before trying to delete it.");
            alert.showAndWait();
            return;
        }

        // Confirm deletion
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Hold Removal");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to remove the hold for \"" + selected.getTitle() + "\"?");
        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No", ButtonType.CANCEL.getButtonData());
        confirm.getButtonTypes().setAll(yes, no);

        if (confirm.showAndWait().orElse(no) != yes) {
            return;
        }

        try {
            // Retrieve the real hold using the ID
            Hold filter = new Hold();
            filter.setIdHold(selected.getIdHold());
            var holds = HoldDAOMySQLImpl.getInstance().select(filter);

            if (holds == null || holds.isEmpty()) {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Error");
                err.setHeaderText(null);
                err.setContentText("Could not find the hold in the database.");
                err.showAndWait();
                return;
            }

            Hold realHold = holds.get(0);

            // Update material status if linked
            Integer matId = realHold.getIdMaterial();
            boolean materialUpdated = false;

            if (matId != null && matId != -1) {
                Material mFilter = new Material();
                mFilter.setIdMaterial(matId);
                var mats = MaterialDAOMySQLImpl.getInstance().select(mFilter);

                if (mats != null && !mats.isEmpty()) {
                    Material mat = mats.get(0);
                    // Assume status should be reset to "available" if the hold is removed
                    mat.setMaterial_status("available");
                    MaterialDAOMySQLImpl.getInstance().update(mat);
                    materialUpdated = true;
                }
            }

            // Delete hold
            HoldDAOMySQLImpl.getInstance().delete(realHold);

            // And update notifications
            if (currentUser != null) {
                // Check overdue notifications for this user
                checkOverdueStatus(currentUser);
            }

            // Remove from UI
            holdList.removeIf(h -> h.getIdHold() == selected.getIdHold());

            // Show success
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Hold Removed");
            info.setHeaderText(null);
            info.setContentText("Hold for \"" + selected.getTitle() + "\" has been removed.");
            info.showAndWait();

        } catch (DAOException e) {
            e.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText("Operation failed");
            error.setContentText("An error occurred while removing the hold. Please try again.");
            error.showAndWait();
        }
    }

    /**
     * Handles logout logic.
     * Shows a confirmation dialog. If confirmed, opens the login dialog and closes the current window.
     *
     * @param event The action event.
     */
    @FXML
    protected void handleLogout(ActionEvent event) {
        if (mainApp == null) {
            System.err.println("Error: MainApp reference is null. Cannot log out.");
            return;
        }

        // --- NEW LOGIC: Show Confirmation Dialog ---
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
            mainApp.showLoginDialog(); // Show login window again

            // 2. Explicitly close the current Admin Landing window/stage.
            try {
                // Find the stage from the event source
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.hide(); // Hide the window
            } catch (Exception e) {
                System.err.println("Error closing User Landing stage: " + e.getMessage());
            }

        } else {
            // User cancelled or closed the dialog. Do nothing.
            System.out.println("Logout cancelled by the user.");
        }
    }
    /**
     * Called by MainApp after loading the FXML. Sets the reference to MainApp.
     * @param mainApp main application reference
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Sets the current logged user and loads user data.
     * @param user the logged user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;

        if (currentUser != null) {
            // Check overdue notifications for this user
            checkOverdueStatus(currentUser);
        }

        loadUserData(); // Load loans and holds for the user
    }

}

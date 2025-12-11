
package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Hold;
import it.unicas.project.template.address.model.Loan;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.HoldDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.LoanDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.MaterialDAOMySQLImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Stage;


import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class UserLandingController {

    private MainApp mainApp; // <--- AÑADIDO

    // Tables
    @FXML private TableView<LoanRow> myLoansTable;
    @FXML private TableColumn<LoanRow, String> loanTitleColumn;
    @FXML private TableColumn<LoanRow, String> loanReturnDateColumn;
    @FXML private TableColumn<LoanRow, String> loanStatusColumn;

    @FXML private TableView<HoldRow> myHoldsTable;
    @FXML private TableColumn<HoldRow, String> holdTitleColumn;
    @FXML private TableColumn<HoldRow, String> holdMaxDateColumn;

    // Buttons
    @FXML private Button searchButton;
    @FXML private Button notificationsButton;
    @FXML private Button deleteHoldButton;
    @FXML private Button logoutButton;

    private User currentUser;

    private final ObservableList<LoanRow> loanList = FXCollections.observableArrayList();
    private final ObservableList<HoldRow> holdList = FXCollections.observableArrayList();

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserData();
    }

    @FXML
    public void initialize() {
        // ---------- Loan Table ----------
        loanTitleColumn.setCellValueFactory(cell -> cell.getValue().titleProperty());
        loanReturnDateColumn.setCellValueFactory(cell -> cell.getValue().dueDateProperty());
        loanStatusColumn.setCellValueFactory(cell -> cell.getValue().delayedProperty());
        myLoansTable.setItems(loanList);

        // Colorear status: Active = verde, Delayed = rojo
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
                        default -> setStyle(""); // Returned u otros
                    }
                }
            }
        });

        // ---------- Hold Table ----------
        holdTitleColumn.setCellValueFactory(cell -> cell.getValue().titleProperty());
        holdMaxDateColumn.setCellValueFactory(cell -> cell.getValue().maxDateProperty());
        myHoldsTable.setItems(holdList);

        // -----------------------------------------
        // Disable Delete button if nothing selected
        // -----------------------------------------
        deleteHoldButton.disableProperty().bind(
                myHoldsTable.getSelectionModel().selectedItemProperty().isNull()
        );
    }


    private void loadUserData() {
        if (currentUser == null) return;

        try {
            // Load Loans
            List<Loan> userLoans = LoanDAOMySQLImpl.getInstance()
                    .select(new Loan(null, currentUser.getIdUser(), null, null, null, null))
                    .stream().collect(Collectors.toList());

            loanList.clear();
            for (Loan loan : userLoans) {

                Material mat = new Material();
                mat.setIdMaterial(loan.getIdMaterial());
                mat = MaterialDAOMySQLImpl.getInstance().select(mat).stream().findFirst().orElse(null);

                String title = (mat != null && mat.getTitle() != null) ? mat.getTitle() : "Unknown";
                String returnDate = (loan.getReturn_date() != null)
                        ? loan.getReturn_date().format(dateFormatter)
                        : "Not Returned";

                // ---------------------------
                // STATUS LOGIC
                // ---------------------------
                String status;

                if (loan.getReturn_date() != null) {
                    status = "Returned";
                } else if (loan.getDue_date() != null &&
                        loan.getDue_date().isBefore(java.time.LocalDateTime.now())) {
                    status = "Delayed";
                } else {
                    status = "Active";
                }

                loanList.add(new LoanRow(loan.getIdLoan(), "", title, "", returnDate, status));
            }

            // Load Holds
            List<Hold> userHolds = HoldDAOMySQLImpl.getInstance()
                    .select(new Hold(null, currentUser.getIdUser(), null, null))
                    .stream().collect(Collectors.toList());

            holdList.clear();
            for (Hold hold : userHolds) {
                Material mat = null;
                if (hold.getIdMaterial() != null) {
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


    @FXML
    private void handleSearch() {
        System.out.println("Search clicked");
    }

    @FXML
    private void handleNotifications() {
        System.out.println("Notifications clicked");
    }

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

        // Confirm with user (show title for friendliness)
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Hold Removal");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to remove the hold for \"" + selected.getTitle() + "\"?");
        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No", ButtonType.CANCEL.getButtonData());
        confirm.getButtonTypes().setAll(yes, no);

        if (confirm.showAndWait().orElse(no) != yes) {
            return; // user cancelled
        }

        try {
            // 1) Retrieve the real Hold from DB (so we have idMaterial)
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

            // 2) If the hold references a material, mark that material as available
            Integer matId = realHold.getIdMaterial();
            boolean materialUpdated = false;
            if (matId != null && matId != -1) {
                Material mFilter = new Material();
                mFilter.setIdMaterial(matId);
                var mats = MaterialDAOMySQLImpl.getInstance().select(mFilter);
                if (mats != null && !mats.isEmpty()) {
                    Material mat = mats.get(0);
                    mat.setMaterial_status("available");
                    MaterialDAOMySQLImpl.getInstance().update(mat);
                    materialUpdated = true;
                }
            }

            // 3) Delete the hold from DB
            HoldDAOMySQLImpl.getInstance().delete(realHold);

            // 4) Remove from UI list
            holdList.removeIf(h -> h.getIdHold() == selected.getIdHold());

            // 5) Inform the user
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
     * Handles the action for the "Logout" button.
     * Logic: Calls MainApp to show the Login Dialog, then explicitly CLOSES
     * the current User Landing stage to remove it from the screen.
     *
     * @param event The action event.
     */
    @FXML
    protected void handleLogout(ActionEvent event) {
        if (mainApp != null) {
            System.out.println("Action: Logging out.");

            // 1. Show the Login Dialog (opens the new modal Stage)
            // This must happen first so the app has an open window after the next step.
            mainApp.showLoginDialog();

            // 2. Explicitly CLOSE the current User Landing window/stage (the primaryStage)
//            try {
//                // Correctly casts the Window object to a Stage before calling close()
//                ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
//            } catch (Exception e) {
//                // Should only occur if the event source is somehow invalid
//                System.err.println("Error closing User Landing stage: " + e.getMessage());
//            }

        } else {
            System.err.println("Error: MainApp reference is null. Cannot log out.");
        }
    }
}
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

    private User currentUser;

    private final ObservableList<LoanRow> loanList = FXCollections.observableArrayList();
    private final ObservableList<HoldRow> holdList = FXCollections.observableArrayList();

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ------------------- AÑADIDO -------------------
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
    // ------------------------------------------------

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

        // ---------- Hold Table ----------
        holdTitleColumn.setCellValueFactory(cell -> cell.getValue().titleProperty());
        holdMaxDateColumn.setCellValueFactory(cell -> cell.getValue().maxDateProperty());
        myHoldsTable.setItems(holdList);
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
                String returnDate = (loan.getReturn_date() != null) ? loan.getReturn_date().format(dateFormatter) : "Not Returned";
                String status = (loan.getReturn_date() == null) ? "Active" : "Returned";

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
                String maxDate = (hold.getHold_date() != null) ? hold.getHold_date().format(dateFormatter) : "-";

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
        if (selected != null) {
            try {
                Hold hold = new Hold();
                hold.setIdHold(selected.getIdHold());
                HoldDAOMySQLImpl.getInstance().delete(hold);
                holdList.remove(selected);
                System.out.println("Deleted hold: " + selected.getIdHold());
            } catch (DAOException e) {
                e.printStackTrace();
            }
        }
    }
}

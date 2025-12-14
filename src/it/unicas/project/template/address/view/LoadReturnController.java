package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Loan;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.LoanDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.MaterialDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;
import it.unicas.project.template.address.service.LoanCatalogService;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;


import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller class for loading, searching, and returning loans.
 * Handles user interactions, table display, real-time search, and loan return logic.
 */
public class LoadReturnController {

    @FXML private TextField searchField;                 // Field for searching loans
    @FXML private Button searchButton;                   // Button to clear search
    @FXML private Button returnLoanButton;               // Button to return selected loan

    @FXML private TableView<LoanRow> loansTable;        // Table to display loans
    @FXML private TableColumn<LoanRow, String> materialTypeColumn;
    @FXML private TableColumn<LoanRow, String> titleColumn;
    @FXML private TableColumn<LoanRow, String> userColumn;
    @FXML private TableColumn<LoanRow, String> dueDateColumn;
    @FXML private TableColumn<LoanRow, String> delayedColumn;

    private Stage dialogStage;                           // Reference to the dialog stage
    private ObservableList<LoanRow> loanRows = FXCollections.observableArrayList();
    private MainApp mainApp;
    private LoanCatalogService loanCatalogService = new LoanCatalogService();


    /**
     * Sets the dialog stage for this controller.
     * @param dialogStage The stage object.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Sets a reference back to the main application.
     * @param mainApp The main application object.
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Initializes the controller.
     * Sets up table columns, colors for delayed loans, search functionality, and button actions.
     */
    @FXML
    private void initialize() {
        loansTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Configure table columns
        materialTypeColumn.setCellValueFactory(data -> data.getValue().materialTypeProperty());
        titleColumn.setCellValueFactory(data -> data.getValue().titleProperty());
        userColumn.setCellValueFactory(data -> data.getValue().userProperty());
        dueDateColumn.setCellValueFactory(data -> data.getValue().dueDateProperty());
        delayedColumn.setCellValueFactory(data -> data.getValue().delayedProperty());

        // Color delayed loans in red
        delayedColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setTextFill(item.equalsIgnoreCase("Yes") ? Color.RED : Color.BLACK);
                }
            }
        });

        loansTable.setItems(loanRows);
        loadAllLoans();

        // Return loan button
        returnLoanButton.setOnAction(e -> handleReturnLoan());

        // Real-time search listener
        searchField.textProperty().addListener((obs, oldText, newText) -> handleSearch());

        // Configure search button as Clear
        searchButton.setText("Clear");
        searchButton.setOnAction(e -> handleClear());
    }

    /**
     * Clears the search field and reloads all active loans.
     */
    private void handleClear() {
        searchField.clear();
        loadAllLoans();
    }
    /**
     * Handles navigation back to the admin landing page.
     */
    @FXML
    private void handleBackToHome() {
        if (mainApp != null) {
            mainApp.showAdminLanding();
        }
    }

    /**
     * Loads all active loans into the table.
     */
    private void loadAllLoans() {
        loanRows.clear();
        try {
            List<Loan> loans = LoanDAOMySQLImpl.getInstance().select(null);
            for (Loan loan : loans) {
                if (loan.getReturn_date() == null) {
                    LoanRow row = buildLoanRow(loan);
                    if (row != null) loanRows.add(row);
                }
            }
            loanRows.sort(Comparator.comparing(LoanRow::getDueDateAsLocalDate));
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds a LoanRow object for table display.
     * @param loan Loan entity from the database
     * @return LoanRow object for TableView, or null if material/user not found
     * @throws DAOException if database access fails
     */
    private LoanRow buildLoanRow(Loan loan) throws DAOException {
        Material m = new Material(); m.setIdMaterial(loan.getIdMaterial());
        m = MaterialDAOMySQLImpl.getInstance().select(m).stream().findFirst().orElse(null);

        User u = new User(); u.setIdUser(loan.getIdUser());
        u = UserDAOMySQLImpl.getInstance().select(u).stream().findFirst().orElse(null);

        if (m == null || u == null) return null;

        String materialType = switch (m.getIdMaterialType() != null ? m.getIdMaterialType() : 0) {
            case 1 -> "Book";
            case 2 -> "CD";
            case 3 -> "Movie";
            case 4 -> "Magazine";
            default -> "Unknown";
        };

        String title = m.getTitle() != null ? m.getTitle() : "—";
        String userName = (u.getName() != null ? u.getName() : "") + " " + (u.getSurname() != null ? u.getSurname() : "");
        String due = loan.getDue_date() != null ? loan.getDue_date().toLocalDate().toString() : "—";
        boolean delayed = loan.getDue_date() != null && loan.getDue_date().isBefore(LocalDateTime.now());

        return new LoanRow(loan.getIdLoan(), materialType, title, userName, due, delayed ? "Yes" : "No");
    }

    /**
     * Handles searching loans based on user input.
     * Supports searching by user name, surname, material title, or delayed loans keywords.
     */
    @FXML
    private void handleSearch() {
        String text = searchField.getText().trim();

        loanRows.clear();

        try {
            // Get all active (not returned) loans
            List<Loan> loans = LoanDAOMySQLImpl.getInstance().select(null);
            loans = loans.stream()
                    .filter(loan -> loan.getReturn_date() == null)
                    .toList();

            // Build material and user maps for the service
            Map<Integer, Material> materialMap = new HashMap<>();
            Map<Integer, User> userMap = new HashMap<>();

            for (Loan loan : loans) {
                // Get material if not already in map
                if (!materialMap.containsKey(loan.getIdMaterial())) {
                    Material m = new Material();
                    m.setIdMaterial(loan.getIdMaterial());
                    Material found = MaterialDAOMySQLImpl.getInstance().select(m)
                            .stream().findFirst().orElse(null);
                    if (found != null) {
                        materialMap.put(loan.getIdMaterial(), found);
                    }
                }

                // Get user if not already in map
                if (!userMap.containsKey(loan.getIdUser())) {
                    User u = new User();
                    u.setIdUser(loan.getIdUser());
                    User found = UserDAOMySQLImpl.getInstance().select(u)
                            .stream().findFirst().orElse(null);
                    if (found != null) {
                        userMap.put(loan.getIdUser(), found);
                    }
                }
            }

            // Determine status filter based on search text
            Set<String> statusFilter = new HashSet<>();
            if (text.equals("delayed") || text.equals("delay") || text.equals("delays")
                    || text.equals("late") || text.equals("overdue")) {
                statusFilter.add("overdue");
            } else {
                // Empty set means no status filtering (show all active loans)
                statusFilter = new HashSet<>();
            }

            // Use the service to filter and search
            String searchTerm = statusFilter.contains("overdue") ? "" : text;
            List<Loan> filteredLoans = loanCatalogService.filterLoans(
                    loans,
                    materialMap,
                    userMap,
                    statusFilter,
                    searchTerm
            );

            // Build rows from filtered loans
            for (Loan loan : filteredLoans) {
                Material m = materialMap.get(loan.getIdMaterial());
                User u = userMap.get(loan.getIdUser());

                if (m != null && u != null) {
                    String materialType = switch (m.getIdMaterialType() != null ? m.getIdMaterialType() : 0) {
                        case 1 -> "Book";
                        case 2 -> "CD";
                        case 3 -> "Movie";
                        case 4 -> "Magazine";
                        default -> "Unknown";
                    };

                    String title = m.getTitle() != null ? m.getTitle() : "—";
                    String userName = (u.getName() != null ? u.getName() : "") + " " + (u.getSurname() != null ? u.getSurname() : "");
                    String due = loan.getDue_date() != null ? loan.getDue_date().toLocalDate().toString() : "—";
                    boolean delayed = loan.getDue_date() != null && loan.getDue_date().isBefore(LocalDateTime.now());

                    loanRows.add(new LoanRow(loan.getIdLoan(), materialType, title, userName, due, delayed ? "Yes" : "No"));
                }
            }

            loanRows.sort(Comparator.comparing(LoanRow::getDueDateAsLocalDate));

        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the Add Loan dialog and refreshes the table after closing.
     */
    @FXML
    public void handleAddLoan() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddLoanDialog.fxml"));
            Parent page = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Add New Loan");
            dialog.initOwner(dialogStage);

            AddLoanController controller = loader.getController();
            controller.setDialogStage(dialog);

            Scene scene = new Scene(page);
            dialog.setScene(scene);
            dialog.showAndWait();

            loadAllLoans(); // Refresh table

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles returning the selected loan.
     * Updates the loan return date and sets the material status to "available".
     */
    @FXML
    private void handleReturnLoan() {
        LoanRow selected = loansTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a loan first.");
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Return Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure " + selected.getUser() + " is returning " + selected.getTitle() + "?");

        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(yes, no);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == yes) {
            try {
                // Find the loan by id
                Loan filtro = new Loan();
                filtro.setIdLoan(selected.getIdLoan());

                List<Loan> loans = LoanDAOMySQLImpl.getInstance().select(filtro);

                if (loans.isEmpty()) {
                    Alert error = new Alert(Alert.AlertType.ERROR, "Error: loan not found in database.");
                    error.showAndWait();
                    return;
                }

                Loan loanReal = loans.get(0);

                // Update return date
                loanReal.setReturn_date(LocalDateTime.now());
                LoanDAOMySQLImpl.getInstance().update(loanReal);

                // Mark material as available
                Material material = new Material();
                material.setIdMaterial(loanReal.getIdMaterial());
                material = MaterialDAOMySQLImpl.getInstance().select(material).get(0);

                material.setMaterial_status("available");
                MaterialDAOMySQLImpl.getInstance().update(material);

                // Refresh table
                loadAllLoans();

            } catch (DAOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Inner class representing a row in the loans TableView.
     */
    public static class LoanRow {
        private final int idLoan;
        private final SimpleStringProperty materialType;
        private final SimpleStringProperty title;
        private final SimpleStringProperty user;
        private final SimpleStringProperty dueDate;
        private final SimpleStringProperty delayed;

        public LoanRow(int idLoan, String materialType, String title, String user, String dueDate, String delayed) {
            this.idLoan = idLoan;
            this.materialType = new SimpleStringProperty(materialType);
            this.title = new SimpleStringProperty(title);
            this.user = new SimpleStringProperty(user);
            this.dueDate = new SimpleStringProperty(dueDate);
            this.delayed = new SimpleStringProperty(delayed);
        }

        public int getIdLoan() { return idLoan; }
        public SimpleStringProperty materialTypeProperty() { return materialType; }
        public SimpleStringProperty titleProperty() { return title; }
        public SimpleStringProperty userProperty() { return user; }
        public SimpleStringProperty dueDateProperty() { return dueDate; }
        public SimpleStringProperty delayedProperty() { return delayed; }

        public String getTitle() { return title.get(); }
        public String getUser() { return user.get(); }

        /**
         * Converts due date string to LocalDateTime.
         * @return LocalDateTime of due date or MAX if invalid
         */
        public LocalDateTime getDueDateAsLocalDate() {
            if (dueDate.get() == null || dueDate.get().equals("—")) return LocalDateTime.MAX;
            return LocalDateTime.parse(dueDate.get() + "T00:00:00");
        }
    }
}

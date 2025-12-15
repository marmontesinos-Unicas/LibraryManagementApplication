package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.view.LoanRow;
import it.unicas.project.template.address.model.Loan;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.LoanDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.MaterialDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;
import it.unicas.project.template.address.service.LoanCatalogService;
import javafx.application.Platform;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Controller class for loading, searching, and returning loans.
 * Handles user interactions, table display, real-time search, and loan return logic.
 */
public class LoanReturnController {

    @FXML private TextField searchField;                 // Field for searching loans
    @FXML private Button searchButton;                   // Button to clear search
    @FXML private Button returnLoanButton;
    @FXML private Button editLoanButton;

    @FXML private TableView<LoanRow> loansTable;        // Table to display loans
    @FXML private TableColumn<LoanRow, String> materialTypeColumn;
    @FXML private TableColumn<LoanRow, String> titleColumn;
    @FXML private TableColumn<LoanRow, String> authorColumn;
    @FXML private TableColumn<LoanRow, String> isbnColumn;
    @FXML private TableColumn<LoanRow, String> userColumn;
    @FXML private TableColumn<LoanRow, String> dueDateColumn;
    @FXML private TableColumn<LoanRow, String> delayedColumn;

    private Stage dialogStage;                           // Reference to the dialog stage
    private ObservableList<LoanRow> loanRows = FXCollections.observableArrayList();
    private MainApp mainApp;
    private LoanCatalogService loanCatalogService = new LoanCatalogService();

    // Cache for loaded data to avoid repeated DB calls
    private List<Loan> cachedLoans = new ArrayList<>();
    private Map<Integer, Material> cachedMaterials = new HashMap<>();
    private Map<Integer, User> cachedUsers = new HashMap<>();

    // Debounce mechanism for search
    private ScheduledExecutorService searchScheduler = Executors.newSingleThreadScheduledExecutor();
    private java.util.concurrent.Future<?> searchTask;

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
        authorColumn.setCellValueFactory(data -> data.getValue().authorProperty());
        isbnColumn.setCellValueFactory(data -> data.getValue().isbnProperty());
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

        // FIXED: Debounced search listener (waits 300ms after typing stops)
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            // Cancel previous search task if still pending
            if (searchTask != null && !searchTask.isDone()) {
                searchTask.cancel(false);
            }

            // Schedule new search after 300ms delay
            searchTask = searchScheduler.schedule(() -> {
                Platform.runLater(this::handleSearch);
            }, 300, TimeUnit.MILLISECONDS);
        });

        // Configure search button as Clear
        searchButton.setText("Clear");
        searchButton.setOnAction(e -> handleClear());
    }

    /**
     * Clears the search field and reloads all active loans.
     */
    private void handleClear() {
        searchField.clear();
        // Force reload from database
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
     * Loads all active loans into the table and caches data.
     */
    private void loadAllLoans() {
        loanRows.clear();
        cachedLoans.clear();
        cachedMaterials.clear();
        cachedUsers.clear();

        try {
            // Load all loans once
            List<Loan> allLoans = LoanDAOMySQLImpl.getInstance().select(null);

            for (Loan loan : allLoans) {
                if (loan.getReturn_date() == null) {
                    cachedLoans.add(loan);

                    // Cache material if not already cached
                    if (!cachedMaterials.containsKey(loan.getIdMaterial())) {
                        Material m = new Material();
                        m.setIdMaterial(loan.getIdMaterial());
                        Material found = MaterialDAOMySQLImpl.getInstance().select(m)
                                .stream().findFirst().orElse(null);
                        if (found != null) {
                            cachedMaterials.put(loan.getIdMaterial(), found);
                        }
                    }

                    // Cache user if not already cached
                    if (!cachedUsers.containsKey(loan.getIdUser())) {
                        User u = new User();
                        u.setIdUser(loan.getIdUser());
                        User found = UserDAOMySQLImpl.getInstance().select(u)
                                .stream().findFirst().orElse(null);
                        if (found != null) {
                            cachedUsers.put(loan.getIdUser(), found);
                        }
                    }

                    LoanRow row = buildLoanRowFromCache(loan);
                    if (row != null) loanRows.add(row);
                }
            }

            loanRows.sort(Comparator.comparing(LoanRow::getDueDateAsLocalDate));

        } catch (DAOException e) {
            e.printStackTrace();
            showError("Database Error", "Failed to load loans: " + e.getMessage());
        }
    }

    /**
     * Builds a LoanRow using cached data.
     */
    private LoanRow buildLoanRowFromCache(Loan loan) {
        Material m = cachedMaterials.get(loan.getIdMaterial());
        User u = cachedUsers.get(loan.getIdUser());

        if (m == null || u == null) return null;

        String materialType = switch (m.getIdMaterialType() != null ? m.getIdMaterialType() : 0) {
            case 1 -> "Book";
            case 2 -> "CD";
            case 3 -> "Movie";
            case 4 -> "Magazine";
            default -> "Unknown";
        };

        String title = m.getTitle() != null ? m.getTitle() : "—";
        String author = m.getAuthor() != null ? m.getAuthor() : "—";
        String isbn = m.getISBN() != null ? m.getISBN() : "—";
        String userName = (u.getName() != null ? u.getName() : "") + " " + (u.getSurname() != null ? u.getSurname() : "");
        String due = loan.getDue_date() != null ? loan.getDue_date().toLocalDate().toString() : "—";
        boolean delayed = loan.getDue_date() != null && loan.getDue_date().isBefore(LocalDateTime.now());

        return new LoanRow(loan.getIdLoan(), materialType, title, author, isbn, userName, due, delayed ? "Yes" : "No");
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
            // Determine status filter
            Set<String> statusFilter = new HashSet<>();
            if (text.equalsIgnoreCase("delayed") || text.equalsIgnoreCase("delay") ||
                    text.equalsIgnoreCase("delays") || text.equalsIgnoreCase("late") ||
                    text.equalsIgnoreCase("overdue")) {
                statusFilter.add("overdue");
            }

            // Use cached data for filtering
            String searchTerm = statusFilter.contains("overdue") ? "" : text;
            List<Loan> filteredLoans = loanCatalogService.filterLoans(
                    cachedLoans,
                    cachedMaterials,
                    cachedUsers,
                    statusFilter,
                    searchTerm
            );

            // Build rows from filtered loans
            for (Loan loan : filteredLoans) {
                LoanRow row = buildLoanRowFromCache(loan);
                if (row != null) {
                    loanRows.add(row);
                }
            }

            loanRows.sort(Comparator.comparing(LoanRow::getDueDateAsLocalDate));

        } catch (Exception e) {
            e.printStackTrace();
            showError("Search Error", "Failed to search loans: " + e.getMessage());
        }
    }

    /**
     * Opens the Add Loan dialog and refreshes the table after closing.
     */
    @FXML
    public void handleAddLoan() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddLoan.fxml"));
            Parent page = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Add New Loan");
            dialog.initOwner(dialogStage);

            AddLoanController controller = loader.getController();
            controller.setDialogStage(dialog);

            Scene scene = new Scene(page);
            dialog.setScene(scene);
            dialog.showAndWait();

            loadAllLoans(); // Refresh table and cache

        } catch (Exception e) {
            e.printStackTrace();
            showError("Dialog Error", "Failed to open Add Loan dialog: " + e.getMessage());
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
                    showError("Error", "Loan not found in database.");
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

                // Refresh table and cache
                loadAllLoans();

            } catch (DAOException e) {
                e.printStackTrace();
                showError("Database Error", "Failed to return loan: " + e.getMessage());
            }
        }
    }

    /**
     * Handles editing the selected loan.
     * Opens the Modify Loan dialog and refreshes the table after closing.
     */
    @FXML
    private void handleEditLoan() {
        LoanRow selected = loansTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a loan first.");
            alert.showAndWait();
            return;
        }

        try {
            // Obtener Loan real desde la BD
            Loan filtro = new Loan();
            filtro.setIdLoan(selected.getIdLoan());
            List<Loan> loans = LoanDAOMySQLImpl.getInstance().select(filtro);
            if (loans.isEmpty()) {
                Alert error = new Alert(Alert.AlertType.ERROR, "Loan not found in database.");
                error.showAndWait();
                return;
            }

            Loan loanToEdit = loans.get(0);

            // Cargar ModifyLoanDialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ModifyLoan.fxml"));
            Parent page = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Modify Loan");
            dialog.initOwner(dialogStage);

            ModifyLoanController controller = loader.getController();
            controller.setDialogStage(dialog);
            controller.setLoan(loanToEdit);

            Scene scene = new Scene(page);
            dialog.setScene(scene);
            dialog.showAndWait();

            loadAllLoans(); // refrescar tabla después de modificar

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows an error alert dialog.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

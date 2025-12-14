package it.unicas.project.template.address.view;

import it.unicas.project.template.address.model.Loan;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.Hold;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.LoanDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.MaterialDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.HoldDAOMySQLImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

/**
 * Controller class for adding a new loan in the library system.
 * Handles user input, material selection, validation, and database operations.
 */
public class AddLoanController {

    @FXML private TextField nationalIDField;        // Field for entering user national ID
    @FXML private TextField searchMaterialField;    // Field for searching materials
    @FXML private Button searchButton;              // Button to clear search
    @FXML private TableView<Material> materialTable; // Table displaying materials
    @FXML private TableColumn<Material, String> titleColumn;
    @FXML private TableColumn<Material, String> authorColumn;
    @FXML private TableColumn<Material, String> isbnColumn;
    @FXML private Button addLoanButton;             // Button to create a loan

    private Stage dialogStage;                       // Reference to the stage for dialogs
    private final ObservableList<Material> materialList = FXCollections.observableArrayList();

    /**
     * Initializes the controller.
     * Sets up table columns, color formatting, loads materials, and configures search behavior.
     */
    @FXML
    public void initialize() {
        // Configure table columns
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        authorColumn.setCellValueFactory(cellData -> cellData.getValue().authorProperty());
        isbnColumn.setCellValueFactory(cellData -> cellData.getValue().ISBNProperty());

        // Change title text color if material is on hold
        titleColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTextFill(Color.BLACK);
                } else {
                    Material material = getTableView().getItems().get(getIndex());
                    setText(item);
                    if ("hold".equalsIgnoreCase(material.getMaterial_status())) {
                        setTextFill(Color.ORANGE);
                    } else {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });

        materialTable.setItems(materialList);
        loadAvailableMaterials();

        // Real-time search listener
        searchMaterialField.textProperty().addListener((obs, oldText, newText) -> handleSearch());

        // Configure search button as Clear
        searchButton.setText("Clear");
        searchButton.setOnAction(e -> handleClear());
    }

    /**
     * Loads all available or hold materials into the table.
     */
    private void loadAvailableMaterials() {
        materialList.clear();
        try {
            var results = MaterialDAOMySQLImpl.getInstance().select(null);
            if (results != null) {
                for (Material m : results) {
                    // Only include materials that are available or on hold
                    if ("available".equalsIgnoreCase(m.getMaterial_status()) ||
                            "hold".equalsIgnoreCase(m.getMaterial_status())) {
                        materialList.add(m);
                    }
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error retrieving materials: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clears the search field and reloads all available materials.
     */
    @FXML
    private void handleClear() {
        searchMaterialField.clear();
        loadAvailableMaterials();
    }

    /**
     * Checks if all search words are present in the target text.
     * Used for multi-word search matching.
     *
     * @param textToCheck Text to search in
     * @param searchText Text entered by the user
     * @return true if all words in searchText are found in textToCheck
     */
    private boolean matchesWords(String textToCheck, String searchText) {
        if (textToCheck == null || searchText == null) return false;
        String[] searchWords = searchText.toLowerCase().split("\\s+");
        String[] targetWords = textToCheck.toLowerCase().split("\\s+");

        for (String sWord : searchWords) {
            boolean wordMatch = false;
            for (String tWord : targetWords) {
                if (tWord.startsWith(sWord)) {
                    wordMatch = true;
                    break;
                }
            }
            if (!wordMatch) return false;
        }
        return true;
    }

    /**
     * Handles searching materials based on user input.
     * Filters materials by title, author, or ISBN.
     */
    @FXML
    private void handleSearch() {
        String searchText = searchMaterialField.getText().trim().toLowerCase();
        materialList.clear();

        try {
            var results = MaterialDAOMySQLImpl.getInstance().select(null);
            if (results != null) {
                for (Material m : results) {
                    if (!"available".equalsIgnoreCase(m.getMaterial_status()) &&
                            !"hold".equalsIgnoreCase(m.getMaterial_status())) continue;

                    boolean matches = searchText.isEmpty();

                    if (!matches) {
                        if (!matches && matchesWords(m.getTitle(), searchText)) matches = true;
                        if (!matches && matchesWords(m.getAuthor(), searchText)) matches = true;
                        if (!matches && m.getISBN() != null && m.getISBN().toLowerCase().startsWith(searchText)) matches = true;
                    }

                    if (matches) materialList.add(m);
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error retrieving materials: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles the creation of a new loan.
     * Validates user, material availability, handles holds, updates the database, and refreshes the table.
     */
    @FXML
    private void handleAddLoan() {
        String userID = nationalIDField.getText().trim();
        Material selectedMaterial = materialTable.getSelectionModel().getSelectedItem();

        if (userID.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a User National ID.");
            return;
        }
        if (selectedMaterial == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a material to loan.");
            return;
        }

        try {
            // 1 Validate user exists
            User userFilter = new User();
            userFilter.setNationalID(userID);
            var users = UserDAOMySQLImpl.getInstance().select(userFilter);
            if (users.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "User ID not found.");
                return;
            }

            // 2 Validate material exists
            Material materialFilter = new Material();
            materialFilter.setIdMaterial(selectedMaterial.getIdMaterial());
            var materials = MaterialDAOMySQLImpl.getInstance().select(materialFilter);
            if (materials.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Material not found.");
                return;
            }

            Material materialToUpdate = materials.get(0);

            // 3 Handle material on hold
            if ("hold".equalsIgnoreCase(materialToUpdate.getMaterial_status())) {
                Hold holdFilter = new Hold();
                holdFilter.setIdMaterial(materialToUpdate.getIdMaterial());
                var holds = HoldDAOMySQLImpl.getInstance().select(holdFilter);

                // Check if hold belongs to current user
                Hold userHold = null;
                for (Hold h : holds) {
                    if (h.getIdUser() == users.get(0).getIdUser()) {
                        userHold = h;
                        break;
                    }
                }


                if (userHold == null) {
                    showAlert(Alert.AlertType.WARNING, "Hold Notice",
                            "Material \"" + materialToUpdate.getTitle() + "\" is on hold for another user.");
                    return; // Block the loan
                }

                // Remove the hold for the user
                HoldDAOMySQLImpl.getInstance().delete(userHold);

            } else if (!"available".equalsIgnoreCase(materialToUpdate.getMaterial_status())) {
                showAlert(Alert.AlertType.ERROR, "Error", "Selected material is not available.");
                return;
            }

            // 4 Create loan
            Loan newLoan = new Loan();
            newLoan.setIdUser(users.get(0).getIdUser());
            newLoan.setIdMaterial(materialToUpdate.getIdMaterial());
            newLoan.setStart_date(java.time.LocalDateTime.now());
            newLoan.setDue_date(java.time.LocalDateTime.now().plusMonths(1));
            newLoan.setReturn_date(null);
            LoanDAOMySQLImpl.getInstance().insert(newLoan);

            // 5 Update material status
            materialToUpdate.setMaterial_status("loaned");
            MaterialDAOMySQLImpl.getInstance().update(materialToUpdate);

            // 6 Refresh table
            handleSearch();

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Loan created for user " + userID + " with material: " + selectedMaterial.getTitle());

        } catch (DAOException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error processing loan: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Utility method to show an alert dialog.
     * @param type Alert type (ERROR, INFORMATION, WARNING)
     * @param title Dialog title
     * @param content Message to display
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Sets the stage for this dialog.
     * @param dialogStage Stage instance
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}

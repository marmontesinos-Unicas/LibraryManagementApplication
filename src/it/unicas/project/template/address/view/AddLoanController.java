package it.unicas.project.template.address.view;

import it.unicas.project.template.address.model.Loan;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.LoanDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.MaterialDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddLoanController {

    @FXML private TextField nationalIDField;
    @FXML private TextField searchMaterialField;
    @FXML private Button searchButton;
    @FXML private TableView<Material> materialTable;
    @FXML private TableColumn<Material, String> titleColumn;
    @FXML private TableColumn<Material, String> authorColumn;
    @FXML private TableColumn<Material, String> isbnColumn;
    @FXML private Button addLoanButton;

    private Stage dialogStage;
    private final ObservableList<Material> materialList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurar columnas
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        authorColumn.setCellValueFactory(cellData -> cellData.getValue().authorProperty());
        isbnColumn.setCellValueFactory(cellData -> cellData.getValue().ISBNProperty());

        materialTable.setItems(materialList);
        loadAvailableMaterials();

        // -----------------------------
        // Búsqueda en tiempo real
        // -----------------------------
        searchMaterialField.textProperty().addListener((obs, oldText, newText) -> handleSearch());

        // -----------------------------
        // Botón Search -> Clear
        // -----------------------------
        searchButton.setText("Clear");
        searchButton.setOnAction(e -> handleClear());
    }

    private void loadAvailableMaterials() {
        materialList.clear();
        try {
            var results = MaterialDAOMySQLImpl.getInstance().select(null);
            if (results != null) {
                for (Material m : results) {
                    if ("available".equalsIgnoreCase(m.getMaterial_status())) {
                        materialList.add(m);
                    }
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error retrieving materials: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // -----------------------------
    // Método Clear
    // -----------------------------
    @FXML
    private void handleClear() {
        searchMaterialField.clear();
        loadAvailableMaterials();
    }

    // -----------------------------
    // Búsqueda multi-palabra al inicio de cada palabra
    // -----------------------------
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
            if (!wordMatch) return false; // alguna palabra no coincide
        }
        return true; // todas las palabras coinciden
    }

    // -----------------------------
    // Búsqueda
    // -----------------------------
    @FXML
    private void handleSearch() {
        String searchText = searchMaterialField.getText().trim().toLowerCase();
        materialList.clear();

        try {
            var results = MaterialDAOMySQLImpl.getInstance().select(null);
            if (results != null) {
                for (Material m : results) {
                    if (!"available".equalsIgnoreCase(m.getMaterial_status())) continue;

                    boolean matches = searchText.isEmpty();

                    if (!matches) {
                        // Buscar en título
                        if (!matches && matchesWords(m.getTitle(), searchText)) matches = true;
                        // Buscar en autor
                        if (!matches && matchesWords(m.getAuthor(), searchText)) matches = true;
                        // Buscar en ISBN (simple startsWith)
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
            // 1️⃣ Validar usuario
            User userFilter = new User();
            userFilter.setNationalID(userID);
            var users = UserDAOMySQLImpl.getInstance().select(userFilter);
            if (users.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "User ID not found.");
                return;
            }

            // 2️⃣ Validar material
            Material materialFilter = new Material();
            materialFilter.setIdMaterial(selectedMaterial.getIdMaterial());
            var materials = MaterialDAOMySQLImpl.getInstance().select(materialFilter);
            if (materials.isEmpty() || !"available".equals(materials.get(0).getMaterial_status())) {
                showAlert(Alert.AlertType.ERROR, "Error", "Selected material is not available.");
                return;
            }

            Material materialToUpdate = materials.get(0);

            // 3️⃣ Crear préstamo
            Loan newLoan = new Loan();
            newLoan.setIdUser(users.get(0).getIdUser());
            newLoan.setIdMaterial(materialToUpdate.getIdMaterial());
            newLoan.setStart_date(java.time.LocalDateTime.now());
            newLoan.setDue_date(java.time.LocalDateTime.now().plusMonths(1));
            newLoan.setReturn_date(null);
            LoanDAOMySQLImpl.getInstance().insert(newLoan);

            // 4️⃣ Actualizar status del material
            materialToUpdate.setMaterial_status("loaned");
            MaterialDAOMySQLImpl.getInstance().update(materialToUpdate);

            // 5️⃣ Refrescar tabla
            handleSearch();

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Loan created for user " + userID + " with material: " + selectedMaterial.getTitle());

        } catch (DAOException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error processing loan: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}

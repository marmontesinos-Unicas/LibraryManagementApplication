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

        // Cambiar color de título si está en hold
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

        // Búsqueda en tiempo real
        searchMaterialField.textProperty().addListener((obs, oldText, newText) -> handleSearch());

        // Botón Search -> Clear
        searchButton.setText("Clear");
        searchButton.setOnAction(e -> handleClear());
    }

    private void loadAvailableMaterials() {
        materialList.clear();
        try {
            var results = MaterialDAOMySQLImpl.getInstance().select(null);
            if (results != null) {
                for (Material m : results) {
                    // Solo disponibles o en hold
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

    @FXML
    private void handleClear() {
        searchMaterialField.clear();
        loadAvailableMaterials();
    }

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
            if (materials.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Material not found.");
                return;
            }

            Material materialToUpdate = materials.get(0);

            // 2️⃣b Si está en hold
            if ("hold".equalsIgnoreCase(materialToUpdate.getMaterial_status())) {
                Hold holdFilter = new Hold();
                holdFilter.setIdMaterial(materialToUpdate.getIdMaterial());
                var holds = HoldDAOMySQLImpl.getInstance().select(holdFilter);

                // Buscar hold del usuario actual
                Hold userHold = null;
                for (Hold h : holds) {
                    if (h.getIdUser().equals(users.get(0).getIdUser())) {
                        userHold = h;
                        break;
                    }
                }

                if (userHold == null) {
                    showAlert(Alert.AlertType.WARNING, "Hold Notice",
                            "Material \"" + materialToUpdate.getTitle() + "\" is on hold for another user.");
                    return; // Bloquear préstamo
                }

                // Eliminar hold del usuario actual
                HoldDAOMySQLImpl.getInstance().delete(userHold);

            } else if (!"available".equalsIgnoreCase(materialToUpdate.getMaterial_status())) {
                showAlert(Alert.AlertType.ERROR, "Error", "Selected material is not available.");
                return;
            }

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

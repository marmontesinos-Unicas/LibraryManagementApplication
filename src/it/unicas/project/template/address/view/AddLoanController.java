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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class AddLoanController {

    @FXML
    private TextField nationalIDField;

    @FXML
    private TextField searchMaterialField;

    @FXML
    private Button searchButton;

    @FXML
    private TableView<Material> materialTable;

    @FXML
    private TableColumn<Material, String> titleColumn;

    @FXML
    private TableColumn<Material, String> authorColumn;

    @FXML
    private TableColumn<Material, String> isbnColumn;

    @FXML
    private Button addLoanButton;

    private Stage dialogStage;

    private final ObservableList<Material> materialList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurar columnas
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        authorColumn.setCellValueFactory(cellData -> cellData.getValue().authorProperty());
        isbnColumn.setCellValueFactory(cellData -> cellData.getValue().ISBNProperty());

        // Asignar lista al TableView
        materialTable.setItems(materialList);

        // Cargar todos los materiales disponibles de la base de datos
        loadAvailableMaterials();
    }

    private void loadAvailableMaterials() {
        materialList.clear();
        try {
            // Traer todos los materiales de la base de datos
            var results = MaterialDAOMySQLImpl.getInstance().select(null);

            if (results != null) {
                for (Material m : results) {
                    // Solo agregar los materiales que estén disponibles
                    if (m.getMaterial_status() != null && m.getMaterial_status().equalsIgnoreCase("available")) {
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
    private void handleSearch() {
        String searchText = searchMaterialField.getText().trim().toLowerCase();

        materialList.clear();

        try {
            // Traer todos los materiales de la base de datos
            var results = MaterialDAOMySQLImpl.getInstance().select(null);

            if (results != null) {
                for (Material m : results) {
                    // Solo considerar materiales disponibles
                    if (m.getMaterial_status() == null || !m.getMaterial_status().equalsIgnoreCase("available")) {
                        continue;
                    }

                    boolean matches = searchText.isEmpty(); // Si el search está vacío, mostramos todos disponibles

                    // Búsqueda parcial por título, autor o ISBN
                    if (!matches) {
                        if (m.getTitle() != null && m.getTitle().toLowerCase().contains(searchText)) {
                            matches = true;
                        } else if (m.getAuthor() != null && m.getAuthor().toLowerCase().contains(searchText)) {
                            matches = true;
                        } else if (m.getISBN() != null && m.getISBN().toLowerCase().contains(searchText)) {
                            matches = true;
                        }
                    }

                    if (matches) {
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
            // 1️⃣ Validar que el usuario existe
            User userFilter = new User();
            userFilter.setNationalID(userID); // suponiendo que tu User tiene un campo nationalID como String
            var users = UserDAOMySQLImpl.getInstance().select(userFilter);
            if (users.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "User ID not found.");
                return;
            }

            // 2️⃣ Validar que el material está disponible
            Material materialFilter = new Material();
            materialFilter.setIdMaterial(selectedMaterial.getIdMaterial());
            var materials = MaterialDAOMySQLImpl.getInstance().select(materialFilter);
            if (materials.isEmpty() || !"available".equals(materials.get(0).getMaterial_status())) {
                showAlert(Alert.AlertType.ERROR, "Error", "Selected material is not available.");
                return;
            }

            Material materialToUpdate = materials.get(0);

            // 3️⃣ Crear nuevo préstamo
            Loan newLoan = new Loan();
            newLoan.setIdUser(users.get(0).getIdUser()); // tomamos el ID real del usuario
            newLoan.setIdMaterial(materialToUpdate.getIdMaterial());
            newLoan.setStart_date(java.time.LocalDateTime.now());
            newLoan.setDue_date(java.time.LocalDateTime.now().plusMonths(1)); // vencimiento 1 mes después
            newLoan.setReturn_date(null);

            LoanDAOMySQLImpl.getInstance().insert(newLoan);

            // 4️⃣ Actualizar status del material
            materialToUpdate.setMaterial_status("loaned");
            MaterialDAOMySQLImpl.getInstance().update(materialToUpdate);

            // 5️⃣ Refrescar la tabla de materiales
            handleSearch(); // recarga los materiales disponibles en la tabla

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

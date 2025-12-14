package it.unicas.project.template.address.view;

import it.unicas.project.template.address.model.*;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.time.LocalDateTime;


public class ModifyLoanController {

    @FXML private TextField nationalIDField;
    @FXML private TextField searchMaterialField;
    @FXML private Button searchButton;
    @FXML private TableView<Material> materialTable;
    @FXML private TableColumn<Material, String> titleColumn;
    @FXML private TableColumn<Material, String> authorColumn;
    @FXML private TableColumn<Material, String> isbnColumn;
    @FXML private Button modifyLoanButton;

    private Stage dialogStage;
    private Loan loanToModify;
    private Material originalMaterial;

    private final ObservableList<Material> materialList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(c -> c.getValue().titleProperty());
        authorColumn.setCellValueFactory(c -> c.getValue().authorProperty());
        isbnColumn.setCellValueFactory(c -> c.getValue().ISBNProperty());

        titleColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTextFill(Color.BLACK);
                } else {
                    Material m = getTableView().getItems().get(getIndex());
                    setText(item);
                    setTextFill("holded".equalsIgnoreCase(m.getMaterial_status()) ? Color.ORANGE : Color.BLACK);
                }
            }
        });

        materialTable.setItems(materialList);
        searchMaterialField.textProperty().addListener((o, oldV, newV) -> handleSearch());
        searchButton.setOnAction(e -> handleClear());
    }

    /* ===================== INITIAL DATA ===================== */
    public void setLoan(Loan loan) {
        this.loanToModify = loan;

        try {
            // Cargar usuario
            User u = new User();
            u.setIdUser(loan.getIdUser());
            u = UserDAOMySQLImpl.getInstance().select(u).get(0);
            nationalIDField.setText(u.getNationalID());

            // Cargar material original
            originalMaterial = new Material();
            originalMaterial.setIdMaterial(loan.getIdMaterial());
            originalMaterial = MaterialDAOMySQLImpl.getInstance().select(originalMaterial).get(0);

            // Cargar materiales en la tabla
            loadMaterials();

            // Seleccionar el material original por ID
            selectOriginalMaterial();

        } catch (DAOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading loan data");
        }
    }

    private void selectOriginalMaterial() {
        if (originalMaterial == null) return;

        for (Material m : materialList) {
            if (m.getIdMaterial().equals(originalMaterial.getIdMaterial())) {
                materialTable.getSelectionModel().select(m);
                materialTable.scrollTo(m); // opcional: hacer scroll para verlo
                break;
            }
        }
    }

    private void loadMaterials() {
        materialList.clear();
        try {
            for (Material m : MaterialDAOMySQLImpl.getInstance().select(null)) {
                // Añadir solo available y holded, y el material original aunque esté loaned
                if ("available".equalsIgnoreCase(m.getMaterial_status())
                        || "holded".equalsIgnoreCase(m.getMaterial_status())
                        || (originalMaterial != null && m.getIdMaterial().equals(originalMaterial.getIdMaterial()))) {
                    materialList.add(m);
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        searchMaterialField.clear();
        loadMaterials();
        selectOriginalMaterial();
    }

    @FXML
    private void handleSearch() {
        String text = searchMaterialField.getText().toLowerCase();
        materialList.clear();

        try {
            for (Material m : MaterialDAOMySQLImpl.getInstance().select(null)) {
                if (!"available".equalsIgnoreCase(m.getMaterial_status())
                        && !"holded".equalsIgnoreCase(m.getMaterial_status())
                        && (originalMaterial == null || !m.getIdMaterial().equals(originalMaterial.getIdMaterial())))
                    continue;

                if (text.isEmpty()
                        || m.getTitle().toLowerCase().contains(text)
                        || m.getAuthor().toLowerCase().contains(text)
                        || (m.getISBN() != null && m.getISBN().toLowerCase().startsWith(text))) {
                    materialList.add(m);
                }
            }
            selectOriginalMaterial(); // mantener la selección al buscar
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
        }
    }

    /* ===================== MODIFY ===================== */
    @FXML
    private void handleModifyLoan() {

        Material selectedMaterial = materialTable.getSelectionModel().getSelectedItem();

        if (selectedMaterial == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Select a material");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to modify this loan?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            // Guardamos fechas originales
            LocalDateTime originalStartDate = loanToModify.getStart_date();
            LocalDateTime originalDueDate = loanToModify.getDue_date();

            // ========================= USUARIO =========================
            String newNationalID = nationalIDField.getText().trim();
            User newUser = null;
            if (!newNationalID.isEmpty()) {
                User u = new User();
                u.setNationalID(newNationalID);
                var users = UserDAOMySQLImpl.getInstance().select(u);
                if (users.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "User not found");
                    return;
                }
                newUser = users.get(0);
            }

            // ========================= MATERIAL =========================
            if (!selectedMaterial.getIdMaterial().equals(loanToModify.getIdMaterial())) {

                // 1. Verificar si el nuevo material está en hold
                if ("holded".equalsIgnoreCase(selectedMaterial.getMaterial_status())) {
                    Hold holdFilter = new Hold();
                    holdFilter.setIdMaterial(selectedMaterial.getIdMaterial());
                    var holds = HoldDAOMySQLImpl.getInstance().select(holdFilter);

                    // Obtener el ID del usuario (nuevo o actual)
                    Integer userId = (newUser != null) ? newUser.getIdUser() : loanToModify.getIdUser();

                    // Verificar si el hold pertenece al usuario
                    Hold userHold = null;
                    for (Hold h : holds) {
                        if (h.getIdUser() == userId) {
                            userHold = h;
                            break;
                        }
                    }

                    if (userHold == null) {
                        // El hold es de otro usuario, no permitir el cambio
                        showAlert(Alert.AlertType.WARNING, "Hold Notice",
                                "Material \"" + selectedMaterial.getTitle() + "\" is on hold for another user.");
                        return;
                    }

                    // Eliminar el hold del usuario actual
                    HoldDAOMySQLImpl.getInstance().delete(userHold);
                } else if (!"available".equalsIgnoreCase(selectedMaterial.getMaterial_status())) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Selected material is not available.");
                    return;
                }

                // 2. Liberar material antiguo
                Material oldMaterial = new Material();
                oldMaterial.setIdMaterial(loanToModify.getIdMaterial());
                oldMaterial = MaterialDAOMySQLImpl.getInstance().select(oldMaterial).get(0);
                if ("loaned".equalsIgnoreCase(oldMaterial.getMaterial_status())) {
                    oldMaterial.setMaterial_status("available");
                    MaterialDAOMySQLImpl.getInstance().update(oldMaterial);
                }

                // 3. Marcar nuevo material como loaned
                selectedMaterial.setMaterial_status("loaned");
                MaterialDAOMySQLImpl.getInstance().update(selectedMaterial);

                loanToModify.setIdMaterial(selectedMaterial.getIdMaterial());
            }

            // ========================= ACTUALIZAR USUARIO =========================
            if (newUser != null) {
                loanToModify.setIdUser(newUser.getIdUser());
            }

            // ========================= MANTENER FECHAS =========================
            loanToModify.setStart_date(originalStartDate);
            loanToModify.setDue_date(originalDueDate);

            // Actualizar préstamo
            LoanDAOMySQLImpl.getInstance().update(loanToModify);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Loan modified successfully");
            dialogStage.close();

        } catch (DAOException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", e.getMessage());
            e.printStackTrace();
        }
    }


    /* ===================== UTILS ===================== */
    private void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}

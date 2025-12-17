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


/**
 * Controller class for the loan modification view.
 * Handles the logic for searching, selecting, and modifying an existing loan.
 */
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

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     * Sets up table column cell value factories and custom cell factories for status indication.
     */
    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(c -> c.getValue().titleProperty());
        authorColumn.setCellValueFactory(c -> c.getValue().authorProperty());
        isbnColumn.setCellValueFactory(c -> c.getValue().ISBNProperty());

        // Custom cell factory to color 'holded' materials orange
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
                    // Set color based on material status (Orange for 'holded')
                    setTextFill("holded".equalsIgnoreCase(m.getMaterial_status()) ? Color.ORANGE : Color.BLACK);
                }
            }
        });

        materialTable.setItems(materialList);
        // Add a listener to automatically search when the text field changes
        searchMaterialField.textProperty().addListener((o, oldV, newV) -> handleSearch());
        // Set the action for the search/clear button
        searchButton.setOnAction(e -> handleClear());
    }

    /* ===================== INITIAL DATA ===================== */
    /**
     * Sets the loan to be modified in the controller.
     * Loads the associated user and material data, populates the material table,
     * and selects the original material.
     *
     * @param loan The Loan object to be modified.
     */
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

    /**
     * Selects and scrolls to the original material associated with the loan
     * in the material table.
     */
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

    /**
     * Loads the materials from the database into the {@code materialList}.
     * Only loads materials that are 'available', 'holded', or the {@code originalMaterial}.
     */
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

    /**
     * Handles the clear button action. Clears the search field,
     * reloads all relevant materials, and re-selects the original material.
     */
    @FXML
    private void handleClear() {
        searchMaterialField.clear();
        loadMaterials();
        selectOriginalMaterial();
    }

    /**
     * Handles the material search functionality. Filters the materials in the
     * table based on the text in the search field (matching title, author, or ISBN prefix).
     * The list includes 'available', 'holded' materials, and the original material.
     */
    @FXML
    private void handleSearch() {
        String text = searchMaterialField.getText().toLowerCase();
        materialList.clear();

        try {
            for (Material m : MaterialDAOMySQLImpl.getInstance().select(null)) {
                // Skip materials that are not available, holded, or the original material
                if (!"available".equalsIgnoreCase(m.getMaterial_status())
                        && !"holded".equalsIgnoreCase(m.getMaterial_status())
                        && (originalMaterial == null || !m.getIdMaterial().equals(originalMaterial.getIdMaterial())))
                    continue;

                // Apply search filter (empty text means show all relevant)
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
    /**
     * Handles the action of modifying the loan.
     * Validates the selected material and new user ID, updates the loan and material statuses
     * in the database, and closes the dialog on success.
     */
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

            // ========================= USER =========================
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

            // ========================= UPDATE USUARIO =========================
            if (newUser != null) {
                loanToModify.setIdUser(newUser.getIdUser());
            }

            // ========================= KEEP DATES =========================
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
    /**
     * Shows a standard alert dialog with the specified type, title, and message.
     *
     * @param t The Alert type (e.g., ERROR, INFORMATION).
     * @param title The title of the alert box.
     * @param msg The message content of the alert.
     */
    private void showAlert(Alert.AlertType t, String title, String msg) {
        Alert a = new Alert(t);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage The Stage object for this view.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}
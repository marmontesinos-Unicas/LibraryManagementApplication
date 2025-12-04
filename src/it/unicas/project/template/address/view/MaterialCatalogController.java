package it.unicas.project.template.address.view;

import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.MaterialType;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.MaterialDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.MaterialTypeDAOMySQLImpl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MaterialCatalogController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> materialTypeCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextField yearFromField;
    @FXML private TextField yearToField;

    @FXML private TableView<Material> materialTable;
    @FXML private TableColumn<Material, String> titleColumn;
    @FXML private TableColumn<Material, String> authorColumn;
    @FXML private TableColumn<Material, Integer> yearColumn;
    @FXML private TableColumn<Material, String> isbnColumn;
    @FXML private TableColumn<Material, Integer> typeColumn;
    @FXML private TableColumn<Material, String> statusColumn;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button searchButton;
    @FXML private Button clearButton;
    @FXML private Label resultCountLabel;
    @FXML private HBox adminActionsBox;

    private DAO<Material> materialDAO;
    private MaterialTypeDAOMySQLImpl materialTypeDAO;
    private ObservableList<Material> materialList;
    private ObservableList<Material> filteredList;
    private Map<Integer, String> materialTypeMap; // Cache for type ID -> name mapping
    private boolean isAdminMode = true; // Set this based on user role

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Initialize DAOs
        materialDAO = MaterialDAOMySQLImpl.getInstance();
        materialTypeDAO = MaterialTypeDAOMySQLImpl.getInstance();
        materialList = FXCollections.observableArrayList();
        filteredList = FXCollections.observableArrayList();
        materialTypeMap = new HashMap<>();

        // Load material types first
        loadMaterialTypes();

        // Setup table columns
        setupTableColumns();

        // Setup filter combos
        setupFilters();

        // Load initial data
        loadAllMaterials();

        // Setup admin mode
        setAdminMode(isAdminMode);

        // Add listeners for real-time search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> handleFilter());
        yearFromField.textProperty().addListener((obs, oldVal, newVal) -> handleFilter());
        yearToField.textProperty().addListener((obs, oldVal, newVal) -> handleFilter());
    }

    /**
     * Load material types from database and populate cache
     */
    private void loadMaterialTypes() {
        List<MaterialType> types = materialTypeDAO.selectAll();
        materialTypeMap.clear();

        for (MaterialType type : types) {
            materialTypeMap.put(type.getIdMaterialType(), type.getMaterial_type());
        }
    }

    /**
     * Setup table columns with property bindings
     */
    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("ISBN"));

        // Custom cell factory for type column to show name instead of ID
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("idMaterialType"));
        typeColumn.setCellFactory(column -> new TableCell<Material, Integer>() {
            @Override
            protected void updateItem(Integer typeId, boolean empty) {
                super.updateItem(typeId, empty);
                if (empty || typeId == null) {
                    setText(null);
                } else {
                    setText(getMaterialTypeName(typeId));
                }
            }
        });

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("material_status"));

        // Custom cell factory for status column to add styling
        statusColumn.setCellFactory(column -> new TableCell<Material, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    // Add color coding for different statuses
                    if (status.equalsIgnoreCase("Available")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (status.equalsIgnoreCase("On Loan")) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red;");
                    }
                }
            }
        });

        materialTable.setItems(filteredList);
    }

    /**
     * Setup filter combo boxes
     */
    private void setupFilters() {
        // Material Type options - dynamically from database
        materialTypeCombo.getItems().add("All Types");
        for (String typeName : materialTypeMap.values()) {
            materialTypeCombo.getItems().add(typeName);
        }
        materialTypeCombo.setValue("All Types");

        // Status options
        statusCombo.getItems().addAll(
                "All Status",
                "Available",
                "On Loan",
                "Reserved",
                "Lost",
                "Damaged"
        );
        statusCombo.setValue("All Status");
    }

    /**
     * Load all materials from database
     */
    private void loadAllMaterials() {
        try {
            List<Material> materials = materialDAO.select(null);
            materialList.clear();
            materialList.addAll(materials);
            filteredList.clear();
            filteredList.addAll(materials);
            updateResultCount();
        } catch (DAOException e) {
            showError("Error loading materials", e.getMessage());
        }
    }

    /**
     * Handle search button click
     */
    @FXML
    private void handleSearch() {
        handleFilter();
    }

    /**
     * Handle clear button click
     */
    @FXML
    private void handleClear() {
        searchField.clear();
        materialTypeCombo.setValue("All Types");
        statusCombo.setValue("All Status");
        yearFromField.clear();
        yearToField.clear();
        filteredList.clear();
        filteredList.addAll(materialList);
        updateResultCount();
    }

    /**
     * Handle filtering based on all criteria
     */
    @FXML
    private void handleFilter() {
        String searchTerm = searchField.getText().toLowerCase().trim();
        String selectedType = materialTypeCombo.getValue();
        String selectedStatus = statusCombo.getValue();
        String yearFrom = yearFromField.getText().trim();
        String yearTo = yearToField.getText().trim();

        filteredList.clear();

        List<Material> filtered = materialList.stream()
                .filter(material -> {
                    // Search term filter (title, author, ISBN)
                    boolean matchesSearch = searchTerm.isEmpty() ||
                            material.getTitle().toLowerCase().contains(searchTerm) ||
                            (material.getAuthor() != null && material.getAuthor().toLowerCase().contains(searchTerm)) ||
                            (material.getISBN() != null && material.getISBN().toLowerCase().contains(searchTerm));

                    // Material type filter
                    boolean matchesType = selectedType.equals("All Types") ||
                            getMaterialTypeName(material.getIdMaterialType()).equals(selectedType);

                    // Status filter
                    boolean matchesStatus = selectedStatus.equals("All Status") ||
                            material.getMaterial_status().equalsIgnoreCase(selectedStatus);

                    // Year range filter
                    boolean matchesYear = true;
                    if (!yearFrom.isEmpty() || !yearTo.isEmpty()) {
                        try {
                            int materialYear = material.getYear();
                            if (!yearFrom.isEmpty()) {
                                int fromYear = Integer.parseInt(yearFrom);
                                matchesYear = materialYear >= fromYear;
                            }
                            if (!yearTo.isEmpty() && matchesYear) {
                                int toYear = Integer.parseInt(yearTo);
                                matchesYear = materialYear <= toYear;
                            }
                        } catch (NumberFormatException e) {
                            matchesYear = true; // Ignore invalid year input
                        }
                    }

                    return matchesSearch && matchesType && matchesStatus && matchesYear;
                })
                .collect(Collectors.toList());

        filteredList.addAll(filtered);
        updateResultCount();
    }

    /**
     * Handle table row click to enable/disable edit and delete buttons
     */
    @FXML
    private void handleTableClick(MouseEvent event) {
        Material selected = materialTable.getSelectionModel().getSelectedItem();
        if (isAdminMode) {
            editButton.setDisable(selected == null);
            deleteButton.setDisable(selected == null);
        }

        // Double click to view/edit
        if (event.getClickCount() == 2 && selected != null) {
            if (isAdminMode) {
                handleEdit();
            } else {
                handleView();
            }
        }
    }

    /**
     * Handle add new material - opens AddMaterial dialog
     */
    @FXML
    private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unicas/project/template/address/view/AddMaterial.fxml"));
            Parent root = loader.load();

            // Create new stage for dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Material");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            // Show dialog and wait for it to close
            dialogStage.showAndWait();

            // Refresh the table after dialog closes
            refresh();

        } catch (IOException e) {
            showError("Error", "Could not load Add Material dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle edit selected material - opens EditMaterial dialog
     */
    @FXML
    private void handleEdit() {
        Material selected = materialTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a material to edit");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unicas/project/template/address/view/EditMaterial.fxml"));
            Parent root = loader.load();

            // Get controller and set the material to edit
            EditMaterialController controller = loader.getController();
            controller.setMaterial(selected);

            // Create new stage for dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Material");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(editButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            // Show dialog and wait for it to close
            dialogStage.showAndWait();

            // Refresh the table after dialog closes
            refresh();

        } catch (IOException e) {
            showError("Error", "Could not load Edit Material dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle delete selected material
     */
    @FXML
    private void handleDelete() {
        Material selected = materialTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a material to delete");
            return;
        }

        // Confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Material");
        confirmation.setContentText("Are you sure you want to delete:\n" +
                selected.getTitle() + "?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                materialDAO.delete(selected);
                materialList.remove(selected);
                filteredList.remove(selected);
                updateResultCount();
                showInfo("Success", "Material deleted successfully");
            } catch (DAOException e) {
                showError("Delete Error", "Failed to delete material: " + e.getMessage());
            }
        }
    }

    /**
     * Handle view material details (for non-admin users)
     */
    private void handleView() {
        Material selected = materialTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        // Create a simple view dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Material Details");
        alert.setHeaderText(selected.getTitle());

        StringBuilder content = new StringBuilder();
        content.append("Author: ").append(selected.getAuthor() != null ? selected.getAuthor() : "N/A").append("\n");
        content.append("Year: ").append(selected.getYear()).append("\n");
        content.append("ISBN: ").append(selected.getISBN() != null ? selected.getISBN() : "N/A").append("\n");
        content.append("Type: ").append(getMaterialTypeName(selected.getIdMaterialType())).append("\n");
        content.append("Status: ").append(selected.getMaterial_status());

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    /**
     * Set admin mode (shows/hides admin action buttons)
     */
    public void setAdminMode(boolean isAdmin) {
        this.isAdminMode = isAdmin;
        adminActionsBox.setVisible(isAdmin);
        adminActionsBox.setManaged(isAdmin);
    }

    /**
     * Update result count label
     */
    private void updateResultCount() {
        resultCountLabel.setText(String.format("Total: %d materials", filteredList.size()));
    }

    /**
     * Get material type name from ID using cached map
     */
    private String getMaterialTypeName(Integer typeId) {
        if (typeId == null) return "Unknown";
        return materialTypeMap.getOrDefault(typeId, "Unknown");
    }

    /**
     * Show error alert
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Show warning alert
     */
    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Show info alert
     */
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Refresh the table data
     */
    public void refresh() {
        loadAllMaterials();
        handleFilter();
    }
}

package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Genre;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.MaterialGenre;
import it.unicas.project.template.address.model.MaterialType;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.GenreDAO;
import it.unicas.project.template.address.model.dao.mysql.GenreDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.MaterialDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.MaterialGenreDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.MaterialTypeDAOMySQLImpl;

import it.unicas.project.template.address.service.MaterialCatalogService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Controller for Material Catalog - ADMIN VIEW ONLY
 * Allows full CRUD operations on materials
 */
public class AdminMaterialCatalogController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> materialTypeFilterButton;
    @FXML private ComboBox<String> statusFilterButton;
    @FXML private ComboBox<String> genreFilterButton;
    @FXML private TextField yearFromField;
    @FXML private TextField yearToField;

    @FXML private TableView<Material> materialTable;
    @FXML private TableColumn<Material, String> titleColumn;
    @FXML private TableColumn<Material, String> authorColumn;
    @FXML private TableColumn<Material, Integer> yearColumn;
    @FXML private TableColumn<Material, String> isbnColumn;
    @FXML private TableColumn<Material, Integer> typeColumn;
    @FXML private TableColumn<Material, Integer> genreColumn;
    @FXML private TableColumn<Material, String> statusColumn;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Label resultCountLabel;

    private MainApp mainApp;

    private DAO<Material> materialDAO;
    private MaterialTypeDAOMySQLImpl materialTypeDAO;
    private GenreDAO genreDAO;
    private DAO<MaterialGenre> materialGenreDAO;

    private ObservableList<Material> materialList;
    private ObservableList<Material> filteredList;
    private Map<Integer, String> materialTypeMap;
    private Map<Integer, String> genreMap;
    private Map<Integer, Set<Integer>> materialGenreMap;

    // Filter selections
    private Set<String> selectedMaterialTypes = new HashSet<>();
    private Set<String> selectedStatuses = new HashSet<>();
    private Set<String> selectedGenres = new HashSet<>();

    // Popups for filters
    private Popup materialTypePopup;
    private Popup statusPopup;
    private Popup genrePopup;

    private final MaterialCatalogService catalogService = new MaterialCatalogService();

    // Debounce mechanism for search
    private ScheduledExecutorService searchScheduler = Executors.newSingleThreadScheduledExecutor();
    private java.util.concurrent.Future<?> filterTask;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        materialDAO = MaterialDAOMySQLImpl.getInstance();
        materialTypeDAO = MaterialTypeDAOMySQLImpl.getInstance();
        genreDAO = GenreDAOMySQLImpl.getInstance();
        materialGenreDAO = MaterialGenreDAOMySQLImpl.getInstance();

        materialList = FXCollections.observableArrayList();
        filteredList = FXCollections.observableArrayList();
        materialTypeMap = new HashMap<>();
        genreMap = new HashMap<>();
        materialGenreMap = new HashMap<>();

        loadMaterialTypes();
        loadGenres();
        loadMaterialGenreRelationships();
        setupTableColumns();
        loadAllMaterials();

        // Initialize filter buttons
        setupFilterButtons();

        // FIXED: Add debounced listeners for real-time search (300ms delay)
        searchField.textProperty().addListener((obs, oldVal, newVal) -> scheduleFilter());
        yearFromField.textProperty().addListener((obs, oldVal, newVal) -> scheduleFilter());
        yearToField.textProperty().addListener((obs, oldVal, newVal) -> scheduleFilter());
    }

    /**
     * Schedule filter execution after 300ms delay
     */
    private void scheduleFilter() {
        // Cancel previous filter task if still pending
        if (filterTask != null && !filterTask.isDone()) {
            filterTask.cancel(false);
        }

        // Schedule new filter after 300ms delay
        filterTask = searchScheduler.schedule(() -> {
            Platform.runLater(this::handleFilter);
        }, 300, TimeUnit.MILLISECONDS);
    }

    /**
     * Setup filter buttons
     */
    private void setupFilterButtons() {
        if (materialTypeFilterButton != null) {
            materialTypeFilterButton.setValue("All Types");
            materialTypeFilterButton.setOnMouseClicked(e -> toggleMaterialTypeFilter());
        }

        if (statusFilterButton != null) {
            statusFilterButton.setValue("All Statuses");
            statusFilterButton.setOnMouseClicked(e -> toggleStatusFilter());
        }

        if (genreFilterButton != null) {
            genreFilterButton.setValue("All Genres");
            genreFilterButton.setOnMouseClicked(e -> toggleGenreFilter());
        }
    }

    /**
     * Toggle Material Type filter popup
     */
    private void toggleMaterialTypeFilter() {
        if (materialTypePopup != null && materialTypePopup.isShowing()) {
            materialTypePopup.hide();
            return;
        }

        Set<String> allTypes = materialList.stream()
                .map(m -> getMaterialTypeName(m.getIdMaterialType()))
                .collect(Collectors.toCollection(TreeSet::new));

        materialTypePopup = createFilterPopup(materialTypeFilterButton, "Material Types",
                allTypes, selectedMaterialTypes);
    }

    /**
     * Toggle Status filter popup
     */
    private void toggleStatusFilter() {
        if (statusPopup != null && statusPopup.isShowing()) {
            statusPopup.hide();
            return;
        }

        Set<String> allStatuses = materialList.stream()
                .map(Material::getMaterial_status)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));

        statusPopup = createFilterPopup(statusFilterButton, "Statuses",
                allStatuses, selectedStatuses);
    }

    /**
     * Toggle Genre filter popup
     */
    private void toggleGenreFilter() {
        if (genrePopup != null && genrePopup.isShowing()) {
            genrePopup.hide();
            return;
        }

        Set<String> allGenres = materialGenreMap.values().stream()
                .flatMap(Set::stream)
                .map(this::getGenreName)
                .collect(Collectors.toCollection(TreeSet::new));

        genrePopup = createFilterPopup(genreFilterButton, "Genres",
                allGenres, selectedGenres);
    }

    /**
     * Create filter popup with checkboxes
     */
    private Popup createFilterPopup(ComboBox<String> sourceButton, String label,
                                    Set<String> allOptions, Set<String> selectedOptions) {
        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox container = new VBox(5);
        container.setPadding(new Insets(5));
        container.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #cccccc; " +
                        "-fx-border-width: 1; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"
        );
        container.setPrefWidth(sourceButton.getWidth());

        // Select All checkbox
        CheckBox selectAllCheckBox = new CheckBox("Select All");
        selectAllCheckBox.setSelected(selectedOptions.size() == allOptions.size());
        selectAllCheckBox.setStyle(
                "-fx-font-weight: bold; " +
                        "-fx-padding: 5; " +
                        "-fx-mark-color: black; " +
                        "-fx-mark-size: 8px;"
        );

        Separator separator = new Separator();

        // Checkboxes container with scrolling
        VBox checkboxContainer = new VBox(3);
        checkboxContainer.setPadding(new Insets(5));

        Map<String, CheckBox> checkBoxMap = new HashMap<>();

        for (String option : allOptions) {
            CheckBox cb = new CheckBox(option);
            cb.setSelected(selectedOptions.contains(option));
            cb.setStyle(
                    "-fx-padding: 3; " +
                            "-fx-mark-color: black; " +
                            "-fx-mark-size: 8px;"
            );

            cb.setOnAction(e -> {
                if (cb.isSelected()) {
                    selectedOptions.add(option);
                } else {
                    selectedOptions.remove(option);
                }
                selectAllCheckBox.setSelected(selectedOptions.size() == allOptions.size());
                updateFilterButtonText(sourceButton, selectedOptions.size(), allOptions.size(), label);
                // Use scheduled filter instead of immediate
                scheduleFilter();
            });

            checkBoxMap.put(option, cb);
            checkboxContainer.getChildren().add(cb);
        }

        // Select All logic
        selectAllCheckBox.setOnAction(e -> {
            boolean isSelected = selectAllCheckBox.isSelected();
            if (isSelected) {
                selectedOptions.clear();
                selectedOptions.addAll(allOptions);
                checkBoxMap.values().forEach(cb -> cb.setSelected(true));
            } else {
                selectedOptions.clear();
                checkBoxMap.values().forEach(cb -> cb.setSelected(false));
            }
            updateFilterButtonText(sourceButton, selectedOptions.size(), allOptions.size(), label);
            // Use scheduled filter instead of immediate
            scheduleFilter();
        });

        // ScrollPane for checkboxes
        ScrollPane scrollPane = new ScrollPane(checkboxContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(250);
        scrollPane.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: transparent;"
        );
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        container.getChildren().addAll(selectAllCheckBox, separator, scrollPane);

        popup.getContent().add(container);

        // Position popup below button
        Bounds bounds = sourceButton.localToScreen(sourceButton.getBoundsInLocal());
        popup.show(sourceButton, bounds.getMinX(), bounds.getMaxY());

        return popup;
    }

    /**
     * Update filter button text to indicate active filters
     */
    private void updateFilterButtonText(ComboBox<String> button, int selected, int total, String label) {
        if (selected == 0) {
            button.setValue("No " + label);
            button.setStyle("-fx-text-fill: red;");
        } else if (selected == total) {
            button.setValue("All " + label);
            button.setStyle("");
        } else {
            button.setValue(selected + "/" + total + " " + label);
            button.setStyle("-fx-text-fill: blue;");
        }
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
     * Load genres from database and populate cache
     */
    private void loadGenres() {
        List<Genre> genres = genreDAO.selectAll();
        genreMap.clear();

        for (Genre genre : genres) {
            genreMap.put(genre.getIdGenre(), genre.getGenre());
        }
    }

    /**
     * Load material-genre relationships
     */
    private void loadMaterialGenreRelationships() {
        try {
            List<MaterialGenre> relationships = materialGenreDAO.select(null);
            materialGenreMap.clear();

            for (MaterialGenre mg : relationships) {
                materialGenreMap
                        .computeIfAbsent(mg.getIdMaterial(), k -> new HashSet<>())
                        .add(mg.getIdGenre());
            }
        } catch (DAOException e) {
            showError("Error loading genre relationships", e.getMessage());
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

        // Genre column
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("idMaterial"));
        genreColumn.setCellFactory(column -> new TableCell<Material, Integer>() {
            @Override
            protected void updateItem(Integer materialId, boolean empty) {
                super.updateItem(materialId, empty);
                if (empty || materialId == null) {
                    setText(null);
                } else {
                    Set<Integer> genreIds = materialGenreMap.get(materialId);
                    if (genreIds != null && !genreIds.isEmpty()) {
                        String genres = genreIds.stream()
                                .map(id -> getGenreName(id))
                                .sorted()
                                .collect(Collectors.joining(", "));
                        setText(genres);
                    } else {
                        setText("—");
                    }
                }
            }
        });

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("material_status"));
        statusColumn.setCellFactory(column -> new TableCell<Material, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if (status.equalsIgnoreCase("available")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (status.equalsIgnoreCase("loaned")) {
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
     * Load all materials from database
     */
    private void loadAllMaterials() {
        try {
            List<Material> materials = materialDAO.select(null);
            materialList.clear();
            materialList.addAll(materials);

            // Initialize filters with all options selected
            selectedMaterialTypes.clear();
            selectedMaterialTypes.addAll(materialList.stream()
                    .map(m -> getMaterialTypeName(m.getIdMaterialType()))
                    .collect(Collectors.toSet()));

            selectedStatuses.clear();
            selectedStatuses.addAll(materialList.stream()
                    .map(Material::getMaterial_status)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()));

            selectedGenres.clear();
            selectedGenres.addAll(materialGenreMap.values().stream()
                    .flatMap(Set::stream)
                    .map(this::getGenreName)
                    .collect(Collectors.toSet()));

            filteredList.clear();
            filteredList.addAll(materials);
            updateResultCount();
        } catch (DAOException e) {
            showError("Error loading materials", e.getMessage());
        }
    }

    /**
     * Handle clear button click
     */
    @FXML
    private void handleClear() {
        searchField.clear();
        yearFromField.clear();
        yearToField.clear();

        // Reset filters to select all
        selectedMaterialTypes.clear();
        selectedMaterialTypes.addAll(materialList.stream()
                .map(m -> getMaterialTypeName(m.getIdMaterialType()))
                .collect(Collectors.toSet()));

        selectedStatuses.clear();
        selectedStatuses.addAll(materialList.stream()
                .map(Material::getMaterial_status)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        selectedGenres.clear();
        selectedGenres.addAll(materialGenreMap.values().stream()
                .flatMap(Set::stream)
                .map(this::getGenreName)
                .collect(Collectors.toSet()));

        // Reset button texts
        if (materialTypeFilterButton != null) {
            materialTypeFilterButton.setValue("All Types");
            materialTypeFilterButton.setStyle("");
        }
        if (statusFilterButton != null) {
            statusFilterButton.setValue("All Statuses");
            statusFilterButton.setStyle("");
        }
        if (genreFilterButton != null) {
            genreFilterButton.setValue("All Genres");
            genreFilterButton.setStyle("");
        }

        filteredList.clear();
        filteredList.addAll(materialList);
        updateResultCount();
    }

    /**
     * Enhanced filter with improved search algorithm and genre support
     * Now works with cached data (materialList) instead of hitting DB
     */
    @FXML
    private void handleFilter() {
        try {
            List<Material> result = catalogService.filterMaterials(
                    materialList,  // Uses cached data
                    materialGenreMap,
                    materialTypeMap,
                    genreMap,
                    selectedMaterialTypes,
                    selectedStatuses,
                    selectedGenres,
                    yearFromField.getText().trim(),
                    yearToField.getText().trim(),
                    searchField.getText().trim()
            );

            filteredList.setAll(result);
            updateResultCount();
        } catch (Exception e) {
            showError("Filter Error", "Failed to filter materials: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles mouse clicks on the material table. Enables/disables edit/delete buttons based on selection
     * and handles double-click to initiate the edit action.
     * @param event The mouse event triggered by clicking the table.
     */
    @FXML
    private void handleTableClick(MouseEvent event) {
        Material selected = materialTable.getSelectionModel().getSelectedItem();
        editButton.setDisable(selected == null);
        deleteButton.setDisable(selected == null);

        if (event.getClickCount() == 2 && selected != null) {
            handleEdit();
        }
    }

    @FXML
    private void handleAddMaterial() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unicas/project/template/address/view/AddMaterial.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Material");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            dialogStage.showAndWait();  // This waits for dialog to close

            // FIXED: Refresh data after dialog closes
            refresh();  // This line was already there!

        } catch (IOException e) {
            showError("Error", "Could not load Add Material dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEdit() {
        Material selected = materialTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a material to edit");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unicas/project/template/address/view/MonoMaterialEdit.fxml"));
            Parent root = loader.load();

            MonoMaterialEditController controller = loader.getController();
            controller.setMaterial(selected);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Material");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(editButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            dialogStage.showAndWait();
            refresh();

        } catch (IOException e) {
            showError("Error", "Could not load Edit Material dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        Material selected = materialTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a material to delete");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Material");
        confirmation.setContentText("Are you sure you want to delete:\n" + selected.getTitle() + "?");

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

    @FXML
    private void handleBack() {
        if (mainApp != null) {
            mainApp.showAdminLanding();
        } else {
            showError("Navigation Error", "Cannot navigate back - MainApp not set");
        }
    }

    /**
     * Updates the label showing the count of filtered materials.
     */
    private void updateResultCount() {
        resultCountLabel.setText(String.format("Total: %d materials", filteredList.size()));
    }

    private String getMaterialTypeName(Integer typeId) {
        if (typeId == null) return "Unknown";
        return materialTypeMap.getOrDefault(typeId, "Unknown");
    }

    private String getGenreName(Integer genreId) {
        if (genreId == null) return "Unknown";
        return genreMap.getOrDefault(genreId, "Unknown");
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Displays a standard JavaFX alert for information.
     * @param title The title of the alert window.
     * @param content The main information message content.
     */
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Reloads all material-related data (relationships and all materials) from the database
     * and re-applies the current filter settings.
     */
    public void refresh() {
        loadMaterialGenreRelationships();
        loadAllMaterials();
        handleFilter();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
}
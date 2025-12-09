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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MaterialCatalogController {

    @FXML private TextField searchField;
    @FXML private Button materialTypeFilterButton;
    @FXML private Button statusFilterButton;
    @FXML private Button genreFilterButton;
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
    private GenreDAO genreDAO;
    private DAO<MaterialGenre> materialGenreDAO;

    private ObservableList<Material> materialList;
    private ObservableList<Material> filteredList;
    private Map<Integer, String> materialTypeMap;
    private Map<Integer, String> genreMap; // Cache for genre ID -> name mapping
    private Map<Integer, Set<Integer>> materialGenreMap; // Maps material ID -> set of genre IDs
    private boolean isAdminMode = true;

    // Filter selections
    private Set<String> selectedMaterialTypes = new HashSet<>();
    private Set<String> selectedStatuses = new HashSet<>();
    private Set<String> selectedGenres = new HashSet<>();

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
        setAdminMode(isAdminMode);

        // Initialize filter buttons
        setupFilterButtons();

        // Add listeners for real-time search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> handleFilter());
        yearFromField.textProperty().addListener((obs, oldVal, newVal) -> handleFilter());
        yearToField.textProperty().addListener((obs, oldVal, newVal) -> handleFilter());
    }

    /**
     * Setup filter buttons with icons/indicators
     */
    private void setupFilterButtons() {
        if (materialTypeFilterButton != null) {
            materialTypeFilterButton.setText("Material Type ▼");
            materialTypeFilterButton.setOnAction(e -> showMaterialTypeFilter());
        }

        if (statusFilterButton != null) {
            statusFilterButton.setText("Status ▼");
            statusFilterButton.setOnAction(e -> showStatusFilter());
        }

        if (genreFilterButton != null) {
            genreFilterButton.setText("Genre ▼");
            genreFilterButton.setOnAction(e -> showGenreFilter());
        }
    }

    /**
     * Show Material Type filter popup with checkboxes
     */
    private void showMaterialTypeFilter() {
        Set<String> allTypes = materialList.stream()
                .map(m -> getMaterialTypeName(m.getIdMaterialType()))
                .collect(Collectors.toSet());

        if (selectedMaterialTypes.isEmpty()) {
            selectedMaterialTypes.addAll(allTypes);
        }

        showCheckboxFilter(materialTypeFilterButton, "Material Type Filter",
                allTypes, selectedMaterialTypes);
    }

    /**
     * Show Status filter popup with checkboxes
     */
    private void showStatusFilter() {
        Set<String> allStatuses = materialList.stream()
                .map(Material::getMaterial_status)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (selectedStatuses.isEmpty()) {
            selectedStatuses.addAll(allStatuses);
        }

        showCheckboxFilter(statusFilterButton, "Status Filter",
                allStatuses, selectedStatuses);
    }

    /**
     * Show Genre filter popup with checkboxes
     */
    private void showGenreFilter() {
        // Get all unique genres from materials
        Set<String> allGenres = materialGenreMap.values().stream()
                .flatMap(Set::stream)
                .map(this::getGenreName)
                .collect(Collectors.toSet());

        if (selectedGenres.isEmpty()) {
            selectedGenres.addAll(allGenres);
        }

        showCheckboxFilter(genreFilterButton, "Genre Filter",
                allGenres, selectedGenres);
    }

    /**
     * Generic method to show checkbox filter popup
     */
    private void showCheckboxFilter(Button sourceButton, String title,
                                    Set<String> allOptions, Set<String> selectedOptions) {
        Stage filterStage = new Stage();
        filterStage.initModality(Modality.APPLICATION_MODAL);
        filterStage.initOwner(sourceButton.getScene().getWindow());
        filterStage.setTitle(title);

        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: white;");

        // Select All checkbox
        CheckBox selectAllCheckBox = new CheckBox("Select All");
        selectAllCheckBox.setSelected(selectedOptions.size() == allOptions.size());
        selectAllCheckBox.setStyle("-fx-font-weight: bold;");

        Separator separator = new Separator();

        // Individual checkboxes
        VBox checkboxContainer = new VBox(5);
        Map<String, CheckBox> checkBoxMap = new HashMap<>();

        List<String> sortedOptions = new ArrayList<>(allOptions);
        Collections.sort(sortedOptions);

        for (String option : sortedOptions) {
            CheckBox cb = new CheckBox(option);
            cb.setSelected(selectedOptions.contains(option));

            // When individual checkbox is clicked
            cb.setOnAction(e -> {
                if (cb.isSelected()) {
                    selectedOptions.add(option);
                } else {
                    selectedOptions.remove(option);
                }

                // Update Select All checkbox
                selectAllCheckBox.setSelected(selectedOptions.size() == allOptions.size());

                // Update button text to show filter is active
                updateFilterButtonText(sourceButton, selectedOptions, allOptions);
            });

            checkBoxMap.put(option, cb);
            checkboxContainer.getChildren().add(cb);
        }

        // Select All logic
        selectAllCheckBox.setOnAction(e -> {
            boolean isSelected = selectAllCheckBox.isSelected();

            if (isSelected) {
                // Select all
                selectedOptions.clear();
                selectedOptions.addAll(allOptions);
                checkBoxMap.values().forEach(cb -> cb.setSelected(true));
            } else {
                // Deselect all
                selectedOptions.clear();
                checkBoxMap.values().forEach(cb -> cb.setSelected(false));
            }

            updateFilterButtonText(sourceButton, selectedOptions, allOptions);
        });

        ScrollPane scrollPane = new ScrollPane(checkboxContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background-color: white;");

        // Action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button applyButton = new Button("Apply");
        applyButton.setOnAction(e -> {
            handleFilter();
            filterStage.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> filterStage.close());

        buttonBox.getChildren().addAll(cancelButton, applyButton);

        container.getChildren().addAll(selectAllCheckBox, separator, scrollPane, buttonBox);

        Scene scene = new Scene(container, 250, 400);
        filterStage.setScene(scene);
        filterStage.showAndWait();
    }

    /**
     * Update filter button text to indicate active filters
     */
    private void updateFilterButtonText(Button button, Set<String> selected, Set<String> all) {
        String baseText = button.getText().split(" \\(")[0].replace(" ▼", "");

        if (selected.isEmpty()) {
            button.setText(baseText + " (None) ▼");
            button.setStyle("-fx-text-fill: red;");
        } else if (selected.size() == all.size()) {
            button.setText(baseText + " ▼");
            button.setStyle("");
        } else {
            button.setText(baseText + " (" + selected.size() + "/" + all.size() + ") ▼");
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

            // Initialize genre filter
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
            materialTypeFilterButton.setText("Material Type ▼");
            materialTypeFilterButton.setStyle("");
        }
        if (statusFilterButton != null) {
            statusFilterButton.setText("Status ▼");
            statusFilterButton.setStyle("");
        }
        if (genreFilterButton != null) {
            genreFilterButton.setText("Genre ▼");
            genreFilterButton.setStyle("");
        }

        filteredList.clear();
        filteredList.addAll(materialList);
        updateResultCount();
    }

    /**
     * Enhanced filter with improved search algorithm and genre support
     */
    @FXML
    private void handleFilter() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        String yearFrom = yearFromField.getText().trim();
        String yearTo = yearToField.getText().trim();

        List<Material> filtered = materialList.stream()
                .filter(material -> {
                    // Material type filter
                    boolean matchesType = selectedMaterialTypes.isEmpty() ||
                            selectedMaterialTypes.contains(getMaterialTypeName(material.getIdMaterialType()));

                    // Status filter
                    boolean matchesStatus = selectedStatuses.isEmpty() ||
                            selectedStatuses.contains(material.getMaterial_status());

                    // Genre filter
                    boolean matchesGenre = true;
                    if (!selectedGenres.isEmpty()) {
                        Set<Integer> materialGenres = materialGenreMap.get(material.getIdMaterial());
                        if (materialGenres == null || materialGenres.isEmpty()) {
                            // Material has no genres - only include if "No Genre" or similar is selected
                            matchesGenre = false;
                        } else {
                            // Check if material has at least one selected genre
                            matchesGenre = materialGenres.stream()
                                    .map(this::getGenreName)
                                    .anyMatch(selectedGenres::contains);
                        }
                    }

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
                            matchesYear = true;
                        }
                    }

                    return matchesType && matchesStatus && matchesGenre && matchesYear;
                })
                .collect(Collectors.toList());

        // Apply search and sort
        if (!searchTerm.isEmpty()) {
            filtered = searchAndSort(filtered, searchTerm);
        }

        filteredList.clear();
        filteredList.addAll(filtered);
        updateResultCount();
    }

    /**
     * Enhanced search that prioritizes title matches, then author, then others
     * Only matches words that START with the search term
     */
    private List<Material> searchAndSort(List<Material> materials, String searchTerm) {
        List<Material> titleMatches = new ArrayList<>();
        List<Material> authorMatches = new ArrayList<>();
        List<Material> otherMatches = new ArrayList<>();

        for (Material material : materials) {
            boolean titleMatch = false;
            boolean authorMatch = false;
            boolean otherMatch = false;

            // Check title
            if (material.getTitle() != null) {
                if (containsWordStartingWith(material.getTitle(), searchTerm)) {
                    titleMatches.add(material);
                    titleMatch = true;
                }
            }

            // Check author (only if not already matched by title)
            if (!titleMatch && material.getAuthor() != null) {
                if (containsWordStartingWith(material.getAuthor(), searchTerm)) {
                    authorMatches.add(material);
                    authorMatch = true;
                }
            }

            // Check ISBN and other fields (only if not already matched)
            if (!titleMatch && !authorMatch) {
                if ((material.getISBN() != null && containsWordStartingWith(material.getISBN(), searchTerm)) ||
                        containsWordStartingWith(getMaterialTypeName(material.getIdMaterialType()), searchTerm) ||
                        (material.getMaterial_status() != null && containsWordStartingWith(material.getMaterial_status(), searchTerm))) {
                    otherMatches.add(material);
                }
            }
        }

        // Combine results: title matches first, then author, then others
        List<Material> result = new ArrayList<>();
        result.addAll(titleMatches);
        result.addAll(authorMatches);
        result.addAll(otherMatches);

        return result;
    }

    /**
     * Check if text contains any word that starts with the search term
     */
    private boolean containsWordStartingWith(String text, String searchTerm) {
        if (text == null || searchTerm == null) {
            return false;
        }

        String lowerText = text.toLowerCase();
        String lowerSearch = searchTerm.toLowerCase();

        // Split by common delimiters: space, comma, period, hyphen, etc.
        String[] words = lowerText.split("[\\s,.-]+");

        for (String word : words) {
            if (word.startsWith(lowerSearch)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Handle table row click
     */
    @FXML
    private void handleTableClick(MouseEvent event) {
        Material selected = materialTable.getSelectionModel().getSelectedItem();
        if (isAdminMode) {
            editButton.setDisable(selected == null);
            deleteButton.setDisable(selected == null);
        }

        if (event.getClickCount() == 2 && selected != null) {
            if (isAdminMode) {
                handleEdit();
            } else {
                handleView();
            }
        }
    }

    @FXML
    private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unicas/project/template/address/view/AddMaterial.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Material");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(addButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root));

            dialogStage.showAndWait();
            refresh();

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unicas/project/template/address/view/EditMaterial.fxml"));
            Parent root = loader.load();

            EditMaterialController controller = loader.getController();
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

    private void handleView() {
        Material selected = materialTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Material Details");
        alert.setHeaderText(selected.getTitle());

        StringBuilder content = new StringBuilder();
        content.append("Author: ").append(selected.getAuthor() != null ? selected.getAuthor() : "N/A").append("\n");
        content.append("Year: ").append(selected.getYear()).append("\n");
        content.append("ISBN: ").append(selected.getISBN() != null ? selected.getISBN() : "N/A").append("\n");
        content.append("Type: ").append(getMaterialTypeName(selected.getIdMaterialType())).append("\n");
        content.append("Status: ").append(selected.getMaterial_status()).append("\n");

        // Add genres
        Set<Integer> genres = materialGenreMap.get(selected.getIdMaterial());
        if (genres != null && !genres.isEmpty()) {
            String genreList = genres.stream()
                    .map(this::getGenreName)
                    .collect(Collectors.joining(", "));
            content.append("Genres: ").append(genreList);
        } else {
            content.append("Genres: None");
        }

        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    public void setAdminMode(boolean isAdmin) {
        this.isAdminMode = isAdmin;
        adminActionsBox.setVisible(isAdmin);
        adminActionsBox.setManaged(isAdmin);
    }

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

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void refresh() {
        loadMaterialGenreRelationships(); // Refresh genre relationships
        loadAllMaterials();
        handleFilter();
    }

    public void setMainApp(MainApp mainApp) {
    }
}
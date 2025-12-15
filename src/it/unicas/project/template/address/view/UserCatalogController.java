package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.*;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.GenreDAO;
import it.unicas.project.template.address.model.dao.mysql.*;
import it.unicas.project.template.address.service.MaterialHoldService;
import it.unicas.project.template.address.service.SearchService;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Controller for Material Catalog - USER VIEW.
 * FIXED: Added debouncing to prevent connection leaks on text input
 */
public class UserCatalogController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> materialTypeFilterButton;
    @FXML private ComboBox<String> genreFilterButton;
    @FXML private TextField yearFromField;
    @FXML private TextField yearToField;
    @FXML private TableView<GroupedMaterial> materialTable;
    @FXML private TableColumn<GroupedMaterial, String> titleColumn;
    @FXML private TableColumn<GroupedMaterial, String> authorColumn;
    @FXML private TableColumn<GroupedMaterial, Integer> yearColumn;
    @FXML private TableColumn<GroupedMaterial, String> isbnColumn;
    @FXML private TableColumn<GroupedMaterial, String> typeColumn;
    @FXML private TableColumn<GroupedMaterial, String> genreColumn;
    @FXML private TableColumn<GroupedMaterial, Void> actionColumn;
    @FXML private Label resultCountLabel;

    private MainApp mainApp;
    private User currentUser;

    // DAO dependencies
    private DAO<Material> materialDAO;
    private MaterialTypeDAOMySQLImpl materialTypeDAO;
    private GenreDAO genreDAO;
    private DAO<MaterialGenre> materialGenreDAO;
    private DAO<Hold> holdDAO;

    // Observable lists used by the UI
    private ObservableList<Material> allMaterials;
    private ObservableList<GroupedMaterial> groupedMaterialList;
    private ObservableList<GroupedMaterial> filteredList;

    // Lookup maps for types/genres and relationships
    private Map<Integer, String> materialTypeMap;
    private Map<Integer, String> genreMap;
    private Map<Integer, Set<Integer>> materialGenreMap;

    // Filter selections
    private Set<String> selectedMaterialTypes = new HashSet<>();
    private Set<String> selectedGenres = new HashSet<>();

    // Popups for filters
    private Popup materialTypePopup;
    private Popup genrePopup;

    private final MaterialHoldService holdService = new MaterialHoldService();
    // Search service for prioritized field searching
    private final SearchService<GroupedMaterial> searchService = new SearchService<>();
    private List<Function<GroupedMaterial, String>> searchFields;
    // FIXED: Debounce mechanism for search
    private ScheduledExecutorService searchScheduler = Executors.newSingleThreadScheduledExecutor();
    private java.util.concurrent.Future<?> filterTask;

    /**
     * Inner class to represent grouped materials.
     */
    public static class GroupedMaterial {
        private String title;
        private String author;
        private Integer year;
        private String ISBN;
        private String type;
        private String genres;
        private List<Material> materials;
        private boolean hasAvailable;
        private boolean hasHolded;

        public GroupedMaterial(String title, String author, Integer year, String ISBN,
                               String type, String genres, List<Material> materials) {
            this.title = title;
            this.author = author;
            this.year = year;
            this.ISBN = ISBN;
            this.type = type;
            this.genres = genres;
            this.materials = materials;
            updateAvailability();
        }

        public void updateAvailability() {
            hasAvailable = materials.stream()
                    .anyMatch(m -> "available".equalsIgnoreCase(m.getMaterial_status()));
        }

        // Getters
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public Integer getYear() { return year; }
        public String getISBN() { return ISBN; }
        public String getType() { return type; }
        public String getGenres() { return genres; }
        public List<Material> getMaterials() { return materials; }
        public boolean hasAvailable() { return hasAvailable; }
    }

    /**
     * Initialize the controller.
     */
    @FXML
    public void initialize() {
        materialDAO = MaterialDAOMySQLImpl.getInstance();
        materialTypeDAO = MaterialTypeDAOMySQLImpl.getInstance();
        genreDAO = GenreDAOMySQLImpl.getInstance();
        materialGenreDAO = MaterialGenreDAOMySQLImpl.getInstance();
        holdDAO = HoldDAOMySQLImpl.getInstance();

        allMaterials = FXCollections.observableArrayList();
        groupedMaterialList = FXCollections.observableArrayList();
        filteredList = FXCollections.observableArrayList();
        materialTypeMap = new HashMap<>();
        genreMap = new HashMap<>();
        materialGenreMap = new HashMap<>();

        loadMaterialTypes();
        loadGenres();
        loadMaterialGenreRelationships();
        setupTableColumns();

        setupFilterButtons();

        // FIXED: Add debounced listeners for real-time search (300ms delay)
        searchField.textProperty().addListener((obs, oldVal, newVal) -> scheduleFilter());
        yearFromField.textProperty().addListener((obs, oldVal, newVal) -> scheduleFilter());
        yearToField.textProperty().addListener((obs, oldVal, newVal) -> scheduleFilter());
        // Setup search fields in priority order
        searchFields = SearchService.<GroupedMaterial>fieldsBuilder()
                .addField(GroupedMaterial::getTitle)     // Highest priority
                .addField(GroupedMaterial::getAuthor)    // Second priority
                .addField(GroupedMaterial::getISBN)      // Third priority
                .addField(GroupedMaterial::getType)      // Fourth priority
                .addField(GroupedMaterial::getGenres)    // Fifth priority
                .build();
    }

    /**
     * FIXED: Schedule filter execution after 300ms delay
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
     * Configure basic behavior for filter trigger controls.
     */
    private void setupFilterButtons() {
        if (materialTypeFilterButton != null) {
            materialTypeFilterButton.setValue("All Types");
            materialTypeFilterButton.setOnMouseClicked(e -> toggleMaterialTypeFilter());
        }

        if (genreFilterButton != null) {
            genreFilterButton.setValue("All Genres");
            genreFilterButton.setOnMouseClicked(e -> toggleGenreFilter());
        }
    }

    /**
     * Toggle Material Type filter popup visibility.
     */
    private void toggleMaterialTypeFilter() {
        if (materialTypePopup != null && materialTypePopup.isShowing()) {
            materialTypePopup.hide();
            return;
        }

        Set<String> allTypes = groupedMaterialList.stream()
                .map(GroupedMaterial::getType)
                .collect(Collectors.toCollection(TreeSet::new));

        materialTypePopup = createFilterPopup(materialTypeFilterButton, "Material Types",
                allTypes, selectedMaterialTypes);
    }

    /**
     * Toggle Genre filter popup visibility.
     */
    private void toggleGenreFilter() {
        if (genrePopup != null && genrePopup.isShowing()) {
            genrePopup.hide();
            return;
        }

        Set<String> allGenres = groupedMaterialList.stream()
                .map(GroupedMaterial::getGenres)
                .filter(g -> g != null && !g.equals("—"))
                .flatMap(g -> Arrays.stream(g.split(", ")))
                .collect(Collectors.toCollection(TreeSet::new));

        genrePopup = createFilterPopup(genreFilterButton, "Genres",
                allGenres, selectedGenres);
    }

    /**
     * Create a popup containing a list of checkboxes for filtering.
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

        CheckBox selectAllCheckBox = new CheckBox("Select All");
        selectAllCheckBox.setSelected(selectedOptions.size() == allOptions.size());
        selectAllCheckBox.setStyle("-fx-font-weight: bold; -fx-padding: 5;");

        Separator separator = new Separator();

        VBox checkboxContainer = new VBox(3);
        checkboxContainer.setPadding(new Insets(5));

        Map<String, CheckBox> checkBoxMap = new HashMap<>();

        for (String option : allOptions) {
            CheckBox cb = new CheckBox(option);
            cb.setSelected(selectedOptions.contains(option));
            cb.setStyle("-fx-padding: 3;");

            cb.setOnAction(e -> {
                if (cb.isSelected()) {
                    selectedOptions.add(option);
                } else {
                    selectedOptions.remove(option);
                }
                selectAllCheckBox.setSelected(selectedOptions.size() == allOptions.size());
                updateFilterButtonText(sourceButton, selectedOptions.size(), allOptions.size(), label);
                // FIXED: Use scheduled filter instead of immediate
                scheduleFilter();
            });

            checkBoxMap.put(option, cb);
            checkboxContainer.getChildren().add(cb);
        }

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
            // FIXED: Use scheduled filter instead of immediate
            scheduleFilter();
        });

        ScrollPane scrollPane = new ScrollPane(checkboxContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(250);
        scrollPane.setStyle("-fx-background-color: white; -fx-border-color: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        container.getChildren().addAll(selectAllCheckBox, separator, scrollPane);

        popup.getContent().add(container);

        Bounds bounds = sourceButton.localToScreen(sourceButton.getBoundsInLocal());
        popup.show(sourceButton, bounds.getMinX(), bounds.getMaxY());

        return popup;
    }

    /**
     * Update the text and style of a filter trigger button according to selection.
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
     * Load material types from the database into materialTypeMap.
     */
    private void loadMaterialTypes() {
        List<MaterialType> types = materialTypeDAO.selectAll();
        materialTypeMap.clear();
        for (MaterialType type : types) {
            materialTypeMap.put(type.getIdMaterialType(), type.getMaterial_type());
        }
    }

    /**
     * Load genres from the database into genreMap.
     */
    private void loadGenres() {
        List<Genre> genres = genreDAO.selectAll();
        genreMap.clear();
        for (Genre genre : genres) {
            genreMap.put(genre.getIdGenre(), genre.getGenre());
        }
    }

    /**
     * Load material-genre relationship table into materialGenreMap.
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
     * Setup table column bindings and the action column cell factory.
     */
    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("ISBN"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genres"));

        // Action column with hold button
        actionColumn.setCellFactory(column -> new TableCell<GroupedMaterial, Void>() {
            private final Button holdButton = new Button();

            {
                holdButton.setMinWidth(100);
                holdButton.setMaxWidth(100);
                holdButton.setOnAction(event -> {
                    GroupedMaterial item = getTableView().getItems().get(getIndex());
                    handleHoldToggle(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    GroupedMaterial material = getTableView().getItems().get(getIndex());
                    updateButtonState(material);
                    setGraphic(holdButton);
                    setAlignment(Pos.CENTER);
                }
            }

            private void updateButtonState(GroupedMaterial material) {
                // Check if current user has a hold on any copy
                boolean currentUserHasHold = false;
                try {
                    for (Material m : material.getMaterials()) {
                        if ("holded".equalsIgnoreCase(m.getMaterial_status())) {
                            Hold searchHold = new Hold();
                            searchHold.setIdUser(currentUser.getIdUser());
                            searchHold.setIdMaterial(m.getIdMaterial());
                            List<Hold> holds = holdDAO.select(searchHold);
                            if (!holds.isEmpty()) {
                                currentUserHasHold = true;
                                break;
                            }
                        }
                    }
                } catch (DAOException e) {
                    e.printStackTrace();
                }

                if (currentUserHasHold) {
                    holdButton.setText("Holded");
                    holdButton.setStyle("-fx-background-color: #81C784; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-cursor: hand;");
                    holdButton.setDisable(false);
                } else if (material.hasAvailable()) {
                    holdButton.setText("Hold");
                    holdButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-cursor: hand;");
                    holdButton.setDisable(false);
                } else {
                    holdButton.setText("Unavailable");
                    holdButton.setStyle("-fx-background-color: #BDBDBD; -fx-text-fill: #757575; " +
                            "-fx-font-weight: bold; -fx-cursor: not-allowed;");
                    holdButton.setDisable(true);
                }
            }
        });

        materialTable.setItems(filteredList);
    }

    /**
     * Handle toggle of a hold for the provided grouped material.
     */
    private void handleHoldToggle(GroupedMaterial groupedMaterial) {
        if (currentUser == null) {
            showError("Error", "No user logged in");
            return;
        }

        try {
            boolean userHasHold = false;
            Hold userHold = null;
            Material heldMaterial = null;

            for (Material material : groupedMaterial.getMaterials()) {
                if ("holded".equalsIgnoreCase(material.getMaterial_status())) {
                    Hold searchHold = new Hold();
                    searchHold.setIdUser(currentUser.getIdUser());
                    searchHold.setIdMaterial(material.getIdMaterial());
                    List<Hold> holds = holdDAO.select(searchHold);
                    if (!holds.isEmpty()) {
                        userHasHold = true;
                        userHold = holds.get(0);
                        heldMaterial = material;
                        break;
                    }
                }
            }

            if (userHasHold) {
                try {
                    holdService.releaseHold(userHold, heldMaterial);
                    refresh();
                } catch (DAOException e) {
                    showError("Error", "Could not release hold: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (groupedMaterial.hasAvailable()) {
                Material availableMaterial = groupedMaterial.getMaterials().stream()
                        .filter(m -> "available".equalsIgnoreCase(m.getMaterial_status()))
                        .findFirst()
                        .orElse(null);

                if (availableMaterial != null) {
                    try {
                        holdService.holdMaterial(currentUser.getIdUser(), availableMaterial);
                        refresh();
                    } catch (DAOException e) {
                        showError("Error", "Could not place hold: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                showInfo("Notice", "No copies available to hold.");
            }
        } catch (DAOException e) {
            showError("Error", "Failed to process hold: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load all materials from the database, group them and populate the
     * groupedMaterialList used by the table view.
     */
    private void loadAllMaterials() {
        try {
            List<Material> materials = materialDAO.select(null);
            allMaterials.clear();
            allMaterials.addAll(materials);

            // Group materials
            Map<String, List<Material>> grouped = new HashMap<>();
            for (Material material : materials) {
                String key = generateGroupKey(material);
                grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(material);
            }

            groupedMaterialList.clear();
            for (List<Material> group : grouped.values()) {
                Material first = group.get(0);
                String type = getMaterialTypeName(first.getIdMaterialType());
                String genres = getGenresForMaterial(first.getIdMaterial());

                GroupedMaterial gm = new GroupedMaterial(
                        first.getTitle(),
                        first.getAuthor(),
                        first.getYear(),
                        first.getISBN(),
                        type,
                        genres,
                        group
                );
                groupedMaterialList.add(gm);
            }

            // Initialize filters
            selectedMaterialTypes.clear();
            selectedMaterialTypes.addAll(groupedMaterialList.stream()
                    .map(GroupedMaterial::getType)
                    .collect(Collectors.toSet()));

            selectedGenres.clear();
            selectedGenres.addAll(groupedMaterialList.stream()
                    .map(GroupedMaterial::getGenres)
                    .filter(g -> g != null && !g.equals("—"))
                    .flatMap(g -> Arrays.stream(g.split(", ")))
                    .collect(Collectors.toSet()));

            filteredList.clear();
            filteredList.addAll(groupedMaterialList);
            updateResultCount();
        } catch (DAOException e) {
            showError("Error loading materials", e.getMessage());
        }
    }

    /**
     * Generate a grouping key for a material.
     */
    private String generateGroupKey(Material material) {
        String isbn = (material.getISBN() != null && !material.getISBN().trim().isEmpty())
                ? material.getISBN() : "NO_ISBN";
        return String.format("%s|%s|%d|%s",
                material.getTitle(),
                material.getAuthor(),
                material.getYear(),
                isbn
        );
    }

    /**
     * Build a comma-separated genre string for a material.
     */
    private String getGenresForMaterial(Integer materialId) {
        Set<Integer> genreIds = materialGenreMap.get(materialId);
        if (genreIds != null && !genreIds.isEmpty()) {
            return genreIds.stream()
                    .map(id -> genreMap.getOrDefault(id, "Unknown"))
                    .sorted()
                    .collect(Collectors.joining(", "));
        }
        return "—";
    }

    /**
     * Clear all filters and reset the view.
     */
    @FXML
    private void handleClear() {
        searchField.clear();
        yearFromField.clear();
        yearToField.clear();

        selectedMaterialTypes.clear();
        selectedMaterialTypes.addAll(groupedMaterialList.stream()
                .map(GroupedMaterial::getType)
                .collect(Collectors.toSet()));

        selectedGenres.clear();
        selectedGenres.addAll(groupedMaterialList.stream()
                .map(GroupedMaterial::getGenres)
                .filter(g -> g != null && !g.equals("—"))
                .flatMap(g -> Arrays.stream(g.split(", ")))
                .collect(Collectors.toSet()));

        if (materialTypeFilterButton != null) {
            materialTypeFilterButton.setValue("All Types");
            materialTypeFilterButton.setStyle("");
        }
        if (genreFilterButton != null) {
            genreFilterButton.setValue("All Genres");
            genreFilterButton.setStyle("");
        }

        filteredList.clear();
        filteredList.addAll(groupedMaterialList);
        updateResultCount();
    }

    /**
     * Apply the combined search and filter criteria.
     * FIXED: Now uses SearchService for prioritized field searching
     */
    @FXML
    private void handleFilter() {
        try {
            String searchText = searchField.getText().trim();
            String yearFrom = yearFromField.getText().trim();
            String yearTo = yearToField.getText().trim();

            // Start with all materials
            List<GroupedMaterial> candidates = new ArrayList<>(groupedMaterialList);

            // Apply type filter
            candidates = candidates.stream()
                    .filter(gm -> selectedMaterialTypes.contains(gm.getType()))
                    .collect(Collectors.toList());

            // Apply genre filter
            candidates = candidates.stream()
                    .filter(gm -> {
                        if (gm.getGenres() == null || gm.getGenres().equals("—")) {
                            return true; // Include materials with no genres
                        }
                        for (String genre : gm.getGenres().split(", ")) {
                            if (selectedGenres.contains(genre)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .collect(Collectors.toList());

            // Apply year range filter
            if (!yearFrom.isEmpty() || !yearTo.isEmpty()) {
                candidates = candidates.stream()
                        .filter(gm -> {
                            try {
                                int year = gm.getYear();
                                if (!yearFrom.isEmpty() && year < Integer.parseInt(yearFrom)) return false;
                                if (!yearTo.isEmpty() && year > Integer.parseInt(yearTo)) return false;
                                return true;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        })
                        .collect(Collectors.toList());
            }

            // Apply search text filter using SearchService for prioritized results
            if (!searchText.isEmpty()) {
                candidates = searchService.searchAndSort(candidates, searchText, searchFields);
            }

            filteredList.setAll(candidates);
            updateResultCount();
        } catch (Exception e) {
            showError("Filter Error", "Failed to filter materials: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigate back to the user landing view.
     */
    @FXML
    private void handleBack() {
        if (mainApp != null) {
            mainApp.showUserLanding();
        } else {
            showError("Navigation Error", "Cannot navigate back");
        }
    }

    /**
     * Update the label that reports the number of visible materials.
     */
    private void updateResultCount() {
        resultCountLabel.setText(String.format("Total: %d materials", filteredList.size()));
    }

    /**
     * Lookup human-friendly material type name by id.
     */
    private String getMaterialTypeName(Integer typeId) {
        if (typeId == null) return "Unknown";
        return materialTypeMap.getOrDefault(typeId, "Unknown");
    }

    /**
     * Show an error dialog to the user.
     */
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Show an informational dialog to the user.
     */
    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Refresh lookup relationships and material list, then reapply filters.
     */
    public void refresh() {
        loadMaterialGenreRelationships();
        loadAllMaterials();
        handleFilter();
    }

    /**
     * Set the main application reference used for navigation.
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Set the current logged-in user.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (allMaterials.isEmpty()) {
            loadAllMaterials();
        }
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
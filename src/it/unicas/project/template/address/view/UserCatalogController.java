package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.*;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.GenreDAO;
import it.unicas.project.template.address.model.dao.mysql.*;
import it.unicas.project.template.address.service.MaterialHoldService;
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
import java.util.stream.Collectors;

/**
 * Controller for Material Catalog - USER VIEW.
 * <p>
 * This JavaFX controller is responsible for displaying a grouped list of materials
 * (books, DVDs, etc.) to a logged-in {@link User}. It supports searching, filtering
 * by material type and genre, year range filtering and placing/releases of holds
 * on materials through {@link MaterialHoldService}.
 * </p>
 */
public class UserCatalogController {

    /** Text field used to enter search queries (title/author/ISBN...). */
    @FXML private TextField searchField;
    /** ComboBox serving as a visual trigger for material type filter popup. */
    @FXML private ComboBox<String> materialTypeFilterButton;
    /** ComboBox serving as a visual trigger for genre filter popup. */
    @FXML private ComboBox<String> genreFilterButton;
    /** Text fields for lower and upper bound of year range filter. */
    @FXML private TextField yearFromField;
    @FXML private TextField yearToField;
    /** TableView displaying grouped materials (one row per title/author group). */
    @FXML private TableView<GroupedMaterial> materialTable;
    @FXML private TableColumn<GroupedMaterial, String> titleColumn;
    @FXML private TableColumn<GroupedMaterial, String> authorColumn;
    @FXML private TableColumn<GroupedMaterial, Integer> yearColumn;
    @FXML private TableColumn<GroupedMaterial, String> isbnColumn;
    @FXML private TableColumn<GroupedMaterial, String> typeColumn;
    @FXML private TableColumn<GroupedMaterial, String> genreColumn;
    @FXML private TableColumn<GroupedMaterial, Void> actionColumn;
    /** Label showing the number of results currently displayed. */
    @FXML private Label resultCountLabel;

    /** Reference to the main application instance for navigation. */
    private MainApp mainApp;
    /** Currently logged-in user. */
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

    /** Service that encapsulates hold/release logic. */
    private final MaterialHoldService holdService = new MaterialHoldService();

    /**
     * Inner class to represent grouped materials.
     * <p>
     * A {@code GroupedMaterial} groups one or more {@link Material} instances that
     * correspond to the same title/author/year/ISBN combination. It contains
     * derived attributes used for display (e.g. combined genres, availability).
     * </p>
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

        /**
         * Construct a grouped material object.
         *
         * @param title display title
         * @param author display author
         * @param year publication year
         * @param ISBN ISBN string (may be null/empty)
         * @param type material type name (e.g. Book, DVD)
         * @param genres comma separated genres string
         * @param materials list of material copies included in this group
         */
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

        /**
         * Update availability flags based on the current {@code materials} list.
         * <p>
         * Sets {@link #hasAvailable} to true if at least one copy is "available".
         * </p>
         */
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
     * <p>
     * This method is invoked by the JavaFX framework after the FXML fields are
     * injected. It initializes DAOs, collections and UI column bindings, loads
     * lookup data (types/genres) and configures listeners used for live search.
     * </p>
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

        // Add listeners for real-time search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> handleFilter());
        yearFromField.textProperty().addListener((obs, oldVal, newVal) -> handleFilter());
        yearToField.textProperty().addListener((obs, oldVal, newVal) -> handleFilter());
    }

    /**
     * Configure basic behavior for filter trigger controls.
     * <p>
     * Sets the default displayed value and attaches mouse click handlers which
     * toggle the corresponding filter popups.
     * </p>
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
     * <p>
     * If the popup is already visible it will be hidden, otherwise a new popup
     * will be created showing all available material types detected in the
     * grouped material list.
     * </p>
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
     * <p>
     * If the popup is already visible it will be hidden, otherwise a new popup
     * will be created showing all genres parsed from grouped material entries.
     * </p>
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
     *
     * @param sourceButton the ComboBox that triggered the popup (used for positioning)
     * @param label a human-readable label used by the popup and button text
     * @param allOptions full set of options to display
     * @param selectedOptions set that will be updated when checkboxes change
     * @return a configured {@link Popup} instance that is already shown on screen
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
                handleFilter();
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
            handleFilter();
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
     *
     * @param button the ComboBox serving as the visual filter trigger
     * @param selected number of selected items
     * @param total total number of available items
     * @param label human-readable label used to compose the button text
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
     * Load material types from the database into {@link #materialTypeMap}.
     * <p>
     * The map keys are database IDs and values are human-readable type names.
     * </p>
     */
    private void loadMaterialTypes() {
        List<MaterialType> types = materialTypeDAO.selectAll();
        materialTypeMap.clear();
        for (MaterialType type : types) {
            materialTypeMap.put(type.getIdMaterialType(), type.getMaterial_type());
        }
    }

    /**
     * Load genres from the database into {@link #genreMap}.
     */
    private void loadGenres() {
        List<Genre> genres = genreDAO.selectAll();
        genreMap.clear();
        for (Genre genre : genres) {
            genreMap.put(genre.getIdGenre(), genre.getGenre());
        }
    }

    /**
     * Load material-genre relationship table into {@link #materialGenreMap}.
     * <p>
     * The result maps a material id to a set of genre ids.
     * </p>
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
     * <p>
     * The action column contains a button that lets the current user place or
     * release a hold on a grouped material. The displayed button text and style
     * depends on availability and whether the current user already holds a copy.
     * </p>
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
                    // Available copies exist
                    holdButton.setText("Hold");
                    holdButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-cursor: hand;");
                    holdButton.setDisable(false);
                } else {
                    // No copies available
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
     * <p>
     * If the current user already has a hold on one of the group's copies this
     * method will release it. Otherwise, if there is an available copy it will
     * attempt to place a new hold for the current user.
     * </p>
     *
     * @param groupedMaterial grouped material selected by the user
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
                // User already has a hold - release it
                try {
                    holdService.releaseHold(userHold, heldMaterial);
                    refresh();
                } catch (DAOException e) {
                    showError("Error", "Could not release hold: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (groupedMaterial.hasAvailable()) {
                // No hold by current user - try to place one
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
     * {@link #groupedMaterialList} used by the table view.
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
     * <p>
     * Materials are grouped by title, author, year and ISBN. If ISBN is missing
     * a sentinel value {@code "NO_ISBN"} is used to avoid nulls.
     * </p>
     *
     * @param material the material used to create the group key
     * @return a string key uniquely identifying the group for identical metadata
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
     * Build a comma-separated genre string for a material using the loaded
     * {@link #materialGenreMap} and {@link #genreMap} lookups.
     *
     * @param materialId id of the material
     * @return a sorted, comma-separated list of genre names or a dash ("—") when none
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
     * Clear all filters and reset the view to show every grouped material.
     * <p>
     * This method is bound to the Clear button in the UI via FXML.
     * </p>
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
     * Apply the combined search and filter criteria to update the displayed list.
     * <p>
     * This method reads search text, year range and the selected filters and
     * updates {@link #filteredList} which backs the TableView.
     * </p>
     */
    @FXML
    private void handleFilter() {
        String searchText = searchField.getText().trim().toLowerCase();
        String yearFrom = yearFromField.getText().trim();
        String yearTo = yearToField.getText().trim();

        List<GroupedMaterial> result = groupedMaterialList.stream()
                .filter(gm -> {
                    // Type filter
                    if (!selectedMaterialTypes.contains(gm.getType())) return false;

                    // Genre filter
                    if (gm.getGenres() != null && !gm.getGenres().equals("—")) {
                        boolean matchesGenre = false;
                        for (String genre : gm.getGenres().split(", ")) {
                            if (selectedGenres.contains(genre)) {
                                matchesGenre = true;
                                break;
                            }
                        }
                        if (!matchesGenre) return false;
                    }

                    // Year range filter
                    if (!yearFrom.isEmpty() || !yearTo.isEmpty()) {
                        try {
                            int year = gm.getYear();
                            if (!yearFrom.isEmpty() && year < Integer.parseInt(yearFrom)) return false;
                            if (!yearTo.isEmpty() && year > Integer.parseInt(yearTo)) return false;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }

                    // Search text filter
                    if (!searchText.isEmpty()) {
                        String title = gm.getTitle() != null ? gm.getTitle().toLowerCase() : "";
                        String author = gm.getAuthor() != null ? gm.getAuthor().toLowerCase() : "";
                        String isbn = gm.getISBN() != null ? gm.getISBN().toLowerCase() : "";

                        return title.contains(searchText) ||
                                author.contains(searchText) ||
                                isbn.contains(searchText);
                    }

                    return true;
                })
                .collect(Collectors.toList());

        filteredList.setAll(result);
        updateResultCount();
    }

    /**
     * Navigate back to the user landing view.
     * <p>
     * Bound to the Back button in the UI. If the {@link #mainApp} reference
     * is missing an error dialog will be shown.
     * </p>
     */
    @FXML
    private void handleBack() {
        if (mainApp != null) {
            mainApp.showUserLandingView();
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
     *
     * @param typeId id from the material table
     * @return name of the material type or "Unknown" when missing
     */
    private String getMaterialTypeName(Integer typeId) {
        if (typeId == null) return "Unknown";
        return materialTypeMap.getOrDefault(typeId, "Unknown");
    }

    /**
     * Show an error dialog to the user.
     *
     * @param title dialog title
     * @param content detailed message to display
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
     *
     * @param title dialog title
     * @param content detailed message to display
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
     *
     * @param mainApp main application instance
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Set the current logged-in user.
     * <p>
     * When the user is set the controller will lazily load the material list if
     * it has not already been loaded.
     * </p>
     *
     * @param user user that will be associated with this controller
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        // Load data after establishing the user
        if (allMaterials.isEmpty()) {
            loadAllMaterials();
        }
    }
}
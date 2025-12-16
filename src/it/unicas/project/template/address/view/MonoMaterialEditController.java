package it.unicas.project.template.address.view;

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
import it.unicas.project.template.address.service.MaterialService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller class for editing a single Material entity.
 * This view allows modification of material details (title, author, ISBN, year, type)
 * and managing its associated genres using a search and tag system.
 */
public class MonoMaterialEditController {

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField isbnField;
    @FXML private TextField yearField;
    @FXML private ComboBox<MaterialType> materialTypeComboBox;

    // Genre UI Components
    @FXML private TextField genreSearchField;
    @FXML private ListView<Genre> genreSearchResultsList;
    @FXML private FlowPane selectedGenresPane;

    private final DAO<Material> materialDAO = MaterialDAOMySQLImpl.getInstance();
    private final MaterialTypeDAOMySQLImpl materialTypeDAO = MaterialTypeDAOMySQLImpl.getInstance();
    private final GenreDAO genreDAO = GenreDAOMySQLImpl.getInstance();
    private final DAO<MaterialGenre> materialGenreDAO = MaterialGenreDAOMySQLImpl.getInstance();


    private List<Genre> allGenres = new ArrayList<>();
    private Set<Genre> selectedGenres = new HashSet<>();
    private Material currentMaterial; // The material being edited

    /**
     * Initializes the controller. This method is called automatically after the FXML file is loaded.
     * It loads material types and genres, and sets up the genre search functionality.
     */
    @FXML
    public void initialize() {
        System.out.println("MonoMaterialEditController.initialize() called");

        loadMaterialTypes();
        loadGenres();
        setupGenreSearch();
    }

    /**
     * Sets the material to be edited and populates all input fields with its current data.
     * It also loads the existing genres associated with the material.
     *
     * @param material The {@code Material} object to be edited.
     */
    public void setMaterial(Material material) {
        this.currentMaterial = material;

        if (material != null) {
            // Populate fields
            titleField.setText(material.getTitle());
            authorField.setText(material.getAuthor());
            isbnField.setText(material.getISBN());
            yearField.setText(String.valueOf(material.getYear()));

            // Select material type
            for (MaterialType type : materialTypeComboBox.getItems()) {
                if (type.getIdMaterialType().equals(material.getIdMaterialType())) {
                    materialTypeComboBox.setValue(type);
                    break;
                }
            }


            // Load existing genres for this material
            loadExistingGenres(material.getIdMaterial());
        }
    }

    /**
     * Loads all available {@code MaterialType} entities from the database and populates the ComboBox.
     */
    private void loadMaterialTypes() {
        List<MaterialType> types = materialTypeDAO.selectAll();
        ObservableList<MaterialType> obs = FXCollections.observableArrayList(types);
        materialTypeComboBox.setItems(obs);
    }


    /**
     * Loads all available {@code Genre} entities from the database into the {@code allGenres} list.
     */
    private void loadGenres() {
        allGenres = genreDAO.selectAll();
    }

    /**
     * Loads the existing genres for the material being edited from the {@code MaterialGenre} association table.
     * The loaded genres are displayed as tags in the {@code selectedGenresPane}.
     *
     * @param materialId The ID of the material whose genres are to be loaded.
     */
    private void loadExistingGenres(Integer materialId) {
        try {
            // Query material_genres table for this material
            MaterialGenre searchCriteria = new MaterialGenre(materialId, -1);
            List<MaterialGenre> materialGenres = materialGenreDAO.select(searchCriteria);

            // Find and add corresponding Genre objects
            for (MaterialGenre mg : materialGenres) {
                Genre genre = allGenres.stream()
                        .filter(g -> g.getIdGenre().equals(mg.getIdGenre()))
                        .findFirst()
                        .orElse(null);

                if (genre != null) {
                    addGenreTag(genre);
                }
            }
        } catch (DAOException e) {
            showAlert(Alert.AlertType.ERROR, "Error loading genres: " + e.getMessage());
        }
    }

    /**
     * Sets up the listener for the genre search field, the mouse click handler for the
     * search results list, and a focus listener to manage the visibility of the results list.
     */
    private void setupGenreSearch() {
        // Setup search field listener
        genreSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                genreSearchResultsList.setVisible(false);
                genreSearchResultsList.setManaged(false);
            } else {
                filterAndShowGenres(newVal.trim());
            }
        });

        // Handle genre selection from list
        genreSearchResultsList.setCellFactory(lv -> {
            ListCell<Genre> cell = new ListCell<>() {
                @Override
                protected void updateItem(Genre item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getGenre());
                }
            };

            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty()) {
                    Genre selected = cell.getItem();
                    if (selected != null) {
                        addGenreTag(selected);
                        genreSearchField.clear();
                        genreSearchResultsList.setVisible(false);
                        genreSearchResultsList.setManaged(false);
                    }
                }
            });

            return cell;
        });

        // Setup focus listener to hide the search results when focus is lost
        genreSearchField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                new Thread(() -> {
                    try {
                        // Small delay to allow click on ListView to register
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    javafx.application.Platform.runLater(() -> {
                        // Only hide if the ListView itself did not receive focus
                        if (!genreSearchResultsList.isFocused()) {
                            genreSearchResultsList.setVisible(false);
                            genreSearchResultsList.setManaged(false);
                        }
                    });
                }).start();
            }
        });
    }

    /**
     * Filters the list of all available genres based on the query, excluding already selected genres,
     * and updates the {@code genreSearchResultsList}.
     *
     * @param query The search string entered by the user.
     */
    private void filterAndShowGenres(String query) {
        String lowerQuery = query.toLowerCase();

        List<Genre> filtered = allGenres.stream()
                .filter(g -> !selectedGenres.contains(g))
                .filter(g -> g.getGenre().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            genreSearchResultsList.setVisible(false);
            genreSearchResultsList.setManaged(false);
        } else {
            ObservableList<Genre> items = FXCollections.observableArrayList(filtered);
            genreSearchResultsList.setItems(items);
            genreSearchResultsList.setVisible(true);
            genreSearchResultsList.setManaged(true);
        }
    }

    /**
     * Adds a {@code Genre} to the {@code selectedGenres} set and displays it as a removable tag
     * (a button) in the {@code selectedGenresPane}.
     *
     * @param genre The genre to be added as a tag.
     */
    private void addGenreTag(Genre genre) {
        if (selectedGenres.contains(genre)) {
            return;
        }

        selectedGenres.add(genre);

        Button tagButton = new Button("× " + genre.getGenre());
        tagButton.setStyle(
                "-fx-background-color: #e0e0e0; " +
                        "-fx-background-radius: 15; " +
                        "-fx-padding: 5 10 5 10; " +
                        "-fx-cursor: hand;"
        );

        // Hover styles
        tagButton.setOnMouseEntered(e ->
                tagButton.setStyle(
                        "-fx-background-color: #d0d0d0; " +
                                "-fx-background-radius: 15; " +
                                "-fx-padding: 5 10 5 10; " +
                                "-fx-cursor: hand;"
                )
        );
        tagButton.setOnMouseExited(e ->
                tagButton.setStyle(
                        "-fx-background-color: #e0e0e0; " +
                                "-fx-background-radius: 15; " +
                                "-fx-padding: 5 10 5 10; " +
                                "-fx-cursor: hand;"
                )
        );

        // Remove tag on click
        tagButton.setOnAction(e -> {
            selectedGenres.remove(genre);
            selectedGenresPane.getChildren().remove(tagButton);
        });

        selectedGenresPane.getChildren().add(tagButton);
    }

    /**
     * Handles the material update action.
     * It validates the input fields, updates the {@code currentMaterial} object,
     * updates the material record in the database, and synchronizes the {@code MaterialGenre}
     * associations (deleting old ones and inserting new ones).
     */
    @FXML
    private void handleUpdateMaterial() {
        if (currentMaterial == null) {
            showAlert(Alert.AlertType.ERROR, "No material to update");
            return;
        }

        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Title is required");
            return;
        }

        MaterialType mt = materialTypeComboBox.getValue();
        if (mt == null) {
            showAlert(Alert.AlertType.WARNING, "Material type is required");
            return;
        }

        // Update material fields
        currentMaterial.setTitle(title);
        currentMaterial.setAuthor(authorField.getText().trim());
        currentMaterial.setISBN(isbnField.getText().trim());

        // Year validation
        String ytxt = yearField.getText().trim();
        if (ytxt.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Year is required");
            return;
        }
        try {
            int year = Integer.parseInt(ytxt);
            currentMaterial.setYear(year);
        } catch (NumberFormatException nfe) {
            showAlert(Alert.AlertType.WARNING, "Year must be an integer");
            return;
        }

        currentMaterial.setIdMaterialType(mt.getIdMaterialType());

        try {
            // Update material
            materialDAO.update(currentMaterial);

            // Delete existing genre associations
            MaterialGenre deleteCriteria = new MaterialGenre(currentMaterial.getIdMaterial(), -1);
            List<MaterialGenre> existingGenres = materialGenreDAO.select(deleteCriteria);
            for (MaterialGenre mg : existingGenres) {
                materialGenreDAO.delete(mg);
            }

            // Insert new genre associations
            if (!selectedGenres.isEmpty()) {
                for (Genre g : selectedGenres) {
                    MaterialGenre mg = new MaterialGenre(currentMaterial.getIdMaterial(), g.getIdGenre());
                    materialGenreDAO.insert(mg);
                }
            }

            showAlert(Alert.AlertType.INFORMATION, "Material updated successfully");

            // Close the dialog
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.close();

        } catch (DAOException ex) {
            showAlert(Alert.AlertType.ERROR, "DB error: " + ex.getMessage());
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Closes the current dialog stage.
     */
    private void navigateBack() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }


    /**
     * Displays an alert dialog to the user.
     *
     * @param type The type of alert (e.g., WARNING, ERROR, INFORMATION).
     * @param message The message content of the alert.
     */
    private void showAlert(Alert.AlertType type, String message) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
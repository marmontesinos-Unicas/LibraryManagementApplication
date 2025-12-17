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
 * Controller class for the "Add Material" dialog, used by the administrator
 * to create a new material record, including its title, author, ISBN, year,
 * material type, and associated genres.
 * <p>
 * Handles form validation, genre selection using a search/tagging mechanism,
 * and calls the {@code MaterialService} for insertion (material + genres).
 * </p>
 */
public class AddMaterialController {

    // --- FXML Input Fields ---
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField isbnField;
    @FXML private TextField yearField;
    @FXML private ComboBox<MaterialType> materialTypeComboBox;

    // Error labels
    @FXML private Label titleErrorLabel;
    @FXML private Label yearErrorLabel;
    @FXML private Label isbnErrorLabel;
    @FXML private Label materialTypeErrorLabel;

    // New Genre UI Components
    @FXML private TextField genreSearchField;
    @FXML private ListView<Genre> genreSearchResultsList;
    @FXML private FlowPane selectedGenresPane;

    private Stage dialogStage;

    private final DAO<Material> materialDAO = MaterialDAOMySQLImpl.getInstance();
    private final MaterialService materialService = new MaterialService(materialDAO);
    private final MaterialTypeDAOMySQLImpl materialTypeDAO = MaterialTypeDAOMySQLImpl.getInstance();
    private final GenreDAO genreDAO = GenreDAOMySQLImpl.getInstance();
    private final DAO<MaterialGenre> materialGenreDAO = MaterialGenreDAOMySQLImpl.getInstance();

    private List<Genre> allGenres = new ArrayList<>();
    private Set<Genre> selectedGenres = new HashSet<>();

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Initializes the controller after its root element has been completely processed.
     */
    @FXML
    public void initialize() {
        System.out.println("AddMaterialController.initialize() called");

        loadMaterialTypes();
        loadGenres();
        setupGenreSearch();
        setupFieldValidation();
    }

    /**
     * Loads available material types from the database and populates the ComboBox.
     */
    private void loadMaterialTypes() {
        List<MaterialType> types = materialTypeDAO.selectAll();
        ObservableList<MaterialType> obs = FXCollections.observableArrayList(types);
        materialTypeComboBox.setItems(obs);
    }

    /**
     * Loads all existing genres from the database into an internal list for searching.
     */
    private void loadGenres() {
        allGenres = genreDAO.selectAll();
    }

    /**
     * Sets up listeners to clear validation error messages dynamically as the user interacts with the fields.
     */
    private void setupFieldValidation() {
        // Clear error when user types in title field
        titleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                clearFieldError(titleField, titleErrorLabel);
            }
        });

        // Clear error when user types in year field
        yearField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                clearFieldError(yearField, yearErrorLabel);
            }
        });

        // Clear error when user types in ISBN field
        isbnField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                clearFieldError(isbnField, isbnErrorLabel);
            }
        });

        // Clear error when user selects material type
        materialTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                clearFieldError(materialTypeComboBox, materialTypeErrorLabel);

                // Auto-clear ISBN if material type is not "Book"
                if (!isBookType(newVal)) {
                    isbnField.clear();
                }
            }
        });
    }

    /**
     * Configures the genre search field and results list behavior (filtering, selection, hiding).
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

        // Setup focus listener
        genreSearchField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                new Thread(() -> {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    javafx.application.Platform.runLater(() -> {
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
     * Filters the genre list based on the user's query and updates the search results list.
     *
     * @param query The text to filter genres by.
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
     * Adds a selected {@code Genre} to the list of selected genres and displays it as a removable tag.
     *
     * @param genre The genre to add as a tag.
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

        tagButton.setOnAction(e -> {
            selectedGenres.remove(genre);
            selectedGenresPane.getChildren().remove(tagButton);
        });

        selectedGenresPane.getChildren().add(tagButton);
    }

    /**
     * Handles the save operation: validates input, creates the Material, inserts it,
     * and inserts the associated MaterialGenre links.
     *
     * Access Keyword Explanation: {@code private @FXML} - Event handler for the Save button.
     */
    @FXML
    private void handleSaveMaterial() {
        // Clear all previous errors
        clearAllErrors();

        boolean hasErrors = false;

        // Validate title
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            setFieldError(titleField, titleErrorLabel, "Title is required");
            hasErrors = true;
        }

        // Validate year
        String ytxt = yearField.getText().trim();
        if (ytxt.isEmpty()) {
            setFieldError(yearField, yearErrorLabel, "Year is required");
            hasErrors = true;
        } else {
            try {
                Integer.parseInt(ytxt);
            } catch (NumberFormatException nfe) {
                setFieldError(yearField, yearErrorLabel, "Year must be a valid number");
                hasErrors = true;
            }
        }

        // Validate material type
        MaterialType mt = materialTypeComboBox.getValue();
        if (mt == null) {
            setFieldError(materialTypeComboBox, materialTypeErrorLabel, "Material type is required");
            hasErrors = true;
        }

        // Validate ISBN - required only for books
        String isbn = isbnField.getText().trim();
        if (mt != null && isBookType(mt)) {
            if (isbn.isEmpty()) {
                setFieldError(isbnField, isbnErrorLabel, "ISBN is required for books");
                hasErrors = true;
            }
        }

        // If there are validation errors, stop here
        if (hasErrors) {
            return;
        }

        // Create and save material
        Material m = new Material();
        m.setTitle(title);
        m.setAuthor(authorField.getText().trim());
        // Only set ISBN if material type is book
        if (isBookType(mt)) {
            m.setISBN(isbn);
        } else {
            m.setISBN(""); // Empty string for non-books
        }
        m.setYear(Integer.parseInt(ytxt));
        m.setIdMaterialType(mt.getIdMaterialType());

        try {
            // Save material
            materialService.save(m);
            int newId = m.getIdMaterial();

            // Save selected genres
            if (!selectedGenres.isEmpty()) {
                for (Genre g : selectedGenres) {
                    MaterialGenre mg = new MaterialGenre(newId, g.getIdGenre());
                    materialGenreDAO.insert(mg);
                }
            }

            show(Alert.AlertType.INFORMATION, "Material saved successfully");
            clearForm();

            // Close the dialog after successful save
            if (dialogStage != null) {
                dialogStage.close();
            }

        } catch (DAOException ex) {
            show(Alert.AlertType.ERROR, "Database error: " + ex.getMessage());
        } catch (Exception ex) {
            show(Alert.AlertType.ERROR, "Unexpected error: " + ex.getMessage());
        }
    }

    /**
     * Checks if the given material type represents a book.
     */
    private boolean isBookType(MaterialType mt) {
        if (mt == null) return false;
        // Check if the material type name is "Book" (case-insensitive)
        String typeName = mt.getMaterial_type();
        return typeName != null && typeName.equalsIgnoreCase("Book");
    }


    /**
     * Applies standard error styling (red border) to a control and displays the error message.
     */
    private void setFieldError(Control field, Label errorLabel, String message) {
        // Set red border on field
        field.setStyle("-fx-border-color: red; -fx-border-width: 2px;");

        // Show error message
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    /**
     * Removes standard error styling from a control and hides the error message.
     */
    private void clearFieldError(Control field, Label errorLabel) {
        // Remove red border
        field.setStyle("");

        // Hide error message
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    /**
     * Clears all validation error indicators across the form.
     */
    private void clearAllErrors() {
        clearFieldError(titleField, titleErrorLabel);
        clearFieldError(yearField, yearErrorLabel);
        clearFieldError(isbnField, isbnErrorLabel);
        clearFieldError(materialTypeComboBox, materialTypeErrorLabel);
    }

    /**
     * Handles the cancel button action, confirming with the user if there are unsaved changes.
     * <p>Access Keyword Explanation: {@code private @FXML} - Event handler for the Cancel button.</p>
     * @param event The action event.
     */
    @FXML
    private void handleCancel(ActionEvent event) {

        boolean hasUnsavedData =
                !titleField.getText().trim().isEmpty() ||
                        !authorField.getText().trim().isEmpty() ||
                        !isbnField.getText().trim().isEmpty() ||
                        !yearField.getText().trim().isEmpty() ||
                        materialTypeComboBox.getValue() != null ||
                        !selectedGenres.isEmpty();

        if (!hasUnsavedData) {
            closeDialog();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("You have unsaved changes. Are you sure you want to cancel?");

        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType no  = new ButtonType("No",  ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yes, no);

        // IMPORTANT: make it modal to the add-material dialog
        if (dialogStage != null) {
            alert.initOwner(dialogStage);
        }

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.YES) {
            closeDialog();
        }
    }


    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    /**
     * Resets all form fields and error indicators to their initial state.
     */
    private void clearForm() {
        titleField.clear();
        authorField.clear();
        isbnField.clear();
        yearField.clear();
        materialTypeComboBox.getSelectionModel().clearSelection();
        genreSearchField.clear();
        selectedGenres.clear();
        selectedGenresPane.getChildren().clear();
        clearAllErrors();
    }

    /**
     * Utility method to display an alert message.
     */
    private void show(Alert.AlertType type, String message) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
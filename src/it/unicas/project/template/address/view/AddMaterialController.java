// File: AddMaterialController.java (UPDATED)
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
import it.unicas.project.template.address.service.MaterialService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent; // Not strictly needed, but kept for compatibility
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox; // Needed for the genre tags (assuming they use HBox)
import javafx.stage.Stage; // NEW IMPORT: Required for dialog management

import java.util.*;
import java.util.logging.Logger;

public class AddMaterialController {

    private static final Logger logger = Logger.getLogger(AddMaterialController.class.getName());

    // FXML Fields
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField yearField;
    @FXML private TextField isbnField;
    @FXML private ComboBox<MaterialType> materialTypeComboBox;
    @FXML private TextField genreSearchField;
    @FXML private ListView<Genre> genreSearchResultsList;
    @FXML private FlowPane selectedGenresPane;
    @FXML private Label messageLabel;

    // FXML Error Labels
    @FXML private Label titleErrorLabel;
    @FXML private Label authorErrorLabel;
    @FXML private Label yearErrorLabel;
    @FXML private Label isbnErrorLabel;
    @FXML private Label materialTypeErrorLabel;

    // --- DIALOG/NAVIGATION FIELDS (UPDATED) ---
    // private MainApp mainApp; // REMOVED: No longer needed for scene navigation
    private Stage dialogStage; // NEW: Reference to the dialog stage
    // ------------------------------------------

    private ObservableList<MaterialType> materialTypes = FXCollections.observableArrayList();
    private ObservableList<Genre> allGenres = FXCollections.observableArrayList();
    private ObservableList<Genre> filteredGenres = FXCollections.observableArrayList();
    private Set<Integer> selectedGenres = new HashSet<>();

    @FXML
    private void initialize() {
        // ... (existing initialization logic) ...

        // Load Material Types
        try {
            List<MaterialType> types = MaterialTypeDAOMySQLImpl.getInstance().selectAll();
            materialTypes.addAll(types);
            materialTypeComboBox.setItems(materialTypes);
        } catch (Exception e) {
            logger.severe("Error loading material types: " + e.getMessage());
            show(Alert.AlertType.ERROR, "Error loading material types.");
        }

        // Load Genres
        GenreDAO genreDAO = GenreDAOMySQLImpl.getInstance();
        allGenres.addAll(genreDAO.selectAll());
        filteredGenres.addAll(allGenres);
        genreSearchResultsList.setItems(filteredGenres);

        // Setup Genre Search Listener
        genreSearchField.textProperty().addListener((obs, oldText, newText) -> {
            filterGenres(newText);
        });

        // Setup Genre List Selection Handler
        genreSearchResultsList.setCellFactory(lv -> {
            ListCell<Genre> cell = new ListCell<>() {
                @Override
                protected void updateItem(Genre item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.getGenre());
                }
            };
            cell.setOnMouseClicked(event -> {
                if (! cell.isEmpty()) {
                    Genre genre = cell.getItem();
                    addGenreToSelected(genre);
                    genreSearchField.clear(); // Clear search and hide list after selection
                }
            });
            return cell;
        });

        // Show/Hide search results list based on focus and content
        genreSearchField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused && !filteredGenres.isEmpty()) {
                genreSearchResultsList.setVisible(true);
                genreSearchResultsList.setManaged(true);
            } else if (!isFocused) {
                // Delay hiding to allow click event to register
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> {
                            if (!genreSearchResultsList.isHover()) {
                                genreSearchResultsList.setVisible(false);
                                genreSearchResultsList.setManaged(false);
                            }
                        });
                    }
                }, 150); // 150ms delay
            }
        });
    }

    // --- DIALOG SETUP METHODS (NEW) ---

    /**
     * Sets the stage of this dialog. Called by MainApp.
     * @param dialogStage The stage of the Add Material dialog.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    // public void setMainApp(MainApp mainApp) { this.mainApp = mainApp; } // REMOVED

    // ----------------------------------

    /**
     * Filters the list of available genres based on the search text.
     * @param searchText The text to search for.
     */
    private void filterGenres(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            filteredGenres.setAll(allGenres.filtered(g -> !selectedGenres.contains(g.getIdGenre())));
            genreSearchResultsList.setVisible(false);
            genreSearchResultsList.setManaged(false);
        } else {
            String lowerCaseFilter = searchText.toLowerCase().trim();
            ObservableList<Genre> newFilteredList = FXCollections.observableArrayList();
            for (Genre genre : allGenres) {
                // Filter by search text AND ensure it's not already selected
                if (genre.getGenre().toLowerCase().contains(lowerCaseFilter) && !selectedGenres.contains(genre.getIdGenre())) {
                    newFilteredList.add(genre);
                }
            }
            filteredGenres.setAll(newFilteredList);
            genreSearchResultsList.setVisible(!filteredGenres.isEmpty());
            genreSearchResultsList.setManaged(!filteredGenres.isEmpty());
        }
    }

    /**
     * Adds a selected genre to the FlowPane as a removable tag.
     * @param genre The Genre object to add.
     */
    private void addGenreToSelected(Genre genre) {
        if (!selectedGenres.contains(genre.getIdGenre())) {
            selectedGenres.add(genre.getIdGenre());

            Label tagLabel = new Label(genre.getGenre());
            tagLabel.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 3 8 3 8; -fx-background-radius: 10;");

            Button removeButton = new Button("x");
            removeButton.setStyle("-fx-background-color: transparent; -fx-padding: 0 0 0 5; -fx-font-size: 10px;");

            HBox tagContainer = new HBox(tagLabel, removeButton);
            tagContainer.setSpacing(0);
            tagContainer.setStyle("-fx-alignment: center;");

            removeButton.setOnAction(e -> {
                selectedGenresPane.getChildren().remove(tagContainer);
                selectedGenres.remove(genre.getIdGenre());
                filterGenres(genreSearchField.getText()); // Refresh filtered list
            });

            selectedGenresPane.getChildren().add(tagContainer);
            filterGenres(genreSearchField.getText()); // Refresh filtered list
        }
    }


    /**
     * Handles the action for the "Save Material" button.
     * Inserts the material into the database and closes the dialog on success.
     */
    @FXML
    private void handleSave() {
        if (isInputValid()) {
            try {
                // 1. Create and Save Material
                Material newMaterial = new Material(
                        null,
                        titleField.getText(),
                        authorField.getText(),
                        Integer.parseInt(yearField.getText()),
                        isbnField.getText(),
                        materialTypeComboBox.getSelectionModel().getSelectedItem().getIdMaterialType(),
                        "Available" // Assuming new materials start as 'Available'
                );

                MaterialService materialService = new MaterialService();
                materialService.insertNewMaterialWithGenres(newMaterial, new ArrayList<>(selectedGenres));

                // If save is successful:
                show(Alert.AlertType.INFORMATION, "Material saved successfully!");
                clearForm();

                // Close the dialog after successful save.
                if (dialogStage != null) {
                    dialogStage.close();
                }

            } catch (NumberFormatException e) {
                show(Alert.AlertType.ERROR, "Internal Error: Year is not a valid number.");
            } catch (DAOException e) {
                show(Alert.AlertType.ERROR, "Database Error: Failed to save material.\n" + e.getMessage());
                logger.severe("DAOException during save: " + e.getMessage());
            }
        } else {
            messageLabel.setText("Please correct the errors marked in red.");
        }
    }

    /**
     * Handles the action for the "Cancel" button.
     * Checks for unsaved changes and prompts the user before closing the dialog.
     */
    @FXML
    private void handleCancel() {
        boolean hasUnsavedData =
                (titleField.getText() != null && !titleField.getText().isEmpty()) ||
                        (authorField.getText() != null && !authorField.getText().isEmpty()) ||
                        (yearField.getText() != null && !yearField.getText().isEmpty()) ||
                        (isbnField.getText() != null && !isbnField.getText().isEmpty()) ||
                        (materialTypeComboBox.getSelectionModel().getSelectedItem() != null) ||
                        !selectedGenres.isEmpty();

        if (hasUnsavedData) {
            // Show confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Cancel Confirmation");
            alert.setHeaderText(null);
            alert.setContentText("You have unsaved changes. Are you sure you want to cancel and close the dialog?");

            // Note: Use ButtonBar.ButtonData for correct button ordering/styling
            ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
            ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(yes, no);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == yes) {
                if (dialogStage != null) {
                    dialogStage.close();
                }
            }
        } else {
            if (dialogStage != null) {
                dialogStage.close();
            }
        }
    }

    // @FXML protected void handleGoBack(ActionEvent event) { ... } // REMOVED
    // private void navigateBack() { ... } // REMOVED

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

    private void show(Alert.AlertType type, String message) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    /**
     * Validates the user input fields.
     * @return true if the input is valid, false otherwise.
     */
    private boolean isInputValid() {
        clearAllErrors();
        boolean isValid = true;
        String errorMessage = "";

        // Validate Title
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            titleErrorLabel.setText("Title cannot be empty.");
            titleErrorLabel.setVisible(true);
            isValid = false;
        }

        // Validate Author
        if (authorField.getText() == null || authorField.getText().trim().isEmpty()) {
            authorErrorLabel.setText("Author cannot be empty.");
            authorErrorLabel.setVisible(true);
            isValid = false;
        }

        // Validate Year
        String yearText = yearField.getText();
        if (yearText == null || yearText.trim().isEmpty()) {
            yearErrorLabel.setText("Year cannot be empty.");
            yearErrorLabel.setVisible(true);
            isValid = false;
        } else {
            try {
                int year = Integer.parseInt(yearText.trim());
                if (year <= 0) {
                    yearErrorLabel.setText("Year must be a positive number.");
                    yearErrorLabel.setVisible(true);
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                yearErrorLabel.setText("Invalid year format.");
                yearErrorLabel.setVisible(true);
                isValid = false;
            }
        }

        // Validate Material Type
        if (materialTypeComboBox.getSelectionModel().getSelectedItem() == null) {
            materialTypeErrorLabel.setText("Please select a material type.");
            materialTypeErrorLabel.setVisible(true);
            isValid = false;
        }

        // ISBN validation is usually optional or checked for length/format if provided, 
        // but assuming it's optional based on prompt text: (optional)

        return isValid;
    }

    /**
     * Clears all error labels.
     */
    private void clearAllErrors() {
        titleErrorLabel.setVisible(false);
        authorErrorLabel.setVisible(false);
        yearErrorLabel.setVisible(false);
        isbnErrorLabel.setVisible(false);
        materialTypeErrorLabel.setVisible(false);
        messageLabel.setText(""); // Also clear the general message
    }
}
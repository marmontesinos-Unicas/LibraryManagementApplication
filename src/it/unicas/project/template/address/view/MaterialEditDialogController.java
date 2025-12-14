package it.unicas.project.template.address.view;

import it.unicas.project.template.address.model.Genre;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.MaterialInventory;
import it.unicas.project.template.address.model.MaterialType;
import it.unicas.project.template.address.model.MaterialGenre;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.MaterialDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.MaterialTypeDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.GenreDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.MaterialGenreDAOMySQLImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaterialEditDialogController {

    @FXML private TextField materialTypeField;
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField yearField;
    @FXML private TextField isbnField;
    @FXML private TextField statusField;
    @FXML private TextField quantityField;
    @FXML private Label errorMessageLabel;
    @FXML private TextField genreSearchField;
    @FXML private ListView<Genre> genreSearchResultsList;
    @FXML private FlowPane selectedGenresPane;

    private ObservableList<Genre> allGenres = FXCollections.observableArrayList();
    private ObservableList<Genre> filteredGenres = FXCollections.observableArrayList();
    private Set<Integer> selectedGenreIds = new HashSet<>();

    private Stage dialogStage;
    private MaterialInventory selectedMaterialInventory;
    private MaterialManagementController materialManagementController; // For refreshing the table

    // Static mapping of MaterialType IDs to names (for display purposes)
    private ObservableList<MaterialType> materialTypes = FXCollections.observableArrayList();
    private String materialTypeName;

    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        // Load material types for local lookup (improves performance over repeated DB calls)
        materialTypes.addAll(MaterialTypeDAOMySQLImpl.getInstance().selectAll());

        // --- Genre Initialization Logic ---
        allGenres.addAll(GenreDAOMySQLImpl.getInstance().selectAll());
        genreSearchResultsList.setItems(filteredGenres);

        genreSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterGenres(newVal);
            boolean shouldShow = !newVal.trim().isEmpty() && !filteredGenres.isEmpty();
            genreSearchResultsList.setVisible(shouldShow);
            genreSearchResultsList.setManaged(shouldShow);
        });

        genreSearchResultsList.setOnMouseClicked(event -> {
            Genre selected = genreSearchResultsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                addGenreTag(selected);
                genreSearchField.clear();
                genreSearchResultsList.getSelectionModel().clearSelection();
            }
        });
    }

    /**
     * Sets the stage of this dialog.
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Sets the MaterialManagementController reference for data refresh.
     */
    public void setMaterialManagementController(MaterialManagementController controller) {
        this.materialManagementController = controller;
    }

    /**
     * Sets the material to be edited in the dialog.
     */
    public void setSelectedMaterialInventory(MaterialInventory materialInventory) {
        this.selectedMaterialInventory = materialInventory;

        String materialTypeName = materialTypes.stream()
                .filter(mt -> mt.getIdMaterialType().equals(materialInventory.getIdMaterialType()))
                .map(MaterialType::getMaterial_type)
                .findFirst().orElse("Unknown");

        materialTypeField.setText(materialTypeName);
        titleField.setText(materialInventory.getTitle());
        authorField.setText(materialInventory.getAuthor());
        yearField.setText(String.valueOf(materialInventory.getYear()));
        isbnField.setText(materialInventory.getISBN());

        // Fields set as read-only in FXML and filled for context
        statusField.setText(materialInventory.getMaterial_status());
        quantityField.setText(String.valueOf(materialInventory.getQuantity()));

        // Disable ISBN field if not a Book (assuming ID 1 is Book)
        if (materialInventory.getIdMaterialType() != 1) {
            isbnField.setDisable(true);
            isbnField.setPromptText("Only editable for Books");
        }

        // --- LOAD EXISTING GENRES ---
        loadExistingGenres();
    }

    // --- Genre Helper Methods (copied from AddMaterialController logic) ---
    private void loadExistingGenres() {
        Integer representativeId = selectedMaterialInventory.getIdMaterial();
        if (representativeId == null || representativeId <= 0) return;

        try {
            // Create a MaterialGenre pattern to search for associations by Material ID
            MaterialGenre searchPattern = new MaterialGenre(representativeId, -1);
            List<MaterialGenre> materialGenres = ((MaterialGenreDAOMySQLImpl) MaterialGenreDAOMySQLImpl.getInstance()).select(searchPattern);

            for (MaterialGenre mg : materialGenres) {
                allGenres.stream()
                        .filter(g -> g.getIdGenre().equals(mg.getIdGenre()))
                        .findFirst()
                        .ifPresent(this::addGenreTag);
            }
        } catch (DAOException e) {
            errorMessageLabel.setText("Database Error: Failed to load existing genres.");
        }
    }

    private void filterGenres(String searchText) {
        filteredGenres.clear();
        String lowerCaseSearch = searchText.toLowerCase();

        for (Genre genre : allGenres) {
            // Only show genres that start with the text and are not already selected
            if (genre.getGenre().toLowerCase().startsWith(lowerCaseSearch) && !selectedGenreIds.contains(genre.getIdGenre())) {
                filteredGenres.add(genre);
            }
        }
    }

    private void addGenreTag(Genre genre) {
        if (!selectedGenreIds.contains(genre.getIdGenre())) {
            selectedGenreIds.add(genre.getIdGenre());

            Label tagLabel = new Label(genre.getGenre());
            tagLabel.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 3 8 3 8; -fx-background-radius: 5;");

            Button removeButton = new Button("X");
            removeButton.setStyle("-fx-background-color: transparent; -fx-padding: 0 0 0 5; -fx-font-size: 10px;");

            HBox tagContainer = new HBox(5, tagLabel, removeButton);
            tagContainer.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 2;");

            removeButton.setOnAction(e -> {
                selectedGenresPane.getChildren().remove(tagContainer);
                selectedGenreIds.remove(genre.getIdGenre());
                filterGenres(genreSearchField.getText());
            });

            selectedGenresPane.getChildren().add(tagContainer);
        }
    }

    /**
     * Called when the user clicks save.
     */
    @FXML
    private void handleSave() {
        if (isInputValid()) {

            Material updatedMaterial = new Material(
                    selectedMaterialInventory.getIdMaterial(),
                    titleField.getText(),
                    authorField.getText(),
                    Integer.parseInt(yearField.getText()),
                    isbnField.getText(),
                    selectedMaterialInventory.getIdMaterialType(),
                    selectedMaterialInventory.getMaterial_status()
            );

            try {
                // 1. Update the Material Group metadata
                ((MaterialDAOMySQLImpl) MaterialDAOMySQLImpl.getInstance()).updateMaterialGroup(updatedMaterial, selectedMaterialInventory);

                // 2. Update Genres for the representative material ID
                updateMaterialGenres(selectedMaterialInventory.getIdMaterial());

                // 3. Success and close
                dialogStage.close();

                // 4. Refresh the management table
                if (materialManagementController != null) {
                    materialManagementController.loadMaterialData();
                }

            } catch (DAOException e) {
                errorMessageLabel.setText("Database Error: Failed to update material group or genres.\n" + e.getMessage());
                new Alert(Alert.AlertType.ERROR, "Database Error: " + e.getMessage()).showAndWait();
            } catch (NumberFormatException e) {
                errorMessageLabel.setText("Error: Year must be a valid number.");
            }
        }
    }

    /**
     * Deletes all current genres for the material and inserts the new list.
     */
    private void updateMaterialGenres(Integer materialId) throws DAOException {
        MaterialGenreDAOMySQLImpl mgDAO = (MaterialGenreDAOMySQLImpl) MaterialGenreDAOMySQLImpl.getInstance();

        // Delete all old genres for the representative material ID
        mgDAO.deleteAllByMaterialId(materialId);

        // Insert new selected genres
        for (Integer genreId : selectedGenreIds) {
            MaterialGenre newMg = new MaterialGenre(materialId, genreId);
            mgDAO.insert(newMg);
        }
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Validates the user input in the text fields.
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (titleField.getText() == null || titleField.getText().length() == 0) {
            errorMessage += "No valid title!\n";
        }

        try {
            int year = Integer.parseInt(yearField.getText());
            if (year < 1000 || year > 2100) { // Simple year check
                errorMessage += "Year must be between 1000 and 2100.\n";
            }
        } catch (NumberFormatException e) {
            errorMessage += "Year must be a number!\n";
        }

        // For books (idMaterialType = 1), ISBN is crucial for grouping/inventory
        if (selectedMaterialInventory.getIdMaterialType() == 1) {
            if (isbnField.getText() == null || isbnField.getText().length() == 0) {
                errorMessage += "ISBN is required for Books!\n";
            }
        }

        if (errorMessage.length() == 0) {
            errorMessageLabel.setText("");
            return true;
        } else {
            errorMessageLabel.setText("Please correct invalid fields:\n" + errorMessage);
            return false;
        }
    }
}
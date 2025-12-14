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

public class AddMaterialController {

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField isbnField;
    @FXML private TextField yearField;
    @FXML private ComboBox<MaterialType> materialTypeComboBox;

    // Error labels
    @FXML private Label titleErrorLabel;
    @FXML private Label yearErrorLabel;
    @FXML private Label materialTypeErrorLabel;

    // New Genre UI Components
    @FXML private TextField genreSearchField;
    @FXML private ListView<Genre> genreSearchResultsList;
    @FXML private FlowPane selectedGenresPane;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

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

    @FXML
    public void initialize() {
        System.out.println("AddMaterialController.initialize() called");

        loadMaterialTypes();
        loadGenres();
        setupGenreSearch();
        setupFieldValidation();
    }

    private void loadMaterialTypes() {
        List<MaterialType> types = materialTypeDAO.selectAll();
        ObservableList<MaterialType> obs = FXCollections.observableArrayList(types);
        materialTypeComboBox.setItems(obs);
    }

    private void loadGenres() {
        allGenres = genreDAO.selectAll();
    }

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

        // Clear error when user selects material type
        materialTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                clearFieldError(materialTypeComboBox, materialTypeErrorLabel);
            }
        });
    }

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

        // If there are validation errors, stop here
        if (hasErrors) {
            return;
        }

        // Create and save material
        Material m = new Material();
        m.setTitle(title);
        m.setAuthor(authorField.getText().trim());
        m.setISBN(isbnField.getText().trim());
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

    private void clearAllErrors() {
        clearFieldError(titleField, titleErrorLabel);
        clearFieldError(yearField, yearErrorLabel);
        clearFieldError(materialTypeComboBox, materialTypeErrorLabel);
    }

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
}
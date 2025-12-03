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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

public class AddMaterialController {

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField isbnField;
    @FXML private TextField yearField;
    @FXML private ComboBox<MaterialType> materialTypeComboBox;

    // New Genre UI Components
    @FXML private TextField genreSearchField;
    @FXML private ListView<Genre> genreSearchResultsList;
    @FXML private FlowPane selectedGenresPane;

    private MainApp mainApp;

    private final DAO<Material> materialDAO = MaterialDAOMySQLImpl.getInstance();
    private final MaterialService materialService = new MaterialService(materialDAO);
    private final MaterialTypeDAOMySQLImpl materialTypeDAO = MaterialTypeDAOMySQLImpl.getInstance();
    private final GenreDAO genreDAO = GenreDAOMySQLImpl.getInstance();
    private final DAO<MaterialGenre> materialGenreDAO = MaterialGenreDAOMySQLImpl.getInstance();

    private List<Genre> allGenres = new ArrayList<>();
    private Set<Genre> selectedGenres = new HashSet<>();

    @FXML
    public void initialize() {
        System.out.println("AddMaterialController.initialize() called");

        loadMaterialTypes();
        loadGenres();
        setupGenreSearch();
    }

    private void loadMaterialTypes() {
        List<MaterialType> types = materialTypeDAO.selectAll();
        ObservableList<MaterialType> obs = FXCollections.observableArrayList(types);
        materialTypeComboBox.setItems(obs);
    }

    private void loadGenres() {
        allGenres = genreDAO.selectAll();
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

        // Handle genre selection from list - use cell factory for better click handling
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

        // Setup focus listener to hide results when focus is lost
        genreSearchField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                // Delay hiding to allow click on list item
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

        // Filter genres that match the search and aren't already selected
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
            return; // Already selected
        }

        selectedGenres.add(genre);

        // Create tag button
        Button tagButton = new Button("× " + genre.getGenre());
        tagButton.setStyle(
                "-fx-background-color: #e0e0e0; " +
                        "-fx-background-radius: 15; " +
                        "-fx-padding: 5 10 5 10; " +
                        "-fx-cursor: hand;"
        );

        // Hover effect
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

        // Remove genre when clicked
        tagButton.setOnAction(e -> {
            selectedGenres.remove(genre);
            selectedGenresPane.getChildren().remove(tagButton);
        });

        selectedGenresPane.getChildren().add(tagButton);
    }

    @FXML
    private void handleSaveMaterial() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            show(Alert.AlertType.WARNING, "Title is required");
            return;
        }

        MaterialType mt = materialTypeComboBox.getValue();
        if (mt == null) {
            show(Alert.AlertType.WARNING, "Material type is required");
            return;
        }

        Material m = new Material();
        m.setTitle(title);
        m.setAuthor(authorField.getText().trim());
        m.setISBN(isbnField.getText().trim());

        // Year validation
        String ytxt = yearField.getText().trim();
        if (ytxt.isEmpty()) {
            show(Alert.AlertType.WARNING, "Year is required");
            return;
        }
        try {
            int year = Integer.parseInt(ytxt);
            m.setYear(year);
        } catch (NumberFormatException nfe) {
            show(Alert.AlertType.WARNING, "Year must be an integer");
            return;
        }

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

            show(Alert.AlertType.INFORMATION, "Material saved");
            clearForm();

        } catch (DAOException ex) {
            show(Alert.AlertType.ERROR, "DB error: " + ex.getMessage());
        } catch (Exception ex) {
            show(Alert.AlertType.ERROR, "Unexpected error: " + ex.getMessage());
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("You are about to exit, are you sure you don't want to save the changes?");

        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No");
        alert.getButtonTypes().setAll(yes, no);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yes) {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
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
    }

    private void show(Alert.AlertType type, String message) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
}

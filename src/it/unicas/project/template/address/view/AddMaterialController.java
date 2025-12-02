package it.unicas.project.template.address.view;

import it.unicas.project.template.address.model.Genre;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.MaterialGenre;
import it.unicas.project.template.address.model.MaterialType;
import it.unicas.project.template.address.model.dao.DAO;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.GenreDAO;
import it.unicas.project.template.address.model.dao.mysql.MaterialDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.MaterialGenreDAOMySQLImpl;
import it.unicas.project.template.address.service.MaterialService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.util.List;

public class AddMaterialController {

    @FXML private AnchorPane rootPane;

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField yearField;
    @FXML private TextField isbnField;

    @FXML private ComboBox<MaterialType> materialTypeComboBox;

    @FXML private ListView<Genre> genreListView;  // <-- NUEVO
    @FXML private TextField newGenreField;
    @FXML private Button addGenreButton;

    // DAOs
    private final DAO<Material> materialDAO = MaterialDAOMySQLImpl.getInstance();
    private final MaterialService materialService = new MaterialService(materialDAO);

    private final DAO<MaterialGenre> materialGenreDAO = MaterialGenreDAOMySQLImpl.getInstance();
    private final MaterialTypeDAOMySQLImpl materialTypeDAO = MaterialTypeDAOMySQLImpl.getInstance();
    private final GenreDAO genreDAO = GenreDAOMySQLImpl.getInstance();

    @FXML
    public void initialize() {
        loadMaterialTypes();
        loadGenres();

        genreListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        addGenreButton.setOnAction(e -> handleAddOtherGenre());
    }

    private void loadMaterialTypes() {
        try {
            List<MaterialType> types = materialTypeDAO.selectAll();
            materialTypeComboBox.getItems().setAll(types);
        } catch (DAOException e) {
            showError("Error loading types", e.getMessage());
        }
    }

    private void loadGenres() {
        List<Genre> genres = genreDAO.selectAll();
        genreListView.getItems().setAll(genres);
    }

    private void handleAddOtherGenre() {
        String name = newGenreField.getText().trim();
        if (name.isEmpty()) return;

        try {
            Genre newGenre = new Genre(null, name);
            genreDAO.insert(newGenre);
            newGenreField.clear();
            loadGenres();

        } catch (DAOException e) {
            showError("Error Adding Genre", e.getMessage());
        }
    }

    @FXML
    private void handleSaveMaterial() {
        try {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            Integer year = Integer.parseInt(yearField.getText().trim());
            String isbn = isbnField.getText().trim();
            MaterialType selectedType = materialTypeComboBox.getValue();

            if (title.isEmpty() || selectedType == null) {
                showError("Validation Error", "Title and material type required.");
                return;
            }

            Material m = new Material(title, author, year, isbn, selectedType.getIdMaterialType(), "available");
            materialService.save(m);

            // ⬇ Guardar múltiples géneros
            List<Genre> selectedGenres = genreListView.getSelectionModel().getSelectedItems();
            for (Genre g : selectedGenres) {
                MaterialGenre mg = new MaterialGenre(m.getIdMaterial(), g.getIdGenre());
                materialGenreDAO.insert(mg);
            }

            showInfo("Success", "Material saved correctly.");
            clearForm();

        } catch (NumberFormatException e) {
            showError("Validation Error", "Year must be numeric.");
        } catch (DAOException e) {
            showError("Database Error", e.getMessage());
        }
    }

    private void clearForm() {
        titleField.clear();
        authorField.clear();
        yearField.clear();
        isbnField.clear();
        materialTypeComboBox.getSelectionModel().clearSelection();
        genreListView.getSelectionModel().clearSelection();
        newGenreField.clear();
    }

    private void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}


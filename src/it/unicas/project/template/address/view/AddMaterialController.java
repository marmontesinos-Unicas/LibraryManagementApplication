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
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class AddMaterialController {

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField isbnField;
    @FXML private TextField yearField;

    @FXML private ComboBox<MaterialType> materialTypeComboBox;
    @FXML private ListView<Genre> genreListView;

    @FXML private TextField newGenreField;
    @FXML private Button addGenreButton;


    private final DAO<Material> materialDAO = MaterialDAOMySQLImpl.getInstance();
    private final MaterialService materialService = new MaterialService(materialDAO);

    private final MaterialTypeDAOMySQLImpl materialTypeDAO = MaterialTypeDAOMySQLImpl.getInstance();
    private final GenreDAO genreDAO = GenreDAOMySQLImpl.getInstance();

    private final DAO<MaterialGenre> materialGenreDAO = MaterialGenreDAOMySQLImpl.getInstance();

    @FXML
    public void initialize() {
        // Load material types and genres
        loadMaterialTypes();
        loadGenres();

        // allow multi-selection for genres
        genreListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // simple wiring for "Add Genre" button-- optional
        addGenreButton.setOnAction(e -> handleAddGenre());
    }

    private void loadMaterialTypes() {
        List<MaterialType> types = materialTypeDAO.selectAll();
        ObservableList<MaterialType> obs = FXCollections.observableArrayList(types);
        materialTypeComboBox.setItems(obs);
    }

    private void loadGenres() {
        List<Genre> list = genreDAO.selectAll();
        ObservableList<Genre> obs = FXCollections.observableArrayList(list);
        genreListView.setItems(obs);
    }

    private void handleAddGenre() {
        String name = newGenreField.getText().trim();
        if (name.isEmpty()) {
            show(Alert.AlertType.WARNING, "Enter a genre name");
            return;
        }

        // check duplicate (GenreDAO.findIdByName should return Integer or null)
        Integer existing = genreDAO.findIdByName(name);
        if (existing != null) {
            show(Alert.AlertType.INFORMATION, "Genre already exists");
            return;
        }

        // your Genre constructor expects (Integer, String) — give a dummy id (-1)
        Genre g = new Genre(-1, name);
        try {
            genreDAO.insert(g);    // throws DAOException in your design
            newGenreField.clear();
            loadGenres();
            show(Alert.AlertType.INFORMATION, "Genre added");
        } catch (DAOException ex) {
            show(Alert.AlertType.ERROR, "DB error: " + ex.getMessage());
        }
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

        // year validation (Material.setYear expects Integer)
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
            // save via service (service calls DAO.insert and sets generated id on the model)
            materialService.save(m);

            // get generated id from material object (your DAO sets it)
            int newId = m.getIdMaterial();

            // persist selected genres into join table
            ObservableList<Genre> selected = genreListView.getSelectionModel().getSelectedItems();
            if (selected != null && !selected.isEmpty()) {
                for (Genre g : selected) {
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

    // Cancel button
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
            // Closes the window
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
        } else {
            // goes back to interface, does nothing
        }
    }

    private void clearForm() {
        titleField.clear();
        authorField.clear();
        isbnField.clear();
        yearField.clear();
        materialTypeComboBox.getSelectionModel().clearSelection();
        genreListView.getSelectionModel().clearSelection();
    }

    private void show(Alert.AlertType type, String message) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}

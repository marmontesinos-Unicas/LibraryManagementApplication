package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.MaterialInventory;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.MaterialDAOMySQLImpl;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Button; // Need to explicitly import Button if not already there
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class MaterialManagementController {

    private MainApp mainApp;

    @FXML private TextField searchField;
    @FXML private TableView<MaterialInventory> materialTable;
    @FXML private TableColumn<MaterialInventory, String> titleColumn;
    @FXML private TableColumn<MaterialInventory, String> authorColumn;
    @FXML private TableColumn<MaterialInventory, String> isbnColumn;
    @FXML private TableColumn<MaterialInventory, Integer> quantityColumn;
    @FXML private TableColumn<MaterialInventory, Integer> yearColumn;
    @FXML private TableColumn<MaterialInventory, String> materialTypeColumn;
    @FXML private TableColumn<MaterialInventory, Integer> availableCountColumn;
    @FXML private TableColumn<MaterialInventory, Integer> onHoldCountColumn;
    @FXML private TableColumn<MaterialInventory, Integer> loanedCountColumn;

    // FXML fields for buttons (must match fx:id in FXML)
    @FXML private Button viewEditMaterialButton;
    @FXML private Button backButton;
    @FXML private Button searchButton;


    private ObservableList<MaterialInventory> materialList = FXCollections.observableArrayList();

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("ISBN"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year")); // Inherited from Material
        materialTypeColumn.setCellValueFactory(new PropertyValueFactory<>("materialTypeName"));
        availableCountColumn.setCellValueFactory(new PropertyValueFactory<>("availableCount"));
        onHoldCountColumn.setCellValueFactory(new PropertyValueFactory<>("onHoldCount"));
        loanedCountColumn.setCellValueFactory(new PropertyValueFactory<>("loanedCount"));

        // 1. Disable the View/Edit button if nothing is selected (The NPE fix)
        viewEditMaterialButton.disableProperty().bind(
                Bindings.isNull(materialTable.getSelectionModel().selectedItemProperty())
        );

        loadMaterialData();

        materialTable.setItems(materialList);
        materialTable.setPlaceholder(new Label("No materials registered in the catalog."));

        searchField.setOnKeyReleased(event -> {
            if (event.getCode().equals(javafx.scene.input.KeyCode.ENTER)) {
                handleSearch();
            }
        });
    }

    public void loadMaterialData() {
        materialList.clear();
        try {
            List<MaterialInventory> materialsFromDB = ((MaterialDAOMySQLImpl) MaterialDAOMySQLImpl.getInstance()).selectAllInventory();
            materialList.addAll(materialsFromDB);
        } catch (DAOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Failed to Load Materials");
            alert.setContentText("Could not load material data from the database: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        materialList.clear();
        try {
            List<MaterialInventory> searchResults = ((MaterialDAOMySQLImpl) MaterialDAOMySQLImpl.getInstance()).selectInventoryBySearchTerm(query);
            materialList.addAll(searchResults);
        } catch (DAOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Search Failed");
            alert.setContentText("Could not execute search: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleAddMaterial() {
        if (mainApp != null) {
            mainApp.showAddMaterialView();
            loadMaterialData();
        }
    }

    @FXML
    private void handleViewEditMaterial() {
        MaterialInventory selectedMaterial = materialTable.getSelectionModel().getSelectedItem();

        if (selectedMaterial == null) {
            // Should be disabled, but defensive check
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("view/MaterialEditDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Material Group: " + selectedMaterial.getTitle());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Stage parentStage = (Stage) materialTable.getScene().getWindow();
            dialogStage.initOwner(parentStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            MaterialEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setMaterialManagementController(this);
            controller.setSelectedMaterialInventory(selectedMaterial);

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not open edit dialog");
            alert.setContentText("Check if 'MaterialEditDialog.fxml' is correctly located and accessible.\nError: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleGoBack() {
        if (mainApp != null) {
            mainApp.showAdminLanding();
        }
    }
}
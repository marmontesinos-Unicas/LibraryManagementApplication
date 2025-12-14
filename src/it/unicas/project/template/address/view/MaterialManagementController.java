package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.MaterialInventory;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.MaterialDAOMySQLImpl;

import it.unicas.project.template.address.service.SearchService;
import javafx.application.Platform;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.List;
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
    private ObservableList<MaterialInventory> allMaterialList = FXCollections.observableArrayList();
    private ObservableList<MaterialInventory> filteredList = FXCollections.observableArrayList();

    // Search service and debounce mechanism
    private final SearchService<MaterialInventory> searchService = new SearchService<>();
    private ScheduledExecutorService searchScheduler = Executors.newSingleThreadScheduledExecutor();
    private java.util.concurrent.Future<?> searchTask;
    private List<Function<MaterialInventory, String>> searchFields;
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

        materialTable.setItems(filteredList);
        materialTable.setPlaceholder(new Label("No materials registered in the catalog."));

        // Setup search fields in priority order
        searchFields = SearchService.<MaterialInventory>fieldsBuilder()
                .addField(MaterialInventory::getTitle)          // Highest priority
                .addField(MaterialInventory::getAuthor)
                .addField(m -> m.getYear() != null ? m.getYear().toString() : "")
                .addField(MaterialInventory::getISBN)           // Third priority
                .addField(m -> m.getMaterialTypeName())         // Fourth priority
                .build();

        // Add debounced live search listener (300ms delay)
        searchField.textProperty().addListener((obs, oldVal, newVal) -> scheduleSearch());
    }

    public void loadMaterialData() {
        allMaterialList.clear();
        filteredList.clear();
        try {
            List<MaterialInventory> materialsFromDB = ((MaterialDAOMySQLImpl) MaterialDAOMySQLImpl.getInstance()).selectAllInventory();
            allMaterialList.addAll(materialsFromDB);
            filteredList.addAll(materialsFromDB);
        } catch (DAOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Failed to Load Materials");
            alert.setContentText("Could not load material data from the database: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Schedule search execution after 300ms delay to prevent connection leaks
     */
    private void scheduleSearch() {
        // Cancel previous search task if still pending
        if (searchTask != null && !searchTask.isDone()) {
            searchTask.cancel(false);
        }

        // Schedule new search after 300ms delay
        searchTask = searchScheduler.schedule(() -> {
            Platform.runLater(this::performSearch);
        }, 300, TimeUnit.MILLISECONDS);
    }

    /**
     * Perform the actual search using SearchService
     */
    private void performSearch() {
        String query = searchField.getText().trim();

        if (query.isEmpty()) {
            // Show all materials if search is empty
            filteredList.setAll(allMaterialList);
        } else {
            // Use SearchService for prioritized field search
            List<MaterialInventory> results = searchService.searchAndSort(
                    allMaterialList,
                    query,
                    searchFields
            );
            filteredList.setAll(results);
        }
    }

    @FXML
    private void handleClear() {
        searchField.clear();
        filteredList.setAll(allMaterialList);
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

    /**
     * Cleanup when controller is destroyed
     */
    public void cleanup() {
        if (searchScheduler != null && !searchScheduler.isShutdown()) {
            searchScheduler.shutdown();
        }
    }

}
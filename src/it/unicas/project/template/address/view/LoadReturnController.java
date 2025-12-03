package it.unicas.project.template.address.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoadReturnController {

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private TableView<Object> loansTable;

    @FXML
    private TableColumn<Object, String> materialTypeColumn;

    @FXML
    private TableColumn<Object, String> titleColumn;

    @FXML
    private TableColumn<Object, String> userColumn;

    @FXML
    private TableColumn<Object, String> dueDateColumn;

    @FXML
    private TableColumn<Object, String> delayedColumn;

    @FXML
    private Button addLoanButton;

    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void initialize() {
        // Eliminar la columna fantasma automáticamente
        loansTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Opcional: ajustar proporciones relativas
        materialTypeColumn.setMaxWidth(1f * Integer.MAX_VALUE * 20); // 20%
        titleColumn.setMaxWidth(1f * Integer.MAX_VALUE * 30);        // 30%
        userColumn.setMaxWidth(1f * Integer.MAX_VALUE * 20);         // 20%
        dueDateColumn.setMaxWidth(1f * Integer.MAX_VALUE * 15);      // 15%
        delayedColumn.setMaxWidth(1f * Integer.MAX_VALUE * 15);      // 15%
    }

    @FXML
    private void handleSearch() {
        System.out.println("Buscar: " + searchField.getText());
    }

    @FXML
    private void handleAddLoan() {
        System.out.println("Agregar nuevo préstamo");
    }
}

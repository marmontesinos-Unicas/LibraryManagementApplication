package it.unicas.project.template.address.view;

import it.unicas.project.template.address.MainApp;
import it.unicas.project.template.address.model.Loan;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.LoanDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.MaterialDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;


import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.*;

public class LoadReturnController {

    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button returnLoanButton;

    @FXML private TableView<LoanRow> loansTable;
    @FXML private TableColumn<LoanRow, String> materialTypeColumn;
    @FXML private TableColumn<LoanRow, String> titleColumn;
    @FXML private TableColumn<LoanRow, String> userColumn;
    @FXML private TableColumn<LoanRow, String> dueDateColumn;
    @FXML private TableColumn<LoanRow, String> delayedColumn;

    private Stage dialogStage;
    private ObservableList<LoanRow> loanRows = FXCollections.observableArrayList();
    private MainApp mainApp; // NEW FIELD

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Is called by the MainApp to give a reference back to itself.
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void initialize() {
        loansTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        materialTypeColumn.setCellValueFactory(data -> data.getValue().materialTypeProperty());
        titleColumn.setCellValueFactory(data -> data.getValue().titleProperty());
        userColumn.setCellValueFactory(data -> data.getValue().userProperty());
        dueDateColumn.setCellValueFactory(data -> data.getValue().dueDateProperty());
        delayedColumn.setCellValueFactory(data -> data.getValue().delayedProperty());

        delayedColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setTextFill(item.equalsIgnoreCase("Yes") ? Color.RED : Color.BLACK);
                }
            }
        });

        loansTable.setItems(loanRows);
        loadAllLoans();

        returnLoanButton.setOnAction(e -> handleReturnLoan());

        searchField.setOnKeyReleased(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleSearch();
            }
        });
    }

    @FXML
    private void handleBackToHome() {
        if (mainApp != null) {
            mainApp.showAdminLanding();
        }
    }

    private void loadAllLoans() {
        loanRows.clear();
        try {
            List<Loan> loans = LoanDAOMySQLImpl.getInstance().select(null);
            for (Loan loan : loans) {
                if (loan.getReturn_date() == null) {
                    LoanRow row = buildLoanRow(loan);
                    if (row != null) loanRows.add(row);
                }
            }
            loanRows.sort(Comparator.comparing(LoanRow::getDueDateAsLocalDate));
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }

    private LoanRow buildLoanRow(Loan loan) throws DAOException {
        Material m = new Material(); m.setIdMaterial(loan.getIdMaterial());
        m = MaterialDAOMySQLImpl.getInstance().select(m).stream().findFirst().orElse(null);

        User u = new User(); u.setIdUser(loan.getIdUser());
        u = UserDAOMySQLImpl.getInstance().select(u).stream().findFirst().orElse(null);

        if (m == null || u == null) return null;

        String materialType = switch (m.getIdMaterialType() != null ? m.getIdMaterialType() : 0) {
            case 1 -> "Book";
            case 2 -> "CD";
            case 3 -> "Movie";
            case 4 -> "Magazine";
            default -> "Unknown";
        };

        String title = m.getTitle() != null ? m.getTitle() : "—";
        String userName = (u.getName() != null ? u.getName() : "") + " " + (u.getSurname() != null ? u.getSurname() : "");
        String due = loan.getDue_date() != null ? loan.getDue_date().toLocalDate().toString() : "—";
        boolean delayed = loan.getDue_date() != null && loan.getDue_date().isBefore(LocalDateTime.now());

        return new LoanRow(loan.getIdLoan(), materialType, title, userName, due, delayed ? "Yes" : "No");
    }

    @FXML
    private void handleSearch() {
        String text = searchField.getText().trim().toLowerCase();

        // -------------------------------------------
        // NEW: search for delayed loans by keywords
        // -------------------------------------------
        if (text.equals("delayed") || text.equals("delay") || text.equals("delays")
                || text.equals("late") || text.equals("overdue")) {

            loanRows.clear();

            try {
                List<Loan> loans = LoanDAOMySQLImpl.getInstance().select(null);

                for (Loan loan : loans) {
                    if (loan.getReturn_date() == null &&
                            loan.getDue_date() != null &&
                            loan.getDue_date().isBefore(java.time.LocalDateTime.now())) {

                        LoanRow row = buildLoanRow(loan);
                        if (row != null) loanRows.add(row);
                    }
                }

                loanRows.sort(Comparator.comparing(LoanRow::getDueDateAsLocalDate));

            } catch (DAOException e) {
                e.printStackTrace();
            }

            return;
        }

        // ----------------------------------------------------
        // If no text → show ALL active (not returned) loans
        // ----------------------------------------------------
        if (text.isEmpty()) {
            loadAllLoans();
            return;
        }

        // ----------------------------------------------------
        // Normal search (title, name, surname, material)
        // ----------------------------------------------------
        try {
            Set<Integer> userIDs = new HashSet<>();
            Set<Integer> materialIDs = new HashSet<>();

            // Search by user name
            User searchByName = new User();
            searchByName.setName(text);
            userIDs.addAll(
                    UserDAOMySQLImpl.getInstance().select(searchByName)
                            .stream().map(User::getIdUser).toList()
            );

            // Search by user surname
            User searchBySurname = new User();
            searchBySurname.setSurname(text);
            userIDs.addAll(
                    UserDAOMySQLImpl.getInstance().select(searchBySurname)
                            .stream().map(User::getIdUser).toList()
            );

            // Search by material title
            Material searchByTitle = new Material();
            searchByTitle.setTitle(text);
            materialIDs.addAll(
                    MaterialDAOMySQLImpl.getInstance().select(searchByTitle)
                            .stream().map(Material::getIdMaterial).toList()
            );

            // Apply filtering
            List<Loan> allLoans = LoanDAOMySQLImpl.getInstance().select(null);
            loanRows.clear();

            for (Loan loan : allLoans) {
                if (loan.getReturn_date() == null &&
                        (userIDs.contains(loan.getIdUser()) ||
                                materialIDs.contains(loan.getIdMaterial()))) {

                    LoanRow row = buildLoanRow(loan);
                    if (row != null) loanRows.add(row);
                }
            }

            loanRows.sort(Comparator.comparing(LoanRow::getDueDateAsLocalDate));

        } catch (DAOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void handleAddLoan() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AddLoanDialog.fxml"));
            Parent page = loader.load();

            Stage dialog = new Stage();
            dialog.setTitle("Add New Loan");
            dialog.initOwner(dialogStage);

            AddLoanController controller = loader.getController();
            controller.setDialogStage(dialog);

            Scene scene = new Scene(page);
            dialog.setScene(scene);
            dialog.showAndWait();

            loadAllLoans(); // refrescar tabla

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleReturnLoan() {
        LoanRow selected = loansTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a loan first.");
            alert.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Return Confirmation");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure " + selected.getUser() + " is returning " + selected.getTitle() + "?");

        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(yes, no);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == yes) {
            try {

                // -----------------------------------------
                // CAMBIO 1: Buscar el Loan real por title + user + dueDate
                // -----------------------------------------
                Loan filtro = new Loan();
                filtro.setReturn_date(null); // solo préstamos NO devueltos

                List<Loan> loans = LoanDAOMySQLImpl.getInstance().select(filtro);

                Loan loanReal = null;

                for (Loan loan : loans) {

                    // Obtener material
                    Material m = new Material();
                    m.setIdMaterial(loan.getIdMaterial());
                    m = MaterialDAOMySQLImpl.getInstance().select(m).stream().findFirst().orElse(null);

                    // Obtener usuario
                    User u = new User();
                    u.setIdUser(loan.getIdUser());
                    u = UserDAOMySQLImpl.getInstance().select(u).stream().findFirst().orElse(null);

                    if (m == null || u == null) continue;

                    String fullUser = u.getName() + " " + u.getSurname();

                    // Comprobar coincidencia por title + user + dueDate
                    if (m.getTitle().equals(selected.getTitle()) &&
                            fullUser.equals(selected.getUser()) &&
                            loan.getDue_date().toLocalDate().toString().equals(selected.dueDate.get())) {

                        loanReal = loan;
                        break;
                    }
                }

                if (loanReal == null) {
                    Alert error = new Alert(Alert.AlertType.ERROR, "Error: loan not found in database.");
                    error.showAndWait();
                    return;
                }

                // -----------------------------------------
                // CAMBIO 2: Actualizar el préstamo real
                // -----------------------------------------
                loanReal.setReturn_date(LocalDateTime.now());
                LoanDAOMySQLImpl.getInstance().update(loanReal);

                // -----------------------------------------
                // NUEVO: MARCAR MATERIAL COMO AVAILABLE
                // -----------------------------------------
                Material material = new Material();
                material.setIdMaterial(loanReal.getIdMaterial());
                material = MaterialDAOMySQLImpl.getInstance().select(material).get(0);

                material.setMaterial_status("available");
                MaterialDAOMySQLImpl.getInstance().update(material);


                loadAllLoans();

            } catch (DAOException e) {
                e.printStackTrace();
            }
        }
    }


    // ---------------------------
    // LoanRow inner class
    // ---------------------------
    public static class LoanRow {
        private final int idLoan;
        private final SimpleStringProperty materialType;
        private final SimpleStringProperty title;
        private final SimpleStringProperty user;
        private final SimpleStringProperty dueDate;
        private final SimpleStringProperty delayed;

        public LoanRow(int idLoan, String materialType, String title, String user, String dueDate, String delayed) {
            this.idLoan = idLoan;
            this.materialType = new SimpleStringProperty(materialType);
            this.title = new SimpleStringProperty(title);
            this.user = new SimpleStringProperty(user);
            this.dueDate = new SimpleStringProperty(dueDate);
            this.delayed = new SimpleStringProperty(delayed);
        }

        public int getIdLoan() { return idLoan; }
        public SimpleStringProperty materialTypeProperty() { return materialType; }
        public SimpleStringProperty titleProperty() { return title; }
        public SimpleStringProperty userProperty() { return user; }
        public SimpleStringProperty dueDateProperty() { return dueDate; }
        public SimpleStringProperty delayedProperty() { return delayed; }

        public String getTitle() { return title.get(); }
        public String getUser() { return user.get(); }

        public LocalDateTime getDueDateAsLocalDate() {
            if (dueDate.get() == null || dueDate.get().equals("—")) return LocalDateTime.MAX;
            return LocalDateTime.parse(dueDate.get() + "T00:00:00");
        }
    }
}

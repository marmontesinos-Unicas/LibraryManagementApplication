package it.unicas.project.template.address;

import it.unicas.project.template.address.model.Hold;
import it.unicas.project.template.address.model.Material;
import it.unicas.project.template.address.model.User;
import it.unicas.project.template.address.model.dao.DAOException;
import it.unicas.project.template.address.model.dao.mysql.HoldDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.MaterialDAOMySQLImpl;
import it.unicas.project.template.address.model.dao.mysql.UserDAOMySQLImpl;
import it.unicas.project.template.address.view.AddMaterialController;
import it.unicas.project.template.address.view.AdminLandingController;
import it.unicas.project.template.address.view.AdminLandingController;
import it.unicas.project.template.address.view.LoginDialogController;
import it.unicas.project.template.address.view.MaterialCatalogController;
import it.unicas.project.template.address.view.UserLandingController;
import it.unicas.project.template.address.view.UserManagementController;
import it.unicas.project.template.address.view.UserEditController;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.control.Alert;
import java.time.LocalDateTime;
import java.util.List;
import java.time.ZonedDateTime;
import java.time.ZoneId;


import java.io.IOException;

public class MainApp extends Application {

    private Stage primaryStage;
    private User loggedUser;

    // Listas para el usuario
    private ObservableList<String> userLoans = FXCollections.observableArrayList();
    private ObservableList<String> userReservations = FXCollections.observableArrayList();

    public ObservableList<String> getUserLoans() {
        return userLoans;
    }

    public ObservableList<String> getUserReservations() {
        return userReservations;
    }


// ------------------------------
// Dentro de tu metodo start()
// ------------------------------
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Library Management App");
        this.primaryStage.getIcons().add(new Image("file:resources/images/address_book_32.png"));

        // Limpiar holds caducados antes de mostrar cualquier vista
        try {
            cleanupExpiredHolds();
        } catch (DAOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cleanup Failed");
            alert.setContentText("Could not clean up expired holds.");
            alert.showAndWait();
        }

        boolean loggedIn = showLoginDialog();
        if (loggedIn) {
            if (loggedUser.getIdRole() == 1) {
                showAdminLanding();
            } else {
                showUserLandingView();
            }
            primaryStage.show();
        } else {
            primaryStage.close();
        }
    }

// ------------------------------
// Añadir este metodo dentro de MainApp
// ------------------------------
    /**
     * Borra los holds caducados (anteriores a ayer) y marca los materiales como available.
     */
    private void cleanupExpiredHolds() throws DAOException {
        // Hora actual en Europa/Roma
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Rome"));
        // Definimos el cutoff restando 24 horas
        LocalDateTime cutoffDate = now.minusHours(24);
        System.out.println("CutoffDate for expired holds: " + cutoffDate);

        // Obtener todos los holds
        List<Hold> allHolds = HoldDAOMySQLImpl.getInstance().select(new Hold());

        for (Hold hold : allHolds) {
            if (hold.getHold_date() != null) {
                // Ajustar la hora del hold si MySQL devuelve en UTC
                LocalDateTime holdAdjusted = hold.getHold_date().minusHours(1); // Ajusta +2 si Roma está UTC+2
                System.out.println("Checking hold " + hold.getIdHold() + " with date " + holdAdjusted);

                if (holdAdjusted.isBefore(cutoffDate)) {
                    System.out.println("Deleting hold: " + hold.getIdHold() + " with date: " + holdAdjusted);

                    // Actualizar material correspondiente a "available"
                    Material material = new Material();
                    material.setIdMaterial(hold.getIdMaterial());
                    material = MaterialDAOMySQLImpl.getInstance().select(material).stream().findFirst().orElse(null);

                    if (material != null) {
                        material.setMaterial_status("available");
                        MaterialDAOMySQLImpl.getInstance().update(material);
                    }

                    // Borrar hold caducado
                    HoldDAOMySQLImpl.getInstance().delete(hold);
                } else {
                    System.out.println("Hold " + hold.getIdHold() + " is not expired yet.");
                }
            }
        }
    }





    private boolean showLoginDialog() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/LoginDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Login");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false); // Login is kept small and fixed size

            LoginDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            if (controller.isLoginSuccessful()) {
                String username = controller.getUsername();
                loggedUser = UserDAOMySQLImpl.getInstance().getByUsername(username);

                // Inicializamos listas de préstamos y reservas del usuario
                loadUserLoans();
                loadUserReservations();

                return true;
            } else {
                return false;
            }
        } catch (IOException | DAOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void loadUserLoans() {
        // Ejemplo de datos de prueba
        userLoans.clear();
        userLoans.addAll("Préstamo 1", "Préstamo 2", "Préstamo 3");
        // Aquí puedes reemplazar por datos reales desde la BD
    }

    private void loadUserReservations() {
        // Ejemplo de datos de prueba
        userReservations.clear();
        userReservations.addAll("Reserva A", "Reserva B");
        // Aquí puedes reemplazar por datos reales desde la BD
    }

    private void showUserLandingView() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/UserLandingView.fxml"));
            BorderPane userPane = loader.load();

            Scene scene = new Scene(userPane);
            primaryStage.setScene(scene);
            primaryStage.setTitle("User Dashboard");
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(520);

            UserLandingController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showAdminLanding() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/AdminLandingView.fxml"));
            BorderPane adminPane = loader.load();

            Scene scene = new Scene(adminPane);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Admin Dashboard"); // Set title back
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(520);

            // Pass the MainApp reference to the AdminLandingController
            it.unicas.project.template.address.view.AdminLandingController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays the User Management interface.
     */
    public void showUserManagement() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/UserManagement.fxml"));
            BorderPane userManagementPane = loader.load();

            // Set the new scene on the primary stage
            Scene scene = new Scene(userManagementPane);
            primaryStage.setScene(scene);
            primaryStage.setTitle("User Management");
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(520);

            // Get the controller and initialize if needed (already done in initialize method)
            UserManagementController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
            // Show an alert if the FXML file can't be loaded
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("View Loading Failed");
            alert.setContentText("Could not load UserInterface.fxml.");
            alert.showAndWait();
        }
    }

    public void showAddMaterialView() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/AddMaterial.fxml"));
            AnchorPane page = loader.load(); // same as your working code

            Scene scene = new Scene(page);
            primaryStage.setScene(scene);

            AddMaterialController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void showCatalogView() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/MaterialCatalog.fxml"));
            AnchorPane page = loader.load();

            Scene scene = new Scene(page);
            primaryStage.setScene(scene);

            MaterialCatalogController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the user edit dialog (e.g., for creating or editing a user).
     * The dialog size is based on the FXML/content, as requested.
     * @param user the user object to be edited or created.
     * @param userManagementController the controller to refresh the table after save/delete.
     */
    public void showUserEditDialog(User user, UserManagementController userManagementController) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/UserEditDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit User Details");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);


            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            UserEditController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            controller.setSelectedUser(user);

            controller.setUserManagementController(userManagementController);

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            // Show an alert if the FXML file can't be loaded
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Dialog Loading Failed");
            alert.setContentText("Could not load UserEditDialog.fxml.");
            alert.showAndWait();
        }
    }

    /**
     * Displays the Loan and Return management interface, replacing the current scene.
     */
    public void showLoadReturn() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("/it/unicas/project/template/address/view/LoadReturn.fxml"));
            AnchorPane loadReturnPane = loader.load();

            Scene scene = new Scene(loadReturnPane);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Loan and Return Management");
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(520);

            // Get the controller and pass the MainApp reference
            it.unicas.project.template.address.view.LoadReturnController controller = loader.getController();
            controller.setMainApp(this); // THIS LINE IS CRUCIAL for the Back button functionality

        } catch (IOException e) {
            e.printStackTrace();

            // Error handling using the correctly imported Alert class
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("View Loading Failed");
            alert.setContentText("Could not load LoadReturn.fxml.");
            alert.showAndWait();
        }
    }




    public User getLoggedUser() {
        return loggedUser;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
